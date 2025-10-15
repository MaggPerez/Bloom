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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val loginController = LoginController()
    val openAlertDialog = remember { mutableStateOf(false) }
    var status by remember { mutableStateOf<Any>("") }

    //Alert Dialog if there was an error signing in
    when {
        openAlertDialog.value -> {
            AlertDialogPopUp(
                onDismissRequest = { openAlertDialog.value = false },
                dialogTitle = "Error Signing in",
                dialogText = status as String,
                icon = Icons.Default.Info
            )
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        modifier = Modifier.fillMaxHeight().padding(12.dp)
    ) {
        Image(
            painter = painterResource(R.drawable.bloom_icon),
            contentDescription = "Bloom Logo",
            modifier = Modifier.size(84.dp)
        )

        //Header and Welcome message
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
            text = "Welcome, Login!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
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
                status = loginController.onHandleLogin(email, password)

                //if login was successful, we take the user to the dashboard
                if(status as Boolean) {
                    val intent = Intent(context, DashboardActivity::class.java)
                    context.startActivity(intent)
                }
                else{
                    //shows Alert Dialog if there was an error
                    openAlertDialog.value = true
                }
            },
            modifier = modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
        //Switch to register button
        SwitchToRegisterButton()

    }
}


/**
 * Alert Dialog
 */
@Composable
fun AlertDialogPopUp(
    onDismissRequest: () -> Unit,
    dialogTitle: String,
    dialogText: String,
    icon: ImageVector,
) {
    AlertDialog(
        icon = {
            Icon(icon, contentDescription = "Example Icon")
        },
        title = {
            Text(text = dialogTitle)
        },
        text = {
            Text(text = dialogText)
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {

        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismissRequest()
                }
            ) {
                Text("Dismiss")
            }
        }
    )
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
private fun SwitchToRegisterButton() {
    val context = LocalContext.current

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("Don't have an account?")
        TextButton(
            onClick = {
                val intent = Intent(context, RegisterActivity::class.java)
                context.startActivity((intent))
            }
        ) {
            Text("Create one")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    BloomTheme {
        LoginView()
    }
}