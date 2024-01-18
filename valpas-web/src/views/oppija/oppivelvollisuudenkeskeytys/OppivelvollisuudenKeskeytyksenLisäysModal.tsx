import React, { useCallback } from "react"
import { createOppivelvollisuudenKeskeytys } from "../../../api/api"
import { useApiMethod, useOnApiSuccess } from "../../../api/apiHooks"
import { isError } from "../../../api/apiUtils"
import { Modal } from "../../../components/containers/Modal"
import { SecondaryHeading } from "../../../components/typography/headings"
import { t } from "../../../i18n/i18n"
import {
  kuntavalvontaAllowed,
  useOrganisaatiotOfRole,
} from "../../../state/accessRights"
import { HenkilöLaajatTiedot } from "../../../state/apitypes/henkilo"
import {
  OppivelvollisuudenKeskeytysForm,
  OppivelvollisuudenKeskeytysFormValues,
} from "./OppivelvollisuudenKeskeytysForm"

export type OppivelvollisuudenKeskeytysModalProps = {
  henkilö: HenkilöLaajatTiedot
  onClose: () => void
  onSubmit: () => void
}

export const OppivelvollisuudenKeskeytyksenLisäysModal = (
  props: OppivelvollisuudenKeskeytysModalProps,
) => {
  const organisaatiot = useOrganisaatiotOfRole(kuntavalvontaAllowed)
  const create = useApiMethod(createOppivelvollisuudenKeskeytys)
  const submit = useCallback(
    (form: OppivelvollisuudenKeskeytysFormValues) => {
      create.call({
        ...form,
        oppijaOid: props.henkilö.oid,
      })
    },
    [create, props.henkilö.oid],
  )

  useOnApiSuccess(create, props.onSubmit)

  return (
    <Modal
      title={t("ovkeskeytys__otsikko")}
      testId="ovkeskeytys-modal"
      onClose={props.onClose}
    >
      <SecondaryHeading data-testid="ovkeskeytys-secondary-heading">
        {props.henkilö.sukunimi} {props.henkilö.etunimet}
        {props.henkilö.hetu && ` (${props.henkilö.hetu})`}
      </SecondaryHeading>
      <OppivelvollisuudenKeskeytysForm
        testId="ovkeskeytys-form"
        organisaatiot={organisaatiot}
        onSubmit={submit}
        errors={isError(create) ? create.errors : []}
      />
    </Modal>
  )
}
