package com.example.bloom

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bloom.ui.theme.BloomTheme
import com.example.bloom.viewmodel.LoginRegisterViewModel
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    loginRegisterViewModel: LoginRegisterViewModel = viewModel()
) {

    val coroutineScope = rememberCoroutineScope()

    //Alert Dialog if there was an error signing in
    when {
        loginRegisterViewModel.openAlertDialog.value -> {
            AlertDialogPopUp(
                onDismissRequest = { loginRegisterViewModel.openAlertDialog.value = false },
                dialogTitle = "Error Signing in",
                dialogText = loginRegisterViewModel.status as String,
                icon = Icons.Default.Info
            )
        }
    }

    Surface(
        modifier = Modifier.fillMaxHeight(),
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
                textInput = loginRegisterViewModel.loginEmail,
                onValueChange = { loginRegisterViewModel.loginEmail = it},
                modifier = modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth()
            )

            //password
            TextFields(
                labelText = "Enter your password",
                textInput = loginRegisterViewModel.loginPassword,
                onValueChange = { loginRegisterViewModel.loginPassword = it },
                modifier = modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth(),
                passwordVisible = loginRegisterViewModel.passwordVisible,
                onPasswordVisibilityChange = {
                    loginRegisterViewModel.passwordVisible = !loginRegisterViewModel.passwordVisible
                }
            )


            Button(
                onClick = {
                    coroutineScope.launch {
                        if(loginRegisterViewModel.login()) {
                            //navigate to dashboard screen if login is successful and clear back stack
                            navController.navigate("dashboard_screen") {
                                popUpTo("main_screen") { inclusive = true }
                            }
                        }
                    }

                },
                modifier = modifier.fillMaxWidth()
            ) {
                Text("Login")
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Don't have an account?")
                TextButton(
                    onClick = {
                        navController.navigate("register_screen")
                    }
                ) {
                    Text("Create one")
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
fun LoginPreview() {
    BloomTheme {
        LoginScreen(navController = rememberNavController())
    }
}