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

class MockOppijat(private var oppijat: List[OppijaHenkilöWithMasterInfo] = Nil) extends Logging {
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
  ): LaajatOppijaHenkilöTiedot =
    addOppija(henkilo.LaajatOppijaHenkilöTiedot(
      oid = oid,
      sukunimi = suku,
      etunimet = etu,
      kutsumanimi = kutsumanimi.getOrElse(etu),
      hetu = Some(hetu),
      syntymäaika = syntymäaika,
      äidinkieli = äidinkieli,
      turvakielto = turvakielto,
      vanhatHetut = vanhaHetu.toList,
      sukupuoli = sukupuoli,
      kotikunta = kotikunta
    ))

  def addOppija(oppija: LaajatOppijaHenkilöTiedot): LaajatOppijaHenkilöTiedot = addOppija(OppijaHenkilöWithMasterInfo(oppija, None)).henkilö.asInstanceOf[LaajatOppijaHenkilöTiedot]

  def addOppija(oppija: OppijaHenkilöWithMasterInfo): OppijaHenkilöWithMasterInfo = {
    oppijat = oppija :: oppijat
    oppija
  }

  def duplicate(masterOppija: LaajatOppijaHenkilöTiedot): LaajatOppijaHenkilöTiedot = {
    val oppijaCopy = masterOppija.copy(oid = generateId())
    addOppija(OppijaHenkilöWithMasterInfo(oppijaCopy, Some(masterOppija))).henkilö.asInstanceOf[LaajatOppijaHenkilöTiedot]
  }

  def getOppijat = oppijat

  def generateId(): String = this.synchronized {
    idCounter = idCounter + 1
    FixtureCreator.generateOppijaOid(idCounter)
  }
}

class TestingException(text: String) extends RuntimeException(text) with Loggable {
  def logString = text
}
