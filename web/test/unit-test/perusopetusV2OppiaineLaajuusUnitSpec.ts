import * as assert from 'assert'
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
    assert.strictEqual(
      shouldShowLaajuusColumn({
        editMode: true,
        isToimintaAlueittain: false,
        pakollinen: true,
        suoritus: suoritus(),
        osasuoritukset: []
      }),
      true
    )
  })

  it('ei näy pakollisille ilman laajuudellisia suorituksia', () => {
    assert.strictEqual(
      shouldShowLaajuusColumn({
        editMode: false,
        isToimintaAlueittain: false,
        pakollinen: true,
        suoritus: suoritus('2020-08-01'),
        osasuoritukset: [laajuudetonOppiaine]
      }),
      false
    )
  })

  it('ei näy pakollisille ennen leikkuripäivää', () => {
    assert.strictEqual(
      shouldShowLaajuusColumn({
        editMode: false,
        isToimintaAlueittain: false,
        pakollinen: true,
        suoritus: suoritus('2020-07-31'),
        osasuoritukset: [laajuudellinenOppiaine]
      }),
      false
    )
  })

  it('näkyy pakollisille leikkuripäivästä alkaen', () => {
    assert.strictEqual(
      shouldShowLaajuusColumn({
        editMode: false,
        isToimintaAlueittain: false,
        pakollinen: true,
        suoritus: suoritus('2020-08-01'),
        osasuoritukset: [laajuudellinenOppiaine]
      }),
      true
    )
  })

  it('näkyy toiminta-alueittain leikkuripäivästä alkaen', () => {
    assert.strictEqual(
      shouldShowLaajuusColumn({
        editMode: false,
        isToimintaAlueittain: true,
        pakollinen: undefined,
        suoritus: suoritus('2020-08-01'),
        osasuoritukset: [laajuudellinenOppiaine]
      }),
      true
    )
  })

  it('näkyy valinnaisille ilman vahvistuspäiväleikkuria', () => {
    assert.strictEqual(
      shouldShowLaajuusColumn({
        editMode: false,
        isToimintaAlueittain: false,
        pakollinen: false,
        suoritus: suoritus(),
        osasuoritukset: [laajuudellinenOppiaine]
      }),
      true
    )
  })
})
