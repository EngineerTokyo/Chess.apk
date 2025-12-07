package com.example.chess.model

data class Position(val row: Int, val col: Int) {
    fun isOnBoard(): Boolean = row in 0..7 && col in 0..7
}
