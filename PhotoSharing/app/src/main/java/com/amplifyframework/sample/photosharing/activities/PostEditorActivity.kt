// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0
package com.amplifyframework.sample.photosharing.activities

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.amplifyframework.sample.photosharing.R
import com.amplifyframework.sample.photosharing.databinding.ActivityPostEditorBinding
import com.amplifyframework.sample.photosharing.util.ImageUtils
import com.amplifyframework.sample.photosharing.viewModels.PostEditorViewModel
import kotlinx.coroutines.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.roundToInt

class PostEditorActivity : AppCompatActivity() {
    companion object {
        private const val HUNDRED_PERCENT = 100
    }

    private val viewModel by viewModel<PostEditorViewModel>()
    private lateinit var binding: ActivityPostEditorBinding
    private lateinit var postImageUri: Uri
    private var postBodyIsEmpty = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPostEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mToolbar = binding.postToolbar.root
        mToolbar.title = "New Post"
        setSupportActionBar(mToolbar)
        // Get a support ActionBar corresponding to this toolbar and enable the Up button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        postImageUri = Uri.parse(intent.getStringExtra("POST_IMAGE_URI"))
        ImageUtils().loadImageFromUri(
            applicationContext,
            binding.imagePreview,
            postImageUri
        )

        binding.postBodyEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val editTextIsEmpty = s.isNullOrEmpty()
                if (editTextIsEmpty != postBodyIsEmpty) {
                    postBodyIsEmpty = editTextIsEmpty
                    invalidateOptionsMenu()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Observe the LiveData, passing in this activity as the LifecycleOwner and the observer.
        viewModel.uploadProgressLiveData.observe(this) {
            if (it.uploadError != null) {
                Toast.makeText(
                    applicationContext,
                    "Post creation failed. ${it.uploadError.recoverySuggestion}",
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
                    Toast.makeText(applicationContext, "Created post", Toast.LENGTH_SHORT).show()
                    setResult(Activity.RESULT_OK)
                    finish()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.create_post_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.share_button)?.isEnabled = !postBodyIsEmpty
        return super.onPrepareOptionsMenu(menu)
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        android.R.id.home -> {
            setResult(Activity.RESULT_CANCELED)
            finish()
            true
        }
        R.id.share_button -> {
            val imageInputStream = contentResolver.openInputStream(postImageUri)
            if (imageInputStream == null) {
                Toast.makeText(applicationContext, "Post creation failed.", Toast.LENGTH_SHORT)
                    .show()
            } else {
                binding.uploadImageProgressIndicator.visibility = View.VISIBLE
                viewModel.createPost(binding.postBodyEditText.text.toString(), imageInputStream)
            }
            true
        }

        else -> {
            super.onOptionsItemSelected(item)
        }
    }
}