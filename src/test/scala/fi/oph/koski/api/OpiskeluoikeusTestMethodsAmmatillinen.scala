package fi.oph.koski.api

import java.time.LocalDate
import java.time.LocalDate.{of => date}

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.KoskiDatabaseMethods
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.Tables.OpiskeluOikeudet
import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.documentation.ExampleData
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._

trait OpiskeluoikeusTestMethodsAmmatillinen extends PutOpiskeluoikeusTestMethods[AmmatillinenOpiskeluoikeus] with KoskiDatabaseMethods {
  override def defaultOpiskeluoikeus = makeOpiskeluoikeus(alkamispäivä = longTimeAgo)

  def makeOpiskeluoikeus(alkamispäivä: LocalDate = longTimeAgo) = AmmatillinenOpiskeluoikeus(
    alkamispäivä = Some(alkamispäivä),
    tila = AmmatillinenOpiskeluoikeudenTila(List(AmmatillinenOpiskeluoikeusjakso(alkamispäivä, opiskeluoikeusLäsnä, None))),
    oppilaitos = Some(Oppilaitos(MockOrganisaatiot.stadinAmmattiopisto)),
    suoritukset = List(autoalanPerustutkinnonSuoritus())
  )

  def päättymispäivällä(oo: AmmatillinenOpiskeluoikeus, päättymispäivä: LocalDate) = lisääTila(oo, päättymispäivä, ExampleData.opiskeluoikeusValmistunut).copy(
    päättymispäivä = Some(päättymispäivä),
    suoritukset = oo.suoritukset.map { case s: AmmatillisenTutkinnonSuoritus =>
      s.copy(alkamispäivä = oo.alkamispäivä, tila = tilaValmis, vahvistus = vahvistusPaikkakunnalla(päättymispäivä, stadinAmmattiopisto, helsinki))
    }
  )

  def päivitäId(oo: AmmatillinenOpiskeluoikeus): AmmatillinenOpiskeluoikeus =
    oo.oid.map { oid =>
      val id = runDbSync(OpiskeluOikeudet.filter(_.oid === oid).map(_.id).result).headOption
      oo.withIdAndVersion(id, oo.oid, oo.versionumero)
    }.getOrElse(oo)

  def lisääTila(oo: AmmatillinenOpiskeluoikeus, päivä: LocalDate, tila: Koodistokoodiviite) = oo.copy(
    tila = AmmatillinenOpiskeluoikeudenTila(oo.tila.opiskeluoikeusjaksot ++ List(AmmatillinenOpiskeluoikeusjakso(päivä, tila)))
  )

  override protected def db: DB = KoskiApplicationForTests.masterDatabase.db
}
