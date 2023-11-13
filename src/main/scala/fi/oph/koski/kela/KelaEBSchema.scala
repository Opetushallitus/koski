package fi.oph.koski.kela

import fi.oph.koski.schema
import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, KoodistoUri}
import fi.oph.scalaschema.annotation.Title

import java.time.{LocalDate, LocalDateTime}

@Title("EB-tutkinnon opiskeluoikeus")
case class KelaEBOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  aikaleima: Option[LocalDateTime],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  arvioituPäättymispäivä: Option[LocalDate],
  tila: KelaEBOpiskeluoikeudenTila,
  suoritukset: List[KelaEBTutkinnonSuoritus],
  @KoodistoKoodiarvo(schema.OpiskeluoikeudenTyyppi.ebtutkinto.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
  organisaatioHistoria: Option[List[OrganisaatioHistoria]],
  organisaatiohistoria: Option[List[OrganisaatioHistoria]],
) extends KelaOpiskeluoikeus {
  def withHyväksyntämerkinnälläKorvattuArvosana: KelaEBOpiskeluoikeus = copy(suoritukset = suoritukset.map(_.withHyväksyntämerkinnälläKorvattuArvosana))

  override def withOrganisaatiohistoria: KelaEBOpiskeluoikeus = copy(
    organisaatioHistoria = organisaatiohistoria,
    organisaatiohistoria = None
  )

  override def sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus] = None
  override def lisätiedot: Option[OpiskeluoikeudenLisätiedot] = None
}

@Title("EB-tutkinnon opiskeluoikeuden tila")
case class KelaEBOpiskeluoikeudenTila(
  opiskeluoikeusjaksot: List[KelaEBOpiskeluoikeudenJakso]
) extends OpiskeluoikeudenTila

@Title("EB-tutkinnon opiskeluoikeuden jakso")
case class KelaEBOpiskeluoikeudenJakso(
  @KoodistoUri("koskiopiskeluoikeudentila")
  @KoodistoKoodiarvo("eronnut")
  @KoodistoKoodiarvo("lasna")
  @KoodistoKoodiarvo("mitatoity")
  @KoodistoKoodiarvo("valmistunut")
  tila: KelaKoodistokoodiviite,
  alku: LocalDate,
) extends Opiskeluoikeusjakso

@Title("EB-tutkinnon suoritus")
case class KelaEBTutkinnonSuoritus(
  koulutusmoduuli: KelaEBTutkinto,
  toimipiste: Toimipiste,
  vahvistus: Option[Vahvistus],
  @Title("Koulutus")
  @KoodistoKoodiarvo("ebtutkinto")
  tyyppi: schema.Koodistokoodiviite,
  override val osasuoritukset: Option[List[KelaEBTutkinnonOsasuoritus]] = None
) extends Suoritus {
  override def withHyväksyntämerkinnälläKorvattuArvosana: KelaEBTutkinnonSuoritus = copy(
    osasuoritukset = osasuoritukset.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana))
  )
}

@Title("EB-tutkinnon osasuoritus")
case class KelaEBTutkinnonOsasuoritus(
  koulutusmoduuli: KelaESHSecondaryGradeOppiaine,
  @KoodistoKoodiarvo("ebtutkinnonosasuoritus")
  tyyppi: schema.Koodistokoodiviite,
  osasuoritukset: Option[List[KelaEBOppiaineenAlaosasuoritus]] = None
) extends Osasuoritus {
  override def withHyväksyntämerkinnälläKorvattuArvosana: KelaEBTutkinnonOsasuoritus = copy(osasuoritukset = osasuoritukset.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana)))
}

@Title("EB-oppiaineen alaosasuoritus")
case class KelaEBOppiaineenAlaosasuoritus(
  @Title("Arviointikomponentti")
  koulutusmoduuli: KelaEBOppiaineKomponentti,
  arviointi: Option[List[KelaEBArviointi]] = None,
  @KoodistoKoodiarvo("ebtutkinnonalaosasuoritus")
  tyyppi: schema.Koodistokoodiviite
) extends Osasuoritus {
  override def withHyväksyntämerkinnälläKorvattuArvosana: KelaEBOppiaineenAlaosasuoritus = copy(
    arviointi = arviointi.map(_.map(_.withHyväksyntämerkinnälläKorvattuArvosana))
  )
}

@Title("EB-oppiainekomponentti")
case class KelaEBOppiaineKomponentti(
  @KoodistoUri("ebtutkinnonoppiaineenkomponentti")
  @KoodistoKoodiarvo("Final")
  @KoodistoKoodiarvo("Oral")
  @KoodistoKoodiarvo("Written")
  tunniste: schema.Koodistokoodiviite
)

@Title("EB-tutkinnon arviointi")
case class KelaEBArviointi(
  @KoodistoUri("arviointiasteikkoeuropeanschoolofhelsinkifinalmark")
  arvosana: Option[schema.Koodistokoodiviite],
  päivä: Option[LocalDate],
  hyväksytty: Option[Boolean]
) extends OsasuorituksenArviointi {
  override def withHyväksyntämerkinnälläKorvattuArvosana: KelaEBArviointi = copy(
    arvosana = None,
    hyväksytty = arvosana.map(schema.EuropeanSchoolOfHelsinkiArviointi.hyväksytty)
  )
}

@Title("EB-tutkinto")
case class KelaEBTutkinto(
  @KoodistoUri("koulutus")
  @KoodistoKoodiarvo("301104")
  tunniste: schema.Koodistokoodiviite,
  @KoodistoKoodiarvo("21")
  koulutustyyppi: Option[schema.Koodistokoodiviite],
  curriculum: schema.Koodistokoodiviite
) extends SuorituksenKoulutusmoduuli
