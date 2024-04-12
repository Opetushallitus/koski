package fi.oph.koski.henkilo

import fi.oph.koski.raportointikanta.RKotikuntahistoriaRow

import java.sql.Date
import java.time.LocalDate


case class OppijanumerorekisteriKotikuntahistoriaRow(
  oid: String,
  kotikunta: Long,
  kuntaanMuuttopv: LocalDate,
  kunnastaPoisMuuttopv: Option[LocalDate],
  turvakielto: Option[Boolean], // TODO TOR-2031: Tästä ei ole vielä sovittu
) {
  def toDbRow: RKotikuntahistoriaRow =
    RKotikuntahistoriaRow(
      oppijaOid = oid,
      kotikunta = kotikunta,
      muuttoPvm = Date.valueOf(kuntaanMuuttopv),
      poismuuttoPvm = kunnastaPoisMuuttopv.map(pvm => Date.valueOf(pvm)),
      turvakielto = turvakielto.contains(true),
    )
}
