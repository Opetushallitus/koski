package fi.oph.koski.henkilo

import java.time.LocalDate

import fi.oph.koski.fixture.FixtureCreator
import fi.oph.koski.henkilo
import fi.oph.koski.log.{Loggable, Logging}
import fi.oph.koski.schema._

object MockOppijat {
  def asUusiOppija(oppija: LaajatOppijaHenkilöTiedot) =
    UusiHenkilö(oppija.hetu.get, oppija.etunimet, Some(oppija.kutsumanimi), oppija.sukunimi)
}

class MockOppijat(
  private var oppijat: List[OppijaHenkilöWithMasterInfo] = Nil,
  var kuntahistoriat: scala.collection.mutable.Map[String, Seq[OppijanumerorekisteriKotikuntahistoriaRow]] = scala.collection.mutable.Map.empty,
  var turvakieltoKuntahistoriat: scala.collection.mutable.Map[String, Seq[OppijanumerorekisteriKotikuntahistoriaRow]] = scala.collection.mutable.Map.empty
) extends Logging {
  private var idCounter = oppijat.length
  val äidinkieli: Some[Koodistokoodiviite] = Some(Koodistokoodiviite("FI", None, "kieli", None))

  def oppija(
    suku: String,
    etu: String,
    hetu: String,
    oid: String = generateId(),
    kutsumanimi: Option[String] = None,
    turvakielto: Boolean = false,
    vanhaHetu: Option[String] = None,
    sukupuoli: Option[String] = None,
    kotikunta: Option[String] = None,
    äidinkieli: Option[String] = Some("fi"),
    syntymäaika: Option[LocalDate] = None
  ): LaajatOppijaHenkilöTiedot = {
    addOppija(henkilo.LaajatOppijaHenkilöTiedot(
      oid = oid,
      sukunimi = suku,
      etunimet = etu,
      kutsumanimi = kutsumanimi.getOrElse(etu),
      hetu = if (hetu.isEmpty) None else Some(hetu),
      syntymäaika = syntymäaika,
      äidinkieli = äidinkieli,
      turvakielto = turvakielto,
      vanhatHetut = vanhaHetu.toList,
      sukupuoli = sukupuoli,
      kotikunta = kotikunta
    ))
  }

  def oppijaSyntymäaikaHetusta(
    suku: String,
    etu: String,
    hetu: String,
    oid: String = generateId(),
    kutsumanimi: Option[String] = None,
    turvakielto: Boolean = false,
    vanhaHetu: Option[String] = None,
    sukupuoli: Option[String] = None,
    kotikunta: Option[String] = None,
    äidinkieli: Option[String] = Some("fi")
  ): LaajatOppijaHenkilöTiedot = {
    val syntymäaika = Hetu.century(hetu).map(century => Hetu.birthday(hetu, century))
    oppija(
      suku, etu, hetu, oid, kutsumanimi, turvakielto, vanhaHetu, sukupuoli, kotikunta, äidinkieli, syntymäaika)
  }

  def addOppija(oppija: LaajatOppijaHenkilöTiedot): LaajatOppijaHenkilöTiedot = addOppija(OppijaHenkilöWithMasterInfo(oppija, None)).henkilö.asInstanceOf[LaajatOppijaHenkilöTiedot]

  def addOppija(oppija: OppijaHenkilöWithMasterInfo): OppijaHenkilöWithMasterInfo = {
    oppijat = oppija :: oppijat

    // Luo tyhjä kotikuntahistoria
    oppija.henkilö match {
      case h: LaajatOppijaHenkilöTiedot if oppija.master.isEmpty =>
        h.kotikunta.foreach(k => {
          val syntymäaika = h.syntymäaika.getOrElse(LocalDate.of(1900, 1, 1))
          val kuntahistoria = OppijanumerorekisteriKotikuntahistoriaRow(h.oid, k, Some(syntymäaika), None)
          if (h.turvakielto) {
            turvakieltoKuntahistoriat.put(h.oid, Seq(kuntahistoria))
          } else {
            kuntahistoriat.put(h.oid, Seq(kuntahistoria))
          }
        })
      case _ => Unit
    }
    oppija
  }

  def duplicate(masterOppija: LaajatOppijaHenkilöTiedot): LaajatOppijaHenkilöTiedot = {
    val oppijaCopy = masterOppija.copy(oid = generateId())
    addOppija(OppijaHenkilöWithMasterInfo(oppijaCopy, Some(masterOppija))).henkilö.asInstanceOf[LaajatOppijaHenkilöTiedot]
  }

  def getOppijat = oppijat

  def getKuntahistoriat = kuntahistoriat

  def getTurvakieltoKuntahistoriat = turvakieltoKuntahistoriat

  def generateId(): String = this.synchronized {
    idCounter = idCounter + 1
    FixtureCreator.generateOppijaOid(idCounter)
  }
}

class TestingException(text: String) extends RuntimeException(text) with Loggable {
  def logString = text
}
