package fi.oph.koski.koskiuser

import fi.oph.koski.koskiuser.AuthenticationUser.fromDirectoryUser
import fi.oph.koski.koskiuser.MockKäyttöoikeusryhmät._
import fi.oph.koski.organisaatio.MockOrganisaatiot._
import fi.oph.koski.organisaatio.{MockOrganisaatiot, Opetushallitus}
import fi.oph.koski.userdirectory._

import java.net.InetAddress

object MockUsers {
  val kalle = KoskiMockUser(
    "käyttäjä",
    "kalle",
    "1.2.246.562.24.99999999987",
    (lehtikuusentienToimipiste :: oppilaitokset).map(oppilaitosTallentaja)
  )

  val pärre = KoskiMockUser(
    "käyttäjä",
    "pärre",
    "1.2.246.562.24.99999999901",
    (lehtikuusentienToimipiste :: oppilaitokset).map(oppilaitosTallentaja),
    "sv"
  )

  val omniaPalvelukäyttäjä = KoskiMockUser(
    "käyttäjä",
    "omnia-palvelukäyttäjä",
    "1.2.246.562.24.99999999989",
    Seq(oppilaitosPalvelukäyttäjä(omnia))
  )

  val omniaKatselija = KoskiMockUser(
    "käyttäjä",
    "omnia-katselija",
    "1.2.246.562.24.99999999990",
    Seq(oppilaitosKatselija(omnia))
  )

  val omniaTallentaja = KoskiMockUser(
    "käyttäjä",
    "omnia-tallentaja",
    "1.2.246.562.24.99999999991",
    Seq(oppilaitosTallentaja(omnia))
  )

  val omniaPääkäyttäjä = KoskiMockUser(
    "omnia-pää",
    "omnia-pää",
    "1.2.246.562.24.99999977777",
    Seq(oppilaitosPääkäyttäjä(MockOrganisaatiot.omnia))
  )

  val tallentajaEiLuottamuksellinen = KoskiMockUser(
    "epäluotettava-tallentaja",
    "epäluotettava-tallentaja",
    "1.2.246.562.24.99999999997",
    Seq(ilmanLuottamuksellisiaTietoja(omnia), ilmanLuottamuksellisiaTietoja(jyväskylänNormaalikoulu))
  )

  val paakayttajaMitatoidytJaPoistetutOpiskeluoikeudet = KoskiMockUser(
    "käyttäjä",
    "mikko",
    "1.2.246.562.24.99999999987",
    Seq(OrganisaatioJaKäyttöoikeudet(Opetushallitus.organisaatioOid,
      ophPääkäyttäjä.kayttooikeudet ++
        List(
          PalveluJaOikeus("KOSKI", Rooli.MITATOIDYT_OPISKELUOIKEUDET),
          PalveluJaOikeus("KOSKI", Rooli.POISTETUT_OPISKELUOIKEUDET)
        )
    ))
  )

  val paakayttajaMitatoidytOpiskeluoikeudet = KoskiMockUser(
    "käyttäjä",
    "mikko",
    "1.2.246.562.24.99999999987",
    Seq(OrganisaatioJaKäyttöoikeudet(Opetushallitus.organisaatioOid,
      ophPääkäyttäjä.kayttooikeudet ++
      List(
        PalveluJaOikeus("KOSKI", Rooli.MITATOIDYT_OPISKELUOIKEUDET)
      )
    ))
  )

  val paakayttaja = KoskiMockUser(
    "käyttäjä",
    "pää",
    "1.2.246.562.24.99999999992",
    Seq(OrganisaatioJaKäyttöoikeudet(Opetushallitus.organisaatioOid,
      ophPääkäyttäjä.kayttooikeudet ++
      localizationAdmin.kayttooikeudet ++
      List(
        PalveluJaOikeus("LOKALISOINTI", "CRUD"),
        PalveluJaOikeus("OPPIJANUMEROREKISTERI", "REKISTERINPITAJA")
      )
    ))
  )

  val viranomainen = KoskiMockUser(
    "käyttäjä",
    "viranomais",
    "1.2.246.562.24.99999999993",
    Seq(viranomaisKatselija)
  )

  val helsinginKaupunkiPalvelukäyttäjä = KoskiMockUser(
    "stadin-palvelu",
    "stadin-palvelu",
    "1.2.246.562.24.99999999994",
    Seq(oppilaitosPalvelukäyttäjä(helsinginKaupunki))
  )

  val helsinginKaupunkiEsiopetus = KoskiMockUser(
    "stadin-esiopetus",
    "stadin-esiopetus",
    "1.2.246.562.24.99999999944",
    Seq(oppilaitosEsiopetusKatselija(helsinginKaupunki))
  )

  val stadinAmmattiopistoTallentaja = KoskiMockUser(
    "tallentaja",
    "tallentaja",
    "1.2.246.562.24.99999999995",
    Seq(oppilaitosTallentaja(MockOrganisaatiot.stadinAmmattiopisto))
  )

  val stadinAmmattiopistoKatselija = KoskiMockUser(
    "katselija",
    "katselija",
    "1.2.246.562.24.99999999985",
    Seq(oppilaitosKatselija(MockOrganisaatiot.stadinAmmattiopisto))
  )

  val jyväskylänKatselijaEsiopetus = KoskiMockUser(
    "esiopetus",
    "esiopetus",
    "1.2.246.562.24.99999999666",
    Seq(oppilaitosEsiopetusKatselija(MockOrganisaatiot.jyväskylänNormaalikoulu))
  )

  val jyväskylänKatselijaEiLuottamuksellinen = KoskiMockUser(
    "jyvas-eiluottoa",
    "jyvas-eiluottoa",
    "1.2.246.562.24.99999999888",
    Seq(ilmanLuottamuksellisiaTietoja(MockOrganisaatiot.jyväskylänNormaalikoulu))
  )

  val stadinAmmattiopistoPääkäyttäjä = KoskiMockUser(
    "stadinammattiopisto-admin",
    "stadinammattiopisto-admin",
    "1.2.246.562.24.99999999986",
    Seq(oppilaitosPääkäyttäjä(MockOrganisaatiot.stadinAmmattiopisto)),
    "fi",
    List("koski-oppilaitos-pääkäyttäjä_1494486198456")
  )

  val stadinVastuukäyttäjä = KoskiMockUser(
    "stadin-vastuu",
    "stadin-vastuu",
    "1.2.246.562.24.99999999996",
    Seq(vastuukäyttäjä(helsinginKaupunki))
  )

  val stadinPääkäyttäjä = KoskiMockUser(
    "stadin-pää",
    "stadin-pää",
    "1.2.246.562.24.99999999997",
    Seq(oppilaitosPääkäyttäjä(helsinginKaupunki)),
    "fi",
    List("koski-oppilaitos-pääkäyttäjä_1494486198456")
  )


  val varsinaisSuomiPalvelukäyttäjä = KoskiMockUser(
    "varsinaissuomi-tallentaja",
    "varsinaissuomi-tallentaja",
    "1.2.246.562.24.99999966699",
    Seq(oppilaitosPalvelukäyttäjä(varsinaisSuomenKansanopisto))
  )

  val helsinkiTallentaja = KoskiMockUser(
    "hki-tallentaja",
    "hki-tallentaja",
    "1.2.246.562.24.99999999977",
    Seq(oppilaitosTallentaja(helsinginKaupunki))
  )

  val tornioTallentaja = KoskiMockUser(
    "tornio-tallentaja",
    "tornio-tallentaja",
    "1.2.246.562.24.99999999988",
    Seq(oppilaitosTallentaja(tornionKaupunki))
  )

  val helsinkiSekäTornioTallentaja = KoskiMockUser(
    "helsinki-tornio-tallentaja",
    "helsinki-tornio-tallentaja",
    "1.2.246.562.24.99999999922",
    Seq(oppilaitosTallentaja(helsinginKaupunki), oppilaitosTallentaja(tornionKaupunki))
  )

  val pyhtäänTallentaja = KoskiMockUser(
    "pyhtaa-tallentaja",
    "pyhtaa-tallentaja",
    "1.2.246.562.24.99999999966",
    Seq(oppilaitosTallentaja(pyhtäänKunta))
  )

  val jyväskyläTallentaja = KoskiMockUser(
    "jyvaskyla-tallentaja",
    "jyvaskyla-tallentaja",
    "1.2.246.562.24.99999999955",
    Seq(oppilaitosTallentaja(jyväskylänYliopisto))
  )

  val touholaTallentaja = KoskiMockUser(
    "touhola-tallentaja",
    "touhola-tallentaja",
    "1.2.246.562.24.99999999933",
    Seq(oppilaitosTallentaja(päiväkotiTouhula))
  )

  val majakkaTallentaja = KoskiMockUser(
    "majakka-tallentaja",
    "majakka-tallentaja",
    "1.2.246.562.24.99999999911",
    Seq(oppilaitosTallentaja(päiväkotiMajakka))
  )

  val kahdenOrganisaatioPalvelukäyttäjä = KoskiMockUser(
    "palvelu2",
    "palvelu2",
    "1.2.246.562.24.99999999998",
    Seq(oppilaitosPalvelukäyttäjä(helsinginKaupunki), oppilaitosPalvelukäyttäjä(MockOrganisaatiot.omnia))
  )

  val omattiedot = KoskiMockUser(
    "Oppija",
    "Oili",
    "1.2.246.562.24.99999999999",
    Seq(oppilaitosTallentaja(omnia))
  )

  val eiOikkia = KoskiMockUser(
    "EiOikkia",
    "Otto",
    "1.2.246.562.24.99999999902",
    Seq(OrganisaatioJaKäyttöoikeudet(lehtikuusentienToimipiste, List(PalveluJaOikeus("OPPIJANUMEROREKISTERI", "READ"))))
  )

  val evira = KoskiMockUser(
    "Evira",
    "Eeva",
    "1.2.246.562.24.99999999111",
    Seq(OrganisaatioJaKäyttöoikeudet(MockOrganisaatiot.evira, List(
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_PERUSOPETUS),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_TOINEN_ASTE),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_KORKEAKOULU),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_MUU_KUIN_SAANNELTY),
    )))
  )

  val kelaSuppeatOikeudet = KoskiMockUser(
    "Kela",
    "Suppea",
    "1.2.246.562.24.88888888111",
    Seq(OrganisaatioJaKäyttöoikeudet(MockOrganisaatiot.kela, List(
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_PERUSOPETUS),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_TOINEN_ASTE),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_KORKEAKOULU),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_MUU_KUIN_SAANNELTY),
      PalveluJaOikeus("KOSKI", Rooli.LUOTTAMUKSELLINEN_KELA_SUPPEA)
    )))
  )

  val kelaLaajatOikeudet = KoskiMockUser(
    "Kela",
    "Laaja",
    "1.2.246.562.24.88888888222",
    Seq(OrganisaatioJaKäyttöoikeudet(MockOrganisaatiot.kela, List(
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_PERUSOPETUS),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_TOINEN_ASTE),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_KORKEAKOULU),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_MUU_KUIN_SAANNELTY),
      PalveluJaOikeus("KOSKI", Rooli.LUOTTAMUKSELLINEN_KELA_LAAJA)
    )))
  )

  val perusopetusViranomainen = KoskiMockUser(
    "Perusopetus",
    "Pertti",
    "1.2.246.562.24.99999999222",
    Seq(OrganisaatioJaKäyttöoikeudet(MockOrganisaatiot.kela, List(PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_PERUSOPETUS))))
  )

  val toinenAsteViranomainen = KoskiMockUser(
    "Toinenaste",
    "Teuvo",
    "1.2.246.562.24.99999999333",
    Seq(
      OrganisaatioJaKäyttöoikeudet(Opetushallitus.organisaatioOid, List(PalveluJaOikeus("OPPIJANUMEROREKISTERI", "REKISTERINPITAJA"))),
      OrganisaatioJaKäyttöoikeudet(MockOrganisaatiot.kela, List(PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_TOINEN_ASTE)))
    )
  )

  val korkeakouluViranomainen = KoskiMockUser(
    "Korkeakoulu",
    "Kaisa",
    "1.2.246.562.24.99999999444",
    Seq(OrganisaatioJaKäyttöoikeudet(MockOrganisaatiot.kela, List(PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_KORKEAKOULU))))
  )

  val jyväskylänNormaalikoulunPalvelukäyttäjä = KoskiMockUser(
    "jyväs-palvelu",
    "jyväs-palvelu",
    "1.2.246.562.24.99999999777",
    Seq(oppilaitosPalvelukäyttäjä(MockOrganisaatiot.jyväskylänNormaalikoulu))
  )

  val jyväskylänYliopistonVastuukäyttäjä = KoskiMockUser(
    "jyväs-vastuu",
    "jyväs-vastuu",
    "1.2.246.562.24.99999997777",
    Seq(vastuukäyttäjä(MockOrganisaatiot.jyväskylänYliopisto)),
    "fi",
    List("Vastuukayttajat")
  )

  val luovutuspalveluKäyttäjä = KoskiMockUser(
    "Luovutus",
    "Lasse",
    "1.2.246.562.24.99999988888",
    Seq(OrganisaatioJaKäyttöoikeudet(MockOrganisaatiot.tilastokeskus, List(
      PalveluJaOikeus("KOSKI", Rooli.TIEDONSIIRTO_LUOVUTUSPALVELU),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_PERUSOPETUS),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_TOINEN_ASTE),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_KORKEAKOULU),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_MUU_KUIN_SAANNELTY),
    )))
  )

  val luovutuspalveluKäyttäjäArkaluontoinen = KoskiMockUser(
    "Arkaluontoinen",
    "Antti",
    "1.2.246.562.24.88888877777",
    Seq(OrganisaatioJaKäyttöoikeudet(MockOrganisaatiot.tilastokeskus, List(
      PalveluJaOikeus("KOSKI", Rooli.TIEDONSIIRTO_LUOVUTUSPALVELU),
      PalveluJaOikeus("KOSKI", Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_PERUSOPETUS),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_TOINEN_ASTE),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_KORKEAKOULU),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_MUU_KUIN_SAANNELTY),
    )))
  )

  val suomiFiKäyttäjä = luovutuspalveluKäyttäjä.copy(
    firstname = "Suomi",
    lastname = "Fi",
    oid = "1.2.246.562.24.99999988889"
  )

  val tilastokeskusKäyttäjä = KoskiMockUser(
    "Tilastokeskus",
    "Teppo",
    "1.2.246.562.24.78787878787",
    Seq(OrganisaatioJaKäyttöoikeudet(MockOrganisaatiot.tilastokeskus, List(
      PalveluJaOikeus("KOSKI", Rooli.TILASTOKESKUS),
      PalveluJaOikeus("KOSKI", Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_PERUSOPETUS),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_TOINEN_ASTE),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_KORKEAKOULU),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_MUU_KUIN_SAANNELTY),
    )))
  )

  val valviraKäyttäjä = KoskiMockUser(
    "Valvira",
    "Ville",
    "1.2.246.562.24.42042042040",
    Seq(OrganisaatioJaKäyttöoikeudet(MockOrganisaatiot.valvira, List(
      PalveluJaOikeus("KOSKI", Rooli.VALVIRA),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_TOINEN_ASTE)
    )))
  )

  val esiopetusTallentaja = KoskiMockUser(
    "esiopetus-tallentaja",
    "esiopetus-tallentaja",
    "1.2.246.562.24.42042042041",
    Seq(OrganisaatioJaKäyttöoikeudet(helsinginKaupunki, List(
      PalveluJaOikeus("KOSKI", Rooli.READ_UPDATE_ESIOPETUS),
      PalveluJaOikeus("KOSKI", Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT)
    )))
  )

  val perusopetusTallentaja = KoskiMockUser(
    "esiopetus-tallentaja",
    "esiopetus-tallentaja",
    "1.2.246.562.24.42042042042",
    Seq(OrganisaatioJaKäyttöoikeudet(jyväskylänNormaalikoulu, List(
      PalveluJaOikeus("KOSKI", Rooli.PERUSOPETUS),
      PalveluJaOikeus("KOSKI", Rooli.READ_UPDATE),
      PalveluJaOikeus("KOSKI", Rooli.LUOTTAMUKSELLINEN_KAIKKI_TIEDOT)
    )))
  )

  val oppivelvollisuutietoRajapinta = KoskiMockUser(
    "oppivelvollisuustieto-rajapinnan-kutsuja",
    "oppivelvollisuustieto-rajapinnan-kutsuja",
    "1.2.246.562.24.42042042444",
    Seq(OrganisaatioJaKäyttöoikeudet(Opetushallitus.organisaatioOid, List(PalveluJaOikeus("KOSKI", Rooli.OPPIVELVOLLISUUSTIETO_RAJAPINTA))))
  )

  val ytlKäyttäjä = KoskiMockUser(
    "YTL-virkailija",
    "ylermi",
    "1.2.246.562.24.42042042058",
    Seq(OrganisaatioJaKäyttöoikeudet(MockOrganisaatiot.ytl, List(
      PalveluJaOikeus("KOSKI", Rooli.YTL),
      PalveluJaOikeus("KOSKI", Rooli.GLOBAALI_LUKU_TOINEN_ASTE)
    )))
  )

  val xssHyökkääjä = KoskiMockUser(
    "Paha Hakkeri</script><script>alert(1);",
    "xss-hakkeri",
    "1.2.246.562.24.42042046666",
    (lehtikuusentienToimipiste :: oppilaitokset).map(oppilaitosTallentaja)
  )

  val muuKuinSäänneltyKoulutusYritys = KoskiMockUser(
    "Jatkuva Koulutus Oy",
    "muks",
    "1.2.246.562.10.53455746569",
    List(MuuKuinSäänneltyKoulutusToimija.oppilaitos).map(oppilaitosTallentaja)
  )

  val users = List(
    kalle,
    pärre,
    omniaPalvelukäyttäjä,
    omniaKatselija,
    omniaTallentaja,
    omniaPääkäyttäjä,
    paakayttaja,
    paakayttajaMitatoidytOpiskeluoikeudet,
    paakayttajaMitatoidytJaPoistetutOpiskeluoikeudet,
    viranomainen,
    helsinginKaupunkiPalvelukäyttäjä,
    helsinginKaupunkiEsiopetus,
    stadinAmmattiopistoPääkäyttäjä,
    stadinAmmattiopistoTallentaja,
    stadinAmmattiopistoKatselija,
    jyväskylänKatselijaEsiopetus,
    jyväskylänKatselijaEiLuottamuksellinen,
    kahdenOrganisaatioPalvelukäyttäjä,
    omattiedot,
    stadinVastuukäyttäjä,
    stadinPääkäyttäjä,
    tallentajaEiLuottamuksellinen,
    helsinkiTallentaja,
    tornioTallentaja,
    helsinkiSekäTornioTallentaja,
    pyhtäänTallentaja,
    jyväskyläTallentaja,
    touholaTallentaja,
    majakkaTallentaja,
    eiOikkia,
    jyväskylänNormaalikoulunPalvelukäyttäjä,
    jyväskylänYliopistonVastuukäyttäjä,
    evira,
    kelaSuppeatOikeudet,
    kelaLaajatOikeudet,
    perusopetusViranomainen,
    toinenAsteViranomainen,
    korkeakouluViranomainen,
    luovutuspalveluKäyttäjä,
    luovutuspalveluKäyttäjäArkaluontoinen,
    suomiFiKäyttäjä,
    tilastokeskusKäyttäjä,
    valviraKäyttäjä,
    esiopetusTallentaja,
    oppivelvollisuutietoRajapinta,
    varsinaisSuomiPalvelukäyttäjä,
    ytlKäyttäjä,
    xssHyökkääjä,
    muuKuinSäänneltyKoulutusYritys,
  )
}

trait MockUser extends UserWithPassword {
  def lastname: String
  def firstname: String
  def oid: String
  def käyttöoikeudet: Set[Käyttöoikeus]
  def lang: String
  def käyttöoikeusRyhmät: List[String]

  def ldapUser: DirectoryUser

  def username = ldapUser.etunimet
  def password = username
}

case class KoskiMockUser(lastname: String, firstname: String, oid: String, käyttöoikeudetRaw: Seq[OrganisaatioJaKäyttöoikeudet], lang: String = "fi", käyttöoikeusRyhmät: List[String] = Nil) extends MockUser {

  val käyttöoikeudet = DirectoryClient.resolveKäyttöoikeudet(HenkilönKäyttöoikeudet(oid, käyttöoikeudetRaw.toList))._2.toSet

  lazy val ldapUser = DirectoryUser(oid, käyttöoikeudet.toList, firstname, lastname, Some(lang))
  def toKoskiSpecificSession(käyttöoikeudet: KäyttöoikeusRepository): KoskiSpecificSession = {
    val authUser: AuthenticationUser = fromDirectoryUser(username, ldapUser)
    new KoskiSpecificSession(authUser, "fi", InetAddress.getByName("192.168.0.10"), "", käyttöoikeudet.käyttäjänKäyttöoikeudet(authUser))
  }
}
