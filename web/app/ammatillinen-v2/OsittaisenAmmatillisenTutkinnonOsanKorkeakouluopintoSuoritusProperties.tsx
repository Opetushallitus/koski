import { FormModel, FormOptic } from '../components-v2/forms/FormModel'
import { AmmatillinenOpiskeluoikeus } from '../types/fi/oph/koski/schema/AmmatillinenOpiskeluoikeus'
import { OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus } from '../types/fi/oph/koski/schema/OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus'
import { finnish, t } from '../i18n/i18n'
import { OsasuoritusTable } from '../components-v2/opiskeluoikeus/OsasuoritusTable'
import React, { useState } from 'react'
import { KorkeakouluopintojenSuoritusProperties } from './KorkeakouluopintojenSuoritusProperties'
import { FormField } from '../components-v2/forms/FormField'
import {
  LaajuusEdit,
  LaajuusView
} from '../components-v2/opiskeluoikeus/LaajuusField'
import { LaajuusOsaamispisteissä } from '../types/fi/oph/koski/schema/LaajuusOsaamispisteissa'
import { deleteAt } from '../util/fp/arrays'
import { hasAmmatillinenArviointi } from './OsasuoritusTables'
import { FlatButton } from '../components-v2/controls/FlatButton'
import {
  Modal,
  ModalBody,
  ModalFooter,
  ModalTitle
} from '../components-v2/containers/Modal'
import { TextEdit } from '../components-v2/controls/TextField'
import { RaisedButton } from '../components-v2/controls/RaisedButton'
import { PaikallinenKoodi } from '../types/fi/oph/koski/schema/PaikallinenKoodi'
import { Column, ColumnRow } from '../components-v2/containers/Columns'
import { KorkeakouluopintojenTutkinnonOsaaPienempiKokonaisuus } from '../types/fi/oph/koski/schema/KorkeakouluopintojenTutkinnonOsaaPienempiKokonaisuus'
import { KorkeakouluopintojenSuoritus } from '../types/fi/oph/koski/schema/KorkeakouluopintojenSuoritus'
import { AmisArvosanaInTableEdit, AmisArvosanaInTableView } from './Arviointi'

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
                        .prop('osasuoritukset')
                        .valueOr([])
                        .at(index)
                        .prop('koulutusmoduuli')
                        .prop('laajuus')}
                    />
                  ),
                  Arvosana: (
                    <FormField
                      form={form}
                      view={AmisArvosanaInTableView}
                      edit={AmisArvosanaInTableEdit}
                      path={osasuoritusPath
                        .prop('osasuoritukset')
                        .valueOr([])
                        .at(index)
                        .prop('arviointi')}
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
            <ColumnRow indent={2}>
              <Column span={12}>
                <NewKorkeakouluopintokokonaisuus
                  form={form}
                  suoritusPath={osasuoritusPath}
                />
              </Column>
            </ColumnRow>
          )}
        />
      </>
    )
  }

type NewKorkeakouluopintokokonaisuusProps = {
  form: FormModel<AmmatillinenOpiskeluoikeus>
  suoritusPath: FormOptic<
    AmmatillinenOpiskeluoikeus,
    OsittaisenAmmatillisenTutkinnonOsanKorkeakouluopintoSuoritus
  >
}

const NewKorkeakouluopintokokonaisuus = ({
  form,
  suoritusPath
}: NewKorkeakouluopintokokonaisuusProps) => {
  const [showModal, setShowModal] = useState(false)

  return (
    <>
      <FlatButton onClick={() => setShowModal(true)}>
        {t('Lisää korkeakouluopintokokonaisuus')}
      </FlatButton>
      {showModal && (
        <NewKorkeakouluopintokokonaisuusModal
          onClose={() => setShowModal(false)}
          onSubmit={(nimi) => {
            form.updateAt(
              suoritusPath.prop('osasuoritukset').valueOr([]),
              (o) => [...o, newKorkeakouluopintokokonaisuus(nimi)]
            )
          }}
        />
      )}
    </>
  )
}

type NewKorkeakouluopintokokonaisuusModalProps = {
  onClose: () => void
  onSubmit: (nimi: string) => void
}

const NewKorkeakouluopintokokonaisuusModal = ({
  onClose,
  onSubmit
}: NewKorkeakouluopintokokonaisuusModalProps) => {
  const [nimi, setNimi] = useState('')

  return (
    <Modal onClose={onClose}>
      <ModalTitle>{t('Lisää korkeakouluopintokokonaisuus')}</ModalTitle>
      <ModalBody>
        <label>
          {t('Nimi')}
          <TextEdit onChange={(o) => setNimi(o ? o : '')} value={nimi} />
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

const newKorkeakouluopintokokonaisuus = (
  nimi: string
): KorkeakouluopintojenSuoritus => {
  return KorkeakouluopintojenSuoritus({
    koulutusmoduuli: KorkeakouluopintojenTutkinnonOsaaPienempiKokonaisuus({
      tunniste: PaikallinenKoodi({
        koodiarvo: nimi,
        nimi: finnish(nimi)
      }),
      kuvaus: finnish(nimi)
    })
  })
}
