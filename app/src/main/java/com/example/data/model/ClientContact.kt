package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "client_contacts")
data class ClientContact(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val email: String = "",
    val company: String = "",
    val address: String = "",
    val taxId: String = "",
    val notes: String = ""
)
