import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import { AktiivisetJaPäättyneetOpinnotLukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu } from './AktiivisetJaPaattyneetOpinnotLukuvuosiIlmoittautumisjaksonLukuvuosiMaksu'

/**
 * AktiivisetJaPäättyneetOpinnotLukukausi_Ilmoittautumisjakso
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotLukukausi_Ilmoittautumisjakso`
 */
export type AktiivisetJaPäättyneetOpinnotLukukausi_Ilmoittautumisjakso = {
  $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotLukukausi_Ilmoittautumisjakso'
  tila: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  maksetutLukuvuosimaksut?: AktiivisetJaPäättyneetOpinnotLukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu
  ylioppilaskunnanJäsen?: boolean
  loppu?: string
  alku: string
}

export const AktiivisetJaPäättyneetOpinnotLukukausi_Ilmoittautumisjakso = (o: {
  tila: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  maksetutLukuvuosimaksut?: AktiivisetJaPäättyneetOpinnotLukuvuosi_IlmoittautumisjaksonLukuvuosiMaksu
  ylioppilaskunnanJäsen?: boolean
  loppu?: string
  alku: string
}): AktiivisetJaPäättyneetOpinnotLukukausi_Ilmoittautumisjakso => ({
  $class:
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotLukukausi_Ilmoittautumisjakso',
  ...o
})

AktiivisetJaPäättyneetOpinnotLukukausi_Ilmoittautumisjakso.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotLukukausi_Ilmoittautumisjakso' as const

export const isAktiivisetJaPäättyneetOpinnotLukukausi_Ilmoittautumisjakso = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotLukukausi_Ilmoittautumisjakso =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotLukukausi_Ilmoittautumisjakso'
