package fi.oph.koski.henkilo

import fi.oph.koski.db.DB
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.KoskiTables.OpiskeluOikeudetWithAccessCheck
import fi.oph.koski.db.{PostgresDriverWithJsonSupport, QueryMethods}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession.systemUser
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.schema.Koodistokoodiviite

class MockOpintopolkuHenkilöFacade extends OpintopolkuHenkilöFacade with Logging {
  private var alkuperäisetOppijat = KoskiSpecificMockOppijat.defaultOppijat
  private var oppijat = new MockOppijat(alkuperäisetOppijat)

  def resetFixtures(uudetOppijat: List[OppijaHenkilöWithMasterInfo]): Unit = synchronized {
    alkuperäisetOppijat = uudetOppijat
    oppijat = new MockOppijat(alkuperäisetOppijat)
  }

  private def create(createUserInfo: UusiOppijaHenkilö): Either[HttpStatus, String] = synchronized {
    if (createUserInfo.sukunimi == "error") {
      throw new TestingException("Testing error handling")
    } else if (oppijat.getOppijat.exists(_.henkilö.hetu == createUserInfo.hetu)) {
      Left(KoskiErrorCategory.conflict.hetu("conflict"))
    } else {
      val newOppija = oppijat.oppija(createUserInfo.sukunimi, createUserInfo.etunimet, createUserInfo.hetu.getOrElse(throw new IllegalArgumentException("Hetu puuttuu")), kutsumanimi = Some(createUserInfo.kutsumanimi))
      Right(newOppija.oid)
    }
  }

  def findOppijaJaYhteystiedotByOid(henkilöOid: String): Option[LaajatOppijaHenkilöTiedot] =
    findHenkilötiedot(henkilöOid).map(_.henkilö).map(withLinkedOids)

  def findMasterOppija(henkilöOid: String): Option[LaajatOppijaHenkilöTiedot] =
    findHenkilötiedot(henkilöOid).flatMap(_.master)
      .orElse(findHenkilötiedot(henkilöOid).map(_.henkilö))
      .map(withLinkedOids)

  def findMasterOppijat(oids: List[String]): Map[String, LaajatOppijaHenkilöTiedot] = oids
    .map(oid => oid -> findMasterOppija(oid))
    .filter(_._2.isDefined)
    .map { case (oid, oppija) => oid -> oppija.get }.toMap

  protected def findHenkilötiedot(id: String): Option[OppijaHenkilöWithMasterInfo] = synchronized {
    oppijat.getOppijat.find(_.henkilö.oid == id)
  }

  def findOppijatNoSlaveOids(oids: Seq[String]): Seq[OppijaHenkilö] = {
    oids.flatMap(findOppijaByOid)
  }

  def findOrCreate(createUserInfo: UusiOppijaHenkilö): Either[HttpStatus, OppijaHenkilö] = {
    def oidFrom(oppijat: Option[LaajatOppijaHenkilöTiedot]): Either[HttpStatus, Oid] = {
      oppijat match {
        case Some(oppija) =>
          Right(oppija.oid)
        case _ =>
          logger.error("Oppijan lisääminen epäonnistui: ei voitu lisätä, muttei myöskään löytynyt.")
          Left(KoskiErrorCategory.internalError())
      }
    }
    val UusiOppijaHenkilö(Some(hetu), sukunimi, etunimet, kutsumanimi, _) = createUserInfo
    val oid = Hetu.validate(hetu, acceptSynthetic = true).right.flatMap { hetu =>
      create(createUserInfo).left.flatMap {
        case HttpStatus(409, _) => oidFrom(findOppijaByHetu(hetu))
        case HttpStatus(_, _) => throw new RuntimeException("Unreachable match arm: HTTP status code must be 409")
      }
    }
    oid.right.map(oid => findOppijaByOid(oid).get)
  }

  def modifyMock(oppija: OppijaHenkilöWithMasterInfo): Unit = synchronized {
    oppijat = new MockOppijat(oppijat.getOppijat.map { o =>
      if (o.henkilö.oid == oppija.henkilö.oid)
        o.copy(henkilö = toLaajat(o.henkilö, findSlaveOids(o.henkilö.oid)).copy(etunimet = oppija.henkilö.etunimet, kutsumanimi = oppija.henkilö.kutsumanimi, sukunimi = oppija.henkilö.sukunimi), master = oppija.master)
      else o
    })
  }

  override def findOppijaByHetu(hetu: String): Option[LaajatOppijaHenkilöTiedot] = synchronized {
    oppijat.getOppijat.find(o => o.henkilö.hetu.contains(hetu) || vanhatHetut(o.henkilö).contains(hetu)).map(h => h.master.getOrElse(h.henkilö)).map(withLinkedOids)
  }

  override def findChangedOppijaOids(since: Long, offset: Int, amount: Int): List[Oid] = synchronized {
    alkuperäisetOppijat.diff(oppijat.getOppijat).map(_.henkilö.oid)
  }

  def findOppijatByHetusNoSlaveOids(hetus: Seq[String]): Seq[OppijaHenkilö] = synchronized {
    hetus.flatMap(findOppijaByHetu)
  }

  override def findSlaveOids(masterOid: String): List[Oid] =
    alkuperäisetOppijat.filter(_.master.exists(_.oid == masterOid)).map(_.henkilö.oid)

  private def withLinkedOids(x: OppijaHenkilö) = x match {
    case y: SuppeatOppijaHenkilöTiedot => toLaajat(y, linkitetytOidit = findSlaveOids(x.oid))
    case z: LaajatOppijaHenkilöTiedot => z.copy(linkitetytOidit = findSlaveOids(x.oid))
  }

  private def vanhatHetut(oh: OppijaHenkilö) = oh match {
    case _: SuppeatOppijaHenkilöTiedot => Nil
    case x: LaajatOppijaHenkilöTiedot => x.vanhatHetut
  }

  private def toLaajat(oh: OppijaHenkilö, linkitetytOidit: List[String]) = LaajatOppijaHenkilöTiedot(
    oid = oh.oid,
    sukunimi = oh.sukunimi,
    etunimet = oh.etunimet,
    kutsumanimi = oh.kutsumanimi,
    hetu = oh.hetu,
    syntymäaika = oh.syntymäaika,
    äidinkieli = oh.äidinkieli,
    kansalaisuus = oh.kansalaisuus,
    modified = oh.modified,
    turvakielto = oh.turvakielto,
    sukupuoli = oh.sukupuoli,
    linkitetytOidit = linkitetytOidit,
    vanhatHetut = Nil,
    kotikunta = None
  )
}

class MockOpintopolkuHenkilöFacadeWithDBSupport(val db: DB) extends MockOpintopolkuHenkilöFacade with QueryMethods {
  def findFromDb(oid: String): Option[LaajatOppijaHenkilöTiedot] = {
    runQuery(OpiskeluOikeudetWithAccessCheck(systemUser).filter(_.oppijaOid === oid)).headOption.map { oppijaRow =>
      LaajatOppijaHenkilöTiedot(oid, oid, oid, oid, Some(oid), None, None, None)
    }
  }

  def runQuery[E, U](fullQuery: PostgresDriverWithJsonSupport.api.Query[E, U, Seq]): Seq[U] = {
    runDbSync(fullQuery.result, allowNestedTransactions = true)
  }

  override protected def findHenkilötiedot(id: String): Option[OppijaHenkilöWithMasterInfo] = {
    super.findHenkilötiedot(id)
      .orElse(findFromDb(id).map(OppijaHenkilöWithMasterInfo(_, None)))
      .map(mockYhteystiedot)
  }

  private def mockYhteystiedot(oppijaHenkilö: OppijaHenkilöWithMasterInfo): OppijaHenkilöWithMasterInfo =
    oppijaHenkilö.henkilö match {
      case henkilö: LaajatOppijaHenkilöTiedot => oppijaHenkilö.copy(
        henkilö = henkilö.copy(
          yhteystiedot = List(
            mockYhteystiedot(henkilö),
            mockYhteystiedot(henkilö).copy(
              alkuperä = Koodistokoodiviite("alkupera7", "yhteystietojenalkupera"), // Oikeustulkkirekisteri
              tyyppi = Koodistokoodiviite("yhteystietotyyppi7", "yhteystietotyypit"), // Muu osoite
            ),
            mockYhteystiedot(henkilö).copy(
              alkuperä = Koodistokoodiviite("alkupera3", "yhteystietojenalkupera"), // Omat tiedot UI
              tyyppi = Koodistokoodiviite("yhteystietotyyppi2", "yhteystietotyypit"), // Työosoite
              sähköposti = None,
              puhelinnumero = None,
              matkapuhelinnumero = None
            ),
            mockYhteystiedot(henkilö).copy(
              alkuperä = Koodistokoodiviite("alkupera3", "yhteystietojenalkupera"), // Omat tiedot UI
              tyyppi = Koodistokoodiviite("yhteystietotyyppi2", "yhteystietotyypit"), // Työosoite
              puhelinnumero = Some("09777 888")
            )
          )
        )
      )
      case _ => oppijaHenkilö
    }

  private def mockYhteystiedot(henkilö: LaajatOppijaHenkilöTiedot) = Yhteystiedot(
    alkuperä = Koodistokoodiviite("alkupera1", "yhteystietojenalkupera"), // VTJ
    tyyppi = Koodistokoodiviite("yhteystietotyyppi1", "yhteystietotyypit"), // Kotiosoite
    sähköposti = Some(s"${henkilö.kutsumanimi.toLowerCase()}@gmail.com"),
    puhelinnumero = Some("0401122334"),
    matkapuhelinnumero = Some("0401122334"),
    katuosoite = Some("Esimerkkitie 10"),
    kunta = henkilö.kotikunta match {
      case Some("091") => Some("Helsinki")
      case Some("179") => Some("Jyväskylä")
      case Some("624") => Some("Pyhtää")
      case Some("999") => Some("Ei tiedossa (kunta)")
      case Some(x) => Some(s"Lisää koodi $x mockYhteystiedot-funktioon")
      case _ => None
    },
    postinumero = henkilö.kotikunta.map(_ => "99999").orElse(Some("00000")),
    kaupunki = henkilö.kotikunta,
    maa = Some("  Costa rica  "),
  )
}
