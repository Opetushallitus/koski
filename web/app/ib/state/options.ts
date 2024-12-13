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
import { koodistokoodiviiteId, KoodistoUriOf } from '../../util/koodisto'
import { fetchOppiaineenKurssit } from '../../util/koskiApi'
import { entries } from '../../util/objects'
import { PäätasonSuoritusOf } from '../../util/opiskeluoikeus'
import { enumValuesToKoodistoSelectOptions } from '../../util/TypedEnumValue'
import { lukiokurssiTunnisteUrit } from '../oppiaineet/preIBKurssi2015'
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

export const UusiPaikallinenKey = '__uusi_paikallinen__'

export const UusiPaikallinen = Koodistokoodiviite({
  koodistoUri: lukiokurssiTunnisteUrit[0],
  koodiarvo: UusiPaikallinenKey
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
          key: UusiPaikallinenKey,
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
          key: koodistokoodiviiteId(UusiPaikallinen),
          label: t('Lisää paikallinen'),
          value: UusiPaikallinen
        } as SelectOption<Koodistokoodiviite>)
      ),
    [data]
  )
}
