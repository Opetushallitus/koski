package fi.oph.koski.api.oppijavalidation

import com.typesafe.config.Config
import com.typesafe.config.ConfigValueFactory.fromAnyRef
import fi.oph.koski.api.misc.PutOpiskeluoikeusTestMethods
import fi.oph.koski.documentation.ExamplesEuropeanSchoolOfHelsinki.alkamispäivä
import fi.oph.koski.documentation.{ExampleData, ExamplesEB, ExamplesEuropeanSchoolOfHelsinki, LukioExampleData}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession}
import fi.oph.koski.schema._
import fi.oph.koski.util.FinnishDateFormat.finnishDateFormat
import fi.oph.koski.validation.KoskiValidator
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import org.scalatest.freespec.AnyFreeSpec

import java.time.LocalDate

class OppijaValidationEBTutkintoSpec
  extends AnyFreeSpec
    with KoskiHttpSpec
    with PutOpiskeluoikeusTestMethods[EBOpiskeluoikeus]
{
  override def tag = implicitly[reflect.runtime.universe.TypeTag[EBOpiskeluoikeus]]

  override def defaultOpiskeluoikeus = ExamplesEB.opiskeluoikeus

  private val eshHenkilö = KoskiSpecificMockOppijat.europeanSchoolOfHelsinki

  "Example-opiskeluoikeus voidaan kirjoittaa tietokantaan" in {
    setupOppijaWithOpiskeluoikeus(ExamplesEuropeanSchoolOfHelsinki.opiskeluoikeus, henkilö = eshHenkilö) {
      verifyResponseStatusOk()
    }

    putOpiskeluoikeus(defaultOpiskeluoikeus, henkilö = eshHenkilö) {
      verifyResponseStatusOk()
    }
  }

  "Koulutustyyppi" - {
    "Täydennetään" in {
      setupOppijaWithOpiskeluoikeus(ExamplesEuropeanSchoolOfHelsinki.opiskeluoikeus, henkilö = eshHenkilö) {
        verifyResponseStatusOk()
      }

      val putOo = defaultOpiskeluoikeus.copy(
        suoritukset = List(
          ExamplesEB.eb.copy(
            koulutusmoduuli = EBTutkinto().copy(koulutustyyppi = None)
          )
        )
      )

      koulutustyypit(putOo) should be(List.empty)

      val oo = putAndGetOpiskeluoikeus(putOo, henkilö = eshHenkilö)

      koulutustyypit(oo) should be(List("21"))
    }

    def koulutustyypit(oo: EBOpiskeluoikeus): List[String] = {
      oo.suoritukset.flatMap(_.koulutusmoduuli.koulutustyyppi).map(_.koodiarvo)
    }
  }

  "EB-tutkinnon opiskeluoikeuden linkitys opiskeluoikeutta luotaessa" - {
    "EB-opiskeluoikeutta ei voi luoda tai muokata, jos oppijalla ei ole ESH-opiskeluoikeutta, jossa S7" in {
      val oppija = KoskiSpecificMockOppijat.eero

      val eshIlmanS7 = ExamplesEuropeanSchoolOfHelsinki.opiskeluoikeus.copy(
        suoritukset = ExamplesEuropeanSchoolOfHelsinki.opiskeluoikeus.suoritukset.filterNot(_.koulutusmoduuli.tunniste.koodiarvo == "S7")
      )

      setupOppijaWithOpiskeluoikeus(eshIlmanS7, henkilö = oppija) {
        verifyResponseStatusOk()
      }

      putOpiskeluoikeus(defaultOpiskeluoikeus, henkilö = oppija) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.eb.puuttuvaESHS7("EB-tutkinnon opiskeluoikeutta ei voi kirjata, jos oppijalla ei ole European School of Helsinki -opiskeluoikeutta, jossa S7-suoritus"))
      }
    }

    "EB-tutkintoa ei voi luoda uudelle oppijalle" in {
      val oppija = KoskiSpecificMockOppijat.tyhjä

      setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus, henkilö = oppija) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.eb.puuttuvaESHS7("EB-tutkinnon opiskeluoikeutta ei voi kirjata, jos oppijalla ei ole European School of Helsinki -opiskeluoikeutta, jossa S7-suoritus"))
      }
    }

    "Tunnistetaan myös eri oppija-oidilla oleva ESH-opiskeluoikeus, jossa S7" in {
      val eshOppija = KoskiSpecificMockOppijat.slave.henkilö

      setupOppijaWithOpiskeluoikeus(ExamplesEuropeanSchoolOfHelsinki.opiskeluoikeus, henkilö = eshOppija) {
        verifyResponseStatusOk()
      }

      val ebOppija = KoskiSpecificMockOppijat.master

      putOpiskeluoikeus(defaultOpiskeluoikeus, henkilö = ebOppija) {
        verifyResponseStatusOk()
      }
    }
  }

  "Päätason suorituksen vahvistus EB-tutkinnossa" - {
    "Ei voi tehdä, jos ei ole final markkia" in {
      setupOppijaWithOpiskeluoikeus(ExamplesEuropeanSchoolOfHelsinki.opiskeluoikeus, henkilö = eshHenkilö) {
        verifyResponseStatusOk()
      }

      val oo = defaultOpiskeluoikeus.copy(
        tila = EBOpiskeluoikeudenTila(
          List(
            EBOpiskeluoikeusjakso(ExamplesEB.alkamispäivä, LukioExampleData.opiskeluoikeusAktiivinen),
          )
        ),
        suoritukset = List(ExamplesEB.eb.copy(
          osasuoritukset = Some(List(
            EBTutkinnonOsasuoritus(
              koulutusmoduuli =  EuropeanSchoolOfHelsinkiMuuOppiaine(
                Koodistokoodiviite("MA", "europeanschoolofhelsinkimuuoppiaine"),
                laajuus = LaajuusVuosiviikkotunneissa(4)
              ),
              suorituskieli = ExampleData.englanti,
              osasuoritukset = Some(List(
                EBOppiaineenAlaosasuoritus(
                  koulutusmoduuli = EBOppiaineKomponentti(
                    tunniste = Koodistokoodiviite("Written", "ebtutkinnonoppiaineenkomponentti")
                  ),
                  arviointi = ExamplesEB.ebTutkintoFinalMarkArviointi(päivä = ExamplesEB.alkamispäivä.plusMonths(3))
                ),
              ))
            ),
          ))
        ))
      )

      putOpiskeluoikeus(oo, henkilö = eshHenkilö) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.valmiiksiMerkityltäPuuttuuOsasuorituksia("Suoritus koulutus/301104 on merkitty valmiiksi, mutta sillä on tyhjä osasuorituslista tai joltain sen osasuoritukselta puuttuu vaadittava arvioitu Final-osasuoritus, tai opiskeluoikeudelta puuttuu linkitys"))
      }
    }

    "Ei voi tehdä, jos on pelkkä final ilman arviointia" in {
      setupOppijaWithOpiskeluoikeus(ExamplesEuropeanSchoolOfHelsinki.opiskeluoikeus, henkilö = eshHenkilö) {
        verifyResponseStatusOk()
      }

      val oo = defaultOpiskeluoikeus.copy(
        tila = EBOpiskeluoikeudenTila(
          List(
            EBOpiskeluoikeusjakso(ExamplesEB.alkamispäivä, LukioExampleData.opiskeluoikeusAktiivinen),
          )
        ),
        suoritukset = List(ExamplesEB.eb.copy(
          osasuoritukset = Some(List(
            EBTutkinnonOsasuoritus(
              koulutusmoduuli =  EuropeanSchoolOfHelsinkiMuuOppiaine(
                Koodistokoodiviite("MA", "europeanschoolofhelsinkimuuoppiaine"),
                laajuus = LaajuusVuosiviikkotunneissa(4)
              ),
              suorituskieli = ExampleData.englanti,
              osasuoritukset = Some(List(
                EBOppiaineenAlaosasuoritus(
                  koulutusmoduuli = EBOppiaineKomponentti(
                    tunniste = Koodistokoodiviite("Final", "ebtutkinnonoppiaineenkomponentti")
                  ),
                  arviointi = None
                ),
              ))
            ),
          ))
        ))
      )

      putOpiskeluoikeus(oo, henkilö = eshHenkilö) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.keskeneräinenOsasuoritus("Valmiiksi merkityllä suorituksella koulutus/301104 on keskeneräinen osasuoritus ebtutkinnonoppiaineenkomponentti/Final"))
      }
    }

    "voi tehdä, jos on pelkkä final" in {
      setupOppijaWithOpiskeluoikeus(ExamplesEuropeanSchoolOfHelsinki.opiskeluoikeus, henkilö = eshHenkilö) {
        verifyResponseStatusOk()
      }

      val oo = defaultOpiskeluoikeus.copy(
        tila = EBOpiskeluoikeudenTila(
          List(
            EBOpiskeluoikeusjakso(ExamplesEB.alkamispäivä, LukioExampleData.opiskeluoikeusAktiivinen),
          )
        ),
        suoritukset = List(ExamplesEB.eb.copy(
          osasuoritukset = Some(List(
            EBTutkinnonOsasuoritus(
              koulutusmoduuli =  EuropeanSchoolOfHelsinkiMuuOppiaine(
                Koodistokoodiviite("MA", "europeanschoolofhelsinkimuuoppiaine"),
                laajuus = LaajuusVuosiviikkotunneissa(4)
              ),
              suorituskieli = ExampleData.englanti,
              osasuoritukset = Some(List(
                EBOppiaineenAlaosasuoritus(
                  koulutusmoduuli = EBOppiaineKomponentti(
                    tunniste = Koodistokoodiviite("Final", "ebtutkinnonoppiaineenkomponentti")
                  ),
                  arviointi = ExamplesEB.ebTutkintoFinalMarkArviointi(päivä = ExamplesEB.alkamispäivä.plusMonths(3))
                ),
              ))
            ),
          ))
        ))
      )

      putOpiskeluoikeus(oo, henkilö = eshHenkilö) {
        verifyResponseStatusOk()
      }
    }

    "Ei voi tehdä, jos yleisarvosanaa ei ole annettu" in {
      setupOppijaWithOpiskeluoikeus(ExamplesEuropeanSchoolOfHelsinki.opiskeluoikeus, henkilö = eshHenkilö) {
        verifyResponseStatusOk()
      }

      val oo = defaultOpiskeluoikeus.copy(
        tila = EBOpiskeluoikeudenTila(
          List(
            EBOpiskeluoikeusjakso(ExamplesEB.alkamispäivä, LukioExampleData.opiskeluoikeusAktiivinen),
          )
        ),
        suoritukset = List(ExamplesEB.eb.copy(
          yleisarvosana = None
        ))
      )

      putOpiskeluoikeus(oo, henkilö = eshHenkilö) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.eb.yleisarvosana())
      }
    }

    "ei voi tehdä, jos ESH opiskeluoikeus ei ole merkitty valmistuneeksi" in {
      setupOppijaWithOpiskeluoikeus(ExamplesEuropeanSchoolOfHelsinki.opiskeluoikeus.copy(
        tila = EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila(
          List(
            EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso(alkamispäivä, LukioExampleData.opiskeluoikeusAktiivinen),
          )
        )
      ), henkilö = eshHenkilö) {
        verifyResponseStatusOk()
      }

      putOpiskeluoikeus(defaultOpiskeluoikeus, henkilö = eshHenkilö) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.eb.eshEiValmistunut())
      }
    }
  }

  "Ei voi tallentaa ennen rajapäivää" in {
    setupOppijaWithOpiskeluoikeus(ExamplesEuropeanSchoolOfHelsinki.opiskeluoikeus, henkilö = eshHenkilö) {
      verifyResponseStatusOk()
    }

    val oppija = Oppija(eshHenkilö, List(defaultOpiskeluoikeus))
    val huominenPäivä = LocalDate.now().plusDays(1)

    val config = KoskiApplicationForTests.config.withValue("validaatiot.europeanSchoolOfHelsinkiAikaisinSallittuTallennuspaiva", fromAnyRef(huominenPäivä.toString))
    implicit val session: KoskiSpecificSession = KoskiSpecificSession.systemUser
    implicit val accessType = AccessType.write
    mockKoskiValidator(config).updateFieldsAndValidateAsJson(oppija)
      .left.get should equal(KoskiErrorCategory.badRequest.validation.esh.tallennuspäivä(s"Helsingin eurooppalaisen koulun opiskeluoikeuksia voi alkaa tallentaa vasta ${finnishDateFormat.format(huominenPäivä)} alkaen"))
  }

  "Ei voi tallentaa, jos päättynyt ennen rajapäivää" in {
    setupOppijaWithOpiskeluoikeus(ExamplesEuropeanSchoolOfHelsinki.opiskeluoikeus, henkilö = eshHenkilö) {
      verifyResponseStatusOk()
    }

    defaultOpiskeluoikeus.päättymispäivä.isDefined should be(true)
    val oppija = Oppija(eshHenkilö, List(defaultOpiskeluoikeus))
    val päättymispäivänjälkeinenPäivä = defaultOpiskeluoikeus.päättymispäivä.get.plusDays(1)

    val config = KoskiApplicationForTests.config.withValue("validaatiot.europeanSchoolOfHelsinkiAikaisinSallittuPaattymispaiva", fromAnyRef(päättymispäivänjälkeinenPäivä.toString))
    implicit val session: KoskiSpecificSession = KoskiSpecificSession.systemUser
    implicit val accessType = AccessType.write
    mockKoskiValidator(config).updateFieldsAndValidateAsJson(oppija)
      .left.get should equal(KoskiErrorCategory.badRequest.validation.esh.päättymispäivä(s"Helsingin eurooppalaisen koulun tallennettavat opiskeluoikeudet eivät voi olla päättyneet ennen lain voimaantuloa ${finnishDateFormat.format(päättymispäivänjälkeinenPäivä)}"))
  }

  def mockKoskiValidator(config: Config) = {
    new KoskiValidator(
      KoskiApplicationForTests.organisaatioRepository,
      KoskiApplicationForTests.possu,
      KoskiApplicationForTests.henkilöRepository,
      KoskiApplicationForTests.ePerusteetValidator,
      KoskiApplicationForTests.ePerusteetLops2019Validator,
      KoskiApplicationForTests.ePerusteetFiller,
      KoskiApplicationForTests.validatingAndResolvingExtractor,
      KoskiApplicationForTests.suostumuksenPeruutusService,
      KoskiApplicationForTests.koodistoViitePalvelu,
      config,
      KoskiApplicationForTests.validationContext,
    )
  }
}
