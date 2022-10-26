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
      return tilat
    case 'ammatillinenkoulutus':
      return tilat.filter((t) => koodiarvo(t) !== 'eronnut')
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
    default:
      return tilat.filter((t) => koodiarvo(t) !== 'loma')
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

const filterBySuorituksenTyyppi = (suorituksenTyyppi, tilat, koodiarvo) => {
  switch (suorituksenTyyppi && suorituksenTyyppi.koodiarvo) {
    case 'vstvapaatavoitteinenkoulutus':
      return tilat.filter((t) =>
        vapaatavoitteisenVapaanSivistystyönKoulutuksenTilat.includes(
          koodiarvo(t)
        )
      )
    case 'vstjotpakoulutus':
      return tilat.filter((t) =>
        jatkuvanOppimisenVapaanSivistystyönKoulutuksenTilat.includes(
          koodiarvo(t)
        )
      )
    default:
      return tilat.filter(
        (t) =>
          !vapaatavoitteisenVapaanSivistystyönKoulutuksenTilat.includes(
            koodiarvo(t)
          )
      )
  }
}
