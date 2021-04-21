package fi.oph.koski.valpas.hakukooste

import fi.oph.koski.henkilo.OppijaHenkilö
import fi.oph.koski.organisaatio.{MockOrganisaatioRepository, MockOrganisaatiot}
import fi.oph.koski.schema._
import fi.vm.sade.oidgenerator.OIDGenerator
import java.time.{LocalDate, LocalDateTime}

import fi.oph.koski.valpas.opiskeluoikeusfixture.ValpasMockOppijat

object HakukoosteExampleData {
  private implicit class Conversions(maybe: Option[LocalizedString]) {
    def toBlankable: BlankableLocalizedString = maybe.getOrElse(BlankLocalizedString())
  }

  lazy val data: Seq[Hakukooste] = Vector(
    haku(
      ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021,
      Vector(Vector(
        hakutoive(
          hakukohdeOid = generateOid(),
          hakukohdeOrganisaatio = MockOrganisaatiot.ressunLukio,
          hakukohdeNimi = "Lukio",
          koulutusNimi = "Lukiokoulutus",
          valintatila = Some("HYLATTY"),
        ).copy(
          alinHyvaksyttyPistemaara = Some(9.01),
          pisteet = Some(9),
        ),
        hakutoive(
          hakukohdeOid = generateOid(),
          hakukohdeOrganisaatio = MockOrganisaatiot.helsinginMedialukio,
          hakukohdeNimi = "Lukio",
          koulutusNimi = "Lukiokoulutus",
          valintatila = Some("HYVAKSYTTY"),
          vastaanottotieto = Some("VASTAANOTTANUT_SITOVASTI"),
        ).copy(
          alinHyvaksyttyPistemaara = Some(8.2),
          pisteet = Some(9),
        ),
        hakutoive(
          hakukohdeOid = generateOid(),
          hakukohdeOrganisaatio = MockOrganisaatiot.omnia,
          hakukohdeNimi = "Leipomoala",
          koulutusNimi = "Leipomoalan ammattitutkinto",
          valintatila = Some("PERUUNTUNUT"),
        ),
        hakutoive(
          hakukohdeOid = generateOid(),
          hakukohdeOrganisaatio = MockOrganisaatiot.omnia,
          hakukohdeNimi = "Puhtaus- ja kiinteistöpalveluala",
          koulutusNimi = "Puhtaus- ja kiinteistöpalvelualan ammattitutkinto laitoshuoltajille ja toimitilahuoltajille",
          valintatila = Some("PERUUNTUNUT"),
        ),
        hakutoive(
          hakukohdeOid = generateOid(),
          hakukohdeOrganisaatio = MockOrganisaatiot.varsinaisSuomenKansanopisto,
          hakukohdeNimi = "Vapaan sivistystyön koulutus oppivelvollisille 2021-2022",
          koulutusNimi = "Vapaan sivistystyön koulutus oppivelvollisille",
          valintatila = Some("PERUUNTUNUT"),
        ),
      ))),
    haku(
      ValpasMockOppijat.turvakieltoOppija,
      Vector(Vector(
        hakutoive(
          hakukohdeOid = generateOid(),
          hakukohdeOrganisaatio = MockOrganisaatiot.ressunLukio,
          hakukohdeNimi = "Lukio",
          koulutusNimi = "Lukiokoulutus",
          valintatila = Some("HYLATTY"),
        ).copy(
          alinHyvaksyttyPistemaara = Some(9.01),
          pisteet = Some(9)
        ),
      ))),
    haku(
      henkilö = ValpasMockOppijat.luokalleJäänytYsiluokkalainen,
      muokkausaika = Some(LocalDateTime.of(2020, 4, 10, 12, 0, 0)),
      lahiosoite = "Uudempi esimerkkikatu 987",
      hakukoosteidenToiveet = Vector(
        Vector(
          hakutoive(
            hakukohdeOid = generateOid(),
            hakukohdeOrganisaatio = MockOrganisaatiot.helsinginMedialukio,
            hakukohdeNimi = "Lukio",
            koulutusNimi = "Lukiokoulutus"
          ),
        ),
        Vector(
          hakutoive(
            hakukohdeOid = generateOid(),
            hakukohdeOrganisaatio = MockOrganisaatiot.varsinaisSuomenKansanopisto,
            hakukohdeNimi = "Vapaan sivistystyön koulutus oppivelvollisille 2021-2022",
            koulutusNimi = "Vapaan sivistystyön koulutus oppivelvollisille"
          ),
        )
      )),
    haku(
      hakuNimi = Finnish("Yhteishaku 2019"),
      aktiivinenHaku = Some(false),
      henkilö = ValpasMockOppijat.luokalleJäänytYsiluokkalainen,
      alkamisaika = LocalDateTime.of(2019, 3, 9, 12, 0, 0),
      muokkausaika = None,
      hakukoosteidenToiveet = Vector(
        Vector(
          hakutoive(
            hakukohdeOid = generateOid(),
            hakukohdeOrganisaatio = MockOrganisaatiot.varsinaisSuomenKansanopisto,
            hakukohdeNimi = "Vapaan sivistystyön koulutus oppivelvollisille 2019-2020",
            koulutusNimi = "Vapaan sivistystyön koulutus oppivelvollisille",
          ),
        )
      )),
    haku(
      ValpasMockOppijat.päällekkäisiäOpiskeluoikeuksia,
      Vector(Vector(
        hakutoive(
          hakukohdeOid = generateOid(),
          hakukohdeOrganisaatio = MockOrganisaatiot.helsinginMedialukio,
          hakukohdeNimi = "Lukio",
          koulutusNimi = "Lukiokoulutus",
          valintatila = Some("HYLATTY"),
        ).copy(
          alinHyvaksyttyPistemaara = Some(8.2),
          pisteet = Some(7.5),
        ),
        hakutoive(
          hakukohdeOid = generateOid(),
          hakukohdeOrganisaatio = MockOrganisaatiot.omnia,
          hakukohdeNimi = "Leipomoala",
          koulutusNimi = "Leipomoalan ammattitutkinto",
          valintatila = Some("HYVAKSYTTY"),
          vastaanottotieto = Some("EHDOLLISESTI_VASTAANOTTANUT"),
        ).copy(
          harkinnanvaraisuus = Some("sosiaalisetsyyt")
        ),
        hakutoive(
          hakukohdeOid = generateOid(),
          hakukohdeOrganisaatio = MockOrganisaatiot.omnia,
          hakukohdeNimi = "Puhtaus- ja kiinteistöpalveluala",
          koulutusNimi = "Puhtaus- ja kiinteistöpalvelualan ammattitutkinto laitoshuoltajille ja toimitilahuoltajille",
          valintatila = Some("HYVAKSYTTY"),
        ),
      ))),
    haku(
      ValpasMockOppijat.oppivelvollinenMonellaOppijaOidillaMaster,
      Vector(Vector(
        hakutoive(
          hakukohdeOid = generateOid(),
          hakukohdeOrganisaatio = MockOrganisaatiot.ressunLukio,
          hakukohdeNimi = "Lukio",
          koulutusNimi = "Lukiokoulutus",
          valintatila = Some("VARALLA"),
        ).copy(
          alinHyvaksyttyPistemaara = Some(8.99),
          pisteet = Some(9)
        ),
      ))),
  ).flatten

  def haku(
    henkilö: OppijaHenkilö,
    hakukoosteidenToiveet: Seq[Seq[Hakutoive]],
    hakuNimi: BlankableLocalizedString = Finnish("Yhteishaku 2021"),
    aktiivinenHaku: Some[Boolean] = Some(true),
    alkamisaika: LocalDateTime = LocalDateTime.of(2020, 3, 9, 12, 0, 0),
    muokkausaika: Option[LocalDateTime] = Some(LocalDateTime.of(2020, 4, 9, 12, 0, 0)),
    lahiosoite: String = "Esimerkkikatu 123"
  ): Seq[Hakukooste] = hakukoosteidenToiveet.map(hakutoiveet =>
    Hakukooste(
      oppijaOid = henkilö.oid,
      hakuOid = generateHakuOid(),
      aktiivinenHaku = aktiivinenHaku,
      hakemusOid = generateHakemusOid(),
      hakemusUrl = "/placeholder-hakemus-url",
      hakutapa = yhteishakukoodi,
      hakutyyppi = varsinaisenHaunKoodi,
      haunAlkamispaivamaara = alkamisaika,
      hakemuksenMuokkauksenAikaleima = muokkausaika,
      hakuNimi = hakuNimi,
      email = generateEmail(henkilö),
      lahiosoite = lahiosoite,
      postinumero = "00000",
      postitoimipaikka = Some("Helsinki"),
      matkapuhelin = "0401234567",
      huoltajanNimi = Some("Huoltaja Sukunimi"),
      huoltajanPuhelinnumero = Some("0407654321"),
      huoltajanSähkoposti = Some("huoltaja.sukunimi@gmail.com"),
      hakutoiveet = hakutoiveet.map(hakutoive => hakutoive.copy(
        organisaatioNimi = MockOrganisaatioRepository
          .getOrganisaationNimiHetkellä(hakutoive.hakukohdeOrganisaatio, alkamisaika.toLocalDate)
          .toBlankable,
        hakukohdeNimi = hakutoive.hakukohdeNimi,
        koulutusNimi = hakutoive.koulutusNimi,
        hakutoivenumero = if (hakutoive.hakutoivenumero >= 0) {
          hakutoive.hakutoivenumero
        } else {
          hakutoiveet.indexOf(hakutoive) + 1
        }
      ))
    )
  )

  def hakutoive(
    hakukohdeOid: String,
    hakukohdeOrganisaatio: String,
    hakukohdeNimi: String,
    koulutusNimi: String,
    valintatila: Option[String] = None,
    vastaanottotieto: Option[String] = None,
  ): Hakutoive =
    Hakutoive(
      hakukohdeOid = hakukohdeOid,
      hakukohdeNimi = Finnish(hakukohdeNimi),
      organisaatioNimi = MockOrganisaatioRepository.getOrganisaationNimiHetkellä(
        oid = hakukohdeOrganisaatio,
        localDate = LocalDate.now()
      ).toBlankable,
      hakutoivenumero = -1,
      koulutusNimi = Finnish(koulutusNimi),
      hakukohdeOrganisaatio = hakukohdeOrganisaatio,
      pisteet = None,
      alinHyvaksyttyPistemaara = None,
      valintatila = valintatila,
      vastaanottotieto = vastaanottotieto,
      ilmoittautumistila = Some("EI_ILMOITTAUTUNUT"),
      koulutusOid = Some("TODO"),
      harkinnanvaraisuus = None,
      hakukohdeKoulutuskoodi = Koodistokoodiviite("321152", "koulutus"),
      varasijanumero = if (valintatila == Some("VARALLA")) Some(3) else None,
    )

  def yhteishakukoodi = Koodistokoodiviite("01", "hakutapa")
  def varsinaisenHaunKoodi = Koodistokoodiviite("01", "hakutyyppi")
  def generateHakuOid() = OIDGenerator.generateOID(100)
  def generateHakemusOid() = OIDGenerator.generateOID(101)
  def generateEmail(henkilö: OppijaHenkilö) =
    s"${henkilö.etunimet}.${henkilö.sukunimi}".replace(" ", "-") + "@gmail.com"

  private var oidCounter: Int = 1
  private def generateOid(): String= {
    val oid = "1.2.246.562.24." + "999%08d".format(oidCounter)
    oidCounter = oidCounter + 1
    oid
  }
}
