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
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.badger.databinding.FragmentAllListsBinding
import com.example.badger.ui.adapter.ListAdapter
import com.example.badger.ui.viewmodel.AllListsEvent
import com.example.badger.ui.viewmodel.AllListsUiState
import com.example.badger.ui.viewmodel.AllListsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AllListsFragment : Fragment() {

    private var _binding: FragmentAllListsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AllListsViewModel by viewModels()

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
        _binding = FragmentAllListsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeUiState()
        observeEvents()
    }

    private fun setupRecyclerView() {
        binding.listsRecyclerView.apply {
            adapter = listAdapter
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is AllListsUiState.Loading -> {
                            binding.progressBar.isVisible = true
                        }
                        is AllListsUiState.Success -> {
                            binding.progressBar.isVisible = false
                            listAdapter.submitList(state.lists)
                        }
                        is AllListsUiState.Error -> {
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
                        is AllListsEvent.NavigateToLogin -> {
                            findNavController().navigate(
                                AllListsFragmentDirections.actionAllListsFragmentToLoginFragment()
                            )
                        }
                        is AllListsEvent.NavigateToCreateList -> {
                            findNavController().navigate(
                                AllListsFragmentDirections.actionAllListsFragmentToCreateListFragment()
                            )
                        }
                        is AllListsEvent.NavigateToList -> {
                            findNavController().navigate(
                                AllListsFragmentDirections.actionAllListsFragmentToListFragment(event.listId)
                            )
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
