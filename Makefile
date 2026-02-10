MVN ?= ./mvnw
DOCKER ?= docker
COMPOSE ?= docker compose
IMAGE_NAME ?= twitter-spring:local

.PHONY: build test clean docker-build up down logs

build:
	$(MVN) -q -DskipTests package

test:
	$(MVN) test

clean:
	$(MVN) clean

docker-build:
	$(DOCKER) build -t $(IMAGE_NAME) .

up:
	$(COMPOSE) up -d --build

down:
	$(COMPOSE) down

logs:
	$(COMPOSE) logs -f app
