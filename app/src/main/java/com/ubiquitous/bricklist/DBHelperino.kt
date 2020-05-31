package com.ubiquitous.bricklist

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.io.File
import java.io.FileOutputStream

class DBHelperino(val context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val preferences: SharedPreferences = context.getSharedPreferences(
        "${context.packageName}.database_versions",
        Context.MODE_PRIVATE
    )

    private fun installedDatabaseIsOutdated(): Boolean {
        return preferences.getInt(DATABASE_NAME, 0) < DATABASE_VERSION
    }

    private fun writeDatabaseVersionInPreferences() {
        preferences.edit().apply {
            putInt(DATABASE_NAME, DATABASE_VERSION)
            apply()
        }
    }

    private fun installDatabaseFromAssets() {
        val inputStream = context.assets.open("$ASSETS_PATH/$DATABASE_NAME.db")

        try {
            val outputFile = File(context.getDatabasePath(DATABASE_NAME).path)
            val outputStream = FileOutputStream(outputFile)

            inputStream.copyTo(outputStream)
            inputStream.close()

            outputStream.flush()
            outputStream.close()
        } catch (exception: Throwable) {
            throw RuntimeException("The $DATABASE_NAME database couldn't be installed.", exception)
        }
    }

    @Synchronized
    private fun installOrUpdateIfNecessary() {
        if (installedDatabaseIsOutdated()) {
            context.deleteDatabase(DATABASE_NAME)
            installDatabaseFromAssets()
            writeDatabaseVersionInPreferences()
        }
    }

    override fun getWritableDatabase(): SQLiteDatabase {
        installOrUpdateIfNecessary()
        return super.getWritableDatabase()
    }

    override fun getReadableDatabase(): SQLiteDatabase {
        installOrUpdateIfNecessary()
        return super.getReadableDatabase()
    }

    override fun onCreate(db: SQLiteDatabase?) {
        // Nothing to do
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        // Nothing to do
    }

    fun getLastID(tablename: String): Int {
        val db = this.readableDatabase
        val query = "SELECT id FROM $tablename ORDER BY id DESC"
        val cursor = db.rawQuery(query, null)
        var id = 0
        if (cursor.moveToFirst()) { // Has records
            id = cursor.getInt(0)
        }
        cursor.close()
        db.close()
        return id
    }

    fun findCodeByID(id: Int): Code? {
        val db = this.readableDatabase
        val query = "SELECT * FROM codes WHERE id = $id"
        val cursor = db.rawQuery(query, null)
        var code: Code? = null
        if (cursor.moveToFirst()) {
            if (cursor.isNull(4)) {
                code = Code(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getInt(2),
                    cursor.getInt(3)
                )
            }
            else {
                code = Code(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getInt(2),
                    cursor.getInt(3),
                    cursor.getBlob(4)
                )
            }
        }
        cursor.close()
        db.close()
        return code
    }

    fun findCode(itemID: Int, colorID: Int): Code? {
        val db = this.readableDatabase
        val query = "SELECT * FROM codes WHERE itemid = $itemID AND colorid = $colorID"
        val cursor = db.rawQuery(query, null)
        var code: Code? = null
        if (cursor.moveToFirst()) {
            if (cursor.isNull(4)) {
                code = Code(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getInt(2),
                    cursor.getInt(3)
                )
            }
            else {
                code = Code(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getInt(2),
                    cursor.getInt(3),
                    cursor.getBlob(4)
                )
            }
        }
        cursor.close()
        db.close()
        return code
    }

    fun findColorByID(id: Int): Color? {
        val db = this.readableDatabase
        val query = "SELECT * FROM colors WHERE id = $id"
        val cursor = db.rawQuery(query, null)
        var color: Color? = null
        if (cursor.moveToFirst()) {
            if (cursor.isNull(3)) {
                color =
                    Color(cursor.getInt(0), cursor.getInt(1), cursor.getString(2))
            }
            else {
                color =
                    Color(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(3))
            }
        }
        cursor.close()
        db.close()
        return color
    }

    fun findColor(colorCode: Int): Color? {
        val db = this.readableDatabase
        val query = "SELECT * FROM colors WHERE code = $colorCode"
        val cursor = db.rawQuery(query, null)
        var color: Color? = null
        if (cursor.moveToFirst()) {
            if (cursor.isNull(3)) {
                color =
                    Color(cursor.getInt(0), cursor.getInt(1), cursor.getString(2))
            }
            else {
                color =
                    Color(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(3))
            }
        }
        cursor.close()
        db.close()
        return color
    }

    fun findInventoryByID(id: Int): Inventory? {
        val db = this.readableDatabase
        val query = "SELECT * FROM inventories WHERE id = $id"
        val cursor = db.rawQuery(query, null)
        var inventory: Inventory? = null
        if (cursor.moveToFirst()) {
            inventory = Inventory(cursor.getInt(0), cursor.getString(1), cursor.getInt(2), cursor.getInt(3))
        }
        cursor.close()
        db.close()
        return inventory
    }

    fun findInventoryPartByID(id: Int): InventoryPart? {
        val db = this.readableDatabase
        val query = "SELECT * FROM inventoriesparts WHERE id = $id"
        val cursor = db.rawQuery(query, null)
        var inventoryPart: InventoryPart? = null
        if (cursor.moveToFirst()) {
            inventoryPart = InventoryPart(
                cursor.getInt(0),
                cursor.getInt(1),
                cursor.getInt(2),
                cursor.getInt(3),
                cursor.getInt(4),
                cursor.getInt(5),
                cursor.getInt(6),
                cursor.getInt(7)
            )
        }
        cursor.close()
        db.close()
        return inventoryPart
    }

    fun findInventoryPart(itemID: Int, colorID: Int, invetoryID: Int): InventoryPart? {
        val db = this.readableDatabase
        val query = "SELECT * FROM inventoriesparts WHERE itemid = $itemID AND colorid = $colorID AND inventoryid = $invetoryID"
        val cursor = db.rawQuery(query, null)
        var inventoryPart: InventoryPart? = null
        if (cursor.moveToFirst()) {
            inventoryPart = InventoryPart(
                cursor.getInt(0),
                cursor.getInt(1),
                cursor.getInt(2),
                cursor.getInt(3),
                cursor.getInt(4),
                cursor.getInt(5),
                cursor.getInt(6),
                cursor.getInt(7)
            )
        }
        cursor.close()
        db.close()
        return inventoryPart
    }

    fun findItemTypeByID(id: Int): ItemType? {
        val db = this.readableDatabase
        val query = "SELECT * FROM itemtypes WHERE id = $id"
        val cursor = db.rawQuery(query, null)
        var itemType: ItemType? = null
        if (cursor.moveToFirst()) {
            if (cursor.isNull(3)) {
                itemType = ItemType(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2)
                )
            }
            else {
                itemType = ItemType(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3)
                )
            }
        }
        cursor.close()
        db.close()
        return itemType
    }

    fun findItemType(code: String): ItemType? {
        val db = this.readableDatabase
        val query = "SELECT * FROM itemtypes WHERE code = '$code'"
        val cursor = db.rawQuery(query, null)
        var itemType: ItemType? = null
        if (cursor.moveToFirst()) {
            if (cursor.isNull(3)) {
                itemType = ItemType(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2)
                )
            }
            else {
                itemType = ItemType(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3)
                )
            }
        }
        cursor.close()
        db.close()
        return itemType
    }

    fun findPartByID(id: Int): Part? {
        val db = this.readableDatabase
        val query = "SELECT * FROM parts WHERE id = $id"
        val cursor = db.rawQuery(query, null)
        var part: Part? = null
        if (cursor.moveToFirst()) {
            if (cursor.isNull(4)) {
                part = Part(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getInt(5)
                )
            }
            else {
                part = Part(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getInt(5)
                )
            }
        }
        cursor.close()
        db.close()
        return part
    }

    fun findPart(code: String): Part? {
        val db = this.readableDatabase
        val query = "SELECT * FROM parts WHERE code = '$code'"
        val cursor = db.rawQuery(query, null)
        var part: Part? = null
        if (cursor.moveToFirst()) {
            if (cursor.isNull(4)) {
                part = Part(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getInt(5)
                )
            }
            else {
                part = Part(
                    cursor.getInt(0),
                    cursor.getInt(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    cursor.getInt(5)
                )
            }
        }
        cursor.close()
        db.close()
        return part
    }

    fun addInventory(inventory: Inventory) {
        val values = ContentValues()
        values.put("id", inventory.id)
        values.put("name", inventory.name)
        values.put("active", inventory.active)
        values.put("lastaccessed", inventory.lastAccessed)
        val db = this.writableDatabase
        db.insert("inventories", null, values)
        db.close()
    }

    fun addInventoryPart(inventoryPart: InventoryPart) {
        val values = ContentValues()
        values.put("id", inventoryPart.id)
        values.put("inventoryid", inventoryPart.inventoryID)
        values.put("typeid", inventoryPart.typeID)
        values.put("itemid", inventoryPart.itemID)
        values.put("quantityinset", inventoryPart.quantityInSet)
        values.put("quantityinstore", inventoryPart.quantityInStore)
        values.put("colorid", inventoryPart.colorID)
        values.put("extra", inventoryPart.extra)
        val db = this.writableDatabase
        db.insert("inventoriesparts", null, values)
        db.close()
    }

    fun editCode(code: Code) {
        val values = ContentValues()
        values.put("itemid", code.itemID)
        values.put("colorid", code.colorID)
        values.put("code", code.code)
        values.put("image", code.image)
        val db = this.writableDatabase
        db.update("codes", values, "id="+code.id, null)
        db.close()
    }

    fun editInventoryPart(inventoryPart: InventoryPart) {
        val values = ContentValues()
        values.put("id", inventoryPart.id)
        values.put("inventoryid", inventoryPart.inventoryID)
        values.put("typeid", inventoryPart.typeID)
        values.put("itemid", inventoryPart.itemID)
        values.put("quantityinstore", inventoryPart.quantityInStore)
        values.put("quantityinset", inventoryPart.quantityInSet)
        values.put("colorid", inventoryPart.colorID)
        values.put("extra", inventoryPart.extra)
        val db = this.writableDatabase
        db.update("inventoriesparts", values, "id="+inventoryPart.id, null)
        db.close()
    }

    companion object {
        const val ASSETS_PATH = "databases"
        const val DATABASE_NAME = "BrickList"
        const val DATABASE_VERSION = 1
    }

}