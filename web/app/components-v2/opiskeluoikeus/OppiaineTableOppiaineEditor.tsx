import React from 'react'
import { t } from '../../i18n/i18n'
import { isIBAineRyhmäOppiaine } from '../../types/fi/oph/koski/schema/IBAineRyhmaOppiaine'
import { isIBOppiaineLanguage } from '../../types/fi/oph/koski/schema/IBOppiaineLanguage'
import { isIBTaso } from '../../types/fi/oph/koski/schema/IBTaso'
import { isValinnaisuus } from '../../types/fi/oph/koski/schema/Valinnaisuus'
import { koodiviiteId } from '../../util/koodisto'
import { createLaajuusOpintopisteissä } from '../../util/laajuus'
import { PathToken, get } from '../../util/laxModify'
import { DialogSelect } from '../../uusiopiskeluoikeus/components/DialogSelect'
import { KeyValueRow, KeyValueTable } from '../containers/KeyValueTable'
import { Modal, ModalBody, ModalFooter, ModalTitle } from '../containers/Modal'
import { Checkbox } from '../controls/Checkbox'
import { LocalizedTextEdit } from '../controls/LocalizedTestField'
import { RaisedButton } from '../controls/RaisedButton'
import { useKoodistoOptions } from '../controls/Select'
import { FormModel } from '../forms/FormModel'
import { LaajuusEdit } from './LaajuusField'
import { Oppiaine, OppiaineTableOpiskeluoikeus } from './OppiaineTable'
import { isIBDPCoreOppiaineExtendedEssay } from '../../types/fi/oph/koski/schema/IBDPCoreOppiaineExtendedEssay'
import { isIBDPCoreOppiaineCAS } from '../../types/fi/oph/koski/schema/IBDPCoreOppiaineCAS'
import { isIBDPCoreOppiaineTheoryOfKnowledge } from '../../types/fi/oph/koski/schema/IBDPCoreOppiaineTheoryOfKnowledge'
import { isIBDPCoreOppiaineLanguage } from '../../types/fi/oph/koski/schema/IBDPCoreOppiaineLanguage'

type OppiaineTableOppiaineEditorProps = {
  form: FormModel<OppiaineTableOpiskeluoikeus>
  path: PathToken[]
  onClose: () => void
}

export const OppiaineTableOppiaineEditor: React.FC<
  OppiaineTableOppiaineEditorProps
> = ({ form, path, onClose }) => {
  const oppiaine = get<Oppiaine>(...path)(form.state)
  const koulutus = oppiaine.koulutusmoduuli

  const tunnisteet = useKoodistoOptions('oppiaineetib')
  const kielet = useKoodistoOptions('kielivalikoima')
  const tasot = useKoodistoOptions('oppiaineentasoib')
  const aineryhmät = useKoodistoOptions('aineryhmaib')

  return (
    <Modal onClose={onClose}>
      <ModalTitle>
        {oppiaine.koulutusmoduuli.tunniste.koodiarvo}{' '}
        {t(oppiaine.koulutusmoduuli.tunniste.nimi)}
      </ModalTitle>
      <ModalBody>
        <KeyValueTable>
          {isIBDPCoreOppiaineExtendedEssay(koulutus) && (
            // Huom. tällä hetkellä aiheuttaa poikkeuksen tallennettaessa jos muokatessa vaihtaa kieliaineen muuhun aineeseen
            <KeyValueRow localizableLabel="Aine">
              <DialogSelect
                options={tunnisteet}
                value={koodiviiteId(koulutus.aine.tunniste)}
                onChange={(opt) => {
                  form.set(
                    ...path,
                    'koulutusmoduuli',
                    'aine',
                    'tunniste'
                  )(opt?.value)
                }}
                testId="tunniste"
              />
            </KeyValueRow>
          )}
          {isIBDPCoreOppiaineExtendedEssay(koulutus) && (
            <KeyValueRow localizableLabel="Laajuus">
              <LaajuusEdit
                value={koulutus.aine.laajuus}
                onChange={form.set(
                  ...path,
                  'koulutusmoduuli',
                  'aine',
                  'laajuus'
                )}
                createLaajuus={createLaajuusOpintopisteissä}
              />
            </KeyValueRow>
          )}
          {isIBDPCoreOppiaineExtendedEssay(koulutus) && (
            <KeyValueRow localizableLabel="Taso">
              <DialogSelect
                options={tasot}
                value={koulutus.aine.taso && koodiviiteId(koulutus.aine.taso)}
                onChange={(opt) =>
                  form.set(
                    ...path,
                    'koulutusmoduuli',
                    'aine',
                    'taso'
                  )(opt?.value)
                }
                testId="kieli"
              />
            </KeyValueRow>
          )}
          {isIBDPCoreOppiaineExtendedEssay(koulutus) &&
            isIBDPCoreOppiaineLanguage(koulutus.aine) && (
              <KeyValueRow localizableLabel="Kieli">
                <DialogSelect
                  options={kielet}
                  value={koodiviiteId(koulutus.aine.kieli)}
                  onChange={(opt) =>
                    form.set(
                      ...path,
                      'koulutusmoduuli',
                      'aine',
                      'kieli'
                    )(opt?.value)
                  }
                  testId="aine-kieli"
                />
              </KeyValueRow>
            )}
          {isIBDPCoreOppiaineExtendedEssay(koulutus) && (
            <KeyValueRow localizableLabel="Ryhmä">
              <DialogSelect
                options={aineryhmät}
                value={koodiviiteId(koulutus.aine.ryhmä)}
                onChange={(opt) =>
                  form.set(
                    ...path,
                    'koulutusmoduuli',
                    'aine',
                    'ryhmä'
                  )(opt?.value)
                }
                testId="kieli"
              />
            </KeyValueRow>
          )}
          {isIBDPCoreOppiaineExtendedEssay(koulutus) && (
            <KeyValueRow localizableLabel="Aihe">
              <LocalizedTextEdit
                value={koulutus.aihe}
                onChange={form.set(...path, 'koulutusmoduuli', 'aihe')}
              />
            </KeyValueRow>
          )}
          {isIBOppiaineLanguage(koulutus) && (
            <KeyValueRow localizableLabel="Kieli">
              <DialogSelect
                options={kielet}
                value={koodiviiteId(koulutus.kieli)}
                onChange={(opt) =>
                  form.set(...path, 'koulutusmoduuli', 'kieli')(opt?.value)
                }
                testId="kieli"
              />
            </KeyValueRow>
          )}
          {isIBTaso(koulutus) && (
            <KeyValueRow localizableLabel="Taso">
              <DialogSelect
                options={tasot}
                value={koulutus.taso && koodiviiteId(koulutus.taso)}
                onChange={(opt) =>
                  form.set(...path, 'koulutusmoduuli', 'taso')(opt?.value)
                }
                testId="kieli"
              />
            </KeyValueRow>
          )}
          {isIBAineRyhmäOppiaine(koulutus) && (
            <KeyValueRow localizableLabel="Ryhmä">
              <DialogSelect
                options={aineryhmät}
                value={koodiviiteId(koulutus.ryhmä)}
                onChange={(opt) =>
                  form.set(...path, 'koulutusmoduuli', 'ryhmä')(opt?.value)
                }
                testId="kieli"
              />
            </KeyValueRow>
          )}
          {(isIBDPCoreOppiaineCAS(koulutus) ||
            isIBDPCoreOppiaineTheoryOfKnowledge(koulutus)) && (
            <KeyValueRow localizableLabel="Laajuus">
              <LaajuusEdit
                value={koulutus.laajuus}
                onChange={form.set(...path, 'koulutusmoduuli', 'laajuus')}
                createLaajuus={createLaajuusOpintopisteissä}
              />
            </KeyValueRow>
          )}
          {isValinnaisuus(koulutus) && (
            <KeyValueRow>
              <Checkbox
                label={t('Pakollinen')}
                checked={koulutus.pakollinen}
                onChange={form.set(...path, 'koulutusmoduuli', 'pakollinen')}
                testId="pakollinen"
              />
            </KeyValueRow>
          )}
        </KeyValueTable>
      </ModalBody>
      <ModalFooter>
        <RaisedButton onClick={onClose}>{t('Sulje')}</RaisedButton>
      </ModalFooter>
    </Modal>
  )
}
