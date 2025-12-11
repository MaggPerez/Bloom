package com.example.bloom

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bloom.datamodels.AIFeatureDataModel
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

    //state for bottom sheet
    var showHealthScore by remember { mutableStateOf(false) }
    var showBillPredictionSheet by remember { mutableStateOf(false) }



    //defining AI features
    val aiFeatures = AIFeatureDataModel.aiFeatures


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

                Spacer(modifier = Modifier.height(24.dp))

                // Vertical stack of A.I feature cards
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    aiFeatures.forEach { feature ->
                        AIFeatureCard(
                            feature = feature,
                            // todo add missing routes
                            onClick = { navController.navigate(feature.route ?: "bloom_ai_screen") }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun AIFeatureCard(
    feature: AIFeatureDataModel.AIFeature,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth()
            .height(160.dp).clickable(onClick = onClick),

        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),

        border = BorderStroke(
            1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),

        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {

        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            //icon with colored background
            Box(
                modifier = Modifier.size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(feature.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {

                //icon
                when(feature.icon) {
                    is ImageVector -> {
                        Icon(
                            imageVector = feature.icon,
                            contentDescription = feature.title,
                            tint = feature.color,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    is Int -> {
                        Icon(
                            painter = painterResource(id = feature.icon),
                            contentDescription = feature.title,
                            tint = feature.color,
                        )
                    }
                }
            }


            //title and description
            Column{
                Text(
                    text = feature.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = feature.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    maxLines = 2
                )
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