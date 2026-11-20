package com.photor.app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.size.Size

private val SurfaceGlass=Color(0xCC17191F);private val SurfaceSoft=Color(0x991F222A);private val Accent=Color(0xFF8AB4F8)

@Composable fun Library(list:List<PhotoItem>,f:Filter,set:(Filter)->Unit,p:PaddingValues,open:(PhotoItem)->Unit){
 val wide=LocalConfiguration.current.screenWidthDp>=700
 Column(Modifier.fillMaxSize().padding(p)){
  Row(Modifier.horizontalScroll(rememberScrollState()).padding(horizontal=16.dp,vertical=8.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)){
   Chip("Все",f==Filter.ALL){set(Filter.ALL)};Chip("Фото",f==Filter.PHOTOS){set(Filter.PHOTOS)};Chip("Видео",f==Filter.VIDEOS){set(Filter.VIDEOS)};Chip("Избранное",f==Filter.FAVORITES){set(Filter.FAVORITES)};Chip("Скриншоты",f==Filter.SCREENSHOTS){set(Filter.SCREENSHOTS)}
  }
  Row(Modifier.fillMaxWidth().padding(horizontal=16.dp,vertical=6.dp),verticalAlignment=Alignment.CenterVertically){
   Column(Modifier.weight(1f)){Text(list.size.toString(),color=Color.White,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold);Text(if(wide)"Лента • большой экран" else "Лента",color=Color.White.copy(.5f),style=MaterialTheme.typography.labelMedium)}
   Surface(shape=RoundedCornerShape(50),color=SurfaceSoft){Text("Живой просмотр",color=Color.White.copy(.5f),style=MaterialTheme.typography.labelSmall,modifier=Modifier.padding(horizontal=12.dp,vertical=7.dp))}
  }
  Grid(list,open)
 }
}
@Composable private fun Chip(label:String,selected:Boolean,onClick:()->Unit){FilterChip(selected=selected,onClick=onClick,label={Text(label)},shape=RoundedCornerShape(50),colors=FilterChipDefaults.filterChipColors(containerColor=SurfaceSoft,selectedContainerColor=Color(0xFF315E9B),labelColor=Color.White,selectedLabelColor=Color.White))}
@Composable fun Grid(list:List<PhotoItem>,open:(PhotoItem)->Unit){
 if(list.isEmpty())Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally){Icon(Icons.Default.PhotoLibrary,null,Modifier.size(54.dp),tint=Color.White.copy(.3f));Spacer(Modifier.height(10.dp));Text("Здесь пока пусто",color=Color.White.copy(.6f))}}
 else{val wide=LocalConfiguration.current.screenWidthDp>=700;LazyVerticalGrid(columns=if(wide)GridCells.Adaptive(150.dp) else GridCells.Adaptive(108.dp),contentPadding=PaddingValues(start=12.dp,end=12.dp,top=8.dp,bottom=24.dp),horizontalArrangement=Arrangement.spacedBy(7.dp),verticalArrangement=Arrangement.spacedBy(7.dp)){items(list,key={it.id}){x->MediaTile(x){open(x)}}}}
}
@Composable private fun MediaTile(x:PhotoItem,open:()->Unit){
 var shown by remember{mutableStateOf(false)};LaunchedEffect(Unit){shown=true};val scale by animateFloatAsState(if(shown)1f else .88f,label="tileScale");val alpha by animateFloatAsState(if(shown)1f else 0f,label="tileAlpha")
 Box(Modifier.aspectRatio(1f).graphicsLayer{scaleX=scale;scaleY=scale;this.alpha=alpha;shadowElevation=7.dp.toPx();shape=RoundedCornerShape(18.dp);clip=true}.combinedClickable(onClick=open,onLongClick=open)){
  AsyncImage(ImageRequest.Builder(LocalContext.current).data(x.uri).size(Size(420,420)).build(),null,Modifier.fillMaxSize(),contentScale=ContentScale.Crop)
  Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent,Color.Black.copy(.48f)))))
  if(x.mime.startsWith("video/"))Icon(Icons.Default.PlayCircle,null,Modifier.align(Alignment.BottomEnd).padding(7.dp),tint=Color.White)
  if(fav(LocalContext.current,x.id))Icon(Icons.Default.Favorite,null,Modifier.align(Alignment.TopEnd).padding(7.dp),tint=Color.White)
 }
}
@Composable fun Collections(list:List<PhotoItem>,p:PaddingValues,open:(PhotoItem)->Unit){
 val c=LocalContext.current;val groups=listOf("Недавние" to list.take(18),"Избранное" to list.filter{fav(c,it.id)},"Видео" to list.filter{it.mime.startsWith("video/")},"Скриншоты" to list.filter{it.name.contains("screenshot",true)},"Скрытые" to emptyList(),"Недавно удалённые" to emptyList())
 LazyColumn(Modifier.fillMaxSize().padding(p),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(14.dp)){item{Text("Подборки",color=Color.White,style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);Text("Ваш контент, собранный автоматически",color=Color.White.copy(.5f))};items(groups){g->CollectionCard(g.first,g.second,open)}}
}
@Composable private fun CollectionCard(title:String,items:List<PhotoItem>,open:(PhotoItem)->Unit){
 Card(onClick={items.firstOrNull()?.let(open)},shape=RoundedCornerShape(26.dp),colors=CardDefaults.cardColors(containerColor=SurfaceGlass),elevation=CardDefaults.cardElevation(8.dp),modifier=Modifier.fillMaxWidth().graphicsLayer{shadowElevation=8.dp.toPx();shape=RoundedCornerShape(26.dp);clip=false}){
  Row(Modifier.padding(12.dp),verticalAlignment=Alignment.CenterVertically){
   if(items.isNotEmpty())AsyncImage(items.first().uri,null,Modifier.size(92.dp).clip(RoundedCornerShape(20.dp)),contentScale=ContentScale.Crop) else Box(Modifier.size(92.dp).clip(RoundedCornerShape(20.dp)).background(Color(0xFF252832)),contentAlignment=Alignment.Center){Icon(Icons.Default.Collections,null,tint=Color.White.copy(.7f))}
   Spacer(Modifier.width(15.dp));Column(Modifier.weight(1f)){Text(title,color=Color.White,fontWeight=FontWeight.SemiBold);Text(items.size.toString()+" элементов",color=Color.White.copy(.5f));Spacer(Modifier.height(7.dp));LinearProgressIndicator(progress={if(items.isEmpty())0f else .72f},modifier=Modifier.fillMaxWidth(.75f),color=Accent,trackColor=Color.White.copy(.08f))};Icon(Icons.Default.ChevronRight,null,tint=Color.White.copy(.35f))
  }
 }
}
@Composable fun Search(q:String,set:(String)->Unit,list:List<PhotoItem>,p:PaddingValues,open:(PhotoItem)->Unit){
 Column(Modifier.fillMaxSize().padding(p)){
  OutlinedTextField(value=q,onValueChange=set,modifier=Modifier.fillMaxWidth().padding(horizontal=14.dp,vertical=8.dp),shape=RoundedCornerShape(22.dp),singleLine=true,colors=OutlinedTextFieldDefaults.colors(focusedContainerColor=SurfaceSoft,unfocusedContainerColor=SurfaceSoft,focusedBorderColor=Accent,unfocusedBorderColor=Color.Transparent),placeholder={Text("Люди, места, даты, объекты…")},leadingIcon={Icon(Icons.Default.Search,null)})
  AnimatedVisibility(q.isEmpty(),enter=fadeIn()){Row(Modifier.horizontalScroll(rememberScrollState()).padding(14.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)){Suggestion("Недавние",Icons.Default.History);Suggestion("Избранное",Icons.Default.Favorite);Suggestion("Видео",Icons.Default.Movie);Suggestion("Скриншоты",Icons.Default.Screenshot)}}
  if(q.isEmpty())Text("Поиск работает локально — ваши фото никуда не отправляются.",color=Color.White.copy(.45f),modifier=Modifier.padding(horizontal=18.dp,vertical=8.dp)) else Grid(list,open)
 }
}
@Composable private fun Suggestion(t:String,icon:androidx.compose.ui.graphics.vector.ImageVector){Surface(shape=RoundedCornerShape(50),color=SurfaceSoft){Row(Modifier.padding(horizontal=14.dp,vertical=10.dp),verticalAlignment=Alignment.CenterVertically){Icon(icon,null,Modifier.size(18.dp),tint=Accent);Spacer(Modifier.width(7.dp));Text(t,color=Color.White)}}}