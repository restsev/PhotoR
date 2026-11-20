package com.photor.app

import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.ContextCompat

fun mediaGranted(c:Context)=ContextCompat.checkSelfPermission(c,Manifest.permission.READ_MEDIA_IMAGES)==PackageManager.PERMISSION_GRANTED||ContextCompat.checkSelfPermission(c,Manifest.permission.READ_MEDIA_VIDEO)==PackageManager.PERMISSION_GRANTED
fun fav(c:Context,id:Long)=c.getSharedPreferences("photor",0).getBoolean("fav$id",false)
fun toggleFav(c:Context,id:Long){val p=c.getSharedPreferences("photor",0);p.edit().putBoolean("fav$id",!p.getBoolean("fav$id",false)).apply()}
fun loadMedia(c:Context):List<PhotoItem>{val r=c.contentResolver;val out=mutableListOf<PhotoItem>();val u=MediaStore.Files.getContentUri("external");val pr=arrayOf(MediaStore.Files.FileColumns._ID,MediaStore.Files.FileColumns.DISPLAY_NAME,MediaStore.Files.FileColumns.DATE_TAKEN,MediaStore.Files.FileColumns.MIME_TYPE,MediaStore.Files.FileColumns.WIDTH,MediaStore.Files.FileColumns.HEIGHT,MediaStore.Files.FileColumns.MEDIA_TYPE);r.query(u,pr,"${MediaStore.Files.FileColumns.MEDIA_TYPE}=? OR ${MediaStore.Files.FileColumns.MEDIA_TYPE}=?",arrayOf("1","3"),"${MediaStore.Files.FileColumns.DATE_TAKEN} DESC")?.use{q->val id=q.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID);val n=q.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME);val d=q.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_TAKEN);val m=q.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE);val w=q.getColumnIndexOrThrow(MediaStore.Files.FileColumns.WIDTH);val h=q.getColumnIndexOrThrow(MediaStore.Files.FileColumns.HEIGHT);while(q.moveToNext()){val i=q.getLong(id);val mm=q.getString(m)?:"image/*";val cu=if(mm.startsWith("video/"))Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,"${i}") else Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"${i}");out+=PhotoItem(i,cu,q.getString(n)?:"${i}",q.getLong(d),mm,q.getInt(w),q.getInt(h))}};return out}
fun share(c:Context,u:Uri){c.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply{type=c.contentResolver.getType(u)?:"image/*";putExtra(Intent.EXTRA_STREAM,u);addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)},"Поделиться"))}
fun trash(c:Context,u:Uri){try{val a=c as? Activity;if(a!=null){val p=MediaStore.createTrashRequest(c.contentResolver,listOf(u),true);a.startIntentSenderForResult(p.intentSender,1201,null,0,0,0)}}catch(_:Throwable){c.contentResolver.delete(u,null,null)}}
fun save(c:Context,b:Bitmap?,rot:Float,gray:Boolean){if(b==null)return;val m=Matrix().apply{postRotate(rot)};var o=Bitmap.createBitmap(b,0,0,b.width,b.height,m,true);if(gray){val x=Bitmap.createBitmap(o.width,o.height,Bitmap.Config.ARGB_8888);android.graphics.Canvas(x).drawBitmap(o,0f,0f,android.graphics.Paint().apply{colorFilter=ColorMatrixColorFilter(ColorMatrix().apply{setSaturation(0f)})});o=x};val v=ContentValues().apply{put(MediaStore.Images.Media.DISPLAY_NAME,"PhotoR_${System.currentTimeMillis()}.jpg");put(MediaStore.Images.Media.MIME_TYPE,"image/jpeg");put(MediaStore.Images.Media.RELATIVE_PATH,"Pictures/PhotoR")};c.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,v)?.let{c.contentResolver.openOutputStream(it)?.use{o.compress(Bitmap.CompressFormat.JPEG,95,it)}};Toast.makeText(c,"Изменённая копия сохранена",Toast.LENGTH_SHORT).show()}