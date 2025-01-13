package com.example.badger.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.badger.R
import com.example.badger.databinding.FragmentDashboardBinding
import com.example.badger.ui.adapter.ListAdapter
import com.example.badger.ui.viewmodel.DashboardEvent
import com.example.badger.ui.viewmodel.DashboardUiState
import com.example.badger.ui.viewmodel.DashboardViewModel
import com.google.android.material.navigation.NavigationView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()

    private val listAdapter = ListAdapter(
        onListClick = { list ->
            viewModel.openList(list.id)
        },
        onFavoriteClick = { list, favorite ->
            viewModel.toggleFavorite(list.id, favorite)
        }
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false).apply {
            lifecycleOwner = viewLifecycleOwner
            viewModel = this@DashboardFragment.viewModel
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupDrawer()
        setupRecyclerView()
        setupClickListeners()
        observeUiState()
        observeEvents()
    }

    private fun setupToolbar() {
        (activity as? AppCompatActivity)?.setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }
    }

    private fun setupDrawer() {
        binding.navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_dashboard -> {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_all_lists -> {
                    // Navigate to all lists
                    findNavController().navigate(R.id.action_dashboardFragment_to_allListsFragment)
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_profile -> {
                    // Navigate to profile
                    findNavController().navigate(R.id.action_dashboardFragment_to_profileFragment)
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_settings -> {
                    // Navigate to settings
                    findNavController().navigate(R.id.action_dashboardFragment_to_settingsFragment)
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                R.id.nav_logout -> {
                    viewModel.signOut()
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        binding.favoriteListsRecyclerView.apply {
            adapter = listAdapter
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
        }
    }

    private fun setupClickListeners() {
        binding.createListFab.setOnClickListener {
            viewModel.createNewList()
        }

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refresh()
        }
    }

    private fun updateNavigationHeader(state: DashboardUiState.Success) {
        val headerView = binding.navigationView.getHeaderView(0)
        headerView?.apply {
            findViewById<android.widget.TextView>(R.id.userName)?.text = state.user.username
            findViewById<android.widget.TextView>(R.id.userEmail)?.text = state.user.email
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.swipeRefresh.isRefreshing = false

                    when (state) {
                        is DashboardUiState.Loading -> {
                            binding.progressBar.isVisible = true
                            binding.emptyStateText.isVisible = false
                        }
                        is DashboardUiState.Success -> {
                            binding.progressBar.isVisible = false
                            binding.welcomeText.text = "Welcome, ${state.user.username}"

                            listAdapter.submitList(state.favoriteLists)
                            binding.emptyStateText.isVisible = state.favoriteLists.isEmpty()

                            // Update navigation drawer header
                            updateNavigationHeader(state)
                        }
                        is DashboardUiState.Error -> {
                            binding.progressBar.isVisible = false
                            Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
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
                        is DashboardEvent.NavigateToLogin -> {
                            findNavController().navigate(R.id.action_dashboardFragment_to_loginFragment)
                        }
                        is DashboardEvent.NavigateToCreateList -> {
                            findNavController().navigate(R.id.action_dashboardFragment_to_createListFragment)
                        }
                        is DashboardEvent.NavigateToList -> {
                            val action = DashboardFragmentDirections.actionDashboardFragmentToListFragment(event.listId)
                            findNavController().navigate(action)
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

    companion object {
        private const val TAG = "DashboardFragment"
    }
}
