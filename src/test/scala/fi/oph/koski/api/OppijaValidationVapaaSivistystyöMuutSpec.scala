package fi.oph.koski.api

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.documentation.ExampleData.{opiskeluoikeusKatsotaanEronneeksi, opiskeluoikeusLäsnä}
import fi.oph.koski.documentation.VapaaSivistystyöExample._
import fi.oph.koski.documentation.VapaaSivistystyöExampleData._
import fi.oph.koski.documentation.{ExamplesVapaaSivistystyöKotoutuskoulutus2022 => Koto2022}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.schema._
import fi.oph.koski.validation.VSTKotoutumiskoulutus2022Validation
import org.scalatest.freespec.AnyFreeSpec

import java.time.LocalDate
import java.time.LocalDate.{of => date}

class OppijaValidationVapaaSivistystyöMuutSpec extends AnyFreeSpec with PutOpiskeluoikeusTestMethods[VapaanSivistystyönOpiskeluoikeus] with KoskiHttpSpec {
  def tag = implicitly[reflect.runtime.universe.TypeTag[VapaanSivistystyönOpiskeluoikeus]]

  "KOPS" - {
    "Laajuudet" - {
      "Osaamiskokonaisuuden laajuus" - {
        "Osaamiskokonaisuuden laajuus lasketaan opintokokonaisuuksien laajuuksista" in {
          val oo = defaultOpiskeluoikeus.copy(suoritukset = List(suoritusKOPS.copy(
            osasuoritukset = Some(List(
              osaamiskokonaisuudenSuoritus("1002", List(
                opintokokonaisuudenSuoritus(
                  opintokokonaisuus("A01", "Arjen rahankäyttö", "Arjen rahankäyttö", 2.0)
                ),
                opintokokonaisuudenSuoritus(
                  opintokokonaisuus("M01", "Mielen liikkeet", "Mielen liikkeet ja niiden havaitseminen", 51),
                  vstArviointi("Hyväksytty", date(2021, 11, 2))
                )
              ))
            ))
          )))

          val opiskeluoikeus: Opiskeluoikeus = putAndGetOpiskeluoikeus(oo)
          opiskeluoikeus.suoritukset.head.osasuoritusLista.head.koulutusmoduuli.laajuusArvo(0) should equal(53.0)
        }

        "Valinnaisten suuntautumisopintojen laajuus lasketaan opintokokonaisuuksien laajuuksista" in {
          val oo = defaultOpiskeluoikeus.copy(suoritukset = List(suoritusKOPS.copy(
            osasuoritukset = Some(List(
              suuntautumisopintojenSuoritus(List(
                opintokokonaisuudenSuoritus(
                  opintokokonaisuus("ATX01", "Tietokoneen huolto", "Nykyaikaisen tietokoneen tyypilliset huoltotoimenpiteet", 3.0),
                  vstArviointi("Hyväksytty", date(2021, 11, 12))
                ),
                opintokokonaisuudenSuoritus(
                  opintokokonaisuus("VT02", "Valaisintekniikka", "Valaisinlähteet ja niiden toiminta", 10.0)
                ),
                opintokokonaisuudenSuoritus(
                  opintokokonaisuus("TAI01", "Taide työkaluna", "Taiteen käyttö työkaluna", 40.0)
                )
              ))
            ))
          )))

          val opiskeluoikeus: Opiskeluoikeus = putAndGetOpiskeluoikeus(oo)
          opiskeluoikeus.suoritukset.head.osasuoritusLista.head.koulutusmoduuli.laajuusArvo(0) should equal(53.0)
        }

        "Jos osaamiskokonaisuudella ei ole opintokokonaisuuksia, sille asetettu laajuus poistetaan" in {
          val oo = defaultOpiskeluoikeus.copy(
            suoritukset = List(suoritusKOPS.copy(
              vahvistus = None,
              osasuoritukset = Some(List(tyhjäOsaamiskokonaisuudenSuoritus("1003", Some(laajuus(5.0)))))
            )))

          val opiskeluoikeus: Opiskeluoikeus = putAndGetOpiskeluoikeus(oo)
          opiskeluoikeus.suoritukset.head.osasuoritusLista.head.koulutusmoduuli.getLaajuus should equal(None)
        }

        "Jos valinnaisilla suuntautumisopinnoilla ei ole opintokokonaisuuksia, sille asetettu laajuus poistetaan" in {
          val oo = defaultOpiskeluoikeus.copy(suoritukset = List(suoritusKOPS.copy(
            vahvistus = None,
            osasuoritukset = Some(List(tyhjäSuuntautumisopintojenSuoritus(Some(laajuus(5.0)))))
          )))

          val opiskeluoikeus: Opiskeluoikeus = putAndGetOpiskeluoikeus(oo)
          opiskeluoikeus.suoritukset.head.osasuoritusLista.head.koulutusmoduuli.getLaajuus should equal(None)
        }

        "Jos päätason suorituksella on osaamiskokonaisuuksia, joiden laajuus on alle 4, päätason suoritusta ei voida merkitä valmiiksi" in {
          val oo = defaultOpiskeluoikeus.copy(suoritukset = List(suoritusKOPS.copy(
            osasuoritukset = Some(List(
              osaamiskokonaisuudenSuoritus("1002", List(
                opintokokonaisuudenSuoritus(
                  opintokokonaisuus("M01", "Mielen liikkeet", "Mielen liikkeet ja niiden havaitseminen", 51),
                  vstArviointi("Hyväksytty", date(2021, 11, 2))
                )
              )),
              osaamiskokonaisuudenSuoritus("1003", List(
                opintokokonaisuudenSuoritus(
                  opintokokonaisuus("A01", "Arjen rahankäyttö", "Arjen rahankäyttö", 2.0),
                  vstArviointi("Hyväksytty", date(2021, 11, 2))
                )
              ))
            ))
          )))

          putOpiskeluoikeus(oo) {
            verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.vapaanSivistystyönVahvistetunPäätasonSuorituksenLaajuus("Päätason suoritus koulutus/999909 on vahvistettu, mutta sillä on hyväksytyksi arvioituja osaamiskokonaisuuksia, joiden laajuus on alle 4 opintopistettä"))
          }
        }
      }
    }
    "Katsotaan eronneeksi tilaan päättyneellä opiskeluoikeudella ei saa olla arvioimattomia osasuorituksia" in {
      val oo = defaultOpiskeluoikeus.copy(
        tila = VapaanSivistystyönOpiskeluoikeudenTila(List(
          VapaanSivistystyönOpiskeluoikeusjakso(date(2021, 9, 1), opiskeluoikeusLäsnä),
          VapaanSivistystyönOpiskeluoikeusjakso(date(2022, 6, 1), opiskeluoikeusKatsotaanEronneeksi)
        )),
        suoritukset = List(suoritusKOPS.copy(
        osasuoritukset = Some(List(
          osaamiskokonaisuudenSuoritus("1002", List(
            opintokokonaisuudenSuoritus(
              opintokokonaisuus("A01", "Arjen rahankäyttö", "Arjen rahankäyttö", 2.0)
            ),
            opintokokonaisuudenSuoritus(
              opintokokonaisuus("M01", "Mielen liikkeet", "Mielen liikkeet ja niiden havaitseminen", 51)
            ).copy(arviointi = None)
          ))
        ))
      )))

      putOpiskeluoikeus(oo) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.eronneeksiKatsotunOpiskeluoikeudenArvioinnit("Katsotaan eronneeksi -tilaan päättyvällä opiskeluoikeudella ei saa olla osasuorituksia, joista puuttuu arviointi"))
      }
    }
  }

  "KOTO" - {
    "Päätason suorituksen laajuus lasketaan automaattisesti osasuoritusten laajuuksista" in {
      val opiskeluoikeus = opiskeluoikeusKOTO.withSuoritukset(List(
        suoritusKOTO.copy(
          vahvistus = None,
          osasuoritukset = Some(List(
            vapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus(laajuus = LaajuusOpintopisteissä(60)),
            vapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus(laajuus = LaajuusOpintopisteissä(9))
          ))
      )))
      val result = putAndGetOpiskeluoikeus(opiskeluoikeus)
      result.suoritukset.head.koulutusmoduuli.laajuusArvo(0) shouldBe(69)
    }
  }

  "KOTO 2022" - {
    "Päätason suorituksen ja osasuorituksen laajuudeet lasketaan automaattisesti (ala)osasuoritusten laajuuksista" in {
      val opiskeluoikeus = Koto2022.Opiskeluoikeus.suoritettu
      val result = putAndGetOpiskeluoikeus(opiskeluoikeus)

      val suoritus = result.suoritukset.head
      val kieliopinnot = suoritus.osasuoritukset.flatMap(_.find(_.koulutusmoduuli.tunniste.koodiarvo == "kielijaviestintaosaaminen")).get
      val yhteiskuntaopinnot = suoritus.osasuoritukset.flatMap(_.find(_.koulutusmoduuli.tunniste.koodiarvo == "yhteiskuntajatyoelamaosaaminen")).get
      val ohjaus = suoritus.osasuoritukset.flatMap(_.find(_.koulutusmoduuli.tunniste.koodiarvo == "ohjaus")).get
      val valinnaiset = suoritus.osasuoritukset.flatMap(_.find(_.koulutusmoduuli.tunniste.koodiarvo == "valinnaisetopinnot")).get

      kieliopinnot.koulutusmoduuli.laajuusArvo(0) shouldBe(40)
      yhteiskuntaopinnot.koulutusmoduuli.laajuusArvo(0) shouldBe(20)
      ohjaus.koulutusmoduuli.laajuusArvo(0) shouldBe(7)
      valinnaiset.koulutusmoduuli.laajuusArvo(0) shouldBe(8)
      suoritus.koulutusmoduuli.laajuusArvo(0) shouldBe(75)
    }

    "Automaattinen opintopisteiden laskenta ylikirjoittaa ylemmän osasuoritustason virheellisen opintopistemäärän" in {
      val opiskeluoikeus = Koto2022.Opiskeluoikeus.suoritettu.copy(
        suoritukset = List(Koto2022.PäätasonSuoritus.suoritettu.copy(
          koulutusmoduuli = Koto2022.PäätasonSuoritus.suoritettu.koulutusmoduuli.copy(
            laajuus = Some(LaajuusOpintopisteissä(666))
          )
        ))
      )

      val result = putAndGetOpiskeluoikeus(opiskeluoikeus)
      result.suoritukset.head.koulutusmoduuli.laajuusArvo(0) shouldBe(75)
    }

    "Kieli- ja viestintäosaaminen" - {
      "Osasuoritus hyväksyttävissä, jos sen kaikki pakolliset alaosasuoritukset on tasoarvioitu" in {
        val kieliopinnot = Koto2022.KieliJaViestintä.suoritettu
        VSTKotoutumiskoulutus2022Validation.validateKieliJaViestintäOsasuoritustenArviointi(kieliopinnot) shouldBe
          HttpStatus.ok
      }

      "Osasuoritus, jota ei vielä ole hyväksytty, saa sisältää vain osan kieliopinnoista" in {
        val kieliopinnot = Koto2022.KieliJaViestintä.keskeneräinen
        VSTKotoutumiskoulutus2022Validation.validateKieliJaViestintäOsasuoritustenArviointi(kieliopinnot) shouldBe
          HttpStatus.ok
      }

      "Osasuoritusta ei voi hyväksyä, jos joku sen pakollisista alaosasuorituksista puuttuu" in {
        val kieliopinnot = Koto2022.KieliJaViestintä.keskeneräinen.copy(
          arviointi = Koto2022.suoritusArviointi(LocalDate.of(2023, 5, 1))
        )
        VSTKotoutumiskoulutus2022Validation.validateKieliJaViestintäOsasuoritustenArviointi(kieliopinnot) shouldBe
          KoskiErrorCategory.badRequest.validation.tila.osasuoritusPuuttuu("Kielten ja viestinnän osasuoritusta ei voi hyväksyä ennen kuin kaikki pakolliset alaosasuoritukset on arvioitu")
      }

      "Opiskeluoikeutta ei voi hyväksyä, jos joku sen kieliopintojen pakollisista alaosasuorituksista on vielä arvioimatta" in {
        val kieliopinnotPuuttuvallaArvioinnilla = Koto2022.KieliJaViestintä.suoritettu.copy(
          osasuoritukset = Some(List(
            Koto2022.KieliJaViestintä.Osasuoritukset.kuullunYmmärtäminen,
            Koto2022.KieliJaViestintä.Osasuoritukset.luetunYmmärtäminen,
            Koto2022.KieliJaViestintä.Osasuoritukset.puhuminen,
            Koto2022.KieliJaViestintä.Osasuoritukset.kirjoittaminen.copy(arviointi = None),
          ))
        )

        val opiskeluoikeus = Koto2022.Opiskeluoikeus.suoritettu.copy(
          suoritukset = List(
            Koto2022.PäätasonSuoritus.suoritettu.copy(
              osasuoritukset = Some(List(
                kieliopinnotPuuttuvallaArvioinnilla,
                Koto2022.YhteiskuntaJaTyöelämä.suoritettu,
                Koto2022.Ohjaus.suoritettu,
                Koto2022.Valinnaiset.suoritettu,
              )),
            )
          )
        )

        putOpiskeluoikeus(opiskeluoikeus) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.osasuoritusPuuttuu("Kielten ja viestinnän osasuoritusta ei voi hyväksyä ennen kuin kaikki pakolliset alaosasuoritukset on arvioitu"))
        }
      }
    }

    "Yhteiskunta- ja työelämäosaaminen" - {
      "Osasuoritus hyväksyttävissä, jos työssäoppimisjaksoja on vähintään 8 opintopisteen verran" in {
        val yhteiskuntaJaTyöosaaminen = Koto2022.YhteiskuntaJaTyöelämä.suoritettu
        VSTKotoutumiskoulutus2022Validation.validateTyössäoppimisjaksot(yhteiskuntaJaTyöosaaminen) shouldBe
          HttpStatus.ok
      }

      "Osasuoritusta ei voi hyväksyä, jos työssäoppimisjaksoja on alle 8 opintopisteen verran" in {
        val yhteiskuntaJaTyöosaaminen = Koto2022.YhteiskuntaJaTyöelämä.keskeneräinen.copy(
          arviointi = Koto2022.suoritusArviointi(LocalDate.of(2023, 5, 1))
        )
        VSTKotoutumiskoulutus2022Validation.validateTyössäoppimisjaksot(yhteiskuntaJaTyöosaaminen) shouldBe
          KoskiErrorCategory.badRequest.validation.laajuudet.oppiaineenLaajuusLiianSuppea(s"Oppiaineen 'Työssäoppiminen' suoritettu laajuus liian suppea (4.0 op, pitäisi olla vähintään 8.0 op)")
      }

      "Opiskeluoikeutta ei voi hyväksyä, jos työoppimisjaksoja on liian vähän" in {
        val yhteiskuntaJaTyöelämäopinnotPuuttuvallaTyöoppimisjaksolla = Koto2022.YhteiskuntaJaTyöelämä.suoritettu.copy(
          osasuoritukset = Some(List(
            Koto2022.YhteiskuntaJaTyöelämä.Alaosasuoritukset.yhteiskunnanPerusrakenteet,
            Koto2022.YhteiskuntaJaTyöelämä.Alaosasuoritukset.yhteyskunnanPeruspalvelut,
            Koto2022.YhteiskuntaJaTyöelämä.Alaosasuoritukset.ammattiJaKoulutustietous,
            Koto2022.YhteiskuntaJaTyöelämä.Alaosasuoritukset.työelämätietous,
            Koto2022.YhteiskuntaJaTyöelämä.Alaosasuoritukset.työelämätietous,
            Koto2022.YhteiskuntaJaTyöelämä.Alaosasuoritukset.työssäoppimisjakso4op,
          )),
        )

        val opiskeluoikeus = Koto2022.Opiskeluoikeus.suoritettu.copy(
          suoritukset = List(
            Koto2022.PäätasonSuoritus.suoritettu.copy(
              osasuoritukset = Some(List(
                Koto2022.KieliJaViestintä.suoritettu,
                yhteiskuntaJaTyöelämäopinnotPuuttuvallaTyöoppimisjaksolla,
                Koto2022.Ohjaus.suoritettu,
                Koto2022.Valinnaiset.suoritettu,
              )),
            )
          )
        )

        putOpiskeluoikeus(opiskeluoikeus) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.laajuudet.oppiaineenLaajuusLiianSuppea(s"Oppiaineen 'Työssäoppiminen' suoritettu laajuus liian suppea (4.0 op, pitäisi olla vähintään 8.0 op)"))
        }
      }
    }

    "Rajapäivät" - {
      "Uuden opsin mukaista opiskeluoikeutta, joka alkaa ennen 1.8.2022, ei voi siirtää" in {
        val opiskeluoikeus = Koto2022.Opiskeluoikeus.keskeneräinen.copy(
          tila = VapaanSivistystyönOpiskeluoikeudenTila(List(
            VapaanSivistystyönOpiskeluoikeusjakso(LocalDate.of(2022, 7, 31), opiskeluoikeusLäsnä)
          )),
        )

        putOpiskeluoikeus(opiskeluoikeus) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.vapaaSivistystyö.kotoAlkamispäivä2022())
        }
      }

      "Vanhan opsin mukaista opiskeluoikeutta, joka alkaa 1.8.2022 tai myöhemmin, ei voi siirtää" in {
        val opiskeluoikeus = opiskeluoikeusKOTO.copy(
          tila = VapaanSivistystyönOpiskeluoikeudenTila(List(
            VapaanSivistystyönOpiskeluoikeusjakso(LocalDate.of(2022, 8, 1), opiskeluoikeusLäsnä)
          )),
          arvioituPäättymispäivä = Some(LocalDate.of(2022, 9, 1)),
        )

        putOpiskeluoikeus(opiskeluoikeus) {
          verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.vapaaSivistystyö.kotoAlkamispäivä2012())
        }
      }
    }
  }

  "Lukutaitokoulutus" - {
    "Päätason suorituksen laajuus lasketaan automaattisesti osasuoritusten laajuuksista" in {
      val opiskeluoikeus = opiskeluoikeusLukutaito.withSuoritukset(List(
        suoritusLukutaito.withOsasuoritukset(Some(List(
          vapaanSivistystyönLukutaitokoulutuksenVuorovaikutustilanteissaToimimisenSuoritus(laajuus = LaajuusOpintopisteissä(60)),
          vapaanSivistystyönLukutaitokoulutuksenTekstienLukeminenJaTulkitseminenSuoritus(laajuus = LaajuusOpintopisteissä(9))
        )))
      ))
      val result = putAndGetOpiskeluoikeus(opiskeluoikeus)
      result.suoritukset.head.koulutusmoduuli.laajuusArvo(0) shouldBe(69)
    }

    "Opiskeluoikeus ei voi käyttää tilaa 'hyvaksytystisuoritettu'" in {
      val opiskeluoikeus = opiskeluoikeusLukutaito.withSuoritukset(List(
        suoritusLukutaito.withOsasuoritukset(Some(List(
          vapaanSivistystyönLukutaitokoulutuksenVuorovaikutustilanteissaToimimisenSuoritus(laajuus = LaajuusOpintopisteissä(60)),
          vapaanSivistystyönLukutaitokoulutuksenTekstienLukeminenJaTulkitseminenSuoritus(laajuus = LaajuusOpintopisteissä(9))
        )))
      )).withTila(
        VapaanSivistystyönOpiskeluoikeudenTila(
          List(
            VapaanSivistystyönOpiskeluoikeusjakso(date(2022, 5, 31), opiskeluoikeusHyväksytystiSuoritettu)
          )
        )
      )

      putOpiskeluoikeus(opiskeluoikeus) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.vapaanSivistystyönOpiskeluoikeudellaVääräTila())
      }
    }

    "Opiskeluoikeus ei voi käyttää tilaa 'keskeytynyt'" in {
      val opiskeluoikeus = opiskeluoikeusLukutaito.withSuoritukset(List(
        suoritusLukutaito.withOsasuoritukset(Some(List(
          vapaanSivistystyönLukutaitokoulutuksenVuorovaikutustilanteissaToimimisenSuoritus(laajuus = LaajuusOpintopisteissä(60)),
          vapaanSivistystyönLukutaitokoulutuksenTekstienLukeminenJaTulkitseminenSuoritus(laajuus = LaajuusOpintopisteissä(9))
        )))
      )).withTila(
        VapaanSivistystyönOpiskeluoikeudenTila(
          List(
            VapaanSivistystyönOpiskeluoikeusjakso(date(2022, 5, 31), opiskeluoikeusKeskeytynyt)
          )
        )
      )

      putOpiskeluoikeus(opiskeluoikeus) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.vapaanSivistystyönOpiskeluoikeudellaVääräTila())
      }
    }
  }

  private def putAndGetOpiskeluoikeus(oo: KoskeenTallennettavaOpiskeluoikeus): Opiskeluoikeus = putOpiskeluoikeus(oo) {
    verifyResponseStatusOk()
    getOpiskeluoikeus(readPutOppijaResponse.opiskeluoikeudet.head.oid)
  }

  override def defaultOpiskeluoikeus: VapaanSivistystyönOpiskeluoikeus = opiskeluoikeusKOPS
  def KOTOOPiskeluoikeus: VapaanSivistystyönOpiskeluoikeus = opiskeluoikeusKOTO
  def VapaatavoitteinenOpiskeluoikeus: VapaanSivistystyönOpiskeluoikeus = opiskeluoikeusVapaatavoitteinen
}
