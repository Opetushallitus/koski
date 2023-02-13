package fi.oph.koski.schema

import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, KoodistoUri}
import fi.oph.koski.util.OptionalLists
import fi.oph.scalaschema.annotation.{Description, MaxItems, MinItems, Title}

import java.time.LocalDateTime

case class YlioppilastutkinnonOpiskeluoikeus(
  oid: Option[String] = None,
  versionumero: Option[Int] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  @Description("Sisältö on aina tyhjä lista opiskeluoikeusjaksoja. Kenttä näkyy tietomallissa vain teknisistä syistä.")
  tila: YlioppilastutkinnonOpiskeluoikeudenTila,
  @MinItems(1) @MaxItems(1)
  suoritukset: List[YlioppilastutkinnonSuoritus],
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.ylioppilastutkinto.koodiarvo)
  tyyppi: Koodistokoodiviite = OpiskeluoikeudenTyyppi.ylioppilastutkinto,
  aikaleima: Option[LocalDateTime] = None
  ) extends KoskeenTallennettavaOpiskeluoikeus {
  override def arvioituPäättymispäivä = None
  override def päättymispäivä = None
  override def lisätiedot = None
  override def sisältyyOpiskeluoikeuteen = None
  override def organisaatiohistoria: Option[List[OpiskeluoikeudenOrganisaatiohistoria]] = None

  override def withOppilaitos(oppilaitos: Oppilaitos): YlioppilastutkinnonOpiskeluoikeus = this.copy(oppilaitos = Some(oppilaitos))
  override def withKoulutustoimija(koulutustoimija: Koulutustoimija): YlioppilastutkinnonOpiskeluoikeus = this.copy(koulutustoimija = Some(koulutustoimija))
}

case class YlioppilastutkinnonOpiskeluoikeudenTila(
  @Description("Sisältö on aina tyhjä lista. Kenttä näkyy tietomallissa vain teknisistä syistä.")
  opiskeluoikeusjaksot: List[LukionOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

case class YlioppilastutkinnonSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: Ylioppilastutkinto = Ylioppilastutkinto(),
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[Organisaatiovahvistus] = None,
  pakollisetKokeetSuoritettu: Boolean,
  @Description("Ylioppilastutkinnon kokeiden suoritukset")
  @Title("Kokeet")
  override val osasuoritukset: Option[List[YlioppilastutkinnonKokeenSuoritus]],
  @KoodistoKoodiarvo("ylioppilastutkinto")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ylioppilastutkinto", koodistoUri = "suorituksentyyppi")
) extends KoskeenTallennettavaPäätasonSuoritus with Arvioinniton with KoulusivistyskieliYlioppilasKokeenSuorituksesta

case class YlioppilastutkinnonKokeenSuoritus(
  @Title("Koe")
  koulutusmoduuli: YlioppilasTutkinnonKoe,
  tutkintokerta: YlioppilastutkinnonTutkintokerta,
  arviointi: Option[List[YlioppilaskokeenArviointi]],
  @KoodistoKoodiarvo("ylioppilastutkinnonkoe")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ylioppilastutkinnonkoe", koodistoUri = "suorituksentyyppi")
) extends Vahvistukseton with DuplikaatitSallittu

case class YlioppilastutkinnonTutkintokerta(koodiarvo: String, vuosi: Int, vuodenaika: LocalizedString)

case class YlioppilaskokeenArviointi(
  @KoodistoUri("koskiyoarvosanat")
  arvosana: Koodistokoodiviite,
  pisteet: Option[Int]
) extends KoodistostaLöytyväArviointi {
  override def arviointipäivä = None
  override def arvioitsijat = None
  def hyväksytty = !List("I", "I-", "I+", "I=").contains(arvosana.koodiarvo)
}

@Description("Ylioppilastutkinnon tunnistetiedot")
case class Ylioppilastutkinto(
 @KoodistoKoodiarvo("301000")
 tunniste: Koodistokoodiviite = Koodistokoodiviite("301000", koodistoUri = "koulutus"),
// perusteenDiaarinumero: Option[String],
 koulutustyyppi: Option[Koodistokoodiviite] = None
) extends Tutkinto with Laajuudeton with Koulutus

@Description("Ylioppilastutkinnon kokeen tunnistetiedot")
case class YlioppilasTutkinnonKoe(
  @KoodistoUri("koskiyokokeet")
  tunniste: Koodistokoodiviite
) extends KoodistostaLöytyväKoulutusmoduuli {
  override def getLaajuus: Option[Laajuus] = None
}

trait KoulusivistyskieliYlioppilasKokeenSuorituksesta extends Koulusivistyskieli {
  def osasuoritukset: Option[List[YlioppilastutkinnonKokeenSuoritus]]
  def koulusivistyskieli: Option[List[Koodistokoodiviite]] = OptionalLists.optionalList(osasuoritukset.toList.flatten.flatMap(ylioppilaskokeesta).sortBy(_.koodiarvo).distinct)

  private def ylioppilaskokeesta(koe: YlioppilastutkinnonKokeenSuoritus) = {
    val äidinkielenKoeSuomi = koe.koulutusmoduuli.tunniste.koodiarvo == "A"
    val äidinkielenKoeRuotsi = koe.koulutusmoduuli.tunniste.koodiarvo == "O"
    val suomiToisenaKielenäKoe = koe.koulutusmoduuli.tunniste.koodiarvo == "A5"
    val ruotsiToisenaKielenäKoe = koe.koulutusmoduuli.tunniste.koodiarvo == "O5"
    val arvosanaVähintäänMagna = koe.viimeisinArvosana.exists(List("M", "E", "L").contains)

    if (äidinkielenKoeSuomi && koe.viimeisinArviointi.exists(_.hyväksytty)) {
      Koulusivistyskieli.suomi
    } else if (äidinkielenKoeRuotsi && koe.viimeisinArviointi.exists(_.hyväksytty)) {
      Koulusivistyskieli.ruotsi
    } else if (suomiToisenaKielenäKoe && arvosanaVähintäänMagna) {
      Koulusivistyskieli.suomi
    } else if (ruotsiToisenaKielenäKoe && arvosanaVähintäänMagna) {
      Koulusivistyskieli.ruotsi
    } else {
      None
    }
  }
}
