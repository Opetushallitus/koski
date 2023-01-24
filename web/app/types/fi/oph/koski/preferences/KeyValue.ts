/**
 * KeyValue
 *
 * @see `fi.oph.koski.preferences.KeyValue`
 */
export type KeyValue = {
  $class: 'fi.oph.koski.preferences.KeyValue'
  key: string
  value: any
}

export const KeyValue = (o: { key: string; value: any }): KeyValue => ({
  $class: 'fi.oph.koski.preferences.KeyValue',
  ...o
})

KeyValue.className = 'fi.oph.koski.preferences.KeyValue' as const

export const isKeyValue = (a: any): a is KeyValue =>
  a?.$class === 'fi.oph.koski.preferences.KeyValue'
