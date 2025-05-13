package br.edu.utfpr.gps_pm46s.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.widget.Toast
import br.edu.utfpr.gps_pm46s.entity.PontoTuristico

class DatabaseHandler( context : Context) : SQLiteOpenHelper( context, DATABASE_NAME, null, DATABASE_VERSION ) {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL( "CREATE TABLE IF NOT EXISTS ${TABLE_NAME} (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "latitude Double, longitude Double, nome TEXT, descricao TEXT, fotoUri TEXT )")
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL( "DROP TABLE IF EXISTS ${TABLE_NAME} " )
        onCreate( db )
    }

    fun insert( pontoTuristico : PontoTuristico ) {
        val registro = ContentValues()
        registro.put( "latitude", pontoTuristico.latitude )
        registro.put( "longitude", pontoTuristico.longitude )
        registro.put( "nome", pontoTuristico.nome )
        registro.put( "descricao", pontoTuristico.descricao )
        registro.put( "fotoUri", pontoTuristico.fotoUri )

        val banco = this.writableDatabase

        banco.insert( TABLE_NAME, null, registro )
    }

    fun update( pontoTuristico : PontoTuristico ) {
        val registro = ContentValues()
        registro.put( "latitude", pontoTuristico.latitude )
        registro.put( "longitude", pontoTuristico.longitude )
        registro.put( "nome", pontoTuristico.nome )
        registro.put( "descricao", pontoTuristico.descricao )
        registro.put( "fotoUri", pontoTuristico.fotoUri )

        val banco = this.writableDatabase

        banco.update( TABLE_NAME, registro, "_id=${pontoTuristico._id}", null )
    }

    fun delete( _id : Int ) {
        val banco = this.writableDatabase
        banco.delete( TABLE_NAME, "_id=${_id}", null)
    }

    fun find( _id : Int ) : PontoTuristico? {
        val banco = this.writableDatabase

        val registro = banco.query(
            TABLE_NAME,
            null,
            "_id=${_id}",
            null,
            null,
            null,
            null
        )

        if ( registro.moveToNext() ) {
            val cadastro = PontoTuristico(
                _id,
                registro.getDouble( LATITUDE ),
                registro.getDouble( LONGITUDE ),
                registro.getString( NOME ),
                registro.getString( DESCRICAO ),
                registro.getString( FOTO_URI )
            )

            return cadastro

        } else {
            return null
        }

    }

    fun list() : Cursor {
        val banco = this.writableDatabase

        val registros = banco.query(
            TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            null
        )

        return registros
    }

    companion object {
        private const val DATABASE_NAME = "dbfile.sqlite"
        private const val DATABASE_VERSION = 2
        private const val TABLE_NAME = "pontoturistico"
        private const val _ID = 0
        private const val LATITUDE = 1
        private const val LONGITUDE = 2
        private const val NOME = 3
        private const val DESCRICAO = 4
        private const val FOTO_URI = 5 }

}