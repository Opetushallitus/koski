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

  const kotoOsaAlue = suoritusPrototypes.find(s => s.value.classes.includes('vapaansivistystyonmaahanmuuttajienkotoutumiskoulutuksenkokonaisuudensuoritus'))
  const kotoTyöelämäJaYhteiskuntataidot = suoritusPrototypes.find(s => s.value.classes.includes('vapaansivistystyonmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataidot'))
  const kotoTyöelämäJaYhteiskuntataidotTyöelämäjakso = suoritusPrototypes.find(s => s.value.classes.includes('vapaansivistystyonmaahanmuuttajienkotoutumiskoulutuksentyoelamajayhteiskuntataitojentyoelamajakso'))
  const kotoValinnaisetOpinnot= suoritusPrototypes.find(s => s.value.classes.includes('vapaansivistystyonmaahanmuuttajienkotoutumiskoulutuksenvalinnaistenopintojenosasuoritus'))

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
                          lisääText={'Lisää paikallinen opintokokonaisuus'}
                          lisääTitle={'Paikallisen opintokokonaisuuden lisäys'}
        />
      }
      {
        kotoTyöelämäJaYhteiskuntataidot &&
        <LisääPaikallinen suoritusPrototype={kotoTyöelämäJaYhteiskuntataidotTyöelämäjakso}
                          setExpanded={setExpanded}
                          lisääText={'Lisää työelämäjakso'}
                          lisääTitle={'Työelämäjakson lisäys'}
        />
      }
      {
        kotoTyöelämäJaYhteiskuntataidot &&
        <LisääPaikallinen suoritusPrototype={kotoTyöelämäJaYhteiskuntataidot}
                          setExpanded={setExpanded}
                          lisääText={'Lisää työelämä- ja yhteiskuntataidon opintokokonaisuus'}
                          lisääTitle={'Työelämä- ja yhteiskuntataidon opintokokonaisuuden lisäys'}
        />
      }
      {
        kotoValinnaisetOpinnot &&
        <LisääPaikallinen suoritusPrototype={kotoValinnaisetOpinnot}
                          setExpanded={setExpanded}
                          lisääText={'Lisää valinnainen opintosuoritus'}
                          lisääTitle={'Valinnaisen opintosuorituksen lisäys'}
        />
      }
      {
        kotoOsaAlue &&
        <LisääKoodistosta koodistoUri={'vstmaahanmuuttajienkotoutumiskoulutuksenkokonaisuus'}
                          suoritusPrototype={kotoOsaAlue}
                          className={'vst-osaamiskokonaisuus'}
                          selectionText={'Lisää osa-alue'}
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

const LisääPaikallinen = ({suoritusPrototype, setExpanded, lisääText, lisääTitle}) => {
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
          <Text name={lisääText}/>
        </a>
      {
        ift(showModal,
          <ModalDialog className="lisaa-paikallinen-vst-suoritus-modal"
                       onDismiss={closeModal}
                       onSubmit={addNewSuoritus}
                       okTextKey={lisääText}
                       validP={validP}>
            <h2><Text name={lisääTitle}/></h2>
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
