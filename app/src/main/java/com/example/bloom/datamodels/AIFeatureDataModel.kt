package com.example.bloom.datamodels

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

class AIFeatureDataModel {
    data class AIFeature(
        val title: String,
        val description: String,
        val icon: ImageVector,
        val color: Color,
        val actionType: AIActionType,
    )

    data class AIGenerativeDataModel(
        val message: Any
    )


    enum class AIActionType {
        NAVIGATE_TO_SCREEN,
        SHOW_BOTTOM_SHEET
    }
}