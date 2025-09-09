package com.rkfinance.loanmanager.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rkfinance.loanmanager.data.database.LoanManagerDatabase
import com.rkfinance.loanmanager.data.entities.Client
import com.rkfinance.loanmanager.data.repository.LoanRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ClientViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: LoanRepository

    val allClients: StateFlow<List<Client>>

    init {
        val clientDao = LoanManagerDatabase.getDatabase(application).clientDao()
        val loanDao = LoanManagerDatabase.getDatabase(application).loanDao()
        val paymentDao = LoanManagerDatabase.getDatabase(application).paymentDao()
        repository = LoanRepository(clientDao, loanDao, paymentDao)

        allClients = repository.getAllClients()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }

    fun insertClient(client: Client) = viewModelScope.launch {
        repository.insertClient(client)
    }

    fun updateClient(client: Client) = viewModelScope.launch {
        repository.updateClient(client)
    }

    fun deleteClient(client: Client) = viewModelScope.launch {
        repository.deleteClient(client)
    }
}