import { describe, expect, it } from 'vitest'

import { perusopetuksenPäätasonSuoritusEq } from '../../app/perusopetus-v2/paatasonSuoritusPoisto'

const vuosiluokanSuoritus = (
  koodiarvo: string,
  luokka: string,
  alkamispäivä: string
) => ({
  $class: 'fi.oph.koski.schema.PerusopetuksenVuosiluokanSuoritus',
  luokka,
  alkamispäivä,
  koulutusmoduuli: { tunniste: { koodiarvo } }
})

const oppimääränSuoritus = {
  $class: 'fi.oph.koski.schema.NuortenPerusopetuksenOppimääränSuoritus',
  koulutusmoduuli: { tunniste: { koodiarvo: '201101' } }
}

describe('perusopetus v2 päätason suorituksen poisto', () => {
  it('erottaa saman vuosiluokan suoritukset', () => {
    const ensimmäinenSeiska = vuosiluokanSuoritus('7', '7A', '2013-08-15')
    const toinenSeiska = vuosiluokanSuoritus('7', '7C', '2013-08-16')
    const tallennetut = [ensimmäinenSeiska, toinenSeiska]

    const poistettava = tallennetut.find((s) =>
      perusopetuksenPäätasonSuoritusEq.equals(s, { ...toinenSeiska })
    )

    expect(poistettava).toBe(toinenSeiska)
  })

  it('ei löydä tallennetuista muokkauksessa lisättyä vuosiluokkaa', () => {
    const tallennetut = [
      oppimääränSuoritus,
      vuosiluokanSuoritus('1', '1A', '2018-08-15')
    ]
    const lisätty = vuosiluokanSuoritus('2', '2A', '2019-08-15')

    expect(tallennetut.some((s) => perusopetuksenPäätasonSuoritusEq.equals(s, lisätty))).toBe(false)
  })

  it('tunnistaa oppimäärän suorituksen', () => {
    expect(perusopetuksenPäätasonSuoritusEq.equals(oppimääränSuoritus, { ...oppimääränSuoritus })).toBe(
      true
    )
  })
})
