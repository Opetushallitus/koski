import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusKursseissa } from './LaajuusKursseissa'

/**
 * MuuValtakunnallinenLukioonValmistavanKoulutuksenOppiaine
 *
 * @see `fi.oph.koski.schema.MuuValtakunnallinenLukioonValmistavanKoulutuksenOppiaine`
 */
export type MuuValtakunnallinenLukioonValmistavanKoulutuksenOppiaine = {
  $class: 'fi.oph.koski.schema.MuuValtakunnallinenLukioonValmistavanKoulutuksenOppiaine'
  tunniste: Koodistokoodiviite<
    'oppiaineetluva',
    'LVMALUO' | 'LVYHKU' | 'LVOPO' | 'LVMFKBM' | 'LVHIYH'
  >
  pakollinen: boolean
  laajuus?: LaajuusKursseissa
}

export const MuuValtakunnallinenLukioonValmistavanKoulutuksenOppiaine = (o: {
  tunniste: Koodistokoodiviite<
    'oppiaineetluva',
    'LVMALUO' | 'LVYHKU' | 'LVOPO' | 'LVMFKBM' | 'LVHIYH'
  >
  pakollinen: boolean
  laajuus?: LaajuusKursseissa
}): MuuValtakunnallinenLukioonValmistavanKoulutuksenOppiaine => ({
  $class:
    'fi.oph.koski.schema.MuuValtakunnallinenLukioonValmistavanKoulutuksenOppiaine',
  ...o
})

MuuValtakunnallinenLukioonValmistavanKoulutuksenOppiaine.className =
  'fi.oph.koski.schema.MuuValtakunnallinenLukioonValmistavanKoulutuksenOppiaine' as const

export const isMuuValtakunnallinenLukioonValmistavanKoulutuksenOppiaine = (
  a: any
): a is MuuValtakunnallinenLukioonValmistavanKoulutuksenOppiaine =>
  a?.$class ===
  'fi.oph.koski.schema.MuuValtakunnallinenLukioonValmistavanKoulutuksenOppiaine'
