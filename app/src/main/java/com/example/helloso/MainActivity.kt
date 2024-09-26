package com.example.helloso

import android.Manifest
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.helloso.ui.theme.HelloSoTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HelloSoTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    companion object {
        init {
            System.loadLibrary("rustlib")
            initLog()
        }
    }
}

private external fun hello(dir: String, input: String): String

private external fun initLog()

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val cachePath = context.cacheDir.path

    var loading by remember {
        mutableStateOf(false)
    }

    val scope = rememberCoroutineScope()

    val imageFilePathState = remember {
        mutableStateOf("")
    }

    val fileState = remember {
        mutableStateOf("")
    }

    Column(modifier = modifier.fillMaxSize()) {
        HorizontalDivider(modifier = Modifier.height(1.dp))
        Text(text = cachePath)
        HorizontalDivider(modifier = Modifier.height(1.dp))
        Text(
            text = fileState.value
        )
        Row() {

            Button(onClick = {
                loading = true
                scope.launch(Dispatchers.IO) {
                    val s = hello(cachePath, imageFilePathState.value)
                    withContext(Dispatchers.Main) {
                        loading = false
                        fileState.value = s
                        imageFilePathState.value = s
                    }
                }
            }) {
                Text("Change Image")
            }
            Spacer(modifier = Modifier.width(10.dp))
            if (loading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .width(30.dp)
                        .height(30.dp)
                )
            }
        }
        HorizontalDivider(modifier = Modifier.height(1.dp))
        TakePhoto {
            imageFilePathState.value = it
        }
        HorizontalDivider(modifier = Modifier.height(1.dp))
        RequestPermission()
        if (imageFilePathState.value.isNotEmpty()) {
            Image(
                painter = rememberAsyncImagePainter(model = imageFilePathState.value),
                contentDescription = ""
            )
        }
        Spacer(modifier = Modifier.height(10.dp))

    }
}

fun getFilePathFromUri(context: Context, uri: Uri): String? {
    // https://developer.android.com/training/data-storage/shared/media?hl=zh-cn#kotlin
    val docsId = DocumentsContract.getDocumentId(uri)
    Log.d("PhotoPicker", "docId: ${docsId}")
    val id = docsId.split(":")[1]
    Log.d("PhotoPicker", "id: ${id}")
    var filePath: String? = null
    val projection = arrayOf(MediaStore.Images.Media.DATA)
    val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    val selection = "${MediaStore.Images.Media._ID}=?"
    val selectionArgs = arrayOf(id)
    val cursor: Cursor? =
        context.contentResolver.query(contentUri, projection, selection, selectionArgs, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            filePath = it.getString(columnIndex)
        }
    }
    return filePath
}

@Composable
fun TakePhoto(callback: (String) -> Unit) {
    val context = LocalContext.current
    val takePhotoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                // uri: content://com.android.providers.media.documents/document/image%3A198809
                Log.d("PhotoPicker", "Selected URI: $uri")

                val filepath = getFilePathFromUri(context, uri)
                if (filepath != null) {
                    callback(filepath)
                }
                Log.d("PhotoPicker", "Selected FilePath: $filepath")
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        })
    Button(onClick = {
        takePhotoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }) {
        Text(text = "Pick Image")
    }
}

@Composable
fun RequestPermission() {
    val permission = Manifest.permission.READ_EXTERNAL_STORAGE
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                // Permission granted, proceed with the action
                // For example, you can now access media images
            } else {
                // Permission denied, show a message to the user
            }
        }
    )
    Button(onClick = {
        permissionLauncher.launch(permission)
    }) {
        Text(text = "Request Permission")
    }

}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    HelloSoTheme {
        Greeting("Android")
    }
}