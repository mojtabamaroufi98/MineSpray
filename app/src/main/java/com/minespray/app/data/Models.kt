package com.minespray.app.data

enum class Level { NORMAL, WARNING, CRITICAL }
enum class TruckStatus { IDLE, ENROUTE, SPRAYING, REFILLING }
enum class Scenario { NORMAL, HIGH_DUST, HEAT, TRAFFIC, LOW_WATER, SENSOR, EMERGENCY }

data class RoadZone(
    val id: Int, val name: String, val dust: Int, val moisture: Int, val temp: Int,
    val humidity: Int, val wind: Int, val traffic: String, val lastWateringMin: Int,
    val sensorOnline: Boolean, val x: Float, val y: Float
) {
    val risk: Int get() {
        val t = when (traffic) { "HIGH" -> 15; "MED" -> 8; else -> 2 }
        return (dust * 0.45 + (100 - moisture) * 0.4 + t + (temp - 25).coerceAtLeast(0) * 0.2).toInt().coerceIn(0, 100)
    }
    val level: Level get() = if (risk >= 75) Level.CRITICAL else if (risk >= 50) Level.WARNING else Level.NORMAL
    val intensity: Int get() = (risk - 7).coerceIn(30, 100)
    val estLiters: Int get() = intensity * 49
}

data class WaterTruck(
    val id: String, val status: TruckStatus, val zoneId: Int?, val tank: Int,
    val speed: Int, val flow: Int, val auto: Boolean
) { val liters: Int get() = tank * 500 }

data class MineState(
    val zones: List<RoadZone>, val trucks: List<WaterTruck>, val scenario: Scenario,
    val auto: Boolean, val waterUsed: Int, val saved: Int, val log: List<LogEvent>
)

enum class LogKind { STARTED, SCENARIO, AUTO_ON, AUTO_OFF, MANUAL, SPRAYING, COMPLETED, ASSIGN }
data class LogEvent(val kind: LogKind, val a: String = "", val b: String = "", val c: String = "")

data class AppSettings(
    val lang: String = "en", val notifCritical: Boolean = true, val notifWarning: Boolean = true,
    val notifTruck: Boolean = true, val notifSensor: Boolean = true, val dispatch: Boolean = true,
    val conserve: Boolean = false, val overwater: Boolean = true, val refreshSec: Int = 2, val demo: Boolean = true
)
