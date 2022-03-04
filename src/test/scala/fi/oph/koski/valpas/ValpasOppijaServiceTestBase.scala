package fi.oph.koski.valpas

import java.time.LocalDateTime
import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.Organisaatio.Oid
import fi.oph.koski.schema.{InternationalSchoolOpiskeluoikeus, InternationalSchoolVuosiluokanSuoritus, KoskeenTallennettavaOpiskeluoikeus, PerusopetuksenLisäopetuksenOpiskeluoikeus, PerusopetuksenLisäopetuksenSuoritus, PerusopetuksenOpiskeluoikeus, PerusopetuksenVuosiluokanSuoritus, Ryhmällinen}
import fi.oph.koski.util.DateOrdering.{localDateOptionOrdering, localDateOrdering}
import fi.oph.koski.valpas.opiskeluoikeusrepository.MockValpasRajapäivätService.defaultMockTarkastelupäivä
import fi.oph.koski.valpas.opiskeluoikeusrepository.{ValpasOpiskeluoikeus, ValpasOppijaLaajatTiedot}
import fi.oph.koski.valpas.valpasrepository.ValpasKuntailmoitusLaajatTiedot
import fi.oph.koski.valpas.valpasuser.ValpasMockUser

case class ExpectedDataPerusopetusTiedot(
  tarkastelupäivänTila: String,
  tarkastelupäivänKoskiTila: String,
  vuosiluokkiinSitomatonOpetus: Boolean = false
)

case class ExpectedDataPerusopetuksenJälkeinenTiedot(
  tarkastelupäivänTila: String,
  tarkastelupäivänKoskiTila: String
)

case class ExpectedDataMuuOpetusTiedot(
  tarkastelupäivänTila: String,
  tarkastelupäivänKoskiTila: String
)

case class ExpectedData(
  opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
  onHakeutumisValvottavaOpiskeluoikeus: Boolean,
  onHakeutumisvalvovaOppilaitos: Boolean,
  onSuorittamisvalvovaOppilaitos: Boolean,
  perusopetusTiedot: Option[ExpectedDataPerusopetusTiedot] = None,
  perusopetuksenJälkeinenTiedot: Option[ExpectedDataPerusopetuksenJälkeinenTiedot] = None,
  muuOpetusTiedot: Option[ExpectedDataMuuOpetusTiedot] = None,
)

trait ValpasOppijaServiceTestBase extends ValpasTestBase {
  protected val oppijaLaajatTiedotService = KoskiApplicationForTests.valpasOppijaLaajatTiedotService
  protected val oppijaSuppeatTiedotService = KoskiApplicationForTests.valpasOppijaSuppeatTiedotService
  protected val rajapäivätService = KoskiApplicationForTests.valpasRajapäivätService
  protected val oppilaitos = MockOrganisaatiot.jyväskylänNormaalikoulu
  protected val amisOppilaitos = MockOrganisaatiot.stadinAmmattiopisto
  protected val organisaatioRepository = KoskiApplicationForTests.organisaatioRepository
  protected val kuntailmoitusRepository = KoskiApplicationForTests.valpasKuntailmoitusRepository

  protected def validateKunnanIlmoitetutOppijat(
    organisaatioOid: Oid,
    user: ValpasMockUser
  )(expectedOppijat: Seq[LaajatOppijaHenkilöTiedot]) = {
    val result = getKunnanIlmoitetutOppijat(organisaatioOid, user)
    result.map(_.map(_.oppija.henkilö.oid).sorted) shouldBe Right(expectedOppijat.map(_.oid).sorted)
  }

  private def getKunnanIlmoitetutOppijat(organisaatioOid: Oid, user: ValpasMockUser) = {
    oppijaSuppeatTiedotService.getKunnanOppijatSuppeatTiedot(organisaatioOid)(session(user))
  }

  protected def canAccessOppijaYhteystiedoillaJaKuntailmoituksilla(oppija: LaajatOppijaHenkilöTiedot, user: ValpasMockUser): Boolean =
    oppijaLaajatTiedotService.getOppijaLaajatTiedotYhteystiedoillaJaKuntailmoituksilla(oppija.oid)(session(user)).isRight

  protected def validateOppijaLaajatTiedot(
    oppija: ValpasOppijaLaajatTiedot,
    expectedOppija: LaajatOppijaHenkilöTiedot,
    expectedData: List[ExpectedData]
  ): Unit = validateOppijaLaajatTiedot(
    oppija,
    expectedOppija,
    Set(expectedOppija.oid),
    expectedData
  )

  protected def validateOppijaLaajatTiedot(
    oppija: ValpasOppijaLaajatTiedot,
    expectedOppija: LaajatOppijaHenkilöTiedot,
    expectedOppijaOidit: Set[String],
    expectedData: List[ExpectedData]
  ): Unit = {
    withClue(s"ValpasOppija(${oppija.henkilö.oid}/${oppija.henkilö.hetu}): ") {
      oppija.henkilö.oid shouldBe expectedOppija.oid
      oppija.henkilö.kaikkiOidit shouldBe expectedOppijaOidit
      oppija.henkilö.hetu shouldBe expectedOppija.hetu
      oppija.henkilö.etunimet shouldBe expectedOppija.etunimet
      oppija.henkilö.sukunimi shouldBe expectedOppija.sukunimi
      oppija.henkilö.turvakielto shouldBe expectedOppija.turvakielto
      oppija.henkilö.äidinkieli shouldBe expectedOppija.äidinkieli

      val expectedHakeutumisvalvovatOppilaitokset = expectedData.filter(_.onHakeutumisvalvovaOppilaitos).map(_.opiskeluoikeus.oppilaitos.get.oid).toSet
      oppija.hakeutumisvalvovatOppilaitokset shouldBe expectedHakeutumisvalvovatOppilaitokset

      val expectedSuorittamisvalvovatOppilaitokset = expectedData.filter(_.onSuorittamisvalvovaOppilaitos).map(_.opiskeluoikeus.oppilaitos.get.oid).toSet
      oppija.suorittamisvalvovatOppilaitokset shouldBe expectedSuorittamisvalvovatOppilaitokset

      oppija.onOikeusValvoaMaksuttomuutta shouldBe true // TODO: true aina, koska toistaiseksi tutkitaan vain peruskoulun hakeutumisvalvottavia
      oppija.onOikeusValvoaKunnalla shouldBe true // TODO: true aina, koska toistaiseksi tutkitaan vain peruskoulun hakeutumisvalvottavia

      val maybeOpiskeluoikeudet = oppija.opiskeluoikeudet.map(o => Some(o))
      val maybeExpectedData = expectedData.map(o => Some(o))

      maybeOpiskeluoikeudet.zipAll(maybeExpectedData, None, None).zipWithIndex.foreach {
        case (element, index) => {
          withClue(s"index ${index}: ") {
            element match {
              case (Some(opiskeluoikeus), Some(expectedData)) => {
                val clue = makeClue("ValpasOpiskeluoikeus", Seq(
                  opiskeluoikeus.oid,
                  opiskeluoikeus.oppilaitos.nimi.get("fi"),
                  opiskeluoikeus.tyyppi.koodiarvo
                ))
                withClue(clue) {
                  validateOpiskeluoikeus(opiskeluoikeus, expectedData)

                  withClue("perusopetusTiedot") {
                    withClue("alkamispäivä") {
                      opiskeluoikeus.perusopetusTiedot.flatMap(_.alkamispäivä) shouldBe expectedData.perusopetusTiedot.flatMap(_ => expectedData.opiskeluoikeus.alkamispäivä.map(_.toString))
                    }
                    withClue("päättymispäivä") {
                      opiskeluoikeus.perusopetusTiedot.flatMap(_.päättymispäivä) shouldBe expectedData.perusopetusTiedot.flatMap(_ => expectedData.opiskeluoikeus.päättymispäivä.map(_.toString))
                    }
                    withClue("päättymispäiväMerkittyTulevaisuuteen") {
                      opiskeluoikeus.perusopetusTiedot.flatMap(_.päättymispäiväMerkittyTulevaisuuteen) shouldBe expectedData.perusopetusTiedot.flatMap(_ => expectedData.opiskeluoikeus.päättymispäivä.map(pp => pp.isAfter(defaultMockTarkastelupäivä)))
                    }
                    withClue("valmistunutAiemminTaiLähitulevaisuudessa") {
                      opiskeluoikeus.perusopetusTiedot.map(_.valmistunutAiemminTaiLähitulevaisuudessa) shouldBe expectedData.perusopetusTiedot.map(pt =>
                        expectedData.opiskeluoikeus.tyyppi.koodiarvo == "perusopetus" &&
                          pt.tarkastelupäivänTila == "valmistunut" &&
                          pt.tarkastelupäivänKoskiTila == "valmistunut" &&
                          expectedData.opiskeluoikeus.päättymispäivä.exists(_.isBefore(defaultMockTarkastelupäivä.plusDays(28)))
                      )
                    }
                  }

                  withClue("perusopetuksenJälkeinenTiedot") {
                    withClue("alkamispäivä") {
                      opiskeluoikeus.perusopetuksenJälkeinenTiedot.flatMap(_.alkamispäivä) shouldBe expectedData.perusopetuksenJälkeinenTiedot.flatMap(_ => expectedData.opiskeluoikeus.alkamispäivä.map(_.toString))
                    }
                    withClue("päättymispäivä") {
                      opiskeluoikeus.perusopetuksenJälkeinenTiedot.flatMap(_.päättymispäivä) shouldBe expectedData.perusopetuksenJälkeinenTiedot.flatMap(_ => expectedData.opiskeluoikeus.päättymispäivä.map(_.toString))
                    }
                    withClue("päättymispäiväMerkittyTulevaisuuteen") {
                      opiskeluoikeus.perusopetuksenJälkeinenTiedot.flatMap(_.päättymispäiväMerkittyTulevaisuuteen) shouldBe expectedData.perusopetuksenJälkeinenTiedot.flatMap(_ => expectedData.opiskeluoikeus.päättymispäivä.map(pp => pp.isAfter(defaultMockTarkastelupäivä)))
                    }
                  }
                }
              }
              case (None, Some(expectedData)) =>
                fail(s"Opiskeluoikeus puuttuu: oppija.oid:${expectedOppija.oid} oppija.hetu:${expectedOppija.hetu} opiskeluoikeus.oid:${expectedData.opiskeluoikeus.oid} opiskeluoikeus.tyyppi:${expectedData.opiskeluoikeus.tyyppi.koodiarvo}")
              case (Some(opiskeluoikeus), None) =>
                fail(s"Saatiin ylimääräinen opiskeluoikeus: oppija.oid:${expectedOppija.oid} oppija.hetu:${expectedOppija.hetu} opiskeluoikeus.oid:${opiskeluoikeus.oid} opiskeluoikeus.tyyppi:${opiskeluoikeus.tyyppi.koodiarvo}")
              case _ =>
                fail("Internal error")
            }
          }
        }
      }
    }
  }

  protected def validateOppijaSuppeatTiedot(
    oppija: ValpasOppijaSuppeatTiedot,
    expectedOppija: LaajatOppijaHenkilöTiedot,
    expectedData: List[ExpectedData]
  ): Unit = {
    withClue(s"ValpasOppija(${oppija.henkilö.oid}/${oppija.henkilö.sukunimi}/${oppija.henkilö.etunimet}): ") {
      oppija.henkilö.oid shouldBe expectedOppija.oid
      oppija.henkilö.etunimet shouldBe expectedOppija.etunimet
      oppija.henkilö.sukunimi shouldBe expectedOppija.sukunimi

      val maybeOpiskeluoikeudet = oppija.opiskeluoikeudet.map(o => Some(o))
      val maybeExpectedData = expectedData.map(o => Some(o))

      maybeOpiskeluoikeudet.zipAll(maybeExpectedData, None, None).zipWithIndex.foreach {
        case (element, index) => {
          withClue(s"index ${index}: ") {
            element match {
              case (Some(opiskeluoikeus), Some(expectedData)) => {
                val clue = makeClue("ValpasOpiskeluoikeus", Seq(
                  opiskeluoikeus.oid,
                  opiskeluoikeus.oppilaitos.nimi.get("fi"),
                  opiskeluoikeus.tyyppi.koodiarvo,
                  opiskeluoikeus.tarkasteltavaPäätasonSuoritus.map(_.suorituksenTyyppi.koodiarvo).getOrElse("None")
                ))
                withClue(clue) {
                  validateOpiskeluoikeus(opiskeluoikeus, expectedData)
                }
              }
              case (None, Some(expectedData)) =>
                fail(s"Opiskeluoikeus puuttuu: oppija.oid:${expectedOppija.oid} oppija.hetu:${expectedOppija.hetu} opiskeluoikeus.oid:${expectedData.opiskeluoikeus.oid} opiskeluoikeus.tyyppi:${expectedData.opiskeluoikeus.tyyppi.koodiarvo}")
              case (Some(opiskeluoikeus), None) =>
                fail(s"Saatiin ylimääräinen opiskeluoikeus: oppija.oid:${expectedOppija.oid} oppija.hetu:${expectedOppija.hetu} opiskeluoikeus.oid:${opiskeluoikeus.oid} opiskeluoikeus.tyyppi:${opiskeluoikeus.tyyppi.koodiarvo}")
              case _ =>
                fail("Internal error")
            }
          }
        }
      }
    }
  }

  private def validateOpiskeluoikeus(opiskeluoikeus: ValpasOpiskeluoikeus, expectedData: ExpectedData) = {
    withClue("onHakeutumisValvottava") {
      opiskeluoikeus.onHakeutumisValvottava shouldBe expectedData.onHakeutumisValvottavaOpiskeluoikeus
    }
    withClue("oppilaitos.oid") {
      opiskeluoikeus.oppilaitos.oid shouldBe expectedData.opiskeluoikeus.oppilaitos.get.oid
    }

    withClue("perusopetusTiedot") {
      withClue("tarkastelupäivänTila") {
        opiskeluoikeus.perusopetusTiedot.map(_.tarkastelupäivänTila.koodiarvo) shouldBe expectedData.perusopetusTiedot.map(_.tarkastelupäivänTila)
      }
      withClue("tarkastelupäivänKoskiTila") {
        opiskeluoikeus.perusopetusTiedot.map(_.tarkastelupäivänKoskiTila.koodiarvo) shouldBe expectedData.perusopetusTiedot.map(_.tarkastelupäivänKoskiTila)
      }
      withClue("vuosiluokkiinSitomatonOpetus") {
        opiskeluoikeus.perusopetusTiedot.map(_.vuosiluokkiinSitomatonOpetus) shouldBe expectedData.perusopetusTiedot.map(_.vuosiluokkiinSitomatonOpetus)
      }
    }

    withClue("perusopetuksenJälkeinenTiedot") {
      withClue("tarkastelupäivänTila") {
        opiskeluoikeus.perusopetuksenJälkeinenTiedot.map(_.tarkastelupäivänTila.koodiarvo) shouldBe expectedData.perusopetuksenJälkeinenTiedot.map(_.tarkastelupäivänTila)
      }
      withClue("tarkastelupäivänKoskiTila") {
        opiskeluoikeus.perusopetuksenJälkeinenTiedot.map(_.tarkastelupäivänKoskiTila.koodiarvo) shouldBe expectedData.perusopetuksenJälkeinenTiedot.map(_.tarkastelupäivänKoskiTila)
      }
    }

    val luokkatietoExpectedFromSuoritus = expectedData.opiskeluoikeus match {
      case oo: PerusopetuksenOpiskeluoikeus =>
        oo.suoritukset.flatMap({
          case p: PerusopetuksenVuosiluokanSuoritus => Some(p)
          case _ => None
        }).sortBy(s => s.alkamispäivä).reverse.headOption.map(r => r.luokka)
      case oo: PerusopetuksenLisäopetuksenOpiskeluoikeus =>
        oo.suoritukset.flatMap({
          case p: PerusopetuksenLisäopetuksenSuoritus => Some(p)
          case _ => None
        }).sortBy(s => s.alkamispäivä)(localDateOptionOrdering).reverse.headOption.flatMap(r => r.luokka)
      case oo: InternationalSchoolOpiskeluoikeus =>
        oo.suoritukset.flatMap({
          case p: InternationalSchoolVuosiluokanSuoritus => Some(p)
          case _ => None
        }).sortBy(s => s.alkamispäivä)(localDateOptionOrdering).reverse.headOption.flatMap(r => r.luokka)
      // Esim. lukiossa jne. voi olla monta päätason suoritusta, eikä mitään järkevää sorttausparametria päätasolla (paitsi mahdollisesti oleva vahvistus).
      // => oletetaan, että saadaan taulukossa viimeisenä olevan suorituksen ryhmä
      case oo: Any =>
        oo.suoritukset.flatMap({
          case r: Ryhmällinen => Some(r)
          case _ => None
        }).reverse.headOption.flatMap(_.ryhmä)
    }
    withClue("ryhmä") {
      opiskeluoikeus.tarkasteltavaPäätasonSuoritus.get.ryhmä shouldBe luokkatietoExpectedFromSuoritus
    }
  }

  protected def täydennäAikaleimallaJaOrganisaatiotiedoilla(
    kuntailmoitus: ValpasKuntailmoitusLaajatTiedot,
    aikaleima: LocalDateTime = rajapäivätService.tarkastelupäivä.atStartOfDay
  ): ValpasKuntailmoitusLaajatTiedot  = {
    // Yksinkertaista vertailukoodia testissä tekemällä samat aikaleiman ja organisaatiodatan täydennykset mitkä tehdään tuotantokoodissa.
    kuntailmoitus.copy(
      aikaleima = Some(aikaleima),
      tekijä = kuntailmoitus.tekijä.copy(
        organisaatio = organisaatioRepository.getOrganisaatio(kuntailmoitus.tekijä.organisaatio.oid).get
      ),
      kunta = organisaatioRepository.getOrganisaatio(kuntailmoitus.kunta.oid).get
    )
  }

  protected def karsiPerustietoihin(kuntailmoitus: ValpasKuntailmoitusLaajatTiedot): ValpasKuntailmoitusLaajatTiedot = {
    kuntailmoitus.copy(
      tekijä = kuntailmoitus.tekijä.copy(
        henkilö = None
      ),
      yhteydenottokieli = None,
      oppijanYhteystiedot = None,
      hakenutMuualle = None
    )
  }

  protected def oppijanPuhelinnumerolla(puhelinnumero: String, kuntailmoitus: ValpasKuntailmoitusLaajatTiedot): ValpasKuntailmoitusLaajatTiedot =
    kuntailmoitus.copy(
      oppijanYhteystiedot = Some(kuntailmoitus.oppijanYhteystiedot.get.copy(
        puhelinnumero = Some(puhelinnumero)
      ))
    )

  protected def validateKuntailmoitukset(oppija: OppijaHakutilanteillaLaajatTiedot, expectedIlmoitukset: Seq[ValpasKuntailmoitusLaajatTiedot]) = {
    def clueMerkkijono(kuntailmoitus: ValpasKuntailmoitusLaajatTiedot): String =
      s"${kuntailmoitus.tekijä.organisaatio.nimi.get.get("fi")}=>${kuntailmoitus.kunta.kotipaikka.get.nimi.get.get("fi")}"

    val maybeIlmoitukset = oppija.kuntailmoitukset.map(o => Some(o))
    val maybeExpectedData = expectedIlmoitukset.map(o => Some(o))

    maybeIlmoitukset.zipAll(maybeExpectedData, None, None).zipWithIndex.foreach {
      case (element, index) => {
        withClue(s"index ${index}: ") {
          element match {
            case (Some(kuntailmoitusLisätiedoilla), Some(expectedData)) => {
              val clue = makeClue("ValpasKuntailmoitusLaajatTiedot", Seq(
                s"${kuntailmoitusLisätiedoilla.id}",
                clueMerkkijono(kuntailmoitusLisätiedoilla)
              ))

              withClue(clue) {
                withClue("aktiivinen") {
                  kuntailmoitusLisätiedoilla.aktiivinen should equal(expectedData.aktiivinen)
                }
                withClue("kunta") {
                  kuntailmoitusLisätiedoilla.kunta should equal(expectedData.kunta)
                }
                withClue("aikaleiman päivämäärä") {
                  kuntailmoitusLisätiedoilla.aikaleima.map(_.toLocalDate) should equal(expectedData.aikaleima.map(_.toLocalDate))
                }
                withClue("tekijä") {
                  kuntailmoitusLisätiedoilla.tekijä should equal(expectedData.tekijä)
                }
                withClue("yhteydenottokieli") {
                  kuntailmoitusLisätiedoilla.yhteydenottokieli should equal(expectedData.yhteydenottokieli)
                }
                withClue("oppijanYhteystiedot") {
                  kuntailmoitusLisätiedoilla.oppijanYhteystiedot should equal(expectedData.oppijanYhteystiedot)
                }
                withClue("hakenutMuualle") {
                  kuntailmoitusLisätiedoilla.hakenutMuualle should equal(expectedData.hakenutMuualle)
                }
              }
            }
            case (None, Some(expectedData)) =>
              fail(s"Ilmoitus puuttuu: oppija.oid:${oppija.oppija.henkilö.oid} oppija.hetu:${oppija.oppija.henkilö.hetu} ilmoitus:${clueMerkkijono(expectedData)}")
            case (Some(kuntailmoitusLisätiedoilla), None) =>
              fail(s"Saatiin ylimääräinen ilmoitus: oppija.oid:${oppija.oppija.henkilö.oid} oppija.hetu:${oppija.oppija.henkilö.hetu} ilmoitus:${clueMerkkijono(kuntailmoitusLisätiedoilla)}")
            case _ =>
              fail("Internal error")
          }
        }
      }
    }
  }

  private def makeClue(otsikko: String, tiedot: Seq[String]): String =
    s"${otsikko}(${tiedot.mkString("/")}) :"
}
