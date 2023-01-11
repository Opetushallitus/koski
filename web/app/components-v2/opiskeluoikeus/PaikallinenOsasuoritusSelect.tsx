import React, { useCallback, useMemo, useState } from 'react'
import { useConstraint } from '../../appstate/constraints'
import { t } from '../../i18n/i18n'
import { LocalizedString } from '../../types/common'
import { Finnish } from '../../types/fi/oph/koski/schema/Finnish'
import { PaikallinenKoodi } from '../../types/fi/oph/koski/schema/PaikallinenKoodi'
import { allLanguages } from '../../util/optics'
import { Modal, ModalBody, ModalFooter, ModalTitle } from '../containers/Modal'
import { FlatButton } from '../controls/FlatButton'
import { RaisedButton } from '../controls/RaisedButton'
import { GroupedOptions, Select, SelectOption } from '../controls/Select'
import { TextEdit, TextView } from '../controls/TextField'
import { FormField } from '../forms/FormField'
import { useForm } from '../forms/FormModel'

const NEW_KEY = '__NEW__'

export type PaikallinenOsasuoritusSelectProps = {
  addNewText?: string | LocalizedString
  onSelect: (tunniste: PaikallinenKoodi) => void
}

export const PaikallinenOsasuoritusSelect: React.FC<
  PaikallinenOsasuoritusSelectProps
> = (props) => {
  const [modalIsVisible, setModalVisible] = useState(false)

  const hideModal = useCallback(() => {
    return setModalVisible(false)
  }, [])

  const options: GroupedOptions<PaikallinenKoodi> = useMemo(
    () => ({
      paikallinen: [
        {
          key: NEW_KEY,
          label: t('Uusi osasuoritus'),
          value: emptyPaikallinenKoodi
        }
      ]
    }),
    []
  )

  const onChange = useCallback(
    (option?: SelectOption<PaikallinenKoodi>) => {
      if (option?.key === NEW_KEY) {
        setModalVisible(true)
      } else if (option) {
        props.onSelect(option.value)
      }
    },
    [props.onSelect]
  )

  const onCreateNew = useCallback(
    (tunniste: PaikallinenKoodi) => {
      setModalVisible(false)
      props.onSelect(tunniste)
    },
    [props.onSelect]
  )

  return (
    <>
      <Select
        placeholder={props.addNewText || t('Lisää osasuoritus')}
        options={options}
        onChange={onChange}
      />
      {modalIsVisible && (
        <UusiOsasuoritusModal onClose={hideModal} onSubmit={onCreateNew} />
      )}
    </>
  )
}

const emptyPaikallinenKoodi = PaikallinenKoodi({
  koodiarvo: '',
  nimi: Finnish({ fi: '' })
})

type UusiOsasuoritusModalProps = {
  onClose: () => void
  onSubmit: (paikallinenKoodi: PaikallinenKoodi) => void
}

const UusiOsasuoritusModal: React.FC<UusiOsasuoritusModalProps> = (props) => {
  const constraint = useConstraint('PaikallinenKoodi')
  const form = useForm(emptyPaikallinenKoodi, true, constraint)
  const koodiarvoPath = form.root.prop('koodiarvo')
  const nimiPath = form.root.prop('nimi').compose(allLanguages)

  const onSubmit = useCallback(() => {
    props.onSubmit(form.state)
  }, [props.onSubmit, form.state])

  return (
    <Modal onSubmit={onSubmit} onClose={props.onClose}>
      <ModalTitle>{t('Uusi osasuoritus')}</ModalTitle>
      <ModalBody>
        <FormField
          form={form}
          path={koodiarvoPath}
          updateAlso={[nimiPath]}
          errorsFromPath="nimi"
          view={TextView}
          edit={(props) => (
            <TextEdit
              {...props}
              placeholder={t('Osasuorituksen nimi')}
              autoFocus
            />
          )}
        />
      </ModalBody>
      <ModalFooter>
        <FlatButton onClick={props.onClose}>Peruuta</FlatButton>
        <RaisedButton disabled={!form.isValid}>Lisää</RaisedButton>
      </ModalFooter>
    </Modal>
  )
}
