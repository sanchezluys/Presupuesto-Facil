package com.example

import com.example.data.model.BusinessProfile
import com.example.data.model.Quote
import com.example.data.model.QuoteItem
import com.example.data.model.QuoteWithItems
import com.example.util.Formatters
import com.example.util.QuoteTextFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun quoteCalculations_areCorrect() {
        val quote = Quote(
            quoteNumber = "PRE-001",
            discountPercent = 10.0,
            taxPercent = 16.0
        )
        val items = listOf(
            QuoteItem(name = "Landing Page", unitPrice = 100.0, quantity = 2.0), // 200.0
            QuoteItem(name = "Hosting", unitPrice = 50.0, quantity = 1.0)       // 50.0
        )
        val quoteWithItems = QuoteWithItems(quote = quote, items = items)

        // Subtotal = 250.0
        assertEquals(250.0, quoteWithItems.subtotal, 0.001)

        // Discount (10%) = 25.0
        assertEquals(25.0, quoteWithItems.discountAmount, 0.001)

        // Subtotal after discount = 225.0
        assertEquals(225.0, quoteWithItems.subtotalAfterDiscount, 0.001)

        // Tax (16% of 225.0) = 36.0
        assertEquals(36.0, quoteWithItems.taxAmount, 0.001)

        // Total = 225.0 + 36.0 = 261.0
        assertEquals(261.0, quoteWithItems.total, 0.001)
    }

    @Test
    fun quoteTextFormatter_generatesFormattedContent() {
        val profile = BusinessProfile(
            companyName = "Tech Solutions",
            phone = "+123456789",
            currencySymbol = "$"
        )
        val quote = Quote(
            quoteNumber = "PRE-100",
            clientName = "Juan Pérez"
        )
        val items = listOf(
            QuoteItem(name = "Servicio Web", unitPrice = 500.0, quantity = 1.0, unit = "servicio")
        )
        val quoteWithItems = QuoteWithItems(quote = quote, items = items)

        val text = QuoteTextFormatter.generateFormattedText(quoteWithItems, profile)

        assertTrue(text.contains("PRESUPUESTO #PRE-100"))
        assertTrue(text.contains("TECH SOLUTIONS"))
        assertTrue(text.contains("Juan Pérez"))
        assertTrue(text.contains("Servicio Web"))
    }
}
