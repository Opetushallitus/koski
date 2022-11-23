import React from 'baret'
import { t } from '../i18n/i18n'
import { modelData, modelLookup, modelTitle } from '../editor/EditorModel'
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

const isEBOsasuoritus = (model: any) =>
  model.value.classes.includes('ebtutkinnonsuoritus')

/*
const isEBAlaosasuoritus = (model: any) =>
  model.value.classes.includes('ebtutkinnonosasuoritus')
const isS7 = (model: any): boolean =>
  model.value.classes.includes('secondaryupperoppiaineensuorituss7')
const isS6 = (tunniste: any, model: any): boolean =>
  tunniste === 'S6' &&
  model.value.classes.includes('secondaryuppervuosiluokansuoritus')
const isS5OrS4 = (tunniste: any, model: any): boolean =>
  (tunniste === 'S5' || tunniste === 'S4') &&
  model.value.classes.includes('secondarylowervuosiluokansuoritus')
const isS1OrS2OrS3 = (tunniste: any, _model: any): boolean =>
  tunniste === 'S1' || tunniste === 'S2' || tunniste === 'S3'


function resolveKoodisto(model: any, isAlaosasuoritus: boolean): string {
  // Päätason suorituksen koulutusmoduuli
  const koulutusmoduuli = modelLookup(model.context.suoritus, 'koulutusmoduuli')
  // Luokka-aste
  const luokkaAste = modelData(koulutusmoduuli, 'tunniste.koodiarvo')
  const koodistot = {
    secondaryS7preliminarymarkarviointi: isAlaosasuoritus && isS7(model), // S7 alaosasuorituksissa käytössä secondaryS7preliminarymarkarviointi
    ebtutkintopreliminarymarkarviointi:
      isEBAlaosasuoritus(model) &&
      koulutusmoduuli?.value.classes.includes(
        'eboppiainekomponenttipreliminary'
      ),
    secondarynumericalmarkarviointi:
      isS5OrS4(luokkaAste, model) || isS6(luokkaAste, model),
    secondarygradearviointi: isS1OrS2OrS3(luokkaAste, model),
    ebtutkintofinalmarkarviointi:
      isAlaosasuoritus &&
      isEBAlaosasuoritus(model) &&
      !koulutusmoduuli?.value.classes.includes(
        'eboppiainekomponenttipreliminary'
      )
  }
  const resolvedArviointiArray = Object.entries(koodistot)
    .filter(([_key, val]) => val)
    .map(([key, _val]) => key)
  if (resolvedArviointiArray.length !== 1) {
    throw new Error('Could not resolve correct koodisto')
  }
  return resolvedArviointiArray[0]
}
*/

export const EshArvosanaColumn: ColumnIface<
  EshArvosanaColumnDataProps,
  EshArvosanaColumnShowProps
> = {
  shouldShow: ({ parentSuoritus, suoritukset, context }) =>
    !isEshS7(parentSuoritus) &&
    !isEBOsasuoritus(parentSuoritus) &&
    (context.edit || suoritukset.find(hasArvosana) !== undefined),
  renderHeader: () => (
    <th key="arvosana" className="arvosana" scope="col">
      {/* @ts-expect-error */}
      <Text name="Arvosana" />
    </th>
  ),
  renderData: ({ model }) => {
    return (
      <td key="arvosana" className={'arvosana'}>
        <ArvosanaEditor model={model} notFoundText={''} />
      </td>
    )
  }
}
