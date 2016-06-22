package fi.oph.koski.schema

trait ValmentavanKoulutuksenOsanSuoritus extends Suoritus {
  def tunnustettu: Option[Tunnustaminen]
  def lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]]
  override def osasuoritukset = None
}
