package abdullah.bari.asif.db

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AndroidDatabaseDriver(
    context: Context,
    dbName: String = "newscrawler.db"
) : DatabaseDriver {

    private val helper = object : SQLiteOpenHelper(context, dbName, null, 1) {
        override fun onCreate(db: SQLiteDatabase) {}
        override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}
    }

    private val db: SQLiteDatabase by lazy { helper.writableDatabase }

    override fun execute(sql: String, args: List<Any?>) {
        if (args.isEmpty()) {
            db.execSQL(sql)
        } else {
            val bindArgs = args.toTypedArray()
            db.execSQL(sql, bindArgs)
        }
    }

    override fun <T> query(sql: String, args: List<Any?>, mapper: (DbRow) -> T): List<T> {
        val bindArgs = if (args.isEmpty()) null else args.map { it?.toString() }.toTypedArray()
        val cursor: Cursor = db.rawQuery(sql, bindArgs)
        val result = mutableListOf<T>()
        cursor.use { c ->
            val row = AndroidDbRow(c)
            while (c.moveToNext()) {
                result.add(mapper(row))
            }
        }
        return result
    }

    override fun close() {
        try {
            helper.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

private class AndroidDbRow(private val cursor: Cursor) : DbRow {
    private fun getColumnIndexSafe(columnName: String): Int {
        var idx = cursor.getColumnIndex(columnName)
        if (idx >= 0) return idx
        idx = cursor.getColumnIndex(columnName.lowercase())
        if (idx >= 0) return idx
        idx = cursor.getColumnIndex(columnName.uppercase())
        if (idx >= 0) return idx
        return cursor.getColumnIndexOrThrow(columnName)
    }

    override fun getString(columnName: String): String {
        val idx = getColumnIndexSafe(columnName)
        return cursor.getString(idx) ?: ""
    }

    override fun getStringOrNull(columnName: String): String? {
        val idx = getColumnIndexSafe(columnName)
        return if (cursor.isNull(idx)) null else cursor.getString(idx)
    }

    override fun getLong(columnName: String): Long {
        val idx = getColumnIndexSafe(columnName)
        return cursor.getLong(idx)
    }

    override fun getLongOrNull(columnName: String): Long? {
        val idx = getColumnIndexSafe(columnName)
        return if (cursor.isNull(idx)) null else cursor.getLong(idx)
    }

    override fun getInt(columnName: String): Int {
        val idx = getColumnIndexSafe(columnName)
        return cursor.getInt(idx)
    }
}
