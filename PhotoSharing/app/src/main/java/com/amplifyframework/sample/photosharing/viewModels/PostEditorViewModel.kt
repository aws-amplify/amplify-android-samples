// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0
package com.amplifyframework.sample.photosharing.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amplifyframework.AmplifyException
import com.amplifyframework.core.model.temporal.Temporal
import com.amplifyframework.datastore.generated.model.Post
import com.amplifyframework.sample.photosharing.services.AmplifyDatastoreService
import com.amplifyframework.sample.photosharing.services.AmplifyStorageService
import com.amplifyframework.sample.photosharing.services.Repository
import kotlinx.coroutines.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.InputStream
import java.util.*

class PostEditorViewModel : ViewModel(), KoinComponent {
    @ExperimentalCoroutinesApi
    @FlowPreview
    private val datastoreService: AmplifyDatastoreService =
        inject<Repository>().value.dataStoreService()

    @ExperimentalCoroutinesApi
    @FlowPreview
    private val storageService: AmplifyStorageService = inject<Repository>().value.storageService()
    private val mutableUploadProgressLiveData: MutableLiveData<UploadProgressViewState> =
        MutableLiveData(UploadProgressViewState(uploadProgress = 0.0))
    val uploadProgressLiveData: LiveData<UploadProgressViewState> = mutableUploadProgressLiveData

    @ExperimentalCoroutinesApi
    @FlowPreview
    fun createPost(postBody: String, selectedImageInputStream: InputStream) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = datastoreService.user
            var post = Post.Builder()
                .postBody(postBody)
                .pictureKey("")
                .createdAt(Temporal.DateTime(Date(), 0))
                .postedBy(user)
                .build()
            val pictureKey = user?.username + "_image" + post.id
            post = post.copyOfBuilder()
                .pictureKey(pictureKey).build()
            try {
                // Upload picture to S3
                val upload = storageService.uploadImage(post.pictureKey, selectedImageInputStream)

                // Get upload progress -> indicate in progress bar
                val progressJob = async {
                    upload.progress().collect {
                        mutableUploadProgressLiveData.postValue(
                            UploadProgressViewState(uploadProgress = it.fractionCompleted)
                        )
                    }
                }

                // Try to get the upload result
                upload.result()
                progressJob.cancel()

                // Save post to datastore
                datastoreService.savePost(post)
            } catch (amplifyException: AmplifyException) {
                mutableUploadProgressLiveData.postValue(
                    UploadProgressViewState(uploadProgress = 0.0, uploadError = amplifyException)
                )
            }
        }
    }
}