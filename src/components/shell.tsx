import Link from "next/link";
import { logoutAction } from "@/app/actions";

export function Shell({
  nav,
  who,
  children,
}: {
  nav: Array<{ href: string; label: string }>;
  who: string;
  children: React.ReactNode;
}) {
  return (
    <div className="min-h-screen">
      <header className="border-b border-line bg-surface">
        <div className="max-w-6xl mx-auto px-6 h-16 flex items-center gap-6">
          <Link href="/" className="font-[family-name:var(--font-display)] text-lg font-extrabold">
            spyrent<span className="text-brand-600">.</span>
          </Link>
          <nav className="flex items-center gap-1 text-sm">
            {nav.map((n) => (
              <Link
                key={n.href}
                href={n.href}
                className="px-3 py-2 rounded-lg font-medium text-ink-700 hover:bg-brand-50 hover:text-brand-700"
              >
                {n.label}
              </Link>
            ))}
          </nav>
          <div className="ml-auto flex items-center gap-3">
            <span className="text-sm text-ink-500 hidden sm:block">{who}</span>
            <form action={logoutAction}>
              <button className="btn btn-ghost text-sm py-1.5">Sign out</button>
            </form>
          </div>
        </div>
      </header>
      <main className="max-w-6xl mx-auto px-6 py-10">{children}</main>
    </div>
  );
}
