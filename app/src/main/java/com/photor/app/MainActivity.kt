package com.photor.app

import android.Manifest
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class PhotoItem(val id:Long,val uri:Uri,val name:String,val date:Long,val mime:String,val width:Int,val height:Int,val folder:String,val relativePath:String)
enum class Tab { LIBRARY,COLLECTIONS,SEARCH }
enum class Filter { ALL,PHOTOS,VIDEOS,FAVORITES,SCREENSHOTS }

class MainActivity:ComponentActivity(){override fun onCreate(b:Bundle?){super.onCreate(b);setContent{PhotoR()}}}

@OptIn(ExperimentalMaterial3Api::class,ExperimentalFoundationApi::class)
@Composable fun PhotoR(){
 val c=LocalContext.current;var permission by remember{mutableStateOf(mediaGranted(c))}
 val launcher=rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){permission=it.values.any{v->v}}
 if(!permission){PermissionScreen{launcher.launch(arrayOf(Manifest.permission.READ_MEDIA_IMAGES,Manifest.permission.READ_MEDIA_VIDEO))};return}
 var data by remember{mutableStateOf<List<PhotoItem>>(emptyList())};var tab by rememberSaveable{mutableStateOf(Tab.LIBRARY)};var filter by rememberSaveable{mutableStateOf(Filter.ALL)};var query by rememberSaveable{mutableStateOf("")};var selected by remember{mutableStateOf<PhotoItem?>(null)};var selectedList by remember{mutableStateOf<List<PhotoItem>>(emptyList())};var editor by remember{mutableStateOf<PhotoItem?>(null)};var info by remember{mutableStateOf<PhotoItem?>(null)};var newest by rememberSaveable{mutableStateOf(true)};var sort by remember{mutableStateOf(false)};var refresh by remember{mutableIntStateOf(0)}
 LaunchedEffect(permission,refresh){data=withContext(Dispatchers.IO){loadMedia(c)}}
 val visible=remember(data,filter,query,newest){val q=query.trim().lowercase();data.filter{f->when(filter){Filter.ALL->true;Filter.PHOTOS->!f.mime.startsWith("video/");Filter.VIDEOS->f.mime.startsWith("video/");Filter.FAVORITES->fav(c,f.id);Filter.SCREENSHOTS->f.name.contains("screenshot",true)}&&(q.isEmpty()||f.name.lowercase().contains(q)||f.folder.lowercase().contains(q))}.sortedBy{if(newest)-it.date else it.date}}
 val wide=LocalConfiguration.current.screenWidthDp>=700
 Scaffold(containerColor=Color.Transparent,contentWindowInsets=WindowInsets.safeDrawing,topBar={PhotoTopBar(tab,wide,{sort=true},{filter=Filter.FAVORITES})},bottomBar={PhotoNav(tab){tab=it}}){p->Box(Modifier.fillMaxSize()){PhotoBackdrop();when(tab){Tab.LIBRARY->Library(visible,filter,{filter=it},p){selected=it;selectedList=visible};Tab.COLLECTIONS->Collections(data,p){items->selected=items.firstOrNull();selectedList=items};Tab.SEARCH->Search(query,{query=it},visible,p){selected=it;selectedList=visible}}}}
 selected?.let{Viewer(it,selectedList.ifEmpty{listOf(it)},{selected=null},{editor=it},{info=it})};editor?.let{Editor(it,{editor=null}){editor=null;refresh++}};info?.let{Info(it){info=null}};if(sort)Sort(newest,{newest=it;sort=false}){sort=false}
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable fun PhotoTopBar(tab:Tab,wide:Boolean,sort:()->Unit,favClick:()->Unit){TopAppBar(title={Column{Text(if(tab==Tab.LIBRARY)"Медиатека" else if(tab==Tab.COLLECTIONS)"Коллекции" else "Поиск",fontWeight=FontWeight.Bold);if(wide)Text("Адаптивный режим • большой экран",style=MaterialTheme.typography.labelSmall,color=Color.White.copy(.52f))}},actions={IconButton(onClick=favClick){Icon(Icons.Default.FavoriteBorder,"Избранное")};IconButton(onClick=sort){Icon(Icons.Default.Tune,"Вид")}},colors=TopAppBarDefaults.topAppBarColors(containerColor=Color.Transparent,titleContentColor=Color.White,actionIconContentColor=Color.White))}
@Composable fun PhotoNav(tab:Tab,set:(Tab)->Unit){NavigationBar(containerColor=Color(0xE6141418),tonalElevation=12.dp){Nav("Медиатека",Icons.Default.PhotoLibrary,tab==Tab.LIBRARY){set(Tab.LIBRARY)};Nav("Коллекции",Icons.Default.Collections,tab==Tab.COLLECTIONS){set(Tab.COLLECTIONS)};Nav("Поиск",Icons.Default.Search,tab==Tab.SEARCH){set(Tab.SEARCH)}}}
@Composable fun RowScope.Nav(t:String,icon:androidx.compose.ui.graphics.vector.ImageVector,sel:Boolean,click:()->Unit){NavigationBarItem(selected=sel,onClick=click,icon={Icon(icon,null)},label={Text(t)},alwaysShowLabel=true)}
@Composable fun PhotoBackdrop(){Box(Modifier.fillMaxSize()){Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF08090C),Color(0xFF101116),Color(0xFF07080A)))));Box(Modifier.fillMaxWidth().height(260.dp).align(Alignment.TopCenter).background(Brush.radialGradient(listOf(Color(0xFF254A78).copy(.28f),Color.Transparent),radius=520f)));Box(Modifier.size(260.dp).align(Alignment.TopEnd).offset(x=70.dp,y=70.dp).background(Brush.radialGradient(listOf(Color(0xFF7A4D9E).copy(.14f),Color.Transparent),radius=300f)))}}
@Composable fun PermissionScreen(grant:()->Unit){Surface(Modifier.fillMaxSize(),color=Color(0xFF08090C)){Box(Modifier.fillMaxSize()){PhotoBackdrop();Column(Modifier.fillMaxSize().padding(28.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Icon(Icons.Default.Collections,null,Modifier.size(72.dp),tint=Color.White);Text("PhotoR",color=Color.White,style=MaterialTheme.typography.displaySmall,fontWeight=FontWeight.Bold);Spacer(Modifier.height(12.dp));Text("Медиатека в стиле флагманского интерфейса.",color=Color.White.copy(.7f));Spacer(Modifier.height(28.dp));Button(onClick=grant,shape=androidx.compose.foundation.shape.RoundedCornerShape(18.dp),modifier=Modifier.height(54.dp)){Text("Разрешить доступ")}}}}}