package fi.oph.koski.henkilo

import com.typesafe.config.Config
import fi.oph.koski.db.PostgresDriverWithJsonSupport.api.actionBasedSQLInterpolation
import fi.oph.koski.raportointikanta.{RKotikuntahistoriaRow, Schema}
import fi.oph.koski.util.Optional.coalesce
import slick.dbio.{DBIO, DBIOAction, Effect, NoStream}

import java.sql.Date
import java.time.LocalDate


object Kotikuntahistoria {
  def createIndexes(s: Schema): DBIOAction[Unit, NoStream, Effect] =
    DBIO.seq(
      sqlu"CREATE INDEX ON #${s.name}.r_kotikuntahistoria(master_oid text_ops,muutto_pvm date_ops)",
      sqlu"CREATE INDEX ON #${s.name}.r_kotikuntahistoria(master_oid text_ops,muutto_pvm date_ops,kotikunta text_ops)"
    )
}

case class OppijanumerorekisteriKotikuntahistoriaRow(
  oid: String,
  kotikunta: String,
  kuntaanMuuttopv: Option[LocalDate],
  kunnastaPoisMuuttopv: Option[LocalDate],
) {
  def toDbRow(turvakielto: Boolean): RKotikuntahistoriaRow =
    RKotikuntahistoriaRow(
      masterOppijaOid = oid,
      kotikunta = kotikunta,
      muuttoPvm = kuntaanMuuttopv.map(pvm => Date.valueOf(pvm)),
      poismuuttoPvm = kunnastaPoisMuuttopv.map(pvm => Date.valueOf(pvm)),
      turvakielto = turvakielto,
    )

  lazy val pvm: Option[LocalDate] = coalesce(kuntaanMuuttopv, kunnastaPoisMuuttopv)
}

case class KotikuntahistoriaConfig(config: Config) {
  def käytäOppivelvollisuudenPäättelyyn: Boolean = getBoolean("kotikuntahistoria.oppivelvollisuustiedotFiltering")
  def käytäMaksuttomuustietojenValidointiin: Boolean = getBoolean("kotikuntahistoria.maksuttomuusValidation")

  private def getBoolean(path: String): Boolean =
    if (config.hasPath(path)) config.getBoolean(path) else false
}
