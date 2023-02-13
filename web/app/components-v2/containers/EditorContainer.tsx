import React, { useCallback, useMemo, useState } from 'react'
import { localize, t } from '../../i18n/i18n'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { Opiskeluoikeus } from '../../types/fi/oph/koski/schema/Opiskeluoikeus'
import { Suoritus } from '../../types/fi/oph/koski/schema/Suoritus'
import { saveOpiskeluoikeus } from '../../util/koskiApi'
import {
  mergeOpiskeluoikeusVersionumero,
  PäätasonSuoritusOf
} from '../../util/opiskeluoikeus'
import { päätasonSuoritusPath } from '../../util/optics'
import { OpiskeluoikeusjaksoOf } from '../../util/schema'
import { ItemOf } from '../../util/types'
import { common, CommonPropsWithChildren } from '../CommonProps'
import { Tabs } from '../controls/Tabs'
import { FormField } from '../forms/FormField'
import { FormModel, FormOptic, getValue } from '../forms/FormModel'
import { Spacer } from '../layout/Spacer'
import { Snackbar } from '../messages/Snackbar'
import { EditBar } from '../opiskeluoikeus/EditBar'
import {
  OpiskeluoikeudenTilaEdit,
  OpiskeluoikeudenTilaView
} from '../opiskeluoikeus/OpiskeluoikeudenTila'
import { OpiskeluoikeusEditToolbar } from '../opiskeluoikeus/OpiskeluoikeusEditToolbar'
import { UusiOpiskeluoikeusjakso } from '../opiskeluoikeus/UusiOpiskeluoikeudenTilaModal'
import { Trans } from '../texts/Trans'

export type EditorContainerProps<T extends Opiskeluoikeus> =
  CommonPropsWithChildren<{
    form: FormModel<T>
    oppijaOid: string
    invalidatable: boolean
    onChangeSuoritus: (suoritusIndex: number) => void
    createOpiskeluoikeusjakso: (
      seed: UusiOpiskeluoikeusjakso<OpiskeluoikeusjaksoOf<T['tila']>>
    ) => void
    suorituksenNimi?: (suoritus: ItemOf<T['suoritukset']>) => LocalizedString
  }>

export type ActivePäätasonSuoritus<T extends Opiskeluoikeus> = {
  index: number
  path: FormOptic<T, PäätasonSuoritusOf<T>>
  suoritus: PäätasonSuoritusOf<T>
}

export const EditorContainer = <T extends Opiskeluoikeus>(
  props: EditorContainerProps<T>
) => {
  const opiskeluoikeudenTilaPath = useMemo(
    () => props.form.root.prop('tila'),
    [props.form]
  )

  const onSave = useCallback(() => {
    props.form.save(
      saveOpiskeluoikeus(props.oppijaOid),
      mergeOpiskeluoikeusVersionumero
    )
  }, [props.form, props.oppijaOid])

  const suorituksetVahvistettu = props.form.state.suoritukset
    .map((s) => Boolean(s.vahvistus))
    .every((s) => s)

  const [suoritusIndex, setSuoritusIndex] = useState(0)
  const changeSuoritusTab = useCallback(
    (index: number) => {
      props.onChangeSuoritus(index)
      setSuoritusIndex(index)
    },
    [props]
  )

  return (
    <article {...common(props, ['EditorContainer'])}>
      <OpiskeluoikeusEditToolbar
        opiskeluoikeus={props.form.state}
        editMode={props.form.editMode}
        invalidatable={props.invalidatable}
        onStartEdit={props.form.startEdit}
      />

      <Spacer />

      <FormField
        form={props.form}
        path={opiskeluoikeudenTilaPath}
        view={OpiskeluoikeudenTilaView}
        edit={OpiskeluoikeudenTilaEdit}
        editProps={{
          enableValmistuminen: suorituksetVahvistettu,
          createJakso: props.createOpiskeluoikeusjakso
        }}
      />
      <Spacer />

      <h2>
        <Trans>{'Suoritukset'}</Trans>
      </h2>

      <Tabs
        onSelect={changeSuoritusTab}
        tabs={props.form.state.suoritukset.map((s, i) => ({
          key: i,
          label: props.suorituksenNimi?.(s) || defaultSuorituksenNimi(s)
        }))}
      />

      <Spacer />

      <div key={suoritusIndex}>{props.children}</div>

      <EditBar form={props.form} onSave={onSave} />
      {props.form.isSaved && <Snackbar>{'Tallennettu'}</Snackbar>}
    </article>
  )
}

export const usePäätasonSuoritus = <T extends Opiskeluoikeus>(
  form: FormModel<T>
): [ActivePäätasonSuoritus<T>, (suoritusIndex: number) => void] => {
  const [index, setIndex] = useState(0)
  const state = useMemo(
    () => ({
      index,
      suoritus: form.state.suoritukset[index],
      path: päätasonSuoritusPath(index)
    }),
    [form.state.suoritukset, index]
  )
  return [state, setIndex]
}

const defaultSuorituksenNimi = (s: Suoritus): LocalizedString =>
  s.tyyppi.lyhytNimi || s.tyyppi.nimi || localize(s.tyyppi.koodiarvo)
