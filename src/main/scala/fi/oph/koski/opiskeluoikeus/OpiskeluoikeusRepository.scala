package fi.oph.koski.opiskeluoikeus

import java.time.LocalDate

import fi.oph.koski.db.OpiskeluOikeusRow
import fi.oph.koski.henkilo.PossiblyUnverifiedHenkilöOid
import fi.oph.koski.http.HttpStatus
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.schema.Henkilö.Oid
import fi.oph.koski.schema._
import org.json4s.JValue
import rx.lang.scala.Observable

trait OpiskeluOikeusRepository extends AuxiliaryOpiskeluOikeusRepository {
  def streamingQuery(filters: List[QueryFilter])(implicit user: KoskiSession): Observable[(Oid, List[OpiskeluOikeusRow])]

  def findById(id: Int)(implicit user: KoskiSession): Option[OpiskeluOikeusRow]
  def delete(id: Int)(implicit user: KoskiSession): HttpStatus
  def createOrUpdate(oppijaOid: PossiblyUnverifiedHenkilöOid, opiskeluOikeus: KoskeenTallennettavaOpiskeluoikeus)(implicit user: KoskiSession): Either[HttpStatus, CreateOrUpdateResult]
  def filterOppijat(oppijat: Seq[HenkilötiedotJaOid])(implicit user: KoskiSession): Seq[HenkilötiedotJaOid]
  def findByOppijaOid(oid: String)(implicit user: KoskiSession): Seq[Opiskeluoikeus]
  def findByUserOid(oid: String)(implicit user: KoskiSession): Seq[Opiskeluoikeus]
}

trait AuxiliaryOpiskeluOikeusRepository {
  def filterOppijat(oppijat: Seq[HenkilötiedotJaOid])(implicit user: KoskiSession): Seq[HenkilötiedotJaOid]
  def findByOppijaOid(oid: String)(implicit user: KoskiSession): Seq[Opiskeluoikeus]
}

sealed trait CreateOrUpdateResult {
  def id: Opiskeluoikeus.Id
  def versionumero: Int
  def diff: JValue
}

case class Created(id: Opiskeluoikeus.Id, versionumero: Opiskeluoikeus.Versionumero, diff: JValue) extends CreateOrUpdateResult
case class Updated(id: Opiskeluoikeus.Id, versionumero: Opiskeluoikeus.Versionumero, diff: JValue) extends CreateOrUpdateResult
case class NotChanged(id: Opiskeluoikeus.Id, versionumero: Opiskeluoikeus.Versionumero, diff: JValue) extends CreateOrUpdateResult

trait QueryFilter

case class FilterCriterion(field: String, value: String)

case class OpiskeluoikeusPäättynytAikaisintaan(päivä: LocalDate) extends QueryFilter
case class OpiskeluoikeusPäättynytViimeistään(päivä: LocalDate) extends QueryFilter
case class TutkinnonTila(tila: Koodistokoodiviite) extends QueryFilter
case class Nimihaku(hakusana: String) extends QueryFilter
case class OpiskeluoikeudenTyyppi(tyyppi: Koodistokoodiviite) extends QueryFilter
case class SuorituksenTyyppi(tyyppi: Koodistokoodiviite) extends QueryFilter
case class KoulutusmoduulinTunniste(tunniste: List[Koodistokoodiviite]) extends QueryFilter
case class Osaamisala(osaamisala: List[Koodistokoodiviite]) extends QueryFilter
case class Tutkintonimike(nimike: List[Koodistokoodiviite]) extends QueryFilter
case class OpiskeluoikeudenTila(tila: Koodistokoodiviite) extends QueryFilter
case class Toimipiste(toimipiste: List[OrganisaatioWithOid]) extends QueryFilter
case class Luokkahaku(hakusana: String) extends QueryFilter


trait SortCriterion {
  def field: String
}
case class Ascending(field: String) extends SortCriterion
case class Descending(field: String) extends SortCriterion

