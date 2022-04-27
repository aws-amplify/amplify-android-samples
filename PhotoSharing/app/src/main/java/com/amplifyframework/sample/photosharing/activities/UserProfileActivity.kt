// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0
package com.amplifyframework.sample.photosharing.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.amplifyframework.sample.photosharing.R
import com.amplifyframework.sample.photosharing.databinding.ActivityUserProfileBinding
import com.amplifyframework.sample.photosharing.fragments.PostListFragment
import com.amplifyframework.sample.photosharing.fragments.UserProfileFragment
import com.amplifyframework.sample.photosharing.util.ImageUtils
import com.amplifyframework.sample.photosharing.viewModels.AuthStatus
import com.amplifyframework.sample.photosharing.viewModels.UserProfileViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class UserProfileActivity : AppCompatActivity() {
    companion object {
        const val NEW_PROFILE_PHOTO_CODE = 1
        const val NEW_POST_PHOTO_CODE = 2
        const val SAVE_PROFILE_PHOTO_CODE = 3
        const val SAVE_POST_CODE = 4
    }

    private val viewModel by viewModel<UserProfileViewModel>()
    private var lastSavedCameraImageName: String = ""

    @OptIn(ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityUserProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mToolbar = binding.mainToolbar.root
        mToolbar.title = "Posts"
        setSupportActionBar(mToolbar)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add<PostListFragment>(R.id.post_list_fragment_container)
                add<UserProfileFragment>(R.id.user_profile_fragment_container)
            }
        }

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        viewModel.viewStateLiveData.observe(this) {
            when (it.state) {
                AuthStatus.SIGNED_OUT -> goToSignInPage(this)
                AuthStatus.ERROR -> Toast.makeText(
                    applicationContext, "Sign in failed",
                    Toast.LENGTH_SHORT
                ).show()
                else -> {
                }
            }
        }

        binding.createPostButton.setOnClickListener {
            lastSavedCameraImageName = SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.getDefault()
            ).format(Date()) + "_post_photo"
            // show a dialog to take photo using camera or choose image from gallery
            ImageUtils().showBottomDialog(
                this,
                layoutInflater,
                resources,
                NEW_POST_PHOTO_CODE,
                lastSavedCameraImageName
            )
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, kotlinx.coroutines.FlowPreview::class)
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.sign_out_button -> {
            runBlocking {
                viewModel.signOut()
            }
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            val userProfileFragment =
                supportFragmentManager.findFragmentById(R.id.user_profile_fragment_container) as UserProfileFragment
            when (requestCode) {
                SAVE_POST_CODE -> viewModel.updatePostInfoAfterSave()
                NEW_PROFILE_PHOTO_CODE -> userProfileFragment.updateProfileImage(data)
                NEW_POST_PHOTO_CODE -> editPost(data)
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            // User closed or canceled the image selection
            Toast.makeText(
                applicationContext, "Image selection cancelled.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun editPost(data: Intent?) {
        try {
            val postImageUri = if (data != null && data.data != null) {
                // Get the URI of the selected image from the gallery
                data.data
            } else {
                // Get the URI of the image saved from the camera
                ImageUtils().getStoredImageUri(lastSavedCameraImageName, this)
            }
            val createPostIntent = Intent(this, PostEditorActivity::class.java)
            createPostIntent.putExtra("POST_IMAGE_URI", postImageUri.toString())
            startActivityForResult(createPostIntent, SAVE_POST_CODE)
        } catch (ioException: IOException) {
            Toast.makeText(applicationContext, "Post creation failed.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun goToSignInPage(from: Activity) {
        from.startActivity(Intent(from, SignInActivity::class.java))
        finish()
    }
}