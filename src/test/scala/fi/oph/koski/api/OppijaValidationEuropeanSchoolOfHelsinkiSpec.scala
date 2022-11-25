package fi.oph.koski.api

import com.typesafe.config.Config
import com.typesafe.config.ConfigValueFactory.fromAnyRef
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import fi.oph.koski.documentation.EuropeanSchoolOfHelsinkiExampleData.{osasuoritusArviointi, primaryOppimisalueenOsasuoritusKieli, primarySuoritus12, secondaryLowerSuoritus1, secondaryLowerSuoritus45, secondaryNumericalMarkArviointi, secondaryS7PreliminaryMarkArviointi, secondaryUpperMuunOppiaineenOsasuoritusS6, secondaryUpperMuunOppiaineenOsasuoritusS7, secondaryUpperSuoritusS6, secondaryUpperSuoritusS7}
import fi.oph.koski.documentation.ExamplesEuropeanSchoolOfHelsinki.alkamispäivä
import fi.oph.koski.documentation.{ExampleData, ExamplesEuropeanSchoolOfHelsinki, LukioExampleData}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.{ErrorMatcher, KoskiErrorCategory}
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession}
import fi.oph.koski.schema._
import fi.oph.koski.util.FinnishDateFormat.finnishDateFormat
import fi.oph.koski.validation.KoskiValidator
import org.scalatest.freespec.AnyFreeSpec

import java.time.LocalDate
import java.time.LocalDate.{of => date}

class OppijaValidationEuropeanSchoolOfHelsinkiSpec
  extends AnyFreeSpec
    with KoskiHttpSpec
    with PutOpiskeluoikeusTestMethods[EuropeanSchoolOfHelsinkiOpiskeluoikeus]
{
  override def tag = implicitly[reflect.runtime.universe.TypeTag[EuropeanSchoolOfHelsinkiOpiskeluoikeus]]

  override def defaultOpiskeluoikeus = ExamplesEuropeanSchoolOfHelsinki.opiskeluoikeus

  val oppija = KoskiSpecificMockOppijat.europeanSchoolOfHelsinki

  "Example-opiskeluoikeus voidaan kirjoittaa tietokantaan" in {
    putOpiskeluoikeus(defaultOpiskeluoikeus, henkilö = oppija) {
      verifyResponseStatusOk()
    }
  }

  "Koulutustyyppi" - {
    "Täydennetään" in {
      val putOo = defaultOpiskeluoikeus.copy(
        suoritukset = List(
          primarySuoritus12("P1", alkamispäivä.plusYears(2)).copy(
            koulutusmoduuli = PrimaryLuokkaAste("P1").copy(koulutustyyppi = None)
          ),
          secondaryLowerSuoritus1("S1", alkamispäivä.plusYears(8)).copy(
            koulutusmoduuli = SecondaryLowerLuokkaAste("S1").copy(koulutustyyppi = None)
          ),
          secondaryUpperSuoritusS6("S6", alkamispäivä.plusYears(13)).copy(
            koulutusmoduuli = SecondaryUpperLuokkaAste("S6").copy(koulutustyyppi = None)
          ),
        )
      )

      koulutustyypit(putOo) should be(List.empty)

      val oo = putAndGetOpiskeluoikeus(putOo)

      koulutustyypit(oo) should be(List("21", "21", "21"))
    }

    def koulutustyypit(oo: EuropeanSchoolOfHelsinkiOpiskeluoikeus): List[String] = {
      oo.suoritukset.flatMap(_.koulutusmoduuli match {
        case k: KoulutustyypinSisältäväEuropeanSchoolOfHelsinkiLuokkaAste =>
          k.koulutustyyppi
        case _ => None
      }).map(_.koodiarvo)
    }
  }

  "Opintojen rahoitus" - {

    val alkamispäivä = defaultOpiskeluoikeus.alkamispäivä.get
    val päättymispäivä = alkamispäivä.plusYears(20)

    "lasna -tilalle täydennetään opintojen rahoitus, koska vaihtoehtoja on toistaiseksi vain yksi" in {
      val oo = putAndGetOpiskeluoikeus(defaultOpiskeluoikeus.copy(tila = EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila(List(EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso(alkamispäivä, ExampleData.opiskeluoikeusLäsnä, opintojenRahoitus = None)))))

      val täydennetytRahoitusmuodot = oo.tila.opiskeluoikeusjaksot.flatMap(_.opintojenRahoitus)
      täydennetytRahoitusmuodot should be(List(ExampleData.muutaKauttaRahoitettu))
    }

    "valmistunut -tilalle täydennetään opintojen rahoitus, koska vaihtoehtoja on toistaiseksi vain yksi" in {
      val tila = EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila(List(
        EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso(alkamispäivä, ExampleData.opiskeluoikeusLäsnä, Some(ExampleData.muutaKauttaRahoitettu)),
        EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso(päättymispäivä, ExampleData.opiskeluoikeusValmistunut, opintojenRahoitus = None)
      ))

      val oo = putAndGetOpiskeluoikeus(defaultOpiskeluoikeus.copy(tila = tila))

      val täydennetytRahoitusmuodot = oo.tila.opiskeluoikeusjaksot.flatMap(_.opintojenRahoitus)
      täydennetytRahoitusmuodot should be(List(ExampleData.muutaKauttaRahoitettu, ExampleData.muutaKauttaRahoitettu))
    }

    "Opintojen rahoitus on kielletty muilta tiloilta" in {
      def verifyRahoitusmuotoKielletty(tila: Koodistokoodiviite) = {
        putOpiskeluoikeus(defaultOpiskeluoikeus.copy(tila = EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila(List(
          EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso(alkamispäivä, ExampleData.opiskeluoikeusLäsnä, Some(ExampleData.muutaKauttaRahoitettu)),
          EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso(päättymispäivä, tila, Some(ExampleData.muutaKauttaRahoitettu))
        )))) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.tilallaEiSaaOllaRahoitusmuotoa(s"Opiskeluoikeuden tilalla ${tila.koodiarvo} ei saa olla rahoitusmuotoa"))
        }
      }

      List(
        ExampleData.opiskeluoikeusEronnut,
        ExampleData.opiskeluoikeusValiaikaisestiKeskeytynyt,
      ).foreach(verifyRahoitusmuotoKielletty)
    }
  }

  "Päätason suorituksen alkamispäivä" - {
    "Vaaditaan" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus.copy(
        tila =
          EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila(
            List(
              EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso(alkamispäivä, ExampleData.opiskeluoikeusLäsnä, Some(ExampleData.muutaKauttaRahoitettu))
            )
          ),
        suoritukset =
          List(
            defaultOpiskeluoikeus.suoritukset.collectFirst { case s: EuropeanSchoolOfHelsinkiVuosiluokanSuoritus => s }.map(_.ilmanAlkamispäivää).get
          )
      )
      ) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.alkamispäiväPuuttuu("Suoritukselle europeanschoolofhelsinkiluokkaaste/N1 ei ole merkitty alkamispäivää"))
      }
    }
  }

  "Osasuorituksen arvostelua ei voi tehdä primaryssä, jos on arvioimattomia alaosasuorituksia" in {
    val oo = defaultOpiskeluoikeus.copy(
      tila = EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila(
        List(
          EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso(alkamispäivä, LukioExampleData.opiskeluoikeusAktiivinen),
        )
      ),
      suoritukset = List(ExamplesEuropeanSchoolOfHelsinki.p3.copy(
        vahvistus = None,
        osasuoritukset = Some(List(
          primaryOppimisalueenOsasuoritusKieli(
            oppiainekoodi = "ONL",
            kieli = ExampleData.ruotsinKieli,
            alaosasuorituskoodit = Some(List(
              "Listening and understanding"
            )),
            arviointi = osasuoritusArviointi(
              arvosana = "fail",
              päivä = alkamispäivä.plusDays(30)
            ),
            alaosasuoritusArviointi = None
          )
        ))
      ))
    )

    putOpiskeluoikeus(oo) {
      verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.keskeneräinenOsasuoritus("Valmiiksi merkityllä suorituksella europeanschoolofhelsinkikielioppiaine/ONL on keskeneräinen osasuoritus europeanschoolofhelsinkiprimaryalaoppimisalue/Listening and understanding"))
    }
  }

  "Päätason suorituksen vahvistus S7:ssa" - {
    "ei voi tehdä, jos ei ole alaosasuorituksia lainkaan" in {
      val oo = defaultOpiskeluoikeus.copy(
        tila = EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila(
          List(
            EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso(alkamispäivä, LukioExampleData.opiskeluoikeusAktiivinen),
          )
        ),
        suoritukset = List(ExamplesEuropeanSchoolOfHelsinki.s7.copy(
          osasuoritukset = Some(List(
            SecondaryUpperOppiaineenSuoritusS7(
              koulutusmoduuli = EuropeanSchoolOfHelsinkiMuuOppiaine(
                Koodistokoodiviite("PE", "europeanschoolofhelsinkimuuoppiaine"),
                laajuus = LaajuusVuosiviikkotunneissa(2)
              ),
              suorituskieli = ExampleData.englanti,
              osasuoritukset = Some(List(
              ))
            )
          ))
        ))
      )

      putOpiskeluoikeus(oo) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.valmiiksiMerkityltäPuuttuuOsasuorituksia("Suoritus europeanschoolofhelsinkiluokkaaste/S7 on merkitty valmiiksi, mutta sillä on tyhjä osasuorituslista tai joltain sen osasuoritukselta puuttuu vaadittavat arvioidut osasuoritukset (joko A ja B, tai yearmark), tai opiskeluoikeudelta puuttuu linkitys"))
      }
    }
    "ei voi tehdä, jos on vain toinen A tai B arvioinneista" in {
      val oo = defaultOpiskeluoikeus.copy(
        tila = EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila(
          List(
            EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso(alkamispäivä, LukioExampleData.opiskeluoikeusAktiivinen),
          )
        ),
        suoritukset = List(ExamplesEuropeanSchoolOfHelsinki.s7.copy(
          osasuoritukset = Some(List(
            SecondaryUpperOppiaineenSuoritusS7(
              koulutusmoduuli = EuropeanSchoolOfHelsinkiMuuOppiaine(
                Koodistokoodiviite("PE", "europeanschoolofhelsinkimuuoppiaine"),
                laajuus = LaajuusVuosiviikkotunneissa(2)
              ),
              suorituskieli = ExampleData.englanti,
              osasuoritukset = Some(List(
                S7OppiaineenAlaosasuoritus(
                  koulutusmoduuli = S7OppiaineKomponentti(
                    Koodistokoodiviite("B", "europeanschoolofhelsinkis7oppiaineenkomponentti")
                  ),
                  arviointi = secondaryS7PreliminaryMarkArviointi(päivä = alkamispäivä.plusMonths(3))
                )
              ))
            )
          ))
        ))
      )

      putOpiskeluoikeus(oo) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.valmiiksiMerkityltäPuuttuuOsasuorituksia("Suoritus europeanschoolofhelsinkiluokkaaste/S7 on merkitty valmiiksi, mutta sillä on tyhjä osasuorituslista tai joltain sen osasuoritukselta puuttuu vaadittavat arvioidut osasuoritukset (joko A ja B, tai yearmark), tai opiskeluoikeudelta puuttuu linkitys"))
      }
    }
    "ei voi tehdä, jos on A ja B mutta toinen arvioimatta" in {
      val oo = defaultOpiskeluoikeus.copy(
        tila = EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila(
          List(
            EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso(alkamispäivä, LukioExampleData.opiskeluoikeusAktiivinen),
          )
        ),
        suoritukset = List(ExamplesEuropeanSchoolOfHelsinki.s7.copy(
          osasuoritukset = Some(List(
            SecondaryUpperOppiaineenSuoritusS7(
              koulutusmoduuli = EuropeanSchoolOfHelsinkiMuuOppiaine(
                Koodistokoodiviite("PE", "europeanschoolofhelsinkimuuoppiaine"),
                laajuus = LaajuusVuosiviikkotunneissa(2)
              ),
              suorituskieli = ExampleData.englanti,
              osasuoritukset = Some(List(
                S7OppiaineenAlaosasuoritus(
                  koulutusmoduuli = S7OppiaineKomponentti(
                    Koodistokoodiviite("A", "europeanschoolofhelsinkis7oppiaineenkomponentti")
                  ),
                  arviointi = secondaryS7PreliminaryMarkArviointi(päivä = alkamispäivä.plusMonths(3))
                ),
                S7OppiaineenAlaosasuoritus(
                  koulutusmoduuli = S7OppiaineKomponentti(
                    Koodistokoodiviite("B", "europeanschoolofhelsinkis7oppiaineenkomponentti")
                  ),
                  arviointi = None
                )
              ))
            )
          ))
        ))
      )

      putOpiskeluoikeus(oo) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.keskeneräinenOsasuoritus("Valmiiksi merkityllä suorituksella europeanschoolofhelsinkiluokkaaste/S7 on keskeneräinen osasuoritus europeanschoolofhelsinkis7oppiaineenkomponentti/B"))
      }
    }

    "Voi tehdä, jos on vain toinen A tai B arvioinneista ja myös year mark" in {
      val oo = defaultOpiskeluoikeus.copy(
        tila = EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila(
          List(
            EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso(alkamispäivä, LukioExampleData.opiskeluoikeusAktiivinen),
          )
        ),
        suoritukset = List(ExamplesEuropeanSchoolOfHelsinki.s7.copy(
          osasuoritukset = Some(List(
            SecondaryUpperOppiaineenSuoritusS7(
              koulutusmoduuli = EuropeanSchoolOfHelsinkiMuuOppiaine(
                Koodistokoodiviite("PE", "europeanschoolofhelsinkimuuoppiaine"),
                laajuus = LaajuusVuosiviikkotunneissa(2)
              ),
              suorituskieli = ExampleData.englanti,
              osasuoritukset = Some(List(
                S7OppiaineenAlaosasuoritus(
                  koulutusmoduuli = S7OppiaineKomponentti(
                    Koodistokoodiviite("B", "europeanschoolofhelsinkis7oppiaineenkomponentti")
                  ),
                  arviointi = secondaryS7PreliminaryMarkArviointi(päivä = alkamispäivä.plusMonths(3))
                ),
                S7OppiaineenAlaosasuoritus(
                  koulutusmoduuli = S7OppiaineKomponentti(
                    Koodistokoodiviite("yearmark", "europeanschoolofhelsinkis7oppiaineenkomponentti")
                  ),
                  arviointi = secondaryS7PreliminaryMarkArviointi(päivä = alkamispäivä.plusMonths(3))
                )
              ))
            )
          ))
        ))
      )

      putOpiskeluoikeus(oo) {
        verifyResponseStatusOk()
      }
    }

    "Ei voi tehdä, jos on pelkkä year mark ilman arviointia" in {
      val oo = defaultOpiskeluoikeus.copy(
        tila = EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila(
          List(
            EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso(alkamispäivä, LukioExampleData.opiskeluoikeusAktiivinen),
          )
        ),
        suoritukset = List(ExamplesEuropeanSchoolOfHelsinki.s7.copy(
          osasuoritukset = Some(List(
            SecondaryUpperOppiaineenSuoritusS7(
              koulutusmoduuli = EuropeanSchoolOfHelsinkiMuuOppiaine(
                Koodistokoodiviite("PE", "europeanschoolofhelsinkimuuoppiaine"),
                laajuus = LaajuusVuosiviikkotunneissa(2)
              ),
              suorituskieli = ExampleData.englanti,
              osasuoritukset = Some(List(
                S7OppiaineenAlaosasuoritus(
                  koulutusmoduuli = S7OppiaineKomponentti(
                    Koodistokoodiviite("yearmark", "europeanschoolofhelsinkis7oppiaineenkomponentti")
                  ),
                  arviointi = None
                )
              ))
            )
          ))
        ))
      )

      putOpiskeluoikeus(oo) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.keskeneräinenOsasuoritus("Valmiiksi merkityllä suorituksella europeanschoolofhelsinkiluokkaaste/S7 on keskeneräinen osasuoritus europeanschoolofhelsinkis7oppiaineenkomponentti/yearmark"))
      }
    }

    "voi tehdä, jos on pelkkä year mark" in {
      val oo = defaultOpiskeluoikeus.copy(
        tila = EuropeanSchoolOfHelsinkiOpiskeluoikeudenTila(
          List(
            EuropeanSchoolOfHelsinkiOpiskeluoikeusjakso(alkamispäivä, LukioExampleData.opiskeluoikeusAktiivinen),
          )
        ),
        suoritukset = List(ExamplesEuropeanSchoolOfHelsinki.s7.copy(
          osasuoritukset = Some(List(
            SecondaryUpperOppiaineenSuoritusS7(
              koulutusmoduuli = EuropeanSchoolOfHelsinkiMuuOppiaine(
                Koodistokoodiviite("PE", "europeanschoolofhelsinkimuuoppiaine"),
                laajuus = LaajuusVuosiviikkotunneissa(2)
              ),
              suorituskieli = ExampleData.englanti,
              osasuoritukset = Some(List(
                S7OppiaineenAlaosasuoritus(
                  koulutusmoduuli = S7OppiaineKomponentti(
                    Koodistokoodiviite("yearmark", "europeanschoolofhelsinkis7oppiaineenkomponentti")
                  ),
                  arviointi = secondaryS7PreliminaryMarkArviointi(päivä = alkamispäivä.plusMonths(3))
                )
              ))
            )
          ))
        ))
      )

      putOpiskeluoikeus(oo) {
        verifyResponseStatusOk()
      }
    }
  }

  "Ei voi tallentaa ennen rajapäivää" in {
    val oppija = Oppija(defaultHenkilö, List(defaultOpiskeluoikeus))
    val huominenPäivä = LocalDate.now().plusDays(1)

    val config = KoskiApplicationForTests.config.withValue("validaatiot.europeanSchoolOfHelsinkiAikaisinSallittuTallennuspaiva", fromAnyRef(huominenPäivä.toString))
    implicit val session: KoskiSpecificSession = KoskiSpecificSession.systemUser
    implicit val accessType = AccessType.write
    mockKoskiValidator(config).updateFieldsAndValidateAsJson(oppija)
      .left.get should equal(KoskiErrorCategory.badRequest.validation.esh.tallennuspäivä(s"Helsingin eurooppalaisen koulun opiskeluoikeuksia voi alkaa tallentaa vasta ${finnishDateFormat.format(huominenPäivä)} alkaen"))
  }

  "Ei voi tallentaa, jos päättynyt ennen rajapäivää" in {
    defaultOpiskeluoikeus.päättymispäivä.isDefined should be(true)
    val oppija = Oppija(defaultHenkilö, List(defaultOpiskeluoikeus))
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
      KoskiApplicationForTests.ePerusteetFiller,
      KoskiApplicationForTests.validatingAndResolvingExtractor,
      KoskiApplicationForTests.suostumuksenPeruutusService,
      KoskiApplicationForTests.koodistoViitePalvelu,
      config
    )
  }

  private def putAndGetOpiskeluoikeus(oo: EuropeanSchoolOfHelsinkiOpiskeluoikeus): EuropeanSchoolOfHelsinkiOpiskeluoikeus = putOpiskeluoikeus(oo) {
    verifyResponseStatusOk()
    getOpiskeluoikeus(readPutOppijaResponse.opiskeluoikeudet.head.oid)
  }.asInstanceOf[EuropeanSchoolOfHelsinkiOpiskeluoikeus]
}
