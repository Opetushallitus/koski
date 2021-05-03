import * as A from "fp-ts/Array"
import * as E from "fp-ts/Either"
import { pipe } from "fp-ts/lib/function"
import * as Separated from "fp-ts/Separated"
import { useMemo, useState } from "react"
import { fromEntries, isEmptyObject } from "../utils/objects"

export type UseFormStateOptions<T extends object> = {
  initialValues: T
  validators: {
    [K in keyof T]: Array<FieldValidator<T[K]>>
  }
}

export type UseFormStateHook<T extends object> = {
  state: FormState<T>
  set: (key: keyof T, value: T[typeof key]) => void
  errors: () => FormErrors<T>
  values: () => E.Either<FormErrors<T>, T>
}

export type FormState<T extends object> = {
  [K in keyof T]: FieldState<T[K]>
}

export type FormErrors<T extends object> = Record<keyof T, FieldError[]>

export type FieldState<T> = {
  initialValue: T
  currentValue: T
  touched: boolean
  errors: string[]
}

export type FieldValidator<T> = (t: T) => E.Either<FieldError, T>

export type FieldError = string

export const useFormState = <T extends object>({
  initialValues,
  validators,
}: UseFormStateOptions<T>): UseFormStateHook<T> => {
  const initialState = useMemo(() => createInitialState(initialValues), [
    initialValues,
  ])
  const [currentState, setState] = useState(initialState)

  const set: UseFormStateHook<T>["set"] = (key, value) => {
    const fieldErrors = pipe(
      validators[key] || [],
      A.map((validate) => validate(value)),
      A.separate,
      Separated.left
    )

    return setState({
      ...currentState,
      [key]: {
        ...currentState[key],
        currentValue: value,
        touched: true,
        errors: fieldErrors,
      },
    })
  }

  const errors: UseFormStateHook<T>["errors"] = () =>
    Object.entries(currentState).reduce(
      (errors, [key, field]): FormErrors<T> => {
        const f = field as FieldState<unknown>
        return A.isNonEmpty(f.errors) ? { ...errors, [key]: f.errors } : errors
      },
      {} as FormErrors<T>
    )

  const values: UseFormStateHook<T>["values"] = () => {
    const errs = errors()
    return isEmptyObject(errs)
      ? E.right(
          Object.entries(currentState).reduce((vals, [key, field]) => {
            const f = field as FieldState<unknown>
            return { ...vals, [key]: f.currentValue }
          }, {} as T)
        )
      : E.left(errs)
  }

  return { state: currentState, set, errors, values }
}

const createInitialState = <T extends object>(values: T): FormState<T> =>
  pipe(
    Object.entries(values),
    A.map(([key, value]): [string, FieldState<typeof value>] => [
      key,
      {
        initialValue: value,
        currentValue: value,
        touched: false,
        errors: [],
      },
    ]),
    (entries) => fromEntries(entries) as FormState<T>
  )
