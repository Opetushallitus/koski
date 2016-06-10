KOSKI-SERVER = tordev-tor-app
TARGET = tordev
final-name = koski-$(shell git rev-parse --short HEAD)

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
	@echo "make tail	- Tail the cloud logs"
	@echo "make ssh	- Ssh connection to koski cloud server"
	@echo "make KOSKI-SERVER=tordev-authentication-app ssh	- Ssh connection to authentication app server in test env"

clean:
	mvn clean
build:
	mvn compile
	# Built the whole application, ready for running or testing
front:
	cd web && npm install
	# front end build done
test:
	mvn test
testresults:
	less +`grep -n "FAILED" target/surefire-reports/koski-tests.txt|head -1|cut -d ':' -f 1` target/surefire-reports/koski-tests.txt
fronttest:
	cd web && npm run test
run:
	mvn exec:java $(JAVA_OPTS) -Dexec.mainClass=fi.oph.koski.jettylauncher.JettyLauncher
postgres:
	postgres --config_file=postgresql/postgresql.conf -D postgresql/data
postgres-clean:
	rm postgresql/data/postmaster.pid 2> /dev/null||true
watch:
	cd web && npm run watch
eslint: front
	cd web && npm run lint
scalastyle:
	mvn verify -DskipTests
lint: eslint scalastyle
it: test
happen:
#	# Pow pow!
dist:
	mkdir target && git archive --format=tar --prefix=build/ HEAD | (cd target && tar xf -)
	cp -r web/node_modules target/build/web/ || true
	cd target/build && mvn install -DskipTests=true -DfinalName=$(final-name)
dist-artifact:
	./target/build/scripts/dist.sh $(version)
deploy: dist
	./scripts/deploy.sh $(TARGET) target/build/target/$(final-name).war
tail:
	ssh $(KOSKI-SERVER) 'tail -f /home/git/logs/*log'
ssh:
	ssh $(KOSKI-SERVER)
