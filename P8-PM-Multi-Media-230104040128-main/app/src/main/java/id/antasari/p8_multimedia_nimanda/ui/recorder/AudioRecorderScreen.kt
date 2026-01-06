package id.antasari.p8_multimedia_nimanda.ui.recorder

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import id.antasari.p8_multimedia_nimanda.ui.Screen
import id.antasari.p8_multimedia_nimanda.util.AudioFileData
import id.antasari.p8_multimedia_nimanda.util.FileManagerUtility
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AudioRecorderScreen(
    onBack: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val activity = context as Activity
    val scrollState = rememberScrollState()

    // Permission check (Fungsi tetap sama)
    LaunchedEffect(Unit) {
        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                101
            )
        }
    }

    var isRecording by remember { mutableStateOf(false) }
    var recorder: MediaRecorder? by remember { mutableStateOf(null) }
    var outputFile by remember { mutableStateOf("") }

    // State for list & dialogs
    var audioFiles by remember { mutableStateOf(loadAudioFiles(context)) }
    var showRenameDialog by remember { mutableStateOf<File?>(null) }
    var newFileName by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf<File?>(null) }

    // Start Recording (Logika tetap sama)
    fun startRecording() {
        val fileName = "audio_${System.currentTimeMillis()}.mp4"
        val file = File(context.filesDir, fileName)
        outputFile = file.absolutePath

        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }

        try {
            recorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile)
                prepare()
                start()
                isRecording = true
            }
        } catch (e: Exception) {
            Log.e("Recorder", "Start error: ${e.message}")
            isRecording = false
        }
    }

    // Stop Recording (Logika tetap sama)
    fun stopRecording() {
        try {
            if (isRecording) recorder?.stop()
        } catch (e: Exception) {
            Log.e("Recorder", "Stop error: ${e.message}")
        }
        recorder?.release()
        recorder = null
        isRecording = false

        val size = File(outputFile).length()
        if (size > 500) {
            val encoded = Uri.encode(outputFile)
            onNavigate(Screen.AudioPlayer.passPath(encoded))
            audioFiles = loadAudioFiles(context)
        }
    }

    // UI Structure
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFAFAFA)) // TEMA: Background Putih Tulang
    ) {
        // TEMA: TopAppBar Putih dengan Ikon/Teks Hitam
        TopAppBar(
            title = { Text("Audio Recorder", fontWeight = FontWeight.SemiBold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.White,
                titleContentColor = Color.Black,
                navigationIconContentColor = Color.Black,
                actionIconContentColor = Color.Black
            )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(28.dp))

            // Status Text
            Text(
                text = if (isRecording) "Recording..." else "Ready to Record",
                // TEMA: Merah saat merekam, Hitam saat diam
                color = if (isRecording) Color(0xFFE53935) else Color.Black,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(28.dp))

            // Main Recording Button (FAB)
            FloatingActionButton(
                onClick = {
                    if (!isRecording) startRecording() else stopRecording()
                },
                shape = CircleShape,
                modifier = Modifier.size(100.dp),
                // TEMA: Merah saat merekam, Hitam (Clean) saat diam
                containerColor = if (isRecording) Color(0xFFE53935) else Color(0xFF212121)
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
            Spacer(Modifier.height(20.dp))

            // Text Button (Alternative)
            Button(
                onClick = {
                    if (!isRecording) startRecording() else stopRecording()
                },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                // TEMA: Tombol Hitam Solid agar elegan
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black,
                    contentColor = Color.White
                )
            ) {
                Text(if (isRecording) "Stop Recording" else "Start Recording")
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider(color = Color.LightGray)
            Spacer(Modifier.height(16.dp))

            Text(
                "Daftar Rekaman",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.Black // Pastikan teks header hitam
            )
            Spacer(Modifier.height(12.dp))

            audioFiles.forEach { item ->
                FileCard(
                    data = item,
                    onPlay = {
                        val encoded = Uri.encode(item.file.absolutePath)
                        onNavigate(Screen.AudioPlayer.passPath(encoded))
                    },
                    onEdit = {
                        showRenameDialog = item.file
                        newFileName = item.file.nameWithoutExtension
                    },
                    onDelete = {
                        showDeleteDialog = item.file
                    }
                )
                Spacer(Modifier.height(12.dp))
            }
            Spacer(Modifier.height(30.dp))
        }
    }

    // Rename Dialog (Logika & Tampilan Standar Material3)
    if (showRenameDialog != null) {
        val file = showRenameDialog!!
        AlertDialog(
            containerColor = Color.White, // TEMA: Dialog Putih
            onDismissRequest = { showRenameDialog = null },
            title = { Text("Edit Nama File", color = Color.Black) },
            text = {
                OutlinedTextField(
                    value = newFileName,
                    onValueChange = { newFileName = it },
                    label = { Text("Nama baru (tanpa .mp4)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedBorderColor = Color.Black,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color.Black
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        FileManagerUtility.renameFile(file, "$newFileName.mp4")
                        audioFiles = loadAudioFiles(context)
                        showRenameDialog = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Black)
                ) { Text("Simpan") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showRenameDialog = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
                ) { Text("Batal") }
            }
        )
    }

    // Delete Dialog
    if (showDeleteDialog != null) {
        val file = showDeleteDialog!!
        AlertDialog(
            containerColor = Color.White, // TEMA: Dialog Putih
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Hapus File?", color = Color.Black) },
            text = { Text(file.name, color = Color.Black) },
            confirmButton = {
                TextButton(
                    onClick = {
                        FileManagerUtility.deleteFile(file)
                        audioFiles = loadAudioFiles(context)
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFFE53935)) // Merah
                ) { Text("Yes") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteDialog = null },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color.Gray)
                ) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun FileCard(
    data: AudioFileData,
    onPlay: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPlay() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        // TEMA: Card Putih Bersih (bukan hijau muda lagi)
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // TEMA: Icon Audio Hitam
                Icon(
                    Icons.Default.AudioFile,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(34.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    data.file.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black // TEMA: Text Hitam
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(
                "${formatDuration(data.durationMs)} • ${data.sizeText}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray, // TEMA: Text info Abu-abu
                modifier = Modifier.padding(start = 46.dp)
            )
            Spacer(Modifier.height(6.dp))
            Row(modifier = Modifier.padding(start = 46.dp)) {
                Text(
                    "[Edit]",
                    modifier = Modifier.clickable { onEdit() },
                    color = Color.Black, // TEMA: Link Hitam
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(Modifier.width(20.dp))
                Text(
                    "[Delete]",
                    modifier = Modifier.clickable { onDelete() },
                    color = Color(0xFFE53935), // TEMA: Link Merah
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

fun loadAudioFiles(context: android.content.Context): List<AudioFileData> {
    val audioExtensions = listOf("mp3", "wav", "m4a", "mp4")
    return FileManagerUtility.getAllAudioFiles(context)
        .filter { it.extension.lowercase() in audioExtensions }
        .map { file ->
            AudioFileData(
                file,
                FileManagerUtility.getAudioDuration(context, file),
                FileManagerUtility.formatFileSize(file.length())
            )
        }
}

fun formatDuration(ms: Long): String {
    val sec = (ms / 1000)
    val min = sec / 60
    val s = sec % 60
    return "%02d:%02d".format(min, s)
}