package fi.oph.koski.henkilo

import fi.oph.koski.fixture.{FixtureCreator, FixtureType}
import fi.oph.koski.henkilo
import fi.oph.koski.log.{Loggable, Logging}
import fi.oph.koski.schema._
import fi.oph.koski.valpas.henkilo.ValpasMockOppijat

object MockOppijat {
  def generateOid(counter: Int) = "1.2.246.562.24." + "%011d".format(counter) // TODO: Käytä eri oid-sarjaa Valpas-oppijoille?

  def oids = (
    KoskiSpecificMockOppijat.defaultOppijat.map(_.henkilö.oid) ++
      ValpasMockOppijat.defaultOppijat.map(_.henkilö.oid) ++
      (1 to (KoskiSpecificMockOppijat.defaultOppijat.length.max(ValpasMockOppijat.defaultOppijat.length)) + 100).map(generateOid).toList
    ).distinct // oids that should be considered when deleting fixture data

  def asUusiOppija(oppija: LaajatOppijaHenkilöTiedot) =
    UusiHenkilö(oppija.hetu.get, oppija.etunimet, Some(oppija.kutsumanimi), oppija.sukunimi)

  def defaultOppijat = FixtureCreator.currentFixtureType match {
    case FixtureType.KOSKI => KoskiSpecificMockOppijat.defaultOppijat
    case FixtureType.VALPAS => ValpasMockOppijat.defaultOppijat
  }
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
    äidinkieli: Option[String] = Some("fi")
  ): LaajatOppijaHenkilöTiedot =
    addOppija(henkilo.LaajatOppijaHenkilöTiedot(
      oid = oid,
      sukunimi = suku,
      etunimet = etu,
      kutsumanimi = kutsumanimi.getOrElse(etu),
      hetu = Some(hetu),
      syntymäaika = None,
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

  def getOppijat = oppijat

  def generateId(): String = this.synchronized {
    idCounter = idCounter + 1
    MockOppijat.generateOid(idCounter)
  }
}

class TestingException(text: String) extends RuntimeException(text) with Loggable {
  def logString = text
}
