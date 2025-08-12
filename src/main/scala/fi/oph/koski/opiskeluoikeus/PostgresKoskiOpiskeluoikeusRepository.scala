package fi.oph.koski.opiskeluoikeus

import fi.oph.koski.db.KoskiTables._
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db._
import fi.oph.koski.util.Timing
import fi.oph.koski.henkilo._
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.schema._
import slick.dbio.DBIOAction.sequence
import slick.dbio.{DBIOAction, NoStream}
import slick.jdbc.GetResult
import slick.lifted.Query

import java.sql.Timestamp
import java.time.LocalDate

class PostgresKoskiOpiskeluoikeusRepository(
  val db: DB,
  actions: PostgresKoskiOpiskeluoikeusRepositoryActions
) extends KoskiOpiskeluoikeusRepository with DatabaseExecutionContext with QueryMethods with Logging with Timing {

  override def filterOppijat[A <: HenkilönTunnisteet](oppijat: List[A])(implicit user: KoskiSpecificSession): List[A] = {
    val queryOppijaOids = sequence(oppijat.map { o =>
      actions.findByOppijaOidsAction(o.oid :: o.linkitetytOidit).map(opiskeluoikeusOids => (o.oid, opiskeluoikeusOids))
    })

    val oppijatJoillaOpiskeluoikeuksia: Set[Oid] = runDbSync(queryOppijaOids)
      .collect { case (oppija, opiskeluoikeudet) if opiskeluoikeudet.nonEmpty => oppija }
      .toSet

    oppijat.filter { oppija => oppijatJoillaOpiskeluoikeuksia.contains(oppija.oid)}
  }

  override def findByOppijaOids(oids: List[String])(implicit user: KoskiSpecificSession): Seq[Opiskeluoikeus] = {
    actions.findByOppijaOids(oids)
  }

  override def findByCurrentUserOids(oids: List[String])(implicit user: KoskiSpecificSession): Seq[Opiskeluoikeus] = {
    assert(oids.contains(user.oid), "Käyttäjän oid: " + user.oid + " ei löydy etsittävän oppijan oideista: " + oids)
    findKansalaisenOpiskeluoikeudet(oids)
  }

  override def findHuollettavaByOppijaOids(oids: List[String])(implicit user: KoskiSpecificSession): Seq[Opiskeluoikeus] = {
    assert(oids.exists(user.isUsersHuollettava), "Käyttäjän oid: " + user.oid + " ei löydy etsittävän oppijan oideista: " + oids)
    findKansalaisenOpiskeluoikeudet(oids)
  }

  private def findKansalaisenOpiskeluoikeudet(oids: List[String])(implicit user: KoskiSpecificSession) = {
    val query = KoskiOpiskeluOikeudet
      .filterNot(_.mitätöity)
      .filter(_.oppijaOid inSetBind oids)

    runDbSync(query.result.map(rows => rows.sortBy(_.id).map(_.toOpiskeluoikeusUnsafe)))
  }

  override def findByOid(oid: String)(implicit user: KoskiSpecificSession, investigate: Boolean): Either[HttpStatus, KoskiOpiskeluoikeusRow] = {
    val oidCheck = timed(s"${if (investigate) "get-oo-by-oid-investigation:" else ""}withOidCheck[KoskiOpiskeluoikeusRow](oid)", thresholdMs = 0) {
      withOidCheck[KoskiOpiskeluoikeusRow](oid) _
    }
    oidCheck {
      val query = timed(s"${if (investigate) "get-oo-by-oid-investigation:" else ""}findByOid.query", thresholdMs = 0) {
        KoskiOpiskeluOikeudetWithAccessCheck.filter(_.oid === oid)
      }
      val result = timed(s"${if (investigate) "get-oo-by-oid-investigation:" else ""}findByOid.result", thresholdMs = 0) {
        withExistenceCheck(runDbSync(query.result))
      }

      result
    }
  }

  override def findByOidIlmanKäyttöoikeustarkistusta(oid: String): Either[HttpStatus, KoskiOpiskeluoikeusRow] = withOidCheck(oid) {
    withExistenceCheck(runDbSync(KoskiOpiskeluOikeudet.filter(_.oid === oid).result))
  }

  override def getOppijaOidsForOpiskeluoikeus(opiskeluoikeusOid: String)(implicit user: KoskiSpecificSession): Either[HttpStatus, List[Oid]] = withOidCheck(opiskeluoikeusOid) {
    withExistenceCheck(runDbSync(KoskiOpiskeluOikeudetWithAccessCheck
      .filter(_.oid === opiskeluoikeusOid)
      .flatMap(row => Henkilöt.filter(_.oid === row.oppijaOid))
      .result)).map(henkilö => henkilö.oid :: henkilö.masterOid.toList)
  }

  override def getMasterOppijaOidForOpiskeluoikeus(opiskeluoikeusOid: String)(implicit user: KoskiSpecificSession): Either[HttpStatus, Oid] = withOidCheck(opiskeluoikeusOid) {
    withExistenceCheck(runDbSync(KoskiOpiskeluOikeudetWithAccessCheck
      .filter(_.oid === opiskeluoikeusOid)
      .flatMap(row => Henkilöt.filter(_.oid === row.oppijaOid))
      .result)).map(henkilö => henkilö.masterOid.getOrElse(henkilö.oid))
  }

  private def withExistenceCheck[T](things: Iterable[T]): Either[HttpStatus, T] = things.headOption.toRight(KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia())

  private def withOidCheck[T](oid: String)(f: => Either[HttpStatus, T]) = {
    OpiskeluoikeusOid.validateOpiskeluoikeusOid(oid).right.flatMap(_ => f)
  }

  override def createOrUpdate(
    oppijaOid: PossiblyUnverifiedHenkilöOid,
    opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus,
    allowUpdate: Boolean,
    allowDeleteCompleted: Boolean = false,
    skipValidations: Boolean = false,
  )(implicit user: KoskiSpecificSession): Either[HttpStatus, CreateOrUpdateResult] = {
    actions.createOrUpdate(oppijaOid, opiskeluoikeus, allowUpdate, allowDeleteCompleted, skipValidations)
  }

  def merkitseSuoritusjakoTehdyksiIlmanKäyttöoikeudenTarkastusta(oid: String): HttpStatus = {
    // Tarkastetaan ensin, tarvitseeko päivitystä edes tehdä, jotta vältetään turha taulun lukitseminen runDbSyncWithoutAikaleimaUpdate-kutsussa
    if (!suoritusjakoTehtyIlmanKäyttöoikeudenTarkastusta(oid)) {
      runDbSyncWithoutAikaleimaUpdate(KoskiTables.KoskiOpiskeluOikeudet.filter(_.oid === oid).map(_.suoritusjakoTehty).update(true)) match {
        case 0 => throw new RuntimeException(s"Oppija not found: $oid")
        case _ => HttpStatus.ok
      }
    } else {
      HttpStatus.ok
    }
  }

  def suoritusjakoTehtyIlmanKäyttöoikeudenTarkastusta(oid: String): Boolean = {
    runDbSync(KoskiTables.KoskiOpiskeluOikeudet.filter(rivi => rivi.oid === oid && rivi.suoritusjakoTehty === true).result).nonEmpty
  }

  override def isKuoriOpiskeluoikeus(opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): Boolean = {
    if (opiskeluoikeus.oid.isDefined && opiskeluoikeus.oppilaitos.isDefined) {
      val ooid = opiskeluoikeus.oid.get
      val oppijaOids = getOppijaOidsForOpiskeluoikeus(ooid)(KoskiSpecificSession.systemUser).right.getOrElse(List())

      findByOppijaOids(oppijaOids)(KoskiSpecificSession.systemUser)
        .exists(_.sisältyyOpiskeluoikeuteen.exists(_.oid == ooid))
    } else {
      false
    }
  }

  def getPerusopetuksenAikavälitIlmanKäyttöoikeustarkistusta(tallennettavaOpiskeluoikeus: Option[KoskeenTallennettavaOpiskeluoikeus], oppijaOid: String): Seq[Päivämääräväli] = {
    // HUOMIOI, JOS TÄTÄ MUUTAT: Pitää olla synkassa Oppivelvollisuustiedot.scala:n createPrecomputedTable-metodissa
    // raportointikantaan tehtävän tarkistuksen kanssa. Muuten Valppaan maksuttomuushaku menee rikki.

    // Ota huomioon myös opiskeluoikeus, jota ollaan lisäämässä, koska sekin saattaa olla esim. oppivelvollisuuden edellisen lain
    // aikana päättänyt.
    val opiskeluoikeudesta = tallennettavaOpiskeluoikeus.toSeq.flatMap(oo =>
      if (!oo.mitätöity && oo.alkamispäivä.isDefined) {
        val alkamispäivä = oo.alkamispäivä.get
        oo.suoritukset.collect {
          case s: AikuistenPerusopetuksenOppimääränSuoritus => s
          case s: PerusopetuksenOppimääränSuoritus => s
          case s: InternationalSchoolVuosiluokanSuoritus if s.koulutusmoduuli.tunniste.koodiarvo == "9" => s
          case s: SecondaryLowerVuosiluokanSuoritus if s.koulutusmoduuli.tunniste.koodiarvo == "S4" => s
        }.map(s =>
          Päivämääräväli(alkamispäivä, oo.päättymispäivä, s.vahvistus.map(_.päivä))
        )
      } else {
        Seq.empty
      }
    )

    val tallennettavaOid = tallennettavaOpiskeluoikeus.flatMap(_.oid)
    val tallennettavaLähdejärjestelmänKoodiarvo = tallennettavaOpiskeluoikeus.flatMap(_.lähdejärjestelmänId).map(_.lähdejärjestelmä.koodiarvo)
    val tallennettavaLähdejärjestelmänId = tallennettavaOpiskeluoikeus.flatMap(_.lähdejärjestelmänId).flatMap(_.id)

    val aiemmistaOpiskeluoikeuksista = runDbSync(
      sql"""
        with master as (
          select case when master_oid is not null then master_oid else oid end as oid
          from henkilo
          where oid = $oppijaOid
        ), linkitetyt as (
          select oid as oids
          from henkilo
          where henkilo.oid = (select oid from master) or henkilo.master_oid = (select oid from master)
        )
        select
          alkamispaiva,
          paattymispaiva,
          ((suoritukset -> 'vahvistus' ->> 'päivä')::date) as vahvistuspaiva
        from opiskeluoikeus
        cross join jsonb_array_elements(data -> 'suoritukset') suoritukset
        where not opiskeluoikeus.mitatoity
          and (not (
                    ($tallennettavaOid is not null)
                    and (opiskeluoikeus.oid = $tallennettavaOid)
                   )
              )
          and (not (
                    ($tallennettavaLähdejärjestelmänId is not null)
                    and (opiskeluoikeus.data -> 'lähdejärjestelmänId' ->> 'id' is not null)
                    and (opiskeluoikeus.data -> 'lähdejärjestelmänId' ->> 'id' = $tallennettavaLähdejärjestelmänId)
                    and (opiskeluoikeus.data -> 'lähdejärjestelmänId' -> 'lähdejärjestelmä' ->> 'koodiarvo' = $tallennettavaLähdejärjestelmänKoodiarvo)
                   )
              )
          and (suoritukset -> 'tyyppi' ->> 'koodiarvo' = 'perusopetuksenoppimaara'
            or suoritukset -> 'tyyppi' ->> 'koodiarvo' = 'aikuistenperusopetuksenoppimaara'
            or (suoritukset -> 'tyyppi' ->> 'koodiarvo' = 'internationalschoolmypvuosiluokka'
              and suoritukset -> 'koulutusmoduuli' -> 'tunniste' ->> 'koodiarvo' = '9')
            or (suoritukset -> 'tyyppi' ->> 'koodiarvo' = 'europeanschoolofhelsinkivuosiluokkasecondarylower'
              and suoritukset -> 'koulutusmoduuli' -> 'tunniste' ->> 'koodiarvo' = 'S4')
          )
          and oppija_oid = any(select oids from linkitetyt)
      """.as[Päivämääräväli])

    opiskeluoikeudesta ++ aiemmistaOpiskeluoikeuksista
  }

  private implicit def getPäivämääräväli: GetResult[Päivämääräväli] = GetResult(r => {
    Päivämääräväli(
      alku = r.getLocalDate("alkamispaiva"),
      päättymispäivä = r.getLocalDateOption("paattymispaiva"),
      vahvistuspäivä = r.getLocalDateOption("vahvistuspaiva"),
    )
  })

  // TODO: Tässä logiikassa on bugi: Muut opiskeluoikeudet pitäisi olla mahdollista tunnistaa myös muulla keinoin
  // kuin oidilla (eli myös lähdejärjestelmän id:llä, aikaisemmin myös oppilaitos yms. tyypeillä)
  def getLukionMuidenOpiskeluoikeuksienAlkamisajatIlmanKäyttöoikeustarkistusta(
    oppijaOid: String,
    muutettavanOpiskeluoikeudenOid: Option[String]
  ) : Seq[LocalDate] =
  {
    runDbSync(
      sql"""
        with master as (
          select case when master_oid is not null then master_oid else oid end as oid
          from henkilo
          where oid = $oppijaOid
        ), linkitetyt as (
          select oid as oids
          from henkilo
          where henkilo.oid = (select oid from master) or henkilo.master_oid = (select oid from master)
        )
        select
          alkamispaiva as paiva
        from opiskeluoikeus
        where not opiskeluoikeus.mitatoity
          and ($muutettavanOpiskeluoikeudenOid is null or opiskeluoikeus.oid <> $muutettavanOpiskeluoikeudenOid)
          and opiskeluoikeus.koulutusmuoto = 'lukiokoulutus'
          and oppija_oid = any(select oids from linkitetyt)
      """.as[LocalDate])
  }

  def getKoulutusmuodonAlkamisajatIlmanKäyttöoikeustarkistusta(
    oppijaOid: String,
    koulutusmuoto: String
  ): Map[Opiskeluoikeus.Oid, LocalDate] =
    runDbSync(sql"""
      SELECT oid, alkamispaiva
      FROM opiskeluoikeus
      WHERE oppija_oid = $oppijaOid
        AND koulutusmuoto = $koulutusmuoto
        AND mitatoity IS NOT TRUE
        AND poistettu IS NOT TRUE
  """.as[(String, Timestamp)])
      .toMap
      .mapValues(_.toLocalDateTime.toLocalDate)

  private implicit def getLocalDate: GetResult[LocalDate] = GetResult(r => {
    r.getLocalDate("paiva")
  })

  private def runDbSyncWithoutAikaleimaUpdate[R](updateRows: DBIOAction[R, NoStream, Nothing]) = {
    val action = for {
      _       <- disableAikaleimaTrigger
      result  <- updateRows
      _       <- enableAikaleimaTrigger
    } yield result

    runDbSync(action.transactionally)
  }

  private def disableAikaleimaTrigger = sqlu"""alter table opiskeluoikeus disable trigger update_opiskeluoikeus_aikaleima"""
  private def enableAikaleimaTrigger = sqlu"""alter table opiskeluoikeus enable trigger update_opiskeluoikeus_aikaleima"""
}

case class Päivämääräväli(
  alku: LocalDate,
  päättymispäivä: Option[LocalDate],
  vahvistuspäivä: Option[LocalDate],
)
