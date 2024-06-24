import * as A from 'fp-ts/Array'
import * as Eq from 'fp-ts/Eq'
import * as string from 'fp-ts/string'
import { useMemo } from 'react'
import { isSuccess, useApiWithParams } from '../../api-fetch'
import { useChildSchema, useChildSchemaSafe } from '../../appstate/constraints'
import { useKoodisto, useKoodistoOfConstraint } from '../../appstate/koodisto'
import {
  SelectOption,
  koodiviiteToOption
} from '../../components-v2/controls/Select'
import { isJotpaRahoituksenKoodistoviite } from '../../jotpa/jotpa'
import { OrganisaatioHierarkia } from '../../types/fi/oph/koski/organisaatio/OrganisaatioHierarkia'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuVapaanSivistystyonOpiskeluoikeusjakso'
import { VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenOpiskeluoikeusjakso'
import { VapaanSivistystyönOpiskeluoikeus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { VapaanSivistystyönOsaamismerkinOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOsaamismerkinOpiskeluoikeusjakso'
import { VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso } from '../../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso'
import { OpiskeluoikeusClass } from '../../types/fi/oph/koski/typemodel/OpiskeluoikeusClass'
import { fetchOrganisaationOpiskeluoikeustyypit } from '../../util/koskiApi'
import {
  UusiOpiskeluoikeusDialogState,
  opiskeluoikeustyyppiToClassNames
} from './state'
import { TutkintokoulutukseenValmentavanOpiskeluoikeus } from '../../types/fi/oph/koski/schema/TutkintokoulutukseenValmentavanOpiskeluoikeus'
import { TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot } from '../../types/fi/oph/koski/schema/TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisatiedot'
import { TutkintokoulutukseenValmentavanOpiskeluoikeudenLukiokoulutuksenLuvanLisätiedot } from '../../types/fi/oph/koski/schema/TutkintokoulutukseenValmentavanOpiskeluoikeudenLukiokoulutuksenLuvanLisatiedot'
import { TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot } from '../../types/fi/oph/koski/schema/TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisatiedot'

export const useOpiskeluoikeustyypit = (
  organisaatio?: OrganisaatioHierarkia
): Array<SelectOption<Koodistokoodiviite<'opiskeluoikeudentyyppi'>>> => {
  const apiCall = useApiWithParams(
    fetchOrganisaationOpiskeluoikeustyypit<'opiskeluoikeudentyyppi'>,
    organisaatio?.oid !== undefined ? [organisaatio?.oid] : undefined
  )
  const options = useMemo(
    () => (isSuccess(apiCall) ? apiCall.data : []).map(koodiviiteToOption),
    [apiCall]
  )
  return options
}

export const usePäätasonSuoritustyypit = (
  state: UusiOpiskeluoikeusDialogState
): Array<SelectOption<Koodistokoodiviite<'suorituksentyyppi'>>> => {
  const koodisto = useKoodisto('suorituksentyyppi')?.map((k) => k.koodiviite)
  const ooTyyppi = state.opiskeluoikeus.value
  const ooMapping = state.ooMapping

  return useMemo(() => {
    const ooClassInfo =
      ooTyyppi !== undefined && ooMapping
        ? ooMapping.find((c) => c.tyyppi === ooTyyppi.koodiarvo)
        : undefined
    const koodit = ooClassInfo
      ? ooClassInfo.suoritukset.flatMap((s) => {
          const viite = koodisto?.find((k) => k.koodiarvo === s.tyyppi)
          return viite ? [viite] : []
        })
      : []
    return distinctKeys(koodit.map(koodiviiteToOption))
  }, [koodisto, ooMapping, ooTyyppi])
}

const SelectOptionKeyEq = <O extends SelectOption<any>>() =>
  Eq.contramap((o: O) => o.key)(string.Eq)

const distinctKeys = <O extends SelectOption<any>>(os: O[]) =>
  A.uniq(SelectOptionKeyEq<O>())(os)

const opiskeluoikeudenTilaClass = (
  ooClass?: OpiskeluoikeusClass,
  suoritustyyppi?: string
): string | undefined => {
  switch (ooClass?.className) {
    case VapaanSivistystyönOpiskeluoikeus.className:
      switch (suoritustyyppi) {
        case 'vstoppivelvollisillesuunnattukoulutus':
        case 'vstmaahanmuuttajienkotoutumiskoulutus':
        case 'vstlukutaitokoulutus':
          return OppivelvollisilleSuunnattuVapaanSivistystyönOpiskeluoikeusjakso.className
        case 'vstjotpakoulutus':
          return VapaanSivistystyönJotpaKoulutuksenOpiskeluoikeusjakso.className
        case 'vstosaamismerkki':
          return VapaanSivistystyönOsaamismerkinOpiskeluoikeusjakso.className
        case 'vstvapaatavoitteinenkoulutus':
          return VapaanSivistystyönVapaatavoitteisenKoulutuksenOpiskeluoikeusjakso.className
        default:
          return undefined
      }
    default: {
      const jaksot = ooClass?.opiskeluoikeusjaksot || []
      if (jaksot.length > 1) {
        throw new Error(
          `Epäselvä tilanne: useampi kuin yksi mahdollinen opiskeluoikeuden tilan luokka mahdollinen: ${ooClass?.className}: ${jaksot.join(', ')}`
        )
      }
      return jaksot[0]
    }
  }
}

export const opiskeluoikeudenLisätiedotClass = (
  ooClass?: OpiskeluoikeusClass,
  tuvaJärjestämislupa?: Koodistokoodiviite<'tuvajarjestamislupa'>
): string | undefined => {
  if (
    ooClass?.className ===
    TutkintokoulutukseenValmentavanOpiskeluoikeus.className
  ) {
    switch (tuvaJärjestämislupa?.koodiarvo) {
      case 'ammatillinen':
        return TutkintokoulutukseenValmentavanOpiskeluoikeudenAmmatillisenLuvanLisätiedot.className
      case 'lukio':
        return TutkintokoulutukseenValmentavanOpiskeluoikeudenLukiokoulutuksenLuvanLisätiedot.className
      case 'perusopetus':
        return TutkintokoulutukseenValmentavanOpiskeluoikeudenPerusopetuksenLuvanLisätiedot.className
      default:
        return undefined
    }
  }

  const lisätiedot = ooClass?.lisätiedot || []
  if (lisätiedot.length > 1) {
    throw new Error(
      `Epäselvä tilanne: useampi kuin yksi mahdollinen opiskeluoikeuden lisätietoluokka mahdollinen: ${lisätiedot.join(', ')}`
    )
  }
  return lisätiedot[0]
}
export const useOpiskeluoikeudenTilat = (
  state: UusiOpiskeluoikeusDialogState
): {
  options: Array<SelectOption<Koodistokoodiviite<'koskiopiskeluoikeudentila'>>>
  initialValue?: string
} => {
  const className = opiskeluoikeustyyppiToClassNames(
    state.ooMapping,
    state.opiskeluoikeus?.value?.koodiarvo
  )

  const opiskelujaksonTila = useChildSchema(
    opiskeluoikeudenTilaClass(
      className,
      state.päätasonSuoritus.value?.koodiarvo
    ),
    'tila'
  )

  const koodistot =
    useKoodistoOfConstraint<'koskiopiskeluoikeudentila'>(opiskelujaksonTila)

  const options = useMemo(
    () =>
      koodistot
        ? koodistot
            .flatMap((k) =>
              k.koodiviite.koodiarvo !== 'mitatoity' ? [k.koodiviite] : []
            )
            .map(koodiviiteToOption)
        : [],
    [koodistot]
  )

  const initialValue = useMemo(() => {
    const defaults = [
      'koskiopiskeluoikeudentila_lasna',
      'koskiopiskeluoikeudentila_valmistunut', // Opiskeluoikeus halutaan merkitä tavallisesti suoraan valmistuneeksi, jolla sillä ei ole läsnä-tilaa
      'koskiopiskeluoikeudentila_hyvaksytystisuoritettu'
    ]
    return defaults.find((tila) => options.find((tt) => tt.key === tila))
  }, [options])

  return { options, initialValue }
}
export const useOpintojenRahoitus = (state: UusiOpiskeluoikeusDialogState) => {
  const className = opiskeluoikeustyyppiToClassNames(
    state.ooMapping,
    state.opiskeluoikeus?.value?.koodiarvo
  )

  const opiskelujaksonTila = useChildSchemaSafe(
    opiskeluoikeudenTilaClass(
      className,
      state.päätasonSuoritus.value?.koodiarvo
    ),
    'opintojenRahoitus'
  )

  const koodistot = useKoodistoOfConstraint<'opintojenrahoitus'>(
    opiskelujaksonTila ? opiskelujaksonTila : null
  )

  const options = useMemo(
    () =>
      koodistot
        ? koodistot.flatMap((k) => k.koodiviite).map(koodiviiteToOption)
        : [],
    [koodistot]
  )

  const initialValue = useMemo(() => options[0]?.value?.koodiarvo, [options])

  return { options, initialValue }
}
export const useJotpaAsianumero = (state: UusiOpiskeluoikeusDialogState) => {
  const className = opiskeluoikeustyyppiToClassNames(
    state.ooMapping,
    state.opiskeluoikeus?.value?.koodiarvo
  )

  const asianumeroSchema = useChildSchemaSafe(
    opiskeluoikeudenLisätiedotClass(className),
    'jotpaAsianumero'
  )

  const koodistot = useKoodistoOfConstraint<'jotpaasianumero'>(
    asianumeroSchema &&
      state.opintojenRahoitus.value &&
      isJotpaRahoituksenKoodistoviite(state.opintojenRahoitus.value)
      ? asianumeroSchema
      : null
  )

  const options = useMemo(
    () =>
      koodistot
        ? koodistot.flatMap((k) => k.koodiviite).map(koodiviiteToOption)
        : [],
    [koodistot]
  )

  const initialValue = useMemo(() => options[0]?.value?.koodiarvo, [options])

  return { options, initialValue }
}

export const useDefaultKieli = (
  state: UusiOpiskeluoikeusDialogState
): string => {
  return state.opiskeluoikeus.value?.koodiarvo === 'diatutkinto'
    ? 'kieli_DE'
    : 'kieli_FI'
}
