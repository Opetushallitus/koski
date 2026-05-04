import * as assert from 'assert'
import { poistettavaPäätasonSuoritus } from '../../app/perusopetus-v2/paatasonSuoritusPoisto'

const vuosiluokanSuoritus = (luokka: string) => ({
  $class: 'fi.oph.koski.schema.PerusopetuksenVuosiluokanSuoritus',
  luokka,
  koulutusmoduuli: {
    tunniste: {
      koodiarvo: '7'
    }
  },
  tyyppi: {
    koodiarvo: 'perusopetuksenvuosiluokka',
    koodistoUri: 'suorituksentyyppi'
  }
})

describe('perusopetus v2 päätason suorituksen poisto', () => {
  it('erottaa saman vuosiluokan suoritukset valitun indeksin perusteella', () => {
    const ensimmäinenSeiska = vuosiluokanSuoritus('7A')
    const toinenSeiska = vuosiluokanSuoritus('7C')
    const opiskeluoikeus = {
      suoritukset: [ensimmäinenSeiska, toinenSeiska]
    }

    const poistettava = poistettavaPäätasonSuoritus(opiskeluoikeus as any, 1)

    assert.strictEqual(poistettava, toinenSeiska)
    assert.notStrictEqual(poistettava, ensimmäinenSeiska)
  })
})
