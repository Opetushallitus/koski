import React from 'baret'
import { t } from '../i18n/i18n'
import {
  contextualizeModel,
  contextualizeSubModel,
  modelData,
  modelItems,
  modelLookup,
  modelProperties,
  modelSet,
  modelTitle,
  pushModel,
  resolveActualModel,
  resolvePrototypeReference,
  wrapOptional
} from '../editor/EditorModel'
import { hasArvosana, tilaText } from '../suoritus/Suoritus'
import Text from '../i18n/Text'
import { Editor } from '../editor/Editor'
import { sortLanguages } from '../util/sorting'
import { suorituksenTilaSymbol } from '../suoritus/Suoritustaulukko'
import { isEshKieliaine } from '../suoritus/Koulutusmoduuli'
import {
  ColumnIface,
  isEB,
  isEshS7,
  OnExpandFn,
  SuoritusModel
} from '../suoritus/SuoritustaulukkoCommon'
import classNames from 'classnames'
import { eshSuoritus } from './europeanschoolofhelsinkiSuoritus'
import { BaseContext } from '../types/EditorModelContext'
import { SynteettinenArvosanaEditor } from '../suoritus/SynteettinenArvosanaEditor'
import { ArvosanaEditor } from '../suoritus/ArvosanaEditor'
import { isOneOfModel } from '../types/EditorModels'

// ESHSuoritusColumn

export type ESHSuoritusColumn = ColumnIface<
  ESHSuoritusColumnDataProps,
  {},
  ESHSuoritusColumnHeaderProps
>

export type ESHSuoritusColumnHeaderProps = {
  suoritusTitle: string
}

export type ESHSuoritusColumnDataProps = {
  model: SuoritusModel
  showTila: boolean
  onExpand: OnExpandFn
  hasProperties: boolean
  expanded: boolean
}

export const EshSuoritusColumn: ESHSuoritusColumn = {
  shouldShow: () => true,
  renderHeader: ({ suoritusTitle }) => (
    <td key="suoritus" className="suoritus">
      {/* @ts-expect-error */}
      <Text name={suoritusTitle} />
    </td>
  ),
  renderData: ({ model, showTila, onExpand, hasProperties, expanded }) => {
    const koulutusmoduuli = modelLookup(model, 'koulutusmoduuli')
    const titleAsExpandLink = false
    const kieliaine = isEshKieliaine(koulutusmoduuli)
    const koulutusmoduuliTunniste = modelData(koulutusmoduuli, 'tunniste.nimi')

    return (
      <td key="suoritus" className="suoritus" role="listitem">
        <a
          className={hasProperties ? 'toggle-expand' : 'toggle-expand disabled'}
          onClick={() => onExpand(!expanded)}
          role="button"
          aria-expanded={expanded}
          aria-label={
            expanded
              ? `Pienennä suoritus ${t(koulutusmoduuliTunniste)}`
              : `Laajenna suoritus ${t(koulutusmoduuliTunniste)}`
          }
        >
          {expanded ? <>&#61766;</> : <>&#61694;</>}
        </a>
        {showTila && (
          <span
            className="tila"
            role="status"
            aria-label={tilaText(model)}
            title={tilaText(model)}
          >
            {suorituksenTilaSymbol(model)}
          </span>
        )}
        {titleAsExpandLink ? (
          <button
            className="nimi inline-link-button"
            onClick={() => onExpand(!expanded)}
          >
            {modelTitle(model, 'koulutusmoduuli')}
          </button>
        ) : (
          <span
            className="nimi"
            aria-label={
              t(koulutusmoduuliTunniste) + (kieliaine ? ', kieliaine' : '')
            }
          >
            {t(koulutusmoduuliTunniste) + (kieliaine ? ', ' : '')}
            {kieliaine && (
              <span className="value kieli">
                <Editor
                  model={koulutusmoduuli}
                  inline={true}
                  showEmptyOption={true}
                  path="kieli"
                  sortBy={sortLanguages}
                />
              </span>
            )}
          </span>
        )}
      </td>
    )
  }
}

// EshArvosanaColumn
export type EshArvosanaColumnShowProps = {
  parentSuoritus: SuoritusModel
  suoritukset: SuoritusModel[]
  context: BaseContext
}

export type EshArvosanaColumnDataProps = {
  model: SuoritusModel
}

export type EshArvosanaColumn = ColumnIface<
  EshArvosanaColumnDataProps,
  EshArvosanaColumnShowProps
>

// @ts-expect-error
function resolveArrayPrototype(model) {
  if (model.arrayPrototype !== undefined) {
    return model.arrayPrototype
  }
  if (
    model.optionalPrototype !== undefined &&
    model.optionalPrototype.arrayPrototype !== undefined
  ) {
    return model.optionalPrototype.arrayPrototype
  }
}

const isEB = (model) => model.value.classes.includes('ebtutkinnonosasuoritus')
const isS7 = (model) =>
  model.value.classes.includes('secondaryupperoppiaineensuorituss7')
const isS6 = (tunniste, model) =>
  tunniste === 'S6' &&
  model.value.classes.includes('secondaryuppervuosiluokansuoritus')
const isS5OrS4 = (tunniste, model) =>
  (tunniste === 'S5' || tunniste === 'S4') &&
  model.value.classes.includes('secondarylowervuosiluokansuoritus')
const isS1OrS2OrS3 = (tunniste, _model) =>
  tunniste === 'S1' || tunniste === 'S2' || tunniste === 'S3'

export const EshArvosanaColumn: ColumnIface<
  EshArvosanaColumnDataProps,
  EshArvosanaColumnShowProps
> = {
  shouldShow: ({ parentSuoritus, suoritukset, context }) =>
    !isEshS7(parentSuoritus) &&
    !isEB(parentSuoritus) &&
    (context.edit || suoritukset.find(hasArvosana) !== undefined),
  renderHeader: () => (
    <th key="arvosana" className="arvosana" scope="col">
      {/* @ts-expect-error */}
      <Text name="Arvosana" />
    </th>
  ),
  renderData: ({ model }) => {
    const arviointi = modelLookup(model, 'arviointi.-1')
    const arviointiModel = modelLookup(model, 'arviointi')
    const arviointiContextualizedModel = contextualizeSubModel(
      resolveArrayPrototype(arviointiModel),
      arviointiModel,
      modelItems(arviointiModel).length
    )

    const isAlaosasuoritus = model.value.classes.includes('foo') // TODO
    const luokkaAste = modelData(
      model.parent,
      'koulutusmoduuli.tunniste.koodiarvo'
    )

    const protoKey =
      isAlaosasuoritus && isS7(model)
        ? 'secondarys7preliminarymarkarviointi'
        : isAlaosasuoritus &&
          isEB(model) &&
          // @ts-expect-error
          modelLookup(model, 'koulutusmoduuli').value.classes.includes(
            'eboppiainekomponenttipreliminary'
          )
        ? 'secondarys7preliminarymarkarviointi'
        : isAlaosasuoritus &&
          isEB(model) &&
          // @ts-expect-error
          !modelLookup(model, 'koulutusmoduuli').value.classes.includes(
            'eboppiainekomponenttipreliminary'
          )
        ? 'ebtutkintofinalmarkarviointi'
        : isS5OrS4(luokkaAste, model) || isS6(luokkaAste, model)
        ? 'secondarynumericalmarkarviointi'
        : isS1OrS2OrS3(luokkaAste, model)
        ? 'secondarygradearviointi'
        : undefined

    const proto = {
      type: 'prototype',
      key: protoKey
    }

    const resolvedArviointiModel = contextualizeSubModel(
      resolvePrototypeReference(proto, arviointiContextualizedModel.context),
      arviointiContextualizedModel
    )

    const actualModel =
      modelData(arviointi, 'arvosana') === undefined &&
      arviointiContextualizedModel !== undefined &&
      isOneOfModel(arviointiContextualizedModel)
        ? modelSet(
            model,
            resolveActualModel(arviointiContextualizedModel, model),
            'arviointi.-1'
          )
        : model

    console.log('model', model)
    console.log('actualModel', actualModel)

    const arvosana = arviointi ? modelLookup(arviointi, 'arvosana') : null
    const isSynthetic = (arvosana?.value?.classes || []).includes(
      'synteettinenkoodiviite'
    )
    return (
      <td
        key="arvosana"
        className={classNames('arvosana', {
          synthetic: isSynthetic
        })}
      >
        {isSynthetic ? (
          <SynteettinenArvosanaEditor model={actualModel} notFoundText={''} />
        ) : (
          <ArvosanaEditor model={actualModel} notFoundText={''} />
        )}
      </td>
    )
  }
}
