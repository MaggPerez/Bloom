package com.example.bloom.datamodels

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.bloom.R

class BottomNavItemDataModel {
    data class BottomNavItem(
        val route: String,
        val label: String,
        val icon: Any,
        val color: Color
    )

    companion object {
        val nav_items = listOf(
            BottomNavItem("dashboard_screen", "Dashboard", R.drawable.home_24px, Color(0xFF10B981)),
            BottomNavItem("budget_screen", "Budget", R.drawable.account_balance_wallet_24px, Color(0xFF3B82F6)),
            BottomNavItem("ai_screen", "Bloom A.I", R.drawable.robot_2_24px, Color(0xFF8B5CF6))
        )
    }

}