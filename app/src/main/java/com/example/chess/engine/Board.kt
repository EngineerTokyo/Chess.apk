package com.example.chess.engine

import com.example.chess.model.*

class Board {

    val grid: Array<Array<Square>> =
        Array(8) { row -> Array(8) { col -> Square(row, col, null) } }

    init {
        initializePieces()
    }

    fun initializePieces() {
        // Clear
        for (row in 0..7) {
            for (col in 0..7) {
                grid[row][col].piece = null
            }
        }

        // Pawns
        for (col in 0..7) {
            grid[6][col].piece = Piece(PieceType.PAWN, PieceColor.WHITE)
            grid[1][col].piece = Piece(PieceType.PAWN, PieceColor.BLACK)
        }

        // Back ranks
        setupBackRank(7, PieceColor.WHITE)
        setupBackRank(0, PieceColor.BLACK)
    }

    private fun setupBackRank(row: Int, color: PieceColor) {
        val order = listOf(
            PieceType.ROOK,
            PieceType.KNIGHT,
            PieceType.BISHOP,
            PieceType.QUEEN,
            PieceType.KING,
            PieceType.BISHOP,
            PieceType.KNIGHT,
            PieceType.ROOK
        )

        for (col in 0..7) {
            grid[row][col].piece = Piece(order[col], color)
        }
    }

    fun getSquare(pos: Position): Square = grid[pos.row][pos.col]

    fun getPiece(pos: Position): Piece? = getSquare(pos).piece

    fun setPiece(pos: Position, piece: Piece?) {
        getSquare(pos).piece = piece
    }

    fun movePiece(from: Position, to: Position): Move {
        val fromPiece = getPiece(from)
            ?: throw IllegalStateException("No piece at $from")
        val captured = getPiece(to)
        setPiece(to, fromPiece)
        setPiece(from, null)

        // Pawn promotion (auto promote to queen)
        var promotion = false
        if (fromPiece.type == PieceType.PAWN) {
            if ((fromPiece.color == PieceColor.WHITE && to.row == 0) ||
                (fromPiece.color == PieceColor.BLACK && to.row == 7)
            ) {
                setPiece(to, Piece(PieceType.QUEEN, fromPiece.color))
                promotion = true
            }
        }

        return Move(from, to, fromPiece, captured, promotion)
    }

    fun undoMove(move: Move) {
        setPiece(move.from, move.movedPiece)
        setPiece(move.to, move.capturedPiece)
    }

    fun copy(): Board {
        val newBoard = Board()
        for (row in 0..7) {
            for (col in 0..7) {
                newBoard.grid[row][col].piece = this.grid[row][col].piece
            }
        }
        return newBoard
    }
}
