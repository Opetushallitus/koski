import React from 'baret'
import Atom from 'bacon.atom'
import {
  modelSet,
  modelSetTitle,
  modelSetValues,
  pushModel
} from '../editor/EditorModel'
import * as R from 'ramda'
import {koodistoValues} from '../uusioppija/koodisto'
import KoodistoDropdown from '../koodisto/KoodistoDropdown'
import {t} from '../i18n/i18n'
import {koulutusModuuliprototypes} from '../suoritus/Koulutusmoduuli'
import {enumValueToKoodiviiteLens} from '../koodisto/koodistot'
import ModalDialog from '../editor/ModalDialog'
import Text from '../i18n/Text'
import {ift} from '../util/util'

export const UusiVapaanSivistystyonOsasuoritus = ({suoritusPrototypes, setExpanded}) => {
  const osaamiskokonaisuus = suoritusPrototypes.find(s => s.value.classes.includes('oppivelvollisillesuunnatunvapaansivistystyonosaamiskokonaisuudensuoritus'))
  const suuntautumisopinnot = suoritusPrototypes.find(s => s.value.classes.includes('oppivelvollisillesuunnatunvapaansivistystyonvalinnaistensuuntautumisopintojensuoritus'))
  const muuallaSuoritettuOpinto = suoritusPrototypes.find(s => s.value.classes.includes('muuallasuoritettuoppivelvollisillesuunnatunvapaansivistystyonopintojensuoritus'))
  const opintokokonaisuus = suoritusPrototypes.find(s => s.value.classes.includes('oppivelvollisillesuunnatunvapaansivistystyonopintokokonaisuudensuoritus'))

  return (
    <>
      {
        osaamiskokonaisuus &&
        <LisääKoodistosta koodistoUri={'vstosaamiskokonaisuus'}
                          suoritusPrototype={osaamiskokonaisuus}
                          className={'vst-osaamiskokonaisuus'}
                          selectionText={'Lisää osaamiskokonaisuus'}
                          setExpanded={setExpanded}
        />
      }
      {
        suuntautumisopinnot &&
        <LisääKoodistosta koodistoUri={'vstmuutopinnot'}
                          suoritusPrototype={suuntautumisopinnot}
                          className={'vst-suuntautumisopinnot'}
                          selectionText={'Lisää suuntautumisopinto'}
                          setExpanded={setExpanded}
        />
      }
      {
        muuallaSuoritettuOpinto &&
        <LisääKoodistosta koodistoUri={'vstmuuallasuoritetutopinnot'}
                          suoritusPrototype={muuallaSuoritettuOpinto}
                          className={'vst-muutopinnot'}
                          selectionText={'Lisää muualla suoritettu opinto'}
                          setExpanded={setExpanded}
        />
      }
      {
        opintokokonaisuus &&
        <LisääPaikallinen suoritusPrototype={opintokokonaisuus}
                          setExpanded={setExpanded}
        />
      }
    </>
  )
}

const LisääKoodistosta = ({
  suoritusPrototype,
  koodistoUri,
  className,
  selectionText,
  setExpanded
}) => {
  const selectedAtom = Atom()
  const koulutusmoduuliPrototype = koulutusModuuliprototypes(suoritusPrototype)[0]

  selectedAtom.filter(R.identity).onValue(newItem => {
    const koulutusmoduuli = modelSetTitle(modelSetValues(koulutusmoduuliPrototype, {tunniste: newItem}), newItem.title)
    const suoritus = modelSet(suoritusPrototype, koulutusmoduuli, 'koulutusmoduuli')
    pushModel(suoritus)
    setExpanded(suoritus)(true)
    selectedAtom.set(undefined)
  })

  return (
    <div className={'lisaa-uusi-suoritus ' + className}>
      <KoodistoDropdown className={className}
                        options={koodistoValues(koodistoUri)}
                        selected={selectedAtom.view(enumValueToKoodiviiteLens)}
                        selectionText={t(selectionText)}
      />
    </div>
  )
}

const LisääPaikallinen = ({suoritusPrototype, setExpanded}) => {
  const showModal = Atom(false)
  const inputState = Atom('')
  const validP = inputState

  const closeModal = () => showModal.set(false)
  const updateInputState = (event) => inputState.set(event.target.value)

  const koulutusmoduuliPrototype = koulutusModuuliprototypes(suoritusPrototype)[0]

  const addNewSuoritus = () => {
    const input = inputState.get()
    const updateValues = {
      'kuvaus.fi': {data: input},
      'tunniste.nimi.fi': {data: input},
      'tunniste.koodiarvo': {data: input}
    }
    const koulutusmoduuli = modelSetTitle(modelSetValues(koulutusmoduuliPrototype, updateValues), input)
    const suoritus = modelSet(suoritusPrototype, koulutusmoduuli, 'koulutusmoduuli')
    pushModel(suoritus)
    setExpanded(suoritus)(true)
    showModal.set(false)
  }

  return (
    <div className={'lisaa-uusi-suoritus paikallinen'}>
      <span className="lisaa-paikallinen-suoritus">
        <a className='add-link'
           onClick={() => showModal.set(true)}>
          <Text name={'Lisää paikallinen opintokokonaisuus'}/>
        </a>
      {
        ift(showModal,
          <ModalDialog className="lisaa-paikallinen-suoritus-modal"
                       onDismiss={closeModal}
                       onSubmit={addNewSuoritus}
                       okTextKey={'Lisää paikallinen opintokokonaisuus'}
                       validP={validP}>
            <h2><Text name={'Paikallisen opintokokonaisuuden lisäys'}/></h2>
            <label>
              <Text name={'Opintokokonaisuuden nimi'} />
              <input className='paikallinen-koulutusmoduuli-nimi'
                    type="text"
                    autoFocus={true}
                    onChange={updateInputState}
              />
            </label>
          </ModalDialog>
        )
      }
      </span>
    </div>
  )
}
