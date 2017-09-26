import React from 'react'
import {modelData, modelTitle} from './EditorModel.js'
import {PropertiesEditor} from './PropertiesEditor.jsx'
import {ArvosanaEditor} from './ArvosanaEditor.jsx'
import {pushRemoval} from './EditorModel'

export class KurssiEditor extends React.Component {
  constructor(props) {
    super(props)
    this.state = { open: false, tooltipPosition: 'bottom' }
    this.handleClickOutside = this.handleClickOutside.bind(this)
    this.handleEsc = this.handleEsc.bind(this)
  }

  render() {
    let {kurssi} = this.props
    let {open, tooltipPosition} = this.state
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
      <li className="kurssi" ref={e => this.kurssiElement = e}>
        <div onClick={showDetails} className={'tunniste ' + kurssinTyyppi } title={modelTitle(kurssi, 'koulutusmoduuli')}>{koulutusmoduuli.tunniste.koodiarvo}</div>
        <div className="arvosana"><ArvosanaEditor model={kurssi}/></div>
        {
          open && (<div className={'details details-' + tooltipPosition}>
            <PropertiesEditor
              model={kurssi}
              propertyFilter={p => !['arviointi', 'tila', 'koodistoUri'].includes(p.key)}
              propertyEditable={p => !['tunniste', 'koodiarvo', 'nimi', 'tunnustettu'].includes(p.key)}
            />
          </div>)
        }
        {
          kurssi.context.edit && <a className="remove-value" onClick={() => pushRemoval(kurssi)}/>
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
  }

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