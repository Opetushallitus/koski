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
test: 
	mvn test
run:
	mvn exec:java -Dexec.mainClass=fi.oph.tor.jettylauncher.JettyLauncher
postgres:
	postgres --config_file=postgresql/postgresql.conf -D postgresql/data
watch: 
	cd web && npm run watch
