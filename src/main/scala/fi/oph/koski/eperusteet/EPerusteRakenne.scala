package fi.oph.koski.eperusteet

import fi.oph.scalaschema.annotation.Discriminator

import java.time.{Instant, LocalDate, ZoneId, ZonedDateTime}


case class EPerusteRakenne(
  id: Long,
  nimi: Map[String, String],
  diaarinumero: String,
  koulutustyyppi: String,
  voimassaoloLoppuu: Option[String],
  koulutukset: List[EPerusteKoulutus],
  suoritustavat: Option[List[ESuoritustapa]],
  tutkinnonOsat: Option[List[ETutkinnonOsa]],
  osaamisalat: List[EOsaamisala],
  lukiokoulutus: Option[ELukiokoulutus]
) {
  def toEPeruste: EPeruste = EPeruste(id, nimi, diaarinumero, koulutukset)
  def päättynyt(vertailupäivämäärä: LocalDate = LocalDate.now()) = voimassaoloLoppuuLocalDate match {
    case Some(loppupäivämäärä) => vertailupäivämäärä.isAfter(loppupäivämäärä)
    case None => false
  }
  def voimassaoloLoppuuLocalDate = voimassaoloLoppuu.map(ms =>
    ZonedDateTime.ofInstant(Instant.ofEpochMilli(ms.toLong), ZoneId.systemDefault()).toLocalDate())
}


case class ESuoritustapa(
  suoritustapakoodi: String,
  laajuusYksikko: Option[String],
  rakenne: Option[ERakenneOsa],
  tutkinnonOsaViitteet: Option[List[ETutkinnonOsaViite]]
)

case class ETutkinnonOsaViite(
  id: Long,
  _tutkinnonOsa: String
)

case class EOsaamisala(
  nimi: Map[String, String],
  arvo: String
)

case class EOsaamisalaViite(
  osaamisalakoodiArvo: String
)

case class ETutkinnonOsa(
  id: Long,
  nimi: Map[String, String],
  koodiArvo: String
)


case class ELukiokoulutus(
  rakenne: ERakenneLukio
)

case class EOppiaine(
  koodiArvo: String,
  oppimaarat: List[EOppimaara],
  kurssit: List[EKurssi]
)

case class EOppimaara(
  koodiArvo: String,
  kurssit: List[EKurssi]
)

case class EKurssi(
  koodiArvo: String
)


case class ELaajuus(
  minimi: Option[Long],
  maksimi: Option[Long],
  yksikko: Option[String]
)

case class EKoko(
  minimi: Option[Long],
  maksimi: Option[Long]
)

case class EMuodostumisSaanto(
  laajuus: Option[ELaajuus],
  koko: Option[EKoko]
)


sealed trait ERakenneOsa

case class ERakenneModuuli(
  nimi: Option[Map[String, String]],
  @Discriminator
  osat: List[ERakenneOsa],
  osaamisala: Option[EOsaamisalaViite],
  rooli: Option[String],
  muodostumisSaanto: Option[EMuodostumisSaanto]
) extends ERakenneOsa

case class ERakenneTutkinnonOsa(
  @Discriminator
  _tutkinnonOsaViite: String
) extends ERakenneOsa

case class ERakenneLukio(
  oppiaineet: List[EOppiaine]
) extends ERakenneOsa
