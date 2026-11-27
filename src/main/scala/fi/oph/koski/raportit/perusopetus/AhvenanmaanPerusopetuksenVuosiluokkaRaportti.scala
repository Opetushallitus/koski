package fi.oph.koski.raportit.perusopetus

import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.raportit.{Column, CompactColumn}
import fi.oph.koski.raportointikanta.ROsasuoritusRow
import fi.oph.koski.schema.Aikajakso
import fi.oph.koski.schema.Organisaatio.Oid

import java.time.LocalDate

object AhvenanmaanPerusopetuksenVuosiluokkaRaportti extends PerusopetuksenVuosiluokkaraporttiPohja {
  val tyypit: VuosiluokkaraportinTyypit = VuosiluokkaraportinTyypit.ahvenanmaanPerusopetus

  protected val otsikonEtuliiteAvain = "raportti-excel-ahvenanmaan-perusopetus-title-etuliite"
  protected val tiedostonEtuliiteAvain = "raportti-excel-ahvenanmaan-perusopetus-tiedoston-etuliite"
  protected val dokumentaatioAvain = "raportti-excel-ahvenanmaan-perusopetus-dokumentaatio"

  private val oppiainekoodisto = "ahvenanmaankoskioppiaineetyleissivistava"
  private val toimintaAlueenSuorituksenTyyppi = "ahvenanmaanperusopetuksentoimintaalue"

  def buildRaportti(
    repository: PerusopetuksenRaportitRepository,
    oppilaitosOids: Seq[Oid],
    paiva: LocalDate,
    kotikuntaPvm: Option[LocalDate],
    vuosiluokka: String,
    t: LocalizationReader
  ): Seq[AhvenanmaanPerusopetusRow] =
    haeRivit(repository, oppilaitosOids, paiva, kotikuntaPvm, vuosiluokka, t).map(buildRow(_, paiva, kotikuntaPvm, t))

  private def buildRow(row: PerusopetuksenRaporttiRows, hakupaiva: LocalDate, kotikuntaPvm: Option[LocalDate], t: LocalizationReader) = {
    val kotiopetusjaksot = JsonSerializer.extract[Option[List[Aikajakso]]](row.opiskeluoikeus.data \ "lisätiedot" \ "kotiopetusjaksot")
    val lähdejärjestelmä = lähdejärjestelmänId(row)
    val (toimintaAlueet, oppiaineet) = row.osasuoritukset.partition(_.suorituksenTyyppi == toimintaAlueenSuorituksenTyyppi)
    val (pakolliset, valinnaiset) = oppiaineet.partition(isPakollinen)
    val (pakollisetValtakunnalliset, pakollisetPaikalliset) = pakolliset.partition(isValtakunnallinen)
    val (valinnaisetValtakunnalliset, valinnaisetPaikalliset) = valinnaiset.partition(isValtakunnallinen)

    def arvosana(koodiarvo: String) = yhteisenOppiaineenArvosanat(pakollisetValtakunnalliset, koodiarvo, t)
    def kieli(koodiarvo: String) = yhteisenOppiaineenKielet(pakollisetValtakunnalliset, koodiarvo, t)

    AhvenanmaanPerusopetusRow(
      opiskeluoikeusOid = row.opiskeluoikeus.opiskeluoikeusOid,
      oppilaitoksenNimi = oppilaitoksenNimi(row, t),
      oppilaitosRaportointipäivänä = oppilaitosRaportointipäivänä(row),
      lähdejärjestelmä = lähdejärjestelmä.map(_.lähdejärjestelmä.koodiarvo),
      lähdejärjestelmänId = lähdejärjestelmä.flatMap(_.id),
      päivitetty = row.opiskeluoikeus.aikaleima.toLocalDateTime.toLocalDate,
      yksiloity = row.henkilo.yksiloity,
      oppijaOid = row.opiskeluoikeus.oppijaOid,
      oppijaMasterOid = row.opiskeluoikeus.oppijaMasterOid,
      hetu = row.henkilo.hetu,
      sukunimi = row.henkilo.sukunimi,
      etunimet = row.henkilo.etunimet,
      sukupuoli = row.henkilo.sukupuoli,
      kotikunta = kotikunta(row, kotikuntaPvm, t),
      opiskeluoikeudenAlkamispäivä = row.opiskeluoikeus.alkamispäivä.map(_.toLocalDate),
      viimeisinTila = row.opiskeluoikeus.viimeisinTila.map(t.koodiarvo("koskiopiskeluoikeudentila", _)).getOrElse(""),
      tilaHakupaivalla = t.koodiarvo("koskiopiskeluoikeudentila", row.aikajaksot.last.tila),
      suorituksenTila = suorituksenTila(row, t),
      suorituksenAlkamispaiva = suorituksenAlkamispäivä(row),
      suorituksenVahvistuspaiva = suorituksenVahvistuspäivä(row),
      jaaLuokalle = jääLuokalle(row),
      luokka = row.luokka,
      voimassaolevatVuosiluokat = row.voimassaolevatVuosiluokat.mkString(","),
      ruotsi = arvosana("SV"),
      ruotsiToisenaKielena = arvosana("SVA"),
      kieliA1 = arvosana("A1"),
      kieliA1Oppimaara = kieli("A1"),
      kieliA2 = arvosana("A2"),
      kieliA2Oppimaara = kieli("A2"),
      kieliB1 = arvosana("B1"),
      kieliB1Oppimaara = kieli("B1"),
      kieliB2 = arvosana("B2"),
      kieliB2Oppimaara = kieli("B2"),
      matematiikka = arvosana("MA"),
      ymparistooppi = arvosana("OM"),
      biologia = arvosana("BI"),
      maantieto = arvosana("GE"),
      fysiikkaJaKemia = arvosana("FYKE"),
      fysiikka = arvosana("FY"),
      kemia = arvosana("KE"),
      terveystieto = arvosana("TE"),
      uskontoJaElamankatsomustieto = arvosana("RELI"),
      historia = arvosana("HI"),
      musiikki = arvosana("MU"),
      yhteiskuntaoppi = arvosana("SA"),
      kuvataide = arvosana("KU"),
      kasityo = arvosana("KS"),
      tekstiilityo = arvosana("TX"),
      tekninenTyo = arvosana("TN"),
      liikunta = arvosana("ID"),
      kotitalous = arvosana("HEKO"),
      oppilaanohjaus = arvosana("EH"),
      vastuuJaYhteistyoArvio = JsonSerializer.extract[Option[String]](row.päätasonSuoritus.data \ "vastuuJaYhteistyöArvio" \ "arvosana" \ "koodiarvo").getOrElse(""),
      mukautetutYhteisetOppiaineet = pakolliset.filter(isMukautettu).map(_.koulutusmoduuliKoodiarvo).sorted.mkString(", "),
      paikallistenOppiaineidenKoodit = (pakollisetPaikalliset ++ valinnaisetPaikalliset).map(_.koulutusmoduuliKoodiarvo).mkString(","),
      pakollisetPaikalliset = pakollisetPaikalliset.map(nimiJaKoodi(_, t)).mkString(","),
      valinnaisetPaikalliset = valinnaisetPaikalliset.map(nimiKoodiJaArvosana(_, t)).mkString(","),
      valinnaisetValtakunnalliset = valinnaisetValtakunnalliset.map(nimiKoodiJaArvosana(_, t)).mkString(","),
      vahvistetutToimintaAlueidenSuoritukset = toimintaAlueet
        .filter(_.arviointiHyväksytty.getOrElse(false))
        .sortBy(_.koulutusmoduuliKoodiarvo)
        .map(nimiJaKoodi(_, t))
        .mkString(","),
      kotiopetus = kotiopetusjaksot.exists(_.exists(_.contains(hakupaiva))),
    )
  }

  private def isValtakunnallinen(osasuoritus: ROsasuoritusRow): Boolean =
    osasuoritus.koulutusmoduuliKoodisto.contains(oppiainekoodisto)

  private def isMukautettu(osasuoritus: ROsasuoritusRow): Boolean =
    JsonSerializer.extract[Option[Boolean]](osasuoritus.data \ "mukautettuOppimäärä").getOrElse(false)

  private def yhteisenOppiaineenArvosanat(oppiaineet: Seq[ROsasuoritusRow], koodiarvo: String, t: LocalizationReader): String =
    oppiaineet.filter(_.koulutusmoduuliKoodiarvo == koodiarvo) match {
      case Nil => t.get("raportti-excel-default-value-oppiaine-puuttuu")
      case suoritukset => suoritukset.map(arvosanaJaLaajuus(_, t)).mkString(",")
    }

  private def yhteisenOppiaineenKielet(oppiaineet: Seq[ROsasuoritusRow], koodiarvo: String, t: LocalizationReader): String =
    oppiaineet.filter(_.koulutusmoduuliKoodiarvo == koodiarvo) match {
      case Nil => t.get("raportti-excel-default-value-oppiaine-puuttuu")
      case suoritukset => suoritukset.map(oppiaineenNimi(_, t)).mkString(",")
    }

  // Esim. "8", "8*" (mukautettu oppimäärä) tai "8* (2.0)" (laajuus vuosiviikkotunteina, jos siirretty)
  private def arvosanaJaLaajuus(osasuoritus: ROsasuoritusRow, t: LocalizationReader): String = {
    val arvosana = osasuoritus.arviointiArvosanaKoodiarvo.getOrElse(t.get("raportti-excel-default-value-arvosana-puuttuu"))
    val tähti = if (isMukautettu(osasuoritus)) "*" else ""
    val laajuus = osasuoritus.koulutusmoduuliLaajuusArvo.map(l => s" ($l)").getOrElse("")
    s"$arvosana$tähti$laajuus"
  }

  private def nimiKoodiJaArvosana(osasuoritus: ROsasuoritusRow, t: LocalizationReader): String =
    s"${nimiJaKoodi(osasuoritus, t)} ${arvosanaJaLaajuus(osasuoritus, t)}"

  def columnSettings(t: LocalizationReader): Seq[(String, Column)] = yhteisetAlkusarakkeet(t) ++ Seq(
    "ruotsi" -> CompactColumn(t.get("raportti-excel-kolumni-ahvenanmaa-ruotsi")),
    "ruotsiToisenaKielena" -> CompactColumn(t.get("raportti-excel-kolumni-ahvenanmaa-ruotsiToisenaKielena")),
    "kieliA1" -> CompactColumn(t.get("raportti-excel-kolumni-kieliA1")),
    "kieliA1Oppimaara" -> CompactColumn(t.get("raportti-excel-kolumni-kieliA1Oppimaara")),
    "kieliA2" -> CompactColumn(t.get("raportti-excel-kolumni-kieliA2")),
    "kieliA2Oppimaara" -> CompactColumn(t.get("raportti-excel-kolumni-kieliA2Oppimaara")),
    "kieliB1" -> CompactColumn(t.get("raportti-excel-kolumni-kieliB")),
    "kieliB1Oppimaara" -> CompactColumn(t.get("raportti-excel-kolumni-kieliBOppimaara")),
    "kieliB2" -> CompactColumn(t.get("raportti-excel-kolumni-kieliB2")),
    "kieliB2Oppimaara" -> CompactColumn(t.get("raportti-excel-kolumni-kieliB2Oppimaara")),
    "matematiikka" -> CompactColumn(t.get("raportti-excel-kolumni-matematiikka")),
    "ymparistooppi" -> CompactColumn(t.get("raportti-excel-kolumni-ahvenanmaa-ymparistooppi")),
    "biologia" -> CompactColumn(t.get("raportti-excel-kolumni-biologia")),
    "maantieto" -> CompactColumn(t.get("raportti-excel-kolumni-maantieto")),
    "fysiikkaJaKemia" -> CompactColumn(t.get("raportti-excel-kolumni-ahvenanmaa-fysiikkaJaKemia")),
    "fysiikka" -> CompactColumn(t.get("raportti-excel-kolumni-fysiikka")),
    "kemia" -> CompactColumn(t.get("raportti-excel-kolumni-kemia")),
    "terveystieto" -> CompactColumn(t.get("raportti-excel-kolumni-terveystieto")),
    "uskontoJaElamankatsomustieto" -> CompactColumn(t.get("raportti-excel-kolumni-ahvenanmaa-uskontoJaElamankatsomustieto")),
    "historia" -> CompactColumn(t.get("raportti-excel-kolumni-historia")),
    "musiikki" -> CompactColumn(t.get("raportti-excel-kolumni-musiikki")),
    "yhteiskuntaoppi" -> CompactColumn(t.get("raportti-excel-kolumni-ahvenanmaa-yhteiskuntaoppi")),
    "kuvataide" -> CompactColumn(t.get("raportti-excel-kolumni-kuvataide")),
    "kasityo" -> CompactColumn(t.get("raportti-excel-kolumni-kasityo")),
    "tekstiilityo" -> CompactColumn(t.get("raportti-excel-kolumni-ahvenanmaa-tekstiilityo")),
    "tekninenTyo" -> CompactColumn(t.get("raportti-excel-kolumni-ahvenanmaa-tekninenTyo")),
    "liikunta" -> CompactColumn(t.get("raportti-excel-kolumni-ahvenanmaa-liikunta")),
    "kotitalous" -> CompactColumn(t.get("raportti-excel-kolumni-ahvenanmaa-kotitalous")),
    "oppilaanohjaus" -> CompactColumn(t.get("raportti-excel-kolumni-ahvenanmaa-oppilaanohjaus")),
    "vastuuJaYhteistyoArvio" -> CompactColumn(t.get("raportti-excel-kolumni-vastuuJaYhteistyoArvio")),
    "mukautetutYhteisetOppiaineet" -> CompactColumn(t.get("raportti-excel-kolumni-mukautetutYhteisetOppiaineet")),
    "paikallistenOppiaineidenKoodit" -> CompactColumn(t.get("raportti-excel-kolumni-paikallistenOppiaineidenKoodit")),
    "pakollisetPaikalliset" -> CompactColumn(t.get("raportti-excel-kolumni-pakollisetPaikalliset")),
    "valinnaisetPaikalliset" -> CompactColumn(t.get("raportti-excel-kolumni-valinnaisetPaikalliset")),
    "valinnaisetValtakunnalliset" -> CompactColumn(t.get("raportti-excel-kolumni-valinnaisetValtakunnalliset")),
    "vahvistetutToimintaAlueidenSuoritukset" -> CompactColumn(t.get("raportti-excel-kolumni-vahvistetutToimintaAlueidenSuoritukset")),
    "kotiopetus" -> compactLisätiedotColumn(t.get("raportti-excel-kolumni-kotiopetus"), t),
  )
}

private[raportit] case class AhvenanmaanPerusopetusRow(
  opiskeluoikeusOid: String,
  oppilaitoksenNimi: String,
  oppilaitosRaportointipäivänä: Option[String],
  lähdejärjestelmä: Option[String],
  lähdejärjestelmänId: Option[String],
  päivitetty: LocalDate,
  yksiloity: Boolean,
  oppijaOid: String,
  oppijaMasterOid: Option[String],
  hetu: Option[String],
  sukunimi: String,
  etunimet: String,
  sukupuoli: Option[String],
  kotikunta: Option[String],
  opiskeluoikeudenAlkamispäivä: Option[LocalDate],
  viimeisinTila: String,
  tilaHakupaivalla: String,
  suorituksenTila: String,
  suorituksenAlkamispaiva: String,
  suorituksenVahvistuspaiva: String,
  jaaLuokalle: Boolean,
  luokka: Option[String],
  voimassaolevatVuosiluokat: String,
  ruotsi: String,
  ruotsiToisenaKielena: String,
  kieliA1: String,
  kieliA1Oppimaara: String,
  kieliA2: String,
  kieliA2Oppimaara: String,
  kieliB1: String,
  kieliB1Oppimaara: String,
  kieliB2: String,
  kieliB2Oppimaara: String,
  matematiikka: String,
  ymparistooppi: String,
  biologia: String,
  maantieto: String,
  fysiikkaJaKemia: String,
  fysiikka: String,
  kemia: String,
  terveystieto: String,
  uskontoJaElamankatsomustieto: String,
  historia: String,
  musiikki: String,
  yhteiskuntaoppi: String,
  kuvataide: String,
  kasityo: String,
  tekstiilityo: String,
  tekninenTyo: String,
  liikunta: String,
  kotitalous: String,
  oppilaanohjaus: String,
  vastuuJaYhteistyoArvio: String,
  mukautetutYhteisetOppiaineet: String,
  paikallistenOppiaineidenKoodit: String,
  pakollisetPaikalliset: String,
  valinnaisetPaikalliset: String,
  valinnaisetValtakunnalliset: String,
  vahvistetutToimintaAlueidenSuoritukset: String,
  kotiopetus: Boolean,
)
