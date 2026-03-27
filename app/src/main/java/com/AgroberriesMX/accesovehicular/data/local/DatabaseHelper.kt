package com.AgroberriesMX.accesovehicular.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.AgroberriesMX.accesovehicular.domain.model.LoginModel
import com.AgroberriesMX.accesovehicular.domain.model.RecordModel
import com.AgroberriesMX.accesovehicular.domain.model.RondinLocation // <-- ¡Añade esta línea!
import com.AgroberriesMX.accesovehicular.domain.model.RondinModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter




class DatabaseHelper(context: Context):SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object{
        private const val DATABASE_NAME = "agroaccess.db"
        private const val DATABASE_VERSION = 7 //subimos la version por que vamos a modificar la base de datos - Ricardo Dimas 16/06/2025

        private const val CREATE_TABLE_RONDIN_LOCATIONS = """
            CREATE TABLE z_segRondines (
                idRondinRon INTEGER PRIMARY KEY AUTOINCREMENT,    
                codigoUsuRon TEXT NOT NULL,
                fechaRon TEXT NOT NULL,
                latGpsRon REAL NOT NULL,
                longGpsRon REAL NOT NULL,
                nomUbicacionRon TEXT NOT NULL,
                usuModRon TEXT NOT NULL,
                isSynced INTEGER DEFAULT 0
            )
        """

        private const val CREATE_TABLE_LOGINS = """
            CREATE TABLE genlogin (
                controlLog INTEGER PRIMARY KEY AUTOINCREMENT,
                vNombreUsu TEXT,                
                cCodigoUsu TEXT,
                vPasswordUsu TEXT
            )
        """

        private const val CREATE_TABLE_VEHICLES = """
            CREATE TABLE z_geningresovehiculo (
                controlLog INTEGER PRIMARY KEY AUTOINCREMENT,
                dIngresoInv TEXT,
                vNombrechofInv TEXT,
                vAcompanianteInv TEXT,
                vEmpresaInv TEXT,
                cPlacaInv TEXT,
                vMotivoInv TEXT,
                dHringresoInv TEXT,
                dHrsalidaInv TEXT,
                cCodigoUsu TEXT,
                cMovimientoInv TEXT,
                isSynced INTEGER DEFAULT 0
            )
        """
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CREATE_TABLE_LOGINS)
        db.execSQL(CREATE_TABLE_VEHICLES)
        db.execSQL(CREATE_TABLE_RONDIN_LOCATIONS)
    }

    private fun dropTables(db: SQLiteDatabase){
        db.execSQL("DROP TABLE IF EXISTS genlogin")
        db.execSQL("DROP TABLE IF EXISTS z_geningresovehiculo")
        db.execSQL("DROP TABLE IF EXISTS z_segRondines")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        dropTables(db)

        onCreate(db)
    }

    fun insertVehicle(
        dIngresoInv: String,
        vNombrechofInv: String,
        vAcompanianteInv: String,
        vEmpresaInv: String,
        cPlacaInv: String,
        vMotivoInv: String,
        dHringresoInv: String,
        dHrsalidaInv: String,
        cCodigoUsu: String,
        cMovimientoInv: String,
        isSynced: Int
    ): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
                put("dIngresoInv", dIngresoInv)
                put("vNombrechofInv", vNombrechofInv)
                put("vAcompanianteInv", vAcompanianteInv)
                put("vEmpresaInv", vEmpresaInv)
                put("cPlacaInv", cPlacaInv)
                put("vMotivoInv", vMotivoInv)
                put("dHringresoInv", dHringresoInv)
                put("dHrsalidaInv", dHrsalidaInv)
                put("cCodigoUsu", cCodigoUsu)
                put("cMovimientoInv", cMovimientoInv)
                put("isSynced", isSynced)
        }

        return db.insert("z_geningresovehiculo", null, values).also {
            db.close()
        }
    }

    fun getRecords(): List<RecordModel> {
        val db = this.readableDatabase
        val cursor = db.query(
            "z_geningresovehiculo",
            null, // Todas las columnas
            null, // Sin selección
            null, // Sin argumentos de selección
            null, // Sin agrupamiento
            null, // Sin having
            null // Sin orden
        )

        val records = mutableListOf<RecordModel>()
        if (cursor.moveToFirst()) {
            do {
                val record = RecordModel(
                    controlLog = cursor.getLong(cursor.getColumnIndexOrThrow("controlLog")),
                    dIngresoInv = cursor.getString(cursor.getColumnIndexOrThrow("dIngresoInv")),
                    vNombrechofInv = cursor.getString(cursor.getColumnIndexOrThrow("vNombrechofInv")),
                    vAcompanianteInv = cursor.getString(cursor.getColumnIndexOrThrow("vAcompanianteInv")),
                    vEmpresaInv = cursor.getString(cursor.getColumnIndexOrThrow("vEmpresaInv")),
                    cPlacaInv = cursor.getString(cursor.getColumnIndexOrThrow("cPlacaInv")),
                    vMotivoInv = cursor.getString(cursor.getColumnIndexOrThrow("vMotivoInv")),
                    dHringresoInv = cursor.getString(cursor.getColumnIndexOrThrow("dHringresoInv")),
                    dHrsalidaInv = cursor.getString(cursor.getColumnIndexOrThrow("dHrsalidaInv")),
                    cCodigoUsu = cursor.getString(cursor.getColumnIndexOrThrow("cCodigoUsu")),
                    cMovimientoInv = cursor.getString(cursor.getColumnIndexOrThrow("cMovimientoInv")),
                    isSynced = cursor.getInt(cursor.getColumnIndexOrThrow("isSynced"))
                )
                records.add(record)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return records
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getVehiclesByDate(): List<RecordModel>? {
        val db = this.readableDatabase
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val formattedDate = currentDate.format(formatter)
        val cursor = db.query(
            "z_geningresovehiculo", // Nombre de la tabla
            null, // Selecciona todas las columnas
            "dIngresoInv = ?", // Condición de selección
            arrayOf(formattedDate), // Argumento para la condición
            null, // No agrupar
            null, // No tener un filtro 'having'
            null, // No ordenar
            null // Limitar a un resultado
        )

        val records = mutableListOf<RecordModel>()

        try {
            if (cursor.moveToFirst()) {
                do{
                    val record = RecordModel(
                        controlLog = cursor.getLong(cursor.getColumnIndexOrThrow("controlLog")),
                        dIngresoInv = cursor.getString(cursor.getColumnIndexOrThrow("dIngresoInv")),
                        vNombrechofInv = cursor.getString(cursor.getColumnIndexOrThrow("vNombrechofInv")),
                        vAcompanianteInv = cursor.getString(cursor.getColumnIndexOrThrow("vAcompanianteInv")),
                        vEmpresaInv = cursor.getString(cursor.getColumnIndexOrThrow("vEmpresaInv")),
                        cPlacaInv = cursor.getString(cursor.getColumnIndexOrThrow("cPlacaInv")),
                        vMotivoInv = cursor.getString(cursor.getColumnIndexOrThrow("vMotivoInv")),
                        dHringresoInv = cursor.getString(cursor.getColumnIndexOrThrow("dHringresoInv")),
                        dHrsalidaInv = cursor.getString(cursor.getColumnIndexOrThrow("dHrsalidaInv")),
                        cCodigoUsu = cursor.getString(cursor.getColumnIndexOrThrow("cCodigoUsu")),
                        cMovimientoInv = cursor.getString(cursor.getColumnIndexOrThrow("cMovimientoInv")),
                        isSynced = cursor.getInt(cursor.getColumnIndexOrThrow("isSynced"))
                    )
                    records.add(record)
                } while (cursor.moveToNext())
            }
        } catch (e: Exception){
            e.printStackTrace()
        } finally {
            cursor.close()
        }
        return if (records.isNotEmpty()) records else null
    }

    fun getVehicleByPlate(cPlacaInv: String): List<RecordModel>? {
        val db = this.readableDatabase
        val cursor = db.query(
            "z_geningresovehiculo", // Nombre de la tabla
            null, // Selecciona todas las columnas
            "cPlacaInv = ?", // Condición de selección
            arrayOf(cPlacaInv), // Argumento para la condición
            null, // No agrupar
            null, // No tener un filtro 'having'
            null, // No ordenar
            null // Limitar a un resultado
        )

        val records = mutableListOf<RecordModel>()

        try {
            if (cursor.moveToFirst()) {
                do {
                    val record = RecordModel(
                        controlLog = cursor.getLong(cursor.getColumnIndexOrThrow("controlLog")),
                        dIngresoInv = cursor.getString(cursor.getColumnIndexOrThrow("dIngresoInv")),
                        vNombrechofInv = cursor.getString(cursor.getColumnIndexOrThrow("vNombrechofInv")),
                        vAcompanianteInv = cursor.getString(cursor.getColumnIndexOrThrow("vAcompanianteInv")),
                        vEmpresaInv = cursor.getString(cursor.getColumnIndexOrThrow("vEmpresaInv")),
                        cPlacaInv = cursor.getString(cursor.getColumnIndexOrThrow("cPlacaInv")),
                        vMotivoInv = cursor.getString(cursor.getColumnIndexOrThrow("vMotivoInv")),
                        dHringresoInv = cursor.getString(cursor.getColumnIndexOrThrow("dHringresoInv")),
                        dHrsalidaInv = cursor.getString(cursor.getColumnIndexOrThrow("dHrsalidaInv")),
                        cCodigoUsu = cursor.getString(cursor.getColumnIndexOrThrow("cCodigoUsu")),
                        cMovimientoInv = cursor.getString(cursor.getColumnIndexOrThrow("cMovimientoInv")),
                        isSynced = cursor.getInt(cursor.getColumnIndexOrThrow("isSynced"))
                    )
                    records.add(record)
                } while (cursor.moveToNext())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor.close()
        }

        return if(records.isNotEmpty()) records else null
    }

    fun getRecordByControlLog(controlLog: Long): RecordModel? {
        val db = this.readableDatabase
        val cursor = db.query(
            "z_geningresovehiculo", // Nombre de la tabla
            null, // Selecciona todas las columnas
            "controlLog = ?", // Condición de selección
            arrayOf(controlLog.toString()), // Argumento para la condición
            null, // No agrupar
            null, // No tener un filtro 'having'
            null, // No ordenar
            "1" // Limitar a un resultado
        )

        var record: RecordModel? = null
        if (cursor.moveToFirst()) {
            record = RecordModel(
                controlLog = cursor.getLong(cursor.getColumnIndexOrThrow("controlLog")),
                dIngresoInv = cursor.getString(cursor.getColumnIndexOrThrow("dIngresoInv")),
                vNombrechofInv = cursor.getString(cursor.getColumnIndexOrThrow("vNombrechofInv")),
                vAcompanianteInv = cursor.getString(cursor.getColumnIndexOrThrow("vAcompanianteInv")),
                vEmpresaInv = cursor.getString(cursor.getColumnIndexOrThrow("vEmpresaInv")),
                cPlacaInv = cursor.getString(cursor.getColumnIndexOrThrow("cPlacaInv")),
                vMotivoInv = cursor.getString(cursor.getColumnIndexOrThrow("vMotivoInv")),
                dHringresoInv = cursor.getString(cursor.getColumnIndexOrThrow("dHringresoInv")),
                dHrsalidaInv = cursor.getString(cursor.getColumnIndexOrThrow("dHrsalidaInv")),
                cCodigoUsu = cursor.getString(cursor.getColumnIndexOrThrow("cCodigoUsu")),
                cMovimientoInv = cursor.getString(cursor.getColumnIndexOrThrow("cMovimientoInv")),
                isSynced = cursor.getInt(cursor.getColumnIndexOrThrow("isSynced"))
            )
        }

        cursor.close()
        return record
    }

    fun listUnsynchronizedRecords(): List<RecordModel>? {
        val db = this.readableDatabase

        val cursor = db.query(
            "z_geningresovehiculo", // Nombre de la tabla
            null, // Selecciona todas las columnas
            "isSynced = 0", // Condición de selección
            null, // Argumento para la condición
            null, // No agrupar
            null, // No tener un filtro 'having'
            null, // No ordenar
            null // Limitar a un resultado
        )

        val records = mutableListOf<RecordModel>()

        try {
            if (cursor.moveToFirst()) {
                do{
                    val record = RecordModel(
                        controlLog = cursor.getLong(cursor.getColumnIndexOrThrow("controlLog")),
                        dIngresoInv = cursor.getString(cursor.getColumnIndexOrThrow("dIngresoInv")),
                        vNombrechofInv = cursor.getString(cursor.getColumnIndexOrThrow("vNombrechofInv")),
                        vAcompanianteInv = cursor.getString(cursor.getColumnIndexOrThrow("vAcompanianteInv")),
                        vEmpresaInv = cursor.getString(cursor.getColumnIndexOrThrow("vEmpresaInv")),
                        cPlacaInv = cursor.getString(cursor.getColumnIndexOrThrow("cPlacaInv")),
                        vMotivoInv = cursor.getString(cursor.getColumnIndexOrThrow("vMotivoInv")),
                        dHringresoInv = cursor.getString(cursor.getColumnIndexOrThrow("dHringresoInv")),
                        dHrsalidaInv = cursor.getString(cursor.getColumnIndexOrThrow("dHrsalidaInv")),
                        cCodigoUsu = cursor.getString(cursor.getColumnIndexOrThrow("cCodigoUsu")).uppercase(),
                        cMovimientoInv = cursor.getString(cursor.getColumnIndexOrThrow("cMovimientoInv")),
                        isSynced = cursor.getInt(cursor.getColumnIndexOrThrow("isSynced"))
                    )
                    records.add(record)
                } while (cursor.moveToNext())
            }
        } catch (e: Exception){
            e.printStackTrace()
        } finally {
            cursor.close()
        }
        return if (records.isNotEmpty()) records else null
    }

    fun getUnsynchronizedRecords(): List<RecordModel>? {
        val db = this.readableDatabase

        val cursor = db.query(
            "z_geningresovehiculo", // Nombre de la tabla
            null, // Selecciona todas las columnas
            //"isSynced = 0 AND dHrsalidaInv IS NOT NULL AND dHrsalidaInv != ''", // Condición de selección
            "isSynced = 0", // Condición de selección Ricardo Dimas 12/06/2025
            null, // Argumento para la condición
            null, // No agrupar
            null, // No tener un filtro 'having'
            null, // No ordenar
            null // Limitar a un resultado
        )

        val records = mutableListOf<RecordModel>()

        try {
            if (cursor.moveToFirst()) {
                do{
                    val record = RecordModel(
                        controlLog = cursor.getLong(cursor.getColumnIndexOrThrow("controlLog")),
                        dIngresoInv = cursor.getString(cursor.getColumnIndexOrThrow("dIngresoInv")),
                        vNombrechofInv = cursor.getString(cursor.getColumnIndexOrThrow("vNombrechofInv")),
                        vAcompanianteInv = cursor.getString(cursor.getColumnIndexOrThrow("vAcompanianteInv")),
                        vEmpresaInv = cursor.getString(cursor.getColumnIndexOrThrow("vEmpresaInv")),
                        cPlacaInv = cursor.getString(cursor.getColumnIndexOrThrow("cPlacaInv")),
                        vMotivoInv = cursor.getString(cursor.getColumnIndexOrThrow("vMotivoInv")),
                        dHringresoInv = cursor.getString(cursor.getColumnIndexOrThrow("dHringresoInv")),
                        dHrsalidaInv = cursor.getString(cursor.getColumnIndexOrThrow("dHrsalidaInv")),
                        cCodigoUsu = cursor.getString(cursor.getColumnIndexOrThrow("cCodigoUsu")).uppercase(),
                        cMovimientoInv = cursor.getString(cursor.getColumnIndexOrThrow("cMovimientoInv")),
                        isSynced = cursor.getInt(cursor.getColumnIndexOrThrow("isSynced"))
                    )
                    records.add(record)
                } while (cursor.moveToNext())
            }
        } catch (e: Exception){
            e.printStackTrace()
        } finally {
            cursor.close()
        }
        return if (records.isNotEmpty()) records else null
    }

    fun updateVehicle(
        controlLog: Long,
        dIngresoInv: String,
        vNombreChofInv: String,
        vAcompanianteInv: String,
        vEmpresaInv: String,
        cPlacaInv: String,
        vMotivoInv: String,
        dHringresoInv: String,
        dHrsalidaInv: String,
        cCodigoUsu: String,
        cMovimientoInv: String,
        isSynced: Int
    ): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("dIngresoInv", dIngresoInv)
            put("vNombrechofInv", vNombreChofInv)
            put("vAcompanianteInv", vAcompanianteInv)
            put("vEmpresaInv", vEmpresaInv)
            put("cPlacaInv", cPlacaInv)
            put("vMotivoInv", vMotivoInv)
            put("dHringresoInv", dHringresoInv)
            put("dHrsalidaInv", dHrsalidaInv)
            put("cCodigoUsu", cCodigoUsu)
            put("cMovimientoInv", cMovimientoInv)
            put("isSynced",isSynced)
        }

        // Actualiza el registro y devuelve el número de filas afectadas
        return try {
            db.update("z_geningresovehiculo", values, "controlLog = ?", arrayOf(controlLog.toString()))
        } catch (e: Exception) {
            // Maneja el error adecuadamente
            Log.e("Database Error", "Error updating vehicle: ${e.message}")
            0 // Retorna 0 o maneja el error según tu lógica
        } finally {
            db.close() // Asegúrate de cerrar la base de datos si no se va a usar más
        }
    }

    //Credentials
    fun getUserByCodeAndPassword(cCodigoUsu: String, vPasswordUsu: String): LoginModel? {
        val db = this.readableDatabase
        return try{
            db.query(
                "genlogin", // Nombre de la tabla
                null, // Selecciona todas las columnas
                "cCodigoUsu = ? AND vPasswordUsu = ?", // Condición de selección
                arrayOf(cCodigoUsu, vPasswordUsu), // Argumento para la condición
                null, // No agrupar
                null, // No tener un filtro 'having'
                null, // No ordenar
                "1" // Limitar a un resultado
            ).use { cursor ->
                if(cursor.moveToFirst()) {
                    LoginModel(
                        controlLog = cursor.getLong(cursor.getColumnIndexOrThrow("controlLog")),
                        vNombreUsu = cursor.getString(cursor.getColumnIndexOrThrow("vNombreUsu")),
                        cCodigoUsu = cursor.getString(cursor.getColumnIndexOrThrow("cCodigoUsu")),
                        vPasswordUsu = cursor.getString(cursor.getColumnIndexOrThrow("vPasswordUsu"))
                    )
                } else {

                    null
                }
            }
        }catch (e:Exception){
            Log.e("DBError", "Error fetching user: ${e.message}")
            null
        }
    }

    fun getAllUsers(): List<LoginModel> {
        val db = this.readableDatabase
        val users = mutableListOf<LoginModel>()

        return try {
            db.query(
                "genlogin", // Nombre de la tabla
                null, // Selecciona todas las columnas
                null, // No condiciones
                null, // Sin argumentos
                null, // No agrupar
                null, // Sin filtro 'having'
                null // Sin ordenar
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    val user = LoginModel(
                        controlLog = cursor.getLong(cursor.getColumnIndexOrThrow("controlLog")),
                        vNombreUsu = cursor.getString(cursor.getColumnIndexOrThrow("vNombreUsu")),
                        cCodigoUsu = cursor.getString(cursor.getColumnIndexOrThrow("cCodigoUsu")),
                        vPasswordUsu = cursor.getString(cursor.getColumnIndexOrThrow("vPasswordUsu"))
                    )
                    users.add(user)
                    Log.d("DBUser", "User: $user") // Imprime el registro en el log
                }
            }
            users
        } catch (e: Exception) {
            Log.e("DBError", "Error fetching users: ${e.message}")
            emptyList()
        }
    }

    fun insertUser(
        vNombreUsu: String,
        cCodigoUsu: String,
        vPasswordUsu: String
    ): Long{
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("vNombreUsu", vNombreUsu)
            put("cCodigoUsu", cCodigoUsu)
            put("vPasswordUsu", vPasswordUsu)
        }

        return db.insert("genlogin", null, values).also {
            db.close()
        }
    }

    fun deleteAllUsers() {
        val db = this.writableDatabase
        try {
            db.execSQL("DELETE FROM genlogin")
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            db.close()
        }
    }

    // --- ¡¡¡ESTE MÉTODO DEBE EXISTIR EN TU DATABASEHELPER!!! ---
    // Maneja la inserción o actualización de un LoginModel.
    fun insertOrUpdateLoginUser(user: LoginModel): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            // No incluyas controlLog aquí si es autoincremental en la tabla
            put("vNombreUsu", user.vNombreUsu)
            put("cCodigoUsu", user.cCodigoUsu)
            put("vPasswordUsu", user.vPasswordUsu)
        }

        // Intenta actualizar primero, basado en cCodigoUsu
        val rowsAffected = db.update(
            "genlogin",
            values,
            "cCodigoUsu = ?",
            arrayOf(user.cCodigoUsu)
        )

        // Si no se actualizó ninguna fila, inserta una nueva
        return if (rowsAffected > 0) {
            rowsAffected.toLong().also { db.close() } // Devuelve el número de filas actualizadas
        } else {
            db.insert("genlogin", null, values).also { db.close() } // Devuelve el ID de la nueva fila insertada
        }
    }

    //RONDINES RICARDO DIMAS 20/06/2025
    //RICARDO DIMAS
    fun listUnsynchronizedRondines(): List<RondinModel>? {
        val db = this.readableDatabase

        val cursor = db.query(
            "z_segRondines", // Usa el nombre de tu tabla
            null,                   // columns (all)
            "isSynced = 0",         // selection (WHERE clause)
            null,                   // selectionArgs
            null,                   // groupBy
            null,                   // having
            "idRondinRon DESC",     // orderBy (¡Esto es correcto aquí!)
            null    // Puedes ordenar por el ID más reciente
        )

        val rondines = mutableListOf<RondinModel>()

        try {
            if (cursor.moveToFirst()) {
                do{
                    val rondin = RondinModel(
                        idRondinRon = cursor.getLong(cursor.getColumnIndexOrThrow("idRondinRon")),
                        codigoUsuRon = cursor.getString(cursor.getColumnIndexOrThrow("codigoUsuRon")),
                        fechaRon = cursor.getString(cursor.getColumnIndexOrThrow("fechaRon")),
                        latGpsRon = cursor.getDouble(cursor.getColumnIndexOrThrow("latGpsRon")),
                        longGpsRon = cursor.getDouble(cursor.getColumnIndexOrThrow("longGpsRon")),
                        nomUbicacionRon = cursor.getString(cursor.getColumnIndexOrThrow("nomUbicacionRon")),
                        usuModRon = cursor.getString(cursor.getColumnIndexOrThrow("usuModRon")),
                        isSynced = cursor.getInt(cursor.getColumnIndexOrThrow("isSynced"))
                    )
                    rondines.add(rondin)
                } while (cursor.moveToNext())
            }
        } catch (e: Exception){
            e.printStackTrace()
        } finally {
            cursor.close()
        }
        return if (rondines.isNotEmpty()) rondines else null
    }

    // Agrega o modifica este método en tu DatabaseHelper
    fun insertRondinLocation(location: RondinLocation): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            // idRondinRon es autoincrementable, no lo incluyes aquí
            put("codigoUsuRon", location.codigoUsuRon)
            put("fechaRon", location.fechaRon)
            put("latGpsRon", location.latGpsRon)
            put("longGpsRon", location.longGpsRon)
            put("nomUbicacionRon", location.nomUbicacionRon)
            put("usuModRon", location.usuModRon)
        }
        return db.insert("z_segRondines", null, values).also {
            db.close()
        }
    }
    // Agrega o modifica este método en tu DatabaseHelper
    fun getAllRondinLocations(): List<RondinLocation> {
        val db = this.readableDatabase
        val cursor = db.query(
            "z_segRondines", // Usa el nombre de tu tabla
            null, // Todas las columnas
            null, null, null, null,
            "idRondinRon DESC" // Puedes ordenar por el ID más reciente
        )

        val locations = mutableListOf<RondinLocation>()
        if (cursor.moveToFirst()) {
            do {
                val location = RondinLocation(
                    idRondinRon = cursor.getInt(cursor.getColumnIndexOrThrow("idRondinRon")),
                    codigoUsuRon = cursor.getString(cursor.getColumnIndexOrThrow("codigoUsuRon")),
                    fechaRon = cursor.getString(cursor.getColumnIndexOrThrow("fechaRon")),
                    latGpsRon = cursor.getDouble(cursor.getColumnIndexOrThrow("latGpsRon")),
                    longGpsRon = cursor.getDouble(cursor.getColumnIndexOrThrow("longGpsRon")),
                    nomUbicacionRon = cursor.getString(cursor.getColumnIndexOrThrow("nomUbicacionRon")),
                    usuModRon = cursor.getString(cursor.getColumnIndexOrThrow("usuModRon"))
                )
                locations.add(location)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()
        return locations
    }

    //RICARDO DIMAS
    fun updateRondines(
         idRondinRon: Long,
         codigoUsuRon: String,
         fechaRon: String,
         latGpsRon: Double,
         longGpsRon: Double,
         nomUbicacionRon: String,
         usuModRon: String,
         isSynced: Int
     ): Int {
         val db = this.writableDatabase
         val values = ContentValues().apply {
             put("idRondinRon", idRondinRon)
             put("codigoUsuRon", codigoUsuRon)
             put("fechaRon", fechaRon)
             put("latGpsRon", latGpsRon)
             put("longGpsRon", longGpsRon)
             put("nomUbicacionRon", nomUbicacionRon)
             put("usuModRon", usuModRon)
             put("isSynced",isSynced)
         }

         // Actualiza el registro y devuelve el número de filas afectadas
         return try {
             db.update("z_segRondines", values, "idRondinRon = ?", arrayOf(idRondinRon.toString()))
         } catch (e: Exception) {
             // Maneja el error adecuadamente
             Log.e("Database Error", "Error updating vehicle: ${e.message}")
             0 // Retorna 0 o maneja el error según tu lógica
         } finally {
             db.close() // Asegúrate de cerrar la base de datos si no se va a usar más
         }
     }

}