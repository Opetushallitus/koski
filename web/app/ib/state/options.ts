import * as A from 'fp-ts/Array'
import { pipe } from 'fp-ts/lib/function'
import * as O from 'fp-ts/Option'
import { useMemo } from 'react'
import { useApiData, useApiWithParams } from '../../api-fetch'
import { useKoodisto, useKoodistot } from '../../appstate/koodisto'
import {
  filterOptions,
  groupKoodistoToOptions,
  mapOptionLabels,
  optionGroup,
  paikallinenKoodiToOption,
  regroupKoodisto,
  SelectOption,
  sortOptions
} from '../../components-v2/controls/Select'
import { t } from '../../i18n/i18n'
import { IBOpiskeluoikeus } from '../../types/fi/oph/koski/schema/IBOpiskeluoikeus'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { PaikallinenLukionOppiaine2015 } from '../../types/fi/oph/koski/schema/PaikallinenLukionOppiaine2015'
import { nonNull } from '../../util/fp/arrays'
import { KoodistoUriOf } from '../../util/koodisto'
import { fetchOppiaineenKurssit } from '../../util/koskiApi'
import { entries } from '../../util/objects'
import { PäätasonSuoritusOf } from '../../util/opiskeluoikeus'
import { enumValuesToKoodistoSelectOptions } from '../../util/TypedEnumValue'
import {
  isIBOppiaineLanguageTunniste,
  isIBOppiaineMuuTunniste,
  isLukionMatematiikka2015Tunniste,
  isLukionMuuValtakunnallinenOppiaine2015Tunniste,
  isLukionUskonto2015Tunniste,
  isLukionÄidinkieliJaKirjallisuus2015Tunniste,
  isVierasTaiToinenKotimainenKieli2015Tunniste
} from '../oppiaineet/tunnisteet'
import {
  PreIBOppiaineTunniste,
  ValtakunnallinenPreIBOppiaineTunniste
} from './preIBOppiaine'
import { PaikallinenLukionOppiaine2019 } from '../../types/fi/oph/koski/schema/PaikallinenLukionOppiaine2019'

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

export const uusiPaikallinenKey = (type?: string) =>
  `__${['uusi_paikallinen', type].filter(nonNull).join('_')}__`

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

export const UusiPaikallinenOppiaineKey = uusiPaikallinenKey('oppiaine')

export const labelWithKoodiarvo = (
  k: SelectOption<Koodistokoodiviite>
): string => (k.value?.koodiarvo ? `${k.value.koodiarvo} ${k.label}` : k.label)

export const usePreIB2015TunnisteOptions = <K extends Koodistokoodiviite>(
  categories: Record<string, Array<(k: K) => boolean>>,
  päätasonSuoritus: PäätasonSuoritusOf<IBOpiskeluoikeus>,
  paikallisetOppiaineet: PaikallinenLukionOppiaine2015[]
): SelectOption<PreIBOppiaineTunniste>[] => {
  const valtakunnallisetTunnisteet = useKoodistot<KoodistoUriOf<K>>(
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

  const paikallisetOptions = useMemo(
    () => [
      optionGroup(t('Paikallinen oppiaine'), [
        ...paikallisetOppiaineet.map((kurssi) =>
          paikallinenKoodiToOption(kurssi.tunniste, { removable: true })
        ),
        {
          key: UusiPaikallinenOppiaineKey,
          label: t('Lisää uusi')
        }
      ])
    ],
    [paikallisetOppiaineet]
  )

  const valtakunnallisetOptions: SelectOption<ValtakunnallinenPreIBOppiaineTunniste>[] =
    useMemo(() => {
      const getCategory = oppiaineCategoryResolver(categories)
      return pipe(
        regroupKoodisto(valtakunnallisetTunnisteet || [], (tunniste) =>
          getCategory(tunniste.koodiviite as K)
        ),
        groupKoodistoToOptions,
        filterOptions(
          (k) =>
            k.value?.koodiarvo === undefined ||
            !existingOppiaineet.includes(k.value?.koodiarvo)
        ),
        mapOptionLabels(labelWithKoodiarvo)
      ) as SelectOption<ValtakunnallinenPreIBOppiaineTunniste>[]
    }, [categories, existingOppiaineet, valtakunnallisetTunnisteet])

  return useMemo(
    () => [...paikallisetOptions, ...valtakunnallisetOptions],
    [paikallisetOptions, valtakunnallisetOptions]
  )
}

export const usePreIB2019TunnisteOptions = (
  päätasonSuoritus: PäätasonSuoritusOf<IBOpiskeluoikeus>,
  paikallisetOppiaineet: PaikallinenLukionOppiaine2019[]
): SelectOption<PreIBOppiaineTunniste>[] => {
  const koodisto = useKoodisto('koskioppiaineetyleissivistava', [
    'A',
    'B1',
    'B2',
    'B3',
    'AI',
    'KT',
    'BI',
    'ET',
    'FI',
    'FY',
    'GE',
    'KE',
    'KU',
    'LI',
    'MU',
    'OP',
    'PS',
    'TE',
    'YH',
    'MA'
  ])

  const existingOppiaineet = useMemo(
    () =>
      (päätasonSuoritus.osasuoritukset || []).map(
        (os) => os.koulutusmoduuli.tunniste.koodiarvo
      ),
    [päätasonSuoritus.osasuoritukset]
  )

  const paikallisetOptions = useMemo(
    () => [
      optionGroup(t('Paikallinen oppiaine'), [
        ...paikallisetOppiaineet.map((kurssi) =>
          paikallinenKoodiToOption(kurssi.tunniste, { removable: true })
        ),
        {
          key: UusiPaikallinenOppiaineKey,
          label: t('Lisää uusi')
        }
      ])
    ],
    [paikallisetOppiaineet]
  )

  const valtakunnallisetOptions = useMemo(() => {
    return pipe(
      koodisto || [],
      groupKoodistoToOptions,
      filterOptions(
        (k) =>
          k.value?.koodiarvo === undefined ||
          !existingOppiaineet.includes(k.value?.koodiarvo)
      ),
      mapOptionLabels(labelWithKoodiarvo)
    ) as SelectOption<Koodistokoodiviite<'koskioppiaineetyleissivistava'>>[]
  }, [existingOppiaineet, koodisto])

  return useMemo(
    () => [...paikallisetOptions, ...valtakunnallisetOptions],
    [paikallisetOptions, valtakunnallisetOptions]
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
export const useOppiaineTasoOptions = optionLoader('oppiaineentasoib')

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
        sortOptions
      ),
    [data]
  )
}
