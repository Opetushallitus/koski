package fi.oph.koski.tiedonsiirto

import fi.oph.koski.documentation.AmmatillinenExampleData._
import fi.oph.koski.documentation.{AmmatillinenExampleData, AmmatillinenPerustutkintoExample, Example}
import fi.oph.koski.henkilo.{KoskiSpecificMockOppijat, LaajatOppijaHenkilöTiedot}
import fi.oph.koski.henkilo.MockOppijat.asUusiOppija
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.{AmmatillinenOpiskeluoikeus, LähdejärjestelmäId, Oppija, Oppilaitos, TäydellisetHenkilötiedot}

object ExamplesTiedonsiirto {
  val opiskeluoikeus: AmmatillinenOpiskeluoikeus = AmmatillinenExampleData.opiskeluoikeus(oppilaitos = Oppilaitos(MockOrganisaatiot.stadinOppisopimuskeskus)).copy(lähdejärjestelmänId = Some(winnovaLähdejärjestelmäId("l-244032")))
  val failingOpiskeluoikeus: AmmatillinenOpiskeluoikeus = opiskeluoikeus.copy(oppilaitos = Some(Oppilaitos(MockOrganisaatiot.aaltoYliopisto)))
  val epävalidiHenkilö: LaajatOppijaHenkilöTiedot = KoskiSpecificMockOppijat.tiedonsiirto.copy(hetu = Some("epävalidiHetu"))
  val failingTutkinnonosaOpiskeluoikeus: AmmatillinenOpiskeluoikeus = AmmatillinenPerustutkintoExample.osittainenPerustutkintoOpiskeluoikeus.copy(
    lähdejärjestelmänId = Some(LähdejärjestelmäId(Some("75489301"), lähdeWinnova))
  )

  val examples: List[Example] = List(
    Example("tiedonsiirto - onnistunut", "Onnistunut tiedonsiirto", Oppija(asUusiOppija(KoskiSpecificMockOppijat.tiedonsiirto), List(opiskeluoikeus)), 403),
    Example("tiedonsiirto - vain syntymäaika", "Onnistunut tiedonsiirto", Oppija(asTäydellisetHenkilötiedotExample(KoskiSpecificMockOppijat.hetuton), List(opiskeluoikeus)), 403),
    Example("tiedonsiirto - epäonnistunut", "Epäonnistunut tiedonsiirto", Oppija(asUusiOppija(KoskiSpecificMockOppijat.tiedonsiirto), List(failingOpiskeluoikeus)), 403),
    Example("tiedonsiirto - epäonnistunut 2", "Onnistunut tiedonsiirto", Oppija(asUusiOppija(KoskiSpecificMockOppijat.ammattilainen), List(failingOpiskeluoikeus)), 403),
    Example("tiedonsiirto - epäonnistunut 3", "Epäonnistunut tiedonsiirto", Oppija(asUusiOppija(epävalidiHenkilö), List(failingTutkinnonosaOpiskeluoikeus)), 403)
  )

  private def asTäydellisetHenkilötiedotExample(henkilö: LaajatOppijaHenkilöTiedot): TäydellisetHenkilötiedot =
    TäydellisetHenkilötiedot(oid = henkilö.oid, etunimet = henkilö.etunimet, kutsumanimi = henkilö.kutsumanimi, sukunimi = henkilö.sukunimi)
}
