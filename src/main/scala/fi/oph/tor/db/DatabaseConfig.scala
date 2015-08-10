package fi.oph.tor.db

case class DatabaseConfig(url: String, user: String, password: String)

object DatabaseConfig {
  val localPostgresDatabase = DatabaseConfig("jdbc:postgresql://localhost/tor", "tor", "tor")
}
