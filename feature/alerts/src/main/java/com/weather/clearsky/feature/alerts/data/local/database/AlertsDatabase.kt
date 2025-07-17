package com.weather.clearsky.feature.alerts.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.weather.clearsky.feature.alerts.data.local.converter.AlertConverters
import com.weather.clearsky.feature.alerts.data.local.dao.WeatherAlertDao
import com.weather.clearsky.feature.alerts.data.local.entity.WeatherAlertEntity

@Database(
    entities = [WeatherAlertEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(AlertConverters::class)
abstract class AlertsDatabase : RoomDatabase() {
    
    abstract fun weatherAlertDao(): WeatherAlertDao
    
    companion object {
        const val DATABASE_NAME = "alerts_database"
        
        @Volatile
        private var INSTANCE: AlertsDatabase? = null
        
        fun getDatabase(context: Context): AlertsDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AlertsDatabase::class.java,
                    DATABASE_NAME
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 