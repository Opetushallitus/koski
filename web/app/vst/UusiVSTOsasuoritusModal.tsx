import React, { useState } from 'react'
import {
  KeyValueTable,
  KeyValueRow
} from '../components-v2/containers/KeyValueTable'
import {
  Modal,
  ModalTitle,
  ModalBody,
  ModalFooter
} from '../components-v2/containers/Modal'
import { FlatButton } from '../components-v2/controls/FlatButton'
import { RaisedButton } from '../components-v2/controls/RaisedButton'
import { TextEdit } from '../components-v2/controls/TextField'
import { localize, t } from '../i18n/i18n'
import { ActivePäätasonSuoritus } from '../components-v2/containers/EditorContainer'
import { VapaanSivistystyönOpiskeluoikeus } from '../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { isOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022 } from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022'
import { Select } from '../components-v2/controls/Select'
import { useKoodisto } from '../appstate/koodisto'
import { PaikallinenKoodi } from '../types/fi/oph/koski/schema/PaikallinenKoodi'
import { Koodistokoodiviite } from '../types/fi/oph/koski/schema/Koodistokoodiviite'
import { isVapaanSivistystyönLukutaitokoulutuksenSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonLukutaitokoulutuksenSuoritus'
import { VSTOsasuoritusOsasuorituksilla } from './typeguards'
import { FormOptic, getValue } from '../components-v2/forms/FormModel'
import { VapaanSivistystyönPäätasonSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonPaatasonSuoritus'

interface UusiVSTOsasuoritusModalProps<T> {
  päätasonSuoritus: ActivePäätasonSuoritus<VapaanSivistystyönOpiskeluoikeus>
  data: {
    osasuoritusPath: FormOptic<
      VapaanSivistystyönPäätasonSuoritus,
      VSTOsasuoritusOsasuorituksilla
    >
  } | null
  onSubmit: (data: PaikallinenKoodi | Koodistokoodiviite) => void
  onCancel: () => void
}

const Koto2022ValinnaisetOpinnotOsasuoritusModal: React.FC<any> = () => {
  return <div />
}

const UusiVSTKoto2022OsasuoritusModal: React.FC<
  UusiVSTOsasuoritusModalProps<{ osaAlue: string }>
> = (props) => {
  const koodisto = useKoodisto('vstkoto2022kokonaisuus')
  const [osaAlue, setOsaAlue] = useState<
    Koodistokoodiviite<'vstkoto2022kokonaisuus'> | undefined
  >()
  return (
    <Modal>
      <ModalTitle>{t('Osa-alan lisäys')}</ModalTitle>
      <ModalBody>
        <KeyValueTable>
          <KeyValueRow label="Osa-ala">
            <Select
              onChange={(opt) => {
                if (opt && opt.value) {
                  setOsaAlue(opt.value)
                } else {
                  console.warn('Warning: received no value for osa-ala')
                }
              }}
              value={osaAlue?.koodiarvo}
              options={(koodisto || []).map((option) => ({
                key: option.koodiviite.koodiarvo,
                label: t(option.koodiviite.nimi),
                value: Koodistokoodiviite({
                  koodiarvo: option.koodiviite.koodiarvo,
                  koodistoUri: option.koodiviite.koodistoUri,
                  nimi: option.koodiviite.nimi
                })
              }))}
            />
          </KeyValueRow>
        </KeyValueTable>
      </ModalBody>
      <ModalFooter>
        <FlatButton
          onClick={(e) => {
            e?.preventDefault()
            props.onCancel()
          }}
        >
          {'Peruuta'}
        </FlatButton>
        <RaisedButton
          onClick={(e) => {
            e?.preventDefault()
            if (osaAlue !== undefined) {
              props.onSubmit(osaAlue)
            }
          }}
        >
          {'Lisää osa-ala'}
        </RaisedButton>
      </ModalFooter>
    </Modal>
  )
}

const UusiVSTLukutaitokoulutusModal: React.FC<
  UusiVSTOsasuoritusModalProps<{ osaAlue: string }>
> = (props) => {
  const koodisto = useKoodisto('vstlukutaitokoulutuksenkokonaisuus')
  const [value, setValue] = useState<
    Koodistokoodiviite<'vstlukutaitokoulutuksenkokonaisuus'> | undefined
  >()
  return (
    <Modal>
      <ModalTitle>{t('Kokonaisuuden lisäys')}</ModalTitle>
      <ModalBody>
        <KeyValueTable>
          <KeyValueRow label="Kokonaisuus">
            <Select
              onChange={(opt) => {
                if (opt && opt.value) {
                  setValue(opt.value)
                } else {
                  console.warn('Warning: received no value for kokonaisuus')
                }
              }}
              value={value?.koodiarvo}
              options={(koodisto || []).map((option) => ({
                key: option.koodiviite.koodiarvo,
                label: t(option.koodiviite.nimi),
                value: Koodistokoodiviite({
                  koodiarvo: option.koodiviite.koodiarvo,
                  koodistoUri: option.koodiviite.koodistoUri,
                  nimi: option.koodiviite.nimi
                })
              }))}
            />
          </KeyValueRow>
        </KeyValueTable>
      </ModalBody>
      <ModalFooter>
        <FlatButton
          onClick={(e) => {
            e?.preventDefault()
            props.onCancel()
          }}
        >
          {'Peruuta'}
        </FlatButton>
        <RaisedButton
          onClick={(e) => {
            e?.preventDefault()
            if (value !== undefined) {
              console.log('value', value)
              props.onSubmit(value)
            }
          }}
        >
          {'Lisää kokonaisuus'}
        </RaisedButton>
      </ModalFooter>
    </Modal>
  )
}

export const UusiVSTOsasuoritusModal: React.FC<
  UusiVSTOsasuoritusModalProps<{ opintokokonaisuus: string }>
> = (props) => {
  const [opintokokonaisuus, setOpintokokonaisuus] = useState('')
  const { data } = props
  if (data?.osasuoritusPath !== undefined) {
    const val = getValue(data.osasuoritusPath)(props.päätasonSuoritus.suoritus)
    console.log(val)
  }
  if (
    isOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022(
      props.päätasonSuoritus.suoritus
    )
  ) {
    return (
      <UusiVSTKoto2022OsasuoritusModal
        data={data}
        onCancel={props.onCancel}
        onSubmit={props.onSubmit}
        päätasonSuoritus={props.päätasonSuoritus}
      />
    )
  }
  if (
    isVapaanSivistystyönLukutaitokoulutuksenSuoritus(
      props.päätasonSuoritus.suoritus
    )
  ) {
    return (
      <UusiVSTLukutaitokoulutusModal
        data={data}
        onCancel={props.onCancel}
        onSubmit={props.onSubmit}
        päätasonSuoritus={props.päätasonSuoritus}
      />
    )
  }

  // TODO: Refaktoroi Dialogiksi
  return (
    <Modal>
      <ModalTitle>{t('Osasuorituksen lisäys')}</ModalTitle>
      <ModalBody>
        <KeyValueTable>
          <KeyValueRow label="Opintokokonaisuus">
            <TextEdit
              value={opintokokonaisuus}
              onChange={(e) => setOpintokokonaisuus(e || '')}
              placeholder={'Opintokokonaisuus'}
            />
          </KeyValueRow>
        </KeyValueTable>
      </ModalBody>
      <ModalFooter>
        <FlatButton
          onClick={(e) => {
            e?.preventDefault()
            props.onCancel()
          }}
        >
          {'Peruuta'}
        </FlatButton>
        <RaisedButton
          onClick={(e) => {
            e?.preventDefault()
            props.onSubmit(
              PaikallinenKoodi({
                koodiarvo: opintokokonaisuus,
                nimi: localize(opintokokonaisuus)
              })
            )
          }}
        >
          {'Lisää osasuoritus'}
        </RaisedButton>
      </ModalFooter>
    </Modal>
  )
}
