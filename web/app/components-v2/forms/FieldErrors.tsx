import * as A from 'fp-ts/Array'
import * as string from 'fp-ts/string'
import React, { useMemo } from 'react'
import { tExists, tTemplate } from '../../i18n/i18n'
import { common, CommonProps } from '../CommonProps'
import { ValidationError } from './validator'
import { useTestId } from '../../appstate/useTestId'

export type FieldErrorsProps = CommonProps<{
  errors?: ValidationError[]
  localErrors?: ValidationError[]
}>

export const FieldErrors: React.FC<FieldErrorsProps> = (props) => {
  const errors: ValidationError[] = useMemo(
    () => [...(props.localErrors || []), ...(props.errors || [])],
    [props.localErrors, props.errors]
  )

  const messages = useMemo(
    () => A.uniq(string.Eq)(errors.map(fieldErrorMessage)),
    [errors]
  )

  const testId = useTestId('errors')

  return A.isNonEmpty(errors) ? (
    <ul {...common(props, ['FieldErrors'])} data-testid={testId}>
      {messages.map((message, index) => (
        <li key={index}>{message}</li>
      ))}
    </ul>
  ) : null
}

export const fieldErrorMessage = (error: ValidationError): string => {
  const messageKey = `validation:${error.type}`
  return tExists(messageKey)
    ? tTemplate(messageKey, error)
    : tTemplate('validation:other', { details: error })
}
