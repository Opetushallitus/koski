package fi.oph.koski.raportointikanta

import java.sql.Date

import fi.oph.koski.schema._
import java.sql.Date


object MuuAmmatillinenRaporttiRowBuilder {
  def build(opiskeluoikeudenOid: String, päätasonSuoritusId: Long, os: Suoritus): MuuAmmatillinenOsasuoritusRaportointiRow = {
    MuuAmmatillinenOsasuoritusRaportointiRow(
      opiskeluoikeusOid = opiskeluoikeudenOid,
      päätasonSuoritusId = päätasonSuoritusId,
      toteuttavanLuokanNimi = os.getClass.getSimpleName.toLowerCase,
      koulutusmoduuliLaajuusArvo = os.koulutusmoduuli.laajuus.map(_.arvo),
      koulutusmoduuliLaajuusYksikkö = os.koulutusmoduuli.laajuus.map(_.yksikkö.koodiarvo),
      arviointiHyväksytty = os.viimeisinArviointi.exists(_.hyväksytty)
    )
  }
}
