import { FormModel, FormOptic } from '../components-v2/forms/FormModel'
import { AmmatillinenOpiskeluoikeus } from '../types/fi/oph/koski/schema/AmmatillinenOpiskeluoikeus'
import { OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus } from '../types/fi/oph/koski/schema/OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus'
import { t } from '../i18n/i18n'
import { OsasuoritusTable } from '../components-v2/opiskeluoikeus/OsasuoritusTable'
import { YhteisenTutkinnonOsanOsaAlueenSuoritusProperties } from './YhteisenTutkinnonOsanOsaAlueenSuoritusProperties'
import React from 'react'
import {
  isYhteisenTutkinnonOsanOsaAlueenSuoritus,
  YhteisenTutkinnonOsanOsaAlueenSuoritus
} from '../types/fi/oph/koski/schema/YhteisenTutkinnonOsanOsaAlueenSuoritus'

export type OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritusPropertiesProps =
  {
    form: FormModel<AmmatillinenOpiskeluoikeus>
    osasuoritusPath: FormOptic<
      AmmatillinenOpiskeluoikeus,
      OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus
    >
    osasuoritus: OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus
  }

export const OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritusProperties =
  ({
    form,
    osasuoritusPath,
    osasuoritus
  }: OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritusPropertiesProps) => {
    return (
      <>
        <OsasuoritusTable
          editMode={form.editMode}
          rows={
            osasuoritus.osasuoritukset?.map((s, index) => {
              return {
                suoritusIndex: 1,
                osasuoritusIndex: index,
                columns: {
                  'Osa-alue': t(s.koulutusmoduuli.tunniste.nimi),
                  Laajuus:
                    s.koulutusmoduuli.laajuus &&
                    `${s.koulutusmoduuli.laajuus?.arvo} ${t(s.koulutusmoduuli.laajuus?.yksikkö.lyhytNimi)}`
                },
                content: OsasuoritusProperties({
                  form,
                  osasuoritusPath,
                  osasuoritus
                }),
                expandable: true
              }
            }) || []
          }
        />
      </>
    )
  }

const OsasuoritusProperties = ({
  form,
  osasuoritusPath,
  osasuoritus
}: OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritusPropertiesProps) => {
  if (isYhteisenTutkinnonOsanOsaAlueenSuoritus(osasuoritus)) {
    const yhteinenPath = osasuoritusPath as unknown as FormOptic<
      AmmatillinenOpiskeluoikeus,
      YhteisenTutkinnonOsanOsaAlueenSuoritus
    >

    return (
      <YhteisenTutkinnonOsanOsaAlueenSuoritusProperties
        form={form}
        osasuoritusPath={yhteinenPath}
        osasuoritus={osasuoritus}
      />
    )
  }
}
