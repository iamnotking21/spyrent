import { db, auditLog } from "@/db";

/**
 * Record what someone did. Never throws: an audit write failing must not take
 * the action itself down with it.
 */
export async function record(entry: {
  actorId: number;
  childId?: number | null;
  action: string;
  detail?: string;
}): Promise<void> {
  try {
    await db.insert(auditLog).values({
      actorId: entry.actorId,
      childId: entry.childId ?? null,
      action: entry.action,
      detail: entry.detail ?? null,
    });
  } catch {
    // deliberately swallowed
  }
}

/** Turn a stored row into something a parent can read. */
export function describe(action: string, detail: string | null): string {
  const what = detail ?? "";
  switch (action) {
    case "rule.set":
      return `Set a limit on ${what}`;
    case "rule.block":
      return `Blocked ${what}`;
    case "rule.remove":
      return `Removed the rule for ${what}`;
    case "request.granted":
      return `Granted more time on ${what}`;
    case "request.denied":
      return `Said not now to ${what}`;
    case "child.added":
      return `Added ${what}`;
    case "child.removed":
      return `Removed ${what}`;
    case "child.token_rotated":
      return "Generated a new pairing code";
    case "user.enable":
      return `Enabled ${what}`;
    case "user.disable":
      return `Disabled ${what}`;
    case "user.reset_link":
      return `Issued a password reset link for ${what}`;
    default:
      return `${action} ${what}`.trim();
  }
}
