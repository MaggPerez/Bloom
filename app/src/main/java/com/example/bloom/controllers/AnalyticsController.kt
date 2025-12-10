package com.example.bloom.controllers

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.Color
import androidx.core.content.FileProvider
import com.example.bloom.SupabaseClient
import com.example.bloom.datamodels.*
import com.itextpdf.text.Document
import com.itextpdf.text.Element
import com.itextpdf.text.Font
import com.itextpdf.text.PageSize
import com.itextpdf.text.Paragraph
import com.itextpdf.text.pdf.PdfPTable
import com.itextpdf.text.pdf.PdfWriter
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.*
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class AnalyticsController {
    private val supabase = SupabaseClient.client

    /**
     * Get current authenticated user ID
     */
    private fun getUserId(): String? {
        return supabase.auth.currentUserOrNull()?.id
    }

    // =====================================================
    // CORE DATA FETCHING
    // =====================================================

    /**
     * Fetch comprehensive analytics summary for a date range
     * This is the main entry point for analytics data
     */
    suspend fun getAnalyticsSummary(
        startDate: String,
        endDate: String
    ): Result<AnalyticsSummary> {
        return withContext(Dispatchers.IO) {
            try {
                val userId = getUserId()
                    ?: return@withContext Result.failure(Exception("User not authenticated"))

                // Fetch all required data
                val monthlyTrends = fetchMonthlyTrends(userId, startDate, endDate)
                val categoryBreakdown = fetchCategoryBreakdown(userId, startDate, endDate)
                val budgetAdherence = fetchBudgetAdherence(userId, startDate, endDate)
                val recurringBills = fetchRecurringBills(userId)
                val totals = calculateTotals(userId, startDate, endDate)
                val insights = generateInsights(userId, startDate, endDate, categoryBreakdown, totals)

                val summary = AnalyticsSummary(
                    dateRange = DateRange(startDate, endDate),
                    totalIncome = totals.first,
                    totalExpenses = totals.second,
                    netSavings = totals.first - totals.second,
                    monthlyTrends = monthlyTrends,
                    categoryBreakdown = categoryBreakdown,
                    budgetAdherence = budgetAdherence,
                    recurringBillSummary = recurringBills,
                    insights = insights
                )

                Result.success(summary)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Fetch monthly income/expense trends from expenses and income tables
     */
    private suspend fun fetchMonthlyTrends(
        userId: String,
        startDate: String,
        endDate: String
    ): List<MonthlyTrendData> {
        return try {
            val start = LocalDate.parse(startDate)
            val end = LocalDate.parse(endDate)

            // Fetch all expenses in date range
            val expenses = supabase.from("expenses")
                .select() {
                    filter {
                        eq("user_id", userId)
                        gte("due_date", startDate)
                        lte("due_date", endDate)
                    }
                }
                .decodeList<ExpenseData>()

            // Fetch all income in date range
            val income = supabase.from("income")
                .select() {
                    filter {
                        eq("user_id", userId)
                        gte("income_date", startDate)
                        lte("income_date", endDate)
                    }
                }
                .decodeList<IncomeData>()

            // Group by month/year
            val monthlyData = mutableMapOf<Pair<Int, Int>, MutableMap<String, Double>>()

            // Process expenses
            expenses.forEach { expense ->
                val date = LocalDate.parse(expense.due_date)
                val key = Pair(date.monthValue, date.year)
                val data = monthlyData.getOrPut(key) { mutableMapOf("income" to 0.0, "expenses" to 0.0, "count" to 0.0) }
                data["expenses"] = (data["expenses"] ?: 0.0) + expense.amount
                data["count"] = (data["count"] ?: 0.0) + 1
            }

            // Process income
            income.forEach { inc ->
                val date = LocalDate.parse(inc.income_date)
                val key = Pair(date.monthValue, date.year)
                val data = monthlyData.getOrPut(key) { mutableMapOf("income" to 0.0, "expenses" to 0.0, "count" to 0.0) }
                data["income"] = (data["income"] ?: 0.0) + inc.amount
                data["count"] = (data["count"] ?: 0.0) + 1
            }

            // Convert to MonthlyTrendData
            monthlyData.map { (monthYear, data) ->
                MonthlyTrendData(
                    month = formatMonthYear(monthYear.first, monthYear.second),
                    monthYear = monthYear,
                    totalIncome = data["income"] ?: 0.0,
                    totalExpenses = data["expenses"] ?: 0.0,
                    netAmount = (data["income"] ?: 0.0) - (data["expenses"] ?: 0.0),
                    transactionCount = data["count"]?.toInt() ?: 0
                )
            }.sortedBy { it.monthYear.second * 12 + it.monthYear.first }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Fetch category breakdown with percentages
     * Groups expenses by name for category-like breakdown
     */
    private suspend fun fetchCategoryBreakdown(
        userId: String,
        startDate: String,
        endDate: String
    ): List<CategoryBreakdownData> {
        return try {
            // Fetch expenses in date range
            val expenses = supabase.from("expenses")
                .select() {
                    filter {
                        eq("user_id", userId)
                        gte("due_date", startDate)
                        lte("due_date", endDate)
                    }
                }
                .decodeList<ExpenseData>()

            // Group by expense name (treating each expense type as a category)
            val categoryMap = mutableMapOf<String, MutableList<Double>>()
            val categoryColors = mutableMapOf<String, String>()

            expenses.forEach { expense ->
                val categoryName = expense.name
                categoryMap.getOrPut(categoryName) { mutableListOf() }.add(expense.amount)
                // Use expense color if available, otherwise use default
                categoryColors[categoryName] = expense.color_hex ?: "#4CAF50"
            }

            val totalSpending = categoryMap.values.flatten().sum()

            categoryMap.map { (name, amounts) ->
                val total = amounts.sum()
                CategoryBreakdownData(
                    categoryId = name, // Using name as ID since we don't have category IDs
                    categoryName = name,
                    categoryColor = parseColor(categoryColors[name] ?: "#4CAF50"),
                    totalSpent = total,
                    percentage = if (totalSpending > 0) ((total / totalSpending) * 100).toFloat() else 0f,
                    transactionCount = amounts.size,
                    averageTransaction = if (amounts.isNotEmpty()) total / amounts.size else 0.0
                )
            }.sortedByDescending { it.totalSpent }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Fetch budget adherence data from budget_summary table
     */
    private suspend fun fetchBudgetAdherence(
        userId: String,
        startDate: String,
        endDate: String
    ): List<BudgetAdherenceData> {
        return try {
            val start = LocalDate.parse(startDate)
            val end = LocalDate.parse(endDate)

            // Fetch budget summaries
            val budgets = supabase.from("budget_summary")
                .select() {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<BudgetSummaryData>()
                .filter { isInDateRange(it.month, it.year, startDate, endDate) }

            // Fetch expenses and group by month
            val expenses = supabase.from("expenses")
                .select() {
                    filter {
                        eq("user_id", userId)
                        gte("due_date", startDate)
                        lte("due_date", endDate)
                    }
                }
                .decodeList<ExpenseData>()

            val expensesByMonth = expenses.groupBy { expense ->
                val date = LocalDate.parse(expense.due_date)
                Pair(date.monthValue, date.year)
            }.mapValues { (_, expenseList) -> expenseList.sumOf { it.amount } }

            budgets.map { budget ->
                val key = Pair(budget.month, budget.year)
                val actualSpent = expensesByMonth[key] ?: 0.0
                val difference = budget.monthly_budget - actualSpent
                val percentage = if (budget.monthly_budget > 0) {
                    ((actualSpent / budget.monthly_budget) * 100).toFloat()
                } else 0f

                BudgetAdherenceData(
                    month = formatMonthYear(budget.month, budget.year),
                    budgetAmount = budget.monthly_budget,
                    actualSpent = actualSpent,
                    difference = difference,
                    adherencePercentage = percentage,
                    isOverBudget = actualSpent > budget.monthly_budget
                )
            }.sortedBy { it.month }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Fetch recurring bills from expenses table
     */
    private suspend fun fetchRecurringBills(userId: String): RecurringBillSummary {
        return try {
            val response = supabase.from("expenses")
                .select() {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<ExpenseData>()
                .filter { !it.recurring_frequency.isNullOrBlank() }

            val today = LocalDate.now()
            val upcomingBills = response.map { expense ->
                val dueDate = LocalDate.parse(expense.due_date)
                val daysUntil = ChronoUnit.DAYS.between(today, dueDate).toInt()

                UpcomingBill(
                    name = expense.name,
                    amount = expense.amount,
                    dueDate = expense.due_date,
                    frequency = expense.recurring_frequency ?: "one-time",
                    daysUntilDue = daysUntil
                )
            }.sortedBy { it.daysUntilDue }

            val totalRecurring = response.sumOf { it.amount }
            val byFrequency = response
                .groupBy { it.recurring_frequency ?: "one-time" }
                .mapValues { (_, bills) -> bills.sumOf { it.amount } }

            RecurringBillSummary(
                totalRecurringAmount = totalRecurring,
                upcomingBills = upcomingBills,
                recurringByFrequency = byFrequency
            )
        } catch (e: Exception) {
            RecurringBillSummary(
                totalRecurringAmount = 0.0,
                upcomingBills = emptyList(),
                recurringByFrequency = emptyMap()
            )
        }
    }

    /**
     * Calculate total income and expenses for date range
     */
    private suspend fun calculateTotals(
        userId: String,
        startDate: String,
        endDate: String
    ): Pair<Double, Double> {
        return try {
            // Fetch all expenses in date range
            val expenses = supabase.from("expenses")
                .select() {
                    filter {
                        eq("user_id", userId)
                        gte("due_date", startDate)
                        lte("due_date", endDate)
                    }
                }
                .decodeList<ExpenseData>()

            // Fetch all income in date range
            val income = supabase.from("income")
                .select() {
                    filter {
                        eq("user_id", userId)
                        gte("income_date", startDate)
                        lte("income_date", endDate)
                    }
                }
                .decodeList<IncomeData>()

            val totalIncome = income.sumOf { it.amount }
            val totalExpenses = expenses.sumOf { it.amount }

            Pair(totalIncome, totalExpenses)
        } catch (e: Exception) {
            Pair(0.0, 0.0)
        }
    }

    /**
     * Generate spending insights for target audience
     */
    private suspend fun generateInsights(
        userId: String,
        startDate: String,
        endDate: String,
        categoryBreakdown: List<CategoryBreakdownData>,
        totals: Pair<Double, Double>
    ): SpendingInsights {
        val start = LocalDate.parse(startDate)
        val end = LocalDate.parse(endDate)
        val daysBetween = ChronoUnit.DAYS.between(start, end) + 1

        val avgDaily = if (daysBetween > 0) totals.second / daysBetween else 0.0

        // Get largest transaction
        val largestTxn = getLargestTransaction(userId, startDate, endDate)

        // Determine budget health
        val budgetHealth = determineBudgetHealth(userId)

        return SpendingInsights(
            topCategory = categoryBreakdown.firstOrNull(),
            averageDailySpending = avgDaily,
            largestTransaction = largestTxn,
            mostFrequentCategory = categoryBreakdown.maxByOrNull { it.transactionCount }?.categoryName,
            budgetHealthStatus = budgetHealth
        )
    }

    /**
     * Get largest expense for the period
     */
    private suspend fun getLargestTransaction(
        userId: String,
        startDate: String,
        endDate: String
    ): TransactionSummary? {
        return try {
            // Fetch all expenses in date range
            val expenses = supabase.from("expenses")
                .select() {
                    filter {
                        eq("user_id", userId)
                        gte("due_date", startDate)
                        lte("due_date", endDate)
                    }
                }
                .decodeList<ExpenseData>()

            // Find the largest expense
            val largestExpense = expenses.maxByOrNull { it.amount } ?: return null

            TransactionSummary(
                amount = largestExpense.amount,
                categoryName = largestExpense.name, // Using expense name as category
                date = largestExpense.due_date,
                description = largestExpense.tags
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Determine budget health status
     */
    private suspend fun determineBudgetHealth(userId: String): BudgetHealthStatus {
        return try {
            val budgetController = BudgetController()

            val budget = budgetController.getCurrentMonthBudget().getOrNull()
                ?: return BudgetHealthStatus.EXCELLENT

            val spent = budgetController.getTotalSpentThisMonth().getOrNull() ?: 0.0

            val percentage = if (budget.monthly_budget > 0) {
                (spent / budget.monthly_budget) * 100
            } else 0.0

            when {
                percentage < 70 -> BudgetHealthStatus.EXCELLENT
                percentage < 85 -> BudgetHealthStatus.GOOD
                percentage < 95 -> BudgetHealthStatus.WARNING
                else -> BudgetHealthStatus.CRITICAL
            }
        } catch (e: Exception) {
            BudgetHealthStatus.EXCELLENT
        }
    }

    // =====================================================
    // EXPORT FUNCTIONS
    // =====================================================

    /**
     * Export analytics as PDF report
     */
    suspend fun exportToPDF(
        context: Context,
        summary: AnalyticsSummary
    ): Result<File> {
        return withContext(Dispatchers.IO) {
            try {
                val fileName = "bloom_analytics_${LocalDate.now()}.pdf"
                val file = File(context.getExternalFilesDir(null), fileName)

                val document = Document(PageSize.A4)
                PdfWriter.getInstance(document, file.outputStream())
                document.open()

                // Add title
                val titleFont = Font(Font.FontFamily.HELVETICA, 24f, Font.BOLD)
                val title = Paragraph("Bloom Analytics Report", titleFont)
                title.alignment = Element.ALIGN_CENTER
                document.add(title)
                document.add(Paragraph(" "))

                // Date range
                val dateFont = Font(Font.FontFamily.HELVETICA, 12f)
                document.add(Paragraph("Period: ${summary.dateRange.startDate} to ${summary.dateRange.endDate}", dateFont))
                document.add(Paragraph(" "))

                // Financial Summary
                addSectionHeader(document, "Financial Summary")
                addKeyValuePair(document, "Total Income", "$${String.format("%.2f", summary.totalIncome)}")
                addKeyValuePair(document, "Total Expenses", "$${String.format("%.2f", summary.totalExpenses)}")
                addKeyValuePair(document, "Net Savings", "$${String.format("%.2f", summary.netSavings)}")
                document.add(Paragraph(" "))

                // Category Breakdown
                if (summary.categoryBreakdown.isNotEmpty()) {
                    addSectionHeader(document, "Spending by Category")
                    val categoryTable = PdfPTable(3)
                    categoryTable.widthPercentage = 100f
                    categoryTable.addCell("Category")
                    categoryTable.addCell("Amount")
                    categoryTable.addCell("Percentage")

                    summary.categoryBreakdown.take(10).forEach { cat ->
                        categoryTable.addCell(cat.categoryName)
                        categoryTable.addCell("$${String.format("%.2f", cat.totalSpent)}")
                        categoryTable.addCell("${String.format("%.1f", cat.percentage)}%")
                    }
                    document.add(categoryTable)
                    document.add(Paragraph(" "))
                }

                // Monthly Trends
                if (summary.monthlyTrends.isNotEmpty()) {
                    addSectionHeader(document, "Monthly Trends")
                    val trendTable = PdfPTable(4)
                    trendTable.widthPercentage = 100f
                    trendTable.addCell("Month")
                    trendTable.addCell("Income")
                    trendTable.addCell("Expenses")
                    trendTable.addCell("Net")

                    summary.monthlyTrends.forEach { trend ->
                        trendTable.addCell(trend.month)
                        trendTable.addCell("$${String.format("%.2f", trend.totalIncome)}")
                        trendTable.addCell("$${String.format("%.2f", trend.totalExpenses)}")
                        trendTable.addCell("$${String.format("%.2f", trend.netAmount)}")
                    }
                    document.add(trendTable)
                    document.add(Paragraph(" "))
                }

                // Insights
                addSectionHeader(document, "Insights")
                summary.insights.topCategory?.let {
                    addKeyValuePair(document, "Top Spending Category", "${it.categoryName} ($${String.format("%.2f", it.totalSpent)})")
                }
                addKeyValuePair(document, "Average Daily Spending", "$${String.format("%.2f", summary.insights.averageDailySpending)}")
                addKeyValuePair(document, "Budget Health", summary.insights.budgetHealthStatus.displayName())

                // Recurring Bills
                if (summary.recurringBillSummary.totalRecurringAmount > 0) {
                    document.add(Paragraph(" "))
                    addSectionHeader(document, "Recurring Bills")
                    addKeyValuePair(document, "Total Monthly Recurring", "$${String.format("%.2f", summary.recurringBillSummary.totalRecurringAmount)}")
                }

                document.add(Paragraph(" "))
                document.add(Paragraph(
                    "Generated by Bloom Finance on ${LocalDate.now()}",
                    Font(Font.FontFamily.HELVETICA, 8f, Font.ITALIC)
                ))

                document.close()
                Result.success(file)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Share exported file via Android Share Sheet
     */
    fun shareFile(context: Context, file: File, mimeType: String = "application/pdf"): Result<Intent> {
        return try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Bloom Analytics Report")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooserIntent = Intent.createChooser(shareIntent, "Share Analytics Report")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            Result.success(chooserIntent)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =====================================================
    // HELPER FUNCTIONS
    // =====================================================

    /**
     * Check if a month/year falls within a date range
     */
    private fun isInDateRange(month: Int, year: Int, startDate: String, endDate: String): Boolean {
        val start = LocalDate.parse(startDate)
        val end = LocalDate.parse(endDate)
        val checkDate = LocalDate.of(year, month, 1)

        return !checkDate.isBefore(start.withDayOfMonth(1)) &&
                !checkDate.isAfter(end.withDayOfMonth(1))
    }

    /**
     * Format month and year to display string
     */
    private fun formatMonthYear(month: Int, year: Int): String {
        return "${getMonthName(month)} $year"
    }

    /**
     * Get month name from number
     */
    private fun getMonthName(month: Int): String {
        return when (month) {
            1 -> "Jan"; 2 -> "Feb"; 3 -> "Mar"; 4 -> "Apr"
            5 -> "May"; 6 -> "Jun"; 7 -> "Jul"; 8 -> "Aug"
            9 -> "Sep"; 10 -> "Oct"; 11 -> "Nov"; 12 -> "Dec"
            else -> "Unknown"
        }
    }

    /**
     * Parse hex color to Compose Color
     */
    private fun parseColor(hexColor: String): Color {
        return try {
            val color = android.graphics.Color.parseColor(hexColor)
            Color(color)
        } catch (e: Exception) {
            Color(0xFF808080) // Default gray
        }
    }

    /**
     * Add section header to PDF
     */
    private fun addSectionHeader(document: Document, title: String) {
        val font = Font(Font.FontFamily.HELVETICA, 16f, Font.BOLD)
        val paragraph = Paragraph(title, font)
        document.add(paragraph)
        document.add(Paragraph(" "))
    }

    /**
     * Add key-value pair to PDF
     */
    private fun addKeyValuePair(document: Document, key: String, value: String) {
        val font = Font(Font.FontFamily.HELVETICA, 12f)
        document.add(Paragraph("$key: $value", font))
    }
}
