package com.wordbook.app.ui.record

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wordbook.app.util.DateUtils
import java.time.LocalDate

@Composable
fun RecordScreen(viewModel: RecordViewModel = viewModel()) {
    val addedDates by viewModel.addedDates.collectAsState()
    val selectedDayCount by viewModel.selectedDayCount.collectAsState()

    val today = LocalDate.now()
    var currentYear by remember { mutableIntStateOf(today.year) }
    var currentMonth by remember { mutableIntStateOf(today.monthValue) }
    var selectedDay by remember { mutableStateOf<Pair<Int, Int, Int>?>(null) }

    val monthName = DateUtils.monthName(currentYear, currentMonth)
    val daysInMonth = DateUtils.daysInMonth(currentYear, currentMonth)
    val firstDayOfWeek = DateUtils.firstDayOfWeek(currentYear, currentMonth)

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp)
    ) {
        Text(
            text = "学习记录",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = {
                if (currentMonth == 1) { currentYear--; currentMonth = 12 }
                else currentMonth--
            }) {
                Text("<", fontSize = 18.sp)
            }

            Text(
                text = "$currentYear年 $monthName",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )

            TextButton(onClick = {
                if (currentMonth == 12) { currentYear++; currentMonth = 1 }
                else currentMonth++
            }) {
                Text(">", fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        val dayHeaders = listOf("一", "二", "三", "四", "五", "六", "日")
        Row(modifier = Modifier.fillMaxWidth()) {
            dayHeaders.forEach { day ->
                Text(
                    text = day,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.weight(1f)
        ) {
            val totalCells = firstDayOfWeek - 1 + daysInMonth
            items(totalCells) { index ->
                val day = index - (firstDayOfWeek - 1) + 1

                if (day < 1 || day > daysInMonth) {
                    Box(modifier = Modifier.aspectRatio(1f))
                } else {
                    val isToday = currentYear == today.year &&
                            currentMonth == today.monthValue && day == today.dayOfMonth

                    val epoch = DateUtils.dayToEpoch(currentYear, currentMonth, day)
                    val hasRecord = addedDates.contains(epoch)

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .padding(2.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .then(
                                if (hasRecord) Modifier.background(
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                ) else Modifier.background(MaterialTheme.colorScheme.surface)
                            )
                            .then(
                                if (isToday) Modifier.border(
                                    2.dp,
                                    MaterialTheme.colorScheme.primary,
                                    RoundedCornerShape(8.dp)
                                ) else Modifier
                            )
                            .clickable {
                                selectedDay = Triple(currentYear, currentMonth, day)
                                viewModel.selectDay(currentYear, currentMonth, day)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$day",
                            fontSize = 14.sp,
                            color = if (hasRecord)
                                MaterialTheme.colorScheme.onPrimary
                            else
                                MaterialTheme.colorScheme.onSurface,
                            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        if (selectedDay != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                val (year, month, day) = selectedDay!!
                Text(
                    text = "${year}年${month}月${day}日: 新增 $selectedDayCount 个单词",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
