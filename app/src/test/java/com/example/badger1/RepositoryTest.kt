package com.example.badger1

import android.content.ClipData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ItemRepositoryTest : BaseTest() {
    private val dao: ItemDao = mockk()
    private lateinit var repository: ItemRepository

    @Before
    fun setUp() {
        repository = ItemRepository(dao)
    }

    @Test
    fun `when inserting item, dao is called`() = runTest {
        // Arrange
        val item = ClipData.Item(name = "Test", description = "Description")
        coEvery { dao.insertItem(item) } just runs

        // Act
        repository.insert(item)

        // Assert
        coVerify { dao.insertItem(item) }
    }
}
