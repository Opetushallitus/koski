const lookupRecursive = (lookupStep, model, [head, ...tail]) => {
  let found = lookupStep(model, head)
  if (tail.length) {
    return lookupRecursive(lookupStep, found, tail)
  }
  return found
}

export const modelLookup = (mainModel, path) => {
  let lookupStep = (model, lookupKey) => {
    return model[lookupKey] ||
      (model.properties && model.properties.find(({key}) => key == lookupKey).model) ||
      (model.items && model.items[lookupKey]) ||
      (model.model && lookupStep(model.model, lookupKey))
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
  if (mainModel && path && mainModel.data) {
    return objectLookup(mainModel.data, path)
  } else {
    let model = path
      ? modelLookup(mainModel, path)
      : mainModel

    return model && (model.data || (model.value && model.value.data) || (model.model && modelData(model.model)))
  }
}