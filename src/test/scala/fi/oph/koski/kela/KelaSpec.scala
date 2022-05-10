package fi.oph.koski.kela

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.api.OpiskeluoikeusTestMethodsAmmatillinen
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.history.OpiskeluoikeusHistoryPatch
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.{JsonFiles, JsonSerializer}
import fi.oph.koski.koskiuser.{MockUser, MockUsers}
import fi.oph.koski.log.{AccessLogTester, AuditLogTester}
import fi.oph.koski.schema._
import fi.oph.scalaschema.{ExtractionContext, SchemaValidatingExtractor}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.{LocalDate, LocalDateTime}

class KelaSpec
  extends AnyFreeSpec
    with KoskiHttpSpec
    with OpiskeluoikeusTestMethodsAmmatillinen
    with Matchers
    with BeforeAndAfterAll {

  "Kelan yhden oppijan rajapinta" - {
    "Yhden oppijan hakeminen onnistuu ja tuottaa auditlog viestin" in {
      AuditLogTester.clearMessages
      postHetu(KoskiSpecificMockOppijat.amis.hetu.get) {
        verifyResponseStatusOk()
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_KATSOMINEN", "target" -> Map("oppijaHenkiloOid" -> KoskiSpecificMockOppijat.amis.oid)))
      }
    }
    "Palautetaan 404 jos opiskelijalla ei ole ollenkaan Kelaa kiinnostavia opiskeluoikeuksia" in {
      postHetu(KoskiSpecificMockOppijat.monimutkainenKorkeakoululainen.hetu.get) {
        verifyResponseStatus(404, KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia("Oppijaa (hetu) ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."))
      }
    }
    "Oppijan opiskeluoikeuksista filtteröidään pois sellaiset opiskeluoikeuden tyypit jotka ei kiinnosta Kelaa" in {
      postHetu(KoskiSpecificMockOppijat.kelaErityyppisiaOpiskeluoikeuksia.hetu.get) {
        verifyResponseStatusOk()
        val response = JsonSerializer.parse[KelaOppija](body)

        response.henkilö.hetu should equal(KoskiSpecificMockOppijat.kelaErityyppisiaOpiskeluoikeuksia.hetu)
        response.opiskeluoikeudet.map(_.tyyppi.koodiarvo) should equal(List(OpiskeluoikeudenTyyppi.perusopetus.koodiarvo))
      }
    }
  }

  "Usean oppijan rajapinta" - {
    "Voidaan hakea usea oppija, jos jollain oppijalla ei löydy Kosken kantaan tallennettuja opintoja, se puuttuu vastauksesta" in {
      val hetut = List(
        KoskiSpecificMockOppijat.amis,
        KoskiSpecificMockOppijat.ibFinal,
        KoskiSpecificMockOppijat.koululainen,
        KoskiSpecificMockOppijat.ylioppilas
      ).map(_.hetu.get)

      postHetut(hetut) {
        verifyResponseStatusOk()
        val response = JsonSerializer.parse[List[KelaOppija]](body)
        response.map(_.henkilö.hetu.get).sorted should equal(hetut.sorted.filterNot(_ == KoskiSpecificMockOppijat.ylioppilas.hetu.get))
      }
    }
    "Luo AuditLogin" in {
      AuditLogTester.clearMessages
      postHetut(List(KoskiSpecificMockOppijat.amis.hetu.get)) {
        verifyResponseStatusOk()
        AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_KATSOMINEN", "target" -> Map("oppijaHenkiloOid" -> KoskiSpecificMockOppijat.amis.oid)))
      }
    }
    "Ei luo AuditLogia jos hetulla löytyvä oppija puuttuu vastauksesta" in {
      AuditLogTester.clearMessages
      postHetut(List(KoskiSpecificMockOppijat.korkeakoululainen.hetu.get)) {
        verifyResponseStatusOk()
        AuditLogTester.getLogMessages.length should equal(0)
      }
    }
    "Sallitaan 1000 hetua" in {
      val hetut = List.fill(1000)(KoskiSpecificMockOppijat.amis.hetu.get)
      postHetut(hetut) {
        verifyResponseStatusOk()
      }
    }
    "Ei sallita yli 1000 hetua" in {
      val hetut = List.fill(1001)(KoskiSpecificMockOppijat.amis.hetu.get)
      postHetut(hetut) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest("Liian monta hetua, enintään 1000 sallittu"))
      }
    }
  }

  "Kelan käyttöoikeudet" - {
    "Suppeilla Kelan käyttöoikeuksilla ei nää kaikkia lisätietoja" in {
      postHetu(KoskiSpecificMockOppijat.amis.hetu.get, user = MockUsers.kelaSuppeatOikeudet) {
        verifyResponseStatusOk()
        val opiskeluoikeudet = JsonSerializer.parse[KelaOppija](body).opiskeluoikeudet
        val lisatiedot = opiskeluoikeudet.head.lisätiedot.get match {
          case l: KelaAmmatillisenOpiskeluoikeudenLisätiedot => l
        }

        lisatiedot.hojks should equal(None)

        opiskeluoikeudet.length should be(1)
      }
    }
    "Laajoilla Kelan käyttöoikeuksilla näkee kaikki KelaSchema:n lisätiedot" in {
      postHetu(KoskiSpecificMockOppijat.amis.hetu.get, user = MockUsers.kelaLaajatOikeudet) {
        verifyResponseStatusOk()
        val opiskeluoikeudet = JsonSerializer.parse[KelaOppija](body).opiskeluoikeudet
        val lisatiedot = opiskeluoikeudet.head.lisätiedot.get match {
          case l: KelaAmmatillisenOpiskeluoikeudenLisätiedot => l
        }

        lisatiedot.hojks shouldBe(defined)

        opiskeluoikeudet.length should be(1)
      }
    }
    "Osasuorituksen yksilöllistetty oppimäärä" - {
      def verify(user: MockUser, yksilöllistettyOppimääräShouldShow: Boolean): Unit = {
        postHetu(KoskiSpecificMockOppijat.koululainen.hetu.get, user = user) {
          verifyResponseStatusOk()
          val opiskeluoikeudet = JsonSerializer.parse[KelaOppija](body).opiskeluoikeudet
          val osasuoritukset = opiskeluoikeudet.flatMap(_.suoritukset.flatMap(_.osasuoritukset)).flatten.flatMap {
            case os: YksilöllistettyOppimäärä => Some(os)
            case _ => None
          }
          osasuoritukset.exists(_.yksilöllistettyOppimäärä.isDefined) shouldBe(yksilöllistettyOppimääräShouldShow)
        }
      }
      "Näkyy laajoilla käyttöoikeuksilla" in {
        verify(MockUsers.kelaLaajatOikeudet, yksilöllistettyOppimääräShouldShow = true)
      }
      "Ei näy suppeilla käyttöoikeuksilla" in {
        verify(MockUsers.kelaSuppeatOikeudet, yksilöllistettyOppimääräShouldShow = false)
      }
    }
    "Osasuoritusten lisätiedot" - {
      "Ei näy suppeilla käyttöoikeuksilla" in {
        postHetu(KoskiSpecificMockOppijat.ammattilainen.hetu.get, MockUsers.kelaSuppeatOikeudet) {
          verifyResponseStatusOk()
          val opiskeluoikeudet = JsonSerializer.parse[KelaOppija](body).opiskeluoikeudet
          val osasuoritukset = opiskeluoikeudet.flatMap(_.suoritukset.flatMap(_.osasuoritukset)).flatten.map{
            case os: KelaAmmatillinenOsasuoritus => os
          }
          osasuoritukset.exists(_.lisätiedot.isDefined) shouldBe(false)
        }
      }
      "Näkyy laajoilla käyttöoikeuksilla vain jos lisätietojen tunnisteen koodiarvo on 'mukautettu'" in {
        postHetu(KoskiSpecificMockOppijat.ammattilainen.hetu.get, MockUsers.kelaLaajatOikeudet) {
          verifyResponseStatusOk()
          val opiskeluoikeudet = JsonSerializer.parse[KelaOppija](body).opiskeluoikeudet
          val osasuoritukset = opiskeluoikeudet.flatMap(_.suoritukset.flatMap(_.osasuoritukset)).flatten.map{
            case os: KelaAmmatillinenOsasuoritus => os
          }
          osasuoritukset.flatMap(_.lisätiedot).flatten.map(_.tunniste.koodiarvo) should equal(List("mukautettu"))
        }
      }
    }
  }

  "Perusopetuksen oppiaineen oppimäärän suorituksesta ei välitetä suoritustapaa Kelalle" in {
    postHetu(KoskiSpecificMockOppijat.montaOppiaineenOppimäärääOpiskeluoikeudessa.hetu.get) {
      verifyResponseStatusOk()
      val opiskeluoikeudet = JsonSerializer.parse[KelaOppija](body).opiskeluoikeudet.map{
        case oo: KelaAikuistenPerusopetuksenOpiskeluoikeus => oo
      }

      opiskeluoikeudet.foreach(_.suoritukset.foreach{
        case suoritus: KelaAikuistenPerusopetuksenPäätasonSuoritus =>
          suoritus.suoritustapa should equal(None)
        case _: KelaAikuistenPerusopetuksenOppiaineenOppimääränSuoritus =>
          1 shouldBe 1
      })
    }
  }

  "Vapaan sivistystyön opiskeluoikeuksista ei välitetä vapaatavoitteisin koulutuksen suorituksia" in {
    postHetu(KoskiSpecificMockOppijat.vapaaSivistystyöVapaatavoitteinenKoulutus.hetu.get) {
      verifyResponseStatus(404, KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia("Oppijaa (hetu) ei löydy tai käyttäjällä ei ole oikeuksia tietojen katseluun."))
    }
  }

  "Opiskeluoikeuden versiohistorian haku tuottaa AuditLogin" in {
    resetFixtures
    val opiskeluoikeus = lastOpiskeluoikeusByHetu(KoskiSpecificMockOppijat.amis)

    luoVersiohistoriaanRivi(KoskiSpecificMockOppijat.amis, opiskeluoikeus.asInstanceOf[AmmatillinenOpiskeluoikeus])

    AuditLogTester.clearMessages

    getVersiohistoria(opiskeluoikeus.oid.get) {
      verifyResponseStatusOk()
      val history = JsonSerializer.parse[List[OpiskeluoikeusHistoryPatch]](body)

      history.length should equal(2)
      AuditLogTester.verifyAuditLogMessage(Map("operation" -> "MUUTOSHISTORIA_KATSOMINEN", "target" -> Map("opiskeluoikeusOid" -> opiskeluoikeus.oid.get)))
    }
  }

  "Tietyn version haku opiskeluoikeudesta tuottaa AuditLogin" in {
    resetFixtures

    val opiskeluoikeus = lastOpiskeluoikeusByHetu(KoskiSpecificMockOppijat.amis)

    luoVersiohistoriaanRivi(KoskiSpecificMockOppijat.amis, opiskeluoikeus.asInstanceOf[AmmatillinenOpiskeluoikeus])

    AuditLogTester.clearMessages

    getOpiskeluoikeudenVersio(KoskiSpecificMockOppijat.amis.oid, opiskeluoikeus.oid.get, 1) {
      verifyResponseStatusOk()
      val response = JsonSerializer.parse[KelaOppija](body)

      response.opiskeluoikeudet.headOption.flatMap(_.versionumero) should equal(Some(1))
      AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_KATSOMINEN", "target" -> Map("oppijaHenkiloOid" -> KoskiSpecificMockOppijat.amis.oid)))
    }
  }

  "Hetu ei päädy lokiin" in {
    AccessLogTester.clearMessages
    val maskedHetu = "******-****"
    getHetu(KoskiSpecificMockOppijat.amis.hetu.get) {
      verifyResponseStatusOk()
      AccessLogTester.getLatestMatchingAccessLog("/koski/kela") should include(maskedHetu)
    }
  }

  "Kela-API taaksepäin yhteensopivuus skeeman refaktoroinnin jälkeen 2022-05-03" - {
    "Palauttaa täsmälleen samat tiedot kuin ennen refaktorointia" - {
      "ammatillisen oppijalle" in {
        resetFixtures

        val vanhaJson = JsonFiles.readFile("src/test/resources/kela_backwardcompatibility/kela-response-ammatillinensuorittaja-laaja_2022-05-03.json")

        postHetu(KoskiSpecificMockOppijat.amis.hetu.get, user = MockUsers.kelaLaajatOikeudet) {
          verifyResponseStatusOk()
          val oppija = JsonSerializer.parse[KelaOppija](body)
          val lisatiedot = oppija.opiskeluoikeudet.head.lisätiedot.get

          lisatiedot match {
            case al: KelaAmmatillisenOpiskeluoikeudenLisätiedot => al.hojks shouldBe(defined)
          }

          oppija.opiskeluoikeudet.length should be(1)

          val ooSiivottu = kopioiOppijaIlmanSyntymäaikaa(oppija, oppija.opiskeluoikeudet.head match {
            case x: KelaAmmatillinenOpiskeluoikeus =>
              x.copy(
                oid = Some("1.2.246.562.15.53705389947"),
                aikaleima = Some(LocalDateTime.parse("2022-04-25T13:30:12.013956"))
              )
          })

          JsonSerializer.serializeWithRoot[KelaOppija](ooSiivottu) shouldBe vanhaJson
        }
      }

      "lukion oppijalle ja ylioppilastutkinnon suorittajalle" in {
        val vanhaJson = JsonFiles.readFile("src/test/resources/kela_backwardcompatibility/kela-response-lukionsuorittaja-laaja_2022-05-03.json")

        postHetu(KoskiSpecificMockOppijat.ylioppilasLukiolainen.hetu.get, user = MockUsers.kelaLaajatOikeudet) {
          verifyResponseStatusOk()
          val oppija = JsonSerializer.parse[KelaOppija](body)

          oppija.opiskeluoikeudet.length should be(2)

          val ooSiivottu = kopioiOppijaIlmanSyntymäaikaa(oppija, oppija.opiskeluoikeudet.head match {
            case x: KelaLukionOpiskeluoikeus =>
              x.copy(
                oid = Some("1.2.246.562.15.89706091538"),
                aikaleima = Some(LocalDateTime.parse("2022-04-25T13:30:12.109193"))
              )
          })

          JsonSerializer.serializeWithRoot[KelaOppija](ooSiivottu) shouldBe vanhaJson

          val ylioppilastutkinto = oppija.opiskeluoikeudet.last match {
            case x: KelaYlioppilastutkinnonOpiskeluoikeus => x
          }
          ylioppilastutkinto.oppilaitos.get.oid shouldBe "1.2.246.562.10.14613773812"
          ylioppilastutkinto.koulutustoimija.get.oid shouldBe "1.2.246.562.10.43628088406"
          ylioppilastutkinto.suoritukset.length shouldBe 1
          ylioppilastutkinto.suoritukset.head.pakollisetKokeetSuoritettu.get shouldBe true
          ylioppilastutkinto.suoritukset.head.vahvistus.map(_.päivä) shouldBe Some(LocalDate.of(2012, 6, 2))
          ylioppilastutkinto.suoritukset.head.osasuoritukset.get.length shouldBe 5
          ylioppilastutkinto.suoritukset.head.osasuoritukset.get.head shouldBe KelaYlioppilastutkinnonOsasuoritus(
            koulutusmoduuli = KelaYlioppilastutkinnonOsasuorituksenKoulutusmoduuli(
              KelaKoodistokoodiviite("A", Some(Finnish("Äidinkielen koe, suomi", Some("Provet i modersmålet, finska"), None)), None, Some("koskiyokokeet"),Some(1))
            ),
            arviointi = Some(List(KelaOsasuorituksenArvionti(None, Some(true), None))),
            tyyppi = Koodistokoodiviite("ylioppilastutkinnonkoe", "suorituksentyyppi"),
            tila = None,
            tutkintokerta = Some(KelaYlioppilastutkinnonTutkintokerta("2012K", 2012, Finnish("kevät", Some("vår"), None)))
          )
        }
      }

      "lukioon valmistavan oppijalle" in {
        val vanhaJson = JsonFiles.readFile("src/test/resources/kela_backwardcompatibility/kela-response-luvasuorittaja-laaja_2022-05-03.json")

        postHetu(KoskiSpecificMockOppijat.luva.hetu.get, user = MockUsers.kelaLaajatOikeudet) {
          verifyResponseStatusOk()
          val oppija = JsonSerializer.parse[KelaOppija](body)

          oppija.opiskeluoikeudet.length should be(1)

          val ooSiivottu = kopioiOppijaIlmanSyntymäaikaa(oppija, oppija.opiskeluoikeudet.head match {
            case x: KelaLuvaOpiskeluoikeus =>
              x.copy(
                oid = Some("1.2.246.562.15.32373771594"),
                aikaleima = Some(LocalDateTime.parse("2022-04-25T13:30:11.805178"))
              )
          })

          JsonSerializer.serializeWithRoot[KelaOppija](ooSiivottu) shouldBe vanhaJson
        }
      }

      "DIA oppijalle" in {
        val vanhaJson = JsonFiles.readFile("src/test/resources/kela_backwardcompatibility/kela-response-diansuorittaja-laaja_2022-05-03.json")

        postHetu(KoskiSpecificMockOppijat.dia.hetu.get, user = MockUsers.kelaLaajatOikeudet) {
          verifyResponseStatusOk()
          val oppija = JsonSerializer.parse[KelaOppija](body)

          oppija.opiskeluoikeudet.length should be(1)

          val ooSiivottu = kopioiOppijaIlmanSyntymäaikaa(oppija, oppija.opiskeluoikeudet.head match {
            case x: KelaDIAOpiskeluoikeus =>
              x.copy(
                oid = Some("1.2.246.562.15.18091744294"),
                aikaleima = Some(LocalDateTime.parse("2022-04-25T13:30:12.767104"))
              )
          })

          JsonSerializer.serializeWithRoot[KelaOppija](ooSiivottu) shouldBe vanhaJson
        }
      }

      "IB oppijalle" in {
        val vanhaJson = JsonFiles.readFile("src/test/resources/kela_backwardcompatibility/kela-response-ibsuorittaja-laaja_2022-05-03.json")

        postHetu(KoskiSpecificMockOppijat.ibFinal.hetu.get, user = MockUsers.kelaLaajatOikeudet) {
          verifyResponseStatusOk()
          val oppija = JsonSerializer.parse[KelaOppija](body)

          oppija.opiskeluoikeudet.length should be(1)

          val ooSiivottu = kopioiOppijaIlmanSyntymäaikaa(oppija, oppija.opiskeluoikeudet.head match {
            case x: KelaIBOpiskeluoikeus =>
              x.copy(
                oid = Some("1.2.246.562.15.98928603160"),
                aikaleima = Some(LocalDateTime.parse("2022-04-25T13:30:12.578317"))
              )
          })

          JsonSerializer.serializeWithRoot[KelaOppija](ooSiivottu) shouldBe vanhaJson
        }
      }

      "Pre-IB oppijalle" in {

        val vanhaJson = JsonFiles.readFile("src/test/resources/kela_backwardcompatibility/kela-response-preibsuorittaja-laaja_2022-05-03.json")

        postHetu(KoskiSpecificMockOppijat.ibPreIB2019.hetu.get, user = MockUsers.kelaLaajatOikeudet) {
          verifyResponseStatusOk()
          val oppija = JsonSerializer.parse[KelaOppija](body)

          oppija.opiskeluoikeudet.length should be(1)

          val ooSiivottu = kopioiOppijaIlmanSyntymäaikaa(oppija, oppija.opiskeluoikeudet.head match {
            case x: KelaIBOpiskeluoikeus =>
              x.copy(
                oid = Some("1.2.246.562.15.47951241752"),
                aikaleima = Some(LocalDateTime.parse("2022-04-25T13:30:12.724341"))
              )
          })

          JsonSerializer.serializeWithRoot[KelaOppija](ooSiivottu) shouldBe vanhaJson
        }
      }

      "International school -oppijalle" in {
        val vanhaJson = JsonFiles.readFile("src/test/resources/kela_backwardcompatibility/kela-response-internationalschoolsuorittaja-laaja_2022-05-03.json")

        postHetu(KoskiSpecificMockOppijat.internationalschool.hetu.get, user = MockUsers.kelaLaajatOikeudet) {
          verifyResponseStatusOk()
          val oppija = JsonSerializer.parse[KelaOppija](body)

          oppija.opiskeluoikeudet.length should be(1)

          val ooSiivottu = kopioiOppijaIlmanSyntymäaikaa(oppija, oppija.opiskeluoikeudet.head match {
            case x: KelaInternationalSchoolOpiskeluoikeus =>
              x.copy(
                oid = Some("1.2.246.562.15.88408121346"),
                aikaleima = Some(LocalDateTime.parse("2022-04-25T13:30:13.059046"))
              )
          })

          JsonSerializer.serializeWithRoot[KelaOppija](ooSiivottu) shouldBe vanhaJson
        }
      }

      "perusopetuksen ja perusopetukseen valmistavan oppijalle" in {
        val vanhaJson = JsonFiles.readFile("src/test/resources/kela_backwardcompatibility/kela-response-perussuorittaja-laaja_2022-05-03.json")

        postHetu(KoskiSpecificMockOppijat.koululainen.hetu.get, user = MockUsers.kelaLaajatOikeudet) {
          verifyResponseStatusOk()
          val oppija = JsonSerializer.parse[KelaOppija](body)

          oppija.opiskeluoikeudet.length should be(2)

          val ooSiivottu = kopioiOppijaIlmanSyntymäaikaa(
            oppija,
            oppija.opiskeluoikeudet.maxBy(_.tyyppi.koodiarvo) match {
              case x: KelaPerusopetuksenOpiskeluoikeus =>
                x.copy(
                  oid = Some("1.2.246.562.15.92263013200"),
                  aikaleima = Some(LocalDateTime.parse("2022-04-25T13:30:10.790239"))
                )
            },
            oppija.opiskeluoikeudet.minBy(_.tyyppi.koodiarvo) match {
              case x: KelaPerusopetukseenValmistavanOpiskeluoikeus =>
                x.copy(
                  oid = Some("1.2.246.562.15.41037507674"),
                  aikaleima = Some(LocalDateTime.parse("2022-04-25T13:30:10.918184"))
                )
            }
          )

          JsonSerializer.serializeWithRoot[KelaOppija](ooSiivottu) shouldBe vanhaJson
        }
      }

      "perusopetuksen lisäopetuksen oppijalle" in {
        val vanhaJson = JsonFiles.readFile("src/test/resources/kela_backwardcompatibility/kela-response-perusopetuksenlisaopetussuorittaja-laaja_2022-05-03.json")

        postHetu(KoskiSpecificMockOppijat.kymppiluokkalainen.hetu.get, user = MockUsers.kelaLaajatOikeudet) {
          verifyResponseStatusOk()
          val oppija = JsonSerializer.parse[KelaOppija](body)

          oppija.opiskeluoikeudet.length should be(1)

          val ooSiivottu = kopioiOppijaIlmanSyntymäaikaa(oppija, oppija.opiskeluoikeudet.head match {
            case x: KelaPerusopetuksenLisäopetuksenOpiskeluoikeus =>
              x.copy(
                oid = Some("1.2.246.562.15.73932003913"),
                aikaleima = Some(LocalDateTime.parse("2022-04-25T13:30:11.348339"))
              )
          })

          JsonSerializer.serializeWithRoot[KelaOppija](ooSiivottu) shouldBe vanhaJson
        }
      }

      "aikuisten perusopetuksen oppijalle" in {
        val vanhaJson = JsonFiles.readFile("src/test/resources/kela_backwardcompatibility/kela-response-aikuistenperussuorittaja-laaja_2022-05-03.json")

        postHetu(KoskiSpecificMockOppijat.aikuisOpiskelija.hetu.get, user = MockUsers.kelaLaajatOikeudet) {
          verifyResponseStatusOk()
          val oppija = JsonSerializer.parse[KelaOppija](body)

          oppija.opiskeluoikeudet.length should be(1)

          val ooSiivottu = kopioiOppijaIlmanSyntymäaikaa(oppija, oppija.opiskeluoikeudet.head match {
            case x: KelaAikuistenPerusopetuksenOpiskeluoikeus =>
              x.copy(
                oid = Some("1.2.246.562.15.33153122164"),
                aikaleima = Some(LocalDateTime.parse("2022-04-25T13:30:11.159361"))
              )
          })

          JsonSerializer.serializeWithRoot[KelaOppija](ooSiivottu) shouldBe vanhaJson
        }
      }

      "vapaan sivistystyön maahanmuuttajien kotoutuksen oppijalle" in {
        val vanhaJson = JsonFiles.readFile("src/test/resources/kela_backwardcompatibility/kela-response-vstmaahanmuuttaja-laaja_2022-05-03.json")

        postHetu(KoskiSpecificMockOppijat.vapaaSivistystyöMaahanmuuttajienKotoutus.hetu.get, user = MockUsers.kelaLaajatOikeudet) {
          verifyResponseStatusOk()
          val oppija = JsonSerializer.parse[KelaOppija](body)

          oppija.opiskeluoikeudet.length should be(1)

          val ooSiivottu = kopioiOppijaIlmanSyntymäaikaa(oppija, oppija.opiskeluoikeudet.head match {
            case x: KelaVapaanSivistystyönOpiskeluoikeus =>
              x.copy(
                oid = Some("1.2.246.562.15.95852166214"),
                aikaleima = Some(LocalDateTime.parse("2022-04-25T13:30:13.937913"))
              )
          })

          JsonSerializer.serializeWithRoot[KelaOppija](ooSiivottu) shouldBe vanhaJson
        }
      }

      "vapaan sivistystyön lukutaidon oppijalle" in {
        val vanhaJson = JsonFiles.readFile("src/test/resources/kela_backwardcompatibility/kela-response-vstlukutaito-laaja_2022-05-03.json")

        postHetu(KoskiSpecificMockOppijat.vapaaSivistystyöLukutaitoKotoutus.hetu.get, user = MockUsers.kelaLaajatOikeudet) {
          verifyResponseStatusOk()
          val oppija = JsonSerializer.parse[KelaOppija](body)

          oppija.opiskeluoikeudet.length should be(1)

          val ooSiivottu = kopioiOppijaIlmanSyntymäaikaa(oppija, oppija.opiskeluoikeudet.head match {
            case x: KelaVapaanSivistystyönOpiskeluoikeus =>
              x.copy(
                oid = Some("1.2.246.562.15.85397451536"),
                aikaleima = Some(LocalDateTime.parse("2022-04-25T13:30:13.981143"))
              )
          })

          JsonSerializer.serializeWithRoot[KelaOppija](ooSiivottu) shouldBe vanhaJson
        }
      }
    }

    "Laajoilla käyttöoikeuksilla haetut oppijat deserialisoituu ja serialisoituu täsmälleen samalla tavalla refaktoroinnin jälkeen" in {
      implicit val context: ExtractionContext = KoskiSchema.lenientDeserializationWithIgnoringNonValidatingListItems
      val kelaKaikkiJson = JsonFiles.readFile("src/test/resources/kela_backwardcompatibility/kela-response-kaikki-oppijat-laajat_2022-05-03.json")
      val opiskeluoikeudet = SchemaValidatingExtractor.extract[List[KelaOppija]](kelaKaikkiJson)
      opiskeluoikeudet.right.get.size shouldBe 88
      JsonSerializer.serializeWithRoot[List[KelaOppija]](opiskeluoikeudet.right.get) shouldBe kelaKaikkiJson
    }

    "Suppeilla käyttöoikeuksilla haetut oppijat deserialisoituu ja serialisoituu täsmälleen samalla tavalla refaktoroinnin jälkeen" in {
      implicit val context: ExtractionContext = KoskiSchema.lenientDeserializationWithIgnoringNonValidatingListItems
      val kelaKaikkiSuppeatJson = JsonFiles.readFile("src/test/resources/kela_backwardcompatibility/kela-response-kaikki-oppijat-suppeat_2022-05-03.json")
      val opiskeluoikeudet = SchemaValidatingExtractor.extract[List[KelaOppija]](kelaKaikkiSuppeatJson)
      opiskeluoikeudet.right.get.size shouldBe 88
      JsonSerializer.serializeWithRoot[List[KelaOppija]](opiskeluoikeudet.right.get) shouldBe kelaKaikkiSuppeatJson
    }
  }

  private def kopioiOppijaIlmanSyntymäaikaa(oppija: KelaOppija, oos: KelaOpiskeluoikeus*): KelaOppija = oppija.copy(
    henkilö = oppija.henkilö.copy(syntymäaika = None),
    opiskeluoikeudet = oos.toList
  )

  private def getHetu[A](hetu: String, user: MockUser = MockUsers.kelaSuppeatOikeudet)(f: => A)= {
    authGet(s"kela/$hetu", user)(f)
  }

  private def postHetu[A](hetu: String, user: MockUser = MockUsers.kelaLaajatOikeudet)(f: => A): A = {
    post(
      "api/luovutuspalvelu/kela/hetu",
      JsonSerializer.writeWithRoot(KelaRequest(hetu)),
      headers = authHeaders(user) ++ jsonContent
    )(f)
  }

  private def postHetut[A](hetut: List[String], user: MockUser = MockUsers.kelaLaajatOikeudet)(f: => A): A = {
    post(
      "api/luovutuspalvelu/kela/hetut",
      JsonSerializer.writeWithRoot(KelaBulkRequest(hetut)),
      headers = authHeaders(user) ++ jsonContent
    )(f)
  }

  private def getVersiohistoria[A](opiskeluoikeudenOid: String, user: MockUser = MockUsers.kelaLaajatOikeudet)(f: => A): A = {
    authGet(s"api/luovutuspalvelu/kela/versiohistoria/$opiskeluoikeudenOid", user)(f)
  }

  private def getOpiskeluoikeudenVersio[A](
    oppijaOid: String,
    opiskeluoikeudenOid: String,
    versio: Int,
    user: MockUser = MockUsers.kelaLaajatOikeudet
  )(f: => A): A = {
    authGet(s"api/luovutuspalvelu/kela/versiohistoria/$oppijaOid/$opiskeluoikeudenOid/$versio", user)(f)
  }

  private def luoVersiohistoriaanRivi(oppija: Henkilö, opiskeluoikeus: AmmatillinenOpiskeluoikeus): Unit = {
    createOrUpdate(oppija, opiskeluoikeus.copy(arvioituPäättymispäivä = Some(LocalDate.now)))
  }
}
