package com.example.chess.engine

import com.example.chess.model.*

class GameSession(
    private val statusCallback: (String) -> Unit
) {
    val board = Board()
    private val moveValidator = MoveValidator()

    var currentTurn: PieceColor = PieceColor.WHITE
        private set

    private val moveHistory = mutableListOf<Move>()

    private var selectedPos: Position? = null
    var legalMovesForSelected: List<Position> = emptyList()
        private set

    var gameState: GameState = GameState.ONGOING
        private set

    init {
        updateStatus()
    }

    fun resetGame() {
        board.initializePieces()
        moveHistory.clear()
        currentTurn = PieceColor.WHITE
        selectedPos = null
        legalMovesForSelected = emptyList()
        gameState = GameState.ONGOING
        updateStatus()
    }

    fun undoLastMove() {
        if (moveHistory.isEmpty() || gameState == GameState.CHECKMATE) return
        val last = moveHistory.removeLast()
        board.undoMove(last)
        currentTurn = currentTurn.opposite()
        selectedPos = null
        legalMovesForSelected = emptyList()
        gameState = GameState.ONGOING
        updateStatus()
    }

    fun onSquareTapped(row: Int, col: Int) {
        if (gameState == GameState.CHECKMATE || gameState == GameState.STALEMATE) return

        val pos = Position(row, col)
        val piece = board.getPiece(pos)

        // 1. Select piece
        val currentSelected = selectedPos
        if (currentSelected == null) {
            if (piece != null && piece.color == currentTurn) {
                selectedPos = pos
                legalMovesForSelected =
                    moveValidator.getLegalMoves(board, pos, currentTurn)
                return
            } else {
                // tap on empty or enemy piece with nothing selected – ignore
                return
            }
        }

        // 2. Change selection to another piece of same color
        val selectedPiece = board.getPiece(currentSelected)
        if (piece != null && piece.color == currentTurn && pos != currentSelected) {
            selectedPos = pos
            legalMovesForSelected =
                moveValidator.getLegalMoves(board, pos, currentTurn)
            return
        }

        // 3. Try to move
        if (legalMovesForSelected.contains(pos)) {
            val move = board.movePiece(currentSelected, pos)
            moveHistory.add(move)
            selectedPos = null
            legalMovesForSelected = emptyList()
            currentTurn = currentTurn.opposite()
            evaluateGameState()
        } else {
            // illegal target -> just clear selection
            selectedPos = null
            legalMovesForSelected = emptyList()
        }
    }

    fun getSelectedPosition(): Position? = selectedPos

    private fun evaluateGameState() {
        val kingInCheck = moveValidator.isKingInCheck(board, currentTurn)
        val anyMoves = moveValidator.hasAnyLegalMove(board, currentTurn)

        gameState = when {
            kingInCheck && !anyMoves -> GameState.CHECKMATE
            !kingInCheck && !anyMoves -> GameState.STALEMATE
            kingInCheck -> GameState.CHECK
            else -> GameState.ONGOING
        }

        updateStatus()
    }

    private fun updateStatus() {
        val status = when (gameState) {
            GameState.ONGOING -> "${currentTurn.name.lowercase().replaceFirstChar { it.uppercase() }} to move"
            GameState.CHECK -> "CHECK! ${currentTurn.name.lowercase().replaceFirstChar { it.uppercase() }} to move"
            GameState.CHECKMATE -> "CHECKMATE! ${currentTurn.opposite().name.lowercase().replaceFirstChar { it.uppercase() }} wins"
            GameState.STALEMATE -> "Draw by stalemate"
        }
        statusCallback(status)
    }
}
