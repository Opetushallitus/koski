import { updateAt } from './array'

/**
 * Polun osa.
 *
 * Merkkijono on objektin propertyn nimi.
 * Numero on taulukon indeksi.
 * OptionalProp on objektin property, joka voi olla tyhjä. Kyseinen tyyppi sisältää myös ohjeet tyhjän arvon luomiseksi.
 */
export type PathToken = string | number | OptionalProp

export type OptionalProp = {
  key: string
  onEmpty: () => any
}

const isOptionalProp = (a?: any): a is OptionalProp =>
  typeof a === 'object' &&
  typeof a.key === 'string' &&
  typeof a.onEmpty === 'function'

const getKey = (token: PathToken): string | number =>
  isOptionalProp(token) ? token.key : token

const getValueOrCreate = (t: any, token: PathToken): any =>
  isOptionalProp(token) ? (t?.[token.key] ?? token.onEmpty()) : t?.[token]

export const get =
  <T>(...path: PathToken[]) =>
  (t: any): T => {
    if (path.length === 0) return t
    const [head, ...tail] = path
    return get(...tail)(getValueOrCreate(t, head)) as T
  }

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
    let result: any = undefined
    let error: any = undefined
    try {
      result = modify(...path)((value) => {
        valueAtPath = value
        resultAtPath = f(value as any)
        return resultAtPath
      })(t)
    } catch (err) {
      error = err
    }
    console.group('Modify')
    console.log('Source:', t)
    console.log(
      'Path:',
      path.map((p) => (isOptionalProp(p) ? `${p.key}?` : p)).join('.')
    )
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
    if (result !== undefined) console.log('Result:', result)
    if (error !== undefined) console.log('Error:', error)
    console.groupEnd()
    return result
  }

const modifyObjectOrArray = <T>(
  obj: T,
  token: PathToken,
  f: (a: any) => any
): T =>
  typeof obj === 'object'
    ? Array.isArray(obj)
      ? typeof token === 'number'
        ? (updateAt(token, f(getValueOrCreate(obj, token)))(obj || []) as T)
        : throwError(
            `Invalid path: key '${getKey(token)}' cannot point to an array`
          )
      : { ...obj, [getKey(token)]: f(getValueOrCreate(obj, token)) }
    : throwError(
        `Invalid path: key '${token}' cannot point to a non-object ${JSON.stringify(obj)}`
      )

const throwError = (message: string): never => {
  throw Error(message)
}

const valuesAlongPath = (
  t: any,
  path: PathToken[],
  accumPath: string = ''
): Array<{ path: string; value: any }> => {
  if (path.length === 0) return [{ path: accumPath, value: t }]
  const [token, ...rest] = path
  return [
    { path: accumPath, value: t },
    ...valuesAlongPath(
      getValueOrCreate(t, token),
      rest,
      accumPath + '.' + getKey(token)
    )
  ]
}
