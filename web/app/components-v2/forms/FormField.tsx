import React, { useCallback, useMemo } from 'react'
import { useKoodistoFiller } from '../../appstate/koodisto'
import { deepEqual } from '../../util/fp/objects'
import { FormModel, FormOptic, getValue } from './FormModel'
import { useFormErrors } from './useFormErrors'
import { ValidationError } from './validator'

export type FieldViewBaseProps<T> = {
  value?: T | undefined
}

export type FieldEditBaseProps<T> = {
  initialValue?: T | undefined
  value?: T | undefined
  optional: boolean
  onChange: (value?: T) => void
  errors: ValidationError[]
}

export type FormFieldProps<O extends object, T> = {
  form: FormModel<O>

  updateAlso?: Array<FormOptic<O, T>>
  errorsFromPath?: string
  view: React.FC<FieldViewBaseProps<T>>
  edit?: React.FC<FieldEditBaseProps<T>>
  auto?: () => T | undefined
} & (
  | { path: FormOptic<O, T>; optional?: false }
  | { path: FormOptic<O, T | undefined>; optional: true }
)

export const FormField = <O extends object, T>({
  form,
  path,
  updateAlso: secondaryPaths,
  view,
  edit,
  auto,
  errorsFromPath,
  optional
}: FormFieldProps<O, T>) => {
  const fillKoodistot = useKoodistoFiller()

  const optics = useMemo(
    () => [path, ...(secondaryPaths || [])] as FormOptic<O, T | undefined>[],
    [path, ...(secondaryPaths || [])]
  )
  const initialValue = useMemo(
    () => getValue(optics[0])(form.initialState),
    [form.initialState]
  )
  const value = useMemo(() => getValue(optics[0])(form.state), [form.state])

  const errors = useFormErrors(
    form,
    errorsFromPath || (path as any as FormOptic<O, object>)
  )

  const set = useCallback(
    async (newValue?: T) => {
      const filledValue = await fillKoodistot(newValue)
      const getValue = () => filledValue
      optics.forEach((optic) =>
        form.updateAt(optic as FormOptic<O, T | undefined>, getValue)
      )
      form.validate()
    },
    [form]
  )
  const View = useMemo(() => view, [])
  const Edit = useMemo(() => edit, [])

  if (form.editMode) {
    if (auto) {
      const automaticValue = auto()
      if (automaticValue && !deepEqual(automaticValue, value)) {
        set(automaticValue)
      }
      return <View key="auto" value={automaticValue} />
    }

    if (Edit) {
      return (
        <Edit
          key="edit"
          initialValue={initialValue}
          value={value}
          optional={Boolean(optional)}
          onChange={set}
          errors={errors}
        />
      )
    }
  }

  return <View key="view" value={value} />
}
