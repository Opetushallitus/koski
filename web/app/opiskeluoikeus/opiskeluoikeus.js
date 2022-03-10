const internationalSchoolTilat = [ 'eronnut', 'lasna', 'valmistunut', 'valiaikaisestikeskeytynyt' ]
const vapaatavoitteisenVapaanSivistystyönKoulutuksenTilat = ['hyvaksytystisuoritettu', 'keskeytynyt']
const tuvaTilat = ['katsotaaneronneeksi', 'lasna', 'valiaikaisestikeskeytynyt', 'valmistunut']
const alwaysExclude = ['mitatoity']

const defaultGetKoodiarvo = x => x && x.koodiarvo
export const filterTilatByOpiskeluoikeudenJaSuorituksenTyyppi = (opiskeluoikeudenTyyppi, suorituksenTyyppi, koodiarvo = defaultGetKoodiarvo) => tilat => {
  const prefilteredTilat = tilat.filter(t => !alwaysExclude.includes(koodiarvo(t)))
  return filterBySuorituksenTyyppi(
    suorituksenTyyppi,
    filterByOpiskeluoikeudenTyyppi(opiskeluoikeudenTyyppi, prefilteredTilat, koodiarvo),
    koodiarvo
  )
}

const filterByOpiskeluoikeudenTyyppi = (opiskeluoikeudenTyyppi, tilat, koodiarvo) => {
  switch (opiskeluoikeudenTyyppi && opiskeluoikeudenTyyppi.koodiarvo) {
    case 'perusopetukseenvalmistavaopetus': return tilat
    case 'ammatillinenkoulutus': return tilat.filter(t => koodiarvo(t) !== 'eronnut')
    case 'internationalschool': return tilat.filter(t => internationalSchoolTilat.includes(koodiarvo(t)))
    case 'tuva': return tilat.filter(t => tuvaTilat.includes(koodiarvo(t)))
    default: return tilat.filter(t => koodiarvo(t) !== 'loma')
  }
}

const filterBySuorituksenTyyppi = (suorituksenTyyppi, tilat, koodiarvo) => {
  switch (suorituksenTyyppi && suorituksenTyyppi.koodiarvo) {
    case 'vstvapaatavoitteinenkoulutus': return tilat.filter(t => vapaatavoitteisenVapaanSivistystyönKoulutuksenTilat.includes(koodiarvo(t)))
    default: return tilat.filter(t => !vapaatavoitteisenVapaanSivistystyönKoulutuksenTilat.includes(koodiarvo(t)))
  }
}
