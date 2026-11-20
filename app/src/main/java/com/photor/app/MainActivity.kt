package com.photor.app

import android.Manifest
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class PhotoItem(val id: Long,val uri: Uri,val name: String,val date: Long,val mime: String,val width: Int,val height: Int)
enum class Tab { LIBRARY,COLLECTIONS,SEARCH }
enum class Filter { ALL,PHOTOS,VIDEOS,FAVORITES,SCREENSHOTS }

class MainActivity: ComponentActivity(){ override fun onCreate(b: Bundle?){super.onCreate(b);setContent{PhotoR()}} }

@OptIn(ExperimentalMaterial3Api::class,ExperimentalFoundationApi::class)
@Composable fun PhotoR(){
 val c=LocalContext.current
 var permission by remember{mutableStateOf(mediaGranted(c))}
 val launcher=rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){permission=it.values.any{v->v}}
 if(!permission){ PermissionScreen{launcher.launch(arrayOf(Manifest.permission.READ_MEDIA_IMAGES,Manifest.permission.READ_MEDIA_VIDEO))};return }
 var data by remember{mutableStateOf<List<PhotoItem>>(emptyList())}; var tab by rememberSaveable{mutableStateOf(Tab.LIBRARY)}; var filter by rememberSaveable{mutableStateOf(Filter.ALL)}; var query by rememberSaveable{mutableStateOf("")}; var selected by remember{mutableStateOf<PhotoItem?>(null)}; var editor by remember{mutableStateOf<PhotoItem?>(null)}; var info by remember{mutableStateOf<PhotoItem?>(null)}; var newest by rememberSaveable{mutableStateOf(true)}; var sort by remember{mutableStateOf(false)}; var refresh by remember{mutableIntStateOf(0)}
 LaunchedEffect(permission,refresh){data=withContext(Dispatchers.IO){loadMedia(c)}}
 val visible=remember(data,filter,query,newest){val q=query.trim().lowercase();data.filter{f->when(filter){Filter.ALL->true;Filter.PHOTOS->!f.mime.startsWith("video/");Filter.VIDEOS->f.mime.startsWith("video/");Filter.FAVORITES->fav(c,f.id);Filter.SCREENSHOTS->f.name.contains("screenshot",true)}&&(q.isEmpty()||f.name.lowercase().contains(q))}.sortedBy{if(newest)-it.date else it.date}}
 Scaffold(containerColor=Color.Black,topBar={CenterAlignedTopAppBar(title={Text("PhotoR",fontWeight=FontWeight.SemiBold)},actions={IconButton({sort=true}){Icon(Icons.Default.Sort,"Сортировка")};IconButton({filter=Filter.FAVORITES}){Icon(Icons.Default.FavoriteBorder,"Избранное")}},colors=TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor=Color.Black,titleContentColor=Color.White,actionIconContentColor=Color.White))},bottomBar={NavigationBar(containerColor=Color.Black){Nav("Медиатека",Icons.Default.Image,tab==Tab.LIBRARY){tab=Tab.LIBRARY};Nav("Коллекции",Icons.Default.Collections,tab==Tab.COLLECTIONS){tab=Tab.COLLECTIONS};Nav("Поиск",Icons.Default.Search,tab==Tab.SEARCH){tab=Tab.SEARCH}}}){p->when(tab){Tab.LIBRARY->Library(visible,filter,{filter=it},p){selected=it};Tab.COLLECTIONS->Collections(data,p){selected=it};Tab.SEARCH->Search(query,{query=it},visible,p){selected=it}}}
 selected?.let{Viewer(it,visible,{selected=null},{editor=it},{info=it})};editor?.let{Editor(it,{editor=null}){editor=null;refresh++}};info?.let{Info(it){info=null}};if(sort)Sort(newest,{newest=it;sort=false}){sort=false}
}
@Composable fun Nav(t:String,icon:androidx.compose.ui.graphics.vector.ImageVector,sel:Boolean,click:()->Unit){NavigationBarItem(selected=sel,onClick=click,icon={Icon(icon,null)},label={Text(t)})}
@Composable fun PermissionScreen(grant:()->Unit){Surface(Modifier.fillMaxSize(),color=Color.Black){Column(Modifier.fillMaxSize().padding(28.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Icon(Icons.Default.Collections,null,Modifier.size(72.dp),tint=Color.White);Text("PhotoR",color=Color.White,style=MaterialTheme.typography.displaySmall,fontWeight=FontWeight.Bold);Spacer(Modifier.height(12.dp));Text("Ваша медиатека — в одном месте",color=Color.White);Spacer(Modifier.height(24.dp));Button(onClick=grant){Text("Разрешить доступ")}}}}