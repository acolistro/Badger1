package com.example.badger.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.badger.data.local.dao.ListDao
import com.example.badger.data.local.dao.ListItemDao
import com.example.badger.data.local.dao.UserDao
import com.example.badger.data.local.entities.ListEntity
import com.example.badger.data.local.entities.ListItemEntity
import com.example.badger.data.local.entities.UserEntity
import com.example.badger.data.local.utils.Converters

@Database(
    entities = [
        ListEntity::class,
        UserEntity::class,
        ListItemEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class BadgerDatabase : RoomDatabase() {
    abstract fun listDao(): ListDao
    abstract fun userDao(): UserDao
    abstract fun listItemDao(): ListItemDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add new columns with default values
                db.execSQL(
                    """
                    ALTER TABLE users 
                    ADD COLUMN firstName TEXT NOT NULL DEFAULT ''
                """
                )

                db.execSQL(
                    """
                    ALTER TABLE users 
                    ADD COLUMN lastName TEXT NOT NULL DEFAULT ''
                """
                )

                db.execSQL(
                    """
                    ALTER TABLE users 
                    ADD COLUMN phone TEXT NOT NULL DEFAULT ''
                """
                )

                db.execSQL(
                    """
                    ALTER TABLE users 
                    ADD COLUMN emailVerified INTEGER NOT NULL DEFAULT 0
                """
                )

                db.execSQL(
                    """
                    ALTER TABLE users 
                    ADD COLUMN phoneVerified INTEGER NOT NULL DEFAULT 0
                """
                )
            }
        }
    }
}
