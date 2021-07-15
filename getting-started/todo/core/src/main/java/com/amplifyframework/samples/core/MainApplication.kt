package com.amplifyframework.samples.core

import android.app.Application
import android.util.Log
import com.amplifyframework.AmplifyException
import com.amplifyframework.api.aws.AWSApiPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.datastore.AWSDataStorePlugin

open class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Add the necessary plugins to initialize Amplify
        try {
            Amplify.addPlugin(AWSDataStorePlugin())
            Amplify.addPlugin(AWSApiPlugin())
            Amplify.configure(applicationContext)

            Log.i("MyAmplifyApp", "Initialized Amplify")
        } catch (error: AmplifyException) {
            Log.e("MyAmplifyApp", "Could not initialize Amplify", error)
        }
    }
}
