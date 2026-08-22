package abdullah.bari.asif.db

import java.sql.Connection
import java.sql.DriverManager
import java.sql.PreparedStatement
import java.sql.ResultSet

class JdbcDatabaseDriver(dbPath: String = "newscrawler.db") : DatabaseDriver {

    private val jdbcUrl: String = when {
        dbPath.startsWith("jdbc:") -> dbPath
        dbPath == ":memory:" -> "jdbc:sqlite::memory:"
        else -> "jdbc:sqlite:$dbPath"
    }

    private val connection: Connection by lazy {
        DriverManager.getConnection(jdbcUrl)
    }

    override fun execute(sql: String, args: List<Any?>) {
        connection.prepareStatement(sql).use { stmt ->
            bindArgs(stmt, args)
            stmt.executeUpdate()
        }
    }

    override fun <T> query(sql: String, args: List<Any?>, mapper: (DbRow) -> T): List<T> {
        val result = mutableListOf<T>()
        connection.prepareStatement(sql).use { stmt ->
            bindArgs(stmt, args)
            stmt.executeQuery().use { rs ->
                val row = JdbcDbRow(rs)
                while (rs.next()) {
                    result.add(mapper(row))
                }
            }
        }
        return result
    }

    private fun bindArgs(stmt: PreparedStatement, args: List<Any?>) {
        args.forEachIndexed { index, arg ->
            val paramIndex = index + 1
            when (arg) {
                null -> stmt.setNull(paramIndex, java.sql.Types.NULL)
                is String -> stmt.setString(paramIndex, arg)
                is Long -> stmt.setLong(paramIndex, arg)
                is Int -> stmt.setInt(paramIndex, arg)
                is Double -> stmt.setDouble(paramIndex, arg)
                is Boolean -> stmt.setInt(paramIndex, if (arg) 1 else 0)
                else -> stmt.setString(paramIndex, arg.toString())
            }
        }
    }

    override fun close() {
        if (!connection.isClosed) {
            connection.close()
        }
    }
}

private class JdbcDbRow(private val rs: ResultSet) : DbRow {
    override fun getString(columnName: String): String {
        return rs.getString(columnName) ?: ""
    }

    override fun getStringOrNull(columnName: String): String? {
        return rs.getString(columnName)
    }

    override fun getLong(columnName: String): Long {
        return rs.getLong(columnName)
    }

    override fun getLongOrNull(columnName: String): Long? {
        val value = rs.getLong(columnName)
        return if (rs.wasNull()) null else value
    }

    override fun getInt(columnName: String): Int {
        return rs.getInt(columnName)
    }
}
