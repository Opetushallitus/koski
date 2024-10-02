package fi.oph.koski.raportointikanta

import fi.oph.koski.raportointikanta.RaportointiDatabaseSchema._
import slick.lifted.TableQuery

trait RaportointikantaTableQueries {
  val ROpiskeluoikeudet = TableQuery[ROpiskeluoikeusTable]
  val ROrganisaatiohistoriat = TableQuery[ROrganisaatioHistoriaTable]
  val ROpiskeluoikeusAikajaksot = TableQuery[ROpiskeluoikeusAikajaksoTable]
  val EsiopetusOpiskeluoikeusAikajaksot = TableQuery[EsiopetusOpiskeluoikeusAikajaksoTable]
  val RPäätasonSuoritukset = TableQuery[RPäätasonSuoritusTable]
  val ROsasuoritukset = TableQuery[ROsasuoritusTable]
  val RHenkilöt = TableQuery[RHenkilöTable]
  val ROrganisaatiot = TableQuery[ROrganisaatioTable]
  val RKoodistoKoodit = TableQuery[RKoodistoKoodiTable]
  val RKotikuntahistoria = TableQuery[RKotikuntahistoriaTable]
  val RaportointikantaStatus = TableQuery[RaportointikantaStatusTable]
  val muuAmmatillinenOsasuoritusRaportointi = TableQuery[MuuAmmatillinenOsasuoritusRaportointiTable]
}
