import { describe, expect, it } from 'vitest'
import {
  calculateTimeUnits,
  buildLocalizedTimeString
} from '../../app/omadata/expirationTime'

const mockT = (key: string): string => {
  const translations: Record<string, string> = {
    omadataoauth2_aika_paiva: 'päivä',
    omadataoauth2_aika_paivaa: 'päivää',
    omadataoauth2_aika_tunti: 'tunti',
    omadataoauth2_aika_tuntia: 'tuntia',
    omadataoauth2_aika_minuutti: 'minuutti',
    omadataoauth2_aika_minuuttia: 'minuuttia'
  }
  return translations[key] ?? key
}

describe('calculateTimeUnits', () => {
  describe('Vain minuutit', () => {
    it('1 minuutti', () => {
      const result = calculateTimeUnits(1)
      expect(result).toStrictEqual({ days: 0, hours: 0, minutes: 1 })
    })

    it('30 minuuttia', () => {
      const result = calculateTimeUnits(30)
      expect(result).toStrictEqual({ days: 0, hours: 0, minutes: 30 })
    })

    it('59 minuuttia', () => {
      const result = calculateTimeUnits(59)
      expect(result).toStrictEqual({ days: 0, hours: 0, minutes: 59 })
    })
  })

  describe('Vain tunnit', () => {
    it('1 tunti', () => {
      const result = calculateTimeUnits(60)
      expect(result).toStrictEqual({ days: 0, hours: 1, minutes: 0 })
    })

    it('2 tuntia', () => {
      const result = calculateTimeUnits(120)
      expect(result).toStrictEqual({ days: 0, hours: 2, minutes: 0 })
    })
  })

  describe('Tunnit ja minuutit', () => {
    it('1 tunti 1 minuutti', () => {
      const result = calculateTimeUnits(61)
      expect(result).toStrictEqual({ days: 0, hours: 1, minutes: 1 })
    })

    it('1 tunti 30 minuuttia', () => {
      const result = calculateTimeUnits(90)
      expect(result).toStrictEqual({ days: 0, hours: 1, minutes: 30 })
    })

    it('23 tuntia 59 minuuttia', () => {
      const result = calculateTimeUnits(60 * 23 + 59)
      expect(result).toStrictEqual({ days: 0, hours: 23, minutes: 59 })
    })
  })

  describe('Vain päivät', () => {
    it('1 päivä', () => {
      const result = calculateTimeUnits(60 * 24)
      expect(result).toStrictEqual({ days: 1, hours: 0, minutes: 0 })
    })

    it('7 päivää', () => {
      const result = calculateTimeUnits(60 * 24 * 7)
      expect(result).toStrictEqual({ days: 7, hours: 0, minutes: 0 })
    })
  })

  describe('Päivät ja tunnit', () => {
    it('1 päivä 1 tunti', () => {
      const result = calculateTimeUnits(60 * 25)
      expect(result).toStrictEqual({ days: 1, hours: 1, minutes: 0 })
    })

    it('2 päivää 3 tuntia', () => {
      const result = calculateTimeUnits(60 * 24 * 2 + 60 * 3)
      expect(result).toStrictEqual({ days: 2, hours: 3, minutes: 0 })
    })
  })

  describe('Päivät ja minuutit', () => {
    it('1 päivä 30 minuuttia', () => {
      const result = calculateTimeUnits(60 * 24 + 30)
      expect(result).toStrictEqual({ days: 1, hours: 0, minutes: 30 })
    })

    it('3 päivää 45 minuuttia', () => {
      const result = calculateTimeUnits(60 * 24 * 3 + 45)
      expect(result).toStrictEqual({ days: 3, hours: 0, minutes: 45 })
    })
  })

  describe('Päivät, tunnit ja minuutit', () => {
    it('1 päivä 1 tunti 1 minuutti', () => {
      const result = calculateTimeUnits(60 * 24 + 60 + 1)
      expect(result).toStrictEqual({ days: 1, hours: 1, minutes: 1 })
    })

    it('2 päivää 3 tuntia 15 minuuttia', () => {
      const result = calculateTimeUnits(60 * 24 * 2 + 60 * 3 + 15)
      expect(result).toStrictEqual({ days: 2, hours: 3, minutes: 15 })
    })
  })
})

describe('buildLocalizedTimeString', () => {
  describe('Yksittäiset yksiköt', () => {
    it('vain minuutit (yksikkö)', () => {
      const result = buildLocalizedTimeString(
        { days: 0, hours: 0, minutes: 1 },
        mockT
      )
      expect(result).toBe('1 minuutti')
    })

    it('vain minuutit (monikko)', () => {
      const result = buildLocalizedTimeString(
        { days: 0, hours: 0, minutes: 30 },
        mockT
      )
      expect(result).toBe('30 minuuttia')
    })

    it('vain tunnit (yksikkö)', () => {
      const result = buildLocalizedTimeString(
        { days: 0, hours: 1, minutes: 0 },
        mockT
      )
      expect(result).toBe('1 tunti')
    })

    it('vain tunnit (monikko)', () => {
      const result = buildLocalizedTimeString(
        { days: 0, hours: 2, minutes: 0 },
        mockT
      )
      expect(result).toBe('2 tuntia')
    })

    it('vain päivät (yksikkö)', () => {
      const result = buildLocalizedTimeString(
        { days: 1, hours: 0, minutes: 0 },
        mockT
      )
      expect(result).toBe('1 päivä')
    })

    it('vain päivät (monikko)', () => {
      const result = buildLocalizedTimeString(
        { days: 7, hours: 0, minutes: 0 },
        mockT
      )
      expect(result).toBe('7 päivää')
    })
  })

  describe('Kaksi yksikköä', () => {
    it('tunnit ja minuutit', () => {
      const result = buildLocalizedTimeString(
        { days: 0, hours: 2, minutes: 30 },
        mockT
      )
      expect(result).toBe('2 tuntia 30 minuuttia')
    })

    it('päivät ja tunnit', () => {
      const result = buildLocalizedTimeString(
        { days: 2, hours: 3, minutes: 0 },
        mockT
      )
      expect(result).toBe('2 päivää 3 tuntia')
    })

    it('päivät ja minuutit', () => {
      const result = buildLocalizedTimeString(
        { days: 1, hours: 0, minutes: 15 },
        mockT
      )
      expect(result).toBe('1 päivä 15 minuuttia')
    })
  })

  describe('Kolme yksikköä', () => {
    it('päivät, tunnit ja minuutit', () => {
      const result = buildLocalizedTimeString(
        { days: 1, hours: 2, minutes: 30 },
        mockT
      )
      expect(result).toBe('1 päivä 2 tuntia 30 minuuttia')
    })

    it('monikot kaikissa', () => {
      const result = buildLocalizedTimeString(
        { days: 2, hours: 3, minutes: 15 },
        mockT
      )
      expect(result).toBe('2 päivää 3 tuntia 15 minuuttia')
    })
  })

  describe('Nolla-arvot', () => {
    it('0 minuuttia näytetään kun kaikki ovat nollia', () => {
      const result = buildLocalizedTimeString(
        { days: 0, hours: 0, minutes: 0 },
        mockT
      )
      expect(result).toBe('0 minuuttia')
    })
  })
})
