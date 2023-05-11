package fi.oph.koski.suoritusjako.suoritetuttutkinnot

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.{Description, Title}

object SuoritetutTutkinnotYlioppilastutkinnonOpiskeluoikeus {
  def fromKoskiSchema(yo: schema.YlioppilastutkinnonOpiskeluoikeus) = SuoritetutTutkinnotYlioppilastutkinnonOpiskeluoikeus(
    oppilaitos = yo.oppilaitos.map(ol =>
      Oppilaitos(
        ol.oid,
        ol.oppilaitosnumero.map(SuoritetutTutkinnotKoodistokoodiviite.fromKoskiSchema),
        ol.nimi,
        ol.kotipaikka.map(SuoritetutTutkinnotKoodistokoodiviite.fromKoskiSchema)
      )
    ),
    koulutustoimija = yo.koulutustoimija.map(kt =>
      Koulutustoimija(
        kt.oid,
        kt.nimi,
        kt.yTunnus,
        kt.kotipaikka.map(SuoritetutTutkinnotKoodistokoodiviite.fromKoskiSchema)
      )
    ),
    suoritukset = yo.suoritukset.map(s => SuoritetutTutkinnotYlioppilastutkinnonPäätasonSuoritus(
      SuoritetutTutkinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli(
        SuoritetutTutkinnotKoodistokoodiviite.fromKoskiSchema(s.koulutusmoduuli.tunniste)
      ),
      Some(Toimipiste(
        s.toimipiste.oid,
        s.toimipiste.nimi,
        s.toimipiste.kotipaikka.map(SuoritetutTutkinnotKoodistokoodiviite.fromKoskiSchema)
      )),
      vahvistus = s.vahvistus.map(v => Vahvistus(v.päivä)),
      tyyppi = s.tyyppi,
    )),
    tyyppi = yo.tyyppi
  )
}

@Title("Ylioppilastutkinnon opiskeluoikeus")
@Description("Ylioppilastutkinnon opiskeluoikeus")
case class SuoritetutTutkinnotYlioppilastutkinnonOpiskeluoikeus(
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  suoritukset: List[SuoritetutTutkinnotYlioppilastutkinnonPäätasonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.ylioppilastutkinto.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
) extends SuoritetutTutkinnotOpiskeluoikeus {
  override def oid = None
  override def versionumero = None
  override def sisältyyOpiskeluoikeuteen = None

  override def withSuoritukset(suoritukset: List[Suoritus]): SuoritetutTutkinnotOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s : SuoritetutTutkinnotYlioppilastutkinnonPäätasonSuoritus => s }
    )
  override def withoutSisältyyOpiskeluoikeuteen: SuoritetutTutkinnotOpiskeluoikeus = this
}

@Title("Ylioppilastutkinnon suoritus")
case class SuoritetutTutkinnotYlioppilastutkinnonPäätasonSuoritus(
  koulutusmoduuli: SuoritetutTutkinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  tyyppi: schema.Koodistokoodiviite,
) extends Suoritus

case class SuoritetutTutkinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli(
  tunniste: SuoritetutTutkinnotKoodistokoodiviite,
) extends SuorituksenKoulutusmoduuli
