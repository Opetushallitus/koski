/**
 * AktiivisetJaPäättyneetOpinnotLukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotLukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu`
 */
export type AktiivisetJaPäättyneetOpinnotLukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu =
  {
    $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotLukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu'
    maksettu?: boolean
    summa?: number
    apuraha?: number
  }

export const AktiivisetJaPäättyneetOpinnotLukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu =
  (
    o: {
      maksettu?: boolean
      summa?: number
      apuraha?: number
    } = {}
  ): AktiivisetJaPäättyneetOpinnotLukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu => ({
    $class:
      'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotLukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu',
    ...o
  })

AktiivisetJaPäättyneetOpinnotLukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotLukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu' as const

export const isAktiivisetJaPäättyneetOpinnotLukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotLukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu =>
    a?.$class ===
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotLukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu'
