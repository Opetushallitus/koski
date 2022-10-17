import * as E from "fp-ts/Either"
import { t } from "../i18n/i18n"
import { Hetu } from "./common"
import { FieldValidator } from "./useFormState"

// Merkkijonojen validaattorit

const stringValidator =
  (isValidValue: (input: string) => boolean) =>
  <T extends object>(errorStringId: string = ""): FieldValidator<string, T> =>
  (input, _form) =>
    input === undefined || !isValidValue(input)
      ? E.left([t(errorStringId)])
      : E.right(input)

export const expectNonEmptyString = stringValidator((input) => input.length > 0)

export const isValidHetu = (input: string): input is Hetu => {
  const match = input.toUpperCase().match(/^(\d{6})[-+A](\d{3})([0-9A-Y])$/)
  return match ? validHetuChecksum(match[1]!! + match[2]!!, match[3]!!) : false
}

export const expectValidHetu = stringValidator(isValidHetu)

const validHetuChecksum = (ddmmyynnn: string, checkChar: string): boolean => {
  const actualCheckSum = parseInt(ddmmyynnn, 10) % 31
  const expectedCheckSum = "0123456789ABCDEFHJKLMNPRSTUVWXY".indexOf(checkChar)
  return actualCheckSum === expectedCheckSum
}

export const expectValidOid = stringValidator((input) => {
  const ids = input.split(".")
  return ids.length >= 6 && ids.every((id) => /\d+/.test(id))
})

// Validaattorien koosteet

export const expectAtLeastOne =
  <T extends object, S>(
    errorStringId: string = "",
    validators: FieldValidator<S, T>[]
  ): FieldValidator<S, T> =>
  (input, form) => {
    const results = validators.map((validator) => validator(input, form))
    return results.some(E.isRight) ? E.right(input) : E.left([t(errorStringId)])
  }
