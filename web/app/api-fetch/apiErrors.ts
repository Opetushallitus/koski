import * as A from 'fp-ts/Array'
import { ApiError, ApiErrorKey } from './apiFetch'

type BasicError = {
  key: ApiErrorKey
  message: string
}

type ValidationError = {
  key: ApiErrorKey
  message: ValidationErrorMessage[]
}

type ValidationErrorMessage = {
  path: string
  value: string
  error: {
    errorType: 'emptyString' // Lisäile tähän tarvittaessa muitakin tyyppejä
  }
}

const isBasicError = (error: unknown): error is BasicError =>
  typeof error === 'object' &&
  error !== null &&
  typeof (error as BasicError).key === 'string' &&
  typeof (error as BasicError).message === 'string'

const isValidationError = (error: unknown): error is ValidationError =>
  typeof error === 'object' &&
  error !== null &&
  typeof (error as ValidationError).key === 'string' &&
  Array.isArray((error as ValidationError).message) // Voisi olla tarkempikin validointi, mutta sillä polulla voisi ottaa jo io-ts:n käyttöön

export const parseErrors = (error: unknown): ApiError[] => {
  if (Array.isArray(error)) {
    return A.flatten(error.map(parseErrors))
  } else if (isBasicError(error)) {
    return [
      {
        key: error.key,
        messageKey: error.message
      }
    ]
  } else if (isValidationError(error)) {
    return error.message.map((message) => ({
      key: error.key,
      messageKey: `${message.error.errorType}: ${message.path}`
    }))
  } else {
    return []
  }
}
