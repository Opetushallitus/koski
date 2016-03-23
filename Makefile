TOR-SERVER = tordev-tor-app
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
	@echo "make ssh	- Ssh connection to tor cloud server"
	@echo "make TOR-SERVER=tordev-authentication-app ssh	- Ssh connection to authentication app server in test env"

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
codegen:
	# Generate database access code from local Postgres database
	mvn compile exec:java -Dexec.mainClass=fi.oph.tor.db.CodeGenerator
test: build
	mvn test
fronttest:
	cd web && npm run test
run:
	mvn exec:java $(JAVA_OPTS) -Dexec.mainClass=fi.oph.tor.jettylauncher.JettyLauncher
postgres:
	postgres --config_file=postgresql/postgresql.conf -D postgresql/data
postgres-clean:
	rm postgresql/data/postmaster.pid 2> /dev/null||true
watch:
	cd web && npm run watch
eslint: front
	cd web && npm run lint
scalastyle:
	mvn clean verify
lint: eslint scalastyle
it: test
happen:
#	# Pow pow!
deploy:
	-@git remote remove tordev &> /dev/null
	git remote add tordev git@`cloud/pouta-nslookup $(TOR-SERVER)`:tor.git
	GIT_SSH=cloud/ssh-wrapper.sh git push -f tordev head:master
tail:
	@cloud/ssh-wrapper.sh -l cloud-user `cloud/pouta-nslookup $(TOR-SERVER)` 'tail -f /home/git/logs/*log'
ssh:
	@cloud/ssh-wrapper.sh -l cloud-user `cloud/pouta-nslookup $(TOR-SERVER)`
