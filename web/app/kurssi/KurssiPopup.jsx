import React from 'react'
import {PropertiesEditor} from '../editor/PropertiesEditor'
import IBKurssinArviointiEditor from '../ib/IBKurssinArviointiEditor'
import {isIBKurssi} from './kurssi'
import {hasArviointi} from '../suoritus/Suoritus'

export const isIBKurssinArviointi = kurssi => property => isIBKurssi(kurssi) && property.key === 'arviointi' && hasArviointi(kurssi)

export class KurssiPopup extends React.Component {
  constructor(props) {
    super(props)
    this.state = { popupAlignment: {x: 'middle', y: 'bottom'}}
  }

  render() {
    let {kurssi} = this.props
    return (<div ref={e => this.popupElem = e}
      className={'details details-' + this.state.popupAlignment.x + ' details-' + this.state.popupAlignment.x + '-' + this.state.popupAlignment.y}>
      <PropertiesEditor
        model={kurssi}
        propertyFilter={p => !['arviointi', 'koodistoUri'].includes(p.key) || isIBKurssinArviointi(kurssi)(p)}
        propertyEditable={p => !['tunniste', 'koodiarvo', 'nimi'].includes(p.key)}
        getValueEditor={(prop, getDefault) => isIBKurssi(kurssi) && prop.key === 'arviointi'
          ? <IBKurssinArviointiEditor model={kurssi}/>
          : getDefault()
        }
        className={kurssi.context.kansalainen ? 'kansalainen' : ''}
      />
    </div>)
  }

  componentDidMount() {
    this.setState({popupAlignment: getAlignment(this.props.parentElemPosition, this.popupElem)})
  }
}

const horizontalAlignment = (kurssi, popup) => {
  const windowWidth = window.innerWidth || document.documentElement.clientWidth
  if (kurssi.left - popup.width < 0) {
    return 'left'
  } else if (kurssi.right + popup.width >= windowWidth) {
    return 'right'
  } else {
    return 'middle'
  }
}

const verticalAlignment = (kurssi, popup) => {
  const windowHeight = window.innerHeight || document.documentElement.clientHeight
  return kurssi.top + kurssi.height + popup.height >= windowHeight ? 'top' : 'bottom'
}

const getAlignment = (rect, popupElem) => {
  const popupRect = popupElem.getBoundingClientRect()
  return {
    x: horizontalAlignment(rect, popupRect),
    y: verticalAlignment(rect, popupRect)
  }
}
