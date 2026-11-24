/**
 * IbisParseError
 *
 * @see `fi.oph.koski.ibis.IbisParseError`
 */
export type IbisParseError = {
  $class: 'fi.oph.koski.ibis.IbisParseError'
  line: number
  message: string
}

export const IbisParseError = (o: {
  line: number
  message: string
}): IbisParseError => ({ $class: 'fi.oph.koski.ibis.IbisParseError', ...o })

IbisParseError.className = 'fi.oph.koski.ibis.IbisParseError' as const

export const isIbisParseError = (a: any): a is IbisParseError =>
  a?.$class === 'fi.oph.koski.ibis.IbisParseError'
