package fi.oph.koski.kela

import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, KoodistoUri}
import fi.oph.koski.schema
import fi.oph.scalaschema.annotation.{Description, Title}

import java.time.{LocalDate, LocalDateTime}

@Title("Muun kuin säännellyn koulutuksen opiskeluoikeus")
@Description("Muu kuin säännelty koulutus (MUKS)")
case class KelaMUKSOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  aikaleima: Option[LocalDateTime],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  arvioituPäättymispäivä: Option[LocalDate],
  tila: KelaMUKSOpiskeluoikeudenTila,
  suoritukset: List[KelaMUKSPäätasonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.muukuinsaanneltykoulutus.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
  organisaatioHistoria: Option[List[OrganisaatioHistoria]],
  organisaatiohistoria: Option[List[OrganisaatioHistoria]],
) extends KelaOpiskeluoikeus {
  override def alkamispäivä: Option[LocalDate] = super.alkamispäivä
  override def päättymispäivä: Option[LocalDate] = super.päättymispäivä
  def withEmptyArvosana: KelaMUKSOpiskeluoikeus = copy(
    suoritukset = suoritukset.map(_.withEmptyArvosana)
  )
  override def withOrganisaatiohistoria: KelaMUKSOpiskeluoikeus = copy(
    organisaatioHistoria = organisaatiohistoria,
    organisaatiohistoria = None
  )
  override def sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus] = None

  override def lisätiedot: Option[OpiskeluoikeudenLisätiedot] = None
}

@Title("Muun kuin säännellyn koulutuksen opiskeluoikeuden tila")
case class KelaMUKSOpiskeluoikeudenTila(
  opiskeluoikeusjaksot: List[KelaMUKSOpiskeluoikeudenJakso],
) extends OpiskeluoikeudenTila

@Title("Muun kuin säännellyn koulutuksen opiskeluoikeuden jakso")
case class KelaMUKSOpiskeluoikeudenJakso(
  @KoodistoUri("koskiopiskeluoikeudentila")
  @KoodistoKoodiarvo("lasna")
  @KoodistoKoodiarvo("hyvaksytystisuoritettu")
  @KoodistoKoodiarvo("keskeytynyt")
  @KoodistoKoodiarvo("mitatoity")
  tila: KelaKoodistokoodiviite,
  alku: LocalDate,
  @KoodistoKoodiarvo("14")
  @KoodistoKoodiarvo("15")
  opintojenRahoitus: Option[schema.Koodistokoodiviite],
) extends Opiskeluoikeusjakso

@Title("Muun kuin säännellyn koulutuksen päätason suoritus")
case class KelaMUKSPäätasonSuoritus(
  koulutusmoduuli: KelaMUKSKoulutus,
  tyyppi: schema.Koodistokoodiviite,
  vahvistus: Option[Vahvistus],
  toimipiste: Toimipiste,
  osasuoritukset: Option[List[KelaMUKSOsasuoritus]],
  arviointi: Option[List[KelaMUKSArviointi]],
) extends Suoritus  {
  def withEmptyArvosana: KelaMUKSPäätasonSuoritus = copy(
    osasuoritukset = osasuoritukset.map(_.map(_.withEmptyArvosana)),
  )
}

@Title("Muun kuin säännellyn koulutuksen koulutusmoduuli")
case class KelaMUKSKoulutus(
  @KoodistoUri("koulutus")
  @KoodistoKoodiarvo("999951")
  tunniste: schema.Koodistokoodiviite,
  @KoodistoUri("koulutustyyppi")
  koulutustyyppi: Option[schema.Koodistokoodiviite],
  laajuus: Option[KelaLaajuus],
  @KoodistoUri("opintokokonaisuudet")
  opintokokonaisuus: schema.Koodistokoodiviite,
) extends SuorituksenKoulutusmoduuli

@Title("Muun kuin säännellyn koulutuksen osasuoritus")
case class KelaMUKSOsasuoritus(
  koulutusmoduuli: KelaMUKSOsasuorituksenKoulutusmoduuli,
  tyyppi: schema.Koodistokoodiviite,
  vahvistus: Option[Vahvistus],
  arviointi: Option[List[KelaMUKSArviointi]],
  osasuoritukset: Option[List[KelaMUKSOsasuoritus]],
) extends Osasuoritus {
  override def withEmptyArvosana: KelaMUKSOsasuoritus = this.copy(
    arviointi = arviointi.map(_.map(_.withEmptyArvosana))
  )
}

@Title("Muun kuin säännellyn koulutuksen osasuorituksen koulutusmoduuli")
case class KelaMUKSOsasuorituksenKoulutusmoduuli(
  tunniste: KelaPaikallinenKoodiviite,
  laajuus: KelaLaajuus,
) extends SuorituksenKoulutusmoduuli

@Title("Muun kuin säännellyn koulutuksen arviointi")
case class KelaMUKSArviointi(
  arvosana: Option[schema.Koodistokoodiviite],
  päivä: Option[LocalDate],
  hyväksytty: Option[Boolean],
) extends OsasuorituksenArviointi {
  override def withEmptyArvosana: KelaMUKSArviointi = copy(
    arvosana = None,
    hyväksytty = arvosana.map(schema.MuunKuinSäännellynKoulutuksenArviointi.hyväksytty)
  )
}
