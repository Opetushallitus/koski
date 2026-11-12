import { describe, expect, it } from 'vitest'
import { finnish } from '../../app/i18n/i18n'
import {
  alkuvalinta,
  esivalitseOrganisaatio,
  kategorianValinta,
  litistäOrganisaatiot,
  näkyvätKategoriat,
  organisaatioidenNimet,
  RaportinOrganisaatio,
  raportinOrganisaatiot,
  raportinValinta,
  rajaaHakusanalla,
  viereinenValittavaOrganisaatio
} from '../../app/raportit/raporttivalinta'

const organisaatio = (
  oid: string,
  nimi: string,
  raportit: string[],
  children: RaportinOrganisaatio[] = []
): RaportinOrganisaatio => ({ oid, nimi: finnish(nimi), raportit, children })

const helsinki = organisaatio(
  'helsinki',
  'Helsingin kaupunki',
  ['esiopetus'],
  [
    organisaatio('kulosaari', 'Kulosaaren ala-aste', ['esiopetus', 'lukio']),
    organisaatio('stadin', 'Stadin ammatti- ja aikuisopisto', ['ammatillinen'])
  ]
)
const jyväskylä = organisaatio(
  'jy',
  'Jyväskylän yliopisto',
  [],
  [organisaatio('jnk', 'Jyväskylän normaalikoulu', ['lukio', 'esiopetus'])]
)
const organisaatiot = [helsinki, jyväskylä]

const oidit = (orgs: { oid: string }[]) => orgs.map((o) => o.oid)

const kategoriat = [
  {
    id: 'esiopetus',
    tab: 'tab-esiopetus',
    heading: 'heading-esiopetus',
    raportit: [{ id: 'esiopetus' }, { id: 'ammatillinen' }]
  },
  {
    id: 'lukio',
    tab: 'tab-lukio',
    heading: 'heading-lukio',
    raportit: [{ id: 'lukio' }, { id: 'esiopetus' }]
  },
  {
    id: 'ib',
    tab: 'tab-ib',
    heading: 'heading-ib',
    raportit: [{ id: 'ib' }]
  },
  {
    id: 'muut',
    tab: 'tab-muut',
    heading: 'heading-muut',
    raportit: [{ id: 'paallekkaiset', visibleForAllOrgs: true }]
  }
]

describe('raportinOrganisaatiot', () => {
  it('näyttää raporttityypin organisaatiot ja niiden yläorganisaatiot', () => {
    const lukio = raportinOrganisaatiot('lukio', organisaatiot)
    expect(oidit(litistäOrganisaatiot(lukio))).toEqual([
      'helsinki',
      'kulosaari',
      'jy',
      'jnk'
    ])
    expect(lukio.map((o) => o.selectable)).toEqual([false, false])
    expect(lukio[0].children[0].selectable).toBe(true)
  })

  it('jättää pois organisaatiot, joilla tai joiden aliorganisaatioilla ei ole raporttityyppiä', () => {
    expect(oidit(raportinOrganisaatiot('ammatillinen', organisaatiot))).toEqual(
      ['helsinki']
    )
    expect(raportinOrganisaatiot('ib', organisaatiot)).toEqual([])
  })
})

describe('esivalitseOrganisaatio', () => {
  const lukioraportti = {
    id: 'lukio',
    organisaatiot: raportinOrganisaatiot('lukio', organisaatiot)
  }

  it('valitsee ensimmäisen valittavissa olevan organisaation', () => {
    expect(esivalitseOrganisaatio(lukioraportti, undefined)?.oid).toBe(
      'kulosaari'
    )
  })

  it('säilyttää valinnan, joka on raportin organisaatioissa', () => {
    const jnk = litistäOrganisaatiot(lukioraportti.organisaatiot).find(
      (o) => o.oid === 'jnk'
    )
    expect(esivalitseOrganisaatio(lukioraportti, jnk)?.oid).toBe('jnk')
  })

  it('vaihtaa valinnan, jota ei ole raportin organisaatioissa', () => {
    const stadin = litistäOrganisaatiot(
      raportinOrganisaatiot('ammatillinen', organisaatiot)
    ).find((o) => o.oid === 'stadin')
    expect(esivalitseOrganisaatio(lukioraportti, stadin)?.oid).toBe('kulosaari')
  })

  it('säilyttää kaikille organisaatioille näkyvässä raportissa aiemman valinnan, myös tyhjän', () => {
    const raportti = {
      id: 'paallekkaiset',
      visibleForAllOrgs: true,
      organisaatiot: []
    }
    expect(esivalitseOrganisaatio(raportti, undefined)).toBeUndefined()
  })
})

describe('viereinenValittavaOrganisaatio', () => {
  const lukio = raportinOrganisaatiot('lukio', organisaatiot)
  const [kulosaari, jnk] = litistäOrganisaatiot(lukio).filter(
    (o) => o.selectable
  )

  it('ohittaa organisaatiot, joita ei voi valita', () => {
    expect(viereinenValittavaOrganisaatio(lukio, kulosaari, 1)?.oid).toBe('jnk')
    expect(viereinenValittavaOrganisaatio(lukio, jnk, -1)?.oid).toBe(
      'kulosaari'
    )
  })

  it('ei siirry listan ensimmäistä ylemmäs eikä viimeistä alemmas', () => {
    expect(viereinenValittavaOrganisaatio(lukio, kulosaari, -1)).toBeUndefined()
    expect(viereinenValittavaOrganisaatio(lukio, jnk, 1)).toBeUndefined()
  })
})

describe('rajaaHakusanalla', () => {
  const kaikki = raportinOrganisaatiot('esiopetus', organisaatiot)

  it('näyttää osuvan organisaation kaikki aliorganisaatiot', () => {
    expect(
      oidit(litistäOrganisaatiot(rajaaHakusanalla('HELSINGIN', kaikki)))
    ).toEqual(['helsinki', 'kulosaari'])
  })

  it('näyttää osuvan aliorganisaation yläorganisaatioineen', () => {
    expect(
      oidit(litistäOrganisaatiot(rajaaHakusanalla('normaali', kaikki)))
    ).toEqual(['jy', 'jnk'])
  })

  it('palauttaa tyhjän listan, kun mikään ei osu', () => {
    expect(rajaaHakusanalla('eiloydy', kaikki)).toEqual([])
  })
})

describe('organisaatioidenNimet', () => {
  it('kerää kaikkien organisaatioiden nimet', () => {
    expect(organisaatioidenNimet([jyväskylä])).toEqual({
      jy: 'Jyväskylän yliopisto',
      jnk: 'Jyväskylän normaalikoulu'
    })
  })
})

describe('näkyvätKategoriat', () => {
  it('näyttää kategoriat, joissa on näkyviä raportteja', () => {
    const näkyvät = näkyvätKategoriat(kategoriat, organisaatiot)
    expect(näkyvät.map((k) => k.visible)).toEqual([true, true, false, true])
  })

  it('näyttää kaikille organisaatioille näkyvän raportin myös ilman organisaatioita', () => {
    const näkyvät = näkyvätKategoriat(kategoriat, [])
    expect(näkyvät.map((k) => k.visible)).toEqual([false, false, false, true])
  })
})

describe('alkuvalinta', () => {
  const näkyvät = näkyvätKategoriat(kategoriat, organisaatiot)
  const valinta = (polku: string[]) => {
    const { kategoriaIdx, raporttiIdx } = alkuvalinta(näkyvät, polku)
    return [kategoriaIdx, raporttiIdx]
  }

  it('valitsee polun mukaisen kategorian ja raportin', () => {
    expect(valinta(['lukio', 'esiopetus'])).toEqual([1, 1])
    expect(valinta(['esiopetus', 'ammatillinen'])).toEqual([0, 1])
  })

  it('valitsee kategorian ensimmäisen raportin, jos polun raporttia ei löydy', () => {
    expect(valinta(['lukio', 'tuntematon'])).toEqual([1, 0])
    expect(valinta(['lukio'])).toEqual([1, 0])
  })

  it('valitsee ensimmäisen näkyvän kategorian, jos polun kategoriaa ei löydy tai se on piilotettu', () => {
    expect(valinta([])).toEqual([0, 0])
    expect(valinta(['tuntematon', 'lukio'])).toEqual([0, 0])
    expect(valinta(['ib', 'ib'])).toEqual([0, 0])
  })

  it('ei käytä piilotetun kategorian raportin indeksiä toisessa kategoriassa', () => {
    const vainEsiopetus = näkyvätKategoriat(kategoriat, [
      organisaatio('kulosaari', 'Kulosaaren ala-aste', [
        'esiopetus',
        'ammatillinen'
      ])
    ])
    const piilotettuLukio = vainEsiopetus.map((k) =>
      k.id === 'lukio' ? { ...k, visible: false } : k
    )
    expect(alkuvalinta(piilotettuLukio, ['lukio', 'esiopetus'])).toMatchObject({
      kategoriaIdx: 0,
      raporttiIdx: 0
    })
  })

  it('esivalitsee raportin ensimmäisen organisaation', () => {
    expect(alkuvalinta(näkyvät, ['lukio', 'lukio']).organisaatio?.oid).toBe(
      'kulosaari'
    )
  })

  it('ei valitse mitään, kun yksikään kategoria ei näy', () => {
    const piilotetut = näkyvät.map((k) => ({ ...k, visible: false }))
    expect(alkuvalinta(piilotetut, [])).toEqual({
      kategoriaIdx: -1,
      raporttiIdx: 0,
      organisaatio: undefined
    })
  })
})

describe('kategorianValinta ja raportinValinta', () => {
  const näkyvät = näkyvätKategoriat(kategoriat, organisaatiot)

  it('kategorian vaihto valitsee sen ensimmäisen näkyvän raportin ja säilyttää sopivan organisaation', () => {
    const alussa = alkuvalinta(näkyvät, ['esiopetus', 'esiopetus'])
    const jnk = litistäOrganisaatiot(näkyvät[0].raportit[0].organisaatiot).find(
      (o) => o.oid === 'jnk'
    )
    const lukio = kategorianValinta(
      näkyvät,
      { ...alussa, organisaatio: jnk },
      1
    )
    expect(lukio).toMatchObject({ kategoriaIdx: 1, raporttiIdx: 0 })
    expect(lukio.organisaatio?.oid).toBe('jnk')
  })

  it('raportin vaihto vaihtaa organisaation, jos sitä ei ole uuden raportin listassa', () => {
    const alussa = alkuvalinta(näkyvät, ['esiopetus', 'esiopetus'])
    expect(alussa.organisaatio?.oid).toBe('helsinki')
    const ammatillinen = raportinValinta(näkyvät, alussa, 1)
    expect(ammatillinen.raporttiIdx).toBe(1)
    expect(ammatillinen.organisaatio?.oid).toBe('stadin')
  })
})
