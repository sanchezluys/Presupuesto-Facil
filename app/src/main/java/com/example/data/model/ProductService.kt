package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products_services")
data class ProductService(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String = "",
    val unitPrice: Double,
    val unit: String = "unid", // unid, hrs, mes, serv, pza, m2, kg
    val isService: Boolean = true,
    val category: String = "General"
)
