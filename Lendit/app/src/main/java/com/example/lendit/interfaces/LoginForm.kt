package com.example.lendit.interfaces

import android.R.attr.contentDescription
import android.R.attr.tint
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.lendit.ui.theme.LenditTheme

@Composable
fun LoginForm(){
    Surface{
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ){
            LoginField(
                value = "data",
                onChange = {}
            )
            /*PasswordField()
            LabeledCheckbox()
            Button(onClick = { *//*TODO *//* }) {
             } */
        }
    }
}

@Composable
fun LoginField(
    value: String,
    onChange: (String) ->Unit,
    modifier: Modifier = Modifier
) {
    val leadingIcon = @Composable{
        Icon(
            Icons.Default.Person,
            contentDescription = "",
            tint = MaterialTheme.colorScheme.primary
        )
    }

    TextField(
        value = value,
        onValueChange = onChange,
        modifier = modifier,
        leadingIcon = leadingIcon
    )
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