"use client";

import { useActionState } from "react";
import { useFormStatus } from "react-dom";

type State = { error?: string; notice?: string } | undefined;
type Action = (state: State, form: FormData) => Promise<State>;

function Submit({ label }: { label: string }) {
  const { pending } = useFormStatus();
  return (
    <button type="submit" className="btn btn-primary w-full" disabled={pending}>
      {pending ? "Working…" : label}
    </button>
  );
}

export function ActionForm({
  action,
  submitLabel,
  children,
  className,
}: {
  action: Action;
  submitLabel: string;
  children: React.ReactNode;
  className?: string;
}) {
  const [state, formAction] = useActionState<State, FormData>(action, undefined);
  return (
    <form action={formAction} className={className ?? "space-y-4"}>
      {children}
      {state?.notice ? (
        <p className="text-sm font-medium text-brand-700 bg-brand-50 rounded-xl px-3 py-2 break-all">
          {state.notice}
        </p>
      ) : null}
      {state?.error ? (
        <p className="text-sm font-medium text-rose-ink bg-rose-soft rounded-xl px-3 py-2">
          {state.error}
        </p>
      ) : null}
      <Submit label={submitLabel} />
    </form>
  );
}

export function Field({
  label,
  name,
  type = "text",
  placeholder,
  required = true,
  defaultValue,
}: {
  label: string;
  name: string;
  type?: string;
  placeholder?: string;
  required?: boolean;
  defaultValue?: string | number;
}) {
  return (
    <div>
      <label className="label" htmlFor={name}>
        {label}
      </label>
      <input
        id={name}
        name={name}
        type={type}
        className="input"
        placeholder={placeholder}
        required={required}
        defaultValue={defaultValue}
      />
    </div>
  );
}
