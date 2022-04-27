// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0
package com.amplifyframework.sample.photosharing.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amplifyframework.AmplifyException
import com.amplifyframework.sample.photosharing.services.AmplifyDatastoreService
import com.amplifyframework.sample.photosharing.services.AmplifyStorageService
import com.amplifyframework.sample.photosharing.services.Repository
import kotlinx.coroutines.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.io.InputStream

class ConfirmImageViewModel : ViewModel(), KoinComponent {

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
    fun updateProfileImage(selectedImageInputStream: InputStream): String {
        var pictureKey = ""
        viewModelScope.launch(Dispatchers.IO) {
            val user = datastoreService.user
            if (user == null) {
                mutableUploadProgressLiveData.postValue(
                    UploadProgressViewState(
                        uploadProgress = 0.0,
                        uploadError = AmplifyException(
                            "User can not be null",
                            "Log out and Log back in"
                        )
                    )
                )
            }
            pictureKey = user?.username + "ProfileImage"
            try {
                val upload = storageService.uploadImage(pictureKey, selectedImageInputStream)

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

                datastoreService.editProfileImage(user?.id as String, pictureKey)

            } catch (amplifyException: AmplifyException) {
                mutableUploadProgressLiveData.postValue(
                    UploadProgressViewState(uploadProgress = 0.0, uploadError = amplifyException)
                )
            }
        }
        return pictureKey
    }
}

class UploadProgressViewState(val uploadProgress: Double, val uploadError: AmplifyException? = null)