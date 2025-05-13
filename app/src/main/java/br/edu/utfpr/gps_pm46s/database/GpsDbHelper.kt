package br.edu.utfpr.gps_pm46s.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class GpsDbHelper(context: Context) : SQLiteOpenHelper(
    context,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {
    companion object {
        private const val DATABASE_NAME = "gps.db"
        private const val DATABASE_VERSION = 2

        const val TABLE_NAME = "gps_points"
        const val COL_ID = "_id"
        const val COL_NAME = "name"
        const val COL_LAT = "latitude"
        const val COL_LNG = "longitude"
        const val COL_DESC = "description"
        const val COL_PHOTO = "photo_uri"
        const val COL_CREATED_AT = "created_at"

    }

    override fun onCreate(db: SQLiteDatabase) {

        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_NAME TEXT NOT NULL,
                $COL_LAT REAL NOT NULL,
                $COL_LNG REAL NOT NULL,
                $COL_DESC TEXT NOT NULL,
                $COL_PHOTO TEXT,
                 $COL_CREATED_AT INTEGER NOT NULL
            );
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // para futuras mudanças de esquema
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertPoint(
        name: String,
        lat: Double,
        lng: Double,
        desc: String,
        photoUri: String?
    ): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_NAME, name)
            put(COL_LAT, lat)
            put(COL_LNG, lng)
            put(COL_DESC, desc)
            put(COL_PHOTO, photoUri)
            put(COL_CREATED_AT, System.currentTimeMillis())
        }
        return db.insert(TABLE_NAME, null, values).also {
            db.close()
        }
    }

    // Você pode adicionar métodos de query, update e delete conforme precisar
}
