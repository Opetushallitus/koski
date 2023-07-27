import * as A from 'fp-ts/Array'
import { constant } from 'fp-ts/lib/function'
import * as NEA from 'fp-ts/NonEmptyArray'
import React, { useCallback, useEffect, useMemo } from 'react'
import { useKoodistoFiller } from '../../appstate/koodisto'
import { deepEqual } from '../../util/fp/objects'
import { FormModel, FormOptic, getValue } from './FormModel'
import { useFormErrors } from './useFormErrors'
import { ValidationError } from './validator'

/*

type Foo = {
  foo: string
}

type Bar = {
  bar: number
}

const FooBarBazView: React.FC<FieldViewerProps<string, Foo>> = (
  props
): React.ReactElement | null => <div>{props.foo}</div>

const FooBarBazEdit: React.FC<FieldEditorProps<string, Bar>> = (
  props
): React.ReactElement | null => <div>{props.bar}</div>

const FooBarField = ({ form, path }: any) => (
  <FormField
    form={form}
    path={path}
    view={FooBarBazView}
    viewProps={{
      foo: '5'
    }}
    edit={FooBarBazEdit}
    editProps={{
      bar: 6
    }}
  />
)

*/

export type FieldViewerProps<FieldValue, ViewerProps> = ViewerProps & {
  value?: FieldValue | undefined
  testId?: string
}

export type FieldEditorProps<FieldValue, EditorProps> = EditorProps & {
  initialValue?: FieldValue | undefined
  onChange: (value?: FieldValue) => void
  optional?: boolean
  errors?: NEA.NonEmptyArray<ValidationError>
  value?: FieldValue | undefined
  testId?: string
}

type ComponentType<T> =
  | React.FunctionComponent<T>
  | ((props: T) => React.ReactElement | null)

type ExtractComponentProps<T> = T extends ComponentType<infer P> ? P : never

type ExtractViewerProps<T> = T extends FieldViewerProps<any, infer K>
  ? K
  : never
type ExtractEditorProps<T> = T extends FieldEditorProps<any, infer K>
  ? K
  : never

export type FormFieldProps<
  FormState extends object,
  FieldValue,
  VIEW_COMPONENT extends React.FunctionComponent<
    FieldViewerProps<FieldValue, any>
  >,
  EDIT_COMPONENT extends React.FunctionComponent<
    FieldEditorProps<FieldValue, any>
  >
> = {
  // Lomake johon kenttä kytketään
  form: FormModel<FormState>
  // Komponentti jota käytetään arvon näyttämiseen normaalitilassa
  view: VIEW_COMPONENT
  // Näyttökomponentille annettavat lisäpropertyt
  viewProps?: Partial<ExtractViewerProps<ExtractComponentProps<VIEW_COMPONENT>>>
  // Komponentti jota käytetään arvon näyttämiseen ja muokkaamiseen muokkaustilassa
  edit?: EDIT_COMPONENT
  // Muokkauskomponentille annettavat lisäpropertyt
  editProps?: Partial<ExtractEditorProps<ExtractComponentProps<EDIT_COMPONENT>>>
  // Funktio joka laskee kentän arvon automaattisesti (esim. laajuuksien yhteismäärä). Tätä käytetäessä älä käytä edit-propertya.
  auto?: () => FieldValue | undefined
  // Muut kohteet lomakedatassa, jotka päivitetään myös kentän arvoa muokatessa
  updateAlso?: Array<SideUpdate<FormState, FieldValue, any>>
  // Polku mitä käytetään virheiden hakemiseen, jos eri kuin mikä voidaan muodostaa path-propertysta.
  errorsFromPath?: string
  testId?: string
} & ( // Polku (Lens tai Prism) joka osoittaa mitä arvoa lomakkeen datasta ollaan muokkaamassa
  | { path: FormOptic<FormState, FieldValue>; optional?: false }
  | { path: FormOptic<FormState, FieldValue | undefined>; optional: true }
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
  FormState extends object,
  FieldValue,
  VIEW_COMPONENT extends React.FunctionComponent<
    FieldViewerProps<FieldValue, any>
  >,
  EDIT_COMPONENT extends React.FunctionComponent<
    FieldEditorProps<FieldValue, any>
  >
>(
  props: FormFieldProps<FormState, FieldValue, VIEW_COMPONENT, EDIT_COMPONENT>
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
    optional,
    testId
  } = props
  const fillKoodistot = useKoodistoFiller()

  const initialValue = useMemo(
    () =>
      getValue(path as FormOptic<FormState, FieldValue | undefined>)(
        form.initialState
      ),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [form.initialState]
  )
  const value = useMemo(
    () =>
      getValue(path as FormOptic<FormState, FieldValue | undefined>)(
        form.state
      ),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    [form.state, path]
  )

  const errors = useFormErrors(
    form,
    errorsFromPath || (path as any as FormOptic<FormState, object>)
  )

  const set = useCallback(
    async (newValue?: FieldValue) => {
      const filledValue = await fillKoodistot(newValue)
      form.updateAt(
        path as FormOptic<FormState, FieldValue | undefined>,
        constant(filledValue)
      )
      secondaryPaths?.forEach(
        <S,>(update: SideUpdate<FormState, FieldValue, S>) => {
          const side = update(filledValue)
          form.updateAt(side.path, side.update)
        }
      )
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
          testId={testId && `${testId}.edit`}
        />
      )
    }
  }

  return (
    // @ts-ignore - TODO tyyppicastaus?
    <View {...viewProps} value={value} testId={testId && `${testId}.value`} />
  )
}

export const Nothing: React.FC = () => null
