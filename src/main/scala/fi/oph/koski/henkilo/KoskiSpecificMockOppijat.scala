package fi.oph.koski.henkilo

import java.time.LocalDate
import fi.oph.koski.koskiuser.MockUsers
import fi.oph.koski.schema.UusiHenkilö

import scala.collection.mutable

object KoskiSpecificMockOppijat {
  private val koskiSpecificOppijat = new MockOppijat

  // Tällä oppijalla ei ole fixtuureissa opiskeluoikeuksia, eikä tätä lisätä henkilöpalveluun.
  val tyhjä = UusiHenkilö("230872-7258", "Tero", Some("Tero"), "Tyhjä")

  val hetuton = koskiSpecificOppijat.addLaajatOppijaHenkilöTiedot(LaajatOppijaHenkilöTiedot(oid = "1.2.246.562.24.99999999123", sukunimi = "Hetuton", etunimet = "Heikki", kutsumanimi = "Heikki", hetu = None, syntymäaika = Some(LocalDate.of(1977, 2, 24)), yksilöity = false))
  val syntymäajallinen = koskiSpecificOppijat.addLaajatOppijaHenkilöTiedot(LaajatOppijaHenkilöTiedot(oid = "1.2.246.562.24.99999999124", sukunimi = "Syntynyt", etunimet = "Sylvi", kutsumanimi = "Sylvi", hetu = Some("220627-833V"), syntymäaika = Some(LocalDate.of(1970, 1, 1))))
  val eero = koskiSpecificOppijat.oppijaSyntymäaikaHetusta("Esimerkki", "Eero", "010101-123N")
  val eerola = koskiSpecificOppijat.oppija("Çelik-Eerola", "Jouni", "081165-793C")
  val markkanen = koskiSpecificOppijat.oppija("Markkanen-Fagerström", "Eéro Jorma-Petteri", "080154-770R", syntymäaika = Some(LocalDate.of(1954, 1, 8)))
  val teija = koskiSpecificOppijat.oppija("Tekijä", "Teija", "251019-039B", kotikunta = Some("091"),
    kuntahistoriaMock = h => {
      val historia = Seq(
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, "178", Some(LocalDate.of(2024, 1, 1)), Some(LocalDate.of(2024, 6, 1)))
      )
      OppijanKuntahistoria(
        Some(h.henkilö.oid),
        historia,
        Seq.empty
      )
    }
  )
  val tero = koskiSpecificOppijat.oppija("Tunkkila-Fagerlund", "Tero Petteri Gustaf", "280608-6619")
  val presidentti = koskiSpecificOppijat.oppija("Presidentti", "Tasavallan", "")
  val koululainen = koskiSpecificOppijat.oppija("Koululainen", "Kaisa", "220109-784L")
  val suoritusTuplana = koskiSpecificOppijat.oppija("Tupla", "Toivo", "270298-533H")
  val luokallejäänyt = koskiSpecificOppijat.oppija("Luokallejäänyt", "Lasse", "170186-6520")
  val ysiluokkalainen = koskiSpecificOppijat.oppija("Ysiluokkalainen", "Ylermi", "160932-311V", kotikunta = Some("179"))
  val vuosiluokkalainen = koskiSpecificOppijat.oppija("Vuosiluokkalainen", "Ville", "010100-325X")
  val monessaKoulussaOllut = koskiSpecificOppijat.oppija("Monikoululainen", "Miia", "180497-112F", kotikunta = Some("091"),
    kuntahistoriaMock = h => {
      val historia = Seq(
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, "178", Some(LocalDate.of(2024, 1, 1)), Some(LocalDate.of(2024, 6, 1)))
      )
      OppijanKuntahistoria(
        Some(h.henkilö.oid),
        historia,
        Seq.empty
      )
    }
  )
  val lukiolainen = koskiSpecificOppijat.oppija("Lukiolainen", "Liisa", "020655-2479", kotikunta = Some("179"))
  val lukioKesken = koskiSpecificOppijat.oppija("Lukiokesken", "Leila", "190363-279X")
  val uusiLukio = koskiSpecificOppijat.oppija("Uusilukio", "Ulla", "250605A518Y")
  val uusiLukionAineopiskelija = koskiSpecificOppijat.oppija("Uusilukionaineopiskelija", "Urho", "010705A6119")
  val lukionAineopiskelija = koskiSpecificOppijat.oppija("Lukioaineopiskelija", "Aino", "210163-2367")
  val lukionAineopiskelijaAktiivinen = koskiSpecificOppijat.oppija("Lukioaineopiskelija", "Aktiivinen", "200300-624E")
  val lukionEiTiedossaAineopiskelija = koskiSpecificOppijat.oppija("Erkki", "Eitiedossa", "151132-746V")
  val ammattilainen = koskiSpecificOppijat.oppija("Ammattilainen", "Aarne", "280618-402H")
  val tutkinnonOsaaPienempiKokonaisuus = koskiSpecificOppijat.oppija("Pieni-Kokonaisuus", "Pentti", "040754-054W")
  val muuAmmatillinen = koskiSpecificOppijat.oppija("Muu-Ammatillinen", "Marjo", "130320-899Y")
  val muuAmmatillinenKokonaisuuksilla = koskiSpecificOppijat.oppija("Kokonaisuuksilla", "Keijo", "130174-452V")
  val ammatilliseenTetäväänValmistavaMuuAmmatillinen = koskiSpecificOppijat.oppija("Tehtävään-Valmistava", "Tauno", "050192-777U")
  val erkkiEiperusteissa = koskiSpecificOppijat.oppija("Eiperusteissa", "Erkki", "201137-361Y")
  val amis = koskiSpecificOppijat.oppija("Amis", "Antti", "211097-402L")
  val dippainssi = koskiSpecificOppijat.oppija("Dippainssi", "Dilbert", "100869-192W")
  val korkeakoululainen = koskiSpecificOppijat.oppija("Korkeakoululainen", "Kikka", "150113-4146")
  val amkValmistunut = koskiSpecificOppijat.oppija("Amis", "Valmis", "250686-102E", vanhaHetu = Some("250686-6493"))
  val opintojaksotSekaisin = koskiSpecificOppijat.oppija("Hassusti", "Opintojaksot", "090992-3237")
  val amkKesken = koskiSpecificOppijat.oppija("Amiskesken", "Jalmari", "090197-411W")
  val amkKeskeytynyt = koskiSpecificOppijat.oppija("Pudokas", "Valtteri", "170691-3962")
  val monimutkainenKorkeakoululainen = koskiSpecificOppijat.oppija("Korkeakoululainen", "Kompleksi", "060458-331R")
  val virtaEiVastaa = koskiSpecificOppijat.oppija("Virtanen", "Eivastaa", "250390-680P")
  val oppiaineenKorottaja = koskiSpecificOppijat.oppija("Oppiaineenkorottaja", "Olli", "110738-839L")
  val montaOppiaineenOppimäärääOpiskeluoikeudessa = koskiSpecificOppijat.oppija("Mervi", "Monioppiaineinen", "131298-5248")
  val virtaKaksiPäätösonSuoritusta = koskiSpecificOppijat.oppija("Kaksi-Päinen", "Ville", "270680-459P")
  val aikuisOpiskelija = koskiSpecificOppijat.oppija("Aikuisopiskelija", "Aini", "280598-2415", vanhaHetu = Some("280598-326W"))
  val aikuisOpiskelijaMuuKuinVos = koskiSpecificOppijat.oppija("Aikuinen", "AikuisopiskelijaMuuKuinVos", "241001B765F")
  val aikuisAineOpiskelijaMuuKuinVos = koskiSpecificOppijat.oppija("Aikuinen", "AikuisAineOpiskelijaMuuKuinVos", "241001B764E")
  val aikuisOpiskelijaVieraskielinen = koskiSpecificOppijat.oppija("Aikuinen", "Vieraskielinen", "241001B762C", äidinkieli = Some("en"))
  val aikuisOpiskelijaVieraskielinenMuuKuinVos = koskiSpecificOppijat.oppija("Aikuinen", "VieraskielinenMuuKuinVos", "241001B763D", äidinkieli = Some("en"))
  val aikuisOpiskelijaMuuRahoitus = koskiSpecificOppijat.oppija("Aikuinen", "MuuRahoitus", "241001C464S")
  val kymppiluokkalainen = koskiSpecificOppijat.oppija("Kymppiluokkalainen", "Kaisa", "131025-6573", sukupuoli = Some("2"), kotikunta = Some("Kontu"))
  val luva = koskiSpecificOppijat.oppija("Lukioonvalmistautuja", "Luke", "211007-442N")
  val luva2019 = koskiSpecificOppijat.oppija("Lukioonvalmistautuja2019", "Luke", "270926-380M")
  val valma = koskiSpecificOppijat.oppija("Amikseenvalmistautuja", "Anneli", "130404-054C")
  val ylioppilas = koskiSpecificOppijat.oppija("Ylioppilas", "Ynjevi", "210244-374K", vanhaHetu = Some("210244-073V"))
  val ylioppilasLukiolainen = koskiSpecificOppijat.oppija("Ylioppilaslukiolainen", "Ynjevi", "080698-703Y")
  val ylioppilasEiOppilaitosta = koskiSpecificOppijat.oppija("Ylioppilas", "Yrjänä", "240775-720P")
  val toimintaAlueittainOpiskelija = koskiSpecificOppijat.oppija("Toiminta", "Tommi", "031112-020J")
  val telma = koskiSpecificOppijat.oppija("Telmanen", "Tuula", "021080-725C")
  val erikoisammattitutkinto = koskiSpecificOppijat.oppija("Erikoinen", "Erja", "250989-419V")
  val reformitutkinto = koskiSpecificOppijat.oppija("Reformi", "Reijo", "251176-003P")
  val osittainenammattitutkinto = koskiSpecificOppijat.oppija("Osittainen", "Outi", "230297-6448")
  val ammatillisenOsittainenRapsa = koskiSpecificOppijat.oppija("Ammatillinen-Osittainen", "Raitsu", "140493-2798")
  val paikallinenTunnustettu = koskiSpecificOppijat.oppija("Tunnustettu", "Teuvo", "140176-449X")
  val tiedonsiirto = koskiSpecificOppijat.oppija("Tiedonsiirto", "Tiina", "270303-281N")
  val perusopetuksenTiedonsiirto = koskiSpecificOppijat.oppija("Perusopetuksensiirto", "Pertti", "010100-071R")
  val omattiedot = koskiSpecificOppijat.oppija(MockUsers.omattiedot.ldapUser.sukunimi, MockUsers.omattiedot.ldapUser.etunimet, "190751-739W", MockUsers.omattiedot.ldapUser.oid)
  val ibFinal = koskiSpecificOppijat.oppija("IB-final", "Iina", "040701-432D")
  val ibPredicted = koskiSpecificOppijat.oppija("IB-predicted", "Petteri", "071096-317K")
  val ibPreIB2019 = koskiSpecificOppijat.oppija("IB-Pre-IB-uusilukio", "Pate", "180300A8736")
  val dia = koskiSpecificOppijat.oppija("Dia", "Dia", "151013-2195")
  val internationalschool = koskiSpecificOppijat.oppija("International", "Ida", "170186-854H")
  val europeanSchoolOfHelsinki = koskiSpecificOppijat.oppija("Eurooppalainen", "Emilia", "050707A130V")
  val eskari = koskiSpecificOppijat.oppija("Eskari", "Essi", "300996-870E", kotikunta = Some("179"),
    kuntahistoriaMock = h => {
      val historia = Seq(
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, "178", Some(LocalDate.of(2024, 1, 1)), Some(LocalDate.of(2024, 6, 1)))
      )
      OppijanKuntahistoria(
        Some(h.henkilö.oid),
        historia,
        Seq.empty
      )
    }
  )
  val eskariAikaisillaLisätiedoilla = koskiSpecificOppijat.oppija("Lisä-Eskari", "Essiina", "300996-7419")
  val master = koskiSpecificOppijat.oppija("of Puppets", "Master", "101097-6107")
  val slave = koskiSpecificOppijat.addOppijaHenkilöWithMasterInfo(OppijaHenkilöWithMasterInfo(LaajatOppijaHenkilöTiedot(oid = "1.2.246.562.24.00000051473", sukunimi = "of Puppets", etunimet = "Slave", kutsumanimi = "Slave", hetu = Some("101097-6107"), syntymäaika = None), Some(master)))
  val masterEiKoskessa = koskiSpecificOppijat.addLaajatOppijaHenkilöTiedot(LaajatOppijaHenkilöTiedot(oid = koskiSpecificOppijat.generateId(), sukunimi = "Master", etunimet = "Master", kutsumanimi = "Master", hetu = Some("270366-697B"), syntymäaika = None))
  val slaveMasterEiKoskessa = koskiSpecificOppijat.addOppijaHenkilöWithMasterInfo(OppijaHenkilöWithMasterInfo(LaajatOppijaHenkilöTiedot(oid = "1.2.246.562.24.41000051473", hetu = Some("270366-697B"), syntymäaika = None, sukunimi = "Slave", etunimet = "Slave", kutsumanimi = "Slave"), Some(masterEiKoskessa)))
  val omattiedotSlave = koskiSpecificOppijat.addOppijaHenkilöWithMasterInfo(OppijaHenkilöWithMasterInfo(LaajatOppijaHenkilöTiedot(oid = koskiSpecificOppijat.generateId(), hetu = Some("190751-739W"), syntymäaika = None, etunimet = MockUsers.omattiedot.ldapUser.etunimet, kutsumanimi = MockUsers.omattiedot.ldapUser.etunimet, sukunimi = MockUsers.omattiedot.ldapUser.sukunimi), Some(omattiedot)))
  val opiskeluoikeudenOidKonflikti = koskiSpecificOppijat.oppija("Oidkonflikti", "Oskari", "260539-745W", "1.2.246.562.24.09090909090")
  val eiKoskessa = koskiSpecificOppijat.oppija("EiKoskessa", "Eino", "270181-5263", "1.2.246.562.24.99999555555", vanhaHetu = Some("270181-517T"))
  val eiKoskessaHetuton = koskiSpecificOppijat.addLaajatOppijaHenkilöTiedot(LaajatOppijaHenkilöTiedot(oid = "1.2.246.562.24.99999555556", sukunimi = "EiKoskessaHetuton", etunimet = "Eino", kutsumanimi = "Eino", hetu = None, syntymäaika = None))
  val turvakielto = koskiSpecificOppijat.oppija("Turvakielto", "Tero", "151067-2193", turvakielto = true,
    kuntahistoriaMock = h => {
      val historia = Seq(
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, "178", Some(LocalDate.of(2010, 1, 1)), Some(LocalDate.of(2024, 3, 1)))
      )
      val turvakieltohistoria = Seq(
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, "91", Some(LocalDate.of(2024, 3, 1)), None)
      )
      OppijanKuntahistoria(
        Some(h.henkilö.oid),
        historia,
        turvakieltohistoria
      )
    }
  )
  val montaJaksoaKorkeakoululainen = koskiSpecificOppijat.oppija("Korkeakoululainen", "Monta-Opintojaksoa", "030199-3419")
  val organisaatioHistoria = koskiSpecificOppijat.oppija("Historoitsija", "Hiisi", "200994-834A")
  val montaKoulutuskoodiaAmis = koskiSpecificOppijat.oppija("Koodari", "Monthy", "151099-036E")
  val tunnisteenKoodiarvoPoistettu = koskiSpecificOppijat.oppija("ePerusteidenKoulutuksen-koodi", "Poistettu", "161097-132N")
  val valtuutusOppija = koskiSpecificOppijat.oppija("Sydänmaanlakka-Horttanainen", "Katariina Eeva Marjatta", "020190-9521", kutsumanimi = Some("Katariina"))
  val siirtoOpiskelijaVirta = koskiSpecificOppijat.oppija("SiirtoOpiskelijaVirta", "Siiri", "141199-418X")
  val faija = koskiSpecificOppijat.oppija("EiOpintojaKoskessa", "Faija", "030300-5215")
  val faijaFeilaa = koskiSpecificOppijat.oppija("EiOpintojaKoskessaLastenHakuFailaa", "Faija", "030300-7053")
  val koulusivistyskieliYlioppilas = koskiSpecificOppijat.oppija("Koulu", "SivistysKieli", "020401-368M")
  val montaKoulusivityskieltäYlioppilas = koskiSpecificOppijat.oppija("MontaKoulu", "SivistysKieltä", "020401-746U")
  val labammattikoulu = koskiSpecificOppijat.oppija("Lahti", "LAB", "260308-361W")
  val valviraaKiinnostavaTutkinto = koskiSpecificOppijat.oppija("Valviralle", "Veera", "120100A2365")
  val valviraaKiinnostavaTutkintoKesken = koskiSpecificOppijat.oppija("Valviralle-Kesken", "Ville", "131099-633D")
  val kelaErityyppisiaOpiskeluoikeuksia = koskiSpecificOppijat.oppija("Kelalle", "Useita", "100800A057R")
  val lukioDiaIbInternationalESHOpiskelijamaaratRaportti_nuortenOppimaara = koskiSpecificOppijat.oppija("nuorten", "oppimaara", "180900A715X")
  val lukioDiaIbInternationalESHOpiskelijamaaratRaportti_aikuistenOppimaara = koskiSpecificOppijat.oppija("aikuisten", "oppimaara", "180900A6638")
  val lukioDiaIbInternationalESHOpiskelijamaaratRaportti_aineopiskelija = koskiSpecificOppijat.oppija("aine", "opiskelija", "180900A115K")
  val lukioDiaIbInternationalESHOpiskelijamaaratRaportti_dia = koskiSpecificOppijat.oppija("dia", "opiskelija", "180900A7790")
  val lukioDiaIbInternationalESHOpiskelijamaaratRaportti_ib = koskiSpecificOppijat.oppija("ib", "opiskelija", "180900A2553")
  val lukioDiaIbInternationalESHOpiskelijamaaratRaportti_international = koskiSpecificOppijat.oppija("international", "opiskelija", "180900A885D")
  val lukioDiaIbInternationalESHOpiskelijamaaratRaportti_esh = koskiSpecificOppijat.oppija("esh", "opiskelija", "081100A1241")
  val perusopetusOppijaMaaratRaportti_tavallinen = koskiSpecificOppijat.oppija("t", "tavallinen", "241001A8751")
  val perusopetusOppijaMaaratRaportti_erikois = koskiSpecificOppijat.oppija("e", "erikois", "180900A2298")
  val perusopetusOppijaMaaratRaportti_virheellisestiSiirretty = koskiSpecificOppijat.oppija("v", "virheellisestiSiirretty", "050501A093H")
  val perusopetusOppijaMaaratRaportti_virheellisestiSiirrettyVieraskielinen = koskiSpecificOppijat.oppija("v", "virheellisestiSiirrettyVieraskielinen", "131100A355P", äidinkieli = Some("en"))
  val perusopetusOppijaMaaratRaportti_kotiopetus = koskiSpecificOppijat.oppija("k", "kotiopetus", "190371-103A")
  val organisaatioHistoriallinen = koskiSpecificOppijat.oppija("o", "organisaatioHistoriallinen", "210728-156E")
  val lukioKurssikertymaRaportti_oppimaara = koskiSpecificOppijat.oppija("Kurssikertyma", "Oppimaara", "280900A787P")
  val lukioKurssikertymaRaportti_aineopiskelija_eronnut = koskiSpecificOppijat.oppija("Kurssikertyma", "Eronnut Aineopiskelija", "280900A323R")
  val lukioKurssikertymaRaportti_aineopiskelija_valmistunut = koskiSpecificOppijat.oppija("Kurssikertyma", "Valmistunut Aineopiskelija", "140802A010A")
  val luvaOpiskelijamaaratRaportti_nuortenOppimaara = koskiSpecificOppijat.oppija("Luva", "Nuorten", "300900A869M", kotikunta = Some("035"))
  val luvaOpiskelijamaaratRaportti_aikuistenOppimaara = koskiSpecificOppijat.oppija("Luva", "Aikuisten", "300900A099T")
  val paallekkaisiOpiskeluoikeuksia = koskiSpecificOppijat.oppija("Paallekkaisia", "Pekka", "210400A021E")
  val vapaaSivistystyöOppivelvollinen = koskiSpecificOppijat.oppija("Vapaa-Sivistys", "Oppivelvollinen", "080177-870W")
  val vapaaSivistystyöMaahanmuuttajienKotoutus = koskiSpecificOppijat.oppija("Vapaa-Sivistys", "Kotoutuja", "260769-598H")
  val vapaaSivistystyöLukutaitoKoulutus = koskiSpecificOppijat.oppija("Vapaa-Sivistys", "Lukutaitokouluttautuja", "231158-467R")
  val vapaaSivistystyöVapaatavoitteinenKoulutus = koskiSpecificOppijat.oppija("Vapaa-Sivistys", "Vapaatavoitteinen-Kouluttautuja", "010917-156A")
  val oikeusOpiskelunMaksuttomuuteen = koskiSpecificOppijat.oppija("Oikeus", "Maksuttomuuteen", "010104A6094", syntymäaika = Some(LocalDate.of(2004, 12, 31)), kotikunta = Some("091"))
  val eiOikeuttaMaksuttomuuteen = koskiSpecificOppijat.oppija("EiOikeutta", "Maksuttomuuteen", "311203A1454", syntymäaika = Some(LocalDate.of(2003, 12, 31)), kotikunta = Some("091"))
  val etk18vSyntynytKesäkuunEnsimmäisenäPäivänä = koskiSpecificOppijat.oppija("Nopea", "Nina", "010698-6646", syntymäaika = Some(LocalDate.of(1998, 6, 1)))
  val etk18vSyntynytToukokuunViimeisenäPäivänä = koskiSpecificOppijat.oppija("Nopea", "Noa", "310598-4959", syntymäaika = Some(LocalDate.of(1998, 5, 31)))
  val oppivelvollisuustietoLiianVanha = koskiSpecificOppijat.oppija("Oppivelvollisuustieto", "LiianVanha", "311203A245B", syntymäaika = Some(LocalDate.of(2003, 12, 31)), kotikunta = Some("091"))
  val oppivelvollisuustietoMaster = koskiSpecificOppijat.oppija("Oppivelvollisuustieto", "Master", "260904A350B", syntymäaika = Some(LocalDate.of(2004, 1, 1)), kotikunta = Some("091"))
  val oppivelvollisuustietoSlave1 = koskiSpecificOppijat.addOppijaHenkilöWithMasterInfo(OppijaHenkilöWithMasterInfo(LaajatOppijaHenkilöTiedot(oid = "1.2.246.562.24.00000051491", sukunimi = "Oppivelvollisuustieto", etunimet = "Slave1", kutsumanimi = "Slave1", hetu = Some("260904A350B"), syntymäaika = Some(LocalDate.of(2005, 1, 1)), kotikunta = Some("091")), Some(oppivelvollisuustietoMaster)))
  val oppivelvollisuustietoSlave2 = koskiSpecificOppijat.addOppijaHenkilöWithMasterInfo(OppijaHenkilöWithMasterInfo(LaajatOppijaHenkilöTiedot(oid = "1.2.246.562.24.00000051492", sukunimi = "Oppivelvollisuustieto", etunimet = "Slave2", kutsumanimi = "Slave2", hetu = Some("260904A350B"), syntymäaika = Some(LocalDate.of(2004, 1, 1)), kotikunta = Some("091")), Some(oppivelvollisuustietoMaster)))
  val maksuttomuuttaPidennetty1 = koskiSpecificOppijat.oppija("Pidennetty1", "Maksuttomuutta1", "190525-0401", syntymäaika = Some(LocalDate.of(2004, 1, 1)), kotikunta = Some("091"))
  val maksuttomuuttaPidennetty2 = koskiSpecificOppijat.oppija("Pidennetty2", "Maksuttomuutta2", "220835-2325", syntymäaika = Some(LocalDate.of(2004, 1, 1)), kotikunta = Some("091"))
  val vuonna2004SyntynytPeruskouluValmis2021 = koskiSpecificOppijat.oppija("Vuonna 2004 syntynyt", "Peruskoulu suoritettu 2021", "010104A153D", syntymäaika = Some(LocalDate.of(2004, 1, 1)), kotikunta = Some("091"),
    kuntahistoriaMock = h => {
      val historia = Seq(
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, "091", Some(LocalDate.of(2010, 1, 1)), None)
      )
      OppijanKuntahistoria(
        Some(h.henkilö.oid),
        historia,
        Seq.empty
      )
    }
  )
  val vuonna2004SyntynytMuttaPeruskouluValmisEnnen2021 = koskiSpecificOppijat.oppija("Vuonna 2004 syntynyt", "Peruskoulu suoritettu ennen 2021", "010104A811M", syntymäaika = Some(LocalDate.of(2004, 1, 1)), kotikunta = Some("091"),
    kuntahistoriaMock = h => {
      val historia = Seq(
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, "091", Some(LocalDate.of(2010, 1, 1)), None)
      )
      OppijanKuntahistoria(
        Some(h.henkilö.oid),
        historia,
        Seq.empty
      )
    }
  )
  val vuonna2005SyntynytEiOpiskeluoikeuksiaFikstuurissa = koskiSpecificOppijat.oppija("Vuonna 2005 syntynyt", "Ei opiskeluoikeuksia fikstuurissa", "030705A5719", syntymäaika = Some(LocalDate.of(2005, 7, 3)), kotikunta = Some("091"))
  val nuoriHetuton = koskiSpecificOppijat.addLaajatOppijaHenkilöTiedot(LaajatOppijaHenkilöTiedot(oid = "1.2.246.562.24.99999999888", sukunimi = "Hetuton", etunimet = "Veeti", kutsumanimi = "Veeti", hetu = None, syntymäaika = Some(LocalDate.of(2004, 2, 24)), yksilöity = false))
  val vuonna2005SyntynytUlkomainenVaihtoopiskelija = koskiSpecificOppijat.addLaajatOppijaHenkilöTiedot(LaajatOppijaHenkilöTiedot(oid = "1.2.246.562.24.55599999333", sukunimi = "Vaihto-opiskelija", etunimet = "Valter", kutsumanimi = "Valter", hetu = None, syntymäaika = Some(LocalDate.of(2005, 2, 24))))
  val vuonna2005SyntynytPeruskouluValmis2021 = koskiSpecificOppijat.oppija("Vuonna 2005 syntynyt", "Peruskoulu suoritettu 2021", "251105A2755", syntymäaika = Some(LocalDate.of(2005, 1, 1)), kotikunta = Some("091"))
  val rikkinäinenOpiskeluoikeus = koskiSpecificOppijat.oppija("Rikkinäinen", "Opiskeluoikeus", "140615-7608", syntymäaika = Some(LocalDate.of(2003, 12, 31)))
  val kelaRikkinäinenOpiskeluoikeus = koskiSpecificOppijat.oppija("Rikkinäinen", "Kela", "111104A855K", syntymäaika = Some(LocalDate.of(2004, 11, 11)))
  val vuonna2004SyntynytPeruskouluValmis2021EiKotikuntaaSuomessa = koskiSpecificOppijat.oppija("Vuonna 2004 syntynyt maastamuuttaja", "Peruskoulu suoritettu 2021", "030904A251A", syntymäaika = Some(LocalDate.of(2004, 1, 1)))
  val vuonna2004SyntynytPeruskouluValmis2021KotikuntaAhvenanmaalla = koskiSpecificOppijat.oppija("Vuonna 2004 syntynyt ahvenanmaalle muuttanut", "Peruskoulu suoritettu 2021", "070504A773H", syntymäaika = Some(LocalDate.of(2004, 1, 1)), kotikunta = Some("478"))
  val vuonna2004SyntynytMuttaEronnutPeruskoulustaEnnen2021 = koskiSpecificOppijat.oppija("Vuonna 2004 syntynyt", "Peruskoulusta eronnut ennen 2021", "100504A476V", syntymäaika = Some(LocalDate.of(2004, 5, 10)), kotikunta = Some("091"))
  val opiskeleeAmmatillisessaErityisoppilaitoksessa = koskiSpecificOppijat.oppija("Erityisoppilaitoksessa", "Emppu", "210205A835C", syntymäaika = Some(LocalDate.of(2005, 2, 21)), kotikunta = Some("091"))
  val opiskeleeAmmatillisessaErityisoppilaitoksessaOrganisaatioHistoriallinen = koskiSpecificOppijat.oppija("Erityisoppilaitoksessa", "Emppu Historia", "210905A671R", syntymäaika = Some(LocalDate.of(2005, 9, 5)), kotikunta = Some("091"))
  val tuva = koskiSpecificOppijat.oppija(suku = "Vaara", etu = "Turo", hetu = "270504A317P", syntymäaika = Some(LocalDate.of(2004, 5, 27)))
  val tuvaPerus = koskiSpecificOppijat.oppija(suku = "Vaara", etu = "Tuula", hetu = "040904A578H", syntymäaika = Some(LocalDate.of(2004, 9, 4)))
  val tuvaLoma = koskiSpecificOppijat.oppija(suku = "Lomailija", etu = "Tuva", hetu = "071018-767Y", syntymäaika = Some(LocalDate.of(2018, 10, 7)))
  val poistettuOpiskeluoikeus = koskiSpecificOppijat.oppija(suku = "Poistettu", etu = "Opiskeluoikeus", hetu = "140408-440H")
  val hakkeri = koskiSpecificOppijat.oppija(suku = "Hakkeri</script><script>alert(1);", etu = "Paha", hetu = "110100A124W")
  val nuortenPerusopetuksenOppimääräErityinenTutkinto = koskiSpecificOppijat.oppija(suku = "ErityinenTutkinto", etu = "NuortenPerusopetus", hetu = "060675-2471")
  val vstKoto2022Aloittaja = koskiSpecificOppijat.oppija(suku = "Kotolainen-Alkujuuri", etu = "Vesti", hetu = "060806A528W")
  val vstKoto2022Kesken = koskiSpecificOppijat.oppija(suku = "Kotonen-Keskinen", etu = "Vesti", hetu = "120706A8456")
  val vstKoto2022Suorittanut = koskiSpecificOppijat.oppija(suku = "Kotinen-Suorsalo", etu = "Vesti", hetu = "270606A1355")
  val ajoneuvoalanOpiskelija = koskiSpecificOppijat.oppija(suku = "Autonen", etu = "Antti", hetu = "030402A6975")
  val eiKoskessaOppivelvollinen = koskiSpecificOppijat.oppija("EiKoskessa", "Erno-oppivelvollinen", "151105A082F")
  val jotpaAmmattikoululainen = koskiSpecificOppijat.oppija("Jotpanen", "Amis", "230600A2358")
  val vstJotpaKeskenOppija = koskiSpecificOppijat.oppija("Jotpanen", "Keskiö", "030200A005P")
  val jotpaMuuKuinSäännelty = koskiSpecificOppijat.oppija("Jotpanen", "Muksu", "200600A515B")
  val taiteenPerusopetusAloitettu = koskiSpecificOppijat.oppija("Taiteilija", "Petra", "230405A067H")
  val taiteenPerusopetusValmis = koskiSpecificOppijat.oppija("Taiteilija", "Pentti", "110505A804B")
  val taiteenPerusopetusHankintakoulutus = koskiSpecificOppijat.oppija("Taiteilija", "Hank", "120505A794N")
  val ylioppilasUusiApi = koskiSpecificOppijat.oppija("Ylioppilas-uusi", "Yölevi", "080380-2432")
  val ylioppilasUusiApi2 = koskiSpecificOppijat.oppija("Ylioppilas-uusi", "Yrmiina", "140380-336X")
  val amiksenKorottaja = koskiSpecificOppijat.oppija("Korhonen", "Korottaja", "190705A0338")
  val opiskeluoikeudenOidKonflikti2 = koskiSpecificOppijat.oppija("Oidkonflikti", "Onneli", "281197-110H", "1.2.246.562.24.09090909091")
  val ylioppilasLukiolainenMaksamatonSuoritus = koskiSpecificOppijat.oppija("Maksamaa-Toikkarinen", "Matti", "101000A3582")
  val ylioppilasLukiolainenVanhaSuoritus = koskiSpecificOppijat.oppija("Vanhanen-Toikkarinen", "Vanja", "190580-678T")
  val ylioppilasLukiolainenTimeouttaava = koskiSpecificOppijat.oppija("Outinen-Toikkarinen", "Taimi", "270900A2635")
  val ylioppilasLukiolainenRikki = koskiSpecificOppijat.oppija("Rikko-Toikkarinen", "Risto", "280100A855E", kotikunta = Some("091"),
    kuntahistoriaMock = h => {
      val historia = Seq(
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, "178", Some(LocalDate.of(2024, 1, 1)), Some(LocalDate.of(2024, 6, 1)))
      )
      OppijanKuntahistoria(
        Some(h.henkilö.oid),
        historia,
        Seq.empty
      )
    }
  )
  val amisKoulutusvienti = koskiSpecificOppijat.oppija("Koulutusvientiläinen", "Amis", "020605A3368")
  val opiskeleeAmmatillisessaErityisoppilaitoksessa2 = koskiSpecificOppijat.oppija("Erityisoppilaitoksessa", "Emppu", "211005A867V", syntymäaika = Some(LocalDate.of(2005, 2, 21)), kotikunta = Some("091"))
  val vanhanMallinenIBOppija = koskiSpecificOppijat.oppija("Ibe", "Vanhanen", "101000A684K")
  val ylioppilasEiValmistunut = koskiSpecificOppijat.oppija("Yliniemi", "Kalevi", "120674-064R")
  val ammattilainenVahvistettuTulevaisuudessa = koskiSpecificOppijat.oppija("Tuleva-ammattilainen", "Tuure", "280128-6867")
  val masterYlioppilasJaAmmattilainen = koskiSpecificOppijat.oppija("Mastinen", "Maarni", "151031-620R")
  val slaveAmmattilainen = koskiSpecificOppijat.addOppijaHenkilöWithMasterInfo(OppijaHenkilöWithMasterInfo(LaajatOppijaHenkilöTiedot(oid = "1.2.246.562.24.00000057489", sukunimi = "Slavinen", etunimet = "Saarni", kutsumanimi = "Ammi", hetu = Some("140661-592P"), syntymäaika = None), Some(masterYlioppilasJaAmmattilainen)))
  val ammatilliseenTetäväänValmistavaMuuAmmatillinenVahvistettu = koskiSpecificOppijat.oppija("Tehtävään-Valmistava-vahvistettu", "Tauno", "120418-275F")
  val jotpaMuuKuinSäänneltySuoritettu = koskiSpecificOppijat.oppija("Suorituinen", "Muksu", "150100A254N")
  val pelkkäYoKannassaUudenOvLainPiirissä = koskiSpecificOppijat.oppija("YO-opiskeluoikeus", "Valpas", "060807A7787", syntymäaika = Some(LocalDate.of(2007, 8, 6)), kotikunta = Some("091"))
  val lukioVajaaSuoritus = koskiSpecificOppijat.oppija("Vajaa-Suoritus", "Lukiolainen", "010105A1995")
  val deprecatedEuropeanSchoolOfHelsinki = koskiSpecificOppijat.oppija("Deprecated-Eurooppalainen", "Emma", "171005A010M")
  val pelkkäESH = koskiSpecificOppijat.oppija("Eurooppalainen", "Eeva", "100906A5544")
  val vapaaSivistystyöOsaamismerkki = koskiSpecificOppijat.oppija("Vapaa-Sivistys", "Osaamismerkki", "050705A564B")
  val moniaEriOpiskeluoikeuksia = koskiSpecificOppijat.oppija("Monia", "Useita", "230108A744P")
  val suomeenTäysiikäisenäMuuttanut = koskiSpecificOppijat.oppija("Jenkins", "Jenny", "010106A1328", kotikunta = Some("091"),
    kuntahistoriaMock = h => {
      val historia = Seq(
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, "200", Some(LocalDate.of(2006, 1, 1)), Some(LocalDate.of(2024, 3, 1))),
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, "091", Some(LocalDate.of(2024, 3, 1)), None),
      )
      OppijanKuntahistoria(
        Some(h.henkilö.oid),
        historia,
        Seq.empty
      )
    }
  )
  val suomeenAlaikäisenäMuuttanut = koskiSpecificOppijat.oppija("Anderson", "Andrew", "010106A8159", kotikunta = Some("091"),
    kuntahistoriaMock = h => {
    val historia = Seq(
      OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, "200", None,                           Some(LocalDate.of(2024, 3, 1))),
      OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, "091", Some(LocalDate.of(2022, 3, 1)), None),
    )
    OppijanKuntahistoria(
      Some(h.henkilö.oid),
      historia,
      Seq.empty
    )
  }
  )
  val ulkomaillaHetkenAsunut = koskiSpecificOppijat.oppija("Matkailija", "Matilda", "010106A604F", kotikunta = Some("091"),
    kuntahistoriaMock = h => {
      val historia = Seq(
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, "091", Some(LocalDate.of(2006, 1, 1)), Some(LocalDate.of(2024, 3, 1))),
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, "200", Some(LocalDate.of(2022, 3, 1)), Some(LocalDate.of(2024, 3, 1))),
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, "091", Some(LocalDate.of(2024, 3, 1)), None)
      )
      OppijanKuntahistoria(
        Some(h.henkilö.oid),
        historia,
        Seq.empty
      )
    }
  )
  val suomeenAhvenanmaaltaTäysiikäisenäMuuttanut = koskiSpecificOppijat.oppija("Anderson", "Anders", "010106A0398", kotikunta = Some("091"),
    kuntahistoriaMock = h => {
      val historia = Seq(
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, "170", Some(LocalDate.of(2006, 1, 1)), Some(LocalDate.of(2024, 3, 1))),
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, "478", Some(LocalDate.of(2010, 1, 1)), Some(LocalDate.of(2024, 3, 1))),
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, "091", Some(LocalDate.of(2024, 3, 1)), None)
      )
      OppijanKuntahistoria(
        Some(h.henkilö.oid),
        historia,
        Seq.empty
      )
    }
  )
  val vapaaSivistystyöMaksuttomuus = koskiSpecificOppijat.oppija("VST", "Maksuttomuus", "311204A059L", syntymäaika = Some(LocalDate.of(2004, 12, 31)), kotikunta = Some("091"))
  val vuonna2003SyntynytPeruskouluValmis2021 = koskiSpecificOppijat.oppija("Vuonna 2003 syntynyt", "Peruskoulu suoritettu 2021", "010103A3373", syntymäaika = Some(LocalDate.of(2003, 1, 1)), kotikunta = Some("091"),
    kuntahistoriaMock = h => {
      val historia = Seq(
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, "091", Some(LocalDate.of(2010, 1, 1)), None)
      )
      OppijanKuntahistoria(
        Some(h.henkilö.oid),
        historia,
        Seq.empty
      )
    }
  )
  val vuonna2004SyntynytPeruskouluValmis2021MuuttanutSuomeenTäysiIkäisenä = koskiSpecificOppijat.oppija("Vuonna 2004 syntynyt (muuttanut Suomeen täysi-ikäisenä)", "Peruskoulu suoritettu 2021", "010104A3304", syntymäaika = Some(LocalDate.of(2004, 1, 1)), kotikunta = Some("091"),
    kuntahistoriaMock = h => {
      val historia = Seq(
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, "478", Some(LocalDate.of(2004, 1, 1)), Some(LocalDate.of(2024, 2, 1))),
        OppijanumerorekisteriKotikuntahistoriaRow(h.henkilö.oid, "091", Some(LocalDate.of(2024, 2, 1)), None)
      )
      OppijanKuntahistoria(
        Some(h.henkilö.oid),
        historia,
        Seq.empty
      )
    }
  )
  val vuonna2004SyntynytPeruskouluValmis2021EiKotikuntahistoriaa = koskiSpecificOppijat.oppija(
    "Vuonna 2004 syntynyt (ei kotikuntahistoriaa)", "Peruskoulu suoritettu 2021", "010104A227T",
    syntymäaika = Some(LocalDate.of(2004, 1, 1)),
    kotikunta = Some("091"),
    kuntahistoriaMock = MockOppijat.kuntahistoriaTyhjä _
  )
  val vainMitätöityjäOpiskeluoikeuksia = koskiSpecificOppijat.oppija("Mitätön", "Minna", "010106A492V", syntymäaika = Some(LocalDate.of(2006, 1, 1)), kotikunta = Some("091"))
  val lahdejarjestelmanPurku = koskiSpecificOppijat.oppija("Purettava", "Lasse", "010106A013D", syntymäaika = Some(LocalDate.of(2006, 1, 1)), kotikunta = Some("091"))
  val rikkinäinenPerusopetus = koskiSpecificOppijat.oppija("Rikkinäinen", "Perusopetus", "311203A729Y", syntymäaika = Some(LocalDate.of(2003, 12, 31)))
  val kielitutkinnonSuorittaja = koskiSpecificOppijat.oppija("Suorittaja", "Kielitutkinto", "010107A329V", syntymäaika = Some(LocalDate.of(2007, 1, 1)))
  val keskeneräinenIbTutkinto = koskiSpecificOppijat.oppija("Keskeneräinen", "IB-Tutkinto", "010107A0938")

  //Jos luot uuden oppijan voi hetun generoida täältä: http://www.lintukoto.net/muut/henkilotunnus/index.php
  //Huomaa, että hetun pitää olla oikean kaltainen

  val virtaOppija = koskiSpecificOppijat.addLaajatOppijaHenkilöTiedot(LaajatOppijaHenkilöTiedot(oid = "1.2.246.562.24.57060795845", sukunimi = "Virta", etunimet = "Veikko", kutsumanimi = "Veikko", hetu = Some("270191-4208"), syntymäaika = Some(LocalDate.of(1978, 3, 25)), äidinkieli = None, kansalaisuus = None))
  val virtaOppijaHetuton = koskiSpecificOppijat.addOppijaHenkilöWithMasterInfo(OppijaHenkilöWithMasterInfo(
    LaajatOppijaHenkilöTiedot(oid = "1.2.246.562.24.20170814313", sukunimi = "Virta", etunimet = "Veikko", kutsumanimi = "Veikko", hetu = None, syntymäaika = Some(LocalDate.of(1978, 3, 25)), äidinkieli = None, kansalaisuus = None),
    Some(virtaOppija)))

  val virtaOppijaValiaikainenHetu = koskiSpecificOppijat.addLaajatOppijaHenkilöTiedot(LaajatOppijaHenkilöTiedot(oid = "1.2.246.562.24.20170814999", sukunimi = "Hetu", etunimet = "Valiaikainen", kutsumanimi = "Vahetu", hetu = Some("010469-999W"), syntymäaika = Some(LocalDate.of(1969, 4, 1)), äidinkieli = None, kansalaisuus = None))
  val virtaOpiskeluoikeuksiaSamallaAvaimella = koskiSpecificOppijat.oppija("Opiskeluoikeuksia-samalla-avaimella", "Korkeakoululainen", "130505A831H")

  val pelkkäYo2021 = koskiSpecificOppijat.oppija("Pelkkä YO", "Koski", "300805A847D", syntymäaika = Some(LocalDate.of(2005,8,30)),kotikunta = Some("091"))
  val esikoululainen2025 = koskiSpecificOppijat.oppija("Esikoululainen", "Essi", "171118A6061")

  def defaultOppijat = koskiSpecificOppijat.getOppijat
  def defaultKuntahistoriat: mutable.Map[String, Seq[OppijanumerorekisteriKotikuntahistoriaRow]] = koskiSpecificOppijat.getKuntahistoriat
  def defaultTurvakieltoKuntahistoriat: mutable.Map[String, Seq[OppijanumerorekisteriKotikuntahistoriaRow]] = koskiSpecificOppijat.getTurvakieltoKuntahistoriat
}
