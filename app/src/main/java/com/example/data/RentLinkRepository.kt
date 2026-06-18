package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RentLinkRepository(private val rentLinkDao: RentLinkDao) {

    val allProperties: Flow<List<Property>> = rentLinkDao.getAllProperties()
    val allTenants: Flow<List<Tenant>> = rentLinkDao.getAllTenants()
    val allLeases: Flow<List<Lease>> = rentLinkDao.getAllLeases()
    val allTransactions: Flow<List<Transaction>> = rentLinkDao.getAllTransactions()
    val allAlerts: Flow<List<RentLinkAlert>> = rentLinkDao.getAllAlerts()
    val allMaintenanceRequests: Flow<List<MaintenanceRequest>> = rentLinkDao.getAllMaintenanceRequests()

    // --- Property operations ---
    suspend fun insertProperty(property: Property): Long = rentLinkDao.insertProperty(property)
    suspend fun updateProperty(property: Property) = rentLinkDao.updateProperty(property)
    suspend fun deleteProperty(property: Property) = rentLinkDao.deleteProperty(property)
    suspend fun getPropertyById(id: Int): Property? = rentLinkDao.getPropertyById(id)

    // --- Tenant operations ---
    suspend fun insertTenant(tenant: Tenant): Long = rentLinkDao.insertTenant(tenant)
    suspend fun updateTenant(tenant: Tenant) = rentLinkDao.updateTenant(tenant)
    suspend fun deleteTenant(tenant: Tenant) = rentLinkDao.deleteTenant(tenant)
    suspend fun getTenantById(id: Int): Tenant? = rentLinkDao.getTenantById(id)

    // --- Lease operations ---
    suspend fun insertLease(lease: Lease): Long = rentLinkDao.insertLease(lease)
    suspend fun updateLease(lease: Lease) = rentLinkDao.updateLease(lease)

    // --- Transaction operations ---
    suspend fun insertTransaction(transaction: Transaction): Long {
        val hashInput = "${transaction.tenantName}${transaction.propertyName}${transaction.amount}${transaction.date}${System.currentTimeMillis()}"
        val verifiedHash = generateLedgerHash(hashInput)
        val securedTransaction = transaction.copy(ledgerHash = verifiedHash)
        return rentLinkDao.insertTransaction(securedTransaction)
    }

    // --- Alert operations ---
    suspend fun insertAlert(alert: RentLinkAlert): Long = rentLinkDao.insertAlert(alert)
    suspend fun markAlertAsRead(id: Int) = rentLinkDao.markAlertAsRead(id)
    suspend fun deleteAlertById(id: Int) = rentLinkDao.deleteAlertById(id)

    // --- Maintenance operations ---
    suspend fun insertMaintenanceRequest(request: MaintenanceRequest): Long = rentLinkDao.insertMaintenanceRequest(request)
    suspend fun updateMaintenanceRequest(request: MaintenanceRequest) = rentLinkDao.updateMaintenanceRequest(request)

    // --- Helper to generate secure transaction SHA-256 ledger hashes ---
    private fun generateLedgerHash(input: String): String {
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val hashBytes = digest.digest(input.toByteArray(Charsets.UTF_8))
            hashBytes.joinToString("") { String.format("%02x", it) }.take(16).uppercase()
        } catch (e: Exception) {
            "TX-SECURE-LEDGER"
        }
    }

    // --- Seed local DB with Premium Bangladeshi Rent Records ---
    suspend fun prepopulateIfEmpty() {
        val properties = rentLinkDao.getAllProperties().firstOrNull() ?: emptyList()
        if (properties.isEmpty()) {
            // Seed Properties
            val p1Id = rentLinkDao.insertProperty(
                Property(
                    name = "Gulshan Heights, Apt 4B",
                    address = "Road 12, Gulshan-2, Dhaka",
                    rentAmount = 45000.0,
                    bedrooms = 3,
                    bathrooms = 3,
                    description = "A premium, fully furnished lakeside apartment in Gulshan-2. Safe residential area with 24/7 security, backup generator, and lift facility.",
                    imageResName = "prop1"
                )
            ).toInt()

            val p2Id = rentLinkDao.insertProperty(
                Property(
                    name = "Dhanmondi Lakeview Villa",
                    address = "Road 27, Dhanmondi, Dhaka",
                    rentAmount = 32000.0,
                    bedrooms = 2,
                    bathrooms = 2,
                    description = "Cozy spacious family apartment right beside Dhanmondi Lake. Beautiful rooftop garden and strict security layout.",
                    imageResName = "prop2"
                )
            ).toInt()

            val p3Id = rentLinkDao.insertProperty(
                Property(
                    name = "Banani Green Plaza, Duplex",
                    address = "Road 11, Banani, Dhaka",
                    rentAmount = 60000.0,
                    bedrooms = 4,
                    bathrooms = 4,
                    description = "Ultra-premium 2400 sq. ft. duplex in Banani. Features designer fittings, high ceilings, automated building controls, and large private verandas.",
                    imageResName = "prop3"
                )
            ).toInt()

            val p4Id = rentLinkDao.insertProperty(
                Property(
                    name = "Uttara Garden Suite",
                    address = "Sector 4, Uttara, Dhaka",
                    rentAmount = 25000.0,
                    bedrooms = 2,
                    bathrooms = 2,
                    description = "Quiet and peaceful suite near Uttara Park. Ideal for corporate workers or small modern couples.",
                    imageResName = "prop4"
                )
            ).toInt()

            // Seed Tenants
            val t1Id = rentLinkDao.insertTenant(
                Tenant(
                    name = "Anisur Rahman",
                    rentLinkId = "RL01711223344",
                    phone = "01711223344",
                    email = "anis@gmail.com",
                    assignedPropertyId = p1Id,
                    assignedPropertyName = "Gulshan Heights, Apt 4B",
                    status = "Active"
                )
            ).toInt()

            val t2Id = rentLinkDao.insertTenant(
                Tenant(
                    name = "Farhana Yasmin",
                    rentLinkId = "RL01855667788",
                    phone = "01855667788",
                    email = "farhana.yas@yahoo.com",
                    assignedPropertyId = p2Id,
                    assignedPropertyName = "Dhanmondi Lakeview Villa",
                    status = "Active"
                )
            ).toInt()

            val t3Id = rentLinkDao.insertTenant(
                Tenant(
                    name = "Tasnim Alam",
                    rentLinkId = "RL01988776655",
                    phone = "01988776655",
                    email = "tasnim_a@gmail.com",
                    assignedPropertyId = p3Id,
                    assignedPropertyName = "Banani Green Plaza, Duplex",
                    status = "Active"
                )
            ).toInt()

            val t4Id = rentLinkDao.insertTenant(
                Tenant(
                    name = "Kabir Hossain",
                    rentLinkId = "RL01633445566",
                    phone = "01633445566",
                    email = "kabir.hossain@outlook.com",
                    assignedPropertyId = p4Id,
                    assignedPropertyName = "Uttara Garden Suite",
                    status = "Pending"
                )
            ).toInt()

            // Seed Leases
            rentLinkDao.insertLease(
                Lease(
                    propertyId = p1Id,
                    tenantId = t1Id,
                    tenantName = "Anisur Rahman",
                    propertyName = "Gulshan Heights, Apt 4B",
                    startDate = "2026-01-01",
                    endDate = "2026-12-31",
                    rentAmount = 45000.0,
                    status = "Active"
                )
            )

            rentLinkDao.insertLease(
                Lease(
                    propertyId = p2Id,
                    tenantId = t2Id,
                    tenantName = "Farhana Yasmin",
                    propertyName = "Dhanmondi Lakeview Villa",
                    startDate = "2026-02-15",
                    endDate = "2027-02-14",
                    rentAmount = 32000.0,
                    status = "Active"
                )
            )

            rentLinkDao.insertLease(
                Lease(
                    propertyId = p3Id,
                    tenantId = t3Id,
                    tenantName = "Tasnim Alam",
                    propertyName = "Banani Green Plaza, Duplex",
                    startDate = "2025-06-01",
                    endDate = "2026-05-31",
                    rentAmount = 60000.0,
                    status = "Expired"
                )
            )

            // Seed Transactions
            insertTransaction(
                Transaction(
                    tenantName = "Anisur Rahman",
                    propertyName = "Gulshan Heights, Apt 4B",
                    amount = 45000.0,
                    status = "Paid",
                    date = "10 Jun 2026",
                    rentLinkId = "RL01711223344",
                    paymentMethod = "bKash",
                    type = "Collection"
                )
            )

            insertTransaction(
                Transaction(
                    tenantName = "Farhana Yasmin",
                    propertyName = "Dhanmondi Lakeview Villa",
                    amount = 32000.0,
                    status = "Paid",
                    date = "12 Jun 2026",
                    rentLinkId = "RL01855667788",
                    paymentMethod = "Nagad",
                    type = "Collection"
                )
            )

            insertTransaction(
                Transaction(
                    tenantName = "Tasnim Alam",
                    propertyName = "Banani Green Plaza, Duplex",
                    amount = 60000.0,
                    status = "Overdue",
                    date = "01 Jun 2026",
                    rentLinkId = "RL01988776655",
                    paymentMethod = "Rocket",
                    type = "Collection"
                )
            )

            insertTransaction(
                Transaction(
                    tenantName = "Kabir Hossain",
                    propertyName = "Uttara Garden Suite",
                    amount = 25000.0,
                    status = "Pending",
                    date = "20 Jun 2026",
                    rentLinkId = "RL01633445566",
                    paymentMethod = "bKash",
                    type = "Collection"
                )
            )

            // Seed Alerts
            rentLinkDao.insertAlert(
                RentLinkAlert(
                    type = "OverdueRent",
                    title = "Overdue Rent Alert",
                    message = "Tenant Tasnim Alam is overdue on rent BDT 60,000 for Banani Green Plaza, Duplex. Due date: Jun 01, 2026.",
                    statusBadge = "Overdue",
                    timestamp = "Jun 16, 2026"
                )
            )

            rentLinkDao.insertAlert(
                RentLinkAlert(
                    type = "Maintenance",
                    title = "New Maintenance Request",
                    message = "Tenant Farhana Yasmin requested assistance: bathroom tap leakage in Dhanmondi Lakeview Villa.",
                    statusBadge = "Maintenance",
                    timestamp = "Jun 15, 2026"
                )
            )

            rentLinkDao.insertAlert(
                RentLinkAlert(
                    type = "LeaseExpiration",
                    title = "Lease Expiring Soon",
                    message = "The lease agreement for Uttara Garden Suite is nearing expiration. Expiry date: Jul 31, 2026.",
                    statusBadge = "Expiring",
                    timestamp = "Jun 14, 2026"
                )
            )

            rentLinkDao.insertAlert(
                RentLinkAlert(
                    type = "Payment",
                    title = "Rent Payment Success",
                    message = "Received rent BDT 45,000 from Anisur Rahman for Gulshan Heights, Apt 4B via bKash.",
                    statusBadge = "Success",
                    timestamp = "Jun 10, 2026"
                )
            )

            // Seed Maintenance Requests
            rentLinkDao.insertMaintenanceRequest(
                MaintenanceRequest(
                    propertyId = p2Id,
                    propertyName = "Dhanmondi Lakeview Villa",
                    tenantName = "Farhana Yasmin",
                    description = "The main bathroom tap is leaking badly. Floods the floor if left on. Needs plumbing support immediately.",
                    status = "Pending",
                    date = "15 Jun 2026",
                    priority = "High"
                )
            )

            rentLinkDao.insertMaintenanceRequest(
                MaintenanceRequest(
                    propertyId = p1Id,
                    propertyName = "Gulshan Heights, Apt 4B",
                    tenantName = "Anisur Rahman",
                    description = "AC in the master bedroom has slow cooling and needs filter gas refill service.",
                    status = "In Progress",
                    date = "13 Jun 2026",
                    priority = "Medium"
                )
            )
        }
    }
}
