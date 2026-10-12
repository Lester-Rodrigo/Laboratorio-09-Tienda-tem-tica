package com.example.laboratorio_09_tienda_temtica.data.local

import android.content.Context
import androidx.room3.Database
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.AndroidSQLiteDriver

@Database(
    entities = [
        FavoriteEntity::class,
        OrderLineEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class StoreDatabase : RoomDatabase() {
    abstract fun storeDao(): StoreDao
    companion object {
        private const val DATABASE_NAME = "book_store.db"
        @Volatile
        private var instance: StoreDatabase? = null
        fun getInstance(context: Context): StoreDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder<StoreDatabase>(
                    context = context.applicationContext,
                    name = DATABASE_NAME
                )
                    .setDriver(AndroidSQLiteDriver())
                    .build()
                    .also { database -> instance = database }
            }
    }
}
