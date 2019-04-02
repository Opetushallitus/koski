package fi.oph.koski.raportit

import java.time.LocalDate

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.api.OpiskeluoikeusTestMethodsPerusopetus
import fi.oph.koski.henkilo.{MockOppijat, OppijaHenkilö}
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.raportointikanta.RaportointikantaTestMethods
import fi.oph.koski.schema._
import org.scalatest.{FreeSpec, Matchers}

class PerusopetuksenVuosiluokkaSpec extends FreeSpec with Matchers with RaportointikantaTestMethods with OpiskeluoikeusTestMethodsPerusopetus {

  val repository = PerusopetuksenRaportitRepository(KoskiApplicationForTests.raportointiDatabase.db)
  resetFixtures

  "Perusopetuksenvuosiluokka raportti" - {

    "Tuottaa oikeat tiedot" in {
      withLisätiedotFixture(MockOppijat.ysiluokkalainen, perusopetuksenOpiskeluoikeudenLisätiedot) {
        val result = PerusopetuksenVuosiluokka.buildRaportti(repository, MockOrganisaatiot.jyväskylänNormaalikoulu, LocalDate.of(2014, 1, 1), vuosiluokka = "8")
        val ynjevinOpiskeluoikeusOid = lastOpiskeluoikeus(MockOppijat.ysiluokkalainen.oid).oid.get
        val rivi = result.find(_.opiskeluoikeusOid == ynjevinOpiskeluoikeusOid)

        rivi should equal(
          Some(
            PerusopetusRow(
              opiskeluoikeusOid = ynjevinOpiskeluoikeusOid,
              lähdejärjestelmä = None,
              lähdejärjestelmänId = None,
              oppijaOid = MockOppijat.ysiluokkalainen.oid,
              hetu = MockOppijat.ysiluokkalainen.hetu,
              sukunimi = Some(MockOppijat.ysiluokkalainen.sukunimi),
              etunimet = Some(MockOppijat.ysiluokkalainen.etunimet),
              luokka = "8C",
              viimeisinTila = "lasna",
              aidinkieli = "9",
              pakollisenAidinkielenOppimaara = "Suomen kieli ja kirjallisuus",
              kieliA = "8",
              kieliAOppimaara = "englanti",
              kieliB = "8",
              kieliBOppimaara = "ruotsi",
              uskonto = "10",
              historia = "8",
              yhteiskuntaoppi = "10",
              matematiikka = "9",
              kemia = "7",
              fysiikka = "9",
              biologia = "9",
              maantieto = "9",
              musiikki = "7",
              kuvataide = "8",
              kotitalous = "8",
              terveystieto = "8",
              kasityo = "9",
              liikunta = "9",
              ymparistooppi = "Arvosana puuttuu",
              kayttaymisenArvio = "S",
              paikallistenOppiaineidenKoodit = "TH",
              pakollisetPaikalliset = "",
              valinnaisetPaikalliset = "Tietokoneen hyötykäyttö (TH)",
              valinnaisetValtakunnalliset = "ruotsi (B1),Kotitalous (KO),Liikunta (LI),saksa (B2)",
              valinnaisetLaajuus_SuurempiKuin_2Vuosiviikkotuntia = "saksa (B2) 4.0",
              valinnaisetLaajuus_PienempiKuin_2Vuosiviikkotuntia = "ruotsi (B1) 1.0,Kotitalous (KO) 1.0,Liikunta (LI) 0.5",
              numeroarviolliset_valinnaisetLaajuus_PienempiKuin_2Vuosiviikkotuntia = "",
              majoitusetu = true,
              kuljetusetu = false,
              kotiopetus = false,
              ulkomailla = false,
              perusopetuksenAloittamistaLykatty = true,
              aloittanutEnnenOppivelvollisuutta = false,
              pidennettyOppivelvollisuus = true,
              erityisenTuenPaatos = false,
              tehostetunTuenPaatos = true,
              joustavaPerusopetus = true,
              vuosiluokkiinSitoutumatonOpetus = true,
              vammainen = true,
              vaikeastiVammainen = true,
              oikeusMaksuttomaanAsuntolapaikkaan = true,
              sisaoppilaitosmainenMaijoitus = true,
              koulukoti = true,
              tukimuodot = "Osa-aikainen erityisopetus"
            )
          )
        )
      }
    }
  }

  private def withLisätiedotFixture[T <: PerusopetuksenOpiskeluoikeus](oppija: OppijaHenkilö, lisätiedot: PerusopetuksenOpiskeluoikeudenLisätiedot)(f: => Any) = {
    val oo = lastOpiskeluoikeus(oppija.oid).asInstanceOf[T].copy(lisätiedot = Some(lisätiedot))
    putOppija(Oppija(oppija, List(oo))) {
      loadRaportointikantaFixtures
      f
    }
  }

  private val mennytAikajakso = Aikajakso(LocalDate.of(2000, 1, 1), Some(LocalDate.of(2001, 1, 1)))
  private val voimassaolevaAikajakso = Aikajakso(LocalDate.of(2008, 1, 1), None)
  private val aikajakso = voimassaolevaAikajakso.copy(loppu = Some(LocalDate.of(2018, 1, 1)))
  private val aikajaksot = Some(List(aikajakso))
  private val tukimuodot = Some(List(Koodistokoodiviite("1", "perusopetuksentukimuoto")))

  private val perusopetuksenOpiskeluoikeudenLisätiedot = PerusopetuksenOpiskeluoikeudenLisätiedot(
    perusopetuksenAloittamistaLykätty = true,
    pidennettyOppivelvollisuus = Some(voimassaolevaAikajakso),
    tukimuodot = tukimuodot,
    erityisenTuenPäätös = None,
    erityisenTuenPäätökset = None,
    tehostetunTuenPäätös = Some(voimassaolevaAikajakso),
    tehostetunTuenPäätökset = aikajaksot,
    joustavaPerusopetus = Some(voimassaolevaAikajakso),
    vuosiluokkiinSitoutumatonOpetus = true,
    vammainen = aikajaksot,
    vaikeastiVammainen = aikajaksot,
    majoitusetu = Some(voimassaolevaAikajakso),
    kuljetusetu = Some(mennytAikajakso),
    oikeusMaksuttomaanAsuntolapaikkaan = Some(aikajakso),
    sisäoppilaitosmainenMajoitus = aikajaksot,
    koulukoti = aikajaksot
  )
}
