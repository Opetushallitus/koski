import { FormModel, FormOptic } from '../components-v2/forms/FormModel'
import { AmmatillinenOpiskeluoikeus } from '../types/fi/oph/koski/schema/AmmatillinenOpiskeluoikeus'
import { OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus } from '../types/fi/oph/koski/schema/OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus'
import { localize, t } from '../i18n/i18n'
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
import { append, deleteAt } from '../util/fp/arrays'
import { hasAmmatillinenArviointi } from './OsasuoritusTables'
import { ParasArvosanaView } from '../components-v2/opiskeluoikeus/ArvosanaField'
import {
  isMuidenOpintovalmiuksiaTukevienOpintojenSuoritus,
  MuidenOpintovalmiuksiaTukevienOpintojenSuoritus
} from '../types/fi/oph/koski/schema/MuidenOpintovalmiuksiaTukevienOpintojenSuoritus'
import {
  isLukioOpintojenSuoritus,
  LukioOpintojenSuoritus
} from '../types/fi/oph/koski/schema/LukioOpintojenSuoritus'
import {
  OsasuoritusProperty,
  OsasuoritusPropertyValue
} from '../components-v2/opiskeluoikeus/OsasuoritusProperty'
import {
  KoodistoEdit,
  KoodistoView
} from '../components-v2/opiskeluoikeus/KoodistoField'
import {
  OsaamisenTunnustusView,
  TunnustusEdit
} from '../components-v2/opiskeluoikeus/TunnustusField'
import { OsaamisenTunnustaminen } from '../types/fi/oph/koski/schema/OsaamisenTunnustaminen'
import { FormListField } from '../components-v2/forms/FormListField'
import {
  emptyAmmatillisenTutkinnonOsanLisätieto,
  LisätietoEdit,
  LisätietoView
} from './LisätietoField'
import { ButtonGroup } from '../components-v2/containers/ButtonGroup'
import { FlatButton } from '../components-v2/controls/FlatButton'
import { ArviointiEdit, ArviointiView, emptyArviointi } from './Arviointi'
import { YhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus } from '../types/fi/oph/koski/schema/YhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus'

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
                content: (
                  <OsasuoritusProperties
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

type OsasuoritusPropertiesProps = {
  form: FormModel<AmmatillinenOpiskeluoikeus>
  osasuoritusPath: FormOptic<
    AmmatillinenOpiskeluoikeus,
    YhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus
  >
  osasuoritus: YhteistenTutkinnonOsienOsaAlueidenTaiLukioOpintojenTaiMuidenOpintovalmiuksiaTukevienOpintojenOsasuoritus
}

const OsasuoritusProperties = ({
  form,
  osasuoritusPath,
  osasuoritus
}: OsasuoritusPropertiesProps) => {
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
  } else if (isLukioTaiMuuOsasuoritus(osasuoritus)) {
    const lukioTaiMuuPath = osasuoritusPath as unknown as FormOptic<
      AmmatillinenOpiskeluoikeus,
      LukioTaiMuuOsasuoritus
    >

    return (
      <LukionJaMuunOsasoritusProperties
        form={form}
        osasuoritusPath={lukioTaiMuuPath}
        osasuoritus={osasuoritus}
      />
    )
  }

  return null
}

type LukioTaiMuuOsasuoritus =
  | LukioOpintojenSuoritus
  | MuidenOpintovalmiuksiaTukevienOpintojenSuoritus
const isLukioTaiMuuOsasuoritus = (a: unknown): a is LukioTaiMuuOsasuoritus =>
  isLukioOpintojenSuoritus(a) ||
  isMuidenOpintovalmiuksiaTukevienOpintojenSuoritus(a)

type LukionJaMuunOsasoritusPropertiesProps = {
  form: FormModel<AmmatillinenOpiskeluoikeus>
  osasuoritusPath: FormOptic<AmmatillinenOpiskeluoikeus, LukioTaiMuuOsasuoritus>
  osasuoritus: LukioTaiMuuOsasuoritus
}

const LukionJaMuunOsasoritusProperties = ({
  form,
  osasuoritusPath,
  osasuoritus
}: LukionJaMuunOsasoritusPropertiesProps) => {
  return (
    <>
      {(form.editMode || osasuoritus.suorituskieli) && (
        <OsasuoritusProperty label={'Suorituskieli'}>
          <OsasuoritusPropertyValue>
            <FormField
              form={form}
              view={KoodistoView}
              edit={KoodistoEdit}
              path={osasuoritusPath.prop('suorituskieli')}
              editProps={{ koodistoUri: 'kieli', zeroValueOption: true }}
            />
          </OsasuoritusPropertyValue>
        </OsasuoritusProperty>
      )}
      {(form.editMode || osasuoritus.tunnustettu) && (
        <OsasuoritusProperty label={'Tunnustettu'}>
          <OsasuoritusPropertyValue>
            <FormField
              form={form}
              path={osasuoritusPath.prop('tunnustettu')}
              view={
                OsaamisenTunnustusView /*TODO custom komponentti amikselle?*/
              }
              editProps={{
                createEmptyTunnustus: () =>
                  OsaamisenTunnustaminen({ selite: localize('') })
              }}
              edit={TunnustusEdit}
            />
          </OsasuoritusPropertyValue>
        </OsasuoritusProperty>
      )}
      {(form.editMode || osasuoritus.lisätiedot) && (
        <OsasuoritusProperty label={'Lisätiedot'}>
          <OsasuoritusPropertyValue>
            <FormListField
              removable
              form={form}
              view={LisätietoView}
              edit={LisätietoEdit}
              path={osasuoritusPath.prop('lisätiedot')}
            />
            {form.editMode && (
              <ButtonGroup>
                <FlatButton
                  onClick={() =>
                    form.updateAt(
                      osasuoritusPath.prop('lisätiedot').valueOr([]),
                      append(emptyAmmatillisenTutkinnonOsanLisätieto)
                    )
                  }
                >
                  {t('Lisää')}
                </FlatButton>
              </ButtonGroup>
            )}
          </OsasuoritusPropertyValue>
        </OsasuoritusProperty>
      )}
      <OsasuoritusProperty label={'Arviointi'}>
        <OsasuoritusPropertyValue>
          <FormListField
            removable
            form={form}
            view={ArviointiView}
            edit={ArviointiEdit}
            path={osasuoritusPath.prop('arviointi')}
          />
          {form.editMode && (
            <ButtonGroup>
              <FlatButton
                onClick={() =>
                  form.updateAt(
                    osasuoritusPath.prop('arviointi').valueOr([]),
                    append(emptyArviointi)
                  )
                }
              >
                {t('Lisää')}
              </FlatButton>
            </ButtonGroup>
          )}
        </OsasuoritusPropertyValue>
      </OsasuoritusProperty>
      {isMuidenOpintovalmiuksiaTukevienOpintojenSuoritus(osasuoritus) &&
        (form.editMode || osasuoritus.korotettu !== undefined) && (
          <OsasuoritusProperty label={'Korotettu suoritus'}>
            <OsasuoritusPropertyValue>
              <FormField
                form={form}
                view={KoodistoView}
                edit={KoodistoEdit}
                editProps={{
                  koodistoUri: 'ammatillisensuorituksenkorotus',
                  zeroValueOption: true
                }}
                path={(
                  osasuoritusPath as FormOptic<
                    AmmatillinenOpiskeluoikeus,
                    MuidenOpintovalmiuksiaTukevienOpintojenSuoritus
                  >
                ).prop('korotettu')}
              />
            </OsasuoritusPropertyValue>
          </OsasuoritusProperty>
        )}
    </>
  )
}
