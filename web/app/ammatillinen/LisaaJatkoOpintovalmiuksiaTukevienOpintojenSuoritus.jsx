import React from 'baret'
import Text from '../i18n/Text'
import {modelItems} from '../editor/EditorModel'
import {
  AMMATILLISET_TUTKINNON_OSAT,
  createSuoritusWithTutkinnonOsanRyhmä,
  koodistoNimi
} from './LisaaKorkeakouluopintoSuoritus'
import {isJatkoOpintovalmiuksiaTukevienOpintojenSuoritus} from './TutkinnonOsa'

export const LisääJatkoOpintovalmiuksiaTukevienOpintojenSuoritus = ({parentSuoritus, suoritusPrototypes, groupTitles, groupId, addSuoritus}) => {
  const alreadyAdded = modelItems(parentSuoritus, 'osasuoritukset').some(isJatkoOpintovalmiuksiaTukevienOpintojenSuoritus)
  const suoritusPrototype = suoritusPrototypes.find(isJatkoOpintovalmiuksiaTukevienOpintojenSuoritus)

  const add = () => koodistoNimi('tutkinnonosatvalinnanmahdollisuus/1').onValue(suorituksenNimi => {
    addSuoritus(createSuoritusWithTutkinnonOsanRyhmä(suoritusPrototype, groupTitles[AMMATILLISET_TUTKINNON_OSAT], suorituksenNimi))
  })

  return alreadyAdded || !suoritusPrototype || groupId !== AMMATILLISET_TUTKINNON_OSAT
    ? null
    : (<div className='jatkoopintovalmiuksiatukevienopintojensuoritus'><a className="add-link" onClick={add}>
        <Text name='Lisää yhteisten tutkinnon osien osa-alueita, lukio-opintoja tai muita jatko-opintovalmiuksia tukevia opintoja' /></a>
       </div>)
}

LisääJatkoOpintovalmiuksiaTukevienOpintojenSuoritus.displayName = 'LisääJatkoOpintovalmiuksiaTukevienOpintojenSuoritus'
