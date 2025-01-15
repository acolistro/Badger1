package com.example.badger.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.badger.R
import com.example.badger.databinding.FragmentLoginBinding
import com.example.badger.ui.state.LoginUiState
import com.example.badger.ui.event.LoginEvent
import com.example.badger.ui.viewmodel.LoginViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@LoginFragment.viewModel
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeUiState()
        observeEvents()
    }

    private fun setupClickListeners() {
        with(binding) {
            loginButton.setOnClickListener {
                val email = emailEditText.text?.toString()?.trim() ?: ""
                val password = passwordEditText.text?.toString()?.trim() ?: ""

                if (validateInput(email, password)) {
                    viewModel.login(email, password)
                }
            }

            signUpButton.setOnClickListener {
                viewModel.navigateToSignUp()
            }

            forgotPasswordButton.setOnClickListener {
                viewModel.navigateToForgotPassword()
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        with(binding) {
            if (email.isEmpty()) {
                emailLayout.error = "Email is required"
                return false
            }
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailLayout.error = "Invalid email format"
                return false
            }
            emailLayout.error = null

            if (password.isEmpty()) {
                passwordLayout.error = "Password is required"
                return false
            }
            if (password.length < 6) {
                passwordLayout.error = "Password must be at least 6 characters"
                return false
            }
            passwordLayout.error = null

            return true
        }
    }

    private fun showVerificationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Email Verification Required")
            .setMessage("Please verify your email address before continuing. Check your inbox for the verification link.")
            .setPositiveButton("Check Status") { _, _ ->
                viewModel.checkVerificationStatus()
            }
            .setNegativeButton("Resend Email") { _, _ ->
                viewModel.resendVerificationEmail()
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is LoginUiState.Error -> {
                            Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                            viewModel.resetError()
                        }
                        is LoginUiState.VerificationRequired -> {
                            showVerificationDialog()
                        }
                        is LoginUiState.VerificationEmailSent -> {
                            Toast.makeText(context, "Verification email sent", Toast.LENGTH_SHORT).show()
                        }
                        else -> { /* Other states handled by data binding */
                            binding.progressBar.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is LoginEvent.NavigateToDashboard -> {
                            findNavController().navigate(R.id.action_loginFragment_to_dashboardFragment)
                        }
                        is LoginEvent.NavigateToSignUp -> {
                            findNavController().navigate(R.id.action_loginFragment_to_signUpFragment)
                        }
                        is LoginEvent.NavigateToForgotPassword -> {
                            findNavController().navigate(R.id.action_loginFragment_to_forgotPasswordFragment)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
