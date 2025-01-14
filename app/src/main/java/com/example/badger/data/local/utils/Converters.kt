package com.example.badger.data.local.utils

import androidx.room.TypeConverter
import com.example.badger.data.model.ListItem
import com.example.badger.data.model.Enums.Priority
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromString(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromList(list: List<String>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun fromListItems(value: List<ListItem>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toListItems(value: String): List<ListItem> {
        val listType = object : TypeToken<List<ListItem>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromPriority(priority: Priority): String {
        return priority.name
    }

    @TypeConverter
    fun toPriority(value: String): Priority {
        return try {
            Priority.valueOf(value)
        } catch (e: IllegalArgumentException) {
            Priority.NORMAL
        }
    }

    @TypeConverter
    fun fromTimestamp(value: Long?): java.util.Date? {
        return value?.let { java.util.Date(it) }
    }

    @TypeConverter
    fun toTimestamp(date: java.util.Date?): Long? {
        return date?.time
    }
}
