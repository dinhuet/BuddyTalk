package com.example.buddytalk.data.model

data class LevelInfo(
    val level: Int,
    val badge: String,
    val xpRequired: Int
)

object LevelSystem {
    val LEVELS = listOf(
        LevelInfo(1, "Bronze", 0),
        LevelInfo(2, "Silver", 1000),
        LevelInfo(3, "Gold", 3000),
        LevelInfo(4, "Platinum", 7000)
    )

    fun getLevelByXP(totalXP: Int): LevelInfo {
        var current = LEVELS[0]
        for (level in LEVELS) {
            if (totalXP >= level.xpRequired) {
                current = level
            }
        }
        return current
    }

    data class XPBarData(
        val currentLevel: Int,
        val currentBadge: String,
        val xpInCurrentLevel: Int,
        val xpNeededForNextLevel: Int,
        val progressPercent: Float
    )

    fun calculateXPBar(totalXP: Int): XPBarData {
        val currentLevel = getLevelByXP(totalXP)
        val nextLevel = LEVELS.find { it.level == currentLevel.level + 1 }

        if (nextLevel == null) {
            return XPBarData(
                currentLevel = currentLevel.level,
                currentBadge = currentLevel.badge,
                xpInCurrentLevel = totalXP,
                xpNeededForNextLevel = 0,
                progressPercent = 1f
            )
        }

        val xpStart = currentLevel.xpRequired
        val xpEnd = nextLevel.xpRequired

        val xpInCurrentLevel = totalXP - xpStart
        val xpNeededForNextLevel = xpEnd - xpStart

        val progressPercent = if (xpNeededForNextLevel > 0) {
            xpInCurrentLevel.toFloat() / xpNeededForNextLevel.toFloat()
        } else {
            1f
        }

        return XPBarData(
            currentLevel = currentLevel.level,
            currentBadge = currentLevel.badge,
            xpInCurrentLevel = xpInCurrentLevel,
            xpNeededForNextLevel = xpNeededForNextLevel,
            progressPercent = progressPercent.coerceIn(0f, 1f)
        )
    }
}
