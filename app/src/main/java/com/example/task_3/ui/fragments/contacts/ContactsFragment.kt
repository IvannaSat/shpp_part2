package com.example.task_3.ui.fragments.contacts

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.task_3.R
import com.example.task_3.databinding.FragmentContactsBinding
import com.example.task_3.domain.model.Contact
import com.example.task_3.ui.fragments.BaseFragment
import com.example.task_3.ui.fragments.DialogFragment
import com.example.task_3.ui.fragments.viewpager.ViewPagerFragment
import com.example.task_3.ui.fragments.viewpager.ViewPagerFragmentDirections
import com.example.task_3.utils.Constants
import com.example.task_3.utils.Fragments
import com.example.task_3.utils.ext.log
import com.example.task_3.utils.ext.showErrorSnackBar
import com.google.android.material.snackbar.Snackbar

class ContactsFragment : BaseFragment<FragmentContactsBinding>(FragmentContactsBinding:: inflate) {

    private val viewModel: ContactsViewModel by viewModels()

    private val adapter: ContactsListAdapter by lazy {
        ContactsListAdapter(listener = object : ContactItemClickListener {

            override fun onClickDelete(contact: Contact) {
                deleteUserWithRestore(contact)
            }

            override fun onClickContact(
                contact: Contact,
                transitionPairs: Array<Pair<View, String>>
            ) {
                if(adapter.isMultiselectMode) {
                    contact.isChecked = !contact.isChecked
                    if(contact.isChecked && viewModel.selectContacts.value?.contains(contact) == false)
                        viewModel.addSelectContact(contact)
                    if(!contact.isChecked && viewModel.selectContacts.value?.contains(contact) == true)
                        viewModel.deleteSelectContact(contact)
                    if(viewModel.selectContacts.value?.size == 0 )
                        viewModel.changeMultiselectMode()
                } else {
                    val extras = FragmentNavigatorExtras(*transitionPairs)
                     val direction =
                         ViewPagerFragmentDirections.actionViewPagerFragmentToContactProfile(contact)
                    navController.navigate(direction, extras)
                }
            }

            override fun onLongClick(contact: Contact) {
                removeChecked()
                contact.isChecked = true
                viewModel.changeMultiselectMode()
                if(viewModel.isMultiselect.value == true) viewModel.addSelectContact(contact)
                else removeChecked()
            }

            override fun onOpenNewFragment(
                contact: Contact,
                transitionPairs: Array<Pair<View, String>>
            ) {
                val direction =
                    ViewPagerFragmentDirections.actionViewPagerFragmentToContactProfile(contact)
                val extras = FragmentNavigatorExtras(*transitionPairs)
                navController.navigate(direction, extras)
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialRecyclerview()
        setClickListener()
        setObservers()
    }

    private fun setObservers() {
        viewModel.contactList.observe(viewLifecycleOwner) {
            adapter.submitList(it)
        }
        viewModel.isMultiselect.observe(viewLifecycleOwner) {
            binding.recyclerViewContacts.adapter = adapter
            adapter.isMultiselectMode = it
            with(binding) {
                textViewAddContacts.visibility = if(it) View.GONE else View.VISIBLE
                imageViewDeleteSelectMode.visibility = if(it) View.VISIBLE else View.GONE
            }

        }
    }

    private fun initialRecyclerview() {
        val layoutManager = LinearLayoutManager(context)
        binding.recyclerViewContacts.layoutManager = layoutManager
        binding.recyclerViewContacts.adapter = adapter
        ItemTouchHelper(setTouchCallBackListener()).attachToRecyclerView(binding.recyclerViewContacts)
    }

    private fun setClickListener() {
        navigationBack()
        showAddContactsDialog()
        deleteSelectedContacts()
    }

    private fun showAddContactsDialog() {
        binding.textViewAddContacts.setOnClickListener {
            val dialogFragment = DialogFragment()
            dialogFragment.setViewModel(viewModel)
            dialogFragment.show(parentFragmentManager, Constants.DIALOG_TAG)
        }
    }

    private fun navigationBack() {
        binding.imageViewNavigationBack.setOnClickListener{
            (parentFragment as ViewPagerFragment)?.openFragment(Fragments.USER_PROFILE.ordinal)
        }
        val callback = object : OnBackPressedCallback (true) {
            override fun handleOnBackPressed() {
                (requireParentFragment() as? ViewPagerFragment)?.openFragment(Fragments.USER_PROFILE.ordinal)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    private fun deleteSelectedContacts() {
        binding.imageViewDeleteSelectMode.setOnClickListener{
            val size = viewModel.selectContacts.value?.size
            viewModel.deleteSelectList()
            binding.root.showErrorSnackBar(requireContext(), if(size!! > 1) R.string.contacts_removed
                else R.string.contact_removed)
            viewModel.changeMultiselectMode()
        }
    }

    private fun removeChecked() {
        viewModel.contactList.value?.forEach { contact ->
            contact.isChecked = false
        }
    }

    private fun setTouchCallBackListener(): ItemTouchHelper.Callback {
        return object : ItemTouchHelper.SimpleCallback(0, 0) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun getSwipeDirs(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                return ItemTouchHelper.LEFT
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                deleteUserWithRestore(
                    viewModel.contactList.value?.getOrNull(viewHolder.bindingAdapterPosition)!!
                )
            }

            override fun isItemViewSwipeEnabled(): Boolean {
                return viewModel.isMultiselect.value == false
            }
        }
    }

    fun deleteUserWithRestore(contact: Contact) {
        val position = getPosition(contact)
        if(viewModel.deleteContact(contact)) {
            Snackbar.make(
                binding.recyclerViewContacts,
                getString(R.string.s_has_been_removed).format(contact.name),
                Snackbar.LENGTH_LONG
            ).setAction(getString(R.string.restore)) {
                viewModel.addContact(contact, position)
            }.show()
        }
    }

    private fun getPosition(currentContact: Contact): Int {
        val contactList = viewModel.contactList.value ?: emptyList()
        return contactList.indexOfFirst { it == currentContact }
    }
}