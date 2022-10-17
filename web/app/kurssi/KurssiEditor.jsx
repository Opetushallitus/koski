import React from 'react'
import {
  modelData,
  modelLookup,
  modelTitle,
  pushRemoval
} from '../editor/EditorModel'
import { ArvosanaEditor } from '../suoritus/ArvosanaEditor'
import { buildClassNames } from '../components/classnames'
import { KurssiPopup } from './KurssiPopup'
import {
  isLukio2019ModuuliTaiOpintojakso,
  isLukionKurssimainen,
  isPaikallinen
} from '../suoritus/Koulutusmoduuli'
import { FootnoteHint } from '../components/footnote'
import { eiLasketaKokonaispistemäärään } from '../dia/DIA'

export class KurssiEditor extends React.Component {
  constructor(props) {
    super(props)
    this.state = { open: false }
    this.handleClickOutside = this.handleClickOutside.bind(this)
    this.handleEsc = this.handleEsc.bind(this)
  }

  render() {
    const { kurssi } = this.props
    const { open } = this.state
    const koulutusmoduuli = modelData(kurssi, 'koulutusmoduuli')
    const koulutusmoduuliModel = modelLookup(kurssi, 'koulutusmoduuli')
    const showDetails = () => {
      if (!open) {
        document.addEventListener('click', this.handleClickOutside, false)
        document.addEventListener('keyup', this.handleEsc)
        this.setState({ open: true })
      }
    }
    const hideDetails = () => {
      this.setState({ open: false })
    }
    const kurssinTyyppi = koulutusmoduuli.kurssinTyyppi
      ? koulutusmoduuli.kurssinTyyppi.koodiarvo
      : ''
    const edit = kurssi.context.edit
    const paikallinenLukionKurssimainen =
      isLukionKurssimainen(koulutusmoduuliModel) &&
      isPaikallinen(koulutusmoduuliModel)
    const paikallinenLukionOpintojakso =
      isLukio2019ModuuliTaiOpintojakso(koulutusmoduuliModel) &&
      isPaikallinen(koulutusmoduuliModel)
    const className = buildClassNames([
      'tunniste',
      kurssinTyyppi,
      !edit && 'hoverable',
      eiLasketaKokonaispistemäärään(kurssi) &&
        'ei-lasketa-kokonaispistemäärään',
      isPaikallinen(koulutusmoduuliModel) &&
        !paikallinenLukionKurssimainen &&
        'paikallinen'
    ])
    const title = kurssi.value.classes.includes('diasuoritus')
      ? modelTitle(kurssi, 'koulutusmoduuli')
      : koulutusmoduuli.tunniste.koodiarvo
    return (
      <li className="kurssi" ref={(e) => (this.kurssiElement = e)}>
        <button
          onClick={showDetails}
          onMouseEnter={!edit ? showDetails : undefined}
          onMouseLeave={!edit ? hideDetails : undefined}
          className={`text-button-small ${className}`}
          title={modelTitle(kurssi, 'koulutusmoduuli')}
        >
          {title}
        </button>
        {edit && (
          <a className="remove-value" onClick={() => pushRemoval(kurssi)} />
        )}
        {paikallinenLukionKurssimainen && (
          <FootnoteHint title={'Paikallinen kurssi'} />
        )}
        {paikallinenLukionOpintojakso && (
          <FootnoteHint title={'Paikallinen opintojakso'} />
        )}
        {eiLasketaKokonaispistemäärään(kurssi) && (
          <FootnoteHint title={'Ei lasketa kokonaispistemäärään'} />
        )}
        <div className="arvosana">
          <ArvosanaEditor model={kurssi} />
        </div>
        {open && (
          <KurssiPopup
            kurssi={kurssi}
            parentElemPosition={this.kurssiElement.getBoundingClientRect()}
          />
        )}
      </li>
    )
  }

  componentDidMount() {
    this.setState({ open: false })
  }

  componentWillUnmount() {
    this.removeListeners()
  }

  removeListeners() {
    document.removeEventListener('click', this.handleClickOutside, false)
    document.removeEventListener('keyup', this.handleEsc)
  }

  handleClickOutside(e) {
    const detailsElem = this.kurssiElement.querySelector('.details')
    if (
      detailsElem &&
      !detailsElem.contains(e.target) &&
      ![
        'DayPicker-Day',
        'DayPicker-Day DayPicker-Day--selected',
        'remove-item'
      ].includes(e.target.className)
    ) {
      this.removeListeners()
      this.setState({ open: false })
    }
  }

  handleEsc(e) {
    e.keyCode == 27 && this.setState({ open: false })
  }
}
