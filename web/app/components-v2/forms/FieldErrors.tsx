import * as A from 'fp-ts/Array'
import React from 'react'
import { t } from '../../i18n/i18n'
import { ValidationError } from './validator'

export type FieldErrorsProps = {
  errors: ValidationError[]
}

export const FieldErrors: React.FC<FieldErrorsProps> = (props) => {
  return A.isNonEmpty(props.errors) ? (
    <ul className="FieldErrors">
      {props.errors.map((error, index) => (
        <li key={index}>{fieldErrorMessage(error)}</li>
      ))}
    </ul>
  ) : null
}

export const fieldErrorMessage = (error: ValidationError): string => {
  switch (error.type) {
    case 'emptyString':
      return t('Kenttä ei voi olla tyhjä')
    case 'mustBeAtLeast':
      return `Arvon pitää olla vähintään ${error.limit}` // TODO: dynaaminen käännös
    case 'mustBeAtMost':
      return `Arvon pitää olla enintään ${error.limit}` // TODO: dynaaminen käännös
    case 'mustBeGreater':
      return `Arvon pitää olla enemmän kuin ${error.limit}` // TODO: dynaaminen käännös
    case 'mustBeLesser':
      return `Arvon pitää olla vähemmän kuin ${error.limit}` // TODO: dynaaminen käännös
    case 'invalidType':
    case 'noClassName':
    case 'noMatch':
      return `Odottamaton virhe, ota yhteyttä KOSKI-tiimiin (lisätiedot: ${JSON.stringify(
        error
      )})`
    default:
      // @ts-expect-error - jos tämä kommentti antaa virheen, yläpuolelta puuttuu case
      return error.type
  }
}
