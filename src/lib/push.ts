import webpush from "web-push";
import { eq } from "drizzle-orm";
import { db, pushSubscriptions } from "../db/index";

const publicKey = process.env.NEXT_PUBLIC_VAPID_PUBLIC_KEY;
const privateKey = process.env.VAPID_PRIVATE_KEY;

export const pushConfigured = Boolean(publicKey && privateKey);

if (pushConfigured) {
  webpush.setVapidDetails(
    process.env.VAPID_SUBJECT ?? "mailto:admin@example.com",
    publicKey!,
    privateKey!,
  );
}

export type PushMessage = { title: string; body: string; url?: string };

export type Subscription = { endpoint: string; keys: { p256dh: string; auth: string } };

/** Injectable so tests can drive delivery without standing up a TLS endpoint. */
export type Sender = (sub: Subscription, payload: string) => Promise<unknown>;

const defaultSender: Sender = (sub, payload) => webpush.sendNotification(sub, payload);

/**
 * Notify every browser this parent has subscribed.
 *
 * Subscriptions expire on their own — a browser that has been cleared or a
 * device that is gone answers 404 or 410, and those rows are dropped rather
 * than retried forever.
 */
export async function notifyUser(
  userId: number,
  message: PushMessage,
  send: Sender = defaultSender,
): Promise<number> {
  if (!pushConfigured && send === defaultSender) return 0;

  const subs = await db
    .select()
    .from(pushSubscriptions)
    .where(eq(pushSubscriptions.userId, userId));

  let delivered = 0;

  await Promise.all(
    subs.map(async (sub) => {
      try {
        await send(
          { endpoint: sub.endpoint, keys: { p256dh: sub.p256dh, auth: sub.auth } },
          JSON.stringify(message),
        );
        delivered++;
      } catch (err) {
        const status = (err as { statusCode?: number }).statusCode;
        if (status === 404 || status === 410) {
          await db.delete(pushSubscriptions).where(eq(pushSubscriptions.id, sub.id));
        }
      }
    }),
  );

  return delivered;
}
