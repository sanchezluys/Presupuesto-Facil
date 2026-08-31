package com.example.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.BusinessProfile
import com.example.data.model.QuoteWithItems
import java.io.File
import java.io.FileOutputStream

object QuotePdfGenerator {

    fun generatePdf(
        context: Context,
        quoteWithItems: QuoteWithItems,
        profile: BusinessProfile?
    ): Uri? {
        val doc = PdfDocument()
        val pageWidth = 595 // Standard A4 width in points (72 dpi)
        val pageHeight = 842 // Standard A4 height in points
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val q = quoteWithItems.quote
        val currency = q.currencySymbol.ifBlank { profile?.currencySymbol ?: "$" }

        // Background
        paint.color = Color.WHITE
        canvas.drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), paint)

        // Top Header Banner
        paint.color = Color.parseColor("#005FB0")
        val headerRect = RectF(0f, 0f, pageWidth.toFloat(), 120f)
        canvas.drawRect(headerRect, paint)

        // Company Name / Tagline
        paint.color = Color.parseColor("#D6E4FF")
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.letterSpacing = 0.15f
        val companyTag = (profile?.companyName ?: "MI EMPRESA").uppercase()
        canvas.drawText(companyTag, 36f, 40f, paint)

        paint.color = Color.WHITE
        paint.textSize = 24f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
        paint.letterSpacing = 0f
        canvas.drawText("Presupuesto #${q.quoteNumber}", 36f, 75f, paint)

        paint.color = Color.parseColor("#EAF2FF")
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        val statusText = "Estado: ${q.status.displayName} | Fecha: ${Formatters.formatDate(q.createdAtMillis)}"
        canvas.drawText(statusText, 36f, 96f, paint)

        // Company Contact on top right
        paint.textAlign = Paint.Align.RIGHT
        paint.textSize = 9f
        paint.color = Color.WHITE
        var rightY = 40f
        if (!profile?.ownerName.isNullOrBlank()) {
            canvas.drawText(profile!!.ownerName, pageWidth - 36f, rightY, paint)
            rightY += 14f
        }
        if (!profile?.phone.isNullOrBlank()) {
            canvas.drawText("Tel: ${profile!!.phone}", pageWidth - 36f, rightY, paint)
            rightY += 14f
        }
        if (!profile?.email.isNullOrBlank()) {
            canvas.drawText(profile!!.email, pageWidth - 36f, rightY, paint)
            rightY += 14f
        }
        if (!profile?.taxId.isNullOrBlank()) {
            canvas.drawText("Ident: ${profile!!.taxId}", pageWidth - 36f, rightY, paint)
        }
        paint.textAlign = Paint.Align.LEFT

        // Client & Validity Info Card
        var currentY = 145f
        paint.color = Color.parseColor("#F1F5F9")
        val clientCard = RectF(36f, currentY, pageWidth - 36f, currentY + 75f)
        canvas.drawRoundRect(clientCard, 12f, 12f, paint)

        paint.color = Color.parseColor("#64748B")
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText("DIRIGIDO A:", 50f, currentY + 22f, paint)
        canvas.drawText("DETALLES:", pageWidth / 2f + 20f, currentY + 22f, paint)

        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        val clientDisplayName = q.clientName.ifBlank { "Cliente General" }
        canvas.drawText(clientDisplayName, 50f, currentY + 40f, paint)

        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        var clientDetailY = currentY + 56f
        if (q.clientCompany.isNotBlank()) {
            canvas.drawText("Empresa: ${q.clientCompany}", 50f, clientDetailY, paint)
        } else if (q.clientPhone.isNotBlank()) {
            canvas.drawText("Teléfono: ${q.clientPhone}", 50f, clientDetailY, paint)
        }

        // Validity Right column in card
        canvas.drawText("Fecha de emisión: ${Formatters.formatDate(q.createdAtMillis)}", pageWidth / 2f + 20f, currentY + 40f, paint)
        canvas.drawText("Válido hasta: ${Formatters.formatDate(q.validUntilMillis)}", pageWidth / 2f + 20f, currentY + 56f, paint)

        currentY += 100f

        // Table Header
        paint.color = Color.parseColor("#005FB0")
        val thRect = RectF(36f, currentY, pageWidth - 36f, currentY + 26f)
        canvas.drawRoundRect(thRect, 6f, 6f, paint)

        paint.color = Color.WHITE
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText("CONCEPTO / DESCRIPCIÓN", 48f, currentY + 17f, paint)
        canvas.drawText("CANT.", 340f, currentY + 17f, paint)
        canvas.drawText("P. UNITARIO", 400f, currentY + 17f, paint)
        canvas.drawText("TOTAL", pageWidth - 80f, currentY + 17f, paint)

        currentY += 34f

        // Items
        quoteWithItems.items.forEachIndexed { index, item ->
            if (currentY > pageHeight - 160f) return@forEachIndexed

            // Row alternate background
            if (index % 2 == 0) {
                paint.color = Color.parseColor("#F8FAFC")
                val rowRect = RectF(36f, currentY - 6f, pageWidth - 36f, currentY + 28f)
                canvas.drawRect(rowRect, paint)
            }

            paint.color = Color.parseColor("#0F172A")
            paint.textSize = 10.5f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            canvas.drawText(item.name, 48f, currentY + 8f, paint)

            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            paint.textSize = 9f
            paint.color = Color.parseColor("#64748B")
            if (item.description.isNotBlank()) {
                val shortDesc = if (item.description.length > 50) item.description.take(47) + "..." else item.description
                canvas.drawText(shortDesc, 48f, currentY + 22f, paint)
            }

            paint.textSize = 10f
            paint.color = Color.parseColor("#0F172A")
            canvas.drawText("${Formatters.formatNumber(item.quantity)} ${item.unit}", 340f, currentY + 12f, paint)
            canvas.drawText(Formatters.formatMoney(item.unitPrice, currency), 400f, currentY + 12f, paint)

            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            canvas.drawText(Formatters.formatMoney(item.total, currency), pageWidth - 90f, currentY + 12f, paint)

            currentY += 34f
        }

        // Divider
        paint.color = Color.parseColor("#E2E8F0")
        paint.strokeWidth = 1f
        canvas.drawLine(36f, currentY, pageWidth - 36f, currentY, paint)
        currentY += 16f

        // Totals Box (Right aligned)
        val totalsX = pageWidth - 220f
        paint.color = Color.parseColor("#64748B")
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        canvas.drawText("Subtotal:", totalsX, currentY, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(Formatters.formatMoney(quoteWithItems.subtotal, currency), pageWidth - 48f, currentY, paint)
        paint.textAlign = Paint.Align.LEFT
        currentY += 16f

        if (q.discountPercent > 0) {
            paint.color = Color.parseColor("#E11D48")
            canvas.drawText("Descuento (${Formatters.formatNumber(q.discountPercent)}%):", totalsX, currentY, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("-${Formatters.formatMoney(quoteWithItems.discountAmount, currency)}", pageWidth - 48f, currentY, paint)
            paint.textAlign = Paint.Align.LEFT
            currentY += 16f
        }

        if (q.taxPercent > 0) {
            paint.color = Color.parseColor("#64748B")
            canvas.drawText("Impuesto / IVA (${Formatters.formatNumber(q.taxPercent)}%):", totalsX, currentY, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("+${Formatters.formatMoney(quoteWithItems.taxAmount, currency)}", pageWidth - 48f, currentY, paint)
            paint.textAlign = Paint.Align.LEFT
            currentY += 16f
        }

        // Total Final highlighted
        currentY += 4f
        paint.color = Color.parseColor("#005FB0")
        val totalRect = RectF(totalsX - 10f, currentY - 14f, pageWidth - 36f, currentY + 22f)
        canvas.drawRoundRect(totalRect, 8f, 8f, paint)

        paint.color = Color.WHITE
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText("TOTAL ESTIMADO:", totalsX, currentY + 6f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(Formatters.formatMoney(quoteWithItems.total, currency), pageWidth - 46f, currentY + 6f, paint)
        paint.textAlign = Paint.Align.LEFT

        // Notes & Terms Footer
        val footerY = pageHeight - 90f
        paint.color = Color.parseColor("#F8FAFC")
        val footerRect = RectF(36f, footerY - 12f, pageWidth - 36f, pageHeight - 20f)
        canvas.drawRoundRect(footerRect, 8f, 8f, paint)

        paint.color = Color.parseColor("#005FB0")
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText("CONDICIONES & TÉRMINOS:", 48f, footerY + 2f, paint)

        paint.color = Color.parseColor("#475569")
        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        val notesStr = if (q.notes.isNotBlank()) q.notes else profile?.defaultNotes ?: "Presupuesto válido por 15 días."
        canvas.drawText(notesStr, 48f, footerY + 16f, paint)

        val termsStr = if (q.terms.isNotBlank()) q.terms else profile?.paymentTerms ?: ""
        if (termsStr.isNotBlank()) {
            canvas.drawText(termsStr, 48f, footerY + 28f, paint)
        }

        doc.finishPage(page)

        // Save to cache file
        val outputDir = File(context.cacheDir, "budgets").apply { mkdirs() }
        val outputFile = File(outputDir, "Presupuesto_${q.quoteNumber}.pdf")

        return try {
            FileOutputStream(outputFile).use { out ->
                doc.writeTo(out)
            }
            doc.close()
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                outputFile
            )
        } catch (e: Exception) {
            e.printStackTrace()
            doc.close()
            null
        }
    }
}
