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

  const valtakunnalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenPrototype =
    selectTutkinnonOsanSuoritusPrototype(
      suoritusPrototypes,
      groupId,
      'valtakunnalliseentutkinnonosaanliittyvantutkinnonosaapienemmankokonaisuudensuoritus'
    )
  const paikalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenPrototype =
    selectTutkinnonOsanSuoritusPrototype(
      suoritusPrototypes,
      groupId,
      'paikalliseentutkinnonosaanliittyvantutkinnonosaapienemmankokonaisuudensuoritus'
    )
  const valtakunnalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenKoulutusmoduuli =
    koulutusModuuliprototypes(
      valtakunnalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenPrototype
    ).find(isPaikallinen)
  const paikalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenKoulutusmoduuli =
    koulutusModuuliprototypes(
      paikalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenPrototype
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
    (koulutusmoduuli, tutkinto, liittyyTutkinnonOsaan, liittyyTutkintoon) => {
      const group = groupId === NON_GROUPED ? undefined : groupId
      const tutkinnonOsa = createTutkinnonOsa(
        suoritusPrototype,
        koulutusmoduuli,
        tutkinto,
        group,
        groupTitles,
        liittyyTutkinnonOsaan,
        liittyyTutkintoon
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
                valtakunnalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenKoulutusmoduuli
              }
              addTutkinnonOsa={addTutkinnonOsa(
                valtakunnalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenPrototype
              )}
              preferredTexts={{
                lisääOsaLink:
                  'Lisää valtakunnalliseen tutkinnon osaan liittyvän tutkinnon osaa pienemmän kokonaisuuden suoritus',
                modalHeader:
                  'Valtakunnalliseen tutkinnon osaan liittyvän tutkinnon osaa pienemmän kokonaisuuden lisäys',
                modalFieldLabel:
                  'Valtakunnalliseen tutkinnon osaan liittyvän tutkinnon osaa pienemmän kokonaisuuden nimi',
                modalOk: 'Lisää'
              }}
            />
            <LisääPaikallinenTutkinnonOsa
              lisättävätTutkinnonOsat={lisättävätTutkinnonOsat}
              paikallinenKoulutusmoduuli={
                paikalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenKoulutusmoduuli
              }
              addTutkinnonOsa={addTutkinnonOsa(
                paikalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenPrototype
              )}
              preferredTexts={{
                lisääOsaLink:
                  'Lisää paikalliseen tutkinnon osaan liittyvän tutkinnon osaa pienemmän kokonaisuuden suoritus',
                modalHeader:
                  'Paikalliseen tutkinnon osaan liittyvän tutkinnon osaa pienemmän kokonaisuuden lisäys',
                modalFieldLabel:
                  'Paikalliseen tutkinnon osaan liittyvän tutkinnon osaa pienemmän kokonaisuuden nimi',
                modalOk: 'Lisää'
              }}
              isPaikalliseenTutkinnonOsaanLiittyvänTutkinnonOsaaPienemmänKokonaisuudenSuoritus={
                true
              }
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
