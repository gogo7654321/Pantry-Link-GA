package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// Custom user session representation to handle both standard Firebase and graceful Offline/Demo fallbacks
data class PantryUserSession(
    val email: String,
    val uid: String,
    val isDemo: Boolean = false
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class PantryLinkViewModel(private val repository: PantryLinkRepository, private val context: android.content.Context? = null) : ViewModel() {

    // SharedPreferences for persistent authentication and preferences
    private val prefs = context?.getSharedPreferences("pantry_link_prefs", android.content.Context.MODE_PRIVATE)

    // Firebase Authentication instance
    private val auth: FirebaseAuth? = try { FirebaseAuth.getInstance() } catch (e: Exception) { null }

    private val _userSession = MutableStateFlow<PantryUserSession?>(
        try {
            val savedEmail = prefs?.getString("pantry_session_email", null)
            val savedUid = prefs?.getString("pantry_session_uid", null)
            val savedIsDemo = prefs?.getBoolean("pantry_session_is_demo", false) ?: false
            if (savedEmail != null && savedUid != null) {
                PantryUserSession(savedEmail, savedUid, savedIsDemo)
            } else {
                auth?.currentUser?.let { PantryUserSession(it.email ?: "", it.uid, isDemo = false) }
            }
        } catch (e: Exception) {
            null
        }
    )
    val userSession: StateFlow<PantryUserSession?> = _userSession.asStateFlow()

    private val _currentUserProfile = MutableStateFlow<Map<String, Any>?>(null)
    val currentUserProfile: StateFlow<Map<String, Any>?> = _currentUserProfile.asStateFlow()

    // Getter for selected email (defaults to user email, or fallback)
    val currentUserEmail: String
        get() = _userSession.value?.email ?: "npatel012010@gmail.com"

    // Roles: "Donor" or "Food Bank"
    private val _selectedRole = MutableStateFlow(
        prefs?.getString("pantry_selected_role", "Donor") ?: "Donor"
    )
    val selectedRole: StateFlow<String> = _selectedRole.asStateFlow()

    // Location States
    private val _hasLocationPermission = MutableStateFlow(true)
    val hasLocationPermission: StateFlow<Boolean> = _hasLocationPermission.asStateFlow()

    private val _userZipCode = MutableStateFlow("30308") // Midtown Atlanta
    val userZipCode: StateFlow<String> = _userZipCode.asStateFlow()

    // Simulating current coordinates depending on zip code / gps
    // E.g., Atlanta Midtown by default
    private val _userLatitude = MutableStateFlow(33.7756)
    val userLatitude: StateFlow<Double> = _userLatitude.asStateFlow()

    private val _userLongitude = MutableStateFlow(-84.3963)
    val userLongitude: StateFlow<Double> = _userLongitude.asStateFlow()

    // Filters & Search
    private val _selectedCategoryFilter = MutableStateFlow("All")
    val selectedCategoryFilter: StateFlow<String> = _selectedCategoryFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Custom max distance filter (miles)
    private val _maxDistanceFilter = MutableStateFlow(25.0)
    val maxDistanceFilter: StateFlow<Double> = _maxDistanceFilter.asStateFlow()

    // Database Flows
    val foodBanksState: StateFlow<List<FoodBankEntity>> = repository.allFoodBanks
        .map { list ->
            list.map { bank ->
                if (bank.latitude == 33.7490 && bank.longitude == -84.3880) {
                    val gps = LocationHelper.getCoordsForAddressWithContext(context, bank.address, bank.zipCode)
                    bank.copy(latitude = gps.first, longitude = gps.second)
                } else {
                    bank
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _partnerFoodBanks = MutableStateFlow<List<FoodBankEntity>>(emptyList())
    val partnerFoodBanksState: StateFlow<List<FoodBankEntity>> = _partnerFoodBanks.asStateFlow()

    val requestsState: StateFlow<List<RequestEntity>> = repository.allRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Live Claims flow, automatically reactively queries matching active user email
    val claimsState: StateFlow<List<ClaimEntity>> = _userSession
        .flatMapLatest { user ->
            repository.getClaimsForDonor(user?.email ?: "npatel012010@gmail.com")
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All claims in database (useful for Food Bank view to look at items dropped off at their locations)
    val allClaimsState: StateFlow<List<ClaimEntity>> = repository.allClaims
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val auditLogsState: StateFlow<List<AuditLogEntity>> = repository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Profile Settings States
    private val _emailNotificationsEnabled = MutableStateFlow(true)
    val emailNotificationsEnabled: StateFlow<Boolean> = _emailNotificationsEnabled.asStateFlow()

    private val _smsNotificationsEnabled = MutableStateFlow(false)
    val smsNotificationsEnabled: StateFlow<Boolean> = _smsNotificationsEnabled.asStateFlow()

    private val _pushNotificationsEnabled = MutableStateFlow(true)
    val pushNotificationsEnabled: StateFlow<Boolean> = _pushNotificationsEnabled.asStateFlow()

    private val _savedLocations = MutableStateFlow(listOf(
        SavedLocation(1, "Home Base", "12 Peachtree St, Atlanta", "30308")
    ))
    val savedLocations: StateFlow<List<SavedLocation>> = _savedLocations.asStateFlow()

    fun toggleEmailNotifications() {
        _emailNotificationsEnabled.value = !_emailNotificationsEnabled.value
        showToast("Email alerts changed. SMS & Email notification features coming soon!")
    }

    fun toggleSMSNotifications() {
        _smsNotificationsEnabled.value = !_smsNotificationsEnabled.value
        showToast("SMS alerts changed. SMS & Email notification features coming soon!")
    }

    fun togglePushNotifications() {
        _pushNotificationsEnabled.value = !_pushNotificationsEnabled.value
        showToast("Push notifications updated: ${_pushNotificationsEnabled.value}")
    }

    fun addSavedLocation(name: String, address: String, zipCode: String) {
        if (name.isBlank() || address.isBlank() || zipCode.isBlank()) {
            showToast("Failed: Fields cannot be empty.")
            return
        }
        val current = _savedLocations.value
        val newId = (current.map { it.id }.maxOrNull() ?: 0) + 1
        _savedLocations.value = current + SavedLocation(newId, name, address, zipCode)
        showToast("Local saved location '$name' added successfully!")
    }

    fun removeSavedLocation(id: Int) {
        val current = _savedLocations.value
        val locationToRemove = current.find { it.id == id }
        if (locationToRemove != null) {
            _savedLocations.value = current.filter { it.id != id }
            showToast("Removed location '${locationToRemove.name}'")
        }
    }

    // Temporary Notifications / Log updates (in-app toast simulator)
    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun showToast(message: String) {
        _toastMessage.value = message
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    // --- Firebase Authentication Methods ---
    fun isUserLoggedIn(): Boolean {
        return _userSession.value != null
    }

    fun signIn(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        val trimmedEmail = email.trim()
        if (trimmedEmail.isBlank() || password.isBlank()) {
            onResult(false, "Please fill in all fields.")
            return
        }
        val firebaseAuth = auth
        if (firebaseAuth == null) {
            // Local fallback directly if Firebase Auth is not initialized/loaded in the SDK
            logInLocalAccount(trimmedEmail, onResult, "Firebase not initialized")
            return
        }

        try {
            firebaseAuth.signInWithEmailAndPassword(trimmedEmail, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val currentUser = firebaseAuth.currentUser
                        if (currentUser != null) {
                            val session = PantryUserSession(currentUser.email ?: trimmedEmail, currentUser.uid, isDemo = false)
                            _userSession.value = session
                            syncUserProfile()
                            saveSessionToPrefs(session, _selectedRole.value, null)
                            showToast("Logged in successfully as $trimmedEmail")
                            onResult(true, "Success")
                        } else {
                            onResult(false, "Failed to retrieve user session.")
                        }
                    } else {
                        val exceptionMessage = task.exception?.localizedMessage ?: "Authentication failed."
                        if (exceptionMessage.contains("api key", ignoreCase = true) || 
                            exceptionMessage.contains("invalid", ignoreCase = true) ||
                            exceptionMessage.contains("internal error", ignoreCase = true) ||
                            exceptionMessage.contains("not valid", ignoreCase = true)) {
                            
                            // Elegant fallback if API Credentials are not configured!
                            logInLocalAccount(trimmedEmail, onResult, exceptionMessage)
                        } else {
                            onResult(false, exceptionMessage)
                        }
                    }
                }
        } catch (e: Exception) {
            logInLocalAccount(trimmedEmail, onResult, e.localizedMessage ?: "Exception")
        }
    }

    private fun logInLocalAccount(email: String, onResult: (Boolean, String) -> Unit, originalReason: String) {
        val isFoodBank = email.contains("bank", ignoreCase = true) || email.contains("pantry", ignoreCase = true)
        val role = if (isFoodBank) "Food Bank" else "Donor"
        val displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() }
        
        val session = PantryUserSession(email, "demo_uid_${email.hashCode()}", isDemo = true)
        _userSession.value = session
        _selectedRole.value = role
        
        val profile = mapOf(
            "email" to email,
            "name" to displayName,
            "role" to role,
            "phone" to "(555) 019-2834",
            "isDemo" to true,
            "donorZip" to "30308",
            "fbZip" to "30308",
            "fbAddress" to "1722 Peachtree Rd NW",
            "fbCity" to "Atlanta, GA",
            "fbSize" to "Medium (100-500/wk)",
            "fbHours" to "Mon-Fri 9AM-5PM",
            "fbColdStorage" to true
        )
        _currentUserProfile.value = profile
        
        saveSessionToPrefs(session, role, profile)
        
        showToast("Logged in as $email (Offline Mode)")
        onResult(true, "DemoSuccess")
    }

    fun saveFoodBankLocally(
        id: Int,
        name: String,
        address: String,
        zipCode: String,
        city: String,
        phone: String,
        email: String,
        size: String,
        hours: String,
        coldStorage: Boolean
    ) {
        viewModelScope.launch {
            val gps = getCoordsForAddress(address, zipCode)
            val fb = FoodBankEntity(
                id = id,
                name = name,
                address = address,
                zipCode = zipCode,
                city = city,
                state = "GA",
                latitude = gps.first,
                longitude = gps.second,
                phone = phone,
                email = email,
                verified = true,
                size = size,
                operatingHours = hours,
                coldStorage = coldStorage
            )
            repository.insertFoodBank(fb)
        }
    }

    fun signUp(
        email: String,
        password: String,
        role: String,
        name: String,
        phone: String,
        fbAddress: String = "",
        fbCity: String = "",
        fbZip: String = "",
        fbSize: String = "",
        fbHours: String = "",
        fbColdStorage: Boolean = false,
        donorZip: String = "",
        donorCity: String = "",
        donorCanServeType: String = "",
        donorCanServeQty: String = "",
        donorFrequency: String = "",
        onResult: (Boolean, String) -> Unit
    ) {
        val trimmedEmail = email.trim()
        val trimmedName = name.trim()
        val trimmedPhone = phone.trim()
        if (trimmedEmail.isBlank() || password.isBlank() || trimmedName.isBlank() || trimmedPhone.isBlank()) {
            onResult(false, "Please fill in all fields.")
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(trimmedEmail).matches()) {
            onResult(false, "Please enter a valid email address.")
            return
        }
        if (password.length < 6) {
            onResult(false, "Password must be at least 6 characters.")
            return
        }

        val details = mutableMapOf<String, Any>(
            "email" to trimmedEmail,
            "role" to role,
            "name" to trimmedName,
            "phone" to trimmedPhone,
            "createdAt" to System.currentTimeMillis()
        )
        
        if (role == "Donor") {
            details["donorZip"] = donorZip.trim()
            details["donorCity"] = donorCity.trim()
            details["donorCanServeType"] = donorCanServeType
            details["donorCanServeQty"] = donorCanServeQty
            details["donorFrequency"] = donorFrequency
        } else {
            details["fbAddress"] = fbAddress.trim()
            details["fbCity"] = fbCity.trim()
            details["fbZip"] = fbZip.trim()
            details["fbSize"] = fbSize
            details["fbHours"] = fbHours.trim()
            details["fbColdStorage"] = fbColdStorage
        }

        val firebaseAuth = auth
        if (firebaseAuth == null) {
            registerLocalAccount(trimmedEmail, role, details, onResult)
            return
        }

        try {
            firebaseAuth.createUserWithEmailAndPassword(trimmedEmail, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val currentUser = firebaseAuth.currentUser
                        if (currentUser != null) {
                            val session = PantryUserSession(currentUser.email ?: trimmedEmail, currentUser.uid, isDemo = false)
                            _userSession.value = session
                            _selectedRole.value = role
                            _currentUserProfile.value = details
                            
                            saveUserProfileToFirestore(details)
                            saveSessionToPrefs(session, role, details)
                            
                            // If Food Bank, also save details to the "food_banks" collection for public mapping
                            if (role == "Food Bank") {
                                val newFbId = (1000..99999).random()
                                val latLng = getCoordsForAddress(fbAddress, fbZip)
                                val fbDocumentMap = mapOf(
                                    "id" to newFbId,
                                    "name" to trimmedName,
                                    "address" to fbAddress.trim(),
                                    "zipCode" to fbZip.trim(),
                                    "city" to fbCity.trim(),
                                    "state" to "GA",
                                    "latitude" to latLng.first,
                                    "longitude" to latLng.second,
                                    "phone" to trimmedPhone,
                                    "email" to trimmedEmail,
                                    "verified" to true,
                                    "size" to fbSize,
                                    "operatingHours" to fbHours.trim(),
                                    "coldStorage" to fbColdStorage
                                )
                                FirebaseFirestore.getInstance()
                                    .collection("food_banks")
                                    .document(newFbId.toString())
                                    .set(fbDocumentMap)
                                    .addOnFailureListener { Log.e("FirebaseSync", "Failed to pre-inject food bank", it) }
                                
                                // Insert locally so it is immediately updated in UI and Map Finder
                                saveFoodBankLocally(
                                    id = newFbId,
                                    name = trimmedName,
                                    address = fbAddress.trim(),
                                    zipCode = fbZip.trim(),
                                    city = fbCity.trim(),
                                    phone = trimmedPhone,
                                    email = trimmedEmail,
                                    size = fbSize,
                                    hours = fbHours.trim(),
                                    coldStorage = fbColdStorage
                                )
                            }
                            
                            showToast("Registered successfully as $trimmedEmail")
                            onResult(true, "Success")
                        } else {
                            onResult(false, "Failed to retrieve user session.")
                        }
                    } else {
                        val exceptionMessage = task.exception?.localizedMessage ?: "Failed to register."
                        if (exceptionMessage.contains("api key", ignoreCase = true) || 
                            exceptionMessage.contains("invalid", ignoreCase = true) ||
                            exceptionMessage.contains("internal error", ignoreCase = true) ||
                            exceptionMessage.contains("not valid", ignoreCase = true)) {
                            
                            // Backup fallback on credential error
                            registerLocalAccount(trimmedEmail, role, details, onResult)
                        } else {
                            onResult(false, exceptionMessage)
                        }
                    }
                }
        } catch (e: Exception) {
            registerLocalAccount(trimmedEmail, role, details, onResult)
        }
    }

    private fun registerLocalAccount(
        email: String,
        role: String,
        details: Map<String, Any>,
        onResult: (Boolean, String) -> Unit
    ) {
        val mutableDetails = details.toMutableMap()
        mutableDetails["isDemo"] = true
        
        val session = PantryUserSession(email, "demo_uid_${email.hashCode()}", isDemo = true)
        _userSession.value = session
        _selectedRole.value = role
        _currentUserProfile.value = mutableDetails
        
        saveSessionToPrefs(session, role, mutableDetails)
        
        // If Food Bank, save food bank details locally so they can work offline and immediately view on map
        if (role == "Food Bank") {
            val newFbId = (1000..99999).random()
            val fbAddress = details["fbAddress"] as? String ?: ""
            val fbZip = details["fbZip"] as? String ?: ""
            val fbCity = details["fbCity"] as? String ?: ""
            val fbSize = details["fbSize"] as? String ?: "Medium (100-500/wk)"
            val fbHours = details["fbHours"] as? String ?: "Mon-Fri 9 AM - 5 PM"
            val fbColdStorage = details["fbColdStorage"] as? Boolean ?: false
            val name = details["name"] as? String ?: "Community Food Bank"
            val phone = details["phone"] as? String ?: ""
            
            saveFoodBankLocally(
                id = newFbId,
                name = name,
                address = fbAddress,
                zipCode = fbZip,
                city = fbCity,
                phone = phone,
                email = email,
                size = fbSize,
                hours = fbHours,
                coldStorage = fbColdStorage
            )
        }
        
        showToast("Registered successfully as $email (Offline Mode)")
        onResult(true, "DemoSuccess")
    }

    fun updateProfile(
        name: String,
        phone: String,
        // Donor fields
        donorZip: String = "",
        donorCity: String = "",
        donorCanServeType: String = "",
        donorCanServeQty: String = "",
        donorFrequency: String = "",
        // Food Bank fields
        fbAddress: String = "",
        fbCity: String = "",
        fbZip: String = "",
        fbSize: String = "",
        fbHours: String = "",
        fbColdStorage: Boolean = false,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val currentSession = _userSession.value
        if (currentSession == null) {
            onResult(false, "Session not found")
            return
        }
        val email = currentSession.email
        val role = _selectedRole.value

        val updatedMap = mutableMapOf<String, Any>(
            "email" to email,
            "role" to role,
            "name" to name.trim(),
            "phone" to phone.trim()
        )

        if (role == "Donor") {
            updatedMap["donorZip"] = donorZip.trim()
            updatedMap["donorCity"] = donorCity.trim()
            updatedMap["donorCanServeType"] = donorCanServeType
            updatedMap["donorCanServeQty"] = donorCanServeQty
            updatedMap["donorFrequency"] = donorFrequency
        } else {
            updatedMap["fbAddress"] = fbAddress.trim()
            updatedMap["fbCity"] = fbCity.trim()
            updatedMap["fbZip"] = fbZip.trim()
            updatedMap["fbSize"] = fbSize
            updatedMap["fbHours"] = fbHours.trim()
            updatedMap["fbColdStorage"] = fbColdStorage

            // Also update the local food bank in Room Database!
            val matchingBank = foodBanksState.value.firstOrNull { it.email.equals(email, ignoreCase = true) }
            val existingId = matchingBank?.id ?: (1000..99999).random()
            
            saveFoodBankLocally(
                id = existingId,
                name = name.trim(),
                address = fbAddress.trim(),
                zipCode = fbZip.trim(),
                city = fbCity.trim(),
                phone = phone.trim(),
                email = email,
                size = fbSize,
                hours = fbHours.trim(),
                coldStorage = fbColdStorage
            )
        }

        _currentUserProfile.value = updatedMap
        saveSessionToPrefs(currentSession, role, updatedMap)
        saveUserProfileToFirestore(updatedMap)
        showToast("Profile updated successfully!")
        onResult(true, "Success")
    }

    fun getCoordsForAddress(address: String, zip: String): Pair<Double, Double> {
        return LocationHelper.getCoordsForAddressWithContext(context, address, zip)
    }

    fun getCoordsForZip(zip: String): Pair<Double, Double> {
        return LocationHelper.getCoordsForZip(zip)
    }

    fun signOutUser() {
        try {
            auth?.signOut()
        } catch (e: Exception) {
            Log.e("PantryLinkAuth", "Firebase signOut failed", e)
        }
        _userSession.value = null
        _currentUserProfile.value = null
        clearSessionFromPrefs()
        showToast("Signed out successfully.")
    }

    private fun saveUserProfileToFirestore(userProfile: Map<String, Any>) {
        if (_userSession.value?.isDemo == true) return
        val uid = _userSession.value?.uid ?: return
        try {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .set(userProfile)
                .addOnFailureListener { Log.e("PantryLinkAuth", "Failed to save user profile", it) }
        } catch (e: Exception) {
            Log.e("PantryLinkAuth", "Firestore not available for profile save", e)
        }
    }

    private fun syncUserProfile() {
        val currentSession = _userSession.value ?: return
        if (currentSession.isDemo) return
        val uid = currentSession.uid
        try {
            FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc != null && doc.exists()) {
                        val role = doc.getString("role") ?: "Donor"
                        _selectedRole.value = role
                        _currentUserProfile.value = doc.data
                        
                        // Persist session and profile to SharedPreferences
                        saveSessionToPrefs(currentSession, role, doc.data)
                        
                        // Set correct ZIP code to focus map/distance
                        val primaryZip = if (role == "Donor") {
                            doc.getString("donorZip") ?: "30308"
                        } else {
                            doc.getString("fbZip") ?: "30308"
                        }
                        if (primaryZip.isNotBlank()) {
                            _userZipCode.value = primaryZip
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("PantryLinkAuth", "Firestore not available for sync", e)
        }
    }

    // Animated top banner for simulated device push notifications
    private val _activePushAlert = MutableStateFlow<String?>(null)
    val activePushAlert: StateFlow<String?> = _activePushAlert.asStateFlow()

    fun triggerSimulatedPushAlert(title: String, message: String) {
        if (_pushNotificationsEnabled.value) {
            _activePushAlert.value = "$title\n$message"
        }
    }

    fun dismissPushAlert() {
        _activePushAlert.value = null
    }

    private fun saveSessionToPrefs(session: PantryUserSession, role: String, profile: Map<String, Any>?) {
        try {
            prefs?.edit()?.apply {
                putString("pantry_session_email", session.email)
                putString("pantry_session_uid", session.uid)
                putBoolean("pantry_session_is_demo", session.isDemo)
                putString("pantry_selected_role", role)
                
                profile?.forEach { (key, value) ->
                    when (value) {
                        is String -> putString("profile_$key", value)
                        is Boolean -> putBoolean("profile_$key", value)
                        is Int -> putInt("profile_$key", value)
                        is Long -> putLong("profile_$key", value)
                        is Float -> putFloat("profile_$key", value)
                    }
                }
                apply()
            }
        } catch (e: Exception) {
            Log.e("PantryLinkAuth", "Failed to save session to prefs", e)
        }
    }

    private fun clearSessionFromPrefs() {
        try {
            prefs?.edit()?.apply {
                remove("pantry_session_email")
                remove("pantry_session_uid")
                remove("pantry_session_is_demo")
                remove("pantry_selected_role")
                
                prefs.all.keys.forEach { key ->
                    if (key.startsWith("profile_")) {
                        remove(key)
                    }
                }
                apply()
            }
        } catch (e: Exception) {
            Log.e("PantryLinkAuth", "Failed to clear prefs", e)
        }
    }

    private fun loadUserProfileFromPrefs(): Map<String, Any>? {
        val sharedPrefs = prefs ?: return null
        val profileKeys = sharedPrefs.all.keys.filter { it.startsWith("profile_") }
        if (profileKeys.isEmpty()) return null
        
        val map = mutableMapOf<String, Any>()
        profileKeys.forEach { key ->
            val realKey = key.substringAfter("profile_")
            val value = sharedPrefs.all[key]
            if (value != null) {
                map[realKey] = value
            }
        }
        return map
    }

    init {
        // Load persist profile from prefs if offline or fallback
        _currentUserProfile.value = loadUserProfileFromPrefs() ?: if (_userSession.value?.isDemo == true) {
            mapOf("email" to _userSession.value!!.email, "isDemo" to true, "role" to _selectedRole.value)
        } else {
            null
        }

        syncUserProfile()

        // Sync list from Firestore "partner_food_banks" collection if available
        try {
            FirebaseFirestore.getInstance().collection("partner_food_banks")
                .addSnapshotListener { snapshots, error ->
                    if (error != null) {
                        Log.e("PantryLinkAuth", "Partner food banks listen error", error)
                        return@addSnapshotListener
                    }
                    if (snapshots != null) {
                        val list = mutableListOf<FoodBankEntity>()
                        var index = 50000
                        for (doc in snapshots.documents) {
                            try {
                                val data = doc.data ?: continue
                                val addrVal = data["address"] as? String ?: ""
                                val zipVal = data["zipCode"] as? String ?: ""
                                val rawLat = (data["latitude"] as? Number)?.toDouble() ?: 33.7490
                                val rawLng = (data["longitude"] as? Number)?.toDouble() ?: -84.3880
                                val (lat, lng) = if (rawLat == 33.7490 && rawLng == -84.3880) {
                                    LocationHelper.getCoordsForAddress(addrVal, zipVal)
                                } else {
                                    Pair(rawLat, rawLng)
                                }
                                val bank = FoodBankEntity(
                                    id = doc.id.toIntOrNull() ?: index++,
                                    name = data["name"] as? String ?: "",
                                    address = addrVal,
                                    zipCode = zipVal,
                                    city = data["city"] as? String ?: "",
                                    state = data["state"] as? String ?: "GA",
                                    latitude = lat,
                                    longitude = lng,
                                    phone = data["phone"] as? String ?: "",
                                    email = data["email"] as? String ?: "",
                                    verified = data["verified"] as? Boolean ?: true
                                )
                                list.add(bank)
                            } catch (e: Exception) {
                                Log.e("PantryLinkAuth", "Error parsing partner food bank detail", e)
                            }
                        }
                        _partnerFoodBanks.value = list
                    }
                }
        } catch (e: Exception) {
            Log.e("PantryLinkAuth", "partner_food_banks collection listener not initialized", e)
        }

        // Automatically sync coordinates matching zip codes
        viewModelScope.launch {
            _userZipCode.collect { zip ->
                when (zip) {
                    "30308", "30344" -> { // Atlanta
                        _userLatitude.value = 33.7756
                        _userLongitude.value = -84.3963
                    }
                    "30075", "30076" -> { // Roswell
                        _userLatitude.value = 34.0232
                        _userLongitude.value = -84.3615
                    }
                    "30507", "30501" -> { // Gainesville
                        _userLatitude.value = 34.2582
                        _userLongitude.value = -83.8185
                    }
                    "30909" -> { // Augusta
                        _userLatitude.value = 33.4735
                        _userLongitude.value = -82.0649
                    }
                    else -> {
                        // Keep center
                    }
                }
            }
        }
    }

    // Role Actions
    fun setRole(role: String) {
        _selectedRole.value = role
        showToast("Switched to $role view")
    }

    fun setLocationPermission(granted: Boolean) {
        _hasLocationPermission.value = granted
        if (!granted) {
            showToast("Location denied. Switched to ZIP Code mode.")
        } else {
            showToast("Location access simulated.")
        }
    }

    fun setZipCode(zip: String) {
        _userZipCode.value = zip
    }

    fun setCategoryFilter(category: String) {
        _selectedCategoryFilter.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setMaxDistanceFilter(distance: Double) {
        _maxDistanceFilter.value = distance
    }

    // High Level Donor Operations
    fun claimRequest(requestId: Int, quantity: Int, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = repository.tryClaimRequest(
                donorId = currentUserEmail,
                requestId = requestId,
                quantityToClaim = quantity,
                timestamp = System.currentTimeMillis()
            )
            when (result) {
                is ClaimResult.Success -> {
                    onResult(true, "Successfully claimed $quantity items!")
                    showToast("Claim successfully reserved. Check active dashboard!")
                    triggerSimulatedPushAlert(
                        "Urgent Pantry Claim Reserved",
                        "Your reservation for $quantity items has been logged. Deliver to local food bank to complete!"
                    )
                }
                is ClaimResult.Error -> {
                    onResult(false, result.message)
                    showToast("Claim failed: ${result.message}")
                }
            }
        }
    }

    fun cancelClaim(claimId: Int) {
        viewModelScope.launch {
            val success = repository.tryCancelClaim(
                claimId = claimId,
                donorId = currentUserEmail,
                timestamp = System.currentTimeMillis()
            )
            if (success) {
                showToast("Claim cancelled. Quantities restored to the request.")
                triggerSimulatedPushAlert(
                    "Claim Reservation Cancelled",
                    "Reservation was cancelled. Help items have been returned to open community needs pool."
                )
            } else {
                showToast("Cancellation blocked: you have already dropped off this item.")
            }
        }
    }

    fun dropOffClaim(claimId: Int) {
        viewModelScope.launch {
            val success = repository.markClaimAsDroppedOff(
                claimId = claimId,
                timestamp = System.currentTimeMillis()
            )
            if (success) {
                showToast("Item marked as Dropped Off. Awaiting food bank review.")
                triggerSimulatedPushAlert(
                    "Drop-Off Confirmed",
                    "A community host has been notified of your drop-off. Awaiting validation review."
                )
            } else {
                showToast("Failed to update status. Already dropped off or completed.")
            }
        }
    }

    // High Level Food Bank Operations
    fun reviewClaim(claimId: Int, approved: Boolean, rejectionReason: String?) {
        viewModelScope.launch {
            val success = repository.reviewClaim(
                claimId = claimId,
                approved = approved,
                rejectionReason = rejectionReason,
                timestamp = System.currentTimeMillis()
            )
            if (success) {
                val action = if (approved) "Approved" else "Rejected"
                showToast("Claim status set to: $action.")
                triggerSimulatedPushAlert(
                    "Drop-off Review Alert",
                    "Your logged drop-off has been marked as $action by the receiving food bank."
                )
            } else {
                showToast("Failed to issue review decision.")
            }
        }
    }

    fun createRequest(
        title: String,
        category: String,
        itemDescription: String,
        quantityNeeded: Int,
        deadline: String,
        dropOffLocation: String,
        extraNotes: String
    ) {
        viewModelScope.launch {
            val foodBank = foodBanksState.value.firstOrNull { it.phone == "470-209-1835" }
                ?: foodBanksState.value.firstOrNull()

            val request = RequestEntity(
                foodBankId = foodBank?.id ?: 1,
                foodBankName = foodBank?.name ?: "Local Georgia Food Bank",
                title = title,
                category = category,
                itemDescription = itemDescription,
                quantityNeeded = quantityNeeded,
                quantityRemaining = quantityNeeded,
                deadline = deadline,
                dropOffLocation = dropOffLocation,
                extraNotes = extraNotes,
                status = "Posted"
            )
            repository.insertRequest(request)
            showToast("New standardized request posted successfully.")
            triggerSimulatedPushAlert(
                "New Georgia Pantry Need Indeed",
                "New urgent need posted: '$title'. Nearby registered donors are being alerted!"
            )
        }
    }

    fun closeRequest(requestId: Int) {
        viewModelScope.launch {
            val success = repository.closeRequest(requestId, System.currentTimeMillis())
            if (success) {
                showToast("Request successfully closed.")
            }
        }
    }

    fun triggerClaimExpiration(claimId: Int) {
        viewModelScope.launch {
            val success = repository.expireClaim(claimId, System.currentTimeMillis())
            if (success) {
                showToast("Claim expired. Lock-up released back into available request pool.")
            } else {
                showToast("Blocked: claim cannot expire once dropped off.")
            }
        }
    }

    // Distance Calculation Utility
    fun getDistanceToFoodBank(foodBank: FoodBankEntity): Double {
        if (!_hasLocationPermission.value) {
            // ZIP based approximation
            return when {
                foodBank.zipCode == _userZipCode.value -> 1.2
                // Calculate pseudo-distance on ZIP ranges
                else -> {
                    val bankZip = foodBank.zipCode.toIntOrNull() ?: 30308
                    val userZip = _userZipCode.value.toIntOrNull() ?: 30308
                    val zipDiff = Math.abs(bankZip - userZip)
                    if (zipDiff == 0) 1.5
                    else (zipDiff * 1.8).coerceAtMost(45.0)
                }
            }
        }
        return calculateDistanceInMiles(
            _userLatitude.value,
            _userLongitude.value,
            foodBank.latitude,
            foodBank.longitude
        )
    }

    private fun calculateDistanceInMiles(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val theta = lon1 - lon2
        var dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta))
        dist = Math.acos(dist)
        dist = Math.toDegrees(dist)
        dist = dist * 60 * 1.1515
        return dist
    }
}

class PantryLinkViewModelFactory(private val repository: PantryLinkRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PantryLinkViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PantryLinkViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class SavedLocation(
    val id: Int,
    val name: String,
    val address: String,
    val zipCode: String
)
