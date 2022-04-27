// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0
package com.amplifyframework.sample.photosharing.services

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.amplifyframework.auth.AuthUser
import com.amplifyframework.core.model.Model
import com.amplifyframework.core.model.query.Page
import com.amplifyframework.core.model.query.QueryOptions
import com.amplifyframework.core.model.query.Where
import com.amplifyframework.core.model.query.predicate.QueryPredicate
import com.amplifyframework.core.model.query.predicate.QueryPredicates
import com.amplifyframework.datastore.DataStoreChannelEventName
import com.amplifyframework.datastore.DataStoreException
import com.amplifyframework.datastore.generated.model.Post
import com.amplifyframework.datastore.generated.model.User
import com.amplifyframework.hub.HubChannel
import com.amplifyframework.kotlin.datastore.KotlinDataStoreFacade
import com.amplifyframework.kotlin.hub.KotlinHubFacade
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.reflect.KClass

@OptIn(DelicateCoroutinesApi::class)
@FlowPreview
class AmplifyDatastoreService : KoinComponent {
    @FlowPreview
    private val authService: AmplifyAuthService = inject<Repository>().value.authService()
    private val amplifyDataStore: KotlinDataStoreFacade = inject<KotlinDataStoreFacade>().value
    private val amplifyHub: KotlinHubFacade = inject<KotlinHubFacade>().value
    private var authUser: AuthUser? = null
    private val postListMutableLiveData = MutableLiveData<ResourceState>()
    private val dataStoreSessionStateMediatorLiveData =
        MediatorLiveData<ResourceState>()
    val dataStoreSessionLiveData: LiveData<ResourceState> = dataStoreSessionStateMediatorLiveData
    var user: User? = null

    init {
        dataStoreSessionStateMediatorLiveData.addSource(authService.authSessionStateLiveData) {
            when (it) {
                is Complete<*> -> {
                    if (it.data is SignedIn) {
                        authUser = it.data.authUser
                        runBlocking {
                            user = getUser(authUser as AuthUser)
                            start()
                            dataStoreSessionStateMediatorLiveData.removeSource(
                                hubEventLiveData(
                                    authUser
                                )
                            )
                            dataStoreSessionStateMediatorLiveData.addSource(
                                hubEventLiveData(
                                    authUser
                                )
                            ) {}
                        }
                    } else if (it.data is SignedOut) {
                        GlobalScope.launch { clear() }
                    }
                }
                is Loading -> {
                    postListMutableLiveData.value = Loading
                }
                is Error -> {
                    postListMutableLiveData.value = Error(it.error)
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun hubEventLiveData(authUser: AuthUser?) = liveData<ResourceState> {
        amplifyHub.subscribe(HubChannel.DATASTORE).collect {
            when (it.name) {
                DataStoreChannelEventName.READY.toString() -> {
                    if (authUser != null) {
                        user = getUser(authUser)
                        val postList = getPostList(0)
                        emit(Complete(postList))
                        dataStoreSessionStateMediatorLiveData.value = Complete(postList)
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun <T : Model> query(itemClass: KClass<T>, options: QueryOptions): Flow<T> {
        return amplifyDataStore.query(itemClass, options)
    }

    private suspend fun <T : Model> save(
        item: T,
        predicate: QueryPredicate = QueryPredicates.all()
    ) {
        amplifyDataStore.save(item, predicate)
    }

    private suspend fun <T : Model> delete(
        item: T,
        predicate: QueryPredicate = QueryPredicates.all()
    ) {
        amplifyDataStore.delete(item, predicate)
    }

    // Start synchronizing data with the cloud.
    private suspend fun start() {
        try {
            amplifyDataStore.start()
        } catch (dataStoreException: DataStoreException) {
            throw dataStoreException
        }
    }

    // Stops synchronizing the local data with the cloud, and clears all local data.
    suspend fun clear() {
        dataStoreSessionStateMediatorLiveData.postValue(Complete(listOf<Post>()))
        try {
            amplifyDataStore.clear()
        } catch (e: Exception) {
            Log.d("DataStore", "Error calling clear possibly due to an existing query in effect")
        }
        user = null
    }

    private suspend fun getUser(authUser: AuthUser): User {
        try {
            val user =
                query(User::class, Where.matches(User.ID.eq(authUser.userId))).firstOrNull()
            return user ?: createUser(authUser)
        } catch (datastoreException: DataStoreException) {
            throw datastoreException
        }
    }

    private suspend fun createUser(authUser: AuthUser): User {
        val user = User.builder()
            .username(authUser.username)
            // profile picture uri
            .profilePic(authUser.username + "ProfileImage")
            .id(authUser.userId)
            .build()
        try {
            save(user, QueryPredicates.all())
            return user
        } catch (dataStoreException: DataStoreException) {
            throw dataStoreException
        }
    }

    private suspend fun getPostList(page: Int): List<Post> {
        if (user == null) {
            return listOf()
        }
        try {
            return query(
                Post::class, Where.matches(Post.POSTED_BY.eq(user?.id))
                    .sorted(Post.CREATED_AT.descending())
                    .paginated(Page.startingAt(page).withLimit(10))
            ).toList()
        } catch (dataStoreException: DataStoreException) {
            throw dataStoreException
        }
    }

    suspend fun loadMorePosts(page: Int, currentPostList: List<Post>) {
        val newPostList = currentPostList + getPostList(page)
        dataStoreSessionStateMediatorLiveData.postValue(Complete(newPostList))
    }

    suspend fun getTotalNumberOfPosts(): Int {
        if (user == null) {
            return 0
        }
        try {
            return query(
                Post::class, Where.matches(Post.POSTED_BY.eq(user?.id))
            ).count()
        } catch (dataStoreException: DataStoreException) {
            throw dataStoreException
        }
    }

    suspend fun savePost(post: Post) {
        try {
            save(post)
            val postList = getPostList(0)
            dataStoreSessionStateMediatorLiveData.postValue(Complete(postList))
        } catch (dataStoreException: DataStoreException) {
            throw dataStoreException
        }
    }

    suspend fun deletePost(postId: String) {
        query(Post::class, Where.matches(Post.ID.eq(postId)))
            .catch { throw it } // Query failed
            .onEach { delete(it) }
            .catch { throw it } // Delete failed
            .collect {}
        val postList = getPostList(0)
        dataStoreSessionStateMediatorLiveData.postValue(Complete(postList))
    }

    suspend fun editProfileImage(userId: String, pictureKey: String) {
        query(User::class, Where.id(userId))
            .map { it.copyOfBuilder().profilePic(pictureKey).build() }
            .onEach {
                // Save and store user with updated profile picture
                save(it)
                user = it
            }
            .catch { throw it }
            .collect {}
        // Reload the posts to show the updated user profile picture
        val postList = getPostList(0)
        dataStoreSessionStateMediatorLiveData.postValue(Complete(postList))
    }
}