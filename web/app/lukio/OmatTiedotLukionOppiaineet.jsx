import React from 'baret'
import * as R from 'ramda'
import {modelData, modelItems} from '../editor/EditorModel'
import {t} from '../i18n/i18n'
import {arvioidutKurssit, paikallisiaLukionOppiaineitaTaiKursseja} from './LukionOppiaineetEditor'
import {FootnoteDescriptions, FootnoteHint} from '../components/footnote'
import {kurssienKeskiarvo, Nimi} from './fragments/LukionOppiaine'
import {laajuusNumberToString} from '../util/format'
import {hyväksytystiSuoritetutKurssit, laajuudet, suoritetutKurssit} from './lukio'
import {KurssitEditor} from '../kurssi/KurssitEditor'
import {isMobileAtom} from '../util/isMobileAtom'
import {ArvosanaEditor} from '../suoritus/ArvosanaEditor'
import {OmatTiedotLukionOppiaineetTableHead} from './fragments/LukionOppiaineetTableHead'
import {KurssitListMobile} from '../kurssi/OmatTiedotKurssit'


export default ({suorituksetModel, suoritusFilter}) => {
  const oppiaineet = modelItems(suorituksetModel).filter(suoritusFilter || R.identity)

  if (R.isEmpty(oppiaineet)) return null

  return (
    <section>
      <table className='omattiedot-suoritukset'>
        <OmatTiedotLukionOppiaineetTableHead />
        <tbody>
          {oppiaineet.map((oppiaine, oppiaineIndex) => (
            <OmatTiedotLukionOppiaine
              baret-lift
              key={oppiaineIndex}
              oppiaine={oppiaine}
              isMobile={isMobileAtom}
            />
          ))}
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
    const {oppiaine, isMobile, footnote, showKeskiarvo = true, notFoundText = '-'} = this.props
    const kurssit = modelItems(oppiaine, 'osasuoritukset')
    const arviointi = modelData(oppiaine, 'arviointi')
    const oppiaineenKeskiarvo = kurssienKeskiarvo(suoritetutKurssit(kurssit))
    const laajuusYhteensä = laajuusNumberToString(laajuudet(hyväksytystiSuoritetutKurssit(kurssit)))
    const expandable = isMobile && kurssit.length > 0
    const Kurssit = isMobile ? KurssitListMobile : KurssitListDesktop

    return [
      <tr key='header' className={`oppiaine-header ${(expandable && expanded) ? 'expanded' : ''}`} onClick={expandable ? this.toggleExpand : undefined}>
        <td className='oppiaine'>
          <div className='otsikko-content'>
            {isMobile && <span className='expand-icon' aria-hidden={true}>{expandable && (expanded ? ' - ' : ' + ')}</span>}
            {expandable
              ? <button className='inline-text-button' onClick={this.toggleExpand} aria-pressed={expanded}><Nimi oppiaine={oppiaine}/></button>
              : <Nimi oppiaine={oppiaine} />}
            <span className='laajuus'>{`(${laajuusYhteensä} ${t('kurssia')})`}</span>
          </div>
        </td>
        <td className='arvosana'>
          <ArvosanaEditor model={oppiaine} notFoundText={notFoundText} />
          {arviointi && footnote && <FootnoteHint title={footnote.title} hint={footnote.hint} />}
        </td>
      </tr>,
      <tr key='content' className='oppiaine-kurssit'>
        {(!isMobile || expanded) && <Kurssit oppiaine={oppiaine} oppiaineenKeskiarvo={showKeskiarvo && oppiaineenKeskiarvo}/>}
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
