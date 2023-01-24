import React, { useCallback, useMemo, useState } from 'react'
import { useConstraint } from '../../appstate/constraints'
import { localize, t } from '../../i18n/i18n'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { PaikallinenKoodi } from '../../types/fi/oph/koski/schema/PaikallinenKoodi'
import { allLanguages } from '../../util/optics'
import { CommonProps } from '../CommonProps'
import { Modal, ModalBody, ModalFooter, ModalTitle } from '../containers/Modal'
import { FlatButton } from '../controls/FlatButton'
import { RaisedButton } from '../controls/RaisedButton'
import { OptionList, Select, SelectOption } from '../controls/Select'
import { TextEdit, TextView } from '../controls/TextField'
import { FormField, sideUpdate } from '../forms/FormField'
import { useForm } from '../forms/FormModel'

const NEW_KEY = '__NEW__'

export type PaikallinenOsasuoritusSelectProps = CommonProps<{
  tunnisteet?: PaikallinenKoodi[]
  addNewText?: string | LocalizedString
  onSelect: (tunniste: PaikallinenKoodi, isNew: boolean) => void
  onRemove?: (tunniste: PaikallinenKoodi) => void
}>

export const PaikallinenOsasuoritusSelect: React.FC<
  PaikallinenOsasuoritusSelectProps
> = (props) => {
  const [modalIsVisible, setModalVisible] = useState(false)

  const hideModal = useCallback(() => {
    return setModalVisible(false)
  }, [])

  const options: OptionList<PaikallinenKoodi> = useMemo(
    () => [
      {
        key: NEW_KEY,
        label: t('Lisää osasuoritus'),
        value: emptyPaikallinenKoodi,
        ignoreFilter: true
      },
      ...(props.tunnisteet || []).map((tunniste) => ({
        key: tunniste.koodiarvo,
        label: t(tunniste.nimi),
        value: tunniste,
        removable: true
      }))
    ],
    [props.tunnisteet]
  )

  const onChange = useCallback(
    (option?: SelectOption<PaikallinenKoodi>) => {
      if (option?.key === NEW_KEY) {
        setModalVisible(true)
      } else if (option?.value) {
        props.onSelect(option.value, false)
      }
    },
    [props.onSelect]
  )

  const onCreateNew = useCallback(
    (tunniste: PaikallinenKoodi) => {
      setModalVisible(false)
      props.onSelect(tunniste, true)
    },
    [props.onSelect]
  )

  const onRemove = useCallback(
    (option: SelectOption<PaikallinenKoodi>) =>
      option.value && props.onRemove?.(option.value),
    [props.onRemove]
  )

  return (
    <>
      <Select
        placeholder={props.addNewText || t('Lisää osasuoritus')}
        options={options}
        hideEmpty
        onChange={onChange}
        onRemove={onRemove}
      />
      {modalIsVisible && (
        <UusiOsasuoritusModal onClose={hideModal} onSubmit={onCreateNew} />
      )}
    </>
  )
}

const emptyPaikallinenKoodi = PaikallinenKoodi({
  koodiarvo: '',
  nimi: localize('')
})

type UusiOsasuoritusModalProps = {
  onClose: () => void
  onSubmit: (paikallinenKoodi: PaikallinenKoodi) => void
}

const UusiOsasuoritusModal: React.FC<UusiOsasuoritusModalProps> = (props) => {
  const constraint = useConstraint('PaikallinenKoodi')
  const form = useForm(emptyPaikallinenKoodi, true, constraint)
  const koodiarvoPath = form.root.prop('koodiarvo')

  const onSubmit = useCallback(() => {
    props.onSubmit(form.state)
  }, [props.onSubmit, form.state])

  const updateOsasuoritusNimi = useMemo(
    () =>
      sideUpdate<PaikallinenKoodi, string, string>(
        form.root.prop('nimi').compose(allLanguages),
        (koodiarvo) => koodiarvo || ''
      ),
    []
  )

  return (
    <Modal onSubmit={onSubmit} onClose={props.onClose}>
      <ModalTitle>{t('Lisää osasuoritus')}</ModalTitle>
      <ModalBody>
        <FormField
          form={form}
          path={koodiarvoPath}
          updateAlso={[updateOsasuoritusNimi]}
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
