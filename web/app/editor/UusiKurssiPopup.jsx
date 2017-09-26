import React from 'baret'
import Atom from 'bacon.atom'
import R from 'ramda'
import {modelData} from './EditorModel'
import Text from '../Text.jsx'
import ModalDialog from './ModalDialog.jsx'
import {UusiKurssiDropdown} from './UusiKurssiDropdown.jsx'
import {isPaikallinen, koulutusModuuliprototypes} from './Koulutusmoduuli'

export default ({oppiaine, resultCallback, toimipiste, uusiKurssinSuoritus}) => {
  let selectedAtom = Atom()
  let validP = selectedAtom
  let päätasonSuoritus = uusiKurssinSuoritus.context.suoritus
  let valtakunnallisetKurssiProtot = filterProtos(päätasonSuoritus, koulutusModuuliprototypes(uusiKurssinSuoritus).filter(R.complement(isPaikallinen)))
  let paikallinenKurssiProto = koulutusModuuliprototypes(uusiKurssinSuoritus).find(isPaikallinen)

  return (<ModalDialog className="uusi-kurssi-modal" onDismiss={resultCallback} onSubmit={() => resultCallback(selectedAtom.get())} validP={validP} okTextKey="Lisää">
    <h2><Text name="Lisää kurssi"/></h2>
    <span className="kurssi"><UusiKurssiDropdown oppiaine={oppiaine}
                                                 kurssinSuoritus={uusiKurssinSuoritus}
                                                 valtakunnallisetKurssiProtot={valtakunnallisetKurssiProtot}
                                                 paikallinenKurssiProto={paikallinenKurssiProto}
                                                 selected={selectedAtom}
                                                 resultCallback={(x) => selectedAtom.set(x)}
                                                 organisaatioOid={toimipiste}
                                                 placeholder="Lisää kurssi"/></span>
  </ModalDialog>)
}

const filterProtos = (päätasonSuoritus, protos) => {
  if (päätasonSuoritus.value.classes.includes('aikuistenperusopetuksenoppimaaransuoritus')) {
    let diaari = modelData(päätasonSuoritus, 'koulutusmoduuli.perusteenDiaarinumero')
    return protos.filter(proto => {
      switch (diaari) {
        case 'OPH-1280-2017': return proto.value.classes.includes('valtakunnallinenaikuistenperusopetuksenpaattovaiheenkurssi2017')
        case '19/011/2015': return proto.value.classes.includes('valtakunnallinenaikuistenperusopetuksenkurssi2015')
        default: return true
      }
    })
  } else {
    return protos
  }
}
