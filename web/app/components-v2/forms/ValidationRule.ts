import { deepEqual } from '../../util/fp/objects'
import { ObjWithClass } from '../../util/types'
import { ValidationError } from './validator'

export type ValidationRule<T = any> = {
  type: 'ValidationRule'
  isMatch: (data: any, path: string[]) => data is T
  validate: (data: T, path: string[]) => ValidationError[]
}

export const classValidationRule = <T extends ObjWithClass>(
  isClassOf: (data: any) => data is T,
  validate: (data: T, path: string[]) => ValidationError[]
): ValidationRule<T> => ({
  type: 'ValidationRule',
  isMatch: (data: any, _path: string[]): data is T => isClassOf(data),
  validate
})

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

export const isValidationRule = <T>(a: any): a is ValidationRule<T> =>
  (a as any)?.type === 'ValidationRule'
