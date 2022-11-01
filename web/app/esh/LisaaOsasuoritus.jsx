import React from 'baret'
import Atom from 'bacon.atom'
import * as R from 'ramda'
import { modelSetTitle, modelSetValues } from '../editor/EditorModel'
import LisaaOsasuoritusDropdown from './LisaaOsasuoritusDropdown'
import { t } from '../i18n/i18n'

export const LisaaOsasuoritus = ({
  lisättävätOsasuoritukset,
  addOsasuoritus,
  koulutusmoduuliProto,
  placeholder = lisättävätOsasuoritukset.osanOsa
    ? t('Lisää alaosasuoritus')
    : t('Lisää osasuoritus')
}) => {
  const selectedAtom = Atom(undefined)
  selectedAtom.filter(R.identity).onValue((newItem) => {
    const osasuoritus = modelSetValues(koulutusmoduuliProto(newItem), {
      tunniste: newItem
    })
    console.log('koulutusmoduuliProto', koulutusmoduuliProto(newItem))
    console.log('osasuoritus', osasuoritus)
    addOsasuoritus(modelSetTitle(osasuoritus, newItem.title))
  })
  return (
    lisättävätOsasuoritukset.osat.length > 0 && (
      <span className="osa-samasta-tutkinnosta">
        <LisaaOsasuoritusDropdown
          selectedAtom={selectedAtom}
          osat={lisättävätOsasuoritukset.osat}
          placeholder={placeholder}
        />
      </span>
    )
  )
}
