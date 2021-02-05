package fi.oph.koski.fixture

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db._
import fi.oph.koski.henkilo.OppijaHenkilö
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{AccessType, KoskiSession}
import fi.oph.koski.schema._
import fi.oph.koski.util.Timing

import scala.reflect.runtime.universe.TypeTag

abstract class KoskiDatabaseFixtureCreator(application: KoskiApplication) extends KoskiDatabaseMethods with Timing {
  implicit val user = KoskiSession.systemUser
  protected val validator = application.validator
  val database = application.masterDatabase
  val db = database.db
  implicit val accessType = AccessType.write

  protected def validateOpiskeluoikeus[T: TypeTag](oo: T, session: KoskiSession = user): T =
    validator.extractAndValidateOpiskeluoikeus(JsonSerializer.serialize(oo))(session, AccessType.write) match {
      case Right(opiskeluoikeus) => opiskeluoikeus.asInstanceOf[T]
      case Left(status) => throw new RuntimeException("Fixture insert failed for " + JsonSerializer.write(oo) + ": " + status)
    }

  protected lazy val opiskeluoikeudet: List[(OppijaHenkilö, KoskeenTallennettavaOpiskeluoikeus)] = validatedOpiskeluoikeudet ++ invalidOpiskeluoikeudet

  protected def resetFixtures: Unit

  protected def validatedOpiskeluoikeudet: List[(OppijaHenkilö, KoskeenTallennettavaOpiskeluoikeus)]

  protected def invalidOpiskeluoikeudet: List[(OppijaHenkilö, KoskeenTallennettavaOpiskeluoikeus)]
}
