/**
 * Tämä kysely on tarkoitettu opiskeluoikeusversioiden löytäiseksi KOSKI-varannoksi, joissa perusopetuksen opiskeluoikeuteen on merkitty tieto, että oppilas jää luokalle.
 * Vastauksen skeema on saatavana <a href="/koski/json-schema-viewer/?schema=luokalle-jaaneet-result.json">täältä.</a>
 *
 * @see `fi.oph.koski.massaluovutus.luokallejaaneet.MassaluovutusQueryLuokalleJaaneetJson`
 */
export type MassaluovutusQueryLuokalleJaaneetJson = {
  $class: 'fi.oph.koski.massaluovutus.luokallejaaneet.MassaluovutusQueryLuokalleJaaneetJson'
  type: 'luokallejaaneet'
  format: 'application/json'
  organisaatioOid?: string
}

export const MassaluovutusQueryLuokalleJaaneetJson = (
  o: {
    type?: 'luokallejaaneet'
    format?: 'application/json'
    organisaatioOid?: string
  } = {}
): MassaluovutusQueryLuokalleJaaneetJson => ({
  $class:
    'fi.oph.koski.massaluovutus.luokallejaaneet.MassaluovutusQueryLuokalleJaaneetJson',
  type: 'luokallejaaneet',
  format: 'application/json',
  ...o
})

MassaluovutusQueryLuokalleJaaneetJson.className =
  'fi.oph.koski.massaluovutus.luokallejaaneet.MassaluovutusQueryLuokalleJaaneetJson' as const

export const isMassaluovutusQueryLuokalleJaaneetJson = (
  a: any
): a is MassaluovutusQueryLuokalleJaaneetJson =>
  a?.$class ===
  'fi.oph.koski.massaluovutus.luokallejaaneet.MassaluovutusQueryLuokalleJaaneetJson'
