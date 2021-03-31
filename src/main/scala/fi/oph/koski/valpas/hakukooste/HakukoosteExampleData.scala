package fi.oph.koski.valpas.hakukooste

import fi.oph.koski.henkilo.OppijaHenkilö
import fi.oph.koski.organisaatio.{MockOrganisaatioRepository, MockOrganisaatiot}
import fi.oph.koski.schema._
import fi.oph.koski.valpas.henkilo.ValpasMockOppijat
import fi.vm.sade.oidgenerator.OIDGenerator

import java.time.{LocalDate, LocalDateTime}

object HakukoosteExampleData {
  private implicit class Conversions(maybe: Option[LocalizedString]) {
    def toBlankable: BlankableLocalizedString = maybe.getOrElse(BlankLocalizedString())
  }

  lazy val data = List(
    haku(
      ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021,
      List(
        hakutoive(
          hakukohdeOid = generateOid(),
          hakukohdeOrganisaatio = MockOrganisaatiot.ressunLukio,
          hakukohdeNimi = "Lukio",
          koulutusNimi = "Lukiokoulutus"
        ).copy(alinValintaPistemaara = Some(9.01), pisteet = Some(9)),
        hakutoive(
          hakukohdeOid = generateOid(),
          hakukohdeOrganisaatio = MockOrganisaatiot.helsinginMedialukio,
          hakukohdeNimi = "Lukio",
          koulutusNimi = "Lukiokoulutus"
        ).copy(alinValintaPistemaara = Some(8.2), pisteet = Some(9)),
        hakutoive(
          hakukohdeOid = generateOid(),
          hakukohdeOrganisaatio = MockOrganisaatiot.omnia,
          hakukohdeNimi = "Leipomoala",
          koulutusNimi = "Leipomoalan ammattitutkinto"
        ),
        hakutoive(
          hakukohdeOid = generateOid(),
          hakukohdeOrganisaatio = MockOrganisaatiot.omnia,
          hakukohdeNimi = "Puhtaus- ja kiinteistöpalveluala",
          koulutusNimi = "Puhtaus- ja kiinteistöpalvelualan ammattitutkinto laitoshuoltajille ja toimitilahuoltajille"
        ),
        hakutoive(
          hakukohdeOid = generateOid(),
          hakukohdeOrganisaatio = MockOrganisaatiot.varsinaisSuomenKansanopisto,
          hakukohdeNimi = "Vapaan sivistystyön koulutus oppivelvollisille 2021-2022",
          koulutusNimi = "Vapaan sivistystyön koulutus oppivelvollisille"
        ),
      )),
    haku(
      ValpasMockOppijat.turvakieltoOppija,
      List(
        hakutoive(
          hakukohdeOid = generateOid(),
          hakukohdeOrganisaatio = MockOrganisaatiot.ressunLukio,
          hakukohdeNimi = "Lukio",
          koulutusNimi = "Lukiokoulutus"
        ).copy(alinValintaPistemaara = Some(9.01), pisteet = Some(9)),
      )),
    haku(
      ValpasMockOppijat.luokalleJäänytYsiluokkalainen,
      List(
        hakutoive(
          hakukohdeOid = generateOid(),
          hakukohdeOrganisaatio = "",
          hakukohdeNimi = "Lukio",
          koulutusNimi = "Lukiokoulutus"
        ),
      )),
  )

  def haku(
    henkilö: OppijaHenkilö,
    hakutoiveet: Seq[Hakutoive],
    alkamisaika: LocalDateTime = LocalDateTime.of(2020, 3, 9, 12, 0, 0),
  ): Hakukooste =
    Hakukooste(
      oppijaOid = henkilö.oid,
      hakuOid = generateHakuOid(),
      hakemusOid = generateHakemusOid(),
      hakemusUrl = "/placeholder-hakemus-url",
      hakutapa = yhteishakukoodi,
      hakutyyppi = varsinaisenHaunKoodi,
      haunAlkamispaivamaara = alkamisaika,
      hakuNimi = Finnish("Yhteishaku 2021"),
      email = generateEmail(henkilö),
      lahiosoite = "Esimerkkikatu 123",
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

  def hakutoive(
    hakukohdeOid: String,
    hakukohdeOrganisaatio: String,
    hakukohdeNimi: String,
    koulutusNimi: String
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
      alinValintaPistemaara = None,
      valintatila = Some("KESKEN"),
      vastaanottotieto = Some("KESKEN"),
      ilmoittautumistila = Some("EI_ILMOITTAUTUNUT"),
      koulutusOid = Some("TODO"),
      harkinnanvaraisuus = Some("TODO"),
      hakukohdeKoulutuskoodi = "TODO"
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
