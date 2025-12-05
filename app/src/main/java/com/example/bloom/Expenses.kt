package com.example.bloom

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

//todo: create Expenses UI
@Composable
fun ExpensesScreen(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Surface(){
        Column() {
            Text("Hi")
        }
    }
}


@Preview (showBackground = true)
@Composable
fun ExpensesScreenPreview() {
    ExpensesScreen(navController = rememberNavController())
}