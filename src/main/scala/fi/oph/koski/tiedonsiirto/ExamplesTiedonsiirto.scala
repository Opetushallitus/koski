package fi.oph.koski.tiedonsiirto

import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.documentation.{AmmatillinenExampleData, Example}
import fi.oph.koski.oppija.MockOppijat
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.{AmmatillinenOpiskeluoikeus, Oppija, Oppilaitos}

object ExamplesTiedonsiirto {
  private val opiskeluoikeus = AmmatillinenExampleData.opiskeluoikeus().copy(lähdejärjestelmänId = Some(winnovaLähdejärjestelmäId))
  private val failingOpiskeluoikeus: AmmatillinenOpiskeluoikeus = opiskeluoikeus.copy(oppilaitos = Oppilaitos(MockOrganisaatiot.aaltoYliopisto))

  val examples: List[Example] = List(
    Example("tiedonsiirto - onnistunut", "Onnistunut tiedonsiirto", Oppija(MockOppijat.tiedonsiirto.vainHenkilötiedot, List(opiskeluoikeus))),
    Example("tiedonsiirto - epäonnistunut", "Epäonnistunut tiedonsiirto", Oppija(MockOppijat.tiedonsiirto.vainHenkilötiedot, List(failingOpiskeluoikeus))),
    Example("tiedonsiirto - epäonnistunut 2", "Onnistunut tiedonsiirto", Oppija(MockOppijat.ammattilainen.vainHenkilötiedot, List(failingOpiskeluoikeus)))
  )
}
