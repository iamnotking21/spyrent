"use client";

import { useEffect, useState } from "react";
import { savePushSubscriptionAction } from "@/app/actions";

type Status = "checking" | "unsupported" | "off" | "on" | "blocked" | "working";

/** base64url, the form the Push API wants its key in. */
function toUint8Array(base64: string) {
  const padded = (base64 + "=".repeat((4 - (base64.length % 4)) % 4))
    .replace(/-/g, "+")
    .replace(/_/g, "/");
  const raw = atob(padded);
  return Uint8Array.from([...raw].map((c) => c.charCodeAt(0)));
}

export function PushToggle({ vapidKey }: { vapidKey: string }) {
  const [status, setStatus] = useState<Status>("checking");
  const [message, setMessage] = useState<string | null>(null);

  useEffect(() => {
    if (!vapidKey || !("serviceWorker" in navigator) || !("PushManager" in window)) {
      setStatus("unsupported");
      return;
    }
    if (Notification.permission === "denied") {
      setStatus("blocked");
      return;
    }

    navigator.serviceWorker
      .register("/sw.js")
      .then((reg) => reg.pushManager.getSubscription())
      .then((sub) => setStatus(sub ? "on" : "off"))
      .catch(() => setStatus("unsupported"));
  }, [vapidKey]);

  async function enable() {
    setStatus("working");
    setMessage(null);

    try {
      const permission = await Notification.requestPermission();
      if (permission !== "granted") {
        setStatus(permission === "denied" ? "blocked" : "off");
        return;
      }

      const reg = await navigator.serviceWorker.ready;
      const sub =
        (await reg.pushManager.getSubscription()) ??
        (await reg.pushManager.subscribe({
          userVisibleOnly: true,
          applicationServerKey: toUint8Array(vapidKey),
        }));

      const form = new FormData();
      form.set("subscription", JSON.stringify(sub));
      const result = await savePushSubscriptionAction(undefined, form);

      if (result?.error) {
        setStatus("off");
        setMessage(result.error);
        return;
      }

      setStatus("on");
      setMessage("You will hear about requests on this device.");
    } catch {
      setStatus("off");
      setMessage("That did not work. Try again from a normal browser window.");
    }
  }

  if (status === "checking" || status === "unsupported") return null;

  return (
    <div className="card p-5">
      <h3 className="font-bold">Notifications</h3>

      {status === "on" ? (
        <p className="text-sm text-ink-700 mt-1">
          On for this device. You will hear when someone asks for more time.
        </p>
      ) : status === "blocked" ? (
        <p className="text-sm text-ink-700 mt-1">
          Your browser is blocking notifications for this site. Turn them back on in its site
          settings, then reload.
        </p>
      ) : (
        <>
          <p className="text-sm text-ink-500 mt-1 mb-4">
            Get a nudge when a child asks for more time, instead of checking the portal.
          </p>
          <button
            className="btn btn-primary"
            onClick={enable}
            disabled={status === "working"}
          >
            {status === "working" ? "Just a moment…" : "Turn on notifications"}
          </button>
        </>
      )}

      {message ? <p className="text-sm text-brand-700 mt-3">{message}</p> : null}
    </div>
  );
}
