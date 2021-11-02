package nl.vleeming.localreceiptsaver

import android.content.ContentValues
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import nl.vleeming.localreceiptsaver.ui.theme.LocalreceiptsaverTheme
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.rememberImagePainter
import java.io.File
import java.net.URI
import android.provider.MediaStore

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Environment
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import java.io.FileOutputStream
import java.lang.Exception
import kotlin.random.Random


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LocalreceiptsaverTheme {
                // A surface container using the 'background' color from the theme
                Default()
            }
        }
    }
}


@Composable
fun Default() {
    val files = LocalContext.current.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    var currentFileUri by remember { mutableStateOf(Uri.fromFile(files?.listFiles()?.last())) }
    LocalreceiptsaverTheme {
        Scaffold(floatingActionButton = {
            ScanReceiptFab() {
                currentFileUri = it
            }
        }) {}
        Column {
            Row {
                PreviewImage(currentFileUri)
                SelectImage {
                    currentFileUri = it
                }
                files?.listFiles()?.toList()?.let { AllImages(files = it) }
            }

        }
    }
}

@Composable
fun AllImages(files: List<File>) {
    LazyColumn {
        items(files) { file ->
            PreviewImage(tempFileUri = Uri.fromFile(file))
        }
    }
}


@Composable
fun SelectImage(pictureSelected: (uri: Uri) -> Unit) {
    val test =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                pictureSelected(uri)
            }
        }
    Button(onClick = { test.launch("image/*") }) {
        Text(text = "Select image")
    }
}

@Composable
fun PreviewImage(tempFileUri: Uri) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(300.dp), shape = RectangleShape
    ) {
        Image(painter = rememberImagePainter(tempFileUri), contentDescription = "Temp photo")
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    LocalreceiptsaverTheme {
        Scaffold(floatingActionButton = { ScanReceiptFab() {} }) {}
    }
}


fun getTempFileUri(context: Context): Uri {
    val storageDirectory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    val file = File.createTempFile("tmp_image_file", ".jpg", storageDirectory)

    return FileProvider.getUriForFile(
        context,
        "${BuildConfig.APPLICATION_ID}.provider",
        file
    )
}

@Composable
fun ScanReceiptFab(pictureTaken: (uri: Uri) -> Unit) {
    val context = LocalContext.current
    var fileUri by remember { mutableStateOf(Uri.EMPTY) }

    val takePictureLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { isSuccess ->
            if (isSuccess) {
                pictureTaken(fileUri)
            }
        }
    FloatingActionButton(onClick = {
        fileUri = getTempFileUri(context)
        takePictureLauncher.launch(fileUri)
    }) {
        Icon(
            Icons.Filled.Search,
            stringResource(id = R.string.scan_receipt_fab_content_description)
        )
    }
}