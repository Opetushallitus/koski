KOSKI-SERVER = tordev-tor-app
TARGET = qa
commit = $(shell git rev-parse --short HEAD)
dist-dir = target/koski-$(commit)
deploy-file = $(dist-dir).zip

help:
	@echo ""
	@echo "make build	- Build the whole application, ready for running or testing"
	@echo "make front	- Build front end"
	@echo "make server	- Build server side"
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
build: front server
	# Built the whole application, ready for running or testing
front:
	cd web && npm install
	# front end build done
server:
	mvn compile
	# server-side build done
test: build
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
dist: front
	mvn dependency:copy-dependencies package -DskipTests -Pdeploy -DoutputDirectory=$(dist-dir)/lib
	./scripts/dist.sh $(dist-dir)
newdeploy: dist
	./scripts/deploy.sh $(TARGET) $(deploy-file)
deploy:
	-@git remote remove tordev &> /dev/null
	git remote add tordev git@$(KOSKI-SERVER):tor.git
	git push -f tordev head:master
tail:
	ssh $(KOSKI-SERVER) 'tail -f /home/git/logs/*log'
ssh:
	ssh $(KOSKI-SERVER)
