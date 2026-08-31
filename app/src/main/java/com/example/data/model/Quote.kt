package com.example.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

enum class QuoteStatus(val displayName: String) {
    DRAFT("Borrador"),
    SENT("Enviado"),
    ACCEPTED("Aceptado"),
    REJECTED("Rechazado")
}

@Entity(tableName = "quotes")
data class Quote(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val quoteNumber: String, // e.g. "PRE-001"
    val title: String = "Presupuesto",
    val clientName: String = "",
    val clientPhone: String = "",
    val clientEmail: String = "",
    val clientCompany: String = "",
    val clientAddress: String = "",
    val clientTaxId: String = "",
    val createdAtMillis: Long = System.currentTimeMillis(),
    val validUntilMillis: Long = System.currentTimeMillis() + (15L * 24 * 60 * 60 * 1000),
    val status: QuoteStatus = QuoteStatus.DRAFT,
    val discountPercent: Double = 0.0,
    val taxPercent: Double = 0.0,
    val notes: String = "",
    val terms: String = "",
    val currencySymbol: String = "$"
)

@Entity(
    tableName = "quote_items",
    foreignKeys = [
        ForeignKey(
            entity = Quote::class,
            parentColumns = ["id"],
            childColumns = ["quoteId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["quoteId"])]
)
data class QuoteItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val quoteId: Long = 0,
    val name: String,
    val description: String = "",
    val unitPrice: Double = 0.0,
    val quantity: Double = 1.0,
    val unit: String = "unid",
    val isService: Boolean = false
) {
    val total: Double
        get() = unitPrice * quantity
}

data class QuoteWithItems(
    @Embedded val quote: Quote,
    @Relation(
        parentColumn = "id",
        entityColumn = "quoteId"
    )
    val items: List<QuoteItem>
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
}
