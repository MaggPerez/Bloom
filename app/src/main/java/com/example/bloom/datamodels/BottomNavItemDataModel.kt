package com.example.bloom.datamodels

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.vector.ImageVector

class BottomNavItemDataModel {
    data class BottomNavItem(
        val route: String,
        val label: String,
        val icon: ImageVector
    )

    companion object {
        val nav_items = listOf<BottomNavItem>(
            BottomNavItem("dashboard_screen", "Dashboard", Icons.Default.Home),
            BottomNavItem("budget_screen", "Budget", Icons.Default.AccountBox),
            BottomNavItem("ai_screen", "A.I Help", Icons.Default.Build)
        )
    }

}