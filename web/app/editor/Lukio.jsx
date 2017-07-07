import React from 'react'
import {modelData, modelTitle, modelItems} from './EditorModel.js'
import {suorituksenTilaSymbol} from './Suoritustaulukko.jsx'
import {PropertiesEditor} from './PropertiesEditor.jsx'
import Text from '../Text.jsx'

export class LukionOppiaineetEditor extends React.Component {
  render() {
    let {oppiaineet} = this.props
    return (
      <table className="suoritukset">
        <thead>
        <tr>
          <th className="suorituksentila"></th>
          <th className="oppiaine"><Text name="Oppiaine"/></th>
          <th className="maara"><Text name="Kurssien määrä"/></th>
          <th className="arvosana"><Text name="Arvosana (keskiarvo)"/></th>
        </tr>
        <tr>
          <th colSpan="4"><hr/></th>
        </tr>
        </thead>
        <tbody>
        {
          oppiaineet.map((oppiaine, oppiaineIndex) =>
            <LukionOppiaineEditor key={oppiaineIndex} oppiaine={oppiaine} />
          )
        }
        </tbody>
      </table>
    )
  }
}

class LukionOppiaineEditor extends React.Component {
  render() {

    let {oppiaine} = this.props
    let arviointi = modelData(oppiaine, 'arviointi')
    let kurssit = modelItems(oppiaine, 'osasuoritukset')
    let suoritetutKurssit = kurssit.map(k => modelData(k)).filter(k => k.arviointi)
    let numeerinenArvosana = kurssi => parseInt(kurssi.arviointi.last().arvosana.koodiarvo)
    let kurssitNumeerisellaArvosanalla = suoritetutKurssit.filter(kurssi => !isNaN(numeerinenArvosana(kurssi)))
    let keskiarvo = kurssitNumeerisellaArvosanalla.length > 0 && Math.round((kurssitNumeerisellaArvosanalla.map(numeerinenArvosana).reduce((a,b) => a + b) / kurssitNumeerisellaArvosanalla.length) * 10) / 10

    return (
      <tr>
        <td className="suorituksentila" title={modelTitle(oppiaine, 'tila')}>
          <div>
          {suorituksenTilaSymbol(modelData(oppiaine, 'tila.koodiarvo'))}
          </div>
        </td>
        <td className="oppiaine">
          <div className="nimi">{modelTitle(oppiaine, 'koulutusmoduuli')}</div>
          <ul className="kurssit">
            {
              kurssit.map((kurssi, kurssiIndex) =>
                <LukionKurssiEditor key={kurssiIndex} kurssi={kurssi}/>
              )
            }
          </ul>
        </td>
        <td className="maara">{suoritetutKurssit.length}</td>
        <td className="arvosana">
          <div className="annettuArvosana">{arviointi ? modelData(oppiaine, 'arviointi.-1.arvosana').koodiarvo : '-'}</div>
          <div className="keskiarvo">{keskiarvo ? '(' + keskiarvo.toFixed(1).replace('.', ',') + ')' : ''}</div>
        </td>
      </tr>
    )
  }
}

class LukionKurssiEditor extends React.Component {
  constructor(props) {
    super(props)
    this.state = { open: false, tooltipPosition: 'bottom' }
    this.handleClickOutside = this.handleClickOutside.bind(this)
    this.handleEsc = this.handleEsc.bind(this)
  }

  render() {
    let {kurssi} = this.props
    let {open, tooltipPosition} = this.state
    let arviointi = modelData(kurssi, 'arviointi')
    let koulutusmoduuli = modelData(kurssi, 'koulutusmoduuli')
    let showDetails = () => {
      if (!open) {
        document.addEventListener('click', this.handleClickOutside, false)
        document.addEventListener('keyup', this.handleEsc)
        this.setState({
          open: true,
          tooltipPosition: this.kurssiElement && getTooltipPosition(this.kurssiElement) || tooltipPosition
        })
      }
    }
    let kurssinTyyppi = koulutusmoduuli.kurssinTyyppi ? koulutusmoduuli.kurssinTyyppi.koodiarvo : ''
    return (
      <li onClick={showDetails} className="kurssi" ref={e => this.kurssiElement = e}>
        <div className={'tunniste ' + kurssinTyyppi } title={modelTitle(kurssi, 'koulutusmoduuli')}>{koulutusmoduuli.tunniste.koodiarvo}</div>
        <div className="arvosana">{arviointi && modelData(kurssi, 'arviointi.-1.arvosana').koodiarvo}</div>
        {
          open && (<div className={'details details-' + tooltipPosition}>
            <PropertiesEditor model={kurssi}/>
          </div>)
        }
      </li>
    )
  }

  componentDidMount() {
    this.setState({open: false, tooltipPosition: getTooltipPosition(this.kurssiElement)})
  }

  componentWillUnmount() {
    this.removeListeners()
  }

  removeListeners() {
    document.removeEventListener('click', this.handleClickOutside, false)
    document.removeEventListener('keyup', this.handleEsc)
  };

  handleClickOutside(e) {
    if (!this.kurssiElement.querySelector('.details').contains(e.target)) {
      this.removeListeners()
      this.setState({open: false})
    }
  }

  handleEsc(e) {
    e.keyCode == 27 && this.setState({open: false})
  }
}

let getTooltipPosition = (kurssiElement) => {
  let tooltipwidth = 275
  let rect = kurssiElement.getBoundingClientRect()
  if (rect.left - tooltipwidth < 0) {
    return 'right'
  } else if (rect.right + tooltipwidth >= (window.innerWidth || document.documentElement.clientWidth)) {
    return 'left'
  } else {
    return 'bottom'
  }
}