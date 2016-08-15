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
    return (xs && xs.length && xs[xs.length - 1]) || undefined
  },
  (x, xs) => xs.slice(0, -1).concat([x])
)

let indexL = (index) => index == -1 ? lastL : L.index(index)

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

export const modelLens = (path) => {
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
    return model && ((model.value && model.value.data))
  }
}

export const modelTitle = (mainModel, path) => {
  let model = modelLookup(mainModel, path)
  return (model && (model.title || (model.value && model.value.title) || (model.value && model.value.data))) || ''
}

export const modelEmpty = (model) => {
  return !model.value || valueEmpty(model.value) && itemsEmpty(modelItems(model.items))
}

export const modelSet = (mainModel, path, value) => {
  let dataLens = L.compose('value', 'data', objectLens(path))
  return L.set(dataLens, value.data, mainModel)
}

export const modelItems = (mainModel, path) => {
  let model = modelLookup(mainModel, path)
  return model.type == 'array' && model.value
}

const valueEmpty = (value) => {
  return !value
}

const itemsEmpty = (items) => {
  return !items || !items.find(item => !valueEmpty(item))
}