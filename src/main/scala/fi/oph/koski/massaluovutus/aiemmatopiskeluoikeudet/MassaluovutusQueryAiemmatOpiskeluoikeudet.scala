package fi.oph.koski.massaluovutus.aiemmatopiskeluoikeudet

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.KoskiOpiskeluoikeusRowImplicits._
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db._
import fi.oph.koski.henkilo.LaajatOppijaHenkilöTiedot
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.json.SensitiveDataAllowed
import fi.oph.koski.koskiuser.{AccessType, KoskiSpecificSession, Rooli, Session}
import fi.oph.koski.log.AuditLogMessage.AuditLogMessageField
import fi.oph.koski.log.{KoskiAuditLogMessageField, KoskiOperation, Logging, OpiskeluoikeusOiditAuditLog}
import fi.oph.koski.massaluovutus.MassaluovutusUtils.QueryResourceManager
import fi.oph.koski.massaluovutus.organisaationopiskeluoikeudet.MassaluovutusOppija
import fi.oph.koski.massaluovutus.{KoulutuksenjärjestäjienMassaluovutusQueryParameters, MassaluovutusUtils, QueryFormat, QueryResultWriter}
import fi.oph.koski.opiskeluoikeus.OpiskeluoikeusQueryContext
import fi.oph.koski.organisaatio.{MockOrganisaatiot, OrganisaationAlaisetOrganisaatiot}
import fi.oph.koski.schema.Organisaatio
import fi.oph.koski.schema.annotation.EnumValues
import fi.oph.koski.util.Retry.retryWithInterval
import fi.oph.scalaschema.annotation.{Description, Title}

import java.sql.Date
import java.time.LocalDate
import scala.concurrent.duration.DurationInt

@Title("Nykyisten opiskelijoiden aiemmat opiskeluoikeudet")
@Description("Palauttaa oppilaitoksen nykyisten opiskelijoiden muissa organisaatioissa suoritetut esi-, perus-, ammatillisen ja lukiokoulutuksen opiskeluoikeudet, jotka ovat alkaneet ennen opiskelijan opintoja kyselyn oppilaitoksessa.")
@Description("Nykyinen opiskelija on oppija, jolla on kyselyn oppilaitoksessa voimassa oleva esi-, perus-, ammatillisen tai lukiokoulutuksen opiskeluoikeus. Oppilaitoksen omia opiskeluoikeuksia ei palauteta. Kysely tehdään aina yhdelle oppilaitokselle.")
@Description("Kysely edellyttää AIEMMAT_OPISKELUOIKEUDET-käyttöoikeutta kyselyn oppilaitoksessa. Palautettavat koulutusmuodot eivät riipu käyttäjän omista koulutusmuotokohtaisista käyttöoikeuksista, vaan ainoastaan tästä roolista. Arkaluontoiset tiedot palautetaan vain, jos käyttäjällä on niihin oikeus.")
@Description("Tulostiedostot vastaavat organisaationOpiskeluoikeudet-kyselyn json-muotoa: yksi tiedosto oppijaa kohden.")
case class MassaluovutusQueryAiemmatOpiskeluoikeudet(
  @EnumValues(Set("aiemmatOpiskeluoikeudet"))
  `type`: String = "aiemmatOpiskeluoikeudet",
  @EnumValues(Set(QueryFormat.json))
  format: String = QueryFormat.json,
  @Description("Oppilaitoksen oid, jonka nykyisten opiskelijoiden aiemmat opiskeluoikeudet haetaan. Koulutustoimijan tai kunnan oidia ei hyväksytä.")
  organisaatioOid: Organisaatio.Oid,
  @Description("Palautettavat koulutusmuodot. Oletuksena kaikki sallitut: esiopetus, perusopetus, ammatillinenkoulutus, lukiokoulutus.")
  koulutusmuoto: Option[List[String]] = None,
) extends KoulutuksenjärjestäjienMassaluovutusQueryParameters with DatabaseConverters with Logging {
  import MassaluovutusQueryAiemmatOpiskeluoikeudet.sallitutKoulutusmuodot

  override def run(application: KoskiApplication, writer: QueryResultWriter)(implicit user: Session with SensitiveDataAllowed): Either[String, Unit] =
    withKoskiSpecificSession { implicit u =>
      QueryResourceManager(logger) { _ =>
        val db = application.replicaDatabase.db
        val tänään = LocalDate.now()
        val organisaatiot = application.organisaatioService.organisaationAlaisetOrganisaatiot(organisaatioOid)
        val nykyisetOpiskelijat = haeNykyisetOpiskelijat(db, organisaatiot, tänään)
        val masterit = retryWithInterval(5, 5.seconds.toMillis) {
          application.opintopolkuHenkilöFacade.findMasterOppijatWithSlaveOids(nykyisetOpiskelijat.keys.toList)
        }
        val puuttuvat = nykyisetOpiskelijat.keySet -- masterit.keySet
        if (puuttuvat.nonEmpty) {
          logger.warn(s"Oppijanumerorekisteri ei palauttanut ${puuttuvat.size} oppijaa, jätetään pois tuloksista: ${puuttuvat.mkString(", ")}")
        }
        val opintojenAlkuKyselynOppilaitoksessa: Map[String, LocalDate] = masterit.toList
          .groupMap { case (_, master) => master.oid } { case (oid, _) => nykyisetOpiskelijat(oid) }
          .view.mapValues(_.min).toMap
        val henkilöt = masterit.values.toList.distinctBy(_.oid)

        writer.predictFileCount(henkilöt.size)
        henkilöt.foreach { henkilö =>
          val aiemmat = haeAiemmatOpiskeluoikeudet(db, organisaatiot, henkilö, opintojenAlkuKyselynOppilaitoksessa(henkilö.oid))
          if (aiemmat.isEmpty) {
            writer.skipFile()
          } else {
            writer.putJson(henkilö.oid, MassaluovutusOppija(
              henkilö = application.henkilöRepository.oppijaHenkilöToTäydellisetHenkilötiedot(henkilö),
              linkitetytOidit = henkilö.linkitetytOidit,
              opiskeluoikeudet = aiemmat.map(MassaluovutusUtils.toKoskeenTallennettavaOpiskeluoikeus(application, logger)),
            ))
            auditLog(henkilö.oid, aiemmat.map(_.oid))
          }
        }
      }
    }

  override def queryAllowed(application: KoskiApplication)(implicit user: Session): Boolean = withKoskiSpecificSession { u =>
    val onOppilaitos = application.organisaatioRepository.getOrganisaatio(organisaatioOid).exists(_.toOppilaitos.isDefined)
    onOppilaitos && (
      u.hasGlobalReadAccess || (
        u.organisationOids(AccessType.read).contains(organisaatioOid)
          && u.hasRoleInOrganisaatio(Rooli.AIEMMAT_OPISKELUOIKEUDET, organisaatioOid)
      )
    )
  }

  override def fillAndValidate(implicit user: Session): Either[HttpStatus, MassaluovutusQueryAiemmatOpiskeluoikeudet] = {
    val käyttöoikeusMuuhunKuinOppilaitokseen = user.orgKäyttöoikeudet.exists(k => k.organisaatio.oid == organisaatioOid && k.organisaatio.toOppilaitos.isEmpty)
    val tuntemattomat = koulutusmuoto.toList.flatten.filterNot(sallitutKoulutusmuodot.contains)
    if (käyttöoikeusMuuhunKuinOppilaitokseen) {
      Left(KoskiErrorCategory.badRequest.massaluovutus.eiOppilaitos())
    } else if (tuntemattomat.nonEmpty) {
      Left(KoskiErrorCategory.badRequest(s"Tuntematon koulutusmuoto: ${tuntemattomat.mkString(", ")}. Sallitut arvot: ${sallitutKoulutusmuodot.mkString(", ")}"))
    } else {
      Right(copy(koulutusmuoto = koulutusmuoto.orElse(Some(sallitutKoulutusmuodot))))
    }
  }

  private def haeNykyisetOpiskelijat(db: DB, organisaatiot: OrganisaationAlaisetOrganisaatiot, tänään: LocalDate)(implicit user: KoskiSpecificSession): Map[String, LocalDate] = {
    val koulutusmuodot = sallitutKoulutusmuodot.intersect(user.allowedOpiskeluoikeudetJaPäätasonSuoritukset.toOpiskeluoikeudenTyypit.toList)
    QueryMethods.runDbSync(db, SQLHelpers.concatMany(
      Some(sql"""
        SELECT oppija_oid, min(alkamispaiva)
        FROM opiskeluoikeus
        WHERE NOT poistettu AND NOT mitatoity
          AND koulutusmuoto = ANY($koulutusmuodot)
          AND alkamispaiva <= $tänään
          AND (paattymispaiva IS NULL OR paattymispaiva > $tänään)
          AND
      """),
      Some(MassaluovutusUtils.organisaatioFilter(organisaatiot)),
      Some(sql" GROUP BY oppija_oid"),
    ).as[(String, Date)])
      .map { case (oid, alkamispäivä) => oid -> alkamispäivä.toLocalDate }
      .toMap
  }

  private def haeAiemmatOpiskeluoikeudet(db: DB, organisaatiot: OrganisaationAlaisetOrganisaatiot, henkilö: LaajatOppijaHenkilöTiedot, alkanutEnnen: LocalDate): Seq[KoskiOpiskeluoikeusRow] = {
    // Tarkoituksella ei rajata käyttäjän omilla koulutusmuoto-oikeuksilla (toisin kuin nykyisten opiskelijoiden haussa):
    // esim. lukio tarvitsee opiskelijoidensa perusopetuksen tiedot, vaikka sillä ei ole perusopetuksen käyttöoikeutta.
    val koulutusmuodot: List[String] = koulutusmuoto.toList.flatten
    QueryMethods.runDbSync(db, SQLHelpers.concatMany(
      Some(sql"""
        SELECT *
        FROM opiskeluoikeus
        WHERE NOT poistettu AND NOT mitatoity
          AND oppija_oid = ANY(${henkilö.kaikkiOidit})
          AND koulutusmuoto = ANY($koulutusmuodot)
          AND alkamispaiva < $alkanutEnnen
          AND NOT
      """),
      Some(MassaluovutusUtils.organisaatioFilter(organisaatiot)),
      Some(sql" ORDER BY alkamispaiva"),
    ).as[KoskiOpiskeluoikeusRow])
  }

  private def auditLog(oppijaOid: String, opiskeluoikeusOidit: Seq[String])(implicit user: KoskiSpecificSession): Unit =
    OpiskeluoikeusOiditAuditLog.log(
      KoskiOperation.AIEMMAT_OPISKELUOIKEUDET_KATSOMINEN,
      oppijaOid,
      opiskeluoikeusOidit,
      Map[AuditLogMessageField, String](KoskiAuditLogMessageField.hakuEhto -> OpiskeluoikeusQueryContext.queryForAuditLog(Map(
        "organisaatio" -> List(organisaatioOid),
        "koulutusmuoto" -> koulutusmuoto.toList.flatten,
      ))),
    )
}

object MassaluovutusQueryAiemmatOpiskeluoikeudet {
  val sallitutKoulutusmuodot: List[String] = List("esiopetus", "perusopetus", "ammatillinenkoulutus", "lukiokoulutus")
}

object MassaluovutusQueryAiemmatOpiskeluoikeudetDocumentation {
  def example: MassaluovutusQueryAiemmatOpiskeluoikeudet = MassaluovutusQueryAiemmatOpiskeluoikeudet(
    organisaatioOid = MockOrganisaatiot.stadinAmmattiopisto,
    koulutusmuoto = Some(List("perusopetus", "lukiokoulutus")),
  )
}
