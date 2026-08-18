import Link from "next/link";

const features = [
  {
    title: "App limits that stick",
    body: "Give each app a daily budget in minutes. When it runs out, it locks — no arguing with the clock.",
  },
  {
    title: "Safer browsing",
    body: "Block domains per child. Adult content lists come pre-loaded and you can add your own in one line.",
  },
  {
    title: "Honest history",
    body: "See what was opened and for how long. Plain timeline, no dark patterns, no guessing.",
  },
  {
    title: "Built for two hands",
    body: "Set up a child device in under a minute with a pairing code. Change limits from your phone.",
  },
];

const steps = [
  { n: "01", t: "Make a parent account", d: "One email, one password. Free while we are in beta." },
  { n: "02", t: "Add a child", d: "You get a pairing code — type it into the Spyrent app on their device." },
  { n: "03", t: "Set the rules", d: "Pick apps and sites, give them a daily budget, and forget about it." },
];

export default function Home() {
  return (
    <main>
      <header className="max-w-6xl mx-auto px-6 py-6 flex items-center justify-between">
        <Link href="/" className="font-[family-name:var(--font-display)] text-xl font-extrabold tracking-tight">
          spyrent<span className="text-brand-600">.</span>
        </Link>
        <nav className="flex items-center gap-2">
          <Link href="/login" className="btn btn-ghost">
            Sign in
          </Link>
          <Link href="/register" className="btn btn-primary">
            Create account
          </Link>
        </nav>
      </header>

      <section className="max-w-6xl mx-auto px-6 pt-14 pb-20 grid lg:grid-cols-[1.05fr_0.95fr] gap-14 items-center">
        <div>
          <span className="pill bg-brand-50 text-brand-700">Family screen time, minus the drama</span>
          <h1 className="mt-5 text-5xl sm:text-6xl font-extrabold leading-[1.03]">
            Set the limits once.
            <br />
            Get your evening back.
          </h1>
          <p className="mt-5 text-lg text-ink-700 max-w-xl leading-relaxed">
            Spyrent gives each child a fair daily budget for apps and websites, then quietly
            enforces it. You see what happened. They see a clear countdown. Nobody has to
            negotiate at bedtime.
          </p>
          <div className="mt-8 flex flex-wrap gap-3">
            <Link href="/register" className="btn btn-primary">
              Start free
            </Link>
            <Link href="/login" className="btn btn-ghost">
              I already have an account
            </Link>
          </div>
          <p className="mt-4 text-sm text-ink-500">
            Children always see that Spyrent is installed. No hidden tracking, ever.
          </p>
        </div>

        <div className="card p-6">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-sm text-ink-500">Today</p>
              <h3 className="text-2xl font-bold">Mia&apos;s tablet</h3>
            </div>
            <span className="pill bg-brand-50 text-brand-700">online</span>
          </div>
          <div className="mt-6 space-y-4">
            {[
              { app: "YouTube", used: 42, cap: 60, tone: "bg-brand-500" },
              { app: "Roblox", used: 55, cap: 60, tone: "bg-amber-ink" },
              { app: "Khan Academy", used: 18, cap: 120, tone: "bg-brand-500" },
            ].map((r) => (
              <div key={r.app}>
                <div className="flex justify-between text-sm mb-1.5">
                  <span className="font-semibold">{r.app}</span>
                  <span className="text-ink-500">
                    {r.used} / {r.cap} min
                  </span>
                </div>
                <div className="h-2.5 rounded-full bg-line overflow-hidden">
                  <div
                    className={`h-full rounded-full ${r.tone}`}
                    style={{ width: `${(r.used / r.cap) * 100}%` }}
                  />
                </div>
              </div>
            ))}
          </div>
          <div className="mt-6 rounded-xl bg-paper p-4 text-sm text-ink-700">
            Roblox locks in 5 minutes. Mia can ask for more — you approve from your phone.
          </div>
        </div>
      </section>

      <section className="max-w-6xl mx-auto px-6 py-16">
        <h2 className="text-3xl font-extrabold">What you actually get</h2>
        <div className="mt-8 grid sm:grid-cols-2 gap-5">
          {features.map((f) => (
            <div key={f.title} className="card p-6">
              <h3 className="text-lg font-bold">{f.title}</h3>
              <p className="mt-2 text-ink-700 leading-relaxed">{f.body}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="max-w-6xl mx-auto px-6 py-16">
        <h2 className="text-3xl font-extrabold">Three steps, one coffee</h2>
        <div className="mt-8 grid sm:grid-cols-3 gap-5">
          {steps.map((s) => (
            <div key={s.n} className="card p-6">
              <div className="text-brand-600 font-bold font-[family-name:var(--font-display)]">{s.n}</div>
              <h3 className="mt-2 text-lg font-bold">{s.t}</h3>
              <p className="mt-1.5 text-ink-700">{s.d}</p>
            </div>
          ))}
        </div>
      </section>

      <section className="max-w-6xl mx-auto px-6 pb-24">
        <div className="card p-10 text-center">
          <h2 className="text-3xl font-extrabold">Ready when you are</h2>
          <p className="mt-3 text-ink-700 max-w-lg mx-auto">
            Free for families. Set up your first child device in about a minute.
          </p>
          <Link href="/register" className="btn btn-primary mt-6">
            Create your account
          </Link>
        </div>
      </section>

      <footer className="border-t border-line">
        <div className="max-w-6xl mx-auto px-6 py-8 flex flex-wrap gap-4 justify-between text-sm text-ink-500">
          <span>© {new Date().getFullYear()} Spyrent</span>
          <div className="flex gap-5">
            <Link href="/login">Parent portal</Link>
            <Link href="/admin">Admin</Link>
          </div>
        </div>
      </footer>
    </main>
  );
}
