package com.minespray.app.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.minespray.app.R
import com.minespray.app.data.*
import com.minespray.app.ui.*
import com.minespray.app.ui.components.*
import com.minespray.app.ui.theme.*
import com.minespray.app.viewmodel.MineViewModel
import kotlinx.coroutines.delay

@Composable
fun ScreenList(content: androidx.compose.foundation.lazy.LazyListScope.() -> Unit) =
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 14.dp), verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 12.dp), content = content)

@Composable
fun SplashScreen(onDone: () -> Unit) {
    LaunchedEffect(Unit) { delay(2000); onDone() }
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(84.dp).background(Amber, RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
            Text("MS", color = Color.Black, fontSize = 32.sp, fontWeight = FontWeight.Black)
        }
        Spacer(Modifier.height(16.dp))
        Text(stringResource(R.string.app_name), fontSize = 32.sp, fontWeight = FontWeight.Bold)
        Text(stringResource(R.string.tagline), color = TextLo)
        Spacer(Modifier.height(28.dp))
        LinearProgressIndicator(Modifier.width(160.dp), color = Amber, trackColor = Border)
    }
}

@Composable
fun DashboardScreen(s: MineState, onZone: (Int) -> Unit, onControl: () -> Unit, onSettings: () -> Unit) {
    val avgM = s.zones.map { it.moisture }.average().toInt(); val avgD = s.zones.map { it.dust }.average().toInt()
    val crit = s.zones.count { it.level == Level.CRITICAL }
    val avgRisk = s.zones.map { it.risk }.average().toInt()
    val lvl = if (crit > 0) Level.CRITICAL else if (s.zones.any { it.level == Level.WARNING }) Level.WARNING else Level.NORMAL
    val spraying = s.trucks.firstOrNull { it.status == TruckStatus.SPRAYING }
    val riskTxt = stringResource(if (avgRisk >= 60) R.string.risk_high else if (avgRisk >= 40) R.string.risk_mod else R.string.risk_low)
    ScreenList {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.mine_name), fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text("${stringResource(R.string.shift_day)} • ${s.scenario.label()}", color = TextLo, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    StatusChip(stringResource(R.string.sys_online), Ok, Icons.Default.Check); Spacer(Modifier.height(4.dp))
                    StatusChip(stringResource(if (s.auto) R.string.auto_mode else R.string.manual), if (s.auto) Ok else Amber)
                }
                IconButton(onClick = onSettings) { Icon(Icons.Default.Settings, stringResource(R.string.nav_settings), tint = TextLo) }
            }
        }
        item {
            Panel(Modifier.fillMaxWidth(), lvl.color().copy(alpha = .6f)) {
                Text(stringResource(R.string.road_condition), color = TextLo, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RiskGauge(avgRisk, lvl, 130.dp); Spacer(Modifier.width(14.dp))
                    Column {
                        Text(stringResource(R.string.dust_risk_fmt, riskTxt), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(stringResource(R.string.avg_moisture_fmt, avgM)); Text(stringResource(R.string.critical_zones_fmt, crit))
                        Spacer(Modifier.height(6.dp))
                        StatusChip(stringResource(if (crit > 0) R.string.watering_required else R.string.roads_ok), lvl.color(), lvl.icon())
                    }
                }
            }
        }
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard(stringResource(R.string.m_dust), "$avgD", stringResource(if (avgD > 60) R.string.sub_high else R.string.sub_ok), if (avgD > 60) Crit else Ok, Modifier.weight(1f))
                    MetricCard(stringResource(R.string.m_moisture), stringResource(R.string.pct_fmt, avgM), stringResource(if (avgM < 40) R.string.sub_low else R.string.sub_ok), if (avgM < 40) Warn else Ok, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard(stringResource(R.string.m_used), stringResource(R.string.liters_fmt, s.waterUsed), stringResource(R.string.sub_demo), Water, Modifier.weight(1f))
                    MetricCard(stringResource(R.string.m_saved), stringResource(R.string.liters_fmt, s.saved), stringResource(R.string.sub_est), Water, Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MetricCard(stringResource(R.string.m_active), "${s.zones.count { it.sensorOnline }} / ${s.zones.size}", stringResource(R.string.sub_sensors), Ok, Modifier.weight(1f))
                    MetricCard(stringResource(R.string.m_critical), "$crit", stringResource(if (crit > 0) R.string.sub_attention else R.string.sub_none), if (crit > 0) Crit else Ok, Modifier.weight(1f))
                }
            }
        }
        item {
            val pulse by rememberInfiniteTransition(label = "p").animateFloat(.3f, 1f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "a")
            Panel(Modifier.fillMaxWidth(), if (spraying != null) Water else Border) {
                SectionHeader(stringResource(R.string.active_watering))
                if (spraying == null) Text(stringResource(R.string.no_active), color = TextLo)
                else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(Modifier.size(12.dp).background(Water.copy(alpha = pulse), RoundedCornerShape(50))); Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.truck_zone_title_fmt, spraying.id, spraying.zoneId ?: 0), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                    Text(stringResource(R.string.active_stats_fmt, 72, spraying.speed, spraying.flow, spraying.tank))
                    TankBar(spraying.tank, Modifier.padding(vertical = 8.dp))
                    OutlinedButton(onClick = onControl) { Text(stringResource(R.string.view_operation)) }
                }
            }
        }
        item { SectionHeader(stringResource(R.string.priority_zones)) }
        items(s.zones.sortedByDescending { it.risk }.take(3)) { ZoneCard(it) { onZone(it.id) } }
        item { SectionHeader(stringResource(R.string.event_log)) }
        item { Panel(Modifier.fillMaxWidth()) { s.log.reversed().forEach { Text("• ${it.text()}", fontSize = 13.sp, color = TextLo) } } }
    }
}

@Composable
fun ZonesScreen(s: MineState, onZone: (Int) -> Unit) {
    var q by remember { mutableStateOf("") }; var f by remember { mutableStateOf("ALL") }
    val spr = s.trucks.mapNotNull { if (it.status == TruckStatus.SPRAYING) it.zoneId else null }
    val names = stringArrayResource(R.array.zone_names)
    val list = s.zones.filter { z ->
        (q.isBlank() || names[z.id - 1].contains(q, true) || z.id.toString() == q.trim()) &&
        when (f) { "NORMAL" -> z.level == Level.NORMAL; "WARNING" -> z.level == Level.WARNING; "CRITICAL" -> z.level == Level.CRITICAL; "SPRAYING" -> z.id in spr; else -> true }
    }.sortedByDescending { it.risk }
    val filters = listOf("ALL" to R.string.f_all, "NORMAL" to R.string.f_normal, "WARNING" to R.string.f_warning, "CRITICAL" to R.string.f_critical, "SPRAYING" to R.string.f_spraying)
    ScreenList {
        item { OutlinedTextField(q, { q = it }, Modifier.fillMaxWidth(), placeholder = { Text(stringResource(R.string.search_zones)) }, singleLine = true) }
        item { Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            filters.forEach { (k, r) -> FilterChip(f == k, { f = k }, { Text(stringResource(r)) }) } } }
        if (list.isEmpty()) item { Text(stringResource(R.string.no_zones), color = TextLo) }
        items(list) { ZoneCard(it) { onZone(it.id) } }
    }
}

@Composable
private fun stringArrayResource(id: Int): Array<String> = androidx.compose.ui.res.stringArrayResource(id)

@Composable
fun ZoneDetailScreen(s: MineState, id: Int, onBack: () -> Unit) {
    val z = s.zones.first { it.id == id }
    val surface = stringResource(if (z.moisture < 30) R.string.dry else if (z.moisture > 75) R.string.wet else R.string.damp)
    val rows = listOf(
        R.string.l_dust to stringResource(R.string.pct_fmt, z.dust), R.string.l_moisture to stringResource(R.string.pct_fmt, z.moisture),
        R.string.l_temp to stringResource(R.string.temp_fmt, z.temp), R.string.l_humidity to stringResource(R.string.pct_fmt, z.humidity),
        R.string.l_wind to stringResource(R.string.kmh_fmt, z.wind), R.string.l_traffic to trafficLabel(z.traffic), R.string.l_surface to surface,
        R.string.l_last to stringResource(R.string.min_ago_fmt, z.lastWateringMin),
        R.string.l_sensor to stringResource(if (z.sensorOnline) R.string.online else R.string.offline))
    ScreenList {
        item { TextButton(onClick = onBack) { Text(stringResource(R.string.back)) } }
        item { Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text(stringResource(R.string.zone_fmt, z.id), color = TextLo); Text(zoneName(z), fontSize = 22.sp, fontWeight = FontWeight.Bold) }
            LevelChip(z.level) } }
        item { Panel(Modifier.fillMaxWidth()) { Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) { RiskGauge(z.risk, z.level, 170.dp) } } }
        item { SectionHeader(stringResource(R.string.sensor_data)) }
        item { Panel(Modifier.fillMaxWidth()) {
            rows.forEach { (k, v) -> Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) { Text(stringResource(k), Modifier.weight(1f), color = TextLo); Text(v, fontWeight = FontWeight.SemiBold) } } } }
        item { Panel(Modifier.fillMaxWidth(), z.level.color()) {
            SectionHeader(stringResource(R.string.rec_title))
            if (z.level == Level.NORMAL) Text(stringResource(R.string.no_water_needed))
            else {
                Text(z.actionText(), fontWeight = FontWeight.Bold, fontSize = 18.sp); Text(stringResource(R.string.sugg_fmt, z.intensity))
                Text(stringResource(R.string.est_water_fmt, z.estLiters)); Text(stringResource(R.string.priority_fmt, z.level.label()), color = z.level.color())
            } } }
    }
}

@Composable
fun ControlScreen(s: MineState, vm: MineViewModel) {
    val top = s.zones.maxByOrNull { it.risk }!!
    var why by remember { mutableStateOf(true) }; var confirm by remember { mutableStateOf(false) }
    var slider by remember { mutableStateOf(60f) }; var pick by remember { mutableStateOf(top.id) }
    ScreenList {
        item { Text(stringResource(R.string.control_title), fontSize = 22.sp, fontWeight = FontWeight.Bold) }
        item { Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Scenario.values().forEach { FilterChip(s.scenario == it, { vm.setScenario(it) }, { Text(it.label()) }) } } }
        item { Panel(Modifier.fillMaxWidth(), if (s.auto) Ok else Amber) { Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) { Text(stringResource(R.string.sys_mode), color = TextLo, fontSize = 12.sp)
                Text(stringResource(if (s.auto) R.string.mode_auto else R.string.mode_manual), fontWeight = FontWeight.Bold, fontSize = 18.sp) }
            Switch(s.auto, { vm.setAuto(it) }) } } }
        item { Panel(Modifier.fillMaxWidth()) {
            SectionHeader(stringResource(R.string.engine_title_fmt, top.id, zoneName(top)))
            Text(stringResource(R.string.engine_l1_fmt, top.dust, top.moisture, top.temp, top.humidity))
            Text(stringResource(R.string.engine_l2_fmt, top.wind, trafficLabel(top.traffic), top.lastWateringMin))
            Text(stringResource(R.string.risk_analysis), color = TextLo, modifier = Modifier.padding(vertical = 6.dp))
            Text(stringResource(R.string.risk_score_fmt, top.risk), fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(stringResource(R.string.sys_decision), color = TextLo, modifier = Modifier.padding(vertical = 6.dp))
            if (top.level == Level.NORMAL) StatusChip(stringResource(R.string.no_spray), Ok, Level.NORMAL.icon())
            else { StatusChip(stringResource(R.string.spray_required_fmt, top.level.label()), top.level.color(), top.level.icon())
                Text(stringResource(R.string.rec_intensity_fmt, top.intensity), Modifier.padding(top = 6.dp)) }
        } }
        item { Panel(Modifier.fillMaxWidth()) {
            Row(Modifier.clickable { why = !why }) { Text(stringResource(R.string.why_title), Modifier.weight(1f), fontWeight = FontWeight.SemiBold); Text(if (why) "▲" else "▼") }
            if (why) {
                if (top.moisture < 30) Text(stringResource(R.string.why_moisture_fmt, top.moisture))
                if (top.dust > 60) Text(stringResource(R.string.why_dust_fmt, top.dust))
                if (top.traffic == "HIGH") Text(stringResource(R.string.why_traffic))
                if (top.temp > 32) Text(stringResource(R.string.why_temp))
                if (top.lastWateringMin > 120) Text(stringResource(R.string.why_old))
                if (top.level == Level.NORMAL) Text(stringResource(R.string.why_ok))
            } } }
        if (!s.auto) item { Panel(Modifier.fillMaxWidth(), Amber) {
            SectionHeader(stringResource(R.string.manual_title))
            Text(stringResource(R.string.zone_pick_fmt, pick, zoneName(s.zones.first { it.id == pick })))
            Row(Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) { s.zones.forEach { FilterChip(pick == it.id, { pick = it.id }, { Text("${it.id}") }) } }
            Text(stringResource(R.string.intensity_flow_fmt, slider.toInt(), (slider * 12).toInt()))
            Slider(slider, { slider = it }, valueRange = 10f..100f)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { confirm = true }) { Text(stringResource(R.string.start_spray)) }
                OutlinedButton(onClick = { vm.stopSpray(pick) }) { Text(stringResource(R.string.stop)) } }
        } }
    }
    if (confirm) ConfirmationDialog(stringResource(R.string.confirm_title), stringResource(R.string.confirm_text_fmt, pick, slider.toInt()),
        { vm.manualSpray(pick); confirm = false }, { confirm = false })
}

@Composable
fun FleetScreen(s: MineState) = ScreenList {
    val q = s.trucks.count { it.status == TruckStatus.REFILLING }
    item { Text(stringResource(R.string.trucks_title), fontSize = 22.sp, fontWeight = FontWeight.Bold) }
    item { Text(stringResource(R.string.fleet_summary_fmt, s.trucks.size, s.trucks.count { it.status == TruckStatus.SPRAYING }, q), color = TextLo) }
    items(s.trucks) { TruckCard(it) }
    item { SectionHeader(stringResource(R.string.refill_stations)) }
    item { Panel(Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.station_a_fmt, stringResource(if (q == 0) R.string.avail else R.string.busy), (q - 1).coerceAtLeast(0)))
        Text(stringResource(R.string.station_b_fmt, stringResource(R.string.avail))) } }
}

@Composable
fun MapScreen(s: MineState, onZone: (Int) -> Unit) {
    val edges = listOf(1 to 2, 1 to 3, 3 to 4, 3 to 5, 2 to 10, 10 to 5, 5 to 6, 6 to 7, 7 to 8, 7 to 11, 11 to 4, 4 to 12, 4 to 9, 9 to 3)
    val spr = s.trucks.mapNotNull { if (it.status == TruckStatus.SPRAYING) it.zoneId else null }
    fun col(z: RoadZone) = if (!z.sensorOnline) Off else if (z.id in spr) Water else z.level.color()
    val lCrusher = stringResource(R.string.map_crusher); val lRefill = stringResource(R.string.map_refill)
    val lLoading = stringResource(R.string.map_loading); val lDump = stringResource(R.string.map_dump)
    ScreenList {
        item { Text(stringResource(R.string.map_title), fontSize = 22.sp, fontWeight = FontWeight.Bold) }
        item { Panel(Modifier.fillMaxWidth()) {
            Canvas(Modifier.fillMaxWidth().aspectRatio(1f).pointerInput(s.zones) {
                detectTapGestures { p -> s.zones.minByOrNull { (Offset(it.x * size.width, it.y * size.height) - p).getDistance() }
                    ?.takeIf { (Offset(it.x * size.width, it.y * size.height) - p).getDistance() < 60f }?.let { onZone(it.id) } } }) {
                val zm = s.zones.associateBy { it.id }
                fun pos(z: RoadZone) = Offset(z.x * size.width, z.y * size.height)
                edges.forEach { (a, b) -> val za = zm[a]!!; val zb = zm[b]!!
                    drawLine(if (za.level.ordinal >= zb.level.ordinal) col(za) else col(zb), pos(za), pos(zb), 10.dp.toPx()) }
                val paint = android.graphics.Paint().apply { color = android.graphics.Color.WHITE; textSize = 11.sp.toPx(); textAlign = android.graphics.Paint.Align.CENTER }
                s.zones.forEach { z -> drawCircle(Bg, 13.dp.toPx(), pos(z)); drawCircle(col(z), 11.dp.toPx(), pos(z), style = Stroke(3.dp.toPx()))
                    drawContext.canvas.nativeCanvas.drawText("${z.id}", pos(z).x, pos(z).y + 4.dp.toPx(), paint) }
                val lbl = android.graphics.Paint().apply { color = android.graphics.Color.GRAY; textSize = 10.sp.toPx(); textAlign = android.graphics.Paint.Align.CENTER }
                mapOf(lCrusher to zm[7]!!, lRefill to zm[10]!!, lLoading to zm[3]!!, lDump to zm[8]!!).forEach { (n, z) ->
                    drawContext.canvas.nativeCanvas.drawText(n, pos(z).x, pos(z).y - 18.dp.toPx(), lbl) }
                s.trucks.forEachIndexed { i, t -> val base = t.zoneId?.let { zm[it] } ?: if (t.status == TruckStatus.REFILLING) zm[10]!! else zm[2]!!
                    val o = pos(base) + Offset(16.dp.toPx() + i * 4.dp.toPx(), 12.dp.toPx() + i * 6.dp.toPx())
                    drawCircle(Amber, 7.dp.toPx(), o)
                    drawContext.canvas.nativeCanvas.drawText(t.id.takeLast(2), o.x + 18.dp.toPx(), o.y + 4.dp.toPx(), lbl) }
            }
            Text(stringResource(R.string.map_hint), color = TextLo, fontSize = 12.sp)
        } }
        item { Panel(Modifier.fillMaxWidth()) { SectionHeader(stringResource(R.string.legend))
            listOf(R.string.leg_ok to Ok, R.string.leg_warn to Warn, R.string.leg_crit to Crit, R.string.leg_water to Water, R.string.leg_off to Off).forEach { (n, c) ->
                Row(verticalAlignment = Alignment.CenterVertically) { Box(Modifier.size(10.dp).background(c, RoundedCornerShape(50))); Spacer(Modifier.width(8.dp)); Text(stringResource(n), fontSize = 13.sp) } } } }
        items(s.trucks.filter { it.status != TruckStatus.IDLE }) { TruckCard(it) }
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) =
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) { Text(label, Modifier.weight(1f)); Switch(checked, onChange) }

@Composable
fun SettingsScreen(s: MineState, set: AppSettings, vm: MineViewModel) = ScreenList {
    item { Text(stringResource(R.string.settings_title), fontSize = 22.sp, fontWeight = FontWeight.Bold) }
    item { Panel(Modifier.fillMaxWidth()) {
        SectionHeader(stringResource(R.string.set_general))
        Text(stringResource(R.string.set_language), color = TextLo, fontSize = 13.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(set.lang == "en", { vm.setLang("en") }, { Text(stringResource(R.string.lang_en)) })
            FilterChip(set.lang == "fa", { vm.setLang("fa") }, { Text(stringResource(R.string.lang_fa)) })
        } } }
    item { Panel(Modifier.fillMaxWidth()) {
        SectionHeader(stringResource(R.string.set_smart))
        SwitchRow(stringResource(R.string.auto_watering), s.auto) { vm.setAuto(it) }
        SwitchRow(stringResource(R.string.auto_dispatch), set.dispatch) { v -> vm.updateSettings { it.copy(dispatch = v) } }
        SwitchRow(stringResource(R.string.conserve), set.conserve) { v -> vm.updateSettings { it.copy(conserve = v) } }
        SwitchRow(stringResource(R.string.overwater), set.overwater) { v -> vm.updateSettings { it.copy(overwater = v) } }
    } }
    item { Panel(Modifier.fillMaxWidth()) {
        SectionHeader(stringResource(R.string.set_notif))
        SwitchRow(stringResource(R.string.n_critical), set.notifCritical) { v -> vm.updateSettings { it.copy(notifCritical = v) } }
        SwitchRow(stringResource(R.string.n_warning), set.notifWarning) { v -> vm.updateSettings { it.copy(notifWarning = v) } }
        SwitchRow(stringResource(R.string.n_truck), set.notifTruck) { v -> vm.updateSettings { it.copy(notifTruck = v) } }
        SwitchRow(stringResource(R.string.n_sensor), set.notifSensor) { v -> vm.updateSettings { it.copy(notifSensor = v) } }
    } }
    item { Panel(Modifier.fillMaxWidth()) {
        SectionHeader(stringResource(R.string.set_data))
        Text(stringResource(R.string.refresh_fmt, set.refreshSec))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(1, 2, 5).forEach { n -> FilterChip(set.refreshSec == n, { vm.updateSettings { it.copy(refreshSec = n) } }, { Text("$n") }) } }
        SwitchRow(stringResource(R.string.demo_mode), set.demo) { v -> vm.updateSettings { it.copy(demo = v) } }
        Text(stringResource(R.string.api_config), Modifier.padding(top = 6.dp), fontWeight = FontWeight.SemiBold)
        Text(stringResource(R.string.api_placeholder), color = TextLo, fontSize = 12.sp)
    } }
    item { Panel(Modifier.fillMaxWidth()) {
        SectionHeader(stringResource(R.string.set_about))
        Text(stringResource(R.string.about_version_fmt, "0.2-demo"), fontWeight = FontWeight.SemiBold)
        Text(stringResource(R.string.about_desc), color = TextLo, fontSize = 13.sp)
        Text(stringResource(R.string.local_note), color = TextLo, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp))
    } }
}
