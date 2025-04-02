package com.example.badger

import com.example.badger.utils.TestUserManager
import org.junit.Before
import org.junit.Rule
import javax.inject.Inject

abstract class BaseTest {
    @Inject
    open lateinit var testUserManager: TestUserManager

    @get:Rule
    open val mainDispatcherRule = MainDispatcherRule()

    @Before
    open fun setup() {
        // Common test setup
    }
}
