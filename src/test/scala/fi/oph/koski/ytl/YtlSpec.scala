package fi.oph.koski.ytl

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.api.OpiskeluoikeusTestMethodsAmmatillinen
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, LaajatOppijaHenkilöTiedot, OppijaHenkilöWithMasterInfo}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{MockUser, MockUsers}
import fi.oph.koski.log.AuditLogTester
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class YtlSpec
  extends AnyFreeSpec
    with KoskiHttpSpec
    with OpiskeluoikeusTestMethodsAmmatillinen
    with Matchers
    with BeforeAndAfterAll {

  "YTL rajapinta" - {
    "Yhden oppijan hakeminen hetulla onnistuu ja tuottaa auditlog viestin" in {
      AuditLogTester.clearMessages
      val hetut = List(
        KoskiSpecificMockOppijat.amis
      ).map(_.hetu.get)


      postHetut(hetut) {
        verifyResponseStatusOk()
        val response = JsonSerializer.parse[List[YtlOppija]](body)

        response.length should equal(1)
        response(0).opiskeluoikeudet.length should equal(1)

        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_KATSOMINEN", "target" -> Map("oppijaHenkiloOid" -> KoskiSpecificMockOppijat.amis.oid)))
      }
    }

    "Ei voi kutsua ilman YTL-käyttöoikeutta" in {
      val hetut = List(
        KoskiSpecificMockOppijat.amis
      ).map(_.hetu.get)

      postHetut(hetut, MockUsers.luovutuspalveluKäyttäjäArkaluontoinen) {
        verifyResponseStatus(403, KoskiErrorCategory.forbidden.kiellettyKäyttöoikeus("Ei sallittu näillä käyttöoikeuksilla"))
      }
    }

    "Yhden oppijan, jolla useampi opiskeluoikeus, hakeminen oidilla onnistuu, tuottaa auditlog viestin ja palauttaa oikeat tiedot" in {
      AuditLogTester.clearMessages
      val oidit = List(
        KoskiSpecificMockOppijat.maksuttomuuttaPidennetty2
      ).map(_.oid)

      postOidit(oidit) {
        verifyResponseStatusOk()
        val response = JsonSerializer.parse[List[YtlOppija]](body)
        response.length should equal(1)
        response(0).opiskeluoikeudet.length should equal(2)
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_KATSOMINEN", "target" -> Map("oppijaHenkiloOid" -> KoskiSpecificMockOppijat.maksuttomuuttaPidennetty2.oid)))

        // Järjestä opiskeluoikeudet samaan järjestykseen kuin vertailudatassa
        val järjestettyResponse = response.updated(0, response(0).copy(opiskeluoikeudet = response(0).opiskeluoikeudet.sortBy(_.tyyppi.koodiarvo)))

        val expectedResponse = JsonSerializer.parse[List[YtlOppija]](
          expectedMaksuttomuuttaPidennetty2(
            järjestettyResponse(0).opiskeluoikeudet.map(_.oid).map(_.getOrElse("")),
            järjestettyResponse(0).opiskeluoikeudet.map(_.aikaleima).map(_.map(_.toString).getOrElse(""))
          )
        )
        järjestettyResponse should equal(expectedResponse)
      }
    }

    "Usean oppijan hakeminen hetulla onnistuu" in {
      val hetut = List(
        KoskiSpecificMockOppijat.amis,
        KoskiSpecificMockOppijat.lukioKesken,
        KoskiSpecificMockOppijat.lukionAineopiskelija,
        KoskiSpecificMockOppijat.uusiLukio,
        KoskiSpecificMockOppijat.ylioppilasLukiolainen,
        KoskiSpecificMockOppijat.ibFinal,
        KoskiSpecificMockOppijat.ibPreIB2019,
        KoskiSpecificMockOppijat.internationalschool,
        KoskiSpecificMockOppijat.dia,
        KoskiSpecificMockOppijat.eskari,
        KoskiSpecificMockOppijat.eiKoskessa
      ).map(_.hetu.get)

      postHetut(hetut) {
        verifyResponseStatusOk()
        val response = JsonSerializer.parse[List[YtlOppija]](body)
        response.length should equal(7)
      }
    }

    "Kosken testioppijoiden hakeminen oidilla onnistuu" in {
      val oidit = List(
        KoskiSpecificMockOppijat.hetuton,
        KoskiSpecificMockOppijat.syntymäajallinen,
        KoskiSpecificMockOppijat.eero,
        KoskiSpecificMockOppijat.eerola,
        KoskiSpecificMockOppijat.markkanen,
        KoskiSpecificMockOppijat.teija,
        KoskiSpecificMockOppijat.tero,
        KoskiSpecificMockOppijat.presidentti,
        KoskiSpecificMockOppijat.koululainen,
        KoskiSpecificMockOppijat.suoritusTuplana,
        KoskiSpecificMockOppijat.luokallejäänyt,
        KoskiSpecificMockOppijat.ysiluokkalainen,
        KoskiSpecificMockOppijat.vuosiluokkalainen,
        KoskiSpecificMockOppijat.monessaKoulussaOllut,
        KoskiSpecificMockOppijat.lukiolainen,
        KoskiSpecificMockOppijat.lukioKesken,
        KoskiSpecificMockOppijat.uusiLukio,
        KoskiSpecificMockOppijat.uusiLukionAineopiskelija,
        KoskiSpecificMockOppijat.lukionAineopiskelija,
        KoskiSpecificMockOppijat.lukionAineopiskelijaAktiivinen,
        KoskiSpecificMockOppijat.lukionEiTiedossaAineopiskelija,
        KoskiSpecificMockOppijat.ammattilainen,
        KoskiSpecificMockOppijat.tutkinnonOsaaPienempiKokonaisuus,
        KoskiSpecificMockOppijat.muuAmmatillinen,
        KoskiSpecificMockOppijat.muuAmmatillinenKokonaisuuksilla,
        KoskiSpecificMockOppijat.ammatilliseenTetäväänValmistavaMuuAmmatillinen,
        KoskiSpecificMockOppijat.erkkiEiperusteissa,
        KoskiSpecificMockOppijat.amis,
        KoskiSpecificMockOppijat.dippainssi,
        KoskiSpecificMockOppijat.korkeakoululainen,
        KoskiSpecificMockOppijat.amkValmistunut,
        KoskiSpecificMockOppijat.opintojaksotSekaisin,
        KoskiSpecificMockOppijat.amkKesken,
        KoskiSpecificMockOppijat.amkKeskeytynyt,
        KoskiSpecificMockOppijat.monimutkainenKorkeakoululainen,
        KoskiSpecificMockOppijat.virtaEiVastaa,
        KoskiSpecificMockOppijat.oppiaineenKorottaja,
        KoskiSpecificMockOppijat.montaOppiaineenOppimäärääOpiskeluoikeudessa,
        KoskiSpecificMockOppijat.virtaKaksiPäätösonSuoritusta,
        KoskiSpecificMockOppijat.aikuisOpiskelija,
        KoskiSpecificMockOppijat.aikuisOpiskelijaMuuKuinVos,
        KoskiSpecificMockOppijat.aikuisAineOpiskelijaMuuKuinVos,
        KoskiSpecificMockOppijat.aikuisOpiskelijaVieraskielinen,
        KoskiSpecificMockOppijat.aikuisOpiskelijaVieraskielinenMuuKuinVos,
        KoskiSpecificMockOppijat.aikuisOpiskelijaMuuRahoitus,
        KoskiSpecificMockOppijat.kymppiluokkalainen,
        KoskiSpecificMockOppijat.luva,
        KoskiSpecificMockOppijat.luva2019,
        KoskiSpecificMockOppijat.valma,
        KoskiSpecificMockOppijat.ylioppilas,
        KoskiSpecificMockOppijat.ylioppilasLukiolainen,
        KoskiSpecificMockOppijat.ylioppilasEiOppilaitosta,
        KoskiSpecificMockOppijat.toimintaAlueittainOpiskelija,
        KoskiSpecificMockOppijat.telma,
        KoskiSpecificMockOppijat.erikoisammattitutkinto,
        KoskiSpecificMockOppijat.reformitutkinto,
        KoskiSpecificMockOppijat.osittainenammattitutkinto,
        KoskiSpecificMockOppijat.ammatillisenOsittainenRapsa,
        KoskiSpecificMockOppijat.paikallinenTunnustettu,
        KoskiSpecificMockOppijat.tiedonsiirto,
        KoskiSpecificMockOppijat.perusopetuksenTiedonsiirto,
        KoskiSpecificMockOppijat.omattiedot,
        KoskiSpecificMockOppijat.ibFinal,
        KoskiSpecificMockOppijat.ibPredicted,
        KoskiSpecificMockOppijat.ibPreIB2019,
        KoskiSpecificMockOppijat.dia,
        KoskiSpecificMockOppijat.internationalschool,
        KoskiSpecificMockOppijat.eskari,
        KoskiSpecificMockOppijat.eskariAikaisillaLisätiedoilla,
        KoskiSpecificMockOppijat.master,
        KoskiSpecificMockOppijat.slave,
        KoskiSpecificMockOppijat.masterEiKoskessa,
        KoskiSpecificMockOppijat.slaveMasterEiKoskessa,
        KoskiSpecificMockOppijat.omattiedotSlave,
        KoskiSpecificMockOppijat.opiskeluoikeudenOidKonflikti,
        KoskiSpecificMockOppijat.eiKoskessa,
        KoskiSpecificMockOppijat.eiKoskessaHetuton,
        KoskiSpecificMockOppijat.turvakielto,
        KoskiSpecificMockOppijat.montaJaksoaKorkeakoululainen,
        KoskiSpecificMockOppijat.organisaatioHistoria,
        KoskiSpecificMockOppijat.valtuutusOppija,
        KoskiSpecificMockOppijat.siirtoOpiskelijaVirta,
        KoskiSpecificMockOppijat.faija,
        KoskiSpecificMockOppijat.faijaFeilaa,
        KoskiSpecificMockOppijat.koulusivistyskieliYlioppilas,
        KoskiSpecificMockOppijat.montaKoulusivityskieltäYlioppilas,
        KoskiSpecificMockOppijat.labammattikoulu,
        KoskiSpecificMockOppijat.valviraaKiinnostavaTutkinto,
        KoskiSpecificMockOppijat.valviraaKiinnostavaTutkintoKesken,
        KoskiSpecificMockOppijat.kelaErityyppisiaOpiskeluoikeuksia,
        KoskiSpecificMockOppijat.lukioDiaIbInternationalOpiskelijamaaratRaportti_nuortenOppimaara,
        KoskiSpecificMockOppijat.lukioDiaIbInternationalOpiskelijamaaratRaportti_aikuistenOppimaara,
        KoskiSpecificMockOppijat.lukioDiaIbInternationalOpiskelijamaaratRaportti_aineopiskelija,
        KoskiSpecificMockOppijat.lukioDiaIbInternationalOpiskelijamaaratRaportti_dia,
        KoskiSpecificMockOppijat.lukioDiaIbInternationalOpiskelijamaaratRaportti_ib,
        KoskiSpecificMockOppijat.lukioDiaIbInternationalOpiskelijamaaratRaportti_international,
        KoskiSpecificMockOppijat.perusopetusOppijaMaaratRaportti_tavallinen,
        KoskiSpecificMockOppijat.perusopetusOppijaMaaratRaportti_erikois,
        KoskiSpecificMockOppijat.perusopetusOppijaMaaratRaportti_virheellisestiSiirretty,
        KoskiSpecificMockOppijat.perusopetusOppijaMaaratRaportti_virheellisestiSiirrettyVieraskielinen,
        KoskiSpecificMockOppijat.perusopetusOppijaMaaratRaportti_kotiopetus,
        KoskiSpecificMockOppijat.organisaatioHistoriallinen,
        KoskiSpecificMockOppijat.lukioKurssikertymaRaportti_oppimaara,
        KoskiSpecificMockOppijat.lukioKurssikertymaRaportti_aineopiskelija_eronnut,
        KoskiSpecificMockOppijat.lukioKurssikertymaRaportti_aineopiskelija_valmistunut,
        KoskiSpecificMockOppijat.luvaOpiskelijamaaratRaportti_nuortenOppimaara,
        KoskiSpecificMockOppijat.luvaOpiskelijamaaratRaportti_aikuistenOppimaara,
        KoskiSpecificMockOppijat.paallekkaisiOpiskeluoikeuksia,
        KoskiSpecificMockOppijat.vapaaSivistystyöOppivelvollinen,
        KoskiSpecificMockOppijat.vapaaSivistystyöMaahanmuuttajienKotoutus,
        KoskiSpecificMockOppijat.vapaaSivistystyöLukutaitoKotoutus,
        KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus,
        KoskiSpecificMockOppijat.oikeusOpiskelunMaksuttomuuteen,
        KoskiSpecificMockOppijat.eiOikeuttaMaksuttomuuteen,
        KoskiSpecificMockOppijat.etk18vSyntynytKesäkuunEnsimmäisenäPäivänä,
        KoskiSpecificMockOppijat.etk18vSyntynytToukokuunViimeisenäPäivänä,
        KoskiSpecificMockOppijat.oppivelvollisuustietoLiianVanha,
        KoskiSpecificMockOppijat.oppivelvollisuustietoMaster,
        KoskiSpecificMockOppijat.oppivelvollisuustietoSlave1,
        KoskiSpecificMockOppijat.oppivelvollisuustietoSlave2,
        KoskiSpecificMockOppijat.maksuttomuuttaPidennetty1,
        KoskiSpecificMockOppijat.maksuttomuuttaPidennetty2,
        KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021,
        KoskiSpecificMockOppijat.vuonna2004SyntynytMuttaPeruskouluValmisEnnen2021,
        KoskiSpecificMockOppijat.vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa,
        KoskiSpecificMockOppijat.nuoriHetuton,
        KoskiSpecificMockOppijat.vuonna2005SyntynytUlkomainenVaihtoopiskelija,
        KoskiSpecificMockOppijat.vuonna2005SyntynytPeruskouluValmis2021,
        KoskiSpecificMockOppijat.rikkinäinenOpiskeluoikeus,
        KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021EiKotikuntaaSuomessa,
        KoskiSpecificMockOppijat.vuonna2004SyntynytPeruskouluValmis2021KotikuntaAhvenanmaalla,
        KoskiSpecificMockOppijat.vuonna2004SyntynytMuttaEronnutPeruskoulustaEnnen2021
      ).map {
        case o: OppijaHenkilöWithMasterInfo => {
          o.henkilö.oid
        }
        case o:  LaajatOppijaHenkilöTiedot => {
          o.oid
        }
      }

      postOidit(oidit) {
        verifyResponseStatusOk()
      }
    }

    "Haku onnistuu myös rikkinäisellä datalla (koodistosta puuttuva koulutuskoodi)" in {
      // Scala-schemaa käytetään tavalla, jossa kaikki json-taulukoiden alkioissa olevat validointivirheet
      // jätetään huomioimatta ja jätetään vain alkio pois listalta. Tämän vuoksi esim. koodistosta
      // puuttuvia arvoja ei huomata. Tämän ei kuitenkaan pitäisi olla ongelma, koska tietokannassa ei voi olla
      // tältä osin rikkinäistä dataa.
      val oidit = List(
        KoskiSpecificMockOppijat.tunnisteenKoodiarvoPoistettu
      ).map(_.oid)

      postOidit(oidit) {
        verifyResponseStatusOk()
      }
    }

    "Sallitaan yhteensä 1000 hetua ja oidia" in {
      val oidit = List.fill(460)(KoskiSpecificMockOppijat.amis.oid)
      val hetut = List.fill(540)(KoskiSpecificMockOppijat.amis.hetu.get)
      postOppijat(oidit, hetut) {
        verifyResponseStatusOk()
      }
    }

    "Ei sallita yli 1000 hetua ja oidia" in {
      val oidit = List.fill(461)(KoskiSpecificMockOppijat.amis.oid)
      val hetut = List.fill(540)(KoskiSpecificMockOppijat.amis.hetu.get)
      postOppijat(oidit, hetut) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest("Liian monta oppijaa, enintään 1000 sallittu"))
      }
    }

    "Saman oppijan hakeminen oidilla ja hetulla palauttaa oppijan vain yhden kerran" in {
      val oidit = List(
        KoskiSpecificMockOppijat.amis
      ).map(_.oid)
      val hetut = List(
        KoskiSpecificMockOppijat.amis
      ).map(_.hetu.get)

      postOppijat(oidit, hetut) {
        verifyResponseStatusOk()
        val response = JsonSerializer.parse[List[YtlOppija]](body)
        response.length should equal(1)
        response(0).opiskeluoikeudet.length should equal(1)
      }
    }

    "Peruskoulun oppijan hakeminen tuottaa tyhjän vastauksen, koska peruskoulun tietoja ei YTL:lle luovuteta" in {
      AuditLogTester.clearMessages
      val hetut = List(
        KoskiSpecificMockOppijat.ysiluokkalainen
      ).map(_.hetu.get)

      postHetut(hetut) {
        verifyResponseStatusOk()
        val response = JsonSerializer.parse[List[YtlOppija]](body)
        response.length should equal(0)
      }
    }
  }

  private def postHetut[A](hetut: List[String], user: MockUser = MockUsers.ytlKäyttäjä)(f: => A): A =
    postOppijat(None, Some(hetut), user)(f)

  private def postOidit[A](oidit: List[String], user: MockUser = MockUsers.ytlKäyttäjä)(f: => A): A =
    postOppijat(Some(oidit), None, user)(f)

  private def postOppijat[A](oidit: List[String], hetut: List[String], user: MockUser = MockUsers.ytlKäyttäjä)(f: => A): A =
    postOppijat(Some(oidit), Some(hetut), user)(f)

  private def postOppijat[A](oidit: Option[List[String]], hetut: Option[List[String]], user: MockUser)(f: => A): A = {
    post(
      "api/luovutuspalvelu/ytl/oppijat",
      JsonSerializer.writeWithRoot(YtlBulkRequest(oidit = oidit, hetut = hetut)),
      headers = authHeaders(user) ++ jsonContent
    )(f)
  }

  private def expectedMaksuttomuuttaPidennetty2(opiskeluoikeusOidit: Seq[String], aikaleimat: Seq[String]) =
    s"""
       |[
       |  {
       |    "henkilö": {
       |      "oid": "1.2.246.562.24.00000000114",
       |      "hetu": "220835-2325",
       |      "syntymäaika": "2004-01-01",
       |      "etunimet": "Maksuttomuutta2",
       |      "sukunimi": "Pidennetty2",
       |      "kutsumanimi": "Maksuttomuutta2",
       |      "äidinkieli": {
       |        "koodiarvo": "FI",
       |        "nimi": {
       |          "fi": "suomi",
       |          "sv": "finska",
       |          "en": "Finnish"
       |        },
       |        "lyhytNimi": {
       |          "fi": "suomi",
       |          "sv": "finska",
       |          "en": "Finnish"
       |        },
       |        "koodistoUri": "kieli",
       |        "koodistoVersio": 1
       |      },
       |      "turvakielto": false
       |    },
       |    "opiskeluoikeudet": [
       |      {
       |        "oid": "${opiskeluoikeusOidit(0)}",
       |        "aikaleima": "${aikaleimat(0)}",
       |        "oppilaitos": {
       |          "oid": "1.2.246.562.10.52251087186",
       |          "oppilaitosnumero": {
       |            "koodiarvo": "10105",
       |            "nimi": {
       |              "fi": "Stadin ammatti- ja aikuisopisto",
       |              "sv": "Stadin ammatti- ja aikuisopisto",
       |              "en": "Stadin ammatti- ja aikuisopisto"
       |            },
       |            "lyhytNimi": {
       |              "fi": "Stadin ammatti- ja aikuisopisto",
       |              "sv": "Stadin ammatti- ja aikuisopisto",
       |              "en": "Stadin ammatti- ja aikuisopisto"
       |            },
       |            "koodistoUri": "oppilaitosnumero",
       |            "koodistoVersio": 1
       |          },
       |          "nimi": {
       |            "fi": "Stadin ammatti- ja aikuisopisto",
       |            "sv": "Stadin ammatti- ja aikuisopisto",
       |            "en": "Stadin ammatti- ja aikuisopisto"
       |          },
       |          "kotipaikka": {
       |            "koodiarvo": "091",
       |            "nimi": {
       |              "fi": "Helsinki",
       |              "sv": "Helsingfors"
       |            },
       |            "koodistoUri": "kunta",
       |            "koodistoVersio": 2
       |          }
       |        },
       |        "koulutustoimija": {
       |          "oid": "1.2.246.562.10.346830761110",
       |          "nimi": {
       |            "fi": "Helsingin kaupunki",
       |            "sv": "Helsingfors stad"
       |          },
       |          "yTunnus": "0201256-6",
       |          "kotipaikka": {
       |            "koodiarvo": "091",
       |            "nimi": {
       |              "fi": "Helsinki",
       |              "sv": "Helsingfors"
       |            },
       |            "koodistoUri": "kunta",
       |            "koodistoVersio": 2
       |          }
       |        },
       |        "tila": {
       |          "opiskeluoikeusjaksot": [
       |            {
       |              "alku": "2021-08-01",
       |              "tila": {
       |                "koodiarvo": "lasna",
       |                "nimi": {
       |                  "fi": "Läsnä",
       |                  "sv": "Närvarande",
       |                  "en": "Present"
       |                },
       |                "koodistoUri": "koskiopiskeluoikeudentila",
       |                "koodistoVersio": 1
       |              }
       |            }
       |          ]
       |        },
       |        "suoritukset": [
       |          {
       |            "tyyppi": {
       |              "koodiarvo": "ammatillinentutkinto",
       |              "nimi": {
       |                "fi": "Ammatillinen tutkinto",
       |                "sv": "Yrkesinriktad examen",
       |                "en": "Vocational education  qualification"
       |              },
       |              "koodistoUri": "suorituksentyyppi",
       |              "koodistoVersio": 1
       |            },
       |            "koulutusmoduuli": {
       |              "tunniste": {
       |                "koodiarvo": "351301",
       |                "nimi": {
       |                  "fi": "Autoalan perustutkinto",
       |                  "sv": "Grundexamen inom bilbranschen",
       |                  "en": "Vocational qualification in the Vehicle Sector"
       |                },
       |                "lyhytNimi": {
       |                  "fi": "Autoalan perustutkinto",
       |                  "sv": "Grundexamen inom bilbranschen",
       |                  "en": "Vocational qualification in the Vehicle Sector"
       |                },
       |                "koodistoUri": "koulutus",
       |                "koodistoVersio": 12
       |              },
       |              "perusteenDiaarinumero": "39/011/2014",
       |              "perusteenNimi": {
       |                "fi": "Autoalan perustutkinto",
       |                "sv": "Grundexamen inom bilbranschen"
       |              },
       |              "koulutustyyppi": {
       |                "koodiarvo": "1",
       |                "nimi": {
       |                  "fi": "Ammatillinen perustutkinto",
       |                  "sv": "Yrkesinriktad grundexamen",
       |                  "en": "Vocational upper secondary qualification"
       |                },
       |                "lyhytNimi": {
       |                  "fi": "Ammatillinen perustutkinto",
       |                  "sv": "Yrkesinriktad grundexamen"
       |                },
       |                "koodistoUri": "koulutustyyppi",
       |                "koodistoVersio": 2
       |              }
       |            },
       |            "toimipiste": {
       |              "oid": "1.2.246.562.10.42456023292",
       |              "nimi": {
       |                "fi": "Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka"
       |              },
       |              "kotipaikka": {
       |                "koodiarvo": "091",
       |                "nimi": {
       |                  "fi": "Helsinki",
       |                  "sv": "Helsingfors"
       |                },
       |                "koodistoUri": "kunta",
       |                "koodistoVersio": 2
       |              }
       |            },
       |            "suorituskieli": {
       |              "koodiarvo": "FI",
       |              "nimi": {
       |                "fi": "suomi",
       |                "sv": "finska",
       |                "en": "Finnish"
       |              },
       |              "lyhytNimi": {
       |                "fi": "suomi",
       |                "sv": "finska",
       |                "en": "Finnish"
       |              },
       |              "koodistoUri": "kieli",
       |              "koodistoVersio": 1
       |            }
       |          }
       |        ],
       |        "lisätiedot": {
       |          "maksuttomuus": [
       |            {
       |              "alku": "2021-10-10",
       |              "maksuton": true
       |            }
       |          ],
       |          "oikeuttaMaksuttomuuteenPidennetty": [
       |            {
       |              "alku": "2021-10-10",
       |              "loppu": "2021-10-15"
       |            },
       |            {
       |              "alku": "2021-10-20",
       |              "loppu": "2021-10-25"
       |            }
       |          ]
       |        },
       |        "tyyppi": {
       |          "koodiarvo": "ammatillinenkoulutus",
       |          "nimi": {
       |            "fi": "Ammatillinen koulutus",
       |            "sv": "Yrkesutbildning"
       |          },
       |          "lyhytNimi": {
       |            "fi": "Ammatillinen koulutus"
       |          },
       |          "koodistoUri": "opiskeluoikeudentyyppi",
       |          "koodistoVersio": 1
       |        }
       |      },
       |      {
       |        "oid": "${opiskeluoikeusOidit(1)}",
       |        "aikaleima": "${aikaleimat(1)}",
       |        "oppilaitos": {
       |          "oid": "1.2.246.562.10.14613773812",
       |          "oppilaitosnumero": {
       |            "koodiarvo": "00204",
       |            "nimi": {
       |              "fi": "Jyväskylän normaalikoulu",
       |              "sv": "Jyväskylän normaalikoulu",
       |              "en": "Jyväskylän normaalikoulu"
       |            },
       |            "lyhytNimi": {
       |              "fi": "Jyväskylän normaalikoulu",
       |              "sv": "Jyväskylän normaalikoulu",
       |              "en": "Jyväskylän normaalikoulu"
       |            },
       |            "koodistoUri": "oppilaitosnumero",
       |            "koodistoVersio": 1
       |          },
       |          "nimi": {
       |            "fi": "Jyväskylän normaalikoulu",
       |            "sv": "Jyväskylän normaalikoulu",
       |            "en": "Jyväskylän normaalikoulu"
       |          },
       |          "kotipaikka": {
       |            "koodiarvo": "179",
       |            "nimi": {
       |              "fi": "Jyväskylä",
       |              "sv": "Jyväskylä"
       |            },
       |            "koodistoUri": "kunta",
       |            "koodistoVersio": 2
       |          }
       |        },
       |        "koulutustoimija": {
       |          "oid": "1.2.246.562.10.77055527103",
       |          "nimi": {
       |            "fi": "Jyväskylän yliopisto"
       |          },
       |          "yTunnus": "0245894-7",
       |          "kotipaikka": {
       |            "koodiarvo": "179",
       |            "nimi": {
       |              "fi": "Jyväskylä",
       |              "sv": "Jyväskylä"
       |            },
       |            "koodistoUri": "kunta",
       |            "koodistoVersio": 2
       |          }
       |        },
       |        "tila": {
       |          "opiskeluoikeusjaksot": [
       |            {
       |              "alku": "2021-08-01",
       |              "tila": {
       |                "koodiarvo": "lasna",
       |                "nimi": {
       |                  "fi": "Läsnä",
       |                  "sv": "Närvarande",
       |                  "en": "Present"
       |                },
       |                "koodistoUri": "koskiopiskeluoikeudentila",
       |                "koodistoVersio": 1
       |              }
       |            }
       |          ]
       |        },
       |        "lisätiedot": {
       |          "maksuttomuus": [
       |            {
       |              "alku": "2021-10-10",
       |              "maksuton": true
       |            }
       |          ],
       |          "oikeuttaMaksuttomuuteenPidennetty": [
       |            {
       |              "alku": "2021-10-20",
       |              "loppu": "2021-10-25"
       |            },
       |            {
       |              "alku": "2021-12-30",
       |              "loppu": "2022-01-10"
       |            }
       |          ]
       |        },
       |        "tyyppi": {
       |          "koodiarvo": "lukiokoulutus",
       |          "nimi": {
       |            "fi": "Lukiokoulutus",
       |            "sv": "Gymnasieutbildning"
       |          },
       |          "lyhytNimi": {
       |            "fi": "Lukiokoulutus"
       |          },
       |          "koodistoUri": "opiskeluoikeudentyyppi",
       |          "koodistoVersio": 1
       |        }
       |      }
       |    ]
       |  }
       |]
       |""".stripMargin

}
