package com.example.gs

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.text.TextUtils
import kotlinx.coroutines.*
import java.io.File
import java.io.FileWriter
import java.io.BufferedWriter
import android.util.Log
import android.view.Gravity
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private var job: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Obtenha a referência do TextView do layout
        val dataTextView: TextView = findViewById(R.id.dataTextView)

        // Inicialize o texto do TextView
        dataTextView.text = ""

        // Verifica o diretório de arquivos externos
        val externalDir = getExternalFilesDir(null)
        if (externalDir != null) {
            val directoryPath = externalDir.absolutePath
            Log.d("MainActivity", "Diretório externo: $directoryPath")
            if (externalDir.canWrite()) {
                Log.d("MainActivity", "Diretório gravável")
            } else {
                Log.d("MainActivity", "Diretório não é gravável")
            }
        } else {
            Log.d("MainActivity", "Diretório externo não encontrado")
        }

        // Lê o arquivo CSV e atualiza a interface
        readCSVAndUpdateUI()

        // Inicia a simulação de sensores
        startSensorSimulation()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancela a simulação de sensores ao encerrar a atividade
        stopSensorSimulation()
    }

    private fun startSensorSimulation() {
        // Cria um novo job para a simulação de sensores
        job = CoroutineScope(Dispatchers.Default).launch {
            while (isActive) {
                // Simula a leitura de sensores
                val temperature = simulateTemperature()
                val humidity = simulateHumidity()

                // Atualiza a interface do usuário com os valores simulados
                withContext(Dispatchers.Main) {
                    updateUI(temperature, humidity)
                }

                // Armazena os valores simulados em um arquivo CSV
                saveToCSV(temperature, humidity)

                // Aguarda um intervalo antes da próxima leitura
                delay(5000) // Intervalo de 5 segundos
            }
        }
    }

    private fun stopSensorSimulation() {
        // Cancela o job da simulação de sensores
        job?.cancel()
    }

    private fun simulateTemperature(): Float {
        // Simulação da leitura de temperatura
        // Substitua esse código pela lógica real de leitura do sensor de temperatura
        return (20..30).random().toFloat() // Simula um valor aleatório entre 20 e 30
    }

    private fun simulateHumidity(): Float {
        // Simulação da leitura de umidade
        // Substitua esse código pela lógica real de leitura do sensor de umidade
        return (40..60).random().toFloat() // Simula um valor aleatório entre 40 e 60
    }

    private fun updateUI(temperature: Float, humidity: Float) {
        val dataTextView: TextView = findViewById(R.id.dataTextView)
        val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val newText = "Data e Hora: $currentDate\nTemperature: $temperature   |   Humidity: $humidity"

        // Obter o texto atual do TextView
        val currentText = dataTextView.text.toString()

        // Concatenar o novo texto com o texto atual, separado por uma quebra de linha
        val finalText = if (currentText.isNotEmpty()) {
            "$newText\n\n$currentText"
        } else {
            newText
        }

        // Definir o novo texto no TextView
        dataTextView.text = finalText

        // Configurar a exibição das últimas linhas do texto
        dataTextView.setSingleLine(false)
        dataTextView.maxLines = Integer.MAX_VALUE
        dataTextView.ellipsize = null
    }

    private fun saveToCSV(temperature: Float, humidity: Float) {
        val csvFile = File(getExternalFilesDir(null), "sensor_data.csv")

        // Cria o arquivo CSV se não existir
        if (!csvFile.exists()) {
            csvFile.createNewFile()
        }

        // Escreve os valores simulados no arquivo CSV
        val fileWriter = FileWriter(csvFile, true)
        val bufferedWriter = BufferedWriter(fileWriter)

        val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val csvLine = "$currentDate,$temperature,$humidity"
        bufferedWriter.write(csvLine)
        bufferedWriter.newLine()

        bufferedWriter.close()
        fileWriter.close()
    }

    private fun readCSVAndUpdateUI() {
        val csvFile = File(getExternalFilesDir(null), "sensor_data.csv")

        if (csvFile.exists()) {
            try {
                val fileReader = FileReader(csvFile)
                val bufferedReader = BufferedReader(fileReader)
                var line: String? = bufferedReader.readLine()

                while (line != null) {
                    val values = line.split(",")
                    if (values.size >= 3) {
                        val dateTime = values[0]
                        val temperature = values[1].toFloatOrNull()
                        val humidity = values[2].toFloatOrNull()

                        if (temperature != null && humidity != null) {
                            runOnUiThread {
                                updateUI(temperature, humidity)
                            }
                        }
                    }
                    line = bufferedReader.readLine()
                }

                bufferedReader.close()
                fileReader.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}
