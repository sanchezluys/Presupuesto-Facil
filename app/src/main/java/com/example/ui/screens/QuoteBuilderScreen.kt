package com.example.ui.screens

import android.app.Activity
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ProductService
import com.example.data.model.QuoteItem
import com.example.ui.MainViewModel
import com.example.ui.components.ArtisticHeader
import com.example.ui.components.CatalogItemDialog
import com.example.ui.components.CatalogPickerBottomSheet
import com.example.ui.components.ClientPickerBottomSheet
import com.example.ui.components.ContactDialog
import com.example.util.ContactPickerHelper
import com.example.util.Formatters
import com.example.util.QuoteImageGenerator
import com.example.util.QuotePdfGenerator
import com.example.util.QuoteTextFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuoteBuilderScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onQuoteSaved: (Long) -> Unit
) {
    val context = LocalContext.current
    val builderState by viewModel.builderState.collectAsState()
    val profile by viewModel.businessProfile.collectAsState()
    val catalogItems by viewModel.catalogItems.collectAsState()
    val contacts by viewModel.contacts.collectAsState()

    var showCatalogPicker by remember { mutableStateOf(false) }
    var showClientPicker by remember { mutableStateOf(false) }
    var showNewItemDialog by remember { mutableStateOf(false) }
    var showNewContactDialog by remember { mutableStateOf(false) }
    var showTaxesAndDiscounts by remember { mutableStateOf(false) }

    // Phone Contacts Picker Launcher
    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickContact()
    ) { uri: Uri? ->
        if (uri != null) {
            val contact = ContactPickerHelper.parseContactFromUri(context, uri)
            if (contact != null) {
                viewModel.setClient(contact)
                viewModel.saveContact(contact)
                Toast.makeText(context, "Cliente cargado: ${contact.name}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val currency = builderState.quote.currencySymbol.ifBlank { profile?.currencySymbol ?: "$" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (builderState.isEditing) "Editar #${builderState.quote.quoteNumber}" else "Nuevo Presupuesto",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { showTaxesAndDiscounts = !showTaxesAndDiscounts }) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Ajustes de impuestos y notas",
                            tint = if (showTaxesAndDiscounts) MaterialTheme.colorScheme.primary else Color(0xFF64748B)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF7F9FC),
        bottomBar = {
            // Footer matching "Artistic Flair" design HTML
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(12.dp, RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
                color = Color.White,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    // Export Quick Action 3-grid: PDF, Imagen, Texto
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // PDF Button
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                                .clickable {
                                    val quoteWithItems = builderState.toQuoteWithItems()
                                    val uri = QuotePdfGenerator.generatePdf(context, quoteWithItems, profile)
                                    if (uri != null) {
                                        ContactPickerHelper.shareFile(context, uri, "application/pdf", "Enviar PDF Presupuesto")
                                    } else {
                                        Toast.makeText(context, "Error al generar PDF", Toast.LENGTH_SHORT).show()
                                    }
                                },
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "📄", fontSize = 18.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "PDF",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        // Imagen Button
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                                .clickable {
                                    val quoteWithItems = builderState.toQuoteWithItems()
                                    val uri = QuoteImageGenerator.generateImage(context, quoteWithItems, profile)
                                    if (uri != null) {
                                        ContactPickerHelper.shareFile(context, uri, "image/png", "Enviar Imagen Presupuesto")
                                    } else {
                                        Toast.makeText(context, "Error al generar Imagen", Toast.LENGTH_SHORT).show()
                                    }
                                },
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "🖼️", fontSize = 18.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "IMAGEN",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }

                        // Texto / WhatsApp Button
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(20.dp))
                                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                                .clickable {
                                    val quoteWithItems = builderState.toQuoteWithItems()
                                    val text = QuoteTextFormatter.generateFormattedText(quoteWithItems, profile)
                                    if (builderState.quote.clientPhone.isNotBlank()) {
                                        ContactPickerHelper.openWhatsApp(context, builderState.quote.clientPhone, text)
                                    } else {
                                        ContactPickerHelper.shareText(context, text, "Enviar Presupuesto")
                                    }
                                },
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "💬", fontSize = 18.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "TEXTO",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }

                    // Main Save / Share Primary Action
                    Button(
                        onClick = {
                            if (builderState.items.isEmpty()) {
                                Toast.makeText(context, "Agrega al menos un producto o servicio", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            viewModel.saveActiveQuote { savedId ->
                                Toast.makeText(context, "¡Presupuesto guardado exitosamente!", Toast.LENGTH_SHORT).show()
                                onQuoteSaved(savedId)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF005FB0)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text(
                            text = if (builderState.isEditing) "ACTUALIZAR PRESUPUESTO" else "GUARDAR Y FINALIZAR",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                color = Color.White
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Artistic Header with Company & Client
            item {
                ArtisticHeader(
                    profile = profile,
                    clientName = builderState.quote.clientName,
                    quoteNumber = builderState.quote.quoteNumber,
                    onClientClick = { showClientPicker = true },
                    onProfileClick = onNavigateToProfile
                )
            }

            // Taxes, Discounts & Notes Collapsible Panel
            item {
                AnimatedVisibility(visible = showTaxesAndDiscounts) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        shadowElevation = 2.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Ajustes de Presupuesto",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = if (builderState.quote.discountPercent > 0) builderState.quote.discountPercent.toString() else "",
                                    onValueChange = {
                                        val disc = it.toDoubleOrNull() ?: 0.0
                                        viewModel.updateDiscountsAndTaxes(disc, builderState.quote.taxPercent)
                                    },
                                    label = { Text("Descuento %") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.weight(1f)
                                )

                                OutlinedTextField(
                                    value = if (builderState.quote.taxPercent > 0) builderState.quote.taxPercent.toString() else "",
                                    onValueChange = {
                                        val tax = it.toDoubleOrNull() ?: 0.0
                                        viewModel.updateDiscountsAndTaxes(builderState.quote.discountPercent, tax)
                                    },
                                    label = { Text("IVA / Impuesto %") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    shape = RoundedCornerShape(16.dp),
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = builderState.quote.notes,
                                onValueChange = {
                                    viewModel.updateValidityAndNotes(15, it, builderState.quote.terms)
                                },
                                label = { Text("Notas y Garantía") },
                                maxLines = 2,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = builderState.quote.terms,
                                onValueChange = {
                                    viewModel.updateValidityAndNotes(15, builderState.quote.notes, it)
                                },
                                label = { Text("Condiciones de Pago") },
                                maxLines = 2,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Section Header: "SERVICIOS Y PRODUCTOS" + "Ver catálogo"
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SERVICIOS Y PRODUCTOS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = Color(0xFF64748B),
                            fontSize = 11.sp
                        )
                    )

                    Text(
                        text = "+ Desde catálogo",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.clickable { showCatalogPicker = true }
                    )
                }
            }

            // Items List
            if (builderState.items.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "✨ Presupuesto vacío",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1E293B)
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Agrega conceptos desde tu catálogo o crea uno personalizado abajo.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF64748B)
                                ),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                itemsIndexed(builderState.items) { index, item ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 5.dp),
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        shadowElevation = 1.dp
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.name,
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = Color(0xFF0F172A)
                                        )
                                    )
                                    if (item.description.isNotBlank()) {
                                        Text(
                                            text = item.description,
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = Color(0xFF64748B)
                                            )
                                        )
                                    }
                                }

                                // Total price of this item
                                Text(
                                    text = Formatters.formatMoney(item.total, currency),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Quantity Stepper and Unit Price Details
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${Formatters.formatMoney(item.unitPrice, currency)} / ${item.unit}",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = Color(0xFF64748B)
                                    )
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Minus button
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFF1F5F9))
                                            .clickable {
                                                viewModel.updateItemQuantity(index, item.quantity - 1.0)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (item.quantity <= 1.0) Icons.Default.Delete else Icons.Default.Remove,
                                            contentDescription = "Restar",
                                            tint = if (item.quantity <= 1.0) Color(0xFFE11D48) else Color(0xFF475569),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Text(
                                        text = Formatters.formatNumber(item.quantity),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF0F172A)
                                        ),
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    )

                                    // Plus button
                                    Box(
                                        modifier = Modifier
                                            .size(30.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFF1F5F9))
                                            .clickable {
                                                viewModel.updateItemQuantity(index, item.quantity + 1.0)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Sumar",
                                            tint = Color(0xFF475569),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // "+ Agregar concepto" Dashed / Soft Button
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(24.dp))
                        .clickable { showNewItemDialog = true },
                    color = Color.White.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Agregar concepto personalizado",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF64748B)
                                )
                            )
                        }
                    }
                }
            }

            // Total Estimation Display
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Subtotal
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Subtotal",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF64748B))
                            )
                            Text(
                                text = Formatters.formatMoney(builderState.subtotal, currency),
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A)
                                )
                            )
                        }

                        if (builderState.quote.discountPercent > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Descuento (${Formatters.formatNumber(builderState.quote.discountPercent)}%)",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFFE11D48))
                                )
                                Text(
                                    text = "-${Formatters.formatMoney(builderState.discountAmount, currency)}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFE11D48)
                                    )
                                )
                            }
                        }

                        if (builderState.quote.taxPercent > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Impuesto / IVA (${Formatters.formatNumber(builderState.quote.taxPercent)}%)",
                                    style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF64748B))
                                )
                                Text(
                                    text = "+${Formatters.formatMoney(builderState.taxAmount, currency)}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF0F172A)
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(1.dp)
                                .background(Color(0xFFE2E8F0))
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Large Total Estimado
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "Total Estimado",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF64748B)
                                )
                            )

                            Text(
                                text = Formatters.formatMoney(builderState.total, currency),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A),
                                    letterSpacing = (-1).sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    // Modal BottomSheets and Dialogs
    if (showCatalogPicker) {
        CatalogPickerBottomSheet(
            catalogItems = catalogItems,
            currencySymbol = currency,
            onDismiss = { showCatalogPicker = false },
            onItemSelected = { item ->
                viewModel.addItemFromCatalog(item)
                showCatalogPicker = false
                Toast.makeText(context, "Agregado: ${item.name}", Toast.LENGTH_SHORT).show()
            },
            onAddNewCatalogItem = {
                showCatalogPicker = false
                showNewItemDialog = true
            }
        )
    }

    if (showClientPicker) {
        ClientPickerBottomSheet(
            contacts = contacts,
            onDismiss = { showClientPicker = false },
            onSelectContact = { contact ->
                viewModel.setClient(contact)
                showClientPicker = false
            },
            onPickFromAgenda = {
                showClientPicker = false
                contactPickerLauncher.launch(null)
            },
            onAddNewContact = {
                showClientPicker = false
                showNewContactDialog = true
            }
        )
    }

    if (showNewItemDialog) {
        CatalogItemDialog(
            currencySymbol = currency,
            onDismiss = { showNewItemDialog = false },
            onSave = { newItem ->
                viewModel.addCustomItem(
                    name = newItem.name,
                    description = newItem.description,
                    unitPrice = newItem.unitPrice,
                    quantity = 1.0,
                    unit = newItem.unit,
                    isService = newItem.isService
                )
                // Also save to catalog so user can reuse it
                viewModel.saveCatalogItem(newItem)
                showNewItemDialog = false
            }
        )
    }

    if (showNewContactDialog) {
        ContactDialog(
            onDismiss = { showNewContactDialog = false },
            onSave = { newContact ->
                viewModel.setClient(newContact)
                viewModel.saveContact(newContact)
                showNewContactDialog = false
            }
        )
    }
}
