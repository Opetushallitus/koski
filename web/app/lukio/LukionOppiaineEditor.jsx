import React from 'baret'
import * as R from 'ramda'

import {modelData, modelItems, modelLookup, pushRemoval} from '../editor/EditorModel.js'
import {suorituksenTilaSymbol} from '../suoritus/Suoritustaulukko'
import {KurssitEditor} from '../kurssi/KurssitEditor'
import {tilaText} from '../suoritus/Suoritus'
import {isPaikallinen} from '../suoritus/Koulutusmoduuli'
import {saveOrganizationalPreference} from '../virkailija/organizationalPreferences'
import {paikallinenOppiainePrototype} from '../perusopetus/PerusopetuksenOppiaineEditor'
import {doActionWhileMounted} from '../util/util'
import {createOppiaineenSuoritus, suoritetutKurssit, laajuudet} from './lukio'
import {Arviointi, KoulutusmoduuliPropertiesEditor, Nimi} from './fragments/LukionOppiaine'
import {laajuusNumberToString} from '../util/format'

export class LukionOppiaineEditor extends React.Component {
  saveChangedPreferences() {
    const {oppiaine} = this.props

    const data = modelData(oppiaine, 'koulutusmoduuli')
    const organisaatioOid = modelData(oppiaine.context.toimipiste).oid
    const key = data.tunniste.koodiarvo

    saveOrganizationalPreference(
      organisaatioOid,
      paikallinenOppiainePrototype(createOppiaineenSuoritus(oppiaine.context.suoritus)).value.classes[0],
      key,
      data
    )
  }

  render() {
    const {oppiaine, footnote, additionalEditableKoulutusmoduuliProperties, allowOppiaineRemoval = true} = this.props
    const kurssit = modelItems(oppiaine, 'osasuoritukset')

    const {edit} = oppiaine.context

    return (
      <tr className={'oppiaine oppiaine-rivi ' + modelData(oppiaine, 'koulutusmoduuli.tunniste.koodiarvo')}>
        <td className='suorituksentila' title={tilaText(oppiaine)}>
          <div>
            {suorituksenTilaSymbol(oppiaine)}
          </div>
        </td>
        <td className='oppiaine'>
          <div className='title'>
            <Nimi oppiaine={oppiaine}/>
            <KoulutusmoduuliPropertiesEditor oppiaine={oppiaine} additionalEditableProperties={additionalEditableKoulutusmoduuliProperties}/>
          </div>
          <KurssitEditor model={oppiaine}/>
        </td>
        <td className='laajuus'>{laajuusNumberToString(Math.round(laajuudet(kurssit) * 10) / 10)}</td>
        <td className='arvosana'>
          <Arviointi oppiaine={oppiaine} suoritetutKurssit={suoritetutKurssit(kurssit)} footnote={footnote}/>
        </td>
        {
          edit && allowOppiaineRemoval && (
            <td className='remove-row'>
              <a className='remove-value' onClick={() => pushRemoval(oppiaine)}/>
            </td>
          )
        }
        {
          this.state && this.state.changed && isPaikallinen(modelLookup(oppiaine, 'koulutusmoduuli')) &&
          doActionWhileMounted(oppiaine.context.saveChangesBus, this.saveChangedPreferences.bind(this))
        }
      </tr>
    )
  }

  componentWillReceiveProps(nextProps) {
    const currentData = modelData(this.props.oppiaine)
    const newData = modelData(nextProps.oppiaine)

    if (!R.equals(currentData, newData)) this.setState({changed: true})
  }
}
