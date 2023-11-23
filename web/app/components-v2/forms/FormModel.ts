import * as A from 'fp-ts/Array'
import { pipe } from 'fp-ts/lib/function'
import * as $ from 'optics-ts'
import { Reducer, useCallback, useEffect, useMemo, useReducer } from 'react'
import { ApiResponse } from '../../api-fetch'
import { useGlobalErrors } from '../../appstate/globalErrors'
import { useUser } from '../../appstate/user'
import { t } from '../../i18n/i18n'
import { Constraint } from '../../types/fi/oph/koski/typemodel/Constraint'
import { tap, tapLeft } from '../../util/fp/either'
import { deepEqual } from '../../util/fp/objects'
import { assertNever } from '../../util/selfcare'
import { ValidationRule } from './ValidationRule'
import { validateData, ValidationError } from './validator'
import { storeDeferredPreferences } from '../../appstate/preferences'

export enum EditMode {
  View = 0,
  Edit = 1,
  Saving = 2
}

export type FormModel<O extends object> = {
  // Lomakkeen tietojen viimeisin tila
  readonly state: O
  // Lomakkeen tiedot ennen muokkausta ja tallennuksen jälkeen
  readonly initialState: O
  // Muokkaustila päällä/pois
  readonly editMode: boolean
  // Onko muokkausten tallennus kesken
  readonly isSaving: boolean
  // Lomakkeelle on tehty muutoksia
  readonly hasChanged: boolean
  // Tiedot on tallennettu viimeisimmän muokkaustilaan siirtymisen jälkeen
  readonly isSaved: boolean
  // Lomakkeen tiedot ovat kunnossa (frontissa tehtävän validaation perusteella)
  readonly isValid: boolean
  // Viimeisimmän validoinnin yhteydessä löydetyt virheet
  readonly errors: ValidationError[]
  // Optic lomaketietojen juureen (kts. https://akheron.github.io/optics-ts/)
  readonly root: $.Equivalence<O, $.OpticParams, O>

  /**
   * Aseta lomake muokkaustilaan
   */
  readonly startEdit: () => void

  /**
   * Päivitä lomakkeen tietoja polun määrittelemästä paikasta.
   *
   * @param optic Lens tai Prism joka osoittaa muutettavaan kohtan lomakkeen tiedoissa.
   * @param modify Funktio joka ottaa argumenttina vastaan osoitetun arvon edellisen tilan ja palauttaa uuden.
   * @see https://akheron.github.io/optics-ts/
   */
  readonly updateAt: <T>(optic: FormOptic<O, T>, modify: (t: T) => T) => void

  /**
   * Validoi lomakkeen tiedot lomakkeelle annettua constraintia vasten.
   * Validoinnin tulos tallentuu propertyihin *isValid* ja *errors*.
   */
  readonly validate: () => void

  /**
   * Tallenna lomakkeen tiedot tietokantaan ja siirry pois muokkaustilasta, jos kutsu onnistui.
   *
   * @param api Funktio joka tallentaa datan (löytyvät tiedosta koskiApi.ts)
   * @param merge Funktio joka yhdistää api-kutsun palauttaman datan lomakkeen dataan
   */
  readonly save: <T>(
    api: (data: O) => Promise<ApiResponse<T>>,
    merge: (response: T) => (data: O) => O
  ) => void

  /**
   * Siirry pois muokkaustilasta ja palauta lomakkeen tiedot edeltäneeseen tilaan.
   */
  readonly cancel: () => void
}

/**
 * Luo uuden lomakkeen vapaamuotoiselle datalle.
 *
 * @param initialState Tyhjä tai tietokannasta ladattu tila
 * @param startWithEditMode Jos tosi, muokkaustila on päällä välittömästi
 * @param constraint Constraint (skeema) jota vasten tiedot validoidaan
 * @returns FormModel
 */
export const useForm = <O extends object>(
  initialState: O,
  startWithEditMode = false,
  constraint: Constraint | null,
  validationRules?: ValidationRule<any>[]
): FormModel<O> => {
  type FormModelProp<T extends keyof FormModel<O>> = FormModel<O>[T]

  const user = useUser()
  const init = useMemo(
    () =>
      internalInitialState(
        initialState,
        user?.hasWriteAccess ? startWithEditMode : false,
        constraint
      ),
    // eslint-disable-next-line react-hooks/exhaustive-deps
    []
  )

  const [
    { data, initialData, editMode, hasChanged, isSaved, errors, pending },
    dispatch
  ] = useReducer<Reducer<InternalFormState<O>, Action<O>>>(reducer, init)

  const globalErrors = useGlobalErrors()

  const startEdit: FormModelProp<'startEdit'> = useCallback(() => {
    if (user?.hasWriteAccess) {
      dispatch({ type: 'startEdit', constraint })
    }
  }, [constraint, user?.hasWriteAccess])

  const cancel: FormModelProp<'cancel'> = useCallback(() => {
    dispatch({ type: 'cancel' })
  }, [])

  const setEditMode = useCallback((mode: EditMode) => {
    dispatch({ type: 'setEditMode', editMode: mode })
  }, [])

  const validate: FormModelProp<'validate'> = useCallback(() => {
    if (constraint && editMode) {
      dispatch({ type: 'validate', constraint, rules: validationRules || [] })
    }
  }, [constraint, editMode, validationRules])

  useEffect(() => {
    validate()
  }, [validate])

  const updateAt: FormModelProp<'updateAt'> = useCallback(
    <T>(optic: FormOptic<O, T>, modify: (t: T) => T) => {
      if (editMode) {
        dispatch({
          type: 'modify',
          modify: modifyValue(optic)(modify),
          modifyInitialData: modifiesShape(optic, modify, initialData)
        })
        // Validate after modify
        validate()
      }
    },
    [editMode, initialData, validate]
  )

  const { push: setErrors, clearAll: clearErrors } = globalErrors
  const save: FormModelProp<'save'> = useCallback(
    async <T>(
      api: (data: O) => Promise<ApiResponse<T>>,
      merge: (response: T) => (data: O) => O
    ) => {
      if (editMode) {
        clearErrors()
        // Asetetaan UI pending-tilaan, jolla mahdollistetaan käyttöliittymäelementtien disablointi.
        setEditMode(EditMode.Saving)
        pipe(
          await api(data),
          tap(async (response) => {
            await storeDeferredPreferences()
            dispatch({ type: 'endEdit', value: merge(response.data)(data) })
          }),
          tapLeft((errorResponse) => {
            setEditMode(EditMode.Edit)
            setErrors(
              errorResponse.errors.map((e) => ({ message: t(e.messageKey) }))
            )
          })
        )
      }
    },
    [clearErrors, data, editMode, setEditMode, setErrors]
  )

  const root: FormModelProp<'root'> = useMemo(() => $.optic_<O>(), [])

  return useMemo(
    () => ({
      state: data,
      initialState: initialData,
      editMode: editMode !== EditMode.View,
      isSaving: editMode === EditMode.Saving,
      hasChanged,
      isSaved,
      isValid: A.isEmpty(errors),
      root,
      startEdit,
      pending,
      updateAt,
      validate,
      save,
      cancel,
      errors
    }),
    [
      data,
      initialData,
      editMode,
      hasChanged,
      isSaved,
      errors,
      root,
      startEdit,
      pending,
      updateAt,
      validate,
      save,
      cancel
    ]
  )
}

type InternalFormState<O> = {
  initialData: O
  data: O
  editMode: EditMode
  pending: boolean
  hasChanged: boolean
  isSaved: boolean
  errors: ValidationError[]
}

const internalInitialState = <O>(
  initialState: O,
  startWithEditMode: boolean,
  constraint?: Constraint | null
): InternalFormState<O> => ({
  initialData: initialState,
  data: initialState,
  editMode: startWithEditMode ? EditMode.Edit : EditMode.View,
  hasChanged: false,
  pending: false,
  isSaved: false,
  errors:
    constraint && startWithEditMode
      ? validateData(initialState, constraint)
      : []
})

type StartEdit = { type: 'startEdit'; constraint?: Constraint | null }
type ModifyData<O> = {
  type: 'modify'
  modify: (o: O) => O
  modifyInitialData?: boolean
}
type Cancel = { type: 'cancel' }
type EndEdit<O> = { type: 'endEdit'; value: O }
type Validate = {
  type: 'validate'
  constraint?: Constraint
  rules: ValidationRule[]
}
type SetEditMode = { type: 'setEditMode'; editMode: EditMode }

type Action<O> =
  | StartEdit
  | ModifyData<O>
  | Cancel
  | EndEdit<O>
  | Validate
  | SetEditMode

const reducer = <O>(
  state: InternalFormState<O>,
  action: Action<O>
): InternalFormState<O> => {
  switch (action.type) {
    case 'modify': {
      const data = action.modify(state.data)
      const initialData = action.modifyInitialData
        ? action.modify(state.initialData)
        : state.initialData

      return state.editMode
        ? {
            ...state,
            data,
            initialData,
            hasChanged: state.hasChanged || !deepEqual(state.data, data)
          }
        : state
    }
    case 'startEdit':
      return {
        ...state,
        editMode: EditMode.Edit,
        isSaved: false,
        hasChanged: false,
        errors: action.constraint
          ? validateData(state.data, action.constraint)
          : []
      }
    case 'cancel':
      return {
        ...state,
        editMode: EditMode.View,
        data: state.initialData,
        isSaved: false,
        errors: []
      }
    case 'endEdit':
      return {
        ...state,
        data: action.value,
        initialData: action.value,
        editMode: EditMode.View,
        isSaved: true,
        errors: []
      }
    case 'setEditMode':
      return {
        ...state,
        editMode: action.editMode
      }
    case 'validate': {
      const schemaErrors = action.constraint
        ? validateData(state.data, action.constraint)
        : []
      const ruleErrors = action.rules.flatMap((rule) =>
        validateData(state.data, rule)
      )
      const errors = [...schemaErrors, ...ruleErrors]
      return deepEqual(errors, state.errors)
        ? state
        : {
            ...state,
            errors
          }
    }
    default:
      return state
  }
}

export type FormOptic<S, A> =
  | $.Equivalence<S, any, A>
  | $.Lens<S, any, A>
  | $.Prism<S, any, A>

export const getValue =
  <S, A>(optic: FormOptic<S, A>) =>
  (source: S): A | undefined => {
    switch (optic._tag) {
      case 'Equivalence':
      case 'Lens':
        return $.get(optic)(source)
      case 'Prism':
        return $.preview(optic)(source)
      default:
        assertNever(optic)
    }
  }

const modifyValue =
  <S, A>(optic: FormOptic<S, A>) =>
  (fn: (a: A) => A) =>
  (source: S): S => {
    switch (optic._tag) {
      case 'Equivalence':
      case 'Lens':
      case 'Prism':
        return $.modify(optic)(fn)(source)
      default:
        return assertNever(optic)
    }
  }

const modifiesShape = <O extends object, T>(
  optic: FormOptic<O, T>,
  modify: (t: T) => T,
  data: O
): boolean => {
  const value = getValue(optic)(data)
  if (value === undefined) {
    return true
  }
  if (Array.isArray(value)) {
    const result = modify(value)
    if (!Array.isArray(result) || result.length !== value.length) {
      return true
    }
  }
  return false
}
