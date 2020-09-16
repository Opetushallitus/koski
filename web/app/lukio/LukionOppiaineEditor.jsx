import React from 'baret'
import * as R from 'ramda'

import {modelData, modelItems, modelLookup, pushRemoval} from '../editor/EditorModel.js'
import {suorituksenTilaSymbol} from '../suoritus/Suoritustaulukko'
import {KurssitEditor} from '../kurssi/KurssitEditor'
import {newOsasuoritusProto, tilaText} from '../suoritus/Suoritus'
import {isPaikallinen} from '../suoritus/Koulutusmoduuli'
import {saveOrganizationalPreference} from '../virkailija/organizationalPreferences'
import {paikallinenOppiainePrototype} from '../perusopetus/PerusopetuksenOppiaineEditor'
import {doActionWhileMounted} from '../util/util'
import {
  arvioidutOsasuoritukset,
  hylkäämättömätOsasuoritukset,
  hyväksytystiArvioidutOsasuoritukset,
  laajuudet,
  suoritetutKurssit
} from './lukio'
import {Arviointi, KoulutusmoduuliPropertiesEditor, Nimi} from './fragments/LukionOppiaine'
import {numberToString} from '../util/format'
import {PropertiesEditor} from '../editor/PropertiesEditor'

export class LukionOppiaineEditor extends React.Component {
  saveChangedPreferences() {
    const {oppiaine} = this.props

    const data = modelData(oppiaine, 'koulutusmoduuli')
    const organisaatioOid = modelData(oppiaine.context.toimipiste).oid
    const key = data.tunniste.koodiarvo

    saveOrganizationalPreference(
      organisaatioOid,
      paikallinenOppiainePrototype(newOsasuoritusProto(oppiaine.context.suoritus, oppiaine.value.classes[0])).value.classes[0],
      key,
      data
    )
  }

  render() {
    const {
      oppiaine,
      footnote,
      additionalEditableProperties,
      additionalOnlyEditableProperties,
      additionalEditableKoulutusmoduuliProperties,
      allowOppiaineRemoval = true,
      showLaajuus = true,
      useOppiaineLaajuus = false,
      showArviointi = true,
      showArviointiEditor = true,
      customOsasuoritusTitle,
      customOsasuoritusAlternativesCompletionFn,
      customKurssitSortFn,
      showKeskiarvo = true,
      useHylkäämättömätLaajuus = true,
      showHyväksytystiArvioitujenLaajuus = false
    } = this.props

    const kurssit = modelItems(oppiaine, 'osasuoritukset')

    const {edit} = oppiaine.context

    const laajuusArvo = () => {
      if (useOppiaineLaajuus) {
        return modelData(oppiaine, 'koulutusmoduuli.laajuus.arvo')
      } else {
        return numberToString(laajuudet(
          useHylkäämättömätLaajuus ? hylkäämättömätOsasuoritukset(kurssit) : arvioidutOsasuoritukset(kurssit)))
      }
    }

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
          {
            additionalEditableProperties && <PropertiesEditor model={oppiaine} propertyFilter={p => additionalEditableProperties.includes(p.key)}/>
          }
          {
            edit && additionalOnlyEditableProperties &&
            <PropertiesEditor
              model={oppiaine}
              propertyFilter={p => additionalOnlyEditableProperties.includes(p.key)}
            />
          }
          <KurssitEditor
            model={oppiaine}
            customTitle={customOsasuoritusTitle}
            customAlternativesCompletionFn={customOsasuoritusAlternativesCompletionFn}
            customKurssitSortFn={customKurssitSortFn}
          />
        </td>
        {
          showLaajuus &&
          <td className='laajuus'>{laajuusArvo()}</td>
        }
        {
          showHyväksytystiArvioitujenLaajuus &&
          (<td className='laajuus arvioitu'>{
              numberToString(laajuudet(hyväksytystiArvioidutOsasuoritukset(kurssit)))
          }</td>)
        }
        {
          showArviointi && (
            <td className='arvosana'>
              {showArviointiEditor && (
                <Arviointi
                  oppiaine={oppiaine}
                  suoritetutKurssit={suoritetutKurssit(kurssit)}
                  footnote={footnote}
                  showKeskiarvo={showKeskiarvo}
                />
              )}
            </td>
          )
        }
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

  UNSAFE_componentWillReceiveProps(nextProps) {
    const currentData = modelData(this.props.oppiaine)
    const newData = modelData(nextProps.oppiaine)

    if (!R.equals(currentData, newData)) this.setState({changed: true})
  }
}
