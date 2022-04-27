// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0
package com.amplifyframework.sample.photosharing.services

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.liveData
import com.amplifyframework.auth.AuthChannelEventName
import com.amplifyframework.auth.AuthException
import com.amplifyframework.auth.AuthUser
import com.amplifyframework.auth.options.AuthSignUpOptions
import com.amplifyframework.hub.HubChannel
import com.amplifyframework.kotlin.auth.KotlinAuthFacade
import com.amplifyframework.kotlin.hub.KotlinHubFacade
import kotlinx.coroutines.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.lang.Exception

@FlowPreview
class AmplifyAuthService(val context: Context) : KoinComponent {
    private val amplifyAuth: KotlinAuthFacade = inject<KotlinAuthFacade>().value
    private val amplifyHub: KotlinHubFacade = inject<KotlinHubFacade>().value
    private val authSessionStateMutableLiveData = MutableLiveData<ResourceState>()
    private val authSessionStateMediatorLiveData = MediatorLiveData<ResourceState>()
    val authSessionStateLiveData: LiveData<ResourceState> =
        authSessionStateMediatorLiveData
    private val authSignUpStateMutableLiveData = MutableLiveData<ResourceState>()
    val authSignUpStateLiveData: LiveData<ResourceState> = authSignUpStateMutableLiveData
    val PREF_KEY_USER_ID = "amplify_user_id"
    val PREF_KEY_USERNAME = "amplify_username"

    @OptIn(ExperimentalCoroutinesApi::class)
    private val hubEventLiveData = liveData {
        amplifyHub.subscribe(HubChannel.AUTH).collect {
            when (AuthChannelEventName.valueOf(it.name)) {
                AuthChannelEventName.SESSION_EXPIRED ->
                    emit(Complete<SessionState>(SignedOut))
                AuthChannelEventName.SIGNED_IN -> {
                    val authUser = getCurrentUser()
                    if (authUser != null) {
                        emit(Complete(SignedIn(authUser)))
                    }
                }
                AuthChannelEventName.SIGNED_OUT -> {
                    emit(Complete<SessionState>(SignedOut))
                }
                else -> {
                }
            }
        }
    }

    init {
        authSessionStateMediatorLiveData.addSource(authSessionStateMutableLiveData) {
            authSessionStateMediatorLiveData.value = authSessionStateMutableLiveData.value
        }
        authSessionStateMediatorLiveData.addSource(hubEventLiveData) {
            authSessionStateMediatorLiveData.value = hubEventLiveData.value
        }
        // check if user is signed in or not when initializing AmplifyAuthService
        if (isSignedIn()) {
            val authUser = getCurrentUser()
            if (authUser != null) {
                authSessionStateMutableLiveData.postValue(Complete<SessionState>(SignedIn(authUser)))
            }
        }
    }

    // SignIn use AmplifyAuthPlugin
    fun signIn(coroutineScope: CoroutineScope, username: String?, password: String?) {
        coroutineScope.launch {
            try {
                authSessionStateMutableLiveData.value = Loading
                amplifyAuth.signIn(username, password)
            } catch (error: AuthException) {
                authSessionStateMutableLiveData.value = Error(error)
            }
        }
    }

    fun signUp(
        coroutineScope: CoroutineScope,
        username: String,
        password: String,
        options: AuthSignUpOptions
    ) {
        coroutineScope.launch {
            try {
                authSignUpStateMutableLiveData.value = Loading
                val sendCodeResult = amplifyAuth.signUp(username, password, options)
                if (sendCodeResult.isSignUpComplete) {
                    authSignUpStateMutableLiveData.value = Complete<SignUpState>(SendCodeComplete)
                } else {
                    authSignUpStateMutableLiveData.value = Complete<SignUpState>(SendCodeInProgress)
                }
            } catch (error: AuthException) {
                authSignUpStateMutableLiveData.value = Error(error)
            }
        }
    }

    fun confirmSignUp(coroutineScope: CoroutineScope, username: String, confirmationCode: String) {
        coroutineScope.launch {
            try {
                authSignUpStateMutableLiveData.value = Loading
                val signUpResult = amplifyAuth.confirmSignUp(username, confirmationCode)
                if (signUpResult.isSignUpComplete) {
                    authSignUpStateMutableLiveData.value = Complete<SignUpState>(SignUpComplete)
                } else {
                    authSignUpStateMutableLiveData.value = Complete<SignUpState>(SignUpInProgress)
                }
            } catch (error: AuthException) {
                authSignUpStateMutableLiveData.value = Error(error)
            }
        }
    }

    fun signOut(coroutineScope: CoroutineScope) {
        coroutineScope.launch {
            try {
                authSessionStateMutableLiveData.value = Loading
                amplifyAuth.signOut()
            } catch (error: AuthException) {
                authSessionStateMutableLiveData.value = Error(error)
            }
            clearLocalSession()
        }
    }

    private fun clearLocalSession(){
        val sharedPreference = context.getSharedPreferences(
            "photosharing_sample_app",
            Context.MODE_PRIVATE
        )
        val editor = sharedPreference.edit()
        editor.remove(PREF_KEY_USER_ID)
        editor.remove(PREF_KEY_USERNAME)
        editor.apply()
    }

    private fun getCurrentUser(): AuthUser? {
        val sharedPreference = context.getSharedPreferences(
            "photosharing_sample_app",
            Context.MODE_PRIVATE
        )

        val user = amplifyAuth.getCurrentUser()
        if (user != null && user.userId.isNotEmpty()) {
            val editor = sharedPreference.edit()
            editor.putString(PREF_KEY_USER_ID, user.userId)
            editor.putString(PREF_KEY_USERNAME, user.username)
            editor.apply()
            return user
        }
        return AuthUser(
            sharedPreference.getString(PREF_KEY_USER_ID, "") ?: "",
            sharedPreference.getString(PREF_KEY_USERNAME, "") ?: ""
        )

    }

    private fun isSignedIn(): Boolean = runBlocking {
        val scope = CoroutineScope(Job())
        scope.async {
            return@async amplifyAuth.fetchAuthSession().isSignedIn
        }.await()
    }
}
