package com.ifpe.tanajura

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ifpe.tanajura.ui.components.DataField
import com.ifpe.tanajura.ui.components.PasswordField

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                RegisterPage(modifier = Modifier.padding(innerPadding))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterPage(modifier: Modifier = Modifier) {
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }

    val activity = LocalActivity.current as Activity

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = CenterHorizontally
    ) {
        Text(
            text = "Crie sua conta",
            fontSize = 24.sp
        )

        Spacer(modifier = Modifier.size(16.dp))

        DataField (
            value = name,
            onValueChange = { name = it },
            label = "Nome completo"
        )

        Spacer(modifier = Modifier.height(12.dp))

        DataField(
            value = email,
            onValueChange = { email = it },
            label = "E-mail"
        )

        Spacer(modifier = Modifier.height(12.dp))

        PasswordField(
            value = password,
            onValueChange = { password = it },
            label = "Senha"
        )

        Spacer(modifier = Modifier.height(12.dp))

        PasswordField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = "Repetir senha"
        )

        Spacer(modifier = Modifier.size(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Button(
                onClick = {
                    if (password == confirmPassword) {
                        Toast.makeText(activity, "Registro realizado com sucesso!", Toast.LENGTH_LONG).show()
                        activity.finish()
                    } else {
                        Toast.makeText(activity, "As senhas não iguais!", Toast.LENGTH_SHORT)
                            .show()
                    }
                },
                enabled = name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()
            ) {
                Text("Registrar")
            }

            Spacer(modifier = Modifier.size(12.dp))

            Button(
                onClick = {
                    name = ""
                    email = ""
                    password = ""
                    confirmPassword = ""
                }
            ) {
                Text("Limpar")
            }
        }
    }
}