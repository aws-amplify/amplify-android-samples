// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0
package com.amplifyframework.sample.photosharing.viewModels

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.amplifyframework.auth.AuthUser
import com.amplifyframework.sample.photosharing.services.*
import io.mockk.*
import junit.framework.AssertionFailedError
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.test.*
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
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class OnBoardingViewModelTest : KoinTest {
    @get:Rule
    val mockProvider = MockProviderRule.create { clazz ->
        mockkClass(clazz, relaxed = true)
    }

    private var context: Context = Mockito.mock(Context::class.java)

    @ExperimentalCoroutinesApi
    @FlowPreview
    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(
            module {
                single { Repository() }
                single { AmplifyAuthService(context) }
            })
    }

    @ExperimentalCoroutinesApi
    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()


    @ExperimentalCoroutinesApi
    @Before
    fun setup() {
        // Set Coroutine Dispatcher.
        Dispatchers.setMain(testDispatcher)
        context = Mockito.mock(Context::class.java)
    }

    @ExperimentalCoroutinesApi
    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    @Test
    fun currentSessionStateEmitsSignedOut() {
        //Arrange
        val countDownLatch = CountDownLatch(1)
        val mockLiveData = MutableLiveData<ResourceState>()
        declareMock<AmplifyAuthService> {
            every { authSessionStateLiveData } returns (mockLiveData)
        }
        mockLiveData.value = Complete<SessionState>(SignedOut)
        val subject = OnBoardingViewModel()
        //Act
        subject.signInViewStateLiveData.observe(TestLifecycleOwner()) { result ->
            when (result.state) {
                AuthStatus.SIGNED_OUT -> countDownLatch.countDown()
                else -> AssertionFailedError()
            }
        }
        //Assert
        assertTrue(countDownLatch.await(3, TimeUnit.SECONDS))
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    @Test
    fun currentSessionStateEmitsSignedIn() {
        //Arrange
        val countDownLatch = CountDownLatch(1)
        val mockLiveData = MutableLiveData<ResourceState>()
        val authUser = AuthUser("123", "test")
        declareMock<AmplifyAuthService> {
            every { authSessionStateLiveData } returns (mockLiveData)
        }
        mockLiveData.value = Complete<SessionState>(SignedIn(authUser))
        val subject = OnBoardingViewModel()

        //Act
        subject.signInViewStateLiveData.observe(TestLifecycleOwner()) { result ->
            when (result.state) {
                AuthStatus.SIGNED_IN -> countDownLatch.countDown()
                else -> AssertionFailedError()
            }
        }
        //Assert
        assertTrue(countDownLatch.await(3, TimeUnit.SECONDS))
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    @Test
    fun testSignInCallsAuthServiceSignInWithWebUI() {
        //Arrange
        val authService = declareMock<AmplifyAuthService>()
        val subject = OnBoardingViewModel()
        //Act
        subject.signIn("", "")
        //Assert
        verify { authService.signIn(any(), any(), any()) }
    }
}
