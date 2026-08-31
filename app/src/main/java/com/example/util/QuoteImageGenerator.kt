package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.BusinessProfile
import com.example.data.model.QuoteWithItems
import java.io.File
import java.io.FileOutputStream

object QuoteImageGenerator {

    fun generateImage(
        context: Context,
        quoteWithItems: QuoteWithItems,
        profile: BusinessProfile?
    ): Uri? {
        val q = quoteWithItems.quote
        val currency = q.currencySymbol.ifBlank { profile?.currencySymbol ?: "$" }

        val width = 1080
        // Dynamically compute height based on items count
        val baseHeight = 1350
        val itemsExtraHeight = (quoteWithItems.items.size * 120).coerceAtLeast(0)
        val height = baseHeight + itemsExtraHeight

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Background
        paint.color = Color.parseColor("#F7F9FC")
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

        // Header Background Card
        paint.color = Color.WHITE
        val headerCard = RectF(0f, 0f, width.toFloat(), 340f)
        canvas.drawRoundRect(headerCard, 0f, 0f, paint)
        // Header bottom curve shadow
        paint.color = Color.parseColor("#E2E8F0")
        val headerBottomCurve = RectF(0f, 0f, width.toFloat(), 360f)
        paint.color = Color.WHITE
        canvas.drawRoundRect(headerBottomCurve, 60f, 60f, paint)

        // Company Tag
        paint.color = Color.parseColor("#005FB0")
        paint.textSize = 28f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.letterSpacing = 0.12f
        val compName = (profile?.companyName ?: "MI EMPRESA").uppercase()
        canvas.drawText(compName, 60f, 100f, paint)

        // Title
        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 64f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
        paint.letterSpacing = 0f
        canvas.drawText("Presupuesto", 60f, 180f, paint)

        // Initials Avatar Badge
        paint.color = Color.parseColor("#D6E4FF")
        val avatarRect = RectF(width - 180f, 60f, width - 60f, 180f)
        canvas.drawRoundRect(avatarRect, 40f, 40f, paint)

        paint.color = Color.parseColor("#005FB0")
        paint.textSize = 42f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        val initials = (profile?.companyName ?: "GS").split(" ")
            .take(2)
            .mapNotNull { it.firstOrNull()?.uppercaseChar() }
            .joinToString("")
            .ifBlank { "PE" }
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(initials, width - 120f, 135f, paint)
        paint.textAlign = Paint.Align.LEFT

        // Client Row inside Header
        paint.color = Color.parseColor("#E2E8F0")
        val clientAvatar = RectF(60f, 220f, 160f, 320f)
        canvas.drawRoundRect(clientAvatar, 50f, 50f, paint)

        paint.color = Color.parseColor("#64748B")
        paint.textSize = 34f
        val clientInitials = q.clientName.split(" ").take(2).mapNotNull { it.firstOrNull()?.uppercaseChar() }.joinToString("").ifBlank { "CL" }
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(clientInitials, 110f, 282f, paint)
        paint.textAlign = Paint.Align.LEFT

        paint.color = Color.parseColor("#64748B")
        paint.textSize = 24f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText("CLIENTE", 180f, 255f, paint)

        paint.color = Color.parseColor("#0F172A")
        paint.textSize = 36f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        val clientNameDisplay = q.clientName.ifBlank { "Cliente General" }
        canvas.drawText(clientNameDisplay, 180f, 298f, paint)

        // Quote No badge right
        paint.color = Color.parseColor("#EEF2F6")
        val numberBadge = RectF(width - 280f, 235f, width - 60f, 305f)
        canvas.drawRoundRect(numberBadge, 25f, 25f, paint)
        paint.color = Color.parseColor("#005FB0")
        paint.textSize = 28f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("#${q.quoteNumber}", width - 170f, 280f, paint)
        paint.textAlign = Paint.Align.LEFT

        // Section Title: SERVICIOS Y PRODUCTOS
        var currentY = 430f
        paint.color = Color.parseColor("#64748B")
        paint.textSize = 28f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        paint.letterSpacing = 0.1f
        canvas.drawText("SERVICIOS Y PRODUCTOS", 60f, currentY, paint)
        paint.letterSpacing = 0f

        currentY += 40f

        // Items cards
        quoteWithItems.items.forEach { item ->
            val cardRect = RectF(60f, currentY, width - 60f, currentY + 130f)
            paint.color = Color.WHITE
            canvas.drawRoundRect(cardRect, 32f, 32f, paint)

            paint.color = Color.parseColor("#0F172A")
            paint.textSize = 34f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            canvas.drawText(item.name, 96f, currentY + 52f, paint)

            paint.color = Color.parseColor("#64748B")
            paint.textSize = 24f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            val desc = if (item.description.isNotBlank()) item.description else "${Formatters.formatNumber(item.quantity)} ${item.unit} x ${Formatters.formatMoney(item.unitPrice, currency)}"
            val truncatedDesc = if (desc.length > 55) desc.take(52) + "..." else desc
            canvas.drawText(truncatedDesc, 96f, currentY + 95f, paint)

            // Price Right
            paint.color = Color.parseColor("#0F172A")
            paint.textSize = 36f
            paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(Formatters.formatMoney(item.total, currency), width - 96f, currentY + 74f, paint)
            paint.textAlign = Paint.Align.LEFT

            currentY += 150f
        }

        // Totals Card Area
        currentY += 20f
        val totalsCard = RectF(60f, currentY, width - 60f, currentY + 230f)
        paint.color = Color.WHITE
        canvas.drawRoundRect(totalsCard, 36f, 36f, paint)

        // Subtotal
        paint.color = Color.parseColor("#64748B")
        paint.textSize = 28f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        canvas.drawText("Subtotal", 96f, currentY + 60f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(Formatters.formatMoney(quoteWithItems.subtotal, currency), width - 96f, currentY + 60f, paint)
        paint.textAlign = Paint.Align.LEFT

        var breakdownY = currentY + 105f
        if (q.discountPercent > 0) {
            paint.color = Color.parseColor("#E11D48")
            canvas.drawText("Descuento (${Formatters.formatNumber(q.discountPercent)}%)", 96f, breakdownY, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("-${Formatters.formatMoney(quoteWithItems.discountAmount, currency)}", width - 96f, breakdownY, paint)
            paint.textAlign = Paint.Align.LEFT
            breakdownY += 45f
        }

        // Total Estimado
        paint.color = Color.parseColor("#005FB0")
        val finalBar = RectF(80f, currentY + 125f, width - 80f, currentY + 215f)
        canvas.drawRoundRect(finalBar, 24f, 24f, paint)

        paint.color = Color.WHITE
        paint.textSize = 34f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
        canvas.drawText("TOTAL ESTIMADO", 110f, currentY + 182f, paint)

        paint.textSize = 48f
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(Formatters.formatMoney(quoteWithItems.total, currency), width - 110f, currentY + 185f, paint)
        paint.textAlign = Paint.Align.LEFT

        currentY += 260f

        // Validity and terms footer
        paint.color = Color.parseColor("#64748B")
        paint.textSize = 24f
        paint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        paint.textAlign = Paint.Align.CENTER
        val footerDate = "Emisión: ${Formatters.formatDate(q.createdAtMillis)} • Válido hasta: ${Formatters.formatDate(q.validUntilMillis)}"
        canvas.drawText(footerDate, width / 2f, currentY + 30f, paint)

        if (!profile?.phone.isNullOrBlank() || !profile?.email.isNullOrBlank()) {
            val contactInfo = listOfNotNull(profile?.phone, profile?.email).joinToString(" | ")
            canvas.drawText(contactInfo, width / 2f, currentY + 70f, paint)
        }

        // Save Bitmap to cache
        val outputDir = File(context.cacheDir, "budgets").apply { mkdirs() }
        val outputFile = File(outputDir, "Presupuesto_${q.quoteNumber}.png")

        return try {
            FileOutputStream(outputFile).use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                outputFile
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
