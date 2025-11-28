package fi.oph.koski.valpas.massaluovutus

import java.time.LocalDate
import fi.oph.koski.valpas.opiskeluoikeusrepository.ValpasHenkilö
import fi.oph.koski.valpas.oppija.ValpasKuntailmoitusSuppeatTiedot
import fi.oph.koski.valpas.rouhinta.{RouhintaOpiskeluoikeus, ValpasRouhintaOppivelvollinen}
import fi.oph.koski.valpas.valpasrepository.ValpasOppivelvollisuudenKeskeytys

case class ValpasMassaluovutusOppija(
  oppijanumero: ValpasHenkilö.Oid,
  kaikkiOidit: Option[Seq[ValpasHenkilö.Oid]],
  etunimet: String,
  sukunimi: String,
  syntymäaika: Option[LocalDate],
  hetu: Option[String],
  viimeisinOppivelvollisuudenSuorittamiseenKelpaavaOpiskeluoikeus: Option[RouhintaOpiskeluoikeus],
  oppivelvollisuudenKeskeytys: Seq[ValpasOppivelvollisuudenKeskeytys],
  vainOppijanumerorekisterissä: Boolean,
  aktiivinenKuntailmoitus: Option[ValpasKuntailmoitusSuppeatTiedot],
  oikeusMaksuttomaanKoulutukseenVoimassaAsti: Option[LocalDate]
)

object ValpasMassaluovutusOppija {
  def apply(oppivelvollinen: ValpasRouhintaOppivelvollinen): ValpasMassaluovutusOppija = {
    ValpasMassaluovutusOppija(
      oppijanumero = oppivelvollinen.oppijanumero,
      kaikkiOidit = oppivelvollinen.kaikkiOidit,
      etunimet = oppivelvollinen.etunimet,
      sukunimi = oppivelvollinen.sukunimi,
      syntymäaika = oppivelvollinen.syntymäaika,
      hetu = oppivelvollinen.hetu,
      viimeisinOppivelvollisuudenSuorittamiseenKelpaavaOpiskeluoikeus = oppivelvollinen.viimeisinOppivelvollisuudenSuorittamiseenKelpaavaOpiskeluoikeus,
      oppivelvollisuudenKeskeytys = oppivelvollinen.oppivelvollisuudenKeskeytys,
      vainOppijanumerorekisterissä = oppivelvollinen.vainOppijanumerorekisterissä,
      aktiivinenKuntailmoitus = oppivelvollinen.aktiivinenKuntailmoitus,
      oikeusMaksuttomaanKoulutukseenVoimassaAsti = None
    )
  }
}
