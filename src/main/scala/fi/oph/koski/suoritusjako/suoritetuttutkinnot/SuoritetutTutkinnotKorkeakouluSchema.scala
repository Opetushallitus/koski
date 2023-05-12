package fi.oph.koski.suoritusjako.suoritetuttutkinnot

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.Title

object SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus {
  def fromKoskiSchema(kk: schema.KorkeakoulunOpiskeluoikeus) = SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus(
    oppilaitos = kk.oppilaitos.map(ol =>
      Oppilaitos(
        ol.oid,
        ol.oppilaitosnumero.map(SuoritetutTutkinnotKoodistokoodiviite.fromKoskiSchema),
        ol.nimi,
        ol.kotipaikka.map(SuoritetutTutkinnotKoodistokoodiviite.fromKoskiSchema)
      )
    ),
    koulutustoimija = kk.koulutustoimija.map(kt =>
      Koulutustoimija(
        kt.oid,
        kt.nimi,
        kt.yTunnus,
        kt.kotipaikka.map(SuoritetutTutkinnotKoodistokoodiviite.fromKoskiSchema)
      )
    ),
    suoritukset = kk.suoritukset
      .collect { case t: schema.KorkeakoulututkinnonSuoritus => t }
      .map(s => SuoritetutTutkinnotKorkeakoulututkinnonSuoritus(
        SuoritetutTutkinnotKorkeakoulututkinto(
          SuoritetutTutkinnotKoodistokoodiviite.fromKoskiSchema(s.koulutusmoduuli.tunniste),
          s.koulutusmoduuli.koulutustyyppi.map(SuoritetutTutkinnotKoodistokoodiviite.fromKoskiSchema),
          s.koulutusmoduuli.virtaNimi
        ),
        Some(Toimipiste(
          s.toimipiste.oid,
          s.toimipiste.nimi,
          s.toimipiste.kotipaikka.map(SuoritetutTutkinnotKoodistokoodiviite.fromKoskiSchema)
        )),
        vahvistus = s.vahvistus.map(v => Vahvistus(v.päivä)),
        tyyppi = s.tyyppi,
      )),
    tyyppi = kk.tyyppi,
    luokittelu = kk.luokittelu.map(_.map(SuoritetutTutkinnotKoodistokoodiviite.fromKoskiSchema)),
  )
}

@Title("Korkeakoulun opiskeluoikeus")
case class SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus(
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  suoritukset: List[SuoritetutTutkinnotKorkeakoulututkinnonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.korkeakoulutus.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
  luokittelu: Option[List[SuoritetutTutkinnotKoodistokoodiviite]]
) extends SuoritetutTutkinnotOpiskeluoikeus {
  override def oid = None
  override def versionumero = None
  override def sisältyyOpiskeluoikeuteen = None
  override def organisaatiohistoria = None
  override def aikaleima = None

  override def withSuoritukset(suoritukset: List[Suoritus]): SuoritetutTutkinnotOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s : SuoritetutTutkinnotKorkeakoulututkinnonSuoritus => s }
    )
}

@Title("Korkeakoulututkinnon suoritus")
case class SuoritetutTutkinnotKorkeakoulututkinnonSuoritus(
  koulutusmoduuli: SuoritetutTutkinnotKorkeakoulututkinto,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  @KoodistoKoodiarvo("korkeakoulututkinto")
  tyyppi: schema.Koodistokoodiviite,
) extends Suoritus

case class SuoritetutTutkinnotKorkeakoulututkinto(
  tunniste: SuoritetutTutkinnotKoodistokoodiviite,
  koulutustyyppi: Option[SuoritetutTutkinnotKoodistokoodiviite],
  virtaNimi: Option[schema.LocalizedString]
) extends SuorituksenKoulutusmoduuli
