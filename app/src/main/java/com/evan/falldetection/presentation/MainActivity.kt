/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter to find the
 * most up to date changes to the libraries and their usages.
 */

package com.evan.falldetection.presentation

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.evan.falldetection.FallDetectionService

class MainActivity : ComponentActivity() {

    private val permissions = arrayOf(
        android.Manifest.permission.ACTIVITY_RECOGNITION,
        android.Manifest.permission.BODY_SENSORS,
        android.Manifest.permission.FOREGROUND_SERVICE
    )

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { granted ->
        if (!granted.values.all { it }) {
            finish() // Close the app if permissions are denied
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Request permissions at runtime
        requestPermissions()

        setContent {
            var isMonitoring by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Button(onClick = {
                    val serviceIntent = Intent(this@MainActivity, FallDetectionService::class.java)
                    if (isMonitoring) stopService(serviceIntent) else startForegroundService(serviceIntent)
                    isMonitoring = !isMonitoring
                }) {
                    Text(if (isMonitoring) "Stop Monitoring" else "Start Monitoring")
                }
            }
        }
    }

    private fun requestPermissions() {
        if (permissions.any {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }) {
            ActivityCompat.requestPermissions(this, permissions, 0)
        }
    }


}
