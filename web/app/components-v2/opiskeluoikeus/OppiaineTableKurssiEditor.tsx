import React, { useCallback, useMemo } from 'react'
import { emptyLocalizedString, t } from '../../i18n/i18n'
import {
  createIBLaajuus,
  createIBLaajuusyksikkö
} from '../../ib/components/IBLaajuusEdit'
import { isIBKurssinSuoritus } from '../../types/fi/oph/koski/schema/IBKurssinSuoritus'
import { LaajuusKursseissa } from '../../types/fi/oph/koski/schema/LaajuusKursseissa'
import { isLukionKurssinSuoritus2015 } from '../../types/fi/oph/koski/schema/LukionKurssinSuoritus2015'
import { OsaamisenTunnustaminen } from '../../types/fi/oph/koski/schema/OsaamisenTunnustaminen'
import { isPaikallinenLukionKurssi2015 } from '../../types/fi/oph/koski/schema/PaikallinenLukionKurssi2015'
import { isPreIBKurssinSuoritus2015 } from '../../types/fi/oph/koski/schema/PreIBKurssinSuoritus2015'
import { isValinnaisuus } from '../../types/fi/oph/koski/schema/Valinnaisuus'
import { isValtakunnallinenLukionKurssi2015 } from '../../types/fi/oph/koski/schema/ValtakunnallinenLukionKurssi2015'
import { PathToken, get } from '../../util/laxModify'
import { KeyValueRow, KeyValueTable } from '../containers/KeyValueTable'
import { Modal, ModalBody, ModalFooter, ModalTitle } from '../containers/Modal'
import { Checkbox } from '../controls/Checkbox'
import { DateEdit } from '../controls/DateField'
import { FlatButton } from '../controls/FlatButton'
import { LocalizedTextEdit } from '../controls/LocalizedTestField'
import { RaisedButton } from '../controls/RaisedButton'
import { FormModel } from '../forms/FormModel'
import { ArvosanaEdit } from './ArvosanaField'
import { KoodistoSelect } from './KoodistoSelect'
import { LaajuusEdit } from './LaajuusField'
import {
  OppiaineTableOpiskeluoikeus,
  OppiaineenOsasuoritus,
  isKuvauksellinen
} from './OppiaineTable'

type OppiaineTableKurssiEditorProps = {
  form: FormModel<OppiaineTableOpiskeluoikeus>
  path: PathToken[]
  onClose: () => void
}
const laajuusPath: PathToken[] = [
  'koulutusmoduuli',
  {
    key: 'laajuus',
    onEmpty: () => LaajuusKursseissa({ arvo: 0 })
  }
]
const pakollinenPath: PathToken[] = ['koulutusmoduuli', 'pakollinen']
const arviointiPath = (index: number): PathToken[] => ['arviointi', index]
const arviointipäiväPath = (index: number): PathToken[] => [
  'arviointi',
  index,
  'päivä'
]
const kurssinTyyppiPath: PathToken[] = ['koulutusmoduuli', 'kurssinTyyppi']
export const OppiaineTableKurssiEditor: React.FC<
  OppiaineTableKurssiEditorProps
> = ({ form, path, onClose }) => {
  const kurssi = get<OppiaineenOsasuoritus>(...path)(form.state)

  const createOsaamisenTunnustaminen = () =>
    form.set(
      ...path,
      'tunnustettu'
    )(
      OsaamisenTunnustaminen({
        selite: emptyLocalizedString
      })
    )

  const ibYksikkö = useMemo(
    () =>
      createIBLaajuusyksikkö(
        kurssi.koulutusmoduuli.laajuus,
        form.state.alkamispäivä
      ),
    [kurssi.koulutusmoduuli.laajuus, form.state.alkamispäivä]
  )
  const createLaajuus = useCallback((arvo: number) => {
    if (isPreIBKurssinSuoritus2015(kurssi) || isIBKurssinSuoritus(kurssi)) {
      return createIBLaajuus(arvo, ibYksikkö)
    }
    throw new Error(`Unimplemented: createLaajuus for ${kurssi.$class}`)
  }, [])

  return (
    <Modal onClose={onClose}>
      <ModalTitle>
        {kurssi.koulutusmoduuli.tunniste.koodiarvo}{' '}
        {t(kurssi.koulutusmoduuli.tunniste.nimi)}
      </ModalTitle>
      <ModalBody>
        <KeyValueTable>
          {isKuvauksellinen(kurssi.koulutusmoduuli) && (
            <KeyValueRow localizableLabel="Kuvaus">
              <LocalizedTextEdit
                value={kurssi.koulutusmoduuli.kuvaus}
                onChange={form.set(...path, 'koulutusmoduuli', 'kuvaus')}
              />
            </KeyValueRow>
          )}

          <KeyValueRow localizableLabel="Laajuus">
            <LaajuusEdit
              value={kurssi.koulutusmoduuli.laajuus}
              onChange={form.set(...path, ...laajuusPath)}
              createLaajuus={createLaajuus}
            />
          </KeyValueRow>

          {isPaikallinenLukionKurssi2015(kurssi.koulutusmoduuli) ||
            (isValtakunnallinenLukionKurssi2015(kurssi.koulutusmoduuli) && (
              <KeyValueRow localizableLabel="Kurssin tyyppi">
                <KoodistoSelect
                  inlineOptions
                  koodistoUri="lukionkurssintyyppi"
                  onSelect={form.set(...path, ...kurssinTyyppiPath)}
                  value={kurssi.koulutusmoduuli.kurssinTyyppi.koodiarvo}
                  testId="kurssinTyyppi"
                />
              </KeyValueRow>
            ))}

          {isValinnaisuus(kurssi.koulutusmoduuli) && (
            <KeyValueRow localizableLabel="Pakollinen">
              <Checkbox
                label=""
                checked={kurssi.koulutusmoduuli.pakollinen}
                onChange={form.set(...path, ...pakollinenPath)}
                testId="pakollinen"
              />
            </KeyValueRow>
          )}

          <KeyValueRow localizableLabel="Arviointi">
            {(kurssi.arviointi || []).map((arviointi, index) => (
              <>
                <KeyValueRow innerKeyValueTable localizableLabel="Arvosana">
                  <ArvosanaEdit
                    suoritusClassName={kurssi.$class}
                    value={arviointi}
                    onChange={form.set(...path, ...arviointiPath(index))}
                  />
                </KeyValueRow>
                <KeyValueRow
                  innerKeyValueTable
                  localizableLabel="Arviointipäivä"
                >
                  <DateEdit
                    value={arviointi.päivä}
                    onChange={form.set(...path, ...arviointipäiväPath(index))}
                  />
                </KeyValueRow>
              </>
            ))}
          </KeyValueRow>

          <KeyValueRow localizableLabel="Suorituskieli">
            <KoodistoSelect
              inlineOptions
              koodistoUri="kieli"
              onSelect={form.set(...path, 'suorituskieli')}
              value={kurssi.suorituskieli?.koodiarvo}
              testId="suorituskieli"
            />
          </KeyValueRow>

          {isLukionKurssinSuoritus2015(kurssi) && (
            <>
              <KeyValueRow localizableLabel="Osaamisen tunnustaminen">
                {kurssi.tunnustettu ? (
                  <fieldset>
                    <LocalizedTextEdit
                      large
                      value={kurssi.tunnustettu.selite}
                      onChange={form.set(...path, 'tunnustettu', 'selite')}
                    />
                    <Checkbox
                      checked={kurssi.tunnustettu.rahoituksenPiirissä}
                      onChange={form.set(
                        ...path,
                        'tunnustettu',
                        'rahoituksenPiirissä'
                      )}
                      label="Rahoituksen piirissä"
                      testId="rahoituksenPiirissä"
                    />
                  </fieldset>
                ) : (
                  <FlatButton onClick={createOsaamisenTunnustaminen}>
                    {t('Lisää osaamisen tunnustaminen')}
                  </FlatButton>
                )}
              </KeyValueRow>
              <KeyValueRow localizableLabel="Lisätiedot">
                <Checkbox
                  checked={!!kurssi.suoritettuLukiodiplomina}
                  onChange={form.set(...path, 'suoritettuLukiodiplomina')}
                  label="Suoritettu lukiodiplomina"
                  testId="suoritettuLukiodiplomina"
                />
              </KeyValueRow>
              <KeyValueRow>
                <Checkbox
                  checked={!!kurssi.suoritettuSuullisenaKielikokeena}
                  onChange={form.set(
                    ...path,
                    'suoritettuSuullisenaKielikokeena'
                  )}
                  label="Suoritettu suullisena kielikokeena"
                  testId="suoritettuSuullisenaKielikokeena"
                />
              </KeyValueRow>
            </>
          )}
        </KeyValueTable>
      </ModalBody>
      <ModalFooter>
        <RaisedButton onClick={onClose}>{t('Sulje')}</RaisedButton>
      </ModalFooter>
    </Modal>
  )
}
