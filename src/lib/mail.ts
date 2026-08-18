import "server-only";

/**
 * Sends through Resend when it is configured. There is no free email service
 * that works without an account, so a deployment without a key simply has no
 * outbound mail — the caller must handle that rather than pretend it sent.
 */
export async function sendResetEmail(to: string, link: string): Promise<boolean> {
  const key = process.env.RESEND_API_KEY;
  const from = process.env.RESEND_FROM;
  if (!key || !from) return false;

  const res = await fetch("https://api.resend.com/emails", {
    method: "POST",
    headers: {
      authorization: `Bearer ${key}`,
      "content-type": "application/json",
    },
    body: JSON.stringify({
      from,
      to,
      subject: "Reset your Spyrent password",
      text: [
        "Someone asked to reset the password on your Spyrent account.",
        "",
        `Open this link within the hour to choose a new one: ${link}`,
        "",
        "If that was not you, ignore this — nothing has changed.",
      ].join("\n"),
    }),
  });

  return res.ok;
}

export const mailConfigured = Boolean(process.env.RESEND_API_KEY && process.env.RESEND_FROM);
