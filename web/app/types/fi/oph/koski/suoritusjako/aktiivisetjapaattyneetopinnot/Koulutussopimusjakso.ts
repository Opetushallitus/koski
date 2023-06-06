import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'

/**
 * Koulutussopimusjakso
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.Koulutussopimusjakso`
 */
export type Koulutussopimusjakso = {
  $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.Koulutussopimusjakso'
  alku: string
  loppu?: string
  paikkakunta: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  maa: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}

export const Koulutussopimusjakso = (o: {
  alku: string
  loppu?: string
  paikkakunta: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  maa: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}): Koulutussopimusjakso => ({
  $class:
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.Koulutussopimusjakso',
  ...o
})

Koulutussopimusjakso.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.Koulutussopimusjakso' as const

export const isKoulutussopimusjakso = (a: any): a is Koulutussopimusjakso =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.Koulutussopimusjakso'
