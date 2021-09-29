package fi.oph.koski.migri

import fi.oph.koski.{DirtiesFixtures, KoskiHttpSpec, schema}
import fi.oph.koski.api.OpiskeluoikeusTestMethodsAmmatillinen
import fi.oph.koski.documentation.{AmmatillinenOldExamples, LukioExampleData, MuunAmmatillisenKoulutuksenExample}
import fi.oph.koski.henkilo.KoskiSpecificMockOppijat._
import fi.oph.koski.http.{ErrorMatcher, KoskiErrorCategory}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.{MockUser, MockUsers, UserWithPassword}
import fi.oph.koski.log.AuditLogTester
import fi.oph.koski.schema._
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers

class MigriSpec extends AnyFreeSpec with KoskiHttpSpec with OpiskeluoikeusTestMethodsAmmatillinen with Matchers with DirtiesFixtures {

  val user = MockUsers.luovutuspalveluKäyttäjä

  "Oppijaa ei löydy, palautetaan 404" in {
    postOid(eiKoskessa.oid, user) {
      verifyResponseStatus(404, ErrorMatcher.regex(KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia, ".*".r))
    }
  }

  "Oppijalla ei ole migriä kiinnostavia opiskeluoikeuksia, palautetaan 404" in {
    postOid(luva.oid, user) {
      verifyResponseStatus(404, ErrorMatcher.regex(KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia, ".*".r))
    }
  }

  "Oppijan opiskeluoikeus ei sisällä migriä kiinnostavia päätason suorituksia, palautetaan 404" in {
    postOid(muuAmmatillinen.oid, user) {
      verifyResponseStatus(404, ErrorMatcher.regex(KoskiErrorCategory.notFound.oppijaaEiLöydyTaiEiOikeuksia, ".*".r))
    }
  }

  "Jos taustajärjestelmästä (virta/ytr) ei saa haettua opiskeluoikeuksia, se kerrotaan virheviestissä" in {
    postOid(virtaEiVastaa.oid, user) {
      verifyResponseStatus(503, KoskiErrorCategory.unavailable.virta())
    }
  }

  "Opiskeluoikeus voidaan hakea" in {
    postOid(ammattilainen.oid, user) {
      verifyResponseStatusOk()
    }
  }

  "Oppijan opiskeluoikeuksista ja suorituksista palautetaan vain migriä kiinnostavat" in {
    resetFixtures
    putOpiskeluoikeus(MuunAmmatillisenKoulutuksenExample.muuAmmatillinenKoulutusOpiskeluoikeus, ammattilainen) {
      verifyResponseStatusOk()
    }
    verifyResponseContent(ammattilainen.oid, user) { migriOppija =>
      val opiskeluoikeusTyypit = migriOppija.opiskeluoikeudet.map(_.tyyppi.koodiarvo)
      val suoritusTyypit = migriOppija.opiskeluoikeudet.flatMap(_.suoritukset.map(_.tyyppi.koodiarvo))

      opiskeluoikeusTyypit should contain theSameElementsAs(List("ammatillinenkoulutus"))
      suoritusTyypit should contain theSameElementsAs(List("ammatillinentutkinto"))
    }
  }

  "Oidilla hakeminen luo auditlogin" in {
    AuditLogTester.clearMessages
    postOid(ammattilainen.oid, user) {
      verifyResponseStatusOk()
      AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_KATSOMINEN"))
    }
  }

  "Hetulla hakeminen luo auditlogin" in {
    AuditLogTester.clearMessages
    postHetu(ammattilainen.hetu, user) {
      verifyResponseStatusOk()
      AuditLogTester.verifyAuditLogMessage(Map("operation" -> "OPISKELUOIKEUS_KATSOMINEN"))
    }
  }

  "Tunnustettu rakenne palautetaan, jos osasuorituksen lisätietojen tunnisteen koodiarvo on 'mukautettu'" in {
    resetFixtures
    val lisätiedot = Some(List(AmmatillisenTutkinnonOsanLisätieto(
      Koodistokoodiviite("mukautettu", "ammatillisentutkinnonosanlisatieto"),
      LocalizedString.finnish("lisätiedot")
    )))
    val tunnustaminen = Some(OsaamisenTunnustaminen(
      osaaminen = None,
      selite = LocalizedString.finnish("osaamisen tunnustaminen")
    ))
    val opiskeluoikeus = modifyOsasuoritukset(
      AmmatillinenOldExamples.mukautettu.opiskeluoikeudet.head,
      lisätiedot,
      tunnustaminen
    )
    putOpiskeluoikeus(opiskeluoikeus, luva) {
      verifyResponseStatusOk()
    }
    verifyResponseContent(luva.oid, user) { migriOppija =>
      val tunnustetut = migriOppija.opiskeluoikeudet.flatMap(tunnustettuRakenteet)
      tunnustetut shouldBe(List(MigriOsaamisenTunnustaminen(LocalizedString.finnish("osaamisen tunnustaminen"))))
    }
  }

  "Muilla koodiarvoilla tunnustettu rakenne on piilotettu" in {
    resetFixtures
    val lisätiedot = Some(List(AmmatillisenTutkinnonOsanLisätieto(
      Koodistokoodiviite("muu", "ammatillisentutkinnonosanlisatieto"),
      LocalizedString.finnish("lisätiedot")
    )))
    val tunnustaminen = Some(OsaamisenTunnustaminen(
      osaaminen = None,
      selite = LocalizedString.finnish("osaamisen tunnustaminen")
    ))
    val opiskeluoikeus = modifyOsasuoritukset(
      AmmatillinenOldExamples.mukautettu.opiskeluoikeudet.head,
      lisätiedot,
      tunnustaminen
    )
    putOpiskeluoikeus(opiskeluoikeus, luva) {
      verifyResponseStatusOk()
    }
    verifyResponseContent(luva.oid, user) { migriOppija =>
      val tunnustetut = migriOppija.opiskeluoikeudet.flatMap(tunnustettuRakenteet)
      tunnustetut shouldBe(Nil)
    }
  }

  private def postOid[A](oid: String, user: MockUser)(f: => A): A = {
    post(
      "api/luovutuspalvelu/migri/oid",
      JsonSerializer.writeWithRoot(MigriOidRequest(oid)),
      headers = authHeaders(user) ++ jsonContent
    )(f)
  }

  private def postHetu[A](hetu: Option[String], user: MockUser)(f: => A): A = {
    post(
      "api/luovutuspalvelu/migri/hetu",
      JsonSerializer.writeWithRoot(MigriHetuRequest(hetu.get)),
      headers = authHeaders(user) ++ jsonContent
    )(f)
  }

  private def verifyResponseContent[A](oid: String, user: MockUser)(verify: MigriOppija => A): A = {
    postOid(oid, user) {
      verifyResponseStatusOk()
      val oppija = JsonSerializer.parse[MigriOppija](body)
      verify(oppija)
    }
  }

  private def tunnustettuRakenteet(opiskeluoikeus: MigriOpiskeluoikeus): Seq[MigriOsaamisenTunnustaminen] = {
    val osasuoritukset = opiskeluoikeus.suoritukset.flatMap(_.osasuoritukset).flatten
    val osasuoritustenOsasuoritukset = osasuoritukset.flatMap(_.osasuoritukset).flatten
    osasuoritukset.flatMap(_.tunnustettu) ++ osasuoritustenOsasuoritukset.flatMap(_.tunnustettu)
  }

  def modifyOsasuoritukset(
    opiskeluoikeus: Opiskeluoikeus,
    lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]],
    tunnustettu: Option[OsaamisenTunnustaminen]
  ) = {
    import mojave._

    val lisätiedotTraversal = traversal[Opiskeluoikeus]
      .field[List[PäätasonSuoritus]]("suoritukset").items
      .field[Option[List[Suoritus]]]("osasuoritukset").items.items
      .ifInstanceOf[AmmatillisenTutkinnonOsanSuoritus]
      .field[Option[List[AmmatillisenTutkinnonOsanLisätieto]]]("lisätiedot")

    val tunnustettuTraversal = traversal[Opiskeluoikeus]
      .field[List[PäätasonSuoritus]]("suoritukset").items
      .field[Option[List[Suoritus]]]("osasuoritukset").items.items
      .ifInstanceOf[AmmatillisenTutkinnonOsanSuoritus]
      .field[Option[OsaamisenTunnustaminen]]("tunnustettu")

    val lisätiedoilla = lisätiedotTraversal.set(opiskeluoikeus)(lisätiedot)
    tunnustettuTraversal.set(lisätiedoilla)(tunnustettu)
  }
}
