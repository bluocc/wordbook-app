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
    var selectedDay by remember { mutableStateOf(today) }
    var showCalendar by remember { mutableStateOf(false) }
    var calendarMode by remember { mutableIntStateOf(0) } // 0=月 1=周 2=年
    var calYear by remember { mutableIntStateOf(today.year) }
    var calMonth by remember { mutableIntStateOf(today.monthValue) }

    val weekDays = listOf("一", "二", "三", "四", "五", "六", "日")
    val weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    LaunchedEffect(selectedDay) {
        val start = DateUtils.dayToEpoch(selectedDay.year, selectedDay.monthValue, selectedDay.dayOfMonth)
        vm.loadByDate(start, start + 24 * 60 * 60 * 1000L)
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
                val isSel = d == selectedDay
                val isToday = d == today

                FilterChip(
                    selected = isSel,
                    onClick = { selectedDay = d },
                    label = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(name, fontSize = 11.sp, color = if (isSel) MaterialTheme.colorScheme.primary else Color.Gray)
                            Text(
                                "${d.dayOfMonth}",
                                fontSize = 15.sp,
                                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) MaterialTheme.colorScheme.primary else Color(0xFF333333)
                            )
                        }
                    },
                    modifier = Modifier.weight(1f).padding(horizontal = 1.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${selectedDay.year}/${selectedDay.monthValue}/${selectedDay.dayOfMonth}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
            TextButton(onClick = { showCalendar = true }) {
                Text("更多 ▾", fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (items.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("该日期暂无学习记录", fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
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
        ModalBottomSheet(onDismissRequest = { showCalendar = false }) {
            CalendarSheet(
                mode = calendarMode,
                year = calYear,
                month = calMonth,
                onModeChange = { calendarMode = it },
                onYearChange = { calYear = it },
                onMonthChange = { calMonth = it },
                onDaySelected = { y, m, d ->
                    selectedDay = LocalDate.of(y, m, d)
                    showCalendar = false
                },
                onWeekSelected = { startOfWeek ->
                    selectedDay = startOfWeek
                    showCalendar = false
                    val end = startOfWeek.plusDays(6)
                    val s = DateUtils.dayToEpoch(startOfWeek.year, startOfWeek.monthValue, startOfWeek.dayOfMonth)
                    val e = DateUtils.dayToEpoch(end.year, end.monthValue, end.dayOfMonth) + 24 * 60 * 60 * 1000L
                    vm.loadByDate(s, e)
                },
                onMonthRangeSelected = { y, mn ->
                    val s = DateUtils.dayToEpoch(y, mn, 1)
                    val lastDay = DateUtils.daysInMonth(y, mn)
                    val e = DateUtils.dayToEpoch(y, mn, lastDay) + 24 * 60 * 60 * 1000L
                    vm.loadByDate(s, e)
                    showCalendar = false
                },
                onYearRangeSelected = { y ->
                    val s = DateUtils.dayToEpoch(y, 1, 1)
                    val e = DateUtils.dayToEpoch(y, 12, 31) + 24 * 60 * 60 * 1000L
                    vm.loadByDate(s, e)
                    showCalendar = false
                }
            )
        }
    }
}

@Composable
private fun CalendarSheet(
    mode: Int,
    year: Int,
    month: Int,
    onModeChange: (Int) -> Unit,
    onYearChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    onDaySelected: (Int, Int, Int) -> Unit,
    onWeekSelected: (LocalDate) -> Unit,
    onMonthRangeSelected: (Int, Int) -> Unit,
    onYearRangeSelected: (Int) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            listOf("月", "周", "年").forEachIndexed { i, label ->
                val sel = mode == i
                FilterChip(
                    selected = sel,
                    onClick = { onModeChange(i) },
                    label = { Text(label, fontSize = 14.sp) },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = {
                when (mode) {
                    0 -> if (month == 1) { onYearChange(year - 1); onMonthChange(12) } else onMonthChange(month - 1)
                    1 -> if (month == 1) { onYearChange(year - 1); onMonthChange(12) } else onMonthChange(month - 1)
                    2 -> onYearChange(year - 1)
                }
            }) { Text("◀", fontSize = 18.sp) }

            Text(
                when (mode) {
                    0 -> "${year}年 ${DateUtils.monthName(year, month)}"
                    1 -> {
                        val d = LocalDate.of(year, month, 1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                        "${d.year}年 ${DateUtils.monthName(d.year, d.monthValue)} 第${
                            d.get(java.time.temporal.IsoFields.WEEK_OF_WEEK_BASED_YEAR)
                        }周"
                    }
                    else -> "${year}年"
                },
                fontSize = 18.sp, fontWeight = FontWeight.Medium
            )

            TextButton(onClick = {
                when (mode) {
                    0 -> if (month == 12) { onYearChange(year + 1); onMonthChange(1) } else onMonthChange(month + 1)
                    1 -> if (month == 12) { onYearChange(year + 1); onMonthChange(1) } else onMonthChange(month + 1)
                    2 -> onYearChange(year + 1)
                }
            }) { Text("▶", fontSize = 18.sp) }
        }

        Spacer(modifier = Modifier.height(8.dp))

        when (mode) {
            0 -> MonthGrid(year, month) { d -> onDaySelected(year, month, d) }
            1 -> {
                WeekSelector(year, month) { w ->
                    val mon = w.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    onWeekSelected(mon)
                }
            }
            else -> {
                YearSelector(year) { y -> onYearRangeSelected(y) }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun MonthGrid(year: Int, month: Int, onDayClick: (Int) -> Unit) {
    val today = LocalDate.now()
    val daysInMonth = DateUtils.daysInMonth(year, month)
    val firstDow = DateUtils.firstDayOfWeek(year, month)
    val headers = listOf("日","一","二","三","四","五","六")

    Row(Modifier.fillMaxWidth()) {
        headers.forEach { Text(it, Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 12.sp, color = Color.Gray) }
    }
    Spacer(Modifier.height(4.dp))

    LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.height(260.dp)) {
        items((firstDow - 1 + daysInMonth)) { idx ->
            val day = idx - (firstDow - 1) + 1
            if (day < 1 || day > daysInMonth) {
                Box(Modifier.aspectRatio(1f))
            } else {
                val isToday = year == today.year && month == today.monthValue && day == today.dayOfMonth
                Box(
                    Modifier.aspectRatio(1f).padding(2.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .then(if (isToday) Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)) else Modifier)
                        .clickable { onDayClick(day) },
                    contentAlignment = Alignment.Center
                ) {
                    Text("$day", fontSize = 13.sp, color = if (isToday) MaterialTheme.colorScheme.primary else Color(0xFF333333),
                        fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal)
                }
            }
        }
    }
}

@Composable
private fun WeekSelector(year: Int, month: Int, onWeekClick: (LocalDate) -> Unit) {
    val fisrtOf = LocalDate.of(year, month, 1)
    val lastOf = fisrtOf.with(TemporalAdjusters.lastDayOfMonth())
    var cursor = fisrtOf.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))

    val weeks = mutableListOf<LocalDate>()
    while (cursor.isBefore(lastOf) || cursor == lastOf) {
        weeks.add(cursor)
        cursor = cursor.plusWeeks(1)
    }

    Column {
        weeks.forEach { start ->
            val end = start.plusDays(6)
            TextButton(
                onClick = { onWeekClick(start) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("${start.monthValue}/${start.dayOfMonth} - ${end.monthValue}/${end.dayOfMonth}",
                    fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun YearSelector(year: Int, onClick: (Int) -> Unit) {
    val years = (year - 2..year + 2).toList()
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        years.forEach { y ->
            TextButton(onClick = { onClick(y) }, modifier = Modifier.padding(horizontal = 4.dp)) {
                Text("$y", fontSize = 16.sp,
                    color = if (y == year) MaterialTheme.colorScheme.primary else Color.Gray,
                    fontWeight = if (y == year) FontWeight.Bold else FontWeight.Normal)
            }
        }
    }
}
