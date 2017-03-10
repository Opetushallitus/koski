import R from 'ramda'
import * as L from 'partial.lenses'

const lookupRecursive = (lookupStep, model, [head, ...tail]) => {
  let found = lookupStep(model, head)
  if (tail.length && found) {
    return lookupRecursive(lookupStep, found, tail)
  }
  return found
}

let lastL = L.lens(
  (xs) => {
    return (xs && xs.length && xs.last()) || undefined
  },
  (x, xs) => xs.slice(0, -1).concat([x])
)

let indexL = (index) => index == -1 ? lastL : L.index(index)

// Find submodel with given path
export const modelLookup = (mainModel, path) => {
  if (!path) return mainModel
  if (!mainModel) return
  let lens = modelLens(path)
  var subModel = prepareModel(mainModel, L.get(lens, mainModel), path)
  return subModel
}

const prepareModel = (mainModel, subModel, path) => {
  if (subModel && subModel.value && !subModel.value.data && mainModel.value && mainModel.value.data) {
    // fill in data from main model (data is not transmitted for all subModels, to save bandwidth)
    let data = objectLookup(mainModel.value.data, path)
    if (subModel.type == 'array' && data) {
      data.forEach((x, i) => subModel.value[i].value.data = x)
    } else {
      subModel.value.data = data
    }
  }
  return subModel
}

const modelLens = (path) => {
  let pathLenses = toPath(path).map(key => {
    let index = parseInt(key)
    return L.compose('value', Number.isNaN(index)
      ? L.compose('properties', L.find(R.whereEq({key})), 'model')
      : indexL(index))
  })
  return L.compose(...pathLenses)
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

const toPath = (path) => typeof path == 'string' ? path.split('.') : path

export const objectLookup = (mainObj, path) => {
  let lens = objectLens(path)
  return L.get(lens, mainObj)
}

export const modelData = (mainModel, path) => {
  if (mainModel && path && mainModel.value && mainModel.value.data) {
    return objectLookup(mainModel.value.data, path)
  } else {
    let model = modelLookup(mainModel, path)
    return model && valueData(model.value)
  }
}

export const modelTitle = (mainModel, path) => {
  let model = modelLookup(mainModel, path)
  return (model && (model.title || (model.value && model.value.title) || (model.value && '' + model.value.data))) || ''
}

export const modelEmpty = (model) => {
  return !model.value || valueEmpty(model.value) && itemsEmpty(modelItems(model.items))
}

const valueData = (value) => {
  if (!value) return
  if (value.data !== undefined) return value.data
  if (value instanceof Array) return value.map(modelData)
}

export const modelSet = (mainModel, path, value) => {
  let dataLens = L.compose('value', 'data', objectLens(path))
  if (value == undefined) {
    throw new Error('Trying to set ' + path + ' to undefined')
  }
  var data = valueData(value)
  return L.set(dataLens, data, mainModel)
}

export const modelItems = (mainModel, path) => {
  let model = modelLookup(mainModel, path)
  return model && model.type == 'array' && model.value
}

const valueEmpty = (value) => {
  return !value
}

const itemsEmpty = (items) => {
  return !items || !items.find(item => !valueEmpty(item))
}

// Add the given context to the model and all submodels. Submodels get a copy where their full path is included,
// so that modifications can be targeted to the correct position in the data that's to be sent to the server.
export const contextualizeModel = (model, context) => {
  let stuff = { context }
  if (model.value && model.value.properties) {
    stuff.value = R.merge(model.value, { properties : model.value.properties.map( p => R.merge(p, { model: contextualizeModel(p.model, childContext(context, p.key))})) })
  }
  if (model.type == 'array' && model.value) {
    stuff.value = model.value.map( (item, index) => contextualizeModel(item, childContext(context, index)))
  }
  return resolveModel(R.merge(model, stuff))
}

export const childContext = (context, ...pathElems) => {
  let path = ((context.path && [context.path]) || []).concat(pathElems).join('.')
  return R.merge(context, { path, root: false, arrayItems: null, parentContext: context })
}

const resolveModel = (model) => {
  if (model && model.type == 'prototype' && model.context.edit) {
    // Some models are delivered as prototype references and are replaced with the actual prototype found in the context
    let prototypeModel = model.context.prototypes[model.key]
    if (!prototypeModel) {
      console.error('Prototype not found: ' + model.key)
    }
    return contextualizeModel(prototypeModel, model.context)
  }
  return model
}

// Add more context parameters to the current context of the model.
export const addContext = (model, additionalContext) => contextualizeModel(model, R.merge(model.context, additionalContext))