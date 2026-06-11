package ec.edu.puce.barrioseguro.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ec.edu.puce.barrioseguro.data.local.dao.IncidenteDao
import ec.edu.puce.barrioseguro.data.local.entity.IncidenteEntity

@Database(
    entities = [IncidenteEntity::class],
    version = 1,
    exportSchema = false
)
abstract class BarrioSeguroDatabase : RoomDatabase() {

    abstract fun incidenteDao(): IncidenteDao

    companion object {
        private const val DATABASE_NAME = "barrio_seguro.db"

        @Volatile
        private var INSTANCE: BarrioSeguroDatabase? = null

        fun getInstance(context: Context): BarrioSeguroDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    BarrioSeguroDatabase::class.java,
                    DATABASE_NAME
                ).build().also { INSTANCE = it }
            }
        }
    }
}