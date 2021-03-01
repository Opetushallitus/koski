package fi.oph.koski.valpas.repository

import fi.oph.koski.raportointikanta.{RHenkilöRow, ROpiskeluoikeusRow}
import fi.oph.koski.schema.{Finnish, Koodistokoodiviite, LocalizedString}

case class ValpasOppija(
  henkilö: ValpasHenkilö,
  opiskeluoikeudet: Seq[ValpasOpiskeluoikeus]
)

object ValpasHenkilö {
  def apply(henkilöRow: RHenkilöRow): ValpasHenkilö = {
    new ValpasHenkilö(
      oid = henkilöRow.oppijaOid,
      hetu = henkilöRow.hetu,
      syntymäaika = henkilöRow.syntymäaika.map(_.toString),
      etunimet = henkilöRow.etunimet,
      sukunimi = henkilöRow.sukunimi
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

object ValpasOpiskeluoikeus {
  def apply(opiskeluoikeusRow: ROpiskeluoikeusRow): ValpasOpiskeluoikeus = ValpasOpiskeluoikeus(
    oid = opiskeluoikeusRow.opiskeluoikeusOid,
    tyyppi = Koodistokoodiviite(opiskeluoikeusRow.koulutusmuoto, "opiskeluoikeudentyyppi"),
    oppilaitos = ValpasOppilaitos(
      oid = opiskeluoikeusRow.oppilaitosOid,
      nimi = Finnish(opiskeluoikeusRow.oppilaitosNimi)),
    alkamispäivä = opiskeluoikeusRow.alkamispäivä.map(_.toString),
    päättymispäivä = opiskeluoikeusRow.päättymispäivä.map(_.toString),
    ryhmä = opiskeluoikeusRow.luokka
  )
}

case class ValpasOpiskeluoikeus(
  oid: String,
  tyyppi: Koodistokoodiviite,
  oppilaitos: ValpasOppilaitos,
  alkamispäivä: Option[String],
  päättymispäivä: Option[String],
  ryhmä: Option[String]
)

case class ValpasOppilaitos(
  oid: String,
  nimi: LocalizedString
)
