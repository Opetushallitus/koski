import React from "baret";
import { t } from "../i18n/i18n";
import {
  modelData,
  modelLookup,
  modelTitle,
} from "../editor/EditorModel";
import { tilaText } from "./Suoritus";
import Text from "../i18n/Text";
import { Editor } from "../editor/Editor";
import { sortLanguages } from "../util/sorting";
import { suorituksenTilaSymbol } from "./Suoritustaulukko";
import { isEshKieliaine } from "./Koulutusmoduuli";
import { ColumnIface, OnExpandFn, SuoritusModel } from "./SuoritustaulukkoCommon";

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

    return (
      <td key="suoritus" className="suoritus">
        <a
          className={hasProperties ? 'toggle-expand' : 'toggle-expand disabled'}
          onClick={() => onExpand(!expanded)}
        >
          {expanded ? <>&#61766;</> : <>&#61694;</>}
        </a>
        {showTila && (
          <span className="tila" title={tilaText(model)}>
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
          <span className="nimi">
            {t(modelData(koulutusmoduuli, 'tunniste.nimi')) +
              (kieliaine ? ', ' : '')}
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
