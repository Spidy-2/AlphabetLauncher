package com.example.alphabetlauncher

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.exp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.graphics.nativeCanvas



data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Bitmap
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { AlphabetLauncherApp() }
    }
}

@Composable
fun AlphabetLauncherApp() {
    val context = LocalContext.current
    var apps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var selectedLetter by remember { mutableStateOf<Char?>(null) }
    var dragging by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        apps = withContext(Dispatchers.IO) { loadLaunchableApps(context) }
    }

    val selectedApps = remember(apps, selectedLetter) {
        selectedLetter?.let { letter ->
            apps.filter { it.name.firstOrNull()?.uppercaseChar() == letter }
                .sortedBy { it.name.lowercase(Locale.getDefault()) }
        } ?: emptyList()
    }

    Box(
        modifier = Modifier.fillMaxSize().background(Color.Black)
    ) {
        if (dragging && selectedLetter != null) {
            FilteredApps(
                letter = selectedLetter!!,
                apps = selectedApps,
                context = context
            )
        } else {
            HomeContent(apps)
        }

        AlphabetBar(
            modifier = Modifier
                .fillMaxHeight()
                .width(92.dp)
                .align(Alignment.CenterEnd),
            onLetterChanged = { selectedLetter = it },
            onDraggingChanged = {
                dragging = it
                if (!it) selectedLetter = null
            }
        )
    }
}

fun loadLaunchableApps(context: Context): List<AppInfo> {
    val pm = context.packageManager
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)

    return pm.queryIntentActivities(intent, 0)
        .mapNotNull { info ->
            val name = info.loadLabel(pm)?.toString()?.trim().orEmpty()
            if (name.isBlank()) return@mapNotNull null
            AppInfo(
                name = name,
                packageName = info.activityInfo.packageName,
                icon = drawableToBitmap(info.loadIcon(pm))
            )
        }
        .distinctBy { it.packageName }
        .sortedBy { it.name.lowercase(Locale.getDefault()) }
}

fun drawableToBitmap(drawable: Drawable): Bitmap {
    if (drawable is BitmapDrawable && drawable.bitmap != null) return drawable.bitmap
    val w = drawable.intrinsicWidth.takeIf { it > 0 } ?: 96
    val h = drawable.intrinsicHeight.takeIf { it > 0 } ?: 96
    return Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888).also { bitmap ->
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
    }
}

@Composable
fun HomeContent(apps: List<AppInfo>) {
    var now by remember { mutableStateOf(Date()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = Date()
            delay(1000)
        }
    }

    val time = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val date = remember { SimpleDateFormat("EEE dd MMM", Locale.getDefault()) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 64.dp, top = 205.dp, end = 100.dp)
    ) {
        Text(time.format(now), color = Color.White, fontSize = 58.sp, fontWeight = FontWeight.Light)
        Text(date.format(now), color = Color.White, fontSize = 17.sp)

        Spacer(Modifier.height(26.dp))

        apps.take(7).forEach { app ->
            AppRow(app, context)
        }
    }
}

@Composable
fun FilteredApps(letter: Char, apps: List<AppInfo>, context: Context) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 38.dp, top = 110.dp, end = 105.dp)
    ) {
        Text(letter.toString(), color = Color.White, fontSize = 64.sp, fontWeight = FontWeight.Light)
        Spacer(Modifier.height(16.dp))

        if (apps.isEmpty()) {
            Text("No apps", color = Color.Gray, fontSize = 18.sp)
        } else {
            LazyColumn {
                items(apps, key = { it.packageName }) { app ->
                    AppRow(app, context)
                }
            }
        }
    }
}

@Composable
fun AppRow(app: AppInfo, context: Context) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                context.packageManager
                    .getLaunchIntentForPackage(app.packageName)
                    ?.let(context::startActivity)
            }
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        androidx.compose.foundation.Image(
            bitmap = app.icon.asImageBitmap(),
            contentDescription = app.name,
            modifier = Modifier.size(42.dp).clip(RoundedCornerShape(10.dp))
        )
        Spacer(Modifier.width(15.dp))
        Text(text = app.name, color = Color.White, fontSize = 16.sp)
    }
}

@Composable
fun AlphabetBar(
    modifier: Modifier,
    onLetterChanged: (Char) -> Unit,
    onDraggingChanged: (Boolean) -> Unit
) {
    val letters = ('A'..'Z').toList()
    var active by remember { mutableStateOf(false) }
    var fingerY by remember { mutableFloatStateOf(0f) }
    var selected by remember { mutableStateOf<Char?>(null) }

    Canvas(
        modifier = modifier.pointerInput(Unit) {
            detectDragGestures(
                onDragStart = { offset ->
                    active = true
                    onDraggingChanged(true)
                    fingerY = offset.y
                    val letter = positionToLetter(offset.y, size.height)
                    if (letter != selected) {
                        selected = letter
                        onLetterChanged(letter)
                    }
                },
                onDrag = { change, _ ->
                    change.consume()
                    fingerY = change.position.y
                    val letter = positionToLetter(change.position.y, size.height)
                    if (letter != selected) {
                        selected = letter
                        onLetterChanged(letter)
                    }
                },
                onDragEnd = {
                    active = false
                    selected = null
                    onDraggingChanged(false)
                },
                onDragCancel = {
                    active = false
                    selected = null
                    onDraggingChanged(false)
                }
            )
        }
    ) {
        val centerX = size.width * 0.70f
        val top = size.height * 0.18f
        val bottom = size.height * 0.86f
        val spacing = (bottom - top) / 25f
        val maxShift = size.width * 0.58f
        val falloff = size.height * 0.055f

        val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
            color = android.graphics.Color.WHITE
            textAlign = android.graphics.Paint.Align.CENTER
            typeface = android.graphics.Typeface.create("sans", android.graphics.Typeface.NORMAL)
        }

        paint.textSize = 22.dp.toPx()
        drawContext.canvas.nativeCanvas.drawText("☆", centerX, top - spacing * 1.15f, paint)

        letters.forEachIndexed { index, letter ->
            val y = top + index * spacing
            val distance = if (active) abs(y - fingerY) else Float.MAX_VALUE
            val shift = if (active) maxShift * exp(-distance / falloff) else 0f
            val x = centerX - shift

            paint.textSize = if (active && selected == letter) 22.dp.toPx() else 16.dp.toPx()
            drawContext.canvas.nativeCanvas.drawText(letter.toString(), x, y, paint)
        }

        drawCircle(Color.White, 2.2.dp.toPx(), Offset(centerX, bottom + spacing))

        if (active && selected != null) {
            val bubbleX = centerX - maxShift - 34.dp.toPx()
            drawCircle(Color.White, 25.dp.toPx(), Offset(bubbleX, fingerY))
            paint.color = android.graphics.Color.BLACK
            paint.textSize = 22.dp.toPx()
            drawContext.canvas.nativeCanvas.drawText(selected.toString(), bubbleX, fingerY + 8.dp.toPx(), paint)
            paint.color = android.graphics.Color.WHITE
        }
    }
}

fun positionToLetter(y: Float, height: Int): Char {
    val top = height * 0.18f
    val bottom = height * 0.86f
    val clamped = y.coerceIn(top, bottom)
    val progress = (clamped - top) / (bottom - top)
    val index = (progress * 25f).toInt().coerceIn(0, 25)
    return ('A'.code + index).toChar()
}
