package com.minespray.app.data

import kotlinx.coroutines.flow.*

/** Repository contract. Add an ApiMineRepository (Retrofit -> FastAPI) later without touching UI. */
interface MineRepository {
    val state: StateFlow<MineState>
    fun tick()
    fun setScenario(s: Scenario)
    fun setAuto(on: Boolean)
    fun manualSpray(zoneId: Int)
    fun stopSpray(zoneId: Int)
}

class MockMineRepository : MineRepository {
    private var n = 0
    private fun z(id: Int, name: String, d: Int, m: Int, t: Int, tr: String, last: Int, x: Float, y: Float) =
        RoadZone(id, name, d, m, t, 30, 10, tr, last, true, x, y)

    private fun baseZones() = listOf(
        z(1, "Pit Exit", 30, 62, 27, "MED", 40, .08f, .75f), z(2, "South Ramp", 25, 66, 27, "LOW", 35, .22f, .85f),
        z(3, "Loading Area", 38, 55, 28, "HIGH", 55, .30f, .62f), z(4, "North Ramp", 35, 58, 28, "MED", 70, .40f, .40f),
        z(5, "Junction A", 28, 64, 27, "MED", 30, .50f, .62f), z(6, "East Haul", 32, 60, 28, "MED", 60, .66f, .72f),
        z(7, "Crusher Road", 33, 57, 29, "HIGH", 85, .80f, .50f), z(8, "Dump Access", 22, 68, 27, "LOW", 25, .88f, .25f),
        z(9, "West Bench", 26, 63, 27, "LOW", 50, .12f, .35f), z(10, "Refill Road", 20, 70, 26, "LOW", 20, .55f, .88f),
        z(11, "Service Road", 36, 56, 28, "MED", 75, .62f, .30f), z(12, "Ridge Road", 24, 65, 27, "LOW", 45, .30f, .15f)
    )
    private fun baseTrucks() = listOf(
        WaterTruck("WT-01", TruckStatus.IDLE, null, 40, 0, 0, true), WaterTruck("WT-02", TruckStatus.REFILLING, null, 18, 0, 0, true),
        WaterTruck("WT-03", TruckStatus.IDLE, null, 64, 0, 0, true), WaterTruck("WT-04", TruckStatus.IDLE, null, 52, 0, 0, true)
    )

    private val _state = MutableStateFlow(MineState(baseZones(), baseTrucks(), Scenario.NORMAL, true, 42650, 12400, listOf(LogEvent(LogKind.STARTED))))
    override val state = _state.asStateFlow()

    private fun log(s: MineState, msg: LogEvent) = s.log.takeLast(5) + msg

    override fun setScenario(s: Scenario) {
        var zones = baseZones(); var trucks = baseTrucks()
        fun edit(id: Int, f: (RoadZone) -> RoadZone) { zones = zones.map { if (it.id == id) f(it) else it } }
        when (s) {
            Scenario.NORMAL -> {}
            Scenario.HIGH_DUST -> { edit(7) { it.copy(dust = 91, moisture = 14, temp = 36, humidity = 22, wind = 16, lastWateringMin = 138) }
                edit(4) { it.copy(dust = 80, moisture = 16, traffic = "HIGH") }; edit(11) { it.copy(dust = 78, moisture = 20) } }
            Scenario.HEAT -> zones = zones.map { it.copy(temp = it.temp + 9, moisture = it.moisture - 22, humidity = 18) }
            Scenario.TRAFFIC -> zones = zones.map { it.copy(traffic = "HIGH", dust = it.dust + 28) }
            Scenario.LOW_WATER -> trucks = trucks.map { it.copy(tank = (12..22).random()) }
            Scenario.SENSOR -> { edit(9) { it.copy(sensorOnline = false) }; edit(3) { it.copy(sensorOnline = false) } }
            Scenario.EMERGENCY -> edit(7) { it.copy(dust = 99, moisture = 5, temp = 38, traffic = "HIGH", lastWateringMin = 300) }
        }
        _state.update { it.copy(zones = zones, trucks = trucks, scenario = s, log = log(it, LogEvent(LogKind.SCENARIO, s.name))) }
    }

    override fun setAuto(on: Boolean) = _state.update { it.copy(auto = on, log = log(it, LogEvent(if (on) LogKind.AUTO_ON else LogKind.AUTO_OFF))) }

    override fun manualSpray(zoneId: Int) = _state.update { s ->
        val t = s.trucks.filter { it.status == TruckStatus.IDLE && it.tank > 15 }.maxByOrNull { it.tank } ?: return@update s
        s.copy(trucks = s.trucks.map { if (it.id == t.id) it.copy(status = TruckStatus.ENROUTE, zoneId = zoneId, auto = false) else it },
            log = log(s, LogEvent(LogKind.MANUAL, t.id, zoneId.toString())))
    }

    override fun stopSpray(zoneId: Int) = _state.update { s ->
        s.copy(trucks = s.trucks.map { if (it.zoneId == zoneId) it.copy(status = TruckStatus.IDLE, zoneId = null, speed = 0, flow = 0) else it })
    }

    override fun tick() {
        n++
        _state.update { s ->
            var msgs = emptyList<LogEvent>(); var used = s.waterUsed; var saved = s.saved
            val heat = s.scenario == Scenario.HEAT; val traffic = s.scenario == Scenario.TRAFFIC
            val sprayed = s.trucks.filter { it.status == TruckStatus.SPRAYING }.mapNotNull { it.zoneId }
            val zones = s.zones.map { z ->
                if (z.id in sprayed) z.copy(moisture = (z.moisture + 8).coerceAtMost(100), dust = (z.dust - 9).coerceAtLeast(5), lastWateringMin = 0)
                else if (n % 2 == 0) z.copy(moisture = (z.moisture - if (heat) 3 else 1).coerceAtLeast(5),
                    dust = (z.dust + if (traffic) 2 else 1).coerceAtMost(100), lastWateringMin = z.lastWateringMin + 1)
                else z
            }
            var trucks = s.trucks.map { t ->
                val zone = zones.find { it.id == t.zoneId }
                when (t.status) {
                    TruckStatus.ENROUTE -> { msgs = msgs + LogEvent(LogKind.SPRAYING, t.id, t.zoneId.toString()); t.copy(status = TruckStatus.SPRAYING, speed = 18, flow = 850) }
                    TruckStatus.SPRAYING -> {
                        used += 1500; saved += 400
                        if ((zone?.moisture ?: 100) >= 60 || t.tank <= 10) {
                            msgs = msgs + LogEvent(LogKind.COMPLETED, t.zoneId.toString())
                            t.copy(status = TruckStatus.IDLE, zoneId = null, speed = 0, flow = 0, tank = t.tank - 3)
                        } else t.copy(tank = t.tank - 3)
                    }
                    TruckStatus.REFILLING -> if (t.tank >= 100) t.copy(status = TruckStatus.IDLE, tank = 100) else t.copy(tank = (t.tank + 10).coerceAtMost(100))
                    TruckStatus.IDLE -> if (t.tank < 20) t.copy(status = TruckStatus.REFILLING) else t
                }
            }
            if (s.auto) {
                val busy = trucks.mapNotNull { it.zoneId }
                val target = zones.filter { it.level == Level.CRITICAL && it.id !in busy }.maxByOrNull { it.risk }
                val truck = trucks.filter { it.status == TruckStatus.IDLE && it.tank >= 20 }.maxByOrNull { it.tank }
                if (target != null && truck != null) {
                    trucks = trucks.map { if (it.id == truck.id) it.copy(status = TruckStatus.ENROUTE, zoneId = target.id, auto = true) else it }
                    msgs = msgs + LogEvent(LogKind.ASSIGN, truck.id, target.id.toString(), target.risk.toString())
                }
            }
            s.copy(zones = zones, trucks = trucks, waterUsed = used, saved = saved, log = (s.log + msgs).takeLast(6))
        }
    }
}
