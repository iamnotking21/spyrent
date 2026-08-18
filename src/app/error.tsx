"use client";

export default function Error({ reset }: { error: Error; reset: () => void }) {
  return (
    <main className="min-h-screen grid place-items-center px-6 text-center">
      <div>
        <p className="pill bg-rose-soft text-rose-ink">Something broke</p>
        <h1 className="text-3xl font-extrabold mt-4">That didn&apos;t load</h1>
        <p className="text-ink-700 mt-2">Give it another try — nothing was lost.</p>
        <button onClick={reset} className="btn btn-primary mt-6">
          Try again
        </button>
      </div>
    </main>
  );
}
