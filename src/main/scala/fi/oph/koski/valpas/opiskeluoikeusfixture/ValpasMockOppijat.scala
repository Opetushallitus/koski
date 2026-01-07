package fi.oph.koski.valpas.opiskeluoikeusfixture

import fi.oph.koski.henkilo.{MockOppijat, OppijanKuntahistoria, OppijanumerorekisteriKotikuntahistoriaRow}
import fi.oph.koski.koodisto.Kunta
import fi.oph.koski.valpas.valpasuser.ValpasMockUsers

import java.time.LocalDate
import java.time.LocalDate.{of => date}

object ValpasMockOppijat {
  private val valpasOppijat = new MockOppijat

  val oppivelvollinenYsiluokkaKeskenKeväällä2021 = valpasOppijat.oppijaSyntymäaikaHetusta("Oppivelvollinen-ysiluokka-kesken-keväällä-2021", "Valpas", "221105A3023", kotikunta = Some(Kunta.helsinki))
  val eiOppivelvollinenSyntynytEnnen2004 = valpasOppijat.oppijaSyntymäaikaHetusta("Ei-oppivelvollinen-syntynyt-ennen-2004", "Valpas", "210303A707J", kotikunta = Some(Kunta.helsinki))
  val päällekkäisiäOpiskeluoikeuksia = valpasOppijat.oppijaSyntymäaikaHetusta("Päällekkäisiä", "Oppivelvollisuuksia", "060605A083N", kotikunta = Some(Kunta.helsinki))
  val lukioOpiskelija = valpasOppijat.oppijaSyntymäaikaHetusta("Lukio-opiskelija", "Valpas", "070504A717P", kotikunta = Some(Kunta.helsinki))
  val kasiluokkaKeskenKeväällä2021 = valpasOppijat.oppijaSyntymäaikaHetusta("Kasiluokka-kesken-keväällä-2021", "Valpas", "191106A1384", kotikunta = Some(Kunta.helsinki))
  val kotiopetusMeneilläänOppija = valpasOppijat.oppijaSyntymäaikaHetusta("Kotiopetus-meneillä", "Valpas", "210905A2151", kotikunta = Some(Kunta.helsinki))
  val kotiopetusMenneisyydessäOppija = valpasOppijat.oppijaSyntymäaikaHetusta("Kotiopetus-menneisyydessä", "Valpas", "060205A8805", kotikunta = Some(Kunta.helsinki))
  val eronnutOppija = valpasOppijat.oppijaSyntymäaikaHetusta("Eroaja-aiemmin", "Valpas", "240905A0078", kotikunta = Some(Kunta.pyhtää))
  val luokalleJäänytYsiluokkalainen = valpasOppijat.oppijaSyntymäaikaHetusta("LuokallejäänytYsiluokkalainen", "Valpas", "020805A5625", kotikunta = Some(Kunta.helsinki))
  val luokallejäänytYsiluokkalainenJollaUusiYsiluokka = valpasOppijat.oppijaSyntymäaikaHetusta("LuokallejäänytYsiluokkalainenJatkaa", "Valpas", "060205A7222", kotikunta = Some(Kunta.helsinki))
  val valmistunutYsiluokkalainen = valpasOppijat.oppijaSyntymäaikaHetusta("Ysiluokka-valmis-keväällä-2021", "Valpas", "190605A006K", kotikunta = Some(Kunta.helsinki))
  val luokalleJäänytYsiluokkalainenVaihtanutKoulua = valpasOppijat.oppijaSyntymäaikaHetusta("LuokallejäänytYsiluokkalainenKouluvaihto", "Valpas", "050605A7684", kotikunta = Some(Kunta.helsinki))
  val luokalleJäänytYsiluokkalainenVaihtanutKouluaMuualta = valpasOppijat.oppijaSyntymäaikaHetusta("LuokallejäänytYsiluokkalainenKouluvaihtoMuualta", "Valpas", "021105A624K", kotikunta = Some(Kunta.helsinki))
  val kasiinAstiToisessaKoulussaOllut = valpasOppijat.oppijaSyntymäaikaHetusta("KasiinAstiToisessaKoulussaOllut", "Valpas", "170805A613F", äidinkieli = Some("sv"), kotikunta = Some(Kunta.helsinki))
  val lukionAloittanut = valpasOppijat.oppijaSyntymäaikaHetusta("LukionAloittanut", "Valpas", "290405A871A", äidinkieli = Some("en"), kotikunta = Some(Kunta.helsinki))
  val lukionLokakuussaAloittanut = valpasOppijat.oppijaSyntymäaikaHetusta("LukionLokakuussaAloittanut", "Valpas", "180405A819J", kotikunta = Some(Kunta.helsinki))
  val oppivelvollinenMonellaOppijaOidillaMaster = valpasOppijat.oppijaSyntymäaikaHetusta("Kahdella-oppija-oidilla", "Valpas", "150205A490C", kotikunta = Some(Kunta.helsinki))
  val oppivelvollinenMonellaOppijaOidillaToinen = valpasOppijat.duplicate(oppivelvollinenMonellaOppijaOidillaMaster)
  val oppivelvollinenMonellaOppijaOidillaKolmas = valpasOppijat.duplicate(oppivelvollinenMonellaOppijaOidillaMaster)
  val aapajoenPeruskoulustaValmistunut = valpasOppijat.oppijaSyntymäaikaHetusta("Aaapajoen-peruskoulusta-valmistunut", "Valpas", "160205A301X", kotikunta = Some(Kunta.helsinki))
  val ennenLainRajapäivääPeruskoulustaValmistunut = valpasOppijat.oppijaSyntymäaikaHetusta("Ennen-lain-rajapäivää-peruskoulusta-valmistunut", "Valpas", "080905A0798", kotikunta = Some(Kunta.helsinki))
  val yli2kkAiemminPeruskoulustaValmistunut = valpasOppijat.oppijaSyntymäaikaHetusta("Yli-2-kk-aiemmin-peruskoulusta-valmistunut", "Valpas", "010204A079U", kotikunta = Some(Kunta.helsinki))
  val useampiYsiluokkaSamassaKoulussa = valpasOppijat.oppijaSyntymäaikaHetusta("UseampiYsiluokkaSamassaKoulussa", "Valpas", "250805A605C", kotikunta = Some(Kunta.helsinki))
  val turvakieltoOppija = valpasOppijat.oppijaSyntymäaikaHetusta("Turvakielto", "Valpas", "290904A4030", valpasOppijat.generateId(), None, turvakielto = true, kotikunta = Some(Kunta.eiTiedossa))
  val eronnutOppijaTarkastelupäivänä = valpasOppijat.oppijaSyntymäaikaHetusta("Eroaja-samana-päivänä", "Valpas", "270805A084V", kotikunta = Some(Kunta.helsinki))
  val eronnutOppijaTarkastelupäivänJälkeen = valpasOppijat.oppijaSyntymäaikaHetusta("Eroaja-myöhemmin", "Valpas", "290905A840B", kotikunta = Some(Kunta.helsinki))
  val oppivelvollinenAloittanutJaEronnutTarkastelupäivänJälkeen = valpasOppijat.oppijaSyntymäaikaHetusta("Aloittanut-ja-eronnut-myöhemmin", "Valpas", "270405A450E", kotikunta = Some(Kunta.helsinki))
  val hakukohteidenHakuEpäonnistuu = valpasOppijat.oppijaSyntymäaikaHetusta("Epäonninen", "Valpas", "301005A336J", kotikunta = Some(Kunta.helsinki))
  val kulosaarenYsiluokkalainen = valpasOppijat.oppijaSyntymäaikaHetusta("Kulosaarelainen", "Oppija", "190105A788S", kotikunta = Some(Kunta.helsinki))
  val kulosaarenYsiluokkalainenJaJyväskylänLukiolainen = valpasOppijat.oppijaSyntymäaikaHetusta("Jkl-Lukio-Kulosaarelainen", "Valpas", "010104A187H", kotikunta = Some(Kunta.helsinki))
  val kulosaarenYsiluokkalainenJaJyväskylänNivelvaiheinen = valpasOppijat.oppijaSyntymäaikaHetusta("Jkl-Nivel-Kulosaarelainen", "Valpas", "010104A787V", kotikunta = Some(Kunta.helsinki))
  val kulosaarenYsiluokkalainenJaJyväskylänEsikoululainen = valpasOppijat.oppijaSyntymäaikaHetusta("Jkl-Esikoulu-Kulosaarelainen", "Valpas", "220304A4173", kotikunta = Some(Kunta.helsinki))
  val lukionAineopinnotAloittanut = valpasOppijat.oppijaSyntymäaikaHetusta("LukionAineopinnotAloittanut", "Valpas", "040305A559A", kotikunta = Some(Kunta.helsinki))
  val valmistunutYsiluokkalainenJollaIlmoitus = valpasOppijat.oppijaSyntymäaikaHetusta("Ysiluokka-valmis-keväällä-2021-ilmo", "Valpas", "260805A3571", kotikunta = Some(Kunta.helsinki))
  val kasiinAstiToisessaKoulussaOllutJollaIlmoitus = valpasOppijat.oppijaSyntymäaikaHetusta("KasiinAstiToisessaKoulussaOllut-ilmo", "Valpas", "020505A164W", äidinkieli = Some("sv"), kotikunta = Some(Kunta.helsinki))
  val kahdenKoulunYsiluokkalainenJollaIlmoitus = valpasOppijat.oppijaSyntymäaikaHetusta("KahdenKoulunYsi-ilmo", "Valpas", "211104A0546", kotikunta = Some(Kunta.helsinki))
  val oppivelvollinenMonellaOppijaOidillaJollaIlmoitusMaster = valpasOppijat.oppijaSyntymäaikaHetusta("Kahdella-oppija-oidilla-ilmo", "Valpas", "040605A0123", kotikunta = Some(Kunta.helsinki))
  val oppivelvollinenMonellaOppijaOidillaJollaIlmoitusToinen = valpasOppijat.duplicate(oppivelvollinenMonellaOppijaOidillaJollaIlmoitusMaster)
  val oppivelvollinenMonellaOppijaOidillaJollaIlmoitusKolmas = valpasOppijat.duplicate(oppivelvollinenMonellaOppijaOidillaJollaIlmoitusMaster)
  val lukionAloittanutJollaVanhaIlmoitus = valpasOppijat.oppijaSyntymäaikaHetusta("LukionAloittanut-ilmo", "Valpas", "110405A435M", kotikunta = Some(Kunta.helsinki))
  val lukionAloittanutJaLopettanutJollaIlmoituksia = valpasOppijat.oppijaSyntymäaikaHetusta("LukionAloittanutJaLopettanut-ilmo", "Valpas", "050405A249S", kotikunta = Some(Kunta.helsinki))
  val ammattikoulustaValmistunutOpiskelija = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-valmistunut-opiskelija", "Valpas", "180304A082P", kotikunta = Some(Kunta.helsinki))
  val eronnutMaaliskuussa17VuottaTäyttäväKasiluokkalainen = valpasOppijat.oppijaSyntymäaikaHetusta("Eronnut-maaliskuussa-17-vuotta-täyttävä-8-luokkalainen", "Valpas", "280904A2768", kotikunta = Some(Kunta.helsinki))
  val eronnutKeväänValmistumisJaksolla17VuottaTäyttäväKasiluokkalainen = valpasOppijat.oppijaSyntymäaikaHetusta("Eronnut-kevään-valmistumisjaksolla-17-vuotta-täyttävä-8-luokkalainen", "Valpas", "121004A189X", kotikunta = Some(Kunta.helsinki))
  val eronnutElokuussa17VuottaTäyttäväKasiluokkalainen = valpasOppijat.oppijaSyntymäaikaHetusta("Eronnut-elokuussa-17-vuotta-täyttävä-8-luokkalainen", "Valpas", "110904A007L", kotikunta = Some(Kunta.helsinki))
  val valmistunutYsiluokkalainenVsop = valpasOppijat.oppijaSyntymäaikaHetusta("Ysiluokka-valmis-keväällä-2021-vsop", "Valpas", "190705A575R", kotikunta = Some(Kunta.helsinki))
  val ysiluokkaKeskenVsop = valpasOppijat.oppijaSyntymäaikaHetusta("Oppivelvollinen-ysiluokka-kesken-vsop", "Valpas", "240305A7103", kotikunta = Some(Kunta.helsinki))
  val valmistunutKasiluokkalainen = valpasOppijat.oppijaSyntymäaikaHetusta("Valmistunut-kasiluokkalainen-alle-17-vuotias", "Valpas", "090605A768P", kotikunta = Some(Kunta.helsinki))
  val oppivelvollinenMonellaOppijaOidillaJollaIlmoitusMaster2 = valpasOppijat.oppijaSyntymäaikaHetusta("Kahdella-oppija-oidilla-ilmo-2", "Valpas", "030605A476D", kotikunta = Some(Kunta.helsinki))
  val oppivelvollinenMonellaOppijaOidillaJollaIlmoitusToinen2 = valpasOppijat.duplicate(oppivelvollinenMonellaOppijaOidillaJollaIlmoitusMaster2)
  val ilmoituksenLisätiedotPoistettu = valpasOppijat.oppijaSyntymäaikaHetusta("Ilmoituksen-lisätiedot–poistettu", "Valpas", "190505A3019", kotikunta = Some(Kunta.helsinki))
  val lukiostaValmistunutOpiskelija = valpasOppijat.oppijaSyntymäaikaHetusta("Lukio-opiskelija-valmistunut", "Valpas", "271105A835H", kotikunta = Some(Kunta.helsinki))
  val ammattikouluOpiskelija = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-opiskelija", "Valpas", "231005A2431", kotikunta = Some(Kunta.helsinki))
  val kolmoistutkinnostaValmistunutOpiskelija = valpasOppijat.oppijaSyntymäaikaHetusta("Kolmois-tutkinnosta-valmistunut", "Valpas", "260905A7672", kotikunta = Some(Kunta.helsinki))
  val nivelvaiheestaValmistunutOpiskelija = valpasOppijat.oppijaSyntymäaikaHetusta("Nivelvaiheesta-valmistunut", "Valpas", "201005A022Y", kotikunta = Some(Kunta.helsinki))
  val oppivelvollisuusKeskeytetty = valpasOppijat.oppijaSyntymäaikaHetusta("Oppivelvollisuus-keskeytetty-määräajaksi", "Valpas", "181005A1560", kotikunta = Some(Kunta.helsinki))
  val oppivelvollisuusKeskeytettyToistaiseksi = valpasOppijat.oppijaSyntymäaikaHetusta("Oppivelvollisuus-keskeytetty-toistaiseksi", "Valpas", "150905A1823", kotikunta = Some(Kunta.helsinki))
  val eiOppivelvollisuudenSuorittamiseenYksinäänKelpaaviaOpiskeluoikeuksia = valpasOppijat.oppijaSyntymäaikaHetusta("Ei-oppivelvollisuuden-suorittamiseen-yksinään-kelpaavia-opiskeluoikeuksia", "Valpas", "061005A671V", kotikunta = Some(Kunta.pyhtää))
  val hetuton = valpasOppijat.oppija("Hetuton", "Valpas", "", syntymäaika = Some(LocalDate.of(2005, 1, 1)), kotikunta = Some(Kunta.helsinki))
  val oppivelvollinenJollaHetu = valpasOppijat.oppijaSyntymäaikaHetusta("Oppivelvollinen-hetullinen", "Valpas", "030105A7507", kotikunta = Some(Kunta.helsinki))
  val oppivelvollinenJollaHetuHetutonSlave = valpasOppijat.duplicate(oppivelvollinenJollaHetu.copy(hetu = None))
  val ammattikouluOpiskelijaValma = valpasOppijat.oppijaSyntymäaikaHetusta("Valma-opiskelija", "Valpas", "190105A839D", kotikunta = Some(Kunta.helsinki))
  val ammattikouluOpiskelijaTelma = valpasOppijat.oppijaSyntymäaikaHetusta("Telma-opiskelija", "Valpas", "020805A7784", kotikunta = Some(Kunta.helsinki))
  val amisEronnutEiUuttaOpiskeluoikeutta = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-eronnut", "Valpas", "010805A852V", kotikunta = Some(Kunta.pyhtää))
  val amisEronnutUusiOpiskeluoikeusTulevaisuudessaKeskeyttänyt = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-eronnut-uusi-oo-tulevaisuudessa-keskeyttänyt", "Valpas", "240905A539D", kotikunta = Some(Kunta.helsinki))
  val amisEronnutUusiOpiskeluoikeusVoimassa = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-eronnut-uusi-oo-voimassa", "Valpas", "241005A214R", kotikunta = Some(Kunta.helsinki))
  val amisEronnutUusiOpiskeluoikeusAlkanutEroamispäivänä = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-eronnut-uusi-oo-samana-päivänä", "Valpas", "300305C243W", kotikunta = Some(Kunta.helsinki))
  val amisEronnutUusiOpiskeluoikeusAlkanutEroamispäivänäJaPäättynyt = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-eronnut-uusi-oo-samana-päivänä-jo-päättynyt", "Valpas", "140305D021D", kotikunta = Some(Kunta.helsinki))
  val amisEronnutUusiNivelvaiheOpiskeluoikeusAlkanutEroamispäivänäJaPäättynyt = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-eronnut-uusi-nivelvaihe-oo-samana-päivänä-jo-päättynyt", "Valpas", "240305A783E", kotikunta = Some(Kunta.helsinki))
  val amisEronnutUusiOpiskeluoikeusAlkanutJaPäättynytEroonKeskellä = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-eronnut-uusi-oo-alkanut-ja-päättynyt-eroon-keskellä", "Valpas", "170205A609H", kotikunta = Some(Kunta.helsinki))
  val amisEronnutUusiOpiskeluoikeusPeruskoulussaKeskeyttänytTulevaisuudessa = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-eronnut-uusi-peruskoulussa-keskeyttänyt-tulevaisuudessa", "Valpas", "100205A291R", kotikunta = Some(Kunta.helsinki))
  val amisEronnutUusiOpiskeluoikeusNivelvaiheessa = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-eronnut-uusi-nivelvaiheessa", "Valpas", "180605A898P", kotikunta = Some(Kunta.helsinki))
  val amisEronnutUusiOpiskeluoikeusNivelvaiheessa2 = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-eronnut-uusi-nivelvaiheessa-valmassa", "Valpas", "040804A0600", kotikunta = Some(Kunta.helsinki))
  val amisEronnutMontaUuttaOpiskeluoikeutta = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-eronnut-monta-uutta-oota", "Valpas", "241005A449A", kotikunta = Some(Kunta.helsinki))
  val amisEronnutUusiKelpaamatonOpiskeluoikeusNivelvaiheessa = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-eronnut-nivelvaihe-ei-kelpaa", "Valpas", "101105A1703", kotikunta = Some(Kunta.helsinki))
  val amisEronnutUusiKelpaamatonOpiskeluoikeusNivelvaiheessa2 = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-eronnut-nivelvaihe-vstssa-ei-kelpaa", "Valpas", "090604A305H", kotikunta = Some(Kunta.helsinki))
  val ammattikouluOpiskelijaMontaOpiskeluoikeutta = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-monta-oota", "Valpas", "280105A505E", kotikunta = Some(Kunta.helsinki))
  val amisAmmatillinenJaNäyttötutkintoonValmistava = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-useita-pts", "Valpas", "280505A418V", kotikunta = Some(Kunta.helsinki))
  val opiskeluoikeudetonOppivelvollisuusikäinenOppija = valpasOppijat.oppijaSyntymäaikaHetusta("Ei-opiskeluoikeuksia-oppivelvollisuusikäinen", "Valpas", "110405A6951", kotikunta = Some(Kunta.pyhtää))
  val opiskeluoikeudetonEiOppivelvollisuusikäinenOppija = valpasOppijat.oppijaSyntymäaikaHetusta("Ei-opiskeluoikeuksia-vanha", "Valpas", "070302A402D", kotikunta = Some(Kunta.pyhtää))
  val lukioVäliaikaisestiKeskeytynyt = valpasOppijat.oppijaSyntymäaikaHetusta("Lukio-väliaikaisesti-keskeytynyt", "Valpas", "300504A157F", kotikunta = Some(Kunta.helsinki))
  val amisLomalla = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-lomalla", "Valpas", "030905A194R", kotikunta = Some(Kunta.helsinki))
  val internationalSchoolista9LuokaltaEnnen2021Valmistunut = valpasOppijat.oppijaSyntymäaikaHetusta("Inter-valmistunut-9-2020", "Valpas", "090605A517L", kotikunta = Some(Kunta.helsinki))
  val internationalSchoolista9Luokalta2021Valmistunut = valpasOppijat.oppijaSyntymäaikaHetusta("Inter-valmistunut-9-2021", "Valpas", "200405A780K", kotikunta = Some(Kunta.helsinki))
  val peruskoulustaValmistunutIlman9Luokkaa = valpasOppijat.oppijaSyntymäaikaHetusta("Valmistunut-ei-ysiluokkaa", "Valpas", "240905A4064", kotikunta = Some(Kunta.helsinki))
  val peruskoulustaLokakuussaValmistunutIlman9Luokkaa = valpasOppijat.oppijaSyntymäaikaHetusta("Valmistunut-lokakuussa-ei-ysiluokkaa", "Valpas", "110505A1818", kotikunta = Some(Kunta.helsinki))
  val lukioVanhallaOpsilla = valpasOppijat.oppijaSyntymäaikaHetusta("LukioVanhallaOpsilla", "Valpas", "060704A687P", kotikunta = Some(Kunta.helsinki))
  val muuttanutUlkomaille = valpasOppijat.oppijaSyntymäaikaHetusta("MuuttanutUlkomaille", "Valpas", "130805A850J", kotikunta = Some(Kunta.kotikuntaUlkomailla),
    kuntahistoriaMock = h => {
      val historia = Seq(
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, Kunta.helsinki, None, Some(LocalDate.of(2023, 1, 1))),
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, Kunta.kotikuntaUlkomailla, Some(LocalDate.of(2023, 1, 1)), None),
      )
      OppijanKuntahistoria(
        Some(h.henkilö.oid),
        historia,
        Seq.empty
      )
    }
  )
  val turvakieltoOppijaTyhjälläKotikunnalla = valpasOppijat.oppijaSyntymäaikaHetusta("TurvakieltoTyhjälläKotikunnalla", "Valpas", "280705A584U", valpasOppijat.generateId(), None, turvakielto = true, kotikunta = Some(""))
  val oppivelvollinenIntSchoolYsiluokkaKeskenKeväällä2021 = valpasOppijat.oppijaSyntymäaikaHetusta("Oppivelvollinen-int-school-kesken-keväällä-2021", "Valpas", "180205A026B", kotikunta = Some(Kunta.helsinki))
  val intSchoolKasiluokkaKeskenKeväällä2021 = valpasOppijat.oppijaSyntymäaikaHetusta("Int-school-kasiluokka-kesken-keväällä-2021", "Valpas", "030705A638E", kotikunta = Some(Kunta.helsinki))
  val intSchool9LuokaltaKeskenEronnutOppija =valpasOppijat.oppijaSyntymäaikaHetusta("Int-school-9-luokalta-kesken-eroaja-aiemmin", "Valpas", "180205A6682", kotikunta = Some(Kunta.helsinki))
  val intSchool9LuokaltaKeskenEronnutOppijaTarkastelupäivänä = valpasOppijat.oppijaSyntymäaikaHetusta("Int-school-9-luokalta-kesken-eroaja-samana-päivänä", "Valpas", "150905A020V", kotikunta = Some(Kunta.helsinki))
  val intSchool9LuokaltaKeskenEronnutOppijaTarkastelupäivänJälkeen = valpasOppijat.oppijaSyntymäaikaHetusta("Int-school-9-luokalta-kesken-eroaja-myöhemmin", "Valpas", "210405A014H", kotikunta = Some(Kunta.helsinki))
  val intSchool9LuokaltaValmistumisenJälkeenEronnutOppija = valpasOppijat.oppijaSyntymäaikaHetusta("Int-school-9-luokalta-valmistumisen-jälkeen-eronnut-aiemmin", "Valpas", "170405A683H", kotikunta = Some(Kunta.pyhtää))
  val intSchool9LuokaltaValmistumisenJälkeenEronnutOppijaTarkastelupäivänä = valpasOppijat.oppijaSyntymäaikaHetusta("Int-school-9-luokalta-valmistumisen-jälkeen-eronnut-samana-päivänä", "Valpas", "090905A633S", kotikunta = Some(Kunta.helsinki))
  val intSchool9LuokaltaValmistumisenJälkeenEronnutOppijaTarkastelupäivänJälkeen = valpasOppijat.oppijaSyntymäaikaHetusta("Int-school-9-luokalta-valmistumisen-jälkeen-eronnut-myöhemmin", "Valpas", "100705A034F", kotikunta = Some(Kunta.helsinki))
  val intSchool9LuokanJälkeenLukionAloittanut = valpasOppijat.oppijaSyntymäaikaHetusta("Int-school-9-luokan-jälkeen-lukion-aloittanut", "Valpas", "120505A3434", kotikunta = Some(Kunta.helsinki))
  val intSchool9LuokanJälkeenIntSchoolin10LuokallaAloittanut = valpasOppijat.oppijaSyntymäaikaHetusta("Int-school-9-luokan-jälkeen-int-schoolin-10-luokalla-aloittanut", "Valpas", "220205A6867", kotikunta = Some(Kunta.helsinki))
  val intSchool9LuokanJälkeenLukionLokakuussaAloittanut = valpasOppijat.oppijaSyntymäaikaHetusta("Int-school-9-luokan-jälkeen-lukion-lokakuussa-aloittanut", "Valpas", "070105A7969", kotikunta = Some(Kunta.helsinki))
  val intSchool9LuokanJälkeenIntSchoolin10LuokallaLokakuussaAloittanut = valpasOppijat.oppijaSyntymäaikaHetusta("Int-school-9-luokan-jälkeen-int-schoolin-10-luokalla-lokakuussa-aloittanut", "Valpas", "080405A722Y", kotikunta = Some(Kunta.helsinki))
  val intSchoolin9LuokaltaYli2kkAiemminValmistunut = valpasOppijat.oppijaSyntymäaikaHetusta("Int-school-yli-2kk-aiemmin-9-valmistunut", "Valpas", "231005A872A", kotikunta = Some(Kunta.helsinki))
  val intSchoolin9LuokaltaYli2kkAiemminValmistunut10Jatkanut = valpasOppijat.oppijaSyntymäaikaHetusta("Int-school-yli-2kk-aiemmin-9-valmistunut-10-jatkanut", "Valpas", "111105A3651", kotikunta = Some(Kunta.helsinki))
  val intSchoolistaEronnutMaaliskuussa17VuottaTäyttäväKasiluokkalainen = valpasOppijat.oppijaSyntymäaikaHetusta("Int-school-eronnut-maaliskuussa-17-vuotta-täyttävä-8-luokkalainen", "Valpas", "100304A1358", kotikunta = Some(Kunta.helsinki))
  val intSchoolistaEronnutElokuussa17VuottaTäyttäväKasiluokkalainen = valpasOppijat.oppijaSyntymäaikaHetusta("Int-school-eronnut-elokuussa-17-vuotta-täyttävä-8-luokkalainen", "Valpas", "220804A101X", kotikunta = Some(Kunta.helsinki))
  val intSchool10LuokaltaAloittanut = valpasOppijat.oppijaSyntymäaikaHetusta("Int-school-10-luokalta-aloittanut", "Valpas", "090605A676R", kotikunta = Some(Kunta.helsinki))
  val intSchool11LuokaltaAloittanut = valpasOppijat.oppijaSyntymäaikaHetusta("Int-school-11-luokalta-aloittanut", "Valpas", "050405A222W", kotikunta = Some(Kunta.helsinki))
  val intSchool8LuokanSyksyllä2021Aloittanut = valpasOppijat.oppijaSyntymäaikaHetusta("Int-school-8-luokan-syksyllä-2021-aloittanut", "Valpas", "040305A8601", kotikunta = Some(Kunta.helsinki))
  val intSchool9LuokanSyksyllä2021Aloittanut = valpasOppijat.oppijaSyntymäaikaHetusta("Int-school-9-luokan-syksyllä-2021-aloittanut", "Valpas", "210805A187A", kotikunta = Some(Kunta.helsinki))
  val intSchoolLokakuussaPerusopetuksenSuorittanut = valpasOppijat.oppijaSyntymäaikaHetusta("Int-school-9-vahvistettu-lokakuussa", "Valpas", "221105A467D", kotikunta = Some(Kunta.helsinki))
  val intSchool10LuokallaIlmanAlkamispäivää = valpasOppijat.oppijaSyntymäaikaHetusta("Int-school-10-luokalla-ilman-alkamispäivää", "Valpas", "140305A455D", kotikunta = Some(Kunta.helsinki))
  val aikuistenPerusopetuksessa = valpasOppijat.oppijaSyntymäaikaHetusta("Aikuisten-perusopetuksessa", "Valpas", "020304A145D", kotikunta = Some(Kunta.pyhtää))
  val aikuistenPerusopetuksessaSyksynRajapäivänJälkeenAloittava = valpasOppijat.oppijaSyntymäaikaHetusta("Aikuisten-perusopetuksessa-syksyn-rajapäivän-jälkeen", "Valpas", "250204A640D", kotikunta = Some(Kunta.helsinki))
  val aikuistenPerusopetuksessaPeruskoulustaValmistunut = valpasOppijat.oppijaSyntymäaikaHetusta("Aikuisten-perusopetuksesta-pk-valmistunut", "Valpas", "050304A177C", kotikunta = Some(Kunta.helsinki))
  val aikuistenPerusopetuksestaKeväänValmistujaksollaValmistunut = valpasOppijat.oppijaSyntymäaikaHetusta("Aikuisten-perusopetuksesta-keväällä-valmistunut", "Valpas", "070304A3464", kotikunta = Some(Kunta.helsinki))
  val aikuistenPerusopetuksestaEronnut = valpasOppijat.oppijaSyntymäaikaHetusta("Aikuisten-perusopetuksesta-eronnut", "Valpas", "040404A8818", kotikunta = Some(Kunta.helsinki))
  val aikuistenPerusopetuksestaYli2kkAiemminValmistunut = valpasOppijat.oppijaSyntymäaikaHetusta("Aikuisten-perusopetuksesta-yli-2kk-aiemmin-valmistunut", "Valpas", "300104A657C", kotikunta = Some(Kunta.helsinki))
  val aikuistenPerusopetuksestaAlle2kkAiemminValmistunut = valpasOppijat.oppijaSyntymäaikaHetusta("Aikuisten-perusopetuksesta-alle-2kk-aiemmin-valmistunut", "Valpas", "131004A1477", kotikunta = Some(Kunta.helsinki))
  val aikuistenPerusopetuksestaLähitulevaisuudessaValmistuva = valpasOppijat.oppijaSyntymäaikaHetusta("Aikuisten-perusopetuksesta-lähitulevaisuudessa-valmistuva", "Valpas", "220304A365D", kotikunta = Some(Kunta.helsinki))
  val aikuistenPerusopetuksestaTulevaisuudessaValmistuva = valpasOppijat.oppijaSyntymäaikaHetusta("Aikuisten-perusopetuksesta-tulevaisuudessa-valmistuva", "Valpas", "121104A0176", kotikunta = Some(Kunta.helsinki))
  val aikuistenPerusopetuksessaAineopiskelija = valpasOppijat.oppijaSyntymäaikaHetusta("Aikuisten-perusopetuksessa-aineopiskelija", "Valpas", "010604A727Y", kotikunta = Some(Kunta.pyhtää))
  val luva = valpasOppijat.oppijaSyntymäaikaHetusta("Luva", "Valpas", "290404A725B", kotikunta = Some(Kunta.helsinki))
  val kymppiluokka = valpasOppijat.oppijaSyntymäaikaHetusta("Kymppi", "Valpas", "160404A8577", kotikunta = Some(Kunta.helsinki))
  val vstKops = valpasOppijat.oppijaSyntymäaikaHetusta("Vst-kops", "Valpas", "190504A564H", kotikunta = Some(Kunta.helsinki))
  val valma = valpasOppijat.oppijaSyntymäaikaHetusta("Valma", "Valpas", "090104A303D", kotikunta = Some(Kunta.helsinki))
  val telma = valpasOppijat.oppijaSyntymäaikaHetusta("Telma", "Valpas", "160304A7532", kotikunta = Some(Kunta.helsinki))
  val telmaJaAmis = valpasOppijat.oppijaSyntymäaikaHetusta("Telma-ja-amis", "Valpas", "030204A7935", kotikunta = Some(Kunta.helsinki))
  val kaksiToisenAsteenOpiskelua = valpasOppijat.oppijaSyntymäaikaHetusta("Kaksi-toisen-asteen-opiskelua", "Valpas", "120504A399N", kotikunta = Some(Kunta.helsinki))
  val kotiopetusMeneilläänVanhallaRakenteellaOppija = valpasOppijat.oppijaSyntymäaikaHetusta("Kotiopetus-meneillä-vanha-rakenne", "Valpas", "170205A776W", kotikunta = Some(Kunta.helsinki))
  val esikoululainen = valpasOppijat.oppijaSyntymäaikaHetusta("Esikoululainen", "Valpas", hetu="270615A6481", kotikunta = Some(Kunta.helsinki))
  val maksuttomuuttaPidennetty = valpasOppijat.oppijaSyntymäaikaHetusta("Maksuttomuutta-pidennetty", "Valpas", "070604A200U", kotikunta = Some(Kunta.helsinki))
  val maksuttomuuttaPidennettyValmistunut = valpasOppijat.oppijaSyntymäaikaHetusta("Maksuttomuutta-pidennetty-valmistunut", "Valpas", "081004A416N", kotikunta = Some(Kunta.helsinki))
  val eiOppivelvollinenLiianNuori = valpasOppijat.oppijaSyntymäaikaHetusta("Pikkulapsi", "Valpas", "021115A679X", kotikunta = Some(Kunta.helsinki))
  val eiKoskessaOppivelvollinen = valpasOppijat.oppijaSyntymäaikaHetusta("Kosketon", "Valpas", "240105A7049", kotikunta = Some(Kunta.helsinki))
  val oppivelvollisuusKeskeytettyEiOpiskele = valpasOppijat.oppijaSyntymäaikaHetusta("Oppivelvollisuus-keskeytetty-ei-opiskele", "Valpas", "011005A115P", kotikunta = Some(Kunta.pyhtää))
  val perusopetukseenValmistautuva = valpasOppijat.oppijaSyntymäaikaHetusta("Perusopetukseen-valmistautuva", "Valpas", "151011A1403", kotikunta = Some(Kunta.pyhtää))
  val amisEronnutUusiKelpaamatonOpiskeluoikeusPerusopetukseenValmistavassa = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-eronnut-perusopetukseen-valmistava-ei-kelpaa", "Valpas", "240205A508S", kotikunta = Some(Kunta.helsinki))
  val casIntegraationTestaus = valpasOppijat.oppijaSyntymäaikaHetusta("Demo", "Nordea", "210281-9988", kotikunta = Some(Kunta.helsinki))
  val turvakieltoOppijanVanhempi = valpasOppijat.oppijaSyntymäaikaHetusta("Turvakielto-oppijan", "Vanhempi", "240470-621T", kotikunta = Some(Kunta.helsinki))
  val preIbAloitettu = valpasOppijat.oppijaSyntymäaikaHetusta("SuorittaaPreIB", "Valpas", "190704A574E", kotikunta = Some(Kunta.helsinki))
  val oppivelvollinenYsiluokkaKeskenKeväällä2021Puuttuva7LuokanAlkamispäivä = valpasOppijat.oppijaSyntymäaikaHetusta("Oppivelvollinen-ysiluokka-kesken-keväällä-2021-rikkinäinen-7-luokka", "Valpas", "210305A6175", kotikunta = Some(Kunta.helsinki))
  val perusopetukseenValmistautuva17VuottaTäyttävä = valpasOppijat.oppijaSyntymäaikaHetusta("Perusopetukseen-valmistautuva-17-vuotta-täyttävä", "Valpas", "060104A339M", kotikunta = Some(Kunta.helsinki))
  val perusopetukseenValmistavastaValmistunut17Vuotias = valpasOppijat.oppijaSyntymäaikaHetusta("Perusopetukseen-valmistavasta-valmistunut-17-vuotta-täyttävä", "Valpas", "290504A780X", kotikunta = Some(Kunta.helsinki))
  val perusopetukseenValmistavastaEronnut17Vuotias = valpasOppijat.oppijaSyntymäaikaHetusta("Perusopetukseen-valmistavasta-eronnut-17-vuotta-täyttävä", "Valpas", "210604A184B", kotikunta = Some(Kunta.helsinki))
  val sureHautAinaEpäonnistuvaOppija = valpasOppijat.oppijaSyntymäaikaHetusta("Sure-haut-aina-epäonnistuvat", "Valpas", "180704A3397", kotikunta = Some(Kunta.helsinki))
  val eiKoskessaAlle18VuotiasMuttaEiOppivelvollinenSyntymäajanPerusteella = valpasOppijat.oppijaSyntymäaikaHetusta("Kosketon-ei-oppivelvollinen-alle-18-v", "Valpas", "250903A698N", kotikunta = Some(Kunta.helsinki))
  val eiKoskessaOppivelvollinenAhvenanmaalainen = valpasOppijat.oppijaSyntymäaikaHetusta("Kosketon-ahvenanmaalainen", "Valpas", "050705A749A", kotikunta = Some("170"))
  val eiKoskessaOppivelvollinenAhvenanmaalainenTurvakiellollinen = valpasOppijat.oppijaSyntymäaikaHetusta("Kosketon-ahvenanmaalainen-turvakielto", "Valpas", "091105A8136", turvakielto = true, kotikunta = Some("170"))
  val eiKoskessaOppivelvollinenJollaKeskeytyksiäJaIlmoituksia = valpasOppijat.oppijaSyntymäaikaHetusta("Kosketon-keskeytyksiä-ilmoituksia", "Valpas", "260705A1119", kotikunta = Some(Kunta.helsinki))
  val eiKoskessaOppivelvollinenJollaKeskeytyksiäJaIlmoituksiaSlave = valpasOppijat.duplicate(eiKoskessaOppivelvollinenJollaKeskeytyksiäJaIlmoituksia.copy(hetu = None))
  val eiKoskessaHetuton = valpasOppijat.oppija("Kosketon-hetuton", "Valpas", "", syntymäaika = Some(LocalDate.of(2005, 3, 3)), kotikunta = Some(Kunta.helsinki))
  val eiKoskessa7VuottaTäyttävä = valpasOppijat.oppijaSyntymäaikaHetusta("Kosketon-7-vuotta-täyttävä", "Valpas", "160614A587P", kotikunta = Some(Kunta.helsinki))
  val valmistunutNivelvaiheenOpiskelija2022 = valpasOppijat.oppijaSyntymäaikaHetusta("Valmistunut-nivelvaiheet-opiskelija-2022", "Valpas", "190305A488P", kotikunta = Some(Kunta.helsinki))
  val alkukesästäEronnutNivelvaiheenOpiskelija = valpasOppijat.oppijaSyntymäaikaHetusta("Alkukesästä-eronnut-nivelvaiheen-opiskelija", "Valpas", "161005A214K", kotikunta = Some(Kunta.helsinki))
  val alkukesästäEronneeksiKatsottuNivelvaiheenOpiskelija = valpasOppijat.oppijaSyntymäaikaHetusta("Alkukesästä-eronneeksi-katsottu-nivelvaiheen-opiskelija", "Valpas", "180105A1064", kotikunta = Some(Kunta.helsinki))
  val alkuvuodestaEronnutNivelvaiheenOpiskelija = valpasOppijat.oppijaSyntymäaikaHetusta("Alkuvuodesta-eronnut-nivelvaiheen-opiskelija", "Valpas", "290805A6137", kotikunta = Some(Kunta.helsinki))
  val alkuvuodestaEronneeksiKatsottuNivelvaiheenOpiskelija = valpasOppijat.oppijaSyntymäaikaHetusta("Alkuvuodesta-eronneeksi-katsottu-nivelvaiheen-opiskelija", "Valpas", "100405A2202", kotikunta = Some(Kunta.helsinki))
  val keväänUlkopuolellaValmistunut17v = valpasOppijat.oppijaSyntymäaikaHetusta("Ysiluokka-valmis-syksyllä-2021", "Valpas", "190604A006K", kotikunta = Some(Kunta.helsinki))
  val keväänUlkopuolellaEronnut17v = valpasOppijat.oppijaSyntymäaikaHetusta("Ysiluokka-eronnut-syksyllä-2021", "Valpas", "190604A006K", kotikunta = Some(Kunta.helsinki))
  val läsnä17VuottaTäyttäväKasiluokkalainen = valpasOppijat.oppijaSyntymäaikaHetusta("Läsnä-17-vuotta-täyttävä-8-luokkalainen", "Valpas", "101104A349L", kotikunta = Some(Kunta.helsinki))
  val keskeyttänyt17VuottaTäyttäväKasiluokkalainen = valpasOppijat.oppijaSyntymäaikaHetusta("Keskeyttänyt-17-vuotta-täyttävä-8-luokkalainen", "Valpas", "101104A349L", kotikunta = Some(Kunta.helsinki))
  val oppivelvollisuudestaVapautettu = valpasOppijat.oppijaSyntymäaikaHetusta("Oppivelvollisuudesta-vapautettu", "Valpas", "060605A538B", kotikunta = Some(Kunta.helsinki))
  val amisEronnutTuvalainen = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-eronnut-tuva", "Valpas", "100905A8414", kotikunta = Some(Kunta.pyhtää))
  val valmistunutTuvalainen = valpasOppijat.oppijaSyntymäaikaHetusta("Nivelvaiheesta-valmistunut-tuva", "Valpas", "160505A841S", kotikunta = Some(Kunta.pyhtää))
  val valmistunutAmiksenOsittainen = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-valmistunut-osittainen", "Valpas", "200105A171H", kotikunta = Some(Kunta.pyhtää))
  val valmistunutAmiksenOsittainenUusiOo = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-valmistunut-osittainen-ja-uusi-opiskeluoikeus", "Valpas", "280705A076E", kotikunta = Some(Kunta.pyhtää))
  val valmistunutYsiluokkalainenJollaIlmoitusJaUusiOpiskeluoikeus = valpasOppijat.oppijaSyntymäaikaHetusta("Ysiluokka-valmis-ja-ilmoitettu-ja-uusi-nivelvaihe", "Valpas", "240706A3571", kotikunta = Some(Kunta.helsinki))
  val oppijaJollaYOOpiskeluoikeus = valpasOppijat.oppijaSyntymäaikaHetusta("YO-opiskeluoikeus", "Valpas", "060807A7787", kotikunta = Some(Kunta.helsinki))
  val amisValmistunutEronnutValmasta = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-valmistunut-eronnut-valmasta", "Valpas", "180605A313U", kotikunta = Some(Kunta.helsinki))
  val oppivelvollinenESHS5KeskenKeväällä2021 = valpasOppijat.oppijaSyntymäaikaHetusta("Oppivelvollinen-esh-s5-kesken-keväällä-2021", "Valpas", "030105A049L", kotikunta = Some(Kunta.helsinki))
  val eshS4KeskenKeväällä2021 = valpasOppijat.oppijaSyntymäaikaHetusta("ESH-s4-kesken-keväällä-2021", "Valpas", "110505A1807", kotikunta = Some(Kunta.helsinki))
  val eshS4JälkeenS5Aloittanut = valpasOppijat.oppijaSyntymäaikaHetusta("ESH-s4-jälkeen-s5-aloittanut", "Valpas", "200305A594S", kotikunta = Some(Kunta.helsinki))
  val eshS5JälkeenLukiossaAloittanut = valpasOppijat.oppijaSyntymäaikaHetusta("ESH-s5-jälkeen-lukiossa-aloittanut", "Valpas", "271105A101U", kotikunta = Some(Kunta.helsinki))
  val eshNurseryssä = valpasOppijat.oppijaSyntymäaikaHetusta("ESH-nurseryssä", "Valpas", hetu="070614A452J", kotikunta = Some(Kunta.helsinki))
  val lukionAineOpinnotJaAmmatillisia = valpasOppijat.oppijaSyntymäaikaHetusta("Lukion-aineopinnot-ja-ammatillisia", "Valpas", "121005A797T", kotikunta = Some(Kunta.pyhtää))
  val oppijaJollaAmisJaValmistunutYO = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-ja-YO", "Valpas", "300805A756F", kotikunta = Some(Kunta.helsinki))
  val eshEbTutkinnonAloittanut = valpasOppijat.oppijaSyntymäaikaHetusta("ESH-EB-tutkinnon-aloittanut", "Valpas", hetu = "220910A863V", kotikunta = Some(Kunta.pyhtää))
  val eshEbTutkinnostaValmistunut = valpasOppijat.oppijaSyntymäaikaHetusta("ESH-EB-tutkinnosta-valmistunut", "Valpas", hetu = "010410A5783", kotikunta = Some(Kunta.pyhtää))
  val eshEbTutkinnostaEronnut = valpasOppijat.oppijaSyntymäaikaHetusta("ESH-EB-tutkinnosta-eronnut", "Valpas", hetu = "180610A758F", kotikunta = Some(Kunta.pyhtää))
  val eshKeskenEbTutkinnonAloittanut = valpasOppijat.oppijaSyntymäaikaHetusta("ESH-kesken-EB-tutkinnon-aloittanut", "Valpas", hetu = "021110A1065", kotikunta = Some(Kunta.pyhtää))
  val taiteenPerusopetusPäättynyt = valpasOppijat.oppijaSyntymäaikaHetusta("Taiteilija", "Petra", "010110A955U", kotikunta = Some(Kunta.helsinki))
  val ulkomailtaSuomeenMuuttanut = valpasOppijat.oppijaSyntymäaikaHetusta("Maahanmuuttaja", "Masa", "010106A431W", kotikunta = Some(Kunta.helsinki),
    kuntahistoriaMock = h => {
      val historia = Seq(
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, Kunta.kotikuntaUlkomailla, Some(LocalDate.of(2006, 1, 1)), Some(LocalDate.of(2016, 1, 1))),
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, Kunta.helsinki, Some(LocalDate.of(2014, 1, 1)), Some(LocalDate.of(2015, 1, 1))),
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, Kunta.kotikuntaUlkomailla, Some(LocalDate.of(2015, 1, 1)), Some(LocalDate.of(2016, 1, 1))),
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, Kunta.helsinki, Some(LocalDate.of(2016, 1, 1)), None),
      )
      OppijanKuntahistoria(
        Some(h.henkilö.oid),
        historia,
        Seq.empty
      )
    }
  )
  val ammattitutkintoYoTutkinnonJalkeen = valpasOppijat.oppijaSyntymäaikaHetusta("Ammattitutkinto yo-tutkinnon Jälkeen", "Antti", "300805A1918", kotikunta = Some(Kunta.helsinki))
  val lukioOpinnotAmmattitutkinnonJalkeen = valpasOppijat.oppijaSyntymäaikaHetusta("Lukio-opinnot Ammattitutkinnon Jälkeen", "Lucia", "300805A4409", kotikunta = Some(Kunta.helsinki))
  val muuttanutUlkomailleEnnen7vIkää = valpasOppijat.oppijaSyntymäaikaHetusta("muuttanutUlkomailleEnnen7vIkää", "Valpas", "130805A881J", kotikunta = Some(Kunta.kotikuntaUlkomailla),
    kuntahistoriaMock = h => {
      val historia = Seq(
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, Kunta.helsinki, None, Some(LocalDate.of(2010, 10, 1))),
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, Kunta.kotikuntaUlkomailla, Some(LocalDate.of(2010, 10, 1)), None),
      )
      OppijanKuntahistoria(
        Some(h.henkilö.oid),
        historia,
        Seq.empty
      )
    }
  )
  val oppijaTyhjälläKotikunnalla = valpasOppijat.oppijaSyntymäaikaHetusta("TyhjälläKotikunnalla", "Valpas", "081105A407E", kotikunta = None)
  val eiKoskessaEikäOppivelvollinenKotikuntahistorianPerusteella = valpasOppijat.oppijaSyntymäaikaHetusta("Kosketon-ei-oppivelvollinen-ulkomailla", "Valpas", "041006A550X", kotikunta = Some(Kunta.helsinki),
    kuntahistoriaMock = h => {
      val historia = Seq(
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, Kunta.eiKotikuntaaSuomessa, Some(LocalDate.of(2006, 4, 10)), None)
      )
      OppijanKuntahistoria(
        Some(h.henkilö.oid),
        historia,
        Seq.empty
      )
    })

  // Kutsumanimi ja yhteystiedot haetaan oppijanumerorekisteristä Valpas-käyttäjälle, tallennetaan siksi käyttäjä myös "oppijana" mockeihin
  val käyttäjäValpasJklNormaalikoulu = valpasOppijat.oppija(
    hetu = "300850-4762",
    oid = ValpasMockUsers.valpasJklNormaalikoulu.oid,
    suku = ValpasMockUsers.valpasJklNormaalikoulu.lastname,
    etu = ValpasMockUsers.valpasJklNormaalikoulu.firstname,
    kutsumanimi = Some("Kutsu"),
    kotikunta = Some(Kunta.helsinki),
  )

  val valmistunutAmiksenOsittainenUseastaTutkinnosta = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-valmistunut-osittainen-useasta-tutkinnosta", "Valpas", "110205A632C", kotikunta = Some(Kunta.pyhtää))
  val valmistunutAmiksenOsittainenUseastaTutkinnostaUusiOo = valpasOppijat.oppijaSyntymäaikaHetusta("Amis-valmistunut-osittainen-useasta-tutkinnosta-ja-uusi-opiskeluoikeus", "Valpas", "250405A616H", kotikunta = Some(Kunta.pyhtää))

  val ulkomailleAlle18vuotiaanaMuuttanutJaAlle18vuotiaanaPalannut = valpasOppijat.oppijaSyntymäaikaHetusta("UlkomailleAlle18vuotiaanaMuuttanutJaAlle18vuotiaanaPalannut", "Valpas", "211005A0993", kotikunta = Some(Kunta.helsinki),
    kuntahistoriaMock = h => {
      val historia = Seq(
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, Kunta.helsinki, None, Some(date(2010, 10, 1))),
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, Kunta.kotikuntaUlkomailla, Some(date(2010, 10, 2)), Some(date(2015, 10, 1))),
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, Kunta.helsinki, Some(date(2015, 10, 2)), None) // 10-vuotiaana muuttanut takaisin
      )
      OppijanKuntahistoria(
        Some(h.henkilö.oid),
        historia,
        Seq.empty
      )
    }
  )
  val ulkomailleAlle18vuotiaanaMuuttanutJaYli18vuotiaanaPalannut = valpasOppijat.oppijaSyntymäaikaHetusta("UlkomailleAlle18vuotiaanaMuuttanutJaYli18vuotiaanaPalannut", "Valpas", "040105A618L", kotikunta = Some(Kunta.helsinki),
    kuntahistoriaMock = h => {
      val historia = Seq(
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, Kunta.helsinki, None, Some(date(2015, 10, 1))),
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, Kunta.kotikuntaUlkomailla, Some(date(2015, 10, 2)), Some(date(2021, 3, 18))),
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, Kunta.helsinki, Some(date(2023, 1, 4)), None) // 18-vuotiaana muuttanut takaisin
      )
      OppijanKuntahistoria(
        Some(h.henkilö.oid),
        historia,
        Seq.empty
      )
    }
  )
  val ulkomailleYli18vuotiaanaMuuttanutJaYli18vuotiaanaPalannut = valpasOppijat.oppijaSyntymäaikaHetusta("UlkomailleYli18vuotiaanaMuuttanutJaYli18vuotiaanaPalannut", "Valpas", "050405A211J", kotikunta = Some(Kunta.helsinki),
    kuntahistoriaMock = h => {
      val historia = Seq(
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, Kunta.helsinki, None, Some(date(2023, 4, 4))),
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, Kunta.kotikuntaUlkomailla, Some(date(2023, 4, 5)), Some(date(2023, 6, 18))), // 18-vuotiaana ulkomaille muuttanut
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, Kunta.helsinki, Some(date(2024, 6, 19)), None)
      )
      OppijanKuntahistoria(
        Some(h.henkilö.oid),
        historia,
        Seq.empty
      )
    }
  )
  val ulkomailleAlle18vuotiaanaMuuttanutJaYli20vuotiaanaPalannut = valpasOppijat.oppijaSyntymäaikaHetusta("UlkomailleAlle18vuotiaanaMuuttanutJaYli21vuotiaanaPalannut", "Valpas", "080505A322V", kotikunta = Some(Kunta.helsinki),
    kuntahistoriaMock = h => {
      val historia = Seq(
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, Kunta.helsinki, None, Some(date(2015, 4, 20))),
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, Kunta.kotikuntaUlkomailla, Some(date(2015, 4, 21)), Some(date(2025, 12, 31))), // alle 18-vuotiaana ulkomaille muuttanut
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, Kunta.helsinki, Some(date(2026, 1, 1)), None) // 20-vuotiaana (sinä vuonna kun täyttää 21) takaisin muuttanut
      )
      OppijanKuntahistoria(
        Some(h.henkilö.oid),
        historia,
        Seq.empty
      )
    }
  )
  val ulkomailleYli18vuotiaanaMuuttanutJaYli20vuotiaanaPalannut = valpasOppijat.oppijaSyntymäaikaHetusta("UlkomailleYli18vuotiaanaMuuttanutJaYli21vuotiaanaPalannut", "Valpas", "210405A695F", kotikunta = Some(Kunta.helsinki),
    kuntahistoriaMock = h => {
      val historia = Seq(
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, Kunta.helsinki, None, Some(date(2023, 4, 20))),
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, Kunta.kotikuntaUlkomailla, Some(date(2023, 4, 21)), Some(date(2025, 12, 31))), // 18-vuotiaana ulkomaille muuttanut
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, Kunta.helsinki, Some(date(2026, 1, 1)), None) // 20-vuotiaana (sinä vuonna kun täyttää 21) takaisin muuttanut
      )
      OppijanKuntahistoria(
        Some(h.henkilö.oid),
        historia,
        Seq.empty
      )
    }
  )

  val ulkomailtaAlle18vuotiaanaMuuttanutJaSuomessaMuuttanut = valpasOppijat.oppijaSyntymäaikaHetusta("UlkomailtaAlle18vuotiaanaMuuttanutJaSuomessaMuuttanut", "Valpas", "020206A171Y", kotikunta = Some(Kunta.helsinki),
    kuntahistoriaMock = h => {
      val historia = Seq(
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, Kunta.eiKotikuntaaSuomessa, Some(date(2018, 10, 1)), Some(date(2018, 11, 30))),
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, Kunta.pyhtää, Some(date(2018, 12, 1)), Some(date(2024, 11, 30))),
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, Kunta.helsinki, Some(date(2024, 12, 1)), None)
      )
      OppijanKuntahistoria(
        Some(h.henkilö.oid),
        historia,
        Seq.empty
      )
    }
  )

  val suomessaMuuttanutPaikastaToiseen = valpasOppijat.oppijaSyntymäaikaHetusta("SuomessaMuuttanutPaikastaToiseen", "Valpas", "060206A2868", kotikunta = Some(Kunta.helsinki),
    kuntahistoriaMock = h => {
      val historia = Seq(
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, Kunta.jyväskylä, Some(date(2018, 10, 1)), Some(date(2018, 11, 30))),
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, Kunta.pyhtää, Some(date(2018, 12, 1)), Some(date(2024, 11, 30))),
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, Kunta.helsinki, Some(date(2024, 12, 1)), None)
      )
      OppijanKuntahistoria(
        Some(h.henkilö.oid),
        historia,
        Seq.empty
      )
    }
  )

  val suomessaJaUlkomaillaVuorotellenAsunut = valpasOppijat.oppijaSyntymäaikaHetusta("SuomessaJaUlkomaillaVuorotellenAsunut", "Valpas", "210306A621J", kotikunta = Some(Kunta.helsinki),
    kuntahistoriaMock = h => {
      val historia = Seq(
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, Kunta.kotikuntaUlkomailla, Some(date(2016, 10, 1)), Some(date(2017, 9, 30))),
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, Kunta.jyväskylä, Some(date(2017, 10, 1)), Some(date(2018, 9, 30))),
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, Kunta.kotikuntaUlkomailla, Some(date(2018, 10, 1)), Some(date(2018, 11, 30))),
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, Kunta.pyhtää, Some(date(2018, 12, 1)), Some(date(2024, 11, 30))),
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, Kunta.helsinki, Some(date(2024, 12, 1)), None)
      )
      OppijanKuntahistoria(
        Some(h.henkilö.oid),
        historia,
        Seq.empty
      )
    }
  )

  val menehtynytOppija = valpasOppijat.oppijaSyntymäaikaHetusta("Menehtynyt", "Valpas", "161215A298F", kuolinpäivä = Some(LocalDate.of(2021, 3, 1)), kotikunta = None,
    kuntahistoriaMock = h => {
      val historia = Seq(
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, Kunta.helsinki, Some(LocalDate.of(2015, 12, 16)), None)
      )
      OppijanKuntahistoria(
        Some(h.henkilö.oid),
        historia,
        Seq.empty
      )
    })

  val menehtynytToisellaAsteellaOppija = valpasOppijat.oppijaSyntymäaikaHetusta("Menehtynyt-toisella-asteella", "Valpas", "030115A589L", kuolinpäivä = Some(LocalDate.of(2021, 3, 1)), kotikunta = None,
    kuntahistoriaMock = h => {
      val historia = Seq(
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, Kunta.helsinki, Some(LocalDate.of(2015, 11, 3)), None)
      )
      OppijanKuntahistoria(
        Some(h.henkilö.oid),
        historia,
        Seq.empty
      )
    })

  val eiKoskessaMenehtynytOppija = valpasOppijat.oppijaSyntymäaikaHetusta("Ei-Koskessa-menehtynyt", "Valpas", "260605A666C", kuolinpäivä = Some(LocalDate.of(2021, 3, 1)), kotikunta = None,
    kuntahistoriaMock = h => {
      val historia = Seq(
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, Kunta.helsinki, Some(LocalDate.of(2005, 6, 5)), None)
      )
      OppijanKuntahistoria(
        Some(h.henkilö.oid),
        historia,
        Seq.empty
      )
    })

  def defaultOppijat = valpasOppijat.getOppijat
  def defaultKuntahistoriat = valpasOppijat.getKuntahistoriat
  def defaultTurvakieltoKuntahistoriat = valpasOppijat.getTurvakieltoKuntahistoriat
}
