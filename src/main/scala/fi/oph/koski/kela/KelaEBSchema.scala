package fi.oph.koski.kela

import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, KoodistoUri}
import fi.oph.koski.schema.{Arvioitsija, HenkilövahvistusPaikkakunnalla, Koodistokoodiviite, OpiskeluoikeudenTyyppi}
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
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.ebtutkinto.koodiarvo)
  tyyppi: Koodistokoodiviite,
  organisaatioHistoria: Option[List[OrganisaatioHistoria]],
  organisaatiohistoria: Option[List[OrganisaatioHistoria]],
) extends KelaOpiskeluoikeus {
  def withEmptyArvosana: KelaEBOpiskeluoikeus = copy(suoritukset = suoritukset.map(_.withEmptyArvosana))

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
  vahvistus: Option[HenkilövahvistusPaikkakunnalla],
  @Title("Koulutus")
  @KoodistoKoodiarvo("ebtutkinto")
  tyyppi: Koodistokoodiviite,
  override val osasuoritukset: Option[List[KelaEBTutkinnonOsasuoritus]] = None
) extends Suoritus {
  override def withEmptyArvosana: KelaEBTutkinnonSuoritus = this
}

@Title("EB-tutkinnon osasuoritus")
case class KelaEBTutkinnonOsasuoritus(
  koulutusmoduuli: KelaESHSecondaryGradeOppiaine,
  @KoodistoKoodiarvo("ebtutkinnonosasuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "ebtutkinnonosasuoritus", koodistoUri = "suorituksentyyppi"),
  suorituskieli: Koodistokoodiviite,
  osasuoritukset: Option[List[KelaEBOppiaineenAlaosasuoritus]] = None
) extends Osasuoritus {
  override def withEmptyArvosana: Osasuoritus = copy(osasuoritukset = osasuoritukset.map(_.map(_.withEmptyArvosana)))
}

@Title("EB-oppiaineen alaosasuoritus")
case class KelaEBOppiaineenAlaosasuoritus(
  @Title("Arviointikomponentti")
  koulutusmoduuli: KelaEBOppiaineKomponentti,
  arviointi: Option[List[KelaEBArviointi]] = None,
  @KoodistoKoodiarvo("ebtutkinnonalaosasuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "ebtutkinnonalaosasuoritus", koodistoUri = "suorituksentyyppi")
) extends Osasuoritus {
  override def withEmptyArvosana: KelaEBOppiaineenAlaosasuoritus = copy(arviointi = None)
}

@Title("EB-oppiainekomponentti")
case class KelaEBOppiaineKomponentti(
  @KoodistoUri("ebtutkinnonoppiaineenkomponentti")
  @KoodistoKoodiarvo("Final")
  @KoodistoKoodiarvo("Oral")
  @KoodistoKoodiarvo("Written")
  tunniste: Koodistokoodiviite
)

@Title("EB-tutkinnon arviointi")
case class KelaEBArviointi(
  @KoodistoUri("arviointiasteikkoeuropeanschoolofhelsinkifinalmark")
  arvosana: Koodistokoodiviite,
  päivä: Option[LocalDate],
  arvioitsijat: Option[List[Arvioitsija]] = None
)

@Title("EB-tutkinto")
case class KelaEBTutkinto(
  @KoodistoUri("koulutus")
  @KoodistoKoodiarvo("301104")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("301104", "koulutus"),
  @KoodistoKoodiarvo("21")
  koulutustyyppi: Option[Koodistokoodiviite] = None,
  curriculum: Koodistokoodiviite = Koodistokoodiviite("2023", "europeanschoolofhelsinkicurriculum")
) extends SuorituksenKoulutusmoduuli
