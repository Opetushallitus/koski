import { FormModel, FormOptic } from '../components-v2/forms/FormModel'
import { AmmatillinenOpiskeluoikeus } from '../types/fi/oph/koski/schema/AmmatillinenOpiskeluoikeus'
import { OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus } from '../types/fi/oph/koski/schema/OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus'
import { localize, t } from '../i18n/i18n'
import { OsasuoritusTable } from '../components-v2/opiskeluoikeus/OsasuoritusTable'
import { YhteisenTutkinnonOsanOsaAlueenSuoritusProperties } from './YhteisenTutkinnonOsanOsaAlueenSuoritusProperties'
import React, { useState } from 'react'
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
  OsaamisenTunnustusEdit,
  OsaamisenTunnustusView
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
import { Column, ColumnRow } from '../components-v2/containers/Columns'
import { KoodistoSelect } from '../components-v2/opiskeluoikeus/KoodistoSelect'
import { newYhteisenOsanOsaAlueenSuoritus } from './YhteisenOsittaisenAmmatillisenTutkinnonOsasuoritusProperties'
import {
  Modal,
  ModalBody,
  ModalFooter,
  ModalTitle
} from '../components-v2/containers/Modal'
import { TextEdit } from '../components-v2/controls/TextField'
import { RaisedButton } from '../components-v2/controls/RaisedButton'
import { PaikallinenLukionOpinto } from '../types/fi/oph/koski/schema/PaikallinenLukionOpinto'
import { PaikallinenKoodi } from '../types/fi/oph/koski/schema/PaikallinenKoodi'
import { PaikallinenOpintovalmiuksiaTukevaOpinto } from '../types/fi/oph/koski/schema/PaikallinenOpintovalmiuksiaTukevaOpinto'
import { TestIdLayer } from '../appstate/useTestId'

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
          addNewOsasuoritusView={() => (
            <NewOsasuoritus form={form} suoritusPath={osasuoritusPath} />
          )}
        />
      </>
    )
  }

type NewOsasuoritusProps = {
  form: FormModel<AmmatillinenOpiskeluoikeus>
  suoritusPath: FormOptic<
    AmmatillinenOpiskeluoikeus,
    OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus
  >
}

const NewOsasuoritus = ({ form, suoritusPath }: NewOsasuoritusProps) => {
  return (
    <ColumnRow indent={2}>
      <Column span={6}>
        <NewYhteisenTutkinnonOsanOsaAlueenSuoritus
          form={form}
          suoritusPath={suoritusPath}
        />
      </Column>
      <Column span={4}>
        <NewLukioOpinto form={form} suoritusPath={suoritusPath} />
      </Column>
      <Column span={6}>
        <NewMuidenOpintovalmiuksiaTukevienOpintojenSuoritus
          form={form}
          suoritusPath={suoritusPath}
        />
      </Column>
    </ColumnRow>
  )
}

type NewYhteisenTutkinnonOsanOsaAlueenSuoritusProps = {
  form: FormModel<AmmatillinenOpiskeluoikeus>
  suoritusPath: FormOptic<
    AmmatillinenOpiskeluoikeus,
    OsittaisenAmmatillisenTutkinnonOsanJatkoOpintovalmiuksiaTukevienOpintojenSuoritus
  >
}

const NewYhteisenTutkinnonOsanOsaAlueenSuoritus = ({
  form,
  suoritusPath
}: NewYhteisenTutkinnonOsanOsaAlueenSuoritusProps) => {
  return (
    <KoodistoSelect
      addNewText="Lisää tutkinnon osan osa-alue"
      koodistoUri="ammatillisenoppiaineet"
      format={(osa) => osa.koodiarvo + ' ' + t(osa.nimi)}
      onSelect={(tunniste) => {
        tunniste &&
          form.updateAt(
            suoritusPath.prop('osasuoritukset').valueOr([]),
            (a) => [...a, newYhteisenOsanOsaAlueenSuoritus(tunniste)]
          )
      }}
      testId="uusi-yhteinen-osan-osa-alue"
    />
  )
}

const NewLukioOpinto = ({
  form,
  suoritusPath
}: NewYhteisenTutkinnonOsanOsaAlueenSuoritusProps) => {
  const [showModal, setShowModal] = useState(false)

  return (
    <>
      <FlatButton onClick={() => setShowModal(true)}>
        {t('Lisää lukio-opinto')}
      </FlatButton>
      {showModal && (
        <NewLukioOpintoModal
          onClose={() => setShowModal(false)}
          onSubmit={(nimi, peruste) => {
            form.updateAt(
              suoritusPath.prop('osasuoritukset').valueOr([]),
              (o) => [...o, newLukioOpinto(nimi, peruste)]
            )
          }}
        />
      )}
    </>
  )
}

const newLukioOpinto = (
  nimi: string,
  peruste: string
): LukioOpintojenSuoritus => {
  return LukioOpintojenSuoritus({
    koulutusmoduuli: PaikallinenLukionOpinto({
      tunniste: PaikallinenKoodi({ koodiarvo: nimi, nimi: localize(nimi) }),
      kuvaus: localize(nimi),
      perusteenDiaarinumero: peruste
    })
  })
}

type NewLukioOpintoModalProps = {
  onClose: () => void
  onSubmit: (nimi: string, peruste: string) => void
}

const NewLukioOpintoModal = ({
  onClose,
  onSubmit
}: NewLukioOpintoModalProps) => {
  const [nimi, setNimi] = useState('')
  const [peruste, setPeruste] = useState('')

  return (
    <Modal onClose={onClose}>
      <ModalTitle>{t('Lisää lukio-opinto')}</ModalTitle>
      <ModalBody>
        <label>
          {t('Nimi')}
          <TextEdit onChange={(n) => setNimi(n ? n : '')} value={nimi} />
        </label>
        <label>
          {t('Peruste')}
          <TextEdit onChange={(p) => setPeruste(p ? p : '')} />
        </label>
      </ModalBody>
      <ModalFooter>
        <FlatButton onClick={onClose} testId="cancel">
          {t('Peruuta')}
        </FlatButton>
        <RaisedButton onClick={() => onSubmit(nimi, peruste)}>
          {t('Lisää')}
        </RaisedButton>
      </ModalFooter>
    </Modal>
  )
}

const NewMuidenOpintovalmiuksiaTukevienOpintojenSuoritus = ({
  form,
  suoritusPath
}: NewOsasuoritusProps) => {
  const [showModal, setShowModal] = useState(false)

  return (
    <>
      <FlatButton onClick={() => setShowModal(true)}>
        {t('Lisää muu opintovalmiuksia tukeva opinto')}
      </FlatButton>
      {showModal && (
        <NewMuidenOpintovalmiuksiaTukevienOpintojenSuoritusModal
          onClose={() => setShowModal(false)}
          onSubmit={(nimi) => {
            form.updateAt(
              suoritusPath.prop('osasuoritukset').valueOr([]),
              (o) => [
                ...o,
                newMuidenOpintovalmiuksiaTukevienOpintojenSuoritus(nimi)
              ]
            )
          }}
        />
      )}
    </>
  )
}

const newMuidenOpintovalmiuksiaTukevienOpintojenSuoritus = (
  nimi: string
): MuidenOpintovalmiuksiaTukevienOpintojenSuoritus => {
  return MuidenOpintovalmiuksiaTukevienOpintojenSuoritus({
    koulutusmoduuli: PaikallinenOpintovalmiuksiaTukevaOpinto({
      tunniste: PaikallinenKoodi({ koodiarvo: nimi, nimi: localize(nimi) }),
      kuvaus: localize(nimi)
    })
  })
}

type NewMuidenOpintovalmiuksiaTukevienOpintojenSuoritusModalProps = {
  onClose: () => void
  onSubmit: (nimi: string) => void
}

const NewMuidenOpintovalmiuksiaTukevienOpintojenSuoritusModal = ({
  onClose,
  onSubmit
}: NewMuidenOpintovalmiuksiaTukevienOpintojenSuoritusModalProps) => {
  const [nimi, setNimi] = useState('')

  return (
    <Modal onClose={onClose}>
      <ModalTitle>{t('Lisää muu opintovalmiuksia tukeva opinto')}</ModalTitle>
      <ModalBody>
        <label>
          {t('Nimi')}
          <TextEdit onChange={(n) => setNimi(n ? n : '')} value={nimi} />
        </label>
      </ModalBody>
      <ModalFooter>
        <FlatButton onClick={onClose} testId="cancel">
          {t('Peruuta')}
        </FlatButton>
        <RaisedButton onClick={() => onSubmit(nimi)}>{t('Lisää')}</RaisedButton>
      </ModalFooter>
    </Modal>
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
              view={OsaamisenTunnustusView}
              editProps={{
                createEmptyTunnustus: () =>
                  OsaamisenTunnustaminen({ selite: localize('') })
              }}
              edit={OsaamisenTunnustusEdit}
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
          <TestIdLayer id="arviointi">
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
                  testId="lisää-arviointi"
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
          </TestIdLayer>
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
