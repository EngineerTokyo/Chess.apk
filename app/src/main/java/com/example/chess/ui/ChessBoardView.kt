package com.example.chess.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import com.example.chess.R
import com.example.chess.engine.GameSession
import com.example.chess.model.Piece
import com.example.chess.model.PieceColor
import com.example.chess.model.PieceType
import com.example.chess.model.Position

class ChessBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    var gameSession: GameSession? = null

    private val lightPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.chess_light)
    }
    private val darkPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.chess_dark)
    }
    private val selectedPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.chess_selected)
    }
    private val moveHintPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.chess_move_hint)
    }
    private val piecePaint = Paint().apply {
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val board = gameSession?.board ?: return

        val size = minOf(width, height)
        val cellSize = size / 8f
        piecePaint.textSize = cellSize * 0.7f

        val offsetX = (width - size) / 2f
        val offsetY = (height - size) / 2f

        val selected = gameSession?.getSelectedPosition()
        val legalMoves = gameSession?.legalMovesForSelected ?: emptyList()

        for (row in 0..7) {
            for (col in 0..7) {
                val left = offsetX + col * cellSize
                val top = offsetY + row * cellSize
                val right = left + cellSize
                val bottom = top + cellSize

                val basePaint = if ((row + col) % 2 == 0) lightPaint else darkPaint
                canvas.drawRect(left, top, right, bottom, basePaint)

                // selection
                if (selected?.row == row && selected.col == col) {
                    canvas.drawRect(left, top, right, bottom, selectedPaint)
                }

                // legal move hints
                if (legalMoves.any { it.row == row && it.col == col }) {
                    canvas.drawRect(left, top, right, bottom, moveHintPaint)
                }

                val piece = board.grid[row][col].piece
                if (piece != null) {
                    drawPiece(canvas, piece, left, top, cellSize)
                }
            }
        }
    }

    private fun drawPiece(
        canvas: Canvas,
        piece: Piece,
        left: Float,
        top: Float,
        cellSize: Float
    ) {
        val centerX = left + cellSize / 2f
        val centerY = top + cellSize * 0.65f

        piecePaint.color = if (piece.color == PieceColor.WHITE)
            ContextCompat.getColor(context, android.R.color.white)
        else
            ContextCompat.getColor(context, android.R.color.black)

        val symbol = when (piece.type) {
            PieceType.KING -> if (piece.color == PieceColor.WHITE) "♔" else "♚"
            PieceType.QUEEN -> if (piece.color == PieceColor.WHITE) "♕" else "♛"
            PieceType.ROOK -> if (piece.color == PieceColor.WHITE) "♖" else "♜"
            PieceType.BISHOP -> if (piece.color == PieceColor.WHITE) "♗" else "♝"
            PieceType.KNIGHT -> if (piece.color == PieceColor.WHITE) "♘" else "♞"
            PieceType.PAWN -> if (piece.color == PieceColor.WHITE) "♙" else "♟"
        }
        canvas.drawText(symbol, centerX, centerY, piecePaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val session = gameSession ?: return false

        if (event.action == MotionEvent.ACTION_UP) {
            val size = minOf(width, height)
            val cellSize = size / 8f
            val offsetX = (width - size) / 2f
            val offsetY = (height - size) / 2f

            val x = event.x
            val y = event.y

            if (x < offsetX || x > offsetX + size ||
                y < offsetY || y > offsetY + size
            ) {
                return true
            }

            val col = ((x - offsetX) / cellSize).toInt()
            val row = ((y - offsetY) / cellSize).toInt()

            session.onSquareTapped(row, col)
            invalidate()
        }
        return true
    }
}
