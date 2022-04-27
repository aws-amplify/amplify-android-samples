// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0
package com.amplifyframework.sample.photosharing.services

import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.AuthUser

sealed class ResourceState
object Loading : ResourceState()
class Complete<T>(val data: T?) : ResourceState()
class Error(val error: AmplifyException) : ResourceState()

sealed class SessionState
class SignedIn constructor(val authUser: AuthUser) : SessionState()
object SignedOut : SessionState()

sealed class SignUpState
object SendCodeComplete : SignUpState()
object SendCodeInProgress : SignUpState()
object SignUpComplete : SignUpState()
object SignUpInProgress : SignUpState()