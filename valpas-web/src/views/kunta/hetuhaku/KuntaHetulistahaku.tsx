import bem from "bem-ts"
import copy from "copy-to-clipboard"
import * as A from "fp-ts/Array"
import React, { useMemo, useState } from "react"
import { downloadRouhintaHetuilla } from "../../../api/api"
import { useApiWithParams } from "../../../api/apiHooks"
import { isError, isLoading } from "../../../api/apiUtils"
import { RaisedButton } from "../../../components/buttons/RaisedButton"
import { Form } from "../../../components/forms/Form"
import { TextAreaField } from "../../../components/forms/TextField"
import { Spinner } from "../../../components/icons/Spinner"
import { ApiErrors } from "../../../components/typography/error"
import { T, t, useLanguage } from "../../../i18n/i18n"
import { parseHetulikes } from "../../../state/hetu"
import { usePassword } from "../../../state/password"
import "./KuntaHetulistahaku.less"

const b = bem("kuntahetulista")

export const KuntaHetulistahaku = () => {
  const password = usePassword()
  const hetus = useHetulist()
  const lang = useLanguage()
  const download = useApiWithParams(downloadRouhintaHetuilla)

  const submit = () => {
    hetus.set(hetus.list.join("\n"))
    download.call({
      hetut: hetus.list,
      password,
      lang,
    })
  }

  return (
    <Form className={b()}>
      <TextAreaField
        label={t("rouhinta_hae_usealla_hetulla")}
        value={hetus.fieldValue}
        onChange={hetus.set}
      />
      <div>
        <Password>{password}</Password>
      </div>
      {isError(download) && <ApiErrors errors={download.errors} />}
      <RaisedButton
        onClick={submit}
        disabled={A.isEmpty(hetus.list) || isLoading(download)}
      >
        <T
          id="rouhinta_lataa_raportti"
          params={{ lukumäärä: hetus.list.length }}
        />
      </RaisedButton>
      {isLoading(download) && <Spinner />}
    </Form>
  )
}

type PasswordProps = {
  children: string
}

const Password = (props: PasswordProps) => (
  <div className={b("password")}>
    <span className={b("passwordlabel")}>
      <T id="rouhinta_tiedoston_salasana" />:
    </span>
    <span className={b("passwordvalue")}>{props.children}</span>
    <span className={b("passwordcopy")} onClick={() => copy(props.children)}>
      Kopioi
    </span>
  </div>
)

const useHetulist = () => {
  const [state, setState] = useState("")

  return useMemo(
    () => ({
      fieldValue: state,
      set: setState,
      list: parseHetulikes(state),
    }),
    [state]
  )
}
