package id.antasari.p8_multimedia_nimanda.ui.gallery

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import id.antasari.p8_multimedia_nimanda.ui.Screen
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraGalleryScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val scrollState = rememberScrollState()

    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showCameraOption by remember { mutableStateOf(false) }

    // TRANSFORM STATE
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // --- LAUNCHERS ---

    // 1. Camera Launcher
    val takePhotoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            selectedBitmap = it
            scale = 1f; offsetX = 0f; offsetY = 0f
        }
    }

    // 2. Video Launcher
    val recordVideoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                val file = copyToInternal(context, uri, "video_", ".mp4")
                onNavigate(Screen.VideoPlayer.passPath(Uri.encode(file.absolutePath)))
            }
        }
    }

    // 3. Permission Launcher (PENTING: Solusi untuk Error Anda)
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showCameraOption = true
        } else {
            Toast.makeText(context, "Izin Kamera diperlukan untuk fitur ini", Toast.LENGTH_SHORT).show()
        }
    }

    // 4. Gallery Launcher
    val pickMediaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val type = context.contentResolver.getType(it) ?: ""
            when {
                type.startsWith("image") -> {
                    selectedBitmap = loadBitmap(context, it)
                    scale = 1f; offsetX = 0f; offsetY = 0f
                }
                type.startsWith("video") -> {
                    val file = copyToInternal(context, it, "video_", ".mp4")
                    onNavigate(Screen.VideoPlayer.passPath(Uri.encode(file.absolutePath)))
                }
                type.startsWith("audio") -> {
                    val file = copyToInternal(context, it, "audio_", ".mp4")
                    onNavigate(Screen.AudioPlayer.passPath(Uri.encode(file.absolutePath)))
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA))
    ) {
        TopAppBar(
            title = { Text("Camera & Gallery", fontWeight = FontWeight.SemiBold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(12.dp))

            // CARD MENU OPEN CAMERA
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // CEK PERMISSION RUNTIME
                        val permissionCheck = ContextCompat.checkSelfPermission(
                            context, Manifest.permission.CAMERA
                        )
                        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                            showCameraOption = true
                        } else {
                            permissionLauncher.launch(Manifest.permission.CAMERA)
                        }
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF212121))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Open Camera", fontWeight = FontWeight.Bold, color = Color.White)
                        Text("Take a new photo or Video", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(0.7f))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // CARD MENU GALLERY
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { pickMediaLauncher.launch("*/*") },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color.Black)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Image, null, tint = Color.Black, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text("Choose from Gallery", fontWeight = FontWeight.Bold, color = Color.Black)
                        Text("Select existing photo or video", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // PREVIEW & EMPTY STATE (TETAP SAMA SEPERTI SEBELUMNYA)
            if (selectedBitmap != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(4.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Image(
                        bitmap = selectedBitmap!!.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isLandscape) 420.dp else 360.dp)
                            .graphicsLayer(scaleX = scale, scaleY = scale, translationX = offsetX, translationY = offsetY)
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scale = (scale * zoom).coerceIn(1f, 5f)
                                    offsetX += pan.x; offsetY += pan.y
                                }
                            }
                            .padding(12.dp)
                    )
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { selectedBitmap?.let { saveImageToGallery(context, it) } },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Icon(Icons.Default.Save, null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Simpan ke Galeri")
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Image, null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("No image selected", color = Color.Gray)
                    }
                }
            }

            Spacer(Modifier.height(30.dp))
            Text(
                "Copyright © 2025\nPraktikum #8\nS1 Teknologi Informasi UIN Antasari",
                textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall, color = Color.Gray
            )
        }
    }

    // DIALOG OPSI CAMERA
    if (showCameraOption) {
        AlertDialog(
            containerColor = Color.White,
            onDismissRequest = { showCameraOption = false },
            title = { Text("Open Camera") },
            text = { Text("Choose what you want to capture") },
            confirmButton = {
                TextButton(onClick = { showCameraOption = false; takePhotoLauncher.launch(null) }) { Text("Photo", color = Color.Black) }
            },
            dismissButton = {
                TextButton(onClick = { showCameraOption = false; recordVideoLauncher.launch(Intent(MediaStore.ACTION_VIDEO_CAPTURE)) }) { Text("Video", color = Color.Black) }
            }
        )
    }
}

// HELPERS (TIDAK BERUBAH)
fun saveImageToGallery(context: Context, bitmap: Bitmap) {
    val filename = "IMG_${System.currentTimeMillis()}.jpg"
    var fos: OutputStream? = null
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraGallery")
        }
        val imageUri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        fos = imageUri?.let { context.contentResolver.openOutputStream(it) }
    }
    fos?.use {
        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, it)
        Toast.makeText(context, "Foto berhasil disimpan ke Galeri", Toast.LENGTH_SHORT).show()
    }
}

fun copyToInternal(context: Context, uri: Uri, prefix: String, ext: String): File {
    val input = context.contentResolver.openInputStream(uri)!!
    val file = File(context.filesDir, "$prefix${System.currentTimeMillis()}$ext")
    FileOutputStream(file).use { output -> input.copyTo(output) }
    input.close()
    return file
}

fun loadBitmap(context: Context, uri: Uri): Bitmap? {
    return context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
}