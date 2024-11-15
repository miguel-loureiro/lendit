package com.example.lendit.interfaces

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.lendit.ui.theme.LenditTheme

@Composable
fun LoginForm(){
    Surface{
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Hello",
                modifier = Modifier.padding(16.dp) // Add some padding for better appearance)

            )
        }
    }
}

@Preview(showBackground = true, device = "id:Nexus One", showSystemUi = true, apiLevel = 34)
@Composable
fun LoginFormPreview() {
    LenditTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background // Set the background color to dark
        ) {
            LoginForm()
        }
    }
}

@Preview(showBackground = true, device = "id:Nexus One", showSystemUi = true, apiLevel = 34)
@Composable
fun LoginFormPreviewDark() {
    LenditTheme(darkTheme = true) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background // Set the background color to dark
        ) {
            LoginForm()
        }
    }
}