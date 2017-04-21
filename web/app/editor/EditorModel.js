import R from 'ramda'
import Bacon from 'baconjs'
import * as L from 'partial.lenses'
import {hashCode, hashAdd} from './hashcode'

// Find submodel with given path
export const modelLookupRequired = (mainModel, path) => {
  var model = modelLookup(mainModel, path)
  if (!model) {
    console.error('model for', path, 'not found from', mainModel)
    throw new Error('model for ' + path + ' not found')
  }
  return model
}

export const modelLookup = (mainModel, path) => {
  return L.get(modelLens(path), mainModel)
}


export const lensedModel = (model, lens) => {
  let modelFromLens = L.get(lens, model)
  if (!modelFromLens) {
    throw new Error('lens returned ' + modelFromLens)
  }
  return contextualizeSubModel(modelFromLens, model, lens)
}

export const modelLens = (path) => {
  var pathElems = toPath(path)
  let pathLenses = pathElems.map(key => {
    let numeric = !isNaN(key)
    let string = typeof key == 'string'

    var l1 = numeric
      ? modelItemLens(parseInt(key))
      : string
        ? modelPropertyValueLens(key)
        : key // an actual lens TODO might be dangerous way to recognize this

    return L.compose(l1, manageModelIdLens)
  })
  return L.compose(manageModelIdLens, ...pathLenses)
}

let ensureModelId = (model, force) => {
  if (model && (force || !model.modelId)) {
    model.modelId = calculateModelId(model)
    model.data = null // reset modelData caching
  }
  return model
}

let getModelId = (model) => {
  return ensureModelId(model).modelId
}

let calculateModelId = (m) => {
  let id = 0
  if (m.value && m.value.properties) {
    for (var i in m.value.properties) {
      id = hashAdd(id, getModelId(m.value.properties[i].model))
    }
  }
  if (m.type === 'array' && m.value) {
    for (var i in m.value) {
      id = hashAdd(id, getModelId(m.value[i]))
    }
  }
  if (m.value && m.value.data) {
    id = hashAdd(id, hashCode(m.value.data))
  }
  return id
}

const manageModelIdLens = L.lens(
  (m) => {
    return ensureModelId(m)
  },
  (m1, ) => {
    return ensureModelId(m1, true) // forces calculation model id on the result
  }
)

export const modelData = (mainModel, path) => {
  if (!mainModel || !mainModel.value) return
  if (mainModel.value.data) {
    if (!path) return mainModel.value.data
    return L.get(objectLens(path), mainModel.value.data)
  }

  let head = toPath(path).slice(0, 1)
  if (head.length) {
    let model = modelLookup(mainModel, head)
    let tail = toPath(path).slice(1)
    return modelData(model, tail)
  } else {
    let model = mainModel
    if (model.data) return model.data
    if (!model || !model.value) return
    if (model.value.properties) {
      return model.data = R.fromPairs(model.value.properties.map(p => [p.key, modelData(p.model)]))
    } else if (model.value instanceof Array) {
      return model.data =  model.value.map(item => modelData(item))
    } else {
      return model.data =  model.value.data
    }
  }
}

export const modelTitle = (mainModel, path) => {
  let model = modelLookup(mainModel, path)
  return (model && (model.title || (model.value && model.value.title) || (model.value && '' + modelData(model)))) || ''
}

export const modelEmpty = (mainModel, path) => {
  let model = modelLookup(mainModel, path)
  return !model.value || valueEmpty(model.value) && itemsEmpty(modelItems(model.items))
}

export const modelSet = (mainModel, newModel, path) => {
  return L.set(modelLens(path), newModel, mainModel)
}

export const modelSetData = (model, data, path) => {
  return modelSetValue(model, { data }, path)
}

export const modelSetValue = (model, value, path) => {
  return L.set(L.compose(modelLens(path), modelValueLens()), value, model)
}

let modelValueLens = ({model, createEmpty} = {}) => L.lens(
  (m) => {
    if (!m) {
      throw new Error('model missing')
    }
    return m.value
  },
  (v, m) => {
    let usedModel = m.optional ? getUsedModelForOptionalModel(m, {model, createEmpty}) : m
    return L.set('value', v, usedModel)
  }
)

let getUsedModelForOptionalModel = (m, {model, createEmpty = (x => x)} = {}) => {
  if (!model) model = m
  if (m.value) return m
  if (!m.context) {
    m = contextualizeSubModel(m, model)
  }
  return createEmpty(optionalPrototypeModel(m))
}
const modelEmptyForOptional = (m) => {
  if (!m.value) return true
  if (m.type == 'object') {
    if (!m.value.properties) return true
    for (var i in m.value.properties) {
      if (!modelEmptyForOptional(m.value.properties[i].model)) return false
    }
    return true
  }
  if (m.type == 'array') {
    return m.value.length == 0
  }
  return !m.value.data
}

export const optionalModelLens = ({model, isEmpty = modelEmptyForOptional, createEmpty}) => {
  return L.lens(
    m => {
      return getUsedModelForOptionalModel(m, {model, isEmpty, createEmpty}) // why not just m?
    },
    (newModel, contextModel) => {
      if (isEmpty(newModel)) {
        //console.log('set empty', newModel)
        return createOptionalEmpty(contextModel)
      } else {
        //console.log('set non-empty', newModel)
        return modelSetValue(getUsedModelForOptionalModel(contextModel, {model, isEmpty, createEmpty}), newModel.value)
      }
    }
  )
}

export const optionalPrototypeModel = (model) => {
  let prototype = model.optionalPrototype && contextualizeSubModel(model.optionalPrototype, model)
  if (!prototype) return prototype
  if (prototype.oneOfPrototypes && !modelData(prototype)) {
    // This is a OneOfModel, just pick the first alternative for now. TODO: allow picking suitable prototype
    prototype = contextualizeSubModel(prototype.oneOfPrototypes[0], model)
  }
  return R.merge(prototype, createOptionalEmpty(model)) // Ensure that the prototype model has optional flag and optionalPrototype
}

export const createOptionalEmpty = (optModel) => ({ optional: optModel.optional, optionalPrototype: optModel.optionalPrototype })
export const resetOptionalModel = (model) => pushModel(contextualizeChild(model, createOptionalEmpty(model)))

export const modelItems = (mainModel, path) => {
  let model = modelLookup(mainModel, path)
  var items = modelItemsRaw(model)
  return items.map((item, index) => {
    return modelLookup(model, index)
  })
}

export const hasModelProperty = (mainModel, key) => {
  return !!findModelProperty(mainModel, p => p.key == key)
}

export const findModelProperty = (mainModel, filter) => {
  if (!mainModel.value) return undefined
  var found = mainModel.value.properties.find(filter)
  return contextualizeProperty(mainModel)(found)
}

export const modelProperty = (mainModel, path) => {
  path = toPath(path)
  if (path.length > 1) {
    return modelProperty(modelLookup(mainModel, path.slice(0, -1)), path.slice(-1))
  }
  if (!mainModel.value || !mainModel.value.properties) {
    throw new Error('No properties found')
  }
  var found = mainModel.value.properties.find(p => p.key == path[0])
  if (!found) {
    throw new Error('Property ' + path[0] + ' not found')
  }
  return contextualizeProperty(mainModel)(found)
}

export const modelProperties = (mainModel, pathsOrFilter) => {
  if (pathsOrFilter && pathsOrFilter instanceof Array) {
    return pathsOrFilter.map(p => modelProperty(mainModel, p))
  }
  var props = modelPropertiesRaw(mainModel)
  if (pathsOrFilter && typeof pathsOrFilter == 'function') {
    props = props.filter(pathsOrFilter)
  }
  return props.map(contextualizeProperty(mainModel))
}


export const oneOfPrototypes = (model) => {
  return model.oneOfPrototypes
    ? model.oneOfPrototypes.map(proto => contextualizeSubModel(proto, model))
    : [model]
}

// Add the given context to the model and all submodels. Submodels get a copy where their full path is included,
// so that modifications can be targeted to the correct position in the data that's to be sent to the server.
export const contextualizeModel = (model, context, path) => {
  if (!context) {
    throw new Error('context missing')
  }
  model = resolvePrototype(model, context)
  return R.merge(model, { context, path: childPath(model, path) })
}

export const contextualizeSubModel = (subModel, parentModel, path) => {
  subModel = resolvePrototype(subModel, parentModel.context)
  var subPath = childPath(parentModel, path)
  return R.merge(subModel, { context: parentModel.context, path: subPath })
}

// Add more context parameters to the current context of the model.
export const addContext = (model, additionalContext) => {
  if (!model.context) throw new Error('context missing')
  return contextualizeModel(model, R.merge(model.context, removeUndefinedValues(additionalContext)))
}

export const modelValid = (model, context) => {
  var errors = validateModel(model, context)
  let valid = R.keys(errors).length == 0
  //if (!valid) console.log("errors", errors)
  return valid
}

export const modelErrorMessages = (model, context) => {
  var errors = validateModel(model, context)
  return R.values(errors).flatten().filter(e => e.message).map(e => e.message)
}

export const applyChanges = (modelBeforeChange, changes) => {
  if (!modelBeforeChange.context) {
    modelBeforeChange.context = { prototypes: modelBeforeChange.prototypes }
  }
  let basePath = toPath(modelBeforeChange.path)
  var withAppliedChanges = changes.reduce((acc, change) => {
    //console.log('apply', change, 'to', acc)

    let subPath = removeCommonPath(toPath(getPathFromChange(change)), basePath)
    let actualLens = modelLens(subPath)

    return L.set(actualLens, getModelFromChange(change), acc)
  }, modelBeforeChange)
  return withAppliedChanges
}

export const getPathFromChange = (change) => {
  let modelForPath = change._remove ? change._remove : change
  return modelForPath.path
}

export const getModelFromChange = (change) => {
  return change._remove ? undefined : change
}

export const accumulateModelStateAndValidity = (model) => {
  let modelP = accumulateModelState(model)
  let errorP = modelP.map(modelValid).not()
  return {
    modelP,
    errorP
  }
}

export const accumulateModelState = (model, changeBus = Bacon.Bus()) => {
  return changeBus.scan(addContext(model, {changeBus}), (m, changes) => applyChanges(m, changes))
}


export const pushModelValue = (model, value, path) => pushModel(modelSetValue(model, value, path))
export const pushModel = (model, changeBus) => getChangeBus(model, changeBus).push([model])
export const pushRemoval = (model, changeBus) => getChangeBus(model, changeBus).push([{_remove: model}])

const modelPropertiesRaw = (mainModel) => ((mainModel.value && mainModel.value.properties) || [])
const modelItemsRaw = (model) => ((model && model.type == 'array' && model.value) || [])

let contextualizeProperty = (mainModel) => (property) => {
  if (!property) return property
  let model = contextualizeChild(mainModel, property.model, property.key)
  return R.merge(property, { model })
}

let arrayKeyCounter = 0
let ensureArrayKey = (v) => {
  if (v && v.value && !v.arrayKey) {
    v.arrayKey = ++arrayKeyCounter
  }
  return v
}
let modelItemLens = (index) => {
  let valueIndexLens = L.compose('value', indexL(index))
  let baseLens = L.lens(
    (m) => {
      if (m && m.optional && !m.value && m.optionalPrototype) {
        // Array is missing -> create optional value using array prototype
        var arrayPrototype = optionalPrototypeModel(m).arrayPrototype
        return { optional: true, optionalPrototype: arrayPrototype }
      }
      if (m && m.value && index >= m.value.length && m.arrayPrototype) {
        if (index >= (m.minItems || 0)) {
          // Index out of bounds -> create optional value using array prototype
          return { optional: true, optionalPrototype: m.arrayPrototype}
        } else {
          // Index out of bounds within required number of items -> create required value using array prototype
          return m.arrayPrototype
        }
      }
      return ensureArrayKey(L.get(valueIndexLens, m))
    },
    (v, m) => {
      if (m && m.optional && !m.value && m.optionalPrototype) {
        let prototypeForArray = optionalPrototypeModel(m)
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

let modelPropertyValueLens = (key) => {
  let propertyModelLens = L.compose('value', 'properties', L.find(R.whereEq({key})), 'model')
  let baseLens = L.lens(
    (m) => {
      if (m && m.optional && !m.value && m.optionalPrototype) {
        let proto = optionalPrototypeModel(m)
        var propertyProto = L.get(propertyModelLens, proto)
        return { optional: true, optionalPrototype: propertyProto}
      }
      return L.get(propertyModelLens, m)
    },
    (v, m) => {
      if (m && m.optional && !m.value && m.optionalPrototype) {
        let proto = optionalPrototypeModel(m)
        return L.set(propertyModelLens, v, proto)
      }
      return L.set(propertyModelLens, v, m)
    }
  )
  return recontextualizingLens(baseLens, key)
}

let recontextualizingLens = (baseLens, pathElem) => {
  return L.lens(
    (m) => contextualizeChild(m, L.get(baseLens, m), pathElem),
    (v, m) => L.set(baseLens, v, m)
  )
}

let contextualizeChild = (m, child, pathElem) => {
  if (!child) return child
  if (!m) {
    throw new Error('parent missing')
  }
  return contextualizeSubModel(child, m, pathElem)
}

const childPath = (model, ...pathElems) => {
  if (!pathElems || pathElems[0] === undefined) return toPath(model.path)
  let basePath = toPath(model.path)
  let allPathElems = (basePath).concat(pathElems)
  let path = L.compose(...allPathElems)
  return toPath(path)
}

const resolvePrototype = (model, context) => {
  if (model && model.type === 'prototype') {
    // Some models are delivered as prototype references and are replaced with the actual prototype found in the context
    let foundProto = context.prototypes[model.key]
    if (!foundProto) {
      console.error('Prototype not found: ' + model.key)
    }
    modelPropertiesRaw(foundProto).forEach(p => p.model = resolvePrototype(p.model, context))
    if (model.type === 'array' && model.value) {
      for (var i in model.value) {
        model.value[i] = resolvePrototype(model.value[i], context)
      }
    }
    return foundProto
  }
  return model
}

const removeUndefinedValues = (obj) => R.fromPairs(R.toPairs(obj).filter(([, v]) => v !== undefined))

const removeCommonPath = (p1, p2) => {
  if (p2.length == 0) return p1
  return p1.slice(p2.length)
}

const getValidator = (model, context) => {
  let validatorMapping = context.editorMapping
  let editor = validatorMapping[model.type]
  if (model.value && model.value.classes) {
    for (var i in model.value.classes) {
      editor = validatorMapping[model.value.classes[i]]
      if (editor) break
    }
  }
  return editor && editor.validateModel
}
const validateModel = (model, context, results = {}, path = []) => {
  if (!context) context = model.context
  if (!context) throw new Error('context missing')

  let validator = getValidator(model, context)
  if (validator) {
    let myResult = validator(model)
    if (myResult && myResult.length) {
      results[path.join('.')] = myResult
    }
  }
  modelPropertiesRaw(model).forEach(p => validateModel(p.model, context, results, path.concat(p.key)))
  modelItemsRaw(model).forEach((item, i) => validateModel(item, context, results, path.concat(i)))
  return results
}

const getChangeBus = (model, changeBus) => changeBus || model.context.changeBus

const valueEmpty = (value) => {
  return !value
}

const itemsEmpty = (items) => {
  return !items || !items.find(item => !valueEmpty(item))
}

let lastL = L.lens(
  (xs) => {
    return (xs && xs.length && xs.last()) || undefined
  },
  (x, xs) => xs.slice(0, -1).concat([x])
)

let indexL = (index) => index == -1 ? lastL : L.index(index)

const toPath = (path) => {
  if (path == undefined) {
    return []
  }
  if (typeof path == 'number') {
    path = '' + path
  }
  if (typeof path == 'string') {
    return path.split('.')
  }
  if (typeof path == 'function') {
    return [path]
  }
  if (path instanceof Array) {
    return path
  }
  throw new Error('Not a path: ' + path)
}

const objectLens = (path) => {

  let pathLenses = toPath(path).map(key => {
    let index = parseInt(key)
    return Number.isNaN(index)
      ? L.prop(key)
      : indexL(index)
  })
  return L.compose(...pathLenses)
}