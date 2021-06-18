package fi.oph.koski.valpas.opiskeluoikeusfixture

import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.valpas.valpasuser.ValpasMockUsers

object ValpasMockOppijat {
  private val valpasOppijat = new MockOppijat

  val oppivelvollinenYsiluokkaKeskenKeväällä2021 = valpasOppijat.oppija("Oppivelvollinen-ysiluokka-kesken-keväällä-2021", "Valpas", "221105A3023")
  val eiOppivelvollinenSyntynytEnnen2004 = valpasOppijat.oppija("Ei-oppivelvollinen-syntynyt-ennen-2004", "Valpas", "210303A707J")
  val päällekkäisiäOpiskeluoikeuksia = valpasOppijat.oppija("Päällekkäisiä", "Oppivelvollisuuksia", "060605A083N")
  val lukioOpiskelija = valpasOppijat.oppija("Lukio-opiskelija", "Valpas", "070504A717P")
  val kasiluokkaKeskenKeväällä2021 = valpasOppijat.oppija("Kasiluokka-kesken-keväällä-2021", "Valpas", "191106A1384")
  val kotiopetusMeneilläänOppija = valpasOppijat.oppija("Kotiopetus-meneillä", "Valpas", "210905A2151")
  val kotiopetusMenneisyydessäOppija = valpasOppijat.oppija("Kotiopetus-menneisyydessä", "Valpas", "060205A8805")
  val eronnutOppija = valpasOppijat.oppija("Eroaja-aiemmin", "Valpas", "240905A0078")
  val luokalleJäänytYsiluokkalainen = valpasOppijat.oppija("LuokallejäänytYsiluokkalainen", "Valpas", "020805A5625")
  val luokallejäänytYsiluokkalainenJollaUusiYsiluokka = valpasOppijat.oppija("LuokallejäänytYsiluokkalainenJatkaa", "Valpas", "060205A7222")
  val valmistunutYsiluokkalainen = valpasOppijat.oppija("Ysiluokka-valmis-keväällä-2021", "Valpas", "190605A006K")
  val luokalleJäänytYsiluokkalainenVaihtanutKoulua = valpasOppijat.oppija("LuokallejäänytYsiluokkalainenKouluvaihto", "Valpas", "050605A7684")
  val luokalleJäänytYsiluokkalainenVaihtanutKouluaMuualta = valpasOppijat.oppija("LuokallejäänytYsiluokkalainenKouluvaihtoMuualta", "Valpas", "021105A624K")
  val kasiinAstiToisessaKoulussaOllut = valpasOppijat.oppija("KasiinAstiToisessaKoulussaOllut", "Valpas", "170805A613F", äidinkieli = Some("sv"))
  val lukionAloittanut = valpasOppijat.oppija("LukionAloittanut", "Valpas", "290405A871A", äidinkieli = Some("en"))
  val lukionLokakuussaAloittanut = valpasOppijat.oppija("LukionLokakuussaAloittanut", "Valpas", "180405A819J")
  val oppivelvollinenMonellaOppijaOidillaMaster = valpasOppijat.oppija("Kahdella-oppija-oidilla", "Valpas", "150205A490C")
  val oppivelvollinenMonellaOppijaOidillaToinen = valpasOppijat.duplicate(oppivelvollinenMonellaOppijaOidillaMaster)
  val oppivelvollinenMonellaOppijaOidillaKolmas = valpasOppijat.duplicate(oppivelvollinenMonellaOppijaOidillaMaster)
  val aapajoenPeruskoulustaValmistunut = valpasOppijat.oppija("Aaapajoen-peruskoulusta-valmistunut", "Valpas", "160205A301X")
  val ennenLainRajapäivääPeruskoulustaValmistunut = valpasOppijat.oppija("Ennen-lain-rajapäivää-peruskoulusta-valmistunut", "Valpas", "080905A0798")
  val yli2kkAiemminPeruskoulustaValmistunut = valpasOppijat.oppija("Yli-2-kk-aiemmin-peruskoulusta-valmistunut", "Valpas", "010204A079U")
  val useampiYsiluokkaSamassaKoulussa = valpasOppijat.oppija("UseampiYsiluokkaSamassaKoulussa", "Valpas", "250805A605C")
  val turvakieltoOppija = valpasOppijat.oppija("Turvakielto", "Valpas", "290904A4030", valpasOppijat.generateId(), None, true)
  val eronnutOppijaTarkastelupäivänä = valpasOppijat.oppija("Eroaja-samana-päivänä", "Valpas", "270805A084V")
  val eronnutOppijaTarkastelupäivänJälkeen = valpasOppijat.oppija("Eroaja-myöhemmin", "Valpas", "290905A840B")
  val oppivelvollinenAloittanutJaEronnutTarkastelupäivänJälkeen = valpasOppijat.oppija("Aloittanut-ja-eronnut-myöhemmin", "Valpas", "270405A450E")
  val hakukohteidenHakuEpäonnistuu = valpasOppijat.oppija("Epäonninen", "Valpas", "301005A336J")
  val kulosaarenYsiluokkalainen = valpasOppijat.oppija("Kulosaarelainen", "Oppija", "190105A788S")
  val kulosaarenYsiluokkalainenJaJyväskylänLukiolainen = valpasOppijat.oppija("Jkl-Lukio-Kulosaarelainen", "Valpas", "010104A187H")
  val kulosaarenYsiluokkalainenJaJyväskylänNivelvaiheinen = valpasOppijat.oppija("Jkl-Nivel-Kulosaarelainen", "Valpas", "010104A787V")
  val kulosaarenYsiluokkalainenJaJyväskylänEsikoululainen = valpasOppijat.oppija("Jkl-Esikoulu-Kulosaarelainen", "Valpas", "220304A4173")
  val lukionAineopinnotAloittanut = valpasOppijat.oppija("LukionAineopinnotAloittanut", "Valpas", "040305A559A")
  val valmistunutYsiluokkalainenJollaIlmoitus = valpasOppijat.oppija("Ysiluokka-valmis-keväällä-2021-ilmo", "Valpas", "260805A3571")
  val kasiinAstiToisessaKoulussaOllutJollaIlmoitus = valpasOppijat.oppija("KasiinAstiToisessaKoulussaOllut-ilmo", "Valpas", "020505A164W", äidinkieli = Some("sv"))
  val kahdenKoulunYsiluokkalainenJollaIlmoitus = valpasOppijat.oppija("KahdenKoulunYsi-ilmo", "Valpas", "211104A0546")
  val oppivelvollinenMonellaOppijaOidillaJollaIlmoitusMaster = valpasOppijat.oppija("Kahdella-oppija-oidilla-ilmo", "Valpas", "040605A0123")
  val oppivelvollinenMonellaOppijaOidillaJollaIlmoitusToinen = valpasOppijat.duplicate(oppivelvollinenMonellaOppijaOidillaJollaIlmoitusMaster)
  val oppivelvollinenMonellaOppijaOidillaJollaIlmoitusKolmas = valpasOppijat.duplicate(oppivelvollinenMonellaOppijaOidillaJollaIlmoitusMaster)
  val lukionAloittanutJollaVanhaIlmoitus = valpasOppijat.oppija("LukionAloittanut-ilmo", "Valpas", "110405A435M")
  val lukionAloittanutJaLopettanutJollaIlmoituksia = valpasOppijat.oppija("LukionAloittanutJaLopettanut-ilmo", "Valpas", "050405A249S")
  val ammattikoulustaValmistunutOpiskelija = valpasOppijat.oppija("Amis-valmistunut-opiskelija", "Valpas", "180304A082P")
  val eronnutMaaliskuussa17VuottaTäyttäväKasiluokkalainen = valpasOppijat.oppija("Eronnut-maaliskuussa-17-vuotta-täyttävä-8-luokkalainen", "Valpas", "280904A2768")
  val eronnutKeväänValmistumisJaksolla17VuottaTäyttäväKasiluokkalainen = valpasOppijat.oppija("Eronnut-kevään-valmistumisjaksolla-17-vuotta-täyttävä-8-luokkalainen", "Valpas", "121004A189X")
  val eronnutElokuussa17VuottaTäyttäväKasiluokkalainen = valpasOppijat.oppija("Eronnut-elokuussa-17-vuotta-täyttävä-8-luokkalainen", "Valpas", "110904A007L")
  val valmistunutYsiluokkalainenVsop = valpasOppijat.oppija("Ysiluokka-valmis-keväällä-2021-vsop", "Valpas", "190705A575R")
  val ysiluokkaKeskenVsop = valpasOppijat.oppija("Oppivelvollinen-ysiluokka-kesken-vsop", "Valpas", "240305A7103")
  val valmistunutKasiluokkalainen = valpasOppijat.oppija("Valmistunut-kasiluokkalainen-alle-17-vuotias", "Valpas", "090605A768P")
  val oppivelvollinenMonellaOppijaOidillaJollaIlmoitusMaster2 = valpasOppijat.oppija("Kahdella-oppija-oidilla-ilmo-2", "Valpas", "030605A476D")
  val oppivelvollinenMonellaOppijaOidillaJollaIlmoitusToinen2 = valpasOppijat.duplicate(oppivelvollinenMonellaOppijaOidillaJollaIlmoitusMaster2)
  val ilmoituksenLisätiedotPoistettu = valpasOppijat.oppija("Ilmoituksen-lisätiedot–poistettu", "Valpas", "190505A3019")
  val lukiostaValmistunutOpiskelija = valpasOppijat.oppija("Lukio-opiskelija-valmistunut", "Valpas", "271105A835H")
  val ammattikouluOpiskelija = valpasOppijat.oppija("Amis-opiskelija", "Valpas", "231005A2431")
  val kaksoistutkinnostaValmistunutOpiskelija = valpasOppijat.oppija("Kaksois-tutkinnosta-valmistunut", "Valpas", "260905A7672")
  val nivelvaiheestaValmistunutOpiskelija = valpasOppijat.oppija("Nivelvaiheesta-valmistunut", "Valpas", "201005A022Y")

  // Kutsumanimi ja yhteystiedot haetaan oppijanumerorekisteristä Valpas-käyttäjälle, tallennetaan siksi käyttäjä myös "oppijana" mockeihin
  val käyttäjäValpasJklNormaalikoulu = valpasOppijat.oppija(
    hetu = "300850-4762",
    oid = ValpasMockUsers.valpasJklNormaalikoulu.oid,
    suku = ValpasMockUsers.valpasJklNormaalikoulu.lastname,
    etu = ValpasMockUsers.valpasJklNormaalikoulu.firstname,
    kutsumanimi = Some("Kutsu")
  )

  def defaultOppijat = valpasOppijat.getOppijat
}
