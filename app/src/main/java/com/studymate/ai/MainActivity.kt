package com.studymate.ai

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

val classOptions = (1..12).map { "Class $it" } + listOf("College / University", "Other")
val subjectOptions = listOf(
    "Mathematics", "Science", "Social Science", "English", "Tamil", "Computer Science", "Other"
)

/** Saves the student profile on this phone only. */
object ProfileStore {
    private const val FILE = "studymate_profile"

    fun save(context: Context, name: String, level: String, subjects: Set<String>) {
        context.getSharedPreferences(FILE, Context.MODE_PRIVATE).edit()
            .putString("name", name)
            .putString("level", level)
            .putStringSet("subjects", subjects)
            .apply()
    }

    fun load(context: Context): Profile? {
        val prefs = context.getSharedPreferences(FILE, Context.MODE_PRIVATE)
        val name = prefs.getString("name", null) ?: return null
        val level = prefs.getString("level", "") ?: ""
        val subjects = prefs.getStringSet("subjects", emptySet())?.toSet() ?: emptySet()
        return Profile(name, level, subjects)
    }
}

data class Profile(val name: String, val level: String, val subjects: Set<String>)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    StudyMateRoot()
                }
            }
        }
    }
}

@Composable
fun StudyMateRoot() {
    val context = LocalContext.current
    var profile by remember { mutableStateOf(ProfileStore.load(context)) }
    val current = profile

    if (current == null) {
        OnboardingScreen { name, level, subjects ->
            ProfileStore.save(context, name, level, subjects)
            profile = Profile(name, level, subjects)
        }
    } else {
        HomeScreen(current) { profile = null }
    }
}

@Composable
fun OnboardingScreen(onSave: (String, String, Set<String>) -> Unit) {
    var name by remember { mutableStateOf("") }
    var level by remember { mutableStateOf<String?>(null) }
    var subjects by remember { mutableStateOf(setOf<String>()) }
    var error by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Welcome to StudyMate AI", style = MaterialTheme.typography.headlineSmall)
        Text("Tell us about you. This stays on your phone.")

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                error = null
            },
            label = { Text("Your name *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Text("Class or level", style = MaterialTheme.typography.titleSmall)
        classOptions.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { level = option },
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = level == option, onClick = { level = option })
                Text(option)
            }
        }

        Text("Subjects", style = MaterialTheme.typography.titleSmall)
        subjectOptions.forEach { subject ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        subjects = if (subject in subjects) subjects - subject else subjects + subject
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked = subject in subjects, onCheckedChange = null)
                Text(subject)
            }
        }

        Button(
            onClick = {
                when {
                    name.isBlank() -> error = "Please enter your name."
                    level == null -> error = "Choose one class or level."
                    else -> level?.let { onSave(name.trim(), it, subjects) }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save and continue")
        }

        error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
fun HomeScreen(profile: Profile, onEdit: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Hi ${profile.name}", style = MaterialTheme.typography.headlineSmall)
        Text("Level: ${profile.level}")
        val subjectText = if (profile.subjects.isEmpty()) {
            "none selected"
        } else {
            profile.subjects.sorted().joinToString(", ")
        }
        Text("Subjects: $subjectText")

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("AI tutor", style = MaterialTheme.typography.titleMedium)
                Text("The on-device AI tutor arrives in the next stage. Your profile is saved.")
            }
        }

        OutlinedButton(onClick = onEdit) {
            Text("Edit profile")
        }
    }
}
