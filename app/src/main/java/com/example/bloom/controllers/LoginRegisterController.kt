package com.example.bloom.controllers

import com.example.bloom.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.Serializable

@Serializable
data class UserProfile(
    val id: String,
    val email: String,
    val username: String? = null,
    val full_name: String? = null,
    val avatar_url: String? = null,
    val theme_preference: String = "system"
)

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

    suspend fun onHandleRegister(fullName: String, username: String, email: String, password: String): Any {
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

            //inserting fullname and username to user_profiles table
            val userId = supabase.auth.currentUserOrNull()?.id ?: throw Exception("User ID not found")

            val userProfile = UserProfile(
                id = userId,
                email = email,
                username = username,
                full_name = fullName
            )

            supabase.from("user_profiles").insert(userProfile)

            true
        } catch (e: Exception) {
            "Registration failed: ${e.message}"
        }

    }
}