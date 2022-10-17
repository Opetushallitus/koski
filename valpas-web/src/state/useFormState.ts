import * as A from "fp-ts/Array"
import * as E from "fp-ts/Either"
import { pipe } from "fp-ts/lib/function"
import * as Separated from "fp-ts/Separated"
import { useCallback, useMemo, useState } from "react"
import { fromEntries } from "../utils/objects"

export type UseFormStateOptions<T extends object> = {
  initialValues: T
  validators: FormValidators<T>
}

export type FormValidators<T extends object> = {
  [K in keyof T]: Array<FieldValidator<T[K], T>>
}

export type UseFormStateHook<T extends object> = {
  state: FormState<T>
  touch: FormTouchFunction<T>
  set: FormSetFunction<T>
  patch: FormPatchFunction<T>
  fieldProps: FormFieldPropsFunction<T>
  submitCallback: FormSubmitCallback<T>
  validateAll: FormValidateFunction
  isValid: boolean
  allFieldsValidated: boolean
}

export type FormTouchFunction<T extends object> = (key: keyof T) => void
export type FormSetFunction<T extends object> = (
  key: keyof T,
  value: T[typeof key]
) => void
export type FormPatchFunction<T extends object> = (patch: Partial<T>) => void
export type FormFieldPropsFunction<T extends object> = <K extends keyof T>(
  key: K
) => FormFieldProps<T[K]>
export type FormSubmitCallback<T extends object> = (
  callback: (t: T) => void
) => () => void
export type FormValidateFunction = () => void

export type FormFieldProps<T> = {
  value: T
  onChange: (value: T) => void
  onBlur: () => void
  error?: React.ReactNode
}

export type FormState<T extends object> = Required<{
  [K in keyof T]: FieldState<T[K]>
}>

export type FormErrors<T extends object> = Record<keyof T, FieldError[]>

export type FieldState<T> = {
  initialValue: T
  currentValue: T
  touched: boolean
  validated: boolean
  errors: string[]
}

export type FieldValidator<T, F extends object> = (
  value: T | undefined,
  formState: FormState<F>
) => E.Either<FieldError[], T | undefined>

export type FieldError = string

export const useFormState = <T extends object>({
  initialValues,
  validators,
}: UseFormStateOptions<T>): UseFormStateHook<T> => {
  const initialState = useMemo(
    () => createInitialState(initialValues),
    [initialValues]
  )
  const [currentState, setState] = useState(initialState)

  const set: FormSetFunction<T> = useCallback(
    (key, value) => {
      const newState = updatedField(key, value, validators, currentState)
      return setState(validateTouchedFields(validators, newState))
    },
    [currentState, validators]
  )

  const patch: FormPatchFunction<T> = useCallback(
    (values) => {
      let newState = currentState
      for (const key in values) {
        const value = values[key] as T[typeof key]
        newState = updatedField(key, value, validators, newState)
      }
      setState(validateAllFields(validators, newState))
    },
    [currentState, validators]
  )

  const touch: FormTouchFunction<T> = useCallback(
    (key) => setState(touched(key, validators, currentState)),
    [currentState, validators]
  )

  const fieldProps: FormFieldPropsFunction<T> = useCallback(
    (key) => ({
      value: currentState[key].currentValue,
      onChange: (value) => set(key, value),
      onBlur: () => touch(key),
      error: currentState[key].errors.join(", "),
    }),
    [currentState, set, touch]
  )

  const [isValid, allFieldsValidated] = useMemo(
    () =>
      foldFields(
        ([accIsValid, accAllFieldsValidated], key) => [
          accIsValid && A.isEmpty(getErrors(key, validators, currentState)),
          accAllFieldsValidated && currentState[key].validated,
        ],
        [true, true],
        currentState
      ),
    [currentState, validators]
  )

  const submitCallback: FormSubmitCallback<T> = useCallback(
    (callback) => () => {
      if (isValid) {
        const values = foldFields(
          (acc, key, field) => ({ ...acc, [key]: field.currentValue }),
          {},
          currentState
        ) as T
        callback(values)
      }
    },
    [currentState, isValid]
  )

  const validateAll: FormValidateFunction = useCallback(
    () => setState(validateAllFields(validators, currentState)),
    [currentState, validators]
  )

  return useMemo(
    () => ({
      state: currentState,
      touch,
      set,
      patch,
      fieldProps,
      submitCallback,
      validateAll,
      isValid,
      allFieldsValidated,
    }),
    [
      allFieldsValidated,
      currentState,
      fieldProps,
      isValid,
      patch,
      set,
      submitCallback,
      touch,
      validateAll,
    ]
  )
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
        validated: false,
        errors: [],
      },
    ]),
    (entries) => fromEntries(entries) as FormState<T>
  )

const touched = <T extends object>(
  key: keyof T,
  validators: FormValidators<T>,
  state: FormState<T>
): FormState<T> =>
  validateField(key, validators, {
    ...state,
    [key]: {
      ...state[key],
      touched: true,
    },
  })

const updatedField = <K extends keyof T, T extends object>(
  key: K,
  value: T[K],
  validators: FormValidators<T>,
  state: FormState<T>
): FormState<T> =>
  validateField(key, validators, {
    ...state,
    [key]: {
      ...state[key],
      currentValue: value,
      touched: true,
    },
  })

const getErrors = <K extends keyof T, T extends object>(
  key: K,
  validators: FormValidators<T>,
  state: FormState<T>
) => {
  const value = state[key].currentValue
  return pipe(
    validators[key] || [],
    A.map((validate) => validate(value, state)),
    A.separate,
    Separated.left,
    A.flatten
  )
}

const validateField = <K extends keyof T, T extends object>(
  key: K,
  validators: FormValidators<T>,
  state: FormState<T>
): FormState<T> => ({
  ...state,
  [key]: {
    ...state[key],
    errors: getErrors(key, validators, state),
    validated: true,
  },
})

const validateTouchedFields = <T extends object>(
  validators: FormValidators<T>,
  state: FormState<T>
): FormState<T> =>
  foldFields(
    (accState, key, field) =>
      field.touched ? validateField(key, validators, accState) : accState,
    state,
    state
  )

const validateAllFields = <T extends object>(
  validators: FormValidators<T>,
  state: FormState<T>
): FormState<T> =>
  foldFields(
    (accState, key) => validateField(key, validators, accState),
    state,
    state
  )

const foldFields = <T extends object, S>(
  fn: <K extends keyof T>(acc: S, key: K, field: FieldState<T[K]>) => S,
  initial: S,
  state: FormState<T>
) => {
  let acc = initial
  for (const key in state) {
    acc = fn(acc, key, state[key])
  }
  return acc
}
