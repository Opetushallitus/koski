import React from 'baret'
import R from 'ramda'

import {t} from '../i18n/i18n'
import {modelData, modelItems, modelLookup, modelTitle, pushRemoval} from '../editor/EditorModel.js'
import {suorituksenTilaSymbol} from '../suoritus/Suoritustaulukko'
import {KurssitEditor} from '../kurssi/KurssitEditor'
import {tilaText} from '../suoritus/Suoritus'
import {FootnoteHint} from '../components/footnote'
import {isKieliaine, isLukionMatematiikka, isPaikallinen} from '../suoritus/Koulutusmoduuli'
import {Editor} from '../editor/Editor'
import {ArvosanaEditor} from '../suoritus/ArvosanaEditor'
import {PropertiesEditor} from '../editor/PropertiesEditor'
import {saveOrganizationalPreference} from '../virkailija/organizationalPreferences'
import {paikallinenOppiainePrototype} from '../perusopetus/PerusopetuksenOppiaineEditor'
import {doActionWhileMounted} from '../util/util'
import {createOppiaineenSuoritus} from './lukio'

const Nimi = ({oppiaine}) => {
  const {edit} = oppiaine.context
  const koulutusmoduuli = modelLookup(oppiaine, 'koulutusmoduuli')
  const nimi = t(modelData(oppiaine, 'koulutusmoduuli.tunniste.nimi'))
  const nimiJaKieli = modelTitle(oppiaine, 'koulutusmoduuli')
  const hasOptions = isKieliaine(koulutusmoduuli) || isLukionMatematiikka(koulutusmoduuli)

  return (
    edit && isPaikallinen(koulutusmoduuli)
      ? (
        <span className='koodi-ja-nimi'>
          <span className='koodi'><Editor model={koulutusmoduuli} path='tunniste.koodiarvo' placeholder={t('Koodi')} /></span>
          <span className='nimi'><Editor model={koulutusmoduuli} path='tunniste.nimi' placeholder={t('Oppiaineen nimi')} /></span>
        </span>
      )
      : (
        <span className='nimi'>
          {edit && hasOptions ? `${nimi}, ` : nimiJaKieli}
        </span>
      )
  )
}

const KoulutusmoduuliPropertiesEditor = ({oppiaine}) => {
  if (!oppiaine.context.edit) return null

  const koulutusmoduuli = modelLookup(oppiaine, 'koulutusmoduuli')

  return (
    <span className='properties'>
      {isKieliaine(koulutusmoduuli) && <Editor model={koulutusmoduuli} path='kieli' inline={true}/>}
      {isLukionMatematiikka(koulutusmoduuli) && <Editor model={koulutusmoduuli} path='oppimäärä' inline={true}/>}
      {isPaikallinen(koulutusmoduuli) && <PropertiesEditor model={koulutusmoduuli} propertyFilter={p => p.key === 'kuvaus'} />}
    </span>
  )
}

const Arviointi = ({oppiaine, suoritetutKurssit, footnote}) => {
  const {edit} = oppiaine.context

  const arviointi = modelData(oppiaine, 'arviointi')
  const numeerinenArvosana = kurssi => parseInt(kurssi.arviointi.last().arvosana.koodiarvo)
  const kurssitNumeerisellaArvosanalla = suoritetutKurssit.filter(kurssi => !isNaN(numeerinenArvosana(kurssi)))
  const keskiarvo = kurssitNumeerisellaArvosanalla.length > 0 && Math.round((kurssitNumeerisellaArvosanalla.map(numeerinenArvosana).reduce((a, b) => a + b) / kurssitNumeerisellaArvosanalla.length) * 10) / 10

  return (
    <div>
      <div className='annettuArvosana'>
        {
          edit || arviointi
            ? <ArvosanaEditor model={oppiaine}/>
            : '-'
        }
        {arviointi && footnote && <FootnoteHint title={footnote.title} hint={footnote.hint} />}
      </div>
      <div className='keskiarvo'>{keskiarvo ? '(' + keskiarvo.toFixed(1).replace('.', ',') + ')' : ''}</div>
    </div>
  )
}

export class LukionOppiaineRowEditor extends React.Component {
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
    const {oppiaine, footnote, allowOppiaineRemoval = true} = this.props
    const kurssit = modelItems(oppiaine, 'osasuoritukset')
    const suoritetutKurssit = kurssit.map(k => modelData(k)).filter(k => k.arviointi)
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
            <KoulutusmoduuliPropertiesEditor oppiaine={oppiaine}/>
          </div>
          <KurssitEditor model={oppiaine}/>
        </td>
        <td className='maara'>{suoritetutKurssit.length}</td>
        <td className='arvosana'>
          <Arviointi oppiaine={oppiaine} suoritetutKurssit={suoritetutKurssit} footnote={footnote}/>
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
