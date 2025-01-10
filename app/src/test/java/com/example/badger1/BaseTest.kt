package com.example.badger1

import org.junit.Before
import org.junit.Rule

abstract class BaseTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Before
    fun setup() {
        // Common test setup
    }
}