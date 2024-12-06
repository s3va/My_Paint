package one.two.three.mypaint

import android.content.ContentResolver
import android.content.ContentValues
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Picture
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.util.TypedValue
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.draw
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import one.two.three.mypaint.ui.theme.MyPaintTheme
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyPaintTheme {
                val vm: MainViewModel = viewModel()
                val picSaveFlow by vm.savePic.collectAsStateWithLifecycle()
                val ctx = LocalContext.current
                var showDialog by remember { mutableStateOf(false) }

                val permi =
                    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { x ->
                        if (x) {
                            Toast.makeText(
                                ctx,
                                "Write external permission granted",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(
                                ctx,
                                "Write external permission NOT granted  !!!!",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                val selectedColor = remember {
                    mutableStateOf(Color.Blue)
                }
                val pathsClrs = remember {
                    mutableStateListOf<Pair<Path, Color>>()
                }
                val pointsList = remember {
                    mutableStateListOf<Pair<Offset, Color>>()
                }

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                        val permissionCheckResult = ContextCompat.checkSelfPermission(
                            ctx,
                            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                        if (permissionCheckResult == PackageManager.PERMISSION_DENIED) {
                            permi.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        }
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(Modifier.padding(innerPadding)) {
                        PaintScr(
                            modifier = Modifier
                                .weight(1f),
                            lColor = selectedColor,
                            pathsList = pathsClrs,
                            pointsList = pointsList,
                            picSave = picSaveFlow,
                            picSaved = vm::picSaved
                        )
                        ColorBar(
                            modifier = Modifier,
                            onClk = { c: Color ->
                                selectedColor.value = c
                            },
                            savePicture = {
                                vm.savePic()
                            },
                            openDialog = { showDialog = true },
                        )
                    }
                }

                if (showDialog) {
                    MyDialog(
                        onDismiss = { showDialog = false },
                        mBitmap = bitmap,
                    )
                }
            }
        }
    }
}

var bitmap: Bitmap? by mutableStateOf(null)
var mUriToPic: Uri? by mutableStateOf(null)
var myFileName: String? by mutableStateOf(null)
var picture = Picture()

@Composable
fun PaintScr(
    modifier: Modifier = Modifier,
    lColor: MutableState<Color>,
    pathsList: SnapshotStateList<Pair<Path, Color>>,
    pointsList: SnapshotStateList<Pair<Offset, Color>>,
    picSave: Boolean,
    picSaved: () -> Unit,
) {
    var mPath by remember { mutableStateOf(Path()) }
    val graphicsLayer = rememberGraphicsLayer()
    val ctx = LocalContext.current

    LaunchedEffect(picSave) {
        Log.i("laucheff", "PaintScr: picSave = $picSave")
        if (picSave) {

            val btm = Bitmap.createBitmap(
                picture.width,
                picture.height,
                Bitmap.Config.ARGB_8888
            )

            val canvas = android.graphics.Canvas(btm)
            //canvas.drawColor(android.graphics.Color.WHITE)

//            pathsList.forEach {
//                val lPaint = android.graphics.Paint()
//                lPaint.color = it.second.toArgb()
//                lPaint.strokeWidth = TypedValue.applyDimension(
//                    TypedValue.COMPLEX_UNIT_DIP,
//                    5f,
//                    ctx.resources.displayMetrics
//                )
//                lPaint.style=android.graphics.Paint.Style.STROKE
//
//                canvas.drawPath(
//                    it.first.asAndroidPath(),
//                    lPaint,
//                    //style = Stroke(5.dp.toPx())
//                )
//            }
//            canvas.drawPath(
//                mPath.asAndroidPath(),
//                android.graphics.Paint().apply {
//                    color = lColor.value.toArgb()
//                    strokeWidth = TypedValue.applyDimension(
//                        TypedValue.COMPLEX_UNIT_DIP,
//                        5f,
//                        ctx.resources.displayMetrics
//                    )
//                    style=android.graphics.Paint.Style.STROKE
//                },
//            )

            canvas.drawPicture(picture)
            bitmap = btm

//            bitmap = graphicsLayer.toImageBitmap().asAndroidBitmap()
            val mFileName = "Paint-" +
                    LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmmss")
                    )
            myFileName= "$mFileName.png"
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                val resolver: ContentResolver = ctx.contentResolver
                val contentValues = ContentValues()
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, mFileName)
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/paint")
                resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                    ?.let { iUri ->
                        mUriToPic = iUri
                        resolver.openOutputStream(iUri)?.use { fos ->
                            bitmap?.compress(Bitmap.CompressFormat.PNG, 100, fos)
                        }
                    }
            } else {
                val rootDir = Environment.getExternalStorageDirectory()
                val picDir = File(rootDir, Environment.DIRECTORY_PICTURES)
                val f = File(picDir, "MyPainter")
                //val f = File(ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES),"MyPaint")
                if (!f.exists()) {
                    if (!f.mkdirs()) {
                        return@LaunchedEffect
                    }
                }
                val picFile = File(f, "$mFileName.png")
                Log.i(TAG, "PaintScr: $picFile")
                picFile.outputStream().use { fos ->
                    bitmap?.compress(Bitmap.CompressFormat.PNG, 100, fos)
                }

            }

            picSaved()
        }
        //fos!!.flush()
        //fos!!.close()
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color.LightGray)
            .drawWithCache {
                // Example that shows how to redirect rendering to an Android Picture and then
                // draw the picture into the original destination
                val width = this.size.width.toInt()
                val height = this.size.height.toInt()
                onDrawWithContent {
                    val pictureCanvas =
                        androidx.compose.ui.graphics.Canvas(
                            picture.beginRecording(
                                width,
                                height
                            )
                        )
                    draw(
                        this,
                        this.layoutDirection,
                        pictureCanvas,
                        this.size
                    ) {
                        this@onDrawWithContent.drawContent()
                    }
                    picture.endRecording()

                    drawIntoCanvas { canvas ->
                        canvas.nativeCanvas.drawPicture(
                            picture
                        )
                    }
                }
            }

//            .drawWithCache {
//                onDrawWithContent {
//                    //.drawWithContent {
//                    Log.i(
//                        "onDrawWithContext",
//                        ".drawWithContent: ${graphicsLayer.size} ${this.size} !!!!!!!!!!!!!!!!!!!!!!!!!!!"
//                    )
//
//                    graphicsLayer.record {
//                        Log.i(
//                            "onDrawWithContext",
//                            "graphicsLayer: ${graphicsLayer.size} ${this.size} !!!!!!!!!!!!!!!!!!!!!!!!!!!"
//                        )
//
//                        //            this@drawWithContent.drawContent()
//
//                        this@onDrawWithContent.drawContent()
//                    }
//                    drawLayer(graphicsLayer)
//                }
//            }
            .pointerInput(true) {
                detectDragGestures(
                    onDragStart = { offset: Offset ->
                        mPath = Path()
                        mPath.moveTo(offset.x, offset.y)

                    },
                    onDragEnd = {
                        pathsList.add(Pair(mPath, lColor.value))
                        mPath = Path()
                    },
                ) { change, dragAmount ->
//                    p.value.moveTo(
//                        change.position.x - dragAmount.x,
//                        change.position.y - dragAmount.y
//                    )
                    mPath = Path().apply {
                        addPath(mPath)
                        lineTo(
                            change.position.x,
                            change.position.y
                        )
                    }
                }
            }
            .pointerInput(true) {
                detectTapGestures { offset: Offset ->
                    pointsList.add(Pair(offset, lColor.value))
                }
            }
    ) {
        pathsList.forEach {
            drawPath(
                it.first,
                it.second,
                style = Stroke(5.dp.toPx())
            )
        }
        pointsList.forEach {
            drawPoints(listOf(it.first), PointMode.Points, it.second, 5.dp.toPx())
        }
        drawPath(
            mPath,
            lColor.value,
            style = Stroke(5.dp.toPx()),
        )

    }
}

private const val TAG = "MainActivity"

@Composable
fun ColorBar(
    modifier: Modifier = Modifier,
    onClk: (Color) -> Unit,
    savePicture: () -> Unit,
    openDialog: () -> Unit,
) {
    val clrs = listOf(Color.Red, Color.Yellow, Color.Green, Color.Magenta, Color.Blue, Color.Black)
    val ctx = LocalContext.current

    Row(
        modifier
            .background(Color.Gray)
            .padding(8.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,

        ) {
        clrs.forEach { clr: Color ->
            Box(
                Modifier
                    .padding(end = 8.dp)
                    .clickable {
                        onClk(clr)
                    }
                    .size(40.dp)
                    .background(clr, CircleShape)
            )
        }
        Spacer(Modifier.weight(1f))
        FilledIconButton(
            onClick = {
                savePicture()
                openDialog()
            },
        ) {
            Icon(Icons.Default.Save, "Save")
        }
//        OutlinedIconButton(
//            // ctx.startActivity(Intent(ctx, BitmActivity::class.java))
//            openDialog
//        ) {
//            Icon(Icons.AutoMirrored.Default.ArrowRight, "open in another activity")
//        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ColorBarPreview() {
    ColorBar(onClk = {}, savePicture = {}, openDialog = {})
}
