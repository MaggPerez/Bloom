package com.example.bloom

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bloom.retrofitapi.RetrofitInstance
import com.example.bloom.ui.theme.BloomTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun BloomAiScreen(
    navController: NavController,
    modifier: Modifier = Modifier
){
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var geminiResponseText by remember { mutableStateOf<Any>("Click to get gemini response") }


    Scaffold(
        bottomBar = {
            BottomNavigationBar(
                navController = navController,
                currentRoute = currentRoute
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .windowInsetsPadding(WindowInsets.captionBar)
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                Text(text = "Bloom A.I",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Text(text = "Gemini Text: $geminiResponseText")
                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                geminiResponseText = "Loading..."
                                val data = RetrofitInstance.instance.getGeminiResponse()
                                geminiResponseText = data.message
                            }
                            catch (e: Exception) {
                                geminiResponseText = "Error: ${e.message}"
                                e.printStackTrace()
                            }
                        }
                    }
                ) {
                    Text(text = "Get Gemini Response")
                }
            }
        }
    }
}


@Preview (showBackground = true)
@Composable
fun BloomAiScreenPreview() {
    BloomTheme {
        BloomAiScreen(navController = rememberNavController())
    }
}