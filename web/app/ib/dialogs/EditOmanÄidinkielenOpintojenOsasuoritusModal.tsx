import { FormModel } from '../../components-v2/forms/FormModel'
import { IBOpiskeluoikeus } from '../../types/fi/oph/koski/schema/IBOpiskeluoikeus'
import { LukionOmanÄidinkielenOpintojenOsasuoritus } from '../../types/fi/oph/koski/schema/LukionOmanAidinkielenOpintojenOsasuoritus'
import { PathToken } from '../../util/laxModify'
import {
  Modal,
  ModalBody,
  ModalFooter,
  ModalTitle
} from '../../components-v2/containers/Modal'
import { emptyLocalizedString, t } from '../../i18n/i18n'
import { KeyValueRow } from '../../components-v2/containers/KeyValueTable'
import { LaajuusEdit } from '../../components-v2/opiskeluoikeus/LaajuusField'
import { LaajuusOpintopisteissä } from '../../types/fi/oph/koski/schema/LaajuusOpintopisteissa'
import { ArvosanaEdit } from '../../components-v2/opiskeluoikeus/ArvosanaField'
import { DateEdit } from '../../components-v2/controls/DateField'
import { KoodistoSelect } from '../../components-v2/opiskeluoikeus/KoodistoSelect'
import { LocalizedTextEdit } from '../../components-v2/controls/LocalizedTestField'
import { Checkbox } from '../../components-v2/controls/Checkbox'
import { FlatButton } from '../../components-v2/controls/FlatButton'
import { OsaamisenTunnustaminen } from '../../types/fi/oph/koski/schema/OsaamisenTunnustaminen'
import { Spacer } from '../../components-v2/layout/Spacer'
import { RaisedButton } from '../../components-v2/controls/RaisedButton'
import React from 'react'

export type EditOmanÄidinkielenOpintojenOsasuoritusModalProps = {
  form: FormModel<IBOpiskeluoikeus>
  osasuoritus: LukionOmanÄidinkielenOpintojenOsasuoritus
  osasuoritusPath: PathToken[]
  onClose: () => void
}

export const EditOmanÄidinkielenOpintojenOsasuoritusModal = ({
  form,
  osasuoritus,
  osasuoritusPath,
  onClose
}: EditOmanÄidinkielenOpintojenOsasuoritusModalProps) => {
  return (
    <Modal onClose={onClose}>
      <ModalTitle>{`${osasuoritus.koulutusmoduuli.tunniste.koodiarvo} ${t(osasuoritus.koulutusmoduuli.tunniste.nimi)}`}</ModalTitle>
      <ModalBody>
        <KeyValueRow localizableLabel="Laajuus">
          <LaajuusEdit
            value={osasuoritus.koulutusmoduuli.laajuus}
            onChange={form.set(
              ...osasuoritusPath,
              ...['koulutusmoduuli', 'laajuus']
            )}
            createLaajuus={(value) => LaajuusOpintopisteissä({ arvo: value })}
          />
        </KeyValueRow>
        {(osasuoritus.arviointi || []).map((arviointi, index) => (
          <div
            key={`${osasuoritus.koulutusmoduuli.tunniste.koodiarvo}.arviointi.${index}`}
          >
            <KeyValueRow
              localizableLabel="Arvosana"
              innerKeyValueTable={(osasuoritus.arviointi || []).length > 1}
            >
              <ArvosanaEdit
                suoritusClassName={osasuoritus.$class}
                value={arviointi}
                onChange={form.set(...osasuoritusPath, ...['arviointi', index])}
              />
            </KeyValueRow>
            <KeyValueRow
              localizableLabel="Arviointipäivä"
              innerKeyValueTable={(osasuoritus.arviointi || []).length > 1}
            >
              <DateEdit
                value={arviointi.päivä}
                onChange={form.set(
                  ...osasuoritusPath,
                  ...['arviointi', index, 'päivä']
                )}
                align="right"
              />
            </KeyValueRow>
          </div>
        ))}
        <KeyValueRow localizableLabel="Suorituskieli">
          <KoodistoSelect
            inlineOptions
            koodistoUri="kieli"
            onSelect={form.set(...osasuoritusPath, 'suorituskieli')}
            value={osasuoritus.suorituskieli?.koodiarvo}
            testId="suorituskieli"
          />
        </KeyValueRow>
        <KeyValueRow localizableLabel="Osaamisen tunnustaminen">
          {osasuoritus.tunnustettu ? (
            <fieldset>
              <LocalizedTextEdit
                large
                value={osasuoritus.tunnustettu.selite}
                onChange={form.set(...osasuoritusPath, 'tunnustettu', 'selite')}
              />
              <Checkbox
                checked={osasuoritus.tunnustettu.rahoituksenPiirissä}
                onChange={form.set(
                  ...osasuoritusPath,
                  'tunnustettu',
                  'rahoituksenPiirissä'
                )}
                label="Rahoituksen piirissä"
                testId="rahoituksenPiirissä"
              />
            </fieldset>
          ) : (
            <FlatButton
              onClick={() =>
                form.set(
                  ...osasuoritusPath,
                  'tunnustettu'
                )(
                  OsaamisenTunnustaminen({
                    selite: emptyLocalizedString
                  })
                )
              }
            >
              {t('Lisää osaamisen tunnustaminen')}
            </FlatButton>
          )}
        </KeyValueRow>
        <Spacer />
      </ModalBody>
      <ModalFooter>
        <RaisedButton onClick={onClose} testId="confirm">
          {t('Sulje')}
        </RaisedButton>
      </ModalFooter>
    </Modal>
  )
}
