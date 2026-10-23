import { describe, expect, it } from 'vitest'

import { shouldShowLaajuusColumn } from '../../app/perusopetus-v2/oppiaineLaajuus'

const laajuudellinenOppiaine = {
  koulutusmoduuli: {
    laajuus: {
      arvo: 4
    }
  }
}

const laajuudetonOppiaine = {
  koulutusmoduuli: {}
}

const suoritus = (vahvistusPäivä?: string) => ({
  vahvistus: vahvistusPäivä ? { päivä: vahvistusPäivä } : undefined
})

describe('perusopetus v2 oppiaineen laajuussarake', () => {
  it('näkyy muokkaustilassa', () => {
    expect(
      shouldShowLaajuusColumn({
        editMode: true,
        sisältääToimintaAlueita: false,
        pakollinen: true,
        suoritus: suoritus(),
        osasuoritukset: []
      })
    ).toBe(true)
  })

  it('ei näy pakollisille ilman laajuudellisia suorituksia', () => {
    expect(
      shouldShowLaajuusColumn({
        editMode: false,
        sisältääToimintaAlueita: false,
        pakollinen: true,
        suoritus: suoritus('2020-08-01'),
        osasuoritukset: [laajuudetonOppiaine]
      })
    ).toBe(false)
  })

  it('ei näy pakollisille ennen leikkuripäivää', () => {
    expect(
      shouldShowLaajuusColumn({
        editMode: false,
        sisältääToimintaAlueita: false,
        pakollinen: true,
        suoritus: suoritus('2020-07-31'),
        osasuoritukset: [laajuudellinenOppiaine]
      })
    ).toBe(false)
  })

  it('näkyy pakollisille leikkuripäivästä alkaen', () => {
    expect(
      shouldShowLaajuusColumn({
        editMode: false,
        sisältääToimintaAlueita: false,
        pakollinen: true,
        suoritus: suoritus('2020-08-01'),
        osasuoritukset: [laajuudellinenOppiaine]
      })
    ).toBe(true)
  })

  it('näkyy toiminta-alueittain leikkuripäivästä alkaen', () => {
    expect(
      shouldShowLaajuusColumn({
        editMode: false,
        sisältääToimintaAlueita: true,
        pakollinen: undefined,
        suoritus: suoritus('2020-08-01'),
        osasuoritukset: [laajuudellinenOppiaine]
      })
    ).toBe(true)
  })

  it('näkyy valinnaisille ilman vahvistuspäiväleikkuria', () => {
    expect(
      shouldShowLaajuusColumn({
        editMode: false,
        sisältääToimintaAlueita: false,
        pakollinen: false,
        suoritus: suoritus(),
        osasuoritukset: [laajuudellinenOppiaine]
      })
    ).toBe(true)
  })
})
