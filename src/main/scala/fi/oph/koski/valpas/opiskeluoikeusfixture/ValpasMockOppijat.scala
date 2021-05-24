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
  val kahdenKoulunYsiluokkalainen = valpasOppijat.oppija("KahdenKoulunYsi", "Valpas", "211104A0546")
  val lukionAineopinnotAloittanut = valpasOppijat.oppija("LukionAineopinnotAloittanut", "Valpas", "040305A559A")

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
