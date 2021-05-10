import React from 'baret'
import {modelLookup} from '../editor/EditorModel.js'
import {PropertiesEditor} from '../editor/PropertiesEditor'
import {pushRemoval} from '../editor/EditorModel'
import {suoritusProperties} from '../suoritus/SuoritustaulukkoCommon'
import {VapaanSivistystyonSuoritustaulukko} from './VapaanSivistystyonSuoritustaulukko'

export const VapaanSivistystyonOsasuoritusEditor = ({
  model,
  onExpand,
  expanded,
  nestedLevel,
  columns
}) => {
  const editableProperties = suoritusProperties(model).filter(p => p.key !== 'osasuoritukset')
  const osasuoritukset = modelLookup(model, 'osasuoritukset')

  return (
    <tbody className={'vst-osasuoritus'}>
    <tr>
      {
        columns.map(column => column.renderData({model, expanded, onExpand, showTila: true, hasProperties: true}))
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
          <VapaanSivistystyonSuoritustaulukko parentSuoritus={model}
                                              suorituksetModel={osasuoritukset}
                                              nestedLevel={nestedLevel}
          />
        </td>
      </tr>
    }
    </tbody>
  )
}
