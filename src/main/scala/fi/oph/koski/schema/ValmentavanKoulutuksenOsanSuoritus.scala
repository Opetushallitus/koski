package fi.oph.koski.schema

trait ValmentavanKoulutuksenOsanSuoritus extends Suoritus {
  def tunnustettu: Option[OsaamisenTunnustaminen]
  def lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]]
  override def osasuoritukset = None
}
