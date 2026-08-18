import { Shell } from "@/components/shell";
import { requireUser } from "@/lib/guard";

export default async function PortalLayout({ children }: { children: React.ReactNode }) {
  const s = await requireUser();
  return (
    <Shell
      who={s.name}
      nav={[
        { href: "/portal", label: "Overview" },
        { href: "/portal/activity", label: "Activity" },
        ...(s.role === "admin" ? [{ href: "/admin", label: "Admin" }] : []),
      ]}
    >
      {children}
    </Shell>
  );
}

export const metadata = { title: "Parent portal · Spyrent" };
