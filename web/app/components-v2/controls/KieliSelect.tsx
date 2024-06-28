import * as A from 'fp-ts/Array'
import * as O from 'fp-ts/Option'
import * as Ord from 'fp-ts/Ord'
import { pipe } from 'fp-ts/lib/function'
import * as number from 'fp-ts/number'
import * as string from 'fp-ts/string'
import React, { useMemo } from 'react'
import { useKoodisto } from '../../appstate/koodisto'
import { Select, SelectProps, koodiviiteToOption } from './Select'
import { t } from '../../i18n/i18n'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'

export type KieliSelectProps = Omit<
  SelectProps<Koodistokoodiviite<'kieli'>>,
  'options'
>

export const KieliSelect = (props: KieliSelectProps) => {
  const options = useSuorituskielet()
  const { initialValue, ...rest } = props
  return (
    <Select
      autoselect
      options={options}
      initialValue={initialValue || 'kieli_FI'}
      {...rest}
    />
  )
}

const useSuorituskielet = () => {
  const koodisto = useKoodisto('kieli')
  return useMemo(
    () =>
      pipe(
        O.fromNullable(koodisto),
        A.fromOption,
        A.flatten,
        A.map((k) => k.koodiviite),
        A.sortBy([suorituskieliPinnedOrd, suorituskieliNameOrd]),
        A.map(koodiviiteToOption)
      ),
    [koodisto]
  )
}

const suorituskieliPinnedOrd = Ord.contramap((a: Koodistokoodiviite) => {
  const pinned = ['FI', 'SV', 'EN']
  const index = pinned.indexOf(a.koodiarvo)
  return index < 0 ? 999999 : index
})(number.Ord)

const suorituskieliNameOrd = Ord.contramap((a: Koodistokoodiviite) =>
  t(a.nimi).toLowerCase()
)(string.Ord)
