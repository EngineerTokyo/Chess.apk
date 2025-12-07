package com.example.chess.model

data class Move(
    val from: Position,
    val to: Position,
    val movedPiece: Piece,
    val capturedPiece: Piece? = null,
    val isPawnPromotion: Boolean = false
)
