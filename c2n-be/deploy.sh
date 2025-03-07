
#!/usr/bin/env sh
echo "***********start to init docker env file***********"
cp deployment/docker-env/portal-api.env.example deployment/docker-env/portal-api.env.example
echo "***********start to maven install java project***********"
mvn clean install -Dmaven.test.skip
echo "***********start to build docker image***********"
cd portal-api
./docker-build.sh
echo "***********start to start docker***********"
cd ../deployment
docker compose up -d
echo "***********docker started successful***********"
