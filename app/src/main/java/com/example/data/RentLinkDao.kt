package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface RentLinkDao {

    // --- Properties ---
    @Query("SELECT * FROM properties ORDER BY id DESC")
    fun getAllProperties(): Flow<List<Property>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProperty(property: Property): Long

    @Update
    suspend fun updateProperty(property: Property)

    @Delete
    suspend fun deleteProperty(property: Property)

    @Query("SELECT * FROM properties WHERE id = :id")
    suspend fun getPropertyById(id: Int): Property?


    // --- Tenants ---
    @Query("SELECT * FROM tenants ORDER BY id DESC")
    fun getAllTenants(): Flow<List<Tenant>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTenant(tenant: Tenant): Long

    @Update
    suspend fun updateTenant(tenant: Tenant)

    @Delete
    suspend fun deleteTenant(tenant: Tenant)

    @Query("SELECT * FROM tenants WHERE id = :id")
    suspend fun getTenantById(id: Int): Tenant?


    // --- Leases ---
    @Query("SELECT * FROM leases ORDER BY id DESC")
    fun getAllLeases(): Flow<List<Lease>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLease(lease: Lease): Long

    @Update
    suspend fun updateLease(lease: Lease)


    // --- Transactions ---
    @Query("SELECT * FROM transactions ORDER BY id DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long


    // --- Alerts ---
    @Query("SELECT * FROM alerts ORDER BY id DESC")
    fun getAllAlerts(): Flow<List<RentLinkAlert>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: RentLinkAlert): Long

    @Query("UPDATE alerts SET isRead = 1 WHERE id = :id")
    suspend fun markAlertAsRead(id: Int)

    @Query("DELETE FROM alerts WHERE id = :id")
    suspend fun deleteAlertById(id: Int)


    // --- Maintenance ---
    @Query("SELECT * FROM maintenance_requests ORDER BY id DESC")
    fun getAllMaintenanceRequests(): Flow<List<MaintenanceRequest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaintenanceRequest(request: MaintenanceRequest): Long

    @Update
    suspend fun updateMaintenanceRequest(request: MaintenanceRequest)
}
