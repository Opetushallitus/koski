package fi.oph.koski.kielitutkinto

import fi.oph.koski.api.misc.PutOpiskeluoikeusTestMethods
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api.actionBasedSQLInterpolation
import fi.oph.koski.db.QueryMethods.runDbSync
import fi.oph.koski.documentation.ExamplesKielitutkinto.{Opiskeluoikeusjakso, YleisetKielitutkinnot, exampleHenkilö, tutkinnonOsa}
import fi.oph.koski.documentation.{ExamplesKielitutkinto, ExamplesLukio}
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, LaajatOppijaHenkilöTiedot}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{KoskiMockUser, KoskiSpecificSession, MockUsers}
import fi.oph.koski.oppija.{HenkilönOpiskeluoikeusVersiot, OppijaServletOppijaAdder}
import fi.oph.koski.organisaatio.{MockOrganisaatiot, Opetushallitus}
import fi.oph.koski.schema._
import fi.oph.koski.util.WithWarnings
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import org.json4s.JValue
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

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
    "ei pysty kirjoittamaan muita opiskeluoikeuksia" in { cannotWrite(lukionOpiskeluoikeus, "Ei oikeuksia opiskeluoikeuden tyyppiin lukiokoulutus (lukionoppimaara)") }
    "ei pysty lukemaan muita opiskeluoikeuksia" in { cannotRead(lukiolainen, s"Oppijaa ${lukiolainen.oid} ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun.") }
  }

  "OPH-pääkäyttäjän käyttöoikeudella" - {
    implicit val session: KoskiSpecificSession = MockUsers.paakayttaja.toKoskiSpecificSession(KoskiApplicationForTests.käyttöoikeusRepository)

    "pystyy kirjoittamaan kielitutkinnon opiskeluoikeuden" in { canWrite(kielitutkinnonOpiskeluoikeus) }
    "pystyy lukemaan kielitutkinnon opiskeluoikeuden" in { canRead(ExamplesKielitutkinto.exampleMockOppija) }
  }

  "Järjestäjäoppilaitoksen palvelukäyttäjätunnuksella" - {
    implicit val session: KoskiSpecificSession = MockUsers.varsinaisSuomiPalvelukäyttäjä.toKoskiSpecificSession(KoskiApplicationForTests.käyttöoikeusRepository)

    "ei pysty kirjoittamaan kielitutkinnon opiskeluoikeuksia, jos oppilaitos on null" in { cannotWrite(kielitutkinnonOpiskeluoikeus, s"Ei oikeuksia organisatioon ${Opetushallitus.organisaatioOid}") }
    "ei pysty kirjoittamaan kielitutkinnon opiskeluoikeuksia, jos oppilaitos on määritelty" in {
      val oo = YleisetKielitutkinnot.opiskeluoikeus(LocalDate.of(2011, 1, 1), "FI", "kt")
      val oo2 = oo.copy(
        oppilaitos = Some(Oppilaitos(MockOrganisaatiot.varsinaisSuomenKansanopisto)),
        suoritukset = oo.suoritukset.map(_.asInstanceOf[YleisenKielitutkinnonSuoritus].copy(
          toimipiste = OidOrganisaatio(MockOrganisaatiot.varsinaisSuomenKansanopistoToimipiste),
          järjestäjä = OidOrganisaatio(MockOrganisaatiot.varsinaisSuomenKansanopistoToimipiste),
        ))
      )
      val oppija = Oppija(exampleHenkilö.copy(hetu = "160586-873P"), Seq(oo2))

      cannotWrite(jsonOf(oppija), s"Yleisen kielitutkinnon opiskeluoikeuden voi tallentaa vain organisaatioille: 1.2.246.562.10.00000000001")
    }
    "ei pysty lukemaan oman organisaation kielitutkinnon opiskeluoikeuksia" in { cannotRead(ExamplesKielitutkinto.exampleMockOppija, s"Oppijaa ${ExamplesKielitutkinto.exampleMockOppija.oid} ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun.") }
  }

  "Raportointikanta" - {
    "Kielitutkinnot eivät siirry raportointikantaan" in {
      val raportointikanta = KoskiApplicationForTests.raportointiDatabase.db
      val koulutusmuodot = runDbSync(raportointikanta, sql"select distinct koulutusmuoto from r_opiskeluoikeus".as[String])

      koulutusmuodot should not contain OpiskeluoikeudenTyyppi.kielitutkinto.koodiarvo
    }
  }

  "Validaatiot" - {
    "Yleinen kielitutkinto" - {
      val tutkintopäivä = LocalDate.of(2025, 1, 15)
      val opiskeluoikeus = ExamplesKielitutkinto.YleisetKielitutkinnot.opiskeluoikeus(
        tutkintopäivä = tutkintopäivä,
        kieli = "FI",
        tutkintotaso = "pt",
      )
      val arviointipäivä = opiskeluoikeus.tila.opiskeluoikeusjaksot.find(_.tila.koodiarvo != "lasna").get.alku

      "Opiskeluoikeuden tila" - {
        "Ei voi siirtää ilman läsnä-tilaa" in {
          val invalidOo = opiskeluoikeus.copy(
            tila = KielitutkinnonOpiskeluoikeudenTila(
              opiskeluoikeusjaksot = List(
                Opiskeluoikeusjakso.valmis(arviointipäivä),
              )
            )
          )

          postOpiskeluoikeus(invalidOo) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.tilaPuuttuu(
              "Kielitutkinnolta puuttuu 'lasna'-tilainen opiskeluoikeuden jakso"
            ))
          }
        }

        "Siirrossa on aina oltava mukana hyvaksytystisuoritettu- tai paattynyt-tila" in {
          val invalidOo = opiskeluoikeus.copy(
            tila = KielitutkinnonOpiskeluoikeudenTila(
              opiskeluoikeusjaksot = List(
                Opiskeluoikeusjakso.tutkintopäivä(tutkintopäivä),
              )
            )
          )

          postOpiskeluoikeus(invalidOo) {
            verifyResponseStatus(400,
              KoskiErrorCategory.badRequest.validation.tila.tilaPuuttuu(
                "Kielitutkinnolta puuttuu 'hyvaksytystisuoritettu' tai 'paattynyt' -tilainen opiskeluoikeuden jakso"
              ),
              KoskiErrorCategory.badRequest.validation.date.päättymispäivämäärä (
                "Arviointipäivä null (hyvaksytystisuoritettu tai paattynyt -tilainen opiskeluoikeusjakso) on eri kuin vahvistuksen päivämäärä 2025-03-16"
              ),
            )
          }
        }

        "hyvaksytystisuoritettu -päivän on vastattava vahvistuksen päivämäärää" in {
          val invalidDate = arviointipäivä.plusDays(1)
          val invalidOo = opiskeluoikeus.copy(
            tila = KielitutkinnonOpiskeluoikeudenTila(
              opiskeluoikeusjaksot = List(
                Opiskeluoikeusjakso.tutkintopäivä(tutkintopäivä),
                Opiskeluoikeusjakso.valmis(invalidDate),
              )
            )
          )

          postOpiskeluoikeus(invalidOo) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.päättymispäivämäärä(
              s"Arviointipäivä $invalidDate (hyvaksytystisuoritettu -tilainen opiskeluoikeusjakso) on eri kuin vahvistuksen päivämäärä $arviointipäivä")
            )
          }
        }
      }

      "Arvioinnit" - {
        "Älä salli perustasolle muita arvosanoja kuin alle 1, 1 ja 2" in {
          val invalidOo = opiskeluoikeus.copy(
            suoritukset = List(
              ExamplesKielitutkinto.YleisetKielitutkinnot.päätasonSuoritus(
                tutkintotaso = "pt",
                kieli = "FI",
                arviointipäivä = arviointipäivä
              ).copy(
                osasuoritukset = Some(List(
                  tutkinnonOsa("tekstinymmartaminen", "alle3", arviointipäivä),
                  tutkinnonOsa("kirjoittaminen", "alle1", arviointipäivä),
                  tutkinnonOsa("puheenymmartaminen", "1", arviointipäivä),
                  tutkinnonOsa("puhuminen", "9", arviointipäivä),
                ))
              )
            )
          )

          postOpiskeluoikeus(invalidOo) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.arviointi.epäsopivaArvosana (
              s"Suoritus ykisuorituksenosa/tekstinymmartaminen sisältää virheellisen arvosanan alle3 (sallitut koodiarvot ovat: alle1, 1, 2, 9, 10, 11)")
            )
          }
        }

        "Älä salli keskitasolle muita arvosanoja kuin alle 3, 3 ja 4" in {
          val invalidOo = opiskeluoikeus.copy(
            suoritukset = List(
              ExamplesKielitutkinto.YleisetKielitutkinnot.päätasonSuoritus(
                tutkintotaso = "kt",
                kieli = "FI",
                arviointipäivä = arviointipäivä
              ).copy(
                osasuoritukset = Some(List(
                  tutkinnonOsa("tekstinymmartaminen", "alle1", arviointipäivä),
                  tutkinnonOsa("kirjoittaminen", "alle3", arviointipäivä),
                  tutkinnonOsa("puheenymmartaminen", "3", arviointipäivä),
                  tutkinnonOsa("puhuminen", "10", arviointipäivä),
                ))
              )
            )
          )

          postOpiskeluoikeus(invalidOo) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.arviointi.epäsopivaArvosana (
              s"Suoritus ykisuorituksenosa/tekstinymmartaminen sisältää virheellisen arvosanan alle1 (sallitut koodiarvot ovat: alle3, 3, 4, 9, 10, 11)")
            )
          }
        }

        "Älä salli ylemmälle tasolle muita arvosanoja kuin alle 5, 5 ja 6" in {
          val invalidOo = opiskeluoikeus.copy(
            suoritukset = List(
              ExamplesKielitutkinto.YleisetKielitutkinnot.päätasonSuoritus(
                tutkintotaso = "yt",
                kieli = "FI",
                arviointipäivä = arviointipäivä
              ).copy(
                osasuoritukset = Some(List(
                  tutkinnonOsa("tekstinymmartaminen", "alle3", arviointipäivä),
                  tutkinnonOsa("kirjoittaminen", "alle5", arviointipäivä),
                  tutkinnonOsa("puheenymmartaminen", "5", arviointipäivä),
                  tutkinnonOsa("puhuminen", "11", arviointipäivä),
                ))
              )
            )
          )

          postOpiskeluoikeus(invalidOo) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.arviointi.epäsopivaArvosana (
              s"Suoritus ykisuorituksenosa/tekstinymmartaminen sisältää virheellisen arvosanan alle3 (sallitut koodiarvot ovat: alle5, 5, 6, 9, 10, 11)")
            )
          }
        }
      }
    }

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
                  alkamispäivä = Some(arviointipäivä),
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
                    alkamispäivä = Some(arviointipäivä),
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

      "Voidaan siirtää arviointia kielitaidolle, myös jos osakoe on hylätty" in {
        val invalidOo = opiskeluoikeus.copy(
          suoritukset = opiskeluoikeus.suoritukset.map {
            case pts: ValtionhallinnonKielitutkinnonSuoritus => pts.copy(
              osasuoritukset = pts.osasuoritukset.map(_.map {
                case kielitaito: ValtionhallinnonKielitutkinnonKirjallisenKielitaidonSuoritus => kielitaito.copy(
                  osasuoritukset = kielitaito.osasuoritukset.map(_.map(_.copy(
                    alkamispäivä = Some(arviointipäivä),
                    arviointi = ExamplesKielitutkinto.ValtionhallinnonKielitutkinnot.Kielitaidot.arviointi("hylatty", arviointipäivä)
                  )))
                )
              })
            )
          }
        )

        postOpiskeluoikeus(invalidOo) {
          verifyResponseStatusOk()
        }
      }
    }
  }

  private def canWrite(data: JValue)(implicit session: KoskiSpecificSession): Unit =
    write(data) should matchPattern { case Right(_) => }

  private def cannotWrite(data: JValue, expectedError: String)(implicit session: KoskiSpecificSession): Unit =
    write(data) should matchPattern { case Left(a) if isError(a, expectedError) => }

  private def canRead(oppija: LaajatOppijaHenkilöTiedot)(implicit session: KoskiSpecificSession): Unit =
    read(oppija) should matchPattern { case Right(_) => }

  private def cannotRead(oppija: LaajatOppijaHenkilöTiedot, expectedError: String)(implicit session: KoskiSpecificSession): Unit =
    read(oppija) should matchPattern { case Left(a) if isError(a, expectedError) => }

  private def isError(a: Any, expectedMessage: String): Boolean = a match {
    case s: HttpStatus => s.errorString.contains(expectedMessage)
    case _ => false
  }

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
  override def defaultUser: KoskiMockUser = MockUsers.paakayttaja
}
