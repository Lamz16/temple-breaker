package com.example.game.entity

data class Paddle(
    var centerX: Float = 0f,
    var y: Float = 0f,
    var baseWidth: Float = 140f,
    var expandedWidth: Float = 220f,
    var currentWidth: Float = 140f,
    var targetWidth: Float = 140f,
    var height: Float = 22f
) {
    val left: Float
        get() = centerX - currentWidth / 2f

    val right: Float
        get() = centerX + currentWidth / 2f

    val top: Float
        get() = y

    val bottom: Float
        get() = y + height

    fun updateWidth(dt: Float) {
        val speed = 350f // pixels per second transition
        if (currentWidth < targetWidth) {
            currentWidth = (currentWidth + speed * dt).coerceAtMost(targetWidth)
        } else if (currentWidth > targetWidth) {
            currentWidth = (currentWidth - speed * dt).coerceAtLeast(targetWidth)
        }
    }

    fun clampPosition(minX: Float, maxX: Float) {
        val halfW = currentWidth / 2f
        centerX = centerX.coerceIn(minX + halfW, maxX - halfW)
    }
}
