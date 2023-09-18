package fi.oph.koski.ytr.download

import fi.oph.koski.TestEnvironment
import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.{helsinginMedialukio, ressunLukio}
import fi.oph.koski.schema.{Finnish, Koodistokoodiviite, Organisaatiovahvistus, YlioppilasTutkinnonKoe, YlioppilaskokeenArviointi, YlioppilastutkinnonKokeenSuoritus, YlioppilastutkinnonOpiskeluoikeudenLisätiedot, YlioppilastutkinnonOpiskeluoikeudenTila, YlioppilastutkinnonOpiskeluoikeus, YlioppilastutkinnonOpiskeluoikeusjakso, YlioppilastutkinnonSisältyväKoe, YlioppilastutkinnonSuoritus, YlioppilastutkinnonTutkintokerranLisätiedot, YlioppilastutkinnonTutkintokerta, YlioppilastutkinnonTutkintokokonaisuudenLisätiedot}
import fi.oph.koski.ytr.{MockYrtClient, YtrConversionUtils}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate

class YtrDownloadOppijaConverterSpec extends AnyFreeSpec with TestEnvironment with Matchers {
  val application = KoskiApplication.apply
  private val oppijaConverter = new YtrDownloadOppijaConverter(
    application.koodistoViitePalvelu,
    application.organisaatioRepository,
    application.koskiLocalizationRepository,
    application.validatingAndResolvingExtractor
  )

  private val conversionUtils = new YtrConversionUtils(application.koskiLocalizationRepository,  application.koodistoViitePalvelu, application.organisaatioRepository)

  val ytl = application.organisaatioRepository
      .getOrganisaatio("1.2.246.562.10.43628088406")
      .flatMap(_.toKoulutustoimija)
    .get
  val helsinki = application.koodistoViitePalvelu
      .validate("kunta", "091")
      .get

  val kevät = application.koskiLocalizationRepository.get("kevät")
  val syksy = application.koskiLocalizationRepository.get("syksy")

  val simppelinHetu = "140380-336X"
  val valmistuneenHetu = "080380-2432"

  "Yksinkertainen YTR:n latausrajapinnan palauttama oppija osataan konvertoida" in {
    val oppijat = MockYrtClient.oppijatByHetut(YtrSsnData(ssns = Some(List("080380-2432", "140380-336X", "220680-7850", "240680-087S", "060807A7787", "300805A756F"))))
    oppijat should have length 6
    val simppeliOppija = oppijat.find(_.ssn == simppelinHetu).get
    simppeliOppija.ssn should be (simppelinHetu)

    val expectedYlioppilastutkinto = YlioppilastutkinnonOpiskeluoikeus(
      lähdejärjestelmänId = None,
      oppilaitos = conversionUtils.ytlOppilaitos,
      koulutustoimija = Some(ytl),
      tila = YlioppilastutkinnonOpiskeluoikeudenTila(opiskeluoikeusjaksot = List(
        YlioppilastutkinnonOpiskeluoikeusjakso(
          LocalDate.of(2015,11,30),
          Koodistokoodiviite("valmistunut", "koskiopiskeluoikeudentila")
        )
      )),
      lisätiedot = Some(YlioppilastutkinnonOpiskeluoikeudenLisätiedot(Some(List(
        YlioppilastutkinnonTutkintokokonaisuudenLisätiedot(
          tunniste = 0,
          tyyppi = Some(Koodistokoodiviite("candidate", "ytrtutkintokokonaisuudentyyppi")),
          tila = None,
          suorituskieli = Some(Koodistokoodiviite("FI", "kieli")),
          tutkintokerrat = List(
            YlioppilastutkinnonTutkintokerranLisätiedot(YlioppilastutkinnonTutkintokerta(koodiarvo = "2015K", vuosi = 2015, vuodenaika = kevät),Some(Koodistokoodiviite("1", "ytrkoulutustausta")), Some(helsinginMedialukio)),
            YlioppilastutkinnonTutkintokerranLisätiedot(YlioppilastutkinnonTutkintokerta(koodiarvo = "2014S", vuosi = 2014, vuodenaika = syksy),Some(Koodistokoodiviite("1", "ytrkoulutustausta")), Some(helsinginMedialukio))
          ),
          aiemminSuoritetutKokeet = Some(List(
            YlioppilastutkinnonSisältyväKoe(
              YlioppilasTutkinnonKoe(
                tunniste = Koodistokoodiviite("A", "koskiyokokeet"),
              ),
              tutkintokerta = YlioppilastutkinnonTutkintokerta("2014K", 2014, kevät)
            )
          ))
        ),
        YlioppilastutkinnonTutkintokokonaisuudenLisätiedot(
          tunniste = 1,
          tyyppi = Some(Koodistokoodiviite("candidate", "ytrtutkintokokonaisuudentyyppi")),
          tila = None,
          suorituskieli = Some(Koodistokoodiviite("FI", "kieli")),
          tutkintokerrat = List(
            YlioppilastutkinnonTutkintokerranLisätiedot(YlioppilastutkinnonTutkintokerta(koodiarvo = "2014K", vuosi = 2014, vuodenaika = kevät), Some(Koodistokoodiviite("1", "ytrkoulutustausta")), Some(helsinginMedialukio))
          ),
          aiemminSuoritetutKokeet = None
        )
      )))),
      // oppilaitosSuorituspäivänä = Some(ressunLukio),
      suoritukset = List(
        YlioppilastutkinnonSuoritus(
          toimipiste = ytl,
          pakollisetKokeetSuoritettu = false,
          vahvistus = Some(
            Organisaatiovahvistus(
              päivä = LocalDate.of(2015, 11, 30),
              paikkakunta = helsinki,
              myöntäjäOrganisaatio = ytl
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
              arviointi = None,
              keskeytynyt = Some(true),
              maksuton = Some(false),
              tutkintokokonaisuudenTunniste = Some(0)
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
              arviointi = None,
              keskeytynyt = Some(true),
              maksuton = Some(false),
              tutkintokokonaisuudenTunniste = Some(0),
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
              arviointi = None,
              keskeytynyt = Some(true),
              maksuton = Some(false),
              tutkintokokonaisuudenTunniste = Some(0),
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
              arviointi = None,
              keskeytynyt = Some(false), // TODO: olisiko null parempi mapata None:ksi?
              maksuton = Some(false),
              tutkintokokonaisuudenTunniste = Some(0),
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
              )),
              keskeytynyt = Some(false), // TODO: olisiko null parempi mapata None:ksi?
              maksuton = Some(false),
              tutkintokokonaisuudenTunniste = Some(1),
            )
          ))
        )
      )
    )

    expectedYlioppilastutkinto.alkamispäivä should be(None)
    expectedYlioppilastutkinto.päättymispäivä should be(Some(LocalDate.of(2015, 11, 30)))
    expectedYlioppilastutkinto.keinotekoinenAlkamispäiväTutkintokerroista should be(
      LocalDate.of(2014, 3, 1)
    )

    oppijaConverter.convertOppijastaOpiskeluoikeus(simppeliOppija) should equal (Some(expectedYlioppilastutkinto))
  }

  "Graduated-tila konverstoidaan opintokokonaisuuksiin" in {
    val oppijat = MockYrtClient.oppijatByHetut(YtrSsnData(ssns = Some(List("080380-2432", "140380-336X", "220680-7850", "240680-087S", "060807A7787", "300805A756F"))))
    oppijat should have length 6
    val valmistunutOppija = oppijat.find(_.ssn == valmistuneenHetu).get
    valmistunutOppija.ssn should be (valmistuneenHetu)

    val opiskeluoikeus = oppijaConverter.convertOppijastaOpiskeluoikeus(valmistunutOppija).get

    val tutkintokokonaisuuksienTilat = opiskeluoikeus.lisätiedot.flatMap(_.tutkintokokonaisuudet.map(_.flatMap(_.tila.map(_.koodiarvo)))).toList.flatten

    tutkintokokonaisuuksienTilat should contain("graduated")
  }
}
