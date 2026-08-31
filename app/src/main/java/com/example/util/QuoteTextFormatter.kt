package com.example.util

import com.example.data.model.BusinessProfile
import com.example.data.model.QuoteWithItems

object QuoteTextFormatter {

    fun generateFormattedText(quoteWithItems: QuoteWithItems, profile: BusinessProfile?): String {
        val q = quoteWithItems.quote
        val currency = q.currencySymbol.ifBlank { profile?.currencySymbol ?: "$" }
        val sb = StringBuilder()

        // Header
        val compName = profile?.companyName ?: "Presupuesto"
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━━")
        sb.appendLine("📋 *PRESUPUESTO #${q.quoteNumber}*")
        sb.appendLine("*${compName.uppercase()}*")
        if (!profile?.phone.isNullOrBlank()) sb.appendLine("📞 Tel: ${profile?.phone}")
        if (!profile?.email.isNullOrBlank()) sb.appendLine("✉️ Email: ${profile?.email}")
        if (!profile?.taxId.isNullOrBlank()) sb.appendLine("🆔 Identificación: ${profile?.taxId}")
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━━")

        // Client info
        sb.appendLine()
        sb.appendLine("👤 *CLIENTE:* ${q.clientName.ifBlank { "Cliente General" }}")
        if (q.clientCompany.isNotBlank()) sb.appendLine("🏢 Empresa: ${q.clientCompany}")
        if (q.clientPhone.isNotBlank()) sb.appendLine("📱 Tel: ${q.clientPhone}")
        sb.appendLine("📅 Fecha: ${Formatters.formatDate(q.createdAtMillis)}")
        sb.appendLine("⏳ Válido hasta: ${Formatters.formatDate(q.validUntilMillis)}")
        sb.appendLine()

        // Items list
        sb.appendLine("🛒 *DETALLE DE SERVICIOS / PRODUCTOS:*")
        sb.appendLine("─────────────────────")
        quoteWithItems.items.forEachIndexed { index, item ->
            val totalItem = Formatters.formatMoney(item.total, currency)
            val qtyStr = Formatters.formatNumber(item.quantity)
            sb.appendLine("${index + 1}. *${item.name}*")
            if (item.description.isNotBlank()) {
                sb.appendLine("   _${item.description}_")
            }
            sb.appendLine("   $qtyStr ${item.unit} x ${Formatters.formatMoney(item.unitPrice, currency)} = *$totalItem*")
            sb.appendLine()
        }
        sb.appendLine("─────────────────────")

        // Totals
        sb.appendLine("🔹 *Subtotal:* ${Formatters.formatMoney(quoteWithItems.subtotal, currency)}")
        if (q.discountPercent > 0) {
            sb.appendLine("🏷️ *Descuento (${Formatters.formatNumber(q.discountPercent)}%):* -${Formatters.formatMoney(quoteWithItems.discountAmount, currency)}")
        }
        if (q.taxPercent > 0) {
            sb.appendLine("🏛️ *IVA / Impuesto (${Formatters.formatNumber(q.taxPercent)}%):* +${Formatters.formatMoney(quoteWithItems.taxAmount, currency)}")
        }
        sb.appendLine("💰 *TOTAL ESTIMADO:* *${Formatters.formatMoney(quoteWithItems.total, currency)}*")
        sb.appendLine("━━━━━━━━━━━━━━━━━━━━━")

        // Notes & Terms
        if (q.notes.isNotBlank()) {
            sb.appendLine()
            sb.appendLine("📝 *Notas:*")
            sb.appendLine(q.notes)
        }
        if (q.terms.isNotBlank()) {
            sb.appendLine()
            sb.appendLine("📌 *Condiciones de pago:*")
            sb.appendLine(q.terms)
        }
        if (!profile?.bankDetails.isNullOrBlank()) {
            sb.appendLine()
            sb.appendLine("🏦 *Datos Bancarios:*")
            sb.appendLine(profile?.bankDetails)
        }

        sb.appendLine()
        sb.appendLine("¡Quedamos a su entera disposición! ✨")
        return sb.toString()
    }
}
