package ec.edu.puce.barrioseguro

import android.app.Application
import ec.edu.puce.barrioseguro.data.local.database.BarrioSeguroDatabase
import ec.edu.puce.barrioseguro.data.repository.IncidenteRepositoryLocal
import ec.edu.puce.barrioseguro.domain.repository.IncidenteRepository
import org.osmdroid.config.Configuration

class BarrioSeguroApplication : Application() {
    lateinit var database: BarrioSeguroDatabase
        private set
    lateinit var repository: IncidenteRepository
        private set

    override fun onCreate() {
        super.onCreate()

        // Inicializar OSMDroid una sola vez en toda la app —
        // debe ejecutarse ANTES de cualquier instancia de MapView.
        Configuration.getInstance().load(
            this,
            getSharedPreferences("osmdroid", MODE_PRIVATE)
        )
        Configuration.getInstance().userAgentValue = packageName

        database = BarrioSeguroDatabase.getInstance(this)
        repository = IncidenteRepositoryLocal(database.incidenteDao())
    }
}
