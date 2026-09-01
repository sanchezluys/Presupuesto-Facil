package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BusinessProfile
import com.example.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BusinessProfileScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val currentProfile by viewModel.businessProfile.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    var companyName by remember { mutableStateOf("") }
    var ownerName by remember { mutableStateOf("") }
    var taxId by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var website by remember { mutableStateOf("") }
    var currencySymbol by remember { mutableStateOf("$") }
    var defaultTaxStr by remember { mutableStateOf("0") }
    var defaultNotes by remember { mutableStateOf("") }
    var paymentTerms by remember { mutableStateOf("") }
    var bankDetails by remember { mutableStateOf("") }

    val commonCurrencies = listOf("$", "€", "S/", "MXN", "Bs", "COP", "CLP", "USD")

    LaunchedEffect(currentProfile) {
        currentProfile?.let { p ->
            companyName = p.companyName
            ownerName = p.ownerName
            taxId = p.taxId
            phone = p.phone
            email = p.email
            address = p.address
            website = p.website
            currencySymbol = p.currencySymbol
            defaultTaxStr = if (p.defaultTaxPercent % 1.0 == 0.0) p.defaultTaxPercent.toInt().toString() else p.defaultTaxPercent.toString()
            defaultNotes = p.defaultNotes
            paymentTerms = p.paymentTerms
            bankDetails = p.bankDetails
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Datos de la Empresa", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleDarkMode() },
                        modifier = Modifier.testTag("dark_mode_toggle_profile")
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDarkMode) "Activar modo claro" else "Activar modo oscuro"
                        )
                    }
                    IconButton(
                        onClick = {
                            val taxVal = defaultTaxStr.toDoubleOrNull() ?: 0.0
                            val updated = BusinessProfile(
                                id = 1,
                                companyName = companyName.trim().ifBlank { "Mi Empresa" },
                                ownerName = ownerName.trim(),
                                taxId = taxId.trim(),
                                phone = phone.trim(),
                                email = email.trim(),
                                address = address.trim(),
                                website = website.trim(),
                                currencySymbol = currencySymbol.trim().ifBlank { "$" },
                                defaultTaxPercent = taxVal,
                                defaultNotes = defaultNotes.trim(),
                                paymentTerms = paymentTerms.trim(),
                                bankDetails = bankDetails.trim()
                            )
                            viewModel.saveBusinessProfile(updated)
                            Toast.makeText(context, "Datos de la empresa guardados", Toast.LENGTH_SHORT).show()
                            onNavigateBack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Guardar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            // Header Info Card
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Business,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(14.dp))
                            Column {
                                Text(
                                    text = "Perfil Comercial",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "Estos datos aparecerán en los presupuestos, PDF e imágenes.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        OutlinedTextField(
                            value = companyName,
                            onValueChange = { companyName = it },
                            label = { Text("Nombre de la Empresa o Negocio *") },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = ownerName,
                            onValueChange = { ownerName = it },
                            label = { Text("Nombre del Titular / Responsable") },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = taxId,
                            onValueChange = { taxId = it },
                            label = { Text("Identificación Fiscal (RUT / CIF / RFC / NIF)") },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("Teléfono / WhatsApp") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Correo Electrónico") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Dirección / Ciudad / País") },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Defaults & Payment Configuration
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 2.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = "Configuración por Defecto",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Currency Selector
                        Text(
                            text = "Moneda del Negocio",
                            style = MaterialTheme.typography.labelMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            commonCurrencies.take(4).forEach { cur ->
                                val isSelected = currencySymbol == cur
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { currencySymbol = cur },
                                    label = { Text(cur, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = defaultTaxStr,
                            onValueChange = { defaultTaxStr = it },
                            label = { Text("IVA / Impuesto por defecto (%)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = defaultNotes,
                            onValueChange = { defaultNotes = it },
                            label = { Text("Notas y Garantía estándar") },
                            maxLines = 3,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = paymentTerms,
                            onValueChange = { paymentTerms = it },
                            label = { Text("Condiciones de Pago estándar") },
                            maxLines = 2,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = bankDetails,
                            onValueChange = { bankDetails = it },
                            label = { Text("Datos Bancarios para Transferencias") },
                            maxLines = 3,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Button(
                            onClick = {
                                val taxVal = defaultTaxStr.toDoubleOrNull() ?: 0.0
                                val updated = BusinessProfile(
                                    id = 1,
                                    companyName = companyName.trim().ifBlank { "Mi Empresa" },
                                    ownerName = ownerName.trim(),
                                    taxId = taxId.trim(),
                                    phone = phone.trim(),
                                    email = email.trim(),
                                    address = address.trim(),
                                    website = website.trim(),
                                    currencySymbol = currencySymbol.trim().ifBlank { "$" },
                                    defaultTaxPercent = taxVal,
                                    defaultNotes = defaultNotes.trim(),
                                    paymentTerms = paymentTerms.trim(),
                                    bankDetails = bankDetails.trim()
                                )
                                viewModel.saveBusinessProfile(updated)
                                Toast.makeText(context, "Datos de la empresa guardados", Toast.LENGTH_SHORT).show()
                                onNavigateBack()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("GUARDAR CAMBIOS", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
