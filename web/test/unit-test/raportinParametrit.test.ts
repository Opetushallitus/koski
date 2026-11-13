import { describe, expect, it } from 'vitest'
import {
  aikajaksoraportinParametrit,
  aikarajattavanAikajaksoraportinParametrit,
  päiväraportinParametrit,
  tyyppivalinnallisenAikajaksoraportinParametrit,
  vuosiluokkaraportinParametrit
} from '../../app/raportit/raportinParametrit'

const organisaatio = { oid: '1.2.246.562.10.14613773812' }
const päivä = (vuosi: number, kuukausi: number, pv: number) =>
  new Date(vuosi, kuukausi - 1, pv)

describe('päiväraportinParametrit', () => {
  const valinnat = {
    organisaatio,
    päivä: päivä(2020, 1, 15),
    näytäKotikunta: true,
    kotikuntaPvm: päivä(2019, 12, 31),
    kieli: 'fi' as const
  }

  it('muodostaa parametrit valinnoista', () => {
    expect(päiväraportinParametrit(valinnat)).toEqual({
      organisaatioOid: organisaatio.oid,
      paiva: '2020-01-15',
      kotikuntaPvm: '2019-12-31',
      language: 'fi'
    })
  })

  it('jättää kuluvan päivän kotikunnan pois', () => {
    expect(
      päiväraportinParametrit({ ...valinnat, kotikuntaPvm: new Date() })
        ?.kotikuntaPvm
    ).toBeUndefined()
  })

  it('jättää kotikunnan pois, kun sitä ei kysytä', () => {
    expect(
      päiväraportinParametrit({ ...valinnat, näytäKotikunta: false })
        ?.kotikuntaPvm
    ).toBeUndefined()
  })

  it('ei muodosta parametreja ilman organisaatiota, päivää tai kysyttyä kotikuntapäivää', () => {
    expect(
      päiväraportinParametrit({ ...valinnat, organisaatio: undefined })
    ).toBeNull()
    expect(
      päiväraportinParametrit({ ...valinnat, päivä: undefined })
    ).toBeNull()
    expect(
      päiväraportinParametrit({ ...valinnat, kotikuntaPvm: undefined })
    ).toBeNull()
    expect(
      päiväraportinParametrit({
        ...valinnat,
        näytäKotikunta: false,
        kotikuntaPvm: undefined
      })
    ).not.toBeNull()
  })
})

describe('vuosiluokkaraportinParametrit', () => {
  it('välittää vuosiluokan merkkijonona', () => {
    expect(
      vuosiluokkaraportinParametrit({
        organisaatio,
        päivä: päivä(2012, 1, 1),
        vuosiluokka: 9,
        näytäKotikunta: true,
        kotikuntaPvm: päivä(2011, 1, 1),
        kieli: 'sv'
      })
    ).toEqual({
      organisaatioOid: organisaatio.oid,
      paiva: '2012-01-01',
      vuosiluokka: '9',
      kotikuntaPvm: '2011-01-01',
      language: 'sv'
    })
  })
})

describe('aikajaksoraportinParametrit', () => {
  const valinnat = {
    organisaatio,
    alku: päivä(2020, 1, 1),
    loppu: päivä(2020, 1, 31),
    kieli: 'fi' as const
  }

  it('muodostaa parametrit aikajaksosta', () => {
    expect(aikajaksoraportinParametrit(valinnat)).toEqual({
      organisaatioOid: organisaatio.oid,
      alku: '2020-01-01',
      loppu: '2020-01-31',
      language: 'fi'
    })
  })

  it('hyväksyy yhden päivän aikajakson mutta ei ennen alkua päättyvää', () => {
    expect(
      aikajaksoraportinParametrit({ ...valinnat, loppu: valinnat.alku })
    ).not.toBeNull()
    expect(
      aikajaksoraportinParametrit({ ...valinnat, loppu: päivä(2019, 12, 31) })
    ).toBeNull()
  })

  it('ei muodosta parametreja ilman molempia päiviä', () => {
    expect(
      aikajaksoraportinParametrit({ ...valinnat, alku: undefined })
    ).toBeNull()
    expect(
      aikajaksoraportinParametrit({ ...valinnat, loppu: undefined })
    ).toBeNull()
  })
})

describe('aikarajattavat ja tyyppivalinnalliset aikajaksoraportit', () => {
  const valinnat = {
    organisaatio,
    alku: päivä(2020, 1, 1),
    loppu: päivä(2020, 1, 31),
    osasuoritustenAikarajaus: true,
    näytäKotikunta: true,
    kotikuntaPvm: päivä(2019, 6, 1),
    kieli: 'fi' as const
  }

  it('välittää osasuoritusten aikarajauksen ja kotikuntapäivän', () => {
    expect(aikarajattavanAikajaksoraportinParametrit(valinnat)).toEqual({
      organisaatioOid: organisaatio.oid,
      alku: '2020-01-01',
      loppu: '2020-01-31',
      osasuoritustenAikarajaus: true,
      kotikuntaPvm: '2019-06-01',
      language: 'fi'
    })
  })

  it('välittää raportin tyypin ja vaatii sen', () => {
    expect(
      tyyppivalinnallisenAikajaksoraportinParametrit({
        ...valinnat,
        raportinTyyppi: 'päättövaihe'
      })?.raportinTyyppi
    ).toBe('päättövaihe')
    expect(
      tyyppivalinnallisenAikajaksoraportinParametrit({
        ...valinnat,
        raportinTyyppi: ''
      })
    ).toBeNull()
  })
})
