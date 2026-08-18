package com.spyrent.child

import android.content.Context

/**
 * Pulls the latest rules and caches what the blocker needs to act on them
 * offline. Shared by SyncWorker's fifteen-minute background pass and
 * BlockerService's much shorter poll, so a new block reaches the device
 * within seconds of the parent saving it rather than at the next slow sync.
 */
object PolicySync {

    suspend fun refresh(context: Context, api: Api, store: Store) {
        val policy = api.fetchPolicy()
        store.childName = policy.childName
        store.saveLockedPackages(policy.apps.filter { it.locked }.map { it.packageName }.toSet())
        store.saveBlockedDomains(policy.sites.filter { it.blocked }.map { it.domain }.toSet())

        // Time-limited sites get their countdown from the server, but this runs
        // far more often (~20s, from BlockerService) than usage gets reported
        // back (~60s, from SiteBlockerService's own flush). Taken naively, that
        // would make an almost-exhausted site jump back up to nearly-full every
        // time this fires, because the server has not heard about the local
        // browsing yet. So a fresher-but-*higher* server number never overrides
        // an already-lower local one — except once the local count has actually
        // hit zero, where a higher number means either a new day or a parent's
        // grant, and should win immediately.
        val existing = store.siteBudgets()
        val budgets = policy.sites
            .filter { !it.blocked && it.dailyMinutes != null }
            .associate { site ->
                val fromServer = (site.remainingMinutes ?: 0) * 60
                val cached = existing[site.domain]
                val seconds = if (cached != null && cached > 0 && fromServer > cached) cached else fromServer
                site.domain to seconds
            }
        store.saveSiteBudgets(budgets)
    }
}
