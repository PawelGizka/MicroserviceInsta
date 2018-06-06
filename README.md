Instagram like application created to learn microservice architecture, 
docker, RabbitMQ and Django.


It consists of three microservices:
- UserManager (Django)
- PhotosManager (Scala)
- UploadManager (Scala)

To start:

``sudo docker network create webnet``

``cd PhotosManager``

``sbt assembly``

``sudo docker build -t photos-app .``

``sudo docker build -t couchbase-custom couchbase``

``sudo docker-compose up``

``cd ..``

``cd UploadManager``

``sbt assembly``

``sudo docker build -t upload-app .``

``sudo docker-compose up``

``cd ..``

``cd UserManager``

``sudo docker-compose up --build``

