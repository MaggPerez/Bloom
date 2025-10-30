package com.example.bloom.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.bloom.LoginRegisterController

class LoginRegisterViewModel: ViewModel() {
    //register variables
    var createEmail by mutableStateOf("")
    var createPassword by mutableStateOf("")

    //login variables
    var loginEmail by mutableStateOf("")
    var loginPassword by mutableStateOf("")
    val loginController = LoginRegisterController()
    val openAlertDialog = mutableStateOf(false)
    var status by mutableStateOf<Any>("")

    fun login(): Boolean {
        status = loginController.onHandleLogin(loginEmail, loginPassword)

        //if there is an error message, an Alert Dialog will show up
        if(status is String){
            openAlertDialog.value = true
            return false
        }
        else{
            return true
        }

    }

    fun register(): Boolean {
        status = loginController.onHandleRegister(createEmail, createPassword)

        //if there is an error message, an Alert Dialog will show up
        if(status is String){
            openAlertDialog.value = true
            return false
        }
        else{
            return true
        }
    }
}