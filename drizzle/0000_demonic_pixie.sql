CREATE TYPE "public"."event_kind" AS ENUM('app', 'site');--> statement-breakpoint
CREATE TYPE "public"."role" AS ENUM('admin', 'parent');--> statement-breakpoint
CREATE TYPE "public"."rule_kind" AS ENUM('app', 'site');--> statement-breakpoint
CREATE TABLE "apps" (
	"id" serial PRIMARY KEY NOT NULL,
	"child_id" integer NOT NULL,
	"package_name" text NOT NULL,
	"label" text NOT NULL,
	"size_bytes" integer,
	"seen_at" timestamp with time zone DEFAULT now() NOT NULL
);
--> statement-breakpoint
CREATE TABLE "audit_log" (
	"id" serial PRIMARY KEY NOT NULL,
	"actor_id" integer,
	"action" text NOT NULL,
	"detail" text,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL
);
--> statement-breakpoint
CREATE TABLE "children" (
	"id" serial PRIMARY KEY NOT NULL,
	"parent_id" integer NOT NULL,
	"name" text NOT NULL,
	"password_hash" text NOT NULL,
	"device_token" text NOT NULL,
	"device_model" text,
	"last_seen_at" timestamp with time zone,
	"active" boolean DEFAULT true NOT NULL,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL
);
--> statement-breakpoint
CREATE TABLE "events" (
	"id" serial PRIMARY KEY NOT NULL,
	"child_id" integer NOT NULL,
	"kind" "event_kind" NOT NULL,
	"target" text NOT NULL,
	"label" text,
	"minutes" integer DEFAULT 0 NOT NULL,
	"blocked" boolean DEFAULT false NOT NULL,
	"occurred_at" timestamp with time zone DEFAULT now() NOT NULL
);
--> statement-breakpoint
CREATE TABLE "rules" (
	"id" serial PRIMARY KEY NOT NULL,
	"child_id" integer NOT NULL,
	"kind" "rule_kind" NOT NULL,
	"target" text NOT NULL,
	"label" text,
	"daily_minutes" integer,
	"used_minutes" integer DEFAULT 0 NOT NULL,
	"window_start" text,
	"window_end" text,
	"blocked" boolean DEFAULT true NOT NULL,
	"reset_on" text,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL
);
--> statement-breakpoint
CREATE TABLE "users" (
	"id" serial PRIMARY KEY NOT NULL,
	"username" text NOT NULL,
	"email" text NOT NULL,
	"first_name" text NOT NULL,
	"last_name" text NOT NULL,
	"password_hash" text NOT NULL,
	"role" "role" DEFAULT 'parent' NOT NULL,
	"active" boolean DEFAULT true NOT NULL,
	"created_at" timestamp with time zone DEFAULT now() NOT NULL
);
--> statement-breakpoint
ALTER TABLE "apps" ADD CONSTRAINT "apps_child_id_children_id_fk" FOREIGN KEY ("child_id") REFERENCES "public"."children"("id") ON DELETE cascade ON UPDATE no action;--> statement-breakpoint
ALTER TABLE "audit_log" ADD CONSTRAINT "audit_log_actor_id_users_id_fk" FOREIGN KEY ("actor_id") REFERENCES "public"."users"("id") ON DELETE set null ON UPDATE no action;--> statement-breakpoint
ALTER TABLE "children" ADD CONSTRAINT "children_parent_id_users_id_fk" FOREIGN KEY ("parent_id") REFERENCES "public"."users"("id") ON DELETE cascade ON UPDATE no action;--> statement-breakpoint
ALTER TABLE "events" ADD CONSTRAINT "events_child_id_children_id_fk" FOREIGN KEY ("child_id") REFERENCES "public"."children"("id") ON DELETE cascade ON UPDATE no action;--> statement-breakpoint
ALTER TABLE "rules" ADD CONSTRAINT "rules_child_id_children_id_fk" FOREIGN KEY ("child_id") REFERENCES "public"."children"("id") ON DELETE cascade ON UPDATE no action;--> statement-breakpoint
CREATE UNIQUE INDEX "apps_child_pkg_idx" ON "apps" USING btree ("child_id","package_name");--> statement-breakpoint
CREATE UNIQUE INDEX "children_parent_name_idx" ON "children" USING btree ("parent_id","name");--> statement-breakpoint
CREATE UNIQUE INDEX "children_token_idx" ON "children" USING btree ("device_token");--> statement-breakpoint
CREATE INDEX "events_child_time_idx" ON "events" USING btree ("child_id","occurred_at");--> statement-breakpoint
CREATE UNIQUE INDEX "rules_child_kind_target_idx" ON "rules" USING btree ("child_id","kind","target");--> statement-breakpoint
CREATE UNIQUE INDEX "users_username_idx" ON "users" USING btree ("username");--> statement-breakpoint
CREATE UNIQUE INDEX "users_email_idx" ON "users" USING btree ("email");