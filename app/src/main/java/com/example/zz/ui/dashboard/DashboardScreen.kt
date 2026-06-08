package com.example.zz.ui.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zz.domain.model.UserProfile
import com.example.zz.domain.model.WeightEntry
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onLogout: () -> Unit = {}
) {
    val userProfile by viewModel.userProfile.collectAsState()
    var showWeightDialog by remember { mutableStateOf(false) }

    userProfile?.let { profile ->
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Fitness App") },
                    actions = {
                        IconButton(onClick = onLogout) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Wyloguj")
                        }
                    }
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { showWeightDialog = true },
                    icon = { Text("+") },
                    text = { Text("Waga") },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Witaj, ${profile.name}!",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    SummaryCard(profile)
                }

                item {
                    WeightChartCard(profile)
                }

                item {
                    MacrosCard(profile)
                }

                item {
                    Text(
                        "Historia wagi",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        fontWeight = FontWeight.Bold
                    )
                }

                items(profile.weightHistory.reversed()) { entry ->
                    WeightEntryRow(entry)
                }
                
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    if (showWeightDialog) {
        AddWeightDialog(
            onDismiss = { showWeightDialog = false },
            onConfirm = { weight ->
                viewModel.addWeightEntry(weight)
                showWeightDialog = false
            }
        )
    }
}

@Composable
fun SummaryCard(profile: UserProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(100.dp)) {
                CircularProgressIndicator(
                    progress = { 0.7f },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 10.dp,
                    strokeCap = StrokeCap.Round,
                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${profile.targetCalories}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text("kcal", style = MaterialTheme.typography.labelSmall)
                }
            }
            
            Column {
                Text(
                    "Twój cel: ${profile.goal}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Waga docelowa: ${profile.targetWeight} kg",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Pozostało: ${(profile.targetCalories * 0.3).toInt()} kcal",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun MacrosCard(profile: UserProfile) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Makroskładniki",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))
            MacroRow("Białko", profile.proteinGrams, Color(0xFFE57373), 0.8f)
            MacroRow("Tłuszcze", profile.fatGrams, Color(0xFFFFB74D), 0.5f)
            MacroRow("Węglowodany", profile.carbGrams, Color(0xFF64B5F6), 0.9f)
        }
    }
}

@Composable
fun MacroRow(label: String, grams: Int, color: Color, progress: Float) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Row(verticalAlignment = Alignment.Bottom) {
                Text("${(grams * progress).toInt()}", fontWeight = FontWeight.Bold)
                Text("/${grams}g", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.2f),
            strokeCap = StrokeCap.Round
        )
    }
}

@Composable
fun WeightChartCard(profile: UserProfile) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Postęp wagi",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    if (expanded) "Zwiń" else "Pokaż wykres",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            val currentWeight = profile.weightHistory.lastOrNull()?.weight ?: profile.currentWeight
            Text(
                "Obecnie: $currentWeight kg / Cel: ${profile.targetWeight} kg",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    WeightChart(profile)
                    Spacer(modifier = Modifier.height(16.dp))
                    Legend()
                }
            }
        }
    }
}

@Composable
fun WeightChart(profile: UserProfile) {
    val history = profile.weightHistory
    if (history.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
            Text("Dodaj pierwszy pomiar, aby zobaczyć wykres")
        }
        return
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        val width = size.width
        val height = size.height
        val padding = 40f

        val minWeight = minOf(
            history.minOf { it.weight },
            profile.targetWeight,
            profile.currentWeight
        ) - 2
        val maxWeight = maxOf(
            history.maxOf { it.weight },
            profile.targetWeight,
            profile.currentWeight
        ) + 2
        
        val weightRange = maxWeight - minWeight
        
        val startTime = history.first().date
        val totalTimeSpan = 1000L * 60 * 60 * 24 * 7 * 12 // 12 tygodni widoku
        val endTime = startTime + totalTimeSpan

        fun getX(time: Long): Float {
            val ratio = (time - startTime).toFloat() / totalTimeSpan
            return padding + ratio * (width - 2 * padding)
        }

        fun getY(weight: Double): Float {
            val ratio = (weight.toFloat() - minWeight.toFloat()) / weightRange.toFloat()
            return height - padding - ratio * (height - 2 * padding)
        }

        // 1. Linia celu
        val targetPath = Path().apply {
            moveTo(getX(startTime), getY(history.first().weight))
            lineTo(getX(endTime), getY(profile.targetWeight))
        }
        drawPath(
            path = targetPath,
            color = secondaryColor.copy(alpha = 0.3f),
            style = Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
        )

        // 2. Linia historii
        val historyPath = Path().apply {
            history.forEachIndexed { index, entry ->
                val x = getX(entry.date)
                val y = getY(entry.weight)
                if (index == 0) moveTo(x, y) else lineTo(x, y)
            }
        }
        drawPath(
            path = historyPath,
            color = primaryColor,
            style = Stroke(width = 6f, cap = StrokeCap.Round)
        )

        history.forEach { entry ->
            drawCircle(
                color = primaryColor,
                radius = 8f,
                center = Offset(getX(entry.date), getY(entry.weight))
            )
        }

        // 3. Przewidywanie
        if (history.size >= 2) {
            val last = history.last()
            val prev = history[history.size - 2]
            val timeDiff = last.date - prev.date
            if (timeDiff > 0) {
                val weightDiff = last.weight - prev.weight
                val ratePerMs = weightDiff / timeDiff
                
                val predictionEndTime = endTime
                val predictedWeight = last.weight + ratePerMs * (predictionEndTime - last.date)
                
                val predictionPath = Path().apply {
                    moveTo(getX(last.date), getY(last.weight))
                    lineTo(getX(predictionEndTime), getY(predictedWeight))
                }
                drawPath(
                    path = predictionPath,
                    color = tertiaryColor,
                    style = Stroke(width = 4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f)))
                )
            }
        }
    }
}

@Composable
fun Legend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        LegendItem("Historia", MaterialTheme.colorScheme.primary, dashed = false)
        LegendItem("Cel", MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f), dashed = true)
        LegendItem("Prognoza", MaterialTheme.colorScheme.tertiary, dashed = true)
    }
}

@Composable
fun LegendItem(label: String, color: Color, dashed: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(width = 20.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun WeightEntryRow(entry: WeightEntry) {
    val dateStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(entry.date))
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(dateStr)
            Text("${entry.weight} kg", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AddWeightDialog(onDismiss: () -> Unit, onConfirm: (Double) -> Unit) {
    var weightText by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dodaj pomiar wagi") },
        text = {
            OutlinedTextField(
                value = weightText,
                onValueChange = { weightText = it },
                label = { Text("Waga (kg)") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(onClick = { weightText.toDoubleOrNull()?.let { onConfirm(it) } }) {
                Text("Dodaj")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Anuluj") }
        }
    )
}
