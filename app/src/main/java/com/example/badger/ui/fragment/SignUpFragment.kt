package com.example.badger.ui.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.badger.R
import com.example.badger.databinding.FragmentSignupBinding
import com.example.badger.ui.state.SignUpUiState
import com.example.badger.ui.viewmodel.SignUpViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SignUpViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is SignUpUiState.Error -> {
                        Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                    }
                    is SignUpUiState.VerificationRequired -> {
                        // Show verification dialog
                        showVerificationDialog()
                    }
                    is SignUpUiState.Success -> {
                        // Navigate to home screen
                        // findNavController().navigate(...)
                    }
                    else -> {
                        // Handle other states
                    }
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.signUpButton.setOnClickListener {
            with(binding) {
                viewModel?.signUp(
                    firstNameEditText.text.toString(),
                    lastNameEditText.text.toString(),
                    usernameEditText.text.toString(),
                    emailEditText.text.toString(),
                    phoneEditText.text.toString(),
                    passwordEditText.text.toString(),
                    confirmPasswordEditText.text.toString(),
                    requireActivity()
                )
            }
        }
    }

    private fun showVerificationDialog() {
        // Show a dialog to enter verification code
        // This is a basic implementation - you might want to create a custom dialog
        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Verification Required")
            .setMessage("Please check your email for verification link and enter the SMS code below")
            .setView(R.layout.dialog_verification_code)
            .setPositiveButton("Verify") { dialog, _ ->
                val codeEditText = (dialog as AlertDialog)
                    .findViewById<EditText>(R.id.verificationCodeEditText)
                val code = codeEditText?.text.toString()
                if (code.isNotEmpty()) {
                    viewModel.verifyPhoneCode(code)
                }
            }
            .setNegativeButton("Cancel", null)
            .create()
        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
