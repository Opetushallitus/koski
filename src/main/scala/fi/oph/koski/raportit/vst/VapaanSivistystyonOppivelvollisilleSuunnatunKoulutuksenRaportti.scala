package fi.oph.koski.raportit.vst

import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.koodisto.KoodistoViitePalvelu
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.raportit.{Column, DynamicDataSheet, RaporttiUtils}
import fi.oph.koski.raportointikanta.{RaportointiDatabase, ROsasuoritusRow}
import fi.oph.koski.schema.{Aikajakso, Koodistokoodiviite, Organisaatio, VapaanSivistystyönOpiskeluoikeudenLisätiedot}

import java.time.LocalDate

object VapaanSivistystyonOppivelvollisilleSuunnatunKoulutuksenRaportti {
  def buildRaportti(
    raportointiDatabase: RaportointiDatabase,
    koodistoViitePalvelu: KoodistoViitePalvelu,
    oppilaitosOids: Set[Organisaatio.Oid],
    alku: LocalDate,
    loppu: LocalDate,
    t: LocalizationReader
  ): Seq[DynamicDataSheet] = {
    val osaamiskokonaisuudet = koodistoViitePalvelu
      .getKoodistoKoodiViitteet(koodistoViitePalvelu.getLatestVersionRequired("vstosaamiskokonaisuus"))
      .sortBy(_.koodiarvo)

    val rows = VstRaportitRepository(raportointiDatabase)
      .suoritustiedot(oppilaitosOids, "vstoppivelvollisillesuunnattukoulutus", alku, loppu)
      .map(row => VSTKOPSRow(row, Aikajakso(alku, Some(loppu)), t))

    Seq(DynamicDataSheet(
      title = t.get("raportti-excel-vst-kops-title"),
      rows = rows.map(_.kentät(osaamiskokonaisuudet.map(_.koodiarvo))),
      columnSettings = columnSettings(osaamiskokonaisuudet, t),
    ))
  }

  private def columnSettings(osaamiskokonaisuudet: Seq[Koodistokoodiviite], t: LocalizationReader): Seq[Column] =
    Seq(
      Column(t.get("raportti-excel-kolumni-oppijaOid")),
      Column(t.get("raportti-excel-kolumni-oppijaMasterOid")),
      Column(t.get("raportti-excel-kolumni-hetu")),
      Column(t.get("raportti-excel-kolumni-sukunimi")),
      Column(t.get("raportti-excel-kolumni-etunimet")),
      Column(t.get("raportti-excel-kolumni-yksiloity")),
      Column(t.get("raportti-excel-kolumni-opetuskieli")),
      Column(t.get("raportti-excel-kolumni-opiskeluoikeusOid")),
      Column(t.get("raportti-excel-kolumni-lähdejärjestelmä")),
      Column(t.get("raportti-excel-kolumni-lähdejärjestelmänId")),
      Column(t.get("raportti-excel-kolumni-koulutustoimijaNimi")),
      Column(t.get("raportti-excel-kolumni-oppilaitoksenNimi")),
      Column(t.get("raportti-excel-kolumni-toimipisteNimi")),
      Column(t.get("raportti-excel-kolumni-päivitetty")),
      Column(t.get("raportti-excel-kolumni-opiskeluoikeudenAlkamispäivä")),
      Column(t.get("raportti-excel-kolumni-opiskeluoikeudenPäättymispäivä")),
      Column(t.get("raportti-excel-kolumni-viimeisinTila")),
      Column(t.get("raportti-excel-kolumni-maksuttomuus"), comment = Some(t.get("raportti-excel-kolumni-maksuttomuus-comment"))),
      Column(t.get("raportti-excel-kolumni-maksullisuus"), comment = Some(t.get("raportti-excel-kolumni-maksullisuus-comment"))),
      Column(t.get("raportti-excel-kolumni-oikeuttaMaksuttomuuteenPidennetty")),
      Column(t.get("raportti-excel-kolumni-suoritusVahvistettu"), comment = Some(t.get("raportti-excel-kolumni-suoritusVahvistettu-comment"))),
      Column(t.get("raportti-excel-kolumni-suorituksenVahvistuspaiva")),
      Column(t.get("raportti-excel-kolumni-päätasonSuorituksenLaajuusOpintopisteet"), comment = Some(t.get("raportti-excel-kolumni-päätasonSuorituksenLaajuusOpintopisteet-comment"))),
    ) ++ osaamiskokonaisuudet.map(k => Column(
      t.get("raportti-excel-kolumni-osaamiskokonaisuudenLaajuus", Map("osaamiskokonaisuus" -> k.nimi.map(t.from).getOrElse(k.koodiarvo))),
      comment = Some(t.get(
        if (k.koodiarvo == VSTKOPSRow.valinnaistenSuuntautumisopintojenSarake) "raportti-excel-kolumni-valinnaistenSuuntautumisopintojenLaajuus-comment"
        else "raportti-excel-kolumni-osaamiskokonaisuudenLaajuus-comment"
      )),
    )) ++ Seq(
      Column(t.get("raportti-excel-kolumni-osaamiskokonaisuuksiaAlle4Opintopistettä"), comment = Some(t.get("raportti-excel-kolumni-osaamiskokonaisuuksiaAlle4Opintopistettä-comment"))),
      Column(t.get("raportti-excel-kolumni-opintokokonaisuuksiaYhteensä"), comment = Some(t.get("raportti-excel-kolumni-opintokokonaisuuksiaYhteensä-comment"))),
      Column(t.get("raportti-excel-kolumni-arvioitujaOpintokokonaisuuksia")),
      Column(t.get("raportti-excel-kolumni-arviointiPuuttuuOpintokokonaisuuksia")),
      Column(t.get("raportti-excel-kolumni-arvioitujenOpintokokonaisuuksienLaajuus")),
    )
}

case class VSTKOPSRow(
  oppijaOid: String,
  oppijaMasterOid: Option[String],
  hetu: Option[String],
  sukunimi: String,
  etunimet: String,
  yksilöity: Boolean,
  opetuskieli: Option[String],
  opiskeluoikeusOid: String,
  lähdejärjestelmä: Option[String],
  lähdejärjestelmänId: Option[String],
  koulutustoimijaNimi: String,
  oppilaitoksenNimi: String,
  toimipisteNimi: String,
  päivitetty: LocalDate,
  opiskeluoikeudenAlkamispäivä: Option[LocalDate],
  opiskeluoikeudenPäättymispäivä: Option[LocalDate],
  viimeisinTila: Option[String],
  maksuttomuus: Option[String],
  maksullisuus: Option[String],
  oikeuttaMaksuttomuuteenPidennetty: Option[String],
  suoritusVahvistettu: Boolean,
  suorituksenVahvistuspäivä: Option[LocalDate],
  päätasonSuorituksenLaajuus: Double,
  osaamiskokonaisuuksienLaajuudet: Map[String, Double],
  osaamiskokonaisuuksiaAlle4Opintopistettä: Int,
  opintokokonaisuuksiaYhteensä: Int,
  arvioitujaOpintokokonaisuuksia: Int,
  arviointiPuuttuuOpintokokonaisuuksia: Int,
  arvioitujenOpintokokonaisuuksienLaajuus: Double,
) {
  def kentät(osaamiskokonaisuusKoodiarvot: Seq[String]): Seq[Any] =
    Seq(
      oppijaOid,
      oppijaMasterOid,
      hetu,
      sukunimi,
      etunimet,
      yksilöity,
      opetuskieli,
      opiskeluoikeusOid,
      lähdejärjestelmä,
      lähdejärjestelmänId,
      koulutustoimijaNimi,
      oppilaitoksenNimi,
      toimipisteNimi,
      päivitetty,
      opiskeluoikeudenAlkamispäivä,
      opiskeluoikeudenPäättymispäivä,
      viimeisinTila,
      maksuttomuus,
      maksullisuus,
      oikeuttaMaksuttomuuteenPidennetty,
      suoritusVahvistettu,
      suorituksenVahvistuspäivä,
      päätasonSuorituksenLaajuus,
    ) ++ osaamiskokonaisuusKoodiarvot.map(osaamiskokonaisuuksienLaajuudet.get) ++ Seq(
      osaamiskokonaisuuksiaAlle4Opintopistettä,
      opintokokonaisuuksiaYhteensä,
      arvioitujaOpintokokonaisuuksia,
      arviointiPuuttuuOpintokokonaisuuksia,
      arvioitujenOpintokokonaisuuksienLaajuus,
    )
}

object VSTKOPSRow {
  // Koodistossa vstosaamiskokonaisuus on oma koodinsa valinnaisille suuntautumisopinnoille, ja käyttöliittymä tarjoaa
  // sen samalla nimellä kuin erillisen valinnaisten suuntautumisopintojen suorituksen, joten ne raportoidaan samassa sarakkeessa.
  val valinnaistenSuuntautumisopintojenSarake = "1008"

  private val osaamiskokonaisuudenVähimmäislaajuus = 4.0

  def apply(data: VstRaporttiRows, rajaus: Aikajakso, t: LocalizationReader): VSTKOPSRow = {
    val oo = data.opiskeluoikeus
    val pts = data.päätasonSuoritus
    val henkilö = data.henkilö
    val lisätiedot = JsonSerializer.extract[Option[VapaanSivistystyönOpiskeluoikeudenLisätiedot]](oo.data \ "lisätiedot")
    val (ylimmänTasonOsasuoritukset, opintokokonaisuudet) = data.osasuoritukset.partition(_.ylempiOsasuoritusId.isEmpty)
    val opintokokonaisuudetYlemmänMukaan = opintokokonaisuudet.groupBy(_.ylempiOsasuoritusId)
    // Laajuudet lasketaan opintokokonaisuuksista kuten VapaaSivistystyöValidation tekee vahvistetulle suoritukselle
    def opintokokonaisuuksienLaajuus(ylempi: ROsasuoritusRow) =
      laajuus(opintokokonaisuudetYlemmänMukaan.getOrElse(Some(ylempi.osasuoritusId), Nil))
    val arvioidut = opintokokonaisuudet.filter(_.arviointiArvosanaKoodiarvo.isDefined)

    VSTKOPSRow(
      oppijaOid = henkilö.oppijaOid,
      oppijaMasterOid = oo.oppijaMasterOid,
      hetu = henkilö.hetu,
      sukunimi = henkilö.sukunimi,
      etunimet = henkilö.etunimet,
      yksilöity = henkilö.yksiloity,
      opetuskieli = JsonSerializer.extract[Option[Koodistokoodiviite]](pts.data \ "suorituskieli").flatMap(_.nimi).map(t.from),
      opiskeluoikeusOid = oo.opiskeluoikeusOid,
      lähdejärjestelmä = oo.lähdejärjestelmäKoodiarvo,
      lähdejärjestelmänId = oo.lähdejärjestelmäId,
      koulutustoimijaNimi = t.pick(oo.koulutustoimijaNimi, oo.koulutustoimijaNimiSv),
      oppilaitoksenNimi = t.pick(oo.oppilaitosNimi, oo.oppilaitosNimiSv),
      toimipisteNimi = t.pick(pts.toimipisteNimi, pts.toimipisteNimiSv),
      päivitetty = oo.aikaleima.toLocalDateTime.toLocalDate,
      opiskeluoikeudenAlkamispäivä = oo.alkamispäivä.map(_.toLocalDate),
      opiskeluoikeudenPäättymispäivä = oo.päättymispäivä.map(_.toLocalDate),
      viimeisinTila = oo.viimeisinTila,
      maksuttomuus = RaporttiUtils.jaksotMerkkijonona(lisätiedot.flatMap(_.maksuttomuus).map(_.filter(_.maksuton)), rajaus),
      maksullisuus = RaporttiUtils.jaksotMerkkijonona(lisätiedot.flatMap(_.maksuttomuus).map(_.filterNot(_.maksuton)), rajaus),
      oikeuttaMaksuttomuuteenPidennetty = RaporttiUtils.jaksotMerkkijonona(lisätiedot.flatMap(_.oikeuttaMaksuttomuuteenPidennetty)),
      suoritusVahvistettu = pts.vahvistusPäivä.isDefined,
      suorituksenVahvistuspäivä = pts.vahvistusPäivä.map(_.toLocalDate),
      päätasonSuorituksenLaajuus = laajuus(opintokokonaisuudet),
      osaamiskokonaisuuksienLaajuudet = ylimmänTasonOsasuoritukset.groupMapReduce(sarake)(opintokokonaisuuksienLaajuus)(_ + _),
      osaamiskokonaisuuksiaAlle4Opintopistettä = ylimmänTasonOsasuoritukset
        .filter(_.suorituksenTyyppi == "vstosaamiskokonaisuus")
        .count(opintokokonaisuuksienLaajuus(_) < osaamiskokonaisuudenVähimmäislaajuus),
      opintokokonaisuuksiaYhteensä = opintokokonaisuudet.size,
      arvioitujaOpintokokonaisuuksia = arvioidut.size,
      arviointiPuuttuuOpintokokonaisuuksia = opintokokonaisuudet.size - arvioidut.size,
      arvioitujenOpintokokonaisuuksienLaajuus = laajuus(arvioidut),
    )
  }

  private def sarake(osasuoritus: ROsasuoritusRow): String =
    if (osasuoritus.suorituksenTyyppi == "vstvalinnainensuuntautuminen") valinnaistenSuuntautumisopintojenSarake
    else osasuoritus.koulutusmoduuliKoodiarvo

  private def laajuus(osasuoritukset: Seq[ROsasuoritusRow]): Double =
    osasuoritukset.flatMap(_.koulutusmoduuliLaajuusArvo).sum
}
