import React, { useCallback, useMemo, useState } from 'react'
import { useSchema } from '../../appstate/constraints'
import { localize, t } from '../../i18n/i18n'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { PaikallinenKoodi } from '../../types/fi/oph/koski/schema/PaikallinenKoodi'
import { allLanguages } from '../../util/optics'
import { common, CommonProps } from '../CommonProps'
import { Modal, ModalBody, ModalFooter, ModalTitle } from '../containers/Modal'
import { FlatButton } from '../controls/FlatButton'
import { RaisedButton } from '../controls/RaisedButton'
import { OptionList, Select, SelectOption } from '../controls/Select'
import { TextEdit, TextView } from '../controls/TextField'
import { FormField, sideUpdate } from '../forms/FormField'
import { useForm } from '../forms/FormModel'
import { TestIdLayer, useTestId } from '../../appstate/useTestId'

const NEW_KEY = '__NEW__'

export type PaikallinenOsasuoritusSelectProps = CommonProps<{
  tunnisteet?: PaikallinenKoodi[]
  addNewText?: string | LocalizedString
  labelText?: string
  modalTitle?: string
  namePlaceholder?: string
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
        label: props.labelText ? props.labelText : t('Lisää osasuoritus'),
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
    [props.labelText, props.tunnisteet]
  )

  const { onSelect, modalTitle, namePlaceholder } = props
  const onChangeCB = useCallback(
    (option?: SelectOption<PaikallinenKoodi>) => {
      if (option?.key === NEW_KEY) {
        setModalVisible(true)
      } else if (option?.value) {
        onSelect(option.value, false)
      }
    },
    [onSelect]
  )

  const onCreateNew = useCallback(
    (tunniste: PaikallinenKoodi) => {
      setModalVisible(false)
      onSelect(tunniste, true)
    },
    [onSelect]
  )

  const { onRemove } = props
  const onRemoveCB = useCallback(
    (option: SelectOption<PaikallinenKoodi>) =>
      option.value && onRemove?.(option.value),
    [onRemove]
  )

  return (
    <TestIdLayer id="addOsasuoritus">
      <Select
        placeholder={props.addNewText || t('Lisää osasuoritus')}
        options={options}
        hideEmpty
        onChange={onChangeCB}
        onRemove={onRemoveCB}
        testId="select"
      />
      {modalIsVisible && (
        <UusiOsasuoritusModal
          title={modalTitle}
          placeholder={namePlaceholder}
          onClose={hideModal}
          onSubmit={onCreateNew}
        />
      )}
    </TestIdLayer>
  )
}

const emptyPaikallinenKoodi = PaikallinenKoodi({
  koodiarvo: '',
  nimi: localize('')
})

export type UusiOsasuoritusModalProps = CommonProps<{
  onClose: () => void
  title?: string
  placeholder?: string
  onSubmit: (paikallinenKoodi: PaikallinenKoodi) => void
}>

export const UusiOsasuoritusModal: React.FC<UusiOsasuoritusModalProps> = (
  props
) => {
  const paikallinenKoodiSchema = useSchema('PaikallinenKoodi')
  const form = useForm(emptyPaikallinenKoodi, true, paikallinenKoodiSchema)
  const koodiarvoPath = form.root.prop('koodiarvo')

  const { onSubmit } = props
  const onSubmitCB = useCallback(() => {
    onSubmit(form.state)
  }, [onSubmit, form.state])

  const updateOsasuoritusNimi = useMemo(
    () =>
      sideUpdate<PaikallinenKoodi, string, string>(
        form.root.prop('nimi').compose(allLanguages),
        (koodiarvo) => koodiarvo || ''
      ),
    [form.root]
  )

  return (
    <Modal {...common(props)} onClose={props.onClose}>
      <ModalTitle>
        {props.title ? props.title : t('Lisää osasuoritus')}
      </ModalTitle>
      <ModalBody>
        <FormField
          form={form}
          path={koodiarvoPath}
          updateAlso={[updateOsasuoritusNimi]}
          errorsFromPath="nimi"
          view={TextView}
          edit={TextEdit}
          editProps={{
            placeholder: props.placeholder
              ? props.placeholder
              : t('Osasuorituksen nimi')
          }}
          testId="nimi"
        />
      </ModalBody>
      <ModalFooter>
        <FlatButton onClick={props.onClose} testId="cancel">
          {t('Peruuta')}
        </FlatButton>
        <RaisedButton
          disabled={!form.isValid}
          onClick={onSubmitCB}
          testId="submit"
        >
          {t('Lisää')}
        </RaisedButton>
      </ModalFooter>
    </Modal>
  )
}
