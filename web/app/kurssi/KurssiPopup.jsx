import React from 'react'
import { isDIAOppiaineenTutkintovaiheenOsasuoritus } from '../dia/DIA'
import DIATutkintovaiheenLukukaudenArviointiEditor, {
  hasLasketaanKokonaispistemäärään
} from '../dia/DIATutkintovaiheenLukukaudenArviointiEditor'
import { PropertiesEditor } from '../editor/PropertiesEditor'
import IBKurssinArviointiEditor from '../ib/IBKurssinArviointiEditor'
import AikuistenPerusopetuksenKurssinArviointiEditor from '../perusopetus/AikuistenPerusopetuksenKurssinArviointiEditor'
import { hasArviointi } from '../suoritus/Suoritus'
import { isAikuistenPerusopetuksenKurssi, isIBKurssi } from './kurssi'

export const isIBKurssinArviointi = (kurssi) => (property) =>
  isIBKurssi(kurssi) && property.key === 'arviointi' && hasArviointi(kurssi)

export const isDIAOsasuorituksenArviointi = (osasuoritus) => (property) =>
  isDIAOppiaineenTutkintovaiheenOsasuoritus(osasuoritus) &&
  property.key === 'arviointi' &&
  hasArviointi(osasuoritus) &&
  hasLasketaanKokonaispistemäärään(osasuoritus)

const resolvePropertyEditor = (model, property) => {
  if (isIBKurssi(model) && property.key === 'arviointi')
    return IBKurssinArviointiEditor
  if (
    isDIAOppiaineenTutkintovaiheenOsasuoritus(model) &&
    property.key === 'arviointi'
  )
    return DIATutkintovaiheenLukukaudenArviointiEditor
  if (isAikuistenPerusopetuksenKurssi(model) && property.key === 'arviointi')
    return AikuistenPerusopetuksenKurssinArviointiEditor

  return null
}

export class KurssiPopup extends React.Component {
  constructor(props) {
    super(props)
    this.state = { popupAlignment: { x: 'middle', y: 'bottom' } }
  }

  render() {
    const { kurssi } = this.props
    return (
      <div
        ref={(e) => (this.popupElem = e)}
        className={
          'details details-' +
          this.state.popupAlignment.x +
          ' details-' +
          this.state.popupAlignment.x +
          '-' +
          this.state.popupAlignment.y
        }
      >
        <PropertiesEditor
          model={kurssi}
          propertyFilter={(p) =>
            !['arviointi', 'koodistoUri'].includes(p.key) ||
            hasArviointi(kurssi)
          }
          propertyEditable={(p) =>
            !['tunniste', 'koodiarvo', 'nimi'].includes(p.key)
          }
          getValueEditor={(prop, getDefault) => {
            const PropertyEditor = resolvePropertyEditor(kurssi, prop)
            return PropertyEditor ? (
              <PropertyEditor model={kurssi} />
            ) : (
              getDefault()
            )
          }}
          className={kurssi.context.kansalainen ? 'kansalainen' : ''}
        />
      </div>
    )
  }

  componentDidMount() {
    this.setState({
      popupAlignment: getAlignment(
        this.props.parentElemPosition,
        this.popupElem
      )
    })
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
  const windowHeight =
    window.innerHeight || document.documentElement.clientHeight
  return kurssi.top + kurssi.height + popup.height >= windowHeight
    ? 'top'
    : 'bottom'
}

const getAlignment = (rect, popupElem) => {
  const popupRect = popupElem.getBoundingClientRect()
  return {
    x: horizontalAlignment(rect, popupRect),
    y: verticalAlignment(rect, popupRect)
  }
}
