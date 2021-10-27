package fi.oph.koski.api

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import fi.oph.koski.koskiuser.{AuthenticationUser, KoskiSpecificSession, KäyttöoikeusRepository, MockUsers, Session}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.net.InetAddress.{getByName => inetAddress}

class OpiskeluoikeusPoistoSpec extends AnyFreeSpec with Matchers with OpiskeluoikeusTestMethods with KoskiHttpSpec with BeforeAndAfterAll {
  val application = KoskiApplication.apply
  val käyttöoikeusRepository: KäyttöoikeusRepository = application.käyttöoikeusRepository

  val masterSession: KoskiSpecificSession = KoskiSpecificSession.systemUser
  val virkailijaSession: KoskiSpecificSession = MockUsers.kalle.toKoskiSpecificSession(käyttöoikeusRepository)

  val eeroOid = KoskiSpecificMockOppijat.eero.oid
  val eeroOpiskeluoikeusOid = getOpiskeluoikeudet(KoskiSpecificMockOppijat.eero.oid).head.oid.get
  val eeroSession = sessio(eeroOid)

  val teijaOid = KoskiSpecificMockOppijat.teija.oid
  val teijaSession = sessio(teijaOid)

  val notFound = KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia ("Opiskeluoikeutta ei löydy annetulla oid:llä tai käyttäjällä ei ole siihen oikeuksia")

  override protected def afterAll(): Unit = {
    resetFixtures()
    super.afterAll()
  }

  "Kun voidaan poistaa" - {
    "Kansalainen voi poistaa omansa" in {
      KoskiApplicationForTests.possu.deleteOpiskeluoikeus(eeroOpiskeluoikeusOid)(eeroSession) should equal (HttpStatus.ok)
    }

    "Poiston jälkeen opiskeluoikeutta ei enää löydy" in {
      getOpiskeluoikeudet(KoskiSpecificMockOppijat.eero.oid).length should equal (0)
    }
  }

  "Kun ei voida poistaa" - {
    "Kansalainen ei voi poistaa toisen opiskeluoikeutta" in {
      KoskiApplicationForTests.possu.deleteOpiskeluoikeus(eeroOpiskeluoikeusOid)(teijaSession) should equal (notFound)
    }

    "Opiskeluoikeutta, jota ei ole, ei voi poistaa" in {
      KoskiApplicationForTests.possu.deleteOpiskeluoikeus("feikki-oid")(eeroSession) should equal (notFound)
    }

    "Muut kuin kansalaiset eivät voi poistaa opiskeluoikeuksia" in {
      KoskiApplicationForTests.possu.deleteOpiskeluoikeus(eeroOpiskeluoikeusOid)(masterSession) should equal (notFound)
      KoskiApplicationForTests.possu.deleteOpiskeluoikeus(eeroOpiskeluoikeusOid)(virkailijaSession) should equal (notFound)
    }
  }

  private def sessio(oid: String) = {
    new KoskiSpecificSession(
      AuthenticationUser(
        oid,
        "",
        "",
        None
      ),
      "",
      inetAddress("127.0.0.1"),
      "",
      Set.empty
    )
  }
}
