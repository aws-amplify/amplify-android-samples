// Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
// SPDX-License-Identifier: MIT-0
package com.amplifyframework.sample.photosharing.util

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.transition.Transition
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.amplifyframework.sample.photosharing.R
import com.amplifyframework.sample.photosharing.databinding.PhotoResourceDialogBinding
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.target.Target
import java.io.File

class ImageUtils {

    fun loadImage(context: Context, imageView: ImageView, url: String) {
        GlideApp.with(context)
            .load(url)
            .apply(
                RequestOptions()
                    .error(imageView.drawable)
            )
            .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
            .into(imageView)
    }

    fun loadImageFromUri(context: Context, imageViewAmplifyGlideModule: ImageView, uri: Uri) {
        GlideApp.with(context)
            .load(uri)
            .override(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)
            .into(imageViewAmplifyGlideModule)
    }

    fun showBottomDialog(
        activity: Activity, layoutInflater: LayoutInflater, resources: Resources,
        requestCode: Int, cameraImageFileName: String
    ) {
        val bottomDialog = Dialog(activity, R.style.BottomDialog)
        val photoResourceDialogBinding = PhotoResourceDialogBinding.inflate(layoutInflater)
        val contentView = photoResourceDialogBinding.root
        bottomDialog.setContentView(contentView)
        val params: ViewGroup.MarginLayoutParams =
            contentView.layoutParams as ViewGroup.MarginLayoutParams
        params.width = resources.displayMetrics.widthPixels - dp2px(activity, 16f)
        params.bottomMargin = dp2px(activity, 8f)
        contentView.layoutParams = params
        bottomDialog.setCanceledOnTouchOutside(true)
        bottomDialog.window?.setGravity(Gravity.BOTTOM)
        bottomDialog.window?.setWindowAnimations(R.style.BottomDialog_Animation)
        bottomDialog.show()

        photoResourceDialogBinding.dialogCancelButton.setOnClickListener {
            bottomDialog.dismiss()
        }

        photoResourceDialogBinding.dialogCameraButton.setOnClickListener {
            // Check for permission to open camera
            if (ContextCompat.checkSelfPermission(
                    activity.applicationContext,
                    Manifest.permission.CAMERA
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Open camera
                openCamera(activity, requestCode, cameraImageFileName)
                bottomDialog.dismiss()
            } else {
                // Request permission to open camera
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.CAMERA), 3
                )
            }
        }

        photoResourceDialogBinding.dialogGalleryButton.setOnClickListener {
            // Check for permission to open photo gallery
            if (ContextCompat.checkSelfPermission(
                    activity.applicationContext,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // Open photo gallery
                showImagePicker(activity, requestCode)
                bottomDialog.dismiss()
            } else {
                // Request permission to open photo gallery
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 3
                )
            }
        }
    }

    fun getStoredImageUri(imageName: String, activity: Activity): Uri {
        val storageDir: File? = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val filePath = "$storageDir/${imageName}.jpg"
        return FileProvider.getUriForFile(
            activity.applicationContext,
            "com.example.android.fileprovider", File(filePath)
        )
    }

    private fun dp2px(context: Context, dpValue: Float): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dpValue,
            context.resources.displayMetrics
        )
            .toInt()
    }

    private fun openCamera(activity: Activity, requestCode: Int, cameraImageFileName: String) {
        // An intent for launching the camera on the phone
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        // Create a file to store the image taken with the camera
        // and get the Uri for this new file.
        val storageDir: File? = activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val photoFile = File("$storageDir/${cameraImageFileName}.jpg")
        // Continue only if the File was successfully created
        photoFile.also {
            val photoURI: Uri = FileProvider.getUriForFile(
                activity.applicationContext,
                "com.example.android.fileprovider",
                it
            )
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            // Launch phone camera app using the constant code
            activity.startActivityForResult(cameraIntent, requestCode)
        }
    }

    private fun showImagePicker(activity: Activity, requestCode: Int) {
        // An intent for launching image selection from the phone's photo gallery
        val galleryIntent = Intent(
            Intent.ACTION_PICK,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        )
        // Launch the photo gallery image selection using the constant code
        activity.startActivityForResult(galleryIntent, requestCode)
    }
}