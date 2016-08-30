const parseClassNames = (classNames) => typeof classNames == 'string'
  ? classNames.split(' ')
  : (classNames instanceof Array
      ? classNames
      : [])
export const buildClassNames = (classNames) => typeof classNames == 'string'
  ? classNames
  : classNames.join(' ')

export const hasClass = (x, name) => {
  if (isElement(x)) {
    return hasClass(x.className, name)
  } else {
    return parseClassNames(x).indexOf(name) >= 0
  }
}
export const addClass = (x, name) => {
  if (isElement(x)) {
    x.className = addClass(x.className, name)
  } else {
    return hasClass(x, name) ? x : x + ' ' + name
  }
}

export const removeClass = (x, name) => {
  if (isElement(x)) {
    x.className = removeClass(x.className, name)
  } else {
    return buildClassNames(parseClassNames(x).filter((n) => n != name))
  }
}
const isElement = (x) => x instanceof HTMLElement