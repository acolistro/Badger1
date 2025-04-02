// src/test/java/com/example/badger/HiltTestRule.kt
package com.example.badger

import dagger.hilt.android.testing.HiltAndroidRule
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement

class HiltTestRule : TestRule {
    private val hiltAndroidRule = HiltAndroidRule(this)

    override fun apply(base: Statement, description: Description): Statement {
        return hiltAndroidRule.apply(base, description)
    }

    fun inject() {
        hiltAndroidRule.inject()
    }
}
