package com.example.bloom.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bloom.controllers.AnalyticsController
import com.example.bloom.datamodels.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

/**
 * ViewModel for Analytics screen
 * Manages analytics data state and user interactions
 */
class AnalyticsViewModel : ViewModel() {

    private val analyticsController = AnalyticsController()

    // =====================================================
    // STATE VARIABLES
    // =====================================================

    // Time period filter
    var selectedTimePeriod by mutableStateOf(TimePeriod.MONTHLY)
        private set
    var customStartDate by mutableStateOf("")
    var customEndDate by mutableStateOf("")

    // Analytics data
    var analyticsSummary by mutableStateOf<AnalyticsSummary?>(null)
        private set
    var monthlyTrends by mutableStateOf<List<MonthlyTrendData>>(emptyList())
        private set
    var categoryBreakdown by mutableStateOf<List<CategoryBreakdownData>>(emptyList())
        private set
    var budgetAdherence by mutableStateOf<List<BudgetAdherenceData>>(emptyList())
        private set
    var recurringBills by mutableStateOf<RecurringBillSummary?>(null)
        private set
    var insights by mutableStateOf<SpendingInsights?>(null)
        private set

    // Summary metrics
    var totalIncome by mutableStateOf(0.0)
        private set
    var totalExpenses by mutableStateOf(0.0)
        private set
    var netSavings by mutableStateOf(0.0)
        private set

    // UI state
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set
    var showExportOptions by mutableStateOf(false)
    var isExporting by mutableStateOf(false)
        private set

    // Current date range being displayed
    var currentDateRange by mutableStateOf<DateRange?>(null)
        private set

    init {
        // Load current month analytics by default
        loadAnalytics(TimePeriod.MONTHLY)
    }

    // =====================================================
    // DATA LOADING FUNCTIONS
    // =====================================================

    /**
     * Load analytics for selected time period
     */
    fun loadAnalytics(timePeriod: TimePeriod = selectedTimePeriod) {
        selectedTimePeriod = timePeriod
        val dateRange = calculateDateRange(timePeriod)
        loadAnalyticsForDateRange(dateRange.startDate, dateRange.endDate)
    }

    /**
     * Load analytics for custom date range
     */
    fun loadCustomDateRange() {
        if (customStartDate.isBlank() || customEndDate.isBlank()) {
            errorMessage = "Please select both start and end dates"
            return
        }

        // Validate date format and order
        try {
            val start = LocalDate.parse(customStartDate)
            val end = LocalDate.parse(customEndDate)

            if (start.isAfter(end)) {
                errorMessage = "Start date must be before end date"
                return
            }

            selectedTimePeriod = TimePeriod.CUSTOM
            loadAnalyticsForDateRange(customStartDate, customEndDate)
        } catch (e: Exception) {
            errorMessage = "Invalid date format. Use YYYY-MM-DD"
        }
    }

    /**
     * Core function to load analytics data for a date range
     */
    private fun loadAnalyticsForDateRange(startDate: String, endDate: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                analyticsController.getAnalyticsSummary(startDate, endDate).fold(
                    onSuccess = { summary ->
                        analyticsSummary = summary
                        currentDateRange = summary.dateRange

                        // Update individual state variables for easier access
                        monthlyTrends = summary.monthlyTrends
                        categoryBreakdown = summary.categoryBreakdown
                        budgetAdherence = summary.budgetAdherence
                        recurringBills = summary.recurringBillSummary
                        insights = summary.insights

                        totalIncome = summary.totalIncome
                        totalExpenses = summary.totalExpenses
                        netSavings = summary.netSavings
                    },
                    onFailure = { e ->
                        errorMessage = "Failed to load analytics: ${e.message}"
                    }
                )
            } catch (e: Exception) {
                errorMessage = "Error loading analytics: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Refresh current analytics view
     */
    fun refreshAnalytics() {
        currentDateRange?.let {
            loadAnalyticsForDateRange(it.startDate, it.endDate)
        } ?: loadAnalytics()
    }

    // =====================================================
    // TIME PERIOD SELECTION
    // =====================================================

    /**
     * Switch to a different time period
     */
    fun selectTimePeriod(timePeriod: TimePeriod) {
        if (timePeriod == TimePeriod.CUSTOM) {
            // Just update selection, wait for user to set dates
            selectedTimePeriod = timePeriod
        } else {
            loadAnalytics(timePeriod)
        }
    }

    /**
     * Update custom start date
     */
    fun updateCustomStartDate(date: String) {
        customStartDate = date
    }

    /**
     * Update custom end date
     */
    fun updateCustomEndDate(date: String) {
        customEndDate = date
    }

    // =====================================================
    // EXPORT FUNCTIONS
    // =====================================================

    /**
     * Export analytics as PDF and share
     */
    fun exportAsPDF(context: Context) {
        val summary = analyticsSummary
        if (summary == null) {
            errorMessage = "No data to export"
            return
        }

        viewModelScope.launch {
            isExporting = true
            errorMessage = null

            try {
                analyticsController.exportToPDF(context, summary).fold(
                    onSuccess = { file ->
                        // Share the PDF
                        analyticsController.shareFile(context, file, "application/pdf").fold(
                            onSuccess = { intent ->
                                context.startActivity(intent)
                                showExportOptions = false
                            },
                            onFailure = { e ->
                                errorMessage = "Failed to share PDF: ${e.message}"
                            }
                        )
                    },
                    onFailure = { e ->
                        errorMessage = "Failed to generate PDF: ${e.message}"
                    }
                )
            } catch (e: Exception) {
                errorMessage = "Export error: ${e.message}"
            } finally {
                isExporting = false
            }
        }
    }

    // =====================================================
    // HELPER FUNCTIONS
    // =====================================================

    /**
     * Calculate date range based on time period
     */
    private fun calculateDateRange(timePeriod: TimePeriod): DateRange {
        val today = LocalDate.now()

        return when (timePeriod) {
            TimePeriod.WEEKLY -> {
                val startOfWeek = today.minusDays(today.dayOfWeek.value.toLong() - 1)
                DateRange(
                    startDate = startOfWeek.toString(),
                    endDate = today.toString()
                )
            }
            TimePeriod.MONTHLY -> {
                val startOfMonth = today.withDayOfMonth(1)
                val endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth())
                DateRange(
                    startDate = startOfMonth.toString(),
                    endDate = endOfMonth.toString()
                )
            }
            TimePeriod.YEARLY -> {
                val startOfYear = today.withDayOfYear(1)
                DateRange(
                    startDate = startOfYear.toString(),
                    endDate = today.toString()
                )
            }
            TimePeriod.CUSTOM -> {
                DateRange(
                    startDate = customStartDate.ifBlank { today.toString() },
                    endDate = customEndDate.ifBlank { today.toString() }
                )
            }
        }
    }

    /**
     * Get income vs expense data formatted for charts
     */
    fun getIncomeVsExpenseData(): List<IncomeVsExpenseData> {
        return monthlyTrends.map { trend ->
            IncomeVsExpenseData(
                period = trend.month,
                income = trend.totalIncome,
                expenses = trend.totalExpenses,
                balance = trend.netAmount
            )
        }
    }

    /**
     * Get top N spending categories
     */
    fun getTopCategories(limit: Int = 5): List<CategoryBreakdownData> {
        return categoryBreakdown.take(limit)
    }

    /**
     * Get budget health color for UI
     */
    fun getBudgetHealthColor(): androidx.compose.ui.graphics.Color {
        return insights?.budgetHealthStatus?.color()
            ?: androidx.compose.ui.graphics.Color(0xFF808080)
    }

    /**
     * Get budget health description for UI
     */
    fun getBudgetHealthDescription(): String {
        return insights?.budgetHealthStatus?.description() ?: "Loading..."
    }

    /**
     * Get upcoming bills (next 7 days)
     */
    fun getUpcomingBills(daysAhead: Int = 7): List<UpcomingBill> {
        return recurringBills?.upcomingBills?.filter { it.daysUntilDue <= daysAhead } ?: emptyList()
    }

    /**
     * Check if there's any data to display
     */
    fun hasData(): Boolean {
        return analyticsSummary != null &&
                (monthlyTrends.isNotEmpty() || categoryBreakdown.isNotEmpty())
    }

    // =====================================================
    // UI STATE MANAGEMENT
    // =====================================================

    /**
     * Clear error message
     */
    fun clearError() {
        errorMessage = null
    }

    /**
     * Toggle export options dialog
     */
    fun toggleExportOptions() {
        showExportOptions = !showExportOptions
    }

    /**
     * Format currency for display
     */
    fun formatCurrency(amount: Double): String {
        return "$${String.format("%.2f", amount)}"
    }

    /**
     * Get formatted date range for display
     */
    fun getFormattedDateRange(): String {
        return currentDateRange?.let {
            "${it.startDate} to ${it.endDate}"
        } ?: "No data"
    }

    /**
     * Get summary statistics for quick view
     */
    fun getSummaryStats(): Triple<String, String, String> {
        return Triple(
            formatCurrency(totalIncome),
            formatCurrency(totalExpenses),
            formatCurrency(netSavings)
        )
    }
}
