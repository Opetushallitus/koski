import * as A from 'fp-ts/Array'
import { pipe } from 'fp-ts/lib/function'
import * as O from 'fp-ts/Option'
import { useMemo } from 'react'
import {
  KoodistokoodiviiteKoodistonNimellä,
  useKoodisto,
  useKoodistot
} from '../../appstate/koodisto'
import {
  filterOptions,
  groupKoodistoToOptions,
  mapOptionLabels,
  regroupKoodisto,
  SelectOption,
  SelectOptionOrd,
  sortOptions
} from '../../components-v2/controls/Select'
import { t } from '../../i18n/i18n'
import { IBOpiskeluoikeus } from '../../types/fi/oph/koski/schema/IBOpiskeluoikeus'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { koodistokoodiviiteId, KoodistoUriOf } from '../../util/koodisto'
import { entries } from '../../util/objects'
import { PäätasonSuoritusOf } from '../../util/opiskeluoikeus'
import {
  isIBOppiaineLanguageTunniste,
  isIBOppiaineMuuTunniste,
  isLukionMatematiikka2015Tunniste,
  isLukionMuuValtakunnallinenOppiaine2015Tunniste,
  isLukionUskonto2015Tunniste,
  isLukionÄidinkieliJaKirjallisuus2015Tunniste,
  isVierasTaiToinenKotimainenKieli2015Tunniste
} from '../oppiaineet/tunnisteet'
import { PreIBOppiaineTunniste } from './preIBOppiaine'
import {
  LukiokurssiTunnisteUri,
  lukiokurssiTunnisteUrit
} from '../oppiaineet/preIBKurssi2015'
import { NonEmptyArray } from 'fp-ts/lib/NonEmptyArray'
import { useApiData, useApiWithParams } from '../../api-fetch'
import { fetchOppiaineenKurssit } from '../../util/koskiApi'
import { enumValuesToKoodistoSelectOptions } from '../../util/TypedEnumValue'

export const preIB2015Oppiainekategoriat = {
  'IB-oppiaine': [isIBOppiaineLanguageTunniste, isIBOppiaineMuuTunniste],
  'Lukion oppiaine': [
    isLukionMatematiikka2015Tunniste,
    isLukionMuuValtakunnallinenOppiaine2015Tunniste,
    isLukionUskonto2015Tunniste,
    isLukionÄidinkieliJaKirjallisuus2015Tunniste,
    isVierasTaiToinenKotimainenKieli2015Tunniste
  ]
}

export const PaikallinenKey = '__paikallinen__'

export const Paikallinen = Koodistokoodiviite({
  koodistoUri: lukiokurssiTunnisteUrit[0],
  koodiarvo: PaikallinenKey
})

const oppiaineCategoryResolver =
  <K extends Koodistokoodiviite>(
    types: Record<string, Array<(k: K) => boolean>>
  ) =>
  (k: K) =>
    pipe(
      types,
      entries,
      A.findFirst(([_, guards]) => guards.some((g) => g(k))),
      O.map(([name, _]) => t(name)),
      O.toNullable
    )

const labelWithKoodiarvo = (k: SelectOption<Koodistokoodiviite>): string =>
  k.value?.koodiarvo ? `${k.value.koodiarvo} ${k.label}` : k.label

export const usePreIBTunnisteOptions = <K extends Koodistokoodiviite>(
  categories: Record<string, Array<(k: K) => boolean>>,
  päätasonSuoritus: PäätasonSuoritusOf<IBOpiskeluoikeus>
): SelectOption<PreIBOppiaineTunniste>[] | null => {
  const tunnisteet = useKoodistot<KoodistoUriOf<K>>(
    'oppiaineetib',
    'koskioppiaineetyleissivistava'
  )

  const existingOppiaineet = useMemo(
    () =>
      (päätasonSuoritus.osasuoritukset || []).map(
        (os) => os.koulutusmoduuli.tunniste.koodiarvo
      ),
    [päätasonSuoritus.osasuoritukset]
  )

  return useMemo(() => {
    const getCategory = oppiaineCategoryResolver(categories)
    return pipe(
      regroupKoodisto(tunnisteet || [], (tunniste) =>
        getCategory(tunniste.koodiviite as K)
      ),
      groupKoodistoToOptions,
      filterOptions(
        (k) =>
          k.value?.koodiarvo === undefined ||
          !existingOppiaineet.includes(k.value?.koodiarvo)
      ),
      mapOptionLabels(labelWithKoodiarvo),
      A.prepend({
        key: PaikallinenKey,
        label: t('Paikallinen')
      })
    )
  }, [categories, existingOppiaineet, tunnisteet])
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

export const useLukiokurssinTyypit = optionLoader('lukionkurssintyyppi')

export const useOppiaineenKurssiOptions = (
  tunniste?: Koodistokoodiviite
): SelectOption<Koodistokoodiviite>[] | null => {
  const response = useApiWithParams(
    fetchOppiaineenKurssit,
    tunniste && [
      tunniste.koodistoUri,
      tunniste.koodiarvo,
      ['lukionkurssit'] // ???
    ]
  )
  const data = useApiData(response)
  return useMemo(
    () =>
      pipe(
        data || [],
        enumValuesToKoodistoSelectOptions,
        mapOptionLabels(labelWithKoodiarvo),
        sortOptions,
        A.append({
          key: koodistokoodiviiteId(Paikallinen),
          label: t('Lisää paikallinen'),
          value: Paikallinen
        } as SelectOption<Koodistokoodiviite>)
      ),
    [data]
  )
}
