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

export const isOppisopimuksenPurkaminen = (
  a: any
): a is OppisopimuksenPurkaminen => a?.$class === 'OppisopimuksenPurkaminen'
