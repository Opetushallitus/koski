import React from 'baret'
import Atom from 'bacon.atom'
import Text from '../i18n/Text'
import TutkinnonOsaToisestaTutkinnostaPicker from './TutkinnonOsaToisestaTutkinnostaPicker'
import { modelData, modelSetTitle, modelSetValues } from '../editor/EditorModel'
import { ift } from '../util/util'
import ModalDialog from '../editor/ModalDialog'
import { isKorkeakouluOpintojenTutkinnonOsaaPienempiKokonaisuus } from './TutkinnonOsa'
import {
  isMuunAmmatillisenKoulutuksenSuoritus,
  isTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus
} from '../suoritus/SuoritustaulukkoCommon'

export const LisääPaikallinenTutkinnonOsa = ({
  lisättävätTutkinnonOsat,
  addTutkinnonOsa,
  paikallinenKoulutusmoduuli,
  preferredTexts
}) => {
  const lisääPaikallinenAtom = Atom(false)
  const lisääPaikallinenTutkinnonOsa = (osa) => {
    lisääPaikallinenAtom.set(false)
    if (osa) {
      addTutkinnonOsa(osa, undefined, liittyyTutkinnonOsaanAtom.get())
    }
  }
  const nameAtom = Atom('')
  const tutkintoAtom = Atom()
  const liittyyTutkinnonOsaanAtom = Atom()
  const selectedAtom = nameAtom.view((name) =>
    modelSetTitle(
      modelSetValues(paikallinenKoulutusmoduuli, {
        'kuvaus.fi': { data: name },
        'tunniste.nimi.fi': { data: name },
        'tunniste.koodiarvo': { data: name }
      }),
      name
    )
  )

  const tutkinnonosaaPienempiKokonaisuus = isTutkinnonosaaPienempiKokonaisuus(
    paikallinenKoulutusmoduuli
  )
  const validP = tutkinnonosaaPienempiKokonaisuus
    ? nameAtom.and(liittyyTutkinnonOsaanAtom.map('.data'))
    : nameAtom

  const texts =
    preferredTexts ||
    lisääTutkinnonOsaTexts(lisättävätTutkinnonOsat, paikallinenKoulutusmoduuli)
  return (
    <span className="paikallinen-tutkinnon-osa">
      {lisättävätTutkinnonOsat.paikallinenOsa && (
        <a className="add-link" onClick={() => lisääPaikallinenAtom.set(true)}>
          <Text name={texts.lisääOsaLink} />
        </a>
      )}
      {ift(
        lisääPaikallinenAtom,
        <ModalDialog
          className="lisaa-paikallinen-tutkinnon-osa-modal"
          onDismiss={lisääPaikallinenTutkinnonOsa}
          onSubmit={() => lisääPaikallinenTutkinnonOsa(selectedAtom.get())}
          okTextKey={texts.modalOk}
          validP={validP}
        >
          <h2>
            <Text name={texts.modalHeader} />
          </h2>
          {tutkinnonosaaPienempiKokonaisuus && (
            <TutkinnonOsaToisestaTutkinnostaPicker
              tutkintoAtom={tutkintoAtom}
              tutkinnonOsaAtom={liittyyTutkinnonOsaanAtom}
              oppilaitos={modelData(
                paikallinenKoulutusmoduuli.context.suoritus,
                'toimipiste'
              )}
              tutkintoTitle="Liittyy tutkintoon"
              tutkinnonOsaTitle="Liittyy tutkinnon osaan"
            />
          )}
          <label>
            <Text name={texts.modalFieldLabel} />
            <input
              className="paikallinen-koulutusmoduuli-nimi"
              type="text"
              autoFocus={!tutkinnonosaaPienempiKokonaisuus}
              onChange={(event) => nameAtom.set(event.target.value)}
            />
          </label>
        </ModalDialog>
      )}
    </span>
  )
}

const isTutkinnonosaaPienempiKokonaisuus = (k) =>
  k && k.value && k.value.classes[0] === 'tutkinnonosaapienempikokonaisuus'

const lisääTutkinnonOsaTexts = (
  lisättävätTutkinnonOsat,
  paikallinenKoulutusmoduuli
) => {
  if (lisättävätTutkinnonOsat.osanOsa) {
    return {
      lisääOsaLink: 'Lisää paikallinen tutkinnon osan osa-alue',
      modalHeader: 'Paikallisen tutkinnon osan osa-alueen lisäys',
      modalFieldLabel: 'Tutkinnon osan osa-alueen nimi',
      modalOk: 'Lisää tutkinnon osan osa-alue'
    }
  } else if (
    isKorkeakouluOpintojenTutkinnonOsaaPienempiKokonaisuus(
      paikallinenKoulutusmoduuli
    )
  ) {
    return {
      lisääOsaLink: 'Lisää korkeakouluopintokokonaisuus',
      modalHeader: 'Lisää korkeakouluopintokokonaisuus',
      modalFieldLabel: 'Nimi',
      modalOk: 'Lisää'
    }
  } else if (
    paikallinenKoulutusmoduuli &&
    isMuunAmmatillisenKoulutuksenSuoritus(
      paikallinenKoulutusmoduuli.context.suoritus
    )
  ) {
    return {
      lisääOsaLink: 'Lisää osasuoritus',
      modalHeader: 'Osasuorituksen lisäys',
      modalFieldLabel: 'Osasuorituksen nimi',
      modalOk: 'Lisää osasuoritus'
    }
  } else if (
    paikallinenKoulutusmoduuli &&
    isTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus(
      paikallinenKoulutusmoduuli.context.suoritus
    )
  ) {
    return {
      lisääOsaLink: 'Lisää tutkinnon osaa pienemmän kokonaisuuden suoritus',
      modalHeader: 'Tutkinnon osaa pienemmän kokonaisuuden lisäys',
      modalFieldLabel: 'Tutkinnon osaa pienemmän kokonaisuuden nimi',
      modalOk: 'Lisää tutkinnon osaa pienempi kokonaisuus'
    }
  } else {
    return {
      lisääOsaLink: 'Lisää paikallinen tutkinnon osa',
      modalHeader: 'Paikallisen tutkinnon osan lisäys',
      modalFieldLabel: 'Tutkinnon osan nimi',
      modalOk: 'Lisää tutkinnon osa'
    }
  }
}
