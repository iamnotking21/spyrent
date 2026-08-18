import { Shell } from "@/components/shell";
import { requireAdmin } from "@/lib/guard";

export default async function AdminLayout({ children }: { children: React.ReactNode }) {
  const s = await requireAdmin();
  return (
    <Shell
      who={`${s.name} · admin`}
      nav={[
        { href: "/admin", label: "Overview" },
        { href: "/admin/users", label: "Accounts" },
        { href: "/admin/children", label: "Children" },
        { href: "/admin/audit", label: "Audit" },
        { href: "/portal", label: "Parent view" },
      ]}
    >
      {children}
    </Shell>
  );
}

export const metadata = { title: "Admin · Spyrent" };
