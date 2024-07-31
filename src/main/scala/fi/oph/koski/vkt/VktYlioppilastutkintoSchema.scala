package fi.oph.koski.vkt

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.{Description, Title}

object VktYlioppilastutkinnonOpiskeluoikeus {
  def fromKoskiSchema(yo: schema.YlioppilastutkinnonOpiskeluoikeus): VktOpiskeluoikeus = VktYlioppilastutkinnonOpiskeluoikeus(
    oid = yo.oid,
    oppilaitos = yo.oppilaitos.map(ol =>
      Oppilaitos(
        ol.oid,
        ol.oppilaitosnumero.map(VktKoodistokoodiviite.fromKoskiSchema),
        ol.nimi,
        ol.kotipaikka.map(VktKoodistokoodiviite.fromKoskiSchema)
      )
    ),
    koulutustoimija = yo.koulutustoimija.map(kt =>
      Koulutustoimija(
        kt.oid,
        kt.nimi,
        kt.yTunnus,
        kt.kotipaikka.map(VktKoodistokoodiviite.fromKoskiSchema)
      )
    ),
    tila = VktOpiskeluoikeudenTila(
      yo.tila.opiskeluoikeusjaksot.map(yot =>
        VktOpiskeluoikeusjakso(
          yot.alku,
          VktKoodistokoodiviite.fromKoskiSchema(yot.tila),
          None
        )
      )
    ),
    suoritukset = yo.suoritukset.map(s => VktYlioppilastutkinnonPäätasonSuoritus(
      VktYlioppilastutkinnonSuorituksenKoulutusmoduuli(
        VktKoodistokoodiviite.fromKoskiSchema(s.koulutusmoduuli.tunniste)
      ),
      Some(Toimipiste(
        s.toimipiste.oid,
        s.toimipiste.nimi,
        s.toimipiste.kotipaikka.map(VktKoodistokoodiviite.fromKoskiSchema)
      )),
      vahvistus = s.vahvistus.map(v => Vahvistus(v.päivä)),
      tyyppi = s.tyyppi,
    )),
    tyyppi = yo.tyyppi
  )
}

@Title("Ylioppilastutkinnon opiskeluoikeus")
@Description("Ylioppilastutkinnon opiskeluoikeus")
case class VktYlioppilastutkinnonOpiskeluoikeus(
  oid: Option[String],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  tila: VktOpiskeluoikeudenTila,
  suoritukset: List[VktYlioppilastutkinnonPäätasonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.ylioppilastutkinto.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
  versionumero: Option[Int] = None
) extends VktKoskeenTallennettavaOpiskeluoikeus {

  override def lisätiedot: Option[VktOpiskeluoikeudenLisätiedot] = None

  override def withSuoritukset(suoritukset: List[Suoritus]): VktOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s: VktYlioppilastutkinnonPäätasonSuoritus => s }
    )
}

@Title("Ylioppilastutkinnon suoritus")
case class VktYlioppilastutkinnonPäätasonSuoritus(
  koulutusmoduuli: VktYlioppilastutkinnonSuorituksenKoulutusmoduuli,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  tyyppi: schema.Koodistokoodiviite,
) extends Suoritus

case class VktYlioppilastutkinnonSuorituksenKoulutusmoduuli(
  tunniste: VktKoodistokoodiviite,
) extends SuorituksenKoulutusmoduuli

