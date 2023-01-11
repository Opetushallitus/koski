import React, { useCallback, useMemo } from 'react'
import { useKoodistoFiller } from '../../appstate/koodisto'
import { deepEqual } from '../../util/fp/objects'
import { FieldRenderer, FormModel, FormOptic, getValue } from './FormModel'
import { useFormErrors } from './useFormErrors'

export const FormField = <O extends object, T>({
  // TODO: Tää tyypitys siistemmäksi
  form,
  path,
  updateAlso: secondaryPaths,
  view,
  edit,
  auto,
  errorsFromPath
}: FieldRenderer<O, T> & { form: FormModel<O> }) => {
  const fillKoodistot = useKoodistoFiller()

  const optics = useMemo(
    () => [path, ...(secondaryPaths || [])],
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
    async (newValue: T) => {
      const filledValue = await fillKoodistot(newValue)
      const getValue = () => filledValue
      optics.forEach((optic) => form.updateAt(optic, getValue))
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
          onChange={set}
          errors={errors}
        />
      )
    }
  }

  return <View key="view" value={value} />
}
