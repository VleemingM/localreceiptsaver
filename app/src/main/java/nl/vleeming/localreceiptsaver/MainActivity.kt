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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import android.graphics.ImageDecoder
import android.os.Build
import android.os.Environment
import androidx.compose.foundation.layout.Row


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
    val context = LocalContext.current
    var tempFileUri by remember { mutableStateOf(getTempFileUri(context)) }
    var currentFileUri by remember { mutableStateOf(Uri.EMPTY) }
    LocalreceiptsaverTheme {
        Scaffold(floatingActionButton = {
            ScanReceiptFab(tempFileUri) {
                currentFileUri = it
                tempFileUri = getTempFileUri(context)
                saveBitmap(context,currentFileUri)
            }
        }) {}
        Row {
            PreviewImage(currentFileUri)
            SelectImage{
                currentFileUri = it
            }
        }
    }
}

@Composable
fun SelectImage(pictureSelected: (uri: Uri) -> Unit) {
    val test =rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){
            uri: Uri? -> uri?.let {pictureSelected(uri) }}
    Button(onClick = {test.launch("image/*")}){
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
        Scaffold(floatingActionButton = { ScanReceiptFab(Uri.EMPTY) {} }) {}
    }
}


fun saveBitmap(context:Context ,uri: Uri){
    // TODO: VLEEMING 01/11/2021 how does one save a file LOL
    var values = ContentValues()
    values.put(MediaStore.Images.Media.TITLE, uri.path);
    values.put(MediaStore.Images.Media.DISPLAY_NAME, "test");
    values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
    values.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
    values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/RECEIPTS")
    }

    context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

}

fun getTempFileUri(context: Context): Uri {
    val tmpFile = File.createTempFile("tmp_image_file", ".png").apply {
        createNewFile()

    }
    return FileProvider.getUriForFile(
        context,
        "${BuildConfig.APPLICATION_ID}.provider",
        tmpFile
    )

}

@Composable
fun ScanReceiptFab(tempFileUri: Uri, pictureTaken: (uri: Uri) -> Unit) {
    val takePictureLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.TakePicture()
        ) { isSuccess ->
            if (isSuccess) {
                pictureTaken(tempFileUri)
            }


        }
    FloatingActionButton(onClick = {
        takePictureLauncher.launch(tempFileUri)
    }) {
        Icon(
            Icons.Filled.Search,
            stringResource(id = R.string.scan_receipt_fab_content_description)
        )
    }
}