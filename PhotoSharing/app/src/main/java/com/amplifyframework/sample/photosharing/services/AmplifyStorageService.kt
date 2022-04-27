// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0
package com.amplifyframework.sample.photosharing.services

import com.amplifyframework.kotlin.storage.KotlinStorageFacade
import com.amplifyframework.kotlin.storage.Storage.InProgressStorageOperation
import com.amplifyframework.storage.StorageAccessLevel
import com.amplifyframework.storage.options.StorageDownloadFileOptions
import com.amplifyframework.storage.options.StorageGetUrlOptions
import com.amplifyframework.storage.options.StorageRemoveOptions
import com.amplifyframework.storage.options.StorageUploadInputStreamOptions
import com.amplifyframework.storage.result.StorageDownloadFileResult
import com.amplifyframework.storage.result.StorageRemoveResult
import com.amplifyframework.storage.result.StorageUploadInputStreamResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.File
import java.io.InputStream
import java.net.URL

@FlowPreview
class AmplifyStorageService : KoinComponent {
    private val amplifyStorage: KotlinStorageFacade = inject<KotlinStorageFacade>().value

    @OptIn(ExperimentalCoroutinesApi::class)
    fun uploadImage(
        key: String,
        data: InputStream
    ): InProgressStorageOperation<StorageUploadInputStreamResult> {
        val options = StorageUploadInputStreamOptions.builder()
            .accessLevel(StorageAccessLevel.PROTECTED)
            .contentType("Image")
            .build()
        return amplifyStorage.uploadInputStream(key, data, options)
    }

    suspend fun getImageDownloadLink(key: String): URL {
        val options = StorageGetUrlOptions.builder()
            .accessLevel(StorageAccessLevel.PROTECTED)
            .build()
        return amplifyStorage.getUrl(key, options).url
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun downloadImage(
        key: String,
        file: File
    ): InProgressStorageOperation<StorageDownloadFileResult> {
        val options = StorageDownloadFileOptions.builder()
            .accessLevel(StorageAccessLevel.PROTECTED)
            .build()
        return amplifyStorage.downloadFile(key, file, options)
    }

    suspend fun removeImage(key: String): StorageRemoveResult {
        val options = StorageRemoveOptions.builder()
            .accessLevel(StorageAccessLevel.PROTECTED)
            .build()
        return amplifyStorage.remove(key, options)
    }
}