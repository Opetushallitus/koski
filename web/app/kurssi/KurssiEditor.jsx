import React from 'react'
import {modelData, modelTitle} from '../editor/EditorModel.js'
import {PropertiesEditor} from '../editor/PropertiesEditor'
import {ArvosanaEditor} from '../suoritus/ArvosanaEditor'
import {pushRemoval} from '../editor/EditorModel'
import {buildClassNames} from '../components/classnames'

export class KurssiEditor extends React.Component {
  constructor(props) {
    super(props)
    this.state = { open: false }
    this.handleClickOutside = this.handleClickOutside.bind(this)
    this.handleEsc = this.handleEsc.bind(this)
  }

  render() {
    let {kurssi} = this.props
    let {open} = this.state
    let koulutusmoduuli = modelData(kurssi, 'koulutusmoduuli')
    let showDetails = () => {
      if (!open) {
        document.addEventListener('click', this.handleClickOutside, false)
        document.addEventListener('keyup', this.handleEsc)
        this.setState({ open: true })
      }
    }
    let hideDetails = () => {
      this.setState({open: false})
    }
    let kurssinTyyppi = koulutusmoduuli.kurssinTyyppi ? koulutusmoduuli.kurssinTyyppi.koodiarvo : ''
    let edit = kurssi.context.edit
    let className = buildClassNames(['tunniste', kurssinTyyppi, !edit && 'hoverable'])
    return (
      <li className="kurssi" ref={e => this.kurssiElement = e}>
        <a onClick={showDetails} onMouseEnter={!edit ? showDetails : undefined} onMouseLeave={!edit ? hideDetails : undefined} className={className} title={modelTitle(kurssi, 'koulutusmoduuli')}>{koulutusmoduuli.tunniste.koodiarvo}</a>
        {
          edit && <a className="remove-value" onClick={() => pushRemoval(kurssi)}/>
        }
        <div className="arvosana"><ArvosanaEditor model={kurssi}/></div>
        {
          open && <KurssiTooltip kurssi={kurssi} parentElemPosition={this.kurssiElement.getBoundingClientRect()}/>
        }
      </li>
    )
  }

  componentDidMount() {
    this.setState({open: false})
  }

  componentWillUnmount() {
    this.removeListeners()
  }

  removeListeners() {
    document.removeEventListener('click', this.handleClickOutside, false)
    document.removeEventListener('keyup', this.handleEsc)
  }

  handleClickOutside(e) {
    let detailsElem = this.kurssiElement.querySelector('.details')
    if (detailsElem && !detailsElem.contains(e.target)) {
      this.removeListeners()
      this.setState({open: false})
    }
  }

  handleEsc(e) {
    e.keyCode == 27 && this.setState({open: false})
  }
}

class KurssiTooltip extends React.Component {
  constructor(props) {
    super(props)
    this.state = { tooltipAlignment: {x: 'middle', y: 'bottom'}}
  }

  render() {
    let {kurssi} = this.props
    return (<div ref={e => this.tooltipElem = e}
      className={'details details-' + this.state.tooltipAlignment.x + ' details-' + this.state.tooltipAlignment.x + '-' + this.state.tooltipAlignment.y}>
      <PropertiesEditor
        model={kurssi}
        propertyFilter={p => !['arviointi', 'koodistoUri'].includes(p.key)}
        propertyEditable={p => !['tunniste', 'koodiarvo', 'nimi', 'tunnustettu'].includes(p.key)}
      />
    </div>)
  }

  componentDidMount() {
    this.setState({tooltipAlignment: getTooltipAlignment(this.props.parentElemPosition, this.tooltipElem)})
  }
}

const tooltipHorizontalAlignment = (kurssi, tooltip) => {
  const windowWidth = window.innerWidth || document.documentElement.clientWidth
  if (kurssi.left - tooltip.width < 0) {
    return 'left'
  } else if (kurssi.right + tooltip.width >= windowWidth) {
    return 'right'
  } else {
    return 'middle'
  }
}

const tooltipVerticalAlignment = (kurssi, tooltip) => {
  const windowHeight = window.innerHeight || document.documentElement.clientHeight
  return kurssi.top + kurssi.height + tooltip.height >= windowHeight ? 'top' : 'bottom'
}

const getTooltipAlignment = (rect, tooltipElem) => {
  const tooltipRect = tooltipElem.getBoundingClientRect()
  return {
    x: tooltipHorizontalAlignment(rect, tooltipRect),
    y: tooltipVerticalAlignment(rect, tooltipRect)
  }
}
