import * as assert from 'assert'
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
      assert.strictEqual(isToimintaAlueittainOpiskelu(ilmanLisätietoa), false)
    })

    it('tunnistaa yksittäisen erityisen tuen päätöksen', () => {
      assert.strictEqual(
        isToimintaAlueittainOpiskelu(erityisenTuenPäätöksellä),
        true
      )
    })

    it('tunnistaa erityisen tuen päätösten listan', () => {
      assert.strictEqual(
        isToimintaAlueittainOpiskelu(erityisenTuenPäätöksillä),
        true
      )
    })

    it('tunnistaa toiminta-alueittain opiskelun jakson', () => {
      assert.strictEqual(
        isToimintaAlueittainOpiskelu(toimintaAlueJaksolla),
        true
      )
    })
  })

  describe('listan sisältö', () => {
    it('tyhjässä listassa ei ole kumpaakaan', () => {
      assert.strictEqual(sisältääOppiaineita([]), false)
      assert.strictEqual(sisältääToimintaAlueita([]), false)
      assert.strictEqual(isSekamuotoinen([]), false)
    })

    it('puhdas oppiainelista ei ole sekamuotoinen', () => {
      assert.strictEqual(sisältääOppiaineita([oppiaine, oppiaine]), true)
      assert.strictEqual(sisältääToimintaAlueita([oppiaine, oppiaine]), false)
      assert.strictEqual(isSekamuotoinen([oppiaine, oppiaine]), false)
    })

    it('puhdas toiminta-aluelista ei ole sekamuotoinen', () => {
      assert.strictEqual(sisältääOppiaineita([toimintaAlue]), false)
      assert.strictEqual(sisältääToimintaAlueita([toimintaAlue]), true)
      assert.strictEqual(isSekamuotoinen([toimintaAlue]), false)
    })

    it('tunnistaa sekamuotoisen listan', () => {
      assert.strictEqual(isSekamuotoinen([toimintaAlue, oppiaine]), true)
    })

    it('tuntematon tyyppi tulkitaan oppiaineeksi, ei toiminta-alueeksi', () => {
      const tuntematon = { $class: 'fi.oph.koski.schema.JokinUusiSuoritus' }
      assert.strictEqual(sisältääOppiaineita([tuntematon]), true)
      assert.strictEqual(sisältääToimintaAlueita([tuntematon]), false)
    })
  })

  describe('osasuoritusten sisältö otsikoita varten', () => {
    it('tunnistaa tyhjän listan', () => {
      assert.strictEqual(osasuoritustenSisältö([]), 'tyhjä')
    })

    it('tunnistaa pelkät oppiaineet', () => {
      assert.strictEqual(osasuoritustenSisältö([oppiaine]), 'oppiaineet')
    })

    it('tunnistaa pelkät toiminta-alueet', () => {
      assert.strictEqual(
        osasuoritustenSisältö([toimintaAlue, toimintaAlue]),
        'toimintaAlueet'
      )
    })

    it('tunnistaa sekamuodon', () => {
      assert.strictEqual(
        osasuoritustenSisältö([toimintaAlue, oppiaine]),
        'sekamuotoinen'
      )
    })

    it('ei riipu lisätietolipusta', () => {
      assert.strictEqual(
        osasuoritustenSisältö([toimintaAlue]),
        'toimintaAlueet'
      )
      assert.strictEqual(osasuoritustenSisältö([oppiaine]), 'oppiaineet')
    })
  })

  describe('suorituskohtainen tila', () => {
    it('on toiminta-aluetaulukko, kun lippu on päällä eikä oppiaineita ole', () => {
      assert.strictEqual(
        isToimintaAlueittainSuoritus(erityisenTuenPäätöksellä, [toimintaAlue]),
        true
      )
    })

    it('palautuu lippuun tyhjällä listalla, jotta esitäyttö säilyy', () => {
      assert.strictEqual(
        isToimintaAlueittainSuoritus(erityisenTuenPäätöksellä, []),
        true
      )
      assert.strictEqual(
        isToimintaAlueittainSuoritus(ilmanLisätietoa, []),
        false
      )
    })

    it('yksikin oppiaine pakottaa normaalitilan lipusta huolimatta', () => {
      assert.strictEqual(
        isToimintaAlueittainSuoritus(erityisenTuenPäätöksellä, [
          toimintaAlue,
          toimintaAlue,
          oppiaine
        ]),
        false
      )
      assert.strictEqual(
        isToimintaAlueittainSuoritus(toimintaAlueJaksolla, [
          toimintaAlue,
          oppiaine
        ]),
        false
      )
    })

    it('on toiminta-aluetaulukko myös ilman lippua, kun listalla on vain toiminta-alueita', () => {
      // 1444 tuotannon suoritusta on tällaisia: sisältö ratkaisee, jotta
      // otsikko ja lisäyspudotus eivät ole eri mieltä keskenään.
      assert.strictEqual(
        isToimintaAlueittainSuoritus(ilmanLisätietoa, [toimintaAlue]),
        true
      )
    })

    it('ei ole toiminta-aluetaulukko sekamuotoisella listalla', () => {
      assert.strictEqual(
        isToimintaAlueittainSuoritus(erityisenTuenPäätöksellä, [
          toimintaAlue,
          oppiaine
        ]),
        false
      )
    })
  })
})
