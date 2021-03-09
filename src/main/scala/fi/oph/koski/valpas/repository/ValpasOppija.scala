package fi.oph.koski.valpas.repository

import fi.oph.koski.schema.{Koodistokoodiviite, LocalizedString}
import fi.oph.koski.valpas.hakukooste.Hakukooste

import java.time.LocalDate

trait ValpasOppija {
  def henkilö: ValpasHenkilö
  def oikeutetutOppilaitokset: Set[String]
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
  oikeutetutOppilaitokset: Set[String],
  opiskeluoikeudet: Seq[ValpasOpiskeluoikeus],
  haut: Option[Seq[Hakukooste]],
  tiedot: ValpasOppijaTiedot
) extends ValpasOppija

object ValpasOppijaLisätiedoilla {
  def apply(oppija: ValpasOppija): ValpasOppijaLisätiedoilla = {
    ValpasOppijaLisätiedoilla(
      henkilö = oppija.henkilö,
      oikeutetutOppilaitokset = oppija.oikeutetutOppilaitokset,
      opiskeluoikeudet = oppija.opiskeluoikeudet,
      haut = None,
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
