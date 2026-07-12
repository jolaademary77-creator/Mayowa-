package com.example.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.R
import com.example.data.CropDiagnosis
import com.example.data.PreloadedCrop
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CropApp(viewModel: CropViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()

    // Warm, organic agricultural colors (vital greens, rich harvest golds, cream backgrounds)
    val lightAgriTheme = lightColorScheme(
        primary = Color(0xFF2E7D32), // Vibrant Leaf Green
        onPrimary = Color.White,
        secondary = Color(0xFFE65100), // Rich Crop Orange
        background = Color(0xFFF9FBE7), // Soft Organic Cream
        surface = Color.White,
        onBackground = Color(0xFF1B5E20), // Rich Forest Green
        onSurface = Color(0xFF1B5E20),
        error = Color(0xFFC62828)
    )

    MaterialTheme(
        colorScheme = lightAgriTheme
    ) {
        val configuration = LocalConfiguration.current
        val isWideScreen = configuration.screenWidthDp >= 600

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Spa,
                                contentDescription = "App Logo",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Crop Doctor AI",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    actions = {
                        if (currentScreen == "HISTORY") {
                            IconButton(onClick = { viewModel.clearHistory() }) {
                                Icon(
                                    imageVector = Icons.Filled.DeleteSweep,
                                    contentDescription = "Clear All Logs",
                                    tint = MaterialTheme.colorScheme.error
                                )
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
                if (!isWideScreen) {
                    NavigationBar(
                        containerColor = Color.White,
                        tonalElevation = 8.dp
                    ) {
                        val navItems = listOf(
                            Triple("DIAGNOSE", Icons.Filled.PhotoCamera, "Scan Crop"),
                            Triple("HISTORY", Icons.Filled.History, "Logs History"),
                            Triple("KNOWLEDGE_BASE", Icons.Filled.MenuBook, "Knowledge")
                        )

                        navItems.forEach { (screen, icon, label) ->
                            NavigationBarItem(
                                selected = currentScreen == screen,
                                onClick = { viewModel.navigateTo(screen) },
                                icon = { Icon(icon, contentDescription = label) },
                                label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = Color.Gray,
                                    unselectedTextColor = Color.Gray,
                                    indicatorColor = Color(0xFFE8F5E9)
                                ),
                                modifier = Modifier.testTag("nav_tab_${screen.lowercase()}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Tablet Navigation Rail
                if (isWideScreen) {
                    NavigationRail(
                        containerColor = Color.White,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        val railItems = listOf(
                            Triple("DIAGNOSE", Icons.Filled.PhotoCamera, "Scan"),
                            Triple("HISTORY", Icons.Filled.History, "Logs"),
                            Triple("KNOWLEDGE_BASE", Icons.Filled.MenuBook, "Knowledge")
                        )

                        railItems.forEach { (screen, icon, label) ->
                            NavigationRailItem(
                                selected = currentScreen == screen,
                                onClick = { viewModel.navigateTo(screen) },
                                icon = { Icon(icon, contentDescription = label) },
                                label = { Text(label, fontSize = 11.sp) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    unselectedIconColor = Color.Gray,
                                    unselectedTextColor = Color.Gray,
                                    indicatorColor = Color(0xFFE8F5E9)
                                ),
                                modifier = Modifier.testTag("rail_tab_${screen.lowercase()}")
                            )
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    when (currentScreen) {
                        "DIAGNOSE" -> DiagnoseScreen(viewModel)
                        "HISTORY" -> HistoryScreen(viewModel)
                        "KNOWLEDGE_BASE" -> KnowledgeBaseScreen()
                    }

                    // Toast SnackBar
                    toastMessage?.let { msg ->
                        Snackbar(
                            action = {
                                TextButton(onClick = { viewModel.clearToast() }) {
                                    Text("OK", color = MaterialTheme.colorScheme.primary)
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                                .testTag("toast_snackbar")
                        ) {
                            Text(text = msg)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 1. DIAGNOSE SCREEN
// ==========================================
@Composable
fun DiagnoseScreen(viewModel: CropViewModel) {
    val isLoading by viewModel.isLoading.collectAsState()
    val diagnosisResult by viewModel.diagnosisResult.collectAsState()
    val isRecording by viewModel.isRecordingVideo.collectAsState()

    var farmerNotes by remember { mutableStateOf("") }
    var selectedDemoCrop by remember { mutableStateOf<PreloadedCrop?>(null) }
    var activeBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isVideoMode by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Set up standard Camera Snap Contract
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            activeBitmap = bitmap
            selectedDemoCrop = null // Override selected preloaded
            if (!isVideoMode) {
                viewModel.diagnoseCrop(bitmap, farmerNotes, isVideo = false)
            } else {
                viewModel.stopVideoRecordingAndDiagnose(bitmap, farmerNotes)
            }
        }
    }

    // Set up standard Gallery/File Picker Contract
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                // Convert Uri safely to Bitmap for local analysis & base64 encoding
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                if (bitmap != null) {
                    activeBitmap = bitmap
                    selectedDemoCrop = null
                    viewModel.diagnoseCrop(bitmap, farmerNotes, isVideo = isVideoMode)
                }
            } catch (e: Exception) {
                viewModel.showToast("Failed to load image: ${e.message}")
            }
        }
    }

    // Dynamic Camera runtime permission handling
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch()
        } else {
            viewModel.showToast("Camera permission is required to snap pictures of the crop.")
        }
    }

    val requestCameraPermissionAndLaunch = {
        val permissionCheck = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        )
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            cameraLauncher.launch()
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("diagnose_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Image Illustration Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .shadow(4.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_crop_hero),
                    contentDescription = "Crop Field",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xAA000000)),
                                startY = 40f
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "REAL-TIME DIAGNOSIS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFAEEA00),
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Instant Crop Disease Scanner",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }
            }
        }

        // Mode switch selector (Photo vs Video)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DIAGNOSTIC MODE",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFE8F5E9))
                        .padding(4.dp)
                ) {
                    val activeColor = MaterialTheme.colorScheme.primary
                    val inactiveColor = Color.Transparent

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (!isVideoMode) activeColor else inactiveColor)
                            .clickable { isVideoMode = false }
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Camera,
                                contentDescription = "Photo Mode",
                                tint = if (!isVideoMode) Color.White else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Photo",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!isVideoMode) Color.White else Color.Gray
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isVideoMode) activeColor else inactiveColor)
                            .clickable { isVideoMode = true }
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Filled.Videocam,
                                contentDescription = "Video Mode",
                                tint = if (isVideoMode) Color.White else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Video",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isVideoMode) Color.White else Color.Gray
                            )
                        }
                    }
                }
            }
        }

        // Animated Scanning Card Frame
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp),
                border = BorderStroke(1.dp, Color(0xFFE0E0E0))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF1F8E9)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (activeBitmap != null) {
                            Image(
                                bitmap = activeBitmap!!.asImageBitmap(),
                                contentDescription = "Crop Scan",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Pulsing laser scan line overlay when loading
                            if (isLoading) {
                                val infiniteTransition = rememberInfiniteTransition(label = "scan_laser")
                                val yOffset by infiniteTransition.animateFloat(
                                    initialValue = 0f,
                                    targetValue = 200f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1500, easing = LinearEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = "laser_y"
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .offset(y = yOffset.dp - 100.dp)
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    Color(0xFF76FF03),
                                                    Color(0xFF76FF03),
                                                    Color.Transparent
                                                )
                                            )
                                        )
                                        .shadow(4.dp, spotColor = Color(0xFF76FF03))
                                )
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (isVideoMode) Icons.Filled.Videocam else Icons.Filled.PhotoCamera,
                                    contentDescription = "Place holder",
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                    modifier = Modifier.size(54.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isVideoMode) "Record crop video to scan" else "Snap crop leaf to identify illness",
                                    fontSize = 13.sp,
                                    color = Color.Gray,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Diagnostic Actions Section
                    if (!isVideoMode) {
                        // PHOTO ACTIONS
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { requestCameraPermissionAndLaunch() },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Icon(Icons.Filled.PhotoCamera, contentDescription = "Camera")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Snap Crop", fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { galleryLauncher.launch("image/*") },
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                            ) {
                                Icon(Icons.Filled.Photo, contentDescription = "Gallery", tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Upload Photo", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // VIDEO ACTIONS
                        if (!isRecording) {
                            Button(
                                onClick = {
                                    viewModel.startVideoRecording()
                                    viewModel.showToast("Recording crop details... Snap camera once crop is in focus.")
                                    // Trigger camera capture to extract high-quality diagnostic keyframe
                                    requestCameraPermissionAndLaunch()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Icon(Icons.Filled.FiberManualRecord, contentDescription = "Record", tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Start Crop Video Scan", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        } else {
                            Button(
                                onClick = {
                                    requestCameraPermissionAndLaunch()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                Icon(Icons.Filled.Check, contentDescription = "Stop", tint = Color.White)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Stop & Extract Keyframe", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // Farmer custom field notes input
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp),
                border = BorderStroke(1.dp, Color(0xFFE0E0E0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ADD FIELD OBSERVATIONS (OPTIONAL)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = farmerNotes,
                        onValueChange = { farmerNotes = it },
                        placeholder = { Text("E.g. Brown spots spreading on leaf borders, plant seems wilting...", fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color(0xFFE0E0E0)
                        )
                    )
                }
            }
        }

        // Genius Emulator-friendly Quick Disease Testing Section (Interactive Canvas Leaf Generator)
        item {
            Column {
                Text(
                    text = "QUICK DEMO & SIMULATOR TESTS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Select a preloaded crop disease template. We dynamically draw the diseased leaf on canvas and send it to the real AI for diagnostics!",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))

                val preloadedCrops = listOf(
                    PreloadedCrop("1", "Tomato", "Late Blight", "Fungal spots with yellow halos", R.drawable.ic_launcher_foreground),
                    PreloadedCrop("2", "Rice", "Rice Blast", "Diamond shaped lesion spots", R.drawable.ic_launcher_foreground),
                    PreloadedCrop("3", "Corn", "Common Rust", "Powdery orange spots", R.drawable.ic_launcher_foreground),
                    PreloadedCrop("4", "Wheat", "Powdery Mildew", "White powdery fungal spots", R.drawable.ic_launcher_foreground),
                    PreloadedCrop("5", "Grape", "Black Rot", "Brown rotting spots on foliage", R.drawable.ic_launcher_foreground)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(preloadedCrops) { crop ->
                        val isSelected = selectedDemoCrop?.id == crop.id
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFFE8F5E9) else Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFE0E0E0)
                            ),
                            modifier = Modifier
                                .width(130.dp)
                                .clickable {
                                    selectedDemoCrop = crop
                                    farmerNotes = "Quick scan test: ${crop.name} leaf showing signs of ${crop.typicalIllness}."
                                    // Generate dynamic canvas image of diseased leaf
                                    val bitmap = generateDiseasedLeafBitmap(crop.name, crop.typicalIllness)
                                    activeBitmap = bitmap
                                }
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Draw leaf icon
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color(0xFFE8F5E9), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Spa,
                                        contentDescription = "Leaf",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    crop.name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    crop.typicalIllness,
                                    fontSize = 10.sp,
                                    color = Color.Gray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active AI scan diagnostics action
        if (activeBitmap != null && selectedDemoCrop != null) {
            item {
                Button(
                    onClick = {
                        viewModel.diagnoseCrop(activeBitmap!!, farmerNotes, isVideo = isVideoMode)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(bottom = 8.dp)
                ) {
                    Icon(Icons.Filled.AutoAwesome, contentDescription = "AI")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Analyze Crop with Gemini AI", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    // Modal AI Results overlay popup
    diagnosisResult?.let { result ->
        Dialog(onDismissRequest = { viewModel.clearDiagnosisResult() }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .shadow(8.dp),
                border = BorderStroke(1.dp, Color(0xFFC8E6C9))
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DIAGNOSTIC REPORT",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.5.sp
                        )

                        IconButton(onClick = { viewModel.clearDiagnosisResult() }) {
                            Icon(Icons.Filled.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Crop & Illness header
                    Icon(
                        imageVector = Icons.Filled.LocalFlorist,
                        contentDescription = "Florist",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = result.cropName.uppercase(Locale.getDefault()),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Gray
                    )
                    Text(
                        text = result.illness,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (result.illness.lowercase() == "healthy") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Status badges row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Severity badge
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = when (result.severity.lowercase()) {
                                    "high" -> Color(0xFFFFEBEE)
                                    "medium" -> Color(0xFFFFF3E0)
                                    else -> Color(0xFFE8F5E9)
                                }
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("SEVERITY", fontSize = 10.sp, color = Color.Gray)
                                Text(
                                    result.severity,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = when (result.severity.lowercase()) {
                                        "high" -> Color(0xFFC62828)
                                        "medium" -> Color(0xFFE65100)
                                        else -> Color(0xFF2E7D32)
                                    }
                                )
                            }
                        }

                        // Confidence badge
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F8E9)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("AI CONFIDENCE", fontSize = 10.sp, color = Color.Gray)
                                Text(
                                    "${result.confidence}%",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Cure Instructions
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FBE7)),
                        border = BorderStroke(1.dp, Color(0xFFD4E157)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Healing, contentDescription = "Cure", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("RECOMMENDED CURE & REMEDIES", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = result.cure,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = Color(0xFF33691E)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.clearDiagnosisResult() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Add to Field Logs", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. HISTORY SCREEN
// ==========================================
@Composable
fun HistoryScreen(viewModel: CropViewModel) {
    val diagnosesList by viewModel.diagnosesList.collectAsState()
    var selectedDiagnosisForDetails by remember { mutableStateOf<CropDiagnosis?>(null) }
    var notesToEdit by remember { mutableStateOf("") }

    if (diagnosesList.isEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
                .testTag("history_screen_empty"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                contentDescription = "No records",
                tint = Color.LightGray,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No diagnosis logs found",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            Text(
                "Go to 'Scan Crop' tab to run your first crop diagnostic analysis.",
                fontSize = 13.sp,
                color = Color.LightGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("history_screen_list"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "FARM CHRONICLES & LOGS",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            }

            items(diagnosesList) { log ->
                val dateStr = remember(log.timestamp) {
                    val sdf = SimpleDateFormat("MMM dd, yyyy - hh:mm a", Locale.getDefault())
                    sdf.format(Date(log.timestamp))
                }

                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(1.dp)
                        .clickable {
                            selectedDiagnosisForDetails = log
                            notesToEdit = log.notes
                        },
                    border = BorderStroke(1.dp, Color(0xFFE0E0E0))
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Small dynamic icon representing crop
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color(0xFFE8F5E9), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (log.isVideo) Icons.Filled.Videocam else Icons.Filled.LocalFlorist,
                                contentDescription = "Crop Icon",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = log.cropName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = when (log.severity.lowercase()) {
                                            "high" -> Color(0xFFFFEBEE)
                                            "medium" -> Color(0xFFFFF3E0)
                                            else -> Color(0xFFE8F5E9)
                                        }
                                    )
                                ) {
                                    Text(
                                        text = log.severity,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (log.severity.lowercase()) {
                                            "high" -> Color(0xFFC62828)
                                            "medium" -> Color(0xFFE65100)
                                            else -> Color(0xFF2E7D32)
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = log.illness,
                                fontSize = 13.sp,
                                color = if (log.illness.lowercase() == "healthy") MaterialTheme.colorScheme.primary else Color.Gray,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = dateStr,
                                fontSize = 11.sp,
                                color = Color.LightGray
                            )
                        }

                        IconButton(onClick = { viewModel.deleteDiagnosis(log) }) {
                            Icon(
                                imageVector = Icons.Filled.DeleteOutline,
                                contentDescription = "Delete record",
                                tint = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }

    // Detailed Log Overlayer Panel
    selectedDiagnosisForDetails?.let { log ->
        Dialog(onDismissRequest = { selectedDiagnosisForDetails = null }) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .shadow(8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "DIAGNOSTIC REPORT DETAILS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.2.sp
                        )

                        IconButton(onClick = { selectedDiagnosisForDetails = null }) {
                            Icon(Icons.Filled.Close, contentDescription = "Close")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = log.cropName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = log.illness,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (log.illness.lowercase() == "healthy") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Remedy Guide
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FBE7)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Healing, contentDescription = "Cure", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Cure / Remedies", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(log.cure, fontSize = 13.sp, color = Color(0xFF33691E), lineHeight = 18.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Edit observation notes
                    OutlinedTextField(
                        value = notesToEdit,
                        onValueChange = { notesToEdit = it },
                        label = { Text("Field Observations & Notes", fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.updateNotes(log, notesToEdit)
                                selectedDiagnosisForDetails = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Save Notes")
                        }

                        OutlinedButton(
                            onClick = { selectedDiagnosisForDetails = null },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Back")
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. KNOWLEDGE BASE SCREEN
// ==========================================
@Composable
fun KnowledgeBaseScreen() {
    var searchQuery by remember { mutableStateOf("") }

    val guides = listOf(
        FarmingGuide("Organic Fungicide Recipe", "Protect your crops naturally using simple kitchen ingredients.", "Mix 1 tbsp baking soda, 1 tsp organic liquid soap, and 1 gallon of water. Spray onto leaves in early morning. This changes leaf surface pH to stop fungal spore multiplication."),
        FarmingGuide("Early Blight vs Late Blight", "Learn to identify early and late blights on Solanaceous plants.", "Early Blight exhibits target-like concentric rings on leaves, starting from the base. Late Blight starts with greasy water-soaked lesions near leaf edges and white mold growth under wet soil conditions."),
        FarmingGuide("Preventing Soil Spores Splash", "Fungal disease prevention guide.", "Mulch soil around plants with organic straw or grass clipping sheets. This creates a barrier and prevents rain droplets from splashing fungal soil spores onto lower crop branches."),
        FarmingGuide("Crop Rotation Strategy", "Rotate crops to maintain root wellness.", "Never plant tomatoes, potatoes, eggplants, or peppers in the same spot consecutively. Rotate them with legumes (beans, alfalfa) which add natural nitrogen back into soils."),
        FarmingGuide("Optimal Water Routines", "Prevent foliage dampness.", "Always irrigate plants early in the morning near the root zone directly. Overhead irrigation leaves crop foliage wet for hours, creating a perfect incubation climate for rust mildew.")
    )

    val filteredGuides = guides.filter {
        it.title.lowercase().contains(searchQuery.lowercase()) ||
                it.desc.lowercase().contains(searchQuery.lowercase())
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("knowledge_base_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "AGRICULTURAL EXPERT KNOWLEDGE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 1.sp
            )
        }

        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
                placeholder = { Text("Search protection guides, blights, remedies...", fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = Color(0xFFE0E0E0)
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }

        items(filteredGuides) { guide ->
            var isExpanded by remember { mutableStateOf(false) }

            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(1.dp)
                    .clickable { isExpanded = !isExpanded },
                border = BorderStroke(1.dp, Color(0xFFE0E0E0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            guide.title,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                            contentDescription = "Expand",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(guide.desc, fontSize = 13.sp, color = Color.Gray)

                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Divider(color = Color(0xFFE8F5E9))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = guide.details,
                            fontSize = 13.sp,
                            color = Color(0xFF33691E),
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

data class FarmingGuide(
    val title: String,
    val desc: String,
    val details: String
)

/**
 * Custom offline bitmap generator that draws a beautiful stylized diseased leaf on Android Canvas.
 * This is parsed and sent to the real Gemini AI model, making emulator scans functional and fun!
 */
private fun generateDiseasedLeafBitmap(cropName: String, disease: String): Bitmap {
    val bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // Paints
    val bgPaint = Paint().apply { color = android.graphics.Color.parseColor("#F1F8E9") }
    val leafPaint = Paint().apply { color = android.graphics.Color.parseColor("#4CAF50") } // Green leaf
    val stemPaint = Paint().apply {
        color = android.graphics.Color.parseColor("#388E3C")
        strokeWidth = 10f
    }
    val spotPaint = Paint().apply { color = android.graphics.Color.parseColor("#795548") } // Brown spots
    val haloPaint = Paint().apply { color = android.graphics.Color.parseColor("#FFEB3B") } // Yellow halos
    val textPaint = Paint().apply {
        color = android.graphics.Color.BLACK
        textSize = 24f
        isFakeBoldText = true
    }

    // 1. Draw Background
    canvas.drawRect(0f, 0f, 400f, 400f, bgPaint)

    // 2. Draw Stem
    canvas.drawLine(200f, 350f, 200f, 150f, stemPaint)

    // 3. Draw Leaf Body (Ellipse/Path)
    canvas.drawOval(100f, 100f, 300f, 320f, leafPaint)

    // 4. Draw Specific spots based on illness
    when (disease.lowercase()) {
        "late blight" -> {
            // Draw greasy blackish brown spots with yellow borders
            canvas.drawCircle(170f, 150f, 35f, haloPaint)
            canvas.drawCircle(170f, 150f, 28f, spotPaint)

            canvas.drawCircle(220f, 250f, 28f, haloPaint)
            canvas.drawCircle(220f, 250f, 22f, spotPaint)
        }
        "rice blast" -> {
            // Draw diamond-shaped brown lesion spots
            canvas.drawCircle(190f, 180f, 15f, spotPaint)
            canvas.drawCircle(210f, 220f, 15f, spotPaint)
        }
        "common rust" -> {
            // Rust pustules: orange spots
            val rustPaint = Paint().apply { color = android.graphics.Color.parseColor("#FF9800") }
            canvas.drawCircle(150f, 160f, 10f, rustPaint)
            canvas.drawCircle(160f, 200f, 8f, rustPaint)
            canvas.drawCircle(220f, 180f, 12f, rustPaint)
            canvas.drawCircle(240f, 240f, 9f, rustPaint)
        }
        "powdery mildew" -> {
            // White mildew patches
            val whitePaint = Paint().apply { color = android.graphics.Color.WHITE; alpha = 180 }
            canvas.drawCircle(180f, 160f, 25f, whitePaint)
            canvas.drawCircle(220f, 220f, 30f, whitePaint)
        }
        else -> {
            // Healthy or fallback general brown rotting spots
            canvas.drawCircle(180f, 170f, 20f, spotPaint)
        }
    }

    // 5. Draw label at the bottom for diagnostics confirmation
    canvas.drawText("$cropName - $disease", 50f, 375f, textPaint)

    return bitmap
}
