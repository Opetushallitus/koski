import React from 'baret'
import * as L from 'partial.lenses'
import { t } from '../i18n/i18n'
import {
  contextualizeSubModel,
  lensedModel,
  modelData,
  modelLookup,
  modelProperty,
  modelSetData,
  modelSetValue,
  modelTitle,
  oneOfPrototypes,
  resolveActualModel,
  wrapOptional
} from '../editor/EditorModel'
import { hasArvosana, hasSuorituskieli, tilaText } from '../suoritus/Suoritus'
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
import { BaseContext } from '../types/EditorModelContext'
import { EshArvosanaEditor } from '../suoritus/EshArvosanaEditor'
import { fetchAlternativesBasedOnPrototypes, zeroValue } from '../editor/EnumEditor'
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

const EshKieliEditor: React.FC<any> = ({ model }) => {
  // model: koulutusmoduuli
  if (!modelProperty(model, 'kieli')) {
    return null
  }
  const resolvedKoulutusmoduuli = isOneOfModel(model)
    ? resolveActualModel(model, model.parent)
    : model

  const alternativesP = fetchAlternativesBasedOnPrototypes(
    [resolvedKoulutusmoduuli],
    'kieli'
  ).startWith([])

  const kieletP = alternativesP.map((alternatives: any) =>
    alternatives.map((m: any) => modelLookup(m, 'kieli').value)
  )

  return (
    <span className="value kieli">
      {alternativesP.map((alternatives: any) => {
        const kieliLens = L.lens<any, any>(
          (model) => {
            return modelLookup(model, 'kieli')
          },
          (value, model) => {
            const valittu = modelData(value) as any
            if (valittu) {
              const found = alternatives.find((alt: any) => {
                const altData = modelData(alt, 'kieli') as any
                return (
                  altData.koodiarvo === valittu.koodiarvo &&
                  altData.koodistoUri === valittu.koodistoUri
                )
              })
              if (found) {
                return modelSetValue(
                  model,
                  { data: valittu, value: valittu.value, title: t(valittu.nimi) },
                  'kieli'
                )
              }
            } else {
              return modelSetValue(
                model,
                zeroValue,
                'kieli'
              )
            }
          }
        )
        const kieliModel = lensedModel(model, kieliLens)
        return (
          <Editor
            key={alternatives.length}
            model={kieliModel}
            fetchAlternatives={() => kieletP}
            showEmptyOption="true"
            inline={true}
            sortBy={sortLanguages}
          />
        )
      })}
    </span>
  )
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
    const koulutusmoduuli = wrapOptional(modelLookup(model, 'koulutusmoduuli'))
    const titleAsExpandLink = false
    const kieliaine = isEshKieliaine(koulutusmoduuli)
    const koulutusmoduuliTunniste = modelData(koulutusmoduuli, 'tunniste.nimi')

    return (
      <td
        key="suoritus"
        className="suoritus"
        role="listitem"
        data-testid="suoritus-cell"
      >
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
            {kieliaine && <EshKieliEditor model={koulutusmoduuli} />}
          </span>
        )}
      </td>
    )
  }
}

const isEBOsasuoritus = (model: any) =>
  model.value.classes.includes('ebtutkinnonsuoritus')

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
      <td key="arvosana" className="arvosana" data-testid="arvosana-cell">
        <EshArvosanaEditor model={model} notFoundText={''} />
      </td>
    )
  }
}

// EshSuorituskieliColumn
export type EshSuorituskieliColumnShowProps = {
  parentSuoritus: SuoritusModel
  suoritukset: SuoritusModel[]
  context: BaseContext
  isAlaosasuoritus: boolean
}

export type EshSuorituskieliColumnDataProps = {
  model: SuoritusModel
}

export type EshSuorituskieliColumn = ColumnIface<
  EshSuorituskieliColumnDataProps,
  EshSuorituskieliColumnShowProps
>

export const EshSuorituskieliColumn: ColumnIface<
  EshSuorituskieliColumnDataProps,
  EshSuorituskieliColumnShowProps
> = {
  shouldShow: ({ parentSuoritus, suoritukset, context, isAlaosasuoritus }) => {
    const suoritusWithSuorituskieli =
      suoritukset.find(hasSuorituskieli) !== undefined
    // Alaosasuorituksilla ei ole suorituskieltä
    return (context.edit && !isAlaosasuoritus) || suoritusWithSuorituskieli
  },
  renderHeader: () => (
    <th key="suorituskieli" className="suorituskieli" scope="col">
      {/* @ts-expect-error */}
      <Text name="Suorituskieli" />
    </th>
  ),
  renderData: ({ model }) => {
    return (
      <td
        key="suorituskieli"
        className="suorituskieli"
        data-testid="suorituskieli-cell"
      >
        <Editor
          model={model}
          path="suorituskieli"
          aria-label="ESH suorituskieli"
          showEmptyOption={true}
        />
      </td>
    )
  }
}
