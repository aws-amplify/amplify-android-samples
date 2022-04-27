// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0
package com.amplifyframework.sample.photosharing.viewModels

import androidx.lifecycle.*
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.AuthUser
import com.amplifyframework.datastore.generated.model.Post
import com.amplifyframework.datastore.generated.model.User
import com.amplifyframework.sample.photosharing.services.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.net.URL

class UserProfileViewModel : ViewModel(), KoinComponent {

    @ExperimentalCoroutinesApi
    @FlowPreview
    private val authService: AmplifyAuthService = inject<Repository>().value.authService()

    @ExperimentalCoroutinesApi
    @FlowPreview
    private val datastoreService: AmplifyDatastoreService =
        inject<Repository>().value.dataStoreService()

    @ExperimentalCoroutinesApi
    @FlowPreview
    private val storageService: AmplifyStorageService =
        inject<Repository>().value.storageService()

    var pageOfPosts = 0
    private val mutableTotalNumPostsLiveData: MutableLiveData<Int> = MutableLiveData(0)
    val totalNumPostsLiveData: LiveData<Int> = mutableTotalNumPostsLiveData

    @ExperimentalCoroutinesApi
    @FlowPreview
    val postListViewStateLiveData: LiveData<PostListViewState> =
        Transformations.map(datastoreService.dataStoreSessionLiveData) {
            when (it) {
                is Complete<*> -> {
                    if (totalNumPostsLiveData.value == 0) {
                        setTotalNumberOfPostsInitially()
                    }
                    @Suppress("UNCHECKED_CAST")
                    PostListViewState(data = it.data as List<Post>?, status = AuthStatus.COMPLETE)
                }
                is Error -> PostListViewState(status = AuthStatus.ERROR)
                Loading -> PostListViewState(status = AuthStatus.LOADING)
            }
        }

    @ExperimentalCoroutinesApi
    @FlowPreview
    val userViewStateLiveData: LiveData<ViewState<User, AuthStatus>> =
        Transformations.map(datastoreService.dataStoreSessionLiveData) {
            when (it) {
                is Complete<*> -> ViewState(
                    data = datastoreService.user,
                    state = AuthStatus.COMPLETE
                )
                is Error -> ViewState(state = AuthStatus.ERROR)
                Loading -> ViewState(state = AuthStatus.LOADING)
            }
        }

    @ExperimentalCoroutinesApi
    @FlowPreview
    val viewStateLiveData: LiveData<ViewState<AuthUser?, AuthStatus>> =
        Transformations.map(authService.authSessionStateLiveData) {
            when (it) {
                is Complete<*> -> {
                    if (it.data is SignedIn) {
                        ViewState(
                            data = it.data.authUser,
                            state = AuthStatus.SIGNED_IN
                        )
                    } else {
                        ViewState(state = AuthStatus.SIGNED_OUT)
                    }
                }
                Loading -> ViewState(state = AuthStatus.LOADING)
                is Error -> ViewState(state = AuthStatus.ERROR)
            }
        }

    @ExperimentalCoroutinesApi
    @FlowPreview
    suspend fun signOut() {
        authService.signOut(viewModelScope)
        datastoreService.clear()
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    fun getImageLink(key: String): LiveData<ViewState<URL?, ServiceCallStatus>> {
        val result: MutableLiveData<ViewState<URL?, ServiceCallStatus>> =
            MutableLiveData(ViewState(data = null, state = ServiceCallStatus.LOADING))
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val imageLink = storageService.getImageDownloadLink(key)
                result.postValue(ViewState(data = imageLink, state = ServiceCallStatus.SUCCESS))
            } catch (amplifyException: AmplifyException) {
                result.postValue(ViewState(state = ServiceCallStatus.ERROR))
            }
        }
        return result
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    fun loadMorePosts(currentList: List<Post>): LiveData<ServiceCallStatus> {
        val result: MutableLiveData<ServiceCallStatus> = MutableLiveData(ServiceCallStatus.LOADING)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                pageOfPosts += 1
                datastoreService.loadMorePosts(pageOfPosts, currentList)
                result.postValue(ServiceCallStatus.SUCCESS)
            } catch (amplifyException: AmplifyException) {
                result.postValue(ServiceCallStatus.ERROR)
            }
        }
        return result
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    fun deletePost(post: Post): LiveData<ServiceCallStatus> {
        val result: MutableLiveData<ServiceCallStatus> = MutableLiveData(ServiceCallStatus.LOADING)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Delete the post from DataStore
                datastoreService.deletePost(post.id)
                // Update the number of posts and current page
                pageOfPosts = 0
                mutableTotalNumPostsLiveData.value?.let {
                    val newNumPosts = it - 1
                    mutableTotalNumPostsLiveData.postValue(newNumPosts)
                }
                // Remove the stored post image
                storageService.removeImage(post.pictureKey)
                result.postValue(ServiceCallStatus.SUCCESS)
            } catch (exception: AmplifyException) {
                result.postValue(ServiceCallStatus.ERROR)
            }
        }
        return result
    }

    fun updatePostInfoAfterSave() {
        pageOfPosts = 0
        mutableTotalNumPostsLiveData.value?.let {
            val newNumPosts = it + 1
            mutableTotalNumPostsLiveData.postValue(newNumPosts)
        }
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    private fun setTotalNumberOfPostsInitially() {
        viewModelScope.launch(Dispatchers.IO) {
            mutableTotalNumPostsLiveData.postValue(datastoreService.getTotalNumberOfPosts())
        }
    }
}

class PostListViewState(val data: List<Post>? = null, val status: AuthStatus)

enum class AuthStatus {
    COMPLETE,
    SIGNED_IN,
    SIGNED_OUT,
    LOADING,
    ERROR
}

enum class SignUpStatus {
    LOADING,
    SEND_CODE_COMPLETE,
    SEND_CODE_IN_PROGRESS,
    SIGN_UP_COMPLETE,
    SIGN_UP_IN_PROGRESS,
    ERROR
}

enum class ServiceCallStatus {
    SUCCESS,
    LOADING,
    ERROR
}