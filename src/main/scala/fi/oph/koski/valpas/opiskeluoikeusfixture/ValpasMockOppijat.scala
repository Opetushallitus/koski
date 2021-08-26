package fi.oph.koski.valpas.opiskeluoikeusfixture

import fi.oph.koski.henkilo.MockOppijat
import fi.oph.koski.valpas.valpasuser.ValpasMockUsers

import java.time.LocalDate

object ValpasMockOppijat {
  private val valpasOppijat = new MockOppijat

  val oppivelvollinenYsiluokkaKeskenKeväällä2021 = valpasOppijat.oppijaSyntymäaikaHetusta("Oppivelvollinen-ysiluokka-kesken-keväällä-2021", "Valpas", "221105A3023")
  val eiOppivelvollinenSyntynytEnnen2004 = valpasOppijat.oppijaSyntymäaikaHetusta("Ei-oppivelvollinen-syntynyt-ennen-2004", "Valpas", "210303A707J")
  val päällekkäisiäOpiskeluoikeuksia = valpasOppijat.oppijaSyntymäaikaHetusta("Päällekkäisiä", "Oppivelvollisuuksia", "060605A083N")
  val lukioOpiskelija = valpasOppijat.oppijaSyntymäaikaHetusta("Lukio-opiskelija", "Valpas", "070504A717P")
  val kasiluokkaKeskenKeväällä2021 = valpasOppijat.oppijaSyntymäaikaHetusta("Kasiluokka-kesken-keväällä-2021", "Valpas", "191106A1384")
  val kotiopetusMeneilläänOppija = valpasOppijat.oppijaSyntymäaikaHetusta("Kotiopetus-meneillä", "Valpas", "210905A2151")
  val kotiopetusMenneisyydessäOppija = valpasOppijat.oppijaSyntymäaikaHetusta("Kotiopetus-menneisyydessä", "Valpas", "060205A8805")
  val eronnutOppija = valpasOppijat.oppijaSyntymäaikaHetusta("Eroaja-aiemmin", "Valpas", "240905A0078")
  val luokalleJäänytYsiluokkalainen = valpasOppijat.oppijaSyntymäaikaHetusta("LuokallejäänytYsiluokkalainen", "Valpas", "020805A5625")
  val luokallejäänytYsiluokkalainenJollaUusiYsiluokka = valpasOppijat.oppijaSyntymäaikaHetusta("LuokallejäänytYsiluokkalainenJatkaa", "Valpas", "060205A7222")
  val valmistunutYsiluokkalainen = valpasOppijat.oppijaSyntymäaikaHetusta("Ysiluokka-valmis-keväällä-2021", "Valpas", "190605A006K")
  val luokalleJäänytYsiluokkalainenVaihtanutKoulua = valpasOppijat.oppijaSyntymäaikaHetusta("LuokallejäänytYsiluokkalainenKouluvaihto", "Valpas", "050605A7684")
  val luokalleJäänytYsiluokkalainenVaihtanutKouluaMuualta = valpasOppijat.oppijaSyntymäaikaHetusta("LuokallejäänytYsiluokkalainenKouluvaihtoMuualta", "Valpas", "021105A624K")
  val kasiinAstiToisessaKoulussaOllut = valpasOppijat.oppijaSyntymäaikaHetusta("KasiinAstiToisessaKoulussaOllut", "Valpas", "170805A613F", äidinkieli = Some("sv"))
  val lukionAloittanut = valpasOppijat.oppijaSyntymäaikaHetusta("LukionAloittanut", "Valpas", "290405A871A", äidinkieli = Some("en"))
  val lukionLokakuussaAloittanut = valpasOppijat.oppijaSyntymäaikaHetusta("LukionLokakuussaAloittanut", "Valpas", "180405A819J")
  val oppivelvollinenMonellaOppijaOidillaMaster = valpasOppijat.oppijaSyntymäaikaHetusta("Kahdella-oppija-oidilla", "Valpas", "150205A490C")
  val oppivelvollinenMonellaOppijaOidillaToinen = valpasOppijat.duplicate(oppivelvollinenMonellaOppijaOidillaMaster)
  val oppivelvollinenMonellaOppijaOidillaKolmas = valpasOppijat.duplicate(oppivelvollinenMonellaOppijaOidillaMaster)
  val aapajoenPeruskoulustaValmistunut = valpasOppijat.oppijaSyntymäaikaHetusta("Aaapajoen-peruskoulusta-valmistunut", "Valpas", "160205A301X")
  val ennenLainRajapäivääPeruskoulustaValmistunut = valpasOppijat.oppijaSyntymäaikaHetusta("Ennen-lain-rajapäivää-peruskoulusta-valmistunut", "Valpas", "080905A0798")
  val yli2kkAiemminPeruskoulustaValmistunut = valpasOppijat.oppijaSyntymäaikaHetusta("Yli-2-kk-aiemmin-peruskoulusta-valmistunut", "Valpas", "010204A079U")
  val useampiYsiluokkaSamassaKoulussa = valpasOppijat.oppijaSyntymäaikaHetusta("UseampiYsiluokkaSamassaKoulussa", "Valpas", "250805A605C")
  val turvakieltoOppija = valpasOppijat.oppijaSyntymäaikaHetusta("Turvakielto", "Valpas", "290904A4030", valpasOppijat.generateId(), None, true)
  val eronnutOppijaTarkastelupäivänä = valpasOppijat.oppijaSyntymäaikaHetusta("Eroaja-samana-päivänä", "Valpas", "270805A084V")
  val eronnutOppijaTarkastelupäivänJälkeen = valpasOppijat.oppijaSyntymäaikaHetusta("Eroaja-myöhemmin", "Valpas", "290905A840B")
  val oppivelvollinenAloittanutJaEronnutTarkastelupäivänJälkeen = valpasOppijat.oppijaSyntymäaikaHetusta("Aloittanut-ja-eronnut-myöhemmin", "Valpas", "270405A450E")
  val hakukohteidenHakuEpäonnistuu = valpasOppijat.oppijaSyntymäaikaHetusta("Epäonninen", "Valpas", "301005A336J")
  val kulosaarenYsiluokkalainen = valpasOppijat.oppijaSyntymäaikaHetusta("Kulosaarelainen", "Oppija", "190105A788S")
  val kulosaarenYsiluokkalainenJaJyväskylänLukiolainen = valpasOppijat.oppijaSyntymäaikaHetusta("Jkl-Lukio-Kulosaarelainen", "Valpas", "010104A187H")
  val kulosaarenYsiluokkalainenJaJyväskylänNivelvaiheinen = valpasOppijat.oppijaSyntymäaikaHetusta("Jkl-Nivel-Kulosaarelainen", "Valpas", "010104A787V")
  val kulosaarenYsiluokkalainenJaJyväskylänEsikoululainen = valpasOppijat.oppijaSyntymäaikaHetusta("Jkl-Esikoulu-Kulosaarelainen", "Valpas", "220304A4173")
  val lukionAineopinnotAloittanut = valpasOppijat.oppijaSyntymäaikaHetusta("LukionAineopinnotAloittanut", "Valpas", "040305A559A")
  val valmistunutYsiluokkalainenJollaIlmoitus = valpasOppijat.oppijaSyntymäaikaHetusta("Ysiluokka-valmis-keväällä-2021-ilmo", "Valpas", "260805A3571")
  val kasiinAstiToisessaKoulussaOllutJollaIlmoitus = valpasOppijat.oppijaSyntymäaikaHetusta("KasiinAstiToisessaKoulussaOllut-ilmo", "Valpas", "020505A164W", äidinkieli = Some("sv"))
  val kahdenKoulunYsiluokkalainenJollaIlmoitus = valpasOppijat.oppijaSyntymäaikaHetusta("KahdenKoulunYsi-ilmo", "Valpas", "211104A0546")
  val oppivelvollinenMonellaOppijaOidillaJollaIlmoitusMaster = valpasOppijat.oppijaSyntymäaikaHetusta("Kahdella-oppija-oidilla-ilmo", "Valpas", "040605A0123")
  val oppivelvollinenMonellaOppijaOidillaJollaIlmoitusToinen = valpasOppijat.duplicate(oppivelvollinenMonellaOppijaOidillaJollaIlmoitusMaster)
  val oppivelvollinenMonellaOppijaOidillaJollaIlmoitusKolmas = valpasOppijat.duplicate(oppivelvollinenMonellaOppijaOidillaJollaIlmoitusMaster)
  val lukionAloittanutJollaVanhaIlmoitus = valpasOppijat.oppijaSyntymäaikaHetusta("LukionAloittanut-ilmo", "Valpas", "110405A435M")
  val lukionAloittanutJaLopettanutJollaIlmoituksia = valpasOppijat.oppijaSyntymäaikaHetusta("LukionAloittanutJaLopettanut-ilmo", "Valpas", "050405A249S")
  val ammattikoulustaValmistunutOpiskelija = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-valmistunut-opiskelija", "Valpas", "180304A082P")
  val eronnutMaaliskuussa17VuottaTäyttäväKasiluokkalainen = valpasOppijat.oppijaSyntymäaikaHetusta("Eronnut-maaliskuussa-17-vuotta-täyttävä-8-luokkalainen", "Valpas", "280904A2768")
  val eronnutKeväänValmistumisJaksolla17VuottaTäyttäväKasiluokkalainen = valpasOppijat.oppijaSyntymäaikaHetusta("Eronnut-kevään-valmistumisjaksolla-17-vuotta-täyttävä-8-luokkalainen", "Valpas", "121004A189X")
  val eronnutElokuussa17VuottaTäyttäväKasiluokkalainen = valpasOppijat.oppijaSyntymäaikaHetusta("Eronnut-elokuussa-17-vuotta-täyttävä-8-luokkalainen", "Valpas", "110904A007L")
  val valmistunutYsiluokkalainenVsop = valpasOppijat.oppijaSyntymäaikaHetusta("Ysiluokka-valmis-keväällä-2021-vsop", "Valpas", "190705A575R")
  val ysiluokkaKeskenVsop = valpasOppijat.oppijaSyntymäaikaHetusta("Oppivelvollinen-ysiluokka-kesken-vsop", "Valpas", "240305A7103")
  val valmistunutKasiluokkalainen = valpasOppijat.oppijaSyntymäaikaHetusta("Valmistunut-kasiluokkalainen-alle-17-vuotias", "Valpas", "090605A768P")
  val oppivelvollinenMonellaOppijaOidillaJollaIlmoitusMaster2 = valpasOppijat.oppijaSyntymäaikaHetusta("Kahdella-oppija-oidilla-ilmo-2", "Valpas", "030605A476D")
  val oppivelvollinenMonellaOppijaOidillaJollaIlmoitusToinen2 = valpasOppijat.duplicate(oppivelvollinenMonellaOppijaOidillaJollaIlmoitusMaster2)
  val ilmoituksenLisätiedotPoistettu = valpasOppijat.oppijaSyntymäaikaHetusta("Ilmoituksen-lisätiedot–poistettu", "Valpas", "190505A3019")
  val lukiostaValmistunutOpiskelija = valpasOppijat.oppijaSyntymäaikaHetusta("Lukio-opiskelija-valmistunut", "Valpas", "271105A835H")
  val ammattikouluOpiskelija = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-opiskelija", "Valpas", "231005A2431")
  val kaksoistutkinnostaValmistunutOpiskelija = valpasOppijat.oppijaSyntymäaikaHetusta("Kaksois-tutkinnosta-valmistunut", "Valpas", "260905A7672")
  val nivelvaiheestaValmistunutOpiskelija = valpasOppijat.oppijaSyntymäaikaHetusta("Nivelvaiheesta-valmistunut", "Valpas", "201005A022Y")
  val oppivelvollisuusKeskeytetty = valpasOppijat.oppijaSyntymäaikaHetusta("Oppivelvollisuus-keskeytetty-määräajaksi", "Valpas", "181005A1560")
  val oppivelvollisuusKeskeytettyToistaiseksi = valpasOppijat.oppijaSyntymäaikaHetusta("Oppivelvollisuus-keskeytetty-toistaiseksi", "Valpas", "150905A1823")
  val eiOppivelvollisuudenSuorittamiseenKelpaaviaOpiskeluoikeuksia = valpasOppijat.oppijaSyntymäaikaHetusta("Ei-oppivelvollisuuden-suorittamiseen-kelpaavia-opiskeluoikeuksia", "Valpas", "061005A671V")
  val hetuton = valpasOppijat.oppija("Hetuton", "Valpas", "", syntymäaika = Some(LocalDate.of(2005, 1, 1)))
  val oppivelvollinenJollaHetu = valpasOppijat.oppijaSyntymäaikaHetusta("Oppivelvollinen-hetullinen", "Valpas", "030105A7507")
  val oppivelvollinenJollaHetuHetutonSlave = valpasOppijat.duplicate(oppivelvollinenJollaHetu.copy(hetu = None))
  val ammattikouluOpiskelijaValma = valpasOppijat.oppijaSyntymäaikaHetusta("Valma-opiskelija", "Valpas", "190105A839D")
  val ammattikouluOpiskelijaTelma = valpasOppijat.oppijaSyntymäaikaHetusta("Telma-opiskelija", "Valpas", "020805A7784")
  val amisEronnutEiUuttaOpiskeluoikeutta = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-eronnut", "Valpas", "010805A852V")
  val amisEronnutUusiOpiskeluoikeusTulevaisuudessaKeskeyttänyt = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-eronnut-uusi-oo-tulevaisuudessa-keskeyttänyt", "Valpas", "240905A539D")
  val amisEronnutUusiOpiskeluoikeusVoimassa = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-eronnut-uusi-oo-voimassa", "Valpas", "241005A214R")
  val amisEronnutUusiOpiskeluoikeusPeruskoulussaKeskeyttänytTulevaisuudessa = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-eronnut-uusi-peruskoulussa-keskeyttänyt-tulevaisuudessa", "Valpas", "100205A291R")
  val amisEronnutUusiOpiskeluoikeusNivelvaiheessa = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-eronnut-uusi-nivelvaiheessa", "Valpas", "180605A898P")
  val amisEronnutUusiOpiskeluoikeusNivelvaiheessa2 = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-eronnut-uusi-nivelvaiheessa-valmassa", "Valpas", "040804A0600")
  val amisEronnutMontaUuttaOpiskeluoikeutta = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-eronnut-monta-uutta-oota", "Valpas", "241005A449A")
  val amisEronnutUusiKelpaamatonOpiskeluoikeusNivelvaiheessa = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-eronnut-nivelvaihe-ei-kelpaa", "Valpas", "101105A1703")
  val amisEronnutUusiKelpaamatonOpiskeluoikeusNivelvaiheessa2 = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-eronnut-nivelvaihe-vstssa-ei-kelpaa", "Valpas", "090604A305H")
  val ammattikouluOpiskelijaMontaOpiskeluoikeutta = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-monta-oota", "Valpas", "280105A505E")
  val amisAmmatillinenJaNäyttötutkintoonValmistava = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-useita-pts", "Valpas", "280505A418V")
  val opiskeluoikeudetonOppivelvollisuusikäinenOppija = valpasOppijat.oppijaSyntymäaikaHetusta("Ei-opiskeluoikeuksia-oppivelvollisuusikäinen", "Valpas", "110405A6951")
  val opiskeluoikeudetonEiOppivelvollisuusikäinenOppija = valpasOppijat.oppijaSyntymäaikaHetusta("Ei-opiskeluoikeuksia-vanha", "Valpas", "070302A402D")
  val lukioVäliaikaisestiKeskeytynyt = valpasOppijat.oppijaSyntymäaikaHetusta("Lukio-väliaikaisesti-keskeytynyt", "Valpas", "300504A157F")
  val amisLomalla = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-lomalla", "Valpas", "030905A194R")

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
