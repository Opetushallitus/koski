package fi.oph.koski.api

import java.time.LocalDate
import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.documentation.ExampleData
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema._

trait OpiskeluoikeusTestMethodsAmmatillinen extends PutOpiskeluOikeusTestMethods[AmmatillinenOpiskeluoikeus] {
  override def defaultOpiskeluoikeus = AmmatillinenOpiskeluoikeus(
    alkamispäivä = Some(longTimeAgo),
    tila = AmmatillinenOpiskeluoikeudenTila(List(AmmatillinenOpiskeluoikeusjakso(longTimeAgo, opiskeluoikeusLäsnä, None))),
    oppilaitos = Oppilaitos(MockOrganisaatiot.stadinAmmattiopisto),
    suoritukset = List(autoalanPerustutkinnonSuoritus()),
    tavoite = tavoiteTutkinto
  )

  def päättymispäivällä(oo: AmmatillinenOpiskeluoikeus, päättymispäivä: LocalDate) = oo.copy(
    päättymispäivä = Some(päättymispäivä),
    tila = AmmatillinenOpiskeluoikeudenTila(oo.tila.opiskeluoikeusjaksot ++ List(AmmatillinenOpiskeluoikeusjakso(päättymispäivä, ExampleData.opiskeluoikeusValmistunut)))
  )
}
