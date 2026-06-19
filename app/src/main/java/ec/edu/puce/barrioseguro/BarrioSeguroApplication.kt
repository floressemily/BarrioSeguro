package ec.edu.puce.barrioseguro

import android.app.Application
import ec.edu.puce.barrioseguro.data.local.database.BarrioSeguroDatabase
import ec.edu.puce.barrioseguro.data.repository.IncidenteRepositoryLocal
import ec.edu.puce.barrioseguro.domain.repository.IncidenteRepository

class BarrioSeguroApplication : Application() {
    lateinit var database: BarrioSeguroDatabase
        private set
    lateinit var repository: IncidenteRepository
        private set

    override fun onCreate() {
        super.onCreate()
        database = BarrioSeguroDatabase.getInstance(this)
        repository = IncidenteRepositoryLocal(database.incidenteDao())
    }
}
