package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "business_profile")
data class BusinessProfile(
    @PrimaryKey val id: Int = 1,
    val companyName: String = "Mi Empresa",
    val ownerName: String = "",
    val taxId: String = "", // RUT, RFC, CIF, NIF
    val phone: String = "",
    val email: String = "",
    val address: String = "",
    val cityCountry: String = "",
    val website: String = "",
    val currencySymbol: String = "$",
    val defaultTaxPercent: Double = 0.0, // e.g. 19%, 21%, 16%
    val defaultNotes: String = "Gracias por su preferencia. Presupuesto válido por 15 días.",
    val paymentTerms: String = "50% al iniciar, 50% al finalizar contra entrega.",
    val bankDetails: String = ""
)
