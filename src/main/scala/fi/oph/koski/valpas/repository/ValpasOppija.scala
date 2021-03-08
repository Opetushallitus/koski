package fi.oph.koski.valpas.repository

import fi.oph.koski.schema.{Koodistokoodiviite, LocalizedString}
import java.time.LocalDate

trait ValpasOppija {
  def henkilö: ValpasHenkilö
  def opiskeluoikeudet: Seq[ValpasOpiskeluoikeus]

  def opiskelee = opiskeluoikeudet.exists(_.isOpiskelu)
  def oppivelvollisuusVoimassaAsti = henkilö.syntymäaika.map(LocalDate.parse(_).plusYears(18).toString)
}

case class ValpasOppijaResult(
  henkilö: ValpasHenkilö,
  oikeutetutOppilaitokset: Set[String],
  opiskeluoikeudet: Seq[ValpasOpiskeluoikeus]
) extends ValpasOppija

case class ValpasOppijaLisätiedoilla(
  henkilö: ValpasHenkilö,
  opiskeluoikeudet: Seq[ValpasOpiskeluoikeus],
  tiedot: ValpasOppijaTiedot
) extends ValpasOppija

object ValpasOppijaLisätiedoilla {
  def apply(oppija: ValpasOppija): ValpasOppijaLisätiedoilla = {
    ValpasOppijaLisätiedoilla(
      henkilö = oppija.henkilö,
      opiskeluoikeudet = oppija.opiskeluoikeudet,
      tiedot = ValpasOppijaTiedot(
        opiskelee = oppija.opiskelee,
        oppivelvollisuusVoimassaAsti = oppija.oppivelvollisuusVoimassaAsti
      )
    )
  }
}

case class ValpasHenkilö(
  oid: String,
  hetu: Option[String],
  syntymäaika: Option[String],
  etunimet: String,
  sukunimi: String
)

case class ValpasOpiskeluoikeus(
  oid: String,
  tyyppi: Koodistokoodiviite,
  oppilaitos: ValpasOppilaitos,
  toimipiste: Option[ValpasToimipiste],
  alkamispäivä: Option[String],
  päättymispäivä: Option[String],
  ryhmä: Option[String],
  viimeisinTila: Koodistokoodiviite
) {
  def isOpiskelu = {
    viimeisinTila.koodiarvo == "lasna" || viimeisinTila.koodiarvo == "valiaikaisestikeskeytynyt"
  }
}

case class ValpasOppilaitos(
  oid: String,
  nimi: LocalizedString
)

case class ValpasToimipiste(
  oid: String,
  nimi: LocalizedString
)

case class ValpasOppijaTiedot(
  opiskelee: Boolean,
  oppivelvollisuusVoimassaAsti: Option[String]
)
