import * as E from "fp-ts/Either"
import { FieldValidator } from "./useFormState"

export const expectNonEmptyString: FieldValidator<string> = (input) =>
  input.length === 0 ? E.left("ei sua olla tyhyjä") : E.right(input)
