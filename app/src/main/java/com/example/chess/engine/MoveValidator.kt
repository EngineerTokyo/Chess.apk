package com.example.chess.engine

import com.example.chess.model.*

class MoveValidator {

    fun getLegalMoves(board: Board, from: Position, color: PieceColor): List<Position> {
        val piece = board.getPiece(from) ?: return emptyList()
        if (piece.color != color) return emptyList()

        val pseudo = getPseudoLegalMoves(board, from, piece)
        return pseudo.filter { to ->
            !wouldKingBeInCheckAfterMove(board, from, to, color)
        }
    }

    fun hasAnyLegalMove(board: Board, color: PieceColor): Boolean {
        for (row in 0..7) {
            for (col in 0..7) {
                val pos = Position(row, col)
                val p = board.getPiece(pos) ?: continue
                if (p.color != color) continue
                if (getLegalMoves(board, pos, color).isNotEmpty()) return true
            }
        }
        return false
    }

    fun isKingInCheck(board: Board, color: PieceColor): Boolean {
        // Find king position
        var kingPos: Position? = null
        for (row in 0..7) {
            for (col in 0..7) {
                val p = board.getPiece(Position(row, col))
                if (p != null && p.type == PieceType.KING && p.color == color) {
                    kingPos = Position(row, col)
                    break
                }
            }
        }
        kingPos ?: return false

        // Is king square attacked by any enemy piece?
        val enemy = color.opposite()
        for (row in 0..7) {
            for (col in 0..7) {
                val from = Position(row, col)
                val p = board.getPiece(from) ?: continue
                if (p.color != enemy) continue
                val moves = getPseudoLegalMoves(board, from, p, forAttack = true)
                if (moves.contains(kingPos)) return true
            }
        }
        return false
    }

    private fun wouldKingBeInCheckAfterMove(
        board: Board,
        from: Position,
        to: Position,
        color: PieceColor
    ): Boolean {
        val copy = board.copy()
        copy.movePiece(from, to)
        return isKingInCheck(copy, color)
    }

    private fun getPseudoLegalMoves(
        board: Board,
        from: Position,
        piece: Piece,
        forAttack: Boolean = false   // when true, pawn attack squares ignore forward moves
    ): List<Position> {
        return when (piece.type) {
            PieceType.PAWN -> pawnMoves(board, from, piece.color, forAttack)
            PieceType.ROOK -> rookMoves(board, from, piece.color)
            PieceType.BISHOP -> bishopMoves(board, from, piece.color)
            PieceType.QUEEN -> queenMoves(board, from, piece.color)
            PieceType.KNIGHT -> knightMoves(board, from, piece.color)
            PieceType.KING -> kingMoves(board, from, piece.color)
        }
    }

    private fun pawnMoves(
        board: Board,
        from: Position,
        color: PieceColor,
        forAttack: Boolean
    ): List<Position> {
        val moves = mutableListOf<Position>()
        val dir = if (color == PieceColor.WHITE) -1 else 1
        val startRow = if (color == PieceColor.WHITE) 6 else 1

        val oneForward = Position(from.row + dir, from.col)
        if (!forAttack && oneForward.isOnBoard() && board.getPiece(oneForward) == null) {
            moves += oneForward

            val twoForward = Position(from.row + 2 * dir, from.col)
            if (from.row == startRow && board.getPiece(twoForward) == null) {
                moves += twoForward
            }
        }

        // captures
        val captures = listOf(
            Position(from.row + dir, from.col - 1),
            Position(from.row + dir, from.col + 1)
        )

        for (c in captures) {
            if (!c.isOnBoard()) continue
            val targetPiece = board.getPiece(c)
            if (forAttack) {
                // For attack map, we consider the square as attacked even if empty
                moves += c
            } else if (targetPiece != null && targetPiece.color != color) {
                moves += c
            }
        }

        return moves
    }

    private fun knightMoves(board: Board, from: Position, color: PieceColor): List<Position> {
        val moves = mutableListOf<Position>()
        val deltas = listOf(
            2 to 1, 2 to -1, -2 to 1, -2 to -1,
            1 to 2, 1 to -2, -1 to 2, -1 to -2
        )
        for ((dr, dc) in deltas) {
            val pos = Position(from.row + dr, from.col + dc)
            if (!pos.isOnBoard()) continue
            val p = board.getPiece(pos)
            if (p == null || p.color != color) moves += pos
        }
        return moves
    }

    private fun kingMoves(board: Board, from: Position, color: PieceColor): List<Position> {
        val moves = mutableListOf<Position>()
        for (dr in -1..1) {
            for (dc in -1..1) {
                if (dr == 0 && dc == 0) continue
                val pos = Position(from.row + dr, from.col + dc)
                if (!pos.isOnBoard()) continue
                val p = board.getPiece(pos)
                if (p == null || p.color != color) moves += pos
            }
        }
        // Castling could be added here later
        return moves
    }

    private fun rookMoves(board: Board, from: Position, color: PieceColor): List<Position> {
        val moves = mutableListOf<Position>()
        val directions = listOf(
            1 to 0, -1 to 0, 0 to 1, 0 to -1
        )
        slideMoves(board, from, color, directions, moves)
        return moves
    }

    private fun bishopMoves(board: Board, from: Position, color: PieceColor): List<Position> {
        val moves = mutableListOf<Position>()
        val directions = listOf(
            1 to 1, 1 to -1, -1 to 1, -1 to -1
        )
        slideMoves(board, from, color, directions, moves)
        return moves
    }

    private fun queenMoves(board: Board, from: Position, color: PieceColor): List<Position> {
        val moves = mutableListOf<Position>()
        val directions = listOf(
            1 to 0, -1 to 0, 0 to 1, 0 to -1,
            1 to 1, 1 to -1, -1 to 1, -1 to -1
        )
        slideMoves(board, from, color, directions, moves)
        return moves
    }

    private fun slideMoves(
        board: Board,
        from: Position,
        color: PieceColor,
        directions: List<Pair<Int, Int>>,
        out: MutableList<Position>
    ) {
        for ((dr, dc) in directions) {
            var r = from.row + dr
            var c = from.col + dc
            while (r in 0..7 && c in 0..7) {
                val pos = Position(r, c)
                val p = board.getPiece(pos)
                if (p == null) {
                    out += pos
                } else {
                    if (p.color != color) out += pos
                    break
                }
                r += dr
                c += dc
            }
        }
    }
}
