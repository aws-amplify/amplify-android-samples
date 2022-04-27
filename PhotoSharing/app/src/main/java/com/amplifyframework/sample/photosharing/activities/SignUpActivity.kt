// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0
package com.amplifyframework.sample.photosharing.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.amplifyframework.auth.AuthUserAttributeKey
import com.amplifyframework.auth.options.AuthSignUpOptions
import com.amplifyframework.sample.photosharing.databinding.ActivitySignUpBinding
import com.amplifyframework.sample.photosharing.viewModels.OnBoardingViewModel
import com.amplifyframework.sample.photosharing.viewModels.SignUpStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

/**
 * SignUpActivity shows a UI for user to sign up a new account.
 */
class SignUpActivity : AppCompatActivity() {
    private val viewModel: OnBoardingViewModel by viewModels()

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // TODO: enable resending code after 60s
        binding.sendConfirmationCodeButton.setOnClickListener {
            val email = binding.signUpEmailEditText.text.toString()
            val username = binding.signUpUsernameEditText.text.toString()
            val password = binding.signUpPasswordEditText.text.toString()
            if (email.isEmpty()) {
                Toast.makeText(
                    applicationContext, "Please enter an email.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            } else if (!email.contains('@')) {
                Toast.makeText(
                    applicationContext, "Please enter a valid email.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
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
            if (isValidPassword(password)) {
                val options =
                    AuthSignUpOptions.builder().userAttribute(AuthUserAttributeKey.email(), email)
                        .build()
                // A confirmation code will be sent to the email id provided during sign up
                viewModel.signUp(username, password, options)
                Toast.makeText(
                    applicationContext,
                    "A confirmation code is sent to the email. Enter the code in the box to confirm your account.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(
                    applicationContext,
                    "Password is invalid. It should contain no less than $DEFAULT_PASSWORD_MIN_LENGTH characters.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.confirmSignUpButton.setOnClickListener {
            val code = binding.confirmationCodeEditText.text.toString()
            val username = binding.signUpUsernameEditText.text.toString()

            if (code.isEmpty()) {
                Toast.makeText(
                    applicationContext, "Please enter confirmation code.",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            viewModel.confirmSignUp(username, code)
        }

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        viewModel.signUpViewStateLiveData.observe(this) {
            when (it.state) {
                SignUpStatus.LOADING -> {}
                SignUpStatus.SEND_CODE_COMPLETE -> {
                    binding.signUpProgressIndicator.visibility = View.INVISIBLE
                    binding.confirmSignUpButton.visibility = View.VISIBLE
                    Toast.makeText(
                        applicationContext, "Code is sent successfully.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                SignUpStatus.SIGN_UP_COMPLETE -> {
                    binding.signUpProgressIndicator.visibility = View.INVISIBLE
                    Toast.makeText(
                        applicationContext, "Sign up succeeded.",
                        Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
                SignUpStatus.ERROR -> {
                    binding.signUpProgressIndicator.visibility = View.INVISIBLE
                    Toast.makeText(
                        applicationContext, "Sign up failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> binding.signUpProgressIndicator.visibility = View.VISIBLE
            }
        }
    }

    private fun isValidPassword(password: String) : Boolean {
        return password.length >= DEFAULT_PASSWORD_MIN_LENGTH
    }

    companion object {
        private const val DEFAULT_PASSWORD_MIN_LENGTH = 8
    }
}