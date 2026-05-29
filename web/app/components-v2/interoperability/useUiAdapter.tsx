import * as string from 'fp-ts/string'
import React, { useEffect, useMemo, useRef } from 'react'
import {
  ApiMethodHook,
  createPreferLocalCache,
  useApiMethod,
  useMergedApiData,
  useOnApiError,
  useOnApiSuccess,
  useSafeState
} from '../../api-fetch'
import { useVersionumero } from '../../appstate/useSearchParam'
import { modelData } from '../../editor/EditorModel'
import { t } from '../../i18n/i18n'
import { Contextualized } from '../../types/EditorModelContext'
import { ObjectModel } from '../../types/EditorModels'
import { isNuortenPerusopetuksenOppimääränSuoritus } from '../../types/fi/oph/koski/schema/NuortenPerusopetuksenOppimaaranSuoritus'
import { Opiskeluoikeus } from '../../types/fi/oph/koski/schema/Opiskeluoikeus'
import { Oppija } from '../../types/fi/oph/koski/schema/Oppija'
import { isPerusopetuksenVuosiluokanSuoritus } from '../../types/fi/oph/koski/schema/PerusopetuksenVuosiluokanSuoritus'
import { intersects, last } from '../../util/fp/arrays'
import { getHenkilöOid } from '../../util/henkilo'
import {
  fetchOmatTiedotOppija,
  fetchOpiskeluoikeus,
  fetchOppija,
  fetchSuoritusjako
} from '../../util/koskiApi'
import { getOpiskeluoikeusOid } from '../../util/opiskeluoikeus'
import { OpiskeluoikeudenTyyppiOf } from '../../util/types'
import { parseQuery } from '../../util/url'
import { opiskeluoikeusEditors } from './uiAdapters'

export type AdaptedOpiskeluoikeusEditorProps<T extends Opiskeluoikeus> = {
  oppijaOid: string
  opiskeluoikeus: T
  invalidatable: boolean
}

export type AdaptedOpiskeluoikeusEditor<T extends Opiskeluoikeus> = React.FC<
  AdaptedOpiskeluoikeusEditorProps<T>
>

export type AdaptedOpiskeluoikeusEditorCollection = Partial<{
  [OO in Opiskeluoikeus as OpiskeluoikeudenTyyppiOf<OO>]: AdaptedOpiskeluoikeusEditor<OO>
}>

export type UiAdapter = {
  isLoadingV2: boolean

  getOpiskeluoikeusEditor: (
    opiskeluoikeusModel: ObjectModel
  ) => AdaptedEditorElement | undefined
}

const loadingUiAdapter: UiAdapter = {
  isLoadingV2: true,
  getOpiskeluoikeusEditor: () => undefined
}

const disabledUiAdapter: UiAdapter = {
  isLoadingV2: false,
  getOpiskeluoikeusEditor: () => undefined
}

export type AdaptedEditorElement = React.ReactElement

export type OpiskeluoikeusEditorProps<T extends Opiskeluoikeus> = {
  opiskeluoikeus: T
}

// Versioidun opiskeluoikeuden data on muuttumatonta (versio N ei koskaan
// muutu), joten välimuisti voi tarjoilla aiemmin katsotun version suoraan ilman
// uutta backend-kutsua. Näin versiohistoriassa edestakaisin liikkuminen ei hae
// samaa versiota uudelleen, kuten vanhassa käyttöliittymässä (Http.cachedGet).
const opiskeluoikeusVersioCache = createPreferLocalCache(fetchOpiskeluoikeus)

export const useVirkailijaUiAdapter = (oppijaModel: ObjectModel): UiAdapter => {
  const oppijaOid = modelData(oppijaModel, 'henkilö.oid')
  const oppijaFetch = useApiMethod(fetchOppija)
  const opiskeluoikeusFetch = useApiMethod(
    fetchOpiskeluoikeus,
    opiskeluoikeusVersioCache
  )

  const ooTyypit: string[] =
    modelData(oppijaModel, 'opiskeluoikeudet')?.map(
      (o: any) => o.tyyppi.koodiarvo
    ) || []

  const loadOppija = () => {
    oppijaFetch.call(oppijaOid)
  }

  // Versiohistorian valinta vaihtaa vain valitun opiskeluoikeuden versiota
  // (koko oppijaa ei ladata uudelleen): replaceOppijanOpiskeluoikeus yhdistää
  // versioidun opiskeluoikeuden oppijan tietoihin.
  //
  // Versiosta poistuttaessa versioitu haku tyhjennetään ja nykyinen
  // opiskeluoikeus haetaan uudelleen, koska se on voinut muuttua (esim. juuri
  // tehty tallennus) sivun avaamisen jälkeen — pelkkä alkuperäinen (mount-ajan)
  // data näyttäisi muuten vanhentuneen version. Sivun avaus ilman versiota ei
  // laukaise uudelleenhakua, koska loadOppija on jo hakenut nykyiset tiedot.
  const edellinenVersionumeroRef = useRef<string | undefined>(undefined)
  const loadValittuVersio = () => {
    const query = parseQuery(window.location.search)
    if (query.opiskeluoikeus && query.versionumero) {
      opiskeluoikeusFetch.call(
        query.opiskeluoikeus,
        parseInt(query.versionumero)
      )
    } else {
      opiskeluoikeusFetch.clear()
      if (edellinenVersionumeroRef.current !== undefined) {
        oppijaFetch.call(oppijaOid)
      }
    }
    edellinenVersionumeroRef.current = query.versionumero
  }

  const oppija = useMergedApiData(
    oppijaFetch,
    opiskeluoikeusFetch,
    replaceOppijanOpiskeluoikeus
  )

  return useUiAdapterImpl(ooTyypit, loadOppija, oppija, loadValittuVersio)
}

const replaceOppijanOpiskeluoikeus = (
  oppija: Oppija,
  opiskeluoikeus: Opiskeluoikeus | null
): Oppija => {
  if (!opiskeluoikeus) {
    return oppija
  }
  const oid = getOpiskeluoikeusOid(opiskeluoikeus)
  return {
    ...oppija,
    opiskeluoikeudet: oppija.opiskeluoikeudet.map((oo) =>
      getOpiskeluoikeusOid(oo) === oid ? opiskeluoikeus : oo
    )
  }
}

export const useKansalainenUiAdapter = (
  kansalainenModel: ObjectModel & Contextualized<{ suoritusjako: boolean }>
): UiAdapter => {
  const isSuoritusjako = Boolean(kansalainenModel.context.suoritusjako)
  const suoritusjakoId = isSuoritusjako
    ? last(window.location.href.split('/'))
    : undefined

  const oppija = useApiMethod(
    isSuoritusjako ? fetchSuoritusjako : fetchOmatTiedotOppija
  )

  const ooTyypit: string[] =
    modelData(kansalainenModel, 'opiskeluoikeudet')?.flatMap(
      (oppilaitos: any) =>
        oppilaitos.opiskeluoikeudet?.map((o: any) => o.tyyppi.koodiarvo) || []
    ) || []

  return useUiAdapterImpl(
    ooTyypit,
    () => oppija.call(suoritusjakoId || ''),
    oppija
  )
}

const useUiAdapterImpl = <T extends any[]>(
  opiskeluoikeustyypit: string[],
  oppijaDataNeeded: () => void,
  oppija: ApiMethodHook<Oppija, T>,
  onVersionumeroChange?: () => void
): UiAdapter => {
  const [adapter, setAdapter] = useSafeState<UiAdapter>(loadingUiAdapter)
  const versionumero = useVersionumero()

  const v2Mode = useMemo(() => {
    const hasPerusopetusFeatureFlag =
      localStorage.getItem('perusopetus-v2') !== null ||
      new URLSearchParams(window.location.search).has('perusopetus-v2')
    const v2OpiskeluoikeusTyypit = Object.keys(opiskeluoikeusEditors).filter(
      (tyyppi) => tyyppi !== 'perusopetus' || hasPerusopetusFeatureFlag
    )
    return intersects(string.Eq)(opiskeluoikeustyypit)(v2OpiskeluoikeusTyypit)
  }, [opiskeluoikeustyypit])

  useEffect(() => {
    if (v2Mode) {
      setAdapter(loadingUiAdapter)
      oppijaDataNeeded()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [v2Mode])

  // Versionumeron muuttuessa haetaan vain valittu versio (ei koko näkymää
  // uudelleen), jolloin versiohistoriassa liikkuminen ei lataa sivua uudelleen.
  useEffect(() => {
    if (v2Mode) {
      onVersionumeroChange?.()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [v2Mode, versionumero])

  useOnApiSuccess(oppija, (result) => {
    const opiskeluoikeudet = result.data.opiskeluoikeudet
    const oppijaOid = getHenkilöOid(result.data.henkilö) || ''

    setAdapter({
      isLoadingV2: false,
      getOpiskeluoikeusEditor(opiskeluoikeusModel) {
        const tyyppi = modelData(opiskeluoikeusModel, 'tyyppi.koodiarvo')
        const oid = modelData(opiskeluoikeusModel, 'oid')

        const oo = opiskeluoikeudet.find(
          (o) =>
            o.tyyppi.koodiarvo === tyyppi && getOpiskeluoikeusOid(o) === oid
        )

        const Editor: AdaptedOpiskeluoikeusEditor<any> | undefined =
          oo && opiskeluoikeusEditors[oo.tyyppi.koodiarvo]

        if (tyyppi === 'ammatillinenkoulutus') {
          const isOsittainen =
            oo?.suoritukset?.[0]?.tyyppi?.koodiarvo ===
            'ammatillinentutkintoosittainen'

          if (!isOsittainen) {
            return undefined
          }
        }

        if (tyyppi === 'perusopetus') {
          const hasFeatureFlag =
            localStorage.getItem('perusopetus-v2') !== null ||
            new URLSearchParams(window.location.search).has('perusopetus-v2')

          if (!hasFeatureFlag) {
            return undefined
          }

          const allSuorituksetSupported = oo?.suoritukset?.every(
            (s) =>
              isPerusopetuksenVuosiluokanSuoritus(s) ||
              isNuortenPerusopetuksenOppimääränSuoritus(s)
          )
          if (!allSuorituksetSupported) {
            return undefined
          }
        }

        // Palautetaan valmis elementti (ei uutta komponenttifunktiota joka
        // renderillä), jolla on versioon sidottu key. Näin React säilyttää
        // editorin re-renderöinneissä eikä kiinnitä sitä uudelleen turhaan
        // (vältetään mm. versiohistorian toistuvat haut), vaan vasta version
        // vaihtuessa — jolloin lomake alustuu uudelleen versioidulla datalla.
        //
        // Key koostuu kahdesta osasta:
        //  - p<param>: osoiterivin versionumero (tai 'cur' nykyiselle), jotta
        //    "nykyinen versio" ja "versio N" eivät koskaan törmää vaikka
        //    ladattu versionumero olisi sama. Ilman tätä tallennuksen jälkeen
        //    vanhentunut oppijaFetch (versionumero=1) ja katseltava versio 1
        //    tuottaisivat saman keyn, jolloin lomake ei alustuisi uudelleen.
        //  - d<dataVersio>: tosiasiassa ladatun datan versionumero, jotta
        //    uudelleenkiinnitys tapahtuu vasta kun haettu data on saapunut.
        const ooVer = (oo as { versionumero?: number } | undefined)
          ?.versionumero
        const versionumeroParam = new URLSearchParams(
          window.location.search
        ).get('versionumero')
        return Editor && oo ? (
          <Editor
            key={`${oid}:p${versionumeroParam ?? 'cur'}:d${ooVer ?? ''}`}
            oppijaOid={oppijaOid}
            opiskeluoikeus={oo}
            invalidatable={opiskeluoikeusModel.invalidatable}
          />
        ) : undefined
      }
    })
  })

  useOnApiError(oppija, () => {
    setAdapter({
      isLoadingV2: false,
      getOpiskeluoikeusEditor(opiskeluoikeusModel) {
        const tyyppi = modelData(opiskeluoikeusModel, 'tyyppi')?.koodiarvo
        if (Object.keys(opiskeluoikeusEditors).includes(tyyppi)) {
          return (
            <div className="error">
              {t('Näkymää ei saada ladattua. Yritä hetken päästä uudelleen.')}
            </div>
          )
        }
        return undefined
      }
    })
  })

  return v2Mode ? adapter : disabledUiAdapter
}
