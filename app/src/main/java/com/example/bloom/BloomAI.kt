package com.example.bloom

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bloom.ui.theme.BloomTheme
import java.time.LocalTime

// Data model for AI features
data class AIFeature(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val actionType: AIActionType
)

enum class AIActionType {
    NAVIGATE_TO_SCREEN,
    SHOW_BOTTOM_SHEET
}

// AI Greeting Header Component
@Composable
fun AIGreetingHeader() {
    val greeting = remember {
        val hour = LocalTime.now().hour
        when {
            hour < 12 -> "Good morning!"
            hour < 18 -> "Good afternoon!"
            else -> "Good evening!"
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = greeting,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Here's how I can help with your finances today",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}

// AI Feature Card Component
@Composable
fun AIFeatureCard(
    feature: AIFeature,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Icon with colored background
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(feature.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = feature.title,
                    tint = feature.color,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Title and Description
            Column {
                Text(
                    text = feature.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
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

@Composable
fun BloomAiScreen(
    navController: NavController,
    modifier: Modifier = Modifier
){
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val scrollState = rememberScrollState()

    // State for bottom sheet
    var showHealthScoreSheet by remember { mutableStateOf(false) }
    var showBillPredictionsSheet by remember { mutableStateOf(false) }

    // Purple color for AI features (matches bottom nav)
    val aiPurple = Color(0xFF8B5CF6)
    val aiBlue = Color(0xFF3B82F6)
    val aiGreen = Color(0xFF10B981)
    val aiOrange = Color(0xFFF97316)
    val aiPink = Color(0xFFEC4899)
    val aiYellow = Color(0xFFFBBF24)

    // Define AI features
    val aiFeatures = remember {
        listOf(
            AIFeature(
                title = "AI Chat",
                description = "Get personalized financial advice and answers to your money questions",
                icon = Icons.Default.Person,
                color = aiPurple,
                actionType = AIActionType.NAVIGATE_TO_SCREEN
            ),
            AIFeature(
                title = "Smart Insights",
                description = "Discover patterns in your spending and get actionable recommendations",
                icon = Icons.Default.Search,
                color = aiBlue,
                actionType = AIActionType.NAVIGATE_TO_SCREEN
            ),
            AIFeature(
                title = "CSV Import",
                description = "Upload your transactions and let AI create your budget automatically",
                icon = Icons.Default.Add,
                color = aiGreen,
                actionType = AIActionType.NAVIGATE_TO_SCREEN
            ),
            AIFeature(
                title = "Health Score",
                description = "See your overall financial wellness score and how to improve it",
                icon = Icons.Default.FavoriteBorder,
                color = aiPink,
                actionType = AIActionType.SHOW_BOTTOM_SHEET
            ),
            AIFeature(
                title = "What-If Scenarios",
                description = "Explore how different financial decisions impact your future",
                icon = Icons.Default.Home,
                color = aiOrange,
                actionType = AIActionType.NAVIGATE_TO_SCREEN
            ),
            AIFeature(
                title = "Bill Predictions",
                description = "AI detects recurring bills and alerts you before they're due",
                icon = Icons.Default.DateRange,
                color = aiYellow,
                actionType = AIActionType.SHOW_BOTTOM_SHEET
            )
        )
    }

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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .windowInsetsPadding(WindowInsets.captionBar)
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
                // AI Greeting Header
                AIGreetingHeader()

                Spacer(modifier = Modifier.height(24.dp))

                // 2x3 Grid of AI Feature Cards
                // Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AIFeatureCard(
                        feature = aiFeatures[0],
                        onClick = {
                            // TODO: Navigate to AI Chat screen
                        },
                        modifier = Modifier.weight(1f)
                    )
                    AIFeatureCard(
                        feature = aiFeatures[1],
                        onClick = {
                            // TODO: Navigate to Smart Insights screen
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AIFeatureCard(
                        feature = aiFeatures[2],
                        onClick = {
                            // TODO: Navigate to CSV Import screen
                        },
                        modifier = Modifier.weight(1f)
                    )
                    AIFeatureCard(
                        feature = aiFeatures[3],
                        onClick = {
                            showHealthScoreSheet = true
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Row 3
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AIFeatureCard(
                        feature = aiFeatures[4],
                        onClick = {
                            // TODO: Navigate to What-If Scenarios screen
                        },
                        modifier = Modifier.weight(1f)
                    )
                    AIFeatureCard(
                        feature = aiFeatures[5],
                        onClick = {
                            showBillPredictionsSheet = true
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }

    // Bottom Sheets (Placeholder implementations)
    if (showHealthScoreSheet) {
        // TODO: Implement Health Score bottom sheet
        // For now, just close it when clicked
        showHealthScoreSheet = false
    }

    if (showBillPredictionsSheet) {
        // TODO: Implement Bill Predictions bottom sheet
        // For now, just close it when clicked
        showBillPredictionsSheet = false
    }
}


@Preview (showBackground = true)
@Composable
fun BloomAiScreenPreview() {
    BloomTheme {
        BloomAiScreen(navController = rememberNavController())
    }
}