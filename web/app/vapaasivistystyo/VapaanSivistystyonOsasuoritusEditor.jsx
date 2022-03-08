import React from 'baret'
import * as R from 'ramda'

import {modelData, modelLookup} from '../editor/EditorModel.js'
import {PropertiesEditor} from '../editor/PropertiesEditor'
import {pushRemoval} from '../editor/EditorModel'
import {suoritusProperties} from '../suoritus/SuoritustaulukkoCommon'
import {VapaanSivistystyonSuoritustaulukko} from './VapaanSivistystyonSuoritustaulukko'
import {saveOrganizationalPreference} from '../virkailija/organizationalPreferences'
import {doActionWhileMounted} from '../util/util'

export class VapaanSivistystyonOsasuoritusEditor extends React.Component {
  saveChangedPreferences() {
    if (!this.state || !this.state.changed) return null

    let { model } = this.props

    const koulutusmoduuliData = modelData(model).koulutusmoduuli
    const organisaatioOid = modelData(model.context.toimipiste).oid
    const key = koulutusmoduuliData.tunniste.koodiarvo
    const moduulinTyyppi = modelLookup(model, 'koulutusmoduuli').value.classes[0]

    if (modelLookup(model, 'koulutusmoduuli').value.classes.includes('paikallinenkoulutusmoduuli')) {
      saveOrganizationalPreference(
        organisaatioOid,
        moduulinTyyppi,
        key,
        koulutusmoduuliData
      )
    }
  }

  render() {
    let { model, onExpand, expanded, nestedLevel, columns } = this.props

    const editableProperties = suoritusProperties(model).filter(p => p.key !== 'osasuoritukset')
    const osasuoritukset = modelLookup(model, 'osasuoritukset')

    return (<tbody className={'vst-osasuoritus'}>
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
    {
      model.context.edit && doActionWhileMounted(model.context.saveChangesBus, () => {
        this.saveChangedPreferences()
      })
    }
    </tbody>)
  }

  UNSAFE_componentWillReceiveProps(nextProps) {
    const currentData = modelData(this.props.model)
    const newData = modelData(nextProps.model)

    if (!R.equals(currentData, newData)) this.setState({changed: true})
  }
}
