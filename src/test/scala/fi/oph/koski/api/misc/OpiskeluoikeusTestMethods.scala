package fi.oph.koski.api.misc

import fi.oph.koski.KoskiApplicationForTests
import fi.oph.koski.http.{HttpSpecification, HttpStatus}
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koskiuser.MockUsers.paakayttajaMitatoidytOpiskeluoikeudet
import fi.oph.koski.koskiuser.{MockUsers, UserWithPassword}
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.schema.KoskiSchema.strictDeserialization
import fi.oph.koski.schema._
import fi.oph.scalaschema.{ExtractionContext, SchemaValidatingExtractor}
import org.json4s.jackson.JsonMethods
import org.scalatest.matchers.should.Matchers

trait OpiskeluoikeusTestMethods extends HttpSpecification with SearchTestMethods with Matchers {
  private implicit val context: ExtractionContext = strictDeserialization

  def lastOpiskeluoikeusByHetu(oppija: Henkilö, user: UserWithPassword = defaultUser): KoskeenTallennettavaOpiskeluoikeus = {
    val hetu = oppija match {
      case u: UusiHenkilö => u.hetu
      case h: Henkilötiedot => h.hetu.get
      case _ => ???
    }
    oppijaByHetu(hetu, user).tallennettavatOpiskeluoikeudet.last
  }

  def lastOpiskeluoikeus(oppijaOid: String, user: UserWithPassword = defaultUser): KoskeenTallennettavaOpiskeluoikeus = {
    oppija(oppijaOid, user).tallennettavatOpiskeluoikeudet.last
  }

  def getOpiskeluoikeudet(oppijaOid: String, user: UserWithPassword = defaultUser): Seq[Opiskeluoikeus] = {
    tryOppija(oppijaOid, user) match {
      case Right(oppija) => oppija.opiskeluoikeudet
      case Left(HttpStatus(404, _)) => Nil
      case Left(status) => throw new RuntimeException(status.toString)
    }
  }

  def getOpiskeluoikeus(oppijaOid: String, tyyppi: String) = {
    getOpiskeluoikeudet(oppijaOid).find(_.tyyppi.koodiarvo == tyyppi).get
  }

  def getOpiskeluoikeus(opiskeluoikeusOid: String, user: UserWithPassword = defaultUser): Opiskeluoikeus = {
    authGet("api/opiskeluoikeus/" + opiskeluoikeusOid, user) {
      verifyResponseStatusOk()
      readOpiskeluoikeus
    }
  }

  def oppija(oppijaOid: String, user: UserWithPassword = defaultUser): Oppija = {
    authGet("api/oppija/" + oppijaOid, user) {
      verifyResponseStatusOk()
      readOppija
    }
  }

  def oppijaByHetu(hetu: String, user: UserWithPassword = defaultUser): Oppija = {
    post("api/henkilo/hetu", JsonSerializer.writeWithRoot(Map("hetu" -> hetu)), headers = authHeaders(user) ++ jsonContent) {
      verifyResponseStatusOk()
      val oid = JsonSerializer.parse[List[HenkilötiedotJaOid]](body).head.oid
      oppija(oid, user)
    }
  }

  def tryOppija(oppijaOid: String, user: UserWithPassword = defaultUser): Either[HttpStatus, Oppija] = {
    authGet("api/oppija/" + oppijaOid, user) {
      response.status match {
        case 200 => Right(readOppija)
        case status => Left(HttpStatus(status, Nil))
      }
    }
  }

  def readOppija = {
    SchemaValidatingExtractor.extract[Oppija](JsonMethods.parse(body))
      .left.map(e => throw new RuntimeException(e.mkString("\n")))
      .right.get
  }

  def readOpiskeluoikeus = {
    SchemaValidatingExtractor.extract[Opiskeluoikeus](JsonMethods.parse(body)).right.get
  }

  def poistaOppijanOpiskeluoikeusDatat(henkilö: Henkilö): Unit = {
    val user = MockUsers.paakayttaja

    val henkilöOidit: Seq[Henkilö.Oid] = henkilö match {
      case h: HenkilöWithOid => List(h.oid)
      case uh: UusiHenkilö =>
        searchForHenkilötiedot(uh.hetu, user).map(_.oid)
    }

    if (!henkilöOidit.isEmpty) {
      clearOppijanOpiskeluoikeudet(henkilöOidit.head)
    }
  }

  def mitätöiOppijanKaikkiOpiskeluoikeudet(henkilö: Henkilö): Unit = {
    val user = MockUsers.paakayttaja

    val henkilöOidit: Seq[Henkilö.Oid] = henkilö match {
      case h: HenkilöWithOid => List(h.oid)
      case uh: UusiHenkilö =>
        searchForHenkilötiedot(uh.hetu, user).map(_.oid)
    }

    if (!henkilöOidit.isEmpty) {
      getOpiskeluoikeudet(henkilöOidit.head, user)
        .flatMap(_.oid.toList)
        .map(oid =>
          delete(s"api/opiskeluoikeus/${oid}", headers = authHeaders(user)) {
            verifyResponseStatusOk()
          }
        )
      KoskiApplicationForTests.perustiedotIndexer.sync(refresh = true)
    }
  }

  lazy val linkitettyOid: Map[Oid, Oid] = (for {
    oppija <- KoskiApplicationForTests.fixtureCreator.defaultOppijat
    masterOid <- oppija.master.map(_.oid)
  } yield masterOid -> oppija.henkilö.oid).toMap

  lazy val masterHenkilöt = KoskiApplicationForTests.fixtureCreator.defaultOppijat.filterNot(_.master.isDefined).map(_.henkilö).sortBy(_.oid)

  lazy val koskeenTallennetutOppijat: List[Oppija] = masterHenkilöt.flatMap { m =>
    tryOppija(m.oid, paakayttajaMitatoidytOpiskeluoikeudet) match {
      case Right(o@Oppija(h: TäydellisetHenkilötiedot, opiskeluoikeudet)) => List(o)
      case _ => Nil
    }
  }

  lazy val koskeenTallennetutOpiskeluoikeudet: List[Opiskeluoikeus] = for {
    opiskeluoikeus <- koskeenTallennetutOppijat.flatMap(_.opiskeluoikeudet)
    if !opiskeluoikeus.mitätöity && opiskeluoikeus.oid.isDefined
  } yield opiskeluoikeus
}
