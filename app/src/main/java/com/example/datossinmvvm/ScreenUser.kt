package com.example.datossinmvvm

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.room.Room
import kotlinx.coroutines.launch

fun crearDatabase(context: Context): UserDatabase {
    return Room.databaseBuilder(
        context,
        UserDatabase::class.java,
        "user.db"
    ).build()
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenUser() {
    val context = LocalContext.current
    val db = remember { crearDatabase(context) }
    val dao = db.userDAO()
    val coroutineScope = rememberCoroutineScope()

    // Variables de estado
    var id by remember { mutableStateOf("") }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var selectedId by remember { mutableStateOf<Int?>(null) }
    var users by remember { mutableStateOf<List<User>>(emptyList()) }

    // 🔸 Cargar usuarios al iniciar
    LaunchedEffect(Unit) {
        users = dao.getAll()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Usuarios") },
                actions = {
                    // Botón Agregar Usuario (en la AppBar)
                    IconButton(onClick = {
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
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar Usuario")
                    }

                    // Botón Listar Usuarios (en la AppBar)
                    IconButton(onClick = {
                        coroutineScope.launch {
                            try {
                                users = dao.getAll()
                                id = ""
                                firstName = ""
                                lastName = ""
                                selectedId = null
                                Toast.makeText(context, "Lista actualizada", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Log.e("User", "Error listar: ${e.message}")
                            }
                        }
                    }) {
                        Icon(Icons.Default.List, contentDescription = "Listar Usuarios")
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Campo ID (solo lectura)
            TextField(
                value = id,
                onValueChange = {},
                label = { Text("ID (Solo Lectura)") },
                singleLine = true,
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            // Campo Nombre
            TextField(
                value = firstName,
                onValueChange = { firstName = it },
                label = { Text("First Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            // Campo Apellido
            TextField(
                value = lastName,
                onValueChange = { lastName = it },
                label = { Text("Last Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // Botom Eliminar
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
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Eliminar", fontSize = 16.sp)
            }

            Spacer(Modifier.height(16.dp))

            // Lista Clickeable de users
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
}
