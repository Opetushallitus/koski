import { calculatePaattymisajankohta } from '../../app/omadata/expirationTime'
import * as assert from 'assert'

describe('Päättymisajankohdan laskenta', () => {
  describe('Vain minuutit', () => {
    it('1 minuutti (yksikkö)', () => {
      const result = calculatePaattymisajankohta(1)
      assert.deepEqual(result, {
        templateName:
          'omadataoauth2_suostumuksesi_paattymisajankohta_min',
        minutes: 1
      })
    })

    it('30 minuuttia (monikko)', () => {
      const result = calculatePaattymisajankohta(30)
      assert.deepEqual(result, {
        templateName:
          'omadataoauth2_suostumuksesi_paattymisajankohta_mins',
        minutes: 30
      })
    })

    it('59 minuuttia', () => {
      const result = calculatePaattymisajankohta(59)
      assert.deepEqual(result, {
        templateName:
          'omadataoauth2_suostumuksesi_paattymisajankohta_mins',
        minutes: 59
      })
    })
  })

  describe('Vain tunnit', () => {
    it('1 tunti (yksikkö)', () => {
      const result = calculatePaattymisajankohta(60)
      assert.deepEqual(result, {
        templateName:
          'omadataoauth2_suostumuksesi_paattymisajankohta_hour',
        hours: 1
      })
    })

    it('2 tuntia (monikko)', () => {
      const result = calculatePaattymisajankohta(120)
      assert.deepEqual(result, {
        templateName:
          'omadataoauth2_suostumuksesi_paattymisajankohta_hours',
        hours: 2
      })
    })
  })

  describe('Tunnit ja minuutit', () => {
    it('1 tunti 1 minuutti', () => {
      const result = calculatePaattymisajankohta(61)
      assert.deepEqual(result, {
        templateName:
          'omadataoauth2_suostumuksesi_paattymisajankohta_hour_min',
        hours: 1,
        minutes: 1
      })
    })

    it('1 tunti 30 minuuttia', () => {
      const result = calculatePaattymisajankohta(90)
      assert.deepEqual(result, {
        templateName:
          'omadataoauth2_suostumuksesi_paattymisajankohta_hour_mins',
        hours: 1,
        minutes: 30
      })
    })

    it('2 tuntia 1 minuutti', () => {
      const result = calculatePaattymisajankohta(121)
      assert.deepEqual(result, {
        templateName:
          'omadataoauth2_suostumuksesi_paattymisajankohta_hours_min',
        hours: 2,
        minutes: 1
      })
    })

    it('2 tuntia 30 minuuttia', () => {
      const result = calculatePaattymisajankohta(150)
      assert.deepEqual(result, {
        templateName:
          'omadataoauth2_suostumuksesi_paattymisajankohta_hours_mins',
        hours: 2,
        minutes: 30
      })
    })

    it('23 tuntia 59 minuuttia', () => {
      const result = calculatePaattymisajankohta(60 * 23 + 59)
      assert.deepEqual(result, {
        templateName:
          'omadataoauth2_suostumuksesi_paattymisajankohta_hours_mins',
        hours: 23,
        minutes: 59
      })
    })
  })

  describe('Vain päivät', () => {
    it('1 päivä (yksikkö)', () => {
      const result = calculatePaattymisajankohta(60 * 24)
      assert.deepEqual(result, {
        templateName:
          'omadataoauth2_suostumuksesi_paattymisajankohta_day',
        days: 1
      })
    })

    it('7 päivää (monikko)', () => {
      const result = calculatePaattymisajankohta(60 * 24 * 7)
      assert.deepEqual(result, {
        templateName:
          'omadataoauth2_suostumuksesi_paattymisajankohta_days',
        days: 7
      })
    })
  })

  describe('Päivät ja tunnit', () => {
    it('1 päivä 1 tunti', () => {
      const result = calculatePaattymisajankohta(60 * 25)
      assert.deepEqual(result, {
        templateName:
          'omadataoauth2_suostumuksesi_paattymisajankohta_day_hour',
        days: 1,
        hours: 1
      })
    })

    it('1 päivä 2 tuntia', () => {
      const result = calculatePaattymisajankohta(60 * 24 + 60 * 2)
      assert.deepEqual(result, {
        templateName:
          'omadataoauth2_suostumuksesi_paattymisajankohta_day_hours',
        days: 1,
        hours: 2
      })
    })

    it('2 päivää 1 tunti', () => {
      const result = calculatePaattymisajankohta(60 * 24 * 2 + 60)
      assert.deepEqual(result, {
        templateName:
          'omadataoauth2_suostumuksesi_paattymisajankohta_days_hour',
        days: 2,
        hours: 1
      })
    })

    it('2 päivää 3 tuntia', () => {
      const result = calculatePaattymisajankohta(60 * 24 * 2 + 60 * 3)
      assert.deepEqual(result, {
        templateName:
          'omadataoauth2_suostumuksesi_paattymisajankohta_days_hours',
        days: 2,
        hours: 3
      })
    })
  })

  describe('Päivät ja minuutit', () => {
    it('1 päivä 1 minuutti', () => {
      const result = calculatePaattymisajankohta(60 * 24 + 1)
      assert.deepEqual(result, {
        templateName:
          'omadataoauth2_suostumuksesi_paattymisajankohta_day_min',
        days: 1,
        minutes: 1
      })
    })

    it('1 päivä 30 minuuttia', () => {
      const result = calculatePaattymisajankohta(60 * 24 + 30)
      assert.deepEqual(result, {
        templateName:
          'omadataoauth2_suostumuksesi_paattymisajankohta_day_mins',
        days: 1,
        minutes: 30
      })
    })

    it('2 päivää 1 minuutti', () => {
      const result = calculatePaattymisajankohta(60 * 24 * 2 + 1)
      assert.deepEqual(result, {
        templateName:
          'omadataoauth2_suostumuksesi_paattymisajankohta_days_min',
        days: 2,
        minutes: 1
      })
    })

    it('3 päivää 45 minuuttia', () => {
      const result = calculatePaattymisajankohta(60 * 24 * 3 + 45)
      assert.deepEqual(result, {
        templateName:
          'omadataoauth2_suostumuksesi_paattymisajankohta_days_mins',
        days: 3,
        minutes: 45
      })
    })
  })

  describe('Päivät, tunnit ja minuutit', () => {
    it('1 päivä 1 tunti 1 minuutti', () => {
      const result = calculatePaattymisajankohta(60 * 24 + 60 + 1)
      assert.deepEqual(result, {
        templateName:
          'omadataoauth2_suostumuksesi_paattymisajankohta_day_hour_min',
        days: 1,
        hours: 1,
        minutes: 1
      })
    })

    it('1 päivä 1 tunti 30 minuuttia', () => {
      const result = calculatePaattymisajankohta(60 * 24 + 60 + 30)
      assert.deepEqual(result, {
        templateName:
          'omadataoauth2_suostumuksesi_paattymisajankohta_day_hour_mins',
        days: 1,
        hours: 1,
        minutes: 30
      })
    })

    it('1 päivä 2 tuntia 1 minuutti', () => {
      const result = calculatePaattymisajankohta(60 * 24 + 60 * 2 + 1)
      assert.deepEqual(result, {
        templateName:
          'omadataoauth2_suostumuksesi_paattymisajankohta_day_hours_min',
        days: 1,
        hours: 2,
        minutes: 1
      })
    })

    it('1 päivä 2 tuntia 30 minuuttia', () => {
      const result = calculatePaattymisajankohta(60 * 24 + 60 * 2 + 30)
      assert.deepEqual(result, {
        templateName:
          'omadataoauth2_suostumuksesi_paattymisajankohta_day_hours_mins',
        days: 1,
        hours: 2,
        minutes: 30
      })
    })

    it('2 päivää 1 tunti 1 minuutti', () => {
      const result = calculatePaattymisajankohta(60 * 24 * 2 + 60 + 1)
      assert.deepEqual(result, {
        templateName:
          'omadataoauth2_suostumuksesi_paattymisajankohta_days_hour_min',
        days: 2,
        hours: 1,
        minutes: 1
      })
    })

    it('2 päivää 1 tunti 30 minuuttia', () => {
      const result = calculatePaattymisajankohta(60 * 24 * 2 + 60 + 30)
      assert.deepEqual(result, {
        templateName:
          'omadataoauth2_suostumuksesi_paattymisajankohta_days_hour_mins',
        days: 2,
        hours: 1,
        minutes: 30
      })
    })

    it('2 päivää 2 tuntia 1 minuutti', () => {
      const result = calculatePaattymisajankohta(60 * 24 * 2 + 60 * 2 + 1)
      assert.deepEqual(result, {
        templateName:
          'omadataoauth2_suostumuksesi_paattymisajankohta_days_hours_min',
        days: 2,
        hours: 2,
        minutes: 1
      })
    })

    it('2 päivää 3 tuntia 15 minuuttia', () => {
      const result = calculatePaattymisajankohta(60 * 24 * 2 + 60 * 3 + 15)
      assert.deepEqual(result, {
        templateName:
          'omadataoauth2_suostumuksesi_paattymisajankohta_days_hours_mins',
        days: 2,
        hours: 3,
        minutes: 15
      })
    })
  })
})
