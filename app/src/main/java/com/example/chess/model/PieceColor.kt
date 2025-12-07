package com.example.chess.model

enum class PieceColor {
    WHITE, BLACK;

    fun opposite() = if (this == WHITE) BLACK else WHITE
}
