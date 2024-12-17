import { updateAt } from './array'

export type PathToken = string | number

export const modify =
  (...path: PathToken[]) =>
  <S>(f: (s: S) => S) =>
  <T>(t: T): T => {
    if (path.length === 0) {
      return f(t as any) as any
    }
    const [token, ...rest] = path
    return modifyObjectOrArray(t, token, modify(...rest)(f))
  }

export const modifyWithDebug =
  (...path: PathToken[]) =>
  <S>(f: (s: S) => S) =>
  <T>(t: T): T => {
    let valueAtPath: any = undefined
    let resultAtPath: any = undefined
    const result = modify(...path)((value) => {
      valueAtPath = value
      resultAtPath = f(value as any)
      return resultAtPath
    })(t)
    console.group('Modify')
    console.log('Source:', t)
    console.log('Path:', path.join('.'))
    console.group('Values along path')
    valuesAlongPath(t, path).forEach(({ path: p, value }) => {
      if (value === undefined) {
        console.warn(p, value)
      } else {
        console.log(p, value)
      }
    })
    console.groupEnd()
    console.group('Modification at place')
    console.log('Before:', valueAtPath)
    console.log('After:', resultAtPath)
    console.groupEnd()
    console.log('Result:', result)
    console.groupEnd()
    return result
  }

const modifyObjectOrArray = <T>(
  obj: T,
  key: PathToken,
  f: (a: any) => any
): T =>
  typeof obj === 'object'
    ? Array.isArray(obj)
      ? typeof key === 'number'
        ? (updateAt(key, f(obj[key]))(obj || []) as T)
        : throwError(`Invalid path: key '${key}' cannot point to an array`)
      : { ...obj, [key]: f((obj as any)[key]) }
    : throwError(
        `Invalid path: key '${key}' cannot point to a non-object ${JSON.stringify(obj)}`
      )

const throwError = (message: string): never => {
  throw Error(message)
}

const valuesAlongPath = (
  t: any,
  path: PathToken[],
  accumPath: string = '.'
): Array<{ path: string; value: any }> => {
  if (path.length === 0) return []
  const [token, ...rest] = path
  return [
    { path: accumPath, value: t },
    ...valuesAlongPath(t?.[token], rest, accumPath + token + '.')
  ]
}
