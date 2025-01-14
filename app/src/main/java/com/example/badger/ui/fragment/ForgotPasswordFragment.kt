package com.example.badger.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.badger.databinding.FragmentForgotPasswordBinding
import com.example.badger.ui.state.ForgotPasswordUiState
import com.example.badger.ui.event.ForgotPasswordEvent
import com.example.badger.ui.viewmodel.ForgotPasswordViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ForgotPasswordFragment : Fragment() {

    private var _binding: FragmentForgotPasswordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ForgotPasswordViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupClickListeners()
        observeUiState()
        observeEvents()
    }

    private fun setupClickListeners() {
        binding.resetButton.setOnClickListener {
            val email = binding.emailEditText.text?.toString()?.trim() ?: ""
            if (validateEmail(email)) {
                viewModel.sendPasswordResetEmail(email)
            }
        }

        binding.backButton.setOnClickListener {
            viewModel.navigateBack()
        }
    }

    private fun validateEmail(email: String): Boolean {
        if (email.isEmpty()) {
            binding.emailLayout.error = "Email is required"
            return false
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.error = "Invalid email format"
            return false
        }
        binding.emailLayout.error = null
        return true
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUi(state)
                    when (state) {
                        is ForgotPasswordUiState.Error -> {
                            Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                            viewModel.resetError()
                        }
                        is ForgotPasswordUiState.Success -> {
                            Toast.makeText(
                                context,
                                "Password reset email sent successfully",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        else -> { /* Other states handled by updateUi */ }
                    }
                }
            }
        }
    }

    private fun updateUi(state: ForgotPasswordUiState) {
        binding.progressBar.isVisible = state is ForgotPasswordUiState.Loading
        binding.resetButton.isEnabled = state !is ForgotPasswordUiState.Loading
        binding.emailLayout.isEnabled = state !is ForgotPasswordUiState.Loading
    }

    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is ForgotPasswordEvent.NavigateBack -> {
                            findNavController().popBackStack()
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
