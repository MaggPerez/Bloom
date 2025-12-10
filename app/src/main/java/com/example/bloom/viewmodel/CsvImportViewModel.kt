package com.example.bloom.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bloom.SupabaseClient
import com.example.bloom.aifeatures.CsvImportResponse
import com.example.bloom.aifeatures.CsvImportTransaction
import com.example.bloom.controllers.CsvImportController
import com.example.bloom.retrofitapi.RetrofitInstance
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

class CsvImportViewModel : ViewModel() {

    private val csvImportController = CsvImportController()

    // =====================================================
    // STATE VARIABLES
    // =====================================================

    // File selection
    var selectedFileUri by mutableStateOf<Uri?>(null)
        private set

    var selectedFileName by mutableStateOf("")
        private set

    // Import status
    var isUploading by mutableStateOf(false)
        private set

    var isSaving by mutableStateOf(false)
        private set

    var uploadProgress by mutableStateOf(0f)
        private set

    // Import results
    var importResponse by mutableStateOf<CsvImportResponse?>(null)
        private set

    var importedTransactions by mutableStateOf<List<CsvImportTransaction>>(emptyList())
        private set

    var totalRows by mutableStateOf(0)
        private set

    var validRows by mutableStateOf(0)
        private set

    var skippedRows by mutableStateOf(0)
        private set

    var savedTransactionCount by mutableStateOf(0)
        private set

    // Error handling
    var errorMessage by mutableStateOf<String?>(null)
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    // UI State
    var showImportDialog by mutableStateOf(false)
        private set

    var showResultsDialog by mutableStateOf(false)
        private set

    var currentStep by mutableStateOf(ImportStep.SELECT_FILE)
        private set

    enum class ImportStep {
        SELECT_FILE,
        UPLOADING,
        REVIEW_TRANSACTIONS,
        SAVING,
        COMPLETE
    }

    // =====================================================
    // FILE SELECTION
    // =====================================================

    /**
     * Handle file selection from file picker
     */
    fun onFileSelected(uri: Uri, context: Context) {
        selectedFileUri = uri
        // Get file name from URI
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    selectedFileName = it.getString(nameIndex)
                    Log.d("CsvImportViewModel", "File selected: $selectedFileName")
                }
            }
        }
    }

    /**
     * Clear selected file
     */
    fun clearFileSelection() {
        selectedFileUri = null
        selectedFileName = ""
        errorMessage = null
    }

    // =====================================================
    // CSV IMPORT PROCESS
    // =====================================================

    /**
     * Upload CSV file and process it
     */
    fun uploadCsvFile(context: Context) {
        val fileUri = selectedFileUri
        if (fileUri == null) {
            errorMessage = "Please select a CSV file first"
            return
        }

        viewModelScope.launch {
            isUploading = true
            currentStep = ImportStep.UPLOADING
            errorMessage = null
            uploadProgress = 0.3f

            try {
                // Copy file from URI to temp file
                val tempFile = createTempFileFromUri(context, fileUri)
                if (tempFile == null) {
                    errorMessage = "Failed to read file. Please try again."
                    currentStep = ImportStep.SELECT_FILE
                    return@launch
                }

                uploadProgress = 0.5f

                // Upload and process CSV
                csvImportController.uploadCsvFile(fileUri, tempFile).fold(
                    onSuccess = { response ->
                        Log.d("CsvImportViewModel", "Upload successful: ${response.message}")
                        importResponse = response
                        importedTransactions = response.transactions
                        totalRows = response.totalRows
                        validRows = response.validRows
                        skippedRows = response.skippedRows

                        uploadProgress = 1.0f
                        currentStep = ImportStep.REVIEW_TRANSACTIONS
                        showResultsDialog = true

                        // Clean up temp file
                        tempFile.delete()
                    },
                    onFailure = { e ->
                        Log.e("CsvImportViewModel", "Upload failed", e)
                        errorMessage = "Failed to process CSV: ${e.message}"
                        currentStep = ImportStep.SELECT_FILE

                        // Clean up temp file
                        tempFile.delete()
                    }
                )
            } catch (e: Exception) {
                Log.e("CsvImportViewModel", "Upload error", e)
                errorMessage = "Failed to upload file: ${e.message}"
                currentStep = ImportStep.SELECT_FILE
            } finally {
                isUploading = false
            }
        }
    }

    /**
     * Save imported transactions to database
     */
    fun saveTransactionsToDatabase(categoryId: String? = null) {
        viewModelScope.launch {
            isSaving = true
            currentStep = ImportStep.SAVING
            errorMessage = null

            try {
                // Get current user ID from Supabase auth
                val userId = SupabaseClient.client.auth.currentUserOrNull()?.id
                if (userId == null) {
                    errorMessage = "User not authenticated. Please log in again."
                    currentStep = ImportStep.REVIEW_TRANSACTIONS
                    return@launch
                }

                csvImportController.saveTransactionsToDatabase(
                    transactions = importedTransactions,
                    userId = userId,
                    categoryId = categoryId
                ).fold(
                    onSuccess = { count ->
                        Log.d("CsvImportViewModel", "Saved $count transactions")
                        savedTransactionCount = count
                        currentStep = ImportStep.COMPLETE
                        successMessage = "Successfully imported $count transactions!"
                    },
                    onFailure = { e ->
                        Log.e("CsvImportViewModel", "Failed to save transactions", e)
                        errorMessage = "Failed to save transactions: ${e.message}"
                        currentStep = ImportStep.REVIEW_TRANSACTIONS
                    }
                )
            } catch (e: Exception) {
                Log.e("CsvImportViewModel", "Save error", e)
                errorMessage = "Error saving transactions: ${e.message}"
                currentStep = ImportStep.REVIEW_TRANSACTIONS
            } finally {
                isSaving = false
            }
        }
    }

    /**
     * Create a temporary file from URI for uploading
     */
    private fun createTempFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                Log.e("CsvImportViewModel", "Failed to open input stream for URI")
                return null
            }

            // Create temp file
            val tempFile = File.createTempFile("csv_import_", ".csv", context.cacheDir)

            // Copy content
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }

            inputStream.close()

            Log.d("CsvImportViewModel", "Created temp file: ${tempFile.absolutePath}")
            tempFile
        } catch (e: Exception) {
            Log.e("CsvImportViewModel", "Failed to create temp file from URI", e)
            null
        }
    }

    // =====================================================
    // UI ACTIONS
    // =====================================================

    /**
     * Open the import dialog
     */
    fun openImportDialog() {
        showImportDialog = true
        currentStep = ImportStep.SELECT_FILE
        resetState()
    }

    /**
     * Close the import dialog
     */
    fun closeImportDialog() {
        showImportDialog = false
        resetState()
    }

    /**
     * Close results dialog
     */
    fun closeResultsDialog() {
        showResultsDialog = false
    }

    /**
     * Clear error message
     */
    fun clearError() {
        errorMessage = null
    }

    /**
     * Clear success message
     */
    fun clearSuccess() {
        successMessage = null
    }

    /**
     * Reset import state
     */
    fun resetState() {
        selectedFileUri = null
        selectedFileName = ""
        importResponse = null
        importedTransactions = emptyList()
        totalRows = 0
        validRows = 0
        skippedRows = 0
        savedTransactionCount = 0
        errorMessage = null
        successMessage = null
        uploadProgress = 0f
        currentStep = ImportStep.SELECT_FILE
    }

    /**
     * Start over with a new import
     */
    fun startNewImport() {
        resetState()
        showResultsDialog = false
        currentStep = ImportStep.SELECT_FILE
    }
}
