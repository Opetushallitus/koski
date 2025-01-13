SHELL := /bin/bash

env = cloud
mvn_argline =
mvn_opts =

DOCKER_COMPOSE = docker compose
DOCKER_COMPOSE_OPTS = --force-recreate --renew-anon-volumes --build

.PHONY: help
help:
	@echo ""
	@echo "make build	- Build the whole application, ready for running or testing"
	@echo "make front	- Build front end"
	@echo "make test	- Run unit tests"
	@echo "make run	- Run previously built application in local environment"
	@echo "make docker-dbs	- Start databases with docker compose"
	@echo "make watch	- Watch for changes in webapp files"
	@echo "make clean	- Remove generated build data"
	@echo "make dist version=<version> - Builds and verifies application version"

.PHONY: logdir
logdir:
	@mkdir -p log

.PHONY: clean
clean:
	mvn clean
	rm -fr web/target

### Building the application

.PHONY: build
build: logdir
	mvn compile
	# Built the whole application, ready for running or testing

.PHONY: build-snapshot-image
build-snapshot-image: build
	docker build -f docker-build/Dockerfile -t local-snapshot --build-arg KOSKI_VERSION=master-SNAPSHOT .

.PHONY: front
front: logdir
	cd web && npm ci && npm run build:prod

.PHONY: watch
watch:
	cd web && npm run watch

.PHONY: watch-prod
watch-prod:
	NODE_ENV="'production'" make watch

.PHONY: ts-types
ts-types:
	cd web && rm -rf app/types/fi && curl http://localhost:7021/types/update && npx prettier --write app/types

.PHONY: prettier-format-mock-data
prettier-format-mock-data:
	./scripts/prettier-format-koodistot.sh


### Running tests

.PHONY: localizationtest
localizationtest:
	mvn $(mvn_opts) -DargLine="$(mvn_argline)" test -DwildcardSuites="fi.oph.koski.inenvironmentlocalization"

.PHONY: testresults
testresults:
	less -R +`grep -n "FAILED" target/surefire-reports/koski-tests.txt|head -1|cut -d ':' -f 1` target/surefire-reports/koski-tests.txt

.PHONY: js-unit-test
js-unit-test:
	cd web && npm run unit-test

.PHONY: js-unit-test-watch
js-unit-test-watch:
	cd web && npm run unit-test-watch

.PHONY: backtest
backtest:
	mvn $(mvn_opts) -DargLine="$(mvn_argline)" test -DwildcardSuites="\
	fi.oph.koski.cache,fi.oph.koski.editor,fi.oph.koski.environment,\
	fi.oph.koski.etk,fi.oph.koski.hakemuspalvelu,fi.oph.koski.henkilo,fi.oph.koski.http,\
	fi.oph.koski.integrationtest,fi.oph.koski.json,fi.oph.koski.kela,fi.oph.koski.koodisto,\
	fi.oph.koski.koskiuser,fi.oph.koski.localization,fi.oph.koski.cas,fi.oph.koski.log,\
	fi.oph.koski.luovutuspalvelu,fi.oph.koski.migration,fi.oph.koski.migri,\
	fi.oph.koski.mocha,fi.oph.koski.mydata,fi.oph.koski.omaopintopolkuloki,\
	fi.oph.koski.opiskeluoikeus,fi.oph.koski.oppilaitos,fi.oph.koski.oppivelvollisuustieto,\
	fi.oph.koski.organisaatio,fi.oph.koski.perftest,fi.oph.koski.raportit,\
	fi.oph.koski.raportointikanta,fi.oph.koski.schedule,fi.oph.koski.schema,\
	fi.oph.koski.sso,fi.oph.koski.sure,fi.oph.koski.tools,\
	fi.oph.koski.userdirectory,fi.oph.koski.util,fi.oph.koski.valpas,\
	fi.oph.koski.valvira,fi.oph.koski.versioning,fi.oph.koski.virta,fi.oph.koski.vkt,fi.oph.koski.vtj,\
	fi.oph.koski.ytl,fi.oph.koski.ytr,fi.oph.koski.ytl,fi.oph.koski.meta,\
	fi.oph.koski.ytl,fi.oph.koski.api,fi.oph.koski.frontendvalvonta,fi.oph.koski.tiedonsiirto,\
	fi.oph.koski.typemodel,fi.oph.koski.suoritusjako,fi.oph.koski.suoritusjako.suoritetuttutkinnot,\
	fi.oph.koski.massaluovutus,fi.oph.koski.omadataoauth2.unit,fi.oph.koski.omadataoauth2.e2e"

.PHONY: backtestnonmock
backtestnonmock:
	mvn $(mvn_opts) -DargLine="$(mvn_argline)" test -DwildcardSuites="fi.oph.koski.nonmockloginsecurity"

.PHONY: fronttest
fronttest:
	mvn $(mvn_opts) -DargLine="$(mvn_argline)" test -DwildcardSuites="fi.oph.koski.mocha" -DtagsToExlude="parallelmocha"

.PHONY: integrationtest
integrationtest:
		mvn $(mvn_opts) -DargLine="$(mvn_argline)" test -Dsuites="fi.oph.koski.e2e.KoskiFrontSpec"

.PHONY: omadataoauth2e2e
omadataoauth2e2e:
		mvn $(mvn_opts) -DargLine="$(mvn_argline)" test -Dsuites="fi.oph.koski.omadataoauth2.e2e.OmaDataOAuth2E2ESpec"

.PHONY: screenshot
screenshot:
	ls -t web/target/screenshots|head -1|xargs -I{} open web/target/screenshots/{}

.PHONY: test
test: backtest fronttest backtestnonmock

### Running application and database

.PHONY: run
run:
	mvn exec:java $(JAVA_OPTS) -Dexec.mainClass=fi.oph.koski.jettylauncher.JettyLauncher

docker-dbs:
	${DOCKER_COMPOSE} up ${DOCKER_COMPOSE_OPTS}

docker-dbs-arm64:
	${DOCKER_COMPOSE} -f docker-compose-arm64.yaml up ${DOCKER_COMPOSE_OPTS}

db-docs:
	cd scripts/db-schema-docs && ./generate.sh

view-db-docs:
	npx http-serve documentation/tietokantaskeemat

### Code checks

.PHONY: eslint
eslint:
	cd web && npm run lint

.PHONY: prettier
	cd web && npm run prettier:check

.PHONY: scalastyle
scalastyle:
	mvn scalastyle:check -P scalastyle

.PHONY: lint
lint: eslint prettier scalastyle

.PHONY: owasp
owasp:
	mvn dependency-check:check -P owasp --batch-mode

.PHONY: owaspresults
owaspresults:
	open target/dependency-check-report.html

.PHONY: snyk
snyk: # javascript dependency vulnerability check
	mvn generate-resources # to download correct node/npm version via frontend-maven-plugin
	node web/node_modules/snyk/dist/cli/index.js test web valpas-web

.PHONY: checkdoc_validation
checkdoc_validation:
	./scripts/checkdoc_validation.sh

.PHONY: checkdoc_schema
checkdoc_schema:
	./scripts/checkdoc_schema.sh

.PHONY: checkdoc_dbdocs
checkdoc_dbdocs:
	./scripts/checkdoc_dbdocs.sh

.PHONY: mvndeps
mvndeps:
	mvn dependency:tree|less

.PHONY: scala-console
scala-console:
	./scripts/mvn-scala-console.sh

.PHONY: reset-raportointikanta
reset-raportointikanta:
	./scripts/local-reset-raportointikanta.sh

### Valpas

.PHONY: install-and-verify-valpas-jest-deps
install-and-verify-valpas-jest-deps:
	./scripts/install-and-verify-valpas-jest-deps.sh

.PHONY: valpas-fronttest-1
valpas-fronttest-1:
	mvn $(mvn_opts) -DargLine="$(mvn_argline)" scalatest:test -Dsuites="fi.oph.koski.frontendvalpas.jest.ValpasFrontSpec1" --batch-mode

.PHONY: valpas-fronttest-2
valpas-fronttest-2:
	mvn $(mvn_opts) -DargLine="$(mvn_argline)" scalatest:test -Dsuites="fi.oph.koski.frontendvalpas.jest.ValpasFrontSpec2" --batch-mode

.PHONY: valpas-fronttest-3
valpas-fronttest-3:
	mvn $(mvn_opts) -DargLine="$(mvn_argline)" scalatest:test -Dsuites="fi.oph.koski.frontendvalpas.jest.ValpasFrontSpec3" --batch-mode

.PHONY: valpas-fronttest-4
valpas-fronttest-4:
	mvn $(mvn_opts) -DargLine="$(mvn_argline)" scalatest:test -Dsuites="fi.oph.koski.frontendvalpas.jest.ValpasFrontSpec4" --batch-mode

.PHONY: valpas-fronttest-5
valpas-fronttest-5:
	mvn $(mvn_opts) -DargLine="$(mvn_argline)" scalatest:test -Dsuites="fi.oph.koski.frontendvalpas.jest.ValpasFrontSpec5" --batch-mode

.PHONY: valpas-fronttest-6
valpas-fronttest-6:
	mvn $(mvn_opts) -DargLine="$(mvn_argline)" scalatest:test -Dsuites="fi.oph.koski.frontendvalpas.jest.ValpasFrontSpec6" --batch-mode


### Dist

.PHONY: dist
dist: check-version
	./scripts/dist.sh $(version)

.PHONY: check-version
check-version:
ifndef version
	@echo "version is not set."
	@echo "Set version with version=<version>"
	exit 1
endif
