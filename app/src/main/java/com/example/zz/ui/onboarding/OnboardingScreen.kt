package com.example.zz.ui.onboarding

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.zz.domain.model.ActivityLevel
import com.example.zz.domain.model.Gender

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onFinish: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentStep by remember { mutableIntStateOf(1) }
    val totalSteps = 5

    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentStep > 1) {
                    TextButton(onClick = { currentStep-- }) {
                        Text("Wstecz")
                    }
                } else {
                    Spacer(modifier = Modifier.width(80.dp))
                }

                Button(
                    onClick = {
                        if (currentStep < totalSteps) {
                            currentStep++
                        } else {
                            viewModel.completeOnboarding(onFinish)
                        }
                    },
                    enabled = isStepValid(currentStep, uiState)
                ) {
                    Text(if (currentStep == totalSteps) "Oblicz i zacznij" else "Dalej")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Progress bar
            LinearProgressIndicator(
                progress = { currentStep.toFloat() / totalSteps },
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            )

            Crossfade(targetState = currentStep, label = "OnboardingStep") { step ->
                when (step) {
                    1 -> WelcomeStep(uiState.name, viewModel::updateName)
                    2 -> GenderAgeStep(uiState.gender, uiState.age, viewModel::updateGender, viewModel::updateAge)
                    3 -> PhysicalDataStep(uiState.height, uiState.currentWeight, viewModel::updateHeight, viewModel::updateCurrentWeight)
                    4 -> TargetWeightStep(uiState.targetWeight, viewModel::updateTargetWeight)
                    5 -> ActivityStep(uiState.activityLevel, viewModel::updateActivityLevel)
                }
            }
        }
    }
}

@Composable
fun WelcomeStep(name: String, onNameChange: (String) -> Unit) {
    Column {
        Text("Witaj! Jak masz na imię?", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("Twoje imię") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

@Composable
fun GenderAgeStep(gender: Gender, age: Int, onGenderChange: (Gender) -> Unit, onAgeChange: (String) -> Unit) {
    Column {
        Text("Powiedz nam coś o sobie", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        
        Text("Płeć:")
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = gender == Gender.MALE, onClick = { onGenderChange(Gender.MALE) })
            Text("Mężczyzna")
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(selected = gender == Gender.FEMALE, onClick = { onGenderChange(Gender.FEMALE) })
            Text("Kobieta")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = if (age == 0) "" else age.toString(),
            onValueChange = onAgeChange,
            label = { Text("Wiek") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}

@Composable
fun PhysicalDataStep(height: Double, weight: Double, onHeightChange: (String) -> Unit, onWeightChange: (String) -> Unit) {
    Column {
        Text("Twoje wymiary", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = if (height == 0.0) "" else height.toString(),
            onValueChange = onHeightChange,
            label = { Text("Wzrost (cm)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = if (weight == 0.0) "" else weight.toString(),
            onValueChange = onWeightChange,
            label = { Text("Obecna waga (kg)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}

@Composable
fun TargetWeightStep(targetWeight: Double, onTargetChange: (String) -> Unit) {
    Column {
        Text("Jaki jest Twój cel wagowy?", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = if (targetWeight == 0.0) "" else targetWeight.toString(),
            onValueChange = onTargetChange,
            label = { Text("Docelowa waga (kg)") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
    }
}

@Composable
fun ActivityStep(selectedLevel: ActivityLevel, onLevelChange: (ActivityLevel) -> Unit) {
    Column {
        Text("Poziom aktywności", fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        ActivityLevel.entries.forEach { level ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (level == selectedLevel),
                        onClick = { onLevelChange(level) }
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = (level == selectedLevel), onClick = { onLevelChange(level) })
                Column {
                    Text(
                        text = when(level) {
                            ActivityLevel.SEDENTARY -> "Siedzący"
                            ActivityLevel.LIGHT -> "Lekka aktywność"
                            ActivityLevel.MODERATE -> "Średnia aktywność"
                            ActivityLevel.ACTIVE -> "Wysoka aktywność"
                            ActivityLevel.VERY_ACTIVE -> "Bardzo wysoka"
                        },
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = when(level) {
                            ActivityLevel.SEDENTARY -> "Praca biurowa, brak ćwiczeń"
                            ActivityLevel.LIGHT -> "1-2 treningi w tygodniu"
                            ActivityLevel.MODERATE -> "3-4 treningi w tygodniu"
                            ActivityLevel.ACTIVE -> "Codzienne treningi"
                            ActivityLevel.VERY_ACTIVE -> "Praca fizyczna + treningi"
                        },
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

private fun isStepValid(step: Int, profile: com.example.zz.domain.model.UserProfile): Boolean {
    return when (step) {
        1 -> profile.name.isNotBlank()
        2 -> profile.age > 0
        3 -> profile.height > 100 && profile.currentWeight > 30
        4 -> profile.targetWeight > 30
        5 -> true
        else -> false
    }
}
