import * as A from "fp-ts/Array"
import * as E from "fp-ts/Either"
import { t } from "../i18n/i18n"
import { FieldValidator, FormState } from "./useFormState"

export const expectNonEmptyString = <T extends object>(
  errorStringId: string = "validointi__ei_voi_olla_tyhjä"
): FieldValidator<string, T> => (input, _form) =>
  input === undefined || input.length === 0
    ? E.left([t(errorStringId)])
    : E.right(input)

export const composeValidators = <T extends object, S>(
  validators: Array<FieldValidator<S, T>>
): FieldValidator<S, T> => (input, form) => {
  const results = validators.map((validate) => validate(input, form))
  const errors = A.flatten(A.lefts(results))
  return A.isNonEmpty(errors) ? E.left(errors) : E.right(input)
}

export const validateIfElse = <T extends object, S>(
  condition: (form: FormState<T>) => boolean,
  caseTrue: Array<FieldValidator<S, T>>,
  caseFalse: Array<FieldValidator<S, T>>
): FieldValidator<S, T> => (input, form) =>
  composeValidators(condition(form) ? caseTrue : caseFalse)(input, form)
