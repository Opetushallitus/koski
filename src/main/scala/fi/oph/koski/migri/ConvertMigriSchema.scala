package fi.oph.koski.migri

import fi.oph.koski.schema._


object ConvertMigriSchema {

  def convert(oppija: Oppija): Option[MigriOppija] = {
    val opiskeluoikeudet = oppija.opiskeluoikeudet
      .filter(migriäKiinnostavaOpiskeluoikeus)
      .flatMap(opiskeluoikeus => {
        val suoritukset = opiskeluoikeus.suoritukset.filter(migriäKiinnostavaPäätasonSuoritus)
        if (suoritukset.isEmpty) {
          None
        } else {
          Some(opiskeluoikeus.withSuoritukset(suoritukset))
        }
      })

    if (opiskeluoikeudet.isEmpty) {
      None
    } else {
      Some(MigriOppija(convertHenkilö(oppija.henkilö), opiskeluoikeudet.map(convertOpiskeluoikeus).toList))
    }
  }

  private def migriäKiinnostavaOpiskeluoikeus(opiskeluoikeus: Opiskeluoikeus) = opiskeluoikeus match {
    case _: AikuistenPerusopetuksenOpiskeluoikeus |
         _: AmmatillinenOpiskeluoikeus |
         _: DIAOpiskeluoikeus |
         _: IBOpiskeluoikeus |
         _: InternationalSchoolOpiskeluoikeus |
         // TODO: TOR-1685 Eurooppalainen koulu
         _: KorkeakoulunOpiskeluoikeus |
         _: LukionOpiskeluoikeus |
         _: PerusopetuksenOpiskeluoikeus |
         _: YlioppilastutkinnonOpiskeluoikeus => true
    case _ => false
  }

  private def migriäKiinnostavaPäätasonSuoritus(suoritus: PäätasonSuoritus)= suoritus match {
    case _: AikuistenPerusopetuksenOppimääränSuoritus |
         _: AmmatillisenTutkinnonOsittainenSuoritus |
         _: AmmatillisenTutkinnonSuoritus |
         _: DIAValmistavanVaiheenSuoritus |
         _: DIATutkinnonSuoritus |
         _: IBTutkinnonSuoritus |
         _: PreIBSuoritus2015 |
         _: PreIBSuoritus2019 |
         _: DiplomaVuosiluokanSuoritus |
         // TODO: TOR-1685 Eurooppalainen koulu
         _: KorkeakoulunOpintojaksonSuoritus |
         _: KorkeakoulututkinnonSuoritus |
         _: MuuKorkeakoulunSuoritus |
         _: LukionOppimääränSuoritus2015 |
         _: LukionOppimääränSuoritus2019 |
         _: NuortenPerusopetuksenOppimääränSuoritus |
         _: PerusopetuksenVuosiluokanSuoritus |
         _: YlioppilastutkinnonSuoritus => true
    case _ => false
  }

  private def convertHenkilö(h: Henkilö) = h match {
    case henkilö: TäydellisetHenkilötiedot =>
      MigriHenkilo(
        oid = henkilö.oid,
        hetu = henkilö.hetu,
        syntymäaika = henkilö.syntymäaika,
        etunimet = henkilö.etunimet,
        sukunimi = henkilö.sukunimi,
        kansalaisuus = henkilö.kansalaisuus
      )
    case _ => throw new RuntimeException("Unreachable match arm, expected TäydellisetHenkilötiedot, got " + h.toString)
  }

  private def convertOpiskeluoikeus(opiskeluoikeus: Opiskeluoikeus): MigriOpiskeluoikeus = {
    MigriOpiskeluoikeus(
      oid = opiskeluoikeus.oid,
      aikaleima = opiskeluoikeus match {
        case x: KoskeenTallennettavaOpiskeluoikeus => x.aikaleima
        case _ => None
      },
      oppilaitos = opiskeluoikeus.oppilaitos.map(_.nimi).map(MigriOppilaitos),
      tila = MigriOpiskeluoikeudenTila(opiskeluoikeus.tila.opiskeluoikeusjaksot.map(jakso =>
        MigriOpiskeluoikeusJakso(jakso.alku, jakso.tila)
      )),
      arvioituPäättymispäivä = opiskeluoikeus.arvioituPäättymispäivä,
      alkamispäivä = opiskeluoikeus.alkamispäivä,
      päättymispäivä = opiskeluoikeus.päättymispäivä,
      tyyppi = opiskeluoikeus.tyyppi,
      lisätiedot = opiskeluoikeus.lisätiedot.map(lisätiedot =>
        MigriOpiskeluoikeudenLisätiedot(
          virtaOpiskeluoikeudenTyyppi = lisätiedot match {
            case x: KorkeakoulunOpiskeluoikeudenLisätiedot => x.virtaOpiskeluoikeudenTyyppi
            case _ => None
          },
          lukukausiIlmoittautuminen = lisätiedot match {
            case x: KorkeakoulunOpiskeluoikeudenLisätiedot => x.lukukausiIlmoittautuminen.map(ilmo =>
              MigriLukukausi_Ilmoittautuminen(
                ilmoittautumisjaksot = ilmo.ilmoittautumisjaksot.map(jakso =>
                  MigriLukukausi_Ilmoittautumisjakso(
                    alku = jakso.alku,
                    loppu = jakso.loppu,
                    tila = jakso.tila,
                    maksetutLukuvuosimaksut = jakso.maksetutLukuvuosimaksut.map(maksu =>
                      MigriLukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu(
                        maksettu = maksu.maksettu,
                        summa = maksu.summa,
                        apuraha = maksu.apuraha
                      )
                    )
                  )
                )
              )
            )
            case _ => None
          },
          maksettavatLukuvuosimaksut = lisätiedot match {
            case x: KorkeakoulunOpiskeluoikeudenLisätiedot => x.maksettavatLukuvuosimaksut.map(_.toList)
            case _ => None
          },
          majoitusetu = lisätiedot match {
            case x: Majoitusetuinen => x.majoitusetu
            case _ => None
          },
          majoitus = lisätiedot match {
            case x: AmmatillisenOpiskeluoikeudenLisätiedot => x.majoitus
            case _ => None
          },
          sisäoppilaitosmainenMajoitus = lisätiedot match {
            case x: SisäoppilaitosmainenMajoitus => x.sisäoppilaitosmainenMajoitus
            case _ => None
          },
          ulkomaanjaksot = lisätiedot match {
            case x: Ulkomaanjaksollinen => x.ulkomaanjaksot.map(_.map(jakso => Aikajakso(jakso.alku, jakso.loppu)))
            case _ => None
          },
          koulutusvienti = lisätiedot match {
            case x: AmmatillisenOpiskeluoikeudenLisätiedot => Some(x.koulutusvienti)
            case _ => None
          }
        )
      ),
      suoritukset = opiskeluoikeus.suoritukset.map(suoritus =>
        MigriSuoritus(
          koulutusmoduuli = MigriSuorituksenKoulutusmoduuli(
            tunniste = convertKoodistoviite(suoritus.koulutusmoduuli.tunniste),
            diplomaType = suoritus.koulutusmoduuli match {
              case x: DiplomaLuokkaAste => Some(x.diplomaType)
              // TODO: TOR-1685 Eurooppalainen koulu, jos tarvitsee
              case _ => None
            },
            nimi = suoritus.koulutusmoduuli.nimi,
            laajuus = suoritus.koulutusmoduuli.getLaajuus
          ),
          vahvistus = suoritus.vahvistus.map(v => MigriVahvistus(päivä = v.päivä)),
          suoritustapa = suoritus match {
            case x: AmmatillisenTutkinnonOsittainenTaiKokoSuoritus => Some(x.suoritustapa)
            case x: SuoritustavallinenPerusopetuksenSuoritus => Some(x.suoritustapa)
            case x: SuoritustapanaMahdollisestiErityinenTutkinto => x.suoritustapa
            case _ => None
          },
          suorituskieli = suoritus match {
            case s: Suorituskielellinen => Some(s.suorituskieli)
            case s: MahdollisestiSuorituskielellinen => s.suorituskieli
            case _ => None
          },
          tyyppi = suoritus.tyyppi,
          tutkintonimike = suoritus match {
            case x: Tutkintonimikkeellinen => x.tutkintonimike
            case _ => None
          },
          alkamispäivä = suoritus.alkamispäivä,
          osaamisenHankkimistapa = suoritus match {
            case x: OsaamisenHankkimistavallinen => x.osaamisenHankkimistavat.map(_.map(jakso =>
              MigriOsaamisenHankkimistapajakso(
                alku = jakso.alku,
                loppu = jakso.loppu,
                osaamisenHankkimistapa = MigriOsaamisenHankkimistapa(
                  tunniste = jakso.osaamisenHankkimistapa.tunniste,
                  oppisopimus = jakso.osaamisenHankkimistapa match {
                    case x: OppisopimuksellinenOsaamisenHankkimistapa => Some(MigriOppisopimus(x.oppisopimus.työnantaja))
                    case _ => None
                  }
                )
              )
            ))
            case _ => None
          },
          theoryOfKnowledge = suoritus match {
            case x: IBTutkinnonSuoritus => x.theoryOfKnowledge
            case _ => None
          },
          extendedEssay = suoritus match {
            case x: IBTutkinnonSuoritus => x.extendedEssay
            case _ => None
          },
          creativityActionService = suoritus match {
            case x: IBTutkinnonSuoritus => x.creativityActionService
            case _ => None
          },
          oppimäärä = suoritus match {
            case x: Oppimäärällinen => Some(x.oppimäärä)
            case _ => None
          },
          suoritettuErityisenäTutkintona = suoritus match {
            case x: SuoritettavissaErityisenäTutkintona2019 => Some(x.suoritettuErityisenäTutkintona)
            case _ => None
          },
          luokka = suoritus match {
            case x: PerusopetuksenLisäopetuksenSuoritus => x.luokka
            case x: PerusopetuksenVuosiluokanSuoritus => Some(x.luokka)
            case _ => None
          },
          pakollisetKokeetSuoritettu = suoritus match {
            case x: YlioppilastutkinnonSuoritus => Some(x.pakollisetKokeetSuoritettu)
            case _ => None
          },
          osasuoritukset = suoritus.osasuoritukset.map(_.map(osasuoritus =>
            MigriOsasuoritus(
              koulutusmoduuli = MigriOsasuorituksenKoulutusmoduuli(
                tunniste = convertKoodistoviite(osasuoritus.koulutusmoduuli.tunniste),
                nimi = osasuoritus.koulutusmoduuli.nimi,
                oppimäärä = osasuoritus.koulutusmoduuli match {
                  case x: Oppimäärä => Some(x.oppimäärä)
                  case _ => None
                },
                kieli = osasuoritus.koulutusmoduuli match {
                  case x: Kieliaine => Some(x.kieli)
                  case _ => None
                },
                pakollinen = osasuoritus.koulutusmoduuli match {
                  case x: Valinnaisuus => Some(x.pakollinen)
                  case _ => None
                },
                laajuus = osasuoritus.koulutusmoduuli.getLaajuus
              ),
              arviointi = osasuoritus.arviointi.map(_.map(a =>
                MigriArviointi(
                  arvosana = convertKoodistoviite(a.arvosana),
                  hyväksytty = a.hyväksytty,
                  arviointiPäivä = a.arviointipäivä
                )
              )),
              tyyppi = osasuoritus.tyyppi,
              tutkinnonOsanRyhmä = osasuoritus match {
                case s: TutkinnonOsanSuoritus => s.tutkinnonOsanRyhmä
                case _ => None
              },
              tunnustettu = osasuoritus match {
                case x: MahdollisestiTunnustettu
                  if lisätiedotKoodiarvonTunnisteEquals("mukautettu", osasuoritus) =>
                  x.tunnustettu.map(tunnustaminen => MigriOsaamisenTunnustaminen(tunnustaminen.selite))
                case _ => None
              },
              lisätiedot = osasuoritus match {
                case x: TutkinnonOsanSuoritus => x.lisätiedot
                case _ => None
              },
              suoritettuErityisenäTutkintona = osasuoritus match {
                case x: SuoritettavissaErityisenäTutkintona2019 => Some(x.suoritettuErityisenäTutkintona)
                case _ => None
              },
              osasuoritukset = osasuoritus.osasuoritukset.map(_.map(osasuorituksenOsasuoritus =>
                MigriOsasuorituksenOsasuoritus(
                  koulutusmoduuli = MigriOsasuorituksenKoulutusmoduuli(
                    tunniste = convertKoodistoviite(osasuorituksenOsasuoritus.koulutusmoduuli.tunniste),
                    nimi = osasuorituksenOsasuoritus.koulutusmoduuli.nimi,
                    oppimäärä = osasuorituksenOsasuoritus.koulutusmoduuli match {
                      case x: Oppimäärä => Some(x.oppimäärä)
                      case _ => None
                    },
                    kieli = osasuorituksenOsasuoritus.koulutusmoduuli match {
                      case x: Kieliaine => Some(x.kieli)
                      case _ => None
                    },
                    pakollinen = osasuorituksenOsasuoritus.koulutusmoduuli match {
                      case x: Valinnaisuus => Some(x.pakollinen)
                      case _ => None
                    },
                    laajuus = osasuorituksenOsasuoritus.koulutusmoduuli.getLaajuus
                  ),
                  arviointi = osasuorituksenOsasuoritus.arviointi.map(_.map(a =>
                    MigriArviointi(
                      arvosana = convertKoodistoviite(a.arvosana),
                      hyväksytty = a.hyväksytty,
                      arviointiPäivä = a.arviointipäivä
                    )
                  )),
                  tyyppi = osasuorituksenOsasuoritus.tyyppi,
                  tunnustettu = osasuorituksenOsasuoritus  match {
                    case x: MahdollisestiTunnustettu
                      if lisätiedotKoodiarvonTunnisteEquals("mukautettu", osasuorituksenOsasuoritus) =>
                      x.tunnustettu.map(tunnustaminen => MigriOsaamisenTunnustaminen(tunnustaminen.selite))
                    case _ => None
                  },
                  lisätiedot = osasuorituksenOsasuoritus match {
                    case x: TutkinnonOsanSuoritus => x.lisätiedot
                    case _ => None
                  }
                )
              ))
            )
          ))
        )
      )
    )
  }

  private def convertKoodistoviite(koodiviite: KoodiViite): MigriKoodiviite = {
    koodiviite match {
      case k: Koodistokoodiviite => MigriKoodiviite(
        koodiarvo = k.koodiarvo,
        nimi = k.nimi,
        lyhytNimi = k.lyhytNimi,
        koodistoUri = Some(k.koodistoUri),
        koodistoVersio = k.koodistoVersio
      )
      case p: PaikallinenKoodiviite => MigriKoodiviite(
        koodiarvo = p.koodiarvo,
        nimi = p.getNimi,
        lyhytNimi = None,
        koodistoUri = None,
        koodistoVersio = None
      )
    }
  }

  private def lisätiedotKoodiarvonTunnisteEquals(koodiarvo: String, suoritus: Suoritus): Boolean = suoritus match {
    case t: TutkinnonOsanSuoritus => t.lisätiedot.exists(_.exists(_.tunniste.koodiarvo == koodiarvo))
    case _ => false
  }
}
