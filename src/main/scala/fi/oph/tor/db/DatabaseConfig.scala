package fi.oph.tor.db

case class DatabaseConfig(url: String, user: String, password: String)

object DatabaseConfig {
  val localDatabase = DatabaseConfig("jdbc:postgresql://localhost/tor", "tor", "tor")
  val localTestDatabase = DatabaseConfig("jdbc:postgresql://localhost/tortest", "tor", "tor")
}
