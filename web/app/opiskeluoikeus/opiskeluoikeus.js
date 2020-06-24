const internationalSchoolTilat = [ 'eronnut', 'lasna', 'valmistunut', 'valiaikaisestikeskeytynyt' ]
const alwaysExclude = ['mitatoity']

const defaultGetKoodiarvo = x => x && x.koodiarvo
export const filterTilatByOpiskeluoikeudenTyyppi = (tyyppi, koodiarvo = defaultGetKoodiarvo) => tilat => {
  const prefilteredTilat = tilat.filter(t => !alwaysExclude.includes(koodiarvo(t)))
  switch (tyyppi && tyyppi.koodiarvo) {
    case 'perusopetukseenvalmistavaopetus': return prefilteredTilat
    case 'ammatillinenkoulutus': return prefilteredTilat.filter(t => koodiarvo(t) !== 'eronnut')
    case 'internationalschool': return prefilteredTilat.filter(t => internationalSchoolTilat.includes(koodiarvo(t)))
    default: return prefilteredTilat.filter(t => koodiarvo(t) !== 'loma')
  }
}
