env = cloud

help:
	@echo ""
	@echo "make build	- Build the whole application, ready for running or testing"
	@echo "make front	- Build front end"
	@echo "make codegen	- Generate database access code from local Postgres database"
	@echo "make test	- Run unit tests"
	@echo "make run	- Run previously built application in local environment"
	@echo "make postgres	- Run local postgres server"
	@echo "make watch	- Watch for changes in webapp files"
	@echo "make deploy 	- Deploy to CSC's ePouta cloud"
	@echo "make dist version=<version> - Tag and deploy application to artifactory."
	@echo "make deploy env=<env> version=<version>	- Install deployed version to env."

logdir:
	@mkdir -p log
clean:
	mvn clean
	rm -fr web/target

### Building the application

build: logdir
	mvn compile
	# Built the whole application, ready for running or testing
front: logdir
	cd web && npm install
watch:
	cd web && npm run watch

### Running tests

test: logdir
	mvn test
testresults:
	less +`grep -n "FAILED" target/surefire-reports/koski-tests.txt|head -1|cut -d ':' -f 1` target/surefire-reports/koski-tests.txt
fronttest:
	cd web && npm run test

### Running application and database 

run:
	mvn exec:java $(JAVA_OPTS) -Dexec.mainClass=fi.oph.koski.jettylauncher.JettyLauncher
postgres:
	postgres --config_file=postgresql/postgresql.conf -D postgresql/data
postgres-clean:
	rm postgresql/data/postmaster.pid 2> /dev/null||true
elastic:
	elasticsearch -E path.conf=elasticsearch -E path.data=elasticsearch/data -E path.logs=elasticsearch/log

### Code checks

eslint: front
	cd web && npm run lint
scalastyle:
	mvn verify -DskipTests
lint: eslint scalastyle
owasp: 
	mvn dependency-check:check -P owasp
owaspresults:
	open target/dependency-check-report.html
snyk: # javascript dependency vulnerability check
	cd web && npm install && node_modules/snyk/cli/index.js test
mvndeps:
	mvn dependency:tree|less

### Dist and deploy

dist: check-version
	./scripts/dist.sh $(version)
deploy: check-version
	./scripts/deploy.sh $(env) $(version)
check-version:
ifndef version
		@echo "version is not set."
		@echo "Set version with version=<version>"
		@echo "Use version=local for locally installed version"
		exit 1
endif
great:
again:
