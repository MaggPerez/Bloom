package com.example.bloom.viewmodel

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bloom.controllers.SettingsController
import com.example.bloom.controllers.UserProfile
import com.example.bloom.datastore.PreferencesManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.InputStream

class SettingsViewModel(context: Context) : ViewModel() {
    private val settingsController = SettingsController()
    private val preferencesManager = PreferencesManager(context)

    // =====================================================
    // USER PROFILE STATE
    // =====================================================
    var userProfile by mutableStateOf<UserProfile?>(null)
        private set

    // =====================================================
    // EDITING STATES
    // =====================================================
    var isEditingFullName by mutableStateOf(false)
        private set

    var isEditingUsername by mutableStateOf(false)
        private set

    var editingFullName by mutableStateOf("")
    var editingUsername by mutableStateOf("")

    // =====================================================
    // THEME STATE
    // =====================================================
    var themePreference by mutableStateOf("system")
        private set

    // =====================================================
    // AVATAR STATE
    // =====================================================
    var avatarUri by mutableStateOf<Uri?>(null)
    var isUploadingAvatar by mutableStateOf(false)
        private set

    // =====================================================
    // LOADING & ERROR STATES
    // =====================================================
    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    // =====================================================
    // USERNAME VALIDATION STATE
    // =====================================================
    var usernameValidationMessage by mutableStateOf<String?>(null)
        private set

    var isUsernameAvailable by mutableStateOf(true)
        private set

    private var usernameCheckJob: Job? = null

    // =====================================================
    // INITIALIZATION
    // =====================================================
    init {
        loadUserData()
        loadThemePreference()
    }

    // =====================================================
    // DATA LOADING
    // =====================================================
    fun loadUserData() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            try {
                loadUserProfile()
            } catch (e: Exception) {
                errorMessage = "Failed to load user data: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    private suspend fun loadUserProfile() {
        settingsController.getUserProfile().fold(
            onSuccess = { profile ->
                userProfile = profile
            },
            onFailure = { e ->
                errorMessage = "Failed to load profile: ${e.message}"
            }
        )
    }

    private fun loadThemePreference() {
        viewModelScope.launch {
            preferencesManager.getThemePreference().collectLatest { theme ->
                themePreference = theme
            }
        }
    }

    // =====================================================
    // PROFILE UPDATES
    // =====================================================
    fun saveFullName() {
        if (editingFullName.isBlank()) {
            errorMessage = "Full name cannot be empty"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            settingsController.updateFullName(editingFullName).fold(
                onSuccess = {
                    // Update local state
                    userProfile?.let { profile ->
                        userProfile = profile.copy(
                            full_name = editingFullName
                        )
                    }
                    isEditingFullName = false
                    successMessage = "Full name updated successfully"
                },
                onFailure = { e ->
                    errorMessage = "Failed to update full name: ${e.message}"
                }
            )

            isLoading = false
        }
    }

    fun saveUsername() {
        if (!validateUsername(editingUsername)) {
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null

            // Check availability first
            settingsController.checkUsernameAvailability(editingUsername).fold(
                onSuccess = { isAvailable ->
                    if (!isAvailable) {
                        errorMessage = "Username is already taken"
                        isLoading = false
                        return@fold
                    }

                    // Username is available, proceed with update
                    settingsController.updateUsername(editingUsername).fold(
                        onSuccess = {
                            userProfile = userProfile?.copy(username = editingUsername)
                            isEditingUsername = false
                            successMessage = "Username updated successfully"
                        },
                        onFailure = { e ->
                            errorMessage = "Failed to update username: ${e.message}"
                        }
                    )
                },
                onFailure = { e ->
                    errorMessage = "Failed to check username availability: ${e.message}"
                }
            )

            isLoading = false
        }
    }

    fun checkUsernameAvailability() {
        if (!validateUsername(editingUsername)) {
            return
        }

        // Cancel previous check if still running
        usernameCheckJob?.cancel()

        usernameCheckJob = viewModelScope.launch {
            // Debounce: wait 500ms before checking
            delay(500)

            settingsController.checkUsernameAvailability(editingUsername).fold(
                onSuccess = { isAvailable ->
                    isUsernameAvailable = isAvailable
                    usernameValidationMessage = if (isAvailable) {
                        "Username is available"
                    } else {
                        "Username is already taken"
                    }
                },
                onFailure = { e ->
                    usernameValidationMessage = "Failed to check availability: ${e.message}"
                    isUsernameAvailable = false
                }
            )
        }
    }

    private fun validateUsername(username: String): Boolean {
        return when {
            username.isBlank() -> {
                usernameValidationMessage = "Username cannot be empty"
                isUsernameAvailable = false
                false
            }
            username.length < 3 -> {
                usernameValidationMessage = "Username must be at least 3 characters"
                isUsernameAvailable = false
                false
            }
            username.length > 20 -> {
                usernameValidationMessage = "Username must be at most 20 characters"
                isUsernameAvailable = false
                false
            }
            !username.matches(Regex("^[a-zA-Z0-9_]+$")) -> {
                usernameValidationMessage = "Username can only contain letters, numbers, and underscores"
                isUsernameAvailable = false
                false
            }
            else -> {
                usernameValidationMessage = null
                true
            }
        }
    }

    fun uploadAvatar(uri: Uri, context: Context) {
        viewModelScope.launch {
            isUploadingAvatar = true
            errorMessage = null

            try {
                val userId = userProfile?.id ?: throw Exception("User ID not found")

                // Read image bytes from URI
                val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                    ?: throw Exception("Failed to read image")

                val imageBytes = inputStream.readBytes()
                inputStream.close()

                // Compress/resize if needed (optional - can add later)
                // For now, just upload as-is

                // Upload to Supabase Storage
                settingsController.uploadAvatar(imageBytes, userId).fold(
                    onSuccess = { avatarUrl ->
                        // Update database with avatar URL
                        settingsController.updateAvatarUrl(avatarUrl).fold(
                            onSuccess = {
                                userProfile = userProfile?.copy(avatar_url = avatarUrl)
                                avatarUri = uri
                                successMessage = "Avatar uploaded successfully"
                            },
                            onFailure = { e ->
                                errorMessage = "Failed to save avatar URL: ${e.message}"
                            }
                        )
                    },
                    onFailure = { e ->
                        errorMessage = "Failed to upload avatar: ${e.message}"
                    }
                )
            } catch (e: Exception) {
                errorMessage = "Failed to upload avatar: ${e.message}"
            } finally {
                isUploadingAvatar = false
            }
        }
    }

    // =====================================================
    // THEME UPDATES
    // =====================================================
    fun updateTheme(newTheme: String) {
        viewModelScope.launch {
            try {
                // Save to DataStore for instant local access
                preferencesManager.saveThemePreference(newTheme)

                // Sync to Supabase for cross-device
                settingsController.updateThemePreference(newTheme).fold(
                    onSuccess = {
                        userProfile = userProfile?.copy(theme_preference = newTheme)
                        // themePreference will be updated automatically via DataStore flow
                    },
                    onFailure = { e ->
                        errorMessage = "Failed to sync theme preference: ${e.message}"
                    }
                )
            } catch (e: Exception) {
                errorMessage = "Failed to update theme: ${e.message}"
            }
        }
    }

    // =====================================================
    // UI HELPERS
    // =====================================================
    fun startEditingFullName() {
        editingFullName = userProfile?.full_name ?: ""
        isEditingFullName = true
    }

    fun startEditingUsername() {
        editingUsername = userProfile?.username ?: ""
        isEditingUsername = true
        usernameValidationMessage = null
        isUsernameAvailable = true
    }

    fun cancelEditing() {
        isEditingFullName = false
        isEditingUsername = false
        editingFullName = ""
        editingUsername = ""
        usernameValidationMessage = null
        isUsernameAvailable = true
    }

    fun clearError() {
        errorMessage = null
    }

    fun clearSuccessMessage() {
        successMessage = null
    }
}