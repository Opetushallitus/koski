import { deepEqual } from '../../util/fp/objects'
import { ObjWithClass } from '../../util/types'
import { ValidationError } from './validator'

/**
 * Skeeman ulkopuolinen lisävalidointi lomakedatalle. Säännöt annetaan useForm:lle.
 *
 * Sääntö ajetaan jokaisessa solmussa, jonka läpi skeemavalidointi kulkee (kts. validator.ts:n
 * validate): *isMatch* ja *validate* saavat kyseisen kohdan datan ja polun. Jos sääntö osuu,
 * sen palauttamat virheet lisätään lomakkeen virheisiin.
 *
 * Virhepolut kannattaa muodostaa *validate*:n saamasta polusta (kts. validator.ts:n pathToString),
 * jolloin useFormErrors löytää virheen sille kentälle, jonka optiikka osoittaa samaan kohtaan.
 *
 * @example
 *    const oppiaineenNimiVaaditaan = classValidationRule(
 *      isPaikallinenKoodi,
 *      (koodi, path) =>
 *        koodi.nimi ? [] : [emptyString([...path, 'nimi'])]
 *    )
 */
export type ValidationRule<T = any> = {
  type: 'ValidationRule'
  isMatch: (data: any, path: string[]) => data is T
  validate: (data: T, path: string[]) => ValidationError[]
}

/**
 * Sääntö, joka osuu jokaiseen annetun luokan olioon missä tahansa kohtaa lomakedataa.
 */
export const classValidationRule = <T extends ObjWithClass>(
  isClassOf: (data: any) => data is T,
  validate: (data: T, path: string[]) => ValidationError[]
): ValidationRule<T> => ({
  type: 'ValidationRule',
  isMatch: (data: any, _path: string[]): data is T => isClassOf(data),
  validate
})

/**
 * Sääntö, joka osuu vain täsmälleen annettuun polkuun, esim.
 * exactPathValidationRule('suoritukset', '0', 'luokka').
 */
export const exactPathValidationRule =
  <T>(...exactPath: string[]) =>
  (
    validate: (data: T, path: string[]) => ValidationError[]
  ): ValidationRule<T> => ({
    type: 'ValidationRule',
    isMatch: (_data: any, path: string[]): _data is T =>
      deepEqual(path, exactPath),
    validate
  })

/**
 * Sääntö, joka osuu polkuihin annetun kaavan mukaan. Kaavassa '*' vastaa mitä tahansa yhtä
 * polun osaa, esim. pathPatternValidationRule('suoritukset', '*', 'luokka') osuu jokaisen
 * päätason suorituksen luokka-kenttään.
 */
export const pathPatternValidationRule =
  <T>(...pattern: string[]) =>
  (
    validate: (data: T, path: string[]) => ValidationError[]
  ): ValidationRule<T> => ({
    type: 'ValidationRule',
    isMatch: (_data: any, path: string[]): _data is T =>
      path.length === pattern.length &&
      pattern.every((part, i) => part === '*' || part === path[i]),
    validate
  })

export const isValidationRule = <T>(a: any): a is ValidationRule<T> =>
  (a as any)?.type === 'ValidationRule'
