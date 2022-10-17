import React from 'baret'
import { elementWithLoadingIndicator } from '../components/AjaxLoadingIndicator'
import {
  NON_GROUPED,
  selectTutkinnonOsanSuoritusPrototype,
  tutkinnonOsanOsaAlueenKoulutusmoduuli
} from '../ammatillinen/TutkinnonOsa'
import {
  createTutkinnonOsa,
  pushSuoritus,
  valtakunnallisetKoulutusmoduuliPrototypes
} from '../ammatillinen/UusiTutkinnonOsa'
import { t } from '../i18n/i18n'
import {
  isPaikallinen,
  koulutusModuuliprototypes
} from '../suoritus/Koulutusmoduuli'
import { koodistoValues } from '../uusioppija/koodisto'
import { LisääRakenteeseenKuuluvaTutkinnonOsa } from '../ammatillinen/LisaaRakenteeseenKuuluvaTutkinnonOsa'
import { LisääPaikallinenTutkinnonOsa } from '../ammatillinen/LisaaPaikallinenTutkinnonOsa'

export const UusiTutkinnonOsaMuuAmmatillinen = ({
  groupId,
  suoritusPrototypes,
  setExpanded,
  groupTitles
}) => {
  const yhteisenTutkinnonOsanOsaAlueenPrototype =
    selectTutkinnonOsanSuoritusPrototype(
      suoritusPrototypes,
      groupId,
      'yhteisentutkinnonosanosaalueensuoritus'
    )
  const yhteisenTutkinnonOsanOsaAlueenValtakunnallisetKoulutusmoduulit =
    valtakunnallisetKoulutusmoduuliPrototypes(
      yhteisenTutkinnonOsanOsaAlueenPrototype
    )
  const yhteisenTutkinnonOsanPaikallinenKoulutusmoduuli =
    koulutusModuuliprototypes(yhteisenTutkinnonOsanOsaAlueenPrototype).find(
      isPaikallinen
    )

  const tutkinnonOsaaPienemmänKokonaisuudenPrototype =
    selectTutkinnonOsanSuoritusPrototype(
      suoritusPrototypes,
      groupId,
      'tutkinnonosaapienemmankokonaisuudensuoritus'
    )
  const tutkinnonOsaaPienemmänKokonaisuudenKoulutusmoduuli =
    koulutusModuuliprototypes(
      tutkinnonOsaaPienemmänKokonaisuudenPrototype
    ).find(isPaikallinen)

  const muuAmmatillisenKoulutuksenOsasuoritusPrototype =
    selectTutkinnonOsanSuoritusPrototype(
      suoritusPrototypes,
      groupId,
      'muunammatillisenkoulutuksenosasuorituksensuoritus'
    )
  const muuAmmatillisenKoulutuksenOsasuorituksenKoulutusmoduuli =
    koulutusModuuliprototypes(
      muuAmmatillisenKoulutuksenOsasuoritusPrototype
    ).find(isPaikallinen)
  const muuAmmatillisenKoulutuksenOsasuoritus =
    muuAmmatillisenKoulutuksenOsasuoritusPrototype.value.classes.includes(
      'muunammatillisenkoulutuksenosasuorituksensuoritus'
    )

  const osatP = koodistoValues('ammatillisenoppiaineet').map((oppiaineet) => {
    return {
      osat: oppiaineet,
      osanOsa: true,
      paikallinenOsa: true,
      osaToisestaTutkinnosta: true,
      muuAmmatillisenKoulutuksenOsasuoritus
    }
  })

  const addTutkinnonOsa =
    (suoritusPrototype) =>
    (koulutusmoduuli, tutkinto, liittyyTutkinnonOsaan) => {
      const group = groupId === NON_GROUPED ? undefined : groupId
      const tutkinnonOsa = createTutkinnonOsa(
        suoritusPrototype,
        koulutusmoduuli,
        tutkinto,
        group,
        groupTitles,
        liittyyTutkinnonOsaan
      )
      pushSuoritus(setExpanded)(tutkinnonOsa)
    }

  return (
    <span>
      {elementWithLoadingIndicator(
        osatP.map((lisättävätTutkinnonOsat) => (
          <div className={'muu-ammatillinen-uusi-tutkinnon-osa'}>
            <LisääRakenteeseenKuuluvaTutkinnonOsa
              lisättävätTutkinnonOsat={lisättävätTutkinnonOsat}
              addTutkinnonOsa={addTutkinnonOsa(
                yhteisenTutkinnonOsanOsaAlueenPrototype
              )}
              koulutusmoduuliProto={(item) =>
                item &&
                tutkinnonOsanOsaAlueenKoulutusmoduuli(
                  yhteisenTutkinnonOsanOsaAlueenValtakunnallisetKoulutusmoduulit,
                  item.data
                )
              }
              placeholder={t('Lisää yhteisen tutkinnon osan osa-alue')}
            />
            <LisääPaikallinenTutkinnonOsa
              lisättävätTutkinnonOsat={lisättävätTutkinnonOsat}
              paikallinenKoulutusmoduuli={
                yhteisenTutkinnonOsanPaikallinenKoulutusmoduuli
              }
              addTutkinnonOsa={addTutkinnonOsa(
                yhteisenTutkinnonOsanOsaAlueenPrototype
              )}
              preferredTexts={{
                lisääOsaLink:
                  'Lisää paikallinen yhteisen tutkinnon osan osa-alue',
                modalHeader:
                  'Paikallisen yhteisen tutkinnon osan osa-alueen lisäys',
                modalFieldLabel: 'Tutkinnon osan osa-alueen nimi',
                modalOk: 'Lisää tutkinnon osan osa-alue'
              }}
            />
            <LisääPaikallinenTutkinnonOsa
              lisättävätTutkinnonOsat={lisättävätTutkinnonOsat}
              paikallinenKoulutusmoduuli={
                tutkinnonOsaaPienemmänKokonaisuudenKoulutusmoduuli
              }
              addTutkinnonOsa={addTutkinnonOsa(
                tutkinnonOsaaPienemmänKokonaisuudenPrototype
              )}
              preferredTexts={{
                lisääOsaLink:
                  'Lisää tutkinnon osaa pienemmän kokonaisuuden suoritus',
                modalHeader: 'Tutkinnon osaa pienemmän kokonaisuuden lisäys',
                modalFieldLabel: 'Tutkinnon osaa pienemmän kokonaisuuden nimi',
                modalOk: 'Lisää tutkinnon osaa pienempi kokonaisuus'
              }}
            />
            {lisättävätTutkinnonOsat.muuAmmatillisenKoulutuksenOsasuoritus && (
              <LisääPaikallinenTutkinnonOsa
                lisättävätTutkinnonOsat={lisättävätTutkinnonOsat}
                paikallinenKoulutusmoduuli={
                  muuAmmatillisenKoulutuksenOsasuorituksenKoulutusmoduuli
                }
                addTutkinnonOsa={addTutkinnonOsa(
                  muuAmmatillisenKoulutuksenOsasuoritusPrototype
                )}
                preferredTexts={{
                  lisääOsaLink: 'Lisää muun ammatillisen koulutuksen suoritus',
                  modalHeader:
                    'Muun ammatillisen koulutuksen suorituksen lisäys',
                  modalFieldLabel: 'Suorituksen nimi',
                  modalOk: 'Lisää suoritus'
                }}
              />
            )}
          </div>
        ))
      )}
    </span>
  )
}
