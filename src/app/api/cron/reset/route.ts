import { sql } from "drizzle-orm";
import { db, rules } from "@/db";

/**
 * Clears yesterday's spent minutes so every daily budget starts fresh.
 *
 * Vercel calls this once a day (see vercel.json). It is also safe to call by
 * hand: rules already reset today are left alone, so a double run is a no-op.
 */
export async function GET(req: Request) {
  // Fails closed. An unprotected reset endpoint is a child's way out of every
  // limit on the device, so a missing secret must not mean "open to all".
  const secret = process.env.CRON_SECRET;
  if (!secret) {
    if (process.env.NODE_ENV === "production") {
      return Response.json(
        { ok: false, error: "CRON_SECRET is not configured" },
        { status: 503 },
      );
    }
  } else if (req.headers.get("authorization") !== `Bearer ${secret}`) {
    return Response.json({ ok: false, error: "unauthorized" }, { status: 401 });
  }

  const today = new Date().toISOString().slice(0, 10);

  const reset = await db
    .update(rules)
    .set({ usedMinutes: 0, resetOn: today })
    .where(sql`${rules.resetOn} is distinct from ${today}`)
    .returning({ id: rules.id });

  return Response.json({ ok: true, resetOn: today, rulesReset: reset.length });
}
