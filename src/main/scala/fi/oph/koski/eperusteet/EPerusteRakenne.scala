package fi.oph.koski.eperusteet

import fi.oph.scalaschema.annotation.Discriminator
import org.json4s.JValue
import org.json4s.jackson.Json

import java.time.{Instant, LocalDate, ZoneId, ZonedDateTime}

case class EPerusteTunniste(
  id: Long,
  nimi: Map[String, String],
  voimassaoloAlkaaLocalDate: Option[LocalDate],
  voimassaoloLoppuuLocalDate: Option[LocalDate],
  siirtymäPäättyyLocalDate: Option[LocalDate],
  luotu: Option[Long]
) extends EPerusteVoimassaololla

trait EPerusteRakenne extends EPerusteVoimassaololla {
  def id: Long
  def nimi: Map[String, String]
  def diaarinumero: String
  def voimassaoloAlkaa: Option[String]
  def voimassaoloLoppuu: Option[String]
  def siirtymaPaattyy: Option[String]
  def koulutukset: List[EPerusteKoulutus]
  def koulutusvienti: Option[Boolean]
  def luotu: Option[Long]

  def toEPerusteTunniste: EPerusteTunniste = EPerusteTunniste(
    id,
    nimi,
    voimassaoloAlkaaLocalDate,
    voimassaoloLoppuuLocalDate,
    siirtymäPäättyyLocalDate,
    luotu
  )

  def voimassaoloAlkaaLocalDate: Option[LocalDate] = voimassaoloAlkaa.map(ms =>
    ZonedDateTime.ofInstant(Instant.ofEpochMilli(ms.toLong), ZoneId.systemDefault()).toLocalDate())


  def voimassaoloLoppuuLocalDate: Option[LocalDate] = voimassaoloLoppuu.map(ms =>
    ZonedDateTime.ofInstant(Instant.ofEpochMilli(ms.toLong), ZoneId.systemDefault()).toLocalDate())


  def siirtymäPäättyyLocalDate: Option[LocalDate] = siirtymaPaattyy.map(ms =>
    ZonedDateTime.ofInstant(Instant.ofEpochMilli(ms.toLong), ZoneId.systemDefault()).toLocalDate())
}

trait EPerusteVoimassaololla {
  def voimassaoloAlkaaLocalDate: Option[LocalDate]
  def voimassaoloLoppuuLocalDate: Option[LocalDate]
  def siirtymäPäättyyLocalDate: Option[LocalDate]

  def voimassaOloAlkanut(vertailupäivämäärä: LocalDate): Boolean = voimassaoloAlkaaLocalDate match {
    case Some(alkupäivämäärä) => !alkupäivämäärä.isAfter(vertailupäivämäärä)
    case _ => false
  }

  def siirtymäTaiVoimassaoloPäättynyt(vertailupäivämäärä: LocalDate): Boolean = if (siirtymäPäättyyLocalDate.isDefined) {
    siirtymäPäättynyt(vertailupäivämäärä)
  } else {
    voimassaoloLoppunut(vertailupäivämäärä)
  }

  def voimassaoloLoppunut(vertailupäivämäärä: LocalDate): Boolean = voimassaoloLoppuuLocalDate match {
    case Some(loppupäivämäärä) => vertailupäivämäärä.isAfter(loppupäivämäärä)
    case None => false
  }

  def siirtymäPäättynyt(vertailupäivämäärä: LocalDate): Boolean = siirtymäPäättyyLocalDate match {
    case Some(päättymispäivämäärä) => vertailupäivämäärä.isAfter(päättymispäivämäärä)
    case None => false
  }
}

trait EPerusteTarkkaRakenne extends EPerusteRakenne {
  def suoritustavat: Option[List[ETarkkaSuoritustapa]]
  def tutkinnonOsat: Option[List[ETutkinnonOsa]]
  def lukiokoulutus: Option[ELukiokoulutus]
  def muokattu: Option[Long]
  def koulutustyyppi: String
  def osaamisalat: List[EOsaamisala]
  def tutkintonimikkeet: Option[List[ETutkintonimike]]
}

trait ESuoritustapa {
  def suoritustapakoodi: String
  def laajuusYksikko: Option[String]
}

trait ETarkkaSuoritustapa extends ESuoritustapa {
  def rakenne: Option[ERakenneOsa]
  def tutkinnonOsaViitteet: Option[List[ETutkinnonOsaViite]]
}

case class EPerusteKoulutus(
  nimi: Map[String, String],
  koulutuskoodiArvo: String
)

case class EPerusteOsaRakenteet(
  data: List[EPerusteOsaRakenne]
)

case class EPerusteOsaRakenne(
  id: Long,
  nimi: Map[String, String],
  diaarinumero: String,
  voimassaoloAlkaa: Option[String],
  voimassaoloLoppuu: Option[String],
  siirtymaPaattyy: Option[String],
  koulutukset: List[EPerusteKoulutus],
  koulutusvienti: Option[Boolean],
  luotu: Option[Long]
) extends EPerusteRakenne

case class EOsaSuoritustapa(
  suoritustapakoodi: String,
  laajuusYksikko: Option[String]
) extends ESuoritustapa

case class EPerusteKokoRakenne(
  id: Long,
  nimi: Map[String, String],
  diaarinumero: String,
  koulutustyyppi: String,
  voimassaoloAlkaa: Option[String],
  voimassaoloLoppuu: Option[String],
  siirtymaPaattyy: Option[String],
  koulutukset: List[EPerusteKoulutus],
  koulutusvienti: Option[Boolean],
  suoritustavat: Option[List[EKokoSuoritustapa]],
  tutkinnonOsat: Option[List[ETutkinnonOsa]],
  osaamisalat: List[EOsaamisala],
  tutkintonimikkeet: Option[List[ETutkintonimike]],
  lukiokoulutus: Option[ELukiokoulutus],
  luotu: Option[Long],
  muokattu: Option[Long],
  lops2019: Option[JValue],
) extends EPerusteTarkkaRakenne {
  def toEPeruste: EPerusteRakenne = EPerusteOsaRakenne(
    id,
    nimi,
    diaarinumero,
    voimassaoloAlkaa,
    voimassaoloLoppuu,
    siirtymaPaattyy,
    koulutukset,
    koulutusvienti,
    luotu
  )
}

case class EKokoSuoritustapa(
  suoritustapakoodi: String,
  laajuusYksikko: Option[String],
  rakenne: Option[ERakenneOsa],
  tutkinnonOsaViitteet: Option[List[ETutkinnonOsaViite]]
) extends ETarkkaSuoritustapa

case class ETutkinnonOsaViite(
  id: Long,
  _tutkinnonOsa: String
)

case class EOsaamisala(
  nimi: Map[String, String],
  arvo: String
)

case class ETutkintonimike(
  nimi: Option[Map[String, String]],
  tutkintonimikeArvo: String
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
