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
    }
}
