import { describe, expect, it } from 'vitest'

import { PerusopetuksenOpiskeluoikeus } from '../../app/types/fi/oph/koski/schema/PerusopetuksenOpiskeluoikeus'
import {
  isSekamuotoinen,
  isToimintaAlueittainOpiskelu,
  isToimintaAlueittainSuoritus,
  osasuoritustenSisältö,
  sisältääOppiaineita,
  sisältääToimintaAlueita
} from '../../app/perusopetus-v2/toimintaAlueittain'

const oppiaine = {
  $class: 'fi.oph.koski.schema.NuortenPerusopetuksenOppiaineenSuoritus'
}

const toimintaAlue = {
  $class: 'fi.oph.koski.schema.PerusopetuksenToiminta_AlueenSuoritus'
}

const ilmanLisätietoa = {} as unknown as PerusopetuksenOpiskeluoikeus

const erityisenTuenPäätöksellä = {
  lisätiedot: { erityisenTuenPäätös: { opiskeleeToimintaAlueittain: true } }
} as unknown as PerusopetuksenOpiskeluoikeus

const erityisenTuenPäätöksillä = {
  lisätiedot: {
    erityisenTuenPäätökset: [
      { opiskeleeToimintaAlueittain: false },
      { opiskeleeToimintaAlueittain: true }
    ]
  }
} as unknown as PerusopetuksenOpiskeluoikeus

const toimintaAlueJaksolla = {
  lisätiedot: {
    toimintaAlueittainOpiskelu: [{ alku: '2026-08-01', loppu: '2027-06-01' }]
  }
} as unknown as PerusopetuksenOpiskeluoikeus

describe('perusopetus v2 toiminta-alueittain opiskelun päättely', () => {
  describe('lisätietolippu', () => {
    it('on pois päältä ilman lisätietoja', () => {
      expect(isToimintaAlueittainOpiskelu(ilmanLisätietoa)).toBe(false)
    })

    it('tunnistaa yksittäisen erityisen tuen päätöksen', () => {
      expect(isToimintaAlueittainOpiskelu(erityisenTuenPäätöksellä)).toBe(true)
    })

    it('tunnistaa erityisen tuen päätösten listan', () => {
      expect(isToimintaAlueittainOpiskelu(erityisenTuenPäätöksillä)).toBe(true)
    })

    it('tunnistaa toiminta-alueittain opiskelun jakson', () => {
      expect(isToimintaAlueittainOpiskelu(toimintaAlueJaksolla)).toBe(true)
    })
  })

  describe('listan sisältö', () => {
    it('tyhjässä listassa ei ole kumpaakaan', () => {
      expect(sisältääOppiaineita([])).toBe(false)
      expect(sisältääToimintaAlueita([])).toBe(false)
      expect(isSekamuotoinen([])).toBe(false)
    })

    it('puhdas oppiainelista ei ole sekamuotoinen', () => {
      expect(sisältääOppiaineita([oppiaine, oppiaine])).toBe(true)
      expect(sisältääToimintaAlueita([oppiaine, oppiaine])).toBe(false)
      expect(isSekamuotoinen([oppiaine, oppiaine])).toBe(false)
    })

    it('puhdas toiminta-aluelista ei ole sekamuotoinen', () => {
      expect(sisältääOppiaineita([toimintaAlue])).toBe(false)
      expect(sisältääToimintaAlueita([toimintaAlue])).toBe(true)
      expect(isSekamuotoinen([toimintaAlue])).toBe(false)
    })

    it('tunnistaa sekamuotoisen listan', () => {
      expect(isSekamuotoinen([toimintaAlue, oppiaine])).toBe(true)
    })

    it('tuntematon tyyppi tulkitaan oppiaineeksi, ei toiminta-alueeksi', () => {
      const tuntematon = { $class: 'fi.oph.koski.schema.JokinUusiSuoritus' }
      expect(sisältääOppiaineita([tuntematon])).toBe(true)
      expect(sisältääToimintaAlueita([tuntematon])).toBe(false)
    })
  })

  describe('osasuoritusten sisältö otsikoita varten', () => {
    it('tunnistaa tyhjän listan', () => {
      expect(osasuoritustenSisältö([])).toBe('tyhjä')
    })

    it('tunnistaa pelkät oppiaineet', () => {
      expect(osasuoritustenSisältö([oppiaine])).toBe('oppiaineet')
    })

    it('tunnistaa pelkät toiminta-alueet', () => {
      expect(osasuoritustenSisältö([toimintaAlue, toimintaAlue])).toBe(
        'toimintaAlueet'
      )
    })

    it('tunnistaa sekamuodon', () => {
      expect(osasuoritustenSisältö([toimintaAlue, oppiaine])).toBe(
        'sekamuotoinen'
      )
    })

    it('ei riipu lisätietolipusta', () => {
      expect(osasuoritustenSisältö([toimintaAlue])).toBe('toimintaAlueet')
      expect(osasuoritustenSisältö([oppiaine])).toBe('oppiaineet')
    })
  })

  describe('suorituskohtainen tila', () => {
    it('on toiminta-aluetaulukko, kun lippu on päällä eikä oppiaineita ole', () => {
      expect(
        isToimintaAlueittainSuoritus(erityisenTuenPäätöksellä, [toimintaAlue])
      ).toBe(true)
    })

    it('palautuu lippuun tyhjällä listalla, jotta esitäyttö säilyy', () => {
      expect(isToimintaAlueittainSuoritus(erityisenTuenPäätöksellä, [])).toBe(
        true
      )
      expect(isToimintaAlueittainSuoritus(ilmanLisätietoa, [])).toBe(false)
    })

    it('yksikin oppiaine pakottaa normaalitilan lipusta huolimatta', () => {
      expect(
        isToimintaAlueittainSuoritus(erityisenTuenPäätöksellä, [
          toimintaAlue,
          toimintaAlue,
          oppiaine
        ])
      ).toBe(false)
      expect(
        isToimintaAlueittainSuoritus(toimintaAlueJaksolla, [
          toimintaAlue,
          oppiaine
        ])
      ).toBe(false)
    })

    it('on toiminta-aluetaulukko myös ilman lippua, kun listalla on vain toiminta-alueita', () => {
      // 1444 tuotannon suoritusta on tällaisia: sisältö ratkaisee, jotta
      // otsikko ja lisäyspudotus eivät ole eri mieltä keskenään.
      expect(
        isToimintaAlueittainSuoritus(ilmanLisätietoa, [toimintaAlue])
      ).toBe(true)
    })

    it('ei ole toiminta-aluetaulukko sekamuotoisella listalla', () => {
      expect(
        isToimintaAlueittainSuoritus(erityisenTuenPäätöksellä, [
          toimintaAlue,
          oppiaine
        ])
      ).toBe(false)
    })
  })
})
