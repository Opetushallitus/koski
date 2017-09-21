package fi.oph.koski.api

import java.time.LocalDate
import java.time.LocalDate.{of => date}

import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.documentation.ExampleData
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.{OrganisaatioWithOid, _}

trait OpiskeluoikeusTestMethodsAmmatillinen extends PutOpiskeluoikeusTestMethods[AmmatillinenOpiskeluoikeus] {
  def tag = implicitly[reflect.runtime.universe.TypeTag[AmmatillinenOpiskeluoikeus]]

  override def defaultOpiskeluoikeus = makeOpiskeluoikeus(alkamispäivä = longTimeAgo)

  def makeOpiskeluoikeus(alkamispäivä: LocalDate = longTimeAgo, toimpiste: OrganisaatioWithOid = stadinToimipiste) = AmmatillinenOpiskeluoikeus(
    alkamispäivä = Some(alkamispäivä),
    tila = AmmatillinenOpiskeluoikeudenTila(List(AmmatillinenOpiskeluoikeusjakso(alkamispäivä, opiskeluoikeusLäsnä, None))),
    oppilaitos = Some(Oppilaitos(MockOrganisaatiot.stadinAmmattiopisto)),
    suoritukset = List(autoalanPerustutkinnonSuoritus(toimpiste))
  )

  def päättymispäivällä(oo: AmmatillinenOpiskeluoikeus, päättymispäivä: LocalDate) = lisääTila(oo, päättymispäivä, ExampleData.opiskeluoikeusValmistunut).copy(
    päättymispäivä = Some(päättymispäivä),
    suoritukset = oo.suoritukset.map { case s: AmmatillisenTutkinnonSuoritus =>
      s.copy(alkamispäivä = oo.alkamispäivä, tila = tilaValmis, vahvistus = vahvistus(päättymispäivä, stadinAmmattiopisto, Some(helsinki)))
    }
  )

  def lisääTila(oo: AmmatillinenOpiskeluoikeus, päivä: LocalDate, tila: Koodistokoodiviite) = oo.copy(
    tila = AmmatillinenOpiskeluoikeudenTila(oo.tila.opiskeluoikeusjaksot ++ List(AmmatillinenOpiskeluoikeusjakso(päivä, tila)))
  )
}
