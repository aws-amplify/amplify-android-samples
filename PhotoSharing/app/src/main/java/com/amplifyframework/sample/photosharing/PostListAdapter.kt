// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0
package com.amplifyframework.sample.photosharing

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.amplifyframework.datastore.generated.model.Post
import com.amplifyframework.sample.photosharing.databinding.LoadMorePostsButtonBinding
import com.amplifyframework.sample.photosharing.databinding.NoMorePostsTextBinding
import com.amplifyframework.sample.photosharing.databinding.PostItemBinding
import com.amplifyframework.sample.photosharing.util.ImageUtils
import com.amplifyframework.sample.photosharing.viewModels.ServiceCallStatus
import com.amplifyframework.sample.photosharing.viewModels.ViewState
import org.koin.core.component.KoinComponent
import java.net.URL

class PostListAdapter(
    private val getImageLink: (String) -> LiveData<ViewState<URL?, ServiceCallStatus>>,
    private val loadMorePostsAction: () -> Unit,
    private val getTotalNumberOfPosts: () -> Int,
    private val handlePostMenuClick: (MenuItem, Post) -> Boolean
) : RecyclerView.Adapter<PostListViewHolder>(), KoinComponent {

    var postList: List<Post> = emptyList()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostListViewHolder {
        val binding = when (viewType) {
            R.layout.post_item ->
                PostItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            R.layout.no_more_posts_text ->
                NoMorePostsTextBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            else -> {
                LoadMorePostsButtonBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            }
        }
        return PostListViewHolder(binding, parent)
    }

    override fun onBindViewHolder(viewHolder: PostListViewHolder, position: Int) {
        when (getItemViewType(position)) {
            R.layout.post_item -> viewHolder.bind(
                postList,
                position,
                getImageLink,
                handlePostMenuClick
            )
            R.layout.load_more_posts_button -> viewHolder.bind(loadMorePostsAction)
        }
    }

    override fun getItemCount(): Int {
        return postList.size + 1
    }

    override fun getItemViewType(position: Int): Int {
        return when {
            position < itemCount - 1 -> {
                R.layout.post_item
            }
            postList.size == getTotalNumberOfPosts() -> {
                R.layout.no_more_posts_text
            }
            else -> {
                R.layout.load_more_posts_button
            }
        }
    }
}

/** ViewHolder for Post, takes in the inflated view and the onClick behavior. */
class PostListViewHolder(private val binding: ViewBinding, private val parent: ViewGroup) :
    RecyclerView.ViewHolder(binding.root) {
    fun bind(
        postList: List<Post>, position: Int,
        getImageLink: (String) -> LiveData<ViewState<URL?, ServiceCallStatus>>,
        handlePostMenuClick: (MenuItem, Post) -> Boolean
    ) {
        val postItemBinding = binding as PostItemBinding
        val item = postList[position]
        if (!item.postedBy.profilePic.isNullOrEmpty()) {
            // Show the user profile image for the post
            getImageLink(item.postedBy.profilePic).observe(itemView.context as LifecycleOwner) { profileImageResult ->
                when (profileImageResult.state) {
                    ServiceCallStatus.SUCCESS -> {
                        profileImageResult.data?.let {
                            ImageUtils().loadImage(
                                parent.context, postItemBinding.avatarImage,
                                it.toString()
                            )
                        }
                    }
                    ServiceCallStatus.ERROR -> {
                        Toast.makeText(
                            parent.context, "Error loading profile image for post.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    else -> {
                    }
                }
            }
        }
        // Show the description for the post
        postItemBinding.postBody.text = item.postBody
        // Show how long ago the post was created
        postItemBinding.postTime.text = getTimeAgo(item.createdAt.toDate().time)
        // Show the username for the user who created the post
        postItemBinding.userName.text = item.postedBy.username
        // Show the image for the post
        getImageLink(item.pictureKey).observe(itemView.context as LifecycleOwner) { postImageResult ->
            when (postImageResult.state) {
                ServiceCallStatus.SUCCESS -> {
                    postImageResult.data?.let {
                        ImageUtils().loadImage(
                            parent.context, postItemBinding.postImage,
                            it.toString()
                        )
                    }
                }
                ServiceCallStatus.ERROR -> {
                    Toast.makeText(
                        parent.context, "Error loading post image.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                }
            }
        }

        // Enable a popup menu for the post
        postItemBinding.postMenu.setOnClickListener {
            val popup = PopupMenu(parent.context, it)
            popup.inflate(R.menu.post_menu)
            popup.setOnMenuItemClickListener { menuItem ->
                handlePostMenuClick(menuItem, item)
            }
            popup.show()
        }
    }

    fun bind(loadMorePostsAction: () -> Unit) {
        (binding as LoadMorePostsButtonBinding).loadMorePosts.setOnClickListener {
            loadMorePostsAction()
        }
    }

    private fun getTimeAgo(timestampOfPosts: Long): String {
        return DateUtils.getRelativeTimeSpanString(
            timestampOfPosts,
            System.currentTimeMillis(),
            DateUtils.SECOND_IN_MILLIS
        ).toString()
    }
}