package com.example.bloom.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.bloom.controllers.LoginRegisterController

class LoginRegisterViewModel: ViewModel() {
    //register variables
    var fullName by mutableStateOf("")
    var username by mutableStateOf("")
    var createEmail by mutableStateOf("")
    var createPassword by mutableStateOf("")

    //login variables
    var loginEmail by mutableStateOf("")
    var loginPassword by mutableStateOf("")
    val loginController = LoginRegisterController()
    val openAlertDialog = mutableStateOf(false)
    var status by mutableStateOf<Any>("")
    var isEmailLoading by mutableStateOf(false)
    var isGoogleLoading by mutableStateOf(false)


    var passwordVisible by mutableStateOf(false)

    suspend fun login(): Boolean {
        isEmailLoading = true
        try {
            status = loginController.onHandleLogin(loginEmail, loginPassword)

            //if there is an error message, an Alert Dialog will show up
            if(status is String){
                openAlertDialog.value = true
                return false
            }
            else{
                return true
            }
        } finally {
            isEmailLoading = false
        }
    }

    suspend fun register(): Boolean {
        isEmailLoading = true
        try {
            status = loginController.onHandleRegister(fullName, username, createEmail, createPassword)

            //if there is an error message, an Alert Dialog will show up
            if(status is String){
                openAlertDialog.value = true
                return false
            }
            else{
                return true
            }
        } finally {
            isEmailLoading = false
        }
    }
}