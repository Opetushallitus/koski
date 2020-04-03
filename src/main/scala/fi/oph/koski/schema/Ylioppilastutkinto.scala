package fi.oph.koski.schema

import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, KoodistoUri}
import fi.oph.koski.util.OptionalLists
import fi.oph.scalaschema.annotation.{Description, MaxItems, MinItems, Title}

case class YlioppilastutkinnonOpiskeluoikeus(
  lähdejärjestelmänId: Option[LähdejärjestelmäId],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  tila: YlioppilastutkinnonOpiskeluoikeudenTila,
  @MinItems(1) @MaxItems(1)
  suoritukset: List[YlioppilastutkinnonSuoritus],
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.ylioppilastutkinto.koodiarvo)
  tyyppi: Koodistokoodiviite = OpiskeluoikeudenTyyppi.ylioppilastutkinto
) extends Opiskeluoikeus {
  override def arvioituPäättymispäivä = None
  override def päättymispäivä = None
  override def oid = None
  override def versionumero = None
  override def lisätiedot = None
  override def sisältyyOpiskeluoikeuteen = None
}

case class YlioppilastutkinnonOpiskeluoikeudenTila(opiskeluoikeusjaksot: List[LukionOpiskeluoikeusjakso]) extends OpiskeluoikeudenTila

case class YlioppilastutkinnonSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: Ylioppilastutkinto = Ylioppilastutkinto(perusteenDiaarinumero = None),
  toimipiste: Option[OrganisaatioWithOid],
  vahvistus: Option[Organisaatiovahvistus] = None,
  pakollisetKokeetSuoritettu: Boolean,
  @Description("Ylioppilastutkinnon kokeiden suoritukset")
  @Title("Kokeet")
  override val osasuoritukset: Option[List[YlioppilastutkinnonKokeenSuoritus]],
  @KoodistoKoodiarvo("ylioppilastutkinto")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ylioppilastutkinto", koodistoUri = "suorituksentyyppi")
) extends PäätasonSuoritus with MahdollisestiToimipisteellinen with Arvioinniton with KoulusivistyskieliYlioppilasKokeenSuorituksesta

case class YlioppilastutkinnonKokeenSuoritus(
  @Title("Koe")
  koulutusmoduuli: YlioppilasTutkinnonKoe,
  tutkintokerta: YlioppilastutkinnonTutkintokerta,
  arviointi: Option[List[YlioppilaskokeenArviointi]],
  @KoodistoKoodiarvo("ylioppilastutkinnonkoe")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ylioppilastutkinnonkoe", koodistoUri = "suorituksentyyppi")
) extends Vahvistukseton

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
 perusteenDiaarinumero: Option[String],
 koulutustyyppi: Option[Koodistokoodiviite] = None
) extends Tutkinto with Laajuudeton with DiaarinumerollinenKoulutus

@Description("Ylioppilastutkinnon kokeen tunnistetiedot")
case class YlioppilasTutkinnonKoe(
  @KoodistoUri("koskiyokokeet")
  tunniste: Koodistokoodiviite
) extends KoodistostaLöytyväKoulutusmoduuli {
  def laajuus = None
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
