import Link from "next/link";
import { redirect } from "next/navigation";
import { getSession } from "@/lib/auth";
import { registerAction } from "@/app/actions";
import { ActionForm, Field } from "@/components/forms";

export default async function RegisterPage() {
  const s = await getSession();
  if (s) redirect(s.role === "admin" ? "/admin" : "/portal");

  return (
    <main className="min-h-screen grid place-items-center px-6 py-16">
      <div className="w-full max-w-md">
        <Link href="/" className="font-[family-name:var(--font-display)] text-xl font-extrabold">
          spyrent<span className="text-brand-600">.</span>
        </Link>
        <div className="card p-7 mt-6">
          <h1 className="text-2xl font-bold">Create your parent account</h1>
          <p className="text-sm text-ink-500 mt-1 mb-6">Free while we are in beta.</p>
          <ActionForm action={registerAction} submitLabel="Create account">
            <div className="grid grid-cols-2 gap-3">
              <Field label="First name" name="firstName" placeholder="Maria" />
              <Field label="Last name" name="lastName" placeholder="Cruz" />
            </div>
            <Field label="Username" name="username" placeholder="mariacruz" />
            <Field label="Email" name="email" type="email" placeholder="maria@example.com" />
            <Field label="Password" name="password" type="password" placeholder="8+ characters" />
          </ActionForm>
        </div>
        <p className="text-sm text-ink-500 mt-5 text-center">
          Already registered?{" "}
          <Link href="/login" className="text-brand-700 font-semibold">
            Sign in
          </Link>
        </p>
      </div>
    </main>
  );
}
