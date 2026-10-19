// Baret 1.2.0 (https://github.com/calmm-js/baret), MIT-lisenssi.

import React from 'react'
import Bacon, { Observable } from 'baconjs'
import {
  array0,
  assocPartialU,
  dissocPartialU,
  inherit,
  isArray,
  isString,
  object0
} from 'infestines'

//

const STYLE = 'style'
const CHILDREN = 'children'
const LIFT = 'baret-lift'
const DD_REF = '$$ref'

//

const reactElement = React.createElement
const Component = React.Component

const isObs = (x) => x instanceof Observable
const bacon2 = typeof Bacon.Next().value !== 'function'
const eventValue = bacon2 ? (e) => e.value : (e) => e.value()
const hasValue = bacon2 ? (e) => e.hasValue : (e) => e.hasValue()
const isError = bacon2 ? (e) => e.isError : (e) => e.isError()
const isEnd = bacon2 ? (e) => e.isEnd : (e) => e.isEnd()
//

const LiftedComponent = /*#__PURE__*/ inherit(
  // eslint-disable-next-line @typescript-eslint/no-shadow
  function LiftedComponent(props) {
    Component.call(this, props)
  },
  Component,
  {
    componentWillReceiveProps(nextProps) {
      this.componentWillUnmount()
      this.doSubscribe(nextProps)
    },
    componentWillMount() {
      this.doSubscribe(this.props)
    }
  }
)

//

const FromBacon = /*#__PURE__*/ inherit(
  // eslint-disable-next-line @typescript-eslint/no-shadow
  function FromBacon(props) {
    LiftedComponent.call(this, props)
    this.callback = null
    this.rendered = null
  },
  LiftedComponent,
  {
    componentWillUnmount() {
      if (this.unsub) this.unsub()
    },
    doSubscribe({ observable }) {
      if (isObs(observable)) {
        const callback = (e) => {
          if (hasValue(e)) {
            this.rendered = eventValue(e) || null
            this.forceUpdate()
          } else if (isError(e)) {
            throw e.error
          } else if (isEnd(e)) {
            this.unsub = null
          }
        }
        this.unsub = observable.subscribe(callback)
      } else {
        this.rendered = observable || null
      }
    },
    render() {
      return this.rendered
    }
  }
)

export const fromBacon = (observable) => reactElement(FromBacon, { observable })

//

function renderChildren(children, at, values) {
  if (isObs(children)) {
    return values[++at[0]]
  } else if (isArray(children)) {
    let newChildren = children
    for (let i = 0, n = children.length; i < n; ++i) {
      const childI = children[i]
      let newChildI = childI
      if (isObs(childI)) {
        newChildI = values[++at[0]]
      } else if (isArray(childI)) {
        newChildI = renderChildren(childI, at, values)
      }
      if (newChildI !== childI) {
        if (newChildren === children) newChildren = children.slice(0)
        newChildren[i] = newChildI
      }
    }
    return newChildren
  } else {
    return children
  }
}

function renderStyle(style, at, values) {
  let newStyle = undefined
  for (const i in style) {
    const styleI = style[i]
    if (isObs(styleI)) {
      if (!newStyle) {
        newStyle = {}
        for (const j in style) {
          if (j === i) break
          newStyle[j] = style[j]
        }
      }
      newStyle[i] = values[++at[0]]
    } else if (newStyle) {
      newStyle[i] = styleI
    }
  }
  return newStyle || style
}

function render(props, values) {
  let type = null
  let newProps = null
  let newChildren = null

  const at = [-1]

  for (const key in props) {
    const val = props[key]
    if (CHILDREN === key) {
      newChildren = renderChildren(val, at, values)
    } else if ('$$type' === key) {
      type = props[key]
    } else if (DD_REF === key) {
      newProps = newProps || {}
      newProps.ref = isObs(val) ? values[++at[0]] : val
    } else if (isObs(val)) {
      newProps = newProps || {}
      newProps[key] = values[++at[0]]
    } else if (STYLE === key) {
      newProps = newProps || {}
      newProps.style = renderStyle(val, at, values) || val
    } else {
      newProps = newProps || {}
      newProps[key] = val
    }
  }

  return newChildren instanceof Array
    ? reactElement(type, newProps, ...newChildren)
    : null !== newChildren
      ? reactElement(type, newProps, newChildren)
      : reactElement(type, newProps)
}

//

function forEachInChildrenArray(children, extra, fn) {
  for (let i = 0, n = children.length; i < n; ++i) {
    const childI = children[i]
    if (isObs(childI)) fn(extra, childI)
    else if (isArray(childI)) forEachInChildrenArray(childI, extra, fn)
  }
}

function forEachInProps(props, extra, fn) {
  for (const key in props) {
    const val = props[key]
    if (isObs(val)) {
      fn(extra, val)
    } else if (CHILDREN === key) {
      if (isArray(val)) forEachInChildrenArray(val, extra, fn)
    } else if (STYLE === key) {
      for (const k in val) {
        const valK = val[k]
        if (isObs(valK)) fn(extra, valK)
      }
    }
  }
}

//

function incValues(self) {
  self.values += 1
}
function onAny1(handler, obs) {
  handler.unsub = obs.subscribe(handler)
}
function onAny(self, obs) {
  const handler = (e) => {
    const handlers = self.handlers
    let idx = 0
    while (handlers[idx] !== handler) ++idx
    // Found the index of this handler/value
    if (hasValue(e)) {
      const value = eventValue(e)
      const values = self.values
      if (values[idx] !== value) {
        values[idx] = value
        self.forceUpdate()
      }
    } else if (isError(e)) {
      throw e.error
    } else {
      // This is End
      handlers[idx] = null
      const n = handlers.length
      if (n !== self.values.length) return
      for (let i = 0; i < n; ++i) if (handlers[i]) return
      self.handlers = null // No handlers left -> nullify
    }
  }
  self.handlers.push(handler)
  handler.unsub = obs.subscribe(handler)
}

function unsub(handler) {
  if (handler) {
    handler.unsub()
  }
}

const FromClass = /*#__PURE__*/ inherit(
  // eslint-disable-next-line @typescript-eslint/no-shadow
  function FromClass(props) {
    LiftedComponent.call(this, props)
    this.values = this
    this.handlers = null
  },
  LiftedComponent,
  {
    componentWillUnmount() {
      const handlers = this.handlers
      if (handlers instanceof Function) {
        handlers.unsub()
      } else if (handlers) {
        handlers.forEach(unsub)
      }
    },
    doSubscribe(props) {
      this.values = 0
      forEachInProps(props, this, incValues)
      const n = this.values // Here this.values contains the number of observable values. Later on, it'll contain the actual values.

      switch (n) {
        case 0:
          this.values = array0
          break
        case 1: {
          this.values = this
          const handlers = (e) => {
            if (hasValue(e)) {
              const value = eventValue(e)
              if (this.values !== value) {
                this.values = value
                this.forceUpdate()
              }
            } else if (isError(e)) {
              throw e.error
            } else {
              // Assume this is End
              this.values = [this.values]
              this.handlers = null
            }
          }
          this.handlers = handlers
          forEachInProps(props, handlers, onAny1)
          break
        }
        default:
          this.values = Array(n).fill(this)
          this.handlers = []
          forEachInProps(props, this, onAny)
      }
    },
    render() {
      if (this.handlers instanceof Function) {
        const value = this.values
        if (value === this) return null
        return render(this.props, [value])
      } else {
        const values = this.values
        for (let i = 0, n = values.length; i < n; ++i)
          if (values[i] === this) return null
        return render(this.props, values)
      }
    }
  }
)

//

function hasObsInChildrenArray(i, children) {
  for (const n = children.length; i < n; ++i) {
    const child = children[i]
    if (isObs(child) || (isArray(child) && hasObsInChildrenArray(0, child)))
      return true
  }
  return false
}

function hasObsInProps(props) {
  for (const key in props) {
    const val = props[key]
    if (isObs(val)) {
      return true
    } else if (CHILDREN === key) {
      if (isArray(val) && hasObsInChildrenArray(0, val)) return true
    } else if (STYLE === key) {
      for (const k in val) if (isObs(val[k])) return true
    }
  }
  return false
}

//

function filterProps(type, props) {
  const newProps = { $$type: type }
  for (const key in props) {
    const val = props[key]
    if ('ref' === key) newProps[DD_REF] = val
    else if (LIFT !== key) newProps[key] = val
  }
  return newProps
}

function createElement(...args) {
  const type = args[0]
  const props = args[1] || object0
  if (isString(type) || props[LIFT]) {
    if (hasObsInChildrenArray(2, args) || hasObsInProps(props)) {
      args[1] = filterProps(type, props)
      args[0] = FromClass
    } else if (props[LIFT]) {
      args[1] = dissocPartialU(LIFT, props) || object0
    }
  }
  return reactElement(...args)
}

export default assocPartialU('createElement', createElement, React)

//

export const fromClass = (Class) => (props) =>
  reactElement(FromClass, filterProps(Class, props))
