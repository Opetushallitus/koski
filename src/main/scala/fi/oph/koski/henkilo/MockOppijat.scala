package fi.oph.koski.henkilo

import java.time.LocalDate

import fi.oph.koski.fixture.FixtureCreator
import fi.oph.koski.henkilo
import fi.oph.koski.log.{Loggable, Logging}
import fi.oph.koski.schema._

object MockOppijat {
  def asUusiOppija(oppija: LaajatOppijaHenkilöTiedot) =
    UusiHenkilö(oppija.hetu.get, oppija.etunimet, Some(oppija.kutsumanimi), oppija.sukunimi)

  def kuntahistoriaDefault(oppija: OppijaHenkilöWithMasterInfo): OppijanKuntahistoria = {
    oppija.henkilö match {
      case h: LaajatOppijaHenkilöTiedot
        if oppija.master.isEmpty && // Ei lisätä historiaa oletusarvoisesti sivoppijaoideille
          h.kotikunta.isDefined =>
        val kotikunta = h.kotikunta.get
        val syntymäaika = h.syntymäaika.getOrElse(LocalDate.of(1900, 1, 1))
        val kuntahistoria = OppijanumerorekisteriKotikuntahistoriaRow(h.oid, kotikunta, Some(syntymäaika), None)
        if (h.turvakielto) {
          OppijanKuntahistoria(Some(h.oid), Seq.empty, Seq(kuntahistoria))
        } else {
          OppijanKuntahistoria(Some(h.oid), Seq(kuntahistoria), Seq.empty)
        }
      case _ => OppijanKuntahistoria(None, Seq.empty, Seq.empty)
    }
  }

  def kuntahistoriaTyhjä(oppija: OppijaHenkilöWithMasterInfo): OppijanKuntahistoria = OppijanKuntahistoria(None, Seq.empty, Seq.empty)
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
    syntymäaika: Option[LocalDate] = None,
    kuntahistoriaMock: OppijaHenkilöWithMasterInfo => OppijanKuntahistoria = MockOppijat.kuntahistoriaDefault
  ): LaajatOppijaHenkilöTiedot = {
    addLaajatOppijaHenkilöTiedot(henkilo.LaajatOppijaHenkilöTiedot(
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
    ), kuntahistoriaMock)
  }

  def oppijaSyntymäaikaHetusta(
    suku: String,
    etu: String,
    hetu: String,
    oid: String = generateId(),
    kutsumanimi: Option[String] = None,
    turvakielto: Boolean = false,
    vanhaHetu: Option[String] = None,
    kotikunta: Option[String] = None,
    äidinkieli: Option[String] = Some("fi"),
    kuntahistoriaMock: OppijaHenkilöWithMasterInfo => OppijanKuntahistoria = MockOppijat.kuntahistoriaDefault
  ): LaajatOppijaHenkilöTiedot = {
    val syntymäaika = Hetu.century(hetu).map(century => Hetu.birthday(hetu, century))
    val sukupuoli = Hetu.sukupuoli(hetu)
    oppija(
      suku, etu, hetu, oid, kutsumanimi, turvakielto, vanhaHetu, sukupuoli, kotikunta, äidinkieli, syntymäaika, kuntahistoriaMock)
  }

  def addLaajatOppijaHenkilöTiedot(
    oppija: LaajatOppijaHenkilöTiedot,
    kuntahistoriaMock: OppijaHenkilöWithMasterInfo => OppijanKuntahistoria = MockOppijat.kuntahistoriaDefault
  ): LaajatOppijaHenkilöTiedot = addOppijaHenkilöWithMasterInfo(OppijaHenkilöWithMasterInfo(oppija, None), kuntahistoriaMock).henkilö.asInstanceOf[LaajatOppijaHenkilöTiedot]

  def addOppijaHenkilöWithMasterInfo(
    oppija: OppijaHenkilöWithMasterInfo,
    kuntahistoriaMock: OppijaHenkilöWithMasterInfo => OppijanKuntahistoria = MockOppijat.kuntahistoriaDefault
  ): OppijaHenkilöWithMasterInfo = {
    oppijat = oppija :: oppijat

    kuntahistoriaMock(oppija) match {
      case OppijanKuntahistoria(Some(oppijaOid), kuntahistoria, turvakieltoKuntahistoria) =>
        kuntahistoriat.put(oppijaOid, kuntahistoria)
        turvakieltoKuntahistoriat.put(oppijaOid, turvakieltoKuntahistoria)
      case _ =>
    }
    oppija
  }

  def duplicate(masterOppija: LaajatOppijaHenkilöTiedot): LaajatOppijaHenkilöTiedot = {
    val oppijaCopy = masterOppija.copy(oid = generateId())
    addOppijaHenkilöWithMasterInfo(OppijaHenkilöWithMasterInfo(oppijaCopy, Some(masterOppija)), MockOppijat.kuntahistoriaTyhjä _).henkilö.asInstanceOf[LaajatOppijaHenkilöTiedot]
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

case class OppijanKuntahistoria(
  val oppijaOid: Option[String],
  val kuntahistoria: Seq[OppijanumerorekisteriKotikuntahistoriaRow],
  val turvakieltoKuntahistoria: Seq[OppijanumerorekisteriKotikuntahistoriaRow]
)
