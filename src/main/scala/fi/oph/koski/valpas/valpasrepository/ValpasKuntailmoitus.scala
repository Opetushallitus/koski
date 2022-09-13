package fi.oph.koski.valpas.valpasrepository

import fi.oph.koski.schema.annotation.{KoodistoKoodiarvo, KoodistoUri}
import fi.oph.koski.schema.{Koodistokoodiviite, OrganisaatioWithOid}
import fi.oph.koski.valpas.yhteystiedot.ValpasYhteystietojenAlkuperä

import java.time.LocalDateTime
import fi.oph.koski.valpas.opiskeluoikeusrepository.{ValpasOpiskeluoikeus, ValpasOpiskeluoikeusLaajatTiedot, ValpasOppivelvollinenOppijaLaajatTiedot, ValpasRajapäivätService}

case class ValpasKuntailmoitusLaajatTiedot(
  oppijaOid: Option[String],
  id: Option[String], // Oikeasti UUID - scala-schemasta puuttuu tuki UUID-tyypille
  kunta: OrganisaatioWithOid,
  aikaleima: Option[LocalDateTime], // Option, koska create-operaatiossa bäkkäri täyttää ilmoitusajan
  tekijä: ValpasKuntailmoituksenTekijäLaajatTiedot,
  @KoodistoUri("kieli")
  @KoodistoKoodiarvo("FI")
  @KoodistoKoodiarvo("SV")

  // Option, koska riippuen käyttöoikeuksista käyttäjä voi saada nähdä vain osan tietyn ilmoituksen tiedoista,
  // tai tätä ei ole enää tallessa, koska on oppivelvollisuusrekisterin ulkopuolista dataa.
  yhteydenottokieli: Option[Koodistokoodiviite],

  // Option, koska riippuen käyttöoikeuksista käyttäjä voi saada nähdä vain osan tietyn
  // ilmoituksen tiedoista, tai tätä ei ole enää tallessa, koska on oppivelvollisuusrekisterin ulkopuolista dataa
  oppijanYhteystiedot: Option[ValpasKuntailmoituksenOppijanYhteystiedot],

  // Option, koska riippuen käyttöoikeuksista käyttäjä voi saada nähdä vain osan tietyn ilmoituksen tiedoista,
  // tai tätä ei ole enää tallessa, koska on oppivelvollisuusrekisterin ulkopuolista dataa.
  hakenutMuualle: Option[Boolean],

  // Option, koska relevantti kenttä vain haettaessa ilmoituksia tietylle kunnalle
  onUudempiaIlmoituksiaMuihinKuntiin: Option[Boolean],

  aktiivinen: Option[Boolean],

  // Tietoja ilmoituksen näytettävistä tiedoista on karsittu käyttöoikeuksien perusteella
  tietojaKarsittu: Option[Boolean] = None,

) {
  def withOppijaOid(oppijaOid: String): ValpasKuntailmoitusLaajatTiedot =
    this.copy(oppijaOid = Some(oppijaOid))

  def withAktiivinen(aktiivinen: Boolean): ValpasKuntailmoitusLaajatTiedot =
    this.copy(aktiivinen = Some(aktiivinen))
}

case class ValpasKuntailmoituksenTekijäLaajatTiedot(
  organisaatio: OrganisaatioWithOid,
  henkilö: Option[ValpasKuntailmoituksenTekijäHenkilö], // Option, koska tämä on oppivelvollisuurekisterin ulkopuolista lisädataa eikä välttämättä tallessa tietokannassa vaikka muuta ilmoituksen tiedot olisivatkin
)

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
  tekijäHenkilö: ValpasKuntailmoituksenTekijäHenkilö,
  mahdollisetTekijäOrganisaatiot: Seq[OrganisaatioWithOid],
  oppijat: Seq[ValpasOppijanPohjatiedot],
  kunnat: Seq[OrganisaatioWithOid],
  @KoodistoUri("maatjavaltiot2")
  maat: Seq[Koodistokoodiviite],
  @KoodistoUri("kieli")
  yhteydenottokielet: Seq[Koodistokoodiviite],
)

case class ValpasOppijanPohjatiedot(
  oppijaOid: String,
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

case class ValpasKuntailmoitusOpiskeluoikeusKonteksti(
  ilmoitusId: String, // Oikeasti UUID - scala-schemasta puuttuu tuki UUID-tyypille
  opiskeluoikeusOid: ValpasOpiskeluoikeus.Oid
)
