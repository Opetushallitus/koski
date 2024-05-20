package fi.oph.koski.api.oppijavalidation

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.api.misc.OpiskeluoikeusTestMethodsPerusopetus
import fi.oph.koski.documentation.AmmatillinenExampleData.primusLähdejärjestelmäId
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.ExamplesEsiopetus.osaAikainenErityisopetus
import fi.oph.koski.documentation.ExamplesPerusopetus.ysinOpiskeluoikeusKesken
import fi.oph.koski.documentation.OsaAikainenErityisopetusExampleData._
import fi.oph.koski.documentation.PerusopetusExampleData._
import fi.oph.koski.documentation.YleissivistavakoulutusExampleData.{helsinginMedialukio, jyväskylänNormaalikoulu, ressunLukio}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä
import fi.oph.koski.localization.LocalizedStringImplicits._
import fi.oph.koski.schema._
import mojave._

import java.time.LocalDate

// Perusopetuksen validointi perustuu tässä testattua diaarinumeroa lukuunottamatta domain-luokista generoituun JSON-schemaan.
// Schemavalidoinnille on tehty kattavat testit ammatillisten opiskeluoikeuksien osalle. Yleissivistävän koulutuksen validoinnissa luotamme
// toistaiseksi siihen, että schema itsessään on katselmoitu, ja että geneerinen mekanismi toimii.

class OppijaValidationPerusopetusSpec extends TutkinnonPerusteetTest[PerusopetuksenOpiskeluoikeus] with KoskiHttpSpec with OpiskeluoikeusTestMethodsPerusopetus {
  def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]) = defaultOpiskeluoikeus.copy(
    oppilaitos = Some(helsinginMedialukio),
    suoritukset = List(
      päättötodistusSuoritus.copy(koulutusmoduuli = päättötodistusSuoritus.koulutusmoduuli.copy(perusteenDiaarinumero = diaari)),
      yhdeksännenLuokanSuoritus
  ))

  def eperusteistaLöytymätönValidiDiaarinumero: String = "1/011/2004"

  "Suoritusten tila" - {
    "Vahvistettu päättötodistus -> HTTP 200" in {
      setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus) {
        verifyResponseStatusOk()
      }
    }

    "Vahvistettu päättötodistus ilman yhtään oppiainetta -> HTTP 400" in {
      setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(yhdeksännenLuokanSuoritus, päättötodistusSuoritus.copy(osasuoritukset = Some(Nil))))) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.oppiaineetPuuttuvat("Suorituksella ei ole osasuorituksena yhtään oppiainetta, vaikka sillä on vahvistus"))
      }
    }

    "Vahvistamaton päättötodistus ilman yhtään oppiainetta -> HTTP 200" in {
      setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(päättötodistusSuoritus.copy(vahvistus = None, osasuoritukset = Some(Nil))))) {
        verifyResponseStatusOk()
      }
    }

    "Vahvistettu päättötodistus keskeneräisellä oppiaineella -> HTTP 400" in {
      val oppiaineidenArvioinnit = traversal[PerusopetuksenOpiskeluoikeus]
        .field[List[PerusopetuksenPäätasonSuoritus]]("suoritukset")
        .items
        .field[Option[List[Suoritus]]]("osasuoritukset")
        .items.items
        .field[Option[List[Arviointi]]]("arviointi")

      setupOppijaWithOpiskeluoikeus(oppiaineidenArvioinnit.set(defaultOpiskeluoikeus)(None)) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.keskeneräinenOsasuoritus("Valmiiksi merkityllä suorituksella koulutus/201101 on keskeneräinen osasuoritus koskioppiaineetyleissivistava/AI"))
      }
    }

    "Kaksi samaa oppiainetta" - {
      "Identtisillä tiedoilla -> HTTP 400" in {
        setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(yhdeksännenLuokanSuoritus, päättötodistusSuoritus.copy(osasuoritukset = Some(List(
          suoritus(äidinkieli("AI1")).copy(arviointi = arviointi(9)),
          suoritus(äidinkieli("AI1")).copy(arviointi = arviointi(9))
        )))))) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.duplikaattiOsasuoritus("Osasuoritus (koskioppiaineetyleissivistava/AI,oppiaineaidinkielijakirjallisuus/AI1) esiintyy useammin kuin kerran ryhmässä pakolliset"))
        }
      }
      "Eri kielivalinnalla -> HTTP 200" in {
        setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(yhdeksännenLuokanSuoritus, päättötodistusSuoritus.copy(osasuoritukset = Some(List(
          suoritus(äidinkieli("AI1")).copy(arviointi = arviointi(9)),
          suoritus(äidinkieli("AI2")).copy(arviointi = arviointi(9))
        )))))) {
          verifyResponseStatusOk()
        }
      }
      "Valinnaisissa oppiaineissa -> HTTP 200" in {
        setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(yhdeksännenLuokanSuoritus, päättötodistusSuoritus.copy(osasuoritukset = Some(List(
          suoritus(äidinkieli("AI1").copy(pakollinen = false)).copy(arviointi = arviointi(9)),
          suoritus(äidinkieli("AI1").copy(pakollinen = false)).copy(arviointi = arviointi(9))
        )))))) {
          verifyResponseStatusOk()
        }
      }
    }

    "Kaksi samaksi katsottua ET ja KT oppiainetta" - {
      def testisuoritus(oppiaineenKoodiarvo: String, pakollinen: Boolean) =
        suoritus(uskonto(
          uskonto = None,
          pakollinen = pakollinen,
          laajuus = vuosiviikkotuntia(3),
          oppiaineenKoodiarvo = oppiaineenKoodiarvo
        )).copy(
          arviointi = arviointi(9),
        )

      "ET ja KT pakollisina -> HTTP 400" in {
        setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(yhdeksännenLuokanSuoritus, päättötodistusSuoritus.copy(osasuoritukset = Some(List(
          testisuoritus("KT", true),
          testisuoritus("ET", true)
        )))))) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.duplikaattiOsasuoritus("Samassa perusopetuksen suorituksessa ei voi esiintyä oppiaineita KT- ja ET-koodiarvoilla"))
        }
      }
      "ET ja KT joista toinen pakollinen ja toinen valinnainen -> HTTP 400" in {
        setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(yhdeksännenLuokanSuoritus, päättötodistusSuoritus.copy(osasuoritukset = Some(List(
          testisuoritus("KT", true),
          testisuoritus("ET", false)
        )))))) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.duplikaattiOsasuoritus("Samassa perusopetuksen suorituksessa ei voi esiintyä oppiaineita KT- ja ET-koodiarvoilla"))
        }
      }
      "ET ja KT valinnaisina 8. luokan suorituksessa -> HTTP 400" in {
        setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(
          yhdeksännenLuokanSuoritus,
          kahdeksannenLuokanSuoritus.copy(osasuoritukset = Some(List(
            testisuoritus("KT", false),
            testisuoritus("ET", false)
          ))),
          päättötodistusSuoritus
        ))) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.duplikaattiOsasuoritus("Samassa perusopetuksen suorituksessa ei voi esiintyä oppiaineita KT- ja ET-koodiarvoilla"))
        }
      }
      "ET pakollisena ja monta ET:ta valinnaisina -> HTTP 200" in {
        setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(yhdeksännenLuokanSuoritus, päättötodistusSuoritus.copy(osasuoritukset = Some(List(
          testisuoritus("ET", true),
          testisuoritus("ET", false),
          testisuoritus("ET", false)
        )))))) {
          verifyResponseStatusOk()
        }
      }
      "ET ja KT eri päätason suorituksissa -> HTTP 200" in {
        setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(
          kahdeksannenLuokanSuoritus.copy(osasuoritukset = Some(List(
            testisuoritus("ET", true)
          ))),
          yhdeksännenLuokanSuoritus,
          päättötodistusSuoritus.copy(osasuoritukset = Some(List(
            testisuoritus("KT", true)
          )))
        ))) {
          verifyResponseStatusOk()
        }
      }
    }

    "Opiskeluoikeudelta puuttuu päättötodistus opiskeluoikeuden tilan ollessa 'valmistunut' -> HTTP 400" in {
      setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(
        suoritukset = List(
          yhdeksännenLuokanSuoritus.copy(alkamispäivä = Some(LocalDate.of(2016, 1, 1)))
        ),
        tila = NuortenPerusopetuksenOpiskeluoikeudenTila(List(
          NuortenPerusopetuksenOpiskeluoikeusjakso(LocalDate.of(2016, 1, 1), opiskeluoikeusLäsnä),
          NuortenPerusopetuksenOpiskeluoikeusjakso(LocalDate.of(2017, 1, 1), opiskeluoikeusValmistunut)
        ))
      )) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.nuortenPerusopetuksenValmistunutTilaIlmanVahvistettuaPäättötodistusta())
      }
    }

    "Oppijalla ei voi olla kahta keskeneräistä vuosiluokan suoritusta" - {
      "Siirrettäessä kerralla -> HTTP 400" in {
        setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(
          perusopetuksenOppimääränSuoritusKesken,
          kahdeksannenLuokanSuoritus.copy(vahvistus = None),
          yhdeksännenLuokanSuoritus.copy(vahvistus = None)
        ))) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.useitaKeskeneräisiäVuosiluokanSuoritukia())
        }
      }

      "Siirrettäessä erissä -> HTTP 400" in {
        val headers = authHeaders(jyväskylänNormaalikoulunPalvelukäyttäjä) ++ jsonContent
        val opiskeluoikeus = defaultOpiskeluoikeus.copy(
          lähdejärjestelmänId = Some(primusLähdejärjestelmäId("primus-30405321")),
          suoritukset = List(
            perusopetuksenOppimääränSuoritusKesken,
            kahdeksannenLuokanSuoritus.copy(vahvistus = None),
          ),
        )

        setupOppijaWithOpiskeluoikeus(opiskeluoikeus, headers = headers) {
          verifyResponseStatusOk()
        }

        putOpiskeluoikeus(
          opiskeluoikeus = opiskeluoikeus.copy(suoritukset = List(yhdeksännenLuokanSuoritus.copy(vahvistus = None))),
          headers = headers,
        ) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.useitaKeskeneräisiäVuosiluokanSuoritukia())
        }
      }
    }

    "Opiskeluoikeudella ei saa olla sama alkamispäivä kahdella vuosiluokalla" - {
      "Siirto estetty" in {
        setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(
          suoritukset = List(
            yhdeksännenLuokanSuoritus.copy(alkamispäivä = Some(LocalDate.of(2006, 1, 1))),
            kahdeksannenLuokanSuoritus.copy(alkamispäivä = Some(LocalDate.of(2006, 1, 1)))
          )
        )) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaSuorituksia("Vuosiluokilla (perusopetuksenluokkaaste/9, perusopetuksenluokkaaste/8) on sama alkamispäivä. Kahdella tai useammalla vuosiluokalla ei saa olla sama alkamispäivämäärä."))
        }
      }
      "Jo Koskeen tallennetut, uudesta tiedonsiirrosta puuttuvat, vuosiluokan suoritukset otetaan mukaan validaatioon" in {
        val opiskeluoikeus = defaultOpiskeluoikeus.copy(
          suoritukset = List(seitsemännenLuokanSuoritus.copy(alkamispäivä = Some(LocalDate.of(2006, 1, 1))))
        )

        setupOppijaWithOpiskeluoikeus(opiskeluoikeus, KoskiSpecificMockOppijat.eero) {
          verifyResponseStatusOk()
        }

        val edellinenVersio = lastOpiskeluoikeus(KoskiSpecificMockOppijat.eero.oid)
        val osittaisillaSuorituksilla = opiskeluoikeus.copy(
          suoritukset = List(kahdeksannenLuokanSuoritus.copy(alkamispäivä = Some(LocalDate.of(2006, 1, 1))))
        ).withOidAndVersion(edellinenVersio.oid, edellinenVersio.versionumero)

        putOpiskeluoikeus(osittaisillaSuorituksilla, KoskiSpecificMockOppijat.eero) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.epäsopiviaSuorituksia("Vuosiluokilla (perusopetuksenluokkaaste/7, perusopetuksenluokkaaste/8) on sama alkamispäivä. Kahdella tai useammalla vuosiluokalla ei saa olla sama alkamispäivämäärä."))
        }
      }
    }

    "Kun suorituksen tila 'vahvistettu', opiskeluoikeuden tila ei voi olla 'eronnut' tai 'katsotaan eronneeksi'" in {
      val opiskeluoikeus = defaultOpiskeluoikeus.copy(
        tila = NuortenPerusopetuksenOpiskeluoikeudenTila(List(
          NuortenPerusopetuksenOpiskeluoikeusjakso(LocalDate.of(2015, 1, 1), opiskeluoikeusLäsnä),
          NuortenPerusopetuksenOpiskeluoikeusjakso(LocalDate.of(2017, 1, 1), opiskeluoikeusEronnut)
        )))
      setupOppijaWithOpiskeluoikeus(opiskeluoikeus) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.tilaEronnutTaiKatsotaanEronneeksiVaikkaVahvistettuPäätasonSuoritus())
      }
    }
  }

  "Arvosanat" - {

    "Päättötodistuksella ei ole vahvistusta" - {
      val vahvistamatonPäättötodistus = päättötodistusSuoritus.copy(vahvistus = None)

      "S" - {
        val valinnainenS = suoritus(kieli("B1", "SV").copy(pakollinen = false, laajuus = vuosiviikkotuntia(2))).copy(arviointi = hyväksytty)
        val pakollinenS = äidinkielenSuoritus.copy(arviointi = hyväksytty)

        "Sallitaan pakollisten oppiaineiden suorituksilta" in {
          setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(vahvistamatonPäättötodistus.copy(osasuoritukset = Some(List(pakollinenS)))))) {
            verifyResponseStatusOk()
          }
        }

        "Sallitaan valinnaisten oppiaineiden suorituksilta joiden laajuus on 2 vuosiviikkotuntia tai yli" in {
          setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(vahvistamatonPäättötodistus.copy(osasuoritukset = Some(List(valinnainenS)))))) {
            verifyResponseStatusOk()
          }
        }
      }

      "O" - {
        val valinnainenO = suoritus(kieli("B1", "SV").copy(pakollinen = false, laajuus = vuosiviikkotuntia(2))).copy(arviointi = osallistunut)
        val pakollinenO = äidinkielenSuoritus.copy(arviointi = osallistunut)

        "Sallittu pakollisten oppiaineiden suorituksilta" in {
          setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(vahvistamatonPäättötodistus.copy(osasuoritukset = Some(List(pakollinenO)))))) {
            verifyResponseStatusOk()
          }
        }

        "Sallittu valinnaisten oppiaineiden suorituksilta joiden laajuus on 2 vuosiviikkotuntia tai yli" in {
          setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(vahvistamatonPäättötodistus.copy(osasuoritukset = Some(List(valinnainenO)))))) {
            verifyResponseStatusOk()
          }
        }
      }
    }

    "Päättötodistuksella on vahvistus" - {
      "S" - {
        val valinnainenS = suoritus(äidinkieli("AI1").copy(pakollinen = false, laajuus = vuosiviikkotuntia(2))).copy(arviointi = hyväksytty)
        val pakollinenS = äidinkielenSuoritus.copy(arviointi = hyväksytty)

        "Kielletty pakollisten oppiaineiden suorituksilta" in {
          setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(yhdeksännenLuokanSuoritus, päättötodistusSuoritus.copy(osasuoritukset = Some(List(pakollinenS)))))) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.arviointi.sallittuVainValinnaiselle("Arviointi S on sallittu vain jos oppimäärä on yksilöllistetty tai valinnaisille oppiaineille joiden laajuus on alle kaksi vuosiviikkotuntia"))
          }
        }

        "Sallittu yksilöllistetyille pakollisille" in {
          setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(yhdeksännenLuokanSuoritus, päättötodistusSuoritus.copy(osasuoritukset = Some(List(pakollinenS.copy(yksilöllistettyOppimäärä = true))))))) {
            verifyResponseStatusOk()
          }
        }

        "Sallittu väliaikaisesti valinnaisten oppiaineiden suorituksille joiden laajuus on 2 vuosiviikkotuntia tai yli" in {
          setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(yhdeksännenLuokanSuoritus, päättötodistusSuoritus.copy(osasuoritukset = Some(List(valinnainenS)))))) {
            verifyResponseStatusOk()
          }
        }

        "Sallittu valinnaisten kielioppiaineiden suorituksilta joiden laajuus on 2 vuosiviikkotuntia tai yli" in {
          val valinnainenKieliS = suoritus(kieli("B1", "SV").copy(pakollinen = false, laajuus = vuosiviikkotuntia(2))).copy(arviointi = hyväksytty)
          setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(yhdeksännenLuokanSuoritus, päättötodistusSuoritus.copy(osasuoritukset = Some(List(valinnainenKieliS)))))) {
            verifyResponseStatusOk()
          }
        }

        "Sallittu valinnaisille oppiaineiden suorituksille joiden laajuus on alle 2" in {
          val valinnainenLaajuusAlle2 = suoritus(oppiaine("HI").copy(pakollinen = false, laajuus = vuosiviikkotuntia(1.9))).copy(arviointi = hyväksytty)
          setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(yhdeksännenLuokanSuoritus, päättötodistusSuoritus.copy(osasuoritukset = Some(List(valinnainenLaajuusAlle2)))))) {
            verifyResponseStatusOk()
          }
        }

        "Sallittu paikallisille oppiaineille joiden laajuus 2 vuosiviikkotuntia tai yli" in {
          val paikallinenLaajuus2 = suoritus(paikallinenOppiaine("HI", "Historia", "Opiskellaan historiaa", vuosiviikkotuntia(2))).copy(arviointi = hyväksytty)
          setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(yhdeksännenLuokanSuoritus, päättötodistusSuoritus.copy(osasuoritukset = Some(List(paikallinenLaajuus2)))))) {
            verifyResponseStatusOk()
          }
        }

        "Sallittu paikallisille oppiaineille joiden laajuus on alle 2 vuosiviikkotuntia" in {
          val paikallinenLaajuus2 = suoritus(paikallinenOppiaine("HI", "Historia", "Opiskellaan historiaa", vuosiviikkotuntia(1.9))).copy(arviointi = hyväksytty)
          setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(yhdeksännenLuokanSuoritus, päättötodistusSuoritus.copy(osasuoritukset = Some(List(paikallinenLaajuus2)))))) {
            verifyResponseStatusOk()
          }
        }
      }

      "O" - {
        val valinnainenO = suoritus(äidinkieli("AI1").copy(pakollinen = false, laajuus = vuosiviikkotuntia(2))).copy(arviointi = osallistunut)
        val pakollinenO = äidinkielenSuoritus.copy(arviointi = osallistunut)

        "Kielletty pakollisten oppiaineiden suorituksilta" in {
          setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(yhdeksännenLuokanSuoritus, päättötodistusSuoritus.copy(osasuoritukset = Some(List(pakollinenO)))))) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.arviointi.sallittuVainValinnaiselle("Arviointi O on sallittu vain jos oppimäärä on yksilöllistetty tai valinnaisille oppiaineille joiden laajuus on alle kaksi vuosiviikkotuntia"))
          }
        }

        "Sallittu yksilöllistetyille pakollisille" in {
          setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(yhdeksännenLuokanSuoritus, päättötodistusSuoritus.copy(osasuoritukset = Some(List(pakollinenO.copy(yksilöllistettyOppimäärä = true))))))) {
            verifyResponseStatusOk()
          }
        }

        "Kielletty valinnaisten oppiaineiden suorituksilta joiden laajuus on 2 vuosiviikkotuntia tai yli" in {
          setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(yhdeksännenLuokanSuoritus, päättötodistusSuoritus.copy(osasuoritukset = Some(List(valinnainenO)))))) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.arviointi.sallittuVainValinnaiselle("Arviointi O on sallittu vain jos oppimäärä on yksilöllistetty tai valinnaisille oppiaineille joiden laajuus on alle kaksi vuosiviikkotuntia"))
          }
        }

        "Kielletty valinnaisten kielioppiaineiden suorituksilta joiden laajuus on 2 vuosiviikkotuntia tai yli" in {
          val valinnainenKieliO = suoritus(kieli("B1", "SV").copy(pakollinen = false, laajuus = vuosiviikkotuntia(2))).copy(arviointi = osallistunut)
          setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(yhdeksännenLuokanSuoritus, päättötodistusSuoritus.copy(osasuoritukset = Some(List(valinnainenKieliO)))))) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.arviointi.sallittuVainValinnaiselle("Arviointi O on sallittu vain jos oppimäärä on yksilöllistetty tai valinnaisille oppiaineille joiden laajuus on alle kaksi vuosiviikkotuntia"))
          }
        }

        "Sallittu valinnaisille oppiaineiden suorituksille joiden laajuus on alle 2" in {
          val valinnainenLaajuusAlle2 = suoritus(äidinkieli("AI1").copy(pakollinen = false, laajuus = vuosiviikkotuntia(1.9))).copy(arviointi = osallistunut)
          setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(yhdeksännenLuokanSuoritus, päättötodistusSuoritus.copy(osasuoritukset = Some(List(valinnainenLaajuusAlle2)))))) {
            verifyResponseStatusOk()
          }
        }

        "Sallittu paikallisille oppiaineille joiden laajuus on alle 2 vuosiviikkotuntia" in {
          val paikallinenLaajuusAlle2 = suoritus(paikallinenOppiaine("HI", "Historia", "Opiskellaan historiaa", vuosiviikkotuntia(1.9))).copy(arviointi = osallistunut)
          setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(yhdeksännenLuokanSuoritus, päättötodistusSuoritus.copy(osasuoritukset = Some(List(paikallinenLaajuusAlle2)))))) {
            verifyResponseStatusOk()
          }
        }

        "Kielletty paikallisten oppiaineden suorituksilta joiden laajuus on 2 vuosiviikkotuntia tai yli" in {
          val paikallinenLaajuus2 = suoritus(paikallinenOppiaine("HI", "Historia", "Opiskellaan historiaa", vuosiviikkotuntia(2))).copy(arviointi = osallistunut)
          setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(yhdeksännenLuokanSuoritus, päättötodistusSuoritus.copy(osasuoritukset = Some(List(paikallinenLaajuus2)))))) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.arviointi.sallittuVainValinnaiselle("Arviointi O on sallittu vain jos oppimäärä on yksilöllistetty tai valinnaisille oppiaineille joiden laajuus on alle kaksi vuosiviikkotuntia"))
          }
        }
      }

      "4-10" - {
        "Kielletty valinnaiselle valtakunnalliselle oppiaineelle, jonka laajuus on alle kaksi vuosiviikkotuntia" in {
          val valinnainenLaajuusAlle2 = suoritus(
            oppiaine("BI").copy(pakollinen = false, laajuus = vuosiviikkotuntia(1.9))
          ).copy(arviointi = arviointi(9))

          setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(yhdeksännenLuokanSuoritus, päättötodistusSuoritus.copy(osasuoritukset = Some(List(valinnainenLaajuusAlle2)))))) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.arviointi.eiSallittuSuppealleValinnaiselle(

            ))
          }
        }

        "Sallittu valinnaiselle valtakunnalliselle oppiaineelle, jonka laajuus on kaksi vuosiviikkotuntia" in {
          val valinnainenLaajuus2 = suoritus(
            oppiaine("BI").copy(pakollinen = false, laajuus = vuosiviikkotuntia(2))
          ).copy(arviointi = arviointi(9))

          setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(yhdeksännenLuokanSuoritus, päättötodistusSuoritus.copy(osasuoritukset = Some(List(valinnainenLaajuus2)))))) {
            verifyResponseStatusOk()
          }
        }
      }

      "Opinto-ohjaus (OP) oppiaineena" - {
        "Sallitaan aina arvosana S" in {
          val opinto_ohjaus_S = suoritus(oppiaine("OP")).copy(arviointi = hyväksytty)
          setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(yhdeksännenLuokanSuoritus, päättötodistusSuoritus.copy(osasuoritukset = Some(List(opinto_ohjaus_S)))))) {
            verifyResponseStatusOk()
          }
        }
        "Sallitaan aina arvosana O" in {
          val opinto_ohjaus_O = suoritus(oppiaine("OP")).copy(arviointi = osallistunut)
          setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(suoritukset = List(yhdeksännenLuokanSuoritus, päättötodistusSuoritus.copy(osasuoritukset = Some(List(opinto_ohjaus_O)))))) {
            verifyResponseStatusOk()
          }
        }
      }
    }
  }

  "Laajuus" - {
    def verify[A](
      päätasonSuoritus: PerusopetuksenPäätasonSuoritus,
      opiskeluoikeus: PerusopetuksenOpiskeluoikeus = defaultOpiskeluoikeus,
      pakollisenOppiaineenSuoritustapa: Option[Koodistokoodiviite] = None
    )(fn: => A): A = {
      val pakollinenEiLaajuutta = suoritus(oppiaine("GE").copy(pakollinen = true, laajuus = None)).copy(arviointi = arviointi(9), suoritustapa = pakollisenOppiaineenSuoritustapa)
      setupOppijaWithOpiskeluoikeus(opiskeluoikeus.withSuoritukset(List(
        yhdeksännenLuokanSuoritus,
        päätasonSuoritus.withOsasuoritukset(Some(List(pakollinenEiLaajuutta))))
      )) {
        fn
      }
    }

    "Suorituksen vahvistuspäivä on 1.8.2020 tai sen jälkeen" - {
      "Vuosiluokan suoritus" - {
        "Pakollisilla oppiaineille tulee olla laajuus > 0" in {
          verify(seitsemännenLuokanSuoritus.copy(vahvistus = vahvistusPaikkakunnalla(LocalDate.of(2020, 8, 1)))) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.laajuudet.oppiaineenLaajuusPuuttuu("Oppiaineen koskioppiaineetyleissivistava/GE laajuus puuttuu"))
          }
        }
      }
      "Päättötodistus" - {
        "Pakollisilla oppiaineille tulee olla laajuus > 0" in {
          verify(päättötodistusSuoritus.copy(vahvistus = vahvistusPaikkakunnalla(LocalDate.of(2020, 8, 1)))) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.laajuudet.oppiaineenLaajuusPuuttuu("Oppiaineen koskioppiaineetyleissivistava/GE laajuus puuttuu"))
          }
        }
      }
      "Toiminta-alueittain opiskeltu" - {
        "Laajuutta ei vaadita osasuorituksilta" in {
          setupOppijaWithOpiskeluoikeus(
            defaultOpiskeluoikeus.copy(
              suoritukset = List(yhdeksännenLuokanSuoritus, päättötodistusToimintaAlueilla.copy(vahvistus = vahvistusPaikkakunnalla(LocalDate.of(2020, 8, 1))))
            )) {
            verifyResponseStatusOk()
          }
        }
      }
    }
    "Suorituksen vahvistuspäivä on ennen 1.8.2020" - {
      "Vuosiluokan suoritus" - {
        "Laajuutta ei vaadita pakollisilta oppiaineilta" in {
          verify(seitsemännenLuokanSuoritus.copy(vahvistus = vahvistusPaikkakunnalla(LocalDate.of(2020, 7, 31)))) {
            verifyResponseStatusOk()
          }
        }
      }
      "Päättötodistus" - {
        "Laajuutta ei vaadita pakollisilta oppiaineilta" in {
          verify(päättötodistusSuoritus.copy(vahvistus = vahvistusPaikkakunnalla(LocalDate.of(2020, 7, 31)))) {
            verifyResponseStatusOk()
          }
        }
      }
      "Toiminta-alueittain opiskeltu" - {
        "Laajuutta ei vaadita osasuorituksilta" in {
          verify(päättötodistusToimintaAlueilla.copy(vahvistus = vahvistusPaikkakunnalla(LocalDate.of(2020, 7, 31)))) {
            verifyResponseStatusOk()
          }
        }
      }
    }
    "Suorituksella ei ole vahvistuspäivää" - {
      "Vuosiluokan suoritus" - {
        "Laajuutta ei vaadita pakollisilta oppiaineilta" in {
          verify(seitsemännenLuokanSuoritus.copy(vahvistus = None)) {
            verifyResponseStatusOk()
          }
        }
      }
      "Päättötodistus" - {
        "Laajuutta ei vaadita pakollisilta oppiaineilta" in {
          verify(päättötodistusSuoritus.copy(vahvistus = None)) {
            verifyResponseStatusOk()
          }
        }
      }
    }
    "Suoritustapana on erityinen tutkinto" - {
      "Vuosiluokan suoritus" - {
        "Laajuutta ei vaadita pakollisilta oppianeilta" in {
          verify(seitsemännenLuokanSuoritus.copy(suoritustapa = Some(suoritustapaErityinenTutkinto), vahvistus = vahvistusPaikkakunnalla(LocalDate.of(2020, 8, 1)))) {
            verifyResponseStatusOk()
          }
        }
      }
      "Päättötodistus" - {
        "Laajuutta ei vaadita pakollisilta oppiaineilta" in {
          verify(päättötodistusSuoritus.copy(suoritustapa = suoritustapaErityinenTutkinto, vahvistus = vahvistusPaikkakunnalla(LocalDate.of(2020, 8, 1)))) {
            verifyResponseStatusOk()
          }
        }
      }
    }
    "Opiskeluoikeudella on lisätiedoissa kotiopetusjakso" - {
      val opiskeluoikeus = defaultOpiskeluoikeus.copy(lisätiedot = Some(PerusopetuksenOpiskeluoikeudenLisätiedot(
        kotiopetus = Some(Aikajakso(LocalDate.of(2020, 7, 1), Some(LocalDate.of(2020, 8, 1)))),
        kotiopetusjaksot = Some(List(Aikajakso(LocalDate.of(2020, 10, 1), Some(LocalDate.of(2020, 10, 2)))))
      )))
      "Kotiopetusjakso on voimassa suorituksen vahvistuspäivänä" - {
        "Vuosiluokan suoritus" - {
          "Laajuutta ei vaadita pakollisilta oppiaineilta" in {
            verify(seitsemännenLuokanSuoritus.copy(vahvistus = vahvistusPaikkakunnalla(LocalDate.of(2020, 8, 1))), opiskeluoikeus) {
              verifyResponseStatusOk()
            }
          }
        }
        "Päättötodistus" - {
          "Laajuutta ei vaadita pakollisilta oppiaineilta" in {
            verify(päättötodistusSuoritus.copy(vahvistus = vahvistusPaikkakunnalla(LocalDate.of(2020, 10, 1))), opiskeluoikeus) {
              verifyResponseStatusOk()
            }
          }
        }
      }
      "Kotiopetusjakso ei ole voimassa suorituksen vahvistuspäivänä" - {
        "Vuosiluokan suoritus" - {
          "Pakollisilla oppiaineille tulee olla laajuus > 0" in {
            verify(seitsemännenLuokanSuoritus.copy(vahvistus = vahvistusPaikkakunnalla(LocalDate.of(2020, 8, 2))), opiskeluoikeus) {
              verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.laajuudet.oppiaineenLaajuusPuuttuu("Oppiaineen koskioppiaineetyleissivistava/GE laajuus puuttuu"))
            }
          }
        }
        "Päättötodistus" - {
          "Pakollisilla oppiaineille tulee olla laajuus > 0" in {
            verify(päättötodistusSuoritus.copy(vahvistus = vahvistusPaikkakunnalla(LocalDate.of(2020, 8, 2))), opiskeluoikeus) {
              verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.laajuudet.oppiaineenLaajuusPuuttuu("Oppiaineen koskioppiaineetyleissivistava/GE laajuus puuttuu"))
            }
          }
        }
      }
    }
    "Oppiaineen suoritustapa on erityinen tutkinto" - {
      "Laajuutta ei vaadita pakollisilta oppiaineilta" in {
        verify(seitsemännenLuokanSuoritus.copy(vahvistus = vahvistusPaikkakunnalla(LocalDate.of(2020, 8, 1))), pakollisenOppiaineenSuoritustapa = Some(suoritustapaErityinenTutkinto)) {
          verifyResponseStatusOk()
        }
      }
    }
  }

  "Osa-aikainen erityisopetus" - {
    "Opiskeluoikeudella on erityisen tuen päätös muusta kuin osa-aikaisesta erityisopetuksesta, muttei tietoa suorituksessa -> HTTP 200" in {
      setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(
        lisätiedot = perusopetuksenOpiskeluoikeudenLisätiedotJoissaErityisenTuenPäätösIlmanOsaAikaistaErityisopetusta,
        suoritukset = List(vuosiluokkasuoritus.copy(osaAikainenErityisopetus = None))
      )) {
        verifyResponseStatusOk()
      }
    }

    "Opiskeluoikeudella on tehostetun tuen päätös muusta kuin osa-aikaisesta erityisopetuksesta, muttei tietoa suorituksessa -> HTTP 200" in {
      setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(
        lisätiedot = perusopetuksenOpiskeluoikeudenLisätiedotJoissaTehostetunTuenPäätösIlmanOsaAikaistaErityisopetusta,
        suoritukset = List(vuosiluokkasuoritus.copy(osaAikainenErityisopetus = None))
      )) {
        verifyResponseStatusOk()
      }
    }
  }

  "Äidinkielen omainen oppiaine" - {
    def verify[A](kieliKoodiarvo: String)(expect: => A): A = {
      val oo = defaultOpiskeluoikeus.copy(
        suoritukset = List(
          yhdeksännenLuokanSuoritus,
          päättötodistusSuoritus.copy(
            vahvistus = None,
            osasuoritukset = Some(List(suoritus(kieli("AOM", kieliKoodiarvo))))
        ))
      )

      setupOppijaWithOpiskeluoikeus(oo) {
        expect
      }
    }

    "FI sallittu" in {
      verify("FI") {
        verifyResponseStatusOk()
      }
    }
    "SV sallittu" in {
      verify("SV") {
        verifyResponseStatusOk()
      }
    }
    "Muita ei sallita" in {
      verify("SE") {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.deprekoituKielikoodi("Äidinkielen omaisen oppiaineen kieli tulee olla suomi tai ruotsi"))
      }
    }
  }

  "Deprekoituja kenttiä, jotka tiputetaan siirrossa pois" - {
    "Lisätiedon kenttiä perusopetuksenAloittamistaLykatty, tehostetunTuenPäätös, tehostetunTuenPäätökset ja tukimuodot ei oteta vastaan siirrossa" in {
      val oo = defaultOpiskeluoikeus.withLisätiedot(
        Some(PerusopetuksenOpiskeluoikeudenLisätiedot(
          perusopetuksenAloittamistaLykätty = Some(true),
          tehostetunTuenPäätös = Some(tehostetunTuenPäätösIlmanOsaAikaistaErityisopetusta),
          tehostetunTuenPäätökset = Some(List(tehostetunTuenPäätösIlmanOsaAikaistaErityisopetusta)),
          tukimuodot = Some(List(osaAikainenErityisopetus))
        )
      ))

      val tallennettuna = setupOppijaWithAndGetOpiskeluoikeus(oo)

      tallennettuna.lisätiedot.get.perusopetuksenAloittamistaLykätty should equal (None)
      tallennettuna.lisätiedot.get.tehostetunTuenPäätös should equal (None)
      tallennettuna.lisätiedot.get.tehostetunTuenPäätökset should equal (None)
      tallennettuna.lisätiedot.get.tukimuodot should equal (None)
    }

    "Vuosiluokan suorituksen kenttää osaAikainenErityisopetus ei oteta vastaan siirrossa - kenttä riippuu tehosteTuenPäätöksestä" in {
      val oo = defaultOpiskeluoikeus.withSuoritukset(
          List(vuosiluokkasuoritus.copy(
            osaAikainenErityisopetus = Some(true)
          ))
        )

      val tallennettuna = setupOppijaWithAndGetOpiskeluoikeus(oo)

      tallennettuna.suoritukset.find(_.isInstanceOf[PerusopetuksenVuosiluokanSuoritus]).get
        .asInstanceOf[PerusopetuksenVuosiluokanSuoritus].osaAikainenErityisopetus should equal (None)
    }
  }

  "Opiskeluoikeuden päättymispäivä on vuosiluokaan suorituksen alkamispäivää ennen -> HTTP 400" in {
    setupOppijaWithOpiskeluoikeus(defaultOpiskeluoikeus.copy(
      suoritukset = List(
        yhdeksännenLuokanSuoritus.copy(alkamispäivä = Some(LocalDate.of(2017, 1, 2)),
          vahvistus = None
      )),
      tila = NuortenPerusopetuksenOpiskeluoikeudenTila(List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(LocalDate.of(2016, 1, 1), opiskeluoikeusLäsnä),
        NuortenPerusopetuksenOpiskeluoikeusjakso(LocalDate.of(2017, 1, 1), opiskeluoikeusKatsotaanEronneeksi)
      ))
    )) {
      verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.päättymisPäiväEnnenAlkamispäivää("Vuosiluokan 9 suoritus ei voi alkaa opiskeluoikeuden päättymisen jälkeen"))
    }
  }

  "Opiskeluoikeus alkaa myöhemmin kuin aikaisin päätason suoritus -> HTTP 400" in {
    val seiskaluokanSuoritus = defaultOpiskeluoikeus.copy(
      lähdejärjestelmänId = Some(primusLähdejärjestelmäId("primus-30405321")),
      suoritukset = List(
        seitsemännenLuokanSuoritus.copy(alkamispäivä = Some(LocalDate.of(2017, 1, 1)),
          vahvistus = Some(HenkilövahvistusPaikkakunnalla(LocalDate.of(2017, 1, 1), jyväskylä, jyväskylänNormaalikoulu, List(Organisaatiohenkilö("Reijo Reksi", "rehtori", jyväskylänNormaalikoulu))))
        )),
      tila = NuortenPerusopetuksenOpiskeluoikeudenTila(List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(LocalDate.of(2017, 1, 1), opiskeluoikeusLäsnä),
        NuortenPerusopetuksenOpiskeluoikeusjakso(LocalDate.of(2019, 1, 1), opiskeluoikeusKatsotaanEronneeksi)
      ))
    )

    setupOppijaWithOpiskeluoikeus(seiskaluokanSuoritus, headers = authHeaders(jyväskylänNormaalikoulunPalvelukäyttäjä) ++ jsonContent) {
      verifyResponseStatus(200)
    }

    val kasiluokanSuoritusVäärälläAlkupäivällä = seiskaluokanSuoritus.copy(
      suoritukset = List(
        // valmis seiskaluokan suoritus kopioidaan tämän rinnalle luokassa OpiskeluoikeusChangeMigrator
        kahdeksannenLuokanSuoritus.copy(alkamispäivä = Some(LocalDate.of(2018, 1, 1)),
          vahvistus = None
        )),
      tila = NuortenPerusopetuksenOpiskeluoikeudenTila(List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(LocalDate.of(2018, 1, 1), opiskeluoikeusLäsnä),
        NuortenPerusopetuksenOpiskeluoikeusjakso(LocalDate.of(2019, 1, 3), opiskeluoikeusKatsotaanEronneeksi)
      ))
    )

    putOpiskeluoikeus(kasiluokanSuoritusVäärälläAlkupäivällä, headers = authHeaders(jyväskylänNormaalikoulunPalvelukäyttäjä) ++ jsonContent) {
      verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.date.suorituksenAlkamispäiväEnnenOpiskeluoikeudenAlkamispäivää("opiskeluoikeuden ensimmäisen tilan alkamispäivä (2018-01-01) oltava sama tai aiempi kuin päätason suorituksen alkamispäivä (2017-01-01)"))
    }
  }

  "Valmiiksi merkkaaminen ilman 9. luokka-asteen suoritusta" - {
    val oo = defaultOpiskeluoikeus.copy(
      tila = NuortenPerusopetuksenOpiskeluoikeudenTila(List(
        NuortenPerusopetuksenOpiskeluoikeusjakso(LocalDate.of(2016, 1, 1), opiskeluoikeusLäsnä)
      )),
      suoritukset = List(päättötodistusSuoritus),
      oppilaitos = Some(ressunLukio)
    )

    "Opiskeluoikeus ilman poikkeavia lisätietoja -> HTTP 400" in {
      setupOppijaWithOpiskeluoikeus(oo) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.nuortenPerusopetuksenValmistunutTilaIlmanYsiluokanSuoritusta())
      }
    }

    "Lisätiedoissa vuosiluokkiin sitoutumaton opetus -> HTTP 200" in {
      setupOppijaWithOpiskeluoikeus(oo.copy(
        lisätiedot = Some(PerusopetuksenOpiskeluoikeudenLisätiedot(
          vuosiluokkiinSitoutumatonOpetus = true
        ))
      )) {
        verifyResponseStatusOk()
      }
    }

    "Opiskelija ollut valmistuessa kotiopetuksessa -> HTTP 200" in {
      setupOppijaWithOpiskeluoikeus(oo.copy(
        suoritukset = List(päättötodistusSuoritus.copy(
          vahvistus = vahvistusPaikkakunnalla(LocalDate.of(2016, 6, 4))
        )),
        lisätiedot = Some(PerusopetuksenOpiskeluoikeudenLisätiedot(
          kotiopetusjaksot = Some(List(Aikajakso(LocalDate.of(2016, 6, 4), Some(LocalDate.of(2016, 6, 4)))))
        ))
      )) {
        verifyResponseStatusOk()
      }
    }

    "Päättötodistuksessa suoritustapa 'erityinen tutkinto' -> HTTP 200" in {
      setupOppijaWithOpiskeluoikeus(oo.withSuoritukset(
        List(päättötodistusSuoritus.copy(
          suoritustapa = suoritustapaErityinenTutkinto
        ))
      )) {
        verifyResponseStatusOk()
      }
    }
  }

  "Opiskeluoikeuksien duplikaatit" - {

    def duplikaattiaEiSallittu(oo1: PerusopetuksenOpiskeluoikeus, oo2: PerusopetuksenOpiskeluoikeus): Unit = {
      setupOppijaWithOpiskeluoikeus(oo1, defaultHenkilö) {
        verifyResponseStatusOk()
      }
      postOppija(makeOppija(defaultHenkilö, List(oo2))) {
        verifyResponseStatus(409, KoskiErrorCategory.conflict.exists())
      }
    }

    def duplikaattiSallittu(oo1: PerusopetuksenOpiskeluoikeus, oo2: PerusopetuksenOpiskeluoikeus): Unit = {
      setupOppijaWithOpiskeluoikeus(oo1, defaultHenkilö) {
        verifyResponseStatusOk()
      }
      postOppija(makeOppija(defaultHenkilö, List(oo2))) {
        verifyResponseStatusOk()
      }
    }

    val lähdejärjestelmänId1 = Some(primusLähdejärjestelmäId("primus-yksi"))
    val lähdejärjestelmänId2 = Some(primusLähdejärjestelmäId("primus-kaksi"))

    def duplikaattiaEiSallittuLähdejärjestelmäIdllä(oo1: PerusopetuksenOpiskeluoikeus, oo2: PerusopetuksenOpiskeluoikeus): Unit = {
      setupOppijaWithOpiskeluoikeus(oo1.copy(lähdejärjestelmänId = lähdejärjestelmänId1), defaultHenkilö, headers = authHeaders(jyväskylänNormaalikoulunPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatusOk()
      }
      postOppija(makeOppija(defaultHenkilö, List(oo2.copy(lähdejärjestelmänId = lähdejärjestelmänId2))), headers = authHeaders(jyväskylänNormaalikoulunPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatus(409, KoskiErrorCategory.conflict.exists())
      }
    }

    def duplikaattiSallittuLähdejärjestelmäIdllä(oo1: PerusopetuksenOpiskeluoikeus, oo2: PerusopetuksenOpiskeluoikeus): Unit = {
      setupOppijaWithOpiskeluoikeus(oo1.copy(lähdejärjestelmänId = lähdejärjestelmänId1), defaultHenkilö, headers = authHeaders(jyväskylänNormaalikoulunPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatusOk()
      }
      postOppija(makeOppija(defaultHenkilö, List(oo2.copy(lähdejärjestelmänId = lähdejärjestelmänId2))), headers = authHeaders(jyväskylänNormaalikoulunPalvelukäyttäjä) ++ jsonContent) {
        verifyResponseStatusOk()
      }
    }

    "oppimäärän opiskeluoikeudet" - {
      "Samaa opiskeluoikeutta ei voi siirää kahteen kertaan" - {
        "ilman tunnistetta" in {
          duplikaattiaEiSallittu(defaultOpiskeluoikeus, defaultOpiskeluoikeus)
        }
        "lähdejärjestelmän id:llä" in {
          duplikaattiaEiSallittuLähdejärjestelmäIdllä(defaultOpiskeluoikeus, defaultOpiskeluoikeus)
        }
      }

      "Samaa opiskeluoikeutta ei voi siirää kahteen kertaan, vaikka se olisikin terminaalitilassa" - {
        val opiskeluoikeus = defaultOpiskeluoikeus.copy(
          tila = NuortenPerusopetuksenOpiskeluoikeudenTila(List(
            NuortenPerusopetuksenOpiskeluoikeusjakso(LocalDate.of(2015, 8, 15), opiskeluoikeusLäsnä),
            NuortenPerusopetuksenOpiskeluoikeusjakso(LocalDate.of(2016, 6, 4), opiskeluoikeusValmistunut),
          ))
        )

        "ilman tunnistetta" in {
          duplikaattiaEiSallittu(opiskeluoikeus, opiskeluoikeus)
        }
        "lähdejärjestelmän id:llä" in {
          duplikaattiaEiSallittuLähdejärjestelmäIdllä(opiskeluoikeus, opiskeluoikeus)
        }
      }

      "Samaa opiskeluoikeutta ei voi siirää kahteen kertaan, vaikka päivämäärät ovat erilaiset (mutta päällekkäiset) ja se olisikin terminaalitilassa" - {
        val opiskeluoikeus = defaultOpiskeluoikeus.copy(
          tila = NuortenPerusopetuksenOpiskeluoikeudenTila(List(
            NuortenPerusopetuksenOpiskeluoikeusjakso(LocalDate.of(2015, 8, 15), opiskeluoikeusLäsnä),
            NuortenPerusopetuksenOpiskeluoikeusjakso(LocalDate.of(2016, 6, 4), opiskeluoikeusValmistunut),
          ))
        )

        val opiskeluoikeus2 = opiskeluoikeus.copy(
          suoritukset = List(
            vuosiluokkasuoritus.copy(alkamispäivä = vuosiluokkasuoritus.alkamispäivä.map(_.plusDays(10))),
            päättötodistusSuoritus,
          )
        )

        "ilman tunnistetta" in {
          duplikaattiaEiSallittu(opiskeluoikeus, opiskeluoikeus2)
        }
        "lähdejärjestelmän id:llä" in {
          duplikaattiaEiSallittuLähdejärjestelmäIdllä(opiskeluoikeus, opiskeluoikeus2)
        }
        "saman oppijan eri oideilla ilman tunnistetta" in {
          mitätöiOppijanKaikkiOpiskeluoikeudet(KoskiSpecificMockOppijat.master)
          setupOppijaWithOpiskeluoikeus(opiskeluoikeus, KoskiSpecificMockOppijat.slave.henkilö) {
            verifyResponseStatusOk()
          }
          postOppija(makeOppija(KoskiSpecificMockOppijat.master, List(opiskeluoikeus2))) {
            verifyResponseStatus(409, KoskiErrorCategory.conflict.exists())
          }
        }
        "saman oppijan eri oideilla lähdejärjestelmän id:llä" in {
          val lähdejärjestelmänId1 = Some(primusLähdejärjestelmäId("primus-yksi"))
          val lähdejärjestelmänId2 = Some(primusLähdejärjestelmäId("primus-kaksi"))

          mitätöiOppijanKaikkiOpiskeluoikeudet(KoskiSpecificMockOppijat.master)
          setupOppijaWithOpiskeluoikeus(opiskeluoikeus.copy(lähdejärjestelmänId = lähdejärjestelmänId1), KoskiSpecificMockOppijat.slave.henkilö, headers = authHeaders(jyväskylänNormaalikoulunPalvelukäyttäjä) ++ jsonContent) {
            verifyResponseStatusOk()
          }
          postOppija(makeOppija(KoskiSpecificMockOppijat.master, List(opiskeluoikeus2.copy(lähdejärjestelmänId = lähdejärjestelmänId2))), headers = authHeaders(jyväskylänNormaalikoulunPalvelukäyttäjä) ++ jsonContent) {
            verifyResponseStatus(409, KoskiErrorCategory.conflict.exists())
          }
        }
      }

      "Samaa opiskeluoikeutta ei voi siirtää kahteen kertaan, vaikka päivämäärät ovat erilaiset (mutta päällekkäiset) ja se olisikin terminaalitilassa, variantti" - {
        val opiskeluoikeus = defaultOpiskeluoikeus.copy(
          tila = NuortenPerusopetuksenOpiskeluoikeudenTila(List(
            NuortenPerusopetuksenOpiskeluoikeusjakso(LocalDate.of(2015, 8, 15), opiskeluoikeusLäsnä),
            NuortenPerusopetuksenOpiskeluoikeusjakso(LocalDate.of(2016, 6, 4), opiskeluoikeusValmistunut),
          ))
        )

        val opiskeluoikeus2 = opiskeluoikeus.copy(
          tila = NuortenPerusopetuksenOpiskeluoikeudenTila(List(
            NuortenPerusopetuksenOpiskeluoikeusjakso(LocalDate.of(2015, 8, 15), opiskeluoikeusLäsnä),
          )),
          suoritukset = List(
            vuosiluokkasuoritus.copy(alkamispäivä = Some(LocalDate.of(2015, 8, 15))),
          )
        )

        "ilman tunnistetta" in {
          duplikaattiaEiSallittu(opiskeluoikeus, opiskeluoikeus2)
        }
        "lähdejärjestelmän id:llä" in {
          duplikaattiaEiSallittuLähdejärjestelmäIdllä(opiskeluoikeus, opiskeluoikeus2)
        }
      }


      "Opiskeluoikeuden voi siirää kahteen kertaan, kunhan aikajaksot eivät ole päällekkäiset ja aiempi on terminaalitilassa" - {
        val opiskeluoikeus = defaultOpiskeluoikeus.copy(
          tila = NuortenPerusopetuksenOpiskeluoikeudenTila(List(
            NuortenPerusopetuksenOpiskeluoikeusjakso(LocalDate.of(2015, 8, 15), opiskeluoikeusLäsnä),
            NuortenPerusopetuksenOpiskeluoikeusjakso(LocalDate.of(2016, 6, 4), opiskeluoikeusValmistunut),
          ))
        )

        val opiskeluoikeus2 = defaultOpiskeluoikeus.copy(
          tila = NuortenPerusopetuksenOpiskeluoikeudenTila(List(
            NuortenPerusopetuksenOpiskeluoikeusjakso(LocalDate.of(2016, 8, 15), opiskeluoikeusLäsnä),
          )),
          suoritukset = List(vuosiluokkasuoritus.copy(
            alkamispäivä = Some(LocalDate.of(2016, 8, 15)),
            vahvistus = None,
          ))
        )

        "ilman tunnistetta" in {
          duplikaattiSallittu(opiskeluoikeus, opiskeluoikeus2)
        }
        "lähdejärjestelmän id:llä" in {
          duplikaattiSallittuLähdejärjestelmäIdllä(opiskeluoikeus, opiskeluoikeus2)
        }
      }
    }

    "Aineopinnot" - {
      "Opiskeluoikeuden voi siirtää kahteen kertaan, kun toinen on oppimäärän ja toinen aineopintojen" - {
        val oppimääränOpiskeluoikeus = defaultOpiskeluoikeus.copy(
          tila = NuortenPerusopetuksenOpiskeluoikeudenTila(List(
            NuortenPerusopetuksenOpiskeluoikeusjakso(LocalDate.of(2015, 8, 15), opiskeluoikeusLäsnä),
            NuortenPerusopetuksenOpiskeluoikeusjakso(LocalDate.of(2016, 6, 4), opiskeluoikeusValmistunut),
          ))
        )
        val aineopintojenOpiskeluoikeus = oppimääränOpiskeluoikeus.copy(
          suoritukset = List(
            nuortenPerusOpetuksenOppiaineenOppimääränSuoritus("LI").copy(
              vahvistus = vahvistusPaikkakunnalla(LocalDate.of(2016, 6, 4))
            )
          )
        )

        "ilman tunnistetta" in {
          duplikaattiSallittu(oppimääränOpiskeluoikeus, aineopintojenOpiskeluoikeus)
        }
        "lähdejärjestelmän id:llä" in {
          duplikaattiSallittuLähdejärjestelmäIdllä(oppimääränOpiskeluoikeus, aineopintojenOpiskeluoikeus)
        }
      }

      "Valmistuneista aineopinnoista voi siirtää duplikaatin" - {
        val aineopintojenOpiskeluoikeus = defaultOpiskeluoikeus.copy(
          tila = NuortenPerusopetuksenOpiskeluoikeudenTila(List(
            NuortenPerusopetuksenOpiskeluoikeusjakso(LocalDate.of(2015, 8, 15), opiskeluoikeusLäsnä),
            NuortenPerusopetuksenOpiskeluoikeusjakso(LocalDate.of(2016, 6, 4), opiskeluoikeusValmistunut),
          )),
          suoritukset = List(
            nuortenPerusOpetuksenOppiaineenOppimääränSuoritus("LI").copy(
              vahvistus = vahvistusPaikkakunnalla(LocalDate.of(2016, 6, 4))
            )
          )
        )
        "ilman tunnistetta" in {
          duplikaattiSallittu(aineopintojenOpiskeluoikeus, aineopintojenOpiskeluoikeus)
        }
        "lähdejärjestelmän id:llä" in {
          duplikaattiSallittuLähdejärjestelmäIdllä(aineopintojenOpiskeluoikeus, aineopintojenOpiskeluoikeus)
        }
      }

      "Keskeneräisistä aineopinnoista ei voi siirtää duplikaattia" - {
        val aineopintojenOpiskeluoikeus = defaultOpiskeluoikeus.copy(
          tila = NuortenPerusopetuksenOpiskeluoikeudenTila(List(
            NuortenPerusopetuksenOpiskeluoikeusjakso(LocalDate.of(2015, 8, 15), opiskeluoikeusLäsnä),
          )),
          suoritukset = List(
            nuortenPerusOpetuksenOppiaineenOppimääränSuoritus("LI").copy(
              vahvistus = vahvistusPaikkakunnalla(LocalDate.of(2016, 6, 4))
            )
          )
        )
        "ilman tunnistetta" in {
          duplikaattiaEiSallittu(aineopintojenOpiskeluoikeus, aineopintojenOpiskeluoikeus)
        }
        "lähdejärjestelmän id:llä" in {
          duplikaattiaEiSallittuLähdejärjestelmäIdllä(aineopintojenOpiskeluoikeus, aineopintojenOpiskeluoikeus)
        }
      }
    }
  }

  private def setupOppijaWithAndGetOpiskeluoikeus(oo: KoskeenTallennettavaOpiskeluoikeus): PerusopetuksenOpiskeluoikeus = setupOppijaWithOpiskeluoikeus(oo) {
    verifyResponseStatusOk()
    getOpiskeluoikeus(readPutOppijaResponse.opiskeluoikeudet.head.oid)
  }.asInstanceOf[PerusopetuksenOpiskeluoikeus]
}
