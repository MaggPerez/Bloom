package com.example.bloom.controllers

import com.example.bloom.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email

class LoginRegisterController() {
    suspend fun onHandleLogin(email: String, password: String): Any {
        if(email.isEmpty() && password.isEmpty()){
            return "Email and Password Fields are Empty"
        }

        //returns error message if there is a missing TextField
        if(email.isEmpty()) { return "Email Field is Empty" }

        if(password.isEmpty()) { return "Password Field is Empty" }

        //logging in user with Supabase Auth
        return try {
            val supabase = SupabaseClient.client

            supabase.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            true
        } catch (e: Exception) {
            "Invalid login, try again"
        }

    }

    suspend fun onHandleRegister(email: String, password: String): Any {
        if(email.isEmpty() && password.isEmpty()){
            return "Email and Password Fields are Empty"
        }

        //returns error message if there is a missing TextField
        if(email.isEmpty()) { return "Email Field is Empty" }

        if(password.isEmpty()) { return "Password Field is Empty" }


        //registering user with Supabase Auth
        return try {
            val supabase = SupabaseClient.client

            supabase.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }

            true
        } catch (e: Exception) {
            "Registration failed: ${e.message}"
        }

    }
}