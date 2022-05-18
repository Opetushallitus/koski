package fi.oph.koski.kela

import fi.oph.koski.schema
import fi.oph.koski.schema.OpiskeluoikeudenTyyppi
import fi.oph.koski.schema.annotation.KoodistoKoodiarvo
import fi.oph.koski.schema.annotation.Deprecated
import fi.oph.scalaschema.annotation.{Description, Title}

import java.time.{LocalDate, LocalDateTime}

@Title("DIA-tutkinnon opiskeluoikeus")
@Description("Deutsche Internationale Abitur -tutkinnon opiskeluoikeus")
case class KelaDIAOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  aikaleima: Option[LocalDateTime],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus],
  arvioituPäättymispäivä: Option[LocalDate],
  tila: KelaOpiskeluoikeudenTila,
  suoritukset: List[KelaDIAPäätasonSuoritus],
  lisätiedot: Option[KelaDIAOpiskeluoikeudenLisätiedot],
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.diatutkinto.koodiarvo)
  tyyppi: schema.Koodistokoodiviite,
  organisaatioHistoria: Option[List[OrganisaatioHistoria]],
  organisaatiohistoria: Option[List[OrganisaatioHistoria]]
) extends KelaOpiskeluoikeus {
  override def alkamispäivä: Option[LocalDate] = super.alkamispäivä
  override def päättymispäivä: Option[LocalDate] = super.päättymispäivä
  def withSuorituksetVastaavuusKopioitu: KelaDIAOpiskeluoikeus = copy(
    suoritukset = suoritukset.map(_.withOsasuorituksetVastaavuusKopioitu)
  )
  def withEmptyArvosana: KelaDIAOpiskeluoikeus = copy(
    suoritukset = suoritukset.map(_.withEmptyArvosana)
  )
  override def withOrganisaatiohistoria: KelaOpiskeluoikeus = copy(
    organisaatioHistoria = organisaatiohistoria,
    organisaatiohistoria = None
  )
}

case class KelaDIAOpiskeluoikeudenLisätiedot(
  ulkomaanjaksot: Option[List[Ulkomaanjakso]],
  ulkomainenVaihtoopiskelija: Option[Boolean],
  maksuttomuus: Option[List[schema.Maksuttomuus]],
  oikeuttaMaksuttomuuteenPidennetty: Option[List[schema.OikeuttaMaksuttomuuteenPidennetty]]
) extends OpiskeluoikeudenLisätiedot

@Title("DIA-tutkinnon suoritus")
case class KelaDIAPäätasonSuoritus(
  koulutusmoduuli: KelaDIASuorituksenKoulutusmoduuli,
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  osasuoritukset: Option[List[KelaDIAOsasuoritus]],
  tyyppi: schema.Koodistokoodiviite,
  tila: Option[KelaKoodistokoodiviite]
) extends Suoritus {
  def withOsasuorituksetVastaavuusKopioitu: KelaDIAPäätasonSuoritus = copy(
    osasuoritukset = osasuoritukset.map(os => os.map(_.withVastaavuusKopioitu))
  )
  def withEmptyArvosana = copy(
    osasuoritukset = osasuoritukset.map(_.map(_.withEmptyArvosana))
  )
}

@Title("DIA-tutkinnon osasuoritus")
case class KelaDIAOsasuoritus(
  koulutusmoduuli: KelaDIAOsasuorituksenKoulutusmoduuli,
  arviointi: Option[List[KelaDIAOsasuorituksenArvionti]],
  osasuoritukset: Option[List[KelaDIAOsasuoritus]],
  tyyppi: schema.Koodistokoodiviite,
  tila: Option[KelaKoodistokoodiviite],
  vastaavuusTodistuksenTiedot: Option[VastaavuusTodistuksenTiedot],
  @Deprecated("Ei palauteta Kela-API:ssa. Kenttä on näkyvissä skeemassa vain teknisistä syistä.")
  vastaavuustodistuksenTiedot: Option[VastaavuusTodistuksenTiedot]
) extends Osasuoritus {
  def withVastaavuusKopioitu: KelaDIAOsasuoritus = copy(
    vastaavuusTodistuksenTiedot = vastaavuustodistuksenTiedot,
    vastaavuustodistuksenTiedot = None
  )
  def withEmptyArvosana: KelaDIAOsasuoritus = copy(
    arviointi = arviointi.map(_.map(_.withEmptyArvosana)),
    osasuoritukset = osasuoritukset.map(_.map(_.withEmptyArvosana))
  )
}

case class KelaDIAOsasuorituksenArvionti(
  arvosana: Option[schema.Koodistokoodiviite],
  hyväksytty: Option[Boolean],
  päivä: Option[LocalDate]
) extends OsasuorituksenArvionti {
  def withEmptyArvosana: KelaDIAOsasuorituksenArvionti = copy(
    arvosana = None,
    hyväksytty = Some(true)
  )
}

case class KelaDIASuorituksenKoulutusmoduuli(
  tunniste: KelaKoodistokoodiviite,
  koulutustyyppi: Option[KelaKoodistokoodiviite],
) extends SuorituksenKoulutusmoduuli

case class KelaDIAOsasuorituksenKoulutusmoduuli(
  tunniste: KelaKoodistokoodiviite,
  laajuus: Option[KelaLaajuus],
  kieli: Option[KelaKoodistokoodiviite],
  osaAlue: Option[KelaKoodistokoodiviite],
  kurssinTyyppi: Option[KelaKoodistokoodiviite],
  oppimäärä: Option[KelaKoodistokoodiviite]
) extends OsasuorituksenKoulutusmoduuli

case class VastaavuusTodistuksenTiedot(
  lukioOpintojenLaajuus: KelaLaajuus
)
