package fi.oph.koski.valpas.valpasrepository

import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, KoodistoUri}
import fi.oph.koski.schema.{Koodistokoodiviite, OrganisaatioWithOid}
import fi.oph.koski.valpas.yhteystiedot.ValpasYhteystietojenAlkuperä
import java.time.LocalDateTime

import fi.oph.koski.valpas.ValpasKuntailmoitusLaajatTiedotLisätiedoilla

case class ValpasKuntailmoitusLaajatTiedotJaOppijaOid(
  oppijaOid: String,
  kuntailmoitus: ValpasKuntailmoitusLaajatTiedot
)

trait ValpasKuntailmoitus {
  def id: Option[String] // TODO: Scala-schemasta puuttuu UUID-tuki :(
  def tekijä: ValpasKuntailmoituksenTekijä
  def kunta: OrganisaatioWithOid // Koska suuri osa kunnista on koulutustoimijoita, on niille vaikea luoda omaa tyyppiä.
                                 // Validointi, että tähän voi tallentaa vain kunta-tyyppisen organisaation, tehdään erikseen.
  def aikaleima: Option[LocalDateTime]
}

trait ValpasKuntailmoituksenTekijä {
  def organisaatio: OrganisaatioWithOid // Tekijä voi olla joko kunta tai oppilaitos. Validointi, että on jompikumpi, tehdään erikseen.
}

case class ValpasKuntailmoitusSuppeatTiedot(
  id: Option[String],
  tekijä: ValpasKuntailmoituksenTekijäSuppeatTiedot,
  kunta: OrganisaatioWithOid,
  aikaleima: Option[LocalDateTime]
) extends ValpasKuntailmoitus

object ValpasKuntailmoitusSuppeatTiedot {
  def apply(laajatTiedot: ValpasKuntailmoitusLaajatTiedotLisätiedoilla): ValpasKuntailmoitusSuppeatTiedot = {
    ValpasKuntailmoitusSuppeatTiedot(
      laajatTiedot.kuntailmoitus.id,
      ValpasKuntailmoituksenTekijäSuppeatTiedot(laajatTiedot.kuntailmoitus.tekijä),
      laajatTiedot.kuntailmoitus.kunta,
      laajatTiedot.kuntailmoitus.aikaleima
    )
  }
}

case class ValpasKuntailmoitusLaajatTiedot(
  id: Option[String],
  kunta: OrganisaatioWithOid,
  aikaleima: Option[LocalDateTime], // Option, koska create-operaatiossa bäkkäri täyttää ilmoitusajan
  tekijä: ValpasKuntailmoituksenTekijäLaajatTiedot,
  @KoodistoUri("kieli")
  @KoodistoKoodiarvo("FI")
  @KoodistoKoodiarvo("SV")
  yhteydenottokieli: Option[Koodistokoodiviite], // Option, koska riippuen käyttöoikeuksista käyttäjä voi saada nähdä vain osan tietyn ilmoituksen tiedoista,
                                                 // tai tätä ei ole enää tallessa, koska on oppivelvollisuusrekisterin ulkopuolista dataa.
  oppijanYhteystiedot: Option[ValpasKuntailmoituksenOppijanYhteystiedot], // Option, koska riippuen käyttöoikeuksista käyttäjä voi saada nähdä vain osan tietyn
                                                                         // ilmoituksen tiedoista, tai tätä ei ole enää tallessa, koska on oppivelvollisuusrekisterin ulkopuolista dataa
  hakenutMuualle: Option[Boolean]             // Option, koska riippuen käyttöoikeuksista käyttäjä voi saada nähdä vain osan tietyn ilmoituksen tiedoista,
                                                 // tai tätä ei ole enää tallessa, koska on oppivelvollisuusrekisterin ulkopuolista dataa.
) extends ValpasKuntailmoitus

case class ValpasKuntailmoituksenTekijäSuppeatTiedot(
  organisaatio: OrganisaatioWithOid
) extends ValpasKuntailmoituksenTekijä

object ValpasKuntailmoituksenTekijäSuppeatTiedot {
  def apply(laajatTiedot: ValpasKuntailmoituksenTekijäLaajatTiedot): ValpasKuntailmoituksenTekijäSuppeatTiedot = {
    ValpasKuntailmoituksenTekijäSuppeatTiedot(laajatTiedot.organisaatio)
  }
}

case class ValpasKuntailmoituksenTekijäLaajatTiedot(
  organisaatio: OrganisaatioWithOid,
  henkilö: Option[ValpasKuntailmoituksenTekijäHenkilö], // Option, koska tämä on oppivelvollisuurekisterin ulkopuolista lisädataa eikä välttämättä tallessa tietokannassa vaikka muuta ilmoituksen tiedot olisivatkin
) extends ValpasKuntailmoituksenTekijä

case class ValpasKuntailmoituksenTekijäHenkilö(
  oid: Option[ValpasKuntailmoituksenTekijäHenkilö.Oid], // Option, koska create-operaatiossa bäkkäri lukee tekijän oidin sessiosta
  etunimet: Option[String], // Option, koska kuntailmoitusta tehdessä tieto täytetään käyttäjän tiedoista
  sukunimi: Option[String], // Option, koska kuntailmoitusta tehdessä tieto täytetään käyttäjän tiedoista
  kutsumanimi: Option[String],
  email: Option[String],
  puhelinnumero: Option[String]
)

object ValpasKuntailmoituksenTekijäHenkilö {
  type Oid = String
}

case class ValpasKuntailmoituksenOppijanYhteystiedot(
  puhelinnumero: Option[String] = None,
  email: Option[String] = None,
  lähiosoite: Option[String] = None,
  postinumero: Option[String] = None,
  postitoimipaikka: Option[String] = None,
  @KoodistoUri("maatjavaltiot2")
  maa: Option[Koodistokoodiviite] = None
)

case class ValpasKuntailmoitusPohjatiedotInput(
  /**
   * Option, koska detaljinäkymästä ilmoitusta tehtäessä tekijäroganisaatiota ei välttämättä tiedetä. Paluuarvossa
   * palautetaan sen vuoksi mahdollisetTekijäOrganisaatiot, jotka voi tarjota käyttäjälle valitsimessa.
   */
  tekijäOrganisaatio: Option[OrganisaatioWithOid],
  oppijaOidit: List[String]
)

case class ValpasKuntailmoitusPohjatiedot(
  tekijäHenkilö: Option[ValpasKuntailmoituksenTekijäHenkilö] = None,
  mahdollisetTekijäOrganisaatiot: Seq[OrganisaatioWithOid] = Seq.empty,
  oppijat: Seq[ValpasOppijanPohjatiedot] = Seq.empty,
  kunnat: Seq[OrganisaatioWithOid] = Seq.empty,
  @KoodistoUri("maatjavaltiot2")
  maat: Seq[Koodistokoodiviite] = Seq.empty,
  @KoodistoUri("kieli")
  yhteydenottokielet: Seq[Koodistokoodiviite] = Seq.empty,
)

case class ValpasOppijanPohjatiedot(
  oppijaOid: String,
  mahdollisetTekijäOrganisaatiot: Seq[OrganisaatioWithOid],
  @KoodistoUri("kieli")
  @KoodistoKoodiarvo("FI")
  @KoodistoKoodiarvo("SV")
  yhteydenottokieli: Option[Koodistokoodiviite],
  turvakielto: Boolean,
  yhteystiedot: Seq[ValpasPohjatietoYhteystieto],
  hetu: Option[String],
)

case class ValpasPohjatietoYhteystieto(
  yhteystietojenAlkuperä: ValpasYhteystietojenAlkuperä,
  yhteystiedot: ValpasKuntailmoituksenOppijanYhteystiedot,
  kunta: Option[OrganisaatioWithOid]
)
