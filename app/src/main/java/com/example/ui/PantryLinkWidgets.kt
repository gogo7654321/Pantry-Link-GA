package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.BuildConfig
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.launch
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.composed
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PantryLinkAppScreen(viewModel: PantryLinkViewModel) {
    val selectedRole by viewModel.selectedRole.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()
    val userSession by viewModel.userSession.collectAsStateWithLifecycle()
    val activePushAlert by viewModel.activePushAlert.collectAsStateWithLifecycle()
    var showTermsDialog by rememberSaveable { mutableStateOf(false) }

    if (showTermsDialog) {
        TermsOfServiceDialog(onDismiss = { showTermsDialog = false })
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Host Scaffold
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.tertiary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Column {
                                Text(
                                    text = "PantryLink",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 21.sp,
                                    fontFamily = FontFamily.SansSerif,
                                    color = MaterialTheme.colorScheme.primary,
                                    style = androidx.compose.ui.text.TextStyle(
                                        letterSpacing = (-0.5).sp
                                    )
                                )
                                Text(
                                    text = "GEORGIA COMMUNITY COLLABORATIVE",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.tertiary,
                                    style = androidx.compose.ui.text.TextStyle(
                                        letterSpacing = 1.2.sp
                                    )
                                )
                            }
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showTermsDialog = true },
                            modifier = Modifier.testTag("top_terms_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = "Terms of Service",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        if (userSession != null) {
                            // Pill selector for Simulator roles and Sign Out icon
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                RoleSelector(
                                    selectedRole = selectedRole,
                                    onRoleSelected = { viewModel.setRole(it) }
                                )

                                IconButton(
                                    onClick = { viewModel.signOutUser() },
                                    modifier = Modifier.testTag("top_logout_icon")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Logout,
                                        contentDescription = "Sign Out",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            bottomBar = {
                // Toast / Banner Simulator
                toastMessage?.let {
                    Snackbar(
                        modifier = Modifier
                            .padding(12.dp)
                            .testTag("app_snackbar"),
                        action = {
                            TextButton(onClick = { viewModel.clearToast() }) {
                                Text("OK", color = MaterialTheme.colorScheme.primaryContainer)
                            }
                        }
                    ) {
                        Text(text = it, fontSize = 14.sp)
                    }
                }
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (userSession == null) {
                    PantryLinkAuthGateScreen(viewModel = viewModel, onViewTerms = { showTermsDialog = true })
                } else {
                    // Main content based on selected role
                    if (selectedRole == "Donor") {
                        DonorWorkspace(viewModel = viewModel)
                    } else {
                        FoodBankWorkspace(viewModel = viewModel)
                    }
                }
            }
        }

        // Real-Time Simulated Push Alert Overlay Card
        activePushAlert?.let { alertString ->
            val lines = alertString.split("\n", limit = 2)
            val alertTitle = lines.getOrNull(0) ?: "PantryLink Georgia"
            val alertMsg = lines.getOrNull(1) ?: ""

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 52.dp)
                    .testTag("simulated_push_alert_overlay"),
                contentAlignment = Alignment.TopCenter
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.dismissPushAlert() },
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF0F172A), // Charcoal Navy slate background
                        contentColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                    shape = RoundedCornerShape(14.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(14.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF1E293B),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Active Push Alert Icon",
                                    tint = Color(0xFF10B981), // Emerald Accent Color
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = alertTitle,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = "now",
                                    fontSize = 10.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = alertMsg,
                                fontSize = 12.sp,
                                color = Color(0xFFCBD5E1),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 16.sp
                            )
                        }
                        IconButton(
                            onClick = { viewModel.dismissPushAlert() },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close push banner",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Role Selector pill UI with beautiful micro-interactions and transitions
@Composable
fun RoleSelector(
    selectedRole: String,
    onRoleSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(end = 12.dp)
            .height(38.dp)
            .clip(RoundedCornerShape(19.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)), CircleShape)
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val roles = listOf("Donor", "Food Bank")
        roles.forEach { role ->
            val isSelected = selectedRole == role
            val targetContainerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
            val targetTextColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant

            val animatedBgColor by animateColorAsState(
                targetValue = targetContainerColor,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "role_tab_bg"
            )
            val animatedTextColor by animateColorAsState(
                targetValue = targetTextColor,
                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                label = "role_tab_text"
            )

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(17.dp))
                    .background(animatedBgColor)
                    .clickable { onRoleSelected(role) }
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = if (role == "Donor") Icons.Default.VolunteerActivism else Icons.Default.MapsHomeWork,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                    Text(
                        text = role,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = animatedTextColor
                    )
                }
            }
        }
    }
}

// ==========================================
// DONOR WORKSPACE
// ==========================================
@Composable
fun DonorWorkspace(viewModel: PantryLinkViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Dashboard", "Browse Needs", "Map Finder", "My Contacts & Claims")

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(tabs.size) { index ->
                    val title = tabs[index]
                    val isSelected = selectedTab == index
                    val icon = when (index) {
                        0 -> Icons.Default.Favorite
                        1 -> Icons.Default.VolunteerActivism
                        2 -> Icons.Default.Explore
                        else -> Icons.Default.Bookmark
                    }

                    val containerColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "donor_tab_bg"
                    )
                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "donor_tab_text"
                    )
                    val borderStroke = if (isSelected) {
                        androidx.compose.foundation.BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                    } else {
                        androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    }

                    Surface(
                        onClick = { selectedTab = index },
                        shape = RoundedCornerShape(16.dp),
                        color = containerColor,
                        contentColor = contentColor,
                        border = borderStroke,
                        modifier = Modifier.height(38.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                style = androidx.compose.ui.text.TextStyle(
                                    letterSpacing = 0.2.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (selectedTab) {
                0 -> DonorDashboardTab(viewModel = viewModel, onBrowseClick = { selectedTab = 1 })
                1 -> DonorBrowseRequestsTab(viewModel = viewModel)
                2 -> DonorMapTab(viewModel = viewModel)
                3 -> DonorClaimsTab(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun DonorDashboardTab(viewModel: PantryLinkViewModel, onBrowseClick: () -> Unit) {
    val requests by viewModel.requestsState.collectAsStateWithLifecycle()
    val foodBanks by viewModel.foodBanksState.collectAsStateWithLifecycle()
    val claims by viewModel.claimsState.collectAsStateWithLifecycle()

    val openRequests = requests.filter { it.status != "Closed" && it.status != "Confirmed by Food Bank" }
    val urgentRequests = openRequests.take(3)
    val activeClaimsCount = claims.count { it.claimStatus == "Claimed" || it.claimStatus == "Ready for Drop-Off" }

    var showClaimDialogForRequest by remember { mutableStateOf<RequestEntity?>(null) }
    var claimQuantityText by remember { mutableStateOf("") }
    var claimErrorString by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome Header
        item {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("welcome_card"),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        text = "Hello, Neil Patel!",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "PantryLink connects you directly to vetted Georgia food pantries. Browse active needs, commit to bring supplies, and coordinate drop-off.",
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White.copy(alpha = 0.12f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "${openRequests.size}",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(text = "Active Needs", fontSize = 11.sp, color = Color.White.copy(alpha = 0.75f))
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color.White.copy(alpha = 0.12f),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "$activeClaimsCount",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFBBF24)
                                )
                                Text(text = "Uncompleted Claims", fontSize = 11.sp, textAlign = TextAlign.Center, color = Color.White.copy(alpha = 0.75f))
                            }
                        }
                    }
                }
            }
        }

        // Section: Urgent Requests
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolunteerActivism,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Urgent Needs Nearby",
                        fontFamily = FontFamily.SansSerif,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                TextButton(onClick = onBrowseClick) {
                    Text("See All Needs", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        if (urgentRequests.isEmpty()) {
            item {
                EmptyStateCard(message = "No active item requests nearby right now. Standardize a new request in Food Bank view to test!")
            }
        } else {
            items(urgentRequests) { request ->
                val foodBank = foodBanks.find { it.id == request.foodBankId }
                val distance = foodBank?.let { viewModel.getDistanceToFoodBank(it) } ?: 0.0

                ItemRequestCard(
                    request = request,
                    distance = distance,
                    onClaimClick = {
                        showClaimDialogForRequest = request
                        clampDefaultClaimQuantity(request) { value -> claimQuantityText = value }
                        claimErrorString = null
                    }
                )
            }
        }

        // Section: Nearby Food Banks list
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.HomeWork,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Registered Food Banks & Agencies",
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        if (foodBanks.isEmpty()) {
            item {
                EmptyStateCard(message = "No registered food banks in your area yet. Newly registered food bank accounts will appear here automatically.")
            }
        }

        items(foodBanks) { bank ->
            val distance = viewModel.getDistanceToFoodBank(bank)
            val bankRequests = requests.filter { it.foodBankId == bank.id && it.status != "Closed" }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp, topStart = 6.dp, topEnd = 24.dp),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFF0EBE3)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("foodbank_card_${bank.id}")
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left: Circle indicator with Georgia green
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HomeWork,
                            contentDescription = "Food Bank Location",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = bank.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            if (bank.verified) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Vetted Partner",
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = bank.address + ", " + bank.city,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsCar,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = "${String.format("%.1f", distance)} mi away",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Inventory,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(11.dp)
                                )
                                Text(
                                    text = "${bankRequests.size} open requests",
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // A. View & B. Accept claim Dialog popup
    showClaimDialogForRequest?.let { request ->
        AlertDialog(
            onDismissRequest = { showClaimDialogForRequest = null },
            title = {
                Text(
                    text = "Claim Assistance Request",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Info blocks
                    ClaimInfoItem(label = "Title", value = request.title)
                    ClaimInfoItem(label = "Category", value = request.category)
                    ClaimInfoItem(label = "Vetted Requirements", value = request.itemDescription)
                    ClaimInfoItem(label = "Pantry", value = request.foodBankName)
                    ClaimInfoItem(label = "Drop-Off", value = request.dropOffLocation)
                    ClaimInfoItem(label = "Deadline Date", value = request.deadline)
                    if (request.extraNotes.isNotEmpty()) {
                        ClaimInfoItem(label = "Special Notes", value = request.extraNotes)
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Needed Today", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            Text(text = "${request.quantityNeeded} units", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Remaining", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            Text(
                                text = "${request.quantityRemaining} remaining",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Enter amount you commit to bring:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = claimQuantityText,
                        onValueChange = {
                            claimQuantityText = it
                            claimErrorString = null
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("dashboard_claim_quantity_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        placeholder = { Text("How many items will you purchase/provide?") }
                    )

                    claimErrorString?.let {
                        Text(
                            text = "⚠️ $it",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = "Note: Once you accept, these items are reserved. You must deliver them to the pantry in person before the deadline.",
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = claimQuantityText.toIntOrNull()
                        if (qty == null || qty <= 0) {
                            claimErrorString = "Please enter a valid amount greater than 0."
                        } else if (qty > request.quantityRemaining) {
                            claimErrorString = "You cannot claim more than the remaining quantity (${request.quantityRemaining})."
                        } else {
                            // Run Room transaction
                            viewModel.claimRequest(
                                requestId = request.id,
                                quantity = qty
                            ) { isSuccess, msg ->
                                if (isSuccess) {
                                    showClaimDialogForRequest = null
                                } else {
                                    claimErrorString = msg
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("dashboard_confirm_claim_btn")
                ) {
                    Text("Reserve & Accept")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClaimDialogForRequest = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonorBrowseRequestsTab(viewModel: PantryLinkViewModel) {
    val requests by viewModel.requestsState.collectAsStateWithLifecycle()
    val foodBanks by viewModel.foodBanksState.collectAsStateWithLifecycle()
    val categoryFilter by viewModel.selectedCategoryFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()

    var showClaimDialogForRequest by remember { mutableStateOf<RequestEntity?>(null) }
    var claimQuantityText by remember { mutableStateOf("") }
    var claimErrorString by remember { mutableStateOf<String?>(null) }

    // Categories List
    val categories = listOf("All", "Canned Foods", "Hygiene Products", "Baby Supplies", "School Supplies", "Shelf-Stable Items")

    // Filter requests
    val filteredRequests = requests.filter { req ->
        val matchesCategory = categoryFilter == "All" || req.category == categoryFilter
        val matchesSearch = req.title.contains(searchQuery, ignoreCase = true) ||
                req.itemDescription.contains(searchQuery, ignoreCase = true) ||
                req.foodBankName.contains(searchQuery, ignoreCase = true)
        val notClosed = req.status != "Closed" && req.status != "Confirmed by Food Bank"
        matchesCategory && matchesSearch && notClosed
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search & Filter header
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Outlined Search Text field with hospitable custom colors
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search food banks, items, cities...", fontSize = 14.sp) },
                leadingIcon = { 
                    Icon(
                        Icons.Default.Search, 
                        contentDescription = "Search", 
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    ) 
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search", modifier = Modifier.size(18.dp))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("donor_search_bar"),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color(0xFFF0EBE3),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                ),
                singleLine = true
            )

            // Category scrolling list with custom warm chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = categoryFilter == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.setCategoryFilter(cat) },
                        label = { 
                            Text(
                                text = cat, 
                                fontSize = 11.5.sp, 
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ) 
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White,
                            containerColor = Color.White,
                            labelColor = MaterialTheme.colorScheme.secondary
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) Color.Transparent else Color(0xFFF0EBE3),
                            selectedBorderColor = Color.Transparent,
                            borderWidth = 1.dp,
                            selectedBorderWidth = 1.dp
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }
        }

        // Active listing
        if (filteredRequests.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Inbox,
                        contentDescription = "None found",
                        tint = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No matching active requests.",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Try clearing search or picking another category.",
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredRequests) { req ->
                    val foodBank = foodBanks.find { it.id == req.foodBankId }
                    val distance = foodBank?.let { viewModel.getDistanceToFoodBank(it) } ?: 0.0

                    ItemRequestCard(
                        request = req,
                        distance = distance,
                        onClaimClick = {
                            showClaimDialogForRequest = req
                            clampDefaultClaimQuantity(req) { value -> claimQuantityText = value }
                            claimErrorString = null
                        }
                    )
                }
            }
        }
    }

    // A. View & B. Accept claim Dialog popup
    showClaimDialogForRequest?.let { request ->
        AlertDialog(
            onDismissRequest = { showClaimDialogForRequest = null },
            title = {
                Text(
                    text = "Claim Assistance Request",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Info blocks
                    ClaimInfoItem(label = "Title", value = request.title)
                    ClaimInfoItem(label = "Category", value = request.category)
                    ClaimInfoItem(label = "Vetted Requirements", value = request.itemDescription)
                    ClaimInfoItem(label = "Pantry", value = request.foodBankName)
                    ClaimInfoItem(label = "Drop-Off", value = request.dropOffLocation)
                    ClaimInfoItem(label = "Deadline Date", value = request.deadline)
                    if (request.extraNotes.isNotEmpty()) {
                        ClaimInfoItem(label = "Special Notes", value = request.extraNotes)
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = "Needed Today", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            Text(text = "${request.quantityNeeded} units", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "Remaining", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            Text(
                                text = "${request.quantityRemaining} remaining",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Enter amount you commit to bring:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    OutlinedTextField(
                        value = claimQuantityText,
                        onValueChange = {
                            claimQuantityText = it
                            claimErrorString = null
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("claim_quantity_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true,
                        placeholder = { Text("How many items will you purchase/provide?") }
                    )

                    claimErrorString?.let {
                        Text(
                            text = "⚠️ $it",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = "Note: Once you accept, these items are reserved. You must deliver them to the pantry in person before the deadline.",
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = claimQuantityText.toIntOrNull()
                        if (qty == null || qty <= 0) {
                            claimErrorString = "Please enter a valid amount greater than 0."
                        } else if (qty > request.quantityRemaining) {
                            claimErrorString = "You cannot claim more than the remaining quantity (${request.quantityRemaining})."
                        } else {
                            // Run Room transaction
                            viewModel.claimRequest(
                                requestId = request.id,
                                quantity = qty
                            ) { isSuccess, msg ->
                                if (isSuccess) {
                                    showClaimDialogForRequest = null
                                } else {
                                    claimErrorString = msg
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("confirm_claim_btn")
                ) {
                    Text("Reserve & Accept")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClaimDialogForRequest = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

fun clampDefaultClaimQuantity(req: RequestEntity, onResult: (String) -> Unit) {
    val defaultVal = if (req.quantityRemaining < 5) req.quantityRemaining else 5
    onResult(defaultVal.toString())
}

@Composable
fun ClaimInfoItem(label: String, value: String) {
    Column {
        Text(text = label.uppercase(Locale.getDefault()), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
        Text(text = value, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
    }
}

// Custom simulated SVG/Canvas location matching locator
@Composable
fun DonorMapTab(viewModel: PantryLinkViewModel) {
    val foodBanks by viewModel.foodBanksState.collectAsStateWithLifecycle()
    val hasPermission by viewModel.hasLocationPermission.collectAsStateWithLifecycle()
    val zipCode by viewModel.userZipCode.collectAsStateWithLifecycle()

    var selectedFBOnMap by remember { mutableStateOf<FoodBankEntity?>(null) }
    val requests by viewModel.requestsState.collectAsStateWithLifecycle()

    val atlantaPosition = remember { LatLng(33.7490, -84.3880) }
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(atlantaPosition, 9.5f)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Upper Controls: Status & Radius
        Card(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp, topStart = 6.dp, topEnd = 24.dp),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFF0EBE3)),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Georgia Proximity Simulator",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.SansSerif,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "SIMULATE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(end = 4.dp))
                        Switch(
                            checked = hasPermission,
                            onCheckedChange = { viewModel.setLocationPermission(it) },
                            modifier = Modifier.testTag("gps_switch")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (hasPermission) {
                    Text(
                        text = "Matching active pantries using simulated device GPS in Atlanta Midtown.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = zipCode,
                            onValueChange = { viewModel.setZipCode(it) },
                            label = { Text("Enter ZIP Code") },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .testTag("map_zip_input"),
                            shape = RoundedCornerShape(10.dp)
                        )
                        Button(
                            onClick = { viewModel.showToast("Radius filtered around ZIP $zipCode") },
                            modifier = Modifier.height(48.dp)
                        ) {
                            Text("Match")
                        }
                    }
                    Text(
                        text = "GPS permission denied. Radius calculated using Georgian ZIP ranges.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        // Registered Pantries Quick Jump / Selection Row
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "Registered Pantries (Tap to Focus):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 4.dp)
            ) {
                items(foodBanks) { bank ->
                    val isSelected = selectedFBOnMap?.id == bank.id
                    Card(
                        onClick = {
                            selectedFBOnMap = bank
                            cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(bank.latitude, bank.longitude), 12.5f)
                            viewModel.showToast("Focused Map on ${bank.name}")
                        },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .width(160.dp)
                            .testTag("quick_jump_fb_${bank.id}")
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = bank.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${bank.city}, ${bank.state}",
                                fontSize = 10.5.sp,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Real Google Map showing registered food banks in Georgia
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 12.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                // Pin Each Registered Food Bank Location with Custom Brand Logo Markers!
                foodBanks.forEach { bank ->
                    MarkerComposable(
                        state = MarkerState(position = LatLng(bank.latitude, bank.longitude)),
                        title = bank.name,
                        onClick = {
                            selectedFBOnMap = bank
                            viewModel.showToast("Selected ${bank.name}")
                            false // Center camera & open info window naturally
                        }
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(
                                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.tertiary
                                            )
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .border(2.dp, Color.White, RoundedCornerShape(12.dp))
                                    .padding(4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = bank.name,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            // Small map pin triangle pointing exactly at coordinates
                            Box(
                                modifier = Modifier
                                    .offset(y = (-4).dp)
                                    .size(10.dp)
                                    .rotate(45f)
                                    .background(MaterialTheme.colorScheme.tertiary)
                                    .border(1.dp, Color.White)
                            )
                        }
                    }
                }
            }

            // Live status badge overlay
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.92f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(Color(0xFF2E7D32), CircleShape)
                    )
                    Text(
                        text = "Live Google Map Active",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Selected Food Bank Detail display
        selectedFBOnMap?.let { bank ->
            val bankRequests = requests.filter { it.foodBankId == bank.id && it.status != "Closed" }

            Card(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
                    .testTag("map_detail_drawer"),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = bank.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(text = "${bank.address}, ${bank.city}, ${bank.state} ${bank.zipCode}", fontSize = 12.sp)
                        }
                        IconButton(onClick = { selectedFBOnMap = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close detailed view")
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Structured Information Grid detailing all available fields
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Row 1: Contact Methods
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(imageVector = Icons.Default.Phone, contentDescription = "Phone", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(13.dp))
                                    Text(text = if (bank.phone.isNotBlank()) bank.phone else "N/A", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(imageVector = Icons.Default.Email, contentDescription = "Email", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(13.dp))
                                    Text(text = if (bank.email.isNotBlank()) bank.email else "N/A", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                            
                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), thickness = 1.dp)

                            // Row 2: Standard Hours & Refrigeration Info
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(imageVector = Icons.Default.Schedule, contentDescription = "Hours", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(13.dp))
                                    Text(text = if (bank.operatingHours.isNotBlank()) bank.operatingHours else "Mon-Fri 9 AM - 5 PM", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = "Cold Storage", tint = if (bank.coldStorage) Color(0xFF2E7D32) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(13.dp))
                                    Text(text = if (bank.coldStorage) "Cold Storage: Yes" else "Cold Storage: No", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), thickness = 1.dp)

                            // Row 3: Organizational Size capacity
                            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Icon(imageVector = Icons.Default.Info, contentDescription = "Classification Scale", tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(13.dp))
                                Text(text = "Agency Capacity Classification: ${if (bank.size.isNotBlank()) bank.size else "Medium"}", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val context = androidx.compose.ui.platform.LocalContext.current
                    Button(
                        onClick = {
                            val address = "${bank.address}, ${bank.city}, ${bank.state} ${bank.zipCode}"
                            val uriStr = "geo:0,0?q=${java.net.URLEncoder.encode(address, "UTF-8")}"
                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(uriStr))
                            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                            try {
                                intent.setPackage("com.google.android.apps.maps")
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                try {
                                    val fallbackIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("geo:0,0?q=${java.net.URLEncoder.encode(address, "UTF-8")}"))
                                    fallbackIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(fallbackIntent)
                                } catch (e2: Exception) {
                                    try {
                                        val webIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://www.google.com/maps/search/?api=1&query=${java.net.URLEncoder.encode(address, "UTF-8")}"))
                                        webIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(webIntent)
                                    } catch (e3: Exception) {
                                        android.widget.Toast.makeText(context, "No maps app or web browser found.", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("map_get_directions_btn_${bank.id}"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Directions, contentDescription = "Get Navigation Directions", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Get Route Directions", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (bankRequests.isEmpty()) {
                        Text(text = "No active requests currently posted for this branch", fontSize = 13.sp)
                    } else {
                        Text(
                            text = "📦 Active Requests (${bankRequests.size}):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(bankRequests) { req ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.width(200.dp)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = req.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = req.category,
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "${req.quantityRemaining} items remaining",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DonorClaimsTab(viewModel: PantryLinkViewModel) {
    val claims by viewModel.claimsState.collectAsStateWithLifecycle()
    val foodBanks by viewModel.foodBanksState.collectAsStateWithLifecycle()
    val userSession by viewModel.userSession.collectAsStateWithLifecycle()
    val currentUserProfile by viewModel.currentUserProfile.collectAsStateWithLifecycle()

    val activeEmail = userSession?.email ?: "npatel012010@gmail.com"
    val profileName = currentUserProfile?.get("name") as? String
    val userName = if (!profileName.isNullOrBlank()) profileName else activeEmail.substringBefore("@").replaceFirstChar { it.uppercase() }
    val userInitials = if (userName.length >= 2) userName.take(2).uppercase() else "PL"

    // Edit states for user profile
    var isEditingProfile by remember { mutableStateOf(false) }
    var editFirstName by remember { mutableStateOf("") }
    var editLastName by remember { mutableStateOf("") }
    var editPhone by remember { mutableStateOf("") }
    var editCity by remember { mutableStateOf("") }
    var editZip by remember { mutableStateOf("") }
    var editCanServeType by remember { mutableStateOf("") }
    var editCanServeQty by remember { mutableStateOf("") }
    var editFrequency by remember { mutableStateOf("") }
    var isInitialized by remember { mutableStateOf(false) }

    // Saved Locations State
    val savedLocations by viewModel.savedLocations.collectAsStateWithLifecycle()
    var isAddingLocation by remember { mutableStateOf(false) }
    var newLocName by remember { mutableStateOf("") }
    var newLocAddr by remember { mutableStateOf("") }
    var newLocZip by remember { mutableStateOf("") }

    // Google Places autocomplete state for donor profile editCity
    var showDonorCitySuggestions by remember { mutableStateOf(false) }
    var donorCitySearchLoading by remember { mutableStateOf(false) }
    var donorCitySuggestions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    
    // Google Places autocomplete state for donor saved locations
    var showNewLocAddrSuggestions by remember { mutableStateOf(false) }
    var newLocAddrSearchLoading by remember { mutableStateOf(false) }
    var newLocAddrSuggestions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }

    val coroutineScope = rememberCoroutineScope()
    val placesApiKey = BuildConfig.PLACES_API_KEY
    val isApiKeyPresent = GooglePlacesClient.isApiKeyValid(placesApiKey)

    LaunchedEffect(editCity, showDonorCitySuggestions) {
        if (editCity.isBlank() || !showDonorCitySuggestions) {
            donorCitySuggestions = emptyList()
            return@LaunchedEffect
        }
        kotlinx.coroutines.delay(200)
        donorCitySearchLoading = true
        try {
            if (isApiKeyPresent) {
                val response = GooglePlacesClient.service.autocomplete(
                    input = editCity,
                    apiKey = placesApiKey,
                    components = "country:us"
                )
                donorCitySuggestions = response.predictions
            } else {
                donorCitySuggestions = GooglePlacesClient.mockAutocomplete(editCity)
            }
        } catch (e: Exception) {
            android.util.Log.e("PantryLinkAuth", "Places Autocomplete request failed", e)
            donorCitySuggestions = GooglePlacesClient.mockAutocomplete(editCity)
        } finally {
            donorCitySearchLoading = false
        }
    }

    val selectDonorCitySuggestion: (AutocompletePrediction) -> Unit = { prediction ->
        showDonorCitySuggestions = false
        donorCitySearchLoading = true
        coroutineScope.launch {
            try {
                val detailsResponse = if (isApiKeyPresent) {
                    GooglePlacesClient.service.getDetails(
                        placeId = prediction.place_id,
                        apiKey = placesApiKey,
                        fields = "address_components"
                    )
                } else {
                    GooglePlacesClient.mockDetails(prediction.place_id)
                }
                
                val result = detailsResponse.result
                if (result != null) {
                    var city = ""
                    var state = ""
                    var zip = ""
                    
                    for (comp in result.address_components) {
                        val types = comp.types
                        when {
                            types.contains("locality") -> city = comp.long_name
                            types.contains("administrative_area_level_1") -> state = comp.short_name
                            types.contains("postal_code") -> zip = comp.long_name
                        }
                    }
                    
                    editCity = if (city.isNotBlank() && state.isNotBlank()) "$city, $state" else city.ifBlank { state }
                    if (zip.isNotBlank()) {
                        editZip = zip
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("PantryLinkAuth", "Places Details request failed", e)
            } finally {
                donorCitySearchLoading = false
            }
        }
    }

    LaunchedEffect(newLocAddr, showNewLocAddrSuggestions) {
        if (newLocAddr.isBlank() || !showNewLocAddrSuggestions) {
            newLocAddrSuggestions = emptyList()
            return@LaunchedEffect
        }
        kotlinx.coroutines.delay(200)
        newLocAddrSearchLoading = true
        try {
            if (isApiKeyPresent) {
                val response = GooglePlacesClient.service.autocomplete(
                    input = newLocAddr,
                    apiKey = placesApiKey,
                    components = "country:us"
                )
                newLocAddrSuggestions = response.predictions
            } else {
                newLocAddrSuggestions = GooglePlacesClient.mockAutocomplete(newLocAddr)
            }
        } catch (e: Exception) {
            android.util.Log.e("PantryLinkAuth", "Places Autocomplete request failed", e)
            newLocAddrSuggestions = GooglePlacesClient.mockAutocomplete(newLocAddr)
        } finally {
            newLocAddrSearchLoading = false
        }
    }

    val selectNewLocAddrSuggestion: (AutocompletePrediction) -> Unit = { prediction ->
        showNewLocAddrSuggestions = false
        newLocAddrSearchLoading = true
        coroutineScope.launch {
            try {
                val detailsResponse = if (isApiKeyPresent) {
                    GooglePlacesClient.service.getDetails(
                        placeId = prediction.place_id,
                        apiKey = placesApiKey,
                        fields = "address_components"
                    )
                } else {
                    GooglePlacesClient.mockDetails(prediction.place_id)
                }
                
                val result = detailsResponse.result
                if (result != null) {
                    var streetNumber = ""
                    var route = ""
                    var zip = ""
                    
                    for (comp in result.address_components) {
                        val types = comp.types
                        when {
                            types.contains("street_number") -> streetNumber = comp.long_name
                            types.contains("route") -> route = comp.long_name
                            types.contains("postal_code") -> zip = comp.long_name
                        }
                    }
                    
                    val formattedStreet = if (streetNumber.isNotBlank() && route.isNotBlank()) {
                        "$streetNumber $route"
                    } else if (route.isNotBlank()) {
                        route
                    } else {
                        streetNumber.ifBlank { prediction.structured_formatting?.main_text ?: "" }
                    }
                    
                    newLocAddr = formattedStreet
                    if (zip.isNotBlank()) {
                        newLocZip = zip
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("PantryLinkAuth", "Places Details request failed", e)
            } finally {
                newLocAddrSearchLoading = false
            }
        }
    }

    LaunchedEffect(currentUserProfile, isEditingProfile) {
        if (currentUserProfile != null && !isInitialized) {
            val fullName = currentUserProfile?.get("name") as? String ?: ""
            val splitNames = fullName.trim().split(" ")
            editFirstName = splitNames.firstOrNull() ?: ""
            editLastName = if (splitNames.size > 1) splitNames.drop(1).joinToString(" ") else ""
            editPhone = currentUserProfile?.get("phone") as? String ?: ""
            editCity = currentUserProfile?.get("donorCity") as? String ?: ""
            editZip = currentUserProfile?.get("donorZip") as? String ?: ""
            editCanServeType = currentUserProfile?.get("donorCanServeType") as? String ?: ""
            editCanServeQty = currentUserProfile?.get("donorCanServeQty") as? String ?: ""
            editFrequency = currentUserProfile?.get("donorFrequency") as? String ?: ""
            isInitialized = true
        }
    }

    // Notification Toggles State
    val pushEnabled by viewModel.pushNotificationsEnabled.collectAsStateWithLifecycle()
    val emailEnabled by viewModel.emailNotificationsEnabled.collectAsStateWithLifecycle()
    val smsEnabled by viewModel.smsNotificationsEnabled.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- SECTION 1: HEADER & USER PROFILE CARD ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth().testTag("profile_user_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (!isEditingProfile) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFD1FAE5), // emerald-100
                                modifier = Modifier.size(56.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                      Text(
                                          text = userInitials,
                                          fontSize = 18.sp,
                                          fontWeight = FontWeight.Black,
                                          color = Color(0xFF064E3B) // emerald-950
                                      )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = userName,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified Contributor Badge",
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = activeEmail,
                                    fontSize = 13.sp,
                                    color = Color(0xFF64748B)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFECFDF5),
                                    modifier = Modifier.wrapContentSize()
                                ) {
                                    Text(
                                        text = "VETTED COMMUNITY DONOR",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF047857),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        // Rich donor characteristics
                        currentUserProfile?.let { prof ->
                            val cap = prof["donorCanServeQty"]?.toString() ?: ""
                            val cat = prof["donorCanServeType"]?.toString() ?: ""
                            val freq = prof["donorFrequency"]?.toString() ?: ""
                            val city = prof["donorCity"]?.toString() ?: ""
                            val zip = prof["donorZip"]?.toString() ?: ""

                            if (cap.isNotBlank() || cat.isNotBlank() || freq.isNotBlank() || city.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = "Registered Logistical Attributes:",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState())
                                ) {
                                    if (cat.isNotBlank()) {
                                        val categoriesList = cat.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                                        categoriesList.forEach { singleCat ->
                                            SuggestionChip(
                                                onClick = {},
                                                label = { Text(singleCat, fontSize = 10.sp) },
                                                icon = { Icon(Icons.Default.Restaurant, null, modifier = Modifier.size(10.dp)) }
                                            )
                                        }
                                    }
                                    if (cap.isNotBlank()) {
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text(cap, fontSize = 10.sp) },
                                            icon = { Icon(Icons.Default.LocalShipping, null, modifier = Modifier.size(10.dp)) }
                                        )
                                    }
                                    if (freq.isNotBlank()) {
                                        SuggestionChip(
                                            onClick = {},
                                            label = { Text(freq, fontSize = 10.sp) },
                                            icon = { Icon(Icons.Default.Update, null, modifier = Modifier.size(10.dp)) }
                                        )
                                    }
                                }
                                if (city.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(Icons.Default.LocationOn, null, tint = Color(0xFF64748B), modifier = Modifier.size(12.dp))
                                        Text(
                                            text = "Primary base: $city" + if (zip.isNotBlank()) " ($zip)" else "",
                                            fontSize = 11.sp,
                                            color = Color(0xFF64748B),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { isEditingProfile = true },
                            modifier = Modifier.fillMaxWidth().height(40.dp).testTag("edit_profile_toggle_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.Edit, null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Edit Profile Details", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // EDIT PROFILE MODE ACTIVE
                        Text(
                            text = "Edit Profile & Logistics",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = editFirstName,
                                onValueChange = { editFirstName = it },
                                label = { Text("First Name") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = editLastName,
                                onValueChange = { editLastName = it },
                                label = { Text("Last Name") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = editPhone,
                            onValueChange = { editPhone = it },
                            label = { Text("Contact Phone") },
                            leadingIcon = { Icon(Icons.Default.Phone, null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = editCity,
                                    onValueChange = { 
                                        editCity = it
                                        showDonorCitySuggestions = true
                                    },
                                    label = { Text("Base City (GA)") },
                                    modifier = Modifier.weight(1.2f).testTag("donor_profile_edit_city_input"),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )
                                OutlinedTextField(
                                    value = editZip,
                                    onValueChange = { editZip = it },
                                    label = { Text("ZIP Code") },
                                    modifier = Modifier.weight(0.8f),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )
                            }

                            if (showDonorCitySuggestions && editCity.isNotBlank()) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                ) {
                                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "CITY SUGGESTIONS",
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.8.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            if (donorCitySearchLoading) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(10.dp),
                                                    strokeWidth = 1.5.dp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            } else {
                                                Text(
                                                    text = "PantryLink Address Helper",
                                                    fontSize = 8.sp,
                                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                )
                                            }
                                        }
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                                        if (donorCitySuggestions.isEmpty() && !donorCitySearchLoading) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 12.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "No cities found. Type to search.",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                )
                                            }
                                        } else {
                                            donorCitySuggestions.forEachIndexed { index, prediction ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            selectDonorCitySuggestion(prediction)
                                                        }
                                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.LocationOn,
                                                        contentDescription = null,
                                                        tint = if (isApiKeyPresent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Column {
                                                        Text(
                                                            text = prediction.structured_formatting?.main_text ?: prediction.description,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        prediction.structured_formatting?.secondary_text?.let { secText ->
                                                            Text(
                                                                text = secText,
                                                                fontSize = 10.sp,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                            )
                                                        }
                                                    }
                                                }
                                                if (index < donorCitySuggestions.size - 1) {
                                                    HorizontalDivider(
                                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                                                        modifier = Modifier.padding(horizontal = 8.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider(color = Color(0xFFF1F5F9))
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "What categories of food can you service?",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        val donorFoodTypes = listOf("Fresh Produce", "Canned Goods", "Dry Goods", "Dairy", "Prepared Food")
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            donorFoodTypes.chunked(3).forEach { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowItems.forEach { item ->
                                        val isSelected = editCanServeType.split(",").map { it.trim() }.contains(item)
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color(0xFFF8FAFC))
                                                .border(
                                                    1.dp,
                                                    if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFE2E8F0),
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .clickable {
                                                    val currentSet = editCanServeType.split(",")
                                                        .map { it.trim() }
                                                        .filter { it.isNotEmpty() }
                                                        .toMutableSet()
                                                    if (currentSet.contains(item)) {
                                                        currentSet.remove(item)
                                                    } else {
                                                        currentSet.add(item)
                                                    }
                                                    editCanServeType = currentSet.sorted().joinToString(", ")
                                                }
                                                .padding(vertical = 8.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(item, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                    if (rowItems.size < 3) {
                                        repeat(3 - rowItems.size) { Spacer(modifier = Modifier.weight(1f)) }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Vehicle Capacity Size:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        val donorCapacities = listOf("Single bag / box", "Trunk Load", "Full SUV / Van", "Pallets / Large Truck")
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(donorCapacities.size) { index ->
                                val item = donorCapacities[index]
                                val active = editCanServeQty == item
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (active) MaterialTheme.colorScheme.secondary else Color(0xFFF8FAFC))
                                        .border(
                                            1.dp,
                                            if (active) MaterialTheme.colorScheme.secondary else Color(0xFFE2E8F0),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { editCanServeQty = item }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(item, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = if (active) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Availability Frequency:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )

                        val frequencies = listOf("Weekly", "Bi-weekly", "Monthly", "Occasionally")
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            frequencies.forEach { item ->
                                val active = editFrequency == item
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (active) MaterialTheme.colorScheme.tertiary else Color(0xFFF8FAFC))
                                        .border(
                                            1.dp,
                                            if (active) MaterialTheme.colorScheme.tertiary else Color(0xFFE2E8F0),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { editFrequency = item }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(item, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = if (active) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { isEditingProfile = false },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                            ) {
                                Text("Discard", fontSize = 12.sp, color = Color(0xFF475569))
                            }

                            Button(
                                onClick = {
                                    viewModel.updateProfile(
                                        name = "$editFirstName $editLastName".trim(),
                                        phone = editPhone,
                                        donorZip = editZip,
                                        donorCity = editCity,
                                        donorCanServeType = editCanServeType,
                                        donorCanServeQty = editCanServeQty,
                                        donorFrequency = editFrequency
                                    )
                                    isEditingProfile = false
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text("Save Changes", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION 2: NOTIFICATION SETTINGS CARD ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth().testTag("profile_notification_settings_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Notification Preferences",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Push Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.togglePushNotifications() }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color(0xFF047857), modifier = Modifier.size(20.dp))
                            Column {
                                Text("Push Alerts", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Text("Urgent local pantry needs", fontSize = 11.sp, color = Color(0xFF64748B))
                            }
                        }
                        Switch(
                            checked = pushEnabled,
                            onCheckedChange = { viewModel.togglePushNotifications() },
                            modifier = Modifier.testTag("push_switch")
                        )
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // Email Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleEmailNotifications() }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(20.dp))
                            Column {
                                Text("Email Confirmations (Coming Soon)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                Text("Automated tax logs & receipts (Coming Soon)", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            }
                        }
                        Switch(
                            checked = emailEnabled,
                            onCheckedChange = { viewModel.toggleEmailNotifications() },
                            modifier = Modifier.testTag("email_switch"),
                            enabled = false // visually standard coming-soon form element
                        )
                    }

                    HorizontalDivider(color = Color(0xFFF1F5F9))

                    // SMS Toggle
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleSMSNotifications() }
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Default.Sms, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(20.dp))
                            Column {
                                Text("SMS Direct Alerts (Coming Soon)", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                Text("Real-time community dispatch coordination (Coming Soon)", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            }
                        }
                        Switch(
                            checked = smsEnabled,
                            onCheckedChange = { viewModel.toggleSMSNotifications() },
                            modifier = Modifier.testTag("sms_switch"),
                            enabled = false
                        )
                    }
                }
            }
        }

        // --- SECTION 3: SAVED LOCATIONS MANAGER ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp, topStart = 6.dp, topEnd = 24.dp),
                border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFF0EBE3)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth().testTag("profile_saved_locations_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Saved Drop-off Coordinates",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        TextButton(
                            onClick = { isAddingLocation = !isAddingLocation },
                            modifier = Modifier.testTag("add_loc_toggle_btn")
                        ) {
                            Text(
                                text = if (isAddingLocation) "Cancel" else "+ Add",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF059669)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    savedLocations.forEachIndexed { idx, loc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Place, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
                                Column {
                                    Text(loc.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                    Text(loc.address + " (" + loc.zipCode + ")", fontSize = 11.sp, color = Color(0xFF64748B))
                                }
                            }
                            IconButton(
                                onClick = { viewModel.removeSavedLocation(loc.id) },
                                modifier = Modifier.size(28.dp).testTag("delete_loc_${loc.id}")
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                            }
                        }
                        if (idx < savedLocations.size - 1) {
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                        }
                    }

                    if (isAddingLocation) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8FAF8), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text("Add New Favorite Delivery Point", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF047857))

                            OutlinedTextField(
                                value = newLocName,
                                onValueChange = { newLocName = it },
                                label = { Text("Label (e.g., Home, Office)") },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(52.dp).testTag("new_loc_name_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = Color(0xFFCBD5E1)
                                )
                            )
                            OutlinedTextField(
                                value = newLocAddr,
                                onValueChange = { 
                                    newLocAddr = it
                                    showNewLocAddrSuggestions = true
                                },
                                label = { Text("Street Address") },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(52.dp).testTag("new_loc_addr_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = Color(0xFFCBD5E1)
                                )
                            )

                            if (showNewLocAddrSuggestions && newLocAddr.isNotBlank()) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                ) {
                                    Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 12.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "ADDRESS SUGGESTIONS",
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.8.sp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            if (newLocAddrSearchLoading) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(10.dp),
                                                    strokeWidth = 1.5.dp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            } else {
                                                Text(
                                                    text = "PantryLink Address Helper",
                                                    fontSize = 8.sp,
                                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                )
                                            }
                                        }
                                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                                        if (newLocAddrSuggestions.isEmpty() && !newLocAddrSearchLoading) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(vertical = 12.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = "No addresses found. Type to search.",
                                                    fontSize = 11.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                )
                                            }
                                        } else {
                                            newLocAddrSuggestions.forEachIndexed { index, prediction ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            selectNewLocAddrSuggestion(prediction)
                                                        }
                                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.LocationOn,
                                                        contentDescription = null,
                                                        tint = if (isApiKeyPresent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Column {
                                                        Text(
                                                            text = prediction.structured_formatting?.main_text ?: prediction.description,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.onSurface
                                                        )
                                                        prediction.structured_formatting?.secondary_text?.let { secText ->
                                                            Text(
                                                                text = secText,
                                                                fontSize = 10.sp,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                            )
                                                        }
                                                    }
                                                }
                                                if (index < newLocAddrSuggestions.size - 1) {
                                                    HorizontalDivider(
                                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                                                        modifier = Modifier.padding(horizontal = 8.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            OutlinedTextField(
                                value = newLocZip,
                                onValueChange = { newLocZip = it },
                                label = { Text("ZIP Code") },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().height(52.dp).testTag("new_loc_zip_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = Color(0xFFCBD5E1)
                                )
                            )

                            Button(
                                onClick = {
                                    viewModel.addSavedLocation(newLocName, newLocAddr, newLocZip)
                                    newLocName = ""
                                    newLocAddr = ""
                                    newLocZip = ""
                                    isAddingLocation = false
                                },
                                modifier = Modifier.fillMaxWidth().height(38.dp).testTag("add_loc_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Save Tagged Location", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // --- SECTION 4: RESERVED CLAIMS TRACKER ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "💼 Active & Past Contributions",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFF1F5F9),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "${claims.size}", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF475569))
                    }
                }
            }
        }

        if (claims.isEmpty()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Inventory,
                            contentDescription = "No claims active",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No reservation claims active right now.",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Select 'Browse Requests' to start supporting nearby food banks in Georgia!",
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            color = Color(0xFF64748B)
                        )
                    }
                }
            }
        } else {
            items(claims) { claim ->
                val matchingFB = foodBanks.find { it.name == claim.foodBankName }
                DonorClaimCard(
                    claim = claim,
                    foodBank = matchingFB,
                    onCancelClick = { viewModel.cancelClaim(claim.id) },
                    onDropOffClick = { viewModel.dropOffClaim(claim.id) },
                    onSimulateExpiration = { viewModel.triggerClaimExpiration(claim.id) }
                )
            }
        }
    }
}

@Composable
fun DonorClaimCard(
    claim: ClaimEntity,
    foodBank: FoodBankEntity?,
    onCancelClick: () -> Unit,
    onDropOffClick: () -> Unit,
    onSimulateExpiration: () -> Unit
) {
    val statusColor = when (claim.claimStatus) {
        "Claimed" -> MaterialTheme.colorScheme.primary
        "Ready for Drop-Off" -> Color(0xFFF57C00)
        "Dropped Off" -> Color(0xFF1976D2)
        "Accepted" -> Color(0xFF2E7D32)
        "Rejected" -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp, topStart = 6.dp, topEnd = 24.dp),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFF0EBE3)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("claim_card_${claim.id}")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = claim.requestTitle, 
                        fontWeight = FontWeight.Bold, 
                        fontSize = 16.sp,
                        fontFamily = FontFamily.SansSerif,
                        color = MaterialTheme.colorScheme.primary,
                        lineHeight = 21.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HomeWork,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = claim.foodBankName, 
                            fontSize = 12.sp, 
                            color = MaterialTheme.colorScheme.secondary, 
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (foodBank != null) {
                        Text(
                            text = "${foodBank.address}, ${foodBank.city}, ${foodBank.state} ${foodBank.zipCode}",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = statusColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = claim.claimStatus.uppercase(),
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        style = androidx.compose.ui.text.TextStyle(letterSpacing = 0.5.sp),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFF3EDE2))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "COMMITTED AMOUNT", 
                        fontSize = 9.sp, 
                        fontWeight = FontWeight.Bold,
                        style = androidx.compose.ui.text.TextStyle(letterSpacing = 0.5.sp),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(text = "${claim.quantityClaimed} Units", fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = MaterialTheme.colorScheme.onSurface)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "RESERVED ON", 
                        fontSize = 9.sp, 
                        fontWeight = FontWeight.Bold,
                        style = androidx.compose.ui.text.TextStyle(letterSpacing = 0.5.sp),
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(text = formatDate(claim.claimTimestamp), fontSize = 13.5.sp, color = MaterialTheme.colorScheme.onSurface)
                }
            }

            if (claim.rejectionReason != null && claim.claimStatus == "Rejected") {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Warning, contentDescription = "Rejected status detail", tint = MaterialTheme.colorScheme.error)
                        Column {
                            Text(text = "REJECTION REASON:", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            Text(text = claim.rejectionReason, fontSize = 12.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }

            // G. Controlled cancellation & Drop-off actions
            if (claim.claimStatus == "Claimed" || claim.claimStatus == "Ready for Drop-Off") {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    if (foodBank != null) {
                        OutlinedButton(
                            onClick = {
                                val address = "${foodBank.address}, ${foodBank.city}, ${foodBank.state} ${foodBank.zipCode}"
                                val uriStr = "geo:0,0?q=${java.net.URLEncoder.encode(address, "UTF-8")}"
                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(uriStr))
                                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                try {
                                    intent.setPackage("com.google.android.apps.maps")
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    try {
                                        val fallbackIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("geo:0,0?q=${java.net.URLEncoder.encode(address, "UTF-8")}"))
                                        fallbackIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                        context.startActivity(fallbackIntent)
                                    } catch (e2: Exception) {
                                        try {
                                            val webIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://www.google.com/maps/search/?api=1&query=${java.net.URLEncoder.encode(address, "UTF-8")}"))
                                            webIntent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                            context.startActivity(webIntent)
                                        } catch (e3: Exception) {
                                            android.widget.Toast.makeText(context, "No maps app or web browser found.", android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().testTag("claim_directions_btn_${claim.id}"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF047857)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA7F3D0)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Directions, contentDescription = "Get Navigation Directions", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Get Navigation Directions", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onCancelClick,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("cancel_claim_btn_${claim.id}"),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Cancel Claim", fontSize = 12.sp)
                        }
                        Button(
                            onClick = onDropOffClick,
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("dropoff_claim_btn_${claim.id}"),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Mark Dropped Off", fontSize = 12.sp)
                        }
                    }

                    // H. Claim Expiration Rules preview selector
                    OutlinedButton(
                        onClick = onSimulateExpiration,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("expire_sim_btn_${claim.id}"),
                    ) {
                        Text("⏳ Simulate Expiration (Lock-up timer rule)", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// FORMAT HELPER
fun formatDate(time: Long): String {
    val date = Date(time)
    val format = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return format.format(date)
}


// ==========================================
// FOOD BANK WORKSPACE (PARTNER SIDE)
// ==========================================
@Composable
fun FoodBankWorkspace(viewModel: PantryLinkViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Inventory Needs", "Post Requests", "Verify Deliveries", "Audit Trail", "My Profile")

    Column(modifier = Modifier.fillMaxSize()) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
            shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(tabs.size) { index ->
                    val title = tabs[index]
                    val isSelected = selectedTab == index
                    val icon = when (index) {
                        0 -> Icons.Default.Inventory
                        1 -> Icons.Default.AddBox
                        2 -> Icons.Default.CheckCircle
                        3 -> Icons.Default.History
                        else -> Icons.Default.Person
                    }

                    val containerColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "fb_tab_bg"
                    )
                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                        label = "fb_tab_text"
                    )
                    val borderStroke = if (isSelected) {
                        androidx.compose.foundation.BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                    } else {
                        androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
                    }

                    Surface(
                        onClick = { selectedTab = index },
                        shape = RoundedCornerShape(16.dp),
                        color = containerColor,
                        contentColor = contentColor,
                        border = borderStroke,
                        modifier = Modifier.height(38.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                style = androidx.compose.ui.text.TextStyle(
                                    letterSpacing = 0.2.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (selectedTab) {
                0 -> FBActiveNeedsTab(viewModel = viewModel)
                1 -> FBPostRequestTab(viewModel = viewModel)
                2 -> FBVerifyDropsTab(viewModel = viewModel)
                3 -> FBAuditLogsTab(viewModel = viewModel)
                4 -> FBProfileTab(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun FBActiveNeedsTab(viewModel: PantryLinkViewModel) {
    val requests by viewModel.requestsState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "📋 Partner Request Management",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Shared backend source-of-truth. Closes requests when requirements are confirmed.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        if (requests.isEmpty()) {
            EmptyStateCard(message = "No matching needs posted. Click 'Post Requests' to list items.")
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(requests) { req ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("fb_request_manager_${req.id}")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = req.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    Text(text = "Category: ${req.category}", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (req.status == "Closed") Color.DarkGray.copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = req.status,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (req.status == "Closed") Color.DarkGray else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = req.itemDescription, fontSize = 13.sp)

                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("NEEDED", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                                    Text("${req.quantityNeeded} units", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Column {
                                    Text("REMAINING", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                                    Text("${req.quantityRemaining} units", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.error)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("DEADLINE", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary)
                                    Text(req.deadline, fontSize = 13.sp)
                                }
                            }

                            if (req.status != "Closed") {
                                Spacer(modifier = Modifier.height(12.dp))
                                HorizontalDivider(color = Color(0xFFF1F5F9))
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { viewModel.closeRequest(req.id) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("close_request_btn_${req.id}"),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Close Target Request")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FBPostRequestTab(viewModel: PantryLinkViewModel) {
    val keyboardController = LocalSoftwareKeyboardController.current

    var reqTitle by remember { mutableStateOf("") }
    var reqCategory by remember { mutableStateOf("Canned Foods") }
    var reqItemDesc by remember { mutableStateOf("") }
    var reqQuantity by remember { mutableStateOf("") }
    var reqDeadline by remember { mutableStateOf("2026-06-30") }
    var reqLocation by remember { mutableStateOf("12 Peachtree St NW, Atlanta, GA 30308") }
    var reqNotes by remember { mutableStateOf("") }

    val categories = listOf("Canned Foods", "Hygiene Products", "Baby Supplies", "School Supplies", "Shelf-Stable Items")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column {
                Text(
                    text = "✍️ Create Standardized Request",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Ensure language is clear, descriptive, and follows uniform formats to reduce donor confusion.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        item {
            OutlinedTextField(
                value = reqTitle,
                onValueChange = { reqTitle = it },
                label = { Text("Request Title (e.g. Bulk Canned Vegetables Needed)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("post_title_input"),
                shape = RoundedCornerShape(10.dp)
            )
        }

        item {
            Column {
                Text(text = "Choose Approved Category", fontWeight = FontWeight.Bold)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = reqCategory == cat,
                            onClick = { reqCategory = cat },
                            label = { Text(cat) }
                        )
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = reqItemDesc,
                onValueChange = { reqItemDesc = it },
                label = { Text("Standardized Approved Item Description") },
                placeholder = { Text("Example: Canned green beans, unopened, standard 14-15 oz cans") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("post_desc_input"),
                shape = RoundedCornerShape(10.dp)
            )
            Text(text = "❌ Format not allowed: \"Need canned stuff\"", fontSize = 11.sp, color = MaterialTheme.colorScheme.error)
            Text(text = "✅ Format approved: \"Canned green beans, unopened, standard-sized cans\"", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = reqQuantity,
                    onValueChange = { reqQuantity = it },
                    label = { Text("Quantity Needed (units)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("post_qty_input"),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = reqDeadline,
                    onValueChange = { reqDeadline = it },
                    label = { Text("Deadline Date") },
                    placeholder = { Text("YYYY-MM-DD") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("post_date_input"),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        }

        item {
            OutlinedTextField(
                value = reqLocation,
                onValueChange = { reqLocation = it },
                label = { Text("Drop-Off Location (Partner Address)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("post_loc_input"),
                shape = RoundedCornerShape(10.dp)
            )
        }

        item {
            OutlinedTextField(
                value = reqNotes,
                onValueChange = { reqNotes = it },
                label = { Text("Handling Notes or Specific Instructions (Optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("post_notes_input"),
                shape = RoundedCornerShape(10.dp)
            )
        }

        item {
            Button(
                onClick = {
                    val qty = reqQuantity.toIntOrNull()
                    if (reqTitle.isEmpty() || reqItemDesc.isEmpty() || qty == null || qty <= 0) {
                        viewModel.showToast("Inputs validation failed. Title, description and numerical quantity are required!")
                    } else {
                        viewModel.createRequest(
                            title = reqTitle,
                            category = reqCategory,
                            itemDescription = reqItemDesc,
                            quantityNeeded = qty,
                            deadline = reqDeadline,
                            dropOffLocation = reqLocation,
                            extraNotes = reqNotes
                        )
                        // Clear
                        reqTitle = ""
                        reqItemDesc = ""
                        reqQuantity = ""
                        keyboardController?.hide()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("submit_request_button")
            ) {
                Text("Compile & Post Standardized Request")
            }
        }
    }
}

@Composable
fun FBVerifyDropsTab(viewModel: PantryLinkViewModel) {
    val allClaims by viewModel.allClaimsState.collectAsStateWithLifecycle()
    val submittedDrops = allClaims.filter { it.claimStatus == "Dropped Off" }

    var selectedRejectClaim by remember { mutableStateOf<ClaimEntity?>(null) }
    val rejectionReasons = listOf("wrong item", "opened item", "damaged item", "expired item", "unsafe item", "incomplete quantity")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "🛡️ Verify Incoming Donations",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Validate actual delivered goods against request parameters. Approve or Reject carefully.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        if (submittedDrops.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Nothing pending",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No unconfirmed drop-offs awaiting review.", fontWeight = FontWeight.Bold)
                    Text("Pantry counts are fully synced with actual deliveries.", fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(submittedDrops) { claim ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp),
                        elevation = CardDefaults.cardElevation(2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("FB_audit_drop_${claim.id}")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = claim.requestTitle, fontWeight = FontWeight.Bold)
                                    Text(text = "Donor: " + claim.donorUserId, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                                }
                                Icon(
                                    imageVector = Icons.Default.LocalShipping,
                                    contentDescription = "Dropped off icon",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = Color(0xFFF1F5F9))
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "QUANTITY DELIVERED:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Text(text = "${claim.quantityClaimed} units", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "DROP DATE:", fontSize = 11.sp)
                                Text(text = claim.dropoffConfirmationTimestamp?.let { formatDate(it) } ?: "Today")
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { selectedRejectClaim = claim },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("FB_reject_btn_${claim.id}")
                                ) {
                                    Text("Reject Drop")
                                }

                                Button(
                                    onClick = { viewModel.reviewClaim(claimId = claim.id, approved = true, rejectionReason = null) },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .testTag("FB_approve_btn_${claim.id}")
                                ) {
                                    Text("Approve & Recount")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Reject Claim Reason Selector Dialog popup
    selectedRejectClaim?.let { claim ->
        AlertDialog(
            onDismissRequest = { selectedRejectClaim = null },
            title = {
                Text(
                    text = "Confirm Delivery Rejection",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Every rejection must specify an approved reason. Tap the condition issue identified below:",
                        fontSize = 13.sp
                    )

                    rejectionReasons.forEach { reason ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.reviewClaim(
                                        claimId = claim.id,
                                        approved = false,
                                        rejectionReason = reason
                                    )
                                    selectedRejectClaim = null
                                }
                                .padding(vertical = 2.dp)
                                .testTag("reason_$reason")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = reason,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = reason.replaceFirstChar { it.uppercase() },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { selectedRejectClaim = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun FBAuditLogsTab(viewModel: PantryLinkViewModel) {
    val auditLogs by viewModel.auditLogsState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text(
                text = "✒️ Permanent Audit Logs",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Legally binding electronic traces of donor submissions, approvals, cancels and system releases.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        if (auditLogs.isEmpty()) {
            EmptyStateCard(message = "No actions logged yet.")
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(auditLogs) { log ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(8.dp),
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = log.actionType,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = formatDate(log.timestamp),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Donor: " + log.donorId, fontSize = 11.sp)
                            Text(text = "Request ID: #${log.requestId} | Claim ID: #${log.claimId}", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.height(2.dp))
                            Row {
                                Text(text = "Status: ", fontSize = 11.sp)
                                Text(text = log.oldStatus, fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                                Text(text = " ➜ ", fontSize = 11.sp)
                                Text(text = log.newStatus, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Global Empty State Card Component
@Composable
fun EmptyStateCard(message: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFAF7F2)),
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp, topStart = 6.dp, topEnd = 24.dp),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFEFECE5)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                modifier = Modifier.size(54.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.VolunteerActivism,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Text(
                text = "COMMUNITY BULLETIN",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary,
                style = androidx.compose.ui.text.TextStyle(letterSpacing = 1.2.sp)
            )
            Text(
                text = message,
                fontSize = 13.5.sp,
                fontFamily = FontFamily.SansSerif,
                textAlign = TextAlign.Center,
                lineHeight = 19.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Global Item Request Card with warm hospitable styling
@Composable
fun ItemRequestCard(
    request: RequestEntity,
    distance: Double,
    onClaimClick: () -> Unit
) {
    // Elegant category badge icon
    val categoryIcon = when (request.category) {
        "Canned Foods" -> Icons.Default.Kitchen
        "Hygiene Products" -> Icons.Default.CleanHands
        "Baby Supplies" -> Icons.Default.ChildCare
        "School Supplies" -> Icons.Default.Backpack
        else -> Icons.Default.Fastfood
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 8.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
        border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFF0EBE3)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("request_card_${request.id}")
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row (Headline & Category Indicator badge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = request.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        fontFamily = FontFamily.SansSerif,
                        color = MaterialTheme.colorScheme.primary,
                        lineHeight = 22.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.HomeWork,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = request.foodBankName,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.12f),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = categoryIcon,
                            contentDescription = request.category,
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.tertiary
                        )
                        Text(
                            text = request.category.uppercase(),
                            color = MaterialTheme.colorScheme.tertiary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            style = androidx.compose.ui.text.TextStyle(letterSpacing = 0.5.sp)
                        )
                    }
                }
            }

            HorizontalDivider(color = Color(0xFFF3EDE2))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "SPECIFIC PANTRIES NEED:",
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    style = androidx.compose.ui.text.TextStyle(letterSpacing = 0.8.sp)
                )
                Text(
                    text = request.itemDescription,
                    fontSize = 13.5.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                    lineHeight = 19.sp
                )
            }

            val isFulfilled = request.quantityRemaining == 0

            // Realtime remaining tracker matching Professional Polish layout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isFulfilled) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color(0xFFF5EDE4),
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    Text(
                        text = if (isFulfilled) "FULLY FULFILLED" else "${request.quantityRemaining} NEEDED",
                        color = if (isFulfilled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        style = androidx.compose.ui.text.TextStyle(letterSpacing = 0.5.sp),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }

                val percent = if (request.quantityNeeded > 0) {
                    (request.quantityNeeded - request.quantityRemaining).toFloat() / request.quantityNeeded.toFloat()
                } else 1.0f
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .background(color = Color(0xFFF0EBE3), shape = RoundedCornerShape(4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(percent)
                            .background(
                                color = if (isFulfilled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary,
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                }
                
                Text(
                    text = "${request.quantityNeeded - request.quantityRemaining} of ${request.quantityNeeded}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            if (request.quantityNeeded - request.quantityRemaining > 0 && !isFulfilled) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolunteerActivism,
                        contentDescription = null,
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "${request.quantityNeeded - request.quantityRemaining} units already committed/fulfilled by neighbors!",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF047857)
                    )
                }
            }

            HorizontalDivider(color = Color(0xFFF3EDE2))

            // Location, Distance, and Date constraints details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "${String.format("%.1f", distance)} miles away",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = "Due: ${request.deadline}",
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }

                Button(
                    onClick = onClaimClick,
                    modifier = Modifier.testTag("claim_btn_${request.id}"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (request.quantityRemaining == 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (request.quantityRemaining == 0) "Review Need" else "Help Fulfill",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

data class OfficialAddress(
    val street: String,
    val city: String,
    val zip: String,
    val state: String = "GA"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun PantryLinkAuthGateScreen(viewModel: PantryLinkViewModel, onViewTerms: () -> Unit) {
    var isSignUp by rememberSaveable { mutableStateOf(false) }
    var agreedToTerms by rememberSaveable { mutableStateOf(false) }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var name by rememberSaveable { mutableStateOf("") }
    var donorFirstName by rememberSaveable { mutableStateOf("") }
    var donorLastName by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf("") }
    var role by rememberSaveable { mutableStateOf("Donor") } // "Donor" or "Food Bank"
    var loading by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val rolesSelectionList = listOf("Donor", "Food Bank")

    val scrollState = androidx.compose.foundation.rememberScrollState()
    val focusManager = LocalFocusManager.current

    // Food Bank Specific Fields
    var fbAddress by rememberSaveable { mutableStateOf("") }
    var fbCity by rememberSaveable { mutableStateOf("") }
    var fbZip by rememberSaveable { mutableStateOf("") }
    var fbSize by rememberSaveable { mutableStateOf("Medium (100-500/wk)") }

    // Google Places Search-As-You-Type Autocomplete components
    var showFbAddressSuggestions by rememberSaveable { mutableStateOf(false) }
    var addressSearchLoading by remember { mutableStateOf(false) }
    var addressSuggestions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    val placesApiKey = BuildConfig.PLACES_API_KEY
    val isApiKeyPresent = GooglePlacesClient.isApiKeyValid(placesApiKey)

    LaunchedEffect(fbAddress, showFbAddressSuggestions) {
        if (fbAddress.isBlank() || !showFbAddressSuggestions) {
            addressSuggestions = emptyList()
            return@LaunchedEffect
        }
        
        // Debounce input to reduce unnecessary api calls
        kotlinx.coroutines.delay(200)
        
        addressSearchLoading = true
        try {
            if (isApiKeyPresent) {
                val response = GooglePlacesClient.service.autocomplete(
                    input = fbAddress,
                    apiKey = placesApiKey,
                    components = "country:us"
                )
                addressSuggestions = response.predictions
            } else {
                addressSuggestions = GooglePlacesClient.mockAutocomplete(fbAddress)
            }
        } catch (e: Exception) {
            android.util.Log.e("PantryLinkAuth", "Places Autocomplete request failed", e)
            addressSuggestions = GooglePlacesClient.mockAutocomplete(fbAddress)
        } finally {
            addressSearchLoading = false
        }
    }

    val selectSuggestion: (AutocompletePrediction) -> Unit = { prediction ->
        showFbAddressSuggestions = false
        addressSearchLoading = true
        coroutineScope.launch {
            try {
                val detailsResponse = if (isApiKeyPresent) {
                    GooglePlacesClient.service.getDetails(
                        placeId = prediction.place_id,
                        apiKey = placesApiKey,
                        fields = "address_components"
                    )
                } else {
                    GooglePlacesClient.mockDetails(prediction.place_id)
                }
                
                val result = detailsResponse.result
                if (result != null) {
                    var streetNumber = ""
                    var route = ""
                    var city = ""
                    var state = ""
                    var zip = ""
                    
                    for (comp in result.address_components) {
                        val types = comp.types
                        when {
                            types.contains("street_number") -> streetNumber = comp.long_name
                            types.contains("route") -> route = comp.long_name
                            types.contains("locality") -> city = comp.long_name
                            types.contains("administrative_area_level_1") -> state = comp.short_name
                            types.contains("postal_code") -> zip = comp.long_name
                        }
                    }
                    
                    val formattedStreet = if (streetNumber.isNotBlank() && route.isNotBlank()) {
                        "$streetNumber $route"
                    } else if (route.isNotBlank()) {
                        route
                    } else {
                        streetNumber.ifBlank { prediction.structured_formatting?.main_text ?: "" }
                    }
                    
                    fbAddress = formattedStreet
                    fbCity = if (city.isNotBlank() && state.isNotBlank()) "$city, $state" else city.ifBlank { state }
                    fbZip = zip
                }
            } catch (e: Exception) {
                android.util.Log.e("PantryLinkAuth", "Error fetching place details", e)
                fbAddress = prediction.structured_formatting?.main_text ?: prediction.description
            } finally {
                addressSearchLoading = false
                focusManager.clearFocus()
            }
        }
    }
    
    // Decomposed Operational Hours State:
    var opDaysSelection by rememberSaveable { mutableStateOf("Mon-Fri") }
    var opHoursSelection by rememberSaveable { mutableStateOf("9 AM - 5 PM") }
    var opCustomHours by rememberSaveable { mutableStateOf("") }
    var opHoursNotes by rememberSaveable { mutableStateOf("") }
    
    var fbColdStorage by rememberSaveable { mutableStateOf(false) }

    // Donor Specific Fields
    var donorZip by rememberSaveable { mutableStateOf("") }
    var donorCity by rememberSaveable { mutableStateOf("") }
    var donorCanServeType by rememberSaveable { mutableStateOf("All Categories") }
    val donorSelectedCategories = remember(donorCanServeType) {
        donorCanServeType.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
    }
    var donorCanServeQty by rememberSaveable { mutableStateOf("Trunk Load") }
    var donorFrequency by rememberSaveable { mutableStateOf("Weekly") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .widthIn(max = 440.dp)
                .padding(vertical = 12.dp)
                .testTag("auth_card"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = androidx.compose.foundation.BorderStroke(1.2.dp, Color(0xFFE2E8F0))
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Circular Hospitality Logo icon
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    modifier = Modifier.size(64.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isSignUp) Icons.Default.AppRegistration else Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Text(
                    text = if (isSignUp) "Create Partner Account" else "Community Portal Sign In",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 19.sp,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (isSignUp) "Register to coordinate and track stock across Georgia" else "Access your Georgia PantryLink dashboard and claims",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                errorMessage?.let {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(10.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (isSignUp) {
                    // Role selector at the VERY TOP
                    Text(
                        text = "I want to join as a:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rolesSelectionList.forEach { r ->
                            val active = role == r
                            OutlinedButton(
                                onClick = { role = r },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = if (active) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                                    contentColor = if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.2.dp,
                                    if (active) MaterialTheme.colorScheme.primary else Color(0xFFCBD5E1)
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (r == "Donor") Icons.Default.VolunteerActivism else Icons.Default.Storefront,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(r, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFF1F5F9))

                    // Quick general contact info
                    if (role == "Donor") {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = donorFirstName,
                                onValueChange = { donorFirstName = it },
                                label = { Text("First Name") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .testTag("auth_first_name_field"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Right) }
                                )
                            )

                            OutlinedTextField(
                                value = donorLastName,
                                onValueChange = { donorLastName = it },
                                label = { Text("Last Name") },
                                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp)) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp)
                                    .testTag("auth_last_name_field"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                )
                            )
                        }
                    } else {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Food Bank / Agency Name") },
                            leadingIcon = { Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("auth_name_field"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )
                    }

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Contact Phone") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("auth_phone_field"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                        )
                    )

                    // Dynamic role profile details
                    if (role == "Donor") {
                        // DONOR SPECIFIC ARTIFACTS
                        Text(
                            text = "Donor Profile & Logistics",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.5.sp,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedTextField(
                                value = donorCity,
                                onValueChange = { donorCity = it },
                                label = { Text("Base City (GA)") },
                                placeholder = { Text("e.g. Atlanta") },
                                modifier = Modifier
                                    .weight(1.2f)
                                    .height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                )
                            )

                            OutlinedTextField(
                                value = donorZip,
                                onValueChange = { donorZip = it },
                                label = { Text("ZIP Code") },
                                leadingIcon = { Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(14.dp)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                modifier = Modifier
                                    .weight(0.8f)
                                    .height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                )
                            )
                        }

                        // What you can serve
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "What category of food can you serve / donate?",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Select all food types you are equipped to handle. Choose 'All Categories' to reset.",
                                fontSize = 9.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        val donorFoodTypes = listOf("Fresh Produce", "Canned Goods", "Dry Goods", "Dairy", "Prepared Food", "All Categories")
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            donorFoodTypes.chunked(3).forEach { rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowItems.forEach { item ->
                                        val isSelected = if (item == "All Categories") {
                                            donorCanServeType == "All Categories"
                                        } else {
                                            donorSelectedCategories.contains(item)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color(0xFFF8FAFC))
                                                .border(
                                                    1.dp,
                                                    if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFE2E8F0),
                                                    RoundedCornerShape(12.dp)
                                                )
                                                .clickable {
                                                    if (item == "All Categories") {
                                                        donorCanServeType = "All Categories"
                                                    } else {
                                                        val currentSet = donorCanServeType.split(",")
                                                            .map { it.trim() }
                                                            .filter { it.isNotEmpty() && it != "All Categories" }
                                                            .toMutableSet()
                                                        if (currentSet.contains(item)) {
                                                            currentSet.remove(item)
                                                        } else {
                                                            currentSet.add(item)
                                                        }
                                                        donorCanServeType = if (currentSet.isEmpty()) {
                                                            "All Categories"
                                                        } else {
                                                            currentSet.sorted().joinToString(", ")
                                                        }
                                                    }
                                                }
                                                .padding(vertical = 10.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                if (isSelected) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = null,
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(12.dp)
                                                    )
                                                }
                                                Text(
                                                    text = item,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                    if (rowItems.size < 3) {
                                        repeat(3 - rowItems.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }

                        // How much you can serve (Capacity)
                        Text(
                            text = "Vehicle / Batch Capacity:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        val donorCapacities = listOf("Single bag / box", "Trunk Load", "Full SUV / Van", "Pallets / Large Truck")
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(donorCapacities) { item ->
                                val active = donorCanServeQty == item
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .background(if (active) MaterialTheme.colorScheme.secondary else Color(0xFFF8FAFC))
                                        .border(
                                            1.dp,
                                            if (active) MaterialTheme.colorScheme.secondary else Color(0xFFE2E8F0),
                                            RoundedCornerShape(16.dp)
                                        )
                                        .clickable { donorCanServeQty = item }
                                        .padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = item,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (active) MaterialTheme.colorScheme.onSecondary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Frequency
                        Text(
                            text = "Availability Frequency:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.align(Alignment.Start)
                        )

                        val frequencies = listOf("Weekly", "Bi-weekly", "Monthly", "Occasionally")
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            frequencies.forEach { item ->
                                val active = donorFrequency == item
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (active) MaterialTheme.colorScheme.tertiary else Color(0xFFF8FAFC))
                                        .border(
                                            1.dp,
                                            if (active) MaterialTheme.colorScheme.tertiary else Color(0xFFE2E8F0),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { donorFrequency = item }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (active) MaterialTheme.colorScheme.onTertiary else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                    } else {
                        // FOOD BANK SPECIFIC ARTIFACTS
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                            ),
                            shape = RoundedCornerShape(16.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Agency Physical Facility Info",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 12.5.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                Text(
                                    text = "Provide your exact physical location to enable seamless delivery coordination and proximity-based donation pairing.",
                                    fontSize = 9.5.sp,
                                    lineHeight = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )

                                OutlinedTextField(
                                    value = fbAddress,
                                    onValueChange = { 
                                        fbAddress = it 
                                        showFbAddressSuggestions = true
                                    },
                                    label = { Text("Agency Street Address") },
                                    placeholder = { Text("e.g. 1722 Peachtree Rd NW") },
                                    leadingIcon = { 
                                        Icon(
                                            imageVector = Icons.Default.LocationOn, 
                                            contentDescription = null, 
                                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                            modifier = Modifier.size(16.dp)
                                        ) 
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                    keyboardActions = KeyboardActions(
                                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                    )
                                )

                                if (showFbAddressSuggestions && fbAddress.isNotBlank()) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surface
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                                    ) {
                                        Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "ADDRESS SUGGESTIONS",
                                                    fontSize = 8.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    letterSpacing = 0.8.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                if (addressSearchLoading) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(10.dp),
                                                        strokeWidth = 1.5.dp,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                } else {
                                                    Text(
                                                        text = "PantryLink Address Helper",
                                                        fontSize = 8.sp,
                                                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                                    )
                                                }
                                            }
                                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                                            if (addressSuggestions.isEmpty() && !addressSearchLoading) {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(vertical = 12.dp),
                                                     contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "No addresses found. Type to search.",
                                                        fontSize = 11.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                    )
                                                }
                                            } else {
                                                addressSuggestions.forEachIndexed { index, prediction ->
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .clickable {
                                                                selectSuggestion(prediction)
                                                            }
                                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.LocationOn,
                                                            contentDescription = null,
                                                            tint = if (isApiKeyPresent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                        Column {
                                                            Text(
                                                                text = prediction.structured_formatting?.main_text ?: prediction.description,
                                                                fontSize = 12.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = MaterialTheme.colorScheme.onSurface
                                                            )
                                                            prediction.structured_formatting?.secondary_text?.let { secText ->
                                                                Text(
                                                                    text = secText,
                                                                    fontSize = 10.sp,
                                                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                                )
                                                            }
                                                        }
                                                    }
                                                    if (index < addressSuggestions.size - 1) {
                                                        HorizontalDivider(
                                                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                                                            modifier = Modifier.padding(horizontal = 8.dp)
                                                        )
                                                     }
                                                 }
                                             }
                                         }
                                     }
                                 }

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    OutlinedTextField(
                                        value = fbCity,
                                        onValueChange = { fbCity = it },
                                        label = { Text("City, State") },
                                        placeholder = { Text("Atlanta, GA") },
                                        modifier = Modifier
                                            .weight(1.2f)
                                            .height(56.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                        ),
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                        keyboardActions = KeyboardActions(
                                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                        )
                                    )

                                    OutlinedTextField(
                                        value = fbZip,
                                        onValueChange = { fbZip = it },
                                        label = { Text("ZIP") },
                                        placeholder = { Text("30309") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                                        modifier = Modifier
                                            .weight(0.8f)
                                            .height(56.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true,
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                        )
                                    )
                                }
                            }
                        }

                        // Operational scale
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Pantry Distribution Capacity (Weekly Families):",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Specifies your facility's operational scale to match donor supply expectations.",
                                fontSize = 9.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                modifier = Modifier.padding(bottom = 2.dp)
                            )
                        }

                        val sizes = listOf("Small (<100/wk)", "Medium (100-500/wk)", "Large (500+/wk)")
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            sizes.forEach { item ->
                                val active = fbSize == item
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (active) MaterialTheme.colorScheme.primaryContainer else Color(0xFFF8FAFC))
                                        .border(
                                            1.1.dp,
                                            if (active) MaterialTheme.colorScheme.primary else Color(0xFFE2E8F0),
                                            RoundedCornerShape(10.dp)
                                        )
                                        .clickable { fbSize = item }
                                        .padding(vertical = 10.dp, horizontal = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = item,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (active) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 2.dp), color = Color(0xFFF1F5F9))

                        // Operational Hours Structured Selection
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Weekly Operating Days:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            val daysList = listOf("Mon-Fri", "Mon-Sat", "Weekends", "Daily", "By Appt Only")
                            LazyRow(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(daysList) { item ->
                                    val active = opDaysSelection == item
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (active) MaterialTheme.colorScheme.secondaryContainer else Color(0xFFF8FAFC))
                                            .border(
                                                1.dp,
                                                if (active) MaterialTheme.colorScheme.secondary else Color(0xFFE2E8F0),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { opDaysSelection = item }
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = item,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (active) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Daily Operating Hours:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            val hoursPresetList = listOf("9 AM - 5 PM", "8 AM - 12 PM", "12 PM - 6 PM", "Custom Hours")
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                hoursPresetList.forEach { item ->
                                    val active = opHoursSelection == item
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (active) MaterialTheme.colorScheme.secondaryContainer else Color(0xFFF8FAFC))
                                            .border(
                                                1.dp,
                                                if (active) MaterialTheme.colorScheme.secondary else Color(0xFFE2E8F0),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable { opHoursSelection = item }
                                            .padding(vertical = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = item,
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (active) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        if (opHoursSelection == "Custom Hours") {
                            OutlinedTextField(
                                value = opCustomHours,
                                onValueChange = { opCustomHours = it },
                                label = { Text("E.g., 7:30 AM - 11 AM, 2 PM - 6 PM") },
                                leadingIcon = { Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(16.dp)) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                keyboardActions = KeyboardActions(
                                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                                )
                            )
                        }

                        OutlinedTextField(
                            value = opHoursNotes,
                            onValueChange = { opHoursNotes = it },
                            label = { Text("Holiday Exceptions & General Notes") },
                            placeholder = { Text("e.g., Closed on Christmas day. Closed during extreme weather.") },
                            leadingIcon = { Icon(Icons.Default.Notes, null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            )
                        )

                        // Cold Storage Check
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.03f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AcUnit,
                                        contentDescription = "Cold Storage",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "Refrigerated Cold Storage Available",
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Check this if you can accept fresh/frozen items",
                                            fontSize = 9.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Switch(
                                    checked = fbColdStorage,
                                    onCheckedChange = { fbColdStorage = it }
                                )
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0xFFF1F5F9))
                }

                // Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email, 
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("auth_email_field")
                        .autofill(
                            autofillTypes = listOf(AutofillType.EmailAddress),
                            onFill = { email = it }
                        ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password, 
                        imeAction = ImeAction.Done
                    ),
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("auth_password_field")
                        .autofill(
                            autofillTypes = listOf(AutofillType.Password),
                            onFill = { password = it }
                        ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    )
                )

                if (isSignUp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Checkbox(
                            checked = agreedToTerms,
                            onCheckedChange = { agreedToTerms = it },
                            modifier = Modifier.testTag("auth_terms_checkbox")
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("I agree to the ", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "Terms of Service",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { onViewTerms() }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Button(
                        onClick = {
                            if (isSignUp && !agreedToTerms) {
                                errorMessage = "You must agree to the Terms of Service to register."
                                return@Button
                            }
                            loading = true
                            errorMessage = null
                            if (isSignUp) {
                                val calculatedFbHours = if (role == "Food Bank") {
                                    buildString {
                                        append(opDaysSelection)
                                        append(" ")
                                        if (opHoursSelection == "Custom Hours") {
                                            if (opCustomHours.isNotBlank()) append(opCustomHours) else append("Flexible Hours")
                                        } else {
                                            append(opHoursSelection)
                                        }
                                        if (opHoursNotes.isNotBlank()) {
                                            append(" (Notes: ")
                                            append(opHoursNotes)
                                            append(")")
                                        }
                                    }
                                } else ""

                                viewModel.signUp(
                                    email = email,
                                    password = password,
                                    role = role,
                                    name = if (role == "Donor") "$donorFirstName $donorLastName".trim() else name,
                                    phone = phone,
                                    fbAddress = fbAddress,
                                    fbCity = fbCity,
                                    fbZip = fbZip,
                                    fbSize = fbSize,
                                    fbHours = calculatedFbHours,
                                    fbColdStorage = fbColdStorage,
                                    donorZip = donorZip,
                                    donorCity = donorCity,
                                    donorCanServeType = donorCanServeType,
                                    donorCanServeQty = donorCanServeQty,
                                    donorFrequency = donorFrequency
                                ) { success, msg ->
                                    loading = false
                                    if (!success) errorMessage = msg
                                }
                            } else {
                                viewModel.signIn(email, password) { success, msg ->
                                    loading = false
                                    if (!success) errorMessage = msg
                                }
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("auth_submit_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = if (isSignUp) "Register now" else "Sign In",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    TextButton(
                        onClick = {
                            isSignUp = !isSignUp
                            errorMessage = null
                        }
                    ) {
                        Text(
                            text = if (isSignUp) "Already have an account? Sign In" else "Don't have an account yet? Create one",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                    ) {
                        Text("By using this service, you agree to our ", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "Terms of Service",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { onViewTerms() }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TermsOfServiceDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Terms Icon",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = "Terms of Service",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Effective: March 22nd, 2026",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxHeight(0.70f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Intro Header info
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("App Name: PantryLink Georgia", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Company: PantryLink Georgia, Inc.", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }

                Text(
                    text = "These Terms of Service, together with any documents expressly incorporated by reference, govern your access to and use of the PantryLink Georgia mobile application, website, and related services (collectively, the “Service”). By accessing, downloading, creating an account for, or using the Service, you agree to be bound by these Terms of Service (the “Terms”). If you do not agree to these Terms, you may not use the Service.",
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 16.sp
                )

                val termsSections = listOf(
                    "1. What the Service Is" to "PantryLink Georgia is a technology platform designed to help connect users with participating food banks, pantries, and similar nonprofit community partners (“Food Bank Partners”) by displaying requested items and allowing users to claim, purchase, and deliver those items.\nPantryLink Georgia, Inc. provides coordination and communication tools only. Except where expressly stated in writing, PantryLink Georgia, Inc. does not manufacture, inspect, store, transport, handle, prepare, distribute, or guarantee donated goods.",
                    "2. Eligibility" to "To use the Service, you must be legally capable of entering into a binding agreement. By using the Service, you represent and warrant that:\n• you are at least eighteen (18) years of age, or you are using the Service under the supervision and with the permission of a parent or legal guardian where permitted by law;\n• you are not prohibited from using the Service under applicable law;\n• the information you provide to us is accurate, current, and complete.\nWe reserve the right to deny access to any person or entity at our discretion.",
                    "3. User Accounts" to "Certain features of the Service require an account. When creating an account, you agree to provide accurate and complete registration information and to keep that information current.\nYou are solely responsible for:\n• maintaining the confidentiality of your login credentials;\n• all activity that occurs under your account;\n• notifying us promptly of any unauthorized use of your account or other security breach.\nWe may suspend, restrict, or terminate your account if we determine, in our sole discretion, that you have violated these Terms, provided false information, or used the Service in an improper, unlawful, or harmful manner.",
                    "4. Food Bank Partner Accounts" to "Only approved and verified Food Bank Partners may create item requests through the Service. Each Food Bank Partner represents and warrants that it is authorized to request and receive the items it lists through the Service.\nFood Bank Partners are solely responsible for:\n• ensuring that posted requests are accurate and current;\n• listing only items they are authorized and prepared to receive;\n• updating quantities, deadlines, and instructions as needed;\n• confirming whether delivered items are accepted or rejected.\nPantryLink Georgia, Inc. may verify, reject, suspend, or remove any Food Bank Partner account or request at any time and for any reason.",
                    "5. Requests and Matching" to "The Service enables Food Bank Partners to post requests for items such as shelf-stable food, hygiene items, baby supplies, school supplies, and other approved categories.\nEach request may include:\n• item description;\n• category;\n• quantity needed;\n• drop-off location;\n• deadline;\n• pantry notes or handling instructions;\n• request status.\nUsers may browse and claim listed requests. A request shall not be deemed completed, fulfilled, satisfied, or closed unless and until the relevant Food Bank Partner confirms receipt through the Service.\nPantryLink Georgia, Inc. reserves the right to standardize request categories, item descriptions, quantities, status labels, and workflows in order to improve accuracy, consistency, and usability.",
                    "6. No Guarantee of Fulfillment or Acceptance" to "Use of the Service does not guarantee that any request will be fulfilled or that any donation will be accepted.\nWithout limitation, PantryLink Georgia, Inc. does not guarantee:\n• that any listed request will be fulfilled;\n• that any claimed item will be purchased or delivered;\n• that any delivered item will be accepted by a Food Bank Partner;\n• that any request, listing, quantity, status, or timeline is error-free, complete, or current;\n• that any Food Bank Partner will continue to participate in the Service.\nFood Bank Partners may reject items that are opened, expired, damaged, unsafe, mislabeled, unapproved, or otherwise unsuitable in their sole discretion.",
                    "7. Donations and Deliveries" to "Any decision to purchase, transport, or deliver items in response to a request is voluntary and made at your own discretion and risk.\nBy using the Service as a donor or contributor, you acknowledge and agree that:\n• you are solely responsible for the condition, legality, suitability, and transport of any item you deliver;\n• title, risk, and responsibility for the item remain with you until the item is accepted by the Food Bank Partner;\n• PantryLink Georgia, Inc. is not a party to the transfer, acceptance, rejection, storage, handling, or ultimate distribution of any donated item unless expressly stated otherwise in writing;\n• Food Bank Partners may impose separate operational and intake requirements, and you are responsible for complying with those requirements.\nYou may not use the Service to deliver:\n• opened items;\n• expired items;\n• items with missing, altered, or misleading labels;\n• illegal, hazardous, or prohibited items;\n• perishable or specially handled items unless specifically requested and permitted by the applicable Food Bank Partner.",
                    "8. Statuses and Notifications" to "The Service may display operational statuses, including but not limited to “Posted,” “Claimed,” “Dropped Off,” “Confirmed,” and “Closed.” These labels are for administrative convenience only and are based on information submitted through the Service.\nYou acknowledge and agree that:\n• statuses may be delayed, inaccurate, incomplete, or later revised;\n• a user indication that an item has been purchased or delivered does not constitute confirmation of acceptance by a Food Bank Partner;\n• a request is not considered fulfilled until the applicable Food Bank Partner confirms receipt through the Service.\nWe may send push notifications, email communications, text messages where enabled, and in-app notices relating to:\n• nearby Food Bank Partners;\n• open or urgent requests;\n• claimed items;\n• request changes;\n• confirmations or rejections;\n• account, policy, legal, operational, or security matters.",
                    "9. Location Features" to "The Service may request access to your location in order to:\n• identify nearby Food Bank Partners;\n• sort requests by proximity;\n• assist with navigation and logistics.\nIf you decline location access, certain features may be limited or unavailable, and the Service may instead rely on information such as ZIP code, city, or manually entered address.\nBy enabling location services, you consent to our collection and use of location information as described in our Privacy Policy. PantryLink Georgia, Inc. does not guarantee the accuracy of location-based matching, maps, directions, or travel estimates.",
                    "10. Privacy" to "Your use of the Service is also governed by our Privacy Policy, which is incorporated into these Terms by reference. By using the Service, you consent to the collection, use, storage, and disclosure of information as described in the Privacy Policy.",
                    "11. Acceptable Use" to "You agree not to use the Service in any manner that is unlawful, fraudulent, deceptive, abusive, harmful, or inconsistent with these Terms.\nWithout limitation, you agree not to:\n• impersonate any person, organization, or Food Bank Partner;\n• submit false, misleading, or inaccurate account information, requests, claims, or confirmations;\n• manipulate quantities, request statuses, deadlines, or other platform records;\n• deliver items that are unsafe, prohibited, or materially inconsistent with the listed request;\n• interfere with or disrupt the Service, its servers, or networks;\n• introduce malware, malicious code, or other harmful technologies into the Service;\n• scrape, copy, reverse engineer, or exploit the Service without authorization;\n• harass, threaten, or abuse any user, Food Bank Partner, employee, contractor, or representative;\n• collect personal information about others through the Service without authorization;\n• use the Service for advertising, solicitation, or unrelated commercial activity not expressly authorized by us.\nWe reserve the right to investigate any suspected violation of these Terms and to take any action we deem appropriate.",
                    "12. Third-Party Services" to "The Service may integrate with or link to third-party services, including mapping services, messaging services, hosting providers, analytics providers, payment processors, or application marketplaces.\nPantryLink Georgia, Inc. does not control and is not responsible for:\n• the content, availability, security, or performance of third-party services;\n• any act or omission of any third party;\n• any damage or loss caused by your use of or reliance on third-party services.\nYour use of third-party services may be governed by separate terms and policies.",
                    "13. No Emergency Service" to "The Service is not an emergency response service. The Service is not intended for urgent food assistance dispatch, crisis intervention, medical response, or any situation in which immediate action is required.\nIf you or another person requires emergency assistance, call 911 or contact the appropriate emergency service provider.",
                    "14. No Professional Advice" to "The Service does not provide medical, nutritional, dietary, tax, accounting, legal, or professional advice of any kind. Any information made available through the Service is for general informational purposes only and should not be relied upon as a substitute for professional advice.",
                    "15. Intellectual Property" to "All rights, title, and interest in and to the Service, including all software, code, designs, text, graphics, logos, trademarks, service marks, features, and functionality, are and shall remain the exclusive property of PantryLink Georgia, Inc. or its licensors.\nSubject to your compliance with these Terms, PantryLink Georgia, Inc. grants you a limited, revocable, non-exclusive, non-transferable license to access and use the Service solely for its intended purpose.\nYou may not:\n• reproduce, modify, distribute, publish, display, transmit, sell, license, or otherwise exploit any part of the Service without our prior written consent;\n• remove or alter any proprietary notices;\n• create derivative works from the Service;\n• use the Service for any unauthorized commercial purpose.",
                    "16. User Content" to "You may submit or upload content through the Service, including account information, request-related communications, confirmations, notes, support messages, photographs, and other materials (“User Content”).\nYou retain ownership of your User Content. However, by submitting User Content, you grant PantryLink Georgia, Inc. a non-exclusive, worldwide, royalty-free, sublicensable license to host, store, reproduce, use, display, transmit, modify for formatting purposes, and otherwise process such User Content as reasonably necessary to operate, maintain, improve, and promote the Service.\nYou represent and warrant that:\n• you own or control the rights to the User Content you submit, or you otherwise have the lawful right to provide it;\n• your User Content does not violate applicable law, these Terms, or the rights of any third party;\n• your User Content is not knowingly false, deceptive, defamatory, or harmful.\nWe may remove, restrict, or refuse User Content at any time and for any reason.",
                    "17. Contributions and Tax Matters" to "If the Service permits users to contribute goods, money, or services, you acknowledge that:\n• all contributions are voluntary;\n• PantryLink Georgia, Inc. does not guarantee that any contribution is tax-deductible;\n• the tax treatment of any contribution, if any, depends on the facts of the transaction, the recipient, and applicable law;\n• you are solely responsible for obtaining any tax, legal, or accounting advice relating to your contribution.",
                    "18. Suspension and Termination" to "We may suspend, limit, restrict, or terminate your access to the Service at any time, with or without notice, if we believe, in our sole discretion, that:\n• you have violated these Terms;\n• your conduct creates legal, reputational, security, operational, or safety risk;\n• your account information is false, incomplete, or misleading;\n• your continued access is not in the best interests of PantryLink Georgia, Inc., its users, or its Food Bank Partners;\n• suspension or termination is required by law or requested by a governmental authority.\nYou may stop using the Service at any time. Termination of your access shall not affect any rights, obligations, or provisions that by their nature should survive termination.",
                    "19. Disclaimers" to "THE SERVICE IS PROVIDED ON AN “AS IS” AND “AS AVAILABLE” BASIS.\nTO THE MAXIMUM EXTENT PERMITTED BY LAW, PANTRYLINK GEORGIA, INC. DISCLAIMS ALL WARRANTIES, EXPRESS, IMPLIED, STATUTORY, OR OTHERWISE, INCLUDING WITHOUT LIMITATION ANY WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, TITLE, NON-INFRINGEMENT, ACCURACY, RELIABILITY, SECURITY, AVAILABILITY, OR UNINTERRUPTED OPERATION.\nWITHOUT LIMITING THE FOREGOING, PANTRYLINK GEORGIA, INC. DOES NOT WARRANT THAT:\n• THE SERVICE WILL BE AVAILABLE AT ANY PARTICULAR TIME OR LOCATION;\n• THE SERVICE WILL BE SECURE, ERROR-FREE, OR UNINTERRUPTED;\n• ANY REQUEST, LISTING, STATUS, OR COMMUNICATION WILL BE ACCURATE OR CURRENT;\n• ANY DONATION WILL BE ACCEPTED, USED, DISTRIBUTED, OR ACKNOWLEDGED IN ANY PARTICULAR MANNER;\n• ANY DEFECT OR ERROR WILL BE CORRECTED.",
                    "20. Limitation of Liability" to "TO THE MAXIMUM EXTENT PERMITTED BY LAW, PANTRYLINK GEORGIA, INC., AND ITS OFFICERS, DIRECTORS, EMPLOYEES, CONTRACTORS, VOLUNTEERS, AFFILIATES, LICENSORS, AND AGENTS, SHALL NOT BE LIABLE FOR ANY INDIRECT, INCIDENTAL, SPECIAL, CONSEQUENTIAL, EXEMPLARY, OR PUNITIVE DAMAGES, OR FOR ANY LOSS OF PROFITS, REVENUE, GOODWILL, BUSINESS OPPORTUNITY, DATA, OR OTHER INTANGIBLE LOSSES, ARISING OUT OF OR RELATING TO THE SERVICE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.\nTO THE MAXIMUM EXTENT PERMITTED BY LAW, THE TOTAL AGGREGATE LIABILITY OF PANTRYLINK GEORGIA, INC. FOR ALL CLAIMS ARISING OUT OF OR RELATING TO THE SERVICE OR THESE TERMS SHALL NOT EXCEED THE GREATER OF:\n• ONE HUNDRED U.S. DOLLARS ($100.00); OR\n• THE AMOUNT, IF ANY, PAID BY YOU TO PANTRYLINK GEORGIA, INC. DURING THE TWELVE (12) MONTHS PRECEDING THE EVENT GIVING RISE TO THE CLAIM.\nTHE LIMITATIONS IN THIS SECTION SHALL APPLY REGARDLESS OF THE THEORY OF LIABILITY AND EVEN IF ANY REMEDY FAILS OF ITS EMBEDDED PURPOSE.",
                    "21. Indemnification" to "You agree to defend, indemnify, and hold harmless PantryLink Georgia, Inc., and its officers, directors, employees, contractors, volunteers, affiliates, licensors, and agents, from and against any and all claims, demands, actions, proceedings, damages, losses, liabilities, judgments, settlements, costs, and expenses, including reasonable attorneys’ fees, arising out of or relating to:\n• your access to or use of the Service;\n• your donations, deliveries, transportation, handling, or submission of items;\n• your User Content;\n• your violation of these Terms;\n• your violation of applicable law or the rights of any third party.",
                    "22. Governing Law" to "These Terms and any dispute arising out of or relating to the Service or these Terms shall be governed by and construed in accordance with the laws of the State of Georgia, without regard to its conflict of law principles, except to the extent federal law governs.",
                    "23. Dispute Resolution and Venue" to "Before filing any claim, you agree to attempt to resolve the dispute informally by contacting PantryLink Georgia, Inc. at pantrylinkgeorgia@gmail.com and providing a written description of the issue.\nIf the dispute cannot be resolved informally, any action arising out of or relating to these Terms or the Service shall be brought exclusively in the state or federal courts located in Georgia, and each party irrevocably submits to the personal jurisdiction and venue of those courts.",
                    "24. Electronic Communications" to "By using the Service, you consent to receive communications from PantryLink Georgia, Inc. electronically, including by email, push notification, text message where enabled, or through the Service itself.\nYou agree that all agreements, notices, disclosures, and other communications provided electronically satisfy any legal requirement that such communications be in writing.",
                    "25. Changes to the Service" to "PantryLink Georgia, Inc. may modify, suspend, discontinue, restrict, or remove any part of the Service at any time, with or without notice, and without liability to you.",
                    "26. Changes to These Terms" to "We may amend these Terms from time to time. Updated Terms will become effective when posted unless otherwise stated. Your continued use of the Service after revised Terms become effective constitutes your acceptance of the revised Terms.",
                    "27. Severability" to "If any provision of these Terms is held to be invalid, unlawful, or unenforceable, that provision shall be enforced to the maximum extent permitted by law, and the remaining provisions shall remain in full force and effect.",
                    "28. Assignment" to "You may not assign, transfer, delegate, or sublicense any rights or obligations under these Terms without our prior written consent. PantryLink Georgia, Inc. may assign or transfer these Terms, in whole or in part, without restriction, including in connection with a merger, acquisition, corporate reorganization, or sale of assets.",
                    "29. Entire Agreement" to "These Terms, together with the Privacy Policy and any other documents expressly incorporated by reference, constitute the entire agreement between you and PantryLink Georgia, Inc. with respect to the Service and supersede all prior or contemporaneous understandings, agreements, communications, and proposals relating to the Service.",
                    "30. Contact Information" to "If you have questions regarding these Terms, you may contact:\nPantryLink Georgia, Inc.\n12 Peachtree St NW\nAtlanta, GA\n470-209-1835\npantrylinkgeorgia@gmail.com",
                    "31. Additional Terms for Food Bank Partners" to "If you use the Service as a Food Bank Partner, you further represent, warrant, and agree that:\n• you are authorized to request and receive the items listed through the Service;\n• you will post requests in good faith and will not knowingly post false or misleading needs;\n• you remain solely responsible for your own intake procedures, storage, handling, distribution, staffing, and site operations;\n• you may accept or reject items in your sole discretion;\n• PantryLink Georgia, Inc. is not responsible for your internal policies, distributions, operations, or compliance obligations.",
                    "32. Additional Terms for Donors and Contributors" to "If you use the Service as a donor or contributor, you further represent, warrant, and agree that:\n• you are solely responsible for the condition, legality, transport, and delivery of any item you provide;\n• you will comply with all posted instructions and all applicable laws;\n• you will not falsely indicate that a donation has been completed or accepted;\n• you will not rely on the Service as proof of tax deductibility, legal compliance, or acceptance of any item;\n• you are solely responsible for your travel, transportation, parking, and delivery-related decisions and risks.",
                    "33. App Marketplace Terms" to "If you download or access the Service through the Apple App Store, Google Play, or another application marketplace, you acknowledge and agree that:\n• these Terms are between you and PantryLink Georgia, Inc., and not with the marketplace operator;\n• the marketplace operator is not responsible for the Service;\n• your use of the Service may also be subject to the applicable marketplace’s own terms, conditions, and policies."
                )

                termsSections.forEach { (heading, content) ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = heading,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        Text(
                            text = content,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Close", fontWeight = FontWeight.Bold)
            }
        },
        shape = RoundedCornerShape(24.dp),
        tonalElevation = 6.dp
    )
}

@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.autofill(
    autofillTypes: List<AutofillType>,
    onFill: (String) -> Unit
): Modifier = composed {
    val autofill = LocalAutofill.current
    val autofillNode = remember {
        AutofillNode(
            onFill = onFill,
            autofillTypes = autofillTypes
        )
    }
    
    LocalAutofillTree.current += autofillNode
    
    this.onGloballyPositioned { coordinates ->
        autofillNode.boundingBox = coordinates.boundsInWindow()
    }.onFocusChanged { focusState ->
        autofill?.let {
            if (focusState.isFocused) {
                it.requestAutofillForNode(autofillNode)
            } else {
                it.cancelAutofillForNode(autofillNode)
            }
        }
    }
}

@Composable
fun FBProfileTab(viewModel: PantryLinkViewModel) {
    val userSession by viewModel.userSession.collectAsStateWithLifecycle()
    val currentUserProfile by viewModel.currentUserProfile.collectAsStateWithLifecycle()

    val activeEmail = userSession?.email ?: "npatel012010@gmail.com"
    var fbName by remember { mutableStateOf("") }
    var fbPhone by remember { mutableStateOf("") }
    var fbAddress by remember { mutableStateOf("") }
    var fbCity by remember { mutableStateOf("") }
    var fbZip by remember { mutableStateOf("") }
    var fbSize by remember { mutableStateOf("Medium (100-500/wk)") }
    var fbHours by remember { mutableStateOf("Mon-Fri 9 AM - 5 PM") }
    var fbColdStorage by remember { mutableStateOf(false) }

    var isInitialized by remember { mutableStateOf(false) }

    var showFbAddressSuggestions by remember { mutableStateOf(false) }
    var addressSearchLoading by remember { mutableStateOf(false) }
    var addressSuggestions by remember { mutableStateOf<List<AutocompletePrediction>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    val placesApiKey = BuildConfig.PLACES_API_KEY
    val isApiKeyPresent = GooglePlacesClient.isApiKeyValid(placesApiKey)

    LaunchedEffect(fbAddress, showFbAddressSuggestions) {
        if (fbAddress.isBlank() || !showFbAddressSuggestions) {
            addressSuggestions = emptyList()
            return@LaunchedEffect
        }
        
        // Debounce input to reduce unnecessary api calls
        kotlinx.coroutines.delay(200)
        
        addressSearchLoading = true
        try {
            if (isApiKeyPresent) {
                val response = GooglePlacesClient.service.autocomplete(
                    input = fbAddress,
                    apiKey = placesApiKey,
                    components = "country:us"
                )
                addressSuggestions = response.predictions
            } else {
                addressSuggestions = GooglePlacesClient.mockAutocomplete(fbAddress)
            }
        } catch (e: Exception) {
            android.util.Log.e("PantryLinkAuth", "Places Autocomplete request failed", e)
            addressSuggestions = GooglePlacesClient.mockAutocomplete(fbAddress)
        } finally {
            addressSearchLoading = false
        }
    }

    val selectSuggestion: (AutocompletePrediction) -> Unit = { prediction ->
        showFbAddressSuggestions = false
        addressSearchLoading = true
        coroutineScope.launch {
            try {
                val detailsResponse = if (isApiKeyPresent) {
                    GooglePlacesClient.service.getDetails(
                        placeId = prediction.place_id,
                        apiKey = placesApiKey,
                        fields = "address_components"
                    )
                } else {
                    GooglePlacesClient.mockDetails(prediction.place_id)
                }
                
                val result = detailsResponse.result
                if (result != null) {
                    var streetNumber = ""
                    var route = ""
                    var city = ""
                    var state = ""
                    var zip = ""
                    
                    for (comp in result.address_components) {
                        val types = comp.types
                        when {
                            types.contains("street_number") -> streetNumber = comp.long_name
                            types.contains("route") -> route = comp.long_name
                            types.contains("locality") -> city = comp.long_name
                            types.contains("administrative_area_level_1") -> state = comp.short_name
                            types.contains("postal_code") -> zip = comp.long_name
                        }
                    }
                    
                    val formattedStreet = if (streetNumber.isNotBlank() && route.isNotBlank()) {
                        "$streetNumber $route"
                    } else if (route.isNotBlank()) {
                        route
                    } else {
                        streetNumber.ifBlank { prediction.structured_formatting?.main_text ?: "" }
                    }
                    
                    fbAddress = formattedStreet
                    fbCity = if (city.isNotBlank() && state.isNotBlank()) "$city, $state" else city.ifBlank { state }
                    fbZip = zip
                }
            } catch (e: Exception) {
                android.util.Log.e("PantryLinkAuth", "Places Details request failed", e)
            } finally {
                addressSearchLoading = false
            }
        }
    }

    // Initialize edit fields from currentUserProfile
    LaunchedEffect(currentUserProfile, isInitialized) {
        if (!isInitialized && currentUserProfile != null) {
            fbName = currentUserProfile?.get("name") as? String ?: ""
            fbPhone = currentUserProfile?.get("phone") as? String ?: ""
            fbAddress = currentUserProfile?.get("fbAddress") as? String ?: ""
            fbCity = currentUserProfile?.get("fbCity") as? String ?: ""
            fbZip = currentUserProfile?.get("fbZip") as? String ?: ""
            fbSize = currentUserProfile?.get("fbSize") as? String ?: "Medium (100-500/wk)"
            fbHours = currentUserProfile?.get("fbHours") as? String ?: "Mon-Fri 9 AM - 5 PM"
            fbColdStorage = currentUserProfile?.get("fbColdStorage") as? Boolean ?: false
            isInitialized = true
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Food Bank Header Info Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth().testTag("fb_profile_header_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFDBEAFE), // blue-100
                            modifier = Modifier.size(56.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = if (fbName.length >= 2) fbName.take(2).uppercase() else "FB",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF1E3A8A) // blue-900
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (fbName.isNotBlank()) fbName else "Agency Profile",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = "Verified Agency Partner",
                                    tint = Color(0xFF2563EB),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = activeEmail,
                                fontSize = 13.sp,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFEFF6FF),
                                modifier = Modifier.wrapContentSize()
                            ) {
                                Text(
                                    text = "OFFICIAL GEORGIA FOOD BANK PARTNER",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1D4ED8),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                elevation = CardDefaults.cardElevation(1.dp),
                modifier = Modifier.fillMaxWidth().testTag("fb_profile_edit_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Edit Agency Profile Information",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )

                    OutlinedTextField(
                        value = fbName,
                        onValueChange = { fbName = it },
                        label = { Text("Food Bank / Agency Name") },
                        leadingIcon = { Icon(Icons.Default.Storefront, null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = fbPhone,
                        onValueChange = { fbPhone = it },
                        label = { Text("Contact Phone") },
                        leadingIcon = { Icon(Icons.Default.Phone, null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = fbAddress,
                        onValueChange = { 
                            fbAddress = it
                            showFbAddressSuggestions = true
                        },
                        label = { Text("Location Address") },
                        leadingIcon = { Icon(Icons.Default.Home, null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.fillMaxWidth().testTag("fb_profile_address_input"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    if (showFbAddressSuggestions && fbAddress.isNotBlank()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        ) {
                            Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "ADDRESS SUGGESTIONS",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    if (addressSearchLoading) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(10.dp),
                                            strokeWidth = 1.5.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        Text(
                                            text = "PantryLink Address Helper",
                                            fontSize = 8.sp,
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

                                if (addressSuggestions.isEmpty() && !addressSearchLoading) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No addresses found. Type to search.",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                        )
                                    }
                                } else {
                                    addressSuggestions.forEachIndexed { index, prediction ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable {
                                                    selectSuggestion(prediction)
                                                }
                                                .padding(horizontal = 12.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = if (isApiKeyPresent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Column {
                                                Text(
                                                    text = prediction.structured_formatting?.main_text ?: prediction.description,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                prediction.structured_formatting?.secondary_text?.let { secText ->
                                                    Text(
                                                        text = secText,
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                    )
                                                }
                                            }
                                        }
                                        if (index < addressSuggestions.size - 1) {
                                            HorizontalDivider(
                                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                                                modifier = Modifier.padding(horizontal = 8.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = fbCity,
                            onValueChange = { fbCity = it },
                            label = { Text("City (GA)") },
                            modifier = Modifier.weight(1.2f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = fbZip,
                            onValueChange = { fbZip = it },
                            label = { Text("ZIP Code") },
                            modifier = Modifier.weight(0.8f),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = fbHours,
                        onValueChange = { fbHours = it },
                        label = { Text("Operating Hours") },
                        leadingIcon = { Icon(Icons.Default.Schedule, null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    Text(
                        text = "Weekly Load Capacity Size:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val sizes = listOf("Small (<100/wk)", "Medium (100-500/wk)", "Large (500+/wk)")
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        sizes.forEach { sizeItem ->
                            val active = fbSize == sizeItem
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (active) MaterialTheme.colorScheme.primaryContainer else Color(0xFFF8FAFC))
                                    .border(
                                        1.dp,
                                        if (active) MaterialTheme.colorScheme.primary else Color(0xFFE2E8F0),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { fbSize = sizeItem }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = sizeItem.substringBefore(" ("),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.AcUnit, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Column {
                                Text("Cold Storage Capability", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                                Text("Equipped with refrigeration / freezers", fontSize = 10.5.sp, color = Color(0xFF64748B))
                            }
                        }
                        Switch(
                            checked = fbColdStorage,
                            onCheckedChange = { fbColdStorage = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            viewModel.updateProfile(
                                name = fbName,
                                phone = fbPhone,
                                fbAddress = fbAddress,
                                fbCity = fbCity,
                                fbZip = fbZip,
                                fbSize = fbSize,
                                fbHours = fbHours,
                                fbColdStorage = fbColdStorage
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("fb_profile_save_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Save, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Profile Changes", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
