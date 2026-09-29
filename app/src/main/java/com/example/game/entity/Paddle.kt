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

    /**
     * Resets the paddle to default geometry and initial horizontal center.
     */
    fun reset(initialCenterX: Float, initialY: Float, width: Float, height: Float, expanded: Float) {
        centerX = initialCenterX
        y = initialY
        baseWidth = width
        currentWidth = width
        targetWidth = width
        expandedWidth = expanded
        this.height = height
    }

    /**
     * Updates paddle horizontal position smoothly according to touch input,
     * maintaining bounds inside [minX, maxX].
     */
    fun moveTo(targetX: Float, minX: Float, maxX: Float) {
        centerX = targetX
        clampPosition(minX, maxX)
    }

    /**
     * Animates paddle width toward target width (e.g. when power-up is activated or expires).
     */
    fun updateWidth(dt: Float) {
        val transitionSpeed = 350f // pixels per second
        if (currentWidth < targetWidth) {
            currentWidth = (currentWidth + transitionSpeed * dt).coerceAtMost(targetWidth)
        } else if (currentWidth > targetWidth) {
            currentWidth = (currentWidth - transitionSpeed * dt).coerceAtLeast(targetWidth)
        }
    }

    /**
     * Keeps paddle within the visible arena horizontal boundary.
     */
    fun clampPosition(minX: Float, maxX: Float) {
        val halfW = currentWidth / 2f
        centerX = centerX.coerceIn(minX + halfW, maxX - halfW)
    }
}
