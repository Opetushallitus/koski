import React from 'baret'
import Http from '../util/http'
import ModalDialog from '../editor/ModalDialog'
import Text from '../i18n/Text'
import Checkbox from '../components/Checkbox'
import Atom from 'bacon.atom'

export const SuostumuksenPeruutusPopup = ({ opiskeluoikeusOid, onDismiss }) => {
  const checkboxAtom = Atom(false)

  return (
    <ModalDialog
      className="peru-suostumus-popup-modal"
      onDismiss={onDismiss}
      onSubmit={() => {
        Http.post(
          `/koski/api/opiskeluoikeus/suostumuksenperuutus/${opiskeluoikeusOid}`
        )
          .doError(() => {})
          .onValue(() => window.location.reload(true))
      }}
      submitOnEnterKey="false"
      cancelTextKey="Takaisin"
      okTextKey="Kyllä, peru suostumus"
      validP={checkboxAtom}
    >
      <h2>
        <Text name="Suostumuksen peruminen: Vapaan sivistystyön koulutus" />
      </h2>
      <Text name="Kun peruutat antamasi suostumuksen..." />
      <br />
      <br />
      <Checkbox
        id="suostumuksen-peruutus-checkbox"
        onChange={() => checkboxAtom.modify((v) => !v)}
        label="Ymmärrän, että suostumuksen peruutus on lopullinen. Kyseisen koulutuksen tiedot poistuvat Opintopolun suoritustiedoistani välittömästi."
        listStylePosition="inside"
      />
    </ModalDialog>
  )
}
