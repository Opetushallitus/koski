package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.schema.annotation.{ComplexObject, KoodistoKoodiarvo, KoodistoUri, Tooltip}
import fi.oph.scalaschema.annotation.{Description, MinItems, Title}

case class MuunAmmatillisenKoulutuksenSuoritus(
  koulutusmoduuli: MuuAmmatillinenKoulutus,
  täydentääTutkintoa: Option[AmmatillinenTutkintoKoulutus],
  toimipiste: OrganisaatioWithOid,
  override val alkamispäivä: Option[LocalDate],
  vahvistus: Option[HenkilövahvistusValinnaisellaPaikkakunnalla] = None,
  suorituskieli: Koodistokoodiviite,
  @Description("Osaamisen hankkimistavat eri ajanjaksoina.")
  @Tooltip("Osaamisen hankkimistavat (oppisopimus, koulutussopimus, oppilaitosmuotoinen koulutus) eri ajanjaksoina.")
  osaamisenHankkimistavat: Option[List[OsaamisenHankkimistapajakso]] = None,
  koulutussopimukset: Option[List[Koulutussopimusjakso]] = None,
  @Description("Suoritukseen kuuluvien osasuoritusten suoritukset")
  override val osasuoritukset: Option[List[MuuAmmatillinenOsasuoritus]],
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("muuammatillinenkoulutus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("muuammatillinenkoulutus", "suorituksentyyppi"),
  ryhmä: Option[String] = None
) extends AmmatillinenPäätasonSuoritus with Todistus with Toimipisteellinen with Ryhmällinen with Työssäoppimisjaksoton with Arvioinniton

case class TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus(
  koulutusmoduuli: PaikallinenMuuAmmatillinenKoulutus,
  toimipiste: OrganisaatioWithOid,
  override val alkamispäivä: Option[LocalDate],
  vahvistus: Option[HenkilövahvistusValinnaisellaPaikkakunnalla] = None,
  suorituskieli: Koodistokoodiviite,
  @Description("Osaamisen hankkimistavat eri ajanjaksoina.")
  @Tooltip("Osaamisen hankkimistavat (oppisopimus, koulutussopimus, oppilaitosmuotoinen koulutus) eri ajanjaksoina.")
  osaamisenHankkimistavat: Option[List[OsaamisenHankkimistapajakso]] = None,
  koulutussopimukset: Option[List[Koulutussopimusjakso]] = None,
  @Description("Tutkinnon osaa pienempään kokonaisuuteen kuuluvien osasuoritusten suoritukset")
  @MinItems(1)
  override val osasuoritukset: Option[List[TutkinnonOsaaPienemmänKokonaisuudenSuoritus]],
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("tutkinnonosaapienemmistäkokonaisuuksistakoostuvasuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("tutkinnonosaapienemmistäkokonaisuuksistakoostuvasuoritus", "suorituksentyyppi"),
  ryhmä: Option[String] = None
) extends AmmatillinenPäätasonSuoritus with Todistus with Toimipisteellinen with Ryhmällinen with Työssäoppimisjaksoton with Arvioinniton

sealed trait Työssäoppimisjaksoton extends AmmatillinenPäätasonSuoritus {
  override def työssäoppimisjaksot: Option[List[Työssäoppimisjakso]] = None
}

sealed trait MuuAmmatillinenKoulutus extends Koulutusmoduuli

case class AmmatilliseenTehtäväänValmistavaKoulutus(
  @KoodistoUri("ammatilliseentehtavaanvalmistavakoulutus")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusKaikkiYksiköt],
  @Description("Kuvaus koulutuksen sisällöstä osaamisena.")
  @Tooltip("Kuvaus koulutuksen sisällöstä osaamisena.")
  kuvaus: LocalizedString
) extends KoodistostaLöytyväKoulutusmoduuli with MuuAmmatillinenKoulutus

case class PaikallinenMuuAmmatillinenKoulutus(
  tunniste: PaikallinenKoodi,
  laajuus: Option[LaajuusKaikkiYksiköt],
  @Description("Kuvaus koulutuksen sisällöstä osaamisena.")
  @Tooltip("Kuvaus koulutuksen sisällöstä osaamisena.")
  kuvaus: LocalizedString
) extends PaikallinenKoulutusmoduuli with MuuAmmatillinenKoulutus

sealed trait MuuAmmatillinenOsasuoritus extends Suoritus with MahdollisestiSuorituskielellinen

case class MuunAmmatillisenKoulutuksenOsasuorituksenSuoritus(
  koulutusmoduuli: MuunAmmatillisenKoulutuksenOsasuoritus,
  override val alkamispäivä: Option[LocalDate],
  arviointi: Option[List[AmmatillinenArviointi]],
  suorituskieli: Option[Koodistokoodiviite],
  @Description("Osasuoritukseen liittyvän näytön tiedot")
  @Tooltip("Osasuoritukseen kuuluvan ammattiosaamisen näytön tiedot.")
  @ComplexObject
  näyttö: Option[Näyttö] = None,
  @Description("Osasuoritukseen sisältyvien osasuoritusten suoritukset")
  @Title("Sisältyvät osasuoritukset")
  override val osasuoritukset: Option[List[MuunAmmatillisenKoulutuksenOsasuorituksenSuoritus]] = None,
  @KoodistoKoodiarvo("muunammatillisenkoulutuksenosasuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("muunammatillisenkoulutuksenosasuoritus", koodistoUri = "suorituksentyyppi")
) extends MuuAmmatillinenOsasuoritus with Vahvistukseton

case class MuunAmmatillisenKoulutuksenOsasuoritus(
  tunniste: PaikallinenKoodi,
  laajuus: Option[LaajuusKaikkiYksiköt],
  kuvaus: LocalizedString
) extends PaikallinenKoulutusmoduuli

@Description("Suoritukseen liittyvät lisätiedot, kuten esimerkiksi mukautettu arviointi tai poikkeus arvioinnissa.")
case class MuunAmmatillisenKoulutuksenOsasuorituksenLisätieto(
  @Description("Lisätiedon tyyppi kooditettuna")
  @KoodistoUri("ammatillisentutkinnonosanlisatieto")
  tunniste: Koodistokoodiviite,
  @Description("Lisätiedon kuvaus siinä muodossa, kuin se näytetään todistuksella")
  kuvaus: LocalizedString
)

case class TutkinnonOsaaPienemmänKokonaisuudenSuoritus(
  koulutusmoduuli: TutkinnonOsaaPienempiKokonaisuus,
  override val alkamispäivä: Option[LocalDate],
  arviointi: Option[List[AmmatillinenArviointi]],
  @Description("Osasuoritukseen liittyvän näytön tiedot")
  @Tooltip("Osasuoritukseen kuuluvan ammattiosaamisen näytön tiedot.")
  @ComplexObject
  näyttö: Option[Näyttö] = None,
  @Tooltip("Suoritukseen liittyvät lisätiedot, kuten esimerkiksi mukautettu arviointi tai poikkeus arvioinnissa. Sisältää lisätiedon tyypin sekä vapaamuotoisen kuvauksen.")
  @ComplexObject
  lisätiedot: Option[List[MuunAmmatillisenKoulutuksenOsasuorituksenLisätieto]],
  @KoodistoUri("tutkinnonosat")
  liittyyTutkinnonOsaan: Koodistokoodiviite,
  suorituskieli: Option[Koodistokoodiviite],
  @KoodistoKoodiarvo("tutkinnonosaapienempikokonaisuus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("tutkinnonosaapienempikokonaisuus", koodistoUri = "suorituksentyyppi")
) extends MuuAmmatillinenOsasuoritus with Vahvistukseton

case class TutkinnonOsaaPienempiKokonaisuus(
  tunniste: PaikallinenKoodi,
  laajuus: Option[LaajuusOsaamispisteissä],
  kuvaus: LocalizedString
) extends PaikallinenKoulutusmoduuli

