// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0
package com.amplifyframework.sample.photosharing.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amplifyframework.sample.photosharing.R
import com.amplifyframework.sample.photosharing.databinding.ActivityConfirmImageBinding
import com.amplifyframework.sample.photosharing.util.ImageUtils
import com.amplifyframework.sample.photosharing.viewModels.ConfirmImageViewModel
import kotlinx.coroutines.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.roundToInt

class ConfirmImageActivity : AppCompatActivity() {
    companion object {
        private const val HUNDRED_PERCENT = 100
    }

    private val viewModel by viewModel<ConfirmImageViewModel>()
    private lateinit var binding: ActivityConfirmImageBinding
    private lateinit var profileImageUri: Uri
    private lateinit var profileImageKey: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfirmImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mToolbar = binding.confirmToolbar.root
        mToolbar.title = ""
        setSupportActionBar(mToolbar)
        // Get a support ActionBar corresponding to this toolbar and enable the Up button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        profileImageUri = Uri.parse(intent.getStringExtra("PROFILE_IMAGE_URI"))
        ImageUtils().loadImageFromUri(
            applicationContext,
            binding.profileImagePreview,
            profileImageUri
        )

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        viewModel.uploadProgressLiveData.observe(this) {
            if (it.uploadError != null) {
                Toast.makeText(
                    applicationContext,
                    "Profile image update failed. ${it.uploadError.recoverySuggestion}",
                    Toast.LENGTH_SHORT
                ).show()
                binding.uploadImageProgressIndicator.visibility = View.GONE
                binding.uploadImageProgressIndicator.setProgressCompat(0, false)
            } else {
                val curProgress = (it.uploadProgress * HUNDRED_PERCENT).roundToInt()
                binding.uploadImageProgressIndicator.setProgressCompat(curProgress, true)
                if (curProgress == HUNDRED_PERCENT) {
                    binding.uploadImageProgressIndicator.visibility = View.GONE
                    binding.uploadImageProgressIndicator.setProgressCompat(0, false)
                    Toast.makeText(applicationContext, "Profile image updated", Toast.LENGTH_SHORT)
                        .show()
                    val resultIntent = Intent().putExtra("PROFILE_PICTURE_KEY", profileImageKey)
                    setResult(Activity.RESULT_OK, resultIntent)
                    finish()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.confirm_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            setResult(Activity.RESULT_CANCELED)
            finish()
            true
        }
        R.id.save_button -> {
            val imageInputStream = contentResolver.openInputStream(profileImageUri)
            if (imageInputStream == null) {
                Toast.makeText(
                    applicationContext,
                    "Profile image update failed.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                binding.uploadImageProgressIndicator.visibility = View.VISIBLE
                profileImageKey = viewModel.updateProfileImage(imageInputStream)
            }
            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }
}