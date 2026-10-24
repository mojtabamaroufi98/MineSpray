package com.minespray.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.minespray.app.R
import com.minespray.app.ui.*
import com.minespray.app.data.*
import com.minespray.app.ui.theme.*
import androidx.compose.runtime.getValue

fun Level.color() = when (this) { Level.NORMAL -> Ok; Level.WARNING -> Warn; Level.CRITICAL -> Crit }
fun Level.icon(): ImageVector = when (this) { Level.NORMAL -> Icons.Default.Check; else -> Icons.Default.Warning }

@Composable
fun Panel(modifier: Modifier = Modifier, borderColor: Color = Border, content: @Composable ColumnScope.() -> Unit) =
    Surface(modifier, shape = RoundedCornerShape(14.dp), color = Surf, border = BorderStroke(1.dp, borderColor)) {
        Column(Modifier.padding(14.dp), content = content)
    }

@Composable
fun StatusChip(text: String, color: Color, icon: ImageVector? = null) =
    Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = .16f), border = BorderStroke(1.dp, color.copy(alpha = .6f))) {
        Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) { Icon(icon, null, tint = color, modifier = Modifier.size(14.dp)); Spacer(Modifier.width(4.dp)) }
            Text(text, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }

@Composable
fun LevelChip(l: Level) = StatusChip(l.label(), l.color(), l.icon())

@Composable
fun SectionHeader(text: String) = Text(text.uppercase(), color = TextLo, fontSize = 12.sp, fontWeight = FontWeight.Bold,
    letterSpacing = 1.2.sp, modifier = Modifier.padding(top = 8.dp, bottom = 2.dp))

@Composable
fun MetricCard(label: String, value: String, sub: String, color: Color, modifier: Modifier = Modifier) =
    Panel(modifier) {
        Text(label, color = TextLo, fontSize = 12.sp)
        Text(value, color = TextHi, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(sub, color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
    }

@Composable
fun RiskGauge(score: Int, level: Level, size: Dp = 150.dp) {
    val p by animateFloatAsState(score / 100f, label = "gauge")
    val c = level.color()
    Box(Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize().padding(10.dp)) {
            val st = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
            drawArc(Border, 135f, 270f, false, style = st)
            drawArc(c, 135f, 270f * p, false, style = st)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$score", fontSize = (size.value / 3.2).sp, fontWeight = FontWeight.Bold, color = TextHi)
            Text("/ 100", fontSize = 11.sp, color = TextLo)
        }
    }
}

@Composable
fun TankBar(percent: Int, modifier: Modifier = Modifier) {
    val p by animateFloatAsState(percent / 100f, label = "tank")
    val c = if (percent < 20) Crit else Water
    Canvas(modifier.fillMaxWidth().height(10.dp)) {
        drawRoundRect(Border, size = size, cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f))
        drawRoundRect(c, size = Size(size.width * p, size.height), cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f))
    }
}

@Composable
fun ZoneCard(z: RoadZone, onClick: () -> Unit) =
    Surface(onClick = onClick, shape = RoundedCornerShape(14.dp), color = Surf, border = BorderStroke(1.dp, z.level.color().copy(alpha = .5f))) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.zone_fmt, z.id), color = TextLo, fontSize = 12.sp)
                    Text(zoneName(z), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                LevelChip(z.level)
            }
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.card_l1_fmt, z.risk, z.dust, z.moisture), fontSize = 13.sp)
            Text(stringResource(R.string.card_l2_fmt, z.temp, trafficLabel(z.traffic), z.lastWateringMin), color = TextLo, fontSize = 12.sp)
            if (!z.sensorOnline) Text(stringResource(R.string.sensor_offline_warn), color = Warn, fontSize = 12.sp)
            if (z.level != Level.NORMAL) Text(stringResource(R.string.action_fmt, z.actionText()), color = z.level.color(), fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }
    }

@Composable
fun TruckCard(t: WaterTruck, onClick: () -> Unit = {}) {
    val c = when (t.status) { TruckStatus.SPRAYING -> Water; TruckStatus.REFILLING -> Warn; TruckStatus.ENROUTE -> Amber; else -> Ok }
    Surface(onClick = onClick, shape = RoundedCornerShape(14.dp), color = Surf, border = BorderStroke(1.dp, Border)) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(t.id, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
                StatusChip(t.status.label(), c); Spacer(Modifier.width(6.dp))
                StatusChip(stringResource(if (t.auto) R.string.mode_auto else R.string.manual), if (t.auto) Ok else Amber)
            }
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.tank_fmt, t.tank, t.liters), fontSize = 13.sp)
            TankBar(t.tank, Modifier.padding(vertical = 6.dp))
            Text(stringResource(R.string.truck_info_fmt, t.zoneId?.let { stringResource(R.string.zone_fmt, it) } ?: "—", t.speed, t.flow), color = TextLo, fontSize = 12.sp)
            if (t.tank < 20) Text(stringResource(R.string.low_tank), color = Crit, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ConfirmationDialog(title: String, text: String, onConfirm: () -> Unit, onDismiss: () -> Unit) = AlertDialog(
    onDismissRequest = onDismiss, title = { Text(title) }, text = { Text(text) },
    confirmButton = { Button(onClick = onConfirm) { Text(stringResource(R.string.confirm)) } },
    dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
)
