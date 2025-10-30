package com.example.bloom

import android.text.Layout
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bloom.ui.theme.BloomTheme

@Composable
fun Navigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "main_screen") {
        composable("main_screen") {
            MainScreen(navController)
        }
        composable("login_screen") {
            LoginScreen(navController)
        }
        composable("register_screen") {
            RegisterScreen(navController)
        }
        // Add other composable destinations here
    }
}

@Composable
fun MainScreen(navController: NavController) {
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        //main screen content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.bloom_icon),
                contentDescription = "Bloom Logo",
                modifier = Modifier.size(84.dp)
            )
            Text("Welcome to Bloom!")

            //buttons to navigate to login and register screens
            Button(
                onClick = { navController.navigate("login_screen") }
            ) {
                Text("Login")
            }

            Button(
                onClick = { navController.navigate("register_screen") }
            ) {
                Text(text = "Register")
            }
        }
    }
}


@Preview
@Composable
fun MainScreenPreview() {
    BloomTheme {
        MainScreen(navController = rememberNavController())
    }
}