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

build: front server
	# Built the whole application, ready for running or testing
front:
	cd web && npm install
	# front end build done
server:
	mvn install -DskipTests
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
watch:
	cd web && npm run watch
it: test
happen:
#	# Pow pow!
deploy:
	git remote set-url tordev git@`cloud/pouta-nslookup $(TOR-SERVER)`:tor.git
	GIT_SSH=cloud/ssh-wrapper.sh git push -f tordev
tail:
	@cloud/ssh-wrapper.sh -l cloud-user `cloud/pouta-nslookup $(TOR-SERVER)` 'tail -f /home/git/logs/*log'
