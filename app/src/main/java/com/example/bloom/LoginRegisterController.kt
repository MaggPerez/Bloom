package com.example.bloom

class LoginRegisterController() {
    fun onHandleLogin(email: String, password: String): Any {
        if(email.isEmpty() && password.isEmpty()){
            return "Email and Password Fields are Empty"
        }

        //returns error message if there is a missing TextField
        if(email.isEmpty()) { return "Email Field is Empty" }

        if(password.isEmpty()) { return "Password Field is Empty" }

        return true

    }

    fun onHandleRegister(email: String, password: String): Any {
        if(email.isEmpty() && password.isEmpty()){
            return "Email and Password Fields are Empty"
        }

        //returns error message if there is a missing TextField
        if(email.isEmpty()) { return "Email Field is Empty" }

        if(password.isEmpty()) { return "Password Field is Empty" }

        return true

    }
}