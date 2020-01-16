import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import ModalDialog from '../editor/ModalDialog'
import UusiOpiskeluoikeus from '../uusioppija/UusiOpiskeluoikeus'
import Text from '../i18n/Text'

export const UusiOpiskeluoikeusPopup = ({resultCallback}) => {
  const submitBus = Bacon.Bus()
  const opiskeluoikeusAtom = Atom()
  const validP = opiskeluoikeusAtom.map(validateOpintojenRahoitus)
  opiskeluoikeusAtom.sampledBy(submitBus).onValue((oo) => {
    resultCallback(oo)
  })
  return (<form className="uusi-oppija">
    <ModalDialog className="lisaa-opiskeluoikeus-modal" onDismiss={resultCallback} onSubmit={() => submitBus.push()} okTextKey="Lisää opiskeluoikeus" validP={validP} >
      <h2><Text name="Opiskeluoikeuden lisäys"/></h2>
      <UusiOpiskeluoikeus opiskeluoikeusAtom={opiskeluoikeusAtom}/>
    </ModalDialog>
  </form>)
}

const validateOpintojenRahoitus = opiskeluoikeus => {
  const tyyppi = opiskeluoikeudenTyyppi(opiskeluoikeus)
  const tila = opiskeluoikeudenTila(opiskeluoikeus)
  const opintojenRahoitusValittu = opintojenRahoitus(opiskeluoikeus)

  if (['aikuistenperusopetus', 'lukiokoulutus', 'internationalschool', 'luva', 'diatutkinto', 'ibtutkinto'].includes(tyyppi)) {
    return !['lasna', 'valmistunut'].includes(tila) || opintojenRahoitusValittu
  }
  if ('ammatillinenkoulutus' === tyyppi) {
    return !['lasna', 'valmistunut', 'loma'].includes(tila) || opintojenRahoitusValittu
  }
  return opiskeluoikeus
}

const opiskeluoikeudenTyyppi = oo => oo && oo.tyyppi && oo.tyyppi.koodiarvo
const opiskeluoikeusjakso = oo => oo && oo.tila && oo.tila.opiskeluoikeusjaksot[0]
const opiskeluoikeudenTila = oo => opiskeluoikeusjakso(oo) && opiskeluoikeusjakso(oo).tila.koodiarvo
const opintojenRahoitus = oo => opiskeluoikeusjakso(oo) && opiskeluoikeusjakso(oo).opintojenRahoitus
