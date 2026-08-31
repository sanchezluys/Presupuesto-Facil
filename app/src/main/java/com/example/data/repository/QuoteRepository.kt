package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.BusinessProfile
import com.example.data.model.ClientContact
import com.example.data.model.ProductService
import com.example.data.model.Quote
import com.example.data.model.QuoteItem
import com.example.data.model.QuoteStatus
import com.example.data.model.QuoteWithItems
import kotlinx.coroutines.flow.Flow

class QuoteRepository(private val database: AppDatabase) {

    // Business Profile
    val businessProfile: Flow<BusinessProfile?> = database.businessProfileDao().getProfile()
    suspend fun getBusinessProfileSync(): BusinessProfile? = database.businessProfileDao().getProfileSync()
    suspend fun updateBusinessProfile(profile: BusinessProfile) = database.businessProfileDao().insertOrUpdate(profile)

    // Catalog Products / Services
    val allCatalogItems: Flow<List<ProductService>> = database.productServiceDao().getAll()
    suspend fun saveCatalogItem(item: ProductService) {
        if (item.id == 0L) {
            database.productServiceDao().insert(item)
        } else {
            database.productServiceDao().update(item)
        }
    }
    suspend fun deleteCatalogItem(item: ProductService) = database.productServiceDao().delete(item)
    fun searchCatalog(query: String): Flow<List<ProductService>> = database.productServiceDao().search(query)

    // Contacts
    val allContacts: Flow<List<ClientContact>> = database.clientContactDao().getAll()
    suspend fun saveContact(contact: ClientContact): Long {
        return if (contact.id == 0L) {
            database.clientContactDao().insert(contact)
        } else {
            database.clientContactDao().update(contact)
            contact.id
        }
    }
    suspend fun deleteContact(contact: ClientContact) = database.clientContactDao().delete(contact)
    fun searchContacts(query: String): Flow<List<ClientContact>> = database.clientContactDao().search(query)

    // Quotes
    val allQuotesWithItems: Flow<List<QuoteWithItems>> = database.quoteDao().getAllQuotesWithItems()
    fun getQuoteWithItems(id: Long): Flow<QuoteWithItems?> = database.quoteDao().getQuoteWithItemsById(id)
    suspend fun getQuoteWithItemsSync(id: Long): QuoteWithItems? = database.quoteDao().getQuoteWithItemsByIdSync(id)

    suspend fun generateNextQuoteNumber(): String {
        val count = database.quoteDao().getQuotesCount()
        return "PRE-%03d".format(count + 1)
    }

    suspend fun saveQuote(quote: Quote, items: List<QuoteItem>): Long {
        return database.quoteDao().saveFullQuote(quote, items)
    }

    suspend fun updateQuoteStatus(quoteId: Long, status: QuoteStatus) {
        database.quoteDao().updateQuoteStatus(quoteId, status)
    }

    suspend fun deleteQuote(quote: Quote) {
        database.quoteDao().deleteQuote(quote)
    }

    suspend fun duplicateQuote(quoteWithItems: QuoteWithItems): Long {
        val nextNumber = generateNextQuoteNumber()
        val newQuote = quoteWithItems.quote.copy(
            id = 0,
            quoteNumber = nextNumber,
            createdAtMillis = System.currentTimeMillis(),
            validUntilMillis = System.currentTimeMillis() + (15L * 24 * 60 * 60 * 1000),
            status = QuoteStatus.DRAFT
        )
        val newItems = quoteWithItems.items.map { it.copy(id = 0, quoteId = 0) }
        return saveQuote(newQuote, newItems)
    }
}
