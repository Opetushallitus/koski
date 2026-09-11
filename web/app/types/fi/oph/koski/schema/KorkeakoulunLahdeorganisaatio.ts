import { Oppilaitos } from './Oppilaitos'

/**
 * Organisaatio, josta opintosuoritus on hyväksiluettu
 *
 * @see `fi.oph.koski.schema.KorkeakoulunLähdeorganisaatio`
 */
export type KorkeakoulunLähdeorganisaatio = {
  $class: 'fi.oph.koski.schema.KorkeakoulunLähdeorganisaatio'
  koodi: string
  oppilaitos?: Oppilaitos
}

export const KorkeakoulunLähdeorganisaatio = (o: {
  koodi: string
  oppilaitos?: Oppilaitos
}): KorkeakoulunLähdeorganisaatio => ({
  $class: 'fi.oph.koski.schema.KorkeakoulunLähdeorganisaatio',
  ...o
})

KorkeakoulunLähdeorganisaatio.className =
  'fi.oph.koski.schema.KorkeakoulunLähdeorganisaatio' as const

export const isKorkeakoulunLähdeorganisaatio = (
  a: any
): a is KorkeakoulunLähdeorganisaatio =>
  a?.$class === 'fi.oph.koski.schema.KorkeakoulunLähdeorganisaatio'
