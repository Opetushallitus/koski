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
    let findProperty = () => {
      let property = model.properties.find(({key}) => key == lookupKey)
      return property && property.model
    }
    return model[lookupKey] ||
      (model.properties && findProperty()) ||
      (model.items && model.items[lookupKey]) ||
      (model.items && model.items[model.items.length + parseInt(lookupKey)]) // for negative indices
  }

  return lookupRecursive(lookupStep, mainModel, path.split('.'))
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
  return !model.value || valueEmpty(model.value) && itemsEmpty(model.items)
}

const valueEmpty = (value) => {
  return !value
}

const itemsEmpty = (items) => {
  return !items || !items.find(item => !valueEmpty(item))
}