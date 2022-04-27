// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0
package com.amplifyframework.sample.photosharing.services

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.amplifyframework.auth.AuthUser
import com.amplifyframework.core.model.query.Where
import com.amplifyframework.datastore.DataStoreChannelEventName
import com.amplifyframework.datastore.generated.model.Post
import com.amplifyframework.datastore.generated.model.User
import com.amplifyframework.hub.HubChannel
import com.amplifyframework.hub.HubEvent
import com.amplifyframework.kotlin.auth.KotlinAuthFacade
import com.amplifyframework.kotlin.datastore.KotlinDataStoreFacade
import com.amplifyframework.kotlin.hub.KotlinHubFacade
import com.amplifyframework.sample.photosharing.viewModels.TestLifecycleOwner
import io.mockk.*
import io.mockk.coEvery
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.KoinTestRule
import org.koin.test.mock.MockProviderRule
import org.koin.test.mock.declareMock
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.mock
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class AmplifyDatastoreServiceTest: KoinTest {
    private var context: Context = mock(Context::class.java)

    @get:Rule
    val mockProvider = MockProviderRule.create { clazz ->
        mockkClass(clazz, relaxed = true)
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(
            module {
                single { Repository() }
                single { KotlinAuthFacade() }
                single { KotlinHubFacade() }
                single { KotlinDataStoreFacade() }
                single { AmplifyAuthService(context = context) }
            })
    }

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    private val testDispatcher = TestCoroutineDispatcher()

    @ExperimentalCoroutinesApi
    private val testScope = TestCoroutineScope(testDispatcher)


    @ExperimentalCoroutinesApi
    @Before
    fun setup() {
        // Set Coroutine Dispatcher.
        Dispatchers.setMain(testDispatcher)
    }

    @ExperimentalCoroutinesApi
    @After
    fun tearDown() {
        // Reset Coroutine Dispatcher and Scope.
        testDispatcher.cleanupTestCoroutines()
        testScope.cleanupTestCoroutines()
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
    }


    @ExperimentalCoroutinesApi
    @FlowPreview
    @Test
    fun testGetPostListLiveData() {
        //Arrange
        val countDownLatch = CountDownLatch(1)
        val expectedAuthUser = AuthUser("test","test")
        val testAuthSessionStateLiveData = MutableLiveData<ResourceState>()
        testAuthSessionStateLiveData.value = Complete<SessionState>(SignedIn(expectedAuthUser))
        val amplifyAuthService = declareMock<AmplifyAuthService>
        {
            every { authSessionStateLiveData}.returns(testAuthSessionStateLiveData)
        }
        val post = Post.Builder().build()
        val amplifyDataStoreFacade = declareMock<KotlinDataStoreFacade>
        {
            every { query(Post::class)} returns (
                    flow {
                        emit(post)
                    })


            every { query(User::class, Where.matches(User.ID.eq("test")))} returns (
                    flow {
                        emit(User.Builder().build())
                    })
        }
        declareMock<KotlinHubFacade> {
            coEvery { subscribe(HubChannel.DATASTORE) } returns (
                    flow {
                        emit(HubEvent.create(DataStoreChannelEventName.READY.toString()))
                    })
        }
        val subject = AmplifyDatastoreService()

        //Act
        // subject.signInWithWebUI(mockk(), TestCoroutineScope())
        //Assert
        // coVerify { amplifyAuthService.signInWithWebUI(any()) }
        subject.dataStoreSessionLiveData.observe(TestLifecycleOwner()) {
            when (it) {
                is Complete<*> -> {
                    countDownLatch.countDown()
                }
                else -> {}
            }
        }
        assertTrue(countDownLatch.await(30, TimeUnit.SECONDS))
    }
}