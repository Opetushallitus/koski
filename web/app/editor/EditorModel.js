import R from 'ramda'
import * as L from 'partial.lenses'

// Find submodel with given path
export const modelLookup = (mainModel, path) => {
  return L.get(modelLens(path), mainModel)
}

export const modelLens = (path) => {
  let pathLenses = toPath(path).map(key => {
    let index = parseInt(key)
    return L.compose('value', Number.isNaN(index)
      ? L.compose('properties', L.find(R.whereEq({key})), 'model')
      : indexL(index))
  })
  return L.compose(...pathLenses)
}

export const objectLookup = (mainObj, path) => {
  return L.get(objectLens(path), mainObj)
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

export const modelEmpty = (model) => {
  return !model.value || valueEmpty(model.value) && itemsEmpty(modelItems(model.items))
}

export const modelSet = (mainModel, newModel, path) => {
  return L.set(modelLens(path), newModel, mainModel)
}

export const modelSetData = (model, data) => {
  return modelSetValue(model, { data })
}

export const modelSetValue = (model, value) => {
  return R.merge(model, { value })
}

export const modelItems = (mainModel, path) => {
  let model = modelLookup(mainModel, path)
  return (model && model.type == 'array' && model.value) || []
}


// Add the given context to the model and all submodels. Submodels get a copy where their full path is included,
// so that modifications can be targeted to the correct position in the data that's to be sent to the server.
export const contextualizeModel = (model, context) => {
  let stuff = { context }
  if (model.value && model.value.properties) {
    stuff.value = R.merge(model.value, { properties : model.value.properties.map( p => R.merge(p, { model: contextualizeModel(p.model, childContext(context, p.key))})) })
  }
  if (model.type == 'array' && model.value) {
    stuff.value = model.value.map( (item, index) => {
      if (!item.arrayKey) {
        item.arrayKey = ++model.arrayKeyCounter || (model.arrayKeyCounter=1)
      }
      let itemModel = contextualizeModel(item, childContext(context, index))
      return itemModel
    })
  }
  return resolveModel(R.merge(model, stuff))
}

export const childContext = (context, ...pathElems) => {
  let path = ((context.path && [context.path]) || []).concat(pathElems).join('.')
  return R.merge(context, { path, root: false, arrayItems: null, parentContext: context })
}

// Add more context parameters to the current context of the model.
export const addContext = (model, additionalContext) => contextualizeModel(model, R.merge(model.context, additionalContext))

const valueEmpty = (value) => {
  return !value
}

const itemsEmpty = (items) => {
  return !items || !items.find(item => !valueEmpty(item))
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