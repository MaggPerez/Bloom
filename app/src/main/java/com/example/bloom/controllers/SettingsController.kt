package com.example.bloom.controllers

import com.example.bloom.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.storage.storage

class SettingsController {
    private val supabase = SupabaseClient.client

    private fun getUserId(): String? {
        return supabase.auth.currentUserOrNull()?.id
    }

    // =====================================================
    // USER PROFILE OPERATIONS
    // =====================================================

    suspend fun getUserProfile(): Result<UserProfile> {
        return try {
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))

            val profiles = supabase.from("user_profiles")
                .select {
                    filter {
                        eq("id", userId)
                    }
                }
                .decodeList<UserProfile>()

            profiles.firstOrNull()?.let {
                Result.success(it)
            } ?: Result.failure(Exception("User profile not found"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateFullName(fullName: String): Result<Boolean> {
        return try {
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))

            supabase.from("user_profiles")
                .update({
                    set("full_name", fullName)
                }) {
                    filter {
                        eq("id", userId)
                    }
                }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUsername(username: String): Result<Boolean> {
        return try {
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))

            supabase.from("user_profiles")
                .update({
                    set("username", username)
                }) {
                    filter {
                        eq("id", userId)
                    }
                }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun checkUsernameAvailability(username: String): Result<Boolean> {
        return try {
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))

            val profiles = supabase.from("user_profiles")
                .select {
                    filter {
                        // Case-insensitive username check, excluding current user
                        ilike("username", username)
                        neq("id", userId)
                    }
                }
                .decodeList<UserProfile>()

            // If no profiles found, username is available
            Result.success(profiles.isEmpty())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateThemePreference(theme: String): Result<Boolean> {
        return try {
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))

            supabase.from("user_profiles")
                .update({
                    set("theme_preference", theme)
                }) {
                    filter {
                        eq("id", userId)
                    }
                }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =====================================================
    // AVATAR OPERATIONS
    // =====================================================

    suspend fun uploadAvatar(imageBytes: ByteArray, userId: String): Result<String> {
        return try {
            val bucketName = "avatars"
            val filePath = "$userId/avatar.jpg"

            // Delete old avatar if it exists
            try {
                supabase.storage.from(bucketName).delete(filePath)
            } catch (e: Exception) {
                // Ignore if file doesn't exist
            }

            // Upload new avatar
            supabase.storage.from(bucketName).upload(filePath, imageBytes)

            // Get public URL
            val publicUrl = supabase.storage.from(bucketName).publicUrl(filePath)

            Result.success(publicUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAvatarUrl(avatarUrl: String): Result<Boolean> {
        return try {
            val userId = getUserId() ?: return Result.failure(Exception("User not authenticated"))

            supabase.from("user_profiles")
                .update({
                    set("avatar_url", avatarUrl)
                }) {
                    filter {
                        eq("id", userId)
                    }
                }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAvatar(userId: String): Result<Boolean> {
        return try {
            val bucketName = "avatars"
            val filePath = "$userId/avatar.jpg"

            supabase.storage.from(bucketName).delete(filePath)

            // Update database to remove avatar_url
            supabase.from("user_profiles")
                .update({
                    set("avatar_url", null as String?)
                }) {
                    filter {
                        eq("id", userId)
                    }
                }

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}