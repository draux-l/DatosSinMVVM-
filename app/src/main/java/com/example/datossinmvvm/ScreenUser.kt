package com.example.datossinmvvm

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import kotlinx.coroutines.launch

// 🔹 Crear la base de datos
fun crearDatabase(context: Context): UserDatabase {
    return Room.databaseBuilder(
        context,
        UserDatabase::class.java,
        "user.db"
    ).build()
}

// 🔹 Pantalla principal
@Composable
fun ScreenUser() {
    val context = LocalContext.current
    val db = remember { crearDatabase(context) }
    val dao = db.userDAO()
    val coroutineScope = rememberCoroutineScope()

    // Estados
    var id by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var selectedId by remember { mutableStateOf<Int?>(null) }
    var users by remember { mutableStateOf<List<User>>(emptyList()) }

    // 🔸 Cargar datos al iniciar
    LaunchedEffect(Unit) {
        users = dao.getAll()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // ID solo lectura
        TextField(
            value = id,
            onValueChange = {},
            label = { Text("ID") },
            singleLine = true,
            readOnly = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        TextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(8.dp))

        TextField(
            value = lastName,
            onValueChange = { lastName = it },
            label = { Text("Last Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        // 🔹 Botones de acción
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 🔸 Agregar
            Button(
                onClick = {
                    coroutineScope.launch {
                        if (firstName.isNotBlank() && lastName.isNotBlank()) {
                            val user = User(0, firstName.trim(), lastName.trim())
                            try {
                                dao.insert(user)
                                users = dao.getAll()

                                firstName = ""
                                lastName = ""
                                id = ""
                                selectedId = null
                                Toast.makeText(context, "Usuario agregado", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Log.e("User", "Error insert: ${e.message}")
                            }
                        } else {
                            Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Agregar", fontSize = 16.sp)
            }

            // 🔸 Eliminar
            Button(
                onClick = {
                    coroutineScope.launch {
                        val targetId = selectedId ?: id.toIntOrNull()
                        if (targetId != null) {
                            try {
                                dao.deleteById(targetId)
                                users = users.filterNot { it.uid == targetId }

                                id = ""
                                firstName = ""
                                lastName = ""
                                selectedId = null
                                Toast.makeText(context, "Usuario eliminado", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Log.e("User", "Error delete: ${e.message}")
                            }
                        } else {
                            Toast.makeText(context, "Selecciona un usuario", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Eliminar", fontSize = 16.sp)
            }

            // 🔸 Listar
            Button(
                onClick = {
                    coroutineScope.launch {
                        try {
                            users = dao.getAll()

                            // 🔹 Limpiar campos al listar
                            id = ""
                            firstName = ""
                            lastName = ""
                            selectedId = null

                            Toast.makeText(context, "Lista actualizada", Toast.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Log.e("User", "Error listar: ${e.message}")
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Listar", fontSize = 16.sp)
            }
        }

        Spacer(Modifier.height(16.dp))

        // 🔹 Lista dinámica
        Text("Usuarios registrados:", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(users) { user ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedId = user.uid
                            id = user.uid.toString()
                            firstName = user.firstName.orEmpty()
                            lastName = user.lastName.orEmpty()
                        }
                        .padding(vertical = 8.dp)
                ) {
                    Text("${user.uid} - ${user.firstName} ${user.lastName}", fontSize = 18.sp)
                    Divider()
                }
            }
        }
    }
}
