package fi.oph.koski.ibis

import scala.io.Source

sealed trait IbisOppiaine {
  def tunniste: String
}

case class IbisMuuOppiaine(tunniste: String) extends IbisOppiaine

case class IbisKieliOppiaine(tunniste: String, kieli: String) extends IbisOppiaine

// Kuvaus IBO:n kuusinumeroisesta oppiainekoodista (IBIS SUBJECT CODE) Kosken oppiaineetib-koodiin ja kielioppiaineilla
// lisäksi kielivalikoima-koodiin.
// TODO TOR-747: Koodit vaihtuvat istunnoittain ja selviävät IBO:n SUB-ristiviittaustiedostosta, jota ei vielä ole. Kartta
// täytetään resurssitiedostoon ensimmäisen oikean aineiston perusteella. Vaihtoehto on viedä kuvaus koodistoon.
case class IbisOppiainekartta(koodit: Map[String, IbisOppiaine]) {
  def get(iboKoodi: String): Option[IbisOppiaine] = koodit.get(iboKoodi)
}

object IbisOppiainekartta {
  val resurssi = "/ibis/oppiainekoodit.csv"

  lazy val default: IbisOppiainekartta = fromCsv(
    Option(getClass.getResourceAsStream(resurssi))
      .map(stream => Source.fromInputStream(stream, "UTF-8").mkString)
      .getOrElse("")
  )

  // Rivimuoto: iboKoodi;oppiaineetib;kielivalikoima. Kieli jätetään tyhjäksi muilla kuin kielioppiaineilla. #-alkuiset rivit ohitetaan.
  def fromCsv(content: String): IbisOppiainekartta = IbisOppiainekartta(
    content.split("\r?\n").toList
      .map(_.trim)
      .filter(line => line.nonEmpty && !line.startsWith("#"))
      .map(_.split(";", -1).toList.map(_.trim))
      .collect {
        case iboKoodi :: tunniste :: kieli :: _ if kieli.nonEmpty => iboKoodi -> IbisKieliOppiaine(tunniste, kieli)
        case iboKoodi :: tunniste :: _ => iboKoodi -> IbisMuuOppiaine(tunniste)
      }
      .toMap
  )
}
