import * as A from 'fp-ts/Array'
import { pipe } from 'fp-ts/lib/function'
import * as $ from 'optics-ts'
import {
  Dispatch,
  Reducer,
  ReducerAction,
  useCallback,
  useEffect,
  useMemo,
  useReducer
} from 'react'
import { ApiResponse } from '../../api-fetch'
import { useGlobalErrors } from '../../appstate/globalErrors'
import { t } from '../../i18n/i18n'
import { Constraint } from '../../types/fi/oph/koski/typemodel/Constraint'
import { tap, tapLeft } from '../../util/fp/either'
import { deepEqual } from '../../util/fp/objects'
import { parsePath } from '../../util/optics'
import { validateData, ValidationError } from './validator'

export type FormModel<O extends object> = {
  readonly state: O
  readonly initialState: O
  readonly editMode: boolean
  readonly hasChanged: boolean
  readonly isSaved: boolean
  readonly isValid: boolean
  readonly root: $.Equivalence<O, $.OpticParams, O>

  readonly startEdit: () => void
  readonly updateAt: <T>(optic: FormOptic<O, T>, modify: (t: T) => T) => void
  readonly validate: () => void
  readonly save: <T>(
    api: (data: O) => Promise<ApiResponse<T>>,
    merge: (data: O, response: T) => O
  ) => void
  readonly cancel: () => void
  readonly errors: ValidationError[]
}

export type FormModelListener<O extends object> = (obj: O) => void

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
type Validate = { type: 'validate'; constraint: Constraint }
type Action<O> = StartEdit | ModifyData<O> | Cancel | EndEdit<O> | Validate

const reducer = <O>(
  state: InternalFormState<O>,
  action: Action<O>
): InternalFormState<O> => {
  switch (action.type) {
    case 'modify':
      const data = action.modify(state.data)
      return state.editMode
        ? {
            ...state,
            data,
            hasChanged: state.hasChanged || !deepEqual(state.data, data)
          }
        : state
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
    case 'validate':
      return {
        ...state,
        errors: validateData(state.data, action.constraint)
      }
    default:
      return state
  }
}

export const useForm = <O extends object>(
  initialState: O,
  startWithEditMode: boolean = false,
  constraint?: Constraint | null
): FormModel<O> => {
  const init = useMemo(
    () => internalInitialState(initialState, startWithEditMode, constraint),
    []
  )

  const [
    { data, initialData, editMode, hasChanged, isSaved, errors },
    dispatch
  ] = useReducer(reducer, init) as [
    InternalFormState<O>,
    Dispatch<ReducerAction<Reducer<O, Action<O>>>>
  ]

  const globalErrors = useGlobalErrors()

  const startEdit = useCallback(() => {
    dispatch({ type: 'startEdit', constraint })
  }, [])

  const cancel = useCallback(() => {
    dispatch({ type: 'cancel' })
  }, [])

  const updateAt = useCallback(
    async <T>(optic: FormOptic<O, T>, modify: (t: T) => T) => {
      dispatch({ type: 'modify', modify: modifyValue(optic)(modify) })
    },
    []
  )

  const validate = useCallback(() => {
    if (constraint && editMode) {
      dispatch({ type: 'validate', constraint })
    }
  }, [constraint, editMode])

  useEffect(() => {
    validate()
  }, [validate])

  const save = useCallback(
    async <T>(
      api: (data: O) => Promise<ApiResponse<T>>,
      merge: (data: O, response: T) => O
    ) => {
      pipe(
        await api(data),
        tap((response) =>
          dispatch({ type: 'endEdit', value: merge(data, response.data) })
        ),
        tapLeft((e) =>
          globalErrors.push(e.errors.map((e) => ({ message: t(e.messageKey) })))
        )
      )
    },
    [data]
  )

  return useMemo(
    () => ({
      state: data,
      initialState: initialData,
      editMode,
      hasChanged,
      isSaved,
      isValid: A.isEmpty(errors),
      root: $.optic_<O>(),
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
      startEdit,
      updateAt,
      validate,
      save,
      cancel,
      errors
    ]
  )
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
        // @ts-expect-error - seuraava rivi antaa virheen, jos kaikki caset on käsitelty
        optic._tag
    }
  }

const modifyValue =
  <S, A>(optic: FormOptic<S, A>) =>
  (fn: (a: A) => A) =>
  (source: S): S => {
    switch (optic._tag) {
      case 'Lens':
      case 'Prism':
        console.log('heh', optic, fn, source)
        console.log('path', parsePath(optic as any, source))
        return $.modify(optic)((a) => {
          console.log('modify', a, '-->', fn(a))
          return fn(a)
        })(source)
      default:
        // @ts-expect-error - seuraava rivi antaa virheen, jos kaikki caset on käsitelty
        optic._tag
        return source
    }
  }
