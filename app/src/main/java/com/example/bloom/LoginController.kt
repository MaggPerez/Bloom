package com.example.bloom

class LoginController() {
    fun onHandleLogin(email: String, password: String): Any {
        if(email.isEmpty() && password.isEmpty()){
            return "Email and Password Fields are Empty"
        }

        if(email.isEmpty()) { return "Email Field is Empty" }
        if(password.isEmpty()) { return "Password Field is Empty" }
        return true

    }
}