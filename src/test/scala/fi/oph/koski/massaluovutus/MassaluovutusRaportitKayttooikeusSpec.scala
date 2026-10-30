package fi.oph.koski.massaluovutus

import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.koskiuser.{KoskiMockUser, MockUsers, UserWithPassword}
import fi.oph.koski.massaluovutus.paallekkaisetopiskeluoikeudet.MassaluovutusQueryPaallekkaisetOpiskeluoikeudet
import fi.oph.koski.massaluovutus.raportit._
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.KoskiSchema
import fi.oph.koski.util.Wait
import fi.oph.scalaschema.{AnyOfSchema, Schema, SchemaWithClassName}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.nio.charset.StandardCharsets
import java.time.LocalDate

// Raporteille tulee luottamuksellisia tietoja suoraan raportointikannasta, joten kyselyt vaativat samat oikeudet kuin
// vanha raporttirajapinta (RaportitServlet, hasRaportitAccess): luottamuksellisten tietojen oikeuden, eikä käyttäjällä
// saa olla viranomaisen globaalia lukuoikeutta.
class MassaluovutusRaportitKayttooikeusSpec extends AnyFreeSpec with MassaluovutusTestMethods with Matchers with BeforeAndAfterEach {
  override def body: String = new String(response.bodyBytes, StandardCharsets.UTF_8)

  override protected def afterEach(): Unit = {
    super.afterEach()
    Wait.until { !app.massaluovutusService.hasWork }
    app.massaluovutusService.truncate()
  }

  // Sama organisaatio kaikissa kyselyissä, jotta esto johtuu vain käyttäjän oikeuksista
  private val organisaatioOid = Some(MockOrganisaatiot.jyväskylänNormaalikoulu)
  private val alku = LocalDate.of(2012, 1, 1)
  private val loppu = LocalDate.of(2016, 1, 1)
  private val paiva = LocalDate.of(2012, 1, 1)

  private val esiopetus = MassaluovutusQueryEsiopetus(paiva = paiva, organisaatioOid = organisaatioOid)
  private val päällekkäiset = MassaluovutusQueryPaallekkaisetOpiskeluoikeudet(format = QueryFormat.xlsx, alku = alku, loppu = loppu, organisaatioOid = organisaatioOid)

  private val raporttikyselyt = raporttikyselytOrganisaatiolle(organisaatioOid)
  private lazy val ostopalvelunRaporttikyselyt = raporttikyselytOrganisaatiolle(Some(app.organisaatioService.ostopalveluRootOid))

  private def raporttikyselytOrganisaatiolle(organisaatioOid: Option[String]): List[(String, MassaluovutusQueryParameters)] = List(
    "Lukion suoritustiedot" -> MassaluovutusQueryLukionSuoritustiedot(alku = alku, loppu = loppu, organisaatioOid = organisaatioOid),
    "Lukio 2019 suoritustiedot" -> MassaluovutusQueryLukio2019Suoritustiedot(alku = alku, loppu = loppu, organisaatioOid = organisaatioOid),
    "Lukion kurssikertymät" -> MassaluovutusQueryLukioKurssikertymat(alku = alku, loppu = loppu, organisaatioOid = organisaatioOid),
    "Lukio 2019 opintopistekertymät" -> MassaluovutusQueryLukio2019Opintopistekertymat(alku = alku, loppu = loppu, organisaatioOid = organisaatioOid),
    "Lukio/DIA/IB/International/ESH opiskelijamäärät" -> MassaluovutusQueryLukioDiaIbInternationalESHOpiskelijamaarat(paiva = paiva, organisaatioOid = organisaatioOid),
    "Lukioon valmistavan koulutuksen opiskelijamäärät" -> MassaluovutusQueryLuvaOpiskelijamaarat(paiva = paiva, organisaatioOid = organisaatioOid),
    "IB-tutkinnon suoritustiedot" -> MassaluovutusQueryIBSuoritustiedot(alku = alku, loppu = loppu, raportinTyyppi = "ibtutkinto", organisaatioOid = organisaatioOid),
    "Ammatillinen opiskelijavuositiedot" -> MassaluovutusQueryAmmatillinenOpiskelijavuositiedot(alku = alku, loppu = loppu, organisaatioOid = organisaatioOid),
    "Ammatillinen tutkinto suoritustietojen tarkistus" -> MassaluovutusQueryAmmatillinenTutkintoSuoritustiedot(alku = alku, loppu = loppu, organisaatioOid = organisaatioOid),
    "Ammatillinen osittainen tutkinto suoritustietojen tarkistus" -> MassaluovutusQueryAmmatillinenOsittainenSuoritustiedot(alku = alku, loppu = loppu, organisaatioOid = organisaatioOid),
    "Muu ammatillinen koulutus" -> MassaluovutusQueryMuuAmmatillinen(alku = alku, loppu = loppu, organisaatioOid = organisaatioOid),
    "TOPKS ammatillinen koulutus" -> MassaluovutusQueryTOPKSAmmatillinen(alku = alku, loppu = loppu, organisaatioOid = organisaatioOid),
    "Perusopetuksen vuosiluokka" -> MassaluovutusQueryPerusopetuksenVuosiluokka(paiva = paiva, vuosiluokka = "9", organisaatioOid = organisaatioOid),
    "Perusopetuksen oppijamäärät" -> MassaluovutusQueryPerusopetuksenOppijamaaratRaportti(paiva = paiva, organisaatioOid = organisaatioOid),
    "Perusopetuksen lisäopetuksen oppijamäärät" -> MassaluovutusQueryPerusopetuksenLisaopetuksenOppijamaaratRaportti(paiva = paiva, organisaatioOid = organisaatioOid),
    "Perusopetukseen valmistava opetus" -> MassaluovutusQueryPerusopetukseenValmistava(alku = alku, loppu = loppu, organisaatioOid = organisaatioOid),
    "Aikuisten perusopetuksen suoritustietojen tarkistus" -> MassaluovutusQueryAikuistenPerusopetusSuoritustiedot(alku = alku, loppu = loppu, raportinTyyppi = "päättövaihe", organisaatioOid = organisaatioOid),
    "Aikuisten perusopetuksen oppijamäärät" -> MassaluovutusQueryAikuistenPerusopetuksenOppijamaaratRaportti(paiva = paiva, organisaatioOid = organisaatioOid),
    "Aikuisten perusopetuksen kurssikertymät" -> MassaluovutusQueryAikuistenPerusopetuksenKurssikertyma(alku = alku, loppu = loppu, organisaatioOid = organisaatioOid),
    "Esiopetus" -> MassaluovutusQueryEsiopetus(paiva = paiva, organisaatioOid = organisaatioOid),
    "Esiopetuksen oppijamäärät" -> MassaluovutusQueryEsiopetuksenOppijamaaratRaportti(paiva = paiva, organisaatioOid = organisaatioOid),
    "TUVA perusopetuksen oppijamäärät" -> MassaluovutusQueryTuvaPerusopetuksenOppijamaaratRaportti(paiva = paiva, organisaatioOid = organisaatioOid),
    "TUVA suoritustiedot" -> MassaluovutusQueryTuvaSuoritustiedot(alku = alku, loppu = loppu, organisaatioOid = organisaatioOid),
    "Vapaan sivistystyön JOTPA" -> MassaluovutusQueryVSTJOTPA(alku = alku, loppu = loppu, organisaatioOid = organisaatioOid),
    "Vapaan sivistystyön vapaatavoitteinen koulutus" -> MassaluovutusQueryVSTVapaatavoitteinen(alku = alku, loppu = loppu, organisaatioOid = organisaatioOid),
    "Vapaan sivistystyön oppivelvollisille suunnattu koulutus" -> MassaluovutusQueryVSTKOPS(alku = alku, loppu = loppu, organisaatioOid = organisaatioOid),
    "Vapaan sivistystyön osaamismerkit" -> MassaluovutusQueryVSTOsaamismerkit(alku = alku, loppu = loppu, organisaatioOid = organisaatioOid),
    "Muu kuin säännelty koulutus" -> MassaluovutusQueryMuuKuinSaanneltyKoulutus(alku = alku, loppu = loppu, organisaatioOid = organisaatioOid),
  )

  // Päällekkäisten opiskeluoikeuksien raportilla ei ole luottamuksellisia tietoja, joten se ei vaadi raporttien oikeuksia
  private val päällekkäisetKyselyt: List[(String, MassaluovutusQueryParameters)] = List(
    "Päällekkäiset opiskeluoikeudet (xlsx)" -> päällekkäiset,
    "Päällekkäiset opiskeluoikeudet (csv)" -> päällekkäiset.copy(format = QueryFormat.csv),
  )

  "Kaikki raporttikyselyt ovat mukana testeissä" in {
    raporttikyselyt.map(_._2.getClass.getName).toSet should equal(kaikkiRaporttikyselyluokat)
  }

  "Luottamuksellisten tietojen oikeudella kaikki raporttikyselyt sallitaan" - {
    "organisaation käyttäjälle" in {
      estetytKyselyt(raporttikyselyt, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä) shouldBe empty
    }

    "OPH:n katselijalle" in {
      estetytKyselyt(raporttikyselyt, MockUsers.ophkatselija) shouldBe empty
    }
  }

  "Ilman luottamuksellisten tietojen oikeutta kaikki raporttikyselyt estetään" - {
    "organisaation käyttäjältä" in {
      sallitutKyselyt(raporttikyselyt, MockUsers.tallentajaEiLuottamuksellinen) shouldBe empty
      raporttikyselyEstetään(esiopetus, MockUsers.tallentajaEiLuottamuksellinen)
    }

    "OPH:n katselijalta" in {
      sallitutKyselyt(raporttikyselyt, MockUsers.viranomainen) shouldBe empty
      raporttikyselyEstetään(esiopetus, MockUsers.viranomainen)
    }

    "kielitutkintorekisterin palvelukäyttäjältä" in {
      sallitutKyselyt(raporttikyselyt, MockUsers.kielitutkintorekisteriKäyttäjä) shouldBe empty
      raporttikyselyEstetään(esiopetus, MockUsers.kielitutkintorekisteriKäyttäjä)
    }
  }

  "Viranomaisen globaalilla lukuoikeudella kaikki raporttikyselyt estetään" - {
    "ilman organisaation oikeuksia" in {
      sallitutKyselyt(raporttikyselyt, MockUsers.tilastokeskusKäyttäjä) shouldBe empty
      raporttikyselyEstetään(esiopetus, MockUsers.tilastokeskusKäyttäjä)
    }

    "organisaation oikeuksista huolimatta" in {
      sallitutKyselyt(raporttikyselyt, MockUsers.viranomainenJaJyväskylänTallentaja) shouldBe empty
      raporttikyselyEstetään(esiopetus, MockUsers.viranomainenJaJyväskylänTallentaja)
    }
  }

  "Ostopalvelu/palveluseteli-valinta" - {
    "sallitaan ostavan koulutustoimijan käyttäjälle vain esiopetuksen oppijamääräraportissa" in {
      sallitutKyselyt(ostopalvelunRaporttikyselyt, MockUsers.helsinkiTallentaja) should equal(List("Esiopetuksen oppijamäärät"))
    }

    "estetään ilman ostopalveluoikeuksia" in {
      sallitutKyselyt(ostopalvelunRaporttikyselyt, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä) shouldBe empty
    }

    "ei sallita pelkällä globaalilla lukuoikeudella" in {
      sallitutKyselyt(ostopalvelunRaporttikyselyt, MockUsers.paakayttaja) should not contain ("Esiopetuksen oppijamäärät")
    }
  }

  "Päällekkäiset opiskeluoikeudet" - {
    "sallitaan organisaation käyttäjälle ilman luottamuksellisten tietojen oikeutta" in {
      estetytKyselyt(päällekkäisetKyselyt, MockUsers.tallentajaEiLuottamuksellinen) shouldBe empty
    }

    "estetään kielitutkintorekisterin palvelukäyttäjältä" in {
      sallitutKyselyt(päällekkäisetKyselyt, MockUsers.kielitutkintorekisteriKäyttäjä) shouldBe empty
      raporttikyselyEstetään(päällekkäiset, MockUsers.kielitutkintorekisteriKäyttäjä)
    }
  }

  "Kielitutkintorekisterin palvelukäyttäjä ei saa toisen käyttäjän kyselyä" - {
    "lähettämällä saman kyselyn kuin meneillään oleva kysely, ei palauteta identtistä kyselyä" in {
      withoutRunningQueryScheduler {
        addQuery(esiopetus, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä) {
          verifyResponseStatusOk()
        }
        raporttikyselyEstetään(esiopetus, MockUsers.kielitutkintorekisteriKäyttäjä)
      }
    }

    "kyselyn tunnisteella, vaikka kielitutkintorekisterin käyttäjällä on globaali lukuoikeus (kielitutkintoihin)" in {
      withoutRunningQueryScheduler {
        val queryId = addQuerySuccessfully(esiopetus, MockUsers.jyväskylänNormaalikoulunPalvelukäyttäjä)(_.queryId)
        getQuery(queryId, MockUsers.kielitutkintorekisteriKäyttäjä) {
          verifyResponseStatus(404, KoskiErrorCategory.notFound())
        }
        getQuery(queryId, MockUsers.ophkatselija) {
          verifyResponseStatusOk()
        }
      }
    }
  }

  private def sallitutKyselyt(kyselyt: List[(String, MassaluovutusQueryParameters)], user: KoskiMockUser): List[String] = {
    val session = user.toKoskiSpecificSession(app.käyttöoikeusRepository)
    kyselyt.collect { case (nimi, kysely) if kysely.queryAllowed(app)(session) => nimi }
  }

  private def estetytKyselyt(kyselyt: List[(String, MassaluovutusQueryParameters)], user: KoskiMockUser): List[String] = {
    val session = user.toKoskiSpecificSession(app.käyttöoikeusRepository)
    kyselyt.collect { case (nimi, kysely) if !kysely.queryAllowed(app)(session) => nimi }
  }

  private def raporttikyselyEstetään(query: MassaluovutusQueryParameters, user: UserWithPassword): Unit =
    addQuery(query, user) {
      verifyResponseStatus(403, KoskiErrorCategory.forbidden())
    }

  private def kaikkiRaporttikyselyluokat: Set[String] = {
    def luokat(schema: Schema): List[String] = schema match {
      case s: AnyOfSchema => s.alternatives.flatMap(luokat)
      case s: SchemaWithClassName => List(s.fullClassName)
      case _ => Nil
    }
    luokat(KoskiSchema.createSchema(classOf[KoulutuksenjärjestäjienMassaluovutusQueryParameters]))
      .filter(_.startsWith("fi.oph.koski.massaluovutus.raportit."))
      .toSet
  }
}
