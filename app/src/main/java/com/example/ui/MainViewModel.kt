package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.BusinessProfile
import com.example.data.model.ClientContact
import com.example.data.model.ProductService
import com.example.data.model.Quote
import com.example.data.model.QuoteItem
import com.example.data.model.QuoteStatus
import com.example.data.model.QuoteWithItems
import com.example.data.repository.QuoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class QuoteBuilderUiState(
    val quote: Quote = Quote(quoteNumber = "PRE-001"),
    val items: List<QuoteItem> = emptyList(),
    val isEditing: Boolean = false,
    val isSaved: Boolean = false,
    val savedQuoteId: Long? = null
) {
    val subtotal: Double
        get() = items.sumOf { it.total }

    val discountAmount: Double
        get() = subtotal * (quote.discountPercent / 100.0)

    val subtotalAfterDiscount: Double
        get() = (subtotal - discountAmount).coerceAtLeast(0.0)

    val taxAmount: Double
        get() = subtotalAfterDiscount * (quote.taxPercent / 100.0)

    val total: Double
        get() = subtotalAfterDiscount + taxAmount

    fun toQuoteWithItems(): QuoteWithItems {
        return QuoteWithItems(quote = quote, items = items)
    }
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: QuoteRepository
    private val prefs = application.getSharedPreferences("app_preferences", android.content.Context.MODE_PRIVATE)

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("dark_mode_enabled", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    fun toggleDarkMode() {
        val next = !_isDarkMode.value
        _isDarkMode.value = next
        prefs.edit().putBoolean("dark_mode_enabled", next).apply()
    }

    val businessProfile: StateFlow<BusinessProfile?>
    val catalogItems: StateFlow<List<ProductService>>
    val contacts: StateFlow<List<ClientContact>>
    val quotes: StateFlow<List<QuoteWithItems>>

    private val _builderState = MutableStateFlow(QuoteBuilderUiState())
    val builderState: StateFlow<QuoteBuilderUiState> = _builderState.asStateFlow()

    private val _selectedQuoteDetail = MutableStateFlow<QuoteWithItems?>(null)
    val selectedQuoteDetail: StateFlow<QuoteWithItems?> = _selectedQuoteDetail.asStateFlow()

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = QuoteRepository(db)

        businessProfile = repository.businessProfile.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            null
        )

        catalogItems = repository.allCatalogItems.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        contacts = repository.allContacts.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

        quotes = repository.allQuotesWithItems.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )
    }

    // --- QUOTE BUILDER OPERATIONS ---

    fun prepareNewQuote() {
        viewModelScope.launch {
            val nextNumber = repository.generateNextQuoteNumber()
            val profile = repository.getBusinessProfileSync()
            _builderState.value = QuoteBuilderUiState(
                quote = Quote(
                    quoteNumber = nextNumber,
                    currencySymbol = profile?.currencySymbol ?: "$",
                    taxPercent = profile?.defaultTaxPercent ?: 0.0,
                    notes = profile?.defaultNotes ?: "Presupuesto válido por 15 días.",
                    terms = profile?.paymentTerms ?: "50% anticipo, 50% al finalizar."
                ),
                items = emptyList(),
                isEditing = false
            )
        }
    }

    fun loadQuoteForEdit(quoteId: Long) {
        viewModelScope.launch {
            val quoteWithItems = repository.getQuoteWithItemsSync(quoteId)
            if (quoteWithItems != null) {
                _builderState.value = QuoteBuilderUiState(
                    quote = quoteWithItems.quote,
                    items = quoteWithItems.items,
                    isEditing = true,
                    savedQuoteId = quoteWithItems.quote.id
                )
            }
        }
    }

    fun loadQuoteDetail(quoteId: Long) {
        viewModelScope.launch {
            repository.getQuoteWithItems(quoteId).collect {
                _selectedQuoteDetail.value = it
            }
        }
    }

    fun setClient(contact: ClientContact) {
        _builderState.update { current ->
            current.copy(
                quote = current.quote.copy(
                    clientName = contact.name,
                    clientPhone = contact.phone,
                    clientEmail = contact.email,
                    clientCompany = contact.company,
                    clientAddress = contact.address,
                    clientTaxId = contact.taxId
                )
            )
        }
    }

    fun setClientInfo(
        name: String,
        phone: String = "",
        email: String = "",
        company: String = "",
        address: String = "",
        taxId: String = ""
    ) {
        _builderState.update { current ->
            current.copy(
                quote = current.quote.copy(
                    clientName = name,
                    clientPhone = phone,
                    clientEmail = email,
                    clientCompany = company,
                    clientAddress = address,
                    clientTaxId = taxId
                )
            )
        }
    }

    fun addItemFromCatalog(catalogItem: ProductService, quantity: Double = 1.0) {
        _builderState.update { current ->
            val newItem = QuoteItem(
                name = catalogItem.name,
                description = catalogItem.description,
                unitPrice = catalogItem.unitPrice,
                quantity = quantity,
                unit = catalogItem.unit,
                isService = catalogItem.isService
            )
            current.copy(items = current.items + newItem)
        }
    }

    fun addCustomItem(
        name: String,
        description: String,
        unitPrice: Double,
        quantity: Double,
        unit: String,
        isService: Boolean
    ) {
        _builderState.update { current ->
            val newItem = QuoteItem(
                name = name,
                description = description,
                unitPrice = unitPrice,
                quantity = quantity,
                unit = unit,
                isService = isService
            )
            current.copy(items = current.items + newItem)
        }
    }

    fun updateItem(index: Int, updatedItem: QuoteItem) {
        _builderState.update { current ->
            val mutableList = current.items.toMutableList()
            if (index in mutableList.indices) {
                mutableList[index] = updatedItem
            }
            current.copy(items = mutableList)
        }
    }

    fun updateItemQuantity(index: Int, newQuantity: Double) {
        if (newQuantity <= 0) {
            removeItem(index)
            return
        }
        _builderState.update { current ->
            val mutableList = current.items.toMutableList()
            if (index in mutableList.indices) {
                mutableList[index] = mutableList[index].copy(quantity = newQuantity)
            }
            current.copy(items = mutableList)
        }
    }

    fun removeItem(index: Int) {
        _builderState.update { current ->
            val mutableList = current.items.toMutableList()
            if (index in mutableList.indices) {
                mutableList.removeAt(index)
            }
            current.copy(items = mutableList)
        }
    }

    fun updateDiscountsAndTaxes(discountPercent: Double, taxPercent: Double) {
        _builderState.update { current ->
            current.copy(
                quote = current.quote.copy(
                    discountPercent = discountPercent,
                    taxPercent = taxPercent
                )
            )
        }
    }

    fun updateValidityAndNotes(validDays: Int, notes: String, terms: String) {
        val validUntil = System.currentTimeMillis() + (validDays.toLong() * 24 * 60 * 60 * 1000)
        _builderState.update { current ->
            current.copy(
                quote = current.quote.copy(
                    validUntilMillis = validUntil,
                    notes = notes,
                    terms = terms
                )
            )
        }
    }

    fun saveActiveQuote(onSuccess: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val state = _builderState.value
            val savedId = repository.saveQuote(state.quote, state.items)
            _builderState.update {
                it.copy(
                    isSaved = true,
                    savedQuoteId = savedId,
                    quote = it.quote.copy(id = savedId)
                )
            }
            onSuccess?.invoke(savedId)
        }
    }

    fun updateQuoteStatus(quoteId: Long, status: QuoteStatus) {
        viewModelScope.launch {
            repository.updateQuoteStatus(quoteId, status)
        }
    }

    fun duplicateQuote(quoteWithItems: QuoteWithItems, onDuplicated: (Long) -> Unit) {
        viewModelScope.launch {
            val newId = repository.duplicateQuote(quoteWithItems)
            onDuplicated(newId)
        }
    }

    fun deleteQuote(quote: Quote) {
        viewModelScope.launch {
            repository.deleteQuote(quote)
        }
    }

    // --- CATALOG OPERATIONS ---
    fun saveCatalogItem(item: ProductService) {
        viewModelScope.launch {
            repository.saveCatalogItem(item)
        }
    }

    fun deleteCatalogItem(item: ProductService) {
        viewModelScope.launch {
            repository.deleteCatalogItem(item)
        }
    }

    // --- CONTACT OPERATIONS ---
    fun saveContact(contact: ClientContact) {
        viewModelScope.launch {
            repository.saveContact(contact)
        }
    }

    fun deleteContact(contact: ClientContact) {
        viewModelScope.launch {
            repository.deleteContact(contact)
        }
    }

    // --- BUSINESS PROFILE ---
    fun saveBusinessProfile(profile: BusinessProfile) {
        viewModelScope.launch {
            repository.updateBusinessProfile(profile)
        }
    }
}
