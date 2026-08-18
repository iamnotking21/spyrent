---
name: spyrent-android
description: Bridge the legacy Android apps in study/ (ChildApp, SpyrentV1) onto the new /api/v1 endpoints. Use when porting a legacy PHP call or reading old behaviour.
tools: Read, Grep, Glob
model: sonnet
---

Legacy backend was `http://spyrent.online/res_api/*.php` called with Volley `StringRequest` + `params.put`.

Mapping (old -> new):
- post_child_register.php -> parent portal "Add child" + `POST /api/v1/pair`
- select_all_child_account.php -> `GET /api/v1/policy` (child scope only)
- post_installed_apps.php, del_installed_apps.php -> `POST /api/v1/apps` (upsert, no delete pass)
- select_all_installed_apps.php, select_all_null_timer_app.php, select_all_have_timer.php -> `GET /api/v1/apps`
- post_history_apps.php, post_history_site.php -> `POST /api/v1/events`
- select_all_domain*.php, update_website.php -> `GET /api/v1/policy` `.sites`
- del_have_timer.php, del_post_walang_timer.php, del_parentid.php -> portal rule delete, no device endpoint

Legacy fields: `oras`/`mins` = hours/minutes budget -> single `dailyMinutes`. `eventstatus` -> `events.blocked`. `packname` -> `packageName`.

Read-only agent. Report the mapping and the exact JSON body the device should send.
