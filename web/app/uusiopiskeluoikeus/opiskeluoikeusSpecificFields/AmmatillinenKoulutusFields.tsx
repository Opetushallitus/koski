import React, { useCallback, useMemo, useState } from 'react'
import { SuoritusFieldsProps } from '.'
import { useKoodisto } from '../../appstate/koodisto'
import {
  SelectOption,
  groupKoodistoToOptions
} from '../../components-v2/controls/Select'
import {
  PaikallinenKoulutus,
  PaikallinenKoulutusFields
} from '../../components-v2/opiskeluoikeus/PaikallinenKoulutusFields'
import { t } from '../../i18n/i18n'
import { isAmmatilliseenTehtäväänValmistavaKoulutus } from '../../types/fi/oph/koski/schema/AmmatilliseenTehtavaanValmistavaKoulutus'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { koodistokoodiviiteId } from '../../util/koodisto'
import { DialogPäätasonSuoritusSelect } from '../components/DialogPaatasonSuoritusSelect'
import { DialogPerusteSelect } from '../components/DialogPerusteSelect'
import { DialogSelect } from '../components/DialogSelect'
import {
  createAmmatilliseenTehtäväänValmistavaKoulutus,
  createPaikallinenMuuAmmatillinenKoulutus,
  createTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus,
  AmmatillisenOsittaisenUseastaTutkinnostaSuorituksenTyyppi
} from '../opiskeluoikeusCreator/ammatillinenTutkinto'
import { useAmmatillisenTutkinnonSuoritustapa } from '../state/ammatillisenTutkinnonSuoritustapa'
import { Finnish } from '../../types/fi/oph/koski/schema/Finnish'
import { tutkintoKey, useTutkinnot } from '../../ammatillinen-v2/useTutkinnot'

export const AmmatillinenKoulutusFields = (props: SuoritusFieldsProps) => {
  const tutkinnot = useTutkinnot(props.state.oppilaitos.value?.oid)
  const suoritustavat = useAmmatillisenTutkinnonSuoritustapa(props.state)

  const onTOPKS = useCallback(
    (koulutus?: PaikallinenKoulutus) =>
      props.state.tutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus.set(
        koulutus &&
          createTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus(
            koulutus.nimi,
            koulutus.koodiarvo,
            koulutus.kuvaus
          )
      ),
    [props.state.tutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus]
  )

  return (
    <>
      <label>
        {t('Suoritustyyppi')}
        <DialogPäätasonSuoritusSelect
          state={props.state}
          extraOptions={extraOptions}
          default="ammatillinentutkinto"
          testId="suoritustyyppi"
        />
      </label>

      {props.state.tutkinto.visible && (
        <label>
          {t('Tutkinto')}
          <DialogSelect
            options={tutkinnot.options}
            value={
              props.state.tutkinto.value &&
              tutkintoKey(props.state.tutkinto.value)
            }
            onChange={(opt) => props.state.tutkinto.set(opt?.value)}
            onSearch={tutkinnot.setQuery}
            testId="tutkinto"
          />
        </label>
      )}

      {props.state.suoritustapa.visible && (
        <label>
          {t('Suoritustapa')}
          <DialogSelect
            options={suoritustavat}
            value={
              props.state.suoritustapa.value &&
              koodistokoodiviiteId(props.state.suoritustapa.value)
            }
            onChange={(opt) => props.state.suoritustapa.set(opt?.value)}
            testId="suoritustapa"
          />
        </label>
      )}

      {props.state.muuAmmatillinenKoulutus.visible && (
        <MuuAmmatillinenKoulutusFields {...props} />
      )}

      {props.state.tutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus
        .visible && <PaikallinenKoulutusFields onChange={onTOPKS} />}

      {props.state.peruste.visible && (
        <DialogPerusteSelect state={props.state} />
      )}
    </>
  )
}

const MuuAmmatillinenKoulutusFields = (props: SuoritusFieldsProps) => {
  const [koulutusmoduuli, setKoulutusmoduuli] =
    useState<MuuAmmatillinenKoulutusmoduuliKey>()

  const tunnisteet = useKoodisto('ammatilliseentehtavaanvalmistavakoulutus')
  const tunnisteOptions = useMemo(
    () => (tunnisteet ? groupKoodistoToOptions(tunnisteet) : []),
    [tunnisteet]
  )

  const onPaikallinenKoulutus = useCallback(
    (koulutus?: PaikallinenKoulutus) => {
      props.state.muuAmmatillinenKoulutus.set(
        koulutus &&
          createPaikallinenMuuAmmatillinenKoulutus(
            koulutus.nimi,
            koulutus.koodiarvo,
            koulutus.kuvaus
          )
      )
    },
    [props.state.muuAmmatillinenKoulutus]
  )

  const onAmmatilliseenTehtäväänValmistavaKoulutus = useCallback(
    (
      option?: SelectOption<
        Koodistokoodiviite<'ammatilliseentehtavaanvalmistavakoulutus'>
      >
    ) =>
      props.state.muuAmmatillinenKoulutus.set(
        option?.value &&
          createAmmatilliseenTehtäväänValmistavaKoulutus(option.value)
      ),
    [props.state.muuAmmatillinenKoulutus]
  )

  const tunniste = isAmmatilliseenTehtäväänValmistavaKoulutus(
    props.state.muuAmmatillinenKoulutus.value
  )
    ? koodistokoodiviiteId(props.state.muuAmmatillinenKoulutus.value.tunniste)
    : undefined

  return (
    <>
      <label>
        {t('Koulutusmoduuli')}
        <DialogSelect
          options={muuAmmatillinenKoulutusOptions}
          value={koulutusmoduuli}
          onChange={(opt) => setKoulutusmoduuli(opt?.value)}
          testId="koulutusmoduuli"
        />
      </label>

      {koulutusmoduuli === 'paikallinen' && (
        <PaikallinenKoulutusFields onChange={onPaikallinenKoulutus} />
      )}

      {koulutusmoduuli === 'ammatilliseentehtavaanvalmistavakoulutus' && (
        <label>
          {t('Ammatilliseen tehtävään valmistava koulutus')}
          <DialogSelect
            options={tunnisteOptions}
            value={tunniste}
            onChange={onAmmatilliseenTehtäväänValmistavaKoulutus}
            testId="ammatilliseentehtavaanvalmistavakoulutus"
          />
        </label>
      )}
    </>
  )
}

type MuuAmmatillinenKoulutusmoduuliKey =
  | 'paikallinen'
  | 'ammatilliseentehtavaanvalmistavakoulutus'

const muuAmmatillinenKoulutusOptions: SelectOption<MuuAmmatillinenKoulutusmoduuliKey>[] =
  [
    {
      key: 'paikallinen',
      value: 'paikallinen',
      label: t('Paikallinen koulutus')
    },
    {
      key: 'ammatilliseentehtavaanvalmistavakoulutus',
      value: 'ammatilliseentehtavaanvalmistavakoulutus',
      label: t('Ammatilliseen tehtävään valmistava koulutus')
    }
  ]

const extraOptions: Array<
  SelectOption<Koodistokoodiviite<'suorituksentyyppi'>>
> = [
  {
    key: `suorituksentyyppi_${AmmatillisenOsittaisenUseastaTutkinnostaSuorituksenTyyppi}`,
    label: 'Ammatillisen tutkinnon osa/osia useasta tutkinnosta',
    value: Koodistokoodiviite({
      koodiarvo: AmmatillisenOsittaisenUseastaTutkinnostaSuorituksenTyyppi,
      koodistoUri: 'suorituksentyyppi',
      nimi: Finnish({
        fi: 'Ammatillisen tutkinnon osa/osia useasta tutkinnosta'
      })
    })
  }
]
