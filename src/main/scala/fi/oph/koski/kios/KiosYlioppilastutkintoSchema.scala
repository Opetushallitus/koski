package fi.oph.koski.kios

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.{Description, Title}

object KiosYlioppilastutkinnonOpiskeluoikeus {
  def fromKoskiSchema(yo: schema.YlioppilastutkinnonOpiskeluoikeus): KiosOpiskeluoikeus = KiosYlioppilastutkinnonOpiskeluoikeus(
    oid = yo.oid,
    oppilaitos = yo.oppilaitos.map(ol =>
      Oppilaitos(
        ol.oid,
        ol.oppilaitosnumero.map(KiosKoodistokoodiviite.fromKoskiSchema),
        ol.nimi,
        ol.kotipaikka.map(KiosKoodistokoodiviite.fromKoskiSchema)
      )
    ),
    koulutustoimija = yo.koulutustoimija.map(kt =>
      Koulutustoimija(
        kt.oid,
        kt.nimi,
        kt.yTunnus,
        kt.kotipaikka.map(KiosKoodistokoodiviite.fromKoskiSchema)
      )
    ),
    tila = KiosOpiskeluoikeudenTila(
      yo.tila.opiskeluoikeusjaksot.map(yot =>
        KiosOpiskeluoikeusjakso(
          yot.alku,
          KiosKoodistokoodiviite.fromKoskiSchema(yot.tila),
          None
        )
      )
    ),
    suoritukset = yo.suoritukset.map(s => KiosYlioppilastutkinnonPäätasonSuoritus(
      KiosYlioppilastutkinnonSuorituksenKoulutusmoduuli(
        KiosKoodistokoodiviite.fromKoskiSchema(s.koulutusmoduuli.tunniste)
      ),
      Some(Toimipiste(
        s.toimipiste.oid,
        s.toimipiste.nimi,
        s.toimipiste.kotipaikka.map(KiosKoodistokoodiviite.fromKoskiSchema)
      )),
      vahvistus = s.vahvistus.map(v => Vahvistus(v.päivä)),
      tyyppi = s.tyyppi,
    )),
    tyyppi = yo.tyyppi
  )
}

@Title("Ylioppilastutkinnon opiskeluoikeus")
@Description("Ylioppilastutkinnon opiskeluoikeus")
case class KiosYlioppilastutkinnonOpiskeluoikeus(
  oid: Option[String],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  tila: KiosOpiskeluoikeudenTila,
  suoritukset: List[KiosYlioppilastutkinnonPäätasonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.ylioppilastutkinto.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
  versionumero: Option[Int] = None
) extends KiosKoskeenTallennettavaOpiskeluoikeus {

  override def lisätiedot: Option[KiosOpiskeluoikeudenLisätiedot] = None

  override def withSuoritukset(suoritukset: List[Suoritus]): KiosOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s: KiosYlioppilastutkinnonPäätasonSuoritus => s }
    )
}

@Title("Ylioppilastutkinnon suoritus")
case class KiosYlioppilastutkinnonPäätasonSuoritus(
  koulutusmoduuli: KiosYlioppilastutkinnonSuorituksenKoulutusmoduuli,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  tyyppi: schema.Koodistokoodiviite,
) extends Suoritus

case class KiosYlioppilastutkinnonSuorituksenKoulutusmoduuli(
  tunniste: KiosKoodistokoodiviite,
) extends SuorituksenKoulutusmoduuli

