// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0
package com.amplifyframework.sample.photosharing.viewModels

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry

class TestLifecycleOwner : LifecycleOwner {
    private val lifecycleRegistry = init()
    private fun init(): LifecycleRegistry {
        val registry = LifecycleRegistry(this)
        registry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
        return registry
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }
}