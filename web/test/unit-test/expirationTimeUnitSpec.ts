import {
  calculateTimeUnits,
  buildLocalizedTimeString
} from '../../app/omadata/expirationTime'
import * as assert from 'assert'

// Mock translation function that returns Finnish translations
const mockT = (key: string): string => {
  const translations: Record<string, string> = {
    'omadataoauth2_aika_paiva': 'päivä',
    'omadataoauth2_aika_paivaa': 'päivää',
    'omadataoauth2_aika_tunti': 'tunti',
    'omadataoauth2_aika_tuntia': 'tuntia',
    'omadataoauth2_aika_minuutti': 'minuutti',
    'omadataoauth2_aika_minuuttia': 'minuuttia'
  }
  return translations[key] ?? key
}

describe('calculateTimeUnits', () => {
  describe('Vain minuutit', () => {
    it('1 minuutti', () => {
      const result = calculateTimeUnits(1)
      assert.deepEqual(result, { days: 0, hours: 0, minutes: 1 })
    })

    it('30 minuuttia', () => {
      const result = calculateTimeUnits(30)
      assert.deepEqual(result, { days: 0, hours: 0, minutes: 30 })
    })

    it('59 minuuttia', () => {
      const result = calculateTimeUnits(59)
      assert.deepEqual(result, { days: 0, hours: 0, minutes: 59 })
    })
  })

  describe('Vain tunnit', () => {
    it('1 tunti', () => {
      const result = calculateTimeUnits(60)
      assert.deepEqual(result, { days: 0, hours: 1, minutes: 0 })
    })

    it('2 tuntia', () => {
      const result = calculateTimeUnits(120)
      assert.deepEqual(result, { days: 0, hours: 2, minutes: 0 })
    })
  })

  describe('Tunnit ja minuutit', () => {
    it('1 tunti 1 minuutti', () => {
      const result = calculateTimeUnits(61)
      assert.deepEqual(result, { days: 0, hours: 1, minutes: 1 })
    })

    it('1 tunti 30 minuuttia', () => {
      const result = calculateTimeUnits(90)
      assert.deepEqual(result, { days: 0, hours: 1, minutes: 30 })
    })

    it('23 tuntia 59 minuuttia', () => {
      const result = calculateTimeUnits(60 * 23 + 59)
      assert.deepEqual(result, { days: 0, hours: 23, minutes: 59 })
    })
  })

  describe('Vain päivät', () => {
    it('1 päivä', () => {
      const result = calculateTimeUnits(60 * 24)
      assert.deepEqual(result, { days: 1, hours: 0, minutes: 0 })
    })

    it('7 päivää', () => {
      const result = calculateTimeUnits(60 * 24 * 7)
      assert.deepEqual(result, { days: 7, hours: 0, minutes: 0 })
    })
  })

  describe('Päivät ja tunnit', () => {
    it('1 päivä 1 tunti', () => {
      const result = calculateTimeUnits(60 * 25)
      assert.deepEqual(result, { days: 1, hours: 1, minutes: 0 })
    })

    it('2 päivää 3 tuntia', () => {
      const result = calculateTimeUnits(60 * 24 * 2 + 60 * 3)
      assert.deepEqual(result, { days: 2, hours: 3, minutes: 0 })
    })
  })

  describe('Päivät ja minuutit', () => {
    it('1 päivä 30 minuuttia', () => {
      const result = calculateTimeUnits(60 * 24 + 30)
      assert.deepEqual(result, { days: 1, hours: 0, minutes: 30 })
    })

    it('3 päivää 45 minuuttia', () => {
      const result = calculateTimeUnits(60 * 24 * 3 + 45)
      assert.deepEqual(result, { days: 3, hours: 0, minutes: 45 })
    })
  })

  describe('Päivät, tunnit ja minuutit', () => {
    it('1 päivä 1 tunti 1 minuutti', () => {
      const result = calculateTimeUnits(60 * 24 + 60 + 1)
      assert.deepEqual(result, { days: 1, hours: 1, minutes: 1 })
    })

    it('2 päivää 3 tuntia 15 minuuttia', () => {
      const result = calculateTimeUnits(60 * 24 * 2 + 60 * 3 + 15)
      assert.deepEqual(result, { days: 2, hours: 3, minutes: 15 })
    })
  })
})

describe('buildLocalizedTimeString', () => {
  describe('Yksittäiset yksiköt', () => {
    it('vain minuutit (yksikkö)', () => {
      const result = buildLocalizedTimeString({ days: 0, hours: 0, minutes: 1 }, mockT)
      assert.strictEqual(result, '1 minuutti')
    })

    it('vain minuutit (monikko)', () => {
      const result = buildLocalizedTimeString({ days: 0, hours: 0, minutes: 30 }, mockT)
      assert.strictEqual(result, '30 minuuttia')
    })

    it('vain tunnit (yksikkö)', () => {
      const result = buildLocalizedTimeString({ days: 0, hours: 1, minutes: 0 }, mockT)
      assert.strictEqual(result, '1 tunti')
    })

    it('vain tunnit (monikko)', () => {
      const result = buildLocalizedTimeString({ days: 0, hours: 2, minutes: 0 }, mockT)
      assert.strictEqual(result, '2 tuntia')
    })

    it('vain päivät (yksikkö)', () => {
      const result = buildLocalizedTimeString({ days: 1, hours: 0, minutes: 0 }, mockT)
      assert.strictEqual(result, '1 päivä')
    })

    it('vain päivät (monikko)', () => {
      const result = buildLocalizedTimeString({ days: 7, hours: 0, minutes: 0 }, mockT)
      assert.strictEqual(result, '7 päivää')
    })
  })

  describe('Kaksi yksikköä', () => {
    it('tunnit ja minuutit', () => {
      const result = buildLocalizedTimeString({ days: 0, hours: 2, minutes: 30 }, mockT)
      assert.strictEqual(result, '2 tuntia 30 minuuttia')
    })

    it('päivät ja tunnit', () => {
      const result = buildLocalizedTimeString({ days: 2, hours: 3, minutes: 0 }, mockT)
      assert.strictEqual(result, '2 päivää 3 tuntia')
    })

    it('päivät ja minuutit', () => {
      const result = buildLocalizedTimeString({ days: 1, hours: 0, minutes: 15 }, mockT)
      assert.strictEqual(result, '1 päivä 15 minuuttia')
    })
  })

  describe('Kolme yksikköä', () => {
    it('päivät, tunnit ja minuutit', () => {
      const result = buildLocalizedTimeString({ days: 1, hours: 2, minutes: 30 }, mockT)
      assert.strictEqual(result, '1 päivä 2 tuntia 30 minuuttia')
    })

    it('monikot kaikissa', () => {
      const result = buildLocalizedTimeString({ days: 2, hours: 3, minutes: 15 }, mockT)
      assert.strictEqual(result, '2 päivää 3 tuntia 15 minuuttia')
    })
  })

  describe('Nolla-arvot', () => {
    it('0 minuuttia näytetään kun kaikki ovat nollia', () => {
      const result = buildLocalizedTimeString({ days: 0, hours: 0, minutes: 0 }, mockT)
      assert.strictEqual(result, '0 minuuttia')
    })
  })
})
