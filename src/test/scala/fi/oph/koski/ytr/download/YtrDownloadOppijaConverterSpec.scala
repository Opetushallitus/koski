package fi.oph.koski.ytr.download

import fi.oph.koski.TestEnvironment
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.schema.{Koodistokoodiviite, Organisaatiovahvistus, YlioppilasTutkinnonKoe, YlioppilaskokeenArviointi, YlioppilastutkinnonKokeenSuoritus, YlioppilastutkinnonOpiskeluoikeudenTila, YlioppilastutkinnonOpiskeluoikeus, YlioppilastutkinnonSuoritus, YlioppilastutkinnonTutkintokerta}
import fi.oph.koski.ytr.MockYrtClient
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate

class YtrDownloadOppijaConverterSpec extends AnyFreeSpec with TestEnvironment with Matchers {
  val application = KoskiApplication.apply
  private val oppijaConverter = new YtrDownloadOppijaConverter(
    application.koodistoViitePalvelu,
    application.organisaatioRepository,
    application.koskiLocalizationRepository
  )

  val ytl =
    application.organisaatioRepository.getOrganisaatio("1.2.246.562.10.43628088406")
      .flatMap(_.toKoulutustoimija)
      .get
  val helsinki =
    application.koodistoViitePalvelu.validate("kunta", "091")
      .get
  val kevät = application.koskiLocalizationRepository.get("kevät")
  val syksy = application.koskiLocalizationRepository.get("syksy")

  val simppelinHetu = "140380-336X"

  "Yksinkertainen YTR:n latausrajapinnan palauttama oppija osataan konvertoida" in {
    val oppijat = MockYrtClient.oppijatByHetut(YtrSsnData(ssns = Some(List("080380-2432", "140380-336X", "220680-7850", "240680-087S"))))
    oppijat should have length 4
    val simppeliOppija = oppijat.find(_.ssn == simppelinHetu).get
    simppeliOppija.ssn should be (simppelinHetu)

    val expectedYlioppilastutkinto = YlioppilastutkinnonOpiskeluoikeus(
      lähdejärjestelmänId = None,
      oppilaitos = None,
      koulutustoimija = Some(ytl),
      tila = YlioppilastutkinnonOpiskeluoikeudenTila(opiskeluoikeusjaksot = List()),
      suoritukset = List(
        YlioppilastutkinnonSuoritus(
          toimipiste = ytl,
          pakollisetKokeetSuoritettu = false,
          vahvistus = Some(
            Organisaatiovahvistus(
              päivä = LocalDate.of(2015, 11, 30),
              paikkakunta = helsinki,
              myöntäjäOrganisaatio = ytl.toOidOrganisaatio
            )
          ),
          osasuoritukset = Some(List(
            YlioppilastutkinnonKokeenSuoritus(
              koulutusmoduuli = YlioppilasTutkinnonKoe(
                tunniste = Koodistokoodiviite("PC", "koskiyokokeet")
              ),
              tutkintokerta = YlioppilastutkinnonTutkintokerta(
                koodiarvo = "2015K",
                vuosi = 2015,
                vuodenaika = kevät
              ),
              arviointi = None
            ),
            YlioppilastutkinnonKokeenSuoritus(
              koulutusmoduuli = YlioppilasTutkinnonKoe(
                tunniste = Koodistokoodiviite("PC", "koskiyokokeet")
              ),
              tutkintokerta = YlioppilastutkinnonTutkintokerta(
                koodiarvo = "2015K",
                vuosi = 2015,
                vuodenaika = kevät
              ),
              arviointi = None
            ),
            YlioppilastutkinnonKokeenSuoritus(
              koulutusmoduuli = YlioppilasTutkinnonKoe(
                tunniste = Koodistokoodiviite("PC", "koskiyokokeet")
              ),
              tutkintokerta = YlioppilastutkinnonTutkintokerta(
                koodiarvo = "2015K",
                vuosi = 2015,
                vuodenaika = kevät
              ),
              arviointi = None
            ),
            YlioppilastutkinnonKokeenSuoritus(
              koulutusmoduuli = YlioppilasTutkinnonKoe(
                tunniste = Koodistokoodiviite("BB", "koskiyokokeet")
              ),
              tutkintokerta = YlioppilastutkinnonTutkintokerta(
                koodiarvo = "2014S",
                vuosi = 2014,
                vuodenaika = syksy
              ),
              arviointi = None
            ),
            YlioppilastutkinnonKokeenSuoritus(
              koulutusmoduuli = YlioppilasTutkinnonKoe(
                tunniste = Koodistokoodiviite("A", "koskiyokokeet")
              ),
              tutkintokerta = YlioppilastutkinnonTutkintokerta(
                koodiarvo = "2014K",
                vuosi = 2014,
                vuodenaika = kevät
              ),
              arviointi = Some(List(
                YlioppilaskokeenArviointi(
                  arvosana = Koodistokoodiviite("E", "koskiyoarvosanat"),
                  pisteet = Some(6)
                )
              ))
            )
          ))
        )
      )
    )

    oppijaConverter.convertOppijastaOpiskeluoikeus(simppeliOppija) should equal (Some(expectedYlioppilastutkinto))
  }
}
