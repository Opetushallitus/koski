package fi.oph.koski.schema

import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, KoodistoUri}
import fi.oph.koski.util.OptionalLists
import fi.oph.scalaschema.annotation.{Description, MaxItems, MinItems, Title}

import java.time.{LocalDate, LocalDateTime}

case class YlioppilastutkinnonOpiskeluoikeus(
  oid: Option[String] = None,
  versionumero: Option[Int] = None,
  lähdejärjestelmänId: Option[LähdejärjestelmäId],
  oppilaitos: Option[Oppilaitos],
  koulutustoimija: Option[Koulutustoimija],
  @Description("Toistaiseksi vain Kosken sisäisessä käytössä. Sisältö on aina tyhjä lista opiskeluoikeusjaksoja. Kenttä näkyy tietomallissa vain teknisistä syistä.")
  tila: YlioppilastutkinnonOpiskeluoikeudenTila,
  @MinItems(1) @MaxItems(1)
  suoritukset: List[YlioppilastutkinnonSuoritus],
  @KoodistoKoodiarvo(OpiskeluoikeudenTyyppi.ylioppilastutkinto.koodiarvo)
  tyyppi: Koodistokoodiviite = OpiskeluoikeudenTyyppi.ylioppilastutkinto,
  aikaleima: Option[LocalDateTime] = None,
  // TODO: Testaa että nimi muuttuu lähtödatan certificateDate-kentän perusteella
  @Description("Toistaiseksi vain Kosken sisäisessä käytössä. Organisaatiopalvelun oppilaitos-OID-tunniste, jossa ylioppilastutkinto on suoritettu. Ei välttämättä ole oppilaitos, jossa henkilöllä on opintooikeus")
  oppilaitosSuorituspäivänä: Option[Oppilaitos] = None,
  @Description("Toistaiseksi vain Kosken sisäisessä käytössä.")
  lisätiedot: Option[YlioppilastutkinnonOpiskeluoikeudenLisätiedot] = None
  ) extends KoskeenTallennettavanKaltainenOpiskeluoikeus {
  override def arvioituPäättymispäivä = None
  override def päättymispäivä = None
  override def sisältyyOpiskeluoikeuteen = None
  override def organisaatiohistoria: Option[List[OpiskeluoikeudenOrganisaatiohistoria]] = None
}

case class YlioppilastutkinnonOpiskeluoikeudenLisätiedot(
  @Description("Toistaiseksi vain Kosken sisäisessä käytössä.")
  tutkintokokonaisuudet: Option[List[YlioppilastutkinnonTutkintokokonaisuudenLisätiedot]]
) extends OpiskeluoikeudenLisätiedot

case class YlioppilastutkinnonTutkintokokonaisuudenLisätiedot(
  @Description("Keinotekoinen tutkintokokonaisuuden tunniste johon viitataan YlioppilastutkinnonKokeenSuoritus-luokasta")
  tunniste: Int,
  @KoodistoUri("ytrtutkintokokonaisuudentyyppi")
  tyyppi: Option[Koodistokoodiviite] = None,
  @KoodistoUri("ytrtutkintokokonaisuudentila")
  tila: Option[Koodistokoodiviite] = None,
  @KoodistoUri("kieli")
  suorituskieli: Option[Koodistokoodiviite] = None,
  tutkintokerrat: List[YlioppilastutkinnonTutkintokerranLisätiedot]
)

case class YlioppilastutkinnonTutkintokerranLisätiedot(
  tutkintokerta: YlioppilastutkinnonTutkintokerta,
  @Description("Toistaiseksi vain Kosken sisäisessä käytössä. YTL:lle ilmoitettu henkilön koulutustausta, jonka perusteella hän osallistuu tutkintoon kyseisellä tutkintokerralla.")
  @KoodistoUri("ytrkoulutustausta")
  koulutustausta: Option[Koodistokoodiviite] = None,
  @Description("Toistaiseksi vain Kosken sisäisessä käytössä. Organisaatiopalvelusta saatu oppilaitos, johon kokelas on ilmoittautunut. Ei välttämättä ole oppilaitos, jossa henkilöllä on opinto-oikeus.")
  oppilaitos: Option[Oppilaitos] = None
)

case class YlioppilastutkinnonOpiskeluoikeudenTila(
  @Description("Toistaiseksi vain Kosken sisäisessä käytössä. Sisältö on tyhjä lista ellei tutkinto ole valmistunut. Streamaus-API:n osalta lista on aina tyhjä.")
  opiskeluoikeusjaksot: List[YlioppilastutkinnonOpiskeluoikeusjakso]
) extends OpiskeluoikeudenTila

case class YlioppilastutkinnonOpiskeluoikeusjakso(
  alku: LocalDate,
  @KoodistoKoodiarvo("valmistunut")
  tila: Koodistokoodiviite,
) extends KoskiOpiskeluoikeusjakso

case class YlioppilastutkinnonSuoritus(
  @Title("Koulutus")
  koulutusmoduuli: Ylioppilastutkinto = Ylioppilastutkinto(perusteenDiaarinumero = None),
  toimipiste: OrganisaatioWithOid,
  vahvistus: Option[Organisaatiovahvistus] = None,
  pakollisetKokeetSuoritettu: Boolean,
  @Description("Ylioppilastutkinnon kokeiden suoritukset")
  @Title("Kokeet")
  override val osasuoritukset: Option[List[YlioppilastutkinnonKokeenSuoritus]],
  @KoodistoKoodiarvo("ylioppilastutkinto")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ylioppilastutkinto", koodistoUri = "suorituksentyyppi")
) extends KoskeenTallennettavanKaltainenPäätasonSuoritus with Arvioinniton with KoulusivistyskieliYlioppilasKokeenSuorituksesta

case class YlioppilastutkinnonKokeenSuoritus(
  @Title("Koe")
  koulutusmoduuli: YlioppilasTutkinnonKoe,
  tutkintokerta: YlioppilastutkinnonTutkintokerta,
  arviointi: Option[List[YlioppilaskokeenArviointi]],
  @KoodistoKoodiarvo("ylioppilastutkinnonkoe")
  tyyppi: Koodistokoodiviite = Koodistokoodiviite("ylioppilastutkinnonkoe", koodistoUri = "suorituksentyyppi"),
  @Description("Toistaiseksi vain Kosken sisäisessä käytössä. Viittaa päätason lisätiedoissa määriteltyyn listaan tutkintokokonaisuuksia")
  tutkintokokonaisuudenTunniste: Option[Int] = None,
  @Description("Toistaiseksi vain Kosken sisäisessä käytössä. Kertoo, onko kyseessä ylioppilastutkinnosta annetun lain (502/2019) 14 § tai 15 § mukaisesti keskeytetty koe.")
  keskeytynyt: Option[Boolean] = None,
  @Description("Toistaiseksi vain Kosken sisäisessä käytössä. Kertoo, onko kyseessä ylioppilastutkinnosta annetun lain (502/2019) 20 § mukaisesti koe, johon osallistumisesta ei peritä maksua.")
  maksuton: Option[Boolean] = None
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
