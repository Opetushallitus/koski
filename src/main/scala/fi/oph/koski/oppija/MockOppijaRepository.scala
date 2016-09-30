package fi.oph.koski.oppija

import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.log.{Loggable, Logging}
import fi.oph.koski.schema._

object MockOppijat {
  private val oppijat = new MockOppijat

  val eero = oppijat.oppija("Esimerkki", "Eero", "010101-123N")
  val eerola = oppijat.oppija("Eerola", "Jouni", "070796-9696")
  val markkanen = oppijat.oppija("Markkanen", "Eero", "070796-9652")
  val teija = oppijat.oppija("Tekijä", "Teija", "150995-914X")
  val tyhjä = oppijat.oppija("Tyhjä", "Tyhjä", "130196-961Y") // Tällä oppijalla ei ole fixtuureissa opiskeluoikeuksia
  val tero = oppijat.oppija("Tunkkila-Fagerlund", "Tero Petteri Gustaf", "091095-9833")
  val presidentti = oppijat.oppija("Presidentti", "Tasavallan", "")
  val koululainen = oppijat.oppija("Koululainen", "Kaisa", "110496-926Y")
  val lukiolainen = oppijat.oppija("Lukiolainen", "Liisa", "110496-9369")
  val ammattilainen = oppijat.oppija("Ammattilainen", "Aarne", "120496-949B")
  val dippainssi = oppijat.oppija("Dippainssi", "Dilbert", "290492-9455")
  val korkeakoululainen = oppijat.oppija("Korkeakoululainen", "Kikka", "010675-9981")
  val amkValmistunut = oppijat.oppija("Amis", "Valmis", "101291-954C")
  val amkKesken = oppijat.oppija("Amiskesken", "Jalmari", "100292-980D")
  val amkKeskeytynyt = oppijat.oppija("Pudokas", "Valtteri", "100193-948U")
  val oppiaineenKorottaja = oppijat.oppija("Oppiaineenkorottaja", "Olli", "190596-953T")
  val kymppiluokkalainen = oppijat.oppija("Kymppiluokkalainen", "Kaisa", "200596-9755")
  val luva = oppijat.oppija("Lukioonvalmistautuja", "Luke", "300596-9615")
  val valma = oppijat.oppija("Amikseenvalmistautuja", "Anneli", "160696-993Y")
  val ylioppilas = oppijat.oppija("Ylioppilas", "Ynjevi", "010696-971K")
  val toimintaAlueittainOpiskelija = oppijat.oppija("Toiminta", "Tommi", "130696-913E")
  val telma = oppijat.oppija("Telmanen", "Tuula", "170696-986C")
  val erikoisammattitutkinto = oppijat.oppija("Erikoinen", "Erja", "200696-906R")
  val tiedonsiirto = oppijat.oppija("Tiedonsiirto", "Tiina", "290896-9674")
  val omattiedot = oppijat.oppija(MockUsers.omattiedot.ldapUser.lastName, MockUsers.omattiedot.ldapUser.givenNames, "190751-739W", MockUsers.omattiedot.ldapUser.oid)
  val ibOpiskelija = oppijat.oppija("IB-opiskelija", "Iina", "130996-9225")
  val eskari = oppijat.oppija("Eskari", "Essi", "300996-870E")

  def defaultOppijat = oppijat.getOppijat
}

class MockOppijat(private var oppijat: List[TäydellisetHenkilötiedot] = Nil) extends Logging {
  private var idCounter = oppijat.length
  val äidinkieli: Some[Koodistokoodiviite] = Some(Koodistokoodiviite("FI", None, "kieli", None))

  def oppija(suku: String, etu: String, hetu: String, oid: String = generateId()): TäydellisetHenkilötiedot = {
    val oppija = TäydellisetHenkilötiedot(oid, hetu, etu, etu, suku, äidinkieli, None)
    oppijat = oppija :: oppijat
    oppija
  }

  def getOppijat = oppijat

  private def generateId(): String = this.synchronized {
    idCounter = idCounter + 1
    "1.2.246.562.24.0000000000" + idCounter
  }
}

class TestingException(text: String) extends RuntimeException(text) with Loggable {
  def logString = text
}