import React from 'baret'
import {
  modelLookup,
  modelErrorMessages,
  pushRemoval
} from '../editor/EditorModel'
import { PropertiesEditor } from '../editor/PropertiesEditor'
import { buildClassNames } from '../components/classnames'
import {
  isValinnanMahdollisuus,
  isYhteinenTutkinnonOsa,
  isYlioppilastutkinnonKokeenSuoritus
} from '../ammatillinen/TutkinnonOsa'
import LiittyyTutkinnonOsaanEditor from '../ammatillinen/LiittyyTutkinnonOsaanEditor'
import { Suoritustaulukko } from '../suoritus/Suoritustaulukko'
import {
  isMuunAmmatillisenKoulutuksenOsasuorituksenSuoritus,
  suoritusProperties
} from '../suoritus/SuoritustaulukkoCommon'

export class TutkinnonOsanSuoritusEditor extends React.Component {
  render() {
    const {
      model,
      showScope,
      showTila,
      onExpand,
      expanded,
      groupId,
      columns,
      nestedLevel
    } = this.props
    const properties = suoritusProperties(
      model,
      !isYlioppilastutkinnonKokeenSuoritus(model)
    )
    const displayProperties = properties.filter(
      (p) => p.key !== 'osasuoritukset'
    )
    const osasuoritukset = modelLookup(model, 'osasuoritukset')
    const showOsasuoritukset =
      (osasuoritukset && osasuoritukset.value) ||
      isYhteinenTutkinnonOsa(model) ||
      isMuunAmmatillisenKoulutuksenOsasuorituksenSuoritus(model) ||
      isValinnanMahdollisuus(model)
    return (
      <tbody
        className={buildClassNames([
          'tutkinnon-osa',
          expanded && 'expanded',
          groupId
        ])}
      >
        <tr>
          {columns.map((column) =>
            column.renderData({
              model,
              showScope,
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
          <tr
            key={'error-' + i}
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
          <tr className="details" key="details">
            <td colSpan="4">
              <PropertiesEditor
                model={model}
                properties={displayProperties}
                getValueEditor={(p, getDefault) =>
                  p.key === 'liittyyTutkinnonOsaan' ? (
                    <LiittyyTutkinnonOsaanEditor model={p.model} />
                  ) : (
                    getDefault()
                  )
                }
              />
            </td>
          </tr>
        )}
        {expanded && showOsasuoritukset && (
          <tr className="osasuoritukset" key="osasuoritukset">
            <td colSpan="4">
              <Suoritustaulukko
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
