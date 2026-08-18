import {
  pgTable,
  serial,
  text,
  integer,
  boolean,
  timestamp,
  uniqueIndex,
  index,
  pgEnum,
} from "drizzle-orm/pg-core";
import { relations } from "drizzle-orm";

export const roleEnum = pgEnum("role", ["admin", "parent"]);
export const ruleKindEnum = pgEnum("rule_kind", ["app", "site"]);
export const eventKindEnum = pgEnum("event_kind", ["app", "site"]);
export const requestStatusEnum = pgEnum("request_status", ["pending", "granted", "denied"]);

/** Parent + admin accounts. */
export const users = pgTable(
  "users",
  {
    id: serial("id").primaryKey(),
    username: text("username").notNull(),
    email: text("email").notNull(),
    firstName: text("first_name").notNull(),
    lastName: text("last_name").notNull(),
    passwordHash: text("password_hash").notNull(),
    role: roleEnum("role").notNull().default("parent"),
    active: boolean("active").notNull().default(true),
    createdAt: timestamp("created_at", { withTimezone: true }).notNull().defaultNow(),
  },
  (t) => [
    uniqueIndex("users_username_idx").on(t.username),
    uniqueIndex("users_email_idx").on(t.email),
  ],
);

/** Child profile; one per monitored device. */
export const children = pgTable(
  "children",
  {
    id: serial("id").primaryKey(),
    parentId: integer("parent_id")
      .notNull()
      .references(() => users.id, { onDelete: "cascade" }),
    name: text("name").notNull(),
    passwordHash: text("password_hash").notNull(),
    deviceToken: text("device_token").notNull(),
    deviceModel: text("device_model"),
    /** IANA zone reported by the device, so midnight means midnight there. */
    timezone: text("timezone").notNull().default("UTC"),
    lastSeenAt: timestamp("last_seen_at", { withTimezone: true }),
    active: boolean("active").notNull().default(true),
    createdAt: timestamp("created_at", { withTimezone: true }).notNull().defaultNow(),
  },
  (t) => [
    uniqueIndex("children_parent_name_idx").on(t.parentId, t.name),
    uniqueIndex("children_token_idx").on(t.deviceToken),
  ],
);

/** Apps discovered on the child device. */
export const apps = pgTable(
  "apps",
  {
    id: serial("id").primaryKey(),
    childId: integer("child_id")
      .notNull()
      .references(() => children.id, { onDelete: "cascade" }),
    packageName: text("package_name").notNull(),
    label: text("label").notNull(),
    sizeBytes: integer("size_bytes"),
    seenAt: timestamp("seen_at", { withTimezone: true }).notNull().defaultNow(),
  },
  (t) => [uniqueIndex("apps_child_pkg_idx").on(t.childId, t.packageName)],
);

/**
 * Block/allow rules. kind=app -> target is package name.
 * kind=site -> target is a domain. dailyMinutes null = hard block.
 */
export const rules = pgTable(
  "rules",
  {
    id: serial("id").primaryKey(),
    childId: integer("child_id")
      .notNull()
      .references(() => children.id, { onDelete: "cascade" }),
    kind: ruleKindEnum("kind").notNull(),
    target: text("target").notNull(),
    label: text("label"),
    dailyMinutes: integer("daily_minutes"),
    usedMinutes: integer("used_minutes").notNull().default(0),
    /** Extra minutes a parent granted for today only; cleared at rollover. */
    bonusMinutes: integer("bonus_minutes").notNull().default(0),
    windowStart: text("window_start"),
    windowEnd: text("window_end"),
    blocked: boolean("blocked").notNull().default(true),
    resetOn: text("reset_on"),
    createdAt: timestamp("created_at", { withTimezone: true }).notNull().defaultNow(),
  },
  (t) => [uniqueIndex("rules_child_kind_target_idx").on(t.childId, t.kind, t.target)],
);

/** Usage log pushed by the child device. */
export const events = pgTable(
  "events",
  {
    id: serial("id").primaryKey(),
    childId: integer("child_id")
      .notNull()
      .references(() => children.id, { onDelete: "cascade" }),
    kind: eventKindEnum("kind").notNull(),
    target: text("target").notNull(),
    label: text("label"),
    minutes: integer("minutes").notNull().default(0),
    blocked: boolean("blocked").notNull().default(false),
    occurredAt: timestamp("occurred_at", { withTimezone: true }).notNull().defaultNow(),
  },
  (t) => [index("events_child_time_idx").on(t.childId, t.occurredAt)],
);

/**
 * Failed sign-in counter. Lives in the database rather than in memory because
 * serverless instances do not share memory — an in-process map would reset on
 * every cold start and throttle nothing.
 */
export const loginAttempts = pgTable(
  "login_attempts",
  {
    id: serial("id").primaryKey(),
    identifier: text("identifier").notNull(),
    attempts: integer("attempts").notNull().default(0),
    windowStart: timestamp("window_start", { withTimezone: true }).notNull().defaultNow(),
    lockedUntil: timestamp("locked_until", { withTimezone: true }),
  },
  (t) => [uniqueIndex("login_attempts_identifier_idx").on(t.identifier)],
);

/**
 * A child asking for more time on an app. The parent answers from the portal;
 * granting adds the minutes straight onto the rule budget.
 */
export const timeRequests = pgTable(
  "time_requests",
  {
    id: serial("id").primaryKey(),
    childId: integer("child_id")
      .notNull()
      .references(() => children.id, { onDelete: "cascade" }),
    ruleId: integer("rule_id").references(() => rules.id, { onDelete: "cascade" }),
    target: text("target").notNull(),
    label: text("label"),
    minutes: integer("minutes").notNull().default(15),
    status: requestStatusEnum("status").notNull().default("pending"),
    answeredAt: timestamp("answered_at", { withTimezone: true }),
    createdAt: timestamp("created_at", { withTimezone: true }).notNull().defaultNow(),
  },
  (t) => [index("time_requests_child_idx").on(t.childId, t.status)],
);

/**
 * One-shot password reset tokens. Only the hash is stored, so a leaked table
 * does not hand over working reset links.
 */
export const passwordResets = pgTable(
  "password_resets",
  {
    id: serial("id").primaryKey(),
    userId: integer("user_id")
      .notNull()
      .references(() => users.id, { onDelete: "cascade" }),
    tokenHash: text("token_hash").notNull(),
    expiresAt: timestamp("expires_at", { withTimezone: true }).notNull(),
    usedAt: timestamp("used_at", { withTimezone: true }),
    createdAt: timestamp("created_at", { withTimezone: true }).notNull().defaultNow(),
  },
  (t) => [uniqueIndex("password_resets_token_idx").on(t.tokenHash)],
);

/** Admin-visible audit trail. */
export const auditLog = pgTable("audit_log", {
  id: serial("id").primaryKey(),
  actorId: integer("actor_id").references(() => users.id, { onDelete: "set null" }),
  action: text("action").notNull(),
  detail: text("detail"),
  createdAt: timestamp("created_at", { withTimezone: true }).notNull().defaultNow(),
});

export const usersRelations = relations(users, ({ many }) => ({
  children: many(children),
}));

export const childrenRelations = relations(children, ({ one, many }) => ({
  parent: one(users, { fields: [children.parentId], references: [users.id] }),
  apps: many(apps),
  rules: many(rules),
  events: many(events),
}));

export type User = typeof users.$inferSelect;
export type Child = typeof children.$inferSelect;
export type Rule = typeof rules.$inferSelect;
export type AppRow = typeof apps.$inferSelect;
export type EventRow = typeof events.$inferSelect;
