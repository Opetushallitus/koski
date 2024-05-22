package fi.oph.koski.vkt

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.{Description, Title}

object VKTYlioppilastutkinnonOpiskeluoikeus {
  def fromKoskiSchema(yo: schema.YlioppilastutkinnonOpiskeluoikeus): VKTOpiskeluoikeus = VKTYlioppilastutkinnonOpiskeluoikeus(
    oppilaitos = yo.oppilaitos.map(ol =>
      Oppilaitos(
        ol.oid,
        ol.oppilaitosnumero.map(VKTKoodistokoodiviite.fromKoskiSchema),
        ol.nimi,
        ol.kotipaikka.map(VKTKoodistokoodiviite.fromKoskiSchema)
      )
    ),
    koulutustoimija = yo.koulutustoimija.map(kt =>
      Koulutustoimija(
        kt.oid,
        kt.nimi,
        kt.yTunnus,
        kt.kotipaikka.map(VKTKoodistokoodiviite.fromKoskiSchema)
      )
    ),
    tila = VKTOpiskeluoikeudenTila(
      yo.tila.opiskeluoikeusjaksot.map(yot =>
        VKTOpiskeluoikeusjakso(
          yot.alku,
          VKTKoodistokoodiviite.fromKoskiSchema(yot.tila),
          None
        )
      )
    ),
    suoritukset = yo.suoritukset.map(s => VKTYlioppilastutkinnonPäätasonSuoritus(
      VKTYlioppilastutkinnonSuorituksenKoulutusmoduuli(
        VKTKoodistokoodiviite.fromKoskiSchema(s.koulutusmoduuli.tunniste)
      ),
      Some(Toimipiste(
        s.toimipiste.oid,
        s.toimipiste.nimi,
        s.toimipiste.kotipaikka.map(VKTKoodistokoodiviite.fromKoskiSchema)
      )),
      vahvistus = s.vahvistus.map(v => Vahvistus(v.päivä)),
      tyyppi = s.tyyppi,
    )),
    tyyppi = yo.tyyppi
  )
}

@Title("Ylioppilastutkinnon opiskeluoikeus")
@Description("Ylioppilastutkinnon opiskeluoikeus")
case class VKTYlioppilastutkinnonOpiskeluoikeus(
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  tila: VKTOpiskeluoikeudenTila,
  suoritukset: List[VKTYlioppilastutkinnonPäätasonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.ylioppilastutkinto.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
) extends VKTOpiskeluoikeus {

  override def lisätiedot: Option[VKTOpiskeluoikeudenLisätiedot] = None

  override def withSuoritukset(suoritukset: List[Suoritus]): VKTOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s: VKTYlioppilastutkinnonPäätasonSuoritus => s }
    )

  override def withoutSisältyyOpiskeluoikeuteen: VKTOpiskeluoikeus = this
}

@Title("Ylioppilastutkinnon suoritus")
case class VKTYlioppilastutkinnonPäätasonSuoritus(
  koulutusmoduuli: VKTYlioppilastutkinnonSuorituksenKoulutusmoduuli,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  tyyppi: schema.Koodistokoodiviite,
) extends Suoritus

case class VKTYlioppilastutkinnonSuorituksenKoulutusmoduuli(
  tunniste: VKTKoodistokoodiviite,
) extends SuorituksenKoulutusmoduuli

