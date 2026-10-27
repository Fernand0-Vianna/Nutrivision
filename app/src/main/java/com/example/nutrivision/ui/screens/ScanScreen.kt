package com.example.nutrivision.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.nutrivision.data.ApiConfig
import com.example.nutrivision.data.FoodAnalyzer
import com.example.nutrivision.data.FoodRecord
import com.example.nutrivision.data.RecordRepository
import com.example.nutrivision.ui.components.NbButtonPrimary
import com.example.nutrivision.ui.components.NbButtonSecondary
import com.example.nutrivision.ui.components.NbSectionHeader
import com.example.nutrivision.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun ScanScreen(
    repository: RecordRepository,
    onBack: () -> Unit,
    onAnalyzed: (FoodRecord) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }
    var pendingPath by remember { mutableStateOf<String?>(null) }
    val configured = remember { ApiConfig.isConfigured(context) }
    val lastPhoto = remember {
        repository.loadRecords().sortedByDescending { it.timestamp }
            .firstOrNull { !it.imagePath.isNullOrBlank() && File(it.imagePath).exists() }
            ?.imagePath
    }

    fun analyze(path: String) {
        loading = true
        error = null
        scope.launch {
            try {
                val record = withContext(Dispatchers.IO) {
                    val bitmap = decodeFile(path)
                        ?: throw IllegalStateException("Não foi possível ler a imagem")
                    FoodAnalyzer.analyze(bitmap, ApiConfig.resolveKey(context))
                        .copy(imagePath = path)
                }
                loading = false
                pendingPath = null
                onAnalyzed(record)
            } catch (e: Exception) {
                loading = false
                pendingPath = path
                error = e.message ?: "Falha na análise"
            }
        }
    }

    fun importGallery(uri: Uri) {
        loading = true
        error = null
        scope.launch {
            try {
                val path = withContext(Dispatchers.IO) { repository.importScanImage(uri) }
                    ?: throw IllegalStateException("Não foi possível importar a imagem")
                analyze(path)
            } catch (e: Exception) {
                loading = false
                error = e.message ?: "Falha ao importar a imagem"
            }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        val uri = cameraUri
        if (success && uri != null) {
            loading = true
            error = null
            scope.launch {
                try {
                    val path = withContext(Dispatchers.IO) { repository.importScanImage(uri) }
                        ?: throw IllegalStateException("Não foi possível salvar a foto")
                    analyze(path)
                } catch (e: Exception) {
                    loading = false
                    error = e.message ?: "Falha ao salvar a foto"
                }
            }
        }
    }

    fun openCamera() {
        val uri = newCameraUri(context)
        cameraUri = uri
        cameraLauncher.launch(uri)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) importGallery(uri)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) openCamera()
        else error = "Permissão de câmera negada. Use a galeria ou autorize nas configurações."
    }

    fun requestCamera() {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        if (granted) openCamera() else permissionLauncher.launch(Manifest.permission.CAMERA)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(SurfaceGesture, RoundedCornerShape(4.dp))
                    .border(2.dp, Bone.copy(alpha = 0.35f), RoundedCornerShape(4.dp))
                    .semantics { contentDescription = "Fechar scan" }
                    .clickable(enabled = !loading, onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "×", color = Bone, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                text = "NOVO SCAN",
                color = Bone,
                fontSize = 14.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = ArchivoFamily,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.width(44.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (loading) {
            CircularProgressIndicator(color = Lime)
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "ANALISANDO...",
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = ArchivoFamily,
                color = Bone,
                letterSpacing = 0.4.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (configured) "Consultando o Gemini" else "Configure a chave Gemini para análise automática",
                fontSize = 13.sp,
                color = Bone.copy(alpha = 0.7f)
            )
        } else {
            // Visor estilizado com cantos de enquadramento
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.92f)
                    .background(SurfaceGesture, RoundedCornerShape(4.dp))
                    .border(2.dp, Bone.copy(alpha = 0.18f), RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (lastPhoto != null) {
                    AsyncImage(
                        model = File(lastPhoto),
                        contentDescription = "Último alimento fotografado",
                        contentScale = ContentScale.Crop,
                        alpha = 0.28f,
                        modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(4.dp))
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .border(3.dp, Lime, RoundedCornerShape(4.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "FOTO",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = ArchivoFamily,
                            color = Lime,
                            letterSpacing = 1.1.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(18.dp))
                    Text(
                        text = "APONTE PARA O ALIMENTO",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = ArchivoFamily,
                        color = Bone,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Enquadre a porção inteira, com boa luz",
                        fontSize = 13.sp,
                        color = Bone.copy(alpha = 0.65f),
                        textAlign = TextAlign.Center
                    )
                }
                // Cantos do visor
                Box(Modifier.align(Alignment.TopStart).padding(14.dp).size(22.dp).border(3.dp, Lime).clip(RoundedCornerShape(2.dp)))
                Box(Modifier.align(Alignment.TopEnd).padding(14.dp).size(22.dp).border(3.dp, Lime).clip(RoundedCornerShape(2.dp)))
                Box(Modifier.align(Alignment.BottomStart).padding(14.dp).size(22.dp).border(3.dp, Lime).clip(RoundedCornerShape(2.dp)))
                Box(Modifier.align(Alignment.BottomEnd).padding(14.dp).size(22.dp).border(3.dp, Lime).clip(RoundedCornerShape(2.dp)))
            }
            Spacer(modifier = Modifier.height(18.dp))
            if (!configured) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Warning.copy(alpha = 0.14f), RoundedCornerShape(4.dp))
                        .border(1.dp, Warning.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        text = "Sem chave Gemini a análise automática fica indisponível. Você pode registrar os valores manualmente.",
                        color = Bone,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        error?.let {
            Text(
                text = it,
                color = Danger,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        NbButtonPrimary(
            text = "Tirar foto",
            onClick = { requestCamera() },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
        NbButtonSecondary(
            text = "Escolher da galeria",
            onClick = { galleryLauncher.launch("image/*") },
            modifier = Modifier.fillMaxWidth()
        )
        if (!configured || error != null) {
            Spacer(modifier = Modifier.height(10.dp))
            NbButtonSecondary(
                text = "Inserir manualmente",
                onClick = {
                    onAnalyzed(
                        com.example.nutrivision.data.FoodRecord(
                            mealName = "Refeição",
                            imagePath = pendingPath,
                            source = "manual"
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.height(28.dp))
    }
}

private fun newCameraUri(context: android.content.Context): Uri {
    val dir = File(context.cacheDir, "camera").apply { mkdirs() }
    val file = File.createTempFile("scan_", ".jpg", dir)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
}

private fun decodeFile(path: String): Bitmap? {
    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(path, bounds)
    var sample = 1
    val maxSide = 1600
    while (bounds.outWidth / sample > maxSide || bounds.outHeight / sample > maxSide) {
        sample *= 2
    }
    val options = BitmapFactory.Options().apply { inSampleSize = sample }
    return BitmapFactory.decodeFile(path, options)
}
