#!/bin/bash

set -euo pipefail

# Refreshes the localization texts from the production Lokalisointipalvelu,
# which in turn gets its data from Tolgee.
#
# Note that this regenerates the *-default-texts.json files too, not just the
# mockdata. The raw diff runs to tens of thousands of lines and hides real
# regressions, so after each fetch the script reports how the incoming data
# diverges from what the repo had, and what to do about it. See AGENTS.md
# "Updating localizations" for the rules behind that advice.
#
# To separate changes made in Tolgee from changes that undo an edit made in the
# repo, every value the fetch would change is attributed to the commit that last
# set its local value, by walking the file's history. The commit accounting for
# most of them is the previous refresh — those are translators' work. Anything
# attributed elsewhere came from a ticket and is about to be reverted.

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
ROOT_DIR="$SCRIPT_DIR/.."

# How far back to walk when attributing a value to the commit that set it.
# Counted in commits that touched the localization file, not in commits overall.
# Anything older than this is reported without a source commit.
ATTRIBUTION_MAX_COMMITS=25

# Longest per-commit list printed before the rest is summarised as a count.
GROUP_LIST_LIMIT=10

WORK_DIR=$(mktemp -d)
trap 'rm -rf "$WORK_DIR"' EXIT

# Colours when writing to a terminal; off when piped or when NO_COLOR is set.
# FORCE_COLOR=1 turns them on regardless, which is what CI logs want.
if [ -n "${FORCE_COLOR:-}" ] || { [ -t 1 ] && [ -z "${NO_COLOR:-}" ] && [ "${TERM:-dumb}" != "dumb" ]; }; then
    C_BOLD=$'\033[1m'; C_DIM=$'\033[2m';  C_RED=$'\033[31m'
    C_YEL=$'\033[33m'; C_GRN=$'\033[32m'; C_CYA=$'\033[36m'
    C_BRED=$'\033[91m'; C_OFF=$'\033[0m'
else
    C_BOLD=""; C_DIM=""; C_RED=""; C_YEL=""; C_GRN=""; C_CYA=""; C_BRED=""; C_OFF=""
fi

# Call the installed binary rather than "npx prettier": npx runs npm, which
# reads web/.npmrc and warns about every pnpm-only setting in it, and would
# download whatever version is newest instead of the one pinned in
# web/package.json.
PRETTIER="$ROOT_DIR/web/node_modules/.bin/prettier"

ADDED_TOTAL=0
CHANGED_TOTAL=0
REWIND_TOTAL=0

function truncate_text() {
    local -r text="$1"
    if [ "${#text}" -gt 76 ]; then printf '%s…' "${text:0:76}"; else printf '%s' "$text"; fi
}

# Prints the two versions of a changed value, windowed around the first
# character that differs. Truncating from the start would render the two lines
# identically whenever a long text changes only deep inside it.
function print_change() {
    local -r before="$1"
    local -r after="$2"
    local lo=0 hi=${#before} mid
    [ "${#after}" -lt "$hi" ] && hi=${#after}
    while [ "$lo" -lt "$hi" ]; do
        mid=$(( (lo + hi + 1) / 2 ))
        if [ "${before:0:mid}" = "${after:0:mid}" ]; then lo=$mid; else hi=$((mid - 1)); fi
    done
    local start=$(( lo - 24 )) lead=""
    if [ "$start" -gt 0 ]; then lead="…"; else start=0; fi
    printf '        %sennen%s %s%s\n' "$C_DIM" "$C_OFF" "$lead" "$(truncate_text "${before:start}")"
    printf '        %snyt  %s %s%s\n' "$C_DIM" "$C_OFF" "$lead" "$(truncate_text "${after:start}")"
}

# Attributes each changed value to the commit that last set the local value, by
# walking the history of $1 starting from the pre-fetch contents in $2.
# Reads "key<TAB>locale" lines on stdin and emits
# "key<TAB>locale<TAB>sha<TAB>subject<TAB>haspre<TAB>pre", where pre is the value
# that commit replaced. Comparing the incoming text against pre is what tells a
# straight revert apart from a newer text that merely overwrites a local edit.
function attribute_changes() {
    local -r rel_path="$1"
    local -r previous="$2"
    local -r pending="$WORK_DIR/pending.json"
    local -r newer="$WORK_DIR/attr.newer.json"
    local -r older="$WORK_DIR/attr.older.json"

    jq -R -s 'split("\n") | map(select(length > 0) | split("\t")) ' > "$pending"
    [ "$(jq 'length' "$pending")" -gt 0 ] || return 0

    # Start from the contents before the fetch: the file on disk has already
    # been overwritten by the time this runs.
    cp "$previous" "$newer"
    local walked=0 sha subject
    while IFS=$'\t' read -r sha subject; do
        walked=$(( walked + 1 ))
        git -C "$ROOT_DIR" show "$sha^:$rel_path" > "$older" 2>/dev/null || printf '[]' > "$older"

        jq -r -n --slurpfile a "$newer" --slurpfile b "$older" --slurpfile p "$pending" \
                 --arg sha "${sha:0:10}" --arg subject "${subject//$'\t'/ }" '
            def clean: gsub("[\n\t\r]"; " ");
            def idx: reduce .[] as $e ({}; .[$e.key][$e.locale] = $e.value);
            ($a[0] | idx) as $A | (($b[0] // []) | idx) as $B |
            $p[0][] | select($A[.[0]][.[1]] != $B[.[0]][.[1]])
            | $B[.[0]][.[1]] as $pre
            | "\(.[0])\t\(.[1])\t\($sha)\t\($subject)\t\(if $pre == null then "0" else "1" end)\t\(($pre // "") | clean)"'

        jq -c -n --slurpfile a "$newer" --slurpfile b "$older" --slurpfile p "$pending" '
            def idx: reduce .[] as $e ({}; .[$e.key][$e.locale] = $e.value);
            ($a[0] | idx) as $A | (($b[0] // []) | idx) as $B |
            [ $p[0][] | select($A[.[0]][.[1]] == $B[.[0]][.[1]]) ]' > "$pending.next"
        mv "$pending.next" "$pending"

        cp "$older" "$newer"
        [ "$(jq 'length' "$pending")" -gt 0 ] || break
        [ "$walked" -lt "$ATTRIBUTION_MAX_COMMITS" ] || break
    done < <(git -C "$ROOT_DIR" log --format=$'%H\t%s' -n "$ATTRIBUTION_MAX_COMMITS" -- "$rel_path")

    jq -r --arg older "ennen $ATTRIBUTION_MAX_COMMITS viimeisintä tiedostoa muuttanutta committia" \
        '.[] | "\(.[0])\t\(.[1])\t-\t\($older)\t0\t"' "$pending"
}

# Emits one tab-separated line per difference:
#   A <count>                         keys or translations only in the new data
#   D <key> <locale>                  in the repo, gone from the new data
#   M <key> <locale> <before> <after>
function diff_localizations() {
    jq -r -n --slurpfile prev "$1" --slurpfile curr "$2" '
        def clean: gsub("[\n\t\r]"; " ");
        def idx: reduce .[] as $e ({}; .[$e.key][$e.locale] = $e.value);
        ($prev[0] | idx) as $o |
        ($curr[0] | idx) as $n |
        ( [ $n | to_entries[] | .key as $k | .value | keys[]
            | select($o[$k][.] == null) ] | length ) as $added |
        "A\t\($added)",
        ( $o | to_entries[] | .key as $k | .value | to_entries[]
          | .key as $l | .value as $v
          | if   $n[$k][$l] == null then "D\t\($k)\t\($l)"
            elif $n[$k][$l] != $v   then "M\t\($k)\t\($l)\t\($v | clean)\t\($n[$k][$l] | clean)"
            else empty end )
    '
}

# Prints entries grouped under their source commit, capped per group.
#   $1 name of an array of "key<TAB>locale[<TAB>before<TAB>after]"
#   $2 marker character, $3 colour
#   $4 "detail"  = one row per key+locale, with the before/after block
#      "locales" = one row per key, with its locales collected on that row
function print_grouped() {
    local -n grouped_entries=$1
    local -r marker="$2"
    local -r colour="$3"
    local -r mode="$4"
    local g line k l before after shown
    for g in "${groups_by_size[@]}"; do
        local -a members=()
        for line in "${grouped_entries[@]}"; do
            IFS=$'\t' read -r k l before after <<< "$line"
            if [ "${attr_sha["$k"$'\t'"$l"]:-}" = "$g" ]; then members+=("$line"); fi
        done
        [ "${#members[@]}" -gt 0 ] || continue
        printf '\n  %slähde %s %s%s\n' "$C_BOLD" "$g" "${group_subj[$g]}" "$C_OFF"
        shown=0

        if [ "$mode" = "locales" ]; then
            local -a keys_seen=()
            local -A locales_of=()
            for line in "${members[@]}"; do
                IFS=$'\t' read -r k l before after <<< "$line"
                if [ -z "${locales_of[$k]:-}" ]; then
                    keys_seen+=("$k"); locales_of["$k"]="[$l]"
                else
                    locales_of["$k"]="${locales_of[$k]} [$l]"
                fi
            done
            for k in "${keys_seen[@]}"; do
                if [ "$shown" -ge "$GROUP_LIST_LIMIT" ]; then
                    printf '    %s… ja %d muuta tästä commitista%s\n' \
                        "$C_DIM" $(( ${#keys_seen[@]} - shown )) "$C_OFF"
                    break
                fi
                printf '    %s%s%s %s %s%s%s\n' \
                    "$colour" "$marker" "$C_OFF" "$k" "$C_DIM" "${locales_of[$k]}" "$C_OFF"
                shown=$(( shown + 1 ))
            done
            continue
        fi

        for line in "${members[@]}"; do
            if [ "$shown" -ge "$GROUP_LIST_LIMIT" ]; then
                printf '    %s… ja %d muuta tästä commitista%s\n' \
                    "$C_DIM" $(( ${#members[@]} - shown )) "$C_OFF"
                break
            fi
            IFS=$'\t' read -r k l before after <<< "$line"
            printf '    %s%s%s %s %s[%s]%s\n' \
                "$colour" "$marker" "$C_OFF" "$k" "$C_DIM" "$l" "$C_OFF"
            print_change "$before" "$after"
            shown=$(( shown + 1 ))
        done
    done
}

# Compares the previous contents of a mockdata file against the freshly fetched
# one, then attributes every difference to the commit that last set the local
# value. Only the mockdata file is compared: it carries every language, and
# *-default-texts.json is derived from its Finnish subset, so reporting on both
# would only duplicate the findings.
function report_divergence() {
    local -r CATEGORY="$1"
    local -r PREVIOUS="$2"
    local -r CURRENT="$3"
    local -r REL_PATH="$4"

    printf '\n%s\n' "${C_BOLD}=== ${CATEGORY}: mitä tuotannosta tuli ===${C_OFF}"

    if [ ! -s "$PREVIOUS" ]; then
        printf '%s\n' "  ${C_DIM}Ei aiempaa versiota, vertailu ohitetaan.${C_OFF}"
        return 0
    fi

    local added=0 kind f1 f2 f3 f4
    local -a diffs=()
    while IFS=$'\t' read -r kind f1 f2 f3 f4; do
        case "$kind" in
            A) added="$f1" ;;
            D) diffs+=("D"$'\t'"$f1"$'\t'"$f2"$'\t'$'\t') ;;
            M) diffs+=("M"$'\t'"$f1"$'\t'"$f2"$'\t'"$f3"$'\t'"$f4") ;;
        esac
    done < <(diff_localizations "$PREVIOUS" "$CURRENT")

    printf '  %s%4d%s uutta avainta tai käännöstä\n' "$C_GRN" "$added" "$C_OFF"
    printf '  %s%4d%s muuttunutta tai poistunutta arvoa\n' "$C_YEL" "${#diffs[@]}" "$C_OFF"
    ADDED_TOTAL=$(( ADDED_TOTAL + added ))
    if [ "${#diffs[@]}" -eq 0 ]; then return 0; fi

    declare -A attr_sha attr_pre attr_haspre group_count group_subj
    local key locale sha subj haspre pre
    while IFS=$'\t' read -r key locale sha subj haspre pre; do
        attr_sha["$key"$'\t'"$locale"]="$sha"
        attr_pre["$key"$'\t'"$locale"]="$pre"
        attr_haspre["$key"$'\t'"$locale"]="$haspre"
        group_count["$sha"]=$(( ${group_count["$sha"]:-0} + 1 ))
        group_subj["$sha"]="$subj"
    done < <(printf '%s\n' "${diffs[@]}" | cut -f2,3 | attribute_changes "$REL_PATH" "$PREVIOUS")

    local -a groups_by_size=()
    mapfile -t groups_by_size < <(
        for sha in "${!group_count[@]}"; do printf '%s\t%s\n' "${group_count[$sha]}" "$sha"; done \
            | sort -rn | cut -f2
    )

    # No attempt is made to decide which of these commits was a localization
    # refresh and which was ticket work. Nothing in the file records that, and
    # every proxy tried for it leaks: diff size does not separate them (a real
    # refresh changed 57 entries where a hand edit changed 84), and neither does
    # group size, because a value points at whichever refresh last touched it,
    # so several refreshes show up at once. The commit subject answers it at a
    # glance, so entries are grouped by source commit and the reader judges.

    local -a dels=() mods=() rewinds=()
    local line k l before after kk
    for line in "${diffs[@]}"; do
        IFS=$'\t' read -r kind k l before after <<< "$line"
        kk="$k"$'\t'"$l"
        if [ "$kind" = "D" ]; then
            dels+=("$kk")
        elif [ "${attr_haspre[$kk]:-0}" = "1" ] && [ "$after" = "${attr_pre[$kk]:-}" ]; then
            # Production holds exactly the text from before the source commit,
            # so that commit's change is being undone in full. This is an exact
            # comparison, not a guess, and it is rare enough to lead with.
            rewinds+=("$kk"$'\t'"$before"$'\t'"$after")
        else
            mods+=("$kk"$'\t'"$before"$'\t'"$after")
        fi
    done

    CHANGED_TOTAL=$(( CHANGED_TOTAL + ${#dels[@]} + ${#mods[@]} + ${#rewinds[@]} ))
    REWIND_TOTAL=$(( REWIND_TOTAL + ${#rewinds[@]} ))

    if [ "${#rewinds[@]}" -gt 0 ]; then
        printf '\n%s %s\n' "${C_RED}${C_BOLD}PALAUTUU ENNALLEEN${C_OFF}" \
            "${C_DIM}(tuotannossa on sama teksti kuin ennen lähde-commitia)${C_OFF}"
        print_grouped rewinds "~" "$C_RED" detail
        cat <<EOF

  ${C_CYA}Näin käsittelet:${C_OFF}
    Lähde-commitin tekemä muutos katoaa kokonaan: tuotannosta tulee täsmälleen
    se teksti, joka arvolla oli ennen sitä commitia.

    Jos lähde on tikettityö:
      ${C_BOLD}${C_BRED}arvo on kirjoitettu käsin repoon eikä ole päätynyt Tolgeehen${C_OFF}
    Palauta arvo JA korjaa teksti Tolgeehen, muuten se katoaa taas seuraavalla
    kerralla.

    Jos lähde on aiempi lokalisointipäivitys, teksti on palautettu Tolgeessa.
    Sekin kannattaa vilkaista, mutta repoon ei tarvitse koskea.
EOF
    fi

    if [ "${#dels[@]}" -gt 0 ]; then
        printf '\n%s %s\n' "${C_YEL}${C_BOLD}POISTUU${C_OFF}" \
            "${C_DIM}(avain on repossa, mutta ei tullut tuotannosta)${C_OFF}"
        print_grouped dels "-" "$C_YEL" locales
        cat <<EOF

  ${C_CYA}Näin käsittelet:${C_OFF}
    Avain voi puuttua tuotannosta kahdesta syystä: sitä ei ole koskaan viety
    Tolgeehen, tai se on poistettu sieltä tarkoituksella.

    1. Tarkista onko avain yhä käytössä:
         ${C_DIM}grep -rn "<avain>" src/main/scala web/app${C_OFF}
    2. Jos on: palauta arvo tähän tiedostoon JA lisää se Tolgeehen.
       Pelkkä repoon palauttaminen katoaa taas seuraavassa päivityksessä.
    3. Jos ei ole: poisto on kunnossa, jatka eteenpäin.

  ${C_DIM}Uuden avaimen sv- ja en-käännökset eivät päädy Tolgeehen itsestään:
  sovellus julkaisee vain suomenkielisen tekstin ja tyhjät sv/en.${C_OFF}
EOF
    fi

    if [ "${#mods[@]}" -gt 0 ]; then
        printf '\n%s %s\n' "${C_YEL}${C_BOLD}MUUTTUU${C_OFF}" \
            "${C_DIM}(tuotannossa on jokin muu teksti kuin repossa)${C_OFF}"
        print_grouped mods "~" "$C_YEL" detail
        cat <<EOF

  ${C_CYA}Näin käsittelet:${C_OFF}
    Nämä eivät palaudu commitia edeltäneeseen tekstiin, eli Tolgeessa on tehty
    jotain muuta. Katso lähde-commitista, mistä repon nykyinen arvo on peräisin:
    aiempi lokalisointipäivitys tarkoittaa kääntäjien työtä eikä vaadi mitään,
    tikettityö taas sitä, että käsin kirjoitettu arvo korvautuu Tolgeen
    versiolla. Jos repon muokkaus on yhä haluttu, palauta se JA korjaa Tolgeehen.
EOF
    fi
}

function load_and_format() {
    local -r CATEGORY="$1"
    local -r LOCALIZATION_FILE="$2"
    local -r DEFAULT_TEXTS_FILE="$3"

    cd "$ROOT_DIR" || exit

    local -r PREVIOUS="$WORK_DIR/$CATEGORY.previous.json"
    cp "$LOCALIZATION_FILE" "$PREVIOUS" 2>/dev/null || true

    # jq -S pins a canonical key order. Lokalisointipalvelu has returned its
    # fields in a different order on every past refresh, which rewrote the whole
    # mockdata file and made it conflict with any unrelated edit to that file.
    curl --silent --show-error --fail \
        "https://virkailija.opintopolku.fi/lokalisointi/cxf/rest/v1/localisation?category=$CATEGORY" \
        | jq -S 'map( . * { createdBy: "anonymousUser", modifiedBy: "anonymousUser" } )' \
        > "$LOCALIZATION_FILE"
    "$PRETTIER" --config "$ROOT_DIR/web/.prettierrc.json" --log-level warn --write "$LOCALIZATION_FILE"
    jq '[.[] | select(.locale | contains("fi"))] | map( { (.key): .value } ) | add' < "$LOCALIZATION_FILE" > "$DEFAULT_TEXTS_FILE"

    report_divergence "$CATEGORY" "$PREVIOUS" "$LOCALIZATION_FILE" "$LOCALIZATION_FILE"
}


if [ ! -x "$PRETTIER" ]; then
    printf '%s\n' "${C_RED}prettier puuttuu polusta $PRETTIER${C_OFF}" >&2
    printf '%s\n' "Aja ensin: ${C_CYA}cd web && pnpm install${C_OFF}" >&2
    exit 1
fi

load_and_format koski \
    "src/main/resources/mockdata/lokalisointi/koski.json" \
    "src/main/resources/localization/koski-default-texts.json"
load_and_format valpas \
    "src/main/resources/valpas/mockdata/lokalisointi/valpas.json" \
    "src/main/resources/valpas/localization/valpas-default-texts.json"

printf '\n%s\n' "${C_BOLD}=== yhteenveto ===${C_OFF}"
if [ "$REWIND_TOTAL" -gt 0 ]; then
    printf '  %s%s%d arvoa palautuu lähde-commitia edeltäneeseen tekstiin. Nämä vaativat huomiota.%s\n' \
        "$C_BOLD" "$C_BRED" "$REWIND_TOTAL" "$C_OFF"
fi
if [ "$CHANGED_TOTAL" -gt 0 ]; then
    printf '  %s%d arvoa muuttuu tai poistuu kaikkiaan.%s Lähde-commitista näet,\n' \
        "$C_YEL" "$CHANGED_TOTAL" "$C_OFF"
    printf '%s\n' "  onko kyse aiemmasta päivityksestä vai tikettityöstä."
elif [ "$ADDED_TOTAL" -gt 0 ]; then
    printf '  %sEi poistuneita eikä muuttuneita arvoja, vain %d uutta käännöstä.%s\n' \
        "$C_GRN" "$ADDED_TOTAL" "$C_OFF"
else
    printf '%s\n' "  ${C_GRN}Ei eroja: repon lokalisoinnit vastaavat tuotantoa.${C_OFF}"
fi
