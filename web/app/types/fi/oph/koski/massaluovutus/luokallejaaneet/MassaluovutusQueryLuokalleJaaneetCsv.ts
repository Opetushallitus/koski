/**
 * Tämä kysely on tarkoitettu opiskeluoikeusversioiden löytäiseksi KOSKI-varannoksi, joissa perusopetuksen opiskeluoikeuteen on merkitty tieto, että oppilas jää luokalle.
 * Vastauksen skeema on saatavana <a href="/koski/json-schema-viewer/?schema=luokalle-jaaneet-result.json">täältä.</a>
 *
 * @see `fi.oph.koski.massaluovutus.luokallejaaneet.MassaluovutusQueryLuokalleJaaneetCsv`
 */
export type MassaluovutusQueryLuokalleJaaneetCsv = {
  $class: 'fi.oph.koski.massaluovutus.luokallejaaneet.MassaluovutusQueryLuokalleJaaneetCsv'
  type: 'luokallejaaneet'
  format: 'text/csv'
  organisaatioOid?: string
}

export const MassaluovutusQueryLuokalleJaaneetCsv = (
  o: {
    type?: 'luokallejaaneet'
    format?: 'text/csv'
    organisaatioOid?: string
  } = {}
): MassaluovutusQueryLuokalleJaaneetCsv => ({
  $class:
    'fi.oph.koski.massaluovutus.luokallejaaneet.MassaluovutusQueryLuokalleJaaneetCsv',
  type: 'luokallejaaneet',
  format: 'text/csv',
  ...o
})

MassaluovutusQueryLuokalleJaaneetCsv.className =
  'fi.oph.koski.massaluovutus.luokallejaaneet.MassaluovutusQueryLuokalleJaaneetCsv' as const

export const isMassaluovutusQueryLuokalleJaaneetCsv = (
  a: any
): a is MassaluovutusQueryLuokalleJaaneetCsv =>
  a?.$class ===
  'fi.oph.koski.massaluovutus.luokallejaaneet.MassaluovutusQueryLuokalleJaaneetCsv'
