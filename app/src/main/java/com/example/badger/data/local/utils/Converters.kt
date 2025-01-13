package com.example.badger.data.local.utils

import androidx.room.TypeConverter
import com.example.badger.data.model.ListItem
import com.example.badger.data.model.Enums.Priority
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
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
}
