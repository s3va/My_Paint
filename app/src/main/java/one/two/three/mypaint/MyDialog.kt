package one.two.three.mypaint

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun MyDialog(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    mBitmap: Bitmap
) {
    Dialog(
        onDismiss,
    ) {
        Surface {
            Column {
                Image(
                    mBitmap.asImageBitmap(),
                    "My Picture",
                    Modifier.border(BorderStroke(1.dp, Color.Black))
                )
                TextButton(
                    onDismiss,
                    Modifier.align(Alignment.End)
                ) {
                    Text("Close")
                }
            }
        }
    }
}