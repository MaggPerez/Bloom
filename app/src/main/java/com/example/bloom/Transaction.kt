package com.example.bloom

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.bloom.ui.theme.BloomTheme
import com.example.bloom.viewmodel.TransactionViewModel

@Composable
fun TransactionScreen(
    navController: NavController,
//    transactionViewModel: TransactionViewModel,
    modifier: Modifier = Modifier
){
    Column(
        modifier = modifier.fillMaxSize()
    ) {
        Text(text = "Transaction Screen")
    }
}


@Preview (showBackground = true)
@Composable
fun TransactionScreenPreview(){
    BloomTheme {
        TransactionScreen(navController = rememberNavController())
    }
}