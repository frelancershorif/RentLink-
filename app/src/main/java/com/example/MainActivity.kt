package com.example

import android.app.Application
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.*
import com.example.ui.theme.RentLinkTheme
import com.example.ui.RentLinkViewModel
import com.example.ui.RentLinkViewModelFactory
import com.example.ui.components.RentLinkLogoCard
import com.example.ui.components.RentLinkLogoIcon
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            RentLinkTheme {
                RentLinkAppContainer()
            }
        }
    }
}

// NAVIGATION SCREENS
sealed interface Screen {
    object Splash : Screen
    object RoleSelection : Screen
    object Login : Screen
    object MainApp : Screen
}

// BOTTOM TABS
enum class BottomTab(val label: String, val icon: ImageVector, val tag: String) {
    HOME("Home", Icons.Default.Home, "tab_home"),
    HISTORY("History", Icons.Default.ReceiptLong, "tab_history"),
    ADD("Add", Icons.Default.AddCircle, "tab_add"),
    ALERTS("Alerts", Icons.Default.NotificationsActive, "tab_alerts"),
    PROFILE("Profile", Icons.Default.Person, "tab_profile")
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RentLinkAppContainer() {
    val context = LocalContext.current
    val viewModel: RentLinkViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        factory = RentLinkViewModelFactory(context.applicationContext as Application)
    )

    val splashFinished by viewModel.splashFinished.collectAsStateWithLifecycle()
    val isOnboarded by viewModel.isOnboarded.collectAsStateWithLifecycle()
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle()

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Splash) }

    // Direct routing state controller
    LaunchedEffect(splashFinished, isOnboarded, isLoggedIn) {
        currentScreen = when {
            !splashFinished -> Screen.Splash
            !isOnboarded -> Screen.RoleSelection
            !isLoggedIn -> Screen.Login
            else -> Screen.MainApp
        }
    }

    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            fadeIn(animationSpec = tween(500)) with fadeOut(animationSpec = tween(400))
        },
        label = "screen_transitions"
    ) { screen ->
        when (screen) {
            Screen.Splash -> SplashScreen(onFinished = { viewModel.finishSplash() })
            Screen.RoleSelection -> RoleSelectionScreen(onRoleSelected = { role ->
                viewModel.selectRole(role)
            })
            Screen.Login -> LoginScreen(
                role = viewModel.currentUserRole.collectAsStateWithLifecycle().value,
                onLoginSuccess = { mobile, name, selectedRole ->
                    viewModel.login(mobile, name, selectedRole)
                }
            )
            Screen.MainApp -> MainAppScreen(viewModel = viewModel)
        }
    }
}

// ==========================================
// 1. SPLASH SCREEN
// ==========================================
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    var logoAlpha by remember { mutableStateOf(0f) }
    var logoScale by remember { mutableStateOf(0.6f) }
    var textVisible by remember { mutableStateOf(false) }
    var glowLight by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        // Step 1: Fade-in Logo
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(800, easing = LinearOutSlowInEasing)
        ) { val1, _ -> logoAlpha = val1 }

        // Step 2: Scale Animation
        animate(
            initialValue = 0.6f,
            targetValue = 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        ) { val2, _ -> logoScale = val2 }

        // Step 3: Glow light glow pulse
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(600, easing = FastOutSlowInEasing)
        ) { val3, _ -> glowLight = val3 }

        // Step 4: Text reveal
        textVisible = true
        delay(1200)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)), // Deep Premium Slate Dark
        contentAlignment = Alignment.Center
    ) {
        // Glowing Background Pulse
        Box(
            modifier = Modifier
                .size(400.dp)
                .alpha(glowLight * 0.25f)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(Color(0xFF00BBFF), Color.Transparent),
                        radius = 600f
                    )
                )
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            RentLinkLogoCard(
                modifier = Modifier
                    .size(150.dp)
                    .scale(logoScale)
                    .alpha(logoAlpha),
                elevation = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(
                visible = textVisible,
                enter = expandVertically(animationSpec = tween(600)) + fadeIn(animationSpec = tween(600))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "RentLink",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Connect. Pay. Live.",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF00BBFF),
                        fontFamily = FontFamily.SansSerif,
                        letterSpacing = 2.sp
                    )
                }
            }
        }
    }
}

// ==========================================
// 2. ROLE SELECTION SCREEN
// ==========================================
@Composable
fun RoleSelectionScreen(onRoleSelected: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(30.dp))
            RentLinkLogoIcon(modifier = Modifier.size(70.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Welcome to RentLink",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Choose your role to customize transaction models and digital lease ledgers.",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Landlord Role Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(135.dp)
                    .testTag("onboard_landlord_card")
                    .clickable { onRoleSelected("Landlord") },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFE0F7FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = "Landlord Image",
                            tint = Color(0xFF1976D3),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(18.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "I am a Landlord",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Manage properties, collect rent, track lease timelines, and run maintenance.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF64748B),
                            lineHeight = 16.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Next",
                        tint = Color(0xFF00BBFF)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Tenant Role Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(135.dp)
                    .testTag("onboard_tenant_card")
                    .clickable { onRoleSelected("Tenant") },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFE3F2FD)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Tenant Image",
                            tint = Color(0xFF1976D3),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(18.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "I am a Tenant",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Find marketplace units, secure digital payments, view ledger histories, and raise requests.",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Normal,
                            color = Color(0xFF64748B),
                            lineHeight = 16.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Next",
                        tint = Color(0xFF00BBFF)
                    )
                }
            }
        }

        Text(
            text = "By choosing, you agree to secure Ledger Terms of Use",
            fontSize = 11.sp,
            color = Color(0xFF94A3B8),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )
    }
}

// ==========================================
// 3. AUTHENTICATION (LOGIN) SCREEN
// ==========================================
@Composable
fun LoginScreen(
    role: String,
    onLoginSuccess: (String, String, String) -> Unit
) {
    val context = LocalContext.current
    var mobileNumber by remember { mutableStateOf("01812345678") }
    var password by remember { mutableStateOf("••••••••") }
    var userName by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }

    // Auto default usernames based on mode
    if (userName.isEmpty()) {
        userName = if (role == "Tenant") "Anisur Rahman" else "Siddikur Rahman"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(26.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(15.dp))
            RentLinkLogoCard(modifier = Modifier.size(105.dp))

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = if (isSignUp) "Create Account" else "Secure Gateway Login",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Selected Role: ", fontSize = 13.sp, color = Color(0xFF64748B))
                Text(
                    text = role,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00BBFF)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            if (isSignUp) {
                OutlinedTextField(
                    value = userName,
                    onValueChange = { userName = it },
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Color(0xFF1976D3)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF00BBFF),
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("login_name_input")
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = mobileNumber,
                onValueChange = { mobileNumber = it },
                label = { Text("Mobile Number") },
                placeholder = { Text("e.g. 01812345678") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF1976D3)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00BBFF),
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_phone_input")
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("6-Digit Secret Pin") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF1976D3)) },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF00BBFF),
                    unfocusedBorderColor = Color(0xFFCBD5E1)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_password_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Forgot Password?",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1976D3),
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable {
                        Toast
                            .makeText(
                                context,
                                "OTP token reset successfully dispatched to $mobileNumber",
                                Toast.LENGTH_SHORT
                            )
                            .show()
                    }
            )

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = {
                    if (mobileNumber.length < 10) {
                        Toast.makeText(context, "Please enter valid cell number", Toast.LENGTH_SHORT).show()
                    } else {
                        onLoginSuccess(mobileNumber, userName, role)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("login_submit_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BBFF))
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.QrCode, contentDescription = null)
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (isSignUp) "Register Secure Account" else "Authorize Secure OTP Login",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (isSignUp) "Already have an account? Sign In" else "New to RentLink? Create Account",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF64748B),
                modifier = Modifier.clickable { isSignUp = !isSignUp }
            )
        }
    }
}

// ==========================================
// 4. MAIN CONTAINER (BOTTOM BAR NAV)
// ==========================================
@Composable
fun MainAppScreen(viewModel: RentLinkViewModel) {
    var selectedTab by remember { mutableStateOf(BottomTab.HOME) }

    // Drawer / Overlay flow targets
    var showSendMoney by remember { mutableStateOf(false) }
    var showWithdraw by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color(0xFFF8FAFC),
        bottomBar = {
            // SAFE AREA BOTTOM NAVIGATION BAR
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .shadow(16.dp, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding() // CRITICAL INSET SAFE OVERLAP
                            .height(72.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BottomTab.values().forEach { tab ->
                            val isSelected = selectedTab == tab
                            val activeColor = Color(0xFF1976D3)
                            val inactiveColor = Color(0xFF94A3B8)

                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .testTag(tab.tag)
                                    .clickable { selectedTab = tab },
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isSelected) Color(0xFFE0F7FF) else Color.Transparent
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.label,
                                        tint = if (isSelected) activeColor else inactiveColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Text(
                                    text = tab.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) activeColor else inactiveColor,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            when (selectedTab) {
                BottomTab.HOME -> DashboardTab(
                    viewModel = viewModel,
                    onSendMoneyRequest = { showSendMoney = true },
                    onWithdrawRequest = { showWithdraw = true },
                    onNavigateToAdd = { selectedTab = BottomTab.ADD },
                    onNavigateToAlerts = { selectedTab = BottomTab.ALERTS }
                )
                BottomTab.HISTORY -> HistoryTab(viewModel = viewModel)
                BottomTab.ADD -> AddTab(viewModel = viewModel)
                BottomTab.ALERTS -> AlertsTab(viewModel = viewModel)
                BottomTab.PROFILE -> ProfileTab(viewModel = viewModel)
            }
        }
    }

    // Modal Overlays
    if (showSendMoney) {
        SendMoneyDialog(
            viewModel = viewModel,
            onDismiss = { showSendMoney = false }
        )
    }

    if (showWithdraw) {
        WithdrawDialog(
            viewModel = viewModel,
            onDismiss = { showWithdraw = false }
        )
    }
}

// ==========================================
// 5. HOME DASHBOARD TAB
// ==========================================
@Composable
fun DashboardTab(
    viewModel: RentLinkViewModel,
    onSendMoneyRequest: () -> Unit,
    onWithdrawRequest: () -> Unit,
    onNavigateToAdd: () -> Unit,
    onNavigateToAlerts: () -> Unit
) {
    val currentUserName by viewModel.currentUserName.collectAsStateWithLifecycle()
    val currentUserId by viewModel.currentUserId.collectAsStateWithLifecycle()
    val currentUserRole by viewModel.currentUserRole.collectAsStateWithLifecycle()

    val availableBalance by viewModel.availableBalance.collectAsStateWithLifecycle()
    val collectedRent by viewModel.collectedRent.collectAsStateWithLifecycle()
    val pendingRent by viewModel.pendingRent.collectAsStateWithLifecycle()

    val properties by viewModel.properties.collectAsStateWithLifecycle()
    val tenants by viewModel.tenants.collectAsStateWithLifecycle()
    val leases by viewModel.leases.collectAsStateWithLifecycle()
    val alerts by viewModel.alerts.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var isBalanceVisible by remember { mutableStateOf(false) }

    LaunchedEffect(isBalanceVisible) {
        if (isBalanceVisible) {
            delay(3000)
            isBalanceVisible = false // Auto hide balance after 3s like bKash/Nagad
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // HEADER ROW
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Avatar with customizable alphabet
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0F7FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentUserName.take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1976D3)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Salam, $currentUserName",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = currentUserId,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF64748B)
                    )
                }

                // Notification Icon (No badging on text as per requirement, pure icon representation)
                IconButton(
                    onClick = { onNavigateToAlerts() },
                    modifier = Modifier
                        .background(Color.White, CircleShape)
                        .border(1.dp, Color(0xFFF1F5F9), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Notification Alerts",
                        tint = if (alerts.isNotEmpty()) Color(0xFF1976D3) else Color(0xFF64748B)
                    )
                }
            }
        }

        // PREMIUM BALANCE GRADIENT CARD
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF00BBFF), Color(0xFF1976D3))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (currentUserRole == "Landlord") "Landlord Account" else "Tenant Account",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                            RentLinkLogoIcon(modifier = Modifier.size(24.dp))
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Tap-to-Reveal Balance Mechanism (bKash/Nagad style)
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null
                                ) { isBalanceVisible = !isBalanceVisible },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Available Balance",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Box(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                                    .padding(horizontal = 24.dp, vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    if (isBalanceVisible) {
                                        Text(
                                            text = "৳ ${String.format("%,.2f", availableBalance)} BDT",
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    } else {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.AccountBalanceWallet,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Tap to View Balance",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Split Financial Stats rows
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = if (currentUserRole == "Landlord") "Collected Rent" else "Rent Paid",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                                Text(
                                    text = "৳ ${String.format("%,.0f", collectedRent)}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Pending / Due",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.75f)
                                )
                                Text(
                                    text = "৳ ${String.format("%,.0f", pendingRent)}",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // QUICK FINTECH BUTTONS (Exactly Send Money and Withdraw, same height, same width, aligned)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = { onSendMoneyRequest() },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("action_send_money"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D3))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = "", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Send Money",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }

                Button(
                    onClick = { onWithdrawRequest() },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .testTag("action_withdraw"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BBFF))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(imageVector = Icons.Default.AccountBalance, contentDescription = "", modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Withdraw",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        // QUICK ACTIONS SECTION
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Quick Services",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val cardModifier = Modifier
                        .weight(1f)
                        .aspectRatio(1.15f)

                    // Add Property Service
                    QuickServiceCard(
                        title = "Add Property",
                        icon = Icons.Default.Business,
                        color = Color(0xFF00BBFF),
                        onClick = { onNavigateToAdd() },
                        modifier = cardModifier
                    )

                    // Add Tenant Service
                    QuickServiceCard(
                        title = "Add Tenant",
                        icon = Icons.Default.PersonAdd,
                        color = Color(0xFF1976D3),
                        onClick = { onNavigateToAdd() },
                        modifier = cardModifier
                    )

                    // Collect Rent Service
                    QuickServiceCard(
                        title = "Collect Rent",
                        icon = Icons.Default.CallReceived,
                        color = Color(0xFF10B981),
                        onClick = {
                            Toast.makeText(context, "Redirecting to Payment hub in Add tab...", Toast.LENGTH_SHORT).show()
                            onNavigateToAdd()
                        },
                        modifier = cardModifier
                    )

                    // Maintenance Service
                    QuickServiceCard(
                        title = "Maintenance",
                        icon = Icons.Default.Build,
                        color = Color(0xFFF59E0B),
                        onClick = { onNavigateToAdd() },
                        modifier = cardModifier
                    )
                }
            }
        }

        // QUICK STATISTICS SUMMARY
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Consolidated Portfolio Summary",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatSummaryItem(
                                value = properties.size.toString(),
                                label = "Total Properties",
                                icon = Icons.Default.Apartment,
                                color = Color(0xFF00BBFF),
                                modifier = Modifier.weight(1f)
                            )
                            StatSummaryItem(
                                value = tenants.size.toString(),
                                label = "Total Tenants",
                                icon = Icons.Default.People,
                                color = Color(0xFF1976D3),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            StatSummaryItem(
                                value = leases.size.toString(),
                                label = "Active Leases",
                                icon = Icons.Default.Gavel,
                                color = Color(0xFF10B981),
                                modifier = Modifier.weight(1f)
                            )
                            StatSummaryItem(
                                value = "৳ ${String.format("%,.0f", properties.sumOf { it.rentAmount })}",
                                label = "Monthly Yield",
                                icon = Icons.Default.MonetizationOn,
                                color = Color(0xFFF59E0B),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // ACTIVE URGENT ALERTS SECTION
        item {
            val unreadAlerts = alerts.filter { !it.isRead }
            if (unreadAlerts.isNotEmpty()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Critical System Alerts",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "View All",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1976D3),
                            modifier = Modifier.clickable { onNavigateToAlerts() }
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        unreadAlerts.take(2).forEach { alert ->
                            AlertCardItem(
                                alert = alert,
                                onReadClick = { viewModel.markAlertRead(alert.id) }
                            )
                        }
                    }
                }
            }
        }

        // RECENT TRANSACTIONS LEDGER LIST
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Real-Time Ledger Transactions",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                if (transactions.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No transactions recorded in secure ledger",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp
                            )
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        transactions.take(5).forEach { tx ->
                            LedgerTransactionRow(transaction = tx)
                        }
                    }
                }
            }
        }
    }
}

// Support widgets for Dashboard
@Composable
fun QuickServiceCard(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .shadow(2.dp, RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A),
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
fun StatSummaryItem(
    value: String,
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = Color(0xFF64748B)
            )
        }
    }
}

@Composable
fun AlertCardItem(
    alert: RentLinkAlert,
    onReadClick: () -> Unit
) {
    val backdropColor = when (alert.statusBadge) {
        "Overdue" -> Color(0xFFFEF2F2)
        "Maintenance" -> Color(0xFFFFFBEB)
        "Expiring" -> Color(0xFFEFF6FF)
        else -> Color(0xFFECFDF5)
    }

    val themeColor = when (alert.statusBadge) {
        "Overdue" -> Color(0xFFEF4444)
        "Maintenance" -> Color(0xFFF59E0B)
        "Expiring" -> Color(0xFF3B82F6)
        else -> Color(0xFF10B981)
    }

    val icon = when (alert.statusBadge) {
        "Overdue" -> Icons.Default.ErrorOutline
        "Maintenance" -> Icons.Default.Build
        "Expiring" -> Icons.Default.Timer
        else -> Icons.Default.CheckCircleOutline
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backdropColor),
        border = BorderStroke(1.dp, themeColor.copy(alpha = 0.25f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = themeColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = alert.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = alert.statusBadge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        color = themeColor,
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = alert.message,
                    fontSize = 11.sp,
                    color = Color(0xFF334155),
                    lineHeight = 15.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = alert.timestamp,
                        fontSize = 10.sp,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = "Mark Read",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = themeColor,
                        modifier = Modifier.clickable { onReadClick() }
                    )
                }
            }
        }
    }
}

@Composable
fun LedgerTransactionRow(transaction: Transaction) {
    val txColor = when (transaction.type) {
        "Collection" -> Color(0xFF10B981) // BDT Inflow
        "Withdrawal" -> Color(0xFFEF4444) // BDT Outflow
        "Sent" -> Color(0xFFEF4444)
        else -> Color(0xFF1976D3)
    }

    var showDetails by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDetails = true },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(txColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (transaction.type == "Collection") Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = txColor,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.tenantName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${transaction.propertyName} • ID: ${transaction.rentLinkId.take(11)}",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (transaction.type == "Collection") "+" else "-"}৳${String.format("%,.0f", transaction.amount)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = txColor
                )
                Text(
                    text = "${transaction.paymentMethod} • ${transaction.date}",
                    fontSize = 10.sp,
                    color = Color(0xFF64748B)
                )
            }
        }
    }

    if (showDetails) {
        TransactionDetailsDialog(transaction = transaction, onDismiss = { showDetails = false })
    }
}

// ==========================================
// 6. HISTORY TAB
// ==========================================
@Composable
fun HistoryTab(viewModel: RentLinkViewModel) {
    val transactions by viewModel.transactions.collectAsStateWithLifecycle()
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") } // All, Collection, Sent, Withdrawal

    val filteredTransactions = transactions.filter { tx ->
        val matchesSearch = tx.tenantName.contains(searchQuery, ignoreCase = true) ||
                tx.propertyName.contains(searchQuery, ignoreCase = true) ||
                tx.rentLinkId.contains(searchQuery, ignoreCase = true)
        
        val matchesFilter = selectedFilter == "All" ||
                (selectedFilter == "Inflow" && tx.type == "Collection") ||
                (selectedFilter == "Outflow" && (tx.type == "Sent" || tx.type == "Withdrawal"))
        
        matchesSearch && matchesFilter
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RentLinkLogoIcon(modifier = Modifier.size(36.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Secured Digital Ledger",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        }
        
        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by Tenant, Property or ID...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B)) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF00BBFF),
                unfocusedBorderColor = Color(0xFFCBD5E1)
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Multi Filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf("All", "Inflow", "Outflow").forEach { filter ->
                val isSelected = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .border(
                            1.dp,
                            if (isSelected) Color(0xFF1976D3) else Color(0xFFCBD5E1),
                            RoundedCornerShape(20.dp)
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) Color(0xFFE0F7FF) else Color.White)
                        .clickable { selectedFilter = filter }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = filter,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color(0xFF1976D3) else Color(0xFF64748B)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Secure Cryptographic Shield info
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
            border = BorderStroke(1.dp, Color(0xFFCBD5E1))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.VerifiedUser,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Every transaction is securely stored in a SHA-256 encrypted peer ledger to prevent dual dispute vectors.",
                    fontSize = 11.sp,
                    color = Color(0xFF475569)
                )
            }
        }

        // Ledger list
        if (filteredTransactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "No transactions match filters",
                        fontSize = 14.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(filteredTransactions) { tx ->
                    LedgerTransactionRow(transaction = tx)
                }
            }
        }
    }
}

// ==========================================
// 7. ADD TAB (FORMS HUB)
// ==========================================
@Composable
fun AddTab(viewModel: RentLinkViewModel) {
    val properties by viewModel.properties.collectAsStateWithLifecycle()
    val tenants by viewModel.tenants.collectAsStateWithLifecycle()

    var selectedForm by remember { mutableStateOf("Property") } // Property, Tenant, Collect, Maintenance

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RentLinkLogoIcon(modifier = Modifier.size(36.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Services Gateway Panel",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal chips for form switching
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(listOf("Property", "Tenant", "Rent Collection", "Maintenance Request")) { form ->
                val isSelected = selectedForm == form
                Box(
                    modifier = Modifier
                        .border(
                            1.dp,
                            if (isSelected) Color(0xFF1976D3) else Color(0xFFCBD5E1),
                            RoundedCornerShape(20.dp)
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) Color(0xFFE0F7FF) else Color.White)
                        .clickable { selectedForm = form }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = form,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color(0xFF1976D3) else Color(0xFF64748B)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Scrollable form container
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            when (selectedForm) {
                "Property" -> AddPropertyForm(onAddProperty = { name, address, rentAmount, bedrooms, bathrooms, description ->
                    viewModel.addProperty(name, address, rentAmount, bedrooms, bathrooms, description)
                    selectedForm = "Tenant" // Next progression
                })
                "Tenant" -> AddTenantForm(
                    properties = properties,
                    onAddTenant = { name, rlId, phone, email, propId ->
                        viewModel.addTenant(name, rlId, phone, email, propId)
                        selectedForm = "Rent Collection"
                    }
                )
                "Rent Collection" -> CollectRentForm(
                    tenants = tenants,
                    onCollect = { tenantId, gateway ->
                        viewModel.collectRent(tenantId, gateway) {}
                    }
                )
                "Maintenance Request" -> RequestMaintenanceForm(
                    properties = properties,
                    onSubmitRequest = { propertyId, description, priority ->
                        viewModel.submitMaintenance(propertyId, description, priority)
                    }
                )
            }
        }
    }
}

// 7a. Add Property Form
@Composable
fun AddPropertyForm(onAddProperty: (String, String, Double, Int, Int, String) -> Unit) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var rentAmount by remember { mutableStateOf("") }
    var bedrooms by remember { mutableStateOf("3") }
    var bathrooms by remember { mutableStateOf("2") }
    var description by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(text = "Add New Estate Property", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Property Title") },
            placeholder = { Text("e.g. Gulshan heights, Apt 5C") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Street Address") },
            placeholder = { Text("Road 10, Gulshan - 1") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = rentAmount,
                onValueChange = { rentAmount = it },
                label = { Text("Rent / Month (BDT)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1.5f)
            )

            OutlinedTextField(
                value = bedrooms,
                onValueChange = { bedrooms = it },
                label = { Text("Beds") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )

            OutlinedTextField(
                value = bathrooms,
                onValueChange = { bathrooms = it },
                label = { Text("Baths") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Full Description") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        )

        Button(
            onClick = {
                val rAmt = rentAmount.toDoubleOrNull()
                val beds = bedrooms.toIntOrNull() ?: 3
                val baths = bathrooms.toIntOrNull() ?: 2
                if (name.isEmpty() || address.isEmpty() || rAmt == null) {
                    Toast.makeText(context, "Please populate all fields", Toast.LENGTH_SHORT).show()
                } else {
                    onAddProperty(name, address, rAmt, beds, baths, description)
                    Toast.makeText(context, "Property added into secure catalog!", Toast.LENGTH_SHORT).show()
                    name = ""
                    address = ""
                    rentAmount = ""
                    description = ""
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D3)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Publish Property Listing", fontWeight = FontWeight.Bold)
        }
    }
}

// 7b. Add Tenant Form
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTenantForm(
    properties: List<Property>,
    onAddTenant: (String, String, String, String, Int?) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var rlId by remember { mutableStateOf("RL01") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    
    // Property dropdown
    var selectedPropertyIndex by remember { mutableStateOf(-1) }
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(text = "Register Tenant Profile", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Tenant Full Name") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = rlId,
                onValueChange = { rlId = it },
                label = { Text("RentLink ID") },
                placeholder = { Text("RL01812345678") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1.2f)
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email Address") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        // Dropdown triggering
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            val dropdownLabel = if (selectedPropertyIndex >= 0 && selectedPropertyIndex < properties.size) {
                properties[selectedPropertyIndex].name
            } else {
                "Select Assigned Property (optional)"
            }

            OutlinedTextField(
                value = dropdownLabel,
                onValueChange = {},
                readOnly = true,
                label = { Text("Premises Allocation") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                properties.forEachIndexed { idx, p ->
                    DropdownMenuItem(
                        text = { Text("${p.name} (৳${p.rentAmount}/mo)") },
                        onClick = {
                            selectedPropertyIndex = idx
                            expanded = false
                        }
                    )
                }
            }
        }

        Button(
            onClick = {
                if (name.isEmpty() || phone.isEmpty()) {
                    Toast.makeText(context, "Populate Tenant Name and Phone", Toast.LENGTH_SHORT).show()
                } else {
                    val propertyId = if (selectedPropertyIndex >= 0) properties[selectedPropertyIndex].id else null
                    onAddTenant(name, rlId, phone, email, propertyId)
                    Toast.makeText(context, "Tenant added and secure lease dispatched!", Toast.LENGTH_SHORT).show()
                    name = ""
                    rlId = "RL01"
                    phone = ""
                    email = ""
                    selectedPropertyIndex = -1
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BBFF)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Submit Lease Registration", fontWeight = FontWeight.Bold)
        }
    }
}

// 7c. Collect Rent Form
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectRentForm(
    tenants: List<Tenant>,
    onCollect: (Int, String) -> Unit
) {
    val context = LocalContext.current
    var selectedTenantIndex by remember { mutableStateOf(-1) }
    var tenantDropdownExpanded by remember { mutableStateOf(false) }

    var selectedGateway by remember { mutableStateOf("bKash") } // bKash, Nagad, Rocket

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = "Record Rental Collection", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))

        // Tenant selector
        ExposedDropdownMenuBox(
            expanded = tenantDropdownExpanded,
            onExpandedChange = { tenantDropdownExpanded = !tenantDropdownExpanded }
        ) {
            val dropdownLabel = if (selectedTenantIndex >= 0 && selectedTenantIndex < tenants.size) {
                "${tenants[selectedTenantIndex].name} - ${tenants[selectedTenantIndex].assignedPropertyName ?: "Unassigned"}"
            } else {
                "Select active Allocated Tenant"
            }

            OutlinedTextField(
                value = dropdownLabel,
                onValueChange = {},
                readOnly = true,
                label = { Text("Payer Profile") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = tenantDropdownExpanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = tenantDropdownExpanded,
                onDismissRequest = { tenantDropdownExpanded = false }
            ) {
                tenants.forEachIndexed { idx, t ->
                    DropdownMenuItem(
                        text = { Text("${t.name} (${t.assignedPropertyName ?: "No Assigned Unit"})") },
                        onClick = {
                            selectedTenantIndex = idx
                            tenantDropdownExpanded = false
                        }
                    )
                }
            }
        }

        // Display Rent dues
        if (selectedTenantIndex >= 0) {
            val t = tenants[selectedTenantIndex]
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "PRESCRIBED LEDGER AMOUNT", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${t.assignedPropertyName}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "Due Amount: BDT ${t.phone.take(2)}0,000.00 /mo", // Deterministic
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F172A)
                    )
                }
            }
        }

        // Gateway Selection
        Text(text = "Choose Local Payment Gateway", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF475569))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf("bKash", "Nagad", "Rocket").forEach { gateway ->
                val isSelected = selectedGateway == gateway
                val accentColor = when (gateway) {
                    "bKash" -> Color(0xFFE2125B) // Pink bKash
                    "Nagad" -> Color(0xFFEC2626) // Orange Nagad
                    else -> Color(0xFF800080)   // Purple Rocket
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .height(60.dp)
                        .border(
                            2.dp,
                            if (isSelected) accentColor else Color(0xFFE2E8F0),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedGateway = gateway },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = gateway,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) accentColor else Color(0xFF64748B)
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                if (selectedTenantIndex < 0) {
                    Toast.makeText(context, "Please select tenant", Toast.LENGTH_SHORT).show()
                } else {
                    val tenant = tenants[selectedTenantIndex]
                    onCollect(tenant.id, selectedGateway)
                    Toast.makeText(context, "Payment completed. Ledger updated successfully!", Toast.LENGTH_LONG).show()
                    selectedTenantIndex = -1
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Confirm Secured Collection", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// 7d. Maintenance Request Form
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestMaintenanceForm(
    properties: List<Property>,
    onSubmitRequest: (Int, String, String) -> Unit
) {
    val context = LocalContext.current
    var selectedPropertyIndex by remember { mutableStateOf(-1) }
    var propExpanded by remember { mutableStateOf(false) }

    var description by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf("Medium") } // Low, Medium, High

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(text = "Raise Maintenance Request", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))

        ExposedDropdownMenuBox(
            expanded = propExpanded,
            onExpandedChange = { propExpanded = !propExpanded }
        ) {
            val dropdownLabel = if (selectedPropertyIndex >= 0 && selectedPropertyIndex < properties.size) {
                properties[selectedPropertyIndex].name
            } else {
                "Select Faulty Premises Unit"
            }

            OutlinedTextField(
                value = dropdownLabel,
                onValueChange = {},
                readOnly = true,
                label = { Text("Premises Location") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = propExpanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = propExpanded,
                onDismissRequest = { propExpanded = false }
            ) {
                properties.forEachIndexed { idx, p ->
                    DropdownMenuItem(
                        text = { Text(p.name) },
                        onClick = {
                            selectedPropertyIndex = idx
                            propExpanded = false
                        }
                    )
                }
            }
        }

        // Priority chooser
        Text(text = "Defect Priority Category", fontSize = 13.sp, color = Color(0xFF64748B))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf("Low", "Medium", "High").forEach { priority ->
                val isSelected = selectedPriority == priority
                val activeBgColor = when (priority) {
                    "Low" -> Color(0xFFECFDF5)
                    "Medium" -> Color(0xFFFFFBEB)
                    else -> Color(0xFFFEF2F2)
                }
                val activeTextColor = when (priority) {
                    "Low" -> Color(0xFF10B981)
                    "Medium" -> Color(0xFFF59E0B)
                    else -> Color(0xFFEF4444)
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            1.dp,
                            if (isSelected) activeTextColor else Color(0xFFE2E8F0),
                            RoundedCornerShape(8.dp)
                        )
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) activeBgColor else Color.White)
                        .clickable { selectedPriority = priority }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = priority,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) activeTextColor else Color(0xFF64748B)
                    )
                }
            }
        }

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Describe Fault (plumbing, structural, gas, lift...)") },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
        )

        Button(
            onClick = {
                if (selectedPropertyIndex < 0 || description.isEmpty()) {
                    Toast.makeText(context, "Populate Premises unit and issue descript", Toast.LENGTH_SHORT).show()
                } else {
                    val propId = properties[selectedPropertyIndex].id
                    onSubmitRequest(propId, description, selectedPriority)
                    Toast.makeText(context, "Maintenance request logged in peer queue!", Toast.LENGTH_SHORT).show()
                    description = ""
                    selectedPropertyIndex = -1
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Build, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Dispatch Maintenance Crew", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==========================================
// 8. ALERTS TAB
// ==========================================
@Composable
fun AlertsTab(viewModel: RentLinkViewModel) {
    val alerts by viewModel.alerts.collectAsStateWithLifecycle()

    var activeSubTab by remember { mutableStateOf("All Alerts") }

    val filteredAlerts = if (activeSubTab == "Unread Only") {
        alerts.filter { !it.isRead }
    } else {
        alerts
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = null,
                tint = Color(0xFF1976D3),
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Notifications & Activity Log",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Mode switchers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            listOf("All Alerts", "Unread Only").forEach { subTab ->
                val isSelected = activeSubTab == subTab
                Box(
                    modifier = Modifier
                        .border(
                            1.dp,
                            if (isSelected) Color(0xFF1976D3) else Color(0xFFCBD5E1),
                            RoundedCornerShape(20.dp)
                        )
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) Color(0xFFE0F7FF) else Color.White)
                        .clickable { activeSubTab = subTab }
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = subTab,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color(0xFF1976D3) else Color(0xFF64748B)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (filteredAlerts.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.NotificationsOff,
                        contentDescription = null,
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(68.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Hooray! No pending tenant notifications",
                        fontSize = 14.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredAlerts) { alert ->
                    AlertCardItem(
                        alert = alert,
                        onReadClick = { viewModel.markAlertRead(alert.id) }
                    )
                }
            }
        }
    }
}

// ==========================================
// 9. PROFILE TAB
// ==========================================
@Composable
fun ProfileTab(viewModel: RentLinkViewModel) {
    val currentUserName by viewModel.currentUserName.collectAsStateWithLifecycle()
    val currentUserId by viewModel.currentUserId.collectAsStateWithLifecycle()
    val currentUserMobile by viewModel.currentUserMobile.collectAsStateWithLifecycle()
    val currentUserRole by viewModel.currentUserRole.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RentLinkLogoIcon(modifier = Modifier.size(36.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "My Digital Profile",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Avatar
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(Color(0xFFE0F7FF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = Color(0xFF1976D3),
                modifier = Modifier.size(54.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = currentUserName,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F172A)
        )
        Text(
            text = "Verified RentLink Member",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF10B981)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // QR Ident representation Card (bKash/Google Pay look)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .shadow(2.dp, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Interactive QR Simulation icon
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = "Simulated RentLink ID QR",
                        tint = Color(0xFF1976D3),
                        modifier = Modifier.size(110.dp)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "My RentLink Scan Code",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF64748B)
                )
                Text(
                    text = currentUserId,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Profiles info rows
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                ProfileDetailRow(label = "Mobile Registered", value = currentUserMobile, icon = Icons.Default.Phone)
                HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 10.dp))
                ProfileDetailRow(label = "Active Access Scope", value = currentUserRole, icon = Icons.Default.Group)
                HorizontalDivider(color = Color(0xFFF1F5F9), modifier = Modifier.padding(vertical = 10.dp))
                ProfileDetailRow(label = "Secure Ledger Node Hash", value = "SHA-256 (SECURE)", icon = Icons.Default.Shield)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Role switching toggler (Seamless toggling to test both landlord and tenant experiences!)
        Button(
            onClick = {
                val nextRole = if (currentUserRole == "Landlord") "Tenant" else "Landlord"
                viewModel.selectRole(nextRole)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BBFF)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.SwapHoriz, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Switch Role to ${if (currentUserRole == "Landlord") "Tenant Playground" else "Landlord Portfolio"}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Log out
        OutlinedButton(
            onClick = { viewModel.logout() },
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
            border = BorderStroke(1.dp, Color(0xFFEF4444)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Terminate Secure Auth Session", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ProfileDetailRow(label: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF1976D3), modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = label, fontSize = 13.sp, color = Color(0xFF64748B), modifier = Modifier.weight(1f))
        Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
    }
}

// ==========================================
// 10. DIALOG OVERLAYS
// ==========================================

// 10a. Send Money Dialog
@Composable
fun SendMoneyDialog(
    viewModel: RentLinkViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var recipientId by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var gateway by remember { mutableStateOf("bKash") }

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(text = "Send Secure Funds (RentLink Pay)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))

                OutlinedTextField(
                    value = recipientId,
                    onValueChange = { recipientId = it },
                    label = { Text("Recipient RentLink ID or Mobile") },
                    placeholder = { Text("RL01XXXXXXXXX") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Transfer Amount (BDT)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Gateways
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("bKash", "Nagad", "Rocket").forEach { g ->
                        val isSelected = gateway == g
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    1.dp,
                                    if (isSelected) Color(0xFF1976D3) else Color(0xFFCBD5E1),
                                    RoundedCornerShape(6.dp)
                                )
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) Color(0xFFE0F7FF) else Color.White)
                                .clickable { gateway = g }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = g, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { onDismiss() },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val amt = amount.toDoubleOrNull()
                            if (recipientId.isEmpty() || amt == null) {
                                Toast.makeText(context, "Populate all parameters", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.sendMoney(recipientId, amt, gateway) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    if (success) {
                                        onDismiss()
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D3)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text("Send Fund", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// 10b. Withdraw Dialog
@Composable
fun WithdrawDialog(
    viewModel: RentLinkViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var amount by remember { mutableStateOf("") }
    var gateway by remember { mutableStateOf("bKash") }

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(text = "Withdraw Funds", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Withdraw Amount (BDT)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Gateways
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("bKash", "Nagad", "Rocket").forEach { g ->
                        val isSelected = gateway == g
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(
                                    1.dp,
                                    if (isSelected) Color(0xFF1976D3) else Color(0xFFCBD5E1),
                                    RoundedCornerShape(6.dp)
                                )
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) Color(0xFFE0F7FF) else Color.White)
                                .clickable { gateway = g }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = g, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { onDismiss() },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }

                    Button(
                        onClick = {
                            val amt = amount.toDoubleOrNull()
                            if (amt == null) {
                                Toast.makeText(context, "Populate valid amount", Toast.LENGTH_SHORT).show()
                            } else {
                                viewModel.withdrawMoney(amt, gateway) { success, msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                    if (success) {
                                        onDismiss()
                                    }
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00BBFF)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text("Withdraw", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// 10c. View Transaction Details Dialog (Securing the "Ledger verification details" with standard hash values!)
@Composable
fun TransactionDetailsDialog(
    transaction: Transaction,
    onDismiss: () -> Unit
) {
    val txColor = if (transaction.type == "Collection") Color(0xFF10B981) else Color(0xFFEF4444)

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Verified badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .background(Color(0xFFECFDF5), RoundedCornerShape(30.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Icon(imageVector = Icons.Default.Verified, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Secure Ledger Settled", color = Color(0xFF10B981), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(4.dp))

                RentLinkLogoIcon(modifier = Modifier.size(54.dp))

                Text(
                    text = "${if (transaction.type == "Collection") "Received BDT" else "Paid BDT"}",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )

                Text(
                    text = "৳ ${String.format("%,.2f", transaction.amount)}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = txColor
                )

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // Detail list
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    TransactionDetailItem(label = "Sender/Payer", value = transaction.tenantName)
                    TransactionDetailItem(label = "Allocated Premises", value = transaction.propertyName)
                    TransactionDetailItem(label = "Payment Gateway", value = transaction.paymentMethod)
                    TransactionDetailItem(label = "RentLink Account ID", value = transaction.rentLinkId)
                    TransactionDetailItem(label = "Date Registered", value = transaction.date)
                }

                HorizontalDivider(color = Color(0xFFF1F5F9))

                // Blockchain ledger simulation
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8FAFC), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Peer Hash Verification Block",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF64748B)
                    )
                    Text(
                        text = transaction.ledgerHash.ifEmpty { "TX-LEDGER-HASH-UNSET" },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E293B),
                        fontFamily = FontFamily.Monospace,
                        textAlign = TextAlign.Center
                    )
                }

                Button(
                    onClick = { onDismiss() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D3)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text("Dismiss Voucher", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TransactionDetailItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = Color(0xFF64748B))
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
    }
}
