import * as assert from 'assert'
import { sortPäätasonSuoritukset } from '../../app/perusopetus-v2/paatasonSuoritustenJarjestys'

type TestiSuoritus = {
  nimi: string
  oppimäärä?: boolean
  koulutusmoduuli: { tunniste: { koodiarvo: string } }
  alkamispäivä?: string
}

const vuosiluokka = (
  nimi: string,
  koodiarvo: string,
  alkamispäivä?: string
): TestiSuoritus => ({
  nimi,
  koulutusmoduuli: { tunniste: { koodiarvo } },
  alkamispäivä
})

const oppimäärä = (nimi = 'päättötodistus'): TestiSuoritus => ({
  nimi,
  oppimäärä: true,
  koulutusmoduuli: { tunniste: { koodiarvo: '201101' } }
})

const järjestä = (suoritukset: TestiSuoritus[]) =>
  sortPäätasonSuoritukset(suoritukset, (s) => s.oppimäärä === true).map(
    (s) => s.nimi
  )

describe('perusopetus v2 päätason suoritusten järjestys', () => {
  it('nostaa oppimäärän suorituksen ensimmäiseksi ja järjestää vuosiluokat laskevasti', () => {
    const järjestetyt = järjestä([
      vuosiluokka('7', '7', '2017-08-01'),
      vuosiluokka('9', '9', '2019-08-01'),
      oppimäärä(),
      vuosiluokka('8', '8', '2018-08-01')
    ])

    assert.deepStrictEqual(järjestetyt, ['päättötodistus', '9', '8', '7'])
  })

  it('järjestää saman vuosiluokan suoritukset alkamispäivän mukaan uusimmasta vanhimpaan', () => {
    const järjestetyt = järjestä([
      vuosiluokka('7 vanha', '7', '2013-08-15'),
      vuosiluokka('8', '8', '2015-08-15'),
      vuosiluokka('7 uusinta', '7', '2014-08-15')
    ])

    assert.deepStrictEqual(järjestetyt, ['8', '7 uusinta', '7 vanha'])
  })

  it('järjestää saman vuosiluokan suoritukset alkamispäivän mukaan myös kun ne ovat valmiiksi oikein päin', () => {
    const järjestetyt = järjestä([
      vuosiluokka('7 uusinta', '7', '2014-08-15'),
      vuosiluokka('7 vanha', '7', '2013-08-15')
    ])

    assert.deepStrictEqual(järjestetyt, ['7 uusinta', '7 vanha'])
  })

  it('jättää alkamispäivättömän suorituksen saman vuosiluokan viimeiseksi', () => {
    const järjestetyt = järjestä([
      vuosiluokka('7 päivätön', '7', undefined),
      vuosiluokka('7 päivällinen', '7', '2013-08-15')
    ])

    assert.deepStrictEqual(järjestetyt, ['7 päivällinen', '7 päivätön'])
  })

  it('ei muuta alkuperäistä listaa', () => {
    const suoritukset = [
      vuosiluokka('7', '7', '2017-08-01'),
      vuosiluokka('9', '9', '2019-08-01')
    ]

    sortPäätasonSuoritukset(suoritukset, (s) => s.oppimäärä === true)

    assert.deepStrictEqual(
      suoritukset.map((s) => s.nimi),
      ['7', '9']
    )
  })
})
