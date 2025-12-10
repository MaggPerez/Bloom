package com.example.bloom

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bloom.aifeatures.AiChatbotScreen
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
        composable("dashboard_screen") {
            DashboardScreen(navController)
        }

        composable("budget_screen") {
            BudgetScreen(navController)
        }

        composable("bloom_ai_screen") {
            BloomAiScreen(navController)
        }

        composable("transaction_screen"){
            TransactionScreen(navController)
        }

        composable("settings_screen") {
            SettingsScreen(navController)
        }

        composable("aichatbot_screen"){
            AiChatbotScreen(navController)
        }

        composable("financials_screen"){
            FinancialsScreen(navController)
        }

        composable("income_screen") {
            IncomeScreen(navController)
        }


        composable("expenses_screen") {
            ExpensesScreen(navController)
        }

        composable("analytics_screen"){
            AnalyticsScreen(navController)
        }

        composable("health_score_screen") {
            com.example.bloom.aifeatures.HealthScoreScreen(navController)
        }

        composable("csv_import_screen"){
            com.example.bloom.aifeatures.CsvImportScreen(navController)
        }

        composable("smart_insights_screen") {
            com.example.bloom.aifeatures.SmartInsightsScreen(navController)
        }

    }
}

@Composable
fun MainScreen(navController: NavController) {
    // Single opacity animation for everything - 0 to 1
    val contentAlpha = remember { Animatable(0f) }

    // Trigger fade-in on launch
    LaunchedEffect(Unit) {
        contentAlpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1000 // Smooth 1 second fade in
            )
        )
    }

    // Primary color
    val primaryPurple = Color(0xFF8B5CF6)

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Diagonal purple shape from top-left with circular corner
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val width = size.width
                val height = size.height

                val path = Path().apply {
                    // Start from top-left corner
                    moveTo(0f, 0f)
                    // Line to top-right
                    lineTo(width, 0f)
                    // Diagonal line down to bottom-left area
                    lineTo(width, height * 0.35f)
                    // Curved line using quadratic bezier for smooth circular feel
                    quadraticTo(
                        x1 = width * 0.5f,  // Control point X
                        y1 = height * 0.55f, // Control point Y
                        x2 = 0f,             // End point X
                        y2 = height * 0.45f  // End point Y
                    )
                    // Close back to start
                    close()
                }

                drawPath(
                    path = path,
                    color = primaryPurple
                )
            }

            // All content fades in together
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .alpha(contentAlpha.value),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Bottom
            ) {
                // Logo
                Image(
                    painter = painterResource(id = R.drawable.bloom_icon),
                    contentDescription = "Bloom Logo",
                    modifier = Modifier.size(100.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Welcome Text
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "Welcome to",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Bloom",
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Take control of your finances\nwith smart budgeting and\nAI-powered insights",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        textAlign = TextAlign.End,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.3
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                // Buttons
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 48.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Login Button - Primary filled style
                    Button(
                        onClick = { navController.navigate("login_screen") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryPurple,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Login",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Register Button - Outlined style
                    OutlinedButton(
                        onClick = { navController.navigate("register_screen") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            width = 1.5.dp,
                            color = primaryPurple
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = primaryPurple
                        )
                    ) {
                        Text(
                            text = "Create Account",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
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