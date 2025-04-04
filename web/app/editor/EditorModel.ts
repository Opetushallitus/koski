/* eslint-disable */
import Bacon from 'baconjs'
import * as L from 'partial.lenses'
import * as R from 'ramda'
import {
  ChangeBus,
  ChangeBusContext,
  ContextData,
  Contextualized,
  DataPath,
  EditorMappingContext,
  hasContext,
  ValidationContext
} from '../types/EditorModelContext'
import {
  BooleanModel,
  ContextualizedObjectModelProperty,
  DateModel,
  DateTimeModel,
  EditableModel,
  EditorModel,
  hasTitle,
  hasValue,
  Identified,
  isEditableModel,
  isEnumeratedModel,
  isIdentified,
  isListModel,
  isObjectModel,
  isOneOfModel,
  isPrototypeModel,
  isSomeOptionalModel,
  isValueModel,
  ListModel,
  Maybe,
  MaybeOneOfModel,
  NotWhen,
  NumberModel,
  ObjectModel,
  ObjectModelProperty,
  OneOfModel,
  OnlyWhen,
  OptionalModel,
  PrototypeModel,
  StringModel,
  ValueModelValue
} from '../types/EditorModels'
import { flatMapArray, notUndefined } from '../util/util'
import { hashAdd, hashCode } from './hashcode'
import { filterObjByKey } from '../util/fp/objects'

export type EditorElement = JSX.Element & {
  isEmpty?: (model: EditorModel) => boolean
  validateModel?: (model: EditorModel) => undefined | ModelError[]
  canShowInline?: () => boolean
  handlesOptional?: () => boolean
  writeOnly?: () => boolean
  createEmpty?: <T extends EditorModel>(model: T) => T
}

export type PathExpr = any
export type ModelErrorRecord = Record<string, ModelError[]>
export type ModelError = {
  key: string
  message?: string
  path: DataPath
}

export type ChangeBusAction = ChangeBusUpdateAction | ChangeBusRemoveAction
export type ChangeBusUpdateAction = EditorModel & Contextualized
export type ChangeBusRemoveAction = {
  _remove: EditorModel & Contextualized
}

const isRemoveAction = (
  change: ChangeBusAction
): change is ChangeBusRemoveAction =>
  (change as ChangeBusRemoveAction)._remove !== undefined

// Find submodel with given path
export const modelLookupRequired = (mainModel: EditorModel, path: string) => {
  const model = modelLookup(mainModel, path)
  if (!model) {
    throwError('model for ' + path + ' not found', mainModel)
  }
  return model
}

export const modelLookup = <
  T extends object,
  M extends EditorModel = EditorModel
>(
  mainModel?: M,
  path?: PathExpr
): (M & Contextualized<T>) | undefined => {
  return L.get(modelLens(path), mainModel)
}

export const lensedModel = <
  M extends EditorModel & Contextualized<T>,
  S extends EditorModel,
  T extends object
>(
  model: M,
  lens: L.Lens<M, S>
): (S & Contextualized<T>) | undefined => {
  const modelFromLens = L.get<M, S>(lens, model)
  if (!modelFromLens) {
    return throwError('lens returned ' + modelFromLens, model, lens)
  }
  return contextualizeSubModel(modelFromLens, model, lens)
}

export const modelLens = <T, S>(path: PathExpr): L.Lens<T, S> => {
  const pathElems = toPath(path)
  const pathLenses = pathElems.map((key) =>
    L.compose(parseModelStep(key), manageModelIdLens)
  )
  return L.compose(manageModelIdLens, ...pathLenses)
}

const parseModelStep = <T, S>(key: any): L.Lens<T, S> => {
  if (key == '..') return modelParentLens as L.Lens<T, S>
  if (!isNaN(parseInt(key))) return modelItemLens(parseInt(key))
  if (typeof key === 'string') return modelPropertyValueLens(key)
  if (typeof key === 'function') return key as L.Lens<T, S> // an actual lens then
  return throwError('Unexpected model path element', key)
}

const modelParentLens = L.lens(
  (m?: Contextualized<object>) => m && findParent(m),
  (_v, m?: Contextualized<object>) =>
    throwError('Cannot set parent of model', m)
)

const findParent = <T extends object>(
  model: Contextualized<T>
): Contextualized<T> | undefined => {
  if (!model.parent) return undefined
  if (typeof R.last(model.path || []) === 'function') {
    // Skip lenses in path, only consider actual path elements
    return findParent(model.parent)
  }
  return model.parent
}

const ensureModelId = <T extends EditorModel>(
  model?: T,
  force?: boolean
): Identified<T> | undefined => {
  if (model && (force || !isIdentified(model))) {
    ;(model as any).modelId = calculateModelId(model)
    ;(model as any).data = null // reset modelData caching
  }
  return model as Identified<T>
}

const getModelId = (model: EditorModel): number => {
  return ensureModelId(model)!.modelId
}

const calculateModelId = (m: EditorModel) => {
  let id = 0
  if (isObjectModel(m) && m.value && m.value.properties) {
    id = 1 // to distinguish from a null value
    for (var i in m.value.properties) {
      id = hashAdd(id, getModelId(m.value.properties[i].model))
    }
  }
  if (isListModel(m) && m.value) {
    id = 1 // to distinguish from a null value
    for (var i in m.value) {
      id = hashAdd(id, getModelId(m.value[i]))
    }
  }
  if (
    (isEnumeratedModel(m) || isValueModel(m)) &&
    m.value &&
    m.value.data !== undefined &&
    m.value.data !== null
  ) {
    id = hashAdd(id, hashCode(m.value.data))
  }
  // hack to fix broken OrganisaatioPicker
  if (isObjectModel(m) && typeof (m as any).value?.data === 'object') {
    id = 1
    id = hashAdd(id, hashCode((m as any).value.data))
  }
  return id
}

const manageModelIdLens = L.lens(
  (m?: EditorModel) => {
    return ensureModelId(m)
  },
  (m1) => {
    return ensureModelId(m1, true) // forces calculation model id on the result
  }
)

export function modelData(mainModel: undefined, path?: PathExpr): undefined
export function modelData(
  mainModel: NumberModel,
  path?: PathExpr
): ValueModelValue<number>
export function modelData(
  mainModel: BooleanModel,
  path?: PathExpr
): ValueModelValue<boolean>
export function modelData(
  mainModel: DateModel,
  path?: PathExpr
): ValueModelValue<Date>
export function modelData(
  mainModel: DateTimeModel,
  path?: PathExpr
): ValueModelValue<Date>
export function modelData(
  mainModel: StringModel,
  path?: PathExpr
): ValueModelValue<string>
export function modelData(mainModel?: EditorModel, path?: PathExpr): any

// TODO: Tämä funktio on vaikea ymmärtää ja se tuntuu mutatoivan annettua objektia --> Pitäisi uudelleenkirjoittaa.
export function modelData(mainModel?: any, path?: PathExpr): any {
  const anyModel = mainModel as any
  if (!anyModel || (!anyModel.value && !('arrayPrototype' in anyModel))) return

  if (anyModel.value && anyModel.value.data) {
    if (!path) return anyModel.value.data
    return L.get(objectLens(path), anyModel.value.data)
  }

  const head = toPath(path).slice(0, 1)
  if (head.length > 0) {
    const model = modelLookup(anyModel, head)
    const tail = toPath(path).slice(1)
    return modelData(model, tail)
  } else {
    if (anyModel.data) return anyModel.data
    if (!anyModel || !anyModel.value) return
    if (anyModel.value.properties) {
      return (anyModel.data = R.fromPairs(
        modelProperties(anyModel).map((p: any) => [p.key, modelData(p.model)])
      ))
    } else if (anyModel.value instanceof Array) {
      return (anyModel.data = modelItems(anyModel).map((item) =>
        modelData(item)
      ))
    } else {
      return (anyModel.data = anyModel.value.data)
    }
  }
}

export const modelTitle = (
  mainModel: EditorModel,
  path?: PathExpr,
  titleFormatter?: (m: EditorModel) => string
): string => {
  const model = modelLookup(mainModel, path)
  if (model && titleFormatter !== undefined) {
    return titleFormatter(model)
  }
  return (
    (model &&
      ((hasTitle(model) && model.title) ||
        // @ts-expect-error
        (model.value && model.value.title) ||
        // @ts-expect-error
        (model.value && '' + modelData(model)))) ||
    ''
  )
}

export const modelEmpty = (mainModel: EditorModel, path?: PathExpr) => {
  const model = modelLookup(mainModel, path)
  if (!model?.context) return throwError('context missing')

  const editor = isEditableModel(model) ? getEditor(model) : undefined
  if (editor && editor.isEmpty) {
    return editor.isEmpty(model)
  }
  return (
    !model ||
    !hasValue(model) ||
    (valueEmpty(model.value) && itemsEmpty(modelItems((model as any).items)))
  )
}

export const recursivelyEmpty = (m: EditorModel) => {
  if (!hasValue(m)) return true
  if (isSomeOptionalModel(m)) return false
  if (isObjectModel(m)) {
    if (!m.value.properties) return true
    for (const i in m.value.properties) {
      if (!recursivelyEmpty(m.value.properties[i].model)) return false
    }
    return true
  }
  if (isListModel(m)) {
    return m.value.length == 0
  }
  return !(m as any)?.value?.data
}

function withCatch<T extends any[], S>(name: string, fn: (...a: T) => S) {
  return function (...args: T) {
    try {
      return fn.apply(null, args)
    } catch (e) {
      console.error('Error in ' + name, ...args, e)
      throw e
    }
  }
}

export const modelSet = withCatch(
  'modelSet',
  <M extends EditorModel, N extends EditorModel>(
    mainModel: M,
    newModel: N,
    path?: PathExpr
  ): M => {
    return L.set(modelLens<N, M>(path), newModel, mainModel) as M
  }
)

export const modelSetData = (
  model: EditorModel,
  data: any,
  path?: PathExpr
) => {
  return modelSetValue(model, { data }, path)
}

export const modelSetTitle = <T extends EditorModel>(
  model: T,
  title: string,
  path?: PathExpr
): T => {
  return modelSetValue(
    model,
    L.set('title', title, hasValue(model) ? model.value : undefined),
    path
  )
}

export const modelSetValue = withCatch(
  'modelSetValue',
  <M extends EditorModel, T extends any>(
    model: M,
    value: T,
    path?: PathExpr
  ): M => {
    return L.set(L.compose(modelLens(path), modelValueLens), value, model) as M
  }
)

export const modelSetValues = <T extends EditorModel>(
  model: T,
  pathsAndValues: Record<string, any>
): T => {
  return R.reduce(
    (m, [path, value]) => modelSetValue(m, value, path),
    model,
    R.toPairs(pathsAndValues)
  )
}

export const modelValueLens = L.lens(
  (m: any) => {
    if (!m) {
      return undefined
    }
    return m.value
  },
  (v, m: any) => {
    if (!m) {
      if (v) {
        throwError('trying to set value of null model to a non-null value', v)
      } else {
        return m
      }
    }
    if (m.type == 'prototype') {
      if (v) {
        throwError(
          `trying to set value of an unresolved prototype model (${m.key}) to a non-null value`,
          v
        )
      }
    }
    const plainOptional = m.optional && !m.type
    const usedModel = plainOptional ? getUsedModelForOptionalModel(m) : m
    return L.set('value', v, usedModel)
  }
)

const throwError = (msg: string, ...args: any[]) => {
  console.error(msg, ...args)
  throw new Error(msg)
}

const getUsedModelForOptionalModel = (
  m: EditorModel & OptionalModel & Contextualized<EditorMappingContext>,
  { model }: { model?: EditorModel & Contextualized } = {}
) => {
  if (!model) model = m
  if (hasValue(m)) return m
  if (!hasContext(m)) {
    m = contextualizeSubModel(m, model)!
  }
  const prototypeModel = optionalPrototypeModel(m)
  const editor =
    prototypeModel && isEditableModel(prototypeModel)
      ? getEditor(prototypeModel)
      : model && isEditableModel(model)
        ? getEditor(model)
        : undefined
  const createEmpty = (editor && editor.createEmpty) || R.identity
  const emptyModel = createEmpty(prototypeModel || model!)

  return emptyModel
}

export const wrapOptional = (model?: EditorModel & Contextualized) => {
  if (!model)
    throw new Error('model missing. remember to wrap model like { model }')
  if (!isSomeOptionalModel(model)) return model
  if (!model.context) throw new Error('cannot wrap without context')

  return lensedModel(model, optionalModelLens({ model }))
}

export const optionalModelLens = <T extends EditorModel & Contextualized, S>({
  model
}: {
  model: T
}): L.Lens<T, S> => {
  return L.lens(
    (m: any) => {
      return getUsedModelForOptionalModel(m, { model })
    },
    (newModel: any, contextModel: any) => {
      if (modelEmpty(newModel)) {
        return createOptionalEmpty(contextModel)
      } else {
        return modelSetValue(
          getUsedModelForOptionalModel(contextModel, { model }),
          newModel.value
        )
      }
    }
  )
}

const preparePrototypeModel = <
  P extends EditorModel & Maybe<Contextualized<T>>,
  T extends object
>(
  prototypeModel: P | undefined,
  forModel: EditorModel & Contextualized<T>
): P | undefined => {
  if (!prototypeModel) return prototypeModel
  if (prototypeModel.context) {
    return prototypeModel as P
  }

  // includes all attributes from parent model (like maxLines etc that come from property annotations)
  return {
    ...forModel,
    ...(contextualizeSubModel(prototypeModel, forModel as any) || {}),
    parent: forModel.parent
  } as P
}

export const optionalPrototypeModel = <
  P extends EditorModel & OptionalModel & MaybeOneOfModel & Contextualized
>(
  model: P
): P | undefined => {
  let prototype
  if (isSomeOptionalModel(model)) {
    prototype = model.optionalPrototype = preparePrototypeModel(
      model.optionalPrototype as P,
      model
    )!
  }
  if (!prototype) return prototype
  if (isOneOfModel(prototype) && !modelData(prototype)) {
    // This is a OneOfModel, just pick the first alternative
    // @ts-expect-error
    prototype = prototype.oneOfPrototypes[0] = preparePrototypeModel(
      prototype.oneOfPrototypes[0] as P,
      model
    )!
  }
  return R.mergeRight(prototype, createOptionalEmpty(model)) as P // Ensure that the prototype model has optional flag and optionalPrototype
}

export const createOptionalEmpty = <M extends EditorModel & OptionalModel>(
  optModel: M
): OptionalModel =>
  isSomeOptionalModel(optModel)
    ? {
        optional: optModel.optional,
        optionalPrototype: optModel.optionalPrototype
      }
    : {}

export const resetOptionalModel = <
  M extends EditorModel & OptionalModel & Contextualized<ChangeBusAction>
>(
  model: M
): void => {
  const m = contextualizeChild(model, createOptionalEmpty(model) as any)
  if (m) pushModel(m as any)
}

export const modelItems = <M extends EditorModel>(
  mainModel?: M,
  path?: PathExpr
): M[] => {
  const model = modelLookup(mainModel, path)
  const items = modelItemsRaw(model)
  return items.map((_item, index) => {
    return modelLookup(model, index) as M
  })
}

export const hasModelProperty = (
  mainModel: EditorModel & Maybe<Contextualized>,
  key: string
): boolean => {
  return (
    isObjectModel(mainModel) &&
    hasContext(mainModel) &&
    !!findModelProperty(mainModel, (p) => p.key == key)
  )
}

export const findModelProperty = (
  mainModel: ObjectModel & Contextualized,
  filter: (p: ObjectModelProperty) => boolean
) => {
  if (!mainModel.value) return undefined
  const found = mainModel.value.properties.find(filter)
  return contextualizeProperty(mainModel)(found)
}

export const modelProperty = <
  M extends ObjectModel & Contextualized<T>,
  T extends object
>(
  mainModel: M,
  path: PathExpr
): ContextualizedObjectModelProperty<M, T> | undefined => {
  path = toPath(path)
  if (path.length > 1) {
    return modelProperty(
      modelLookup(mainModel, path.slice(0, -1))!,
      path.slice(-1)
    )
  }
  if (!mainModel.value || !mainModel.value.properties) {
    throwError('No properties found', mainModel)
  }
  const found = mainModel.value.properties.find((p) => p.key == path[0])
  if (!found) {
    return undefined
  }
  return contextualizeProperty(mainModel)(found)
}

export const modelProperties = <
  M extends EditorModel & Contextualized<T>,
  T extends object
>(
  mainModel: M,
  pathsOrFilter?: PathExpr
): ContextualizedObjectModelProperty<M, T>[] => {
  if (isObjectModel(mainModel)) {
    if (pathsOrFilter && pathsOrFilter instanceof Array) {
      return flatMapArray(pathsOrFilter, (p) => {
        const prop = modelProperty(mainModel, p)
        return prop ? [prop] : []
      })
    }
    let props = modelPropertiesRaw(mainModel).map(
      contextualizeProperty(mainModel)
    )
    if (pathsOrFilter && typeof pathsOrFilter === 'function') {
      props = props.filter(pathsOrFilter)
    }
    return props.filter(notUndefined)
  }
  return []
}

export const oneOfPrototypes = <
  M extends EditorModel & OneOfModel & Contextualized
>(
  model?: M
) => {
  if (!model) return []
  if (model.oneOfPrototypes) {
    return (model.oneOfPrototypes = model.oneOfPrototypes
      .map((proto) => preparePrototypeModel(proto, model))
      .filter(notUndefined)
      .filter((m) => checkNotWhen(m, m.notWhen))
      .filter((m) => checkOnlyWhen(m, m.onlyWhen)))
  }
  return [model]
}

// Add the given context to the model and all submodels. Submodels get a copy where their full path is included,
// so that modifications can be targeted to the correct position in the data that's to be sent to the server.
export const contextualizeModel = <M extends EditorModel, T extends object>(
  model: M,
  context: T,
  path?: PathExpr
): M & Contextualized<T> => {
  if (!context) {
    return throwError('context missing')
  }
  const protoModel = resolvePrototypeReference(model, context)
  if (!protoModel) return protoModel
  return R.mergeRight(protoModel, {
    context,
    path: childPath(protoModel, path)
  }) as unknown as M & Contextualized<T>
}

// TODO: don't call this for arrayPrototype. Add arrayPrototype accessor instead
export function contextualizeSubModel<M extends EditorModel, T extends object>(
  subModel?: M,
  parentModel?: Contextualized<T>,
  path?: PathExpr
): (M & Contextualized<T>) | undefined {
  if (!subModel) return subModel
  if (!parentModel) return parentModel
  const model = resolvePrototypeReference(subModel, parentModel.context)
  if (!model) return model
  const subPath = childPath(parentModel, path)
  return R.mergeRight(model, {
    context: parentModel.context,
    path: subPath,
    parent: parentModel
  }) as unknown as M & Contextualized<T>
}

// Add more context parameters to the current context of the model.
export const addContext = <
  M extends EditorModel & Maybe<Contextualized>,
  T extends object
>(
  model: M,
  additionalContext: T
): M & Contextualized<T> => {
  additionalContext = removeUndefinedValues(additionalContext)
  return contextualizeModel(
    model,
    model.context
      ? (R.mergeRight(model.context, additionalContext) as unknown as T)
      : additionalContext
  )
}

export const modelValid = (
  model: EditorModel & Contextualized,
  recursive = true
): boolean => {
  const errors = modelErrors(model, recursive)
  const valid = R.keys(errors).length == 0
  // if (!valid) console.log("errors", errors)
  return valid
}

export const modelErrorMessages = <T extends ValidationContext>(
  model: Contextualized<T>,
  recursive = true
) => {
  return R.uniq(
    R.unnest(R.values(modelErrors(model, recursive)))
      .filter((e) => e.message)
      .map((e) => e.message)
  )
}

const modelErrors = <T extends ValidationContext>(
  model: Contextualized<T>,
  recursive = true
): ModelErrorRecord => {
  const context = model.context
  const pathString = justPath(model.path).join('.')
  const keyMatch = <T>([key, _value]: [string, T]) =>
    recursive
      ? pathString === key || R.startsWith(pathString + '.', key)
      : pathString === key
  const validationResult = (context && context.validationResult) || {}
  return pathString.length
    ? R.fromPairs(R.toPairs(validationResult).filter(keyMatch))
    : validationResult
}

export const applyChangesAndValidate = <
  M extends EditorModel &
    Contextualized<EditorMappingContext & ValidationContext>
>(
  modelBeforeChange: M,
  changes: ChangeBusAction[]
) => {
  type AppliedChanges = { model: M; scopes: string[] }

  const basePath = toPath(modelBeforeChange.path)
  const withAppliedChanges = changes.reduce(
    (acc: AppliedChanges, change: ChangeBusAction) => {
      //console.log('apply', change, 'to', acc)

      const subPath = removeCommonPath(
        toPath(getPathFromChange(change)),
        basePath
      )
      const actualLens = modelLens(subPath)
      const scope = getValidationScope(subPath)

      return {
        model: L.set(actualLens, getModelFromChange(change), acc.model) as M,
        scopes: R.uniq([...acc.scopes, scope])
      }
    },
    { model: modelBeforeChange, scopes: [] } satisfies AppliedChanges
  )

  return applyValidationScopes(
    validateModel(withAppliedChanges.model),
    withAppliedChanges.scopes
  )
}

const getValidationScope = (path: string[]): string => {
  switch (path[0]) {
    case 'opiskeluoikeudet':
      return path.slice(0, 2).join('.') // Esim. 'opiskeluoikeudet.0'
    default:
      return ''
  }
}

const applyValidationScopes = <
  M extends EditorModel & Contextualized<ValidationContext>
>(
  model: M,
  scopes: string[]
) => {
  const validationScope = R.uniq([
    ...(model.context.validationScope || []),
    ...scopes
  ])

  const errors = model.context.validationResult
  if (errors) {
    const inValidationScope = (path: string) =>
      !!validationScope.find((s) => path.startsWith(s))
    const validationResult =
      filterObjByKey<typeof errors>(inValidationScope)(errors)
    return addContext(model, { validationResult, validationScope })
  }

  return model
}

// adds validationResult to model.context
export const validateModel = <
  M extends EditorModel & Contextualized<EditorMappingContext>
>(
  mainModel: M
): M & Contextualized<ValidationContext> => {
  const context = mainModel.context
  if (!context) throwError('context missing')

  const pushError =
    (model: EditorModel & Contextualized, results: ModelErrorRecord) =>
    (error: ModelError) => {
      const path = justPath(model.path)
      const fullPath = path.concat(error.path || []).join('.')
      results[fullPath]
        ? results[fullPath].push(error)
        : (results[fullPath] = [error])
    }

  const validateInner = (
    model: EditableModel & Contextualized<EditorMappingContext>,
    results: ModelErrorRecord
  ) => {
    const validator = getValidator(model, context)
    if (validator) {
      const myResult = validator(model)
      if (myResult) {
        myResult.forEach(pushError(model, results))
      }
    }
    modelProperties(model).forEach((p) => {
      validateInner(p.model, results)
    })
    modelItems(model).forEach((item) => {
      validateInner(item, results)
    })
    return results
  }

  const validationResult = isEditableModel(mainModel)
    ? validateInner(mainModel, {})
    : {}

  return addContext(mainModel, { validationResult })
}

export const getPathFromChange = (
  change: ChangeBusAction
): DataPath | undefined => {
  const modelForPath = isRemoveAction(change) ? change._remove : change
  return modelForPath.path
}

export const getModelFromChange = (change: ChangeBusAction) => {
  return isRemoveAction(change) ? undefined : change
}

export const accumulateModelStateAndValidity = <
  M extends EditorModel & Contextualized<EditorMappingContext>
>(
  model: M
) => {
  const modelP = accumulateModelState(model)
  const errorP = modelP.map(modelValid).not()
  return {
    modelP,
    errorP
  }
}

export const accumulateModelState = <
  M extends EditorModel & Contextualized<EditorMappingContext>
>(
  model: M
): Bacon.Bus<M, ChangeBusAction[]> => {
  const changeBus = Bacon.Bus<M, ChangeBusAction[]>()
  const validatedInitialModel = validateModel(addContext(model, { changeBus }))
  return changeBus.scan(validatedInitialModel, (m, changes) =>
    applyChangesAndValidate(m, changes)
  )
}

export const pushModelValue = <
  M extends EditorModel & Contextualized<ChangeBusContext>
>(
  model: M,
  value: any,
  path?: PathExpr
): void => {
  pushModel(modelSetValue(model, value, path))
}

export const pushModel = <
  M extends EditorModel & Contextualized<ChangeBusContext>
>(
  model: M,
  changeBus?: Bacon.Bus<M, ChangeBusAction[]>
) => getChangeBus(model, changeBus)?.push([model])

export const pushRemoval = <
  M extends EditorModel & Contextualized<ChangeBusContext>
>(
  model: M,
  changeBus?: Bacon.Bus<M, ChangeBusAction[]>
) => getChangeBus(model, changeBus)?.push([{ _remove: model }])

const modelPropertiesRaw = (mainModel: EditorModel): ObjectModelProperty[] =>
  (mainModel && isObjectModel(mainModel) ? mainModel.value?.properties : []) ||
  []

const modelItemsRaw = (model?: EditorModel): EditorModel[] =>
  (model && isListModel(model) && model.value) || []

const contextualizeProperty =
  <M extends ObjectModel & Contextualized<T>, T extends object>(mainModel: M) =>
  (
    property?: ObjectModelProperty
  ): ContextualizedObjectModelProperty<M, T> | undefined => {
    if (!property) return property
    const model = contextualizeChild(mainModel, property.model, property.key)!
    return R.mergeRight(property, {
      model,
      owner: mainModel,
      editable:
        property.editable === undefined ? mainModel.editable : property.editable
    }) as any as ContextualizedObjectModelProperty<M, T>
  }

let arrayKeyCounter = 0
export const ensureArrayKey = (v: ListModel) => {
  if (v && v.value && !v.arrayKey) {
    v.arrayKey = ++arrayKeyCounter
  }
  return v
}

const modelItemLens = <T, S>(index: number): L.Lens<T, S> => {
  const valueIndexLens = L.compose('value', indexL(index))
  const baseLens = L.lens(
    (m?: ListModel & OptionalModel & Contextualized): EditorModel => {
      if (m && isSomeOptionalModel(m) && m.optionalPrototype && !m.value) {
        // Array is missing -> create optional value using array prototype
        // @ts-expect-error
        const arrayPrototype = optionalPrototypeModel(m).arrayPrototype!
        return { optional: true, optionalPrototype: arrayPrototype } as any
      }
      if (m && m.value && index >= m.value.length && m.arrayPrototype) {
        if (index >= (m.minItems || 0)) {
          // Index out of bounds -> create optional value using array prototype
          return { optional: true, optionalPrototype: m.arrayPrototype } as any
        } else {
          // Index out of bounds within required number of items -> create required value using array prototype
          return m.arrayPrototype
        }
      }
      return ensureArrayKey(L.get(valueIndexLens, m)!)
    },
    (v: any, m: any) => {
      if (m && m.optional && !m.value && m.optionalPrototype) {
        const prototypeForArray = optionalPrototypeModel(m)
        return L.set(valueIndexLens, v, prototypeForArray)
      }
      if (m && (!v || !v.value)) {
        // remove value at index
        return L.set(valueIndexLens, undefined, m)
      }
      return L.set(valueIndexLens, v, m)
    }
  )
  return recontextualizingLens(baseLens, index)
}

const modelPropertyValueLens = (key: string) => {
  const propertyModelLens = L.compose(
    'value',
    'properties',
    L.find(R.whereEq({ key })),
    'model'
  )
  const baseLens = L.lens(
    (m?: EditorModel & OptionalModel & Contextualized) => {
      if (m && isSomeOptionalModel(m) && m.optionalPrototype && !hasValue(m)) {
        const proto = optionalPrototypeModel(m)
        const propertyProto = L.get(propertyModelLens, proto)
        return { optional: true, optionalPrototype: propertyProto }
      }
      return L.get(propertyModelLens, m)
    },
    (v: any, m: any) => {
      if (m && isSomeOptionalModel(m) && m.optionalPrototype && !hasValue(m)) {
        const proto = optionalPrototypeModel(
          m as PrototypeModel & OptionalModel & Contextualized
        )
        return L.set(propertyModelLens, v, proto)
      }
      return L.set(propertyModelLens, v, m)
    }
  )
  return recontextualizingLens(baseLens, key)
}

const recontextualizingLens = <T, S>(
  baseLens: L.Lens<T, S>,
  pathElem?: PathExpr
): L.Lens<T, S> => {
  return L.lens(
    (m: any) => contextualizeChild(m, L.get(baseLens, m), pathElem),
    (v: any, m: any) => L.set(baseLens, v, m)
  )
}

const contextualizeChild = <M extends EditorModel, T extends object>(
  parent?: Contextualized<T>,
  child?: M,
  pathElem?: PathExpr
): (M & Contextualized<T>) | undefined => {
  if (!child) return child
  if (!parent) {
    return throwError('parent missing')
  }
  return contextualizeSubModel(child, parent, pathElem)
}

const childPath = (
  model: Contextualized,
  ...pathElems: PathExpr[]
): DataPath => {
  if (!pathElems || pathElems[0] === undefined) return toPath(model.path)
  const basePath = toPath(model.path)
  const allPathElems = basePath.concat(pathElems)
  const path = L.compose(...allPathElems)
  return toPath(path)
}

export function resolvePrototypeReference<T extends object>(
  model: undefined,
  context?: T
): undefined
export function resolvePrototypeReference<
  M extends EditorModel,
  T extends object
>(model: M, context?: T): M & Contextualized<T>
export function resolvePrototypeReference<T extends object>(
  model?: EditorModel,
  context?: T
): EditorModel | undefined {
  if (model && isPrototypeModel(model)) {
    // Some models are delivered as prototype references and are replaced with the actual prototype found in the context
    if (!context)
      return throwError(
        `Cannot resolve prototype ${model.key} without context`,
        model
      )
    const foundProto = (context as any)?.prototypes[model.key]
    if (!foundProto) {
      console.error('Prototype not found: ' + model.key)
    }
    modelPropertiesRaw(foundProto).forEach(
      (p) => (p.model = resolvePrototypeReference(p.model, context)!)
    )
    const restructuredModel = model as EditorModel

    if (isListModel(restructuredModel) && restructuredModel.value) {
      for (const i in restructuredModel.value) {
        restructuredModel.value[i] = resolvePrototypeReference(
          restructuredModel.value[i],
          context
        )!
      }
    }

    // merge in properties, such as maxLines
    const { type: _type, key: _key, ...cleanedModel } = restructuredModel as any
    return R.mergeRight(foundProto, cleanedModel) as EditorModel
  }
  return model
}

export const resolveActualModel = <T extends object>(
  oneOfModel: EditorModel & OneOfModel & Contextualized,
  parentModel: EditorModel & Contextualized<T>
) => {
  const protoModels = oneOfModel.oneOfPrototypes
  const resolvedPrototypeReferences = protoModels.map((protos) =>
    resolvePrototypeReference(protos, parentModel.context)
  )
  const actualModels = resolvedPrototypeReferences
    .map((model) => ({ ...model, path: oneOfModel.path, parent: parentModel }))
    .filter((p) => {
      if (p.notWhen !== undefined) {
        // FIXME: Vertailussa ei voi käyttää spreadattua propertyä, koska modelLookup ja modelData eivät tällöin toimi
        return checkNotWhen(oneOfModel, p.notWhen)
      }
      return true
    })
    .filter((p) => {
      if (p.onlyWhen !== undefined) {
        // FIXME: Vertailussa ei voi käyttää spreadattua propertyä, koska modelLookup ja modelData eivät tällöin toimi.
        return checkOnlyWhen(oneOfModel, p.onlyWhen)
      }
      return true
    })

  const actualModelsWithAnnotations = actualModels.filter(
    (p) => p.onlyWhen !== undefined || p.notWhen !== undefined
  )

  if (actualModels.length > 1) {
    if (actualModelsWithAnnotations.length === 0) {
      console.warn(
        `Could not resolve actual editor model reliably, as there are no annotated candidates, and ${actualModels.length} candidates without annotations:`,
        actualModels
      )
    } else if (actualModelsWithAnnotations.length > 1) {
      console.warn(
        `Could not resolve actual editor model reliably, as there are ${actualModelsWithAnnotations.length} annotated candidates, and ${actualModels.length} candidates without annotations:`,
        actualModels,
        actualModelsWithAnnotations
      )
    }
  }

  if (actualModelsWithAnnotations.length > 1) {
    console.warn(
      `More than one annotated candidate resolved. Please consider making changes to the annotations to match only one case at a time:`,
      actualModelsWithAnnotations
    )
  }

  // Jos löydetään tasan yksi prototype, niin palautetaan se suoraan
  // Jos toisaalta löydetään edes yksi model, joka täsmää, palautetaan se.
  // Muuten palautetaan tyhjää.
  const actualModel =
    actualModelsWithAnnotations.length === 1
      ? actualModelsWithAnnotations[0]
      : actualModels.length > 0
        ? actualModels[0]
        : null
  return actualModel
    ? contextualizeModel(actualModel, parentModel.context)
    : oneOfModel
}

const removeUndefinedValues = <T extends object>(obj: T): T =>
  R.fromPairs(R.toPairs(obj).filter(([, v]) => v !== undefined)) as T

export const removeCommonPath = (
  deeperPath: DataPath,
  parentPath: DataPath
): DataPath => {
  if (parentPath.length == 0) return deeperPath
  return deeperPath.slice(parentPath.length)
}

const getValidator = (
  model: EditableModel & Contextualized<EditorMappingContext>,
  context: ContextData<EditorMappingContext>
) => {
  const editor = getEditor(model, context)
  return editor && editor.validateModel
}

const getEditor = <M extends EditableModel & Contextualized, T>(
  model: M,
  context?: ContextData<EditorMappingContext>
): EditorElement => {
  const ctx = (context || model.context) as ContextData<EditorMappingContext>
  const editorMapping = ctx?.editorMapping
  if (!editorMapping) {
    return throwError('editorMapping missing', model, ctx)
  }
  if (model.value && isObjectModel(model)) {
    for (const i in model.value.classes) {
      const editor = editorMapping[model.value.classes[i]]
      if (editor) {
        return editor
      }
    }
  }
  if (!editorMapping[model.type]) {
    // console.log('not found by type', model.type, model)
  }
  return editorMapping[model.type]
}

const getChangeBus = (
  model: EditorModel & Contextualized<ChangeBusContext>,
  changeBus?: ChangeBus
): ChangeBus | undefined => changeBus || model.context.changeBus

const valueEmpty = (value: any): boolean => {
  return !value
}

const itemsEmpty = <T>(items: T[]) => {
  return !items || !items.find((item) => !valueEmpty(item))
}

const lastL = L.lens(
  (xs?: any[]) => {
    return (xs && xs.length && R.last(xs)) || undefined
  },
  (x?: any, xs?: any[]) => (xs || []).slice(0, -1).concat([x])
)

const indexL = (index: number) => (index == -1 ? lastL : L.index(index))

const toPath = (path: any): DataPath => {
  if (path == undefined) {
    return []
  }
  if (typeof path === 'number') {
    path = '' + path
  }
  if (typeof path === 'string') {
    return path.split('.')
  }
  if (typeof path === 'function') {
    return [path]
  }
  if (path instanceof Array) {
    return path
  }
  throwError('Not a path: ' + path)
  return []
}

// removes function/lenses, leaving just the data path
const justPath = (path: PathExpr): DataPath =>
  toPath(path).filter((pathElem) => typeof pathElem !== 'function')

const objectLens = (path: any) => {
  const pathLenses = toPath(path).map((key) => {
    const index = parseInt(key)
    return Number.isNaN(index) ? L.prop(key) : indexL(index)
  })
  return L.compose<object, unknown>(...pathLenses)
}

export const checkOnlyWhen = (model: EditorModel, conditions?: OnlyWhen[]) => {
  if (!conditions) return true
  return conditions.some((onlyWhen) => {
    const data = modelData(model, onlyWhen.path.split('/'))
    const match = onlyWhen.value == data
    return match
  })
}

export const checkNotWhen = (model: EditorModel, conditions?: NotWhen[]) => {
  if (!conditions) return true
  return conditions.some((notWhen) => {
    const data = modelData(model, notWhen.path.split('/'))
    const match = Array.isArray(notWhen.values)
      ? !notWhen.values.includes(data)
      : notWhen.values != data
    return match
  })
}
