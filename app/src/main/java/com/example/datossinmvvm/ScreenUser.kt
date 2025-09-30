package com.example.datossinmvvm

import android.service.controls.templates.TemperatureControlTemplate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.TextField
import androidx.compose.ui.res.fontResource
import kotlinx.coroutines.launch
import kotlin.concurrent.fixedRateTimer
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Button
import androidx.compose.ui.unit.sp

import androidx.room.Room
import android.content.Context
import java.util.logging.LogRecord

import android.util.Log
@Composable
fun ScreenUser(){
    val context = LocalContext.current
    val db: UserDatabase
    var id         by remember { mutableStateOf("") }
    var firstName  by remember { mutableStateOf("") }
    var lastName   by remember { mutableStateOf("") }
    var dataUser   = remember { mutableStateOf("") }

    db = crearDatabase(context)
    val dao = db.userDAO()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ){
        Spacer(Modifier.height(50.dp))
        TextField(
            value = id,
            onValueChange = {id = it},
            label = { Text("ID (solo lectura )")},
            readOnly = true,
            singleLine = true
        )
        TextField(
            value = firstName,
            onValueChange = { firstName = it },
            label = { Text("First Name")},
            singleLine = true
        )
        TextField(
            value = lastName,
            onValueChange = { lastName = it},
            label = { Text("Last Name")},
            singleLine = true
        )
        Button(
            onClick = {
                val user = User(0,firstName, lastName)
                coroutineScope.launch {
                    AgregarUsuario(user, dao = dao)
                }
                firstName = ""
                lastName = ""
            }
        ){
            Text("Agregar Usuario", fontSize = 16.sp)
        }
        Button(
            onClick = {
                val user = User(0,firstName, lastName)
                coroutineScope.launch {
                    val data = getUsers (dao = dao)
                    dataUser.value = data
                }
            }
        ){
            Text("Listar Usuarios", fontSize = 16.sp)
        }
        Text(
            text = dataUser.value, fontSize =20.sp
        )
    }
}

@Composable
fun crearDatabase(context: Context): UserDatabase{
    return Room.databaseBuilder(
        context,
        UserDatabase::class.java,
        "user.db"
    ).build()
}

suspend fun getUsers(dao: UserDAO):String{
    var rpta:String = ""

    val users = dao.getAll()
    users.forEach { user->
        val fila = user.firstName + " - " + user.lastName + "\n"
        rpta += fila
    }
    return rpta
}

suspend fun AgregarUsuario(user: User, dao: UserDAO):Unit{
    try{
        dao.insert(user)
    }
    catch (e: Exception){
        Log.e("User", "Error: insert: ${e.message}")
    }
}