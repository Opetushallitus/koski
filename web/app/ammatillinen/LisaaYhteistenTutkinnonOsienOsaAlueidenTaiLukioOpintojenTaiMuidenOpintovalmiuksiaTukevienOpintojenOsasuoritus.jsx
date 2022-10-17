import React, { fromBacon } from 'baret'
import Bacon from 'baconjs'
import {
  isJatkoOpintovalmiuksiaTukevienOpintojenSuoritus,
  isLukioOpintojenSuoritus,
  isMuidenOpintovalmiuksiaTukevienOpintojenSuoritus,
  isYhteinenTutkinnonOsanOsa,
  tutkinnonOsanOsaAlueenKoulutusmoduuli
} from './TutkinnonOsa'
import {
  createTutkinnonOsa,
  pushSuoritus,
  valtakunnallisetKoulutusmoduuliPrototypes
} from './UusiTutkinnonOsa'
import { koodistoValues } from '../uusioppija/koodisto'
import {
  isPaikallinen,
  koulutusModuuliprototypes
} from '../suoritus/Koulutusmoduuli'
import {
  modelData,
  modelSetData,
  modelSetTitle,
  modelSetValues
} from '../editor/EditorModel'
import Atom from 'bacon.atom'
import { ift } from '../util/util'
import Text from '../i18n/Text'
import ModalDialog from '../editor/ModalDialog'
import Peruste from '../uusioppija/Peruste'
import * as R from 'ramda'
import { t } from '../i18n/i18n'
import { LisääRakenteeseenKuuluvaTutkinnonOsa } from './LisaaRakenteeseenKuuluvaTutkinnonOsa'

export const LisääYhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus =
  ({ parentSuoritus, suoritusPrototypes, setExpanded }) => {
    if (!isJatkoOpintovalmiuksiaTukevienOpintojenSuoritus(parentSuoritus)) {
      return null
    }

    return (
      <div className="yhteistentutkinnonosienosaalueidentailukioopintojentaimuidenopintovalmiuksiatukevienopintojenosasuoritus">
        <LisääYhteinenTutkinnonOsa {...{ suoritusPrototypes, setExpanded }} />
        <LisääLukioOpinto {...{ suoritusPrototypes, setExpanded }} />
        <LisääMuuOpintovalmiuksiaTukevaOpinto
          {...{ suoritusPrototypes, setExpanded }}
        />
      </div>
    )
  }

const LisääYhteinenTutkinnonOsa = ({ suoritusPrototypes, setExpanded }) => {
  const yhteisenTutkinnonOsanOsanPrototype = suoritusPrototypes.find(
    isYhteinenTutkinnonOsanOsa
  )
  const yhteisetTutkinnonOsatP = koodistoValues('ammatillisenoppiaineet').map(
    (oppiaineet) => {
      return { osat: oppiaineet, paikallinenOsa: true, osanOsa: true }
    }
  )

  const addTutkinnonOsa = (koulutusmoduuli) =>
    addSuoritus(
      setExpanded,
      yhteisenTutkinnonOsanOsanPrototype,
      koulutusmoduuli
    )

  return fromBacon(
    yhteisetTutkinnonOsatP.map((lisättävätTutkinnonOsat) => (
      <LisääRakenteeseenKuuluvaTutkinnonOsa
        {...{
          addTutkinnonOsa,
          lisättävätTutkinnonOsat,
          koulutusmoduuliProto: selectKoulutusModuuliProto(
            yhteisenTutkinnonOsanOsanPrototype
          ),
          placeholder: t('Lisää yhteisen tutkinnon osan osa-alue')
        }}
      />
    ))
  )
}

const LisääLukioOpinto = ({ suoritusPrototypes, setExpanded }) => (
  <LisääPaikallinen
    suoritusPrototype={suoritusPrototypes.find(isLukioOpintojenSuoritus)}
    setExpanded={setExpanded}
    headerText="Lisää lukio-opinto"
    className="lukio-opinto"
  />
)

const LisääMuuOpintovalmiuksiaTukevaOpinto = ({
  suoritusPrototypes,
  setExpanded
}) => (
  <LisääPaikallinen
    suoritusPrototype={suoritusPrototypes.find(
      isMuidenOpintovalmiuksiaTukevienOpintojenSuoritus
    )}
    setExpanded={setExpanded}
    headerText="Lisää muu opintovalmiuksia tukeva opinto"
    className="muu-opintovalmiuksia-tukeva-opinto"
  />
)

const LisääPaikallinen = ({
  suoritusPrototype,
  setExpanded,
  headerText,
  className
}) => {
  const koulutusmoduuliPrototype =
    koulutusModuuliprototypes(suoritusPrototype).find(isPaikallinen)
  const addAtom = Atom(false)
  const add = (koulutusmoduuli) => {
    addAtom.set(false)
    if (koulutusmoduuli) {
      addSuoritus(setExpanded, suoritusPrototype, koulutusmoduuli)
    }
  }
  const nameAtom = Atom()
  const perusteAtom = Atom()
  const selectedAtom = Atom()
  const hasPeruste = !R.isNil(
    modelData(koulutusmoduuliPrototype, 'perusteenDiaarinumero')
  )

  Bacon.combineWith(nameAtom, perusteAtom, (name, peruste) => ({
    name,
    peruste
  })).onValue((np) => {
    if (np.name) {
      let koulutusmoduuliModel = modelSetValues(koulutusmoduuliPrototype, {
        'kuvaus.fi': { data: np.name },
        'tunniste.nimi.fi': { data: np.name },
        'tunniste.koodiarvo': { data: np.name }
      })
      if (np.peruste) {
        koulutusmoduuliModel = modelSetData(
          koulutusmoduuliModel,
          np.peruste,
          'perusteenDiaarinumero'
        )
      }
      selectedAtom.set(modelSetTitle(koulutusmoduuliModel, np.name))
    }
  })

  return (
    <span className={className}>
      <a className="add-link" onClick={() => addAtom.set(true)}>
        <Text name={headerText} />
      </a>
      {ift(
        addAtom,
        <ModalDialog
          className={`lisaa-${className}-modal`}
          onDismiss={add}
          onSubmit={() => add(selectedAtom.get())}
          okTextKey="Lisää"
          validP={nameAtom}
        >
          <h2>
            <Text name={headerText} />
          </h2>
          <label>
            <Text name="Nimi" />
            <input
              className="paikallinen-koulutusmoduuli-nimi"
              type="text"
              autoFocus={true}
              onChange={(event) => nameAtom.set(event.target.value)}
            />
          </label>
          {hasPeruste && (
            <Peruste
              suoritusTyyppiP={Bacon.constant({
                koodiarvo: 'lukionoppiaineenoppimaara'
              })}
              perusteAtom={perusteAtom}
            />
          )}
        </ModalDialog>
      )}
    </span>
  )
}

const selectKoulutusModuuliProto = (suoritusPrototype) => (selectedItem) =>
  tutkinnonOsanOsaAlueenKoulutusmoduuli(
    valtakunnallisetKoulutusmoduuliPrototypes(suoritusPrototype),
    selectedItem.data
  )

const addSuoritus = (
  setExpanded,
  suoritusPrototype,
  koulutusmoduuliPrototype
) =>
  pushSuoritus(setExpanded)(
    createTutkinnonOsa(suoritusPrototype, koulutusmoduuliPrototype)
  )
