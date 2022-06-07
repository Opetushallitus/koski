import * as R from 'ramda'
import Bacon from 'baconjs'
import * as L from 'partial.lenses'
import {hashAdd, hashCode} from './hashcode'
import {flatMapArray} from '../util/util'

// Find submodel with given path
export const modelLookupRequired = (mainModel, path) => {
  var model = modelLookup(mainModel, path)
  if (!model) {
    throwError('model for ' + path + ' not found', mainModel)
  }
  return model
}

export const modelLookup = (mainModel, path) => {
  return L.get(modelLens(path), mainModel)
}


export const lensedModel = (model, lens) => {
  let modelFromLens = L.get(lens, model)
  if (!modelFromLens) {
    throwError('lens returned ' + modelFromLens, model, lens)
  }
  return contextualizeSubModel(modelFromLens, model, lens)
}

export const modelLens = (path) => {
  var pathElems = toPath(path)
  let pathLenses = pathElems.map(key => L.compose(parseModelStep(key), manageModelIdLens))
  return L.compose(manageModelIdLens, ...pathLenses)
}

let parseModelStep = (key) => {
  if (key == '..') return modelParentLens
  if (!isNaN(key)) return modelItemLens(parseInt(key))
  if (typeof key == 'string') return modelPropertyValueLens(key)
  if (typeof key == 'function') return key // an actual lens then
  throwError('Unexpected model path element', key)
}

let modelParentLens = L.lens(
    m => findParent(m),
    (v, m) => throwError('Cannot set parent of model', m)
)
let findParent = (model) => {
  if (!model.parent) return undefined
  if (typeof R.last(model.path) == 'function') {
    // Skip lenses in path, only consider actual path elements
    return findParent(model.parent)
  }
  return model.parent
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
    id = 1 // to distinguish from a null value
    for (var i in m.value.properties) {
      id = hashAdd(id, getModelId(m.value.properties[i].model))
    }
  }
  if (m.type === 'array' && m.value) {
    id = 1 // to distinguish from a null value
    for (var i in m.value) {
      id = hashAdd(id, getModelId(m.value[i]))
    }
  }
  if (m.value && m.value.data !== undefined && m.value.data !== null) {
    id = hashAdd(id, hashCode(m.value.data))
  }
  return id
}

const manageModelIdLens = L.lens(
  (m) => {
    return ensureModelId(m)
  },
  (m1) => {
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
      return model.data = R.fromPairs(modelProperties(model).map(p => [p.key, modelData(p.model)]))
    } else if (model.value instanceof Array) {
      return model.data =  modelItems(model).map(item => modelData(item))
    } else {
      return model.data =  model.value.data
    }
  }
}

export const modelTitle = (mainModel, path = undefined, titleFormatter = undefined) => {
  let model = modelLookup(mainModel, path)
  if(model && titleFormatter !== undefined) {
    return titleFormatter(model)
  }
  return (model && (model.title || (model.value && model.value.title) || (model.value && '' + modelData(model)))) || ''
}

export const modelEmpty = (mainModel, path) => {
  let model = modelLookup(mainModel, path)
  if (!model.context) throwError('context missing')
  let editor = model.context && getEditor(model)
  if (editor && editor.isEmpty) {
    return editor.isEmpty(model)
  }
  return !model.value || valueEmpty(model.value) && itemsEmpty(modelItems(model.items))
}

export const recursivelyEmpty = (m) => {
  if (!m.value) return true
  if (m.optional) return false
  if (m.type == 'object') {
    if (!m.value.properties) return true
    for (var i in m.value.properties) {
      if (!recursivelyEmpty(m.value.properties[i].model)) return false
    }
    return true
  }
  if (m.type == 'array') {
    return m.value.length == 0
  }
  return !m.value.data
}


function withCatch(name, fn) {
  return function() {
    try {
      return fn.apply(null, arguments)
    } catch (e) {
      console.error('Error in ' + name, ...arguments, e)
      throw e
    }
  }
}

export const modelSet = withCatch('modelSet', (mainModel, newModel, path) => {
  return L.set(modelLens(path), newModel, mainModel)
})

export const modelSetData = (model, data, path) => {
  return modelSetValue(model, { data }, path)
}

export const modelSetTitle = (model, title, path) => {
  return modelSetValue(model, L.set('title', title, model.value), path)
}

export const modelSetValue = withCatch('modelSetValue', (model, value, path) => {
  return L.set(L.compose(modelLens(path), modelValueLens), value, model)
})

export const modelSetValues = (model, pathsAndValues) => {
  return R.reduce(
    (m, [path, value]) => modelSetValue(m, value, path),
    model,
    R.toPairs(pathsAndValues)
  )
}

export const modelValueLens = L.lens(
  (m) => {
    if (!m) {
      return undefined
    }
    return m.value
  },
  (v, m) => {
    if (!m) {
      if (v) {
        throwError('trying to set value of null model to a non-null value', v)
      } else {
        return m
      }
    }
    if (m.type == 'prototype') {
      if (v) {
        throwError(`trying to set value of an unresolved prototype model (${m.key}) to a non-null value`, v)
      }
    }
    let plainOptional = (m.optional && !m.type)
    let usedModel = plainOptional ? getUsedModelForOptionalModel(m) : m
    return L.set('value', v, usedModel)
  }
)

let throwError = (msg, ...args) => {
  console.error(msg, ...args)
  throw new Error(msg)
}

let getUsedModelForOptionalModel = (m, {model} = {}) => {
  if (!model) model = m
  if (m.value) return m
  if (!m.context) {
    m = contextualizeSubModel(m, model)
  }
  let prototypeModel = optionalPrototypeModel(m)
  let editor = undefined
  if (prototypeModel) {
    editor = getEditor(prototypeModel)
  } else {
    editor = getEditor(model)
  }
  let createEmpty = (editor && editor.createEmpty) || R.identity
  let emptyModel = createEmpty(prototypeModel || model)

  return emptyModel
}

export const wrapOptional = (model) => {
  if (!model) throw new Error('model missing. remember to wrap model like { model }')
  if (!model.optional) return model
  if (!model.context) throw new Error('cannot wrap without context')

  return lensedModel(model, optionalModelLens({model}))
}

export const optionalModelLens = ({model}) => {
  return L.lens(
    m => {
      return getUsedModelForOptionalModel(m, {model})
    },
    (newModel, contextModel) => {
      if (modelEmpty(newModel)) {
        return createOptionalEmpty(contextModel)
      } else {
        return modelSetValue(getUsedModelForOptionalModel(contextModel, {model}), newModel.value)
      }
    }
  )
}

const preparePrototypeModel = (prototypeModel, forModel) => {
  if (!prototypeModel) return prototypeModel
  if (prototypeModel.context) {
    return prototypeModel
  }

  let contextualizedProto = contextualizeSubModel(prototypeModel, forModel)
  contextualizedProto.parent = forModel.parent // parent fix

  let mergedModel = R.mergeRight(forModel, contextualizedProto) // includes all attributes from parent model (like maxLines etc that come from property annotations)
  return mergedModel
}

export const optionalPrototypeModel = (model) => {
  let prototype = model.optionalPrototype = preparePrototypeModel(model.optionalPrototype, model)
  if (!prototype) return prototype
  if (prototype.oneOfPrototypes && !modelData(prototype)) {
    // This is a OneOfModel, just pick the first alternative
    prototype = prototype.oneOfPrototypes[0] = preparePrototypeModel(prototype.oneOfPrototypes[0], model)
  }
  return R.mergeRight(prototype, createOptionalEmpty(model)) // Ensure that the prototype model has optional flag and optionalPrototype
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
    throwError('No properties found', mainModel)
  }
  var found = mainModel.value.properties.find(p => p.key == path[0])
  if (!found) {
    return undefined
  }
  return contextualizeProperty(mainModel)(found)
}

export const modelProperties = (mainModel, pathsOrFilter) => {
  if (pathsOrFilter && pathsOrFilter instanceof Array) {
    return flatMapArray(pathsOrFilter, p => {
      let prop = modelProperty(mainModel, p)
      return prop ? [prop] : []
    })
  }
  var props = modelPropertiesRaw(mainModel).map(contextualizeProperty(mainModel))
  if (pathsOrFilter && typeof pathsOrFilter == 'function') {
    props = props.filter(pathsOrFilter)
  }
  return props
}


export const oneOfPrototypes = (model) => {
  if (!model) return []
  if (model.oneOfPrototypes) {
    return model.oneOfPrototypes = model.oneOfPrototypes
      .map(proto => preparePrototypeModel(proto, model))
      .filter(m => checkOnlyWhen(m, m.onlyWhen))
  }
  return [model]
}

// Add the given context to the model and all submodels. Submodels get a copy where their full path is included,
// so that modifications can be targeted to the correct position in the data that's to be sent to the server.
export const contextualizeModel = (model, context, path) => {
  if (!context) {
    throwError('context missing')
  }
  model = resolvePrototype(model, context)
  return R.mergeRight(model, { context, path: childPath(model, path) })
}

// TODO: don't call this for arrayPrototype. Add arrayPrototype accessor instead
export const contextualizeSubModel = (subModel, parentModel, path) => {
  if (!subModel) return subModel
  subModel = resolvePrototype(subModel, parentModel.context)
  var subPath = childPath(parentModel, path)
  return R.mergeRight(subModel, { context: parentModel.context, path: subPath, parent: parentModel })
}

// Add more context parameters to the current context of the model.
export const addContext = (model, additionalContext) => {
  additionalContext = removeUndefinedValues(additionalContext)
  if (!model.context) return contextualizeModel(model, additionalContext)
  return contextualizeModel(model, R.mergeRight(model.context, additionalContext))
}

export const modelValid = (model, recursive = true) => {
  let errors = modelErrors(model, recursive)
  let valid = R.keys(errors).length == 0
  //if (!valid) console.log("errors", errors)
  return valid
}

export const modelErrorMessages = (model, recursive = true) => {
  return R.uniq(R.unnest(R.values(modelErrors(model, recursive)))
    .filter(e => e.message)
    .map(e => e.message))
}

const modelErrors = (model, recursive = true) => {
  let context = model.context
  let pathString = justPath(model.path).join('.')
  let keyMatch = ([key]) => recursive ? pathString === key || R.startsWith(pathString + '.', key) : pathString === key
  let validationResult = context && context.validationResult || {}

  return pathString.length
    ? R.fromPairs(R.toPairs(validationResult).filter(keyMatch))
    : validationResult
}


export const applyChangesAndValidate = (modelBeforeChange, changes) => {
  let basePath = toPath(modelBeforeChange.path)
  var withAppliedChanges = changes.reduce((acc, change) => {
    //console.log('apply', change, 'to', acc)

    let subPath = removeCommonPath(toPath(getPathFromChange(change)), basePath)
    let actualLens = modelLens(subPath)

    return L.set(actualLens, getModelFromChange(change), acc)
  }, modelBeforeChange)


  return validateModel(withAppliedChanges)
}

// adds validationResult to model.context
export const validateModel = (mainModel) => {
  let context = mainModel.context
  if (!context) throwError('context missing')
  const pushError = (model, results) => error => {
    let path = justPath(model.path)
    let fullPath = path.concat(error.path || []).join('.')
    results[fullPath] ? results[fullPath].push(error) : results[fullPath] = [error]
  }
  const validateInner = (model, results) => {
    let validator = getValidator(model, context)
    if (validator) {
      let myResult = validator(model)
      if (myResult) {
        myResult.forEach(pushError(model, results))
      }
    }
    modelProperties(model).forEach(p => {
      validateInner(p.model, results)
    })
    modelItems(model).forEach(item => validateInner(item, results))
    return results
  }
  let validationResult = validateInner(mainModel, {})
  return addContext(mainModel, { validationResult })
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

export const accumulateModelState = (model) => {
  let changeBus = Bacon.Bus()
  let validatedInitialModel = validateModel(addContext(model, {changeBus}))
  return changeBus.scan(validatedInitialModel, (m, changes) => applyChangesAndValidate(m, changes))
}

export const pushModelValue = (model, value, path) => pushModel(modelSetValue(model, value, path))
export const pushModel = (model, changeBus) => getChangeBus(model, changeBus).push([model])
export const pushRemoval = (model, changeBus) => getChangeBus(model, changeBus).push([{_remove: model}])

const modelPropertiesRaw = (mainModel) => ((mainModel && mainModel.value && mainModel.value.properties) || [])
const modelItemsRaw = (model) => ((model && model.type == 'array' && model.value) || [])

let contextualizeProperty = (mainModel) => (property) => {
  if (!property) return property
  let model = contextualizeChild(mainModel, property.model, property.key)
  return R.mergeRight(property, { model, owner: mainModel })
}

let arrayKeyCounter = 0
export const ensureArrayKey = (v) => {
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
    throwError('parent missing')
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
    if (!context) throwError(`Cannot resolve prototype ${model.key} without context`, model)
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
    // merge in properties, such as maxLines
    foundProto = R.mergeRight(foundProto, R.dissoc('type', R.dissoc('key', model)))
    return foundProto
  }
  return model
}

const removeUndefinedValues = (obj) => R.fromPairs(R.toPairs(obj).filter(([, v]) => v !== undefined))

export const removeCommonPath = (deeperPath, parentPath) => {
  if (parentPath.length == 0) return deeperPath
  return deeperPath.slice(parentPath.length)
}

const getValidator = (model, context) => {
  let editor = getEditor(model, context)
  return editor && editor.validateModel
}

const getEditor = (model, context) => {
  context = context || model.context
  let editorMapping = context.editorMapping
  if (!editorMapping) {
    throwError('editorMapping missing', model, context)
  }
  if (model.value && model.value.classes) {
    for (var i in model.value.classes) {
      let editor = editorMapping[model.value.classes[i]]
      if (editor) {
        return editor
      }
    }
  }
  if (!editorMapping[model.type]) {
    //console.log('not found by type', model.type, model)
  }
  return editorMapping[model.type]
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
    return (xs && xs.length && R.last(xs)) || undefined
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
  throwError('Not a path: ' + path)
}

// removes function/lenses, leaving just the data path
const justPath = path => toPath(path).filter(pathElem => typeof pathElem != 'function')

const objectLens = (path) => {
  let pathLenses = toPath(path).map(key => {
    let index = parseInt(key)
    return Number.isNaN(index)
      ? L.prop(key)
      : indexL(index)
  })
  return L.compose(...pathLenses)
}

export const checkOnlyWhen = (model, conditions) => {
  if (!conditions) return true
  return conditions.some(onlyWhen => {
    let data = modelData(model, onlyWhen.path.split('/'))
    let match = onlyWhen.value == data
    /*if (!match) {
      console.log('onlyWhen mismatch at ' + justPath(model.path) + ". Condition " + onlyWhen.path + "=" + onlyWhen.value, '. Value at path=' + data)
    }*/
    return match
  })
}
