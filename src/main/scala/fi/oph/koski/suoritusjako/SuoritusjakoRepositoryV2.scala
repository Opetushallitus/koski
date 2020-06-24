package fi.oph.koski.suoritusjako


import java.sql.{Date, Timestamp}
import java.time.{Instant, LocalDate}

import fi.oph.koski.db.Tables.SuoritusJakoV2
import fi.oph.koski.db.KoskiDatabase.DB
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api._
import fi.oph.koski.db.{DatabaseExecutionContext, KoskiDatabaseMethods, SuoritusjakoRowV2}
import fi.oph.koski.http.{HttpStatus, KoskiErrorCategory}
import fi.oph.koski.koskiuser.KoskiSession
import fi.oph.koski.log.Logging
import fi.oph.koski.schema.{KoskiSchema, Opiskeluoikeus}
import fi.oph.scalaschema._
import org.json4s.JsonAST.JValue

class SuoritusjakoRepositoryV2(val db: DB) extends Logging with DatabaseExecutionContext with KoskiDatabaseMethods {
  type OppijaOid = String

  val  MAX_SUORITUSJAKO_COUNT = 20
  def SUORITUSJAON_DEFAULT_VOIMASSAOLOAIKA = Date.valueOf(LocalDate.now.plusMonths(6))

  def createSuoritusjako(opiskeluoikeudet: List[Opiskeluoikeus])(implicit user: KoskiSession): HttpStatus = {
    if (suoritusjakoCount(user.oid) < MAX_SUORITUSJAKO_COUNT)  {
      httpStatus(runDbSync(SuoritusJakoV2 += SuoritusjakoRowV2(
        secret = SuoritusjakoSecret.generateNew,
        oppijaOid = user.oid,
        data = SuoritusjakoJsonMethods.serialize(opiskeluoikeudet),
        voimassaAsti = SUORITUSJAON_DEFAULT_VOIMASSAOLOAIKA,
        aikaleima = Timestamp.from(Instant.now))))
    } else {
      KoskiErrorCategory.unprocessableEntity.liianMontaSuoritusjakoa()
    }
  }

  def findBySecret(secret: String): Either[HttpStatus, (OppijaOid, List[Opiskeluoikeus])] = {
    runDbSync(VoimassaOlevatSuoritusJaotV2.filter(_.secret === secret).result.headOption)
      .toRight(KoskiErrorCategory.notFound())
      .flatMap(row => SuoritusjakoJsonMethods.extractOpiskeluoikeudet(row.data).map(opiskeluoikeudet => (row.oppijaOid, opiskeluoikeudet)))
  }

  def listActivesByOppijaOid(oppijaOid: String): Seq[Suoritusjako] = {
    runDbSync(VoimassaOlevatSuoritusJaotV2.filter(_.oppijaOid === oppijaOid).result)
      .map(r => Suoritusjako(r.secret, r.voimassaAsti.toLocalDate, r.aikaleima))
  }

  def updateExpirationDate(oppijaOid: String, secret: String, voimassaAsti: LocalDate): HttpStatus = {
    httpStatus(runDbSync(VoimassaOlevatSuoritusJaotV2
      .filter(_.oppijaOid === oppijaOid)
      .filter(_.secret === secret)
      .map(_.voimassaAsti)
      .update(Date.valueOf(voimassaAsti))))
  }

  def deleteSuoritusjako(oppijaOid: String, secret: String): HttpStatus = {
    httpStatus(runDbSync(SuoritusJakoV2
      .filter(_.oppijaOid === oppijaOid)
      .filter(_.secret === secret)
      .delete))
  }

  private def suoritusjakoCount(oppijaOid: String) = {
    runDbSync(SuoritusJakoV2.filter(_.oppijaOid === oppijaOid).length.result)
  }

  private def VoimassaOlevatSuoritusJaotV2 = {
    SuoritusJakoV2.filter(_.voimassaAsti >= Date.valueOf(LocalDate.now))
  }

  private def httpStatus(result: Int) = {
    if (result == 1) HttpStatus.ok else KoskiErrorCategory.badRequest()
  }
}

object SuoritusjakoJsonMethods {
  private def skipSyntheticProperties(s: ClassSchema, p: Property) = if (p.synthetic) Nil else List(p)

  private val serializationContext = SerializationContext(KoskiSchema.schemaFactory, skipSyntheticProperties)
  private implicit val deserializationContext = ExtractionContext(KoskiSchema.schemaFactory).copy(validate = false)

  def serialize(opiskeluoikeudet: List[Opiskeluoikeus]): JValue =
    Serializer.serialize(opiskeluoikeudet, serializationContext)

  def extractOpiskeluoikeudet(opiskeluoikeudet: JValue): Either[HttpStatus, List[Opiskeluoikeus]] =
    SchemaValidatingExtractor.extract[List[Opiskeluoikeus]](opiskeluoikeudet)
      .left.map(_ => KoskiErrorCategory.internalError())
}
