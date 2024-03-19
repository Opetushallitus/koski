package fi.oph.koski.schema

import java.time.LocalDate
import fi.oph.koski.schema.annotation._
import fi.oph.scalaschema.annotation.{Description, Discriminator, MinItems, Title}

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
) extends AmmatillinenPäätasonSuoritus with Todistus with Toimipisteellinen with Ryhmällinen with Työssäoppimisjaksoton with Arvioinniton with OsaamisenHankkimistavallinen

case class MuunAmmatillisenKoulutuksenArviointi(
  @KoodistoUri("arviointiasteikkomuuammatillinenkoulutus")
  arvosana: Koodistokoodiviite,
  päivä: LocalDate,
  @Description("Tutkinnon osan suorituksen arvioinnista päättäneen henkilön nimi")
  arvioitsijat: Option[List[Arvioitsija]] = None,
  kuvaus: Option[LocalizedString] = None
) extends AmmatillinenKoodistostaLöytyväArviointi with SanallinenArviointi

case class TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus(
  koulutusmoduuli: TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus,
  toimipiste: OrganisaatioWithOid,
  override val alkamispäivä: Option[LocalDate],
  vahvistus: Option[HenkilövahvistusValinnaisellaPaikkakunnalla] = None,
  suorituskieli: Koodistokoodiviite,
  @Description("Osaamisen hankkimistavat eri ajanjaksoina.")
  @Tooltip("Osaamisen hankkimistavat (oppisopimus, koulutussopimus, oppilaitosmuotoinen koulutus) eri ajanjaksoina.")
  osaamisenHankkimistavat: Option[List[OsaamisenHankkimistapajakso]] = None,
  koulutussopimukset: Option[List[Koulutussopimusjakso]] = None,
  @Description("Tutkinnon osaa pienempään kokonaisuuteen kuuluvien osasuoritusten suoritukset")
  override val osasuoritukset: Option[List[TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvanSuorituksenOsasuoritus]],
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("tutkinnonosaapienemmistäkokonaisuuksistakoostuvasuoritus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("tutkinnonosaapienemmistäkokonaisuuksistakoostuvasuoritus", "suorituksentyyppi"),
  ryhmä: Option[String] = None
) extends AmmatillinenPäätasonSuoritus with Todistus with Toimipisteellinen with Ryhmällinen with Työssäoppimisjaksoton with Arvioinniton with OsaamisenHankkimistavallinen

sealed trait Työssäoppimisjaksoton extends AmmatillinenPäätasonSuoritus {
  override def työssäoppimisjaksot: Option[List[Työssäoppimisjakso]] = None
}

trait MuuAmmatillinenKoulutus extends KoulutusmoduuliValinnainenLaajuus
trait Kuvaus {
  @Description("Kuvaus koulutuksen sisällöstä osaamisena.")
  @Tooltip("Kuvaus koulutuksen sisällöstä osaamisena.")
  @MultiLineString(5)
  def kuvaus: LocalizedString
}

case class AmmatilliseenTehtäväänValmistavaKoulutus(
  @KoodistoUri("ammatilliseentehtavaanvalmistavakoulutus")
  tunniste: Koodistokoodiviite,
  laajuus: Option[LaajuusKaikkiYksiköt],
  @Description("Kuvaus koulutuksen sisällöstä osaamisena.")
  @Tooltip("Kuvaus koulutuksen sisällöstä osaamisena.")
  @MultiLineString(5)
  kuvaus: Option[LocalizedString]
) extends KoodistostaLöytyväKoulutusmoduuli with MuuAmmatillinenKoulutus

case class PaikallinenMuuAmmatillinenKoulutus(
  tunniste: PaikallinenKoodi,
  laajuus: Option[LaajuusKaikkiYksiköt],
  kuvaus: LocalizedString
) extends PaikallinenKoulutusmoduuliKuvauksella with MuuAmmatillinenKoulutus with Kuvaus

case class TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus(
  tunniste: PaikallinenKoodi,
  laajuus: Option[LaajuusKaikkiYksiköt],
  kuvaus: LocalizedString
) extends PaikallinenKoulutusmoduuliValinnainenLaajuus with Kuvaus

trait TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvanSuorituksenOsasuoritus extends Vahvistukseton with Suoritus

trait MuuAmmatillinenOsasuoritus extends Suoritus with MahdollisestiSuorituskielellinen

case class MuunAmmatillisenKoulutuksenOsasuorituksenSuoritus(
  koulutusmoduuli: MuunAmmatillisenKoulutuksenOsasuoritus,
  override val alkamispäivä: Option[LocalDate],
  arviointi: Option[List[MuunAmmatillisenKoulutuksenArviointi]],
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
) extends PaikallinenKoulutusmoduuliValinnainenLaajuus

@Description("Suoritukseen liittyvät lisätiedot, kuten esimerkiksi mukautettu arviointi tai poikkeus arvioinnissa.")
case class MuunAmmatillisenKoulutuksenOsasuorituksenLisätieto(
  @Description("Lisätiedon tyyppi kooditettuna")
  @KoodistoUri("ammatillisentutkinnonosanlisatieto")
  tunniste: Koodistokoodiviite,
  @Description("Lisätiedon kuvaus siinä muodossa, kuin se näytetään todistuksella")
  kuvaus: LocalizedString
)


trait TutkinnonOsaaPienemmänKokonaisuudenSuoritus
  extends MuuAmmatillinenOsasuoritus
    with TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvanSuorituksenOsasuoritus
{
  @Description("Osasuoritukseen liittyvän näytön tiedot")
  @Tooltip("Osasuoritukseen kuuluvan ammattiosaamisen näytön tiedot.")
  @ComplexObject
  def näyttö: Option[Näyttö]

  @Tooltip("Suoritukseen liittyvät lisätiedot, kuten esimerkiksi mukautettu arviointi tai poikkeus arvioinnissa. Sisältää lisätiedon tyypin sekä vapaamuotoisen kuvauksen.")
  @ComplexObject
  def lisätiedot: Option[List[MuunAmmatillisenKoulutuksenOsasuorituksenLisätieto]]

  @KoodistoKoodiarvo("tutkinnonosaapienempikokonaisuus")
  def tyyppi: Koodistokoodiviite
}

case class ValtakunnallisenTutkinnonOsaaPienemmänKokonaisuudenSuoritus(
  koulutusmoduuli: TutkinnonOsaaPienempiKokonaisuus,
  override val alkamispäivä: Option[LocalDate],
  arviointi: Option[List[MuunAmmatillisenKoulutuksenArviointi]],
  näyttö: Option[Näyttö] = None,
  lisätiedot: Option[List[MuunAmmatillisenKoulutuksenOsasuorituksenLisätieto]],
  @KoodistoUri("tutkinnonosat")
  @Discriminator
  liittyyTutkinnonOsaan: Koodistokoodiviite,
  suorituskieli: Option[Koodistokoodiviite],
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("tutkinnonosaapienempikokonaisuus", koodistoUri = "suorituksentyyppi")
) extends TutkinnonOsaaPienemmänKokonaisuudenSuoritus

case class TutkinnonOsaaPienempiKokonaisuus(
  tunniste: PaikallinenKoodi,
  laajuus: Option[LaajuusKaikkiYksiköt],
  kuvaus: LocalizedString
) extends PaikallinenKoulutusmoduuliValinnainenLaajuus
