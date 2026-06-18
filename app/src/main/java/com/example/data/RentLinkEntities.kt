package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "properties")
data class Property(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val address: String,
    val rentAmount: Double,
    val bedrooms: Int,
    val bathrooms: Int,
    val description: String,
    val imageResName: String = "ic_property_placeholder"
) : Serializable

@Entity(tableName = "tenants")
data class Tenant(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val rentLinkId: String,
    val phone: String,
    val email: String,
    val assignedPropertyId: Int?,
    val assignedPropertyName: String? = null,
    val status: String = "Active" // Active, Pending, Inactive
) : Serializable

@Entity(tableName = "leases")
data class Lease(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val propertyId: Int,
    val tenantId: Int,
    val tenantName: String,
    val propertyName: String,
    val startDate: String,
    val endDate: String,
    val rentAmount: Double,
    val status: String = "Active" // Active, Expired
) : Serializable

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val tenantName: String,
    val propertyName: String,
    val amount: Double,
    val status: String, // Paid, Pending, Overdue
    val date: String,
    val rentLinkId: String,
    val paymentMethod: String, // bKash, Nagad, Rocket
    val type: String, // Collection, Payment, Withdrawal, Sent
    val ledgerHash: String = "" // Simulates standard ledger verification hash
) : Serializable

@Entity(tableName = "alerts")
data class RentLinkAlert(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // LeaseExpiration, Maintenance, OverdueRent, Payment
    val title: String,
    val message: String,
    val statusBadge: String, // Expiring, Maintenance, Overdue, Success
    val timestamp: String,
    val isRead: Boolean = false
) : Serializable

@Entity(tableName = "maintenance_requests")
data class MaintenanceRequest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val propertyId: Int,
    val propertyName: String,
    val tenantName: String,
    val description: String,
    val status: String = "Pending", // Pending, In Progress, Resolved
    val date: String,
    val priority: String = "Medium"
) : Serializable
