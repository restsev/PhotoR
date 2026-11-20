package com.photor.app

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.size.Size

@Composable fun Library(list:List<PhotoItem>,f:Filter,set:(Filter)->Unit,p:PaddingValues,open:(PhotoItem)->Unit){Column(Modifier.fillMaxSize().padding(p).background(Color.Black)){Row(Modifier.horizontalScroll(rememberScrollState()).padding(8.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)){FilterChip(f==Filter.ALL,{set(Filter.ALL)},{Text("Все")});FilterChip(f==Filter.PHOTOS,{set(Filter.PHOTOS)},{Text("Фото")});FilterChip(f==Filter.VIDEOS,{set(Filter.VIDEOS)},{Text("Видео")});FilterChip(f==Filter.FAVORITES,{set(Filter.FAVORITES)},{Text("Избранное")})};Grid(list,open)}}
@Composable fun Grid(list:List<PhotoItem>,open:(PhotoItem)->Unit){if(list.isEmpty())Text("Нет медиа",color=Color.Gray,modifier=Modifier.padding(20.dp)) else LazyVerticalGrid(columns=GridCells.Adaptive(112.dp),contentPadding=PaddingValues(6.dp),horizontalArrangement=Arrangement.spacedBy(6.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){items(list,key={it.id}){x->Box(Modifier.aspectRatio(1f).combinedClickable(onClick={open(x)})){AsyncImage(ImageRequest.Builder(LocalContext.current).data(x.uri).size(Size(300,300)).build(),null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop);if(x.mime.startsWith("video/"))Icon(Icons.Default.PlayCircle,null,Modifier.align(Alignment.BottomEnd).padding(5.dp),tint=Color.White);if(fav(LocalContext.current,x.id))Icon(Icons.Default.Favorite,null,Modifier.align(Alignment.TopEnd).padding(5.dp),tint=Color.White)}}}}
@Composable fun Collections(list:List<PhotoItem>,p:PaddingValues,open:(PhotoItem)->Unit){val c=LocalContext.current;val groups=listOf("Недавние дни" to list.take(12),"Избранное" to list.filter{fav(c,it.id)},"Видео" to list.filter{it.mime.startsWith("video/")},"Скриншоты" to list.filter{it.name.contains("screenshot",true)},"Скрытые" to emptyList(),"Недавно удалённые" to emptyList());LazyColumn(Modifier.fillMaxSize().padding(p).background(Color.Black),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){item{Text("Коллекции",color=Color.White,style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold)};items(groups){g->Card(onClick={g.second.firstOrNull()?.let(open)},shape=RoundedCornerShape(22.dp),colors=CardDefaults.cardColors(containerColor=Color(0xFF17171B)),elevation=CardDefaults.cardElevation(defaultElevation=8.dp),modifier=Modifier.shadow(6.dp,RoundedCornerShape(22.dp))){Row(Modifier.padding(14.dp),verticalAlignment=Alignment.CenterVertically){if(g.second.isNotEmpty())AsyncImage(g.second.first().uri,null,Modifier.size(88.dp).shadow(3.dp,RoundedCornerShape(16.dp)).clip(RoundedCornerShape(16.dp)),contentScale=ContentScale.Crop)else Box(Modifier.size(88.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFF222228)),contentAlignment=Alignment.Center){Icon(Icons.Default.Collections,null,tint=Color.White)};Spacer(Modifier.width(14.dp));Column{Text(g.first);Text("${g.second.size} элементов",color=Color.Gray)}}}}}}
@Composable fun Search(q:String,set:(String)->Unit,list:List<PhotoItem>,p:PaddingValues,open:(PhotoItem)->Unit){Column(Modifier.fillMaxSize().padding(p).background(Color.Black)){OutlinedTextField(value=q,onValueChange=set,modifier=Modifier.fillMaxWidth().padding(14.dp).shadow(5.dp,RoundedCornerShape(20.dp)),placeholder={Text("Люди, места, даты, объекты…")},leadingIcon={Icon(Icons.Default.Search,null)});if(q.isEmpty())Text("Поиск по имени файла и локальным категориям. Медиатека не отправляется в сеть.",color=Color.Gray,modifier=Modifier.padding(20.dp))else Grid(list,open)}}