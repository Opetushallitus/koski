package fi.oph.koski.valpas.hakukooste

import fi.oph.koski.henkilo.OppijaHenkilö
import fi.oph.koski.organisaatio.MockOrganisaatiot
import fi.oph.koski.schema.{Finnish, Koodistokoodiviite}
import fi.oph.koski.valpas.henkilo.ValpasMockOppijat
import fi.vm.sade.oidgenerator.OIDGenerator

class MockHakukoosteService extends ValpasHakukoosteService {
  // TODO: Naputtele oikeat testidatat
  private val mockData = ValpasMockOppijat.defaultOppijat.map(oppija => generateMockHaku(oppija.henkilö))

  def getHakukoosteet(oppijaOids: Set[String]): Either[ValpasHakukoosteServiceError, Seq[Hakukooste]] =
    Right(mockData.filter(entry => oppijaOids.contains(entry.oppijaOid)))

  private def generateMockHaku(henkilö: OppijaHenkilö): Hakukooste =
    Hakukooste(
      oppijaOid = henkilö.oid,
      hakuOid = generateHakuOid(),
      hakemusOid = generateHakemusOid(),
      hakutapa = Koodistokoodiviite("01", "hakutapa"), // 01 = Yhteishaku
      hakutyyppi = Koodistokoodiviite("01", "hakutyyppi"), // 01 = Varsinainen haku
      muokattu = "2020-03-09T09:31:00+03:00",
      hakuNimi = Finnish("Jatkuva haku 2021"),
      email = generateEmail(henkilö),
      osoite = "Esimerkkikatu 123, 00000 KAUPUNKI",
      matkapuhelin = "0401234567",
      huoltajanNimi = "Huoltaja Sukunimi",
      huoltajanPuhelinnumero = "0407654321",
      huoltajanSahkoposti = "huoltaja.sukunimi@gmail.com",
      hakutoiveet = List(generateMockHakutoive())
    )

  private def generateMockHakutoive(): Hakutoive =
    Hakutoive(
      hakukohdeOid = MockOrganisaatiot.ressunLukio,
      hakukohdeNimi = Finnish("Ressun lukio"),
      hakutoivenumero = 1,
      koulutusNimi = Finnish("Lukio"),
      hakukohdeOrganisaatio = "TODO",
      pisteet = 3.45f,
      valintatila = Valintatila.KESKEN,
      vastaanottotieto = Vastaanottotieto.KESKEN,
      ilmoittautumistila = Ilmoittautumistila.EI_ILMOITTAUTUNUT,
      koulutusOid = "TODO",
      harkinnanvaraisuus = "TODO",
      hakukohdeKoulutuskoodi = "TODO"
    )

  private def generateHakuOid() = OIDGenerator.generateOID(100)
  private def generateHakemusOid() = OIDGenerator.generateOID(101)
  private def generateEmail(henkilö: OppijaHenkilö) =
    s"${henkilö.etunimet}.${henkilö.sukunimi}".replace(" ", "-") + "@gmail.com"
}
