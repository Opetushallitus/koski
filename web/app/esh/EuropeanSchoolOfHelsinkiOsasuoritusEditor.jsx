import React from 'baret'
import classNames from 'classnames'
import {
  modelLookup,
  modelErrorMessages,
  pushRemoval
} from '../editor/EditorModel'
import { PropertiesEditor } from '../editor/PropertiesEditor'
import { suoritusProperties } from '../suoritus/SuoritustaulukkoCommon'
import { EuropeanSchoolOfHelsinkiSuoritustaulukko } from '../suoritus/EuropeanSchoolOfHelsinkiSuoritustaulukko'

export class EuropeanSchoolOfHelsinkiOsasuoritusEditor extends React.Component {
  render() {
    const {
      model,
      onExpand,
      showTila,
      expanded,
      groupId,
      columns,
      nestedLevel
    } = this.props
    const properties = suoritusProperties(model)
    const displayProperties = properties.filter(
      (p) => p.key !== 'osasuoritukset'
    )

    const osasuoritukset = modelLookup(model, 'osasuoritukset')
    const showOsasuoritukset = osasuoritukset
      ? osasuoritukset.value !== undefined
      : false

    return (
      <tbody className={classNames('tutkinnon-osa', groupId, { expanded })}>
        <tr>
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
              <a className="remove-value" onClick={() => pushRemoval(model)} />
            </td>
          )}
        </tr>
        {modelErrorMessages(model).map((error, i) => (
          <tr key={`error-${i}`} className="error">
            <td colSpan="42" className="error">
              {error}
            </td>
          </tr>
        ))}
        {expanded && displayProperties.length > 0 && (
          <tr className="details" key="details">
            <td colSpan="4">
              <PropertiesEditor model={model} properties={displayProperties} />
            </td>
          </tr>
        )}
        {expanded && showOsasuoritukset && (
          <tr className="osasuoritukset" key="osasuoritukset">
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
