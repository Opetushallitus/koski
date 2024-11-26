import { Koodistokoodiviite } from '../schema/Koodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'

/**
 * AktiivisetJaPäättyneetOpinnotKoulutuskuntaJakso
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKoulutuskuntaJakso`
 */
export type AktiivisetJaPäättyneetOpinnotKoulutuskuntaJakso = {
  $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKoulutuskuntaJakso'
  alku: string
  loppu?: string
  koulutuskunta: Koodistokoodiviite<'kunta', string>
}

export const AktiivisetJaPäättyneetOpinnotKoulutuskuntaJakso = (o: {
  alku: string
  loppu?: string
  koulutuskunta: Koodistokoodiviite<'kunta', string>
}): AktiivisetJaPäättyneetOpinnotKoulutuskuntaJakso => ({
  $class:
    'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKoulutuskuntaJakso',
  ...o
})

AktiivisetJaPäättyneetOpinnotKoulutuskuntaJakso.className =
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKoulutuskuntaJakso' as const

export const isAktiivisetJaPäättyneetOpinnotKoulutuskuntaJakso = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotKoulutuskuntaJakso =>
  a?.$class ===
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKoulutuskuntaJakso'
