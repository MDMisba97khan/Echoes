package com.example.echoes

import android.content.Context
import com.example.echoes.AudioManager
import com.example.echoes.model.Orb
import com.example.echoes.model.Shape

class GameEngine(
    audio: AudioManager,
    context: Context,
) {
    var orb = Orb()
        private set
    private val shapes = mutableListOf<Shape>()

    private var screenW = 1080f
    private var screenH = 1920f
    private var running = false
    private var muted = false

    var score = 0
        private set
    var timeRemaining = 60f
        private set
    var lost = false
        private set
    var won = false
        private set
    var darkenAlpha = 0f
        private set

    var onScore: ((Int) -> Unit)? = null
    var onLose: (() -> Unit)? = null
    var onWin: (() -> Unit)? = null
    var onTick: ((Float) -> Unit)? = null

    private var lastDriftLeft = true
    private var echoDelay = 0.4f
    private var lastTapTime = 0L

    fun init(w: Float, h: Float) {
        screenW = w; screenH = h
        orb.reset(screenW, screenH)
        shapes.clear()
        score = 0; timeRemaining = 60f
        lost = false; won = false
        darkenAlpha = 0f; lastDriftLeft = true
    }

    fun start() { running = true; audio.playAmbient() }
    fun stop() { running = false; audio.stopAmbient() }
    fun toggleMute() { muted = !muted; audio.toggleMute() }

    /** Call on tap. Returns true if a new echo tone should be scheduled. */
    fun onTap(tapX: Float): Boolean {
        if (lost || won) return false
        val now = System.nanoTime()
        val driftLeft = tapX < screenW / 2f
        lastDriftLeft = driftLeft

        orb.vx = if (driftLeft) -80f else 80f
        orb.vy = 0f
        orb.hue = if (driftLeft) 220f else 160f

        // Schedule echo tone after echoDelay
        val shouldEcho = true
        lastTapTime = now
        shapes.add(Shape.spawnOpposite(screenW, screenH, driftLeft, echoDelay))
        return shouldEcho
    }

    fun update(dt: Float): Boolean {
        if (!running || lost || won) return false

        timeRemaining -= dt
        onTick?.invoke(timeRemaining.coerceAtLeast(0f))

        if (timeRemaining <= 0f) { checkWin(); return true }

        // Orb
        orb.x += orb.vx * dt
        orb.y += orb.vy * dt
        orb.x = orb.x.coerceIn(orb.radius, screenW - orb.radius)
        orb.y = orb.y.coerceIn(orb.radius, screenH - orb.radius)
        orb.vx *= 0.98f; orb.vy *= 0.98f
        orb.radius = 28f + (Math.sin(System.currentTimeMillis() * 0.003).toFloat() * 4f)
        orb.glowRadius = orb.radius * 2f

        // Shapes
        val iter = shapes.iterator()
        while (iter.hasNext()) {
            val s = iter.next()
            s.grow(dt)
            s.drift(dt)
            if (s.alive && s.collidesWith(orb.x, orb.y, orb.radius)) {
                onHit(s); return true
            }
            if (s.offScreen(screenW, screenH)) { s.alive = false; iter.remove() }
        }

        if (won) return true
        if (lost) { darkenAlpha = (darkenAlpha + dt * 2f).coerceAtMost(0.7f) }
        return false
    }

    fun onSurvivedTap() {
        if (lost || won) return
        score++
        onScore?.invoke(score)
        if (score >= 10) checkWin()
    }

    private fun onHit(s: Shape) {
        lost = true
        audio.stopAmbient()
        audio.playChime()
        audio.vibrate(null, 100)
        darkenAlpha = 0.3f
        onLose?.invoke()
    }

    private fun checkWin() {
        if (score >= 10 && !won) {
            won = true
            audio.playBurst()
            audio.vibrate(null, 200)
            onWin?.invoke()
        }
    }

    fun getShapes() = shapes.toList()
}