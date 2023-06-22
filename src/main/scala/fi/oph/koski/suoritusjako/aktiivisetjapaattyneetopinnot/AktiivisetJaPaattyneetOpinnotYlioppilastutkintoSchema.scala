package fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.{Description, Title}

object AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonOpiskeluoikeus {
  def fromKoskiSchema(yo: schema.YlioppilastutkinnonOpiskeluoikeus): AktiivisetJaPäättyneetOpinnotOpiskeluoikeus = AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonOpiskeluoikeus(
    oppilaitos = yo.oppilaitos.map(ol =>
      Oppilaitos(
        ol.oid,
        ol.oppilaitosnumero.map(AktiivisetJaPäättyneetOpinnotKoodistokoodiviite.fromKoskiSchema),
        ol.nimi,
        ol.kotipaikka.map(AktiivisetJaPäättyneetOpinnotKoodistokoodiviite.fromKoskiSchema)
      )
    ),
    koulutustoimija = yo.koulutustoimija.map(kt =>
      Koulutustoimija(
        kt.oid,
        kt.nimi,
        kt.yTunnus,
        kt.kotipaikka.map(AktiivisetJaPäättyneetOpinnotKoodistokoodiviite.fromKoskiSchema)
      )
    ),
    tila = AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila(
      yo.tila.opiskeluoikeusjaksot.map(yot =>
        AktiivisetJaPäättyneetOpinnotOpiskeluoikeusjakso(
          yot.alku,
          AktiivisetJaPäättyneetOpinnotKoodistokoodiviite.fromKoskiSchema(yot.tila),
          None
        )
      )
    ),
    suoritukset = yo.suoritukset.map(s => AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonPäätasonSuoritus(
      AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli(
        AktiivisetJaPäättyneetOpinnotKoodistokoodiviite.fromKoskiSchema(s.koulutusmoduuli.tunniste)
      ),
      Some(Toimipiste(
        s.toimipiste.oid,
        s.toimipiste.nimi,
        s.toimipiste.kotipaikka.map(AktiivisetJaPäättyneetOpinnotKoodistokoodiviite.fromKoskiSchema)
      )),
      vahvistus = s.vahvistus.map(v => Vahvistus(v.päivä)),
      tyyppi = s.tyyppi,
    )),
    tyyppi = yo.tyyppi
  )
}

@Title("Ylioppilastutkinnon opiskeluoikeus")
@Description("Ylioppilastutkinnon opiskeluoikeus")
case class AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonOpiskeluoikeus(
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  tila: AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila,
  suoritukset: List[AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonPäätasonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.ylioppilastutkinto.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
) extends AktiivisetJaPäättyneetOpinnotOpiskeluoikeus {

  override def lisätiedot: Option[AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenLisätiedot] = None

  override def withSuoritukset(suoritukset: List[Suoritus]): AktiivisetJaPäättyneetOpinnotOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s : AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonPäätasonSuoritus => s }
    )
  override def withoutSisältyyOpiskeluoikeuteen: AktiivisetJaPäättyneetOpinnotOpiskeluoikeus = this
}

@Title("Ylioppilastutkinnon suoritus")
case class AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonPäätasonSuoritus(
  koulutusmoduuli: AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  tyyppi: schema.Koodistokoodiviite,
) extends Suoritus

case class AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli(
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite,
) extends SuorituksenKoulutusmoduuli
