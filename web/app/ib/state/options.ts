import { useMemo } from 'react'
import { useKoodisto, useKoodistot } from '../../appstate/koodisto'
import {
  groupKoodistoToOptions,
  SelectOption
} from '../../components-v2/controls/Select'
import {
  PreIBOppiaineTunniste,
  PreIBOppiaineTunnisteKoodistoUri
} from './preIBOppiaine'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'

export const usePreIBTunnisteOptions = ():
  | SelectOption<PreIBOppiaineTunniste>[]
  | null => {
  const tunnisteet = useKoodistot<PreIBOppiaineTunnisteKoodistoUri>(
    'oppiaineetib',
    'koskioppiaineetyleissivistava'
  )
  return useMemo(
    () => tunnisteet && groupKoodistoToOptions(tunnisteet),
    [tunnisteet]
  )
}

const optionLoader =
  <T extends string>(koodistoUri: T) =>
  (required: boolean): SelectOption<Koodistokoodiviite<T>>[] | null => {
    const kielet = useKoodisto(required ? koodistoUri : null)
    return useMemo(() => kielet && groupKoodistoToOptions(kielet), [kielet])
  }

export const useKielivalikoimaOptions = optionLoader('kielivalikoima')
export const useMatematiikanOppimääräOptions = optionLoader(
  'oppiainematematiikka'
)
export const useAineryhmäOptions = optionLoader('aineryhmaib')
export const useÄidinkielenKieliOptions = optionLoader(
  'oppiaineaidinkielijakirjallisuus'
)
