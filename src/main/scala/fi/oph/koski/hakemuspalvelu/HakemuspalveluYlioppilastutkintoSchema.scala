package fi.oph.koski.hakemuspalvelu

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.{Description, Title}

object HakemuspalveluYlioppilastutkinnonOpiskeluoikeus {
  def fromKoskiSchema(yo: schema.YlioppilastutkinnonOpiskeluoikeus): HakemuspalveluOpiskeluoikeus = HakemuspalveluYlioppilastutkinnonOpiskeluoikeus(
    oppilaitos = yo.oppilaitos.map(ol =>
      Oppilaitos(
        ol.oid,
        ol.oppilaitosnumero.map(HakemuspalveluKoodistokoodiviite.fromKoskiSchema),
        ol.nimi,
        ol.kotipaikka.map(HakemuspalveluKoodistokoodiviite.fromKoskiSchema)
      )
    ),
    koulutustoimija = yo.koulutustoimija.map(kt =>
      Koulutustoimija(
        kt.oid,
        kt.nimi,
        kt.yTunnus,
        kt.kotipaikka.map(HakemuspalveluKoodistokoodiviite.fromKoskiSchema)
      )
    ),
    tila = HakemuspalveluOpiskeluoikeudenTila(
      yo.tila.opiskeluoikeusjaksot.map(yot =>
        HakemuspalveluOpiskeluoikeusjakso(
          yot.alku,
          HakemuspalveluKoodistokoodiviite.fromKoskiSchema(yot.tila),
          None
        )
      )
    ),
    suoritukset = yo.suoritukset.map(s => HakemuspalveluYlioppilastutkinnonPäätasonSuoritus(
      HakemuspalveluYlioppilastutkinnonSuorituksenKoulutusmoduuli(
        HakemuspalveluKoodistokoodiviite.fromKoskiSchema(s.koulutusmoduuli.tunniste)
      ),
      Some(Toimipiste(
        s.toimipiste.oid,
        s.toimipiste.nimi,
        s.toimipiste.kotipaikka.map(HakemuspalveluKoodistokoodiviite.fromKoskiSchema)
      )),
      vahvistus = s.vahvistus.map(v => Vahvistus(v.päivä)),
      tyyppi = s.tyyppi,
    )),
    tyyppi = yo.tyyppi
  )
}

@Title("Ylioppilastutkinnon opiskeluoikeus")
@Description("Ylioppilastutkinnon opiskeluoikeus")
case class HakemuspalveluYlioppilastutkinnonOpiskeluoikeus(
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  tila: HakemuspalveluOpiskeluoikeudenTila,
  suoritukset: List[HakemuspalveluYlioppilastutkinnonPäätasonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.ylioppilastutkinto.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
) extends HakemuspalveluOpiskeluoikeus {

  override def lisätiedot: Option[HakemuspalveluOpiskeluoikeudenLisätiedot] = None

  override def withSuoritukset(suoritukset: List[Suoritus]): HakemuspalveluOpiskeluoikeus =
    this.copy(
      suoritukset = suoritukset.collect { case s: HakemuspalveluYlioppilastutkinnonPäätasonSuoritus => s }
    )
}

@Title("Ylioppilastutkinnon suoritus")
case class HakemuspalveluYlioppilastutkinnonPäätasonSuoritus(
  koulutusmoduuli: HakemuspalveluYlioppilastutkinnonSuorituksenKoulutusmoduuli,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  tyyppi: schema.Koodistokoodiviite,
) extends Suoritus

case class HakemuspalveluYlioppilastutkinnonSuorituksenKoulutusmoduuli(
  tunniste: HakemuspalveluKoodistokoodiviite,
) extends SuorituksenKoulutusmoduuli

