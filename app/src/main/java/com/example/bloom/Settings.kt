package com.example.bloom

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bloom.ui.theme.BloomTheme

@Composable
fun SettingsScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Column() {
        Text(
            text = "Hello"
        )
    }
}


@Preview (showBackground = true)
@Composable
fun SettingsPreviewScreen(){
    BloomTheme {
        SettingsScreen(navController = rememberNavController())
    }
}