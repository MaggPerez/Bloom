package com.example.bloom

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bloom.ui.theme.BloomTheme

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BloomTheme {
                RegisterView()
            }
        }
    }
}

@Composable
fun RegisterView(modifier: Modifier = Modifier){
    val context = LocalContext.current
    var createEmail by remember { mutableStateOf("") }
    var createPassword by remember { mutableStateOf("") }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        modifier = Modifier.fillMaxHeight().padding(12.dp)
    ) {

        //Logo
        Image(
            painter = painterResource(R.drawable.bloom_icon),
            contentDescription = "Bloom Logo",
            modifier = Modifier.size(84.dp)
        )

        //Header, and create account message
        Text(
            text = "Bloom",
            modifier = modifier
        )
        Text(
            text = "Helping people with their finances",
            fontSize = 12.sp,
            fontWeight = FontWeight.Light
        )

        Text(
            text = "Create an Account",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )



        //create email
        TextFields(
            labelText = "Create email",
            textInput = createEmail,
            onValueChange = { createEmail = it},
            modifier = modifier.padding(bottom = 8.dp).fillMaxWidth()
        )

        //create password
        TextFields(
            labelText = "Create password",
            textInput = createPassword,
            onValueChange = { createPassword = it },
            modifier = modifier.padding(bottom = 8.dp).fillMaxWidth()
        )


        //Register Button
        Button(
            onClick = {
                onHandleRegister(createEmail, createPassword) {
                    val intent = Intent(context, DashboardActivity::class.java)
                    context.startActivity(intent)
                }
            },
            modifier = modifier.fillMaxWidth()
        ) {
            Text("Register")
        }


        SwitchToLoginButton()
    }

}

private fun onHandleRegister(email: String, password: String, onSuccess: ()-> Unit){
    if(!email.isEmpty() && !password.isEmpty()){
        print("Success")
        onSuccess()
    }
    else{
        print("error")
    }
}


/**
 * TextField composable for email and password
 */
@Composable
private fun TextFields(
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


@Composable
private fun SwitchToLoginButton() {
    val context = LocalContext.current

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Already registered?")
        TextButton(
            onClick = {
                val intent = Intent(context, MainActivity::class.java)
                context.startActivity(intent)
            }
        ) {
            Text("Login")
        }
    }
}


@Preview(showBackground = true)
@Composable
fun RegisterPreview() {
    BloomTheme {
        RegisterView()
    }
}