MVN ?= ./mvnw
DOCKER ?= docker
COMPOSE ?= docker compose
IMAGE_NAME ?= twitter-spring:local
NPM ?= npm
UI_DIR ?= ui

.PHONY: build test clean docker-build up down logs ui-install ui-test ui-e2e test-all

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

ui-install:
	cd $(UI_DIR) && $(NPM) install

ui-test:
	cd $(UI_DIR) && $(NPM) run test

ui-e2e:
	cd $(UI_DIR) && $(NPM) run test:e2e

test-all: test ui-test ui-e2e
