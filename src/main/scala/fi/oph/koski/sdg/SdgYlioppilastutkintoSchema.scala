package fi.oph.koski.sdg

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.{Description, Title}

object YlioppilastutkinnonOpiskeluoikeus {
  def fromKoskiSchema(yo: schema.YlioppilastutkinnonOpiskeluoikeus): Opiskeluoikeus = YlioppilastutkinnonOpiskeluoikeus(
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
      opiskeluoikeusjaksot = yo.tila.opiskeluoikeusjaksot.map(j => Opiskeluoikeusjakso(
        tila = j.tila,
        alku = j.alku
      ))
    ),
    suoritukset = yo.suoritukset.map(s => YlioppilastutkinnonSuoritus(
      s.koulutusmoduuli,
      Some(Toimipiste(
        s.toimipiste.oid,
        s.toimipiste.nimi,
        s.toimipiste.kotipaikka
      )),
      vahvistus = s.vahvistus.map(v => Vahvistus(v.päivä)),
      tyyppi = s.tyyppi,
      osasuoritukset = s.osasuoritukset.map(_.map(YlioppilastutkinnonOsasuoritus.fromKoskiSchema)),
      koulusivistyskieli = s.koulusivistyskieli
    )),
    tyyppi = yo.tyyppi
  )
}

@Title("Ylioppilastutkinnon opiskeluoikeus")
@Description("Ylioppilastutkinnon opiskeluoikeus")
case class YlioppilastutkinnonOpiskeluoikeus(
  oid: Option[String],
  oppilaitos: Option[schema.Oppilaitos],
  koulutustoimija: Option[schema.Koulutustoimija],
  tila: OpiskeluoikeudenTila,
  suoritukset: List[YlioppilastutkinnonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.ylioppilastutkinto.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
  versionumero: Option[Int] = None
) extends Opiskeluoikeus {
  override def withSuoritukset(suoritukset: List[Suoritus]): Opiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s: YlioppilastutkinnonSuoritus => s }
    )
}

@Title("Ylioppilastutkinnon suoritus")
case class YlioppilastutkinnonSuoritus(
  koulutusmoduuli: schema.Ylioppilastutkinto,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  tyyppi: schema.Koodistokoodiviite,
  koulusivistyskieli: Option[List[schema.Koodistokoodiviite]],
  osasuoritukset: Option[List[YlioppilastutkinnonOsasuoritus]]
) extends Suoritus {
  override def withOsasuoritukset(os: Option[List[Osasuoritus]]): YlioppilastutkinnonSuoritus =
    this.copy(osasuoritukset = os.map(_.collect {
      case s: YlioppilastutkinnonOsasuoritus => s
    }))
}


@Title("Ylioppilastutkinnon koe")
case class YlioppilastutkinnonOsasuoritus(
  koulutusmoduuli: schema.YlioppilasTutkinnonKoe,
  arviointi: Option[List[schema.YlioppilaskokeenArviointi]],
  tyyppi: schema.Koodistokoodiviite,
  tutkintokerta: schema.YlioppilastutkinnonTutkintokerta,
) extends Osasuoritus

object YlioppilastutkinnonOsasuoritus {
  def fromKoskiSchema(k: schema.YlioppilastutkinnonKokeenSuoritus): YlioppilastutkinnonOsasuoritus =
    YlioppilastutkinnonOsasuoritus(
      koulutusmoduuli = k.koulutusmoduuli, arviointi = k.arviointi, tyyppi = k.tyyppi, tutkintokerta = k.tutkintokerta)
}

