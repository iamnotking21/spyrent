"use client";

import { useActionState } from "react";
import { issueResetLinkAction } from "@/app/actions";

type State = { error?: string; notice?: string } | undefined;

/** Admin-side: mint a one-time reset link and show it once. */
export function ResetLinkButton({ email }: { email: string }) {
  const [state, action, pending] = useActionState<State, FormData>(
    issueResetLinkAction,
    undefined,
  );

  return (
    <div>
      <form action={action}>
        <input type="hidden" name="email" value={email} />
        <button className="text-ink-500 hover:text-brand-700 font-medium" disabled={pending}>
          {pending ? "Working…" : "Reset link"}
        </button>
      </form>
      {state?.notice ? (
        <p className="mt-1 text-xs text-brand-700 break-all max-w-xs">{state.notice}</p>
      ) : null}
      {state?.error ? <p className="mt-1 text-xs text-rose-ink">{state.error}</p> : null}
    </div>
  );
}
