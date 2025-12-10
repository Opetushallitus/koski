package fi.oph.koski.sdg

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.{Description, Title}

object SdgYlioppilastutkinnonOpiskeluoikeus {
  def fromKoskiSchema(yo: schema.YlioppilastutkinnonOpiskeluoikeus): Opiskeluoikeus = SdgYlioppilastutkinnonOpiskeluoikeus(
    oid = yo.oid,
    oppilaitos = yo.oppilaitos.map(ol =>
      schema.Oppilaitos(
        ol.oid,
        ol.oppilaitosnumero,
        ol.nimi,
        ol.kotipaikka
      )
    ),
    koulutustoimija = yo.koulutustoimija.map(kt =>
      schema.Koulutustoimija(
        kt.oid,
        kt.nimi,
        kt.yTunnus,
        kt.kotipaikka
      )
    ),
    tila = OpiskeluoikeudenTila(
      opiskeluoikeusjaksot = yo.tila.opiskeluoikeusjaksot.map(j => SdgOpiskeluoikeusjakso(
        tila = j.tila,
        alku = j.alku
      ))
    ),
    suoritukset = yo.suoritukset.map(s => SdgYlioppilastutkinnonSuoritus(
      s.koulutusmoduuli,
      Some(SdgToimipiste(
        s.toimipiste.oid,
        s.toimipiste.nimi,
        s.toimipiste.kotipaikka
      )),
      vahvistus = s.vahvistus.map(v => SdgVahvistus(v.päivä)),
      tyyppi = s.tyyppi,
      osasuoritukset = s.osasuoritukset.map(_.map(SdgYlioppilastutkinnonOsasuoritus.fromKoskiSchema)),
      koulusivistyskieli = s.koulusivistyskieli
    )),
    tyyppi = yo.tyyppi
  )
}

@Title("Ylioppilastutkinnon opiskeluoikeus")
@Description("Ylioppilastutkinnon opiskeluoikeus")
case class SdgYlioppilastutkinnonOpiskeluoikeus(
  oid: Option[String],
  oppilaitos: Option[schema.Oppilaitos],
  koulutustoimija: Option[schema.Koulutustoimija],
  tila: OpiskeluoikeudenTila,
  suoritukset: List[SdgYlioppilastutkinnonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.ylioppilastutkinto.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
  versionumero: Option[Int] = None
) extends Opiskeluoikeus {
  override def withSuoritukset(suoritukset: List[Suoritus]): Opiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s: SdgYlioppilastutkinnonSuoritus => s }
    )
}

@Title("Ylioppilastutkinnon suoritus")
case class SdgYlioppilastutkinnonSuoritus(
  koulutusmoduuli: schema.Ylioppilastutkinto,
  toimipiste: Option[SdgToimipiste],
  vahvistus: Option[SdgVahvistus],
  tyyppi: schema.Koodistokoodiviite,
  koulusivistyskieli: Option[List[schema.Koodistokoodiviite]],
  osasuoritukset: Option[List[SdgYlioppilastutkinnonOsasuoritus]]
) extends Suoritus {
  override def withOsasuoritukset(os: Option[List[Osasuoritus]]): SdgYlioppilastutkinnonSuoritus =
    this.copy(osasuoritukset = os.map(_.collect {
      case s: SdgYlioppilastutkinnonOsasuoritus => s
    }))
}


@Title("Ylioppilastutkinnon koe")
case class SdgYlioppilastutkinnonOsasuoritus(
  koulutusmoduuli: schema.YlioppilasTutkinnonKoe,
  arviointi: Option[List[schema.YlioppilaskokeenArviointi]],
  tyyppi: schema.Koodistokoodiviite,
  tutkintokerta: schema.YlioppilastutkinnonTutkintokerta,
  suoritusMukanaTodistuksella: Option[Boolean]
) extends Osasuoritus

object SdgYlioppilastutkinnonOsasuoritus {
  def fromKoskiSchema(k: schema.YlioppilastutkinnonKokeenSuoritus): SdgYlioppilastutkinnonOsasuoritus =
    SdgYlioppilastutkinnonOsasuoritus(
      koulutusmoduuli = k.koulutusmoduuli, arviointi = k.arviointi, tyyppi = k.tyyppi, tutkintokerta = k.tutkintokerta, suoritusMukanaTodistuksella = k.suoritusMukanaTodistuksella)
}

