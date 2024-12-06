package one.two.three.mypaint

import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat.startActivity

private const val TAG = "MyDialog"
@Composable
fun MyDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    mBitmap: Bitmap?,
) {
    val ctx = LocalContext.current

    Dialog(
        onDismiss,
    ) {
        Surface {
            Column {
                mBitmap?.let {
                    Image(
                        it.asImageBitmap(),
                        "My Picture",
                        Modifier.border(BorderStroke(1.dp, Color.Black))
                    )
                }
                Text(myFileName?:"")
                HorizontalDivider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    TextButton({
                        Log.i(TAG, "MyDialog: myFileName $myFileName")
                        Log.i(TAG, "MyDialog: mUriToPic  $mUriToPic")
                        val shareIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            // Example: content://com.google.android.apps.photos.contentprovider/...
                            putExtra(Intent.EXTRA_STREAM, mUriToPic)
                            type = "image/png"
                        }
                        ctx.startActivity(Intent.createChooser(shareIntent, null))

                    }) {
                        Text("Send To")
                    }

                    TextButton(
                        onDismiss,
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }
}