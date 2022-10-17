package fi.oph.koski.schema

import java.time.{LocalDate, LocalDateTime}

import fi.oph.koski.schema.annotation.{Hidden, KoodistoKoodiarvo, KoodistoUri, Tooltip}
import fi.oph.scalaschema.annotation.{Description, MaxItems, MinItems, Title}

@Description("Perusopetukseen valmistavan opetuksen opiskeluoikeuden tiedot")
case class PerusopetukseenValmistavanOpetuksenOpiskeluoikeus(
  oid: Option[String] = None,
  versionumero: Option[Int] = None,
  aikaleima: Option[LocalDateTime] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId] = None,
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija] = None,
  @Hidden
  sisältyyOpiskeluoikeuteen: Option[SisältäväOpiskeluoikeus] = None,
  tila: PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila,
  @MaxItems(1)
  suoritukset: List[PerusopetukseenValmistavanOpetuksenSuoritus],
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.perusopetukseenvalmistavaopetus.koodiarvo)
  tyyppi: Koodistokoodiviite = OpiskeluoikeudenTyyppi.perusopetukseenvalmistavaopetus,
  organisaatiohistoria: Option[List[OpiskeluoikeudenOrganisaatiohistoria]] = None
) extends KoskeenTallennettavaOpiskeluoikeus {
  @Description("Oppijan oppimäärän päättymispäivä")
  override def päättymispäivä: Option[LocalDate] = super.päättymispäivä
  override def withOppilaitos(oppilaitos: Oppilaitos) = this.copy(oppilaitos = Some(oppilaitos))
  override def withKoulutustoimija(koulutustoimija: Koulutustoimija) = this.copy(koulutustoimija = Some(koulutustoimija))
  override def arvioituPäättymispäivä = None
  override def lisätiedot = None
}

@Description("Perusopetukseen valmistavan opetuksen suorituksen tiedot")
case class PerusopetukseenValmistavanOpetuksenSuoritus(
  @Title("Koulutus")
  @Tooltip("Suoritettava koulutus ja koulutuksen opetussuunnitelman perusteiden diaarinumero.")
  koulutusmoduuli: PerusopetukseenValmistavaOpetus,
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[HenkilövahvistusPaikkakunnalla] = None,
  suorituskieli: Koodistokoodiviite,
  @Tooltip("Mahdolliset muut suorituskielet.")
  muutSuorituskielet: Option[List[Koodistokoodiviite]] = None,
  @Tooltip("Osallistuminen perusopetusta täydentävän oppilaan oman äidinkielen opiskeluun perusopetukseen valmistavan opetuksen aikana")
  omanÄidinkielenOpinnot: Option[OmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina] = None,
  @Description("Oppiaineiden suoritukset")
  @Title("Oppiaineet")
  override val osasuoritukset: Option[List[PerusopetukseenValmistavanOpetuksenOsasuoritus]],
  @Description("Oppiaineiden kokonaislaajuus vuosiviikkotunneissa")
  kokonaislaajuus: Option[LaajuusVuosiviikkotunneissa] = None,
  @Tooltip("Todistuksella näkyvät lisätiedot. Esimerkiksi tieto oppilaan perusopetuksen aloittamisesta (luokkataso).")
  todistuksellaNäkyvätLisätiedot: Option[LocalizedString] = None,
  @KoodistoKoodiarvo("perusopetukseenvalmistavaopetus")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetukseenvalmistavaopetus", koodistoUri = "suorituksentyyppi")
) extends KoskeenTallennettavaPäätasonSuoritus with Toimipisteellinen with Todistus with Arvioinniton with MonikielinenSuoritus with Suorituskielellinen

trait PerusopetukseenValmistavanOpetuksenOsasuoritus extends Suoritus

@Description("Perusopetukseen valmistavan opetuksen oppiaineen suoritustiedot")
case class PerusopetukseenValmistavanOpetuksenOppiaineenSuoritus(
  @Title("Oppiaine")
  koulutusmoduuli: PerusopetukseenValmistavanOpetuksenOppiaine,
  arviointi: Option[List[SanallinenPerusopetuksenOppiaineenArviointi]],
  suorituskieli: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("perusopetukseenvalmistavanopetuksenoppiaine")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("perusopetukseenvalmistavanopetuksenoppiaine", koodistoUri = "suorituksentyyppi")
) extends Vahvistukseton with MahdollisestiSuorituskielellinen with PerusopetukseenValmistavanOpetuksenOsasuoritus

@Description("Perusopetuksen oppiaineen suoritus osana perusopetukseen valmistavaa opetusta")
case class NuortenPerusopetuksenOppiaineenSuoritusValmistavassaOpetuksessa(
  koulutusmoduuli: NuortenPerusopetuksenOppiaine,
  arviointi: Option[List[PerusopetuksenOppiaineenArviointi]] = None,
  suorituskieli: Option[Koodistokoodiviite] = None,
  @Description("Luokka-asteen tunniste (1-9). Minkä vuosiluokan mukaisesta oppiainesuorituksesta on kyse.")
  @Tooltip("Minkä vuosiluokan mukaisesta oppiainesuorituksesta on kyse")
  @Title("Luokka-aste")
  @KoodistoUri("perusopetuksenluokkaaste")
  luokkaAste: Option[Koodistokoodiviite] = None,
  @KoodistoKoodiarvo("perusopetuksenoppiaineperusopetukseenvalmistavassaopetuksessa")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite(koodiarvo = "perusopetuksenoppiaineperusopetukseenvalmistavassaopetuksessa", koodistoUri = "suorituksentyyppi"),
  suoritustapa: Option[Koodistokoodiviite] = None
) extends PerusopetuksenOppiaineenSuoritus with Vahvistukseton with MahdollisestiSuorituskielellinen with PerusopetukseenValmistavanOpetuksenOsasuoritus with Laajuudellinen with SuoritustapanaMahdollisestiErityinenTutkinto{
  private val luokkaAstettaEiVaaditaArvosana = "O"
  def luokkaAsteVaaditaan: Boolean =
    arvioitu && !viimeisinArviointi.map(_.arvosana.koodiarvo).contains(luokkaAstettaEiVaaditaArvosana)
}

@Description("Perusopetukseen valmistavan opetuksen tunnistetiedot")
case class PerusopetukseenValmistavaOpetus(
  @KoodistoKoodiarvo("999905")
  tunniste: Koodistokoodiviite = Koodistokoodiviite("999905", koodistoUri = "koulutus"),
  perusteenDiaarinumero: Option[String],
  koulutustyyppi: Option[Koodistokoodiviite] = None
) extends DiaarinumerollinenKoulutus with Laajuudeton

@Description("Perusopetukseen valmistavan opetuksen oppiaineen tunnistetiedot")
case class PerusopetukseenValmistavanOpetuksenOppiaine(
  tunniste: PaikallinenKoodi,
  laajuus: Option[LaajuusKaikkiYksiköt],
  @Tooltip("Oppiaineen opetuksen sisältö.")
  opetuksenSisältö: Option[LocalizedString]
) extends PaikallinenKoulutusmoduuliValinnainenLaajuus with StorablePreference {
  def kuvaus: LocalizedString = opetuksenSisältö.getOrElse(LocalizedString.empty)
}

@Description("Ks. tarkemmin perusopetuksen opiskeluoikeuden tilat: [wiki](https://wiki.eduuni.fi/display/OPHPALV/Perusopetukseen+valmistava+opetus#Perusopetukseenvalmistavaopetus-Opiskeluoikeudentilat:L%C3%A4sn%C3%A4olojaopintojenlopettaminen)")
case class PerusopetukseenValmistavanOpetuksenOpiskeluoikeudenTila(
  @MinItems(1)
  opiskeluoikeusjaksot: List[PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso]
) extends OpiskeluoikeudenTila

case class PerusopetukseenValmistavanOpetuksenOpiskeluoikeusJakso(
  alku: LocalDate,
  tila: Koodistokoodiviite
) extends KoskiLomanSallivaLaajaOpiskeluoikeusjakso
