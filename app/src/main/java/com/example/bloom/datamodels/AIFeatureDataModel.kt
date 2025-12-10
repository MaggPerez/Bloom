package com.example.bloom.datamodels

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.bloom.R

class AIFeatureDataModel {
    data class AIFeature(
        val title: String,
        val description: String,
        val icon: Any,
        val color: Color,
        val actionType: AIActionType,
        val route: String? = null
    )

    data class AIGenerativeDataModel(
        val message: String
    )

    data class AIHealthScoreResponse(
        val score: Int,
        val budgetAdherenceScore: Int = 0,
        val savingsRateScore: Int = 0,
        val spendingConsistencyScore: Int = 0,
        val emergencyFundScore: Int = 0,
        val recommendations: String,
        val message: String
    )

    data class ChatRequest(
        val message: String
    )


    enum class AIActionType {
        NAVIGATE_TO_SCREEN,
        SHOW_BOTTOM_SHEET
    }

    companion object {
        //purple color for AI features (matches bottom nav)
        val aiPurple = Color(0xFF8B5CF6)
        val aiBlue = Color(0xFF3B82F6)
        val aiGreen = Color(0xFF10B981)
        val aiOrange = Color(0xFFF97316)
        val aiPink = Color(0xFFEC4899)
        val aiYellow = Color(0xFFFBBF24)



        val aiFeatures = listOf(
            AIFeature(
                title = "AI Chat",
                description = "Get personalized financial advice and answers to your money questions",
                icon = R.drawable.robot_2_24px,
                color = aiPurple,
                actionType = AIFeatureDataModel.AIActionType.NAVIGATE_TO_SCREEN,
                route = "aichatbot_screen"
            ),

            AIFeature(
                title = "Smart Insights",
                description = "Discover patterns in your spending and get actionable recommendations",
                icon = Icons.Default.Search,
                color = aiPurple,
                actionType = AIFeatureDataModel.AIActionType.NAVIGATE_TO_SCREEN,
                route = "smart_insights_screen"
            ),
            AIFeature(
                title = "CSV Import",
                description = "Upload your transactions and let AI create your budget automatically",
                icon = Icons.Default.Add,
                color = aiPurple,
                actionType = AIActionType.NAVIGATE_TO_SCREEN,
                route = "csv_import_screen"
            ),
            AIFeature(
                title = "Health Score",
                description = "See your overall financial wellness score and how to improve it",
                icon = Icons.Default.FavoriteBorder,
                color = aiPurple,
                actionType = AIActionType.NAVIGATE_TO_SCREEN,
                route = "health_score_screen"
            ),
            AIFeature(
                title = "What-If Scenarios",
                description = "Explore how different financial decisions impact your future",
                icon = Icons.Default.Home,
                color = aiPurple,
                actionType = AIActionType.NAVIGATE_TO_SCREEN
            ),
            AIFeature(
                title = "Bill Predictions",
                description = "AI detects recurring bills and alerts you before they're due",
                icon = Icons.Default.DateRange,
                color = aiPurple,
                actionType = AIActionType.NAVIGATE_TO_SCREEN,
                route = "health_score_screen"
            )
        )
    }
}