import { neon } from "@neondatabase/serverless";
import { drizzle } from "drizzle-orm/neon-http";
import * as schema from "./schema";

import { DATABASE_URL } from "@/lib/env";

export const db = drizzle(neon(DATABASE_URL), { schema });
export * from "./schema";
