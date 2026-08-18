import Link from "next/link";
import { cx } from "@/lib/utils";

export function Card({
  children,
  className,
}: {
  children: React.ReactNode;
  className?: string;
}) {
  return <div className={cx("card p-5", className)}>{children}</div>;
}

export function SectionTitle({ title, hint }: { title: string; hint?: string }) {
  return (
    <div className="mb-4">
      <h2 className="text-lg font-bold">{title}</h2>
      {hint ? <p className="text-sm text-ink-500 mt-0.5">{hint}</p> : null}
    </div>
  );
}

export function Stat({ label, value, tone = "brand" }: { label: string; value: string; tone?: "brand" | "amber" | "rose" }) {
  const tones = {
    brand: "bg-brand-50 text-brand-700",
    amber: "bg-amber-soft text-amber-ink",
    rose: "bg-rose-soft text-rose-ink",
  } as const;
  return (
    <div className="card p-5">
      <div className={cx("pill mb-3", tones[tone])}>{label}</div>
      <div className="text-3xl font-bold font-[family-name:var(--font-display)]">{value}</div>
    </div>
  );
}

export function Badge({ children, tone = "brand" }: { children: React.ReactNode; tone?: "brand" | "amber" | "rose" | "muted" }) {
  const tones = {
    brand: "bg-brand-50 text-brand-700",
    amber: "bg-amber-soft text-amber-ink",
    rose: "bg-rose-soft text-rose-ink",
    muted: "bg-paper text-ink-500",
  } as const;
  return <span className={cx("pill", tones[tone])}>{children}</span>;
}

export function Empty({ title, hint, action }: { title: string; hint: string; action?: { href: string; label: string } }) {
  return (
    <div className="card p-10 text-center">
      <h3 className="text-base font-bold">{title}</h3>
      <p className="text-sm text-ink-500 mt-1 max-w-sm mx-auto">{hint}</p>
      {action ? (
        <Link href={action.href} className="btn btn-primary mt-5">
          {action.label}
        </Link>
      ) : null}
    </div>
  );
}
