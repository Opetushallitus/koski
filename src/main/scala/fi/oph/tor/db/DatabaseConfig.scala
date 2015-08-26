package fi.oph.tor.db

case class DatabaseConfig(host: String, port: Int, databaseName: String, user: String, password: String) {
  def url = "jdbc:postgresql://" + host + ":" + port + "/" + databaseName
  def isRemote = host != "localhost"
}

object DatabaseConfig {
  val localDatabase = DatabaseConfig("localhost", 5432, "tor", "tor", "tor")
  val localTestDatabase = DatabaseConfig("localhost", 5432, "tortest", "tor", "tor")
}
