package com.datoban.slidepuzzle

import androidx.lifecycle.ViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class PuzzleViewModel : ViewModel() {

    var grid by mutableStateOf(generateGrid())
        private set

    var emptyPosition by mutableStateOf(findEmptyPosition(grid))
        private set

    var moves by mutableStateOf(0)
        private set

    fun move(newGrid: List<List<Int>>, newEmptyPosition: Pair<Int, Int>) {
        grid = newGrid
        emptyPosition = newEmptyPosition
        moves++
    }

    fun resetGame() {
        grid = generateGrid()
        emptyPosition = findEmptyPosition(grid)
        moves = 0
    }
}