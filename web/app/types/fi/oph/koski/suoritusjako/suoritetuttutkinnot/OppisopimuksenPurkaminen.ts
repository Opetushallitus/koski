/**
 * OppisopimuksenPurkaminen
 *
 * @see `fi.oph.koski.suoritusjako.suoritetuttutkinnot.OppisopimuksenPurkaminen`
 */
export type OppisopimuksenPurkaminen = {
  $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.OppisopimuksenPurkaminen'
  päivä: string
  purettuKoeajalla: boolean
}

export const OppisopimuksenPurkaminen = (o: {
  päivä: string
  purettuKoeajalla: boolean
}): OppisopimuksenPurkaminen => ({
  $class:
    'fi.oph.koski.suoritusjako.suoritetuttutkinnot.OppisopimuksenPurkaminen',
  ...o
})

OppisopimuksenPurkaminen.className =
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.OppisopimuksenPurkaminen' as const

export const isOppisopimuksenPurkaminen = (
  a: any
): a is OppisopimuksenPurkaminen =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.OppisopimuksenPurkaminen'
