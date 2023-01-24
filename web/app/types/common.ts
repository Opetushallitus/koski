import { Koodistokoodiviite as SchemaKoodistokoodiviite } from './fi/oph/koski/schema/Koodistokoodiviite'

// Älä käytä tätä, tässä on uri ja koodiarvo väärinpäin
export type Deprecated_Koodistokoodiviite<
  T extends string = string,
  S extends string = string
> = SchemaKoodistokoodiviite<S, T>
