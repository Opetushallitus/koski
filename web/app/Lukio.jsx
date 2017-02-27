import React from 'react'
import { modelData, modelTitle, modelItems } from './EditorModel.js'
import * as GenericEditor from './GenericEditor.jsx'

export const LukionOppiaineetEditor = React.createClass({
  render() {
    let {model, context} = this.props
    let oppiaineet = modelItems(model, 'osasuoritukset') || []
    return (
      <table className="suoritukset">
        <thead>
        <tr>
          <th className="oppiaine">Oppiaine</th>
          <th className="maara">Kurssien määrä</th>
          <th className="arvosana">Arvosana (keskiarvo)</th>
        </tr>
        <tr>
          <th colSpan="3"><hr/></th>
        </tr>
        </thead>
        <tbody>
        {
          oppiaineet.map((oppiaine, oppiaineIndex) =>
            <LukionOppiaineEditor key={oppiaineIndex} oppiaine={oppiaine} context={GenericEditor.childContext(this, context, 'osasuoritukset')}/>
          )
        }
        </tbody>
      </table>
    )
  }
})

const LukionOppiaineEditor = React.createClass({
  render() {

    let {oppiaine} = this.props
    let arviointi = modelData(oppiaine, 'arviointi')
    let kurssit = modelItems(oppiaine, 'osasuoritukset') || []
    let suoritetutKurssit = kurssit.map(k => modelData(k)).filter(k => k.arviointi)
    let numeerinenArvosana = kurssi => parseInt(kurssi.arviointi.last().arvosana.koodiarvo)
    let kurssitNumeerisellaArvosanalla = suoritetutKurssit.filter(kurssi => !isNaN(numeerinenArvosana(kurssi)))
    let keskiarvo = kurssitNumeerisellaArvosanalla.length > 0 && Math.round((kurssitNumeerisellaArvosanalla.map(numeerinenArvosana).reduce((a,b) => a + b) / kurssitNumeerisellaArvosanalla.length) * 10) / 10

    return (
      <tr>
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
          <div className="keskiarvo">{keskiarvo ? '(' + keskiarvo.toFixed(1) + ')' : ''}</div>
        </td>
      </tr>
    )
  }
})

const LukionKurssiEditor = React.createClass({
  render() {
    let {kurssi} = this.props
    let {open, tooltipPosition} = this.state
    let arviointi = modelData(kurssi, 'arviointi')
    let koulutusmoduuli = modelData(kurssi, 'koulutusmoduuli')
    let toggleDetails = () => {
      this.setState({
        open: !open,
        tooltipPosition: this.kurssiElement && getTooltipPosition(this.kurssiElement) || tooltipPosition
      })
    }
    return (
      <li className="kurssi" ref={e => this.kurssiElement = e} onClick={toggleDetails}>
        <div className={'tunniste ' + koulutusmoduuli.kurssinTyyppi.koodiarvo} title={modelTitle(kurssi, 'koulutusmoduuli')}>{koulutusmoduuli.tunniste.koodiarvo}</div>
        <div className="arvosana">{arviointi && modelData(kurssi, 'arviointi.-1.arvosana').koodiarvo}</div>
        {
          open && (<div className={'details details-' + tooltipPosition}>
            <table>
              <tbody>
              <tr><td>tunniste:</td><td>{koulutusmoduuli.tunniste.koodiarvo}</td></tr>
              <tr><td>nimi:</td><td>{koulutusmoduuli.tunniste.nimi.fi}</td></tr>
              <tr><td>laajuus:</td><td>{koulutusmoduuli.laajuus.arvo} kurssia</td></tr>
              {koulutusmoduuli.kuvaus && <tr><td>kuvaus:</td><td>{koulutusmoduuli.kuvaus.fi}</td></tr>}
              <tr><td>kurssin tyyppi:</td><td>{koulutusmoduuli.kurssinTyyppi.nimi.fi}</td></tr>
              <tr><td>tila:</td><td>{modelData(kurssi, 'tila.nimi.fi')}</td></tr>
              <tr><td>arvosana:</td><td>{modelData(kurssi, 'arviointi.-1.arvosana').koodiarvo} / {modelData(kurssi, 'arviointi.-1.arvosana').nimi.fi}</td></tr>
              </tbody>
            </table>
          </div>)
        }
      </li>
    )
  },
  getInitialState() {
    return { open: false, tooltipPosition: 'bottom' }
  },
  componentDidMount() {
    document.addEventListener('click', this.handleClickOutside, false)
    document.addEventListener('keyup', this.handleEsc)
    this.setState({open: false, tooltipPosition: getTooltipPosition(this.kurssiElement)})
  },
  componentWillUnmount() {
    document.removeEventListener('click', this.handleClickOutside, false)
    document.removeEventListener('keyup', this.handleEsc)
  },
  handleClickOutside(e) {
    !this.kurssiElement.contains(e.target) && this.setState({open: false})
  },
  handleEsc(e) {
    e.keyCode == 27 && this.setState({open: false})
  }
})

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