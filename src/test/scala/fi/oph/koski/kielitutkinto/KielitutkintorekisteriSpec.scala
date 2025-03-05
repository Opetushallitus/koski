package fi.oph.koski.kielitutkinto

import fi.oph.koski.documentation.{ExamplesKielitutkinto, ExamplesLukio}
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, LaajatOppijaHenkilöTiedot}
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{KoskiSpecificSession, MockUsers}
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import fi.oph.koski.oppija.{HenkilönOpiskeluoikeusVersiot, OppijaServletOppijaAdder}
import fi.oph.koski.schema.{KielitutkinnonOpiskeluoikeus, Koodistokoodiviite, LukionOpiskeluoikeus, LähdejärjestelmäId, Oppija}
import fi.oph.koski.util.WithWarnings
import org.json4s.JValue

class KielitutkintorekisteriSpec
  extends AnyFreeSpec
    with KoskiHttpSpec
    with Matchers {

  val oppijaAdder = new OppijaServletOppijaAdder(KoskiApplicationForTests)
  val kielitutkinnonSuorittaja = ExamplesKielitutkinto.exampleMockOppija
  val kielitutkinnonOpiskeluoikeus = jsonOf(ExamplesKielitutkinto.examples.head.data)
  val lukiolainen = KoskiSpecificMockOppijat.lukiolainen
  val lukionOpiskeluoikeus = jsonOf(ExamplesLukio.examples.head.data)

  "Kielitutkintorekisterin käyttöoikeudella" - {
    implicit val session: KoskiSpecificSession = MockUsers.kielitutkintorekisteriKäyttäjä.toKoskiSpecificSession(KoskiApplicationForTests.käyttöoikeusRepository)

    "pystyy kirjoittamaan kielitutkinnon opiskeluoikeuden" in { canWrite(kielitutkinnonOpiskeluoikeus) }
    "pystyy lukemaan kielitutkinnon opiskeluoikeuden" in { canRead(ExamplesKielitutkinto.exampleMockOppija) }
    "ei pysty kirjoittamaan muita opiskeluoikeuksia" in { cannotWrite(lukionOpiskeluoikeus) }
    "ei pysty lukemaan muita opiskeluoikeuksia" in { cannotRead(lukiolainen) }
  }

  "OPH-pääkäyttäjän käyttöoikeudella" - {
    implicit val session: KoskiSpecificSession = MockUsers.paakayttaja.toKoskiSpecificSession(KoskiApplicationForTests.käyttöoikeusRepository)

    "pystyy kirjoittamaan kielitutkinnon opiskeluoikeuden" in { canWrite(kielitutkinnonOpiskeluoikeus) }
    "pystyy lukemaan kielitutkinnon opiskeluoikeuden" in { canRead(ExamplesKielitutkinto.exampleMockOppija) }
  }

  "Oppilaitoksen tavallisella palvelukäyttäjätunnuksella" - {
    implicit val session: KoskiSpecificSession = MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä.toKoskiSpecificSession(KoskiApplicationForTests.käyttöoikeusRepository)

    "ei pysty kirjoittamaan kielitutkinnon opiskeluoikeuden" in { cannotWrite(kielitutkinnonOpiskeluoikeus) }
    "ei pysty lukemaan kielitutkinnon opiskeluoikeuden" in { cannotRead(ExamplesKielitutkinto.exampleMockOppija) }
  }

  private def canWrite(data: JValue)(implicit session: KoskiSpecificSession): Unit =
    write(data) should matchPattern { case Right(_) => }

  private def cannotWrite(data: JValue)(implicit session: KoskiSpecificSession): Unit =
    write(data) should matchPattern { case Left(_) => }

  private def canRead(oppija: LaajatOppijaHenkilöTiedot)(implicit session: KoskiSpecificSession): Unit =
    read(oppija) should matchPattern { case Right(_) => }

  private def cannotRead(oppija: LaajatOppijaHenkilöTiedot)(implicit session: KoskiSpecificSession): Unit =
    read(oppija) should matchPattern { case Left(_) => }

  private def write(data: JValue)(implicit session: KoskiSpecificSession): Either[HttpStatus, HenkilönOpiskeluoikeusVersiot] =
    oppijaAdder.add(session, data, allowUpdate = true, requestDescription = "Test")

  private def read(oppija: LaajatOppijaHenkilöTiedot)(implicit session: KoskiSpecificSession): Either[HttpStatus, WithWarnings[Oppija]] =
    KoskiApplicationForTests.oppijaFacade.findOppija(oppija.oid)

  private def jsonOf(o: Oppija): JValue = JsonSerializer.serializeWithRoot(o.copy(
    opiskeluoikeudet = o.opiskeluoikeudet.map {
      case oo: KielitutkinnonOpiskeluoikeus => oo.copy(lähdejärjestelmänId = Some(LähdejärjestelmäId(
        id = Some("183424"),
        lähdejärjestelmä = Koodistokoodiviite("kielitutkintorekisteri", "lahdejarjestelma")
      )))
      case oo: LukionOpiskeluoikeus => oo.copy(lähdejärjestelmänId = Some(LähdejärjestelmäId(
        id = Some("999999"),
        lähdejärjestelmä = Koodistokoodiviite("kielitutkintorekisteri", "lahdejarjestelma")
      )))
    }
  ))
}
