import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * RahoituslähdeJakso
 *
 * @see `fi.oph.koski.schema.RahoituslähdeJakso`
 */
export type RahoituslähdeJakso = {
  $class: 'fi.oph.koski.schema.RahoituslähdeJakso'
  alku: string
  loppu?: string
  rahoituslähde: Koodistokoodiviite<'virtarahoituslahde', string>
}

export const RahoituslähdeJakso = (o: {
  alku: string
  loppu?: string
  rahoituslähde: Koodistokoodiviite<'virtarahoituslahde', string>
}): RahoituslähdeJakso => ({
  $class: 'fi.oph.koski.schema.RahoituslähdeJakso',
  ...o
})

RahoituslähdeJakso.className = 'fi.oph.koski.schema.RahoituslähdeJakso' as const

export const isRahoituslähdeJakso = (a: any): a is RahoituslähdeJakso =>
  a?.$class === 'fi.oph.koski.schema.RahoituslähdeJakso'
