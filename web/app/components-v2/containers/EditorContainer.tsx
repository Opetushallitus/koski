import { NonEmptyArray } from 'fp-ts/lib/NonEmptyArray'
import React, { useCallback, useMemo, useState } from 'react'
import { TestIdLayer, TestIdRoot } from '../../appstate/useTestId'
import { localize, t } from '../../i18n/i18n'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { Opiskeluoikeus } from '../../types/fi/oph/koski/schema/Opiskeluoikeus'
import { Suoritus } from '../../types/fi/oph/koski/schema/Suoritus'
import { saveOpiskeluoikeus } from '../../util/koskiApi'
import {
  PäätasonSuoritusOf,
  mergeOpiskeluoikeusVersionumero
} from '../../util/opiskeluoikeus'
import { päätasonSuoritusPath } from '../../util/optics'
import { OpiskeluoikeusjaksoOf } from '../../util/schema'
import { ClassOf, ItemOf } from '../../util/types'
import { useConfirmUnload } from '../../util/useConfirmUnload'
import { CommonPropsWithChildren, common } from '../CommonProps'
import { FlatButton } from '../controls/FlatButton'
import { Tab, Tabs } from '../controls/Tabs'
import { FormField } from '../forms/FormField'
import { FormModel, FormOptic } from '../forms/FormModel'
import { ValidationError } from '../forms/validator'
import { Spacer } from '../layout/Spacer'
import { Snackbar } from '../messages/Snackbar'
import { EditBar } from '../opiskeluoikeus/EditBar'
import {
  OpiskeluoikeudenTilaEdit,
  OpiskeluoikeudenTilaView
} from '../opiskeluoikeus/OpiskeluoikeudenTila'
import { OpiskeluoikeusEditToolbar } from '../opiskeluoikeus/OpiskeluoikeusEditToolbar'
import { OrganisaatiohistoriaView } from '../opiskeluoikeus/OrganisaatiohistoriaView'
import { UusiOpiskeluoikeusjakso } from '../opiskeluoikeus/UusiOpiskeluoikeudenTilaModal'
import { CHARCODE_ADD, Icon } from '../texts/Icon'
import { Trans } from '../texts/Trans'

export type EditorContainerProps<T extends Opiskeluoikeus> =
  CommonPropsWithChildren<{
    form: FormModel<T>
    oppijaOid: string
    invalidatable: boolean
    onChangeSuoritus: (suoritusIndex: number) => void
    createOpiskeluoikeusjakso: (
      seed: UusiOpiskeluoikeusjakso<OpiskeluoikeusjaksoOf<T['tila']>>
    ) => OpiskeluoikeusjaksoOf<T['tila']> | NonEmptyArray<ValidationError>
    suorituksenNimi?: (suoritus: ItemOf<T['suoritukset']>) => LocalizedString
    /**
     * Jos opiskeluoikeuden tila voi olla useampaa kuin yhtä tyyppiä, tätä funktiota käytetään oikeanlaisen luokan määrittämiseen.
     */
    opiskeluoikeusJaksoClassName?: ClassOf<OpiskeluoikeusjaksoOf<T['tila']>>
    suorituksenLisäys?: string | LocalizedString
    onCreateSuoritus?: () => void
    suorituksetVahvistettu?: boolean
    lisätiedotContainer?: React.FC<any>
  }>

export type ActivePäätasonSuoritus<
  T extends Opiskeluoikeus,
  S extends PäätasonSuoritusOf<T> = PäätasonSuoritusOf<T>
> = {
  index: number
  path: FormOptic<T, S>
  suoritus: S
  testId: string
}

export const EditorContainer = <T extends Opiskeluoikeus>(
  props: EditorContainerProps<T>
) => {
  useConfirmUnload(props.form.editMode && props.form.hasChanged)

  const opiskeluoikeudenTilaPath = useMemo(
    () => props.form.root.prop('tila'),
    [props.form]
  )

  const opiskeluoikeudenOrganisaatiohistoriaPath = useMemo(
    // @ts-expect-error
    () => props.form.root.prop('organisaatiohistoria').optional(),
    [props.form]
  )

  const [lisatiedotOpen, setLisatiedotOpen] = useState(false)
  const onSave = useCallback(() => {
    props.form.save(
      saveOpiskeluoikeus(props.oppijaOid),
      mergeOpiskeluoikeusVersionumero
    )
  }, [props.form, props.oppijaOid])

  const suorituksetVahvistettu =
    props.suorituksetVahvistettu !== undefined
      ? props.suorituksetVahvistettu
      : props.form.state.suoritukset
          .map((s) => Boolean(s.vahvistus))
          .every((s) => s)

  const [suoritusIndex, setSuoritusIndex] = useState(0)
  const changeSuoritusTab = useCallback(
    (index: number) => {
      if (index < 0) {
        props.onCreateSuoritus?.()
      } else {
        props.onChangeSuoritus(index)
        setSuoritusIndex(index)
      }
    },
    [props]
  )

  const suoritusTabs: Tab<number>[] = useMemo(
    () => [
      ...props.form.state.suoritukset.map((s, i) => ({
        key: i,
        label: props.suorituksenNimi?.(s) || defaultSuorituksenNimi(s),
        testId: `suoritukset.${i}.tab`
      })),
      ...(props.suorituksenLisäys &&
      props.form.editMode &&
      props.onCreateSuoritus
        ? [
            {
              key: -1,
              label: props.suorituksenLisäys,
              display: (
                <>
                  <Icon
                    charCode={CHARCODE_ADD}
                    className="EditorContainer__addIcon"
                  />{' '}
                  {t(props.suorituksenLisäys)}
                </>
              ),
              testId: `suoritukset.addNew`
            }
          ]
        : [])
    ],
    [props]
  )

  const { lisätiedotContainer: LisätiedotContainer } = props

  const hasLisätiedot = (state: undefined | { $class: string }): Boolean => {
    if (state === undefined) {
      return false
    }
    const { $class, ...otherProps } = state
    return Object.keys(otherProps).length > 0
  }

  return (
    <article {...common(props, ['EditorContainer'])}>
      <TestIdRoot id="opiskeluoikeus">
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
            createJakso: props.createOpiskeluoikeusjakso,
            opiskeluoikeusJaksoClassName: props.opiskeluoikeusJaksoClassName
          }}
        />
        <Spacer />
        <FormField
          form={props.form}
          path={opiskeluoikeudenOrganisaatiohistoriaPath}
          view={OrganisaatiohistoriaView}
        />
        <Spacer />
        {LisätiedotContainer !== undefined &&
          (props.form.editMode ||
            ('lisätiedot' in props.form.state &&
              hasLisätiedot(props.form.state.lisätiedot))) && (
            <>
              <FlatButton
                onClick={(e) => {
                  e.preventDefault()
                  setLisatiedotOpen((prev) => !prev)
                }}
              >
                {lisatiedotOpen
                  ? t('lisatiedot:sulje_lisatiedot')
                  : t('lisatiedot:nayta_lisatiedot')}
              </FlatButton>
              {lisatiedotOpen && <LisätiedotContainer form={props.form} />}
              <Spacer />
            </>
          )}
      </TestIdRoot>

      <h2>
        <Trans>{'Suoritukset'}</Trans>
      </h2>

      <TestIdRoot id="suoritusTabs">
        <Tabs
          tabs={suoritusTabs}
          active={suoritusIndex}
          onSelect={changeSuoritusTab}
        />
      </TestIdRoot>

      <Spacer />

      <div key={suoritusIndex}>{props.children}</div>

      <TestIdRoot id="opiskeluoikeus">
        <EditBar form={props.form} onSave={onSave} />
        {props.form.isSaved && (
          <TestIdLayer id="saved">
            <Snackbar>{t('Tallennettu')}</Snackbar>
          </TestIdLayer>
        )}
      </TestIdRoot>
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
      path: päätasonSuoritusPath(index),
      testId: `suoritukset.${index}`
    }),
    [form.state.suoritukset, index]
  )
  return [state, setIndex]
}

const defaultSuorituksenNimi = (s: Suoritus): LocalizedString =>
  s.tyyppi.lyhytNimi || s.tyyppi.nimi || localize(s.tyyppi.koodiarvo)

export const hasPäätasonsuoritusOf = <
  T extends Opiskeluoikeus,
  S extends PäätasonSuoritusOf<T>
>(
  guard: (s: any) => s is S,
  pts: ActivePäätasonSuoritus<T>
  // @ts-ignore
): pts is ActivePäätasonSuoritus<T, S> => guard(pts.suoritus)
