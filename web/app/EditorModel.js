import R from 'ramda'

const lookupRecursive = (lookupStep, model, [head, ...tail]) => {
  let found = lookupStep(model, head)
  if (tail.length && found) {
    return lookupRecursive(lookupStep, found, tail)
  }
  return found
}

export const modelLookup = (mainModel, path) => {
  if (!path) return mainModel
  let lookupStep = (model, lookupKey) => {
    return model[lookupKey] || (propertyLookup(model, lookupKey)) || itemsLookup(model, lookupKey)
  }
  return lookupRecursive(lookupStep, mainModel, path.split('.'))
}

export const modelSet = (mainModel, path, value) => {
  let clone = R.clone(mainModel) // TODO: this is extremely slow
  let subModel = modelLookup(clone, path)
  subModel.value = value
  return clone
}

const itemsLookup = (model, lookupKey) => {
  let items = modelItems(model)
  return items
    ? items[lookupKey] || items[items.length + parseInt(lookupKey)] // for negative indices
    : null
}

let propertyLookup = (model, lookupKey) => {
  let properties = model.value && model.value.properties || []
  let property = properties.find(({key}) => key == lookupKey)
  return property && property.model
}

const objectLookup = (mainObj, path) => {
  let lookupStep = (obj, lookupKey) => {
    return obj[lookupKey]
  }
  return lookupRecursive(lookupStep, mainObj, path.split('.'))
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

const modelItems = (model) => model.type == 'array' && model.value

const valueEmpty = (value) => {
  return !value
}

const itemsEmpty = (items) => {
  return !items || !items.find(item => !valueEmpty(item))
}