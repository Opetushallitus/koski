package fi.oph.tor.db

object CodeGenerator extends App {
  val config = DatabaseConfig.localPostgresDatabase

  slick.codegen.SourceCodeGenerator.main(
    Array("slick.driver.PostgresDriver", "org.postgresql.Driver", config.url, "src/main/scala", "fi.oph.tor.db", config.user, config.password)
  )
}
