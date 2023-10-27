import React from 'baret'
import classNames from 'classnames'
import {
  modelLookup,
  modelErrorMessages,
  pushRemoval,
  modelItems,
  modelData
} from '../editor/EditorModel'
import { PropertiesEditor } from '../editor/PropertiesEditor'
import { suoritusProperties } from '../suoritus/SuoritustaulukkoCommon'
import {
  ebSuorituksenClass,
  eshSuorituksenClass
} from './europeanschoolofhelsinkiSuoritus'
import { EuropeanSchoolOfHelsinkiSuoritustaulukko } from './EuropeanSchoolOfHelsinkiSuoritustaulukko'
import { t } from '../i18n/i18n'

export class EuropeanSchoolOfHelsinkiOsasuoritusEditor extends React.Component {
  render() {
    const {
      model,
      onExpand,
      showTila,
      expanded,
      groupId,
      columns,
      nestedLevel = 0
    } = this.props

    const properties = suoritusProperties(model)
    const displayProperties = properties.filter(
      (p) => p.key !== 'osasuoritukset'
    )

    const osasuoritukset = modelLookup(model, 'osasuoritukset')

    const hasOsasuorituksia =
      (modelItems(model, 'osasuoritukset') || []).length > 0

    const showOsasuoritukset =
      (model.context.edit || hasOsasuorituksia) &&
      (model.value.classes.includes(ebSuorituksenClass.ebtutkintoOsasuoritus) ||
        model.value.classes.includes(eshSuorituksenClass.secondaryUppers7) ||
        model.value.classes.includes(eshSuorituksenClass.primaryOsasuoritus))

    const koulutusmoduuliTunniste = modelData(
      model,
      'koulutusmoduuli.tunniste.nimi'
    )

    return (
      <tbody
        className={classNames('tutkinnon-osa', groupId, { expanded })}
        data-testid={`tutkinnon-osa`}
        data-test-osasuoritus-class={model.value.classes[0]}
        data-test-expanded={!!expanded}
        data-test-nested-level={`${nestedLevel}`}
      >
        <tr data-testid={`osasuoritus-row-${t(koulutusmoduuliTunniste)}`}>
          {columns.map((column) =>
            column.renderData({
              model,
              showTila,
              onExpand,
              hasProperties: properties.length > 0 || showOsasuoritukset,
              expanded
            })
          )}
          {model.context.edit && (
            <td className="remove">
              <a
                className="remove-value"
                role="button"
                aria-label="Poista osasuoritus"
                onClick={() => pushRemoval(model)}
              />
            </td>
          )}
        </tr>
        {modelErrorMessages(model, nestedLevel !== 0).map((error, i) => (
          <tr
            key={`error-${i}`}
            className="error"
            role="error"
            aria-live="polite"
          >
            <td colSpan="42" className="error">
              {error}
            </td>
          </tr>
        ))}
        {expanded && displayProperties.length > 0 && (
          <tr
            className="details"
            key="details"
            data-testid={`osasuoritus-details-row-${t(
              koulutusmoduuliTunniste
            )}`}
          >
            <td colSpan="4">
              <PropertiesEditor model={model} properties={displayProperties} />
            </td>
          </tr>
        )}
        {expanded && showOsasuoritukset && (
          <tr
            className="osasuoritukset"
            key="osasuoritukset"
            data-testid={`osasuoritukset-row-${t(koulutusmoduuliTunniste)}`}
          >
            <td colSpan="4">
              <EuropeanSchoolOfHelsinkiSuoritustaulukko
                parentSuoritus={model}
                nestedLevel={nestedLevel}
                suorituksetModel={osasuoritukset}
              />
            </td>
          </tr>
        )}
      </tbody>
    )
  }
}
