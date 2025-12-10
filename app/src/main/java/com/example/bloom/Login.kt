package com.example.bloom

import android.credentials.GetCredentialException
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.Color
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
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID

import androidx.compose.material3.CircularProgressIndicator

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

            GoogleSignInButton(navController, loginRegisterViewModel)


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
                modifier = modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                enabled = !loginRegisterViewModel.isEmailLoading && !loginRegisterViewModel.isGoogleLoading
            ) {
                if (loginRegisterViewModel.isEmailLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = "Login",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
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


@Composable
fun GoogleSignInButton(
    navController: NavController,
    loginRegisterViewModel: LoginRegisterViewModel,
    buttonText: String = "Sign in with Google"
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showErrorDialog by remember { mutableStateOf(false) }

    // Show error dialog if there's an error
    if (showErrorDialog && errorMessage != null) {
        AlertDialogPopUp(
            onDismissRequest = {
                showErrorDialog = false
                errorMessage = null
            },
            dialogTitle = "Google Sign In Error",
            dialogText = errorMessage ?: "Unknown error occurred",
            icon = Icons.Default.Info
        )
    }

    val onClick: () -> Unit = {
        val credentialManager = CredentialManager.create(context)

        // Generate a nonce and hash it with sha-256
        // Providing a nonce is optional but recommended
        val rawNonce = UUID.randomUUID().toString() // Generate a random String. UUID should be sufficient, but can also be any other random string.
        val bytes = rawNonce.toString().toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) } // Hashed nonce to be passed to Google sign-in


        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(BuildConfig.GOOGLE_CLIENT_ID)
            .setNonce(hashedNonce) // Provide the nonce if you have one
            .build()

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        coroutineScope.launch {
            loginRegisterViewModel.isGoogleLoading = true
            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = context,
                )

                val googleIdTokenCredential = GoogleIdTokenCredential
                    .createFrom(result.credential.data)

                val googleIdToken = googleIdTokenCredential.idToken

                SupabaseClient.client.auth.signInWith(IDToken) {
                    idToken = googleIdToken
                    provider = Google
                    nonce = rawNonce
                }

                // Navigate to dashboard on successful sign-in
                navController.navigate("dashboard_screen") {
                    popUpTo("main_screen") { inclusive = true }
                }
            } catch (e: GetCredentialException) {
                // Handle GetCredentialException thrown by `credentialManager.getCredential()`
                android.util.Log.e("GoogleSignIn", "GetCredentialException: ${e.message}", e)
                errorMessage = "Failed to get credentials: ${e.message}"
                showErrorDialog = true
            } catch (e: GoogleIdTokenParsingException) {
                // Handle GoogleIdTokenParsingException thrown by `GoogleIdTokenCredential.createFrom()`
                android.util.Log.e("GoogleSignIn", "GoogleIdTokenParsingException: ${e.message}", e)
                errorMessage = "Failed to parse Google ID token: ${e.message}"
                showErrorDialog = true
            } catch (e: RestException) {
                // Handle RestException thrown by Supabase
                android.util.Log.e("GoogleSignIn", "RestException: ${e.message}", e)
                errorMessage = "Supabase authentication failed: ${e.message}"
                showErrorDialog = true
            } catch (e: Exception) {
                // Handle unknown exceptions
                android.util.Log.e("GoogleSignIn", "Unknown exception: ${e.message}", e)
                errorMessage = "Sign in failed: ${e.message}"
                showErrorDialog = true
            } finally {
                loginRegisterViewModel.isGoogleLoading = false
            }
        }
    }

    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = Color.White,
            contentColor = Color(0xFF757575)
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDADADA)),
        enabled = !loginRegisterViewModel.isGoogleLoading && !loginRegisterViewModel.isEmailLoading
    ) {
        Row(
            modifier = Modifier.padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (loginRegisterViewModel.isGoogleLoading) {
                 CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color(0xFF757575)
                )
            } else {
                Icon(
                    painter = painterResource(id = R.drawable.google_logo),
                    contentDescription = "Google logo",
                    modifier = Modifier.size(20.dp),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = buttonText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
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