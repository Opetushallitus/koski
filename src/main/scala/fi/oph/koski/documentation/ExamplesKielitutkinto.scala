package fi.oph.koski.documentation

import fi.oph.koski.henkilo.MockOppijat.asUusiOppija
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, LaajatOppijaHenkilöTiedot}
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._

import java.time.LocalDate
import scala.collection.immutable

object ExamplesKielitutkinto {
  lazy val exampleMockOppija: LaajatOppijaHenkilöTiedot = KoskiSpecificMockOppijat.kielitutkinnonSuorittaja
  lazy val exampleHenkilö: UusiHenkilö = asUusiOppija(exampleMockOppija)

  lazy val examples: immutable.Seq[Example] = List(
    Example(
      "kielitutkinto - yleinen kielitutkinto",
      "Vanhemman rakenteen mukainen yleisen kielitutkinnon suoritus (sisältää yleisarvosanan)",
      Oppija(exampleHenkilö.copy(hetu = "160586-873P"), Seq(YleisetKielitutkinnot.opiskeluoikeus(LocalDate.of(2011, 1, 1), "FI", "kt"))),
    )
  )

  object YleisetKielitutkinnot {
    def opiskeluoikeus(tutkintopäivä: LocalDate, kieli: String, tutkintotaso: String): KielitutkinnonOpiskeluoikeus = {
      val arviointipäivä = tutkintopäivä.plusDays(60)
      KielitutkinnonOpiskeluoikeus(
        tila = KielitutkinnonOpiskeluoikeudenTila(
          opiskeluoikeusjaksot = List(
            Opiskeluoikeusjakso.tutkintopäivä(tutkintopäivä),
            Opiskeluoikeusjakso.valmis(arviointipäivä),
          )
        ),
        suoritukset = List(
          päätasonSuoritus(tutkintotaso, kieli, arviointipäivä)
        )
      )
    }

    def päätasonSuoritus(tutkintotaso: String, kieli: String, arviointipäivä: LocalDate): YleisenKielitutkinnonSuoritus = {
      val arvosana = tutkintotaso match {
        case "pt" => 1
        case "kt" => 3
        case "yt" => 5
      }
      YleisenKielitutkinnonSuoritus(
        koulutusmoduuli = YleinenKielitutkinto(
          tunniste = Koodistokoodiviite(tutkintotaso.toString, "ykitutkintotaso"),
          kieli = Koodistokoodiviite(kieli, "kieli"),
        ),
        toimipiste = OidOrganisaatio(MockOrganisaatiot.varsinaisSuomenKansanopistoToimipiste),
        vahvistus = Some(Päivämäärävahvistus(
          päivä = arviointipäivä,
          myöntäjäOrganisaatio = OidOrganisaatio(MockOrganisaatiot.helsinginKaupunki),
        )),
        osasuoritukset = Some(List(
          tutkinnonOsa("tekstinymmartaminen", s"$arvosana", arviointipäivä),
          tutkinnonOsa("kirjoittaminen", s"alle$arvosana", arviointipäivä),
          tutkinnonOsa("puheenymmartaminen", s"${arvosana + 1}", arviointipäivä),
          tutkinnonOsa("puhuminen", s"$arvosana", arviointipäivä),
        )),
        yleisarvosana = if (arviointipäivä.isBefore(LocalDate.of(2012, 1, 1))) Some(Koodistokoodiviite(s"$arvosana", "ykiarvosana")) else None,
      )
    }

    def tutkinnonOsa(tyyppi: String, arvosana: String, arviointiPäivä: LocalDate): YleisenKielitutkinnonOsakokeenSuoritus =
      YleisenKielitutkinnonOsakokeenSuoritus(
        koulutusmoduuli = YleisenKielitutkinnonOsakoe(
          tunniste = Koodistokoodiviite(tyyppi, "ykisuorituksenosa"),
        ),
        arviointi = Some(List(
          YleisenKielitutkinnonOsakokeenArviointi(
            arvosana = Koodistokoodiviite(arvosana, "ykiarvosana"),
            päivä = arviointiPäivä,
          )
        ))
      )
  }

  object ValtionhallinnonKielitutkinnot {
    def opiskeluoikeus(tutkintopäivä: LocalDate, kieli: String, kielitaidot: List[String], tutkintotaso: String): KielitutkinnonOpiskeluoikeus = {
      val arviointipäivä = tutkintopäivä.plusDays(60)
      KielitutkinnonOpiskeluoikeus(
        tila = KielitutkinnonOpiskeluoikeudenTila(
          opiskeluoikeusjaksot = List(
            Opiskeluoikeusjakso.tutkintopäivä(tutkintopäivä),
            Opiskeluoikeusjakso.valmis(arviointipäivä),
          )
        ),
        suoritukset = List(
          päätasonSuoritus(kielitaidot, tutkintotaso, kieli, arviointipäivä)
        )
      )
    }

    def päätasonSuoritus(
      kielitaidot: List[String],
      tutkintotaso: String,
      kieli: String,
      arviointipäivä: LocalDate,
    ): ValtionhallinnonKielitutkinnonSuoritus = {
      val arvosana = tutkintotaso match {
        case "erinomainen" => "erinomainen"
        case "hyvajatyydyttava" => "hyva"
      }
      ValtionhallinnonKielitutkinnonSuoritus(
        koulutusmoduuli = ValtionhallinnonKielitutkinto(
          tunniste = Koodistokoodiviite(tutkintotaso, "vkttutkintotaso"),
          kieli = Koodistokoodiviite(kieli, "kieli"),
        ),
        toimipiste = OidOrganisaatio(MockOrganisaatiot.varsinaisSuomenKansanopistoToimipiste),
        vahvistus = Some(Päivämäärävahvistus(
          päivä = arviointipäivä,
          myöntäjäOrganisaatio = OidOrganisaatio(MockOrganisaatiot.helsinginKaupunki),
        )),
        osasuoritukset = Some(kielitaidot.map {
          case "kirjallinen" => Kielitaidot.Kirjallinen.suoritus(arviointipäivä, arvosana)
          case "suullinen" => Kielitaidot.Suullinen.suoritus(arviointipäivä, arvosana)
          case "ymmartaminen" => Kielitaidot.Ymmärtäminen.suoritus(arviointipäivä, arvosana)
        }),
      )
    }

    object Kielitaidot {
      object Suullinen {
        def suoritus(pvm: LocalDate, arvosana: String): ValtionhallinnonKielitutkinnonSuullisenKielitaidonSuoritus =
          ValtionhallinnonKielitutkinnonSuullisenKielitaidonSuoritus(
            koulutusmoduuli = ValtionhallinnonKielitutkinnonSuullinenKielitaito(),
            osasuoritukset = Some(List(
              osakoe("puhuminen", arvosana, pvm),
              osakoe("puheenymmartaminen", arvosana, pvm),
            )),
            vahvistus = Some(Päivämäärävahvistus(
              päivä = pvm,
              myöntäjäOrganisaatio = OidOrganisaatio(MockOrganisaatiot.helsinginKaupunki),
            )),
            arviointi = Some(List(ValtionhallinnonKielitutkinnonArviointi(
              arvosana = Koodistokoodiviite(arvosana, "vktarvosana"),
              päivä = pvm,
            )))
          )

        def osakoe(osakoe: String, arvosana: String, arviointiPäivä: LocalDate): ValtionhallinnonKielitutkinnonSuullisenKielitaidonOsakokeenSuoritus =
          ValtionhallinnonKielitutkinnonSuullisenKielitaidonOsakokeenSuoritus(
            koulutusmoduuli = osakoe match {
              case "puhuminen" => ValtionhallinnonPuhumisenOsakoe()
              case "puheenymmartaminen" => ValtionhallinnonPuheenYmmärtämisenOsakoe()
            },
            arviointi = Some(List(
              ValtionhallinnonKielitutkinnonArviointi(
                arvosana = Koodistokoodiviite(arvosana, "vktarvosana"),
                päivä = arviointiPäivä,
              )
            ))
          )
      }

      object Kirjallinen {
        def suoritus(pvm: LocalDate, arvosana: String): ValtionhallinnonKielitutkinnonKirjallisenKielitaidonSuoritus =
          ValtionhallinnonKielitutkinnonKirjallisenKielitaidonSuoritus(
            koulutusmoduuli = ValtionhallinnonKielitutkinnonKirjallinenKielitaito(),
            osasuoritukset = Some(List(
              osakoe("kirjoittaminen", arvosana, pvm),
              osakoe("tekstinymmartaminen", arvosana, pvm),
            )),
            vahvistus = Some(Päivämäärävahvistus(
              päivä = pvm,
              myöntäjäOrganisaatio = OidOrganisaatio(MockOrganisaatiot.helsinginKaupunki),
            )),
            arviointi = Some(List(ValtionhallinnonKielitutkinnonArviointi(
              arvosana = Koodistokoodiviite(arvosana, "vktarvosana"),
              päivä = pvm,
            )))
          )

        def osakoe(osakoe: String, arvosana: String, arviointiPäivä: LocalDate): ValtionhallinnonKielitutkinnonKirjallisenKielitaidonOsakokeenSuoritus =
          ValtionhallinnonKielitutkinnonKirjallisenKielitaidonOsakokeenSuoritus(
            koulutusmoduuli = osakoe match {
              case "kirjoittaminen" => ValtionhallinnonKirjoittamisenOsakoe()
              case "tekstinymmartaminen" => ValtionhallinnonTekstinYmmärtämisenOsakoe()
            },
            arviointi = Some(List(
              ValtionhallinnonKielitutkinnonArviointi(
                arvosana = Koodistokoodiviite(arvosana, "vktarvosana"),
                päivä = arviointiPäivä,
              )
            ))
          )
      }

      object Ymmärtäminen {
        def suoritus(pvm: LocalDate, arvosana: String): ValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonSuoritus =
          ValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonSuoritus(
            koulutusmoduuli = ValtionhallinnonKielitutkinnonYmmärtämisenKielitaito(),
            osasuoritukset = Some(List(
              osakoe("tekstinymmartaminen", arvosana, pvm),
              osakoe("puheenymmartaminen", arvosana, pvm),
            )),
            vahvistus = Some(Päivämäärävahvistus(
              päivä = pvm,
              myöntäjäOrganisaatio = OidOrganisaatio(MockOrganisaatiot.helsinginKaupunki),
            )),
            arviointi = Some(List(ValtionhallinnonKielitutkinnonArviointi(
              arvosana = Koodistokoodiviite(arvosana, "vktarvosana"),
              päivä = pvm,
            )))
          )


        def osakoe(osakoe: String, arvosana: String, arviointiPäivä: LocalDate): ValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonOsakokeenSuoritus =
          ValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonOsakokeenSuoritus(
            koulutusmoduuli = osakoe match {
              case "puheenymmartaminen" => ValtionhallinnonPuheenYmmärtämisenOsakoe()
              case "tekstinymmartaminen" => ValtionhallinnonTekstinYmmärtämisenOsakoe()
            },
            arviointi = Some(List(
              ValtionhallinnonKielitutkinnonArviointi(
                arvosana = Koodistokoodiviite(arvosana, "vktarvosana"),
                päivä = arviointiPäivä,
              )
            ))
          )
      }
    }
  }

  object Opiskeluoikeusjakso {
    def tutkintopäivä(alku: LocalDate): KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso =
      KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso(
        alku = alku,
        tila = Koodistokoodiviite("lasna", "koskiopiskeluoikeudentila"),
      )

    def valmis(alku: LocalDate): KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso =
      KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso(
        alku = alku,
        tila = Koodistokoodiviite("hyvaksytystisuoritettu", "koskiopiskeluoikeudentila"),
      )
  }

}
