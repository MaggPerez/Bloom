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
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bloom.ui.theme.BloomTheme


@Composable
fun RegisterScreen(
    navController: NavController,
    modifier: Modifier = Modifier
){
    val context = LocalContext.current
    var createEmail by remember { mutableStateOf("") }
    var createPassword by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            modifier = Modifier
                .fillMaxHeight()
//                .background(MaterialTheme.colorScheme.background)
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
//                if(onHandleRegister(createEmail, createPassword)){
//                    //navigate to dashboard
//                    navController.navigate("dashboard_screen")
//                }
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
    OutlinedTextField(
        value = textInput,
        onValueChange = onValueChange,
        label = { Text(labelText) },
        singleLine = true,
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