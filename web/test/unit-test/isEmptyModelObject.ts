import { isEmptyModelObject } from '../../app/util/objects'
import assert from 'node:assert'

describe('isEmptyModelObject', () => {
  it('Palauttaa true tyhjällä objektilla', () => {
    const obj = {}
    assert(isEmptyModelObject(obj))
  })
  it('Palauttaa true objektilla missä $class kenttä', () => {
    const obj = { $class: 'joku_luokka' }
    assert(isEmptyModelObject(obj))
  })
  it('Palauttaa true jos objektissa on pelkkä tyhjä array', () => {
    const obj = { $class: 'joku', maksuttomuudet: [] }
    assert(isEmptyModelObject(obj))
  })
  it('Palauttaa true jos objektissa on montaa tyhjää arrayta', () => {
    const obj = {
      $class: 'joku',
      maksuttomuudet: [],
      oikeuttaMaksuttomuuteenPidennetty: []
    }
    assert(isEmptyModelObject(obj))
  })
  it('Palauttaa true jos objektissa on tyhjä aliobjekti', () => {
    const obj = {
      $class: 'joku',
      objekti: {}
    }
    assert(isEmptyModelObject(obj))
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
    assert(!isEmptyModelObject(obj))
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
    assert(!isEmptyModelObject(obj))
  })
  it('Palauttaa false jos objektissa on stringi', () => {
    const obj = {
      $class: 'joku',
      maksuttomuudet: [],
      oikeuttaMaksuttomuuteenPidennetty: [],
      joku: 'stringi'
    }
    assert(!isEmptyModelObject(obj))
  })
})
