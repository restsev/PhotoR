package com.photor.app
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun Viewer(item: PhotoItem, list: List<PhotoItem>, close: () -> Unit, edit: () -> Unit, showInfo: () -> Unit) {
    val c = LocalContext.current
    val safeList = if (list.isEmpty()) listOf(item) else list
    val pager = rememberPagerState(initialPage = safeList.indexOfFirst { it.id == item.id }.coerceAtLeast(0), pageCount = { safeList.size })
    Surface(Modifier.fillMaxSize(), color = Color.Black) {
        Box(Modifier.fillMaxSize()) {
            HorizontalPager(state = pager, modifier = Modifier.fillMaxSize()) { page ->
                val x = safeList[page]
                if (x.mime.startsWith("video/")) VideoPlayer(x.uri) else ZoomablePhoto(x.uri)
            }
            Row(Modifier.fillMaxWidth().statusBarsPadding().padding(6.dp)) {
                IconButton(onClick = close) { Icon(Icons.Default.Close, null, tint = Color.White) }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = showInfo) { Icon(Icons.Default.Info, null, tint = Color.White) }
            }
            Row(Modifier.align(Alignment.BottomCenter).fillMaxWidth().navigationBarsPadding().padding(horizontal = 18.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                val x = safeList[pager.currentPage]
                IconButton(onClick = { toggleFav(c, x.id) }) { Icon(if (fav(c, x.id)) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null, tint = Color.White) }
                if (!x.mime.startsWith("video/")) IconButton(onClick = edit) { Icon(Icons.Default.Edit, null, tint = Color.White) }
                IconButton(onClick = { share(c, x.uri) }) { Icon(Icons.Default.Share, null, tint = Color.White) }
                IconButton(onClick = { trash(c, x.uri) }) { Icon(Icons.Default.Delete, null, tint = Color.White) }
            }
        }
    }
}

@Composable
private fun ZoomablePhoto(uri: android.net.Uri) {
    var scale by remember(uri) { mutableFloatStateOf(1f) }
    var offsetX by remember(uri) { mutableFloatStateOf(0f) }
    var offsetY by remember(uri) { mutableFloatStateOf(0f) }
    AsyncImage(model = uri, contentDescription = null, modifier = Modifier.fillMaxSize()
        .pointerInput(uri) { detectTransformGestures { _, pan, zoom, _ ->
            scale = (scale * zoom).coerceIn(1f, 5f)
            offsetX = (offsetX + pan.x).coerceIn(-1600f, 1600f)
            offsetY = (offsetY + pan.y).coerceIn(-1600f, 1600f)
        }}
        .graphicsLayer { scaleX = scale; scaleY = scale; translationX = offsetX; translationY = offsetY },
        contentScale = ContentScale.Fit)
}

@Composable
private fun VideoPlayer(uri: android.net.Uri) {
    val context = LocalContext.current
    val player = remember(uri) { ExoPlayer.Builder(context).build().apply {
        setMediaItem(ExoMediaItem.fromUri(uri)); prepare(); playWhenReady = false
    }}
    DisposableEffect(player) { onDispose { player.release() } }
    AndroidView(factory = { ctx -> PlayerView(ctx).apply {
        this.player = player; useController = true; setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
    }}, update = { it.player = player }, modifier = Modifier.fillMaxSize())
}

@Composable
fun Editor(item: PhotoItem, close: () -> Unit, saved: () -> Unit) {
    val c = LocalContext.current
    var rot by remember { mutableFloatStateOf(0f) }
    var gray by remember { mutableStateOf(false) }
    var bmp by remember { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(item.id) { bmp = withContext(Dispatchers.IO) { c.contentResolver.openInputStream(item.uri)?.use { BitmapFactory.decodeStream(it) } } }
    Surface(Modifier.fillMaxSize(), color = Color.Black) {
        Column(Modifier.fillMaxSize()) {
            Row(Modifier.fillMaxWidth().statusBarsPadding()) {
                IconButton(onClick = close) { Icon(Icons.Default.Close, null, tint = Color.White) }
                Text("Изменить", color = Color.White, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 12.dp))
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { save(c, bmp, rot, gray); saved() }) { Text("Готово") }
            }
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                bmp?.let { Image(it.asImageBitmap(), null, Modifier.fillMaxSize().padding(12.dp), contentScale = ContentScale.Fit) }
            }
            Row(Modifier.fillMaxWidth().navigationBarsPadding().padding(20.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                AssistChip(onClick = { rot += 90f }, label = { Text("Повернуть") })
                AssistChip(onClick = { gray = !gray }, label = { Text(if (gray) "Цвет" else "Ч/Б") })
            }
        }
    }
}

@Composable
fun Info(x: PhotoItem, close: () -> Unit) {
    val date = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(x.date))
    AlertDialog(onDismissRequest = close, title = { Text(x.name) },
        text = { Text("Дата: ${date}\nРазмер: ${x.width} × ${x.height}\nПапка: ${x.folder}\nПуть: ${x.relativePath}\nТип: ${x.mime}") },
        confirmButton = { TextButton(onClick = close) { Text("Закрыть") } })
}

@Composable
fun Sort(newest: Boolean, choose: (Boolean) -> Unit, close: () -> Unit) {
    AlertDialog(onDismissRequest = close, title = { Text("Сортировка") },
        text = { Column {
            TextButton(onClick = { choose(true) }) { Text(if (newest) "✓ Сначала новые" else "Сначала новые") }
            TextButton(onClick = { choose(false) }) { Text(if (!newest) "✓ Сначала старые" else "Сначала старые") }
        }}, confirmButton = {})
}
