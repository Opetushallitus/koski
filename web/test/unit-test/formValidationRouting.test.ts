import { describe, expect, it } from 'vitest'

import * as $ from 'optics-ts'

import { allLanguages, parsePath } from '../../app/util/optics'
import {
  pathToString,
  validateData,
  ValidationError
} from '../../app/components-v2/forms/validator'
import {
  classValidationRule,
  exactPathValidationRule,
  pathPatternValidationRule,
  ValidationRule
} from '../../app/components-v2/forms/ValidationRule'
import { Constraint } from '../../app/types/fi/oph/koski/typemodel/Constraint'
import { ObjectConstraint } from '../../app/types/fi/oph/koski/typemodel/ObjectConstraint'
import { ArrayConstraint } from '../../app/types/fi/oph/koski/typemodel/ArrayConstraint'
import { StringConstraint } from '../../app/types/fi/oph/koski/typemodel/StringConstraint'
import { LocalizedString } from '../../app/types/fi/oph/koski/schema/LocalizedString'

type Suoritus = {
  $class: string
  luokka: string
  osasuoritukset: Array<{ arviointi: unknown[] }>
}

type State = {
  suoritukset: Suoritus[]
  nimi: LocalizedString
}

// Tyyppi kuvaa täyden muodon; datassa osa kentistä puuttuu tarkoituksella, koska juuri
// silloin kentän pitää löytää oma polkunsa.
const state = {
  suoritukset: [
    {
      $class: 'Vuosiluokka',
      luokka: '8A',
      osasuoritukset: [{ arviointi: [{ arvosana: '9' }] }, {}]
    },
    { $class: 'Vuosiluokka', luokka: '' },
    { $class: 'Vuosiluokka' }
  ],
  nimi: { $class: 'Finnish', fi: 'nimi', sv: 'nimi', en: 'nimi' }
} as unknown as State

const root = $.optic_<State>()

describe('parsePath', () => {
  it('löytää polun kun kentällä on arvo', () => {
    expect(
      parsePath(root.prop('suoritukset').at(0).prop('luokka'), state)
    ).toBe('suoritukset.0.luokka')
  })

  it('löytää polun kun kentän arvo on tyhjä merkkijono', () => {
    expect(
      parsePath(root.prop('suoritukset').at(1).prop('luokka'), state)
    ).toBe('suoritukset.1.luokka')
  })

  it('löytää polun kun kenttä puuttuu objektista kokonaan', () => {
    expect(
      parsePath(root.prop('suoritukset').at(2).prop('luokka'), state)
    ).toBe('suoritukset.2.luokka')
    expect(
      parsePath(
        root
          .prop('suoritukset')
          .at(0)
          .prop('osasuoritukset')
          .at(1)
          .prop('arviointi'),
        state
      )
    ).toBe('suoritukset.0.osasuoritukset.1.arviointi')
  })

  it('ei sekoita samanarvoisia kenttiä keskenään', () => {
    // Vanha toteutus etsi kentän arvoa datasta, jolloin identtinen arvo toisaalla
    // saattoi antaa väärän polun.
    const identtiset = { a: 'sama', b: 'sama' }
    expect(parsePath($.optic_<typeof identtiset>().prop('b'), identtiset)).toBe(
      'b'
    )
  })

  it('palauttaa yhteisen alkuosan kun optiikka levittää arvon useaan paikkaan', () => {
    expect(parsePath(root.prop('nimi').compose(allLanguages), state)).toBe(
      'nimi'
    )
  })

  it('palauttaa undefined kun optiikka ei osoita mihinkään', () => {
    expect(
      parsePath(root.prop('suoritukset').at(9).prop('luokka'), state)
    ).toBeUndefined()
    expect(
      parsePath(root.prop('suoritukset').at(2).prop('luokka').optional(), state)
    ).toBeUndefined()
  })

  it('palauttaa undefined datan juurelle', () => {
    expect(parsePath(root, state)).toBeUndefined()
  })
})

const constraint: Constraint = ObjectConstraint({
  class: 'Opiskeluoikeus',
  properties: {
    suoritukset: ArrayConstraint({
      items: ObjectConstraint({
        class: 'Vuosiluokka',
        properties: { luokka: StringConstraint() }
      })
    })
  }
})

const data = {
  suoritukset: [
    { $class: 'Vuosiluokka', luokka: '8A' },
    { $class: 'Vuosiluokka' }
  ]
}

const merkkaus = (_d: unknown, path: string[]): ValidationError[] => [
  { type: 'emptyValue', path: pathToString([...path, 'testi']) }
]

const merkatut = (rule: ValidationRule<any>): string[] =>
  validateData(data, constraint, [rule])
    .filter((e) => e.path.endsWith('testi'))
    .map((e) => e.path)

describe('validateData', () => {
  it('raportoi puuttuvan pakollisen arvon tyhjänä arvona eikä tyyppivirheenä', () => {
    expect(validateData(data, constraint)).toStrictEqual([
      { type: 'emptyValue', path: 'suoritukset.1.luokka' }
    ])
  })

  it('ajaa ValidationRulen jokaisessa solmussa oikealla polulla', () => {
    const nähdytPolut: string[] = []
    const rule: ValidationRule<any> = {
      type: 'ValidationRule',
      isMatch: (_d: any, path: string[]): _d is any => {
        nähdytPolut.push(pathToString(path))
        return false
      },
      validate: () => []
    }
    validateData(data, constraint, [rule])
    expect(nähdytPolut).toStrictEqual([
      '',
      'suoritukset',
      'suoritukset.0',
      'suoritukset.0.luokka',
      'suoritukset.1',
      'suoritukset.1.luokka'
    ])
  })

  it('classValidationRule osuu jokaiseen luokan olioon, ei vain juureen', () => {
    expect(
      merkatut(
        classValidationRule(
          (d: any): d is Suoritus => d?.$class === 'Vuosiluokka',
          merkkaus
        )
      )
    ).toStrictEqual(['suoritukset.0.testi', 'suoritukset.1.testi'])
  })

  it('exactPathValidationRule osuu annettuun polkuun', () => {
    expect(
      merkatut(exactPathValidationRule('suoritukset', '1')(merkkaus))
    ).toStrictEqual(['suoritukset.1.testi'])
  })

  it('pathPatternValidationRule osuu jokaiseen taulukon alkioon', () => {
    expect(
      merkatut(pathPatternValidationRule('suoritukset', '*')(merkkaus))
    ).toStrictEqual(['suoritukset.0.testi', 'suoritukset.1.testi'])
  })
})
