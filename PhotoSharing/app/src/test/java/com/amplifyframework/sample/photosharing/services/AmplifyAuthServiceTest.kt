// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0

package com.amplifyframework.sample.photosharing.services

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.amplifyframework.auth.AuthException
import com.amplifyframework.auth.AuthSession
import com.amplifyframework.kotlin.auth.KotlinAuthFacade
import com.amplifyframework.kotlin.hub.KotlinHubFacade
import com.amplifyframework.sample.photosharing.viewModels.TestLifecycleOwner
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockkClass
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
import org.mockito.Mockito.mock
import java.util.concurrent.CountDownLatch

class AmplifyAuthServiceTest : KoinTest {
    @get:Rule
    val mockProvider = MockProviderRule.create { clazz ->
        mockkClass(clazz, relaxed = true)
    }

    @get:Rule
    val koinTestRule = KoinTestRule.create {
        modules(
            module {
                single { Repository() }
                single { KotlinAuthFacade() }
                single { KotlinHubFacade() }
            })
    }

    @ExperimentalCoroutinesApi
    private val testDispatcher = StandardTestDispatcher()

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    var context: Context = mock(Context::class.java)

    @ExperimentalCoroutinesApi
    @Before
    fun setup() {
        // Set Coroutine Dispatcher.
        Dispatchers.setMain(testDispatcher)
    }

    @ExperimentalCoroutinesApi
    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    @Test
    fun testSignInCallsAmplifySignInWithWebUI() {
        //Arrange
        val amplifyAuthService = declareMock<KotlinAuthFacade> {
            coEvery { fetchAuthSession() } returns AuthSession(false)
        }
        val subject = AmplifyAuthService(context)

        //Act
        subject.signIn(TestCoroutineScope(), "test","test")
        //Assert
        coVerify {
            amplifyAuthService.signIn("test","test", any())
            amplifyAuthService.fetchAuthSession()
        }
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    @Test
    fun testSignInWithWebUICallsAmplifySignInWithWebUIThrowsException() {
        //Arrange
        val countDownLatch = CountDownLatch(2)
        val amplifyAuthService = declareMock<KotlinAuthFacade>
        {
            coEvery { signInWithWebUI(any()) } throws AuthException("test", "test")
            coEvery { fetchAuthSession() } returns AuthSession(false)
        }
        val subject = AmplifyAuthService(context)

        //Act
        subject.signIn(TestCoroutineScope(), "test", "test")
        //Assert
        coVerify {
            amplifyAuthService.signIn("test","test", any())
            amplifyAuthService.fetchAuthSession()
        }
        subject.authSessionStateLiveData.observe(TestLifecycleOwner()) {
            when (it) {
                is Error -> countDownLatch.countDown()
                is Loading -> countDownLatch.countDown()
                else -> {
                }
            }
        }
    }
}