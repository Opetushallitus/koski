package fi.oph.tor.db

object CodeGenerator extends App with GlobalExecutionContext {
  val config = DatabaseConfig.localTestDatabase

  TorDatabase.init(config)

  slick.codegen.SourceCodeGenerator.main(
    Array("slick.driver.PostgresDriver", "org.postgresql.Driver", config.url, "src/main/scala", "fi.oph.tor.db", config.user, config.password)
  )
}
