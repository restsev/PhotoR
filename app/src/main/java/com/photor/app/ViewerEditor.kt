package com.photor.app

import android.graphics.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

@Composable fun Viewer(item:PhotoItem,list:List<PhotoItem>,close:()->Unit,edit:()->Unit,showInfo:()->Unit){val c=LocalContext.current;val pager=rememberPagerState(initialPage=maxOf(0,list.indexOfFirst{it.id==item.id}),pageCount={list.size.coerceAtLeast(1)});Surface(Modifier.fillMaxSize(),color=Color.Black){Box(Modifier.fillMaxSize()){HorizontalPager(state=pager,modifier=Modifier.fillMaxSize()){i->AsyncImage(list[i].uri,null,Modifier.fillMaxSize(),contentScale=ContentScale.Fit)};Row(Modifier.fillMaxWidth().statusBarsPadding().padding(6.dp)){IconButton(close){Icon(Icons.Default.Close,null,tint=Color.White)};Spacer(Modifier.weight(1f));IconButton(showInfo){Icon(Icons.Default.Info,null,tint=Color.White)}};Row(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(18.dp),horizontalArrangement=Arrangement.SpaceEvenly){val x=list[pager.currentPage];IconButton({toggleFav(c,x.id)}){Icon(if(fav(c,x.id))Icons.Default.Favorite else Icons.Default.FavoriteBorder,null,tint=Color.White)};IconButton(edit){Icon(Icons.Default.Edit,null,tint=Color.White)};IconButton({share(c,x.uri)}){Icon(Icons.Default.Share,null,tint=Color.White)};IconButton({trash(c,x.uri)}){Icon(Icons.Default.Delete,null,tint=Color.White)}}}}}

@Composable fun Editor(item:PhotoItem,close:()->Unit,saved:()->Unit){val c=LocalContext.current;var rot by remember{mutableFloatStateOf(0f)};var gray by remember{mutableStateOf(false)};var bmp by remember{mutableStateOf<Bitmap?>(null)};LaunchedEffect(item.id){bmp=withContext(Dispatchers.IO){c.contentResolver.openInputStream(item.uri)?.use{BitmapFactory.decodeStream(it)}}};Surface(Modifier.fillMaxSize(),color=Color.Black){Column(Modifier.fillMaxSize()){Row(Modifier.fillMaxWidth().statusBarsPadding()){IconButton(close){Icon(Icons.Default.Close,null,tint=Color.White)};Text("Изменить",color=Color.White,style=MaterialTheme.typography.titleLarge);Spacer(Modifier.weight(1f));TextButton(onClick={save(c,bmp,rot,gray);saved()}){Text("Готово")}};Box(Modifier.weight(1f).fillMaxWidth(),contentAlignment=Alignment.Center){bmp?.let{androidx.compose.foundation.Image(it.asImageBitmap(),null,Modifier.fillMaxSize().padding(12.dp),contentScale=ContentScale.Fit)}};Row(Modifier.fillMaxWidth().padding(20.dp).navigationBarsPadding(),horizontalArrangement=Arrangement.SpaceEvenly){AssistChip(onClick={rot+=90},{Text("Повернуть")});AssistChip(onClick={gray=!gray},{Text(if(gray)"Цвет" else "Ч/Б")})}}}}
@Composable fun Info(x:PhotoItem,close:()->Unit){AlertDialog(onDismissRequest=close,title={Text(x.name)},text={Text("Дата: ${SimpleDateFormat("dd.MM.yyyy HH:mm",Locale.getDefault()).format(Date(x.date))}\nРазмер: ${x.width} × ${x.height}\nТип: ${x.mime}")},confirmButton={TextButton(onClick=close){Text("Закрыть")}})}
@Composable fun Sort(newest:Boolean,choose:(Boolean)->Unit,close:()->Unit){AlertDialog(onDismissRequest=close,title={Text("Сортировка")},text={Column{TextButton(onClick={choose(true)}){Text(if(newest)"✓ Сначала новые" else "Сначала новые")};TextButton(onClick={choose(false)}){Text(if(!newest)"✓ Сначала старые" else "Сначала старые")}}},confirmButton={})}