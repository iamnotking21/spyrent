import Link from "next/link";
import { requestResetAction } from "@/app/actions";
import { ActionForm, Field } from "@/components/forms";

export const metadata = { title: "Forgot password · Spyrent" };

export default function ForgotPage() {
  return (
    <main className="min-h-screen grid place-items-center px-6 py-16">
      <div className="w-full max-w-sm">
        <Link href="/" className="font-[family-name:var(--font-display)] text-xl font-extrabold">
          spyrent<span className="text-brand-600">.</span>
        </Link>
        <div className="card p-7 mt-6">
          <h1 className="text-2xl font-bold">Forgot your password?</h1>
          <p className="text-sm text-ink-500 mt-1 mb-6">
            Type the email you signed up with and we will sort you out.
          </p>
          <ActionForm action={requestResetAction} submitLabel="Send reset link">
            <Field label="Email" name="email" type="email" placeholder="maria@example.com" />
          </ActionForm>
        </div>
        <p className="text-sm text-ink-500 mt-5 text-center">
          <Link href="/login" className="text-brand-700 font-semibold">
            Back to sign in
          </Link>
        </p>
      </div>
    </main>
  );
}
