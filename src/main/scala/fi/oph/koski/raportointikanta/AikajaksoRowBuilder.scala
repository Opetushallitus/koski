package fi.oph.koski.raportointikanta

import java.sql.Date
import java.time.LocalDate

import fi.oph.koski.schema._
import fi.oph.koski.util.DateOrdering

object AikajaksoRowBuilder {

  def buildROpiskeluoikeusAikajaksoRows(opiskeluoikeusOid: String, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): Seq[ROpiskeluoikeusAikajaksoRow] = {
    buildAikajaksoRows(buildROpiskeluoikeusAikajaksoRowForOneDay, opiskeluoikeusOid, opiskeluoikeus)
  }

  private def buildAikajaksoRows[A <: KoskeenTallennettavaOpiskeluoikeus, B <: AikajaksoRow[B]](buildAikajaksoRow: ((String, A, LocalDate) => B), opiskeluoikeusOid: String, opiskeluoikeus: A): Seq[B] = {
    var edellinenTila: Option[String] = None
    var edellinenTilaAlkanut: Option[Date] = None
    for ((alku, loppu) <- aikajaksot(opiskeluoikeus)) yield {
      val aikajakso = buildAikajaksoRow(opiskeluoikeusOid, opiskeluoikeus, alku).withLoppu(Date.valueOf(loppu))
      if (edellinenTila.isDefined && edellinenTila.get == aikajakso.tila) {
        aikajakso.withTilaAlkanut(edellinenTilaAlkanut.get)
      } else {
        edellinenTila = Some(aikajakso.tila)
        edellinenTilaAlkanut = Some(aikajakso.alku)
        aikajakso
      }
    }
  }

  private def buildROpiskeluoikeusAikajaksoRowForOneDay(opiskeluoikeusOid: String, o: KoskeenTallennettavaOpiskeluoikeus, päivä: LocalDate): ROpiskeluoikeusAikajaksoRow = {
    // Vanhassa datassa samalla alku-päivämäärällä voi löytyä useampi opiskeluoikeusjakso (nykyään tämä
    // ei enää mene läpi opiskeluoikeusjaksojenPäivämäärät-validaatiosta). Tässä otetaan näistä jaksoista
    // viimeinen, mikä lienee oikein.
    val jakso = o.tila.opiskeluoikeusjaksot
      .filterNot(_.alku.isAfter(päivä))
      .lastOption.getOrElse(throw new RuntimeException(s"Opiskeluoikeusjaksoa ei löydy $opiskeluoikeusOid $päivä"))

    val ammatillisenLisätiedot: Option[AmmatillisenOpiskeluoikeudenLisätiedot] = if (o.lisätiedot.nonEmpty) o.lisätiedot.get match {
      case a: AmmatillisenOpiskeluoikeudenLisätiedot => Some(a)
      case _ => None
    } else None
    val aikuistenPerusopetuksenLisätiedot: Option[AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot] = if (o.lisätiedot.nonEmpty) o.lisätiedot.get match {
      case l: AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot => Some(l)
      case _ => None
    } else None
    val perusopetuksenLisätiedot: Option[PerusopetuksenOpiskeluoikeudenLisätiedot] = if (o.lisätiedot.nonEmpty) o.lisätiedot.get match {
      case l: PerusopetuksenOpiskeluoikeudenLisätiedot => Some(l)
      case _ => None
    } else None
    val lukionLisätiedot: Option[LukionOpiskeluoikeudenLisätiedot] = if (o.lisätiedot.nonEmpty) o.lisätiedot.get match {
      case l: LukionOpiskeluoikeudenLisätiedot => Some(l)
      case _ => None
    } else None
    val lukioonValmistavanLisätiedot: Option[LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot] = if (o.lisätiedot.nonEmpty) o.lisätiedot.get match {
      case l: LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot => Some(l)
      case _ => None
    } else None

    def ammatillinenAikajakso(lisätieto: AmmatillisenOpiskeluoikeudenLisätiedot => Option[List[Aikajakso]]): Byte =
      ammatillisenLisätiedot.flatMap(lisätieto).flatMap(_.find(_.contains(päivä))).size.toByte

    val oppisopimus = oppisopimusAikajaksot(o)

    ROpiskeluoikeusAikajaksoRow(
      opiskeluoikeusOid = opiskeluoikeusOid,
      alku = Date.valueOf(päivä),
      loppu = Date.valueOf(päivä), // korvataan oikealla päivällä ylempänä
      tila = jakso.tila.koodiarvo,
      tilaAlkanut = Date.valueOf(päivä), // korvataan oikealla päivällä ylempänä
      opiskeluoikeusPäättynyt = jakso.opiskeluoikeusPäättynyt,
      opintojenRahoitus = jakso match {
        case k: KoskiOpiskeluoikeusjakso => k.opintojenRahoitus.map(_.koodiarvo)
        case _ => None
      },
      majoitus = ammatillinenAikajakso(_.majoitus),
      sisäoppilaitosmainenMajoitus = (
        ammatillinenAikajakso(_.sisäoppilaitosmainenMajoitus) +
          aikuistenPerusopetuksenLisätiedot.flatMap(_.sisäoppilaitosmainenMajoitus).flatMap(_.find(_.contains(päivä))).size +
          perusopetuksenLisätiedot.flatMap(_.sisäoppilaitosmainenMajoitus).flatMap(_.find(_.contains(päivä))).size +
          lukionLisätiedot.flatMap(_.sisäoppilaitosmainenMajoitus).flatMap(_.find(_.contains(päivä))).size +
          lukioonValmistavanLisätiedot.flatMap(_.sisäoppilaitosmainenMajoitus).flatMap(_.find(_.contains(päivä))).size
        ).toByte,
      vaativanErityisenTuenYhteydessäJärjestettäväMajoitus = ammatillinenAikajakso(_.vaativanErityisenTuenYhteydessäJärjestettäväMajoitus),
      erityinenTuki = ammatillinenAikajakso(_.erityinenTuki),
      vaativanErityisenTuenErityinenTehtävä = ammatillinenAikajakso(_.vaativanErityisenTuenErityinenTehtävä),
      hojks = ammatillisenLisätiedot.flatMap(_.hojks).find(_.contains(päivä)).size.toByte,
      vaikeastiVammainen = (
        ammatillinenAikajakso(_.vaikeastiVammainen) +
          aikuistenPerusopetuksenLisätiedot.flatMap(_.vaikeastiVammainen).flatMap(_.find(_.contains(päivä))).size +
          perusopetuksenLisätiedot.flatMap(_.vaikeastiVammainen).flatMap(_.find(_.contains(päivä))).size
        ).toByte,
      vammainenJaAvustaja = ammatillinenAikajakso(_.vammainenJaAvustaja),
      osaAikaisuus = ammatillisenLisätiedot.flatMap(_.osaAikaisuusjaksot).flatMap(_.find(_.contains(päivä))).map(_.osaAikaisuus).getOrElse(100).toByte,
      opiskeluvalmiuksiaTukevatOpinnot = ammatillisenLisätiedot.flatMap(_.opiskeluvalmiuksiaTukevatOpinnot).flatMap(_.find(_.contains(päivä))).size.toByte,
      vankilaopetuksessa = ammatillinenAikajakso(_.vankilaopetuksessa),
      oppisopimusJossainPäätasonSuorituksessa = oppisopimus.find(_.contains(päivä)).size.toByte
    )
    // Note: When adding something here, remember to update aikajaksojenAlkupäivät (below), too
  }

  val IndefiniteFuture = LocalDate.of(9999, 12, 31) // no special meaning, but must be after any possible real alkamis/päättymispäivä

  private def aikajaksot(o: KoskeenTallennettavaOpiskeluoikeus): Seq[(LocalDate, LocalDate)] = {
    val alkupäivät: Seq[LocalDate] = mahdollisetAikajaksojenAlkupäivät(o)
    val alkamispäivä: LocalDate = o.tila.opiskeluoikeusjaksot.headOption.map(_.alku).getOrElse(throw new RuntimeException(s"Alkamispäivä puuttuu ${o.oid}"))
    val päättymispäivä: LocalDate = o.tila.opiskeluoikeusjaksot.lastOption.filter(_.opiskeluoikeusPäättynyt).map(_.alku).getOrElse(IndefiniteFuture)
    val rajatutAlkupäivät = alkupäivät
      .filterNot(_.isBefore(alkamispäivä))
      .filterNot(_.isAfter(päättymispäivä))
    if (rajatutAlkupäivät.isEmpty) {
      // Can happen only if alkamispäivä or päättymispäivä are totally bogus (e.g. in year 10000)
      throw new RuntimeException(s"Virheelliset alkamis-/päättymispäivät: ${o.oid} $alkamispäivä $päättymispäivä")
    }
    rajatutAlkupäivät.zip(rajatutAlkupäivät.tail.map(_.minusDays(1)) :+ päättymispäivä)
  }

  private def mahdollisetAikajaksojenAlkupäivät(o: KoskeenTallennettavaOpiskeluoikeus): Seq[LocalDate] = {
    // logiikka: uusi r_opiskeluoikeus_aikajakso-rivi pitää aloittaa, jos ko. päivänä alkaa joku jakso (erityinen tuki tms),
    // tai jos edellisenä päivänä on loppunut joku jakso.
    val lisätiedotAikajaksot: Seq[Aikajakso] = if (o.lisätiedot.nonEmpty) o.lisätiedot.get match {
      case aol: AmmatillisenOpiskeluoikeudenLisätiedot =>
        Seq(
          aol.majoitus,
          aol.sisäoppilaitosmainenMajoitus,
          aol.vaativanErityisenTuenYhteydessäJärjestettäväMajoitus,
          aol.erityinenTuki,
          aol.vaativanErityisenTuenErityinenTehtävä,
          aol.vaikeastiVammainen,
          aol.vammainenJaAvustaja,
          aol.vankilaopetuksessa
        ).flatMap(_.getOrElse(List.empty)) ++
          aol.opiskeluvalmiuksiaTukevatOpinnot.getOrElse(Seq.empty).map(j => Aikajakso(j.alku, Some(j.loppu))) ++
          aol.osaAikaisuusjaksot.getOrElse(Seq.empty).map(j => Aikajakso(j.alku, j.loppu)) ++
          aol.hojks.toList.map(h => Aikajakso(h.alku.getOrElse(o.alkamispäivä.getOrElse(throw new RuntimeException(s"Alkamispäivä puuttuu ${o.oid}"))), h.loppu))
      case apol: AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot =>
        Seq(
          apol.sisäoppilaitosmainenMajoitus,
          apol.vaikeastiVammainen
        ).flatMap(_.getOrElse(List.empty))
      case pol: PerusopetuksenOpiskeluoikeudenLisätiedot =>
        Seq(
          pol.sisäoppilaitosmainenMajoitus,
          pol.vaikeastiVammainen
        ).flatMap(_.getOrElse(List.empty))
      case lol: LukionOpiskeluoikeudenLisätiedot =>
        Seq(
          lol.sisäoppilaitosmainenMajoitus
        ).flatMap(_.getOrElse(List.empty))
      case lvol: LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot =>
        Seq(
          lvol.sisäoppilaitosmainenMajoitus
        ).flatMap(_.getOrElse(List.empty))
      case _ => Seq()
    } else Seq()

    val jaksot = lisätiedotAikajaksot ++ oppisopimusAikajaksot(o)

    (o.tila.opiskeluoikeusjaksot.map(_.alku) ++
      jaksot.map(_.alku) ++
      jaksot.map(_.loppu).filter(_.nonEmpty).map(_.get.plusDays(1))
      ).sorted(DateOrdering.localDateOrdering).distinct
  }

  private val JarjestamismuotoOppisopimus = Koodistokoodiviite("20", "jarjestamismuoto")
  private val OsaamisenhankkimistapaOppisopimus = Koodistokoodiviite("oppisopimus", "osaamisenhankkimistapa")

  private def oppisopimusAikajaksot(o: KoskeenTallennettavaOpiskeluoikeus): Seq[Jakso] = {
    def convert(järjestämismuodot: Option[List[Järjestämismuotojakso]], osaamisenHankkimistavat: Option[List[OsaamisenHankkimistapajakso]]): Seq[Jakso] = {
      järjestämismuodot.getOrElse(List.empty).filter(_.järjestämismuoto.tunniste == JarjestamismuotoOppisopimus) ++
        osaamisenHankkimistavat.getOrElse(List.empty).filter(_.osaamisenHankkimistapa.tunniste == OsaamisenhankkimistapaOppisopimus)
    }
    o.suoritukset.flatMap {
      case s: NäyttötutkintoonValmistavanKoulutuksenSuoritus => convert(s.järjestämismuodot, s.osaamisenHankkimistavat)
      case s: AmmatillisenTutkinnonSuoritus => convert(s.järjestämismuodot, s.osaamisenHankkimistavat)
      case s: AmmatillisenTutkinnonOsittainenSuoritus => convert(s.järjestämismuodot, s.osaamisenHankkimistavat)
      case _ => Seq.empty
    }
  }
}
