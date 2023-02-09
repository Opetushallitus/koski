import * as A from 'fp-ts/Array'
import { pipe } from 'fp-ts/lib/function'
import * as $ from 'optics-ts'
import { Reducer, useCallback, useEffect, useMemo, useReducer } from 'react'
import { ApiResponse } from '../../api-fetch'
import { useGlobalErrors } from '../../appstate/globalErrors'
import { useVirkailijaUser } from '../../appstate/user'
import { t } from '../../i18n/i18n'
import { Constraint } from '../../types/fi/oph/koski/typemodel/Constraint'
import { tap, tapLeft } from '../../util/fp/either'
import { deepEqual } from '../../util/fp/objects'
import { assertNever } from '../../util/selfcare'
import { ValidationRule } from './ValidationRule'
import { validateData, ValidationError } from './validator'

export type FormModel<O extends object> = {
  // Lomakkeen tietojen viimeisin tila
  readonly state: O
  // Lomakkeen tiedot ennen muokkausta ja tallennuksen jälkeen
  readonly initialState: O
  // Muokkaustila päällä/pois
  readonly editMode: boolean
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
    merge: (data: O, response: T) => O
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

  const user = useVirkailijaUser()
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
    { data, initialData, editMode, hasChanged, isSaved, errors },
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

  const updateAt: FormModelProp<'updateAt'> = useCallback(
    <T>(optic: FormOptic<O, T>, modify: (t: T) => T) => {
      if (editMode) {
        dispatch({ type: 'modify', modify: modifyValue(optic)(modify) })
      }
    },
    [editMode]
  )

  const validate: FormModelProp<'validate'> = useCallback(() => {
    if (constraint && editMode) {
      dispatch({ type: 'validate', constraint, rules: validationRules || [] })
    }
  }, [constraint, editMode, validationRules])

  useEffect(() => {
    validate()
  }, [validate])

  const { push: setErrors } = globalErrors
  const save: FormModelProp<'save'> = useCallback(
    async <T>(
      api: (data: O) => Promise<ApiResponse<T>>,
      merge: (data: O, response: T) => O
    ) => {
      if (editMode) {
        pipe(
          await api(data),
          tap((response) =>
            dispatch({ type: 'endEdit', value: merge(data, response.data) })
          ),
          tapLeft((errorResponse) =>
            setErrors(
              errorResponse.errors.map((e) => ({ message: t(e.messageKey) }))
            )
          )
        )
      }
    },
    [data, editMode, setErrors]
  )

  const root: FormModelProp<'root'> = useMemo(() => $.optic_<O>(), [])

  return useMemo(
    () => ({
      state: data,
      initialState: initialData,
      editMode,
      hasChanged,
      isSaved,
      isValid: A.isEmpty(errors),
      root,
      startEdit,
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
  editMode: boolean
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
  editMode: startWithEditMode,
  hasChanged: false,
  isSaved: false,
  errors:
    constraint && startWithEditMode
      ? validateData(initialState, constraint)
      : []
})

type StartEdit = { type: 'startEdit'; constraint?: Constraint | null }
type ModifyData<O> = { type: 'modify'; modify: (o: O) => O }
type Cancel = { type: 'cancel' }
type EndEdit<O> = { type: 'endEdit'; value: O }
type Validate = {
  type: 'validate'
  constraint?: Constraint
  rules: ValidationRule[]
}
type Action<O> = StartEdit | ModifyData<O> | Cancel | EndEdit<O> | Validate

const reducer = <O>(
  state: InternalFormState<O>,
  action: Action<O>
): InternalFormState<O> => {
  switch (action.type) {
    case 'modify': {
      const data = action.modify(state.data)
      return state.editMode
        ? {
            ...state,
            data,
            hasChanged: state.hasChanged || !deepEqual(state.data, data)
          }
        : state
    }
    case 'startEdit':
      return {
        ...state,
        editMode: true,
        isSaved: false,
        hasChanged: false,
        errors: action.constraint
          ? validateData(state.data, action.constraint)
          : []
      }
    case 'cancel':
      return {
        ...state,
        editMode: false,
        data: state.initialData,
        isSaved: false,
        errors: []
      }
    case 'endEdit':
      return {
        ...state,
        data: action.value,
        initialData: action.value,
        editMode: false,
        isSaved: true,
        errors: []
      }
    case 'validate': {
      const schemaErrors = action.constraint
        ? validateData(state.data, action.constraint)
        : []
      const ruleErrors = action.rules.flatMap((rule) =>
        validateData(state.data, rule)
      )
      const errors = [...schemaErrors, ...ruleErrors]
      return {
        ...state,
        errors: deepEqual(errors, state.errors) ? state.errors : errors
      }
    }
    default:
      return state
  }
}

export type FormOptic<S, A> = $.Lens<S, any, A> | $.Prism<S, any, A>

export const getValue =
  <S, A>(optic: FormOptic<S, A>) =>
  (source: S): A | undefined => {
    switch (optic._tag) {
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
      case 'Lens':
      case 'Prism':
        return $.modify(optic)(fn)(source)
      default:
        return assertNever(optic)
    }
  }
