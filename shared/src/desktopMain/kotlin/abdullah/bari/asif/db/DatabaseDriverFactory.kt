package abdullah.bari.asif.db

actual class DatabaseDriverFactory {
    actual fun createDriver(): DatabaseDriver {
        return JdbcDatabaseDriver("newscrawler.db")
    }
}
