package com.example.bloom.viewmodel

import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class SettingsViewModel: ViewModel() {
    var isDarkMode = mutableStateOf(false)

    var editProfileName by mutableStateOf("")
}