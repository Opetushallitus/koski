package fi.oph.koski.kela

import fi.oph.koski.koskiuser.Rooli
import fi.oph.koski.schema
import fi.oph.koski.schema.LocalizedString
import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, SensitiveData}
import fi.oph.scalaschema.annotation.{Description, Title}

import java.time.{LocalDate, LocalDateTime}

@Title("Ammatillisen koulutuksen opiskeluoikeus")
@Description("Ammatillisen koulutuksen opiskeluoikeus")
case class KelaAmmatillinenOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  aikaleima: Option[LocalDateTime],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  sisältyyOpiskeluoikeuteen: Option[Sisältäväopiskeluoikeus],
  arvioituPäättymispäivä: Option[LocalDate],
  ostettu: Option[Boolean],
  tila: KelaAmmatillisenOpiskeluoikeudenTila,
  suoritukset: List[KelaAmmatillinenPäätasonSuoritus],
  lisätiedot: Option[KelaAmmatillisenOpiskeluoikeudenLisätiedot],
  @KoodistoKoodiarvo("ammatillinenkoulutus")
  tyyppi: schema.Koodistokoodiviite,
  organisaatioHistoria: Option[List[OrganisaatioHistoria]]
) extends KelaOpiskeluoikeus {
  override def alkamispäivä: Option[LocalDate] = super.alkamispäivä
  override def päättymispäivä: Option[LocalDate] = super.päättymispäivä
  def withEmptyArvosana: KelaAmmatillinenOpiskeluoikeus = copy(
    suoritukset = suoritukset.map(_.withEmptyArvosana)
  )
}

case class KelaAmmatillisenOpiskeluoikeudenTila(
  opiskeluoikeusjaksot: List[KelaAmmatillisenOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

case class KelaAmmatillisenOpiskeluoikeusjakso(
  alku: LocalDate,
  tila: KelaKoodistokoodiviite,
  opintojenRahoitus: Option[KelaKoodistokoodiviite]
) extends Opiskeluoikeusjakso

case class KelaAmmatillisenOpiskeluoikeudenLisätiedot(
  majoitus: Option[List[schema.Aikajakso]],
  sisäoppilaitosmainenMajoitus: Option[List[schema.Aikajakso]],
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  vaativanErityisenTuenYhteydessäJärjestettäväMajoitus: Option[List[schema.Aikajakso]],
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  erityinenTuki: Option[List[schema.Aikajakso]],
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  vaativanErityisenTuenErityinenTehtävä: Option[List[schema.Aikajakso]],
  ulkomaanjaksot: Option[List[Ulkomaanjakso]],
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  hojks: Option[Hojks],
  osaAikaisuusjaksot: Option[List[schema.OsaAikaisuusJakso]],
  opiskeluvalmiuksiaTukevatOpinnot: Option[List[schema.OpiskeluvalmiuksiaTukevienOpintojenJakso]],
  vankilaopetuksessa: Option[List[schema.Aikajakso]],
  maksuttomuus: Option[List[schema.Maksuttomuus]],
  oikeuttaMaksuttomuuteenPidennetty: Option[List[schema.OikeuttaMaksuttomuuteenPidennetty]]
) extends OpiskeluoikeudenLisätiedot

@Title("Ammatillisen koulutuksen suoritus")
case class KelaAmmatillinenPäätasonSuoritus(
  koulutusmoduuli: KelaAmmatillisenSuorituksenKoulutusmoduuli,
  suoritustapa: Option[KelaKoodistokoodiviite],
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  osasuoritukset: Option[List[KelaAmmatillinenOsasuoritus]],
  tyyppi: schema.Koodistokoodiviite,
  tila: Option[KelaKoodistokoodiviite],
  osaamisala: Option[List[schema.Osaamisalajakso]],
  toinenOsaamisala: Option[Boolean],
  alkamispäivä: Option[LocalDate],
  järjestämismuodot: Option[List[Järjestämismuotojakso]],
  osaamisenHankkimistavat: Option[List[OsaamisenHankkimistapajakso]],
  työssäoppimisjaksot: Option[List[Työssäoppimisjakso]],
  koulutussopimukset: Option[List[Koulutussopimusjakso]],
  tutkintonimike: Option[List[KelaKoodistokoodiviite]],
  toinenTutkintonimike: Option[Boolean],
) extends Suoritus {
  def withEmptyArvosana = copy(
    osasuoritukset = osasuoritukset.map(_.map(_.withEmptyArvosana))
  )
}

@Title("Ammatillisen koulutuksen osasuoritus")
case class KelaAmmatillinenOsasuoritus(
  koulutusmoduuli: KelaAmmatillisenOsasuorituksenKoulutusmoduuli,
  liittyyTutkinnonOsaan: Option[KelaKoodistokoodiviite],
  arviointi: Option[List[KelaAmmatillisenOsasuorituksenArvionti]],
  toimipiste: Option[Toimipiste],
  vahvistus: Option[Vahvistus],
  osasuoritukset: Option[List[KelaAmmatillinenOsasuoritus]],
  tyyppi: schema.Koodistokoodiviite,
  tila: Option[KelaKoodistokoodiviite],
  tutkinto: Option[Tutkinto],
  tutkinnonOsanRyhmä: Option[KelaKoodistokoodiviite],
  osaamisala: Option[List[schema.Osaamisalajakso]],
  alkamispäivä: Option[LocalDate],
  tunnustettu: Option[OsaamisenTunnustaminen],
  toinenOsaamisala: Option[Boolean],
  toinenTutkintonimike: Option[Boolean],
  näyttö: Option[Näyttö],
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  lisätiedot: Option[List[AmmatillisenTutkinnonOsanLisätieto]]
) extends Osasuoritus {
  def withEmptyArvosana: KelaAmmatillinenOsasuoritus = copy(
    arviointi = arviointi.map(_.map(_.withEmptyArvosana)),
    osasuoritukset = osasuoritukset.map(_.map(_.withEmptyArvosana))
  )
}

case class KelaAmmatillisenOsasuorituksenArvionti(
  arvosana: Option[schema.Koodistokoodiviite],
  hyväksytty: Option[Boolean],
  päivä: Option[LocalDate]
) extends OsasuorituksenArvionti {
  def withEmptyArvosana: KelaAmmatillisenOsasuorituksenArvionti = copy(
    arvosana = None,
    hyväksytty = arvosana.map(schema.AmmatillinenKoodistostaLöytyväArviointi.hyväksytty)
  )
}

case class KelaAmmatillisenSuorituksenKoulutusmoduuli(
  tunniste: KelaKoodistokoodiviite,
  laajuus: Option[KelaLaajuus],
  perusteenDiaarinumero: Option[String],
  perusteenNimi: Option[schema.LocalizedString],
  koulutustyyppi: Option[KelaKoodistokoodiviite],
  pakollinen: Option[Boolean],
  kuvaus: Option[schema.LocalizedString],
  kieli: Option[KelaKoodistokoodiviite],
) extends SuorituksenKoulutusmoduuli

case class KelaAmmatillisenOsasuorituksenKoulutusmoduuli(
  tunniste: KelaKoodistokoodiviite,
  laajuus: Option[KelaLaajuus],
  perusteenNimi: Option[schema.LocalizedString],
  pakollinen: Option[Boolean],
  kieli: Option[KelaKoodistokoodiviite],
) extends OsasuorituksenKoulutusmoduuli

case class Hojks(
  opetusryhmä: KelaKoodistokoodiviite,
  alku: Option[LocalDate],
  loppu: Option[LocalDate]
)

case class Näyttö(
  suorituspaikka: Option[NäytönSuorituspaikka],
  suoritusaika: Option[schema.NäytönSuoritusaika],
  työssäoppimisenYhteydessä: Boolean,
  arviointi: Option[NäytönArviointi],
)

case class NäytönSuorituspaikka(
  tunniste: KelaKoodistokoodiviite,
  kuvaus: schema.LocalizedString
)

case class NäytönArviointi(
  hyväksytty: Boolean
)

case class Tutkinto(
  tunniste: KelaKoodistokoodiviite,
  perusteenDiaarinumero: Option[String],
  perusteenNimi: Option[schema.LocalizedString],
  koulutustyyppi: Option[KelaKoodistokoodiviite]
)

case class AmmatillisenTutkinnonOsanLisätieto(
  tunniste: KelaKoodistokoodiviite,
  kuvaus: schema.LocalizedString
)
