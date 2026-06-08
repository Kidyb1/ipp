package com.example.zz.ui.dashboard

import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.health.connect.client.PermissionController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Refresh
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
    val syncedCalories by viewModel.syncedCalories.collectAsState(initial = 0.0)
    val hasHealthPermissions by viewModel.hasHealthPermissions.collectAsState()
    val currentTip by viewModel.currentTip.collectAsState()
    var showWeightDialog by remember { mutableStateOf(false) }
    var showEditGoalDialog by remember { mutableStateOf(false) }

    val permissionsLauncher = rememberLauncherForActivityResult(
        PermissionController.createRequestPermissionResultContract()
    ) { granted ->
        viewModel.refreshHealthData()
    }

    userProfile?.let { profile ->
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Shaper") },
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
                    SummaryCard(
                        profile = profile,
                        syncedCalories = syncedCalories,
                        hasHealthPermissions = hasHealthPermissions,
                        onSyncClick = {
                            permissionsLauncher.launch(viewModel.healthPermissions)
                        },
                        onEditClick = { showEditGoalDialog = true }
                    )
                }

                item {
                    CoachTipCard(
                        profile = profile,
                        tip = currentTip,
                        onRefresh = { viewModel.refreshTip() }
                    )
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
            onConfirm = { weight, date ->
                viewModel.addWeightEntry(weight, date)
                showWeightDialog = false
            }
        )
    }

    if (showEditGoalDialog) {
        userProfile?.let { profile ->
            EditGoalDialog(
                profile = profile,
                onDismiss = { showEditGoalDialog = false },
                onConfirm = { targetWeight, pace ->
                    viewModel.updateGoalSettings(targetWeight, pace)
                    showEditGoalDialog = false
                }
            )
        }
    }
}

@Composable
fun SummaryCard(
    profile: UserProfile,
    syncedCalories: Double,
    hasHealthPermissions: Boolean,
    onSyncClick: () -> Unit,
    onEditClick: () -> Unit
) {
    val progress = if (profile.targetCalories > 0) {
        (syncedCalories / profile.targetCalories).toFloat().coerceIn(0f, 1f)
    } else 0f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick() },
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
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 10.dp,
                    strokeCap = StrokeCap.Round,
                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "${syncedCalories.toInt()}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text("z ${profile.targetCalories}", style = MaterialTheme.typography.labelSmall)
                    Text("kcal", style = MaterialTheme.typography.labelSmall)
                }
            }
            
            Column {
                Text(
                    "Cel: ${profile.goal}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                val weightDiff = kotlin.math.abs(profile.currentWeight - profile.targetWeight)
                val weeksLeft = weightDiff / profile.dietPace.weeklyChangeKg
                val daysLeft = (weeksLeft * 7).toInt()
                
                if (daysLeft > 0 && profile.goal != com.example.zz.domain.model.UserGoal.UTRZYMANIE) {
                    Text(
                        "Koniec za ok. $daysLeft dni",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                
                if (!hasHealthPermissions) {
                    Button(
                        onClick = onSyncClick,
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Połącz z Health", fontSize = 10.sp)
                    }
                } else {
                    val remaining = (profile.targetCalories - syncedCalories).coerceAtLeast(0.0).toInt()
                    Text(
                        "Pozostało: $remaining kcal",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    
                    if (syncedCalories > 0) {
                        Text(
                            "Zsynchronizowano z Health Connect",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            fontSize = 8.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CoachTipCard(
    profile: UserProfile,
    tip: String,
    onRefresh: () -> Unit
) {
    val daysOnGoal = if (profile.createdAt > 0) {
        ((System.currentTimeMillis() - profile.createdAt) / (1000 * 60 * 60 * 24)).toInt()
    } else 0

    val displayTip = if (tip.isEmpty()) {
        when {
            profile.goal == com.example.zz.domain.model.UserGoal.REDUKCJA && daysOnGoal < 7 ->
                "Pierwsze dni redukcji! Pamiętaj o picu dużej ilości wody i wyspaniu się. Organizm musi się przyzwyczaić."
            profile.goal == com.example.zz.domain.model.UserGoal.REDUKCJA && daysOnGoal >= 14 ->
                "Dwa tygodnie za Tobą! Jeśli czujesz duże zmęczenie, rozważ lekki 'refeed' - dorzuć 200kcal z węglowodanów."
            profile.goal == com.example.zz.domain.model.UserGoal.MASA ->
                "Budujemy masę! Skup się na progresji ciężarowej na treningu. Kalorie to paliwo."
            else -> "Dobra robota! Trzymaj się wyznaczonych makroskładników, a efekty same przyjdą."
        }
    } else tip

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💡", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "Podpowiedź trenera",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
                IconButton(onClick = onRefresh) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Odśwież poradę",
                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                displayTip,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            if (profile.createdAt > 0) {
                Text(
                    "Dzień ${daysOnGoal + 1} Twojego celu",
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 8.dp),
                    color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditGoalDialog(
    profile: UserProfile,
    onDismiss: () -> Unit,
    onConfirm: (Double, com.example.zz.domain.model.DietPace) -> Unit
) {
    var targetWeightText by remember { mutableStateOf(profile.targetWeight.toString()) }
    var selectedPace by remember { mutableStateOf(profile.dietPace) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edytuj cel") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = targetWeightText,
                    onValueChange = { targetWeightText = it },
                    label = { Text("Waga docelowa (kg)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                    )
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedPace.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tempo") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        com.example.zz.domain.model.DietPace.entries.forEach { pace ->
                            DropdownMenuItem(
                                text = { Text(pace.label) },
                                onClick = {
                                    selectedPace = pace
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                targetWeightText.toDoubleOrNull()?.let {
                    onConfirm(it, selectedPace)
                }
            }) {
                Text("Zapisz")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Anuluj") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWeightDialog(onDismiss: () -> Unit, onConfirm: (Double, Long) -> Unit) {
    var weightText by remember { mutableStateOf("") }
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )
    var showDatePicker by remember { mutableStateOf(false) }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("OK")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    val selectedDate = datePickerState.selectedDateMillis ?: System.currentTimeMillis()
    val dateStr = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(selectedDate))

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dodaj pomiar wagi") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text("Waga (kg)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                    )
                )
                
                OutlinedButton(
                    onClick = { showDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Data: $dateStr")
                }
            }
        },
        confirmButton = {
            Button(onClick = { 
                weightText.toDoubleOrNull()?.let { 
                    onConfirm(it, selectedDate) 
                } 
            }) {
                Text("Dodaj")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Anuluj") }
        }
    )
}
