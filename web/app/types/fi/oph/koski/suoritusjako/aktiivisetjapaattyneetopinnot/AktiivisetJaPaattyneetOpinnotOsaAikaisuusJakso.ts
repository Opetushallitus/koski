/**
 * AktiivisetJaPäättyneetOpinnotOsaAikaisuusJakso
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOsaAikaisuusJakso`
 */
export type AktiivisetJaPäättyneetOpinnotOsaAikaisuusJakso = {
  $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOsaAikaisuusJakso'
  alku: string
  loppu?: string
  osaAikaisuus: number
}

export const AktiivisetJaPäättyneetOpinnotOsaAikaisuusJakso = (o: {
  alku: string
  loppu?: string
  osaAikaisuus: number
}): AktiivisetJaPäättyneetOpinnotOsaAikaisuusJakso => ({
  $class:
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOsaAikaisuusJakso',
  ...o
})

AktiivisetJaPäättyneetOpinnotOsaAikaisuusJakso.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOsaAikaisuusJakso' as const

export const isAktiivisetJaPäättyneetOpinnotOsaAikaisuusJakso = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotOsaAikaisuusJakso =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOsaAikaisuusJakso'
