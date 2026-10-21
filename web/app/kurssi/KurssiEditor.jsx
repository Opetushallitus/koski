import React, { useEffect, useRef, useState } from 'react'
import { cx } from '../components-v2/CommonProps'
import {
  modelData,
  modelLookup,
  modelTitle,
  pushRemoval
} from '../editor/EditorModel'
import { ArvosanaEditor } from '../suoritus/ArvosanaEditor'
import { KurssiPopup } from './KurssiPopup'
import {
  isLukio2019ModuuliTaiOpintojakso,
  isLukionKurssimainen,
  isPaikallinen
} from '../suoritus/Koulutusmoduuli'
import { FootnoteHint } from '../components/footnote'
import { eiLasketaKokonaispistemäärään } from '../dia/DIA'
import { useOutsideClick } from '../util/useOutsideClick'

export const KurssiEditor = ({ kurssi }) => {
  const [open, setOpen] = useState(false)
  const hideDetails = () => setOpen(false)

  const buttonRef = useRef(null)
  const popupRef = useRef(null)
  // Painike rajataan pois ulkopuolisista klikkauksista, koska muuten sen
  // klikkaus sulkisi avoimen popupin ja toggleDetails avaisi sen heti
  // uudelleen. Painike hoitaa itse sekä avaamisen että sulkemisen.
  useOutsideClick([buttonRef, popupRef], hideDetails, open)

  useEffect(() => {
    if (open) {
      const handleEsc = (e) => e.keyCode === 27 && setOpen(false)
      document.addEventListener('keyup', handleEsc)
      return () => document.removeEventListener('keyup', handleEsc)
    }
  }, [open])

  const koulutusmoduuli = modelData(kurssi, 'koulutusmoduuli')
  const koulutusmoduuliModel = modelLookup(kurssi, 'koulutusmoduuli')
  const edit = kurssi.context.edit

  const kurssinTyyppi = koulutusmoduuli.kurssinTyyppi?.koodiarvo
  const paikallinenLukionKurssimainen =
    isLukionKurssimainen(koulutusmoduuliModel) &&
    isPaikallinen(koulutusmoduuliModel)
  const className = cx(
    'text-button-small',
    'tunniste',
    kurssinTyyppi,
    !edit && 'hoverabe',
    eiLasketaKokonaispistemäärään(kurssi) && 'ei-lasketa-kokonaispistemäärään',
    isPaikallinen(koulutusmoduuliModel) &&
      !paikallinenLukionKurssimainen &&
      'paikallinen'
  )

  const title = kurssi.value.classes.includes('diasuoritus')
    ? modelTitle(kurssi, 'koulutusmoduuli')
    : koulutusmoduuli.tunniste.koodiarvo

  const paikallinenLukionOpintojakso =
    isLukio2019ModuuliTaiOpintojakso(koulutusmoduuliModel) &&
    isPaikallinen(koulutusmoduuliModel)

  const showDetails = () => setOpen(true)
  const toggleDetails = () => setOpen((isOpen) => !isOpen)

  const kurssiRef = useRef(null)

  return (
    <li className="kurssi" ref={kurssiRef}>
      <button
        ref={buttonRef}
        onClick={toggleDetails}
        onMouseEnter={!edit ? showDetails : undefined}
        onMouseLeave={!edit ? hideDetails : undefined}
        className={className}
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
          popupRef={popupRef}
          parentElemPosition={kurssiRef.current.getBoundingClientRect()}
        />
      )}
    </li>
  )
}
