package dev.mskelton.versly.persistence

import androidx.compose.runtime.compositionLocalOf
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [User::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}

/**
 * Composition local for the [AppDatabase] instance.
 *
 * This is used to provide the database instance to the composables that need it.
 */
val LocalAppDatabase = compositionLocalOf<AppDatabase> {
    error("No AppDatabase provided")
}
