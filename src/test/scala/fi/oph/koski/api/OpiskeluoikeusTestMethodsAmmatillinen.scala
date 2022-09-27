package fi.oph.koski.api

import java.time.LocalDate

import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.documentation.AmmatillinenOldExamples.muunAmmatillisenTutkinnonOsanSuoritus
import fi.oph.koski.documentation.ExampleData
import fi.oph.koski.documentation.ExampleData._
import fi.oph.koski.http.{JsonErrorMessage, KoskiErrorCategory}
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.{OrganisaatioWithOid, _}
import org.json4s.{JArray, JObject, JString}

trait OpiskeluoikeusTestMethodsAmmatillinen extends PutOpiskeluoikeusTestMethods[AmmatillinenOpiskeluoikeus] {
  def tag = implicitly[reflect.runtime.universe.TypeTag[AmmatillinenOpiskeluoikeus]]

  override def defaultOpiskeluoikeus = makeOpiskeluoikeus(alkamispäivä = longTimeAgo)

  def makeOpiskeluoikeus(alkamispäivä: LocalDate = longTimeAgo, toimpiste: OrganisaatioWithOid = stadinToimipiste, oppilaitos: Organisaatio.Oid = MockOrganisaatiot.stadinAmmattiopisto) = AmmatillinenOpiskeluoikeus(
    tila = AmmatillinenOpiskeluoikeudenTila(List(AmmatillinenOpiskeluoikeusjakso(alkamispäivä, opiskeluoikeusLäsnä, Some(valtionosuusRahoitteinen)))),
    oppilaitos = Some(Oppilaitos(oppilaitos)),
    suoritukset = List(autoalanPerustutkinnonSuoritus(toimpiste).copy(alkamispäivä = Some(alkamispäivä.plusDays(1))))
  )

  def päättymispäivällä(oo: AmmatillinenOpiskeluoikeus, päättymispäivä: LocalDate) =
    lisääTila(oo, päättymispäivä, ExampleData.opiskeluoikeusValmistunut).copy(
      suoritukset = oo.suoritukset.map {
        case s: AmmatillisenTutkinnonSuoritus => s.copy(
          alkamispäivä = oo.alkamispäivä,
          vahvistus = vahvistus(päättymispäivä, stadinAmmattiopisto, Some(helsinki)),
          keskiarvo = Some(4.0),
          osasuoritukset = Some(List(muunAmmatillisenTutkinnonOsanSuoritus.copy(vahvistus = None)))
        )
        case _ => ???
      }
    )

  def alkamispäivällä(oo: AmmatillinenOpiskeluoikeus, alkamispäivä: LocalDate) =
    lisääTila(
      oo.copy(tila = new AmmatillinenOpiskeluoikeudenTila(opiskeluoikeusjaksot = List())),
      alkamispäivä,
      ExampleData.opiskeluoikeusLäsnä
    ).copy(
      suoritukset = oo.suoritukset.map {
        case s: AmmatillisenTutkinnonSuoritus => s.copy(
          alkamispäivä = Some(alkamispäivä),
          osasuoritukset = Some(List(muunAmmatillisenTutkinnonOsanSuoritus.copy(vahvistus = None)))
        )
        case _ => ???
      }
    )

  def lisääTila(oo: AmmatillinenOpiskeluoikeus, päivä: LocalDate, tila: Koodistokoodiviite) = oo.copy(
    tila = AmmatillinenOpiskeluoikeudenTila(oo.tila.opiskeluoikeusjaksot ++ List(AmmatillinenOpiskeluoikeusjakso(päivä, tila, Some(valtionosuusRahoitteinen))))
  )

  def lisääTiloja(opiskeluoikeus: AmmatillinenOpiskeluoikeus, jaksot: List[(LocalDate, Koodistokoodiviite)]) = {
    jaksot.foldLeft(opiskeluoikeus) { case (oo, (päivä, tila)) => lisääTila(oo, päivä, tila)}
  }

  def sisällytäOpiskeluoikeus(oo: AmmatillinenOpiskeluoikeus, sisältyy: SisältäväOpiskeluoikeus) = oo.copy(
    sisältyyOpiskeluoikeuteen = Option(sisältyy)
  )

  val sukunimiPuuttuu = KoskiErrorCategory.badRequest.validation.jsonSchema(JsonErrorMessage(JArray(List(JObject(
    "path" -> JString("henkilö.sukunimi"),
    "value" -> JString(""),
    "error" -> JObject("errorType" -> JString("emptyString"))
  )))))
}
