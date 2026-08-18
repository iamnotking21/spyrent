import Link from "next/link";

export default function NotFound() {
  return (
    <main className="min-h-screen grid place-items-center px-6 text-center">
      <div>
        <p className="pill bg-brand-50 text-brand-700">404</p>
        <h1 className="text-3xl font-extrabold mt-4">We can&apos;t find that page</h1>
        <p className="text-ink-700 mt-2">It may have moved, or the link is out of date.</p>
        <Link href="/" className="btn btn-primary mt-6">
          Back to the start
        </Link>
      </div>
    </main>
  );
}
