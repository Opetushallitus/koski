package fi.oph.koski.raportointikanta

import java.sql.Date
import java.time.LocalDate

import fi.oph.koski.schema._
import fi.oph.koski.util.DateOrdering

object AikajaksoRowBuilder {

  def buildROpiskeluoikeusAikajaksoRows(opiskeluoikeusOid: String, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): Seq[ROpiskeluoikeusAikajaksoRow] = {
    buildAikajaksoRows(buildROpiskeluoikeusAikajaksoRowForOneDay, opiskeluoikeusOid, opiskeluoikeus).flatten
  }

  def buildAikajaksoRows(opiskeluoikeusOid: String, opiskeluoikeus: KoskeenTallennettavaOpiskeluoikeus): Seq[RAikajaksoRow] = {
    buildRAikajaksoRows(opiskeluoikeusOid, opiskeluoikeus)
  }

  def buildAmmatillisenKoulutuksenJarjestamismuotoAikajaksoRows(opiskeluoikeusOid: String, opiskeluoikeus: AmmatillinenOpiskeluoikeus): Seq[RAmmatillisenKoulutuksenJarjestamismuotoAikajaksoRow] = {
    buildRAmmatillisenKoulutuksenJarjestamismuotoAikajaksoRows(opiskeluoikeusOid, opiskeluoikeus)
  }

  def buildOsaamisenHankkimistapaAikajaksoRows(opiskeluoikeusOid: String, opiskeluoikeus: AmmatillinenOpiskeluoikeus): Seq[ROsaamisenhankkimistapaAikajaksoRow] = {
    buildROsaamisenHankkimistapaAikajaksoRows(opiskeluoikeusOid, opiskeluoikeus)
  }

  def buildEsiopetusOpiskeluoikeusAikajaksoRows(opiskeluoikeusOid: String, opiskeluoikeus: EsiopetuksenOpiskeluoikeus): Seq[EsiopetusOpiskeluoikeusAikajaksoRow] = {
    buildAikajaksoRows(buildEsiopetusAikajaksoRowForOneDay, opiskeluoikeusOid, opiskeluoikeus).flatten
  }

  private def buildAikajaksoRows[A <: KoskeenTallennettavaOpiskeluoikeus, B <: AikajaksoRow[B]](buildAikajaksoRow: ((String, A, LocalDate) => Option[B]), opiskeluoikeusOid: String, opiskeluoikeus: A): Seq[Option[B]] = {
    var edellinenTila: Option[String] = None
    var edellinenTilaAlkanut: Option[Date] = None
    for ((alku, loppu) <- aikajaksot(opiskeluoikeus)) yield {
      buildAikajaksoRow(opiskeluoikeusOid, opiskeluoikeus, alku).map(ajr => {
        val aikajakso = ajr.withLoppu(Date.valueOf(loppu))
        if (edellinenTila.isDefined && edellinenTila.get == aikajakso.tila) {
          aikajakso.withTilaAlkanut(edellinenTilaAlkanut.get)
        } else {
          edellinenTila = Some(aikajakso.tila)
          edellinenTilaAlkanut = Some(aikajakso.alku)
          aikajakso
        }
      })
    }
  }

  private def buildROpiskeluoikeusAikajaksoRowForOneDay(opiskeluoikeusOid: String, o: KoskeenTallennettavaOpiskeluoikeus, päivä: LocalDate): Option[ROpiskeluoikeusAikajaksoRow] = {
    // Vanhassa datassa samalla alku-päivämäärällä voi löytyä useampi opiskeluoikeusjakso (nykyään tämä
    // ei enää mene läpi opiskeluoikeusjaksojenPäivämäärät-validaatiosta). Tässä otetaan näistä jaksoista
    // viimeinen, mikä lienee oikein.
    val jakso = o.tila.opiskeluoikeusjaksot
      .filterNot(_.alku.isAfter(päivä))
      .lastOption.getOrElse(throw new RuntimeException(s"Opiskeluoikeusjaksoa ei löydy $opiskeluoikeusOid $päivä"))

    def aikajaksoVoimassaPäivänä(aikajakso: Option[Seq[DateContaining]]): Boolean = {
      aikajakso.exists(_.exists(_.contains(päivä)))
    }

    def lisätietoVoimassaPäivänä(
      aikajaksoLisätiedosta: PartialFunction[OpiskeluoikeudenLisätiedot, Option[Seq[DateContaining]]]
    ): Boolean = {
      o.lisätiedot.exists(l => aikajaksoVoimassaPäivänä(aikajaksoLisätiedosta.lift(l).flatten))
    }

    if (o.isInstanceOf[EsiopetuksenOpiskeluoikeus]) {
      None
    } else {
      Some(ROpiskeluoikeusAikajaksoRow(
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
        erityisenKoulutusTehtävänJaksoTehtäväKoodiarvo = o.lisätiedot.flatMap {
          case l: ErityisenKoulutustehtävänJaksollinen =>
            l.erityisenKoulutustehtävänJaksot.toList.flatten.find(_.contains(päivä)).map(_.tehtävä.koodiarvo)
          case _ => None
        },
        ulkomainenVaihtoopiskelija = o.lisätiedot.exists {
          case l: UlkomainenVaihtoopiskelija => l.ulkomainenVaihtoopiskelija
          case _ => false
        },
        osaAikaisuus = o.lisätiedot.collect {
          case a: OsaAikaisuusjaksollinen => a
        }.flatMap(_.osaAikaisuusjaksot).flatMap(_.find(_.contains(päivä))).map(_.osaAikaisuus).getOrElse(100).toByte,
        majoitus = lisätietoVoimassaPäivänä {
          case l: Majoituksellinen => l.majoitus
        },
        majoitusetu = lisätietoVoimassaPäivänä {
          case l: Majoitusetuinen => Some(l.majoitusetu.toList)
        },
        kuljetusetu = lisätietoVoimassaPäivänä {
          case l: Kuljetusetuinen => Some(l.kuljetusetu.toList)
        },
        sisäoppilaitosmainenMajoitus = lisätietoVoimassaPäivänä {
          case l: SisäoppilaitosmainenMajoitus => l.sisäoppilaitosmainenMajoitus
        },
        vaativanErityisenTuenYhteydessäJärjestettäväMajoitus = lisätietoVoimassaPäivänä {
          case l: VaativanErityisenTuenYhteydessäJärjestettävänMajoituksenSisältäväLisätieto => l.vaativanErityisenTuenYhteydessäJärjestettäväMajoitus
        },
        erityinenTuki = lisätietoVoimassaPäivänä {
          case l: AmmatillisenOpiskeluoikeudenLisätiedot => l.erityinenTuki
          case l: TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot => l.erityinenTuki
          case l: PerusopetuksenOpiskeluoikeudenLisätiedot =>
            Some(l.erityisenTuenPäätös.toList ::: l.erityisenTuenPäätökset.toList.flatten)
          case l: PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot =>
            Some(l.erityisenTuenPäätös.toList ::: l.erityisenTuenPäätökset.toList.flatten)
          case l: TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot =>
            l.erityisenTuenPäätökset
        },
        vaativanErityisenTuenErityinenTehtävä = lisätietoVoimassaPäivänä {
          case l: VaativanErityisenTuenErityisenTehtävänSisältäväLisätieto => l.vaativanErityisenTuenErityinenTehtävä
        },
        hojks = lisätietoVoimassaPäivänä {
          case l: AmmatillisenOpiskeluoikeudenLisätiedot => Some(l.hojks.toList)
        },
        vammainen = lisätietoVoimassaPäivänä {
          case l: Vammainen => l.vammainen
        },
        vaikeastiVammainen = lisätietoVoimassaPäivänä {
          case l: VaikeastiVammainen => l.vaikeastiVammainen
        },
        vammainenJaAvustaja = lisätietoVoimassaPäivänä {
          case l: VammainenJaAvustaja => l.vammainenJaAvustaja
        },
        opiskeluvalmiuksiaTukevatOpinnot = lisätietoVoimassaPäivänä {
          case l: AmmatillisenOpiskeluoikeudenLisätiedot => l.opiskeluvalmiuksiaTukevatOpinnot
        },
        vankilaopetuksessa = lisätietoVoimassaPäivänä {
          case l: Vankilaopetuksessa => l.vankilaopetuksessa
        },
        pidennettyOppivelvollisuus = lisätietoVoimassaPäivänä {
          case l: PerusopetuksenOpiskeluoikeudenLisätiedot => Some(l.pidennettyOppivelvollisuus.toList)
          case l: PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot => Some(l.pidennettyOppivelvollisuus.toList)
        },
        joustavaPerusopetus = lisätietoVoimassaPäivänä {
          case l: PerusopetuksenOpiskeluoikeudenLisätiedot => Some(l.joustavaPerusopetus.toList)
          case l: PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot => Some(l.joustavaPerusopetus.toList)
        },
        koulukoti = lisätietoVoimassaPäivänä {
          case l: PerusopetuksenOpiskeluoikeudenLisätiedot => l.koulukoti
          case l: PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot => l.koulukoti
          case l: TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot => l.koulukoti
        },
        oppimääränSuorittaja = o.suoritukset.exists {
          case _: AikuistenPerusopetuksenOppimääränSuoritus => true
          case _ => false
        },
        oppisopimusJossainPäätasonSuorituksessa = oppisopimusAikajaksot(o).exists(_.contains(päivä)),
        maksuton = lisätietoVoimassaPäivänä {
          case l: MaksuttomuusTieto => l.maksuttomuus.map(ms => ms.filter(_.maksuton))
        },
        maksullinen = lisätietoVoimassaPäivänä {
          case l: MaksuttomuusTieto => l.maksuttomuus.map(ms => ms.filterNot(_.maksuton))
        },
        oikeuttaMaksuttomuuteenPidennetty = lisätietoVoimassaPäivänä {
          case l: MaksuttomuusTieto => l.oikeuttaMaksuttomuuteenPidennetty
        },
        kotiopetus = lisätietoVoimassaPäivänä {
          case l: PerusopetuksenOpiskeluoikeudenLisätiedot => Some(l.kotiopetusjaksot.toList.flatten ++ l.kotiopetus.toList)
        },
        ulkomaanjakso = lisätietoVoimassaPäivänä {
          case l: UlkomaanaikajaksojaSisältävä => Some(l.kaikkiUlkomaanaikajaksot)
        }
      ))
      // Note: When adding something here, remember to update aikajaksojenAlkupäivät (below), too
    }
  }

  object AikajaksoTyyppi {
    val ulkomaanjakso = "ULKOMAANJAKSO"
  }

  private def buildRAikajaksoRows(opiskeluoikeudenOid: String, o: Opiskeluoikeus): List[RAikajaksoRow] = {
    val ulkomaanjaksolliset = o.lisätiedot match {
      case Some(lisatiedot) => lisatiedot match {
        case a: UlkomaanaikajaksojaSisältävä => a.kaikkiUlkomaanaikajaksot.map(a => RAikajaksoRow(opiskeluoikeudenOid, AikajaksoTyyppi.ulkomaanjakso, Date.valueOf(a.alku), a.loppu.map(l => Date.valueOf(l))))
        case _ => Nil
      }
      case _ => Nil
    }
    ulkomaanjaksolliset
  }

  private def buildRAmmatillisenKoulutuksenJarjestamismuotoAikajaksoRows(opiskeluoikeudenOid: String, o: AmmatillinenOpiskeluoikeus): List[RAmmatillisenKoulutuksenJarjestamismuotoAikajaksoRow] = {
    val aikajaksot: List[RAmmatillisenKoulutuksenJarjestamismuotoAikajaksoRow] = o.suoritukset.flatMap({
      case a: Järjestämismuodollinen => a.järjestämismuodot.toList.flatten
      case _ => Nil
    }).flatMap {
      case Järjestämismuotojakso(alku, loppu, jm: JärjestämismuotoIlmanLisätietoja) if jm.tunniste.koodiarvo == "10" =>
        Some(RAmmatillisenKoulutuksenJarjestamismuotoAikajaksoRow(opiskeluoikeudenOid, jm.tunniste.nimi.map(_.get("fi")).getOrElse(""), Date.valueOf(alku), loppu.map(l => Date.valueOf(l)), None, None, None))
      case Järjestämismuotojakso(alku, loppu, jm: OppisopimuksellinenJärjestämismuoto) if jm.tunniste.koodiarvo == "20" =>
        Some(RAmmatillisenKoulutuksenJarjestamismuotoAikajaksoRow(opiskeluoikeudenOid, jm.tunniste.nimi.map(_.get("fi")).getOrElse(""), Date.valueOf(alku), loppu.map(l => Date.valueOf(l)), Some(jm.oppisopimus.työnantaja.yTunnus), jm.oppisopimus.oppisopimuksenPurkaminen.map(_.päivä).map(l => Date.valueOf(l)), jm.oppisopimus.oppisopimuksenPurkaminen.map(_.purettuKoeajalla)))
      case Järjestämismuotojakso(alku, loppu, jm: OppisopimuksellinenJärjestämismuoto) =>
        Some(RAmmatillisenKoulutuksenJarjestamismuotoAikajaksoRow(opiskeluoikeudenOid, jm.tunniste.nimi.map(_.get("fi")).getOrElse(""), Date.valueOf(alku), loppu.map(l => Date.valueOf(l)), Some(jm.oppisopimus.työnantaja.yTunnus), jm.oppisopimus.oppisopimuksenPurkaminen.map(_.päivä).map(l => Date.valueOf(l)), jm.oppisopimus.oppisopimuksenPurkaminen.map(_.purettuKoeajalla)))
      case _ => None
    }
    aikajaksot
  }

  private def buildROsaamisenHankkimistapaAikajaksoRows(opiskeluoikeudenOid: String, o: AmmatillinenOpiskeluoikeus): List[ROsaamisenhankkimistapaAikajaksoRow] = {
    val osaamisenHankkimistavat: List[ROsaamisenhankkimistapaAikajaksoRow] = o.suoritukset.flatMap({
      case a: OsaamisenHankkimistavallinen => a.osaamisenHankkimistavat.toList.flatten.map(jakso => (jakso, jakso.osaamisenHankkimistapa)).map({
        case (hankkimistapajakso, hankkimistapa: OsaamisenHankkimistapaIlmanLisätietoja) if hankkimistapa.tunniste.koodiarvo == "oppilaitosmuotoinenkoulutus" =>
          Some(
            ROsaamisenhankkimistapaAikajaksoRow(
              opiskeluoikeudenOid,
              aikajaksoTyyppi = hankkimistapa.tunniste.koodiarvo,
              alku = Date.valueOf(hankkimistapajakso.alku),
              loppu = hankkimistapajakso.loppu.map(a => Date.valueOf(a))
            )
          )
        case (hankkimistapajakso, hankkimistapa: OppisopimuksellinenOsaamisenHankkimistapa) if hankkimistapa.tunniste.koodiarvo == "oppisopimus" =>
          Some(
            ROsaamisenhankkimistapaAikajaksoRow(
              opiskeluoikeudenOid,
              aikajaksoTyyppi = hankkimistapa.tunniste.koodiarvo,
              alku = Date.valueOf(hankkimistapajakso.alku),
              loppu = hankkimistapajakso.loppu.map(a => Date.valueOf(a)),
              oppisopimusYTunnus = Some(hankkimistapa.oppisopimus.työnantaja.yTunnus),
              oppisopimusPurkamisenPäivä = hankkimistapa.oppisopimus.oppisopimuksenPurkaminen.map(p => Date.valueOf(p.päivä)),
              oppisopimusPurettuKoeajalla = hankkimistapa.oppisopimus.oppisopimuksenPurkaminen.map(_.purettuKoeajalla)
            )
          )
        case _ => None
      })
      case _ => None
    }).flatten

    val koulutussopimukset: List[ROsaamisenhankkimistapaAikajaksoRow] = o.suoritukset.flatMap({
      case a: Koulutussopimuksellinen => a.koulutussopimukset.toList.flatten.map(r =>
        Some(
          ROsaamisenhankkimistapaAikajaksoRow(
            opiskeluoikeudenOid,
            aikajaksoTyyppi = "koulutussopimus",
            alku = Date.valueOf(r.alku),
            loppu = r.loppu.map(a => Date.valueOf(a)),
            koulutussopimusPaikkakunta = Some(r.paikkakunta.koodiarvo),
            koulutussopimusMaa = Some(r.maa.koodiarvo),
            koulutussopimusYTunnus = r.työssäoppimispaikanYTunnus
          )
        )
      )
      case _ => None
    }).flatten

    osaamisenHankkimistavat ++ koulutussopimukset
  }

  private def buildEsiopetusAikajaksoRowForOneDay(opiskeluoikeudenOid: String, o: EsiopetuksenOpiskeluoikeus, päivä: LocalDate): Option[EsiopetusOpiskeluoikeusAikajaksoRow] = {
    val jakso = o.tila.opiskeluoikeusjaksot
      .filterNot(_.alku.isAfter(päivä))
      .lastOption.getOrElse(throw new RuntimeException(s"Opiskeluoikeusjaksoa ei löydy $opiskeluoikeudenOid $päivä"))
    val erityisenTuenPäätökset = o.lisätiedot.map(lt => (lt.erityisenTuenPäätös.toList ++ lt.erityisenTuenPäätökset.toList.flatten)).toList.flatten
    val päivänäAktiivisetPäätökset = erityisenTuenPäätökset.filter(_.contains(päivä))
    val aktiivistenErityisenTuenPäätöksienToteutuspaikat = päivänäAktiivisetPäätökset.flatMap(_.toteutuspaikka.map(_.koodiarvo))

    Some(EsiopetusOpiskeluoikeusAikajaksoRow(
      opiskeluoikeudenOid,
      alku = Date.valueOf(päivä),
      loppu = Date.valueOf(päivä),
      tila = jakso.tila.koodiarvo,
      tilaAlkanut = Date.valueOf(päivä),
      opiskeluoikeusPäättynyt = jakso.opiskeluoikeusPäättynyt,
      pidennettyOppivelvollisuus = o.lisätiedot.exists(_.pidennettyOppivelvollisuus.exists(_.contains(päivä))),
      tukimuodot = o.lisätiedot.flatMap(_.tukimuodot.map(_.map(_.koodiarvo))).map(_.mkString(";")),
      erityisenTuenPäätös = !päivänäAktiivisetPäätökset.isEmpty,
      erityisenTuenPäätösOpiskeleeToimintaAlueittain = päivänäAktiivisetPäätökset.exists(_.opiskeleeToimintaAlueittain),
      erityisenTuenPäätösErityisryhmässä = päivänäAktiivisetPäätökset.exists(_.erityisryhmässä.getOrElse(false)),
      erityisenTuenPäätösToteutuspaikka = if (aktiivistenErityisenTuenPäätöksienToteutuspaikat.isEmpty) None else Some(aktiivistenErityisenTuenPäätöksienToteutuspaikat.mkString(";")),
      vammainen = o.lisätiedot.exists(_.vammainen.exists(_.exists(_.contains(päivä)))),
      vaikeastiVammainen = o.lisätiedot.exists(_.vaikeastiVammainen.exists(_.exists(_.contains(päivä)))),
      majoitusetu = o.lisätiedot.exists(_.majoitusetu.exists(_.contains(päivä))),
      kuljetusetu = o.lisätiedot.exists(_.kuljetusetu.exists(_.contains(päivä))),
      sisäoppilaitosmainenMajoitus = o.lisätiedot.exists(_.sisäoppilaitosmainenMajoitus.exists(_.exists(_.contains(päivä)))),
      koulukoti = o.lisätiedot.exists(_.koulukoti.exists(_.exists(_.contains(päivä))))
    ))
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

  def toSeq[A <: Jakso](xs: Option[List[A]]*): Seq[Jakso] = xs.flatMap(_.getOrElse(Nil))

  private def aikajaksoMahdollisestiAlkamispäivällisestä
    (o: KoskeenTallennettavaOpiskeluoikeus)
    (m: MahdollisestiAlkupäivällinenJakso): Option[Jakso] = {
    List(m.alku, o.alkamispäivä).flatten.headOption match {
      case Some(alku) => Some(Aikajakso(alku, m.loppu))
      case None => None
    }
  }

  private def aikajaksotErityisenTuenPäätöksistä(
    erityisenTuenPäätös: Option[ErityisenTuenPäätös],
    erityisenTuenPäätökset: Option[List[ErityisenTuenPäätös]]
  ): List[Aikajakso] = {
    (erityisenTuenPäätös.toList ++ erityisenTuenPäätökset.toList.flatten)
      .flatMap(päätös => päätös.alku.map(Aikajakso(_, päätös.loppu)))
  }

  private def aikajaksotTuvaErityisenTuenPäätöksistä(
    erityisenTuenPäätökset: Option[List[TuvaErityisenTuenPäätös]]
  ): List[Aikajakso] = {
    erityisenTuenPäätökset.toList.flatten.flatMap(päätös => päätös.alku.map(Aikajakso(_, päätös.loppu)))
  }

  private def mahdollisetAikajaksojenAlkupäivät(o: KoskeenTallennettavaOpiskeluoikeus): Seq[LocalDate] = {
    // logiikka: uusi r_opiskeluoikeus_aikajakso-rivi pitää aloittaa, jos ko. päivänä alkaa joku jakso (erityinen tuki tms),
    // tai jos edellisenä päivänä on loppunut joku jakso.
    val lisätiedotAikajaksot: Seq[Jakso] = o.lisätiedot.collect {
      case aol: AmmatillisenOpiskeluoikeudenLisätiedot =>
        toSeq(
          aol.majoitus,
          aol.sisäoppilaitosmainenMajoitus,
          aol.vaativanErityisenTuenYhteydessäJärjestettäväMajoitus,
          aol.erityinenTuki,
          aol.vaativanErityisenTuenErityinenTehtävä,
          aol.vaikeastiVammainen,
          aol.vammainenJaAvustaja,
          aol.vankilaopetuksessa,
          aol.maksuttomuus,
        ) ++
          aol.opiskeluvalmiuksiaTukevatOpinnot.map(_.map(j => Aikajakso(j.alku, Some(j.loppu)))).toList.flatten ++
          aol.osaAikaisuusjaksot.map(_.map(j => Aikajakso(j.alku, j.loppu))).toList.flatten ++
          aol.hojks.toList.flatMap(aikajaksoMahdollisestiAlkamispäivällisestä(o)) ++
          aol.oikeuttaMaksuttomuuteenPidennetty.toList.flatten.map(j => Aikajakso(j.alku, Some(j.loppu))) ++
          aol.kaikkiUlkomaanaikajaksot
      case apol: AikuistenPerusopetuksenOpiskeluoikeudenLisätiedot =>
        toSeq(
          apol.sisäoppilaitosmainenMajoitus,
          apol.vammainen,
          apol.vaikeastiVammainen,
          apol.maksuttomuus
        ) ++ Seq(
          apol.majoitusetu
        ).flatten ++
          apol.oikeuttaMaksuttomuuteenPidennetty.toList.flatten.map(j => Aikajakso(j.alku, Some(j.loppu))) ++
          apol.kaikkiUlkomaanaikajaksot
      case pol: PerusopetuksenOpiskeluoikeudenLisätiedot =>
        toSeq(
          pol.sisäoppilaitosmainenMajoitus,
          pol.vammainen,
          pol.vaikeastiVammainen,
          pol.koulukoti,
          pol.kotiopetusjaksot
        ) ++ Seq(
          pol.majoitusetu,
          pol.kuljetusetu,
          pol.pidennettyOppivelvollisuus,
          pol.joustavaPerusopetus,
          pol.kotiopetus
        ).flatten ++ aikajaksotErityisenTuenPäätöksistä(pol.erityisenTuenPäätös, pol.erityisenTuenPäätökset) ++
          pol.kaikkiUlkomaanaikajaksot
      case poll: PerusopetuksenLisäopetuksenOpiskeluoikeudenLisätiedot =>
        toSeq(
          poll.sisäoppilaitosmainenMajoitus,
          poll.vammainen,
          poll.vaikeastiVammainen,
          poll.koulukoti,
          poll.kotiopetusjaksot,
          poll.maksuttomuus
        ) ++ Seq(
          poll.majoitusetu,
          poll.kuljetusetu,
          poll.pidennettyOppivelvollisuus,
          poll.joustavaPerusopetus
        ).flatten ++ aikajaksotErityisenTuenPäätöksistä(poll.erityisenTuenPäätös, poll.erityisenTuenPäätökset) ++
          poll.oikeuttaMaksuttomuuteenPidennetty.toList.flatten.map(j => Aikajakso(j.alku, Some(j.loppu))) ++
          poll.kaikkiUlkomaanaikajaksot
      case lol: LukionOpiskeluoikeudenLisätiedot =>
        toSeq(
          lol.sisäoppilaitosmainenMajoitus,
          lol.erityisenKoulutustehtävänJaksot,
          lol.maksuttomuus
        ) ++
          lol.oikeuttaMaksuttomuuteenPidennetty.toList.flatten.map(j => Aikajakso(j.alku, Some(j.loppu))) ++
          lol.kaikkiUlkomaanaikajaksot
      case isol: InternationalSchoolOpiskeluoikeudenLisätiedot =>
        toSeq(
          isol.erityisenKoulutustehtävänJaksot,
          isol.maksuttomuus
        ) ++
          isol.oikeuttaMaksuttomuuteenPidennetty.toList.flatten.map(j => Aikajakso(j.alku, Some(j.loppu))) ++
          isol.kaikkiUlkomaanaikajaksot
      case eshol: EuropeanSchoolOfHelsinkiOpiskeluoikeudenLisätiedot =>
        toSeq(
          eshol.maksuttomuus
        ) ++
          eshol.oikeuttaMaksuttomuuteenPidennetty.toList.flatten.map(j => Aikajakso(j.alku, Some(j.loppu))) ++
          eshol.kaikkiUlkomaanaikajaksot
      case dol: DIAOpiskeluoikeudenLisätiedot =>
        toSeq(
          dol.erityisenKoulutustehtävänJaksot,
          dol.maksuttomuus
        ) ++
          dol.oikeuttaMaksuttomuuteenPidennetty.toList.flatten.map(j => Aikajakso(j.alku, Some(j.loppu))) ++
          dol.kaikkiUlkomaanaikajaksot
      case lvol: LukioonValmistavanKoulutuksenOpiskeluoikeudenLisätiedot =>
        toSeq(
          lvol.sisäoppilaitosmainenMajoitus,
          lvol.maksuttomuus
        ) ++
          lvol.oikeuttaMaksuttomuuteenPidennetty.toList.flatten.map(j => Aikajakso(j.alku, Some(j.loppu))) ++
          lvol.kaikkiUlkomaanaikajaksot
      case eol: EsiopetuksenOpiskeluoikeudenLisätiedot =>
        toSeq(
          eol.vammainen,
          eol.vaikeastiVammainen,
          eol.sisäoppilaitosmainenMajoitus,
          eol.koulukoti
        ) ++ Seq(
          eol.pidennettyOppivelvollisuus,
          eol.majoitusetu,
          eol.kuljetusetu
        ).flatten ++ aikajaksotErityisenTuenPäätöksistä(eol.erityisenTuenPäätös, eol.erityisenTuenPäätökset)
      case vstol: VapaanSivistystyönOpiskeluoikeudenLisätiedot =>
        toSeq(
          vstol.maksuttomuus
        ) ++ vstol.oikeuttaMaksuttomuuteenPidennetty.toList.flatten.map(j => Aikajakso(j.alku, Some(j.loppu)))
      case tall: TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot =>
        toSeq(
          tall.maksuttomuus,
          tall.majoitus,
          tall.sisäoppilaitosmainenMajoitus,
          tall.vaativanErityisenTuenYhteydessäJärjestettäväMajoitus,
          tall.erityinenTuki,
          tall.vaativanErityisenTuenErityinenTehtävä,
          tall.vaikeastiVammainen,
          tall.vammainenJaAvustaja,
          tall.vankilaopetuksessa,
        ) ++ tall.oikeuttaMaksuttomuuteenPidennetty.toList.flatten.map(j => Aikajakso(j.alku, Some(j.loppu))) ++
          tall.osaAikaisuusjaksot.map(_.map(j => Aikajakso(j.alku, j.loppu))).toList.flatten ++
          tall.kaikkiUlkomaanaikajaksot
      case tlll: TutkintokoulutukseenValmentavanOpiskeluoikeudenLukiokoulutuksenLuvanLisätiedot =>
        toSeq(
          tlll.maksuttomuus,
          tlll.sisäoppilaitosmainenMajoitus
        ) ++ tlll.oikeuttaMaksuttomuuteenPidennetty.toList.flatten.map(j => Aikajakso(j.alku, Some(j.loppu))) ++
          tlll.kaikkiUlkomaanaikajaksot
      case tpll: TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot =>
        toSeq(
          tpll.maksuttomuus,
          tpll.vammainen,
          tpll.vaikeastiVammainen,
          tpll.sisäoppilaitosmainenMajoitus,
          tpll.koulukoti
        ) ++ Seq(
          tpll.majoitusetu,
          tpll.kuljetusetu
        ).flatten ++
          aikajaksotTuvaErityisenTuenPäätöksistä(tpll.erityisenTuenPäätökset) ++
          tpll.oikeuttaMaksuttomuuteenPidennetty.toList.flatten.map(j => Aikajakso(j.alku, Some(j.loppu))) ++
          tpll.kaikkiUlkomaanaikajaksot
    }.getOrElse(Nil)

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
      case s: Järjestämismuodollinen with OsaamisenHankkimistavallinen => convert(s.järjestämismuodot, s.osaamisenHankkimistavat)
      case s: Järjestämismuodollinen => convert(s.järjestämismuodot, None)
      case s: OsaamisenHankkimistavallinen => convert(None, s.osaamisenHankkimistavat)
      case _ => Seq.empty
    }
  }
}
