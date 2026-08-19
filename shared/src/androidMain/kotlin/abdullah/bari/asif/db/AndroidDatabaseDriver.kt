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
            val bindArgs = args.map { it?.toString() }.toTypedArray()
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
        helper.close()
    }
}

private class AndroidDbRow(private val cursor: Cursor) : DbRow {
    override fun getString(columnName: String): String {
        val idx = cursor.getColumnIndexOrThrow(columnName)
        return cursor.getString(idx) ?: ""
    }

    override fun getStringOrNull(columnName: String): String? {
        val idx = cursor.getColumnIndexOrThrow(columnName)
        return if (cursor.isNull(idx)) null else cursor.getString(idx)
    }

    override fun getLong(columnName: String): Long {
        val idx = cursor.getColumnIndexOrThrow(columnName)
        return cursor.getLong(idx)
    }

    override fun getLongOrNull(columnName: String): Long? {
        val idx = cursor.getColumnIndexOrThrow(columnName)
        return if (cursor.isNull(idx)) null else cursor.getLong(idx)
    }

    override fun getInt(columnName: String): Int {
        val idx = cursor.getColumnIndexOrThrow(columnName)
        return cursor.getInt(idx)
    }
}
