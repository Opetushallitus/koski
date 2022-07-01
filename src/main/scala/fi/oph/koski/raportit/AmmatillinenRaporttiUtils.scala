package fi.oph.koski.raportit

import java.sql.Date
import java.time.LocalDate
import fi.oph.koski.json.JsonSerializer
import fi.oph.koski.localization.LocalizationReader
import fi.oph.koski.raportointikanta.{ROsasuoritusRow, RPäätasonSuoritusRow}
import fi.oph.koski.schema._

object AmmatillinenRaporttiUtils {
  def extractOsaamisalatAikavalilta(päätasonSuoritus: RPäätasonSuoritusRow, alku: LocalDate, loppu: LocalDate) = {
    JsonSerializer.extract[Option[List[Osaamisalajakso]]](päätasonSuoritus.data \ "osaamisala")
      .getOrElse(List())
      .filterNot(j => j.alku.exists(_.isAfter(loppu)))
      .filterNot(j => j.loppu.exists(_.isBefore(alku)))
      .map(_.osaamisala.koodiarvo)
      .sorted
      .distinct
  }

  def tutkintonimike(osasuoritus: RPäätasonSuoritusRow, lang: String) = {
    JsonSerializer.extract[Option[List[Koodistokoodiviite]]](osasuoritus.data \ "tutkintonimike").map(_.flatMap(_.nimi)).map(_.map(_.get(lang))).map(_.mkString(","))
  }

  def suoritusTavat(päätasonsuoritukset: Seq[RPäätasonSuoritusRow], lang: String) = {
    päätasonsuoritukset.flatMap(pts => JsonSerializer.extract[Option[Koodistokoodiviite]](pts.data \ "suoritustapa").flatMap(_.nimi.map(_.get(lang)))).mkString(",")
  }

  def vahvistusPäiväToTila(vahvistusPäivä: Option[Date], t: LocalizationReader) = vahvistusPäivä match {
    case Some(päivä) if isTulevaisuudessa(päivä) =>  s"${t.get("raportti-excel-default-value-valmistumassa")} ${päivä.toLocalDate}"
    case Some(_) => t.get("raportti-excel-default-value-valmis").capitalize
    case _ => t.get("raportti-excel-default-value-kesken").capitalize
  }

  def isTulevaisuudessa(date: Date) = date.toLocalDate.isAfter(LocalDate.now())

  def tutkinnonOsanRyhmä(osasuoritus: ROsasuoritusRow, koodiarvot: String*) = {
    val viite = JsonSerializer.extract[Option[Koodistokoodiviite]](osasuoritus.data \ "tutkinnonOsanRyhmä")
    viite.exists(v => koodiarvot.contains(v.koodiarvo))
  }

  def isAnyOf(osasuoritus: ROsasuoritusRow, fs: (ROsasuoritusRow => Boolean)*) = fs.exists(f => f(osasuoritus))

  val sisältyyVahvistettuunPäätasonSuoritukseen: (ROsasuoritusRow, RPäätasonSuoritusRow) => Boolean = (os, ps) => ps.päätasonSuoritusId == os.päätasonSuoritusId & ps.vahvistusPäivä.isDefined

  val yhteislaajuus: Seq[ROsasuoritusRow] => Double = osasuoritukset => osasuoritukset.flatMap(_.koulutusmoduuliLaajuusArvo).sum

  val isVahvistusPäivällinen: ROsasuoritusRow => Boolean = osasuoritus => osasuoritus.vahvistusPäivä.isDefined

  val tunnustetut: Seq[ROsasuoritusRow] => Seq[ROsasuoritusRow] = osasuoritukset => osasuoritukset.filter(os => os.tunnustettu)

  val valinnaiset: Seq[ROsasuoritusRow] => Seq[ROsasuoritusRow] = osasuoritukset => osasuoritukset.filterNot(isPakollinen)

  val pakolliset: Seq[ROsasuoritusRow] => Seq[ROsasuoritusRow] = osasuoritukset => osasuoritukset.filter(isPakollinen)

  val isPakollinen: ROsasuoritusRow => Boolean = osasuoritus => osasuoritus.koulutusmoduuliPakollinen.getOrElse(false)

  val näytöt: Seq[ROsasuoritusRow] => Seq[ROsasuoritusRow] = osasuoritukset => osasuoritukset.filter(isNäyttö)

  val isNäyttö: ROsasuoritusRow => Boolean = osasuoritus => JsonSerializer.extract[Option[Näyttö]](osasuoritus.data \ "näyttö").isDefined

  val rahoituksenPiirissä: Seq[ROsasuoritusRow] => Seq[ROsasuoritusRow] = osasuoritukset => osasuoritukset.filter(isRahoituksenPiirissä)

  val isRahoituksenPiirissä: ROsasuoritusRow => Boolean = osasuoritus => JsonSerializer.extract[Option[OsaamisenTunnustaminen]](osasuoritus.data \ "tunnustettu").exists(_.rahoituksenPiirissä)

  val yhteistenTutkinnonOsienKoodistokoodiarvot = AmmatillisenTutkinnonOsa.yhteisetTutkinnonOsat.map(_.koodiarvo).toSet

  val isYhteinenTutkinnonOsa: ROsasuoritusRow => Boolean = osasuoritus => yhteistenTutkinnonOsienKoodistokoodiarvot.contains(osasuoritus.koulutusmoduuliKoodiarvo)

  val osasuorituksenaOsissa: (ROsasuoritusRow, Seq[ROsasuoritusRow]) => Boolean = (osasuoritus, osat) => osat.exists(osa => osasuoritus.ylempiOsasuoritusId.contains(osa.osasuoritusId))

  val isAmmatillisenLukioOpintoja: ROsasuoritusRow => Boolean = osasuoritus => osasuoritus.suorituksenTyyppi == "ammatillinenlukionopintoja"

  val isAmmatillinenMuitaOpintoValmiuksiaTukeviaOpintoja: ROsasuoritusRow => Boolean = osasuoritus => osasuoritus.suorituksenTyyppi == "ammatillinenmuitaopintovalmiuksiatukeviaopintoja"

  val isAmmatillisenKorkeakouluOpintoja: ROsasuoritusRow => Boolean = osasuoritus => osasuoritus.suorituksenTyyppi == "ammatillinenkorkeakouluopintoja"

  val isAmmatillinenTutkinnonOsaaPienempiKokonaisuus: ROsasuoritusRow => Boolean = osasuoritus => osasuoritus.suorituksenTyyppi == "ammatillisentutkinnonosaapienempikokonaisuus"

  val isAmmatillisenTutkinnonOsanOsaalue: ROsasuoritusRow => Boolean = osasuoritus => osasuoritus.suorituksenTyyppi == "ammatillisentutkinnonosanosaalue"

  val isAmmatillisenYhteisenTutkinnonOsienOsaalue: (ROsasuoritusRow, Seq[ROsasuoritusRow]) => Boolean = (osasuoritus, osasuoritukset) => {
    val osat = osasuoritukset.filter(os => os.suorituksenTyyppi == "ammatillisentutkinnonosa" & !isYhteinenTutkinnonOsa(os))
    isAmmatillisenTutkinnonOsanOsaalue(osasuoritus) & osasuorituksenaOsissa(osasuoritus, osat)
  }

  val isYhteinenTutkinnonOsanOsaalue: (ROsasuoritusRow, Seq[ROsasuoritusRow]) => Boolean = (osasuoritus, osasuoritukset) => {
    val osat = osasuoritukset.filter(isYhteinenTutkinnonOsa)
    isAmmatillisenTutkinnonOsanOsaalue(osasuoritus) & osasuorituksenaOsissa(osasuoritus, osat)
  }

  val tutkinnonosatvalinnanmahdollisuusKoodiarvot = Seq("1", "2")

  val isAmmatillisenTutkinnonOsa: ROsasuoritusRow => Boolean = osasuoritus => {
    osasuoritus.suorituksenTyyppi == "ammatillisentutkinnonosa" &&
      !tutkinnonosatvalinnanmahdollisuusKoodiarvot.contains(osasuoritus.koulutusmoduuliKoodiarvo) &&
      !isYhteinenTutkinnonOsa(osasuoritus) &&
      !tutkinnonOsanRyhmä(osasuoritus, "3", "4")
  }

  val isAmmatillinenJatkovalmiuksiaTukeviaOpintoja: (ROsasuoritusRow, Seq[ROsasuoritusRow]) => Boolean = (osasuoritus, osasuoritukset) => {
    isAmmatillisenLukioOpintoja(osasuoritus) ||
    isAmmatillinenMuitaOpintoValmiuksiaTukeviaOpintoja(osasuoritus) ||
    isAmmatillisenYhteisenTutkinnonOsienOsaalue(osasuoritus, osasuoritukset)
  }

  val isArvioinniton: ROsasuoritusRow => Boolean = osasuoritus => isAnyOf(osasuoritus,
    isAmmatillisenLukioOpintoja,
    isAmmatillisenKorkeakouluOpintoja,
    isAmmatillinenMuitaOpintoValmiuksiaTukeviaOpintoja,
    isAmmatillisenTutkinnonOsanOsaalue
  )

  val korotettuEriVuonna: Seq[ROsasuoritusRow] => Seq[ROsasuoritusRow] = osasuoritukset => osasuoritukset.filter(_.korotettuEriVuonna)

  def suorituksetJaKorotuksetLaajuuksina(suoritukset: Seq[ROsasuoritusRow]): String = {
    val eriVuonnaKorotetut = korotettuEriVuonna(suoritukset)
    if (eriVuonnaKorotetut.size > 0 ) {
      s"${yhteislaajuus(suoritukset)} (${yhteislaajuus(eriVuonnaKorotetut)})"
    } else {
      s"${yhteislaajuus(suoritukset)}"
    }
  }

  def suorituksetJaKorotuksetSuoritustenMäärässä(suoritukset: Seq[ROsasuoritusRow]): String = {
    val eriVuonnaKorotetut = korotettuEriVuonna(suoritukset)
    if (eriVuonnaKorotetut.size > 0 ) {
      s"${suoritukset.size} (${eriVuonnaKorotetut.size})"
    } else {
      s"${suoritukset.size}"
    }
  }
}
