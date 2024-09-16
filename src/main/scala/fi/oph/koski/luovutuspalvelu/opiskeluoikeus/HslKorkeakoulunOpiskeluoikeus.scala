package fi.oph.koski.luovutuspalvelu.opiskeluoikeus

import fi.oph.koski.schema.annotation.KoodistoUri
import fi.oph.koski.schema._
import fi.oph.scalaschema.annotation.Title

import java.time.{LocalDate, LocalDateTime}

@Title("Korkeakoulun opiskeluoikeus")
case class HslKorkeakoulunOpiskeluoikeus(
  tyyppi: Koodistokoodiviite,
  oid: String,
  oppilaitos: Option[Oppilaitos],
  tila: HslOpiskeluoikeudenTila,
  suoritukset: List[HslDefaultPäätasonSuoritus],
  lisätiedot: Option[HslKorkeakouluOpiskeluoikeudenLisätiedot],
  arvioituPäättymispäivä: Option[LocalDate],
  aikaleima: Option[LocalDateTime],
  alkamispäivä: Option[LocalDate],
  versionumero: Option[Int],
  päättymispäivä: Option[LocalDate],
  synteettinen: Boolean = false,
  luokittelu: Option[List[Koodistokoodiviite]],
  organisaatiohistoria: Option[List[HslOpiskeluoikeudenOrganisaatiohistoria]]
) extends HslOpiskeluoikeus

object HslKorkeakoulunOpiskeluoikeus {
  def apply(oo: KorkeakoulunOpiskeluoikeus): HslKorkeakoulunOpiskeluoikeus =
    HslKorkeakoulunOpiskeluoikeus(
      tyyppi = oo.tyyppi,
      oid = oo.oid.getOrElse(""),
      oppilaitos = oo.oppilaitos,
      tila = HslOpiskeluoikeudenTila(Some(oo.tila.opiskeluoikeusjaksot.map(j => HslOpiskeluoikeusJakso(j.tila, j.alku, j.nimi)))),
      suoritukset = oo.suoritukset.map(HslDefaultPäätasonSuoritus.apply),
      lisätiedot = oo.lisätiedot.map(HslKorkeakouluOpiskeluoikeudenLisätiedot.apply),
      arvioituPäättymispäivä = oo.arvioituPäättymispäivä,
      aikaleima = None,
      alkamispäivä = oo.alkamispäivä,
      versionumero = oo.versionumero,
      päättymispäivä = oo.päättymispäivä,
      synteettinen = oo.synteettinen,
      luokittelu = oo.luokittelu,
      organisaatiohistoria = None
    )
}


@Title("Korkeakoulun opiskeluoikeuden lisätiedot")
case class HslKorkeakouluOpiskeluoikeudenLisätiedot(
  osaAikaisuusjaksot: Option[List[OsaAikaisuusJakso]] = None,
  @Title("Korkeakoulun opiskeluoikeuden tyyppi")
  @KoodistoUri("virtaopiskeluoikeudentyyppi")
  virtaOpiskeluoikeudenTyyppi: Option[Koodistokoodiviite],
  lukukausiIlmoittautuminen: Option[HslLukukausiIlmoittautuminen]
) extends HslOpiskeluoikeudenLisätiedot

object HslKorkeakouluOpiskeluoikeudenLisätiedot {
  def apply(lt: KorkeakoulunOpiskeluoikeudenLisätiedot): HslKorkeakouluOpiskeluoikeudenLisätiedot =
    new HslKorkeakouluOpiskeluoikeudenLisätiedot(
      virtaOpiskeluoikeudenTyyppi = lt.virtaOpiskeluoikeudenTyyppi,
      lukukausiIlmoittautuminen = lt.lukukausiIlmoittautuminen.map(HslLukukausiIlmoittautuminen.apply)
    )
}

case class HslLukukausiIlmoittautuminen(
  ilmoittautumisjaksot: List[HslLukukausiIlmoittautumisjakso]
)

object HslLukukausiIlmoittautuminen {
  def apply(l: Lukukausi_Ilmoittautuminen): HslLukukausiIlmoittautuminen =
    HslLukukausiIlmoittautuminen(
      ilmoittautumisjaksot = l.ilmoittautumisjaksot.map(HslLukukausiIlmoittautumisjakso.apply)
    )
}

case class HslLukukausiIlmoittautumisjakso(
  alku: LocalDate,
  loppu: Option[LocalDate],
  @KoodistoUri("virtalukukausiilmtila")
  tila: Koodistokoodiviite,
  ylioppilaskunnanJäsen: Option[Boolean] = None,
  @Title("Lukuvuosimaksu")
  maksetutLukuvuosimaksut: Option[HslLukuvuosiMaksu] = None
) extends Jakso

object HslLukukausiIlmoittautumisjakso {
  def apply(j: Lukukausi_Ilmoittautumisjakso): HslLukukausiIlmoittautumisjakso =
    HslLukukausiIlmoittautumisjakso(
      alku = j.alku,
      loppu = j.loppu,
      tila = j.tila,
      ylioppilaskunnanJäsen = j.ylioppilaskunnanJäsen,
      maksetutLukuvuosimaksut = j.maksetutLukuvuosimaksut.map(HslLukuvuosiMaksu.apply)
    )
}

case class HslLukuvuosiMaksu(
  @Title("Maksettu kokonaan")
  maksettu: Option[Boolean] = None,
  summa: Option[Int] = None,
  apuraha: Option[Int] = None
)

object HslLukuvuosiMaksu {
  def apply(m: Lukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu): HslLukuvuosiMaksu =
    HslLukuvuosiMaksu(
      maksettu = m.maksettu,
      summa = m.summa,
      apuraha = m.apuraha
    )
}
