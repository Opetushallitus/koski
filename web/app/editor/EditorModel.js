import R from 'ramda'
import Bacon from 'baconjs'
import * as L from 'partial.lenses'

// Find submodel with given path
export const modelLookup = (mainModel, path) => {
  return L.get(modelLens(path), mainModel)
}


let modelItemLens = (index) => {
  let baseLens = L.compose('value', indexL(index))
  return recontextualizingLens(baseLens, index)
}

let modelPropertyValueLens = (key) => {
  let baseLens = L.compose('value', 'properties', L.find(R.whereEq({key})), 'model')
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

    return l1
  })
  return L.compose(...pathLenses)
}

export const modelData = (mainModel, path) => {
  if (!mainModel || !mainModel.value) return
  if (mainModel.value.data) {
    return L.get(objectLens(path), mainModel.value.data)
  }

  let head = toPath(path).slice(0, 1)
  if (head.length) {
    let model = modelLookup(mainModel, head)
    let tail = toPath(path).slice(1)
    return modelData(model, tail)
  } else {
    let model = mainModel
    if (!model || !model.value) return
    if (model.value.properties) {
      return R.fromPairs(model.value.properties.map(p => [p.key, modelData(p.model)]))
    } else if (model.value instanceof Array) {
      return model.value.map(item => modelData(item))
    } else {
      return model.value.data
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

export const modelSetData = (model, data) => {
  return modelSetValue(model, { data })
}

export const modelSetValue = (model, value, path) => {
  return L.set(L.compose(modelLens(path), 'value'), value, model)
}

export const modelItems = (mainModel, path) => {
  let model = modelLookup(mainModel, path)
  var items = modelItemsRaw(model)
  return items.map((item, index) => {
    return contextualizeChild(model, item, index)
  })
}

const modelItemsRaw = (model) => ((model && model.type == 'array' && model.value) || [])

export const findModelProperty = (mainModel, filter) => {
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

const modelPropertiesRaw = (mainModel) => ((mainModel.value && mainModel.value.properties) || [])

let contextualizeProperty = (mainModel) => (property) => {
  if (!property) return property
  let model = contextualizeChild(mainModel, property.model, property.key)
  return R.merge(property, { model })
}

export const contextualizeSubModel = (subModel, parentModel, path) => {
  incCounter('contextualizeSubModel')
  subModel = resolvePrototype(subModel, parentModel.context)
  var subPath = childPath(parentModel, path)
  return R.merge(subModel, { context: parentModel.context, path: subPath })
}

// Add the given context to the model and all submodels. Submodels get a copy where their full path is included,
// so that modifications can be targeted to the correct position in the data that's to be sent to the server.
export const contextualizeModel = (model, context, path) => {
  incCounter('contextualize')
  if (!context) {
    throw new Error('context missing')
  }

  model = resolvePrototype(model, context)

  return R.merge(model, { context, path: childPath(model, path) })
}

export const childPath = (model, ...pathElems) => {
  if (!pathElems || pathElems[0] === undefined) return toPath(model.path)
  let basePath = toPath(model.path)
  let allPathElems = (basePath).concat(pathElems)
  let path = L.compose(...allPathElems)
  return toPath(path)
}

const resolvePrototype = (model, context) => {
  if (model && model.type === 'prototype') {
    if (!context.edit) {
      return model
    }
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

// Add more context parameters to the current context of the model.
export const addContext = (model, additionalContext) => {
  incCounter('addContext')
  if (!model.context) throw new Error('context missing')
  return contextualizeModel(model, R.merge(model.context, removeUndefinedValues(additionalContext)))
}

export const applyChanges = (modelBeforeChange, changes) => {
  let basePath = toPath(modelBeforeChange.path)
  var withAppliedChanges = R.splitEvery(2, changes).reduce((acc, [, model]) => {
    //console.log('apply', model, 'to', context.path)
    let modelForPath = model._remove ? model._remove : model
    let modelForValue = model._remove ? undefined : model

    let subPath = removeCommonPath(toPath(modelForPath.path), basePath)
    let actualLens = modelLens(subPath)

    return L.set(actualLens, modelForValue, acc)
  }, modelBeforeChange)
  return withAppliedChanges
}

const removeCommonPath = (p1, p2) => {
  if (p2.length == 0) return p1
  return p1.slice(p2.length)
}

export const accumulateModelState = (model) => {
  let changeBus = Bacon.Bus()
  let modelP = changeBus.scan(addContext(model, {changeBus}), (m, changes) => applyChanges(m, changes))
  let errorP = modelP.map(modelValid).not()
  return {
    modelP,
    errorP
  }
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

export const modelValid = (model, context) => {
  if (!context) {
    if (!model.context) throw new Error('context missing')
    context = model.context
  }
  var errors = validateModel(model, context)
  let valid = R.keys(errors).length == 0
  //if (!valid) console.log("errors", errors)
  return valid
}


window.counters = {}
const incCounter = (key) => window.counters[key] = (window.counters[key] || 0) + 1
window.resetCounters = () => window.counters = {}

export const lensedModel = (model, lens) => {
  let modelFromLens = L.get(lens, model)
  if (!modelFromLens) {
    throw new Error('lens returned ' + modelFromLens)
  }
  return contextualizeSubModel(modelFromLens, model, lens)
}

export const pushModelValue = (model, value, path) => model.context.changeBus.push([model.context, modelSetValue(model, value, path)])

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