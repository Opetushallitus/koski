package fi.oph.koski.integrationtest

trait EnvVariables {
  def requiredEnv(name: String) = util.Properties.envOrNone(name).orElse(sys.props.get(name)).getOrElse(throw new IllegalStateException("Environment property " + name + " missing"))
}
