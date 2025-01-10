package com.example.badger1

import android.content.ClipData
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.runs
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ItemViewModelTest : BaseTest() {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val repository: ItemRepository = mockk()
    private lateinit var viewModel: ItemViewModel

    @Before
    fun setUp() {
        viewModel = ItemViewModel(repository)
    }

    @Test
    fun `when inserting item, repository is called`() = runTest {
        // Arrange
        val item = ClipData.Item(name = "Test", description = "Description")
        coEvery { repository.insert(item) } just runs

        // Act
        viewModel.insertItem(item.name, item.description)

        // Assert
        coVerify { repository.insert(any()) }
    }

    @Test
    fun `when loading items, LiveData is updated`() = runTest {
        // Arrange
        val items = listOf(ClipData.Item(name = "Test", description = "Description"))
        every { repository.allItems } returns flow { emit(items) }

        // Act & Assert
        viewModel.allItems.test {
            assertEquals(items, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }
}
