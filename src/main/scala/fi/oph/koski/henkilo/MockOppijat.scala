package fi.oph.koski.henkilo

import java.time.LocalDate

import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.log.{Loggable, Logging}
import fi.oph.koski.schema._

object MockOppijat {
  private val oppijat = new MockOppijat

  // Tällä oppijalla ei ole fixtuureissa opiskeluoikeuksia, eikä tätä lisätä henkilöpalveluun.
  val tyhjä = UusiHenkilö(Some("230872-7258"), "Tero", "Tero", "Tyhjä")

  val hetuton = oppijat.addOppija(TäydellisetHenkilötiedot("1.2.246.562.24.99999999123", None, Some(LocalDate.of(1977, 2, 24)), "Heikki", "Heikki", "Hetuton", None, None))
  val eero = oppijat.oppija("Esimerkki", "Eero", "010101-123N")
  val eerola = oppijat.oppija("Eerola", "Jouni", "081165-793C")
  val markkanen = oppijat.oppija("Markkanen-Fagerström", "Eéro Jorma-Petteri", "080154-770R")
  val teija = oppijat.oppija("Tekijä", "Teija", "251019-039B")
  val tero = oppijat.oppija("Tunkkila-Fagerlund", "Tero Petteri Gustaf", "280608-6619")
  val presidentti = oppijat.oppija("Presidentti", "Tasavallan", "")
  val koululainen = oppijat.oppija("Koululainen", "Kaisa", "220109-784L")
  val ysiluokkalainen = oppijat.oppija("Ysiluokkalainen", "Ylermi", "160932-311V")
  val monessaKoulussaOllut = oppijat.oppija("Monikoululainen", "Miia", "180497-112F")
  val lukiolainen = oppijat.oppija("Lukiolainen", "Liisa", "020655-2479")
  val lukioKesken = oppijat.oppija("Lukiokesken", "Leila", "190363-279X")
  val lukionAineopiskelija = oppijat.oppija("Lukioaineopiskelija", "Aino", "210163-2367")
  val ammattilainen = oppijat.oppija("Ammattilainen", "Aarne", "280618-402H")
  val amis = oppijat.oppija("Amis", "Antti", "211097-402L")
  val dippainssi = oppijat.oppija("Dippainssi", "Dilbert", "290492-9455")
  val korkeakoululainen = oppijat.oppija("Korkeakoululainen", "Kikka", "010675-9981")
  val amkValmistunut = oppijat.oppija("Amis", "Valmis", "101291-954C")
  val amkKesken = oppijat.oppija("Amiskesken", "Jalmari", "100292-980D")
  val amkKeskeytynyt = oppijat.oppija("Pudokas", "Valtteri", "100193-948U")
  val oppiaineenKorottaja = oppijat.oppija("Oppiaineenkorottaja", "Olli", "110738-839L")
  val aikuisOpiskelija = oppijat.oppija("Aikuisopiskelija", "Aini", "280598-2415")
  val kymppiluokkalainen = oppijat.oppija("Kymppiluokkalainen", "Kaisa", "131025-6573")
  val luva = oppijat.oppija("Lukioonvalmistautuja", "Luke", "211007-442N")
  val valma = oppijat.oppija("Amikseenvalmistautuja", "Anneli", "130404-054C")
  val ylioppilas = oppijat.oppija("Ylioppilas", "Ynjevi", "010696-971K")
  val toimintaAlueittainOpiskelija = oppijat.oppija("Toiminta", "Tommi", "031112-020J")
  val telma = oppijat.oppija("Telmanen", "Tuula", "021080-725C")
  val erikoisammattitutkinto = oppijat.oppija("Erikoinen", "Erja", "250989-419V")
  val osittainenammattitutkinto = oppijat.oppija("Osittainen", "Outi", "230297-6448")
  val paikallinenTunnustettu = oppijat.oppija("Tunnustettu", "Teuvo", "140176-449X")
  val tiedonsiirto = oppijat.oppija("Tiedonsiirto", "Tiina", "270303-281N")
  val omattiedot = oppijat.oppija(MockUsers.omattiedot.ldapUser.lastName, MockUsers.omattiedot.ldapUser.givenNames, "190751-739W", MockUsers.omattiedot.ldapUser.oid)
  val ibFinal = oppijat.oppija("IB-final", "Iina", "040701-432D")
  val ibPredicted = oppijat.oppija("IB-predicted", "Petteri", "071096-317K")
  val eskari = oppijat.oppija("Eskari", "Essi", "300996-870E")
  val opiskeluoikeudenOidKonflikti = oppijat.oppija("Oidkonflikti", "Oskari", "260539-745W", "1.2.246.562.24.09090909090")
  val eiKoskessa = oppijat.oppija("EiKoskessa", "Eino", "270181-5263", "1.2.246.562.24.99999555555")
  val eiKoskessaHetuton = oppijat.addOppija(TäydellisetHenkilötiedot("1.2.246.562.24.99999555556", None, None, "Eino", "Eino", "EiKoskessaHetuton", None, None))

  def defaultOppijat = oppijat.getOppijat

  def generateOid(counter: Int) = "1.2.246.562.24." + "%011d".format(counter)

  def oids = (defaultOppijat.map(_.oid) ++ (1 to defaultOppijat.length + 10).map(generateOid).toList).distinct // oids that should be considered when deleting fixture data
}

class MockOppijat(private var oppijat: List[TäydellisetHenkilötiedot] = Nil) extends Logging {
  private var idCounter = oppijat.length
  val äidinkieli: Some[Koodistokoodiviite] = Some(Koodistokoodiviite("FI", None, "kieli", None))

  def oppija(suku: String, etu: String, hetu: String, oid: String = generateId()): TäydellisetHenkilötiedot =
    addOppija(TäydellisetHenkilötiedot(oid, Some(hetu), None, etu, etu, suku, äidinkieli, None))

  def addOppija(oppija: TäydellisetHenkilötiedot): TäydellisetHenkilötiedot = {
    oppijat = oppija :: oppijat
    oppija
  }

  def getOppijat = oppijat

  private def generateId(): String = this.synchronized {
    idCounter = idCounter + 1
    MockOppijat.generateOid(idCounter)
  }
}

class TestingException(text: String) extends RuntimeException(text) with Loggable {
  def logString = text
}