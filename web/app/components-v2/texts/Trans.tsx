import { t } from '../../i18n/i18n'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'

export type TransProps = {
  children?: LocalizedString | string
}

export const Trans = (props: TransProps) => t(props.children) || null
