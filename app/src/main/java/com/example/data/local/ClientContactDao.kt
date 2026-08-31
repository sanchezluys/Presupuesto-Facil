package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ClientContact
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientContactDao {
    @Query("SELECT * FROM client_contacts ORDER BY name ASC")
    fun getAll(): Flow<List<ClientContact>>

    @Query("SELECT * FROM client_contacts WHERE name LIKE '%' || :query || '%' OR phone LIKE '%' || :query || '%' OR company LIKE '%' || :query || '%'")
    fun search(query: String): Flow<List<ClientContact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: ClientContact): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(contacts: List<ClientContact>)

    @Update
    suspend fun update(contact: ClientContact)

    @Delete
    suspend fun delete(contact: ClientContact)
}
