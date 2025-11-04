# Money Transfer System
## Prerequisites
* Java 25
* Maven
* Docker Compose
* JMeter

## Build and Run
1. Clone the repository
2. Run `./build-images.sh`
3. Run `docker-compose up`
4. Wait until all containers are up and running

## Load data
1. `cd cli-tool`
2. `mvn spring-boot:run`
3. Run `help` command to see available commands
4. Create clients and generate accounts

## Test
1. Run Jmeter
2. Load `./tests/test.jmx`
3. Configure Thread Group per your preference
4. Run the test

## Monitoring
* Docker Compose stack has Prometheus and Grafana.
* Open http://localhost:3000 in your browser.
* Enter `admin` as username and `admin` as password.
* Configure Prometheus data source by adding `http://prometheus:9090` as URL.
* Update datasource id in `./monitoring/dashboard.json`
  * Replace uuid field in `"datasource": {"type": "prometheus", "uid": "bf32si3ftxnuoc"}` element in an entire file(58 entries)
* Import Grafana dashboards from `./monitoring/dashboard.json`
