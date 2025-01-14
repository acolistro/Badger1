package com.example.badger.util

object ValidationUtil {
    private val PASSWORD_PATTERN = Regex("^(?=.*[0-9])(?=.*[A-Z])(?=.*[!@#\$%^&*(),.?\":{}|<>])[a-zA-Z0-9!@#\$%^&*(),.?\":{}|<>]{8,}\$")
    private val USERNAME_PATTERN = Regex("^[a-zA-Z0-9_]{3,20}\$")
    private val PHONE_PATTERN = Regex("^\\+?[1-9]\\d{1,14}\$")
    private val NAME_PATTERN = Regex("^[a-zA-Z\\s-']{2,30}\$")

    fun isValidPassword(password: String): Boolean {
        return PASSWORD_PATTERN.matches(password)
    }

    fun isValidUsername(username: String): Boolean {
        return USERNAME_PATTERN.matches(username)
    }

    fun isValidPhone(phone: String): Boolean {
        return PHONE_PATTERN.matches(phone)
    }

    fun isValidName(name: String): Boolean {
        return NAME_PATTERN.matches(name)
    }

    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    fun passwordsMatch(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }

    // Remove potential SQL injection characters
    fun sanitizeInput(input: String): String {
        return input.replace(Regex("[;'\"\\\\]"), "")
    }
}
