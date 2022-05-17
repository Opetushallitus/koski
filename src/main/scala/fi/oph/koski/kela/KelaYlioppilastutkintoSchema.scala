package fi.oph.koski.kela

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.scalaschema.annotation.{Description, Title}

import java.time.LocalDate

object KelaYlioppilastutkinnonOpiskeluoikeus {
  def fromKoskiSchema(yo: schema.YlioppilastutkinnonOpiskeluoikeus) = KelaYlioppilastutkinnonOpiskeluoikeus(
    oppilaitos = yo.oppilaitos.map(ol =>
      Oppilaitos(
        ol.oid,
        ol.oppilaitosnumero.map(KelaKoodistokoodiviite.fromKoskiSchema),
        ol.nimi,
        ol.kotipaikka.map(KelaKoodistokoodiviite.fromKoskiSchema)
      )
    ),
    koulutustoimija = yo.koulutustoimija.map(kt =>
      Koulutustoimija(
        kt.oid,
        kt.nimi,
        kt.yTunnus,
        kt.kotipaikka.map(KelaKoodistokoodiviite.fromKoskiSchema)
      )
    ),
    tila = KelaOpiskeluoikeudenTila(
      yo.tila.opiskeluoikeusjaksot.map(oj =>
        KelaOpiskeluoikeusjakso(oj.alku, KelaKoodistokoodiviite.fromKoskiSchema(oj.tila))
      )
    ),
    suoritukset = yo.suoritukset.map(s => KelaYlioppilastutkinnonPäätasonSuoritus(
      KelaYlioppilastutkinnonSuorituksenKoulutusmoduuli(
        KelaKoodistokoodiviite.fromKoskiSchema(s.koulutusmoduuli.tunniste),
        s.koulutusmoduuli.perusteenDiaarinumero,
        s.koulutusmoduuli.koulutustyyppi.map(KelaKoodistokoodiviite.fromKoskiSchema)
      ),
      s.toimipiste.map(t => Toimipiste(
        t.oid,
        t.nimi,
        t.kotipaikka.map(KelaKoodistokoodiviite.fromKoskiSchema)
      )),
      vahvistus = s.vahvistus.map(v => Vahvistus(v.päivä)),
      osasuoritukset = s.osasuoritukset.map(opt => opt.map(os => KelaYlioppilastutkinnonOsasuoritus(
        KelaYlioppilastutkinnonOsasuorituksenKoulutusmoduuli(
          KelaKoodistokoodiviite.fromKoskiSchema(os.koulutusmoduuli.tunniste)
        ),
        os.arviointi.map(opt => opt.map(a => KelaYlioppilastutkinnonOsasuorituksenArvionti(None, Some(a.hyväksytty), a.arviointipäivä))),
        os.tyyppi,
        os.tila.map(KelaKoodistokoodiviite.fromKoskiSchema),
        Some(KelaYlioppilastutkinnonTutkintokerta(
          os.tutkintokerta.koodiarvo,
          os.tutkintokerta.vuosi,
          os.tutkintokerta.vuodenaika
        ))
      ))),
      tyyppi = s.tyyppi,
      tila = s.tila.map(KelaKoodistokoodiviite.fromKoskiSchema),
      alkamispäivä = s.alkamispäivä,
      pakollisetKokeetSuoritettu = Some(s.pakollisetKokeetSuoritettu)
    )),
    tyyppi = yo.tyyppi
  )
}

@Title("Ylioppilastutkinnon opiskeluoikeus")
@Description("Ylioppilastutkinnon opiskeluoikeus")
case class KelaYlioppilastutkinnonOpiskeluoikeus(
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  tila: KelaOpiskeluoikeudenTila,
  suoritukset: List[KelaYlioppilastutkinnonPäätasonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.ylioppilastutkinto.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
) extends KelaOpiskeluoikeus {
  override def arvioituPäättymispäivä = None
  override def alkamispäivä = None
  override def päättymispäivä = None
  override def oid = None
  override def versionumero = None
  override def lisätiedot = None
  override def sisältyyOpiskeluoikeuteen = None
  override def organisaatioHistoria = None
  override def organisaatiohistoria = None
  override def aikaleima = None

  override def withEmptyArvosana: KelaOpiskeluoikeus = this
  override def withOrganisaatiohistoria: KelaOpiskeluoikeus = this
}

@Title("Ylioppilastutkinnon suoritus")
case class KelaYlioppilastutkinnonPäätasonSuoritus(
  koulutusmoduuli: KelaYlioppilastutkinnonSuorituksenKoulutusmoduuli,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  osasuoritukset: Option[List[KelaYlioppilastutkinnonOsasuoritus]],
  tyyppi: schema.Koodistokoodiviite,
  tila: Option[KelaKoodistokoodiviite],
  alkamispäivä: Option[LocalDate],
  pakollisetKokeetSuoritettu: Option[Boolean],
) extends Suoritus {
  override def withEmptyArvosana: Suoritus = this
}

@Title("Ylioppilastutkinnon osasuoritus")
case class KelaYlioppilastutkinnonOsasuoritus(
  koulutusmoduuli: KelaYlioppilastutkinnonOsasuorituksenKoulutusmoduuli,
  arviointi: Option[List[KelaYlioppilastutkinnonOsasuorituksenArvionti]],
  tyyppi: schema.Koodistokoodiviite,
  tila: Option[KelaKoodistokoodiviite],
  tutkintokerta: Option[KelaYlioppilastutkinnonTutkintokerta],
) extends Osasuoritus {
  override def withEmptyArvosana: Osasuoritus = this
}

case class KelaYlioppilastutkinnonSuorituksenKoulutusmoduuli(
  tunniste: KelaKoodistokoodiviite,
  perusteenDiaarinumero: Option[String],
  koulutustyyppi: Option[KelaKoodistokoodiviite],
) extends SuorituksenKoulutusmoduuli

case class KelaYlioppilastutkinnonOsasuorituksenKoulutusmoduuli(
  tunniste: KelaKoodistokoodiviite,
) extends OsasuorituksenKoulutusmoduuli

case class KelaYlioppilastutkinnonTutkintokerta(
  koodiarvo: String,
  vuosi: Int,
  vuodenaika: schema.LocalizedString
)

case class KelaYlioppilastutkinnonOsasuorituksenArvionti(
  arvosana: Option[schema.Koodistokoodiviite],
  hyväksytty: Option[Boolean],
  päivä: Option[LocalDate]
) extends OsasuorituksenArvionti {
  override def withEmptyArvosana: KelaYlioppilastutkinnonOsasuorituksenArvionti = copy(arvosana = None)
}
