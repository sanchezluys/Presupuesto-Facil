package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuoteStatus
import com.example.ui.MainViewModel
import com.example.ui.theme.StatusAccepted
import com.example.ui.theme.StatusDraft
import com.example.ui.theme.StatusRejected
import com.example.ui.theme.StatusSent
import com.example.util.ContactPickerHelper
import com.example.util.Formatters
import com.example.util.QuoteImageGenerator
import com.example.util.QuotePdfGenerator
import com.example.util.QuoteTextFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteDetailScreen(
    quoteId: Long,
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onEditQuote: (Long) -> Unit
) {
    val context = LocalContext.current
    val quoteWithItems by viewModel.selectedQuoteDetail.collectAsState()
    val profile by viewModel.businessProfile.collectAsState()

    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(quoteId) {
        viewModel.loadQuoteDetail(quoteId)
    }

    val qwi = quoteWithItems

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = qwi?.let { "#${it.quote.quoteNumber}" } ?: "Detalle",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { qwi?.let { onEditQuote(it.quote.id) } }) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Editar")
                    }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "Más")
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Duplicar Presupuesto") },
                            leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                            onClick = {
                                showMenu = false
                                qwi?.let {
                                    viewModel.duplicateQuote(it) { newId ->
                                        Toast.makeText(context, "Presupuesto duplicado", Toast.LENGTH_SHORT).show()
                                        onEditQuote(newId)
                                    }
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar Presupuesto", color = Color(0xFFE11D48)) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFE11D48)) },
                            onClick = {
                                showMenu = false
                                showDeleteConfirm = true
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF7F9FC)
    ) { innerPadding ->
        if (qwi == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Cargando presupuesto...")
            }
        } else {
            val q = qwi.quote
            val currency = q.currencySymbol.ifBlank { profile?.currencySymbol ?: "$" }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 32.dp)
            ) {
                // Status Selector Card
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 12.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        shadowElevation = 1.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "ESTADO DEL PRESUPUESTO",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp,
                                    color = Color(0xFF64748B)
                                )
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                QuoteStatus.values().forEach { status ->
                                    val isSelected = q.status == status
                                    val statusColor = when (status) {
                                        QuoteStatus.DRAFT -> StatusDraft
                                        QuoteStatus.SENT -> StatusSent
                                        QuoteStatus.ACCEPTED -> StatusAccepted
                                        QuoteStatus.REJECTED -> StatusRejected
                                    }
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = { viewModel.updateQuoteStatus(q.id, status) },
                                        label = {
                                            Text(
                                                text = status.displayName,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                fontSize = 11.sp
                                            )
                                        },
                                        shape = RoundedCornerShape(14.dp),
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = statusColor.copy(alpha = 0.2f),
                                            selectedLabelColor = statusColor
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // 3 Export Buttons Grid
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // PDF
                        Button(
                            onClick = {
                                val uri = QuotePdfGenerator.generatePdf(context, qwi, profile)
                                if (uri != null) {
                                    ContactPickerHelper.shareFile(context, uri, "application/pdf", "Enviar PDF Presupuesto")
                                } else {
                                    Toast.makeText(context, "Error al generar PDF", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 6.dp)
                            ) {
                                Text("📄", fontSize = 20.sp)
                                Text(
                                    "PDF",
                                    color = Color(0xFF0F172A),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Imagen
                        Button(
                            onClick = {
                                val uri = QuoteImageGenerator.generateImage(context, qwi, profile)
                                if (uri != null) {
                                    ContactPickerHelper.shareFile(context, uri, "image/png", "Enviar Imagen Presupuesto")
                                } else {
                                    Toast.makeText(context, "Error al generar Imagen", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 6.dp)
                            ) {
                                Text("🖼️", fontSize = 20.sp)
                                Text(
                                    "IMAGEN",
                                    color = Color(0xFF0F172A),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // WhatsApp / Texto
                        Button(
                            onClick = {
                                val text = QuoteTextFormatter.generateFormattedText(qwi, profile)
                                if (q.clientPhone.isNotBlank()) {
                                    ContactPickerHelper.openWhatsApp(context, q.clientPhone, text)
                                } else {
                                    ContactPickerHelper.shareText(context, text, "Enviar Presupuesto")
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(vertical = 6.dp)
                            ) {
                                Text("💬", fontSize = 20.sp)
                                Text(
                                    "TEXTO",
                                    color = Color(0xFF0F172A),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // Full Artistic Quote Preview Card
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 10.dp),
                        shape = RoundedCornerShape(32.dp),
                        color = Color.White,
                        shadowElevation = 3.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        ) {
                            // Company Name & Presupuesto Title
                            Text(
                                text = (profile?.companyName ?: "MI EMPRESA").uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Presupuesto",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontFamily = FontFamily.Serif,
                                        fontStyle = FontStyle.Italic,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                )
                                Text(
                                    text = "#${q.quoteNumber}",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF64748B)
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(18.dp))

                            // Client Block
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFFF8FAFC)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "CLIENTE",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 1.sp,
                                            color = Color(0xFF64748B),
                                            fontSize = 10.sp
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = q.clientName.ifBlank { "Cliente General" },
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                    )
                                    if (q.clientCompany.isNotBlank()) {
                                        Text(
                                            text = q.clientCompany,
                                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                                        )
                                    }
                                    if (q.clientPhone.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Phone,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = q.clientPhone,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    color = MaterialTheme.colorScheme.primary,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Emisión: ${Formatters.formatDate(q.createdAtMillis)} • Vence: ${Formatters.formatDate(q.validUntilMillis)}",
                                        style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFF94A3B8))
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Conceptos list
                            Text(
                                text = "CONCEPTOS",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp,
                                    color = Color(0xFF64748B)
                                )
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            qwi.items.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF0F172A)
                                            )
                                        )
                                        Text(
                                            text = "${Formatters.formatNumber(item.quantity)} ${item.unit} x ${Formatters.formatMoney(item.unitPrice, currency)}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color(0xFF64748B)
                                            )
                                        )
                                    }
                                    Text(
                                        text = Formatters.formatMoney(item.total, currency),
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color(0xFFE2E8F0))
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            // Breakdown
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Subtotal", style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF64748B)))
                                Text(
                                    Formatters.formatMoney(qwi.subtotal, currency),
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }

                            if (q.discountPercent > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Descuento (${Formatters.formatNumber(q.discountPercent)}%)",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFE11D48))
                                    )
                                    Text(
                                        "-${Formatters.formatMoney(qwi.discountAmount, currency)}",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFFE11D48)
                                        )
                                    )
                                }
                            }

                            if (q.taxPercent > 0) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Impuesto / IVA (${Formatters.formatNumber(q.taxPercent)}%)",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                                    )
                                    Text(
                                        "+${Formatters.formatMoney(qwi.taxAmount, currency)}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Total Card Highlight
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFF005FB0)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 20.dp, vertical = 16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "TOTAL",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                    Text(
                                        text = Formatters.formatMoney(qwi.total, currency),
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                }
                            }

                            if (q.notes.isNotBlank() || q.terms.isNotBlank()) {
                                Spacer(modifier = Modifier.height(18.dp))
                                if (q.notes.isNotBlank()) {
                                    Text(
                                        text = "NOTAS:",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF64748B)
                                        )
                                    )
                                    Text(
                                        text = q.notes,
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF475569))
                                    )
                                }
                                if (q.terms.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "CONDICIONES DE PAGO:",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF64748B)
                                        )
                                    )
                                    Text(
                                        text = q.terms,
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF475569))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("¿Eliminar Presupuesto?") },
            text = { Text("Esta acción no se puede deshacer. Se eliminará el presupuesto permanentemente.") },
            confirmButton = {
                Button(
                    onClick = {
                        qwi?.let { viewModel.deleteQuote(it.quote) }
                        showDeleteConfirm = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
