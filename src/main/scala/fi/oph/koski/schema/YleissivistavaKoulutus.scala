package fi.oph.koski.schema

import java.time.LocalDate

import fi.oph.koski.koskiuser.Rooli
import fi.oph.koski.schema.annotation._
import fi.oph.koski.util.OptionalLists
import fi.oph.scalaschema.annotation.{DefaultValue, Description, Title}

trait OppiaineenSuoritus extends Suoritus {
  @Title("Oppiaine")
  def koulutusmoduuli: Koulutusmoduuli
}

trait KurssinSuoritus extends Suoritus with Vahvistukseton{
  @Title("Kurssi")
  @FlattenInUI
  def koulutusmoduuli: Koulutusmoduuli
}

trait ValtakunnallisenModuulinSuoritus extends Suoritus with Vahvistukseton{
  @Title("Moduuli")
  @FlattenInUI
  def koulutusmoduuli: Koulutusmoduuli
}

trait Yksilöllistettävä {
  @DefaultValue(false)
  @Description("Tieto siitä, onko oppiaineen oppimäärä yksilöllistetty (true/false). Jos oppilas opiskelee yhdessä yksilöllistetyn oppimäärän mukaan, myös päättöarviointi voi näissä aineissa olla sanallinen.")
  @Tooltip("Onko oppilas opiskellut oppiaineessa yksilöllisen oppimäärän. Jos oppilas opiskelee yhdessä yksilöllistetyn oppimäärän mukaan, myös päättöarviointi voi näissä aineissa olla sanallinen.")
  @SensitiveData(Set(Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT))
  def yksilöllistettyOppimäärä: Boolean
}

trait YleissivistavaOppiaine extends KoodistostaLöytyväKoulutusmoduuli with Valinnaisuus {
  @Description("Oppiaine")
  @KoodistoUri("koskioppiaineetyleissivistava")
  @OksaUri("tmpOKSAID256", "oppiaine")
  def tunniste: Koodistokoodiviite
}

trait YleissivistävänKoulutuksenArviointi extends KoodistostaLöytyväArviointi {
  @KoodistoUri("arviointiasteikkoyleissivistava")
  def arvosana: Koodistokoodiviite
  def arvioitsijat = None
  def hyväksytty = YleissivistävänKoulutuksenArviointi.hyväksytty(arvosana)
}
object YleissivistävänKoulutuksenArviointi {
  def hyväksytty(arvosana: Koodistokoodiviite): Boolean = arvosana.koodiarvo match {
    case "H" => false
    case "4" => false
    case "O" => false
    case _ => true
  }
}

trait NumeerinenYleissivistävänKoulutuksenArviointi extends YleissivistävänKoulutuksenArviointi {
  @KoodistoKoodiarvo("4")
  @KoodistoKoodiarvo("5")
  @KoodistoKoodiarvo("6")
  @KoodistoKoodiarvo("7")
  @KoodistoKoodiarvo("8")
  @KoodistoKoodiarvo("9")
  @KoodistoKoodiarvo("10")
  def arvosana: Koodistokoodiviite
}

trait SanallinenYleissivistävänKoulutuksenArviointi extends YleissivistävänKoulutuksenArviointi with SanallinenArviointi {
  @KoodistoKoodiarvo("S")
  @KoodistoKoodiarvo("H")
  @KoodistoKoodiarvo("O")
  def arvosana: Koodistokoodiviite
}

trait OmanÄidinkielenArviointi extends NumeerinenYleissivistävänKoulutuksenArviointi {
  @KoodistoKoodiarvo("O")
  override def arvosana: Koodistokoodiviite
  @Description("Mikä kieli on kyseessä")
  @KoodistoUri("kielivalikoima")
  def kieli: Koodistokoodiviite
}

case class OmanÄidinkielenOpinnotLaajuusKursseina(
  arvosana: Koodistokoodiviite,
  arviointipäivä: Option[LocalDate],
  kieli: Koodistokoodiviite,
  laajuus: Option[LaajuusKursseissa]
) extends OmanÄidinkielenArviointi

case class OmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina(
  arvosana: Koodistokoodiviite,
  arviointipäivä: Option[LocalDate],
  kieli: Koodistokoodiviite,
  laajuus: Option[LaajuusVuosiviikkotunneissa]
) extends OmanÄidinkielenArviointi

case class OmanÄidinkielenOpinnotLaajuusOpintopisteinä(
  arvosana: Koodistokoodiviite,
  arviointipäivä: Option[LocalDate],
  kieli: Koodistokoodiviite,
  laajuus: LaajuusOpintopisteissä
) extends OmanÄidinkielenArviointi

trait KoulusivistyskieliKieliaineesta extends Koulusivistyskieli with PäätasonSuoritus {
  def koulusivistyskieli: Option[List[Koodistokoodiviite]] = OptionalLists.optionalList(osasuoritukset.toList.flatten
    .filter(_.viimeisinArviointi.exists(_.hyväksytty))
    .map(_.koulutusmoduuli)
    .collect {
      case o: NuortenPerusopetuksenÄidinkieliJaKirjallisuus if o.pakollinen => kieliaineesta(o)
      case l: LukionÄidinkieliJaKirjallisuus if l.pakollinen => kieliaineesta(l)
    }.flatten
    .sortBy(_.koodiarvo)
    .distinct
  )

  private def kieliaineesta(kieliaine: Kieliaine): Option[Koodistokoodiviite] =
    if (kieliaine.kieli.koodiarvo == "AI1") {
      Koulusivistyskieli.suomi
    } else if (kieliaine.kieli.koodiarvo == "AI2") {
      Koulusivistyskieli.ruotsi
    } else {
      None
    }
}
