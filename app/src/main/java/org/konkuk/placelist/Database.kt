package org.konkuk.placelist

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import org.konkuk.placelist.dao.Converters
import org.konkuk.placelist.dao.PlacesDao
import org.konkuk.placelist.dao.TodoDao
import org.konkuk.placelist.domain.Place
import org.konkuk.placelist.domain.Todo

@Database(
    entities = [Place::class, Todo::class],
    version = 1
)
@TypeConverters(Converters::class)
abstract class PlacesListDatabase: RoomDatabase() {
    abstract fun placesDao(): PlacesDao
    abstract fun TodoDao(): TodoDao

    companion object {
        private var INSTANCE: PlacesListDatabase? = null

        fun getDatabase(context: Context): PlacesListDatabase{
            val instance = INSTANCE
            if (instance != null) return instance

            val newInstance = Room.databaseBuilder(
                context, PlacesListDatabase::class.java, "database"
            ).fallbackToDestructiveMigration()
                .build()

            INSTANCE = newInstance
            return newInstance
        }
    }
}