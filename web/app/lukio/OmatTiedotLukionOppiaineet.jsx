import React from 'baret'
import Bacon from 'baconjs'
import * as R from 'ramda'
import {modelData, modelItems, modelLookup} from '../editor/EditorModel'
import Text from '../i18n/Text'
import {t} from '../i18n/i18n'
import {arvioidutKurssit, paikallisiaLukionOppiaineitaTaiKursseja} from './LukionOppiaineetEditor'
import {FootnoteDescriptions, FootnoteHint} from '../components/footnote'
import {kurssienKeskiarvo, Nimi} from './fragments/LukionOppiaine'
import {laajuusNumberToString} from '../util/format'
import {hyväksytystiSuoritetutKurssit, laajuudet, suoritetutKurssit} from './lukio'
import {KurssitEditor} from '../kurssi/KurssitEditor'
import {isMobileAtom} from '../util/isMobileAtom'
import {ArvosanaEditor} from '../suoritus/ArvosanaEditor'
import {PropertiesEditor} from '../editor/PropertiesEditor'
import {
  isLukionKurssi,
  isLukioonValmistavanKoulutuksenKurssi,
  isPaikallinen,
  isPreIBKurssi
} from '../suoritus/Koulutusmoduuli'
import IBKurssinArviointiEditor from '../ib/IBKurssinArviointiEditor'
import {isIBKurssinArviointi} from '../kurssi/KurssiPopup'
import {isIBKurssi} from '../kurssi/kurssi'
import {nothing} from '../util/util'
import {OmatTiedotLukionOppiaineetTableHead} from './fragments/LukionOppiaineetTableHead'


export default ({suorituksetModel, suoritusFilter}) => {
  const oppiaineet = modelItems(suorituksetModel).filter(suoritusFilter || R.identity)

  if (R.isEmpty(oppiaineet)) return null

  return (
    <section>
      <table className='omattiedot-suoritukset'>
        <OmatTiedotLukionOppiaineetTableHead />
        <tbody>
          {Bacon.combineWith(isMobileAtom, mobile =>
            oppiaineet.map((oppiaine, oppiaineIndex) =>
              <OmatTiedotLukionOppiaine key={oppiaineIndex} oppiaine={oppiaine} isMobile={mobile}/>
            )
          )}
        </tbody>
      </table>
      <div className='kurssit-yhteensä'>{t('Suoritettujen kurssien laajuus yhteensä') + ': ' + laajuusNumberToString(laajuudet(arvioidutKurssit(oppiaineet)))}</div>
      {paikallisiaLukionOppiaineitaTaiKursseja(oppiaineet) && <FootnoteDescriptions data={[{title: 'Paikallinen kurssi tai oppiaine', hint: '*'}]}/>}
    </section>
  )
}

export class OmatTiedotLukionOppiaine extends React.Component {
  constructor(props){
    super(props)
    this.state = {
      expanded: false
    }

    this.toggleExpand = this.toggleExpand.bind(this)
  }

  toggleExpand(e) {
    e.stopPropagation()
    this.setState(({expanded}) => ({expanded: !expanded}))
  }

  render() {
    const {expanded} = this.state
    const {oppiaine, isMobile, footnote} = this.props
    const kurssit = modelItems(oppiaine, 'osasuoritukset')
    const arviointi = modelData(oppiaine, 'arviointi')
    const oppiaineenKeskiarvo = kurssienKeskiarvo(suoritetutKurssit(kurssit))
    const laajuusYhteensä = laajuusNumberToString(laajuudet(hyväksytystiSuoritetutKurssit(kurssit)))
    const expandable = isMobile && kurssit.length > 0
    const Kurssit = isMobile ? KurssitListMobile : KurssitListDesktop

    return [
      <tr key='header' className={`oppiaine-header ${(expandable && expanded) ? 'expanded' : ''}`} onClick={expandable ? this.toggleExpand : nothing}>
        <td className='oppiaine'>
          <div className='otsikko-content'>
            {isMobile && <span className='expand-icon' aria-hidden={true}>{expandable && (expanded ? ' - ' : ' + ')}</span>}
            {expandable
              ? <button className='inline-text-button' onClick={this.toggleExpand}><Nimi oppiaine={oppiaine}/></button>
              : <Nimi oppiaine={oppiaine} />}
            <span className='laajuus'>{`(${laajuusYhteensä} ${t('kurssia')})`}</span>
          </div>
        </td>
        <td className='arvosana'>
          <ArvosanaEditor model={oppiaine} notFoundText='-'/>
          {arviointi && footnote && <FootnoteHint title={footnote.title} hint={footnote.hint} />}
        </td>
      </tr>,
      <tr key='content' className='oppiaine-kurssit'>
        {(!isMobile || expanded) && <Kurssit oppiaine={oppiaine} oppiaineenKeskiarvo={oppiaineenKeskiarvo}/>}
      </tr>
    ]
  }
}

const KurssitListDesktop = ({oppiaine, oppiaineenKeskiarvo}) => (
  [
    <td className='kurssilista' key='kurssit'>
      <KurssitEditor model={oppiaine}/>
    </td>,
    <td className='arvosana' key='arvosana'>
      {oppiaineenKeskiarvo &&
        <span>
          <span className='screenreader-info'>{`${t('Keskiarvo')} ${oppiaineenKeskiarvo}`}</span>
          <span aria-hidden={true}>{`(${oppiaineenKeskiarvo})`}</span>
        </span>}
    </td>
  ]
)

const KurssitListMobile = ({oppiaine, oppiaineenKeskiarvo}) => {
  const osasuoritukset = modelLookup(oppiaine, 'osasuoritukset')
  if (!osasuoritukset) return null
  const kurssit = modelItems(osasuoritukset)

  return (
    <td colSpan='2'>
      <table className='kurssilista-mobile'>
        <thead>
        <tr>
          <th className='nimi'><Text name='Kurssi'/></th>
          <th className='arvosana'><Text name='Arvosana' /></th>
          <th className='lisatiedot'><Text name='Lisätiedot' /></th>
        </tr>
        </thead>
        <tbody>
        {kurssit.map((kurssi, index) =>
          <MobileKurssi kurssi={kurssi} even={(index + 1) % 2 === 0} key={index}/>
        )}
        {oppiaineenKeskiarvo && <tr>
          <td aria-hidden={true}/>
          <td className='arvosana keskiarvo'>
            {`(${t('Keskiarvo')} ${oppiaineenKeskiarvo})`}
          </td>
        </tr>}
        </tbody>
      </table>
    </td>
  )
}

class MobileKurssi extends React.Component {
  constructor(props) {
    super(props)
    this.state = { expanded: false }

    this.toggleExpand = this.toggleExpand.bind(this)
  }

  toggleExpand() {
    this.setState(prevState => ({expanded: !prevState.expanded}))
  }

  render() {
    const {kurssi, even} = this.props
    const {expanded} = this.state
    const koulutusmoduuli = modelData(kurssi, 'koulutusmoduuli')
    const koulutusmoduuliModel = modelLookup(kurssi, 'koulutusmoduuli')

    return [
      <tr key='kurssi-row' className={`kurssi ${even ? 'even' : ''}`}>
        <td className='nimi'>{koulutusmoduuli.tunniste.koodiarvo} {hasFootnoteHint(koulutusmoduuliModel) && <FootnoteHint title={'Paikallinen kurssi'}/>}</td>
        <td className='arvosana'><ArvosanaEditor model={kurssi}/></td>
        <td className='lisatiedot'><button className='inline-link-button' onClick={this.toggleExpand}><Text name={expanded ? 'Sulje' : 'Avaa'}/></button></td>
      </tr>,
      expanded && <tr key='kurssi-details'>
        <td colSpan='3'>
          <PropertiesEditor
            model={kurssi}
            propertyFilter={p => !['arviointi', 'koodistoUri'].includes(p.key) || isIBKurssinArviointi(kurssi)(p)}
            propertyEditable={p => !['tunniste', 'koodiarvo', 'nimi'].includes(p.key)}
            className='kansalainen'
            getValueEditor={(prop, getDefault) => isIBKurssi(kurssi) && prop.key === 'arviointi'
              ? <IBKurssinArviointiEditor model={kurssi}/>
              : getDefault()
            }
          />
        </td>
      </tr>
    ]
  }
}

const hasFootnoteHint = koulutusmoduuliModel => {
  return (
    isLukionKurssi(koulutusmoduuliModel) ||
    isPreIBKurssi(koulutusmoduuliModel) ||
    isLukioonValmistavanKoulutuksenKurssi(koulutusmoduuliModel)
  ) &&
  isPaikallinen(koulutusmoduuliModel)
}
