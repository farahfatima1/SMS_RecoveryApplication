
package com.example.data_recoveryapplication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SmsExportScreen(
    onExportCsv: () -> Unit,
    onExportTxt: () -> Unit,
    onExportPdf: () -> Unit
) {

    Column(

        modifier =
            Modifier
                .fillMaxSize()
                .padding(24.dp),

        verticalArrangement =
            Arrangement.spacedBy(18.dp)
    ) {

        Text(
            text = "Export Results",

            style =
                MaterialTheme
                    .typography
                    .headlineMedium
        )


        Text(
            text = "Select an export format:",

            style =
                MaterialTheme
                    .typography
                    .bodyLarge
        )


        Button(

            onClick =
                onExportCsv,

            modifier =
                Modifier.fillMaxWidth()
        ) {

            Text(
                text = "Export CSV"
            )
        }


        Button(

            onClick =
                onExportTxt,

            modifier =
                Modifier.fillMaxWidth()
        ) {

            Text(
                text = "Export TXT"
            )
        }


        Button(

            onClick =
                onExportPdf,

            modifier =
                Modifier.fillMaxWidth()
        ) {

            Text(
                text = "Export PDF"
            )
        }
    }
}
