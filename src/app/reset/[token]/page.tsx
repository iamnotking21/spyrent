import Link from "next/link";
import { resetPasswordAction } from "@/app/actions";
import { ActionForm, Field } from "@/components/forms";

export const metadata = { title: "Choose a new password · Spyrent" };

export default async function ResetPage({ params }: { params: Promise<{ token: string }> }) {
  const { token } = await params;

  return (
    <main className="min-h-screen grid place-items-center px-6 py-16">
      <div className="w-full max-w-sm">
        <Link href="/" className="font-[family-name:var(--font-display)] text-xl font-extrabold">
          spyrent<span className="text-brand-600">.</span>
        </Link>
        <div className="card p-7 mt-6">
          <h1 className="text-2xl font-bold">Choose a new password</h1>
          <p className="text-sm text-ink-500 mt-1 mb-6">
            This link works once, and only for the next hour.
          </p>
          <ActionForm action={resetPasswordAction} submitLabel="Save new password">
            <input type="hidden" name="token" value={token} />
            <Field label="New password" name="password" type="password" placeholder="8+ characters" />
            <Field label="Type it again" name="confirm" type="password" placeholder="8+ characters" />
          </ActionForm>
        </div>
      </div>
    </main>
  );
}
