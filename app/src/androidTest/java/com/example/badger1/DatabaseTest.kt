package com.example.badger1

import android.content.ClipData
import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat

import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

// DatabaseTest.kt - Room database test
@RunWith(AndroidJUnit4::class)
class DatabaseTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: ItemDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.itemDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndReadItem() = runTest {
        val item = ClipData.Item(name = "Test", description = "Description")
        dao.insertItem(item)

        val items = dao.getAllItems().first()
        assertThat(items).contains(item)
    }
}

// UI Tests
@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @get:Rule
    val activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun displayItemInList() {
        // Add test item
        onView(withId(R.id.addButton)).perform(click())
        onView(withId(R.id.nameInput)).perform(typeText("Test Item"))
        onView(withId(R.id.descriptionInput)).perform(typeText("Test Description"))
        onView(withId(android.R.id.button1)).perform(click())

        // Verify item is displayed
        onView(withText("Test Item")).check(matches(isDisplayed()))
    }
}