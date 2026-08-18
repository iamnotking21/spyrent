import { rolloverAll } from "@/lib/budget";

/**
 * Nightly backstop for the budget rollover.
 *
 * The device rolls its own budgets over when it fetches its policy, so this
 * only matters for devices that were switched off. Each child rolls over on
 * their own local date, not on UTC midnight.
 */
export async function GET(req: Request) {
  // Fails closed. An unprotected reset endpoint is a child's way out of every
  // limit on the device, so a missing secret must not mean "open to all".
  const secret = process.env.CRON_SECRET;
  if (!secret) {
    if (process.env.NODE_ENV === "production") {
      return Response.json({ ok: false, error: "CRON_SECRET is not configured" }, { status: 503 });
    }
  } else if (req.headers.get("authorization") !== `Bearer ${secret}`) {
    return Response.json({ ok: false, error: "unauthorized" }, { status: 401 });
  }

  const rulesReset = await rolloverAll();
  return Response.json({ ok: true, rulesReset });
}
