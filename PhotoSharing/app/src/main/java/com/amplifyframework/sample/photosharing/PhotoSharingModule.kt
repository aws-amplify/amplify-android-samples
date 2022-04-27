// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0
package com.amplifyframework.sample.photosharing

import com.amplifyframework.kotlin.core.Amplify
import com.amplifyframework.sample.photosharing.services.AmplifyAuthService
import com.amplifyframework.sample.photosharing.services.AmplifyDatastoreService
import com.amplifyframework.sample.photosharing.services.AmplifyStorageService
import com.amplifyframework.sample.photosharing.services.Repository
import com.amplifyframework.sample.photosharing.viewModels.ConfirmImageViewModel
import com.amplifyframework.sample.photosharing.viewModels.OnBoardingViewModel
import com.amplifyframework.sample.photosharing.viewModels.PostEditorViewModel
import com.amplifyframework.sample.photosharing.viewModels.UserProfileViewModel
import kotlinx.coroutines.FlowPreview
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

@FlowPreview
val photoSharingModule = module {
    single { Repository() }
    single { AmplifyAuthService(androidContext()) }
    single { AmplifyDatastoreService() }
    single { AmplifyStorageService() }
    single { Amplify.Auth }
    single { Amplify.Hub }
    single { Amplify.DataStore }
    single { Amplify.Storage }
    viewModel {
        OnBoardingViewModel()
    }
    viewModel {
        UserProfileViewModel()
    }
    viewModel {
        ConfirmImageViewModel()
    }
    viewModel {
        PostEditorViewModel()
    }
}