package fi.oph.koski.kela

import fi.oph.koski.koskiuser.Rooli
import fi.oph.koski.schema.{HenkilövahvistusPaikkakunnalla, Koodistokoodiviite, OpiskeluoikeudenTyyppi, Päivämäärävahvistus}
import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, KoodistoUri, SensitiveData}
import fi.oph.scalaschema.annotation.Title

import java.time.{LocalDate, LocalDateTime}

// TODO: TOR-2052 - EB-tutkinto rinnalle

@Title("European School of Helsingin opiskeluoikeus")
case class KelaESHOpiskeluoikeus(
  oid: Option[String],
  versionumero: Option[Int],
  aikaleima: Option[LocalDateTime],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  arvioituPäättymispäivä: Option[LocalDate],
  tila: KelaESHOpiskeluoikeudenTila,
  suoritukset: List[KelaESHPäätasonSuoritus],
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.europeanschoolofhelsinki.koodiarvo)
  tyyppi: Koodistokoodiviite,
  lisätiedot: Option[KelaESHOpiskeluoikeudenLisätiedot],
  organisaatioHistoria: Option[List[OrganisaatioHistoria]],
  organisaatiohistoria: Option[List[OrganisaatioHistoria]],
) extends KelaOpiskeluoikeus {
  def withEmptyArvosana: KelaESHOpiskeluoikeus = copy(
    suoritukset = suoritukset.map(_.withEmptyArvosana)
  )
  override def withOrganisaatiohistoria: KelaESHOpiskeluoikeus = copy(
    organisaatioHistoria = organisaatiohistoria,
    organisaatiohistoria = None
  )
  override def sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus] = None
}

@Title("European School of Helsingin opiskeluoikeuden tila")
case class KelaESHOpiskeluoikeudenTila(
  opiskeluoikeusjaksot: List[KelaESHOpiskeluoikeudenJakso]
) extends OpiskeluoikeudenTila

@Title("European School of Helsingin opiskeluoikeusjakso")
case class KelaESHOpiskeluoikeudenJakso(
  @KoodistoUri("koskiopiskeluoikeudentila")
  @KoodistoKoodiarvo("eronnut")
  @KoodistoKoodiarvo("lasna")
  @KoodistoKoodiarvo("mitatoity")
  @KoodistoKoodiarvo("valiaikaisestikeskeytynyt")
  @KoodistoKoodiarvo("valmistunut")
  tila: KelaKoodistokoodiviite,
  alku: LocalDate,
  @KoodistoKoodiarvo("6")
  opintojenRahoitus: Option[Koodistokoodiviite],
) extends Opiskeluoikeusjakso

@Title("European School of Helsingin päätason suoritus")
trait KelaESHPäätasonSuoritus extends Suoritus {
  def withEmptyArvosana: KelaESHPäätasonSuoritus
}

@Title("Secondary lower vuosiluokan suoritus")
case class KelaESHSecondaryLowerVuosiluokanSuoritus(
  koulutusmoduuli: KelaESHSecondaryLowerLuokkaAste,
  toimipiste: Toimipiste,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla],
  @KoodistoUri("suorituksentyyppi")
  @KoodistoKoodiarvo("europeanschoolofhelsinkivuosiluokkasecondarylower")
  tyyppi: Koodistokoodiviite,
  jääLuokalle: Boolean,
  osasuoritukset: Option[List[KelaESHSecondaryLowerOppiaineenSuoritus]],
) extends KelaESHPäätasonSuoritus {
  def withEmptyArvosana: KelaESHSecondaryLowerVuosiluokanSuoritus = copy(
    osasuoritukset = osasuoritukset.map(_.map(_.withEmptyArvosana)),
  )
}

@Title("Secondary lower luokka-aste")
case class KelaESHSecondaryLowerLuokkaAste(
  @KoodistoUri("europeanschoolofhelsinkiluokkaaste")
  @KoodistoKoodiarvo("S1")
  @KoodistoKoodiarvo("S2")
  @KoodistoKoodiarvo("S3")
  @KoodistoKoodiarvo("S4")
  @KoodistoKoodiarvo("S5")
  tunniste: Koodistokoodiviite,
  @KoodistoUri("europeanschoolofhelsinkicurriculum")
  curriculum: Koodistokoodiviite,
  @KoodistoUri("koulutustyyppi")
  @KoodistoKoodiarvo("21")
  koulutustyyppi: Option[Koodistokoodiviite],
) extends SuorituksenKoulutusmoduuli


@Title("Secondary lower oppiaineen suoritus")
case class KelaESHSecondaryLowerOppiaineenSuoritus(
  koulutusmoduuli: KelaESHSecondaryGradeOppiaine,
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  yksilöllistettyOppimäärä: Boolean,
  arviointi: Option[List[KelaESHArviointi]],
  @KoodistoUri("suorituksentyyppi")
  @KoodistoKoodiarvo("europeanschoolofhelsinkiosasuoritussecondarylower")
  tyyppi: Koodistokoodiviite,
) extends Osasuoritus {
  def withEmptyArvosana: KelaESHSecondaryLowerOppiaineenSuoritus = copy(arviointi = None)
}

@Title("Secondary upper vuosiluokan suoritus")
case class KelaESHSecondaryUpperVuosiluokanSuoritus(
  koulutusmoduuli: KelaESHSecondaryUpperLuokkaAste,
  toimipiste: Toimipiste,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla],
  @KoodistoUri("suorituksentyyppi")
  @KoodistoKoodiarvo("europeanschoolofhelsinkivuosiluokkasecondaryupper")
  tyyppi: Koodistokoodiviite,
  jääLuokalle: Boolean,
  osasuoritukset: Option[List[KelaESHSecondaryUpperOppiaineenSuoritus]],
) extends KelaESHPäätasonSuoritus {
  def withEmptyArvosana: KelaESHSecondaryUpperVuosiluokanSuoritus = copy(
    osasuoritukset = osasuoritukset.map(_.map(_.withEmptyArvosana)),
  )
}

@Title("Secondary upper luokka-aste")
case class KelaESHSecondaryUpperLuokkaAste(
  @KoodistoUri("europeanschoolofhelsinkiluokkaaste")
  @KoodistoKoodiarvo("S6")
  @KoodistoKoodiarvo("S7")
  tunniste: Koodistokoodiviite,
  @KoodistoUri("europeanschoolofhelsinkicurriculum")
  curriculum: Koodistokoodiviite,
  @KoodistoUri("koulutustyyppi")
  @KoodistoKoodiarvo("21")
  koulutustyyppi: Option[Koodistokoodiviite],
) extends SuorituksenKoulutusmoduuli

@Title("Secondary upper oppiaineen suoritus")
trait KelaESHSecondaryUpperOppiaineenSuoritus extends Osasuoritus {
  def withEmptyArvosana: KelaESHSecondaryUpperOppiaineenSuoritus
}

@Title("Secondary grade -oppiaine")
case class KelaESHSecondaryUpperOppiaineenSuoritusS6(
  koulutusmoduuli: KelaESHSecondaryGradeOppiaine,
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  yksilöllistettyOppimäärä: Boolean,
  arviointi: Option[List[KelaESHArviointi]],
  @KoodistoUri("suorituksentyyppi")
  @KoodistoKoodiarvo("europeanschoolofhelsinkiosasuorituss6")
  tyyppi: Koodistokoodiviite,
) extends KelaESHSecondaryUpperOppiaineenSuoritus {
  def withEmptyArvosana: KelaESHSecondaryUpperOppiaineenSuoritusS6 = copy(arviointi = None)
}

@Title("Secondary upper vuosiluokan suoritus")
case class KelaESHSecondaryUpperOppiaineenSuoritusS7(
  koulutusmoduuli: KelaESHSecondaryGradeOppiaine,
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA))
  yksilöllistettyOppimäärä: Boolean,
  arviointi: Option[List[KelaESHArviointi]],
  @KoodistoUri("suorituksentyyppi")
  @KoodistoKoodiarvo("europeanschoolofhelsinkiosasuorituss7")
  tyyppi: Koodistokoodiviite,
  osasuoritukset: Option[List[KelaESHS7OppiaineenAlaosasuoritus]],
) extends KelaESHSecondaryUpperOppiaineenSuoritus {
  def withEmptyArvosana: KelaESHSecondaryUpperOppiaineenSuoritusS7 = copy(
    arviointi = None,
    osasuoritukset = osasuoritukset.map(_.map(_.withEmptyArvosana)),
  )
}

@Title("Secondary grade oppiaine")
trait KelaESHSecondaryGradeOppiaine extends SuorituksenKoulutusmoduuli

@Title("European School of Helsingin kielioppiaine")
case class KelaESHKielioppiaine(
  @KoodistoUri("europeanschoolofhelsinkikielioppiaine")
  tunniste: Koodistokoodiviite,
  laajuus: Option[KelaLaajuus],
  @KoodistoUri("kieli")
  kieli: Koodistokoodiviite,
) extends KelaESHSecondaryGradeOppiaine

@Title("European School of Helsingin muu oppiaine")
case class KelaESHMuuOppiaine(
  @KoodistoUri("europeanschoolofhelsinkimuuoppiaine")
  tunniste: Koodistokoodiviite,
  laajuus: Option[KelaLaajuus],
) extends KelaESHSecondaryGradeOppiaine

@Title("European School of Helsingin S7-luokan oppiaineen alaosasuoritus")
case class KelaESHS7OppiaineenAlaosasuoritus(
  koulutusmoduuli: KelaESHS7Oppiainekomponentti,
  arviointi: Option[List[KelaESHArviointi]],
  @KoodistoUri("suorituksentyyppi")
  @KoodistoKoodiarvo("europeanschoolofhelsinkialaosasuorituss7")
  tyyppi: Koodistokoodiviite,
) extends Suoritus {
  def osasuoritukset: Option[List[Osasuoritus]] = None
  def withEmptyArvosana: KelaESHS7OppiaineenAlaosasuoritus = this.copy(arviointi = None)
}

@Title("European School of Helsingin S7-luokan oppiainekomponentti")
case class KelaESHS7Oppiainekomponentti(
  @KoodistoUri("europeanschoolofhelsinkis7oppiaineenkomponentti")
  tunniste: Koodistokoodiviite,
) extends SuorituksenKoulutusmoduuli

case class KelaESHArviointi(
  päivä: Option[LocalDate],
  hyväksytty: Option[Boolean],
) extends OsasuorituksenArvionti {
  override def arvosana: Option[Koodistokoodiviite] = None
  override def withEmptyArvosana: OsasuorituksenArvionti = this
}

@Title("European School of Helsingin lisätiedot")
case class KelaESHOpiskeluoikeudenLisätiedot(
  ulkomaanjaksot: Option[List[Ulkomaanjakso]],
  maksuttomuus: Option[List[KelaMaksuttomuus]],
  oikeuttaMaksuttomuuteenPidennetty: Option[List[KelaOikeuttaMaksuttomuuteenPidennetty]],
) extends OpiskeluoikeudenLisätiedot
