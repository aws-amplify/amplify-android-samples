// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0
package com.amplifyframework.sample.photosharing.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.amplifyframework.auth.AuthUser
import com.amplifyframework.auth.options.AuthSignUpOptions
import com.amplifyframework.sample.photosharing.services.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
    ViewModel for sign in and sign up activities.
 */
class OnBoardingViewModel : ViewModel(), KoinComponent {
    @ExperimentalCoroutinesApi
    @FlowPreview
    private val authService: AmplifyAuthService = inject<Repository>().value.authService()

    @ExperimentalCoroutinesApi
    @FlowPreview
    val signInViewStateLiveData: LiveData<ViewState<AuthUser?, AuthStatus>> =
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
                is Loading -> ViewState(state = AuthStatus.LOADING)
                is Error -> ViewState(state = AuthStatus.ERROR)
            }
        }


    @ExperimentalCoroutinesApi
    @FlowPreview
    val signUpViewStateLiveData: LiveData<ViewState<AuthUser?, SignUpStatus>> =
        Transformations.map(authService.authSignUpStateLiveData) {
            when (it) {
                is Complete<*> -> {
                    when (it.data) {
                        is SendCodeComplete -> ViewState(state = SignUpStatus.SEND_CODE_COMPLETE)
                        is SendCodeInProgress -> ViewState(state = SignUpStatus.SEND_CODE_IN_PROGRESS)
                        is SignUpComplete -> ViewState(state = SignUpStatus.SIGN_UP_COMPLETE)
                        is SignUpInProgress -> ViewState(state = SignUpStatus.SIGN_UP_IN_PROGRESS)
                        else -> ViewState(state = SignUpStatus.LOADING)
                    }
                }
                is Loading -> ViewState(state = SignUpStatus.LOADING)
                is Error -> ViewState(state = SignUpStatus.ERROR)
            }
        }

    @ExperimentalCoroutinesApi
    @FlowPreview
    fun signIn(username: String, password: String) {
        authService.signIn(viewModelScope, username, password)
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    fun signUp(username: String, password: String, options: AuthSignUpOptions) {
        authService.signUp(viewModelScope, username, password, options)
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    fun confirmSignUp(
        username: String,
        confirmationCode: String,
    ) {
        authService.confirmSignUp(viewModelScope, username, confirmationCode)
    }
}

data class ViewState<T, U>(val data: T? = null, val state: U)