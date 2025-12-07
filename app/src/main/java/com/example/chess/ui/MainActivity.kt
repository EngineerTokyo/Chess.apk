package com.example.chess.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.chess.R
import com.example.chess.engine.GameSession

class MainActivity : AppCompatActivity() {

    private lateinit var gameSession: GameSession
    private lateinit var chessBoardView: ChessBoardView
    private lateinit var tvStatus: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        tvStatus = findViewById(R.id.tvStatus)
        chessBoardView = findViewById(R.id.chessBoard)

        gameSession = GameSession { statusText ->
            tvStatus.text = statusText
        }

        chessBoardView.gameSession = gameSession

        findViewById<Button>(R.id.btnUndo).setOnClickListener {
            gameSession.undoLastMove()
            chessBoardView.invalidate()
        }

        findViewById<Button>(R.id.btnReset).setOnClickListener {
            gameSession.resetGame()
            chessBoardView.invalidate()
        }
    }
}
