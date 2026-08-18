import Link from "next/link";
import { redirect } from "next/navigation";
import { getSession } from "@/lib/auth";
import { loginAction } from "@/app/actions";
import { ActionForm, Field } from "@/components/forms";

export default async function LoginPage() {
  const s = await getSession();
  if (s) redirect(s.role === "admin" ? "/admin" : "/portal");

  return (
    <main className="min-h-screen grid place-items-center px-6 py-16">
      <div className="w-full max-w-sm">
        <Link href="/" className="font-[family-name:var(--font-display)] text-xl font-extrabold">
          spyrent<span className="text-brand-600">.</span>
        </Link>
        <div className="card p-7 mt-6">
          <h1 className="text-2xl font-bold">Welcome back</h1>
          <p className="text-sm text-ink-500 mt-1 mb-6">Sign in to your family dashboard.</p>
          <ActionForm action={loginAction} submitLabel="Sign in">
            <Field label="Username" name="username" placeholder="mariacruz" />
            <Field label="Password" name="password" type="password" placeholder="••••••••" />
          </ActionForm>
        </div>
        <p className="text-sm text-ink-500 mt-5 text-center">
          No account yet?{" "}
          <Link href="/register" className="text-brand-700 font-semibold">
            Create one
          </Link>
        </p>
      </div>
    </main>
  );
}
