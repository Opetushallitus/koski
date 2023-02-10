import * as A from 'fp-ts/Array'
import { constant } from 'fp-ts/lib/function'
import * as NEA from 'fp-ts/NonEmptyArray'
import React, { useCallback, useEffect, useMemo } from 'react'
import { useKoodistoFiller } from '../../appstate/koodisto'
import { deepEqual } from '../../util/fp/objects'
import { FormModel, FormOptic, getValue } from './FormModel'
import { useFormErrors } from './useFormErrors'
import { ValidationError } from './validator'

export type FieldViewerProps<T, P extends object = object> = P & {
  value?: T | undefined
}

export type FieldEditorProps<T, P extends object = object> = P & {
  initialValue?: T | undefined
  value?: T | undefined
  onChange: (value?: T) => void
  optional?: boolean
  errors?: NEA.NonEmptyArray<ValidationError>
}

export type FormFieldProps<
  O extends object,
  T,
  VP extends object,
  EP extends object,
  VIEW_PROPS extends FieldViewerProps<T, VP>,
  EDIT_PROPS extends FieldEditorProps<T, EP>
> = {
  // Lomake johon kenttä kytketään
  form: FormModel<O>
  // Komponentti jota käytetään arvon näyttämiseen normaalitilassa
  view: React.FC<VIEW_PROPS>
  // Näyttökomponentille annettavat lisäpropertyt
  viewProps?: VP
  // Komponentti jota käytetään arvon näyttämiseen ja muokkaamiseen muokkaustilassa
  edit?: React.FC<EDIT_PROPS>
  // Muokkauskomponentille annettavat lisäpropertyt
  editProps?: EP
  // Funktio joka laskee kentän arvon automaattisesti (esim. laajuuksien yhteismäärä). Tätä käytetäessä älä käytä edit-propertya.
  auto?: () => T | undefined
  // Muut kohteet lomakedatassa, jotka päivitetään myös kentän arvoa muokatessa
  updateAlso?: Array<SideUpdate<O, T, any>>
  // Polku mitä käytetään virheiden hakemiseen, jos eri kuin mikä voidaan muodostaa path-propertysta.
  errorsFromPath?: string
} & ( // Polku (Lens tai Prism) joka osoittaa mitä arvoa lomakkeen datasta ollaan muokkaamassa
  | { path: FormOptic<O, T>; optional?: false }
  | { path: FormOptic<O, T | undefined>; optional: true }
)

export type SideUpdate<O extends object, T, S> = (value?: T) => {
  path: FormOptic<O, S>
  update: (sideValue?: S) => S
}

export const sideUpdate =
  <O extends object, T, S>(
    path: FormOptic<O, S>,
    transform: (value?: T, sideValue?: S) => S
  ): SideUpdate<O, T, S> =>
  (value?: T) => ({
    path,
    update: (sideValue?: S) => transform(value, sideValue)
  })

/**
 * Lomakkeen kenttä. Vaihtaa automaattisesti muokkaustilassa käytettävän komponentin ja huolehtii kommunikoinnista FormModelin kanssa.
 *
 * @param props
 * @returns
 */
export const FormField = <
  O extends object,
  T,
  VP extends object,
  EP extends object,
  VIEW_PROPS extends FieldViewerProps<T, VP>,
  EDIT_PROPS extends FieldEditorProps<T, EP>
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

  const initialValue = useMemo(
    () => getValue(path as FormOptic<O, T | undefined>)(form.initialState),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [form.initialState]
  )
  const value = useMemo(
    () => getValue(path as FormOptic<O, T | undefined>)(form.state),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [form.state, path]
  )

  const errors = useFormErrors(
    form,
    errorsFromPath || (path as any as FormOptic<O, object>)
  )

  const set = useCallback(
    async (newValue?: T) => {
      const filledValue = await fillKoodistot(newValue)
      form.updateAt(path as FormOptic<O, T | undefined>, constant(filledValue))
      secondaryPaths?.forEach(<S,>(update: SideUpdate<O, T, S>) => {
        const side = update(filledValue)
        form.updateAt(side.path, side.update)
      })
      form.validate()
    },
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [form, path]
  )

  useEffect(() => {
    if (form.editMode && auto) {
      const automaticValue = auto()
      if (!deepEqual(automaticValue, value)) {
        set(automaticValue)
      }
    }
  }, [form.editMode, auto, value, set, form.state])

  const View = useMemo(
    () => view,
    // eslint-disable-next-line react-hooks/exhaustive-deps
    viewProps ? Object.values(viewProps) : []
  )
  const Edit = useMemo(
    () => edit,
    // eslint-disable-next-line react-hooks/exhaustive-deps
    editProps ? Object.values(editProps) : []
  )

  if (form.editMode) {
    if (Edit) {
      return (
        // @ts-ignore - TODO tyyppicastaus?
        <Edit
          {...editProps}
          initialValue={initialValue}
          value={value}
          optional={Boolean(optional)}
          onChange={set}
          errors={A.isNonEmpty(errors) ? errors : undefined}
          path={path}
        />
      )
    }
  }

  // @ts-ignore - TODO tyyppicastaus?
  return <View {...viewProps} value={value} />
}

export const Nothing: React.FC = () => null
