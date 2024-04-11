set -euo pipefail

BASEDIR=$(dirname "$0")

for SRCFILE in "${BASEDIR}"/*.puml; do
  TARGETFILE="${BASEDIR}"/png/$(basename "${SRCFILE}" | sed 's/puml/png/')
  TARGETFILE_SVG="${BASEDIR}"/svg/$(basename "${SRCFILE}" | sed 's/puml/svg/')
  echo Converting "${SRCFILE} -> ${TARGETFILE} & ${TARGETFILE_SVG}"
  docker run -e PLANTUML_LIMIT_SIZE=20000 --rm -i dstockhammer/plantuml:latest -pipe > "${TARGETFILE}" < "${SRCFILE}"
  docker run -e PLANTUML_LIMIT_SIZE=20000 --rm -i dstockhammer/plantuml:latest -tsvg -pipe > "${TARGETFILE_SVG}" < "${SRCFILE}"
done
