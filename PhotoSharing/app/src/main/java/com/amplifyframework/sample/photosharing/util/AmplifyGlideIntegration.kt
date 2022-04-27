// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0
package com.amplifyframework.sample.photosharing.util

import android.content.Context
import com.amplifyframework.core.Amplify
import com.amplifyframework.storage.StorageAccessLevel
import com.amplifyframework.storage.operation.StorageDownloadFileOperation
import com.amplifyframework.storage.options.StorageDownloadFileOptions
import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.Options
import com.bumptech.glide.load.data.DataFetcher
import com.bumptech.glide.load.model.ModelLoader
import com.bumptech.glide.load.model.ModelLoader.LoadData
import com.bumptech.glide.load.model.ModelLoaderFactory
import com.bumptech.glide.load.model.MultiModelLoaderFactory
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.signature.ObjectKey
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream

/**
 * Amplify + Glide integration for image rendering.
 */
data class AmplifyImageModel(
    val context: Context,
    val key: String,
    val accessLevel: StorageAccessLevel = StorageAccessLevel.PUBLIC
) {

    val cacheKey: String
        get() = "${accessLevel.name.lowercase()}_${key}"
}

class AmplifyStorageModelLoader : ModelLoader<AmplifyImageModel, InputStream> {

    override fun buildLoadData(
        model: AmplifyImageModel,
        width: Int,
        height: Int,
        options: Options
    ): LoadData<InputStream> {
        return LoadData(ObjectKey(model.cacheKey), AmplifyStorageDataFetcher(model))
    }

    override fun handles(model: AmplifyImageModel): Boolean {
        return true
    }
}

class AmplifyStorageDataFetcher(
    private val model: AmplifyImageModel
) : DataFetcher<InputStream> {

    private lateinit var downloadOperation: StorageDownloadFileOperation<*>

    private val tempFile: File
        get() = File(model.context.cacheDir, model.cacheKey)

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in InputStream>) {
        val options = StorageDownloadFileOptions
            .builder()
            .accessLevel(model.accessLevel)
            .build()

        val file = tempFile
        file.delete()
        if (!file.createNewFile()) {
            callback.onLoadFailed(
                FileNotFoundException("Could not create temp file for image loading: ${file.path}")
            )
            return
        }

        this.downloadOperation = Amplify.Storage.downloadFile(
            model.key,
            tempFile,
            options,
            { callback.onDataReady(it.file.inputStream()) },
            { callback.onLoadFailed(it) }
        )
    }

    override fun cleanup() {
        if (tempFile.exists()) {
            tempFile.delete()
        }
    }

    override fun cancel() {
        if (this::downloadOperation.isInitialized) {
            downloadOperation.cancel()
        }
    }

    override fun getDataClass(): Class<InputStream> {
        return InputStream::class.java
    }

    override fun getDataSource(): DataSource {
        return DataSource.REMOTE
    }
}

object AmplifyGlideModelLoaderFactory : ModelLoaderFactory<AmplifyImageModel, InputStream> {
    override fun build(multiFactory: MultiModelLoaderFactory): ModelLoader<AmplifyImageModel, InputStream> {
        return AmplifyStorageModelLoader()
    }

    override fun teardown() {
        // do nothing
    }

}

@GlideModule
class AmplifyGlideModule : AppGlideModule() {
    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        registry.append(
            AmplifyImageModel::class.java,
            InputStream::class.java,
            AmplifyGlideModelLoaderFactory
        )
    }
}
