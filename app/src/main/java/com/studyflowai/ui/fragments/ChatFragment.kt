package com.studyflowai.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.MenuInflater
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import com.studyflowai.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.recyclerview.widget.RecyclerView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.studyflowai.agent.GuideAgent
import com.studyflowai.databinding.FragmentChatBinding
import com.studyflowai.ui.adapter.ChatAdapter
import com.studyflowai.ui.adapter.ChatMessage
import com.studyflowai.utils.SecurityManager
import kotlinx.coroutines.launch

import androidx.fragment.app.activityViewModels
import com.studyflowai.viewmodel.ChatViewModel

class ChatFragment : Fragment() {

    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!
    private lateinit var chatAdapter: ChatAdapter
    private val viewModel: ChatViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatAdapter = ChatAdapter()
        binding.rvChat.apply {
            adapter = chatAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        binding.btnSendMessage.setOnClickListener {
            val text = binding.etChatMessage.text.toString()
            if (text.isNotBlank()) {
                viewModel.sendMessage(text)
                binding.etChatMessage.setText("")
            }
        }

        observeViewModel()
        setupMenu()
    }

    private fun setupMenu() {
        binding.toolbarChat.inflateMenu(R.menu.chat_menu)
        binding.toolbarChat.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_new_chat -> {
                    viewModel.clearChat()
                    true
                }
                R.id.action_history -> {
                    showHistoryDialog()
                    true
                }
                else -> false
            }
        }
    }

    private fun showHistoryDialog() {
        val archivedMessages = viewModel.archivedMessages.value ?: emptyList()

        if (archivedMessages.isEmpty()) {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Historique")
                .setMessage("Aucun message archivé pour le moment.")
                .setPositiveButton("OK", null)
                .show()
            return
        }

        val recyclerView = RecyclerView(requireContext()).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = ChatAdapter().apply {
                setMessages(archivedMessages)
            }
            setPadding(16, 16, 16, 16)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Anciens Messages")
            .setView(recyclerView)
            .setPositiveButton("Fermer", null)
            .show()
    }

    private fun observeViewModel() {
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            chatAdapter.setMessages(messages)
            if (messages.isNotEmpty()) {
                binding.rvChat.smoothScrollToPosition(messages.size - 1)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnSendMessage.isEnabled = !isLoading
            // Optionnel : afficher une barre de progression
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
