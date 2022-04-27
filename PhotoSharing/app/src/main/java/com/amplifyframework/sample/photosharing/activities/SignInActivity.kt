// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0
package com.amplifyframework.sample.photosharing.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.amplifyframework.sample.photosharing.databinding.ActivitySignInBinding
import com.amplifyframework.sample.photosharing.viewModels.AuthStatus
import com.amplifyframework.sample.photosharing.viewModels.OnBoardingViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

/**
 * SignInActivity is the launcher activity for customers to sign in.
 */
class SignInActivity : AppCompatActivity() {
    private val viewModel: OnBoardingViewModel by viewModels()

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.signInButton.setOnClickListener {
            if (viewModel.signInViewStateLiveData.value?.state == AuthStatus.SIGNED_IN) {
                goToUserProfile(this)
            } else {
                val username = binding.signInUsernameEditText.text.toString()
                val password = binding.signInPasswordEditText.text.toString()
                if (username.isEmpty()) {
                    Toast.makeText(
                        applicationContext, "Please enter a username.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                if (password.isEmpty()) {
                    Toast.makeText(
                        applicationContext, "Please enter a password.",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                viewModel.signIn(username, password)
            }
        }

        binding.signUpButton.setOnClickListener {
            if (viewModel.signInViewStateLiveData.value?.state == AuthStatus.SIGNED_IN) {
                Toast.makeText(
                    applicationContext, "User already signed in.",
                    Toast.LENGTH_SHORT
                ).show()
                goToUserProfile(this)
            } else {
                goToSignUpActivity(this)
            }
        }

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        viewModel.signInViewStateLiveData.observe(this) {
            when (it.state) {
                AuthStatus.LOADING -> {
                    binding.signInProgressIndicator.visibility = View.VISIBLE
                }
                AuthStatus.SIGNED_IN -> {
                    binding.signInProgressIndicator.visibility = View.GONE
                    binding.signInPasswordEditText.text?.clear()
                    binding.signInUsernameEditText.text?.clear()
                    goToUserProfile(this)
                }
                AuthStatus.ERROR -> {
                    binding.signInProgressIndicator.visibility = View.INVISIBLE
                    Toast.makeText(
                        applicationContext, "Sign in failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    binding.signInProgressIndicator.visibility = View.INVISIBLE
                }
            }
        }
    }

    private fun goToUserProfile(from: Activity) {
        from.startActivity(Intent(from, UserProfileActivity::class.java))
        finish()
    }

    private fun goToSignUpActivity(from: Activity) {
        from.startActivity(Intent(from, SignUpActivity::class.java))
    }
}