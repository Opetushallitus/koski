package fi.oph.koski.henkilo

import fi.oph.koski.db.PostgresDriverWithJsonSupport.api.actionBasedSQLInterpolation
import fi.oph.koski.raportointikanta.{RKotikuntahistoriaRow, Schema}
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
  kotikunta: Long,
  kuntaanMuuttopv: LocalDate,
  kunnastaPoisMuuttopv: Option[LocalDate],
  turvakielto: Option[Boolean], // TODO TOR-2031: Tästä ei ole vielä sovittu
) {
  def toDbRow: RKotikuntahistoriaRow =
    RKotikuntahistoriaRow(
      masterOppijaOid = oid,
      kotikunta = kotikuntaKoodiarvo,
      muuttoPvm = Date.valueOf(kuntaanMuuttopv),
      poismuuttoPvm = kunnastaPoisMuuttopv.map(pvm => Date.valueOf(pvm)),
      turvakielto = turvakielto.contains(true),
    )

  def kotikuntaKoodiarvo: String = "%03d".format(kotikunta)
}
