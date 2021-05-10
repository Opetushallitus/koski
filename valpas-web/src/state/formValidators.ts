import * as E from "fp-ts/Either"
import { t } from "../i18n/i18n"
import { FieldValidator } from "./useFormState"

export const expectNonEmptyString = <T extends object>(
  errorStringId: string
): FieldValidator<string, T> => (input, _form) =>
  input === undefined || input.length === 0
    ? E.left([t(errorStringId)])
    : E.right(input)
