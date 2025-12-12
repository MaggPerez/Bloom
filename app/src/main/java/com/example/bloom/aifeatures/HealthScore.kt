package com.example.bloom.aifeatures

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.bloom.BottomNavigationBar
import com.example.bloom.LinearProgressBar
import com.example.bloom.ui.theme.BloomTheme
import com.example.bloom.viewmodel.HealthScoreViewModel

@Composable
fun HealthScoreScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    viewModel: HealthScoreViewModel = viewModel()
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val scrollState = rememberScrollState()

    // Load cached health score when the screen is first composed
    LaunchedEffect(Unit) {
        viewModel.loadCachedHealthScore()
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
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }

                        Text(
                            text = "Health Score",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Loading state
                if (viewModel.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    // Score gauge card
                    HealthScoreGaugeCard(
                        score = viewModel.healthScore,
                        rating = viewModel.scoreRating,
                        scoreColor = Color(viewModel.scoreColor),
                        isLoadingAI = viewModel.isLoadingAI
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // AI Generation Button
                    Button(
                        onClick = { viewModel.generateAIHealthScore() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !viewModel.isLoadingAI,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8B5CF6),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (viewModel.isLoadingAI) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                            Text("Generating AI Analysis...")
                        } else {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = "Generate AI Analysis",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                            Text(if (viewModel.aiRecommendations == null) "Generate AI Health Analysis" else "Regenerate AI Analysis")
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Score breakdown
                    Text(
                        text = "Score Breakdown",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    ScoreBreakdownCard(
                        budgetAdherenceScore = viewModel.budgetAdherenceScore,
                        savingsRateScore = viewModel.savingsRateScore,
                        spendingConsistencyScore = viewModel.spendingConsistencyScore,
                        emergencyFundScore = viewModel.emergencyFundScore
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // A.I - Powered Recommendations
                    Text(
                        text = "AI-Powered Recommendations",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    RecommendationsCard(recommendations = viewModel.recommendations)

                }

                // Error message
                viewModel.errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = error,
                        color = Color(0xFFEF4444),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Composable
fun HealthScoreGaugeCard(
    score: Int,
    rating: String,
    scoreColor: Color,
    isLoadingAI: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Your Financial Health",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Circular gauge with loading overlay
            Box(contentAlignment = Alignment.Center) {
                CircularHealthGauge(
                    percentage = score.toFloat(),
                    color = scoreColor,
                    modifier = Modifier.size(200.dp)
                )

                if (isLoadingAI) {
                    // Loading overlay
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .background(
                                MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = Color(0xFF8B5CF6),
                                strokeWidth = 4.dp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Analyzing...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF8B5CF6),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$score",
                            style = MaterialTheme.typography.displayLarge,
                            fontWeight = FontWeight.Bold,
                            color = scoreColor
                        )
                        Text(
                            text = "out of 100",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Rating badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(scoreColor.copy(alpha = 0.15f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (isLoadingAI) "Calculating..." else rating,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = scoreColor
                )
            }
        }
    }
}

@Composable
fun CircularHealthGauge(
    percentage: Float,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 20.dp.toPx()
        val radius = (size.minDimension - strokeWidth) / 2

        // Background arc
        drawArc(
            color = Color.LightGray.copy(alpha = 0.3f),
            startAngle = 135f,
            sweepAngle = 270f,
            useCenter = false,
            topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Progress arc
        val sweepAngle = (percentage / 100f) * 270f
        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(color, color.copy(alpha = 0.7f))
            ),
            startAngle = 135f,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
            size = Size(radius * 2, radius * 2),
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}

@Composable
fun ScoreBreakdownCard(
    budgetAdherenceScore: Int,
    savingsRateScore: Int,
    spendingConsistencyScore: Int,
    emergencyFundScore: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            ScoreBreakdownItem(
                label = "Budget Adherence",
                score = budgetAdherenceScore,
                maxScore = 40,
                icon = Icons.Default.Settings,
                color = Color(0xFF3B82F6)
            )

            Spacer(modifier = Modifier.height(16.dp))

            ScoreBreakdownItem(
                label = "Savings Rate",
                score = savingsRateScore,
                maxScore = 30,
                icon = Icons.Default.Star,
                color = Color(0xFFFBBF24)
            )

            Spacer(modifier = Modifier.height(16.dp))

            ScoreBreakdownItem(
                label = "Spending Consistency",
                score = spendingConsistencyScore,
                maxScore = 20,
                icon = Icons.Default.CheckCircle,
                color = Color(0xFF10B981)
            )

            Spacer(modifier = Modifier.height(16.dp))

            ScoreBreakdownItem(
                label = "Emergency Fund",
                score = emergencyFundScore,
                maxScore = 10,
                icon = Icons.Default.FavoriteBorder,
                color = Color(0xFFEC4899)
            )
        }
    }
}

@Composable
fun ScoreBreakdownItem(
    label: String,
    score: Int,
    maxScore: Int,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = color,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.padding(horizontal = 8.dp))

                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Text(
                text = "$score / $maxScore",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressBar(
            percentage = (score.toFloat() / maxScore * 100),
            color = color
        )
    }
}

@Composable
fun RecommendationsCard(
    recommendations: List<String>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            if (recommendations.isEmpty()) {
                Text(
                    text = "No recommendations available. Add budget and transaction data to get personalized insights!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            } else {
                recommendations.forEachIndexed { index, recommendation ->
                    RecommendationItem(
                        recommendation = recommendation,
                        index = index + 1
                    )

                    if (index < recommendations.size - 1) {
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun RecommendationItem(
    recommendation: String,
    index: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(0xFF8B5CF6).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$index",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8B5CF6)
            )
        }

        Spacer(modifier = Modifier.padding(horizontal = 8.dp))

        Text(
            text = recommendation,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}




@Preview(showBackground = true)
@Composable
fun HealthScoreScreenPreview() {
    BloomTheme {
        HealthScoreScreen(navController = rememberNavController())
    }
}
