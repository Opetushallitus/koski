#!/bin/bash

set -euo pipefail

# Refreshes the localization texts from the production Lokalisointipalvelu,
# which in turn gets its data from Tolgee.
#
# Both files of each pair are rewritten: the mockdata file (every language) and
# *-default-texts.json (Finnish). Production's values win, but what production
# lacks is kept in its old place instead of dropped:
# - every key in *-default-texts.json. When the app is deployed to QA, it adds
#   the keys missing from Lokalisointipalvelu from that file, so a key dropped
#   from it never gets there and the UI shows the bare key. Production gets the
#   key only when the localizations are next copied from QA, so a new key is
#   usually missing from production until then.
# - the mockdata entries of those keys, so their translations survive locally.
# Mockdata entries whose key is in neither production nor the default texts are
# dropped.
#
# The raw diff runs to tens of thousands of lines and hides real regressions,
# so after each fetch the script reports how the incoming data diverges from
# what the repo had, and what to do about it. Both files are compared, because
# they drift apart: tickets usually edit only the default texts. See AGENTS.md
# "Updating localizations" for the rules behind that advice.
#
# To separate changes made in Tolgee from changes that undo an edit made in the
# repo, every value the fetch would change is attributed to the commit that last
# set its local value, by walking the file's history. The commit accounting for
# most of them is the previous refresh — those are translators' work. Anything
# attributed elsewhere came from a ticket and is about to be reverted.

SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
ROOT_DIR=$( cd -- "$SCRIPT_DIR/.." && pwd )
cd "$ROOT_DIR"

# How far back to walk when attributing a value to the commit that set it.
# Counted in commits that touched the localization file, not in commits overall.
# Anything older than this is reported without a source commit. It should reach
# past the previous refresh: merges touch the default texts about twice a week.
ATTRIBUTION_MAX_COMMITS=100

# Longest per-commit list printed before the rest is summarised as a count.
GROUP_LIST_LIMIT=10

# Separates the fields of the report's internal lines. Not a tab: read merges
# consecutive tabs, so an empty text would shift the fields after it.
readonly SEP=$'\x1f'

# Indexes a localization file as {key: {locale: value}}. A mockdata file is an
# array of entries; *-default-texts.json is a {key: value} object of Finnish.
readonly JQ_IDX='
    def idx: (if type == "object" then to_entries | map({key, locale: "fi", value}) else . end)
        | reduce .[] as $e ({}; .[$e.key][$e.locale] = $e.value);
'

# Returns $new plus the entries of $old that $new lacks and keep accepts, each
# placed right after the entry that preceded it in $old. Re-sorting would
# rewrite the whole file: neither file is in plain sort order.
readonly JQ_MERGE='
    def merge_missing($new; $old; id; keep):
        (reduce ($new[] | id) as $i ({}; .[$i] = true)) as $in_new
        | (reduce $old[] as $e ({anchor: null, head: [], after: {}};
              ($e | id) as $i
              | if $in_new[$i] then .anchor = $i
                elif ($e | keep | not) then .
                elif .anchor == null then .head += [$e]
                else .after[.anchor] += [$e] end)) as $kept
        | $kept.head + [ $new[] | ., ($kept.after[id] // [])[] ];
'

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

# Use the installed Prettier version pinned in the root package.json.
PRETTIER="$ROOT_DIR/node_modules/.bin/prettier"
if [ ! -x "$PRETTIER" ]; then
    pnpm install --frozen-lockfile
fi

ADDED_TOTAL=0
CHANGED_TOTAL=0
REWIND_TOTAL=0
KEPT_TOTAL=0

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

# Walks the history of $1 newest first, along the commits that git log lists
# for the arguments after $3, starting from the contents in $2. Each entry of
# the pending file $3 ([[key, locale], …]) goes to the first commit whose parent
# had a different value, emitted as
# "key<TAB>locale<TAB>sha<TAB>subject<TAB>haspre<TAB>pre", where pre is the
# value that commit replaced. Entries emitted are removed from $3.
function walk_history() {
    local -r rel_path="$1"
    local -r start="$2"
    local -r pending="$3"
    shift 3
    local -r newer=$(mktemp -p "$WORK_DIR")
    local -r older=$(mktemp -p "$WORK_DIR")

    [ "$(jq 'length' "$pending")" -gt 0 ] || return 0
    cp "$start" "$newer"
    local sha subject
    while IFS=$'\t' read -r sha subject; do
        git -C "$ROOT_DIR" show "$sha^:$rel_path" > "$older" 2>/dev/null || printf '[]' > "$older"

        jq -r -n --slurpfile a "$newer" --slurpfile b "$older" --slurpfile p "$pending" \
                 --arg sha "${sha:0:10}" --arg subject "${subject//$'\t'/ }" "$JQ_IDX"'
            def clean: gsub("[\n\t\r]"; " ");
            ($a[0] | idx) as $A | (($b[0] // []) | idx) as $B |
            $p[0][] | select($A[.[0]][.[1]] != $B[.[0]][.[1]])
            | $B[.[0]][.[1]] as $pre
            | "\(.[0])\t\(.[1])\t\($sha)\t\($subject)\t\(if $pre == null then "0" else "1" end)\t\(($pre // "") | clean)"'

        jq -c -n --slurpfile a "$newer" --slurpfile b "$older" --slurpfile p "$pending" "$JQ_IDX"'
            ($a[0] | idx) as $A | (($b[0] // []) | idx) as $B |
            [ $p[0][] | select($A[.[0]][.[1]] == $B[.[0]][.[1]]) ]' > "$pending.next"
        mv "$pending.next" "$pending"

        cp "$older" "$newer"
        [ "$(jq 'length' "$pending")" -gt 0 ] || break
    done < <(git -C "$ROOT_DIR" log --format=$'%H\t%s' "$@" -- "$rel_path")
}

# Replaces each merge commit in the attributions on stdin with the commit on the
# merged branch that set the value, so that the report shows that commit's
# message. The merge stays when the branch never set the value itself: a
# conflict resolution, or a stale branch bringing an old text back.
function refine_merges() {
    local -r rel_path="$1"
    local -r depth="$2"
    local -r attributions=$(mktemp -p "$WORK_DIR")
    local -r replacements=$(mktemp -p "$WORK_DIR")
    cat > "$attributions"

    local merge subject pr tip merged pending
    for merge in $(cut -f3 "$attributions" | sort -u); do
        [ "$merge" != "-" ] && [ "$depth" -lt 5 ] || continue
        git -C "$ROOT_DIR" rev-parse -q --verify "$merge^2" > /dev/null || continue
        tip=$(mktemp -p "$WORK_DIR"); merged=$(mktemp -p "$WORK_DIR"); pending=$(mktemp -p "$WORK_DIR")
        git -C "$ROOT_DIR" show "$merge^2:$rel_path" > "$tip" 2>/dev/null || continue
        git -C "$ROOT_DIR" show "$merge:$rel_path" > "$merged"

        awk -F'\t' -v sha="$merge" '$3 == sha { print $1 "\t" $2 }' "$attributions" \
            | jq -R -s --slurpfile t "$tip" --slurpfile m "$merged" "$JQ_IDX"'
                ($t[0] | idx) as $T | ($m[0] | idx) as $M |
                [ split("\n")[] | select(length > 0) | split("\t")
                  | select($T[.[0]][.[1]] == $M[.[0]][.[1]]) ]' > "$pending"

        pr=""
        subject=$(git -C "$ROOT_DIR" log -1 --format=%s "$merge")
        if [ "$depth" -eq 0 ] && [[ "$subject" =~ ^Merge\ pull\ request\ (#[0-9]+) ]]; then
            pr=" (PR ${BASH_REMATCH[1]})"
        fi
        walk_history "$rel_path" "$tip" "$pending" --first-parent "$merge^1..$merge^2" \
            | refine_merges "$rel_path" $(( depth + 1 )) \
            | awk -F'\t' -v OFS='\t' -v pr="$pr" '{ print $1, $2, $3, $4 pr }' >> "$replacements"
    done

    awk -F'\t' -v OFS='\t' '
        FILENAME == ARGV[1] { sha[$1 FS $2] = $3; subj[$1 FS $2] = $4; next }
        ($1 FS $2) in sha { $3 = sha[$1 FS $2]; $4 = subj[$1 FS $2] }
        { print }' "$replacements" "$attributions"
}

# Attributes each changed value to the commit that last set the local value, by
# walking the history of $1 starting from the pre-fetch contents in $2.
# Reads "key<TAB>locale" lines on stdin and emits
# "key<TAB>locale<TAB>sha<TAB>subject<TAB>haspre<TAB>pre", where pre is the value
# that commit replaced. Comparing the incoming text against pre is what tells a
# straight revert apart from a newer text that merely overwrites a local edit.
#
# The walk follows master's first parents, so that each step compares master
# before and after one commit or merge: a branch commit's parent is an older
# master, which would credit it with changes merged in the meantime. pre is
# therefore master's value before the merge; refine_merges then names the
# commit on the branch.
function attribute_changes() {
    local -r rel_path="$1"
    local -r previous="$2"
    local -r pending="$WORK_DIR/pending.json"

    jq -R -s 'split("\n") | map(select(length > 0) | split("\t")) ' > "$pending"
    [ "$(jq 'length' "$pending")" -gt 0 ] || return 0

    # Start from the contents before the fetch: the file on disk has already
    # been overwritten by the time this runs.
    walk_history "$rel_path" "$previous" "$pending" --first-parent -n "$ATTRIBUTION_MAX_COMMITS" \
        | refine_merges "$rel_path" 0

    jq -r --arg older "ennen $ATTRIBUTION_MAX_COMMITS viimeisintä tiedostoa muuttanutta committia" \
        '.[] | "\(.[0])\t\(.[1])\t-\t\($older)\t0\t"' "$pending"
}

# Compares a file before ($1) and after ($2) the fetch, given what production
# returned ($3). Emits one line per difference, fields separated by SEP:
#   A <count>                         keys or translations only in the new data
#   D <key> <locale>                  in the repo, gone from the new data
#   K <key> <locale>                  in the repo, not in production, kept
#   M <key> <locale> <before> <after>
# With $4, the default texts after the fetch, leaves out the Finnish of every
# key in them: that is reported from the default texts instead.
function diff_localizations() {
    jq -r -n --slurpfile prev "$1" --slurpfile curr "$2" --slurpfile fetched "$3" \
             --slurpfile defaults "${4:-/dev/null}" --arg t "$SEP" "$JQ_IDX"'
        def clean: gsub("[\n\t\r]"; " ");
        ($prev[0] // [] | idx) as $o |
        ($curr[0] | idx) as $n |
        ($fetched[0] | idx) as $f |
        ($defaults[0] // {}) as $d |
        def elsewhere($k; $l): $l == "fi" and ($d | has($k));
        ( [ $n | to_entries[] | .key as $k | .value | keys[]
            | select($o[$k][.] == null and (elsewhere($k; .) | not)) ] | length ) as $added |
        "A\($t)\($added)",
        ( $o | to_entries[] | .key as $k | .value | to_entries[]
          | .key as $l | .value as $v
          | select(elsewhere($k; $l) | not)
          | if   $n[$k][$l] == null then "D\($t)\($k)\($t)\($l)"
            elif $f[$k][$l] == null then "K\($t)\($k)\($t)\($l)"
            elif $n[$k][$l] != $v   then "M\($t)\($k)\($t)\($l)\($t)\($v | clean)\($t)\($n[$k][$l] | clean)"
            else empty end )
    '
}

# Prints entries grouped under their source commit, capped per group.
#   $1 name of an array of "key<SEP>locale[<SEP>before<SEP>after]"
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
            IFS="$SEP" read -r k l before after <<< "$line"
            if [ "${attr_sha["$k"$'\t'"$l"]:-}" = "$g" ]; then members+=("$line"); fi
        done
        [ "${#members[@]}" -gt 0 ] || continue
        printf '\n  %slähde %s %s%s\n' "$C_BOLD" "$g" "${group_subj[$g]}" "$C_OFF"
        shown=0

        if [ "$mode" = "locales" ]; then
            local -a keys_seen=()
            local -A locales_of=()
            for line in "${members[@]}"; do
                IFS="$SEP" read -r k l before after <<< "$line"
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
            IFS="$SEP" read -r k l before after <<< "$line"
            printf '    %s%s%s %s %s[%s]%s\n' \
                "$colour" "$marker" "$C_OFF" "$k" "$C_DIM" "$l" "$C_OFF"
            print_change "$before" "$after"
            shown=$(( shown + 1 ))
        done
    done
}

# Prints "key<TAB>locale" for each difference line given as an argument.
function changed_keys() {
    local line
    for line in "$@"; do
        [ "${line%%"$SEP"*}" != "A" ] || continue
        printf '%s\n' "$line"
    done | cut -d "$SEP" -f2,3 | tr "$SEP" '\t'
}

# Compares both files of a pair before and after the fetch and lists every
# difference once, attributed to the commit that last set the local value in
# that file. Finnish comes from the default texts, the text the repo means and
# the one the app adds keys from. The mockdata gives the other languages, and
# Finnish only for keys that the default texts lack.
function report_divergence() {
    local -r CATEGORY="$1"
    local -r PREV_MOCK="$2"
    local -r CURR_MOCK="$3"
    local -r PREV_DEFAULTS="$4"
    local -r CURR_DEFAULTS="$5"
    local -r FETCHED="$6"

    printf '\n%s\n' "${C_BOLD}=== ${CATEGORY}: mitä tuotannosta tuli ===${C_OFF}"

    if [ ! -s "$PREV_MOCK" ] || [ ! -s "$PREV_DEFAULTS" ]; then
        printf '%s\n' "  ${C_DIM}Ei aiempaa versiota, vertailu ohitetaan.${C_OFF}"
        return 0
    fi

    local -a default_diffs=() mock_diffs=() diffs=()
    mapfile -t default_diffs < <(diff_localizations "$PREV_DEFAULTS" "$CURR_DEFAULTS" "$FETCHED")
    mapfile -t mock_diffs < <(diff_localizations "$PREV_MOCK" "$CURR_MOCK" "$FETCHED" "$CURR_DEFAULTS")

    local added=0 kept_count=0 line
    for line in "${default_diffs[@]}" "${mock_diffs[@]}"; do
        case "${line%%"$SEP"*}" in
            A) added=$(( added + ${line#*"$SEP"} )) ;;
            K) diffs+=("$line"); kept_count=$(( kept_count + 1 )) ;;
            *) diffs+=("$line") ;;
        esac
    done

    printf '  %s%4d%s uutta avainta tai käännöstä\n' "$C_GRN" "$added" "$C_OFF"
    printf '  %s%4d%s muuttunutta tai poistunutta arvoa\n' \
        "$C_YEL" $(( ${#diffs[@]} - kept_count )) "$C_OFF"
    printf '  %s%4d%s tuotannosta puuttuvaa arvoa säilyy\n' "$C_CYA" "$kept_count" "$C_OFF"
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
    done < <(changed_keys "${default_diffs[@]}" | attribute_changes "$CURR_DEFAULTS" "$PREV_DEFAULTS"
             changed_keys "${mock_diffs[@]}" | attribute_changes "$CURR_MOCK" "$PREV_MOCK")

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

    local -a dels=() kept=() mods=() rewinds=()
    local kind k l before after kk entry
    for line in "${diffs[@]}"; do
        IFS="$SEP" read -r kind k l before after <<< "$line"
        kk="$k"$'\t'"$l"
        entry="${line#*"$SEP"}"
        if [ "$kind" = "D" ]; then
            dels+=("$entry")
        elif [ "$kind" = "K" ]; then
            kept+=("$entry")
        elif [ "${attr_haspre[$kk]:-0}" = "1" ] && [ "$after" = "${attr_pre[$kk]:-}" ]; then
            # Production holds exactly the text from before the source commit,
            # so that commit's change is being undone in full. This is an exact
            # comparison, not a guess, and it is rare enough to lead with.
            rewinds+=("$entry")
        else
            mods+=("$entry")
        fi
    done

    CHANGED_TOTAL=$(( CHANGED_TOTAL + ${#dels[@]} + ${#mods[@]} + ${#rewinds[@]} ))
    REWIND_TOTAL=$(( REWIND_TOTAL + ${#rewinds[@]} ))
    KEPT_TOTAL=$(( KEPT_TOTAL + ${#kept[@]} ))

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

    if [ "${#kept[@]}" -gt 0 ]; then
        printf '\n%s %s\n' "${C_CYA}${C_BOLD}EI VIELÄ TUOTANNOSSA${C_OFF}" \
            "${C_DIM}(avain on repossa, mutta ei tullut tuotannosta; säilyy)${C_OFF}"
        print_grouped kept "=" "$C_CYA" locales
        cat <<EOF

  ${C_CYA}Näin käsittelet:${C_OFF}
    Arvo säilyy, koska sovellus lisää QA-deployssa lokalisointipalveluun ne
    oletusteksteissä olevat avaimet, jotka sieltä puuttuvat. Tuotantoon avain
    päätyy vasta, kun lokalisoinnit viedään QA:lta tuotantoon, joten uusi avain
    puuttuu tuotannosta yleensä siihen asti.

    1. Jos avain on uusi: varmista, että sen sv- ja en-käännökset ovat
       Tolgeessa. Sovellus luo avaimen vain suomeksi, sv ja en tyhjinä.
    2. Jos avain ei ole enää käytössä:
         ${C_DIM}grep -rn "<avain>" src/main/scala web/app${C_OFF}
       poista se käsin oletusteksteistä ja mockdatasta.
EOF
    fi

    if [ "${#dels[@]}" -gt 0 ]; then
        printf '\n%s %s\n' "${C_YEL}${C_BOLD}POISTUU${C_OFF}" \
            "${C_DIM}(avain on mockdatassa, mutta ei oletusteksteissä eikä tuotannossa)${C_OFF}"
        print_grouped dels "-" "$C_YEL" locales
        cat <<EOF

  ${C_CYA}Näin käsittelet:${C_OFF}
    Avain voi puuttua tuotannosta kahdesta syystä: sitä ei ole koskaan viety
    Tolgeehen, tai se on poistettu sieltä tarkoituksella.

    1. Tarkista onko avain yhä käytössä:
         ${C_DIM}grep -rn "<avain>" src/main/scala web/app${C_OFF}
    2. Jos on: palauta arvo mockdataan, lisää avain oletusteksteihin JA lisää
       se Tolgeehen. Oletusteksteissä oleva avain säilyy seuraavissa
       päivityksissä.
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

    local -r PREVIOUS="$WORK_DIR/$CATEGORY.previous.json"
    local -r PREVIOUS_DEFAULTS="$WORK_DIR/$CATEGORY.previous-defaults.json"
    local -r FETCHED="$WORK_DIR/$CATEGORY.fetched.json"
    cp "$LOCALIZATION_FILE" "$PREVIOUS" 2>/dev/null || : > "$PREVIOUS"
    cp "$DEFAULT_TEXTS_FILE" "$PREVIOUS_DEFAULTS" 2>/dev/null || : > "$PREVIOUS_DEFAULTS"

    # jq -S pins a canonical key order. Lokalisointipalvelu has returned its
    # fields in a different order on every past refresh, which rewrote the whole
    # mockdata file and made it conflict with any unrelated edit to that file.
    curl --silent --show-error --fail \
        "https://virkailija.opintopolku.fi/lokalisointi/cxf/rest/v1/localisation?category=$CATEGORY" \
        | jq -S 'map( . * { createdBy: "anonymousUser", modifiedBy: "anonymousUser" } )' \
        > "$FETCHED"

    jq -n --slurpfile fetched "$FETCHED" --slurpfile old "$PREVIOUS_DEFAULTS" "$JQ_MERGE"'
        ($fetched[0] | map(select(.locale | contains("fi")) | { (.key): .value }) | add // {}) as $prod
        | merge_missing($prod | to_entries; $old[0] // {} | to_entries; .key; true)
        | from_entries
    ' > "$DEFAULT_TEXTS_FILE"

    jq -n --slurpfile fetched "$FETCHED" --slurpfile old "$PREVIOUS" \
          --slurpfile defaults "$DEFAULT_TEXTS_FILE" "$JQ_MERGE"'
        merge_missing($fetched[0]; $old[0] // []; .key + "\t" + .locale;
                      .key as $k | $defaults[0] | has($k))
    ' > "$LOCALIZATION_FILE"
    "$PRETTIER" --log-level warn --write --cache "$LOCALIZATION_FILE"

    report_divergence "$CATEGORY" "$PREVIOUS" "$LOCALIZATION_FILE" \
        "$PREVIOUS_DEFAULTS" "$DEFAULT_TEXTS_FILE" "$FETCHED"
}


if ! "$PRETTIER" --version >/dev/null; then
    printf '%s\n' "${C_RED}Komennon $PRETTIER suoritus epäonnistui${C_OFF}" >&2
    printf '%s\n' "Aja ensin: ${C_CYA}cd \"$ROOT_DIR\" && pnpm install --frozen-lockfile${C_OFF}" >&2
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
if [ "$KEPT_TOTAL" -gt 0 ]; then
    printf '  %s%d tuotannosta puuttuvaa arvoa säilyy.%s Tarkista niiden käännökset Tolgeessa.\n' \
        "$C_CYA" "$KEPT_TOTAL" "$C_OFF"
fi
if [ "$CHANGED_TOTAL" -gt 0 ]; then
    printf '  %s%d arvoa muuttuu tai poistuu kaikkiaan.%s Lähde-commitista näet,\n' \
        "$C_YEL" "$CHANGED_TOTAL" "$C_OFF"
    printf '%s\n' "  onko kyse aiemmasta päivityksestä vai tikettityöstä."
elif [ "$ADDED_TOTAL" -gt 0 ]; then
    printf '  %sEi poistuneita eikä muuttuneita arvoja, vain %d uutta käännöstä.%s\n' \
        "$C_GRN" "$ADDED_TOTAL" "$C_OFF"
elif [ "$KEPT_TOTAL" -eq 0 ]; then
    printf '%s\n' "  ${C_GRN}Ei eroja: repon lokalisoinnit vastaavat tuotantoa.${C_OFF}"
fi
