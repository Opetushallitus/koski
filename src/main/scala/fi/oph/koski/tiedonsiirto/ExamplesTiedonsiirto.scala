package fi.oph.koski.tiedonsiirto

import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.documentation.{AmmatillinenExampleData, AmmatillinenPerustutkintoExample, Example}
import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.henkilo.MockOppijat.asUusiOppija
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.{AmmatillinenOpiskeluoikeus, Oppija, Oppilaitos, TäydellisetHenkilötiedot}

object ExamplesTiedonsiirto {
  val opiskeluoikeus: AmmatillinenOpiskeluoikeus = AmmatillinenExampleData.opiskeluoikeus().copy(lähdejärjestelmänId = Some(winnovaLähdejärjestelmäId))
  val failingOpiskeluoikeus: AmmatillinenOpiskeluoikeus = opiskeluoikeus.copy(oppilaitos = Some(Oppilaitos(MockOrganisaatiot.aaltoYliopisto)))
  val epävalidiHenkilö: TäydellisetHenkilötiedot = MockOppijat.tiedonsiirto.henkilö.copy(hetu = Some("epävalidiHetu"))
  val failingTutkinnonosaOpiskeluoikeus: AmmatillinenOpiskeluoikeus = AmmatillinenPerustutkintoExample.osittainenPerustutkintoOpiskeluoikeus.copy(
    lähdejärjestelmänId = Some(winnovaLähdejärjestelmäId)
  )

  val examples: List[Example] = List(
    Example("tiedonsiirto - onnistunut", "Onnistunut tiedonsiirto", Oppija(asUusiOppija(MockOppijat.tiedonsiirto.henkilö), List(opiskeluoikeus)), 403),
    Example("tiedonsiirto - vain syntymäaika", "Onnistunut tiedonsiirto", Oppija(MockOppijat.hetuton.henkilö, List(opiskeluoikeus)), 403),
    Example("tiedonsiirto - epäonnistunut", "Epäonnistunut tiedonsiirto", Oppija(asUusiOppija(MockOppijat.tiedonsiirto.henkilö), List(failingOpiskeluoikeus)), 403),
    Example("tiedonsiirto - epäonnistunut 2", "Onnistunut tiedonsiirto", Oppija(asUusiOppija(MockOppijat.ammattilainen.henkilö), List(failingOpiskeluoikeus)), 403),
    Example("tiedonsiirto - epäonnistunut 3", "Epäonnistunut tiedonsiirto", Oppija(asUusiOppija(epävalidiHenkilö), List(failingTutkinnonosaOpiskeluoikeus)), 403)
  )
}
