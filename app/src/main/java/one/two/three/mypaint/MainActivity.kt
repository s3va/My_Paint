package one.two.three.mypaint

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import one.two.three.mypaint.ui.theme.MyPaintTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyPaintTheme {
                val selectedColor = remember {
                    mutableStateOf(Color.Blue)
                }
                val pathsClrs = remember {
                    mutableStateListOf<Pair<Path, Color>>()
                }
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(Modifier.padding(innerPadding)) {
                        PaintScr(
                            modifier = Modifier
                                .weight(1f),
                            color = selectedColor,
                            pathsList = pathsClrs,
                        )
                        ColorBar(modifier = Modifier,
                            onClk = { c: Color ->
                                selectedColor.value = c
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PaintScr(
    modifier: Modifier = Modifier,
    color: MutableState<Color>,
    pathsList: SnapshotStateList<Pair<Path, Color>>,
) {
    val p = remember {
        mutableStateOf(Path())
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .background(Color.LightGray)
            .pointerInput(true) {
                detectDragGestures(
                    onDragStart = { offset: Offset ->
                        p.value = Path()
                        p.value.moveTo(offset.x, offset.y)

                    },
                    onDragEnd = {
                        pathsList.add(Pair(p.value, color.value))
                        p.value= Path()
                    },
                ) { change, dragAmount ->
//                    p.value.moveTo(
//                        change.position.x - dragAmount.x,
//                        change.position.y - dragAmount.y
//                    )
                    p.value=Path().apply {
                        addPath(p.value)
                        lineTo(
                            change.position.x,
                            change.position.y
                        )
                    }
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
        drawPath(
            p.value,
            color.value,
            style = Stroke(5.dp.toPx()),
        )

    }
}

@Composable
fun ColorBar(modifier: Modifier = Modifier, onClk: (Color) -> Unit) {
    val clrs = listOf(Color.Red, Color.Yellow, Color.Green, Color.Magenta, Color.Blue, Color.Black)

    Row(
        modifier
            .background(Color.Gray)
            .padding(8.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,

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
    }
}

@Preview(showBackground = true)
@Composable
private fun ColorBarPreview() {
    ColorBar(onClk = {})
}
