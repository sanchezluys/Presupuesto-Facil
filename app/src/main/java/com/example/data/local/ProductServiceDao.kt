package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ProductService
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductServiceDao {
    @Query("SELECT * FROM products_services ORDER BY isService DESC, name ASC")
    fun getAll(): Flow<List<ProductService>>

    @Query("SELECT * FROM products_services WHERE isService = :isService ORDER BY name ASC")
    fun getByType(isService: Boolean): Flow<List<ProductService>>

    @Query("SELECT * FROM products_services WHERE name LIKE '%' || :query || '%' OR description LIKE '%' || :query || '%'")
    fun search(query: String): Flow<List<ProductService>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: ProductService): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ProductService>)

    @Update
    suspend fun update(item: ProductService)

    @Delete
    suspend fun delete(item: ProductService)
}
