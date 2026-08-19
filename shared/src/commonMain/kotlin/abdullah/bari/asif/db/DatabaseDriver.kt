package abdullah.bari.asif.db

interface DatabaseDriver {
    fun execute(sql: String, args: List<Any?> = emptyList())
    fun <T> query(sql: String, args: List<Any?> = emptyList(), mapper: (DbRow) -> T): List<T>
    fun close()
}

interface DbRow {
    fun getString(columnName: String): String
    fun getStringOrNull(columnName: String): String?
    fun getLong(columnName: String): Long
    fun getLongOrNull(columnName: String): Long?
    fun getInt(columnName: String): Int
}
