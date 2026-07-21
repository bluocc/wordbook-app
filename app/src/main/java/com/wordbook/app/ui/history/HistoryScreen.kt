package com.wordbook.app.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wordbook.app.ui.theme.qualityColor
import com.wordbook.app.ui.theme.Grey
import com.wordbook.app.util.DateUtils
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    onWordClick: (Long) -> Unit,
    vm: HistoryViewModel = viewModel()
) {
    val items by vm.items.collectAsState()
    val isLoading by vm.isLoading.collectAsState()

    val today = LocalDate.now()
    val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    val weekDays = listOf("一", "二", "三", "四", "五", "六", "日")

    var filterMode by remember { mutableIntStateOf(0) } // 0=日 1=周 2=月 3=年
    var selectedDay by remember { mutableStateOf(today) }
    var filterLabel by remember { mutableStateOf("${today.monthValue}月${today.dayOfMonth}日") }
    var showCalendar by remember { mutableStateOf(false) }

    LaunchedEffect(selectedDay) {
        applyDayFilter(selectedDay, vm)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "学习历史", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            weekDays.forEachIndexed { i, name ->
                val d = weekStart.plusDays(i.toLong())
                val isSel = selectedDay == d || (filterMode > 0 && isInFilterRange(d))
                val isToday = d == today

                FilterChip(
                    selected = isSel,
                    onClick = {
                        filterMode = 0
                        selectedDay = d
                        filterLabel = "${d.monthValue}月${d.dayOfMonth}日"
                    },
                    label = {
                        Text(
                            name,
                            fontSize = 13.sp,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSel) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.weight(1f).padding(horizontal = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(filterLabel, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            TextButton(onClick = { showCalendar = true }) {
                Text("更多 ▾", fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (items.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("暂无学习记录", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(items, key = { it.word.id }) { item ->
                    val stateColor = if (item.progress != null && item.progress!!.lastQuality > 0)
                        qualityColor(item.progress!!.lastQuality) else Grey

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(88.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White)
                            .border(2.dp, stateColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .clickable { onWordClick(item.word.id) }
                    ) {
                        Box(Modifier.width(4.dp).fillMaxHeight().background(stateColor))
                        Column(
                            Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 5.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(item.word.word, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                                color = Color(0xFF333333), textAlign = TextAlign.Center, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(item.word.pronunciation, fontSize = 9.sp, color = Color(0xFF999999), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(item.word.explanation, fontSize = 10.sp, color = Color(0xFF666666), maxLines = 2, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
                        }
                    }
                }
            }
        }
    }

    if (showCalendar) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showCalendar = false },
            sheetState = sheetState
        ) {
            CalendarSheet(
                year = today.year,
                month = today.monthValue,
                onDaySelected = { y, m, d ->
                    filterMode = 0
                    selectedDay = LocalDate.of(y, m, d)
                    filterLabel = "${m}月${d}日"
                    showCalendar = false
                },
                onWeekSelected = { start ->
                    filterMode = 1
                    selectedDay = start
                    val end = start.plusDays(6)
                    filterLabel = "第${start.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR)}周"
                    val s = DateUtils.dayToEpoch(start.year, start.monthValue, start.dayOfMonth)
                    val e = DateUtils.dayToEpoch(end.year, end.monthValue, end.dayOfMonth) + 24L * 3600 * 1000
                    vm.loadByDate(s, e)
                    showCalendar = false
                },
                onMonthSelected = { y, m ->
                    filterMode = 2
                    filterLabel = "${y}年${DateUtils.monthName(y, m)}"
                    val s = DateUtils.dayToEpoch(y, m, 1)
                    val lastDay = DateUtils.daysInMonth(y, m)
                    val e = DateUtils.dayToEpoch(y, m, lastDay) + 24L * 3600 * 1000
                    vm.loadByDate(s, e)
                    selectedDay = LocalDate.of(y, m, 1)
                    showCalendar = false
                },
                onYearSelected = { y ->
                    filterMode = 3
                    filterLabel = "${y}年"
                    val s = DateUtils.dayToEpoch(y, 1, 1)
                    val e = DateUtils.dayToEpoch(y, 12, 31) + 24L * 3600 * 1000
                    vm.loadByDate(s, e)
                    selectedDay = LocalDate.of(y, 1, 1)
                    showCalendar = false
                }
            )
        }
    }
}

private fun applyDayFilter(day: LocalDate, vm: HistoryViewModel) {
    val s = DateUtils.dayToEpoch(day.year, day.monthValue, day.dayOfMonth)
    vm.loadByDate(s, s + 24L * 3600 * 1000)
}

private fun isInFilterRange(day: LocalDate): Boolean = false

@Composable
private fun CalendarSheet(
    year: Int, month: Int,
    onDaySelected: (Int, Int, Int) -> Unit,
    onWeekSelected: (LocalDate) -> Unit,
    onMonthSelected: (Int, Int) -> Unit,
    onYearSelected: (Int) -> Unit
) {
    var mode by remember { mutableIntStateOf(0) }
    var calYear by remember { mutableIntStateOf(year) }
    var calMonth by remember { mutableIntStateOf(month) }
    val modes = listOf("日", "周", "月", "年")

    Column(modifier = Modifier.padding(16.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            modes.forEachIndexed { i, label ->
                FilterChip(
                    selected = mode == i,
                    onClick = { mode = i },
                    label = { Text(label, fontSize = 14.sp) },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        when (mode) {
            0 -> DayPicker(calYear, calMonth, { calYear = it }, { calMonth = it }) { d -> onDaySelected(calYear, calMonth, d) }
            1 -> WeekPicker(calYear, calMonth, { calYear = it }, { calMonth = it }) { onWeekSelected(it) }
            2 -> MonthPicker(calYear, { calYear = it }, onMonth = { m -> onMonthSelected(calYear, m) })
            else -> YearPicker(calYear) { onYearSelected(it) }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun DayPicker(
    year: Int, month: Int,
    onYearChange: (Int) -> Unit, onMonthChange: (Int) -> Unit,
    onDayClick: (Int) -> Unit
) {
    val today = LocalDate.now()
    val daysInMonth = DateUtils.daysInMonth(year, month)
    val firstDow = DateUtils.firstDayOfWeek(year, month)

    MonthYearHeader(year, month, onYearChange, onMonthChange)

    Row(Modifier.fillMaxWidth()) {
        listOf("日","一","二","三","四","五","六").forEach {
            Text(it, Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 12.sp, color = Color.Gray)
        }
    }
    Spacer(Modifier.height(4.dp))

    LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.height(240.dp)) {
        items((firstDow - 1 + daysInMonth)) { idx ->
            val day = idx - (firstDow - 1) + 1
            if (day < 1 || day > daysInMonth) {
                Box(Modifier.aspectRatio(1f))
            } else {
                val isToday = year == today.year && month == today.monthValue && day == today.dayOfMonth
                Box(
                    Modifier.aspectRatio(1f).padding(2.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .then(if (isToday) Modifier.background(MaterialTheme.colorScheme.primary) else Modifier)
                        .clickable { onDayClick(day) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("$day", fontSize = 14.sp,
                        color = if (isToday) Color.White else Color(0xFF333333),
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
    }
}

@Composable
private fun WeekPicker(
    year: Int, month: Int,
    onYearChange: (Int) -> Unit, onMonthChange: (Int) -> Unit,
    onClick: (LocalDate) -> Unit
) {
    MonthYearHeader(year, month, onYearChange, onMonthChange)

    val today = LocalDate.now()
    val firstOf = LocalDate.of(year, month, 1)
    val lastOf = firstOf.with(TemporalAdjusters.lastDayOfMonth())
    val currentWeekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
    var cursor = firstOf.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    val weeks = mutableListOf<LocalDate>()
    while (cursor.isBefore(lastOf) || cursor == lastOf) {
        if (!cursor.isAfter(currentWeekStart)) {
            weeks.add(cursor)
        }
        cursor = cursor.plusWeeks(1)
    }
    weeks.reverse()

    Column {
        weeks.forEachIndexed { i, start ->
            val end = start.plusDays(6)
            val isCurrent = !today.isBefore(start) && !today.isAfter(end)
            Row(
                Modifier.fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .then(if (isCurrent) Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)) else Modifier)
                    .clickable { onClick(start) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "${start.monthValue}/${start.dayOfMonth} - ${end.monthValue}/${end.dayOfMonth}",
                    fontSize = 15.sp,
                    color = if (isCurrent) MaterialTheme.colorScheme.primary else Color(0xFF333333),
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun MonthPicker(year: Int, onYearChange: (Int) -> Unit, onMonth: (Int) -> Unit) {
    val today = LocalDate.now()
    val months = (1..12).map { m ->
        val mn = LocalDate.of(1, m, 1).month.getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale.getDefault())
        mn
    }

    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = { onYearChange(year - 1) }) { Text("◀", fontSize = 20.sp) }
        Text("${year}年", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        TextButton(onClick = { onYearChange(year + 1) }) { Text("▶", fontSize = 20.sp) }
    }

    Spacer(Modifier.height(12.dp))

    LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.height(280.dp)) {
        items(12) { idx ->
            val m = idx + 1
            val isThisMonth = year == today.year && m == today.monthValue
            Box(
                Modifier.aspectRatio(1f).padding(4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .then(if (isThisMonth) Modifier.background(MaterialTheme.colorScheme.primary) else Modifier.background(MaterialTheme.colorScheme.surfaceVariant))
                    .clickable { onMonth(m) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "${m}月",
                    fontSize = 15.sp,
                    fontWeight = if (isThisMonth) FontWeight.Bold else FontWeight.Normal,
                    color = if (isThisMonth) Color.White else Color(0xFF333333)
                )
            }
        }
    }
}

@Composable
private fun YearPicker(year: Int, onYear: (Int) -> Unit) {
    val now = LocalDate.now()
    val startYear = now.year - 20
    val years = (startYear..now.year).toList().reversed()

    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = {
            val idx = years.indexOf(year)
            if (idx > 0) onYear(years[idx - 1])
        }) { Text("◀", fontSize = 20.sp) }
        Text("${year}年", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        TextButton(onClick = {
            val idx = years.indexOf(year)
            if (idx < years.size - 1) onYear(years[idx + 1])
        }) { Text("▶", fontSize = 20.sp) }
    }

    Spacer(Modifier.height(12.dp))

    LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.height(300.dp)) {
        items(years.size) { idx ->
            val y = years[idx]
            val isThisYear = y == now.year
            Box(
                Modifier.aspectRatio(1f).padding(4.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .then(if (isThisYear) Modifier.background(MaterialTheme.colorScheme.primary) else Modifier.background(MaterialTheme.colorScheme.surfaceVariant))
                    .clickable { onYear(y) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "$y", fontSize = 15.sp,
                    fontWeight = if (isThisYear) FontWeight.Bold else FontWeight.Normal,
                    color = if (isThisYear) Color.White else Color(0xFF333333)
                )
            }
        }
    }
}

@Composable
private fun MonthYearHeader(
    year: Int, month: Int,
    onYearChange: (Int) -> Unit, onMonthChange: (Int) -> Unit
) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(onClick = {
            if (month == 1) { onYearChange(year - 1); onMonthChange(12) } else onMonthChange(month - 1)
        }) { Text("◀", fontSize = 18.sp) }
        Text("${year}年 ${DateUtils.monthName(year, month)}", fontSize = 18.sp, fontWeight = FontWeight.Medium)
        TextButton(onClick = {
            if (month == 12) { onYearChange(year + 1); onMonthChange(1) } else onMonthChange(month + 1)
        }) { Text("▶", fontSize = 18.sp) }
    }
    Spacer(Modifier.height(8.dp))
}
