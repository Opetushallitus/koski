package fi.oph.koski.kielitutkinto

import fi.oph.koski.api.misc.{OpiskeluoikeusTestMethods, PutOpiskeluoikeusTestMethods}
import fi.oph.koski.documentation.{ExamplesKielitutkinto, ExamplesLukio}
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, LaajatOppijaHenkilöTiedot}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{KoskiSpecificSession, MockUsers}
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import fi.oph.koski.oppija.{HenkilönOpiskeluoikeusVersiot, OppijaServletOppijaAdder}
import fi.oph.koski.schema.{KielitutkinnonOpiskeluoikeus, Koodistokoodiviite, LukionOpiskeluoikeus, LähdejärjestelmäId, Oppija, ValtionhallinnonKielitutkinnonArviointi, ValtionhallinnonKielitutkinnonKirjallisenKielitaidonSuoritus, ValtionhallinnonKielitutkinnonSuoritus, YlioppilastutkinnonOpiskeluoikeus}
import fi.oph.koski.util.WithWarnings
import org.json4s.JValue

import java.time.LocalDate
import scala.reflect.runtime.universe

class KielitutkintorekisteriSpec
  extends AnyFreeSpec
    with KoskiHttpSpec
    with Matchers
    with PutOpiskeluoikeusTestMethods[KielitutkinnonOpiskeluoikeus] {



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

  "Raportointikanta" - {
    "Kielitutkinnot eivät siirry raportointikantaan" in {
      val oppija = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja
      implicit val session: KoskiSpecificSession = MockUsers.paakayttaja.toKoskiSpecificSession(KoskiApplicationForTests.käyttöoikeusRepository)
      val rows = KoskiApplicationForTests
        .opiskeluoikeusRepository
        .findByOppija(oppija, useVirta = false, useYtr = false)
        .getIgnoringWarnings
        .toList

      rows should equal(List.empty)
    }
  }

  "Validaatiot" - {
    "Valtionhallinnon kielitutkinto" - {
      val arviointipäivä = LocalDate.of(2025, 1, 15)
      val opiskeluoikeus = ExamplesKielitutkinto.ValtionhallinnonKielitutkinnot.Opiskeluoikeus.valmis(
        tutkintopäivä = LocalDate.of(2025, 1, 1),
        kieli = "FI",
        kielitaidot = List("kirjallinen"),
        tutkintotaso = "hyvajatyydyttava",
      )

      "Ei voi siirtää väärän taitotason mukaisella kielitaidon arvioinnilla" in {
        val invalidOo = opiskeluoikeus.copy(
          suoritukset = opiskeluoikeus.suoritukset.map {
            case pts: ValtionhallinnonKielitutkinnonSuoritus => pts.copy(
              osasuoritukset = pts.osasuoritukset.map(_.map {
                case kielitaito: ValtionhallinnonKielitutkinnonKirjallisenKielitaidonSuoritus => kielitaito.copy(
                  arviointi = ExamplesKielitutkinto.ValtionhallinnonKielitutkinnot.Kielitaidot.arviointi("erinomainen", arviointipäivä)
                )
              })
            )
          }
        )

        postOpiskeluoikeus(invalidOo) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.arviointi.epäsopivaArvosana(
            "Suoritus vktkielitaito/kirjallinen sisältää virheellisen arvosanan erinomainen (sallitut koodiarvot ovat: hyva, tyydyttava, hylatty)"
          ))
        }
      }

      "Ei voi siirtää väärän taitotason mukaisella osakokeen arvioinnilla" in {
        val invalidOo = opiskeluoikeus.copy(
          suoritukset = opiskeluoikeus.suoritukset.map {
            case pts: ValtionhallinnonKielitutkinnonSuoritus => pts.copy(
              osasuoritukset = pts.osasuoritukset.map(_.map {
                case kielitaito: ValtionhallinnonKielitutkinnonKirjallisenKielitaidonSuoritus => kielitaito.copy(
                  osasuoritukset = kielitaito.osasuoritukset.map(_.map(_.copy(
                    arviointi = ExamplesKielitutkinto.ValtionhallinnonKielitutkinnot.Kielitaidot.arviointi("erinomainen", arviointipäivä)
                  )))
                )
              })
            )
          }
        )

        postOpiskeluoikeus(invalidOo) {
          verifyResponseStatus(400,
            KoskiErrorCategory.badRequest.validation.arviointi.epäsopivaArvosana(
              "Suoritus vktosakoe/kirjoittaminen sisältää virheellisen arvosanan erinomainen (sallitut koodiarvot ovat: hyva, tyydyttava, hylatty)"
            ),
            KoskiErrorCategory.badRequest.validation.arviointi.epäsopivaArvosana(
              "Suoritus vktosakoe/tekstinymmartaminen sisältää virheellisen arvosanan erinomainen (sallitut koodiarvot ovat: hyva, tyydyttava, hylatty)"
            ),
          )
        }
      }

      "Ei voi siirtää arviointia kielitaidolle, jos osakoe on hylätty" in {
        val invalidOo = opiskeluoikeus.copy(
          suoritukset = opiskeluoikeus.suoritukset.map {
            case pts: ValtionhallinnonKielitutkinnonSuoritus => pts.copy(
              osasuoritukset = pts.osasuoritukset.map(_.map {
                case kielitaito: ValtionhallinnonKielitutkinnonKirjallisenKielitaidonSuoritus => kielitaito.copy(
                  osasuoritukset = kielitaito.osasuoritukset.map(_.map(_.copy(
                    arviointi = ExamplesKielitutkinto.ValtionhallinnonKielitutkinnot.Kielitaidot.arviointi("hylatty", arviointipäivä)
                  )))
                )
              })
            )
          }
        )

        postOpiskeluoikeus(invalidOo) {
          verifyResponseStatus(400,
            KoskiErrorCategory.badRequest.validation.tila.keskeneräinenOsasuoritus(
              "Valmiiksi merkityllä suorituksella vkttutkintotaso/hyvajatyydyttava on keskeneräinen osasuoritus vktosakoe/kirjoittaminen"
            ),
            KoskiErrorCategory.badRequest.validation.tila.keskeneräinenOsasuoritus(
              "Valmiiksi merkityllä suorituksella vktkielitaito/kirjallinen on keskeneräinen osasuoritus vktosakoe/kirjoittaminen"
            ),
          )
        }
      }
    }
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

  def tag: universe.TypeTag[KielitutkinnonOpiskeluoikeus] = implicitly[reflect.runtime.universe.TypeTag[KielitutkinnonOpiskeluoikeus]]
  def defaultOpiskeluoikeus: KielitutkinnonOpiskeluoikeus = ExamplesKielitutkinto.YleisetKielitutkinnot.opiskeluoikeus(LocalDate.of(2025, 1, 1), "FI", "pt")
}
