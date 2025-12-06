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
     * Fetch monthly income/expense trends from SQL view
     */
    private suspend fun fetchMonthlyTrends(
        userId: String,
        startDate: String,
        endDate: String
    ): List<MonthlyTrendData> {
        return try {
            val response = supabase.from("monthly_income_expense_summary")
                .select() {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<MonthlyAggregateResult>()

            // Filter by date range and transform
            response
                .filter { isInDateRange(it.month, it.year, startDate, endDate) }
                .map { result ->
                    MonthlyTrendData(
                        month = formatMonthYear(result.month, result.year),
                        monthYear = Pair(result.month, result.year),
                        totalIncome = result.total_income,
                        totalExpenses = result.total_expenses,
                        netAmount = result.total_income - result.total_expenses,
                        transactionCount = result.transaction_count
                    )
                }
                .sortedBy { it.monthYear.second * 12 + it.monthYear.first }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Fetch category breakdown with percentages
     * Joins transactions with categories table
     */
    private suspend fun fetchCategoryBreakdown(
        userId: String,
        startDate: String,
        endDate: String
    ): List<CategoryBreakdownData> {
        return try {
            val response = supabase.from("transactions")
                .select(
                    columns = Columns.raw("""
                        category_id,
                        amount,
                        categories!inner(name, color_hex)
                    """.trimIndent())
                ) {
                    filter {
                        eq("user_id", userId)
                        eq("transaction_type", "expense")
                        gte("transaction_date", startDate)
                        lte("transaction_date", endDate)
                    }
                }
                .decodeList<JsonObject>()

            // Group by category and aggregate
            val categoryMap = mutableMapOf<String, MutableList<Double>>()
            val categoryInfo = mutableMapOf<String, Pair<String, String>>() // name, color

            response.forEach { txn ->
                val categoryId = txn["category_id"]?.jsonPrimitive?.content ?: return@forEach
                val amount = txn["amount"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0
                val category = txn["categories"]?.jsonObject
                val name = category?.get("name")?.jsonPrimitive?.content ?: "Unknown"
                val color = category?.get("color_hex")?.jsonPrimitive?.content ?: "#808080"

                categoryMap.getOrPut(categoryId) { mutableListOf() }.add(amount)
                categoryInfo[categoryId] = Pair(name, color)
            }

            val totalSpending = categoryMap.values.flatten().sum()

            categoryMap.map { (catId, amounts) ->
                val (name, colorHex) = categoryInfo[catId] ?: Pair("Unknown", "#808080")
                val total = amounts.sum()
                CategoryBreakdownData(
                    categoryId = catId,
                    categoryName = name,
                    categoryColor = parseColor(colorHex),
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
     * Fetch budget adherence data from SQL view
     */
    private suspend fun fetchBudgetAdherence(
        userId: String,
        startDate: String,
        endDate: String
    ): List<BudgetAdherenceData> {
        return try {
            val response = supabase.from("budget_performance_analysis")
                .select() {
                    filter {
                        eq("user_id", userId)
                    }
                }
                .decodeList<BudgetPerformanceResult>()

            response
                .filter { isInDateRange(it.month, it.year, startDate, endDate) }
                .map { result ->
                    BudgetAdherenceData(
                        month = formatMonthYear(result.month, result.year),
                        budgetAmount = result.monthly_budget,
                        actualSpent = result.actual_spent,
                        difference = result.budget_remaining,
                        adherencePercentage = result.spent_percentage.toFloat(),
                        isOverBudget = result.actual_spent > result.monthly_budget
                    )
                }
                .sortedBy { it.month }
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
                        not("recurring_frequency", "is", null)
                    }
                }
                .decodeList<ExpenseData>()

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
            val response = supabase.from("transactions")
                .select(columns = Columns.raw("transaction_type, amount")) {
                    filter {
                        eq("user_id", userId)
                        gte("transaction_date", startDate)
                        lte("transaction_date", endDate)
                    }
                }
                .decodeList<JsonObject>()

            var totalIncome = 0.0
            var totalExpenses = 0.0

            response.forEach { txn ->
                val type = txn["transaction_type"]?.jsonPrimitive?.content
                val amount = txn["amount"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: 0.0

                when (type) {
                    "income" -> totalIncome += amount
                    "expense" -> totalExpenses += amount
                }
            }

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
     * Get largest transaction for the period
     */
    private suspend fun getLargestTransaction(
        userId: String,
        startDate: String,
        endDate: String
    ): TransactionSummary? {
        return try {
            val response = supabase.from("transactions")
                .select(columns = Columns.raw("""
                    amount,
                    transaction_date,
                    description,
                    categories!inner(name)
                """.trimIndent())) {
                    filter {
                        eq("user_id", userId)
                        eq("transaction_type", "expense")
                        gte("transaction_date", startDate)
                        lte("transaction_date", endDate)
                    }
                    order("amount", ascending = false)
                    limit(1)
                }
                .decodeList<JsonObject>()

            val txn = response.firstOrNull() ?: return null

            TransactionSummary(
                amount = txn["amount"]?.jsonPrimitive?.content?.toDouble() ?: 0.0,
                categoryName = txn["categories"]?.jsonObject?.get("name")?.jsonPrimitive?.content ?: "Unknown",
                date = txn["transaction_date"]?.jsonPrimitive?.content ?: "",
                description = txn["description"]?.jsonPrimitive?.content
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
