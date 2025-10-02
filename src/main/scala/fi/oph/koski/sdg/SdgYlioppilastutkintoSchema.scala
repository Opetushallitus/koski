package fi.oph.koski.sdg

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.{Description, Title}

object SdgYlioppilastutkinnonOpiskeluoikeus {
  def fromKoskiSchema(yo: schema.YlioppilastutkinnonOpiskeluoikeus): SdgOpiskeluoikeus = SdgYlioppilastutkinnonOpiskeluoikeus(
    oid = yo.oid,
    oppilaitos = yo.oppilaitos.map(ol =>
      Oppilaitos(
        ol.oid,
        ol.oppilaitosnumero.map(SdgKoodistokoodiviite.fromKoskiSchema),
        ol.nimi,
        ol.kotipaikka.map(SdgKoodistokoodiviite.fromKoskiSchema)
      )
    ),
    koulutustoimija = yo.koulutustoimija.map(kt =>
      Koulutustoimija(
        kt.oid,
        kt.nimi,
        kt.yTunnus,
        kt.kotipaikka.map(SdgKoodistokoodiviite.fromKoskiSchema)
      )
    ),
    tila = SdgOpiskeluoikeudenTila(
      yo.tila.opiskeluoikeusjaksot.map(yot =>
        SdgOpiskeluoikeusjakso(
          yot.alku,
          SdgKoodistokoodiviite.fromKoskiSchema(yot.tila),
          None
        )
      )
    ),
    suoritukset = yo.suoritukset.map(s => SdgYlioppilastutkinnonPäätasonSuoritus(
      SdgYlioppilastutkinnonSuorituksenKoulutusmoduuli(
        SdgKoodistokoodiviite.fromKoskiSchema(s.koulutusmoduuli.tunniste)
      ),
      Some(Toimipiste(
        s.toimipiste.oid,
        s.toimipiste.nimi,
        s.toimipiste.kotipaikka.map(SdgKoodistokoodiviite.fromKoskiSchema)
      )),
      vahvistus = s.vahvistus.map(v => Vahvistus(v.päivä)),
      tyyppi = s.tyyppi,
    )),
    tyyppi = yo.tyyppi
  )
}

@Title("Ylioppilastutkinnon opiskeluoikeus")
@Description("Ylioppilastutkinnon opiskeluoikeus")
case class SdgYlioppilastutkinnonOpiskeluoikeus(
  oid: Option[String],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  tila: SdgOpiskeluoikeudenTila,
  suoritukset: List[SdgYlioppilastutkinnonPäätasonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.ylioppilastutkinto.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
  versionumero: Option[Int] = None
) extends SdgKoskeenTallennettavaOpiskeluoikeus {

  override def lisätiedot: Option[SdgOpiskeluoikeudenLisätiedot] = None

  override def withSuoritukset(suoritukset: List[Suoritus]): SdgOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s: SdgYlioppilastutkinnonPäätasonSuoritus => s }
    )
}

@Title("Ylioppilastutkinnon suoritus")
case class SdgYlioppilastutkinnonPäätasonSuoritus(
  koulutusmoduuli: SdgYlioppilastutkinnonSuorituksenKoulutusmoduuli,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  tyyppi: schema.Koodistokoodiviite,
) extends Suoritus

case class SdgYlioppilastutkinnonSuorituksenKoulutusmoduuli(
  tunniste: SdgKoodistokoodiviite,
) extends SuorituksenKoulutusmoduuli

