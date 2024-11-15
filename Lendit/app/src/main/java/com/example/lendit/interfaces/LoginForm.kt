package com.example.lendit.interfaces

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.lendit.ui.theme.LenditTheme

@Composable
fun LoginForm(){
    Surface{
        Column{
            /*LoginField()
            PasswordField()
            LabeledCheckbox()
            Button(onClick = { *//*TODO *//* }) {
             } */
        }
    }
}

@Preview(showBackground = true, device = "id:Nexus One", showSystemUi = true, apiLevel = 34)
@Composable
fun GreetingPreview() {
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
fun GreetingPreviewDark() {
    LenditTheme(darkTheme = true) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background // Set the background color to dark
        ) {
            LoginForm()
        }
    }
}