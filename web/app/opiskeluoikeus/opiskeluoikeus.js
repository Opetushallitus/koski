const internationalSchoolTilat = [
  'eronnut',
  'lasna',
  'valmistunut',
  'valiaikaisestikeskeytynyt'
]
const europeanSchoolOfHelsinkiTilat = [
  'eronnut',
  'lasna',
  'valmistunut',
  'valiaikaisestikeskeytynyt',
  'mitatoity'
]
const oppivelvollisilleSuunnatunVapaanSivistystyönKoulutuksenTilat = [
  'lasna',
  'valiaikaisestikeskeytynyt',
  'katsotaaneronneeksi',
  'valmistunut'
]
const vapaatavoitteisenVapaanSivistystyönKoulutuksenTilat = [
  'hyvaksytystisuoritettu',
  'keskeytynyt'
]
const jatkuvanOppimisenVapaanSivistystyönKoulutuksenTilat = [
  'hyvaksytystisuoritettu',
  'keskeytynyt',
  'lasna'
]
const tuvaTilat = [
  'katsotaaneronneeksi',
  'lasna',
  'valiaikaisestikeskeytynyt',
  'valmistunut'
]
const muunKuinSäännellynKoulutuksenTilat = [
  'lasna',
  'hyvaksytystisuoritettu',
  'keskeytynyt'
]
const vainVstJaMuksKoulutuksissaKäytettävätTilat = [
  'hyvaksytystisuoritettu',
  'keskeytynyt'
]
const taiteenPerusopetuksenTilat = [
  'lasna',
  'hyvaksytystisuoritettu',
  'paattynyt'
]
const tuvaAmmatillinenTilat = [...tuvaTilat, 'loma']
const alwaysExclude = ['mitatoity']

const defaultGetKoodiarvo = (x) => x && x.koodiarvo
export const filterTilatByOpiskeluoikeudenJaSuorituksenTyyppi =
  (
    opiskeluoikeudenTyyppi,
    tuvaJärjestämislupa,
    suorituksenTyyppi,
    koodiarvo = defaultGetKoodiarvo
  ) =>
  (tilat) => {
    const prefilteredTilat = tilat.filter(
      (t) => !alwaysExclude.includes(koodiarvo(t))
    )
    return filterBySuorituksenTyyppi(
      opiskeluoikeudenTyyppi,
      suorituksenTyyppi,
      filterByOpiskeluoikeudenTyyppi(
        opiskeluoikeudenTyyppi,
        tuvaJärjestämislupa,
        prefilteredTilat,
        koodiarvo
      ),
      koodiarvo
    )
  }

const filterByOpiskeluoikeudenTyyppi = (
  opiskeluoikeudenTyyppi,
  tuvaJärjestämislupa,
  tilat,
  koodiarvo
) => {
  switch (opiskeluoikeudenTyyppi && opiskeluoikeudenTyyppi.koodiarvo) {
    case 'perusopetukseenvalmistavaopetus':
      return tilat.filter((t) => koodiarvo(t) !== 'paattynyt')
    case 'ammatillinenkoulutus':
      return tilat.filter(
        (t) => koodiarvo(t) !== 'eronnut' && koodiarvo(t) !== 'paattynyt'
      )
    case 'internationalschool':
      return tilat.filter((t) =>
        internationalSchoolTilat.includes(koodiarvo(t))
      )
    case 'europeanschoolofhelsinki':
      return tilat.filter((t) =>
        europeanSchoolOfHelsinkiTilat.includes(koodiarvo(t))
      )
    case 'tuva':
      return filterByJärjestämislupa(tuvaJärjestämislupa, tilat, koodiarvo)
    case 'taiteenperusopetus':
      return tilat.filter((t) =>
        taiteenPerusopetuksenTilat.includes(koodiarvo(t))
      )
    default:
      return tilat.filter(
        (t) => koodiarvo(t) !== 'loma' && koodiarvo(t) !== 'paattynyt'
      )
  }
}

const filterByJärjestämislupa = (tuvaJärjestämislupa, tilat, koodiarvo) => {
  switch (tuvaJärjestämislupa && tuvaJärjestämislupa.koodiarvo) {
    case 'ammatillinen':
      return tilat.filter((t) => tuvaAmmatillinenTilat.includes(koodiarvo(t)))
    default:
      return tilat.filter((t) => tuvaTilat.includes(koodiarvo(t)))
  }
}

const filterBySuorituksenTyyppi = (
  opiskeluoikeudenTyyppi,
  suorituksenTyyppi,
  tilat,
  koodiarvo
) => {
  const includedIn = (koodiarvot) => (tila) =>
    koodiarvot.includes(koodiarvo(tila))
  const notIncludedIn = (koodiarvot) => (tila) =>
    !koodiarvot.includes(koodiarvo(tila))

  if (opiskeluoikeudenTyyppi?.koodiarvo === 'vapaansivistystyonkoulutus') {
    const sallitutTilatSuoritustyypeittäin = {
      vstoppivelvollisillesuunnattukoulutus:
        oppivelvollisilleSuunnatunVapaanSivistystyönKoulutuksenTilat,
      vstmaahanmuuttajienkotoutumiskoulutus:
        oppivelvollisilleSuunnatunVapaanSivistystyönKoulutuksenTilat,
      vstlukutaitokoulutus:
        oppivelvollisilleSuunnatunVapaanSivistystyönKoulutuksenTilat,
      vstvapaatavoitteinenkoulutus:
        vapaatavoitteisenVapaanSivistystyönKoulutuksenTilat,
      vstjotpakoulutus: jatkuvanOppimisenVapaanSivistystyönKoulutuksenTilat
    }

    const sallitutTilat =
      sallitutTilatSuoritustyypeittäin[suorituksenTyyppi?.koodiarvo]

    return sallitutTilat ? tilat.filter(includedIn(sallitutTilat)) : []
  }

  if (opiskeluoikeudenTyyppi?.koodiarvo === 'muukuinsaanneltykoulutus') {
    return tilat.filter(includedIn(muunKuinSäännellynKoulutuksenTilat))
  }

  return tilat.filter(notIncludedIn(vainVstJaMuksKoulutuksissaKäytettävätTilat))
}
