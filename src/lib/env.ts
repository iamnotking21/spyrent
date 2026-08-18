/**
 * Fail at boot rather than silently signing sessions with a guessable key.
 */
function required(name: string, devFallback?: string) {
  const value = process.env[name];
  if (value) return value;
  if (process.env.NODE_ENV !== "production" && devFallback) return devFallback;
  throw new Error(`${name} is not set — add it to .env (and to the Vercel project settings)`);
}

export const AUTH_SECRET = required(
  "AUTH_SECRET",
  "dev-only-insecure-secret-change-me-please",
);

export const DATABASE_URL = required("DATABASE_URL");
