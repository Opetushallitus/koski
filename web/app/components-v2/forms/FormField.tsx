import React, { useCallback, useMemo } from 'react'
import { useKoodistoFiller } from '../../appstate/koodisto'
import { deepEqual } from '../../util/fp/objects'
import { FormModel, FormOptic, getValue } from './FormModel'
import { useFormErrors } from './useFormErrors'
import { ValidationError } from './validator'

export type FieldViewBaseProps<T, P = {}> = P & {
  value?: T | undefined
}

export type FieldEditBaseProps<T, P = {}> = P & {
  initialValue?: T | undefined
  value?: T | undefined
  optional: boolean
  onChange: (value?: T) => void
  errors: ValidationError[]
}

export type FormFieldProps<
  O extends object,
  T,
  VP,
  EP,
  VIEW_PROPS extends FieldViewBaseProps<T, VP>,
  EDIT_PROPS extends FieldEditBaseProps<T, EP>
> = {
  form: FormModel<O>

  updateAlso?: Array<FormOptic<O, T>>
  errorsFromPath?: string
  view: React.FC<VIEW_PROPS>
  viewProps?: VP
  edit?: React.FC<EDIT_PROPS>
  editProps?: EP
  auto?: () => T | undefined
} & (
  | { path: FormOptic<O, T>; optional?: false }
  | { path: FormOptic<O, T | undefined>; optional: true }
)

export const FormField = <
  O extends object,
  T,
  VP,
  EP,
  VIEW_PROPS extends FieldViewBaseProps<T, VP>,
  EDIT_PROPS extends FieldEditBaseProps<T, EP>
>(
  props: FormFieldProps<O, T, VP, EP, VIEW_PROPS, EDIT_PROPS>
) => {
  const {
    form,
    path,
    updateAlso: secondaryPaths,
    view,
    viewProps,
    edit,
    editProps,
    auto,
    errorsFromPath,
    optional
  } = props
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

  const View = useMemo(() => view, [JSON.stringify(viewProps)])
  const Edit = useMemo(() => edit, [JSON.stringify(editProps)])

  if (form.editMode) {
    if (auto) {
      const automaticValue = auto()
      if (automaticValue && !deepEqual(automaticValue, value)) {
        set(automaticValue)
      }
      // @ts-ignore - TODO tyyppicastaus?
      return <View {...viewProps} key="auto" value={automaticValue} />
    }

    if (Edit) {
      return (
        // @ts-ignore - TODO tyyppicastaus?
        <Edit
          {...editProps}
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

  // @ts-ignore - TODO tyyppicastaus?
  return <View {...viewProps} key="view" value={value} />
}
