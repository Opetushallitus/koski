import * as A from 'fp-ts/Array'
import * as string from 'fp-ts/string'
import React, { useMemo } from 'react'
import { t } from '../../i18n/i18n'
import { otherError, ValidationError } from './validator'

export type FieldErrorsProps = {
  errors: ValidationError[]
  customError?: string
}

export const FieldErrors: React.FC<FieldErrorsProps> = (props) => {
  const errors: ValidationError[] = props.customError
    ? [otherError(props.customError), ...props.errors]
    : props.errors

  const messages = useMemo(
    () => A.uniq(string.Eq)(errors.map(fieldErrorMessage)),
    [errors]
  )

  return A.isNonEmpty(errors) ? (
    <ul className="FieldErrors">
      {messages.map((message, index) => (
        <li key={index}>{message}</li>
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
    case 'otherError':
      return error.message
    default:
      // @ts-expect-error - jos tämä kommentti antaa virheen, yläpuolelta puuttuu case
      return error.type
  }
}
