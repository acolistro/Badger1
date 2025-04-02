// src/test/java/com/example/badger/HiltTestRunner.kt
package com.example.badger

import android.app.Application
import android.content.Context
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Config(application = HiltTestApplication::class)
@RunWith(RobolectricTestRunner::class)
abstract class HiltTestRunner
