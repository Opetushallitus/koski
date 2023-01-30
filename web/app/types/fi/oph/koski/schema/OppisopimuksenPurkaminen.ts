/**
 * OppisopimuksenPurkaminen
 *
 * @see `fi.oph.koski.schema.OppisopimuksenPurkaminen`
 */
export type OppisopimuksenPurkaminen = {
  $class: 'fi.oph.koski.schema.OppisopimuksenPurkaminen'
  päivä: string
  purettuKoeajalla: boolean
}

export const OppisopimuksenPurkaminen = (o: {
  päivä: string
  purettuKoeajalla: boolean
}): OppisopimuksenPurkaminen => ({
  $class: 'fi.oph.koski.schema.OppisopimuksenPurkaminen',
  ...o
})

OppisopimuksenPurkaminen.className =
  'fi.oph.koski.schema.OppisopimuksenPurkaminen' as const

export const isOppisopimuksenPurkaminen = (
  a: any
): a is OppisopimuksenPurkaminen =>
  a?.$class === 'fi.oph.koski.schema.OppisopimuksenPurkaminen'
