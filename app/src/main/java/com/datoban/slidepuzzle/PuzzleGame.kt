package com.datoban.slidepuzzle
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

@Preview(showBackground = true, showSystemUi= true)

@Composable
fun PreviewPuzzle(modifier: Modifier = Modifier){
    SlidingPuzzle()

}

@Composable
fun SlidingPuzzle(modifier: Modifier = Modifier) {
    var grid by remember {
        mutableStateOf(generateGrid())
    }
    var emptyPosition by remember {
        mutableStateOf(findEmptyPosition(grid))
    }
    var moves by remember {
        mutableStateOf(0)
    }
    Box(modifier = Modifier.fillMaxSize()) {
        androidx.compose.material3.Text(
            text = "Movimientos: $moves",
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
        )
        Canvas(
            modifier = Modifier
                .size(300.dp)
                .align(Alignment.Center)
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragEnd = {},
                        onDragCancel = {},
                        onDrag = { change, dragAmount ->

                            val direction = getDragDirection(dragAmount)

                            if (direction != null) {
                                val touchedBox = findTouchedBox(
                                    change.position,
                                    grid.size,
                                    size.height / 3f
                                )

                                if (touchedBox != null) {
                                    val (newGrid, newEmptyPosition) =
                                        grid.tryMove(direction, emptyPosition, touchedBox)

                                    if (newGrid != grid) {
                                        grid = newGrid
                                        emptyPosition = newEmptyPosition
                                        moves++
                                    }
                                }
                            }
                        }
                    )
                }
        ) {
            drawGrid(grid)
        }
    }
}
fun DrawScope.drawGrid(grid: List<List<Int>>){
    val cellSize=size.width/3f

    grid.forEachIndexed { y, row ->
        row.forEachIndexed { x, number ->
            if(number!=0){
                drawBoxwithNumber(
                    number,
                    x,
                    y,
                    cellSize,
                    5.dp
                )
            }
        }
    }
}

fun DrawScope.drawBoxwithNumber(number:Int,x: Int,y:Int,cellSize: Float,padding: Dp) {
    val boxSize = cellSize - padding.toPx()
    val left = x * cellSize + padding.toPx()
    val top = y * cellSize + padding.toPx()

    drawRoundRect(
        color = Color.Green.copy( 0.5f), 
        topLeft = Offset(left,top), 
        size= Size(boxSize,boxSize), 
        cornerRadius = CornerRadius(16.dp.toPx(),16.dp.toPx())
    )

    drawContext.canvas.nativeCanvas.drawText(
        number.toString(),
        left+boxSize/2,
        top+boxSize/1.5f,
        Paint().asFrameworkPaint().apply {
            textSize = 40.sp.toPx()
            textAlign = android.graphics.Paint.Align.CENTER
            color = android.graphics.Color.BLACK
        }
    )
}
    

fun List<List<Int>>.tryMove(direction: com.datoban.slidepuzzle.Direction,emptyPosition:Pair<Int, Int>, touchBox:Pair<Int,Int>):Pair<List<List<Int>>,Pair<Int,Int>>{
    val (emptyX, emptyY)=emptyPosition
    val (touchedX, touchedY)=touchBox

    val newGrid=this.map{
        it.toMutableList()
    }
    return when(direction){
        com.datoban.slidepuzzle.Direction.Up->
            if (touchedX == emptyX && touchedY == emptyY + 1) {
            newGrid[emptyY][emptyX] = newGrid[touchedY][touchedX]
            newGrid[touchedY][touchedX] = 0
            newGrid to (touchedX to touchedY)
        } else this to emptyPosition

        Direction.Down ->
            if (touchedX == emptyX && touchedY == emptyY - 1) {
                newGrid[emptyY][emptyX] = newGrid[touchedY][touchedX]
                newGrid[touchedY][touchedX] = 0
                newGrid to (touchedX to touchedY)
            } else this to emptyPosition

        Direction.Left ->
            if (touchedY == emptyY && touchedX == emptyX + 1) {
                newGrid[emptyY][emptyX] = newGrid[touchedY][touchedX]
                newGrid[touchedY][touchedX] = 0
                newGrid to (touchedX to touchedY)
            } else this to emptyPosition

        Direction.Right ->
            if (touchedY == emptyY && touchedX == emptyX - 1) {
                newGrid[emptyY][emptyX] = newGrid[touchedY][touchedX]
                newGrid[touchedY][touchedX] = 0
                newGrid to (touchedX to touchedY)
            } else this to emptyPosition

    }
}

fun findTouchedBox(position: Offset, gridSize:Int, cellSize: Float):Pair<Int,Int>?{
    val x = (position.x/cellSize).toInt()
    val y = (position.y/cellSize).toInt()
    return if(x in 0  until gridSize && y in 0 until gridSize) x to y else null
}
//Drag
enum class Direction{
    Up,Down,Right,Left
}

fun getDragDirection(dragAmount: Offset):com.datoban.slidepuzzle.Direction?{
    return when {
        abs(dragAmount.x) > abs(dragAmount.y) && dragAmount.x > 0 -> Direction.Right
        abs(dragAmount.x) > abs(dragAmount.y) && dragAmount.x < 0 -> Direction.Left
        abs(dragAmount.y) > abs(dragAmount.x) && dragAmount.y > 0 -> Direction.Down
        abs(dragAmount.y) > abs(dragAmount.x) && dragAmount.y < 0 -> Direction.Up
        else -> null
    }
}


fun generateGrid(): List<List<Int>>{
    return (0 .. 8).shuffled().chunked(3)
}

fun findEmptyPosition(grid:List<List<Int>>): Pair<Int, Int> {
    grid.forEachIndexed { y, row ->
        row.forEachIndexed { x, number ->
            if (number==0){
                return x to y
            }
        }
    }
    throw IllegalArgumentException("No empty space found un the grid")
}