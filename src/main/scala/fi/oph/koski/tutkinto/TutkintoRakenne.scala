package fi.oph.koski.tutkinto

import fi.oph.koski.schema.{Koodistokoodiviite, LocalizedString}
import fi.oph.koski.tutkinto.Koulutustyyppi.Koulutustyyppi

case class TutkintoRakenne(
  id: Long,
  diaarinumero: String,
  koulutustyyppi: Koulutustyyppi,
  suoritustavat: List[SuoritustapaJaRakenne],
  osaamisalat: List[Koodistokoodiviite],
  tutkintonimikkeet: List[Koodistokoodiviite],
  koulutukset: List[Koodistokoodiviite]
) {
  def findSuoritustapaJaRakenne(suoritustapa: Koodistokoodiviite): Option[SuoritustapaJaRakenne] = {
    suoritustavat.find(_.suoritustapa == suoritustapa)
  }
}

case class SuoritustapaJaRakenne(suoritustapa: Koodistokoodiviite, rakenne: Option[RakenneOsa])
case class TutkinnonOsanLaajuus(min: Option[Long], max: Option[Long])

sealed trait RakenneOsa {
  def sisältääMäärittelemättömiäOsia: Boolean
  def tutkinnonOsat: List[TutkinnonOsa]
}

case class RakenneModuuli(nimi: LocalizedString, osat: List[RakenneOsa], määrittelemätön: Boolean, laajuus: Option[TutkinnonOsanLaajuus]) extends RakenneOsa {
  def tutkinnonOsat: List[TutkinnonOsa] = osat flatMap {
    case m: RakenneModuuli => m.tutkinnonOsat
    case o: TutkinnonOsa => List(o)
  }
  def tutkinnonRakenneLaajuus: TutkinnonOsanLaajuus = {
    this.laajuus.getOrElse(TutkinnonOsanLaajuus(None, None))
  }

  override def sisältääMäärittelemättömiäOsia: Boolean = määrittelemätön || osat.exists(_.sisältääMäärittelemättömiäOsia)
}
case class TutkinnonOsa(tunniste: Koodistokoodiviite, nimi: LocalizedString, laajuus: Option[Long], osaAlueet: List[TutkinnonOsanOsaAlue]) extends RakenneOsa {
  override def tutkinnonOsat: List[TutkinnonOsa] = List(this)
  override def sisältääMäärittelemättömiäOsia: Boolean = false
  // TODO add laajuus (from viite) + osa-alueet (+ thier laajuus + pakollisuus)
}

case class TutkinnonOsanOsaAlue(id: Long, koodiarvo: String, pakollisenOsanLaajuus: Option[Long], valinnaisenOsanLaajuus: Option[Long])
