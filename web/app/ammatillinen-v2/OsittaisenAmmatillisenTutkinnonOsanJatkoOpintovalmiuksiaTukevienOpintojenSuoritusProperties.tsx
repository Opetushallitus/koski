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
import { FormField } from '../components-v2/forms/FormField'
import {
  LaajuusEdit,
  LaajuusView
} from '../components-v2/opiskeluoikeus/LaajuusField'
import { LaajuusOsaamispisteissä } from '../types/fi/oph/koski/schema/LaajuusOsaamispisteissa'
import { deleteAt } from '../util/fp/arrays'
import { hasAmmatillinenArviointi } from './OsasuoritusTables'
import { ParasArvosanaView } from '../components-v2/opiskeluoikeus/ArvosanaField'

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
                  Laajuus: (
                    <FormField
                      form={form}
                      view={LaajuusView}
                      edit={LaajuusEdit}
                      editProps={{
                        createLaajuus: (arvo) =>
                          LaajuusOsaamispisteissä({ arvo })
                      }}
                      path={osasuoritusPath
                        .prop('osasuoritukset')
                        .valueOr([])
                        .at(index)
                        .prop('koulutusmoduuli')
                        .prop('laajuus')}
                    />
                  ),
                  Arvosana: <ParasArvosanaView value={s.arviointi} />
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
          onRemove={(rowIndex) => {
            form.updateAt(osasuoritusPath, (os) => {
              return {
                ...os,
                osasuoritukset: deleteAt(os.osasuoritukset || [], rowIndex)
              }
            })
          }}
          completed={(rowIndex) => {
            const s = (osasuoritus.osasuoritukset || [])[rowIndex]
            if (s === undefined) {
              return undefined
            }
            return hasAmmatillinenArviointi(s)
          }}
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
