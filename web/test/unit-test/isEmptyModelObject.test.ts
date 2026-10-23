import { describe, expect, it } from 'vitest'
import { isEmptyModelObject } from '../../app/util/objects'

describe('isEmptyModelObject', () => {
  it('Palauttaa true tyhjällä objektilla', () => {
    const obj = {}
    expect(isEmptyModelObject(obj)).toBe(true)
  })
  it('Palauttaa true objektilla missä $class kenttä', () => {
    const obj = { $class: 'joku_luokka' }
    expect(isEmptyModelObject(obj)).toBe(true)
  })
  it('Palauttaa true jos objektissa on pelkkä tyhjä array', () => {
    const obj = { $class: 'joku', maksuttomuudet: [] }
    expect(isEmptyModelObject(obj)).toBe(true)
  })
  it('Palauttaa true jos objektissa on montaa tyhjää arrayta', () => {
    const obj = {
      $class: 'joku',
      maksuttomuudet: [],
      oikeuttaMaksuttomuuteenPidennetty: []
    }
    expect(isEmptyModelObject(obj)).toBe(true)
  })
  it('Palauttaa true jos objektissa on tyhjä aliobjekti', () => {
    const obj = {
      $class: 'joku',
      objekti: {}
    }
    expect(isEmptyModelObject(obj)).toBe(true)
  })

  it('Palauttaa false jos objektissa on yksi epätyhjä array', () => {
    const obj = {
      $class: 'joku',
      maksuttomuudet: [
        {
          $class: 'fi.oph.koski.schema.Maksuttomuus',
          alku: '2024-08-15',
          maksuton: true
        }
      ],
      oikeuttaMaksuttomuuteenPidennetty: []
    }
    expect(isEmptyModelObject(obj)).toBe(false)
  })
  it('Palauttaa false jos objektissa on monta epätyhjää arrayta', () => {
    const obj = {
      $class: 'joku',
      maksuttomuudet: [
        {
          $class: 'fi.oph.koski.schema.Maksuttomuus',
          alku: '2024-08-15',
          maksuton: true
        }
      ],
      oikeuttaMaksuttomuuteenPidennetty: [
        {
          alku: '2024-08-15',
          loppu: '2024-12-30',
          $class: 'fi.oph.koski.schema.OikeuttaMaksuttomuuteenPidennetty'
        }
      ]
    }
    expect(isEmptyModelObject(obj)).toBe(false)
  })
  it('Palauttaa false jos objektissa on stringi', () => {
    const obj = {
      $class: 'joku',
      maksuttomuudet: [],
      oikeuttaMaksuttomuuteenPidennetty: [],
      joku: 'stringi'
    }
    expect(isEmptyModelObject(obj)).toBe(false)
  })
})
