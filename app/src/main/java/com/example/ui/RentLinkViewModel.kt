package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RentLinkViewModel(application: Application) : AndroidViewModel(application) {

    private val db = RentLinkDatabase.getDatabase(application)
    private val repository = RentLinkRepository(db.rentLinkDao())

    // --- Database Flows ---
    val properties: StateFlow<List<Property>> = repository.allProperties
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tenants: StateFlow<List<Tenant>> = repository.allTenants
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val leases: StateFlow<List<Lease>> = repository.allLeases
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val transactions: StateFlow<List<Transaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val alerts: StateFlow<List<RentLinkAlert>> = repository.allAlerts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val maintenanceRequests: StateFlow<List<MaintenanceRequest>> = repository.allMaintenanceRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Onboarding & Auth State ---
    private val _currentUserId = MutableStateFlow("RL01812345678")
    val currentUserId: StateFlow<String> = _currentUserId.asStateFlow()

    private val _currentUserName = MutableStateFlow("Siddikur Rahman")
    val currentUserName: StateFlow<String> = _currentUserName.asStateFlow()

    private val _currentUserMobile = MutableStateFlow("01812345678")
    val currentUserMobile: StateFlow<String> = _currentUserMobile.asStateFlow()

    private val _currentUserRole = MutableStateFlow("Landlord") // Landlord or Tenant
    val currentUserRole: StateFlow<String> = _currentUserRole.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _splashFinished = MutableStateFlow(false)
    val splashFinished: StateFlow<Boolean> = _splashFinished.asStateFlow()

    private val _isOnboarded = MutableStateFlow(false)
    val isOnboarded: StateFlow<Boolean> = _isOnboarded.asStateFlow()

    // --- Financial Stats (Simulated Reactive Properties) ---
    private val _availableBalance = MutableStateFlow(245000.0) // BDT
    val availableBalance: StateFlow<Double> = _availableBalance.asStateFlow()

    private val _collectedRent = MutableStateFlow(77000.0) // BDT
    val collectedRent: StateFlow<Double> = _collectedRent.asStateFlow()

    private val _pendingRent = MutableStateFlow(25000.0) // BDT
    val pendingRent: StateFlow<Double> = _pendingRent.asStateFlow()

    // --- App States ---
    init {
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
            recalculateFinancialStats()
        }
    }

    fun finishSplash() {
        _splashFinished.value = true
    }

    fun selectRole(role: String) {
        _currentUserRole.value = role
        if (role == "Tenant") {
            _currentUserId.value = "RL01711223344"
            _currentUserName.value = "Anisur Rahman"
            _currentUserMobile.value = "01711223344"
            _availableBalance.value = 18500.0
        } else {
            _currentUserId.value = "RL01812345678"
            _currentUserName.value = "Siddikur Rahman"
            _currentUserMobile.value = "01812345678"
            _availableBalance.value = 245000.0
        }
        _isOnboarded.value = true
    }

    fun login(mobile: String, name: String, role: String) {
        _currentUserMobile.value = mobile
        _currentUserName.value = name
        _currentUserRole.value = role
        // Format RentLink ID
        val sanitized = mobile.trim().replace("+88", "").replace(" ", "")
        _currentUserId.value = "RL$sanitized"
        
        if (role == "Tenant") {
            _availableBalance.value = 15000.0
        } else {
            _availableBalance.value = 120000.0
        }
        _isLoggedIn.value = true
        _isOnboarded.value = true
        
        viewModelScope.launch {
            // Recalculate stats based on name & DB
            recalculateFinancialStats()
        }
    }

    fun logout() {
        _isLoggedIn.value = false
        _isOnboarded.value = false
        _currentUserRole.value = "Landlord"
    }

    // --- Financial Ledger Updates ---
    private fun recalculateFinancialStats() {
        viewModelScope.launch {
            // Aggregate database values
            var collected = 0.0
            var pending = 0.0
            
            transactions.value.forEach { tx ->
                if (tx.type == "Collection") {
                    when (tx.status) {
                        "Paid" -> collected += tx.amount
                        "Pending" -> pending += tx.amount
                        "Overdue" -> {
                            // If overdue, add to pending too
                            pending += tx.amount
                        }
                    }
                }
            }
            if (collected > 0) {
                _collectedRent.value = collected
            }
            if (pending > 0) {
                _pendingRent.value = pending
            }
        }
    }

    // --- Quick Actions ---
    fun sendMoney(targetId: String, amount: Double, method: String, onComplete: (Boolean, String) -> Unit) {
        if (amount <= 0 || amount > _availableBalance.value) {
            onComplete(false, "Invalid amount or insufficient balance")
            return
        }

        viewModelScope.launch {
            // Update balance
            _availableBalance.value -= amount

            // Save transaction
            val dateStr = SimpleDateFormat("dd MMM 2026", Locale.getDefault()).format(Date())
            val tx = Transaction(
                tenantName = "RentLink Transfer",
                propertyName = "Target ID: $targetId",
                amount = amount,
                status = "Paid",
                date = dateStr,
                rentLinkId = targetId,
                paymentMethod = method,
                type = "Sent"
            )
            repository.insertTransaction(tx)

            // Save Alert
            repository.insertAlert(
                RentLinkAlert(
                    type = "Payment",
                    title = "Money Sent Successfully",
                    message = "BDT ${String.format("%,.2f", amount)} was sent securely to ID $targetId via $method.",
                    statusBadge = "Success",
                    timestamp = "Today"
                )
            )

            recalculateFinancialStats()
            onComplete(true, "Transaction completed successfully")
        }
    }

    fun withdrawMoney(amount: Double, method: String, onComplete: (Boolean, String) -> Unit) {
        if (amount <= 0 || amount > _availableBalance.value) {
            onComplete(false, "Invalid amount or insufficient balance")
            return
        }

        viewModelScope.launch {
            _availableBalance.value -= amount

            val dateStr = SimpleDateFormat("dd MMM 2026", Locale.getDefault()).format(Date())
            val tx = Transaction(
                tenantName = "Withdrawal to Personal Account",
                propertyName = "Gateway: $method",
                amount = amount,
                status = "Paid",
                date = dateStr,
                rentLinkId = _currentUserId.value,
                paymentMethod = method,
                type = "Withdrawal"
            )
            repository.insertTransaction(tx)

            repository.insertAlert(
                RentLinkAlert(
                    type = "Payment",
                    title = "Withdrawal Success",
                    message = "Withdrew BDT ${String.format("%,.2f", amount)} successfully to your $method account.",
                    statusBadge = "Success",
                    timestamp = "Today"
                )
            )

            recalculateFinancialStats()
            onComplete(true, "Withdrawal processed successfully")
        }
    }

    // --- Property Management ---
    fun addProperty(name: String, address: String, rentAmount: Double, bedrooms: Int, bathrooms: Int, description: String) {
        viewModelScope.launch {
            val prop = Property(
                name = name,
                address = address,
                rentAmount = rentAmount,
                bedrooms = bedrooms,
                bathrooms = bathrooms,
                description = description
            )
            repository.insertProperty(prop)

            repository.insertAlert(
                RentLinkAlert(
                    type = "Payment",
                    title = "New Property Added",
                    message = "Property '$name' created successfully at $address with rent BDT $rentAmount/mo.",
                    statusBadge = "Success",
                    timestamp = "Today"
                )
            )
        }
    }

    fun removeProperty(property: Property) {
        viewModelScope.launch {
            repository.deleteProperty(property)
        }
    }

    // --- Tenant Management ---
    fun addTenant(name: String, rentLinkId: String, phone: String, email: String, propertyId: Int?) {
        viewModelScope.launch {
            var propName: String? = null
            if (propertyId != null) {
                val prop = repository.getPropertyById(propertyId)
                propName = prop?.name
            }

            val tenant = Tenant(
                name = name,
                rentLinkId = rentLinkId,
                phone = phone,
                email = email,
                assignedPropertyId = propertyId,
                assignedPropertyName = propName,
                status = "Active"
            )
            val insertedId = repository.insertTenant(tenant).toInt()

            if (propertyId != null && propName != null) {
                val prop = repository.getPropertyById(propertyId)
                if (prop != null) {
                    val dateToday = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                    val dateNextYear = "2027-06-17" // Static projection for 1 year
                    repository.insertLease(
                        Lease(
                            propertyId = propertyId,
                            tenantId = insertedId,
                            tenantName = name,
                            propertyName = propName,
                            startDate = dateToday,
                            endDate = dateNextYear,
                            rentAmount = prop.rentAmount,
                            status = "Active"
                        )
                    )
                }
            }

            repository.insertAlert(
                RentLinkAlert(
                    type = "Payment",
                    title = "Tenant Assigned",
                    message = "Tenant '$name' added successfully and assigned to ${propName ?: "unassigned space"}.",
                    statusBadge = "Success",
                    timestamp = "Today"
                )
            )
        }
    }

    fun removeTenant(tenant: Tenant) {
        viewModelScope.launch {
            repository.deleteTenant(tenant)
        }
    }

    // --- Rent Collection & Lease Tracking ---
    fun collectRent(tenantId: Int, paymentMethod: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val t = repository.getTenantById(tenantId)
            if (t != null) {
                val prop = t.assignedPropertyId?.let { repository.getPropertyById(it) }
                val amount = prop?.rentAmount ?: 0.0
                
                // Add trans
                val dateStr = SimpleDateFormat("dd MMM 2026", Locale.getDefault()).format(Date())
                val tx = Transaction(
                    tenantName = t.name,
                    propertyName = t.assignedPropertyName ?: "Assigned Property",
                    amount = amount,
                    status = "Paid",
                    date = dateStr,
                    rentLinkId = t.rentLinkId,
                    paymentMethod = paymentMethod,
                    type = "Collection"
                )
                repository.insertTransaction(tx)

                // Increase landlord balance
                _availableBalance.value += amount

                // Add alert
                repository.insertAlert(
                    RentLinkAlert(
                        type = "Payment",
                        title = "Rent Paid via $paymentMethod",
                        message = "Tenant ${t.name} paid rent of BDT ${String.format("%,.2f", amount)} successfully for ${t.assignedPropertyName}.",
                        statusBadge = "Success",
                        timestamp = "Today"
                    )
                )

                recalculateFinancialStats()
                onComplete()
            }
        }
    }

    // --- Maintenance Management ---
    fun submitMaintenance(propertyId: Int, description: String, priority: String) {
        viewModelScope.launch {
            val prop = repository.getPropertyById(propertyId)
            val dateStr = SimpleDateFormat("dd MMM 2026", Locale.getDefault()).format(Date())
            
            val request = MaintenanceRequest(
                propertyId = propertyId,
                propertyName = prop?.name ?: "RentLink Property",
                tenantName = _currentUserName.value,
                description = description,
                status = "Pending",
                date = dateStr,
                priority = priority
            )
            repository.insertMaintenanceRequest(request)

            repository.insertAlert(
                RentLinkAlert(
                    type = "Maintenance",
                    title = "New Maintenance Opened",
                    message = "A new ${priority.uppercase()} priority maintenance ticket has been opened for ${prop?.name}.",
                    statusBadge = "Maintenance",
                    timestamp = "Today"
                )
            )
        }
    }

    fun resolveMaintenance(request: MaintenanceRequest) {
        viewModelScope.launch {
            val updated = request.copy(status = "Resolved")
            repository.updateMaintenanceRequest(updated)
            
            repository.insertAlert(
                RentLinkAlert(
                    type = "Maintenance",
                    title = "Maintenance Resolved",
                    message = "Maintenance ticket for ${request.propertyName} reported by ${request.tenantName} has been resolved.",
                    statusBadge = "Success",
                    timestamp = "Today"
                )
            )
        }
    }

    fun markAlertRead(id: Int) {
        viewModelScope.launch {
            repository.markAlertAsRead(id)
        }
    }

    fun deleteAlert(id: Int) {
        viewModelScope.launch {
            repository.deleteAlertById(id)
        }
    }
}

class RentLinkViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RentLinkViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RentLinkViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
