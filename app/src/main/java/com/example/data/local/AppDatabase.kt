package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.BusinessProfile
import com.example.data.model.ClientContact
import com.example.data.model.ProductService
import com.example.data.model.Quote
import com.example.data.model.QuoteItem
import com.example.data.model.QuoteStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Converters {
    @TypeConverter
    fun fromQuoteStatus(status: QuoteStatus): String = status.name

    @TypeConverter
    fun toQuoteStatus(value: String): QuoteStatus = try {
        QuoteStatus.valueOf(value)
    } catch (e: Exception) {
        QuoteStatus.DRAFT
    }
}

@Database(
    entities = [
        BusinessProfile::class,
        ProductService::class,
        ClientContact::class,
        Quote::class,
        QuoteItem::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun businessProfileDao(): BusinessProfileDao
    abstract fun productServiceDao(): ProductServiceDao
    abstract fun clientContactDao(): ClientContactDao
    abstract fun quoteDao(): QuoteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "presupuestos_database"
                )
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            // Initial Business Profile
            val defaultProfile = BusinessProfile(
                id = 1,
                companyName = "García Soluciones IT",
                ownerName = "Carlos García",
                taxId = "B-98765432",
                phone = "+34 612 345 678",
                email = "contacto@garciasoluciones.com",
                address = "Av. Principal 124, Of. 3B",
                cityCountry = "Madrid, España",
                website = "www.garciasoluciones.com",
                currencySymbol = "$",
                defaultTaxPercent = 16.0,
                defaultNotes = "Presupuesto válido por 15 días hábiles. Incluye garantía de satisfacción.",
                paymentTerms = "50% anticipo al iniciar proyecto, 50% al entregar y aprobar.",
                bankDetails = "Banco Santander: ES91 2100 0418 4502 0005 1332"
            )
            database.businessProfileDao().insertOrUpdate(defaultProfile)

            // Initial Products & Services
            val sampleItems = listOf(
                ProductService(name = "Desarrollo Landing Page", description = "Diseño UX/UI + Desarrollo React / Móvil adaptable", unitPrice = 450.0, unit = "servicio", isService = true, category = "Desarrollo"),
                ProductService(name = "Hosting Premium Anual", description = "Servidor dedicado con soporte 24/7 y SSL", unitPrice = 120.0, unit = "año", isService = false, category = "Infraestructura"),
                ProductService(name = "Mantenimiento Mensual", description = "Actualizaciones, backups diarios y monitoreo", unitPrice = 80.0, unit = "mes", isService = true, category = "Soporte"),
                ProductService(name = "Consultoría Técnica (Hora)", description = "Asesoría especializada en arquitectura y software", unitPrice = 50.0, unit = "hora", isService = true, category = "Consultoría"),
                ProductService(name = "Integración Pasarela de Pago", description = "Configuración Stripe/PayPal/MercadoPago", unitPrice = 200.0, unit = "servicio", isService = true, category = "Desarrollo")
            )
            database.productServiceDao().insertAll(sampleItems)

            // Initial Clients
            val sampleClients = listOf(
                ClientContact(name = "Roberto Martínez", phone = "+34 689 112 233", email = "roberto.m@empresa.es", company = "Martínez Logistics", address = "Calle Mayor 45", taxId = "B-12345678"),
                ClientContact(name = "Elena Rostova", phone = "+34 655 443 322", email = "elena@designstudio.io", company = "Studio Creativo", address = "Paseo de Gracia 12", taxId = "A-87654321")
            )
            database.clientContactDao().insertAll(sampleClients)

            // Initial Demo Quote
            val quote = Quote(
                id = 0,
                quoteNumber = "PRE-001",
                title = "Presupuesto",
                clientName = "Roberto Martínez",
                clientPhone = "+34 689 112 233",
                clientEmail = "roberto.m@empresa.es",
                clientCompany = "Martínez Logistics",
                clientAddress = "Calle Mayor 45",
                createdAtMillis = System.currentTimeMillis(),
                validUntilMillis = System.currentTimeMillis() + (15L * 24 * 60 * 60 * 1000),
                status = QuoteStatus.SENT,
                discountPercent = 0.0,
                taxPercent = 0.0,
                notes = "Presupuesto válido por 15 días. Soporte y entrega garantizada.",
                terms = "50% anticipo, 50% contra entrega.",
                currencySymbol = "$"
            )
            val items = listOf(
                QuoteItem(name = "Desarrollo Landing Page", description = "Diseño UX/UI + Desarrollo React", unitPrice = 450.0, quantity = 1.0, unit = "servicio", isService = true),
                QuoteItem(name = "Hosting Premium (12m)", description = "Soporte 24/7 y SSL incluido", unitPrice = 120.0, quantity = 1.0, unit = "año", isService = false)
            )
            database.quoteDao().saveFullQuote(quote, items)
        }
    }
}
