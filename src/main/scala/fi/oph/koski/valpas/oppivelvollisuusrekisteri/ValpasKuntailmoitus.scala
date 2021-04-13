package fi.oph.koski.valpas.oppivelvollisuusrekisteri

import java.time.LocalDate

import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, KoodistoUri}
import fi.oph.koski.schema.{Koodistokoodiviite, LocalizedString}

trait ValpasKuntailmoitus {
  def id: Option[Int]
  def tekijä: Option[ValpasKuntailmoituksenTekijä]
  def kunta: ValpasKunta
  def ilmoituspäivä: Option[LocalDate]
}

case class ValpasKuntailmoitusSuppeatTiedot(
  id: Option[Int],
  tekijä: Option[ValpasKuntailmoituksenTekijäSuppeatTiedot],
  kunta: ValpasKunta,
  ilmoituspäivä: Option[LocalDate]
) extends ValpasKuntailmoitus

object ValpasKuntailmoitusSuppeatTiedot {
  def apply(laajatTiedot: ValpasKuntailmoitusLaajatTiedot): ValpasKuntailmoitusSuppeatTiedot = {
    ValpasKuntailmoitusSuppeatTiedot(
      laajatTiedot.id,
      laajatTiedot.tekijä match {
        case Some(tekijänLaajatTiedot) => Some(ValpasKuntailmoituksenTekijäSuppeatTiedot(tekijänLaajatTiedot))
        case _ => None
      },
      laajatTiedot.kunta,
      laajatTiedot.ilmoituspäivä
    )
  }
}

case class ValpasKuntailmoitusLaajatTiedot(
  id: Option[Int],
  kunta: ValpasKunta,
  ilmoituspäivä: Option[LocalDate], // Option, koska create-operaatiossa bäkkäri täyttää ilmoituspäivän
  tekijä: Option[ValpasKuntailmoituksenTekijäLaajatTiedot], // Option, koska kunnan tekemissä ilmoituksissa tiedot voidaan päätellä kokonaan aiemmista
                                                            // ilmoituksista ja tekijän sessiosta
  @KoodistoUri("kieli")
  @KoodistoKoodiarvo("FI")
  @KoodistoKoodiarvo("SV")
  yhteydenottokieli: Option[Koodistokoodiviite], // Option, koska riippuen käyttöoikeuksista käyttäjä voi saada nähdä vain osan tietyn ilmoituksen tiedoista,
                                                 // tai tätä ei ole enää tallessa, koska on oppivelvollisuusrekisterin ulkopuolista dataa
  oppijanYhteystiedot: Option[ValpasKuntailmoituksenOppijanYhteystiedot] // Option, koska riippuen käyttöoikeuksista käyttäjä voi saada nähdä vain osan tietyn
                                                                         // ilmoituksen tiedoista, tai tätä ei ole enää tallessa, koska on oppivelvollisuusrekisterin ulkopuolista dataa
) extends ValpasKuntailmoitus

case class ValpasKunta(
  oid: ValpasKunta.Oid,
  nimi: Option[LocalizedString], // Option, koska create-operaatiossa käytetään pelkkää oidia
  @KoodistoUri("kunta")
  kotipaikka: Option[Koodistokoodiviite] // Option, koska create-operaatiossa käytetään pelkkää oidia
)

object ValpasKunta {
  type Oid = String
}

trait ValpasKuntailmoituksenTekijä {
  def organisaatio: ValpasKuntailmoituksenTekijäOrganisaatio
}

case class ValpasKuntailmoituksenTekijäSuppeatTiedot(
  organisaatio: ValpasKuntailmoituksenTekijäOrganisaatio
) extends ValpasKuntailmoituksenTekijä

object ValpasKuntailmoituksenTekijäSuppeatTiedot {
  def apply(laajatTiedot: ValpasKuntailmoituksenTekijäLaajatTiedot): ValpasKuntailmoituksenTekijäSuppeatTiedot = {
    ValpasKuntailmoituksenTekijäSuppeatTiedot(laajatTiedot.organisaatio)
  }
}

case class ValpasKuntailmoituksenTekijäLaajatTiedot(
  organisaatio: ValpasKuntailmoituksenTekijäOrganisaatio,
  henkilö: Option[ValpasKuntailmoituksenTekijäHenkilö] // Option, koska tämä on oppivelvollisuurekisterin ulkopuolista lisädataa eikä välttämättä tallessa tietokannassa vaikka muuta ilmoituksen tiedot olisivatkin
) extends ValpasKuntailmoituksenTekijä

case class ValpasKuntailmoituksenTekijäOrganisaatio(
  oid: ValpasKuntailmoituksenTekijäOrganisaatio.Oid,
  nimi: Option[LocalizedString]
)

object ValpasKuntailmoituksenTekijäOrganisaatio {
  type Oid = String
}

case class ValpasKuntailmoituksenTekijäHenkilö(
  oid: Option[ValpasKuntailmoituksenTekijäHenkilö.Oid], // Option, koska create-operaatiossa bäkkäri lukee tekijän oidin sessiosta
  etunimi: String,
  sukunimi: String,
  email: Option[String] // Option, koska kunnan itselleen tekemissä osoitteenmuutosilmoituksissa tätä ei tallenneta.
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
  maa: Option[String] = None
)
