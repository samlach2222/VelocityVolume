package com.samlach2222.velocityvolume

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    // below is the method for creating a database by a sqlite query
    override fun onCreate(db: SQLiteDatabase) {
        // below is a sqlite query, where column names
        // along with their data types is given
        val queryProfile = ("CREATE TABLE Profile (" +
                ID + " INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                NAME + " VARCHAR(30) NOT NULL," +
                SWITCH + " BOOLEAN NOT NULL," +
                I1O + " INTEGER," +
                I1C + " INTEGER," +
                I2O + " INTEGER," +
                I2C + " INTEGER," +
                I3O + " INTEGER," +
                I3C + " INTEGER," +
                I4O + " INTEGER," +
                I4C + " INTEGER," +
                I5O + " INTEGER," +
                I5C + " INTEGER" +
        ")")

        // we are calling sqlite
        // method for executing our query
        db.execSQL(queryProfile)

        val querySettings = ("CREATE TABLE Settings (" +
                UOM + " TEXT CHECK( " + UOM + " IN ('km','miles') )   NOT NULL DEFAULT 'km', " +
                NM + " TEXT CHECK( " + NM + " IN ('system','on','off') )   NOT NULL DEFAULT 'system', " +
                GPSD + " INTEGER DEFAULT 0," +
                LSPI + " INTEGER DEFAULT null" +
        ")")
        db.execSQL(querySettings)

        // we are in the onCreate function, so that means there's no row in the Settings table
        // we add a single row to the Settings table with LSPI of -1
        val values = ContentValues()
        values.put(LSPI, -1)
        db.insert("Settings", null, values)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        //// this method is to check if table already exists
        // db.execSQL("DROP TABLE IF EXISTS Profile")
        // db.execSQL("DROP TABLE IF EXISTS Settings")
        // onCreate(db)
    }

    /* **************************************** */
    /*               PROFILE PART               */
    /* **************************************** */

    fun addProfile(profileName : String){
        // below we are creating  content values variable
        val values = ContentValues()
        // we are inserting our values in the form of key-value pair
        values.put(NAME, profileName)
        values.put(SWITCH, 0)
        values.put(I1O, 100)
        values.put(I1C, 100)
        values.put(I2O, 100)
        values.put(I2C, 100)
        values.put(I3O, 100)
        values.put(I3C, 100)
        values.put(I4O, 100)
        values.put(I4C, 100)
        values.put(I5O, 100)
        values.put(I5C, 100)
        // here we are creating a writable variable of our database as we want to insert value in our database
        val db = this.writableDatabase
        // all values are inserted into database
        db.insert("Profile", null, values)
        // at last we are closing our database
        db.close()
    }

    fun updateProfileSwitchOpen(profileName : String, I1O : Int, I2O : Int, I3O : Int, I4O : Int, I5O : Int) {
        // below we are creating  content values variable
        val values = ContentValues()
        // we are inserting our values in the form of key-value pair
        values.put(Companion.I1O, I1O)
        values.put(Companion.I2O, I2O)
        values.put(Companion.I3O, I3O)
        values.put(Companion.I4O, I4O)
        values.put(Companion.I5O, I5O)
        // here we are creating a writable variable of our database as we want to insert value in our database
        val db = this.writableDatabase
        db.update("Profile", values, "$NAME = ?", arrayOf(profileName))
    }

    fun updateProfileSwitchClose(profileName : String, I1C : Int, I2C : Int, I3C : Int, I4C : Int, I5C : Int) {
        // below we are creating  content values variable
        val values = ContentValues()
        // we are inserting our values in the form of key-value pair
        values.put(Companion.I1C, I1C)
        values.put(Companion.I2C, I2C)
        values.put(Companion.I3C, I3C)
        values.put(Companion.I4C, I4C)
        values.put(Companion.I5C, I5C)
        // here we are creating a writable variable of our database as we want to insert value in our database
        val db = this.writableDatabase
        db.update("Profile", values, "$NAME = ?", arrayOf(profileName))
    }

    fun switchWindowOpenStatueChange(profileName : String, switchWindowOpen : Boolean) {
        val values = ContentValues()
        // we are inserting our values in the form of key-value pair
        values.put(SWITCH, switchWindowOpen)
        // here we are creating a writable variable of our database as we want to insert value in our database
        val db = this.writableDatabase
        db.update("Profile", values, "$NAME = ?", arrayOf(profileName))
    }

    fun getProfiles(): Cursor? {
        // here we are creating a readable variable of our database as we want to read value from it
        val db = this.readableDatabase
        // below code returns a cursor to read data from the database
        return db.rawQuery("SELECT * FROM Profile", null)
    }

    fun getProfilesNameAndId(): Cursor? {
        // here we are creating a readable variable of our database as we want to read value from it
        val db = this.readableDatabase
        // below code returns a cursor to read data from the database
        return db.rawQuery("SELECT * FROM Profile", null)
    }

    fun deleteProfile(id : Int) {
        val db = this.readableDatabase
        db.delete("Profile", "$ID = ?", arrayOf(id.toString()))
    }

    /* **************************************** */
    /*              SETTINGS PART               */
    /* **************************************** */

    fun updateUnitOfMeasurement(unitOfMeasurement : String) {
        val values = ContentValues()
        // we are inserting our values in the form of key-value pair
        values.put(UOM, unitOfMeasurement)
        // here we are creating a writable variable of our database as we want to insert value in our database
        val db = this.writableDatabase
        db.update("Settings", values, null, null)
    }

    fun updateNightMode(nightMode : String) {
        val values = ContentValues()
        // we are inserting our values in the form of key-value pair
        values.put(NM, nightMode)
        // here we are creating a writable variable of our database as we want to insert value in our database
        val db = this.writableDatabase
        db.update("Settings", values, null, null)
    }

    fun updateGPSDifference(gpsDifference : Int) {
        val values = ContentValues()
        // we are inserting our values in the form of key-value pair
        values.put(GPSD, gpsDifference)
        // here we are creating a writable variable of our database as we want to insert value in our database
        val db = this.writableDatabase
        db.update("Settings", values, null, null)
    }

    fun updateLatestSelectedProfileId(latestSelectedProfileId : Int) {
        val values = ContentValues()
        // we are inserting our values in the form of key-value pair
        values.put(LSPI, latestSelectedProfileId)
        // here we are creating a writable variable of our database as we want to insert value in our database
        val db = this.writableDatabase
        db.update("Settings", values, null, null)
    }

    fun getSettings(): Cursor {
        // here we are creating a readable variable of our database as we want to read value from it
        val db = this.readableDatabase
        // below code returns a cursor at the first row (there's only one row) to read data from the database
        val settings = db.rawQuery("SELECT * FROM Settings", null)
        settings.moveToFirst()
        return settings
    }

    companion object{
        // here we have defined variables for our database

        // below is variable for database name
        private const val DATABASE_NAME = "VELOCITY_VOLUME_DB"

        // below is the variable for database version
        private const val DATABASE_VERSION = 1


        // below is the variable for columns for PROFILE Table
        const val ID = "ProfileID"
        const val NAME = "ProfileName"
        const val SWITCH = "SwitchWindowOpen"
        const val I1O = "Interval1Open"
        const val I1C = "Interval1Close"
        const val I2O = "Interval2Open"
        const val I2C = "Interval2Close"
        const val I3O = "Interval3Open"
        const val I3C = "Interval3Close"
        const val I4O = "Interval4Open"
        const val I4C = "Interval4Close"
        const val I5O = "Interval5Open"
        const val I5C = "Interval5Close"

        // below is the variable for columns for SETTINGS Table
        const val UOM = "UnitOfMeasurement"
        const val NM = "NightMode"
        const val GPSD = "GPSDifference"
        const val LSPI = "LatestSelectedProfileId"
    }
}

/* HOW TO CALL ?

    val vvDB = DBHelper(this, null) // get DBHelper
    val database: SQLiteDatabase = vvDB.readableDatabase // Use this only if you need directly the DB !

    // HERE CALL FUNCTIONS YOU NEED

    vvDB.close()
    database.close()
 */