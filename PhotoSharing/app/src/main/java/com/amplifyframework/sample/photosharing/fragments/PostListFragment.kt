// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0
package com.amplifyframework.sample.photosharing.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import com.amplifyframework.datastore.generated.model.Post
import com.amplifyframework.sample.photosharing.PostListAdapter
import com.amplifyframework.sample.photosharing.R
import com.amplifyframework.sample.photosharing.databinding.FragmentPostListBinding
import com.amplifyframework.sample.photosharing.viewModels.AuthStatus
import com.amplifyframework.sample.photosharing.viewModels.ServiceCallStatus
import com.amplifyframework.sample.photosharing.viewModels.UserProfileViewModel
import com.amplifyframework.sample.photosharing.viewModels.ViewState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import org.koin.androidx.viewmodel.ext.android.sharedViewModel
import java.net.URL

class PostListFragment : Fragment() {
    private val viewModel by sharedViewModel<UserProfileViewModel>()
    private lateinit var binding: FragmentPostListBinding

    @ExperimentalCoroutinesApi
    @FlowPreview
    private val listAdapter: PostListAdapter = PostListAdapter(
        this::getImageLink, this::loadMorePosts,
        this::getTotalNumberOfPosts, this::handlePostMenuClick
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPostListBinding.inflate(inflater, container, false)
        return binding.root
    }

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    private fun initViews() {
        binding.postListRecyclerView.adapter = listAdapter
        binding.loadPostListProgressIndicator.visibility = View.VISIBLE
        setupLiveData()
    }

    @FlowPreview
    @ExperimentalCoroutinesApi
    private fun setupLiveData() {
        viewModel.postListViewStateLiveData.observe(viewLifecycleOwner) {
            when (it.status) {
                AuthStatus.COMPLETE -> {
                    it.data?.run {
                        binding.loadPostListProgressIndicator.visibility = View.GONE
                        listAdapter.postList = it.data
                        if (viewModel.pageOfPosts == 0) {
                            // Scroll to the top of the recycler view
                            binding.postListRecyclerView.scrollToPosition(0)
                        }
                    }
                }
                AuthStatus.ERROR -> {
                    Toast.makeText(
                        context, "Get list of posts failed.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                AuthStatus.LOADING -> {
                    binding.loadPostListProgressIndicator.visibility = View.VISIBLE
                }
                else -> {
                }
            }
        }
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    private fun getImageLink(key: String): LiveData<ViewState<URL?, ServiceCallStatus>> {
        return viewModel.getImageLink(key)
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    private fun loadMorePosts() {
        viewModel.loadMorePosts(listAdapter.postList).observe(viewLifecycleOwner) {
            when (it) {
                ServiceCallStatus.SUCCESS -> {
                    binding.loadPostListProgressIndicator.visibility = View.GONE
                }
                ServiceCallStatus.LOADING -> {
                    binding.loadPostListProgressIndicator.visibility = View.VISIBLE
                }
                ServiceCallStatus.ERROR -> {
                    binding.loadPostListProgressIndicator.visibility = View.GONE
                    Toast.makeText(context, "Error loading more posts.", Toast.LENGTH_SHORT).show()
                }
                else -> {
                }
            }
        }
    }

    private fun getTotalNumberOfPosts(): Int {
        return viewModel.totalNumPostsLiveData.value ?: 0
    }

    @ExperimentalCoroutinesApi
    @FlowPreview
    private fun handlePostMenuClick(menuItem: MenuItem, post: Post): Boolean {
        return when (menuItem.itemId) {
            R.id.delete_post -> {
                viewModel.deletePost(post).observe(viewLifecycleOwner) {
                    when (it) {
                        ServiceCallStatus.SUCCESS -> {
                            Toast.makeText(context, "Deleted post.", Toast.LENGTH_SHORT).show()
                        }
                        ServiceCallStatus.ERROR -> {
                            Toast.makeText(context, "Delete post failed.", Toast.LENGTH_SHORT)
                                .show()
                        }
                        ServiceCallStatus.LOADING -> {
                            Toast.makeText(context, "Deleting post...", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                        }
                    }
                }
                true
            }
            else -> false
        }
    }
}