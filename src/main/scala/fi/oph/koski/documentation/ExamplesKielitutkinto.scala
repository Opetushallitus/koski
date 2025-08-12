package fi.oph.koski.documentation

import fi.oph.koski.henkilo.MockOppijat.asUusiOppija
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, LaajatOppijaHenkilöTiedot}
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.organisaatio.MockOrganisaatiot.ValtionhallinnonKielitutkinnotOrg
import fi.oph.koski.schema._
import fi.oph.koski.util.DateOrdering.localDateOrdering

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
    ),
    Example(
      "kielitutkinto - valtionhallinnon kielitutkinto",
      "",
      Oppija(
        exampleHenkilö.copy(hetu = "160586-873P"),
        Seq(ValtionhallinnonKielitutkinnot.Opiskeluoikeus.valmis(LocalDate.of(2011, 1, 1), "FI", List("kirjallinen", "suullinen"), "erinomainen"))
      ),
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
    object Opiskeluoikeus {
      def valmis(tutkintopäivä: LocalDate, kieli: String, kielitaidot: List[String], tutkintotaso: String): KielitutkinnonOpiskeluoikeus = {
        val arviointipäivä = tutkintopäivä.plusDays(60)
        val suoritus = päätasonSuoritus(
          kielitaidot,
          tutkintotaso,
          kieli,
          arviointipäivä,
          osakokeidenArvosanat = tutkintotaso match {
            case "erinomainen" => List("erinomainen")
            case "hyvajatyydyttava" => List("hyva", "tyydyttava")
          },
        )

        KielitutkinnonOpiskeluoikeus(
          tila = KielitutkinnonOpiskeluoikeudenTila(
            opiskeluoikeusjaksot = List(
              Opiskeluoikeusjakso.tutkintopäivä(tutkintopäivä),
              Opiskeluoikeusjakso.valmis(suoritus.vahvistus.get.päivä),
            )
          ),
          suoritukset = List(suoritus)
        )
      }

      def keskeneräinen(tutkintopäivä: LocalDate, kieli: String, kielitaidot: List[String], tutkintotaso: String): KielitutkinnonOpiskeluoikeus = {
        val arviointipäivä = tutkintopäivä.plusDays(60)
        KielitutkinnonOpiskeluoikeus(
          tila = KielitutkinnonOpiskeluoikeudenTila(
            opiskeluoikeusjaksot = List(
              Opiskeluoikeusjakso.tutkintopäivä(tutkintopäivä),
            )
          ),
          suoritukset = List(
            päätasonSuoritus(
              kielitaidot,
              tutkintotaso,
              kieli,
              arviointipäivä,
              osakokeidenArvosanat = tutkintotaso match {
                case "erinomainen" => List("erinomainen", "hylatty")
                case "hyvajatyydyttava" => List("tyydyttava", "hylatty")
              },
            )
          )
        )
      }
    }

    def päätasonSuoritus(
      kielitaidot: List[String],
      tutkintotaso: String,
      kieli: String,
      arviointipäivä: LocalDate,
      osakokeidenArvosanat: List[String],
    ): ValtionhallinnonKielitutkinnonSuoritus = {
      val osasuoritukset = kielitaidot.map {
        case "kirjallinen" => Kielitaidot.Kirjallinen.suoritus(arviointipäivä, osakokeidenArvosanat)
        case "suullinen" => Kielitaidot.Suullinen.suoritus(arviointipäivä, osakokeidenArvosanat)
        case "ymmartaminen" => Kielitaidot.Ymmärtäminen.suoritus(arviointipäivä, osakokeidenArvosanat)
      }

      val viimeisinArviointipäivä = osasuoritukset
        .flatMap(_.arviointi.toList.flatten)
        .map(_.päivä)
        .max

      val organisaatio = tutkintotaso match {
        case "erinomainen" => OidOrganisaatio(ValtionhallinnonKielitutkinnotOrg.organisaatio)
        case _ => OidOrganisaatio(MockOrganisaatiot.varsinaisSuomenKansanopistoToimipiste)
      }

      ValtionhallinnonKielitutkinnonSuoritus(
        koulutusmoduuli = ValtionhallinnonKielitutkinto(
          tunniste = Koodistokoodiviite(tutkintotaso, "vkttutkintotaso"),
          kieli = Koodistokoodiviite(kieli, "kieli"),
        ),
        toimipiste = organisaatio,
        vahvistus = if (osakokeidenArvosanat.contains("hylatty")) None else Some(HenkilövahvistusValinnaisellaTittelilläJaValinnaisellaPaikkakunnalla(
          päivä = viimeisinArviointipäivä,
          myöntäjäOrganisaatio = organisaatio,
          myöntäjäHenkilöt = List(
            OrganisaatiohenkilöValinnaisellaTittelillä(
              nimi = "Vallu Vastaanottaja",
              organisaatio = organisaatio,
            )
          ),
          paikkakunta = Some(Koodistokoodiviite("853", "kunta")),
        )),
        osasuoritukset = Some(osasuoritukset),
      )
    }

    object Kielitaidot {
      def arviointi(arvosana: String, pvm: LocalDate): Option[List[ValtionhallinnonKielitutkinnonArviointi]] = Some(List(ValtionhallinnonKielitutkinnonArviointi(
        arvosana = Koodistokoodiviite(arvosana, "vktarvosana"),
        päivä = pvm,
      )))

      object Suullinen {
        def suoritus(pvm: LocalDate, osakokeidenArvosanat: List[String]): ValtionhallinnonKielitutkinnonSuullisenKielitaidonSuoritus =
          ValtionhallinnonKielitutkinnonSuullisenKielitaidonSuoritus(
            koulutusmoduuli = ValtionhallinnonKielitutkinnonSuullinenKielitaito(),
            osasuoritukset = Some(List(
              osakoe("puhuminen", osakokeenArvosana(osakokeidenArvosanat, 0), pvm),
              osakoe("puheenymmartaminen", osakokeenArvosana(osakokeidenArvosanat, 1), pvm),
            )),
            arviointi =  kielitaidonArviointi(osakokeidenArvosanat, pvm),
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
        def suoritus(pvm: LocalDate, osakokeidenArvosanat: List[String]): ValtionhallinnonKielitutkinnonKirjallisenKielitaidonSuoritus = {
          val osasuoritukset = List(
            osakoe("kirjoittaminen", List(
              "hylatty",
              osakokeenArvosana(osakokeidenArvosanat, 0),
            ), pvm),
            osakoe("tekstinymmartaminen", List(osakokeenArvosana(osakokeidenArvosanat, 1)), pvm),
          )

          val viimeisinArviointipäivä = osasuoritukset
            .flatMap(_.arviointi.toList.flatten)
            .map(_.päivä)
            .max

          ValtionhallinnonKielitutkinnonKirjallisenKielitaidonSuoritus(
            koulutusmoduuli = ValtionhallinnonKielitutkinnonKirjallinenKielitaito(),
            osasuoritukset = Some(osasuoritukset),
            arviointi = kielitaidonArviointi(osakokeidenArvosanat, viimeisinArviointipäivä),
          )
        }

        def osakoe(osakoe: String, arvosanat: List[String], arviointiPäivä: LocalDate): ValtionhallinnonKielitutkinnonKirjallisenKielitaidonOsakokeenSuoritus =
          ValtionhallinnonKielitutkinnonKirjallisenKielitaidonOsakokeenSuoritus(
            koulutusmoduuli = osakoe match {
              case "kirjoittaminen" => ValtionhallinnonKirjoittamisenOsakoe()
              case "tekstinymmartaminen" => ValtionhallinnonTekstinYmmärtämisenOsakoe()
            },
            arviointi = Some(arvosanat.zipWithIndex.map { case (arvosana, index) =>
              ValtionhallinnonKielitutkinnonArviointi(
                arvosana = Koodistokoodiviite(arvosana, "vktarvosana"),
                päivä = arviointiPäivä.plusMonths(index),
              )
            }),
          )
      }

      object Ymmärtäminen {
        def suoritus(pvm: LocalDate, osakokeidenArvosanat: List[String]): ValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonSuoritus =
          ValtionhallinnonKielitutkinnonYmmärtämisenKielitaidonSuoritus(
            koulutusmoduuli = ValtionhallinnonKielitutkinnonYmmärtämisenKielitaito(),
            osasuoritukset = Some(List(
              osakoe("tekstinymmartaminen", osakokeenArvosana(osakokeidenArvosanat, 0), pvm),
              osakoe("puheenymmartaminen", osakokeenArvosana(osakokeidenArvosanat, 1), pvm),
            )),
            arviointi = kielitaidonArviointi(osakokeidenArvosanat, pvm),
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

      private def kielitaidonArviointi(osakokeidenArvosanat: List[String], pvm: LocalDate): Option[List[ValtionhallinnonKielitutkinnonArviointi]] =
        arviointi(Arviointi.huonoinArvosana(osakokeidenArvosanat), pvm)

      private def osakokeenArvosana(osakokeidenArvosanat: List[String], index: Int): String =
        osakokeidenArvosanat(index % osakokeidenArvosanat.length)
    }

    object Arviointi {
      val arvosanajärjestys = List("hylatty", "tyydyttava", "hyva", "erinomainen")
      implicit val order: Ordering[String] = Ordering.fromLessThan(
        (a: String, b: String) => arvosanajärjestys.indexOf(a) < arvosanajärjestys.indexOf(b)
      )

      def huonoinArvosana(arvosanat: List[String]) = arvosanat.min
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

    def päättynyt(alku: LocalDate): KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso =
      KielitutkinnonOpiskeluoikeudenOpiskeluoikeusjakso(
        alku = alku,
        tila = Koodistokoodiviite("paattynyt", "koskiopiskeluoikeudentila"),
      )
  }
}
