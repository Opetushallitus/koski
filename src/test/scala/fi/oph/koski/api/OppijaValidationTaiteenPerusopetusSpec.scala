package fi.oph.koski.api

import com.typesafe.config.Config
import fi.oph.koski.db.OpiskeluoikeusRow
import fi.oph.koski.documentation.{ExamplesTaiteenPerusopetus => TPO}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.schema._
import fi.oph.koski.validation.KoskiValidator
import fi.oph.koski.{KoskiApplicationForTests, KoskiHttpSpec}
import org.json4s.JObject
import org.json4s.jackson.JsonMethods
import org.scalatest.freespec.AnyFreeSpec

import java.time.LocalDate

class OppijaValidationTaiteenPerusopetusSpec
  extends AnyFreeSpec
    with KoskiHttpSpec
    with TutkinnonPerusteetTest[TaiteenPerusopetuksenOpiskeluoikeus]
    with OpiskeluoikeudenMitätöintiJaPoistoTestMethods {
  override def tag = implicitly[reflect.runtime.universe.TypeTag[TaiteenPerusopetuksenOpiskeluoikeus]]

  override def defaultOpiskeluoikeus = TPO.Opiskeluoikeus.aloitettuYleinenOppimäärä

  override def opiskeluoikeusWithPerusteenDiaarinumero(diaari: Option[String]): TaiteenPerusopetuksenOpiskeluoikeus = {
    defaultOpiskeluoikeus.copy(
      suoritukset = List(
        TPO.PäätasonSuoritus.yleistenYhteistenOpintojenSuoritusEiArvioituEiOsasuorituksia.copy(
          koulutusmoduuli = TPO.PäätasonSuoritus.Koulutusmoduuli.musiikkiYleinenOppimääräEiLaajuutta.copy(
              perusteenDiaarinumero = diaari
            )
        )
      )
    )
  }

  override def eperusteistaLöytymätönValidiDiaarinumero: String = "6/011/2015"

  val oppija = KoskiSpecificMockOppijat.taiteenPerusopetusAloitettu

  val taiteenPerusopetusAloitettuOpiskeluoikeus = getOpiskeluoikeudet(
    KoskiSpecificMockOppijat.taiteenPerusopetusAloitettu.oid
  ).head match {
    case oo: TaiteenPerusopetuksenOpiskeluoikeus => oo
  }
  val taiteenPerusopetusAloitettuOpiskeluoikeusOid = taiteenPerusopetusAloitettuOpiskeluoikeus.oid.get


  "Opiskeluoikeuden lisääminen" - {
    "aloittanut example-opiskeluoikeus voidaan kirjoittaa tietokantaan" in {
      putOpiskeluoikeus(defaultOpiskeluoikeus, henkilö = oppija) {
        verifyResponseStatusOk()
      }
    }

    "valmistunut example-opiskeluoikeus voidaan kirjoittaa tietokantaan" in {
      putOpiskeluoikeus(TPO.Opiskeluoikeus.hyväksytystiSuoritettuLaajaOppimäärä, henkilö = oppija) {
        verifyResponseStatusOk()
      }
    }

    "vastaavan rinnakkaisen opiskeluoikeuden lisääminen on sallittu" in {
      resetFixtures()

      val oo1 = postAndGetOpiskeluoikeusV2(TPO.Opiskeluoikeus.aloitettuYleinenOppimäärä, henkilö = KoskiSpecificMockOppijat.tyhjä)
      val oo2 = postAndGetOpiskeluoikeusV2(TPO.Opiskeluoikeus.aloitettuYleinenOppimäärä, henkilö = KoskiSpecificMockOppijat.tyhjä)

      oo2.oid.get should not be(oo1.oid.get)

      val oot = oppijaByHetu(KoskiSpecificMockOppijat.tyhjä.hetu).tallennettavatOpiskeluoikeudet
      oot.size should be(2)
      oot.flatMap(_.oid) should contain(oo2.oid.get)
      oot.flatMap(_.oid) should contain(oo1.oid.get)
    }

    "päätason valmis suoritus yhdistetään samalle opiskeluoikeudelle uuden päätason suorituksen kanssa vaikka suoritukset siirretään erikseen" in {
      resetFixtures()

      val ooVanhaSuoritus = TPO.Opiskeluoikeus.hyväksytystiSuoritettuLaajaOppimäärä.copy(
        suoritukset = List(
          TPO.PäätasonSuoritus.laajojenPerusopintojenSuoritusArvioituJaVahvistettuJaOsasuorituksia
        )
      )

      val oid = postAndGetOpiskeluoikeusV2(ooVanhaSuoritus, henkilö = KoskiSpecificMockOppijat.tyhjä).oid

      val ooUusiSuoritus = TPO.Opiskeluoikeus.hyväksytystiSuoritettuLaajaOppimäärä.copy(
        oid = oid,
        suoritukset = List(
          TPO.PäätasonSuoritus.laajojenSyventävienOpintojenSuoritusArvioituJaVahvistettuJaOsasuorituksia
        )
      )

      putOpiskeluoikeus(ooUusiSuoritus, henkilö = KoskiSpecificMockOppijat.tyhjä){
        verifyResponseStatusOk()
      }

      val oot = getOpiskeluoikeus(oid.get).asInstanceOf[TaiteenPerusopetuksenOpiskeluoikeus]
      oot.suoritukset.size shouldBe 2
    }
  }

  "Suorituksen vahvistaminen" - {
    "suoritusta ei voi vahvistaa jos sillä on arvioimattomia osasuorituksia" in {
      val oo = TPO.Opiskeluoikeus.aloitettuYleinenOppimäärä.copy(
        suoritukset = List(
          TPO.PäätasonSuoritus.yleistenYhteistenOpintojenSuoritusEiArvioituEiOsasuorituksia.copy(
            koulutusmoduuli = TPO.PäätasonSuoritus.Koulutusmoduuli.musiikkiYleinenOppimääräYhteisetOpinnot,
            arviointi = Some(List(TPO.arviointiHyväksytty)),
            vahvistus = Some(TPO.vahvistus),
            osasuoritukset = Some(List(
              TPO.Osasuoritus.osasuoritusMusiikki("Musa 1", 11.11).copy(
                arviointi = None
              )
            ))
          )
        )
      )

      putOpiskeluoikeus(oo, henkilö = oppija) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.keskeneräinenOsasuoritus("Valmiiksi merkityllä suorituksella koulutus/999907 on keskeneräinen osasuoritus Musa 1 (Musiikin kurssi)"))
      }
    }

    "suorituksen voi vahvistaa jos sillä ei ole yhtään osasuoritusta" in {
      val oo = TPO.Opiskeluoikeus.aloitettuYleinenOppimäärä.copy(
        suoritukset = List(
          TPO.PäätasonSuoritus.yleistenYhteistenOpintojenSuoritusEiArvioituEiOsasuorituksia.copy(
            koulutusmoduuli = TPO.PäätasonSuoritus.Koulutusmoduuli.musiikkiYleinenOppimääräYhteisetOpinnot,
            arviointi = Some(List(TPO.arviointiHyväksytty)),
            vahvistus = Some(TPO.vahvistus)
          )
        )
      )

      putOpiskeluoikeus(oo, henkilö = oppija) {
        verifyResponseStatusOk()
      }
    }
  }

  "Opiskeluoikeuden päättäminen" - {
    "opiskeluoikeudelle ei voi lisätä päättävää tilaa hyväksytysti suoritettu jos on suorituksia ilman vahvistusta" in {
      val oo = TPO.Opiskeluoikeus.hyväksytystiSuoritettuLaajaOppimäärä.copy(
        suoritukset = List(
          TPO.PäätasonSuoritus.laajojenPerusopintojenSuoritusArvioituJaVahvistettuJaOsasuorituksia.copy(
            vahvistus = None
          )
        )
      )

      putOpiskeluoikeus(oo, henkilö = oppija) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.tila.vahvistusPuuttuu ("Suoritukselta puuttuu vahvistus, vaikka opiskeluoikeus on tilassa Hyväksytysti suoritettu"))
      }
    }

    "keskeneräiselle opiskeluoikeudelle voi aina lisätä päättävän tilan päättynyt (keskeytynyt) suorituksista riippumatta" in {
      val oo = TPO.Opiskeluoikeus.aloitettuYleinenOppimäärä.copy(
        tila = TaiteenPerusopetuksenOpiskeluoikeudenTila(
          opiskeluoikeusjaksot = List(
            TPO.Opiskeluoikeus.jaksoLäsnä(),
            TPO.Opiskeluoikeus.jaksoPäättynyt(TPO.alkupäivä.plusDays(1))
          )
        )
      )

      putOpiskeluoikeus(oo, henkilö = oppija) {
        verifyResponseStatusOk()
      }
    }
  }

  "Suoritusten laajuudet" - {
    "suoritusta ei voi vahvistaa ilman laajuutta" in {
      val oo = TPO.Opiskeluoikeus.aloitettuYleinenOppimäärä.copy(
        suoritukset = List(
          TPO.PäätasonSuoritus.yleistenYhteistenOpintojenSuoritusEiArvioituEiOsasuorituksia.copy(
            koulutusmoduuli = TPO.PäätasonSuoritus.Koulutusmoduuli.musiikkiYleinenOppimääräYhteisetOpinnot.copy(
              laajuus = None
            ),
            arviointi = Some(List(TPO.arviointiHyväksytty)),
            vahvistus = Some(TPO.vahvistus)
          )
        )
      )

      putOpiskeluoikeus(oo, henkilö = oppija) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.laajuudet.taiteenPerusopetuksenLaajuus ("Yleisen oppimäärän yhteisten opintojen laajuus on oltava vähintään 11.11 opintopistettä."))
      }
    }

    "vahvistetun yleisen oppimäärän yhteisten opintojen suorituksen laajuus oltava vähintään 11,11op" in {
      val oo = TPO.Opiskeluoikeus.aloitettuYleinenOppimäärä.copy(
        tila = TPO.Opiskeluoikeus.tilaHyväksytystiSuoritettu(),
        suoritukset = List(
          TPO.PäätasonSuoritus.yleistenYhteistenOpintojenSuoritusEiArvioituEiOsasuorituksia.copy(
            koulutusmoduuli = TPO.PäätasonSuoritus.Koulutusmoduuli.musiikkiYleinenOppimääräYhteisetOpinnot.copy(
              laajuus = Some(LaajuusOpintopisteissä(11.10))
            ),
            arviointi = Some(List(TPO.arviointiHyväksytty)),
            vahvistus = Some(TPO.vahvistus),
            osasuoritukset = None
          )
        )
      )

      putOpiskeluoikeus(oo, henkilö = oppija) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.laajuudet.taiteenPerusopetuksenLaajuus ("Yleisen oppimäärän yhteisten opintojen laajuus on oltava vähintään 11.11 opintopistettä."))
      }
    }

    "vahvistamattoman suorituksen laajuutta ei ole rajoitettu validaatiolla" in {
      val oo = TPO.Opiskeluoikeus.aloitettuYleinenOppimäärä.copy(
        suoritukset = List(
          TPO.PäätasonSuoritus.yleistenYhteistenOpintojenSuoritusEiArvioituEiOsasuorituksia.copy(
            koulutusmoduuli = TPO.PäätasonSuoritus.Koulutusmoduuli.musiikkiYleinenOppimääräYhteisetOpinnot.copy(
              laajuus = Some(LaajuusOpintopisteissä(1))
            ),
            arviointi = None,
            vahvistus = None,
            osasuoritukset = None
          )
        )
      )

      putOpiskeluoikeus(oo, henkilö = oppija) {
        verifyResponseStatusOk()
      }
    }

    "vahvistetun yleisen oppimäärän teemaopintojen suorituksen laajuus oltava vähintään 7,41op" in {
      val oo = TPO.Opiskeluoikeus.aloitettuYleinenOppimäärä.copy(
        tila = TPO.Opiskeluoikeus.tilaHyväksytystiSuoritettu(),
        suoritukset = List(
          TPO.PäätasonSuoritus.yleistenTeemaopintojenSuoritusEiArvioituEiOsasuorituksia.copy(
            koulutusmoduuli = TPO.PäätasonSuoritus.Koulutusmoduuli.musiikkiYleinenOppimääräTeemaopinnot.copy(
              laajuus = Some(LaajuusOpintopisteissä(7.4))
            ),
            arviointi = Some(List(TPO.arviointiHyväksytty)),
            vahvistus = Some(TPO.vahvistus),
            osasuoritukset = None
          )
        )
      )

      putOpiskeluoikeus(oo, henkilö = oppija) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.laajuudet.taiteenPerusopetuksenLaajuus ("Yleisen oppimäärän teemaopintojen laajuus on oltava vähintään 7.41 opintopistettä."))
      }
    }

    "vahvistetun laajan oppimäärän perusopintojen suorituksen laajuus oltava vähintään 29,63op" in {
      val oo = TPO.Opiskeluoikeus.hyväksytystiSuoritettuLaajaOppimäärä.copy(
        suoritukset = List(
          TPO.PäätasonSuoritus.laajojenPerusopintojenSuoritusArvioituJaVahvistettuJaOsasuorituksia.copy(
            koulutusmoduuli = TPO.PäätasonSuoritus.Koulutusmoduuli.musiikkiLaajaOppimääräPerusopinnot.copy(
              laajuus = Some(LaajuusOpintopisteissä(29.62))
            ),
            osasuoritukset = None
          ),
          TPO.PäätasonSuoritus.laajojenSyventävienOpintojenSuoritusArvioituJaVahvistettuJaOsasuorituksia
        )
      )

      putOpiskeluoikeus(oo, henkilö = oppija) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.laajuudet.taiteenPerusopetuksenLaajuus ("Laajan oppimäärän perusopintojen laajuus on oltava vähintään 29.63 opintopistettä."))
      }
    }

    "vahvistetun laajan oppimäärän syventävien opintojen suorituksen laajuus oltava vähintään 18,52op" in {
      val oo = TPO.Opiskeluoikeus.hyväksytystiSuoritettuLaajaOppimäärä.copy(
        suoritukset = List(
          TPO.PäätasonSuoritus.laajojenPerusopintojenSuoritusArvioituJaVahvistettuJaOsasuorituksia,
          TPO.PäätasonSuoritus.laajojenSyventävienOpintojenSuoritusArvioituJaVahvistettuJaOsasuorituksia.copy(
            koulutusmoduuli = TPO.PäätasonSuoritus.Koulutusmoduuli.musiikkiLaajaOppimääräSyventävätOpinnot.copy(
              laajuus = Some(LaajuusOpintopisteissä(18.51))
            ),
            osasuoritukset = None
          )
        )
      )

      putOpiskeluoikeus(oo, henkilö = oppija) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.laajuudet.taiteenPerusopetuksenLaajuus("Laajan oppimäärän syventävien opintojen laajuus on oltava vähintään 18.52 opintopistettä."))
      }
    }

    "vahvistetun suorituksen laajuus on oltava sama kuin paikallisten osasuoritusten yhteenlasketut laajuudet" in {
      val oo = TPO.Opiskeluoikeus.hyväksytystiSuoritettuLaajaOppimäärä.copy(
        suoritukset = List(
          TPO.PäätasonSuoritus.laajojenPerusopintojenSuoritusArvioituJaVahvistettuJaOsasuorituksia,
          TPO.PäätasonSuoritus.laajojenSyventävienOpintojenSuoritusArvioituJaVahvistettuJaOsasuorituksia.copy(
            koulutusmoduuli = TPO.PäätasonSuoritus.Koulutusmoduuli.musiikkiLaajaOppimääräSyventävätOpinnot.copy(
              laajuus = Some(LaajuusOpintopisteissä(20))
            ),
            osasuoritukset = Some(List(
              TPO.Osasuoritus.osasuoritusMusiikki("musa1", 19.0)
            ))
          )
        )
      )

      putOpiskeluoikeus(oo, henkilö = oppija) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.laajuudet.osasuoritustenLaajuuksienSumma ("Suorituksen koulutus/999907 osasuoritusten laajuuksien summa 19.0 ei vastaa suorituksen laajuutta 20.0"))
      }
    }
  }

  "Suoritusten tyypit ja taiteenalat" - {
    "yleisen oppimäärän opiskeluoikeudelle ei voi siirtää laajan oppimäärän opintotasoja" in {
      val oo = TPO.Opiskeluoikeus.aloitettuYleinenOppimäärä.copy(
        suoritukset = List(
          TPO.PäätasonSuoritus.laajojenPerusopintojenSuoritusArvioituJaVahvistettuJaOsasuorituksia
        )
      )

      putOpiskeluoikeus(oo, henkilö = oppija) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.tpoVääräOpintotaso ("Suorituksen opintotaso ei sisälly opiskeluoikeuden oppimäärään."))
      }
    }

    "laajan oppimäärän opiskeluoikeudelle voi siirtää vain laajan oppimäärän opintotasoja" in {
      val oo = TPO.Opiskeluoikeus.hyväksytystiSuoritettuLaajaOppimäärä.copy(
        suoritukset = List(
          TPO.PäätasonSuoritus.yleistenTeemaopintojenSuoritusEiArvioituEiOsasuorituksia.copy(
            koulutusmoduuli = TPO.PäätasonSuoritus.Koulutusmoduuli.musiikkiYleinenOppimääräTeemaopinnot.copy(
              laajuus = Some(LaajuusOpintopisteissä(7.41))
            ),
            arviointi = Some(List(TPO.arviointiHyväksytty)),
            vahvistus = Some(TPO.vahvistus),
            osasuoritukset = None
          )
        )
      )

      putOpiskeluoikeus(oo, henkilö = oppija) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.tpoVääräOpintotaso ("Suorituksen opintotaso ei sisälly opiskeluoikeuden oppimäärään."))
      }
    }

    "opiskeluoikeudelle ei voi siirtää suorituksia eri taiteenaloilta" in {
      val oo = TPO.Opiskeluoikeus.aloitettuYleinenOppimäärä.copy(
        suoritukset = List(
          TPO.PäätasonSuoritus.yleistenYhteistenOpintojenSuoritusEiArvioituEiOsasuorituksia,
          TPO.PäätasonSuoritus.yleistenTeemaopintojenSuoritusEiArvioituEiOsasuorituksia.copy(
            koulutusmoduuli = TPO.PäätasonSuoritus.Koulutusmoduuli.musiikkiYleinenOppimääräTeemaopinnot.copy(
              taiteenala = TPO.kuvataiteenTaiteenala
            )
          )
        )
      )

      putOpiskeluoikeus(oo, henkilö = oppija) {
        verifyResponseStatus(400, KoskiErrorCategory.badRequest.validation.rakenne.tpoEriTaiteenalat ("Taiteen perusopetuksen opiskeluoikeudella ei voi olla suorituksia eri taiteenaloilta."))
      }
    }
  }

  "Perusteet" - {
    "yleisen oppimäärän opiskeluoikeudelle ei voi lisätä väärää diaarinumeroa" in {
      val oo = TPO.Opiskeluoikeus.aloitettuYleinenOppimäärä.copy(
        suoritukset = List(
          TPO.PäätasonSuoritus.yleistenTeemaopintojenSuoritusEiArvioituEiOsasuorituksia.copy(
            koulutusmoduuli = TPO.PäätasonSuoritus.Koulutusmoduuli.musiikkiYleinenOppimääräTeemaopinnot.copy(
              perusteenDiaarinumero = Some(TPO.taiteenPerusopetusLaajaOppimääräDiaari)
            )
          )
        )
      )

      putOpiskeluoikeus(oo, henkilö = oppija) {
        // TODO: virheilmoitus
        verifyResponseStatus(400)
      }
    }

    "laajan oppimäärän opiskeluoikeudelle ei voi lisätä väärää diaarinumeroa" in {
      val oo = TPO.Opiskeluoikeus.hyväksytystiSuoritettuLaajaOppimäärä.copy(
        suoritukset = List(
          TPO.PäätasonSuoritus.laajojenPerusopintojenSuoritusArvioituJaVahvistettuJaOsasuorituksia.copy(
            koulutusmoduuli = TPO.PäätasonSuoritus.Koulutusmoduuli.musiikkiLaajaOppimääräPerusopinnot.copy(
              perusteenDiaarinumero = Some(TPO.taiteenPerusopetusYleinenOppimääräDiaari)
            )
          )
        )
      )

      putOpiskeluoikeus(oo, henkilö = oppija) {
        // TODO: virheilmoitus
        verifyResponseStatus(400)
      }
    }

    "opiskeluoikeutta ei voi siirtää jos se ei ole voimassa perusteen voimassaolon aikana" in {
      val oo = TPO.Opiskeluoikeus.hyväksytystiSuoritettuLaajaOppimäärä.copy(
        tila = TaiteenPerusopetuksenOpiskeluoikeudenTila(
          List(
            TPO.Opiskeluoikeus.jaksoLäsnä(LocalDate.of(2018, 1,1)),
            TPO.Opiskeluoikeus.jaksoHyväksytystiSuoritettu(LocalDate.of(2018, 7, 31))
          )
        )
      )

      putOpiskeluoikeus(oo, henkilö = oppija) {
        verifyResponseStatus(
          400,
          KoskiErrorCategory.badRequest.validation.rakenne.perusteEiVoimassa("Perusteen tulee olla voimassa opiskeluoikeuden voimassaoloaikana."),
          KoskiErrorCategory.badRequest.validation.rakenne.perusteEiVoimassa("Perusteen tulee olla voimassa opiskeluoikeuden voimassaoloaikana.")
        )
      }
    }
  }

  "Mitätöinti" - {
    "Opiskeluoikeuden voi mitätöidä PUT-rajapinnalla ja mitätöinti on poisto" in {
      resetFixtures()
      val oo = taiteenPerusopetusAloitettuOpiskeluoikeus.copy(
        lähdejärjestelmänId = Some(
          LähdejärjestelmäId(
            id = Some("tpo1"),
            lähdejärjestelmä = Koodistokoodiviite("primus", "lahdejarjestelma")
          )
        ),
        tila = TPO.Opiskeluoikeus.tilaMitätöity()
      )

      putOpiskeluoikeus(oo, henkilö = oppija) {
        verifyResponseStatusOk()
      }

      val poistettuRivi: Either[HttpStatus, OpiskeluoikeusRow] =
        KoskiApplicationForTests.opiskeluoikeusRepository.findByOid(
          taiteenPerusopetusAloitettuOpiskeluoikeusOid
        )(
          KoskiSpecificSession.systemUserMitätöidytJaPoistetut
        )

      poistettuRivi.isRight should be(true)

      poistettuRivi.map(ooRow => {
        ooRow.oid should be(taiteenPerusopetusAloitettuOpiskeluoikeusOid)
        ooRow.versionumero should be(taiteenPerusopetusAloitettuOpiskeluoikeus.versionumero.get + 1)
        ooRow.aikaleima.toString should include(LocalDate.now.toString)
        ooRow.oppijaOid should be(KoskiSpecificMockOppijat.taiteenPerusopetusAloitettu.oid)
        ooRow.oppilaitosOid should be("")
        ooRow.koulutustoimijaOid should be(None)
        ooRow.sisältäväOpiskeluoikeusOid should be(None)
        ooRow.sisältäväOpiskeluoikeusOppilaitosOid should be(None)
        ooRow.data should be(JObject(List()))
        ooRow.luokka should be(None)
        ooRow.mitätöity should be(true)
        ooRow.koulutusmuoto should be("")
        ooRow.alkamispäivä.toString should be(LocalDate.now.toString)
        ooRow.päättymispäivä should be(None)
        ooRow.suoritusjakoTehty should be(false)
        ooRow.suoritustyypit should be(Nil)
        ooRow.poistettu should be(true)
      })
    }

    "Opiskeluoikeuden voi mitätöidä DELETE-rajapinnalla ja mitätöinti on poisto" in {
      resetFixtures()
      mitätöiOpiskeluoikeus(taiteenPerusopetusAloitettuOpiskeluoikeusOid)

      val poistettuRivi: Either[HttpStatus, OpiskeluoikeusRow] =
        KoskiApplicationForTests.opiskeluoikeusRepository.findByOid(
          taiteenPerusopetusAloitettuOpiskeluoikeusOid
        )(
          KoskiSpecificSession.systemUserMitätöidytJaPoistetut
        )

      poistettuRivi.isRight should be(true)

      poistettuRivi.map(ooRow => {
        ooRow.oid should be(taiteenPerusopetusAloitettuOpiskeluoikeusOid)
        ooRow.versionumero should be(taiteenPerusopetusAloitettuOpiskeluoikeus.versionumero.get + 1)
        ooRow.aikaleima.toString should include(LocalDate.now.toString)
        ooRow.oppijaOid should be(KoskiSpecificMockOppijat.taiteenPerusopetusAloitettu.oid)
        ooRow.oppilaitosOid should be("")
        ooRow.koulutustoimijaOid should be(None)
        ooRow.sisältäväOpiskeluoikeusOid should be(None)
        ooRow.sisältäväOpiskeluoikeusOppilaitosOid should be(None)
        ooRow.data should be(JObject(List()))
        ooRow.luokka should be(None)
        ooRow.mitätöity should be(true)
        ooRow.koulutusmuoto should be("")
        ooRow.alkamispäivä.toString should be(LocalDate.now.toString)
        ooRow.päättymispäivä should be(None)
        ooRow.suoritusjakoTehty should be(false)
        ooRow.suoritustyypit should be(Nil)
        ooRow.poistettu should be(true)
      })
    }
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

  private def postAndGetOpiskeluoikeusV2(
    oo: TaiteenPerusopetuksenOpiskeluoikeus,
    henkilö: Henkilö = defaultHenkilö,
    headers: Headers = authHeaders() ++ jsonContent
  ): TaiteenPerusopetuksenOpiskeluoikeus = {
    val jsonString = JsonMethods.pretty(makeOppija(henkilö, List(oo)))
    post("api/v2/oppija", body = jsonString, headers = headers) {
      verifyResponseStatusOk()
      val response = readPutOppijaResponse
      response.opiskeluoikeudet.size shouldBe 1
      getOpiskeluoikeus(readPutOppijaResponse.opiskeluoikeudet.head.oid).asInstanceOf[TaiteenPerusopetuksenOpiskeluoikeus]
    }
  }
}
