package com.example.bloom

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bloom.ui.theme.BloomTheme
import com.example.bloom.viewmodel.LoginRegisterViewModel


@Composable
fun RegisterScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    loginRegisterViewModel: LoginRegisterViewModel = viewModel()
){
    val coroutineScope = rememberCoroutineScope()

    when {
        loginRegisterViewModel.openAlertDialog.value -> {
            AlertDialogPopUp(
                onDismissRequest = { loginRegisterViewModel.openAlertDialog.value = false },
                dialogTitle = "Error Registering",
                dialogText = loginRegisterViewModel.status as String,
                icon = Icons.Default.Info
            )
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            modifier = Modifier
                .fillMaxHeight()
                .padding(12.dp)
        )
        {

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
                textInput = loginRegisterViewModel.createEmail,
                onValueChange = { loginRegisterViewModel.createEmail = it},
                modifier = modifier.padding(bottom = 8.dp).fillMaxWidth()
            )

            //create password
            TextFields(
                labelText = "Enter your password",
                textInput = loginRegisterViewModel.createPassword,
                onValueChange = { loginRegisterViewModel.createPassword = it },
                modifier = modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth(),
                passwordVisible = loginRegisterViewModel.passwordVisible,
                onPasswordVisibilityChange = {
                    loginRegisterViewModel.passwordVisible = !loginRegisterViewModel.passwordVisible
                }
            )



            //Register Button
            Button(
                onClick = {
                    coroutineScope.launch {
                        if(loginRegisterViewModel.register()){
                            //navigate to dashboard and clear back stack
                            navController.navigate("dashboard_screen") {
                                popUpTo("main_screen") { inclusive = true }
                            }
                        }
                    }
                },
                modifier = modifier.fillMaxWidth()
            ) {
                Text("Register")
            }


            //Navigate to Login Screen
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Already have an account?")
                TextButton(
                    onClick = {
                        navController.navigate("login_screen")
                    }
                ) {
                    Text("Login")
                }
            }
        }
    }

}



/**
 * Alert Dialog
 */
@Composable
private fun AlertDialogPopUp(
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
    modifier: Modifier = Modifier,
    passwordVisible: Boolean = false,
    onPasswordVisibilityChange: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = textInput,
        onValueChange = onValueChange,
        label = { Text(labelText) },
        singleLine = true,
        //if password, show password icon and hide text
        visualTransformation = if (labelText.contains("password", ignoreCase = true) && !passwordVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },

        //adding trailing icon for the password field
        trailingIcon = {
            if (labelText.contains("password", ignoreCase = true) && onPasswordVisibilityChange != null) {
                val image = if (passwordVisible)
                    Icons.Filled.Visibility
                else
                    Icons.Filled.VisibilityOff

                val description = if (passwordVisible) "Hide password" else "Show password"

                IconButton(onClick = onPasswordVisibilityChange) {
                    Icon(imageVector = image, contentDescription = description)
                }
            }
        },
        modifier = modifier
    )
}





@Preview(showBackground = true)
@Composable
fun RegisterPreview() {
    BloomTheme {
        RegisterScreen(navController = rememberNavController())
    }
}