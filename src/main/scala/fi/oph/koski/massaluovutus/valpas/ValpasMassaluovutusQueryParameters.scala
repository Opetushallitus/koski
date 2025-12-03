package fi.oph.koski.massaluovutus.valpas

import fi.oph.koski.config.KoskiApplication
import fi.oph.koski.massaluovutus.MassaluovutusQueryParameters
import fi.oph.koski.oppivelvollisuustieto.Oppivelvollisuustiedot
import fi.oph.koski.valpas.massaluovutus.ValpasMassaluovutusOppija

trait ValpasMassaluovutusQueryParameters extends MassaluovutusQueryParameters {

  def withOppivelvollisuustiedot(
    oppijat: Seq[ValpasMassaluovutusOppija],
    application: KoskiApplication
  ): Seq[ValpasMassaluovutusOppija] = {
    val oppijaOids = oppijat.map(_.oppijanumero)
    val oppivelvollisuustiedot = Oppivelvollisuustiedot.queryByOids(
      oppijaOids,
      application.raportointiDatabase
    )
    val oppivelvollisuustiedotByOid = oppivelvollisuustiedot.map(t => t.oid -> t).toMap

    oppijat.map { oppija =>
      val oikeusMaksuttomuuteenPäättyy = oppivelvollisuustiedotByOid
        .get(oppija.oppijanumero)
        .map(_.oikeusMaksuttomaanKoulutukseenVoimassaAsti)
      val kotikuntaSuomessaAlkaen = oppivelvollisuustiedotByOid
        .get(oppija.oppijanumero)
        .map(_.kotikuntaSuomessaAlkaen)

      oppija.copy(
        oikeusMaksuttomaanKoulutukseenVoimassaAsti = oikeusMaksuttomuuteenPäättyy,
        kotikuntaSuomessaAlkaen = kotikuntaSuomessaAlkaen
      )
    }
  }
}
