import React from 'baret'
import Atom from 'bacon.atom'
import Dropdown from '../components/Dropdown'
import * as R from 'ramda'
import Text from '../i18n/Text'
import {t} from '../i18n/i18n'
import {ift} from '../util/util'
import {koodistoValues} from './koodisto'
import KoodistoDropdown from '../koodisto/KoodistoDropdown'
import PaikallinenKoulutusmoduuli from './PaikallinenKoulutusmoduuli'

export default ({koulutusmoduuliNimiAtom, koulutusmoduuliKoodiAtom, koulutusmoduuliKuvausAtom, ammatilliseentehtavaanvalmistavakoulutusAtom}) => {
  const koulutusmoduulityyppiAtom = Atom(paikallinenKoulutus)
  const ammatilliseentehtavaanvalmistavakoulutusP =  koodistoValues('ammatilliseentehtavaanvalmistavakoulutus')

  return (
    <div className='koulutusmoduuli'>
      <label>
        <Text name='Koulutusmoduuli' />
        <Dropdown
          options={[paikallinenKoulutus, ammatilliseenTehtäväänValmistavaKoulutus]}
          keyValue={R.identity}
          displayValue={t}
          onSelectionChanged={o => koulutusmoduulityyppiAtom.set(o) }
          selected={koulutusmoduulityyppiAtom}
        />
      </label>
      {
        ift(koulutusmoduulityyppiAtom.map(k => k === ammatilliseenTehtäväänValmistavaKoulutus),
          <KoodistoDropdown
            className='ammatilliseentehtäväänvalmistavakoulutus'
            title={ammatilliseenTehtäväänValmistavaKoulutus}
            options={ammatilliseentehtavaanvalmistavakoulutusP}
            selected={ammatilliseentehtavaanvalmistavakoulutusAtom}/>
        )
      }
      {
        ift(koulutusmoduulityyppiAtom.map(k => k === paikallinenKoulutus),
          <PaikallinenKoulutusmoduuli nimi={koulutusmoduuliNimiAtom} koodiarvo={koulutusmoduuliKoodiAtom} kuvaus={koulutusmoduuliKuvausAtom} />
        )
      }
    </div>
  )
}

const paikallinenKoulutus = 'Paikallinen koulutus'
const ammatilliseenTehtäväänValmistavaKoulutus = 'Ammatilliseen tehtävään valmistava koulutus'
