package ec.edu.puce.barrioseguro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ec.edu.puce.barrioseguro.core.navigation.BarrioSeguroNavGraph
import ec.edu.puce.barrioseguro.ui.theme.BarrioSeguroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BarrioSeguroTheme {
                BarrioSeguroNavGraph()
            }
        }
    }
}