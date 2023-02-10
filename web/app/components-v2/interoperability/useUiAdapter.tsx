import * as string from 'fp-ts/string'
import React, { useEffect, useMemo } from 'react'
import {
  ApiMethodHook,
  useApiMethod,
  useOnApiSuccess,
  useSafeState
} from '../../api-fetch'
import { modelData } from '../../editor/EditorModel'
import { Contextualized } from '../../types/EditorModelContext'
import { ObjectModel } from '../../types/EditorModels'
import { Opiskeluoikeus } from '../../types/fi/oph/koski/schema/Opiskeluoikeus'
import { Oppija } from '../../types/fi/oph/koski/schema/Oppija'
import { intersects, last } from '../../util/fp/arrays'
import { getHenkilöOid } from '../../util/henkilo'
import {
  fetchOmatTiedotOppija,
  fetchOppija,
  fetchSuoritusjako
} from '../../util/koskiApi'
import { getOpiskeluoikeusOid } from '../../util/opiskeluoikeus'
import { OpiskeluoikeudenTyyppiOf } from '../../util/types'
import { opiskeluoikeusEditors } from './uiAdapters'

export type AdaptedOpiskeluoikeusEditor<T extends Opiskeluoikeus> = (props: {
  oppijaOid: string
  opiskeluoikeus: T
}) => React.ReactElement

export type AdaptedOpiskeluoikeusEditorCollection = Partial<{
  [OO in Opiskeluoikeus as OpiskeluoikeudenTyyppiOf<OO>]: AdaptedOpiskeluoikeusEditor<OO>
}>

export type UiAdapter = {
  isLoadingV2: boolean

  getOpiskeluoikeusEditor: (
    opiskeluoikeusModel: ObjectModel
  ) => AdapterComponent | undefined
}

const loadingUiAdapter: UiAdapter = {
  isLoadingV2: true,
  getOpiskeluoikeusEditor: () => undefined
}

const disabledUiAdapter: UiAdapter = {
  isLoadingV2: false,
  getOpiskeluoikeusEditor: () => undefined
}

export type AdapterComponent = () => React.ReactElement

export type OpiskeluoikeusEditorProps<T extends Opiskeluoikeus> = {
  opiskeluoikeus: T
}

export const useVirkailijaUiAdapter = (oppijaModel: ObjectModel): UiAdapter => {
  const oppijaOid = modelData(oppijaModel, 'henkilö.oid')
  const oppija = useApiMethod(fetchOppija)

  const ooTyypit: string[] =
    modelData(oppijaModel, 'opiskeluoikeudet')?.map(
      (o: any) => o.tyyppi.koodiarvo
    ) || []

  const fetchData = () => {
    oppija.call(oppijaOid)
  }

  return useUiAdapterImpl(ooTyypit, fetchData, oppija)
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
    () => {
      oppija.call(suoritusjakoId || 'xxx')
    },
    oppija
  )
}

const useUiAdapterImpl = <T extends any[]>(
  opiskeluoikeustyypit: string[],
  oppijaDataNeeded: () => void,
  oppija: ApiMethodHook<Oppija, T>
): UiAdapter => {
  const [adapter, setAdapter] = useSafeState<UiAdapter>(loadingUiAdapter)

  const v2Mode = useMemo(() => {
    const v2OpiskeluoikeusTyypit = Object.keys(opiskeluoikeusEditors)
    return intersects(string.Eq)(opiskeluoikeustyypit)(v2OpiskeluoikeusTyypit)
  }, [opiskeluoikeustyypit])

  useEffect(() => {
    if (v2Mode) {
      setAdapter(loadingUiAdapter)
      oppijaDataNeeded()
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [v2Mode])

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

        return Editor
          ? () => <Editor oppijaOid={oppijaOid} opiskeluoikeus={oo} />
          : undefined
      }
    })
  })

  return v2Mode ? adapter : disabledUiAdapter
}
