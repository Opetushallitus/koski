import { AppConfiguration } from "./state/apitypes/appConfiguration"
import { Feature } from "./state/featureFlags"
import { OppijaRaamitService } from "./state/oppijaRaamitService"

declare global {
  interface Window extends AppConfiguration {
    virkailija_raamit_set_to_load?: boolean
    enableFeature?: (feature: Feature) => void
    Service?: OppijaRaamitService
  }
}
