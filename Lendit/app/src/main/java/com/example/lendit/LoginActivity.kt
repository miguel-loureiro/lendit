package com.example.lendit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.lendit.ui.theme.LenditTheme
import com.example.lendit.interfaces.LoginForm

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent{
            LenditTheme {
                LoginForm()
            }
        }
    }
}