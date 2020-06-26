package fi.oph.koski.raportointikanta

import fi.oph.koski.schema._


object MuuAmmatillinenRaporttiRowBuilder {
  def build(opiskeluoikeudenOid: String, päätasonSuoritusId: Long, os: Suoritus): MuuAmmatillinenOsasuoritusRaportointiRow = {
    MuuAmmatillinenOsasuoritusRaportointiRow(
      opiskeluoikeusOid = opiskeluoikeudenOid,
      päätasonSuoritusId = päätasonSuoritusId,
      toteuttavanLuokanNimi = os.getClass.getSimpleName.toLowerCase,
      koulutusmoduuliLaajuusArvo = os.koulutusmoduuli.getLaajuus.map(_.arvo),
      koulutusmoduuliLaajuusYksikkö = os.koulutusmoduuli.getLaajuus.map(_.yksikkö.koodiarvo),
      arviointiHyväksytty = os.viimeisinArviointi.exists(_.hyväksytty)
    )
  }
}
