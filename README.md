# Bloom - Personal Finance Management Application

## Table of Contents
- [Project Overview](#project-overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Architecture & Design](#architecture--design)
- [Database Schema](#database-schema)
- [Server-Side Architecture](#server-side-architecture)
- [API Documentation](#api-documentation)
- [RetrofitAPI Integration](#retrofitapi-integration)
- [Key Components](#key-components)
- [Conclusion](#conclusion)

---

## Project Overview

### Purpose
Bloom is a comprehensive Android-based personal finance management application specifically designed for **college students** and **low-income individuals**. The application combines traditional budget tracking with cutting-edge AI capabilities to provide intelligent financial insights, helping users make better financial decisions and improve their overall financial health.

### Target Audience
- College students managing limited budgets
- Low-income individuals seeking financial guidance
- Anyone looking for AI-powered financial assistance

### Core Philosophy
Bloom goes beyond basic expense tracking by leveraging Google's Gemini AI to provide personalized financial advice, health scoring, and actionable insights tailored to users with constrained budgets.

---

## Features

### 1. Authentication & User Management
- Email/password authentication via Supabase Auth
- Google OAuth integration for streamlined sign-in
- User profile management
- Secure session handling with JWT tokens

### 2. Dashboard Overview
- Real-time financial metrics display
- Four key metric cards:
    - Monthly Budget tracker
    - Total Expenses visualization
    - Remaining Budget with progress indicator
    - Savings Goal with achievement progress
- Interactive spending breakdown pie chart
- Quick action shortcuts to major features
- Bottom navigation for seamless app navigation

### 3. Budget Management
- Monthly budget allocation
- Savings goal definition
- Custom expense category creation
- Budget distribution across categories
- Real-time spending vs. budget comparison
- Category-level budget tracking
- Edit and delete category functionality

### 4. Transaction Management
- Add/edit/delete transactions
- Support for both income and expenses
- Advanced filtering capabilities:
    - Date range selection
    - Category filtering
    - Transaction type (income/expense)
    - Amount range
    - Text search
- Multiple payment method support:
    - Cash
    - Debit Card
    - Credit Card
    - Bank Transfer
    - Digital Wallet
    - Other

### 5. Expenses & Income Tracking
**Expense Features:**
- Track recurring bills and one-time expenses
- Due date reminders
- Custom icons and color coding
- Tag-based organization
- Recurring frequency options (Daily, Weekly, Monthly, Yearly)
- **Smart average calculations**:
    - Automatically calculates per-month and per-year expense averages based on recurring frequency
    - Daily expenses annualized (×365), Weekly (×52), Monthly (×12), Yearly (×1)
    - Provides accurate financial projections and budgeting insights
- **Batch delete functionality**:
    - Selection mode with red-themed UI for safe deletion
    - Multi-select expenses with visual confirmation (red borders and checkmarks)
    - Delete button appears only when expenses are selected
    - Prevents accidental deletions with clear visual indicators

**Income Features:**
- Multiple income source tracking (Salary, Freelance, Investment, Gift, etc.)
- Recurring income support
- Income categorization
- **Real-time income analytics**:
    - "This Month" card shows total income for current month
    - "Average" card displays monthly average over last 6 months
    - Automatic updates when new income sources are added
- Robust error handling for accurate financial reporting

### 6. Analytics & Reporting
- Time period filtering (Weekly, Monthly, Yearly, Custom Range)
- Income vs. Expenses trend analysis
- Category spending breakdown with visualizations
- Budget adherence tracking
- Recurring bills summary
- Actionable spending insights
- **PDF report generation** using iText library
- Export analytics data to CSV

**Analytics Metrics Provided:**
- Total income and expenses
- Net savings
- Average daily spending
- Top spending category
- Most frequent transaction category
- Largest transaction identification
- Budget health status (Excellent, Good, Warning, Critical)

### 7. AI-Powered Features (Bloom AI)

#### 7.1 AI Financial Chatbot
- Natural language financial advice
- Context-aware responses powered by Google Gemini 2.5 Flash
- Strictly financial assistance (rejects non-financial queries)
- Personalized recommendations for budget-conscious users

#### 7.2 Financial Health Score
- **Comprehensive scoring system (0-100 points)**:
    - Budget Adherence Score (0-40 points): Measures spending discipline
    - Savings Rate Score (0-30 points): Evaluates income-to-savings ratio
    - Spending Consistency Score (0-20 points): Assesses predictability
    - Emergency Fund Score (0-10 points): Checks financial buffer
- Score rating classification (Excellent, Good, Fair, Poor, Very Poor)
- AI-generated personalized recommendations
- Historical score tracking
- Monthly comparison and trend analysis
- Visual progress indicators with color-coded metrics

#### 7.3 Smart Insights
- **Data Sources**: Analyzes data from dedicated Income and Expenses tables
    - **Income Analysis**: Tracks income sources, amounts, and patterns from the `income` table
    - **Expense Analysis**: Monitors expense categories, due dates, and spending habits from the `expenses` table
    - **Separated Tracking**: Uses `IncomeController` and `ExpensesController` for precise financial data retrieval
- **Automated Insight Generation**:
    - Top spending categories grouped by expense name
    - Peak spending day patterns based on expense due dates
    - Largest expense identification and alerts
    - Income vs. expense analysis with savings rate calculations
    - Transaction frequency tracking (combined income and expense entries)
- **AI-Powered Analysis**:
    - Google Gemini 2.5 Flash integration for intelligent insights
    - Comprehensive financial summary including top income sources and top expenses
    - Personalized recommendations tailored to spending and income patterns
- Budget optimization suggestions
- Savings opportunity detection
- Supportive messaging for low-income users
- Real-time updates when new income or expenses are added

#### 7.4 CSV Import with AI Validation
- Upload CSV transaction files
- **AI-powered column mapping** with fuzzy matching
- Automatic data quality verification
- Filters irrelevant/invalid rows
- Bulk transaction import
- Smart field detection for:
    - Transaction Name
    - Amount
    - Transaction Type
    - Date
    - Description
    - Payment Method

#### 7.5 File Processing (PDF/CSV)
- Upload financial documents (bank statements, receipts, invoices)
- AI extracts and summarizes financial data
- Ask questions about uploaded documents
- Supports PDF and CSV formats
- Two-stage processing for improved accuracy

---

## Technology Stack

### Frontend (Android Application)
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Target SDK**: API 36 (Android 14+)
- **Minimum SDK**: API 34

### Backend Services
- **Primary Backend**: Supabase (Backend-as-a-Service)
    - PostgreSQL database
    - Authentication (Email + Google OAuth)
    - Realtime subscriptions
    - Cloud storage for receipts/files

- **AI Server**: Python FastAPI
    - RESTful API endpoints
    - Google Gemini 2.5 Flash integration
    - Hosted locally for development (http://10.0.2.2:8000)

### Key Android Libraries

**UI & Design:**
- `androidx.compose.material3` - Material Design 3 components
- `androidx.navigation.compose` - Type-safe navigation
- Custom theme implementation inspired by **shadcn** design system

**Backend Integration:**
- `io.github.jan-tennert.supabase` - Supabase SDK
    - `supabase-postgrest-kt` - Database operations
    - `supabase-auth-kt` - Authentication
    - `supabase-realtime-kt` - Real-time subscriptions
    - `supabase-storage-kt` - File storage
- `io.ktor:ktor-client-android` - HTTP client for Supabase
- `com.squareup.retrofit2` - REST API communication with FastAPI
- `com.squareup.okhttp3` - HTTP client with logging interceptor

**Data & Visualization:**
- `com.github.PhilJay:MPAndroidChart` - Interactive chart library
- `androidx.datastore:datastore-preferences` - Local data persistence
- `com.itextpdf:itextpdf` - PDF generation for reports

**Authentication:**
- `androidx.credentials` - Google OAuth credential handling
- `com.google.android.libraries.identity.googleid` - Google Sign-In

**Image Handling:**
- `io.coil-kt:coil-compose` - Async image loading and caching

### Python Server Dependencies
- `fastapi` - Modern web framework
- `uvicorn` - ASGI server
- `python-dotenv` - Environment variable management
- `google-genai` - Google Gemini AI SDK
- `PyPDF2` - PDF file parsing

---

## Architecture & Design

### MVVM (Model-View-ViewModel) Architecture

Bloom implements a clean MVVM architecture with an additional Controller layer for enhanced separation of concerns:

```
┌─────────────────────────────────────────────────┐
│                    View Layer                    │
│        (Jetpack Compose UI Components)          │
└────────────────┬────────────────────────────────┘
                 │ User Interactions
                 ↓
┌─────────────────────────────────────────────────┐
│                ViewModel Layer                   │
│         (State Management & UI Logic)           │
└────────────────┬────────────────────────────────┘
                 │ Business Logic Calls
                 ↓
┌─────────────────────────────────────────────────┐
│               Controller Layer                   │
│     (Repository Pattern - Data Operations)      │
└────────────────┬────────────────────────────────┘
                 │ Database/API Calls
                 ↓
┌─────────────────────────────────────────────────┐
│                 Model Layer                      │
│    (Data Classes, Supabase, FastAPI Server)     │
└─────────────────────────────────────────────────┘
```

**Key Benefits:**
- **Testability**: Business logic isolated in Controllers
- **Reactivity**: ViewModels expose state as Kotlin Flows
- **Lifecycle Awareness**: ViewModels survive configuration changes
- **Clear Data Flow**: Unidirectional data flow pattern
- **Separation of Concerns**: Each layer has a single responsibility

### Design Patterns

#### 1. Repository Pattern (via Controllers)
Controllers abstract data operations, providing a clean API to ViewModels:

```kotlin
// Example: BudgetController.kt
class BudgetController {
    private val supabase = SupabaseClient.client

    suspend fun getCurrentMonthBudget(): Result<BudgetSummaryData?> {
        return try {
            // Encapsulated Supabase query logic
            Result.success(budgetData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

**Controllers in the project:**
- `LoginRegisterController.kt` - Authentication operations
- `BudgetController.kt` - Budget management
- `TransactionController.kt` - Transaction CRUD operations
- `ExpensesController.kt` - Expense tracking
- `IncomeController.kt` - Income management
- `AnalyticsController.kt` - Financial analytics
- `HealthScoreController.kt` - Health score calculation
- `SettingsController.kt` - User preferences
- `BloomAIController.kt` - AI feature integration
- `CsvImportController.kt` - CSV import logic

#### 2. Singleton Pattern
Used for shared resources:

```kotlin
// SupabaseClient.kt - Single Supabase instance
object SupabaseClient {
    val client: SupabaseClient = createSupabaseClient(
        supabaseUrl = BuildConfig.SUPABASE_URL,
        supabaseKey = BuildConfig.SUPABASE_PUBLISHABLE_KEY
    ) {
        install(Auth)
        install(Postgrest)
        install(Realtime)
        install(Storage)
    }
}

// RetrofitInstance.kt - Single Retrofit instance
object RetrofitInstance {
    private const val BASE_URL = "http://10.0.2.2:8000/bloomLogic/"

    val instance: RetrofitAPI by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RetrofitAPI::class.java)
    }
}
```



#### 3. Kotlin Coroutines & Structured Concurrency
All network and database operations are non-blocking:
- Suspend functions for async operations
- ViewModelScope for lifecycle-aware execution
- Exception propagation through coroutine context



### Navigation Architecture

The app uses **Jetpack Compose Navigation** with route-based navigation:

**Main Routes:**
- `main_screen` - Entry point (auth check)
- `login_screen` - User login
- `register_screen` - User registration
- `dashboard_screen` - Main dashboard (home)
- `budget_screen` - Budget management
- `transaction_screen` - Transaction list/management
- `financials_screen` - Financial overview such as Budget, Income, Transactions, Expenses, and Analytics
- `expenses_screen` - Expense tracking
- `income_screen` - Income tracking
- `analytics_screen` - Financial analytics
- `bloom_ai_screen` - AI features hub
- `settings_screen` - User settings

**Bottom Navigation Tabs:**
1. **Dashboard** (Green, Home icon)
2. **Financials** (Blue, Money icon)
3. **Bloom A.I** (Purple, Robot icon)
4. **Settings** (Orange, Settings icon)

---

## Database Schema

Bloom uses **Supabase PostgreSQL** as its primary database. This is a comprehensive overview of all tables and their purposes.

### Table Summaries

#### 1. `user_profiles`
**Purpose**: Store user account information and preferences.

**Columns:**
- `id` (UUID, PK) - Unique user identifier
- `email` (String) - User email address
- `username` (String, nullable) - Display username
- `full_name` (String, nullable) - User's full name
- `avatar_url` (String, nullable) - Profile picture URL
- `theme_preference` (String) - UI theme choice (default: "system")

**Usage**: Created during registration; linked to Supabase Auth users.

---

#### 2. `budget_summary`
**Purpose**: Store monthly budget allocations and savings goals.

**Columns:**
- `id` (UUID, PK) - Unique budget summary ID
- `user_id` (UUID, FK) - References `user_profiles.id`
- `month` (Int) - Month number (1-12)
- `year` (Int) - Year
- `monthly_budget` (Double) - Total budget for the month
- `savings_goal` (Double) - Target savings amount
- `current_savings` (Double) - Actual savings accumulated
- `created_at` (Timestamp) - Record creation time
- `updated_at` (Timestamp) - Last modification time

**Usage**: Dashboard displays budget metrics; Budget screen manages these records.

---

#### 3. `categories`
**Purpose**: User-defined expense and income categories with budget allocations.

**Columns:**
- `id` (UUID, PK) - Unique category ID
- `user_id` (UUID, FK) - References `user_profiles.id`
- `name` (String) - Category name (e.g., "Groceries", "Rent")
- `color_hex` (String) - Color for visual identification
- `icon_name` (String, nullable) - Icon identifier
- `category_type` (String) - "expense" or "income"
- `budget_allocation` (Double) - Amount allocated to this category
- `is_default` (Boolean) - Whether it's a system-provided category
- `created_at` (Timestamp) - Record creation time
- `updated_at` (Timestamp) - Last modification time

**Usage**: Budget screen for category management; Transaction screen for categorization; Analytics for spending breakdown.

---

#### 4. `transactions`
**Purpose**: Detailed record of all financial transactions (expenses and income).

**Columns:**
- `id` (UUID, PK) - Unique transaction ID
- `user_id` (UUID, FK) - References `user_profiles.id`
- `category_id` (UUID, FK, nullable) - References `categories.id`
- `transaction_name` (String, nullable) - Transaction description
- `amount` (Double) - Transaction amount
- `transaction_date` (String) - Date in yyyy-MM-dd format
- `transaction_type` (String) - "expense" or "income"
- `description` (String, nullable) - Additional details
- `notes` (String, nullable) - User notes
- `payment_method` (String, nullable) - Payment type (CASH, DEBIT_CARD, etc.)
- `location` (String, nullable) - Transaction location
- `receipt_url` (String, nullable) - Supabase Storage URL for receipt
- `tags` (Array<String>, nullable) - Searchable tags
- `is_recurring` (Boolean) - Whether transaction repeats
- `recurring_frequency` (String, nullable) - Frequency if recurring
- `created_at` (Timestamp) - Record creation time
- `updated_at` (Timestamp) - Last modification time

**Usage**: Core data for Transaction screen; aggregated in Dashboard and Analytics; used for Health Score calculation.

---

#### 5. `expenses`
**Purpose**: Track recurring bills and expense obligations.

**Columns:**
- `id` (UUID, PK) - Unique expense ID
- `user_id` (UUID, FK) - References `user_profiles.id`
- `name` (String) - Expense name (e.g., "Netflix Subscription")
- `amount` (Double) - Expense amount
- `due_date` (String) - Due date in yyyy-MM-dd format
- `image_url` (String, nullable) - Associated image
- `icon_name` (String, nullable) - Icon identifier
- `color_hex` (String, nullable) - Color for visualization
- `tags` (String, nullable) - Comma-separated tags
- `recurring_frequency` (String, nullable) - "Daily", "Weekly", "Monthly", "Yearly"
- `created_at` (Timestamp) - Record creation time
- `updated_at` (Timestamp) - Last modification time

**Usage**: Expenses screen for bill tracking; Dashboard for upcoming bills; Analytics for recurring expense analysis.

---

#### 6. `income`
**Purpose**: Track income sources and receipts.

**Columns:**
- `id` (UUID, PK) - Unique income ID
- `user_id` (UUID, FK) - References `user_profiles.id`
- `source` (String) - Income source ("Salary", "Freelance", "Investment", "Gift", etc.)
- `amount` (Double) - Income amount
- `income_date` (String) - Date received in yyyy-MM-dd format
- `description` (String, nullable) - Income details
- `image_url` (String, nullable) - Associated image
- `icon_name` (String, nullable) - Icon identifier
- `color_hex` (String, nullable) - Color for visualization
- `tags` (String, nullable) - Comma-separated tags
- `is_recurring` (Boolean) - Whether income repeats
- `recurring_frequency` (String, nullable) - Frequency if recurring
- `created_at` (Timestamp) - Record creation time
- `updated_at` (Timestamp) - Last modification time

**Usage**: Income screen for income tracking; Dashboard for total income; Analytics for income trends; Health Score calculation.

---

#### 7. `health_scores`
**Purpose**: Store financial health score history and AI recommendations.

**Columns:**
- `id` (UUID, PK) - Unique score ID
- `user_id` (UUID, FK) - References `user_profiles.id`
- `overall_score` (Int) - Total score (0-100)
- `budget_adherence_score` (Int) - Budget discipline score (0-40)
- `savings_rate_score` (Int) - Savings efficiency score (0-30)
- `spending_consistency_score` (Int) - Spending predictability score (0-20)
- `emergency_fund_score` (Int) - Emergency buffer score (0-10)
- `recommendations` (String) - AI-generated advice
- `monthly_budget` (Double) - Budget at time of scoring
- `total_spent` (Double) - Spending at time of scoring
- `monthly_income` (Double) - Income at time of scoring
- `current_savings` (Double) - Savings at time of scoring
- `savings_goal` (Double) - Savings target at time of scoring
- `score_rating` (String) - "Excellent", "Good", "Fair", "Poor", "Very Poor"
- `score_month` (Int) - Month of score (1-12)
- `score_year` (Int) - Year of score
- `created_at` (Timestamp) - Score generation time
- `updated_at` (Timestamp) - Last modification time

**Usage**: Health Score screen for displaying current and historical scores; tracking financial improvement over time.

---

### Database Relationships

```
user_profiles (1) ──────── (∞) budget_summary
                │
                ├──────── (∞) categories
                │
                ├──────── (∞) transactions
                │              │
                │              └── (1) categories [optional FK]
                │
                ├──────── (∞) expenses
                │
                ├──────── (∞) income
                │
                └──────── (∞) health_scores
```

**Data Isolation**: All tables include `user_id` foreign key, ensuring complete data isolation between users. Queries always filter by `eq("user_id", userId)`.

---

## Server-Side Architecture

### FastAPI Server Overview

The backend AI server is built with **FastAPI**, a modern Python web framework known for its speed and automatic API documentation. The server handles all AI-related operations, keeping the Android app lightweight.

**Location**: `/server/`

**Key Files:**
- `main.py` - Application entry point
- `routers/bloomLogic.py` - AI endpoint implementations
- `.env` - Environment variables (GEMINI_API_KEY)
- `requirements.txt` - Python dependencies

### Server Entry Point (`main.py`)

```python
from fastapi import FastAPI
from routers import bloomLogic

app = FastAPI()

# Include AI router with /bloomLogic prefix
app.include_router(
    bloomLogic.router,
    prefix="/bloomLogic",
    tags=["bloomLogic"]
)

# Health check endpoint
@app.get("/")
async def root():
    return {"message": "Bloom AI Server Running"}
```

**Run Command**: `uvicorn main:app --reload --port 8000`

### Google Gemini AI Integration

**Model Used**: Google Gemini 2.5 Flash

**Configuration**:
```python
import google.generativeai as genai
import os
from dotenv import load_dotenv

load_dotenv()
client = genai.Client(api_key=os.getenv("GEMINI_API_KEY"))

response = client.models.generate_content(
    model="gemini-2.5-flash",
    contents=prompt
)
```

**Why Gemini 2.5 Flash?**
- Fast response times (critical for mobile UX)
- Cost-effective for student projects
- Strong performance on financial reasoning tasks
- Supports multimodal inputs (text + files)

### System Prompts

Each AI endpoint uses carefully crafted system instructions to ensure:
- **Financial-only responses** (rejects unrelated queries)
- **Supportive tone** for budget-conscious users
- **Actionable advice** tailored to college students and low-income individuals
- **Structured outputs** (e.g., JSON for health scores)
- **No hallucination** of financial data

---

## API Documentation

### Base URL
**Development**: `http://10.0.2.2:8000/bloomLogic/`
(Android emulator's localhost mapping)

**Production**: Would use deployed server URL (e.g., Heroku, Railway, Render)

### Authentication
Currently, the FastAPI server does not implement authentication. In production, it would validate Supabase JWT tokens to ensure requests are authorized.

---

### Endpoint: `POST /bloomLogic/processFile`

**Purpose**: Process financial documents (PDF/CSV) and answer user questions about them.

**Request:**
- **Content-Type**: `multipart/form-data`
- **Parameters**:
    - `file` (required): PDF or CSV file
    - `user_question` (optional): Question about the file content

**Response:**
```json
{
  "message": "The document shows total expenses of $1,234.56 for March 2024..."
}
```

**Processing Flow:**
1. Validate file type (PDF or CSV only)
2. Extract text:
    - **PDF**: Use `PyPDF2.PdfReader` to extract text from all pages
    - **CSV**: Read with `csv` module and format as text
3. **First AI Call**: Extract and summarize financial content
    - System prompt enforces financial-only content
    - Rejects non-financial documents
4. **Second AI Call**: Answer user's question based on summary
5. Return AI response

**Error Handling:**
- Invalid file type: Returns 400 error
- Non-financial content: AI responds "This document does not contain financial data"
- File too large: Truncates to 30,000 characters

**Usage in App**:
- Bloom A.I screen → File Processing feature
- Users upload bank statements, receipts, or transaction exports
- Ask questions like "What were my biggest expenses?" or "Summarize this statement"

---

### Endpoint: `POST /bloomLogic/chat`

**Purpose**: Financial chatbot for personalized advice.

**Request:**
```json
{
  "message": "How can I save money on groceries as a college student?"
}
```

**Response:**
```json
{
  "message": "Here are budget-friendly grocery tips for college students:\n1. Buy generic brands...\n2. Meal prep on Sundays...\n3. Use student discounts..."
}
```

**System Instruction:**
> "You are a Financial Data Assistant. You assist with financial data (e.g., revenues, expenses, balance sheets, transactions, P&L, cash flow, account statements, financial metrics). If the user asks about non-financial topics, respond exactly: 'I cannot help with that request as I only handle financial data.'"

**Behavior:**
- Accepts only financial questions
- Rejects queries about weather, sports, general knowledge, etc.
- Provides actionable, specific advice
- Tailored for budget-conscious users

**Example Interactions:**
- ✅ "How should I budget $500/month?" → Detailed budget breakdown
- ✅ "What's a good savings rate?" → Explains 50/30/20 rule
- ❌ "What's the weather?" → "I cannot help with that request..."

**Usage in App**: Bloom A.I screen → AI Chatbot feature

---

### Endpoint: `POST /bloomLogic/insights`

**Purpose**: Generate smart financial insights from user's income and expense data.

**Request:**
```json
{
  "message": "Financial Summary for DECEMBER 2024:\nTotal Income: $2500.00\nTotal Expenses: $1850.00\nNet Savings: $650.00\n\nTop Income Sources:\n- Salary: $2000.00 (80%)\n- Freelance: $500.00 (20%)\n\nTop 5 Expenses:\n- Rent: $800.00 (43%)\n- Groceries: $300.00 (16%)\n- Utilities: $150.00 (8%)..."
}
```

**Response:**
```json
{
  "message": "1. Excellent savings rate at 26%! You're building a healthy financial buffer.\n2. Your rent is 32% of your total income - this is within the recommended 30-35% range.\n3. Your grocery spending is efficient at $300/month for a single person.\n4. Consider setting aside your freelance income for taxes and emergency savings..."
}
```

**System Instruction:**
> "You are a Financial Insights Advisor for college students and low-income individuals. Provide 3-4 specific, actionable insights based on spending patterns. Focus on: spending patterns, budget optimization, savings opportunities, and financial health. Be encouraging and supportive."

**Input Data Provided:**
- **Income Data**: Top income sources with amounts and percentages
- **Expense Data**: Top expenses grouped by name with amounts and percentages
- **Summary Metrics**:
    - Total monthly income
    - Total monthly expenses
    - Net savings amount
    - Savings rate percentage

**Data Source**:
- Fetched from `income` table via `IncomeController`
- Fetched from `expenses` table via `ExpensesController`
- Filtered to current month's data

**Output Format**:
- 3-4 numbered insights
- Specific percentages and dollar amounts
- Actionable recommendations based on income sources and expense categories
- Positive reinforcement for good financial habits

**Usage in App**: Bloom A.I screen → Smart Insights feature

---

### Endpoint: `POST /bloomLogic/healthScore`

**Purpose**: Calculate comprehensive financial health score with AI recommendations.

**Request:**
```json
{
  "message": "Monthly Budget: $2000\nTotal Spent: $1750\nMonthly Income: $2500\nCurrent Savings: $1200\nSavings Goal: $3000"
}
```

**Response:**
```json
{
  "score": 78,
  "budgetAdherenceScore": 35,
  "savingsRateScore": 24,
  "spendingConsistencyScore": 16,
  "emergencyFundScore": 3,
  "recommendations": "1. Excellent budget discipline! You stayed under budget.\n2. Try to increase your savings rate to 30% of income.\n3. Build your emergency fund to cover 3 months of expenses.",
  "message": "[Full AI response with detailed scoring breakdown]"
}
```

**Scoring Criteria:**

1. **Budget Adherence (0-40 points)**:
    - 40 points: Spent ≤ 80% of budget (excellent discipline)
    - 30 points: 80-90% of budget (good)
    - 20 points: 90-100% of budget (on track)
    - 10 points: 100-110% of budget (warning)
    - 0 points: > 110% of budget (over budget)

2. **Savings Rate (0-30 points)**:
    - 30 points: Saving ≥ 30% of income
    - 20 points: Saving 20-29% of income
    - 15 points: Saving 10-19% of income
    - 5 points: Saving < 10% of income

3. **Spending Consistency (0-20 points)**:
    - Based on transaction patterns and predictability
    - AI analyzes whether spending is stable or erratic

4. **Emergency Fund (0-10 points)**:
    - 10 points: Emergency fund ≥ 3 months expenses
    - 7 points: 2-3 months expenses
    - 5 points: 1-2 months expenses
    - 2 points: < 1 month expenses

**Score Ratings:**
- **90-100**: Excellent
- **75-89**: Good
- **60-74**: Fair
- **40-59**: Poor
- **0-39**: Very Poor

**System Instruction Enforces:**
- All scores must be whole integers (no decimals)
- Total score ≤ 100
- Individual scores stay within ranges
- JSON-compatible response format

**Usage in App**:
- Bloom A.I screen → Health Score feature
- Results saved to `health_scores` table for history tracking
- Dashboard can display current health score

---

### Endpoint: `POST /bloomLogic/importCSV`

**Purpose**: Import transactions from CSV with AI-powered validation and mapping.

**Request:**
- **Content-Type**: `multipart/form-data`
- **Parameters**:
    - `file` (required): CSV file with transaction data

**Response:**
```json
{
  "success": true,
  "message": "Successfully imported 45 transactions. Skipped 2 invalid rows.",
  "transactions": [
    {
      "transactionName": "Starbucks Coffee",
      "amount": "5.75",
      "transactionType": "Expense",
      "date": "2024-12-01",
      "description": "Morning coffee",
      "paymentMethod": "Debit Card"
    }
  ],
  "totalRows": 47,
  "validRows": 45,
  "skippedRows": 2
}
```

**CSV Validation Process:**

1. **Parse CSV**: Read file with `csv.DictReader`
2. **Sample Analysis**: Send first 5 rows to Gemini AI
3. **AI Validation**: AI responds with:
   ```
   STATUS: VALID
   REASON: This CSV contains bank transaction data
   COLUMN_MAPPING:
     Transaction Name: Description
     Amount: Debit
     Transaction Type: [INFERRED from amount sign]
     Date: Transaction Date
     Description: Memo
     Payment Method: MISSING
   ```
4. **Process All Rows**: Apply mapping to entire CSV
5. **Filter Invalid**: Skip rows that don't match financial patterns
6. **Return Transactions**: App inserts into `transactions` table

**AI Fuzzy Matching Examples:**
- "Description" → Transaction Name
- "Debit" or "Credit" → Amount
- "Transaction Date" or "Date" or "Posting Date" → Date
- Infers transaction type from amount sign (+/-)

**Error Handling:**
- Invalid CSV structure: Returns error with explanation
- Non-financial CSV: "STATUS: IRRELEVANT"
- Missing critical columns: AI indicates "MISSING" in mapping

**Usage in App**:
- Bloom A.I screen → CSV Import feature
- Users export transactions from their bank
- Upload to Bloom for automatic import

---

### API Error Responses

All endpoints use consistent error handling:

**400 Bad Request**:
```json
{
  "detail": "Invalid file type. Only PDF and CSV files are accepted."
}
```

**500 Internal Server Error**:
```json
{
  "detail": "AI processing failed: [error message]"
}
```

---

## RetrofitAPI Integration

Bloom uses **Retrofit** to communicate with the FastAPI server. Retrofit simplifies HTTP requests with type-safe interfaces and automatic JSON serialization.

### RetrofitAPI Interface

**Location**: `/app/src/main/java/com/example/bloom/retrofitapi/RetrofitAPI.kt`

```kotlin
interface RetrofitAPI {

    // Process PDF/CSV files with optional question
    @Multipart
    @POST("processFile")
    suspend fun processFile(
        @Part file: MultipartBody.Part,
        @Part("user_question") userQuestion: RequestBody?
    ): AIGenerativeDataModel

    // Chat with financial AI
    @POST("chat")
    suspend fun chat(
        @Body request: ChatRequest
    ): AIGenerativeDataModel

    // Generate spending insights
    @POST("insights")
    suspend fun generateInsights(
        @Body request: ChatRequest
    ): AIGenerativeDataModel

    // Calculate health score
    @POST("healthScore")
    suspend fun calculateHealthScore(
        @Body request: ChatRequest
    ): AIHealthScoreResponse

    // Import CSV transactions
    @Multipart
    @POST("importCSV")
    suspend fun importCSV(
        @Part file: MultipartBody.Part
    ): CsvImportResponse
}
```

**Key Features:**
- **Suspend functions**: All calls are coroutine-based for async execution
- **Type-safe**: Automatic JSON deserialization to Kotlin data classes
- **Multipart**: File upload support for PDFs and CSVs
- **Annotations**: Retrofit annotations define HTTP methods and parameters

### RetrofitInstance Configuration

**Location**: `/app/src/main/java/com/example/bloom/retrofitapi/RetrofitInstance.kt`

```kotlin
object RetrofitInstance {
    private const val BASE_URL = "http://10.0.2.2:8000/bloomLogic/"

    // OkHttp client with custom timeouts
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)  // AI calls can be slow
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY  // Debug logging
        })
        .build()

    // Lazy-initialized Retrofit instance
    val instance: RetrofitAPI by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RetrofitAPI::class.java)
    }
}
```

**Configuration Details:**
- **Base URL**: `http://10.0.2.2:8000` is Android emulator's way to access host machine's `localhost:8000`
- **Timeouts**: 60 seconds to accommodate AI processing time
- **Logging**: OkHttp interceptor logs all requests/responses for debugging
- **Gson Converter**: Automatically converts JSON to Kotlin objects
- **Singleton Pattern**: Single shared instance across the app

### Data Models for API Responses

**Location**: `/app/src/main/java/com/example/bloom/datamodels/AIFeatureDataModel.kt`

```kotlin
// Standard AI response
@Serializable
data class AIGenerativeDataModel(
    val message: String
)

// Chat request
@Serializable
data class ChatRequest(
    val message: String
)

// Health score response with structured data
@Serializable
data class AIHealthScoreResponse(
    val score: Int,
    val budgetAdherenceScore: Int,
    val savingsRateScore: Int,
    val spendingConsistencyScore: Int,
    val emergencyFundScore: Int,
    val recommendations: String,
    val message: String
)

// CSV import response
@Serializable
data class CsvImportResponse(
    val success: Boolean,
    val message: String,
    val transactions: List<CsvTransaction>,
    val totalRows: Int,
    val validRows: Int,
    val skippedRows: Int
)

@Serializable
data class CsvTransaction(
    val transactionName: String,
    val amount: String,
    val transactionType: String,
    val date: String,
    val description: String,
    val paymentMethod: String
)
```

### Usage Example in Controller

**BloomAIController.kt** demonstrates typical Retrofit usage:

```kotlin
class BloomAIController {
    private val api = RetrofitInstance.instance

    suspend fun chat(message: String): Result<String> {
        return try {
            val response = api.chat(ChatRequest(message))
            Result.success(response.message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun calculateHealthScore(financialData: String): Result<AIHealthScoreResponse> {
        return try {
            val response = api.calculateHealthScore(ChatRequest(financialData))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun processFile(file: File, question: String?): Result<String> {
        return try {
            val filePart = MultipartBody.Part.createFormData(
                "file",
                file.name,
                file.asRequestBody("application/octet-stream".toMediaType())
            )
            val questionBody = question?.toRequestBody("text/plain".toMediaType())

            val response = api.processFile(filePart, questionBody)
            Result.success(response.message)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

**Flow:**
1. ViewModel calls Controller method
2. Controller uses RetrofitInstance.instance
3. Retrofit sends HTTP request to FastAPI
4. FastAPI processes with Gemini AI
5. Response auto-deserialized to Kotlin object
6. Controller returns Result<T>
7. ViewModel updates UI state

---

## Key Components

### UI Theme (shadcn-inspired)

**Location**: `/app/src/main/java/com/example/bloom/ui/theme/`

The app's visual design is inspired by **shadcn**, featuring:
- Clean, modern aesthetics
- Subtle shadows and borders
- Accessible color contrasts
- Smooth animations

**Theme Features:**
- **Dark/Light Mode**: Automatic system detection or manual override
- **Material 3 Design**: Utilizes Material You color system
- **Custom Typography**: Defined in `Type.kt`
- **Color Palette**: Defined in `Color.kt`


**Theme Persistence**:
User preferences stored with **DataStore** via `PreferencesManager.kt`:
```kotlin
class PreferencesManager(context: Context) {
    private val dataStore = context.dataStore

    fun getThemePreference(): Flow<String> = dataStore.data.map {
        it[THEME_KEY] ?: "system"
    }

    suspend fun setThemePreference(theme: String) {
        dataStore.edit { it[THEME_KEY] = theme }
    }
}
```

Options: `"light"`, `"dark"`, `"system"`

---

### Bottom Navigation Bar

**Location**: Defined in `Dashboard.kt`, used across main screens.

**Implementation:**
```kotlin
BottomNavigationBar(
    selectedIndex = selectedBottomNavIndex,
    onItemSelected = { index ->
        selectedBottomNavIndex = index
        // Navigate to corresponding screen
    }
)
```

**Navigation Items:**
1. **Dashboard** - Home icon (Green) → `dashboard_screen`
2. **Financials** - Money icon (Blue) → `financials_screen`
3. **Bloom A.I** - Robot icon (Purple) → `bloom_ai_screen`
4. **Settings** - Settings icon (Orange) → `settings_screen`

**Features:**
- Selected state highlighting
- Icon color changes per tab
- Smooth transitions
- Persistent selection across navigation

---

### Reusable UI Components

#### MetricCard
**Purpose**: Display financial metrics with icons and progress.

```kotlin
@Composable
fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    backgroundColor: Color,
    progress: Float? = null,
    subtitle: String? = null
)
```

**Usage**:
- Dashboard: Monthly Budget, Total Expenses, Remaining Budget, Savings Goal
- Budget screen: Category spending vs. allocation
- Analytics: Summary statistics

---

#### Pie Chart Components

**Library**: MPAndroidChart (`com.github.PhilJay:MPAndroidChart`)

**Components:**
1. **SpendingPieChart** - Budget category breakdown
2. **SpendingPieChartAnalytics** - Analytics screen visualization

**Features:**
- Interactive (tap to highlight slice)
- Animated entry with rotation
- Custom colors per category
- Legend with percentages
- Center hole with total amount
- Value labels on slices

**Data Structure**:
```kotlin
data class PieChartEntry(
    val label: String,        // Category name
    val value: Float,         // Amount spent
    val color: Color          // Category color
)
```

---

#### Transaction List Item

**Purpose**: Display individual transactions with category, amount, date.

**Features**:
- Icon with category color
- Transaction name and description
- Formatted amount (color-coded: red for expenses, green for income)
- Date display
- Swipe actions for edit/delete

---

### PDF Report Generation

**Location**: `AnalyticsController.kt`

**Library**: iText (`com.itextpdf:itextpdf`)

**Report Contents:**
1. Header with date range
2. Summary statistics table
3. Category breakdown
4. Income vs. Expenses comparison
5. Monthly trend analysis
6. Budget performance

**Export Flow:**
1. User taps "Export PDF" in Analytics
2. AnalyticsController gathers data
3. iText generates PDF document
4. File saved to device storage
5. User can share/view PDF

---


## Conclusion

Bloom represents a comprehensive, personal finance application that successfully combines traditional financial management features with AI capabilities. The project demonstrates:

- **Modern Android Development**: Jetpack Compose, Kotlin, MVVM architecture
- **Backend Integration**: Supabase for data, FastAPI for AI processing
- **AI-Powered Insights**: Google Gemini 2.5 Flash for intelligent recommendations
- **Clean Code Practices**: Separation of concerns, error handling, testability
- **User-Centric Design**: Shadcn-inspired UI, accessibility, responsive design
- **Scalable Architecture**: Modular components, clear data flow, extensible design

The application is well-suited for college students and budget-conscious individuals seeking to improve their financial literacy and management skills through innovative technology.

With Bloom, also powered by AI, users can gain deeper insights into their spending habits, receive personalized advice, and make smarter financial decisions.
