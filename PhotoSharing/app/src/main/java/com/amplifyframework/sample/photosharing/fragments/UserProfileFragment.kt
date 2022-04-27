// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0
package com.amplifyframework.sample.photosharing.fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.amplifyframework.sample.photosharing.activities.ConfirmImageActivity
import com.amplifyframework.sample.photosharing.activities.UserProfileActivity
import com.amplifyframework.sample.photosharing.databinding.FragmentUserProfileBinding
import com.amplifyframework.sample.photosharing.util.ImageUtils
import com.amplifyframework.sample.photosharing.viewModels.AuthStatus
import com.amplifyframework.sample.photosharing.viewModels.ServiceCallStatus
import com.amplifyframework.sample.photosharing.viewModels.UserProfileViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class UserProfileFragment : Fragment() {
    private val viewModel by sharedViewModel<UserProfileViewModel>()
    private lateinit var binding: FragmentUserProfileBinding
    private var profileImageUri: Uri? = null
    private var lastSavedCameraImageName: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentUserProfileBinding.inflate(inflater, container, false)
        val view = binding.root

        binding.profileImageView.setOnClickListener {
            lastSavedCameraImageName = SimpleDateFormat(
                "yyyyMMdd_HHmmss",
                Locale.getDefault()
            ).format(Date()) + "_profile_photo"
            ImageUtils().showBottomDialog(
                requireActivity(), layoutInflater, resources,
                UserProfileActivity.NEW_PROFILE_PHOTO_CODE, lastSavedCameraImageName
            )
        }

        return view
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    private fun initViews() {
        binding.numOfPosts.text = "0"
        showUsername()
        showProfileImage()
        showNumberOfPosts()
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    private fun showUsername() {
        viewModel.viewStateLiveData.observe(viewLifecycleOwner) {
            when (it.state) {
                AuthStatus.SIGNED_IN -> {
                    it.data?.run {
                        val authUser = this
                        binding.userNameText.text = authUser.username
                    }
                }
                else -> {
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    private fun showProfileImage() {
        viewModel.userViewStateLiveData.observe(viewLifecycleOwner) {
            when (it.state) {
                AuthStatus.COMPLETE -> {
                    it.data?.run {
                        if (!it.data.profilePic.isNullOrEmpty()) {
                            viewModel.getImageLink(it.data.profilePic)
                                .observe(viewLifecycleOwner) { profileImageResult ->
                                    when (profileImageResult.state) {
                                        ServiceCallStatus.SUCCESS -> {
                                            profileImageResult.data?.let { profileImageUrl ->
                                                ImageUtils().loadImage(
                                                    requireContext(),
                                                    binding.profileImageView,
                                                    profileImageUrl.toString()
                                                )
                                            }
                                        }
                                        ServiceCallStatus.ERROR -> {
                                            Toast.makeText(
                                                context, "Show profile image failed",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                        else -> {
                                        }
                                    }
                                }
                        }
                    }
                }
                AuthStatus.ERROR -> {
                    Toast.makeText(
                        context, "Show profile image failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    private fun showNumberOfPosts() {
        viewModel.postListViewStateLiveData.observe(viewLifecycleOwner) {
            when (it.status) {
                AuthStatus.COMPLETE -> {
                    it.data?.run {
                        viewModel.totalNumPostsLiveData.observe(viewLifecycleOwner) { totalNumPosts ->
                            binding.numOfPosts.text = totalNumPosts.toString()
                        }
                    }
                }
                AuthStatus.ERROR -> {
                    Toast.makeText(
                        context, "Get number of posts failed",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                AuthStatus.LOADING -> binding.numOfPosts.text = "0"
                else -> {
                }
            }
        }
    }

    fun updateProfileImage(data: Intent?) {
        try {
            profileImageUri = if (data != null && data.data != null) {
                // Get the URI of the selected image from the gallery
                data.data
            } else {
                // Get the URI of the image saved from the camera
                ImageUtils().getStoredImageUri(lastSavedCameraImageName, requireActivity())
            }
            val confirmImageIntent = Intent(requireActivity(), ConfirmImageActivity::class.java)
            confirmImageIntent.putExtra("PROFILE_IMAGE_URI", profileImageUri.toString())
            startActivityForResult(confirmImageIntent, UserProfileActivity.SAVE_PROFILE_PHOTO_CODE)
        } catch (ioException: IOException) {
            Toast.makeText(context, "Profile image update failed.", Toast.LENGTH_SHORT).show()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == UserProfileActivity.SAVE_PROFILE_PHOTO_CODE) {
            viewModel.pageOfPosts = 0
            showProfileImage()
            val profileImageKey = data?.getStringExtra("PROFILE_PICTURE_KEY")
            if (!profileImageKey.isNullOrEmpty()) {
                // Remove old image from local cache since the profile picture key doesn't change.
                requireContext().deleteFile(profileImageKey)
            }
        }
    }
}