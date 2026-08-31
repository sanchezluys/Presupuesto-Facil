package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuoteStatus
import com.example.data.model.QuoteWithItems
import com.example.ui.MainViewModel
import com.example.ui.components.QuoteCard
import com.example.util.ContactPickerHelper
import com.example.util.Formatters
import com.example.util.QuotePdfGenerator
import com.example.util.QuoteTextFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuotesListScreen(
    viewModel: MainViewModel,
    onNewQuoteClick: () -> Unit,
    onQuoteClick: (Long) -> Unit,
    onCatalogClick: () -> Unit,
    onContactsClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    val context = LocalContext.current
    val quotes by viewModel.quotes.collectAsState()
    val profile by viewModel.businessProfile.collectAsState()

    var selectedStatusFilter by remember { mutableStateOf<QuoteStatus?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearchField by remember { mutableStateOf(false) }

    val currency = profile?.currencySymbol ?: "$"

    // Filter quotes
    val filteredQuotes = quotes.filter { qwi ->
        val matchesStatus = selectedStatusFilter == null || qwi.quote.status == selectedStatusFilter
        val matchesSearch = searchQuery.isBlank() ||
                qwi.quote.quoteNumber.contains(searchQuery, ignoreCase = true) ||
                qwi.quote.clientName.contains(searchQuery, ignoreCase = true) ||
                qwi.quote.clientCompany.contains(searchQuery, ignoreCase = true) ||
                qwi.items.any { it.name.contains(searchQuery, ignoreCase = true) }
        matchesStatus && matchesSearch
    }

    val totalAmount = filteredQuotes.sumOf { it.total }
    val acceptedCount = quotes.count { it.quote.status == QuoteStatus.ACCEPTED }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = (profile?.companyName ?: "MI EMPRESA").uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 10.sp
                            )
                        )
                        Text(
                            text = "Presupuestos",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = FontFamily.Serif,
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showSearchField = !showSearchField }) {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar")
                    }
                    IconButton(onClick = onCatalogClick) {
                        Icon(imageVector = Icons.Default.Inventory2, contentDescription = "Catálogo")
                    }
                    IconButton(onClick = onContactsClick) {
                        Icon(imageVector = Icons.Default.Contacts, contentDescription = "Clientes")
                    }
                    IconButton(onClick = onProfileClick) {
                        Icon(imageVector = Icons.Default.Business, contentDescription = "Mi Empresa")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNewQuoteClick,
                containerColor = Color(0xFF005FB0),
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp),
                icon = { Icon(imageVector = Icons.Default.Add, contentDescription = null) },
                text = { Text("Nuevo Presupuesto", fontWeight = FontWeight.Bold) }
            )
        },
        containerColor = Color(0xFFF7F9FC)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // Optional Search Bar
            item {
                AnimatedVisibility(visible = showSearchField) {
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Buscar por cliente, número o concepto...") },
                            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Stats Summary Card
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "TOTAL COTIZADO",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp,
                                    color = Color(0xFF64748B)
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = Formatters.formatMoney(totalAmount, currency),
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "${quotes.size} Presupuestos",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0F172A)
                                )
                            )
                            Text(
                                text = "$acceptedCount Aceptados",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF16A34A),
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }
            }

            // Filter Chips (Todos, Borrador, Enviado, Aceptado, Rechazado)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedStatusFilter == null,
                        onClick = { selectedStatusFilter = null },
                        label = { Text("Todos (${quotes.size})") },
                        shape = RoundedCornerShape(14.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )

                    QuoteStatus.values().forEach { status ->
                        val count = quotes.count { it.quote.status == status }
                        FilterChip(
                            selected = selectedStatusFilter == status,
                            onClick = { selectedStatusFilter = status },
                            label = { Text("${status.displayName} ($count)") },
                            shape = RoundedCornerShape(14.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }

            // Section Title
            item {
                Text(
                    text = "HISTORIAL",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp,
                        color = Color(0xFF64748B),
                        fontSize = 11.sp
                    ),
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)
                )
            }

            // Empty State or List of Cards
            if (filteredQuotes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 40.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(Color.White)
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "📋", fontSize = 36.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "No hay presupuestos que mostrar",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Toca '+ Nuevo Presupuesto' para comenzar a cotizar.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFF64748B)
                                )
                            )
                        }
                    }
                }
            } else {
                items(filteredQuotes, key = { it.quote.id }) { qwi ->
                    Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                        QuoteCard(
                            quoteWithItems = qwi,
                            onClick = { onQuoteClick(qwi.quote.id) },
                            onSharePdf = {
                                val uri = QuotePdfGenerator.generatePdf(context, qwi, profile)
                                if (uri != null) {
                                    ContactPickerHelper.shareFile(context, uri, "application/pdf", "Enviar PDF Presupuesto")
                                } else {
                                    Toast.makeText(context, "Error al generar PDF", Toast.LENGTH_SHORT).show()
                                }
                            },
                            onShareText = {
                                val text = QuoteTextFormatter.generateFormattedText(qwi, profile)
                                if (qwi.quote.clientPhone.isNotBlank()) {
                                    ContactPickerHelper.openWhatsApp(context, qwi.quote.clientPhone, text)
                                } else {
                                    ContactPickerHelper.shareText(context, text, "Enviar Presupuesto")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
