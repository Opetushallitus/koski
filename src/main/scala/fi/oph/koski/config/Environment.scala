package fi.oph.koski.config

import fi.oph.koski.util.Files

object Environment {
  def isLocalDevelopmentEnvironment = Files.exists("Makefile")
}
