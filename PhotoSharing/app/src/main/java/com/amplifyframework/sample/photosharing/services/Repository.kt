// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0
package com.amplifyframework.sample.photosharing.services

import kotlinx.coroutines.FlowPreview
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class Repository : KoinComponent {

    @FlowPreview
    fun authService(): AmplifyAuthService {
        return inject<AmplifyAuthService>().value
    }

    @FlowPreview
    fun dataStoreService(): AmplifyDatastoreService {
        return inject<AmplifyDatastoreService>().value
    }

    @FlowPreview
    fun storageService(): AmplifyStorageService {
        return inject<AmplifyStorageService>().value
    }
}