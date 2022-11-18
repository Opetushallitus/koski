import React from 'baret'
import { t } from '../i18n/i18n'
import {
  modelData,
  modelLookup,
  modelProperties,
  modelTitle
} from '../editor/EditorModel'
import { hasArvosana, tilaText } from '../suoritus/Suoritus'
import Text from '../i18n/Text'
import { Editor } from '../editor/Editor'
import { sortLanguages } from '../util/sorting'
import { suorituksenTilaSymbol } from '../suoritus/Suoritustaulukko'
import { isEshKieliaine } from '../suoritus/Koulutusmoduuli'
import {
  ColumnIface,
  isEshS7,
  OnExpandFn,
  SuoritusModel
} from '../suoritus/SuoritustaulukkoCommon'
import classNames from 'classnames'
import { eshSuoritus } from './europeanschoolofhelsinkiSuoritus'
import { BaseContext } from '../types/EditorModelContext'
import { SynteettinenArvosanaEditor } from '../suoritus/SynteettinenArvosanaEditor'
import { ArvosanaEditor } from '../suoritus/ArvosanaEditor'

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

export const EshArvosanaColumn: ColumnIface<
  EshArvosanaColumnDataProps,
  EshArvosanaColumnShowProps
> = {
  shouldShow: ({ parentSuoritus, suoritukset, context }) =>
    !isEshS7(parentSuoritus) &&
    (context.edit || suoritukset.find(hasArvosana) !== undefined),
  renderHeader: () => (
    <th key="arvosana" className="arvosana" scope="col">
      {/* @ts-expect-error */}
      <Text name="Arvosana" />
    </th>
  ),
  renderData: ({ model }) => {
    const arviointi = modelLookup(model, 'arviointi.-1')
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
          <SynteettinenArvosanaEditor model={model} notFoundText={''} />
        ) : (
          <ArvosanaEditor model={model} notFoundText={''} />
        )}
      </td>
    )
  }
}
