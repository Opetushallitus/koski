import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * KoulutuskuntaJakso
 *
 * @see `fi.oph.koski.schema.KoulutuskuntaJakso`
 */
export type KoulutuskuntaJakso = {
  $class: 'fi.oph.koski.schema.KoulutuskuntaJakso'
  alku: string
  loppu?: string
  koulutuskunta: Koodistokoodiviite<'kunta', string>
}

export const KoulutuskuntaJakso = (o: {
  alku: string
  loppu?: string
  koulutuskunta: Koodistokoodiviite<'kunta', string>
}): KoulutuskuntaJakso => ({
  $class: 'fi.oph.koski.schema.KoulutuskuntaJakso',
  ...o
})

KoulutuskuntaJakso.className = 'fi.oph.koski.schema.KoulutuskuntaJakso' as const

export const isKoulutuskuntaJakso = (a: any): a is KoulutuskuntaJakso =>
  a?.$class === 'fi.oph.koski.schema.KoulutuskuntaJakso'
