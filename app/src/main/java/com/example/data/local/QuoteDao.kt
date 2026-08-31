package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.Quote
import com.example.data.model.QuoteItem
import com.example.data.model.QuoteStatus
import com.example.data.model.QuoteWithItems
import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteDao {
    @Transaction
    @Query("SELECT * FROM quotes ORDER BY createdAtMillis DESC")
    fun getAllQuotesWithItems(): Flow<List<QuoteWithItems>>

    @Transaction
    @Query("SELECT * FROM quotes WHERE id = :id LIMIT 1")
    fun getQuoteWithItemsById(id: Long): Flow<QuoteWithItems?>

    @Transaction
    @Query("SELECT * FROM quotes WHERE id = :id LIMIT 1")
    suspend fun getQuoteWithItemsByIdSync(id: Long): QuoteWithItems?

    @Query("SELECT COUNT(*) FROM quotes")
    suspend fun getQuotesCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuote(quote: Quote): Long

    @Update
    suspend fun updateQuote(quote: Quote)

    @Query("UPDATE quotes SET status = :status WHERE id = :quoteId")
    suspend fun updateQuoteStatus(quoteId: Long, status: QuoteStatus)

    @Delete
    suspend fun deleteQuote(quote: Quote)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuoteItems(items: List<QuoteItem>)

    @Query("DELETE FROM quote_items WHERE quoteId = :quoteId")
    suspend fun deleteQuoteItems(quoteId: Long)

    @Transaction
    suspend fun saveFullQuote(quote: Quote, items: List<QuoteItem>): Long {
        val quoteId = if (quote.id == 0L) {
            insertQuote(quote)
        } else {
            updateQuote(quote)
            deleteQuoteItems(quote.id)
            quote.id
        }
        val itemsWithId = items.map { it.copy(id = 0, quoteId = quoteId) }
        insertQuoteItems(itemsWithId)
        return quoteId
    }
}
