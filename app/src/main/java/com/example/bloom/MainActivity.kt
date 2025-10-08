package com.example.bloom

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bloom.ui.theme.BloomTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BloomTheme {
                LoginView()
            }
        }
    }
}

@Composable
fun LoginView(modifier: Modifier = Modifier) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxHeight()
    ) {
        Text(
            text = "Bloom",
            modifier = modifier
        )
        Text(
            text = "Helping people with their finances",
            fontSize = 12.sp,
            fontWeight = FontWeight.Light
        )

        //email
        TextFields(
            labelText = "Enter your email",
            textInput = email,
            onValueChange = { email = it},
            modifier = modifier.padding(bottom = 8.dp).fillMaxWidth()
        )

        //password
        TextFields(
            labelText = "Enter your password",
            textInput = password,
            onValueChange = { password = it },
            modifier = modifier.padding(bottom = 8.dp).fillMaxWidth()
        )

        Button(
            onClick = {
                onHandleSignIn(email, password)
            },
            modifier = modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
    }
}

private fun onHandleSignIn(email: String, password: String){
    if(!email.isEmpty() && !password.isEmpty()){
        print("Success")
    }
    else{
        print("error")
    }
}


/**
 * TextField composable for email and password
 */
@Composable
fun TextFields(
    labelText: String,
    textInput: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    TextField(
        value = textInput,
        onValueChange = onValueChange,
        label = { Text(labelText) },
        singleLine = true,
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    BloomTheme {
        LoginView()
    }
}