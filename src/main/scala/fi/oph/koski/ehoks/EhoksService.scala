package fi.oph.koski.ehoks

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.db.KoskiTables
import fi.oph.koski.db.PostgresDriverWithJsonSupport.plainAPI._
import fi.oph.koski.db.{KoskiOpiskeluoikeusRow, QueryMethods}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSpecificSession
import fi.oph.koski.schema.KoskiSchema
import fi.oph.koski.suoritusjako.common.OpiskeluoikeusFacade

import java.sql.Timestamp

class EhoksService(application: KoskiApplication) extends QueryMethods {
  protected val db = application.replicaDatabase.db

  private val opiskeluoikeusFacade = new OpiskeluoikeusFacade[EhoksOpiskeluoikeus](application, None, None)

  def findOppija(oppijaOid: String)(implicit user: KoskiSpecificSession): Either[HttpStatus, EhoksOppija] =
    opiskeluoikeusFacade
      .haeOpiskeluoikeudet(oppijaOid, EhoksSchema.palautettavatOpiskeluoikeudenTyypit)
      .map(raw => EhoksOppija(EhoksHenkilö(raw.henkilö.oid), raw.opiskeluoikeudet.toList))

  def findOpiskeluoikeus(opiskeluoikeusOid: String)(implicit user: KoskiSpecificSession): Either[HttpStatus, (String, EhoksOpiskeluoikeus)] =
    application.opiskeluoikeusRepository.findByOid(opiskeluoikeusOid)
      .filterOrElse(
        row => !row.mitätöity && EhoksSchema.palautettavatOpiskeluoikeudenTyypit.contains(row.koulutusmuoto),
        KoskiErrorCategory.notFound.opiskeluoikeuttaEiLöydyTaiEiOikeuksia()
      )
      .flatMap(row => deserialize(row).map(oo => (row.oppijaOid, oo)))

  def mitätöidytOpiskeluoikeudet(alku: Timestamp, loppu: Timestamp): Seq[String] =
    runDbSync(sql"""
      select oid from opiskeluoikeus
      where mitatoity
        and koulutusmuoto = any(${EhoksSchema.palautettavatOpiskeluoikeudenTyypit})
        and aikaleima >= $alku and aikaleima < $loppu
      union
      select oid from poistettu_opiskeluoikeus
      where koulutusmuoto = any(${EhoksSchema.palautettavatOpiskeluoikeudenTyypit})
        and mitatoity_aikaleima >= $alku and mitatoity_aikaleima < $loppu
      order by oid
    """.as[String])

  private def deserialize(row: KoskiOpiskeluoikeusRow): Either[HttpStatus, EhoksOpiskeluoikeus] =
    application.validatingAndResolvingExtractor.extract[EhoksOpiskeluoikeus](
      KoskiSchema.lenientDeserializationWithIgnoringNonValidatingListItemsWithoutValidation
    )(KoskiTables.KoskiOpiskeluoikeusTable.readAsJValue(row.data, row.oid, row.versionumero, row.aikaleima))
}
