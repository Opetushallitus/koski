import React from 'baret'
import Atom from 'bacon.atom'
import {ift} from '../util/util'
import ModalDialog from '../editor/ModalDialog'
import {
  modelSet,
  modelSetTitle,
  pushModel,
  modelLookup, modelSetValues
} from '../editor/EditorModel'
import {koulutusModuuliprototypes} from '../suoritus/Koulutusmoduuli'
import Text from '../i18n/Text'

export const UusiTutkintokoulutukseenValmentavanKoulutuksenOsasuoritus = ({suoritusPrototypes, setExpanded, suoritukset}) => {
  const findSuoritus = (tyyppi) => suoritukset.find(s => s.value.classes.includes(tyyppi))
  const findSuoritusByKoulutusmoduuli = (tyyppi) => {
    return suoritukset.find(s => s.value.properties.find(p => p.key === 'koulutusmoduuli' && p.model.value.classes.find(c => c === tyyppi)))
  }
  const findSuoritusPrototyyppi = (tyyppi) => suoritusPrototypes.find(s => s.value.classes.includes(tyyppi))

  const tuvaOpiskeluJaUrasuunnittelu =
    !findSuoritusByKoulutusmoduuli('tutkintokoulutukseenvalmentavatopiskelujaurasuunnittelutaidot')
    && findSuoritusPrototyyppi('tutkintokoulutukseenvalmentavakoulutuksenmuunosansuoritus')
  const tuvaOsaSuoritusPerustaitojenVahvistaminen =
    !findSuoritusByKoulutusmoduuli('tutkintokoulutukseenvalmentavaperustaitojenvahvistaminen')
    && findSuoritusPrototyyppi('tutkintokoulutukseenvalmentavakoulutuksenmuunosansuoritus')
  const tuvaAmmatillisenKoulutuksenOpinnot =
    !findSuoritusByKoulutusmoduuli('tutkintokoulutukseenvalmentavatammatillisenkoulutuksenopinnot')
    && findSuoritusPrototyyppi('tutkintokoulutukseenvalmentavakoulutuksenmuunosansuoritus')
  const tuvaLukioKoulutuksenOpinnot =
    !findSuoritusByKoulutusmoduuli('tutkintokoulutukseenvalmentavatlukiokoulutuksenopinnot')
    && findSuoritusPrototyyppi('tutkintokoulutukseenvalmentavakoulutuksenmuunosansuoritus')
  const tuvaTyöelämäTaidot =
    !findSuoritusByKoulutusmoduuli('tutkintokoulutukseenvalmentavattyoelamataidotjatyopaikallatapahtuvaoppiminen')
    && findSuoritusPrototyyppi('tutkintokoulutukseenvalmentavakoulutuksenmuunosansuoritus')
  const tuvaArjenJaYhteiskunnallisenOsallisuudenTaidot =
    !findSuoritusByKoulutusmoduuli('tutkintokoulutukseenvalmentavatarjenjayhteiskunnallisenosallisuudentaidot')
    && findSuoritusPrototyyppi('tutkintokoulutukseenvalmentavakoulutuksenmuunosansuoritus')
  const tuvaOsaSuoritusVapaavalintaiset =
    !findSuoritus('tutkintokoulutukseenvalmentavankoulutuksenvalinnaisenosansuoritus')
    && findSuoritusPrototyyppi('tutkintokoulutukseenvalmentavankoulutuksenvalinnaisenosansuoritus')
  const tuvaOsaSuorituksenOsaSuoritusVapaavalintaiset =
    findSuoritusPrototyyppi('tutkintokoulutukseenvalmentavankoulutuksenvalinnaisenkoulutusosanosasuorituksensuoritus')

  return (
    <>
      {
        tuvaOpiskeluJaUrasuunnittelu &&
        <LisääOsasuoritus suoritusPrototype={tuvaOpiskeluJaUrasuunnittelu}
                          setExpanded={setExpanded}
                          lisääText={'Lisää opiskelu- ja urasuunnittelutaitojen koulutuksen osa'}
                          koulutusModuuliTyyppi={'tutkintokoulutukseenvalmentavatopiskelujaurasuunnittelutaidot'}
                          className={'tuva-lisaa-osasuoritus-opiskelujaura'}
        />
      }
      {
        tuvaOsaSuoritusPerustaitojenVahvistaminen &&
        <LisääOsasuoritus suoritusPrototype={tuvaOsaSuoritusPerustaitojenVahvistaminen}
                          setExpanded={setExpanded}
                          lisääText={'Lisää perustaitojen vahvistamisen koulutuksen osa'}
                          koulutusModuuliTyyppi={'tutkintokoulutukseenvalmentavaperustaitojenvahvistaminen'}
                          className={'tuva-lisaa-osasuoritus-perustaidot'}
        />
      }
      {
        tuvaAmmatillisenKoulutuksenOpinnot &&
        <LisääOsasuoritus suoritusPrototype={tuvaAmmatillisenKoulutuksenOpinnot}
                          setExpanded={setExpanded}
                          lisääText={'Lisää ammatillisen koulutuksen opintojen ja niihin valmistautumisen koulutuksen osa'}
                          koulutusModuuliTyyppi={'tutkintokoulutukseenvalmentavatammatillisenkoulutuksenopinnot'}
                          className={'tuva-lisaa-osasuoritus-ammatillinen'}
        />
      }
      {
        tuvaLukioKoulutuksenOpinnot &&
        <LisääOsasuoritus suoritusPrototype={tuvaLukioKoulutuksenOpinnot}
                          setExpanded={setExpanded}
                          lisääText={'Lisää lukiokoulutuksen opintojen ja niihin valmistautumisen koulutuksen osa'}
                          koulutusModuuliTyyppi={'tutkintokoulutukseenvalmentavatlukiokoulutuksenopinnot'}
                          className={'tuva-lisaa-osasuoritus-lukio'}
        />
      }
      {
        tuvaTyöelämäTaidot &&
        <LisääOsasuoritus suoritusPrototype={tuvaTyöelämäTaidot}
                          setExpanded={setExpanded}
                          lisääText={'Lisää työelämätaitojen ja työpaikalla tapahtuvan oppimisen koulutuksen osa'}
                          koulutusModuuliTyyppi={'tutkintokoulutukseenvalmentavattyoelamataidotjatyopaikallatapahtuvaoppiminen'}
                          className={'tuva-lisaa-osasuoritus-tyoelamataidot'}
        />
      }
      {
        tuvaArjenJaYhteiskunnallisenOsallisuudenTaidot &&
        <LisääOsasuoritus suoritusPrototype={tuvaArjenJaYhteiskunnallisenOsallisuudenTaidot}
                          setExpanded={setExpanded}
                          lisääText={'Lisää arjen ja yhteiskunnallisen osallisuuden taitojen koulutuksen osa'}
                          koulutusModuuliTyyppi={'tutkintokoulutukseenvalmentavatarjenjayhteiskunnallisenosallisuudentaidot'}
                          className={'tuva-lisaa-osasuoritus-arkijayhteiskunta'}
        />
      }
      {
        tuvaOsaSuoritusVapaavalintaiset &&
        <LisääOsasuoritus suoritusPrototype={tuvaOsaSuoritusVapaavalintaiset}
                          setExpanded={setExpanded}
                          lisääText={'Lisää valinnaisten opintojen koulutuksen osa/osasuoritus'}
                          koulutusModuuliTyyppi={'tutkintokoulutukseenvalmentavankoulutuksenvalinnaisenkoulutusosa'}
                          className={'tuva-lisaa-osasuoritus-vapaavalintainen'}
        />
      }
      {
        tuvaOsaSuorituksenOsaSuoritusVapaavalintaiset &&
        <LisääPaikallinen suoritusPrototype={tuvaOsaSuorituksenOsaSuoritusVapaavalintaiset}
                          setExpanded={setExpanded}
                          lisääText={'Lisää paikallinen osasuoritus'}
                          lisääTitle={'Paikallisen osasuorituksen lisäys'}
                          className={'tuva-lisaa-osasuoritus-vapaavalintainen-paikallinen'}
        />
      }
    </>
  )
}

UusiTutkintokoulutukseenValmentavanKoulutuksenOsasuoritus.displayName = 'UusiTutkintokoulutukseenValmentavanKoulutuksenOsasuoritus'

const LisääOsasuoritus = ({suoritusPrototype, setExpanded, lisääText, koulutusModuuliTyyppi, className}) => {

  const valinnainenOsasuoritus = koulutusModuuliTyyppi === 'tutkintokoulutukseenvalmentavankoulutuksenvalinnaisenkoulutusosa'

  const koulutusmoduuliPrototype = valinnainenOsasuoritus
    ? koulutusModuuliprototypes(suoritusPrototype)[0]
    : koulutusModuuliprototypes(suoritusPrototype).find(s => s.value.classes.includes(koulutusModuuliTyyppi))

  const addNewSuoritus = () => {
    const koulutusmoduuli = modelSetTitle(
      koulutusmoduuliPrototype,
      modelLookup(koulutusmoduuliPrototype, 'tunniste').value.title
    )
    const suoritus = modelSet(suoritusPrototype, koulutusmoduuli, 'koulutusmoduuli')
    pushModel(suoritus)
    setExpanded(suoritus)(true)
  }

  return (
    <div className={'lisaa-uusi-suoritus'}>
      <span className={`lisaa-uusi-tuva-osasuoritus ${className}`}>
        <a className='add-link'
           onClick={() => addNewSuoritus()}>
          <Text name={lisääText}/>
        </a>
      </span>
    </div>
  )
}

LisääOsasuoritus.displayName = 'LisääOsasuoritus'

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
      'nimi.fi': {data: input},
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
            <ModalDialog className='lisaa-paikallinen-tuva-suoritus-modal'
                         onDismiss={closeModal}
                         onSubmit={addNewSuoritus}
                         okTextKey={lisääText}
                         validP={validP}>
              <h2><Text name={lisääTitle}/></h2>
              <label>
                <Text name={'Paikallisen osasuorituksen nimi'} />
                <input className='paikallinen-koulutusmoduuli-nimi'
                       type='text'
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

LisääPaikallinen.displayName = 'LisääPaikallinen'
