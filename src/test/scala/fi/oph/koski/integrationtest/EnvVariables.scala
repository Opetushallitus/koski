package fi.oph.koski.integrationtest

trait EnvVariables {
  def requiredEnv(name: String): String = optEnv(name).getOrElse(throw new IllegalStateException("Environment property " + name + " missing"))
  def env(name: String, defaultValue: String): String = optEnv(name).getOrElse(defaultValue)
  def optEnv(name: String): Option[String] = util.Properties.envOrNone(name).orElse(sys.props.get(name))
}

