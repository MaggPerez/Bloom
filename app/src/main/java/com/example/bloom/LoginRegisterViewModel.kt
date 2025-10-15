package com.example.bloom

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel

class LoginRegisterViewModel: ViewModel() {
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    val loginController = LoginRegisterController()
    val openAlertDialog = mutableStateOf(false)
    var status by  mutableStateOf<Any>("")

    fun login(): Boolean {
        status = loginController.onHandleLogin(email, password)

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