import { FormModel, FormOptic } from '../components-v2/forms/FormModel'
import { AmmatillinenOpiskeluoikeus } from '../types/fi/oph/koski/schema/AmmatillinenOpiskeluoikeus'
import { OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus } from '../types/fi/oph/koski/schema/OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus'
import { t } from '../i18n/i18n'
import { OsasuoritusTable } from '../components-v2/opiskeluoikeus/OsasuoritusTable'
import React from 'react'
import { KorkeakouluopintojenSuoritusProperties } from './KorkeakouluopintojenSuoritusProperties'
import { FormField } from '../components-v2/forms/FormField'
import {
  LaajuusEdit,
  LaajuusView
} from '../components-v2/opiskeluoikeus/LaajuusField'
import { LaajuusOsaamispisteissä } from '../types/fi/oph/koski/schema/LaajuusOsaamispisteissa'

export type OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritusPropertiesProps =
  {
    form: FormModel<AmmatillinenOpiskeluoikeus>
    osasuoritusPath: FormOptic<
      AmmatillinenOpiskeluoikeus,
      OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus
    >
    osasuoritus: OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus
  }

export const OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritusProperties =
  ({
    form,
    osasuoritusPath,
    osasuoritus
  }: OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritusPropertiesProps) => {
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
                        .prop('koulutusmoduuli')
                        .prop('laajuus')}
                    />
                  )
                },
                content: (
                  <KorkeakouluopintojenSuoritusProperties
                    form={form}
                    osasuoritusPath={osasuoritusPath
                      .prop('osasuoritukset')
                      .valueOr([])
                      .at(index)}
                    osasuoritus={s}
                  />
                ),
                expandable: true
              }
            }) || []
          }
        />
      </>
    )
  }
