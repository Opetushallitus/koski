package fi.oph.koski.valpas.hakukooste

import fi.oph.koski.henkilo.OppijaHenkilö
import fi.oph.koski.organisaatio.{MockOrganisaatioRepository, MockOrganisaatiot}
import fi.oph.koski.schema.{Finnish, Koodistokoodiviite}
import fi.oph.koski.valpas.henkilo.ValpasMockOppijat
import fi.vm.sade.oidgenerator.OIDGenerator

import java.time.{LocalDate, LocalDateTime}

object HakukoosteExampleData {
  lazy val data = List(
    haku(
      ValpasMockOppijat.oppivelvollinenYsiluokkaKeskenKeväällä2021,
      List(
        hakutoive(
          hakukohdeOid = MockOrganisaatiot.ressunLukio,
          koulutusNimi = "Lukiokoulutus"
        ),
        hakutoive(
          hakukohdeOid = MockOrganisaatiot.helsinginMedialukio,
          koulutusNimi = "Lukiokoulutus"
        ),
        hakutoive(
          hakukohdeOid = MockOrganisaatiot.omnia,
          koulutusNimi = "Leipomoalan ammattitutkinto"
        ),
        hakutoive(
          hakukohdeOid = MockOrganisaatiot.omnia,
          koulutusNimi = "Puhtaus- ja kiinteistöpalvelualan ammattitutkinto laitoshuoltajille ja toimitilahuoltajille"
        ),
        hakutoive(
          hakukohdeOid = MockOrganisaatiot.varsinaisSuomenKansanopisto,
          koulutusNimi = "Vapaan sivistystyön koulutus oppivelvollisille"
        ),
      ))
  )

  def haku(
    henkilö: OppijaHenkilö,
    hakutoiveet: Seq[Hakutoive],
    muokattu: LocalDateTime = LocalDateTime.of(2020, 3, 9, 12, 0, 0),
  ): Hakukooste =
    Hakukooste(
      oppijaOid = henkilö.oid,
      hakuOid = generateHakuOid(),
      hakemusOid = generateHakemusOid(),
      hakutapa = yhteishakukoodi,
      hakutyyppi = varsinaisenHaunKoodi,
      muokattu = muokattu.toString,
      hakuNimi = Finnish("Yhteishaku 2021"),
      email = generateEmail(henkilö),
      osoite = "Esimerkkikatu 123, 00000 KAUPUNKI",
      matkapuhelin = "0401234567",
      huoltajanNimi = "Huoltaja Sukunimi",
      huoltajanPuhelinnumero = "0407654321",
      huoltajanSahkoposti = "huoltaja.sukunimi@gmail.com",
      hakutoiveet = hakutoiveet.map(hakutoive => hakutoive.copy(
        hakukohdeNimi = MockOrganisaatioRepository.getOrganisaationNimiHetkellä(oid = hakutoive.hakukohdeOid, localDate = muokattu.toLocalDate).get,
        hakutoivenumero = if (hakutoive.hakutoivenumero >= 0) {
          hakutoive.hakutoivenumero
        } else {
          hakutoiveet.indexOf(hakutoive)
        }
      ))
    )

  def hakutoive(
    hakukohdeOid: String,
    koulutusNimi: String,
    hakutoivenumero: Int = -1
  ): Hakutoive =
    Hakutoive(
      hakukohdeOid = hakukohdeOid,
      hakukohdeNimi = MockOrganisaatioRepository.getOrganisaationNimiHetkellä(oid = hakukohdeOid, localDate = LocalDate.now()).get,
      hakutoivenumero = hakutoivenumero,
      koulutusNimi = Finnish(koulutusNimi),
      hakukohdeOrganisaatio = hakukohdeOid,
      pisteet = 0,
      alinValintaPistemaara = 0,
      valintatila = "KESKEN",
      vastaanottotieto = "KESKEN",
      ilmoittautumistila = "EI_ILMOITTAUTUNUT",
      koulutusOid = "TODO",
      harkinnanvaraisuus = "TODO",
      hakukohdeKoulutuskoodi = "TODO"
    )

  def yhteishakukoodi = Koodistokoodiviite("01", "hakutapa")
  def varsinaisenHaunKoodi = Koodistokoodiviite("01", "hakutyyppi")
  def generateHakuOid() = OIDGenerator.generateOID(100)
  def generateHakemusOid() = OIDGenerator.generateOID(101)
  def generateEmail(henkilö: OppijaHenkilö) =
    s"${henkilö.etunimet}.${henkilö.sukunimi}".replace(" ", "-") + "@gmail.com"
}
