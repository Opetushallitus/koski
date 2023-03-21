package fi.oph.koski.api

import fi.oph.koski.KoskiHttpSpec
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.documentation.{AmmatillinenExampleData, LukioExampleData}
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, OppijaHenkilö}
import fi.oph.koski.http.KoskiErrorCategory
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.koski.schema._
import fi.oph.koski.suoritusjako.{Suoritusjako, SuoritusjakoDeleteRequest, SuoritusjakoRequest, SuoritusjakoUpdateRequest}
import fi.oph.scalaschema.{ExtractionContext, SchemaValidatingExtractor}
import org.json4s.jackson.JsonMethods
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDate

class SuoritusjakoV2Spec extends AnyFreeSpec with Matchers with OpiskeluoikeusTestMethodsAmmatillinen with KoskiHttpSpec {
  "Voi jakaa koko opiskeluoikeuden" in {
    val oppija = KoskiSpecificMockOppijat.lukiolainen
    val oppimääränOpiskeluoikeus = getOpiskeluoikeudet(oppija.oid).collectFirst {
      case o: LukionOpiskeluoikeus if o.suoritukset.exists { case _: LukionOppimääränSuoritus2015 => true } => o
    }
    oppimääränOpiskeluoikeus shouldBe (defined)
    postSuoritusjakoV2(List(oppimääränOpiskeluoikeus.get), oppija) {
      verifyResponseStatusOk()
    }
  }

  "Voi jakaa koko opiskeluoikeuden, josta on piilotettu kenttiä" in {
    val oppija = KoskiSpecificMockOppijat.lukiolainen
    val oppimääränOpiskeluoikeus = getOpiskeluoikeudet(oppija.oid).collectFirst {
      case o: LukionOpiskeluoikeus if o.suoritukset.exists { case _: LukionOppimääränSuoritus2015 => true } => o
    }
    oppimääränOpiskeluoikeus shouldBe (defined)
    postSuoritusjakoV2(List(oppimääränOpiskeluoikeus.get.copy(lisätiedot = None)), oppija) {
      verifyResponseStatusOk()
    }
  }

  "Voi jakaa opiskeluoikeuden, jossa ei ole kaikkia päätason suorituksia" in {
    val oppija = KoskiSpecificMockOppijat.koululainen
    val opiskeluoikeus = getOpiskeluoikeudet(oppija.oid).collectFirst {
      case o: PerusopetuksenOpiskeluoikeus => o.copy(suoritukset = o.suoritukset.filter(_.isInstanceOf[NuortenPerusopetuksenOppimääränSuoritus]))
    }
    opiskeluoikeus shouldBe (defined)
    postSuoritusjakoV2(List(opiskeluoikeus.get), oppija) {
      verifyResponseStatusOk()
    }
  }

  "Voi jakaa opiskeluoikeuden, jossa päätason suorituksella ei ole kaikkia osasuorituksia" in {
    val oppija = KoskiSpecificMockOppijat.koululainen
    val opiskeluoikeus = getOpiskeluoikeudet(oppija.oid).collectFirst {
      case o: PerusopetuksenOpiskeluoikeus => o.copy(suoritukset = o.suoritukset.map {
        case oppimääränSuoritus: NuortenPerusopetuksenOppimääränSuoritus =>
          oppimääränSuoritus.copy(osasuoritukset = oppimääränSuoritus.osasuoritukset.map(_.filter(_.koulutusmoduuli.tunniste.koodiarvo == "BI")))
        case s => s
      })
    }
    opiskeluoikeus shouldBe (defined)
    postSuoritusjakoV2(List(opiskeluoikeus.get), oppija) {
      verifyResponseStatusOk()
    }
  }

  "Ei voi jakaa opiskeluoikeutta jossa muokattu tietoja" in {
    val oppija = KoskiSpecificMockOppijat.koululainen
    val opiskeluoikeus = getOpiskeluoikeudet(oppija.oid).collectFirst {
      case o: PerusopetuksenOpiskeluoikeus => o.copy(tila =
        NuortenPerusopetuksenOpiskeluoikeudenTila(
          List(NuortenPerusopetuksenOpiskeluoikeusjakso(LocalDate.of(2020, 1, 1), opiskeluoikeusLäsnä))
        ))
    }
    postSuoritusjakoV2(List(opiskeluoikeus.get), oppija) {
      verifyResponseStatus(400, KoskiErrorCategory.badRequest())
    }
  }

  "Ei voi jakaa opiskeluoikeutta johon on lisätty suorituksia" in {
    import fi.oph.koski.documentation.LukioExampleData._
    val ylimääräinenSuoritus = suoritus(LukioExampleData.lukionOppiaine("FOO", None)).copy(arviointi = arviointi("8")).copy(osasuoritukset = Some(List(
      kurssisuoritus(valtakunnallinenKurssi("BAR")).copy(arviointi = numeerinenArviointi(8), tunnustettu = Some(AmmatillinenExampleData.tunnustettu))
    )))
    val oppija = KoskiSpecificMockOppijat.lukiolainen
    val opiskeluoikeus = getOpiskeluoikeudet(oppija.oid).collectFirst {
      case o: LukionOpiskeluoikeus => o.copy(suoritukset = o.suoritukset.map {
        case oppimääränSuoritus: LukionOppimääränSuoritus2015 => oppimääränSuoritus.copy(osasuoritukset = oppimääränSuoritus.osasuoritukset.map(_ ++ List(ylimääräinenSuoritus)))
        case s => s
      })
    }
    opiskeluoikeus shouldBe (defined)
    postSuoritusjakoV2(List(opiskeluoikeus.get), oppija) {
      verifyResponseStatus(400, KoskiErrorCategory.badRequest())
    }
  }

  "Uuden suoritusjaon lisääminen luo auditlogin" in {
    AuditLogTester.clearMessages
    val oppija = KoskiSpecificMockOppijat.lukiolainen
    postSuoritusjakoV2(getOpiskeluoikeudet(oppija.oid).toList, oppija) {
      verifyResponseStatusOk()
      AuditLogTester.verifyAuditLogMessage(Map(
        "operation" -> "KANSALAINEN_SUORITUSJAKO_LISAYS",
        "user" -> Map("oid" -> oppija.oid),
        "target" -> Map("oppijaHenkiloOid" -> oppija.oid)
      ))
    }
  }

  "Voimassa olevat suoritusjaot voi listata" in {
    resetFixtures
    val oppija = KoskiSpecificMockOppijat.lukiolainen

    createSuoritusjako(oppija)
    createSuoritusjako(oppija)
    getSuoritusjaot(oppija).length should be(2)
  }

  "Voimassa olevan suoritusjaon voimassaolo aikaa voi päivittää" in {
    resetFixtures
    val oppija = KoskiSpecificMockOppijat.lukiolainen
    createSuoritusjako(oppija)
    val suoritusjako = getSuoritusjaot(oppija).head
    val voimassaAsti = LocalDate.now.plusDays(10)
    updateSuoritusjako(voimassaAsti, suoritusjako.secret, oppija)
    val päivitetty = getSuoritusjaot(oppija).head

    päivitetty.expirationDate should equal(voimassaAsti)
  }

  "Suoritusjaon voi poistaa" in {
    resetFixtures
    val oppija = KoskiSpecificMockOppijat.lukiolainen
    createSuoritusjako(oppija)
    val suoritusjako = getSuoritusjaot(oppija).head
    deleteSuoritusjako(suoritusjako.secret, oppija)

    getSuoritusjaot(oppija) should equal(Nil)
  }

  "Jaetun suorituksen tarkastelu ei vaadi kirjautumista" in {
    resetFixtures
    createSuoritusjako(KoskiSpecificMockOppijat.lukiolainen)
    val secret = getSuoritusjaot(KoskiSpecificMockOppijat.lukiolainen).head.secret

    post("api/test/suoritusjakoV2/editor", body = JsonMethods.pretty(JsonSerializer.serializeWithRoot(SuoritusjakoRequest(secret))), headers = jsonContent) {
      verifyResponseStatusOk()
    }
  }

  "Suoritusjakoja ei voi luoda yli maksimimäärän" in {
    resetFixtures
    (1 to 20).foreach(_ => createSuoritusjako(KoskiSpecificMockOppijat.koululainen))

    postSuoritusjakoV2(getOpiskeluoikeudet(KoskiSpecificMockOppijat.koululainen.oid).toList, KoskiSpecificMockOppijat.koululainen) {
      verifyResponseStatus(403, KoskiErrorCategory.forbidden.liianMontaSuoritusjakoa())
    }
  }

  def postSuoritusjakoV2[A](opiskeluoikeudet: List[Opiskeluoikeus], oppija: OppijaHenkilö)(f: => A): A = {
    val json = JsonMethods.pretty(JsonSerializer.serializeWithRoot(opiskeluoikeudet))
    post("api/test/suoritusjakoV2/create",
      body = json,
      headers = kansalainenLoginHeaders(oppija.hetu.get) ++ jsonContent
    )(f)
  }

  def getSuoritusjaot(oppija: OppijaHenkilö) = {
    get("api/test/suoritusjakoV2/available", headers = kansalainenLoginHeaders(oppija.hetu.get)) {
      verifyResponseStatusOk()
      implicit val context: ExtractionContext = strictDeserialization
      SchemaValidatingExtractor.extract[List[Suoritusjako]](body).right.get
    }
  }

  def updateSuoritusjako(voimassaAsti: LocalDate, secret: String, oppija: OppijaHenkilö) = {
    val json = JsonMethods.pretty(JsonSerializer.serializeWithRoot(SuoritusjakoUpdateRequest(secret, voimassaAsti)))
    post("api/test/suoritusjakoV2/update", body = json, headers = kansalainenLoginHeaders(oppija.hetu.get) ++ jsonContent ) {
      verifyResponseStatusOk()
    }
  }

  def createSuoritusjako(oppija: OppijaHenkilö): Unit = {
    postSuoritusjakoV2(getOpiskeluoikeudet(oppija.oid).toList, oppija) { verifyResponseStatusOk() }
  }

  def deleteSuoritusjako(secret: String, oppija: OppijaHenkilö) = {
    val json = JsonMethods.pretty(JsonSerializer.serializeWithRoot(SuoritusjakoDeleteRequest(secret)))
    post("api/test/suoritusjakoV2/delete", body = json, headers = kansalainenLoginHeaders(oppija.hetu.get) ++ jsonContent ) {
      verifyResponseStatusOk()
    }
  }
}
