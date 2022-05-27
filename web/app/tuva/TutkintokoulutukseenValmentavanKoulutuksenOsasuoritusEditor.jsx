import React from 'baret'
import {modelLookup} from '../editor/EditorModel.js'
import {PropertiesEditor} from '../editor/PropertiesEditor'
import {pushRemoval} from '../editor/EditorModel'
import {suoritusProperties} from '../suoritus/SuoritustaulukkoCommon'
import {TutkintokoulutukseenValmentavanKoulutuksenSuoritustaulukko} from './TutkintokoulutukseenValmentavanKoulutuksenSuoritustaulukko'

export const TutkintokoulutukseenValmentavanKoulutuksenOsasuoritusEditor = ({
  model,
  onExpand,
  expanded,
  nestedLevel,
  columns
}) => {
  const editableProperties = suoritusProperties(model).filter(p => p.key !== 'osasuoritukset')
  const osasuoritukset = modelLookup(model, 'osasuoritukset')

  const laajuusReadOnly = model && model.value.classes.includes('tutkintokoulutukseenvalmentavankoulutuksenvalinnaisenosansuoritus')

  return (
    <tbody className={'tuva-osasuoritus tuva-osasuoritus-' + nestedLevel}>
    <tr className={'tuva-osasuoritusrivi-' + nestedLevel}>
      {
        columns.map(column => column.renderData({model, expanded, onExpand, showTila: true, hasProperties: true, disabled: laajuusReadOnly}))
      }
      {
        model.context.edit && (
          <td className="remove">
            <a className="remove-value" onClick={() => pushRemoval(model)}/>
          </td>
        )
      }
    </tr>
    {
      expanded && editableProperties.length > 0 &&
      <tr className="details" key="details">
        <td colSpan="4">
          <PropertiesEditor model={model}
                            properties={editableProperties}
          />
        </td>
      </tr>
    }
    {
      expanded &&
      <tr className="osasuoritukset">
        <td colSpan="4">
          <TutkintokoulutukseenValmentavanKoulutuksenSuoritustaulukko parentSuoritus={model}
                                                                      suorituksetModel={osasuoritukset}
                                                                      nestedLevel={nestedLevel}
          />
        </td>
      </tr>
    }
    </tbody>
  )
}
