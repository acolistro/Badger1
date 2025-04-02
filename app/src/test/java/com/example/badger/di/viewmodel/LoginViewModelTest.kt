package com.example.badger.di.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import app.cash.turbine.test
import com.example.badger.MainDispatcherRule
import com.example.badger.data.model.User
import com.example.badger.data.repository.PreferencesRepository
import com.example.badger.data.repository.UserRepository
import com.example.badger.di.RepositoryModule
import com.example.badger.ui.event.LoginEvent
import com.example.badger.ui.state.LoginUiState
import com.example.badger.ui.viewmodel.LoginViewModel
import com.example.badger.utils.TestUserManager
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import dagger.hilt.android.testing.UninstallModules
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import javax.inject.Inject
import javax.inject.Named
import kotlin.test.assertIs
import kotlin.test.assertTrue

@HiltAndroidTest
@Config(
    application = HiltTestApplication::class,
    manifest = Config.NONE,
    sdk = [33]
)
@RunWith(RobolectricTestRunner::class)
@UninstallModules(RepositoryModule::class)  // Add this line
class LoginViewModelTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule(order = 2)
    val mainDispatcherRule = MainDispatcherRule()

    @Inject
    lateinit var userRepository: UserRepository

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    @Inject
    @Named("auth")
    lateinit var auth: FirebaseAuth

    private lateinit var viewModel: LoginViewModel
    private lateinit var testUser: User
    private val testPassword = "BadgerTest123!"

    @Before
    fun setup() {
        hiltRule.inject()
        viewModel = LoginViewModel(userRepository, preferencesRepository, auth)
    }

    @Test
    fun `initial state has correct keep signed in value`() = runTest {
        // Given
        preferencesRepository.setKeepSignedIn(true)

        // When
        advanceUntilIdle() // Add this line
        val initialState = viewModel.uiState.first()

        // Then
        assertIs<LoginUiState.Initial>(initialState)
        assertTrue(initialState.keepSignedIn)
    }

    @Test
    fun `login with valid credentials shows verification required for unverified email`() = runTest {
        // When
        viewModel.login(testUser.email, testPassword)

        // Advance coroutines
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertIs<LoginUiState.VerificationRequired>(state)
    }

    @Test
    fun `login with invalid credentials shows error state`() = runTest {
        // Setup mock behavior
        Mockito.`when`(userRepository.signIn(
            email = "wrong@email.com",
            password = "wrongpass"
        )).thenReturn(Result.failure(Exception("Invalid credentials")))

        viewModel.uiState.test {
            // Initial state
            assertIs<LoginUiState.Initial>(awaitItem())

            // Trigger login
            viewModel.login("wrong@email.com", "wrongpass")

            // Loading state
            assertIs<LoginUiState.Loading>(awaitItem())

            // Error state
            assertIs<LoginUiState.Error>(awaitItem())

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `keep signed in preference is persisted after successful login`() = runTest {
        // Given
        viewModel.onKeepSignedInChanged(true)

        // When
        viewModel.login(testUser.email, testPassword)

        // Advance coroutines
        advanceUntilIdle()

        // Then
        assertTrue(preferencesRepository.shouldKeepSignedIn())
    }

    @Test
    fun `resend verification email updates state correctly`() = runTest {
        // Given
        viewModel.login(testUser.email, testPassword)
        advanceUntilIdle()

        // When
        viewModel.resendVerificationEmail()
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertIs<LoginUiState.VerificationEmailSent>(state)
    }

    @Test
    fun `navigation events are emitted correctly`() = runTest {
        val events = mutableListOf<LoginEvent>()
        val job = launch {
            viewModel.events.collect { events.add(it) }
        }

        // When
        viewModel.navigateToSignUp()
        viewModel.navigateToForgotPassword()

        // Advance coroutines
        advanceUntilIdle()

        // Then
        assertTrue(events.contains(LoginEvent.NavigateToSignUp))
        assertTrue(events.contains(LoginEvent.NavigateToForgotPassword))

        job.cancel()
    }
}
