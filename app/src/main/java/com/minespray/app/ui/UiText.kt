package com.minespray.app.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.minespray.app.R
import com.minespray.app.data.*

@Composable fun Level.label() = stringResource(when (this) { Level.NORMAL -> R.string.lvl_normal; Level.WARNING -> R.string.lvl_warning; Level.CRITICAL -> R.string.lvl_critical })
@Composable fun RoadZone.actionText() = stringResource(when (level) { Level.CRITICAL -> R.string.act_now; Level.WARNING -> R.string.act_soon; else -> R.string.act_none })
@Composable fun zoneName(z: RoadZone): String = stringArrayResource(R.array.zone_names)[z.id - 1]
@Composable fun trafficLabel(t: String) = stringResource(when (t) { "HIGH" -> R.string.tr_high; "MED" -> R.string.tr_med; else -> R.string.tr_low })
@Composable fun Scenario.label() = stringResource(when (this) {
    Scenario.NORMAL -> R.string.sc_normal; Scenario.HIGH_DUST -> R.string.sc_high_dust; Scenario.HEAT -> R.string.sc_heat; Scenario.TRAFFIC -> R.string.sc_traffic
    Scenario.LOW_WATER -> R.string.sc_low_water; Scenario.SENSOR -> R.string.sc_sensor; Scenario.EMERGENCY -> R.string.sc_emergency })
@Composable fun TruckStatus.label() = stringResource(when (this) { TruckStatus.IDLE -> R.string.st_idle; TruckStatus.ENROUTE -> R.string.st_enroute; TruckStatus.SPRAYING -> R.string.st_spraying; TruckStatus.REFILLING -> R.string.st_refilling })
@Composable fun LogEvent.text(): String = when (kind) {
    LogKind.STARTED -> stringResource(R.string.log_started)
    LogKind.SCENARIO -> stringResource(R.string.log_scenario_fmt, Scenario.valueOf(a).label())
    LogKind.AUTO_ON -> stringResource(R.string.log_auto_on)
    LogKind.AUTO_OFF -> stringResource(R.string.log_auto_off)
    LogKind.MANUAL -> stringResource(R.string.log_manual_fmt, a, b)
    LogKind.SPRAYING -> stringResource(R.string.log_spraying_fmt, a, b)
    LogKind.COMPLETED -> stringResource(R.string.log_completed_fmt, a)
    LogKind.ASSIGN -> stringResource(R.string.log_assign_fmt, a, b, c)
}
