HOST = nuremberg.office.dc19.m6r.eu
DIR = /opt/kicker
VERSION=$(shell date +%Y%m%dT%H%M%S).git$(shell git rev-parse --short HEAD)

.PHONY: install
install:
	ssh root@$(HOST) "mkdir -p /opt/kicker; cd /opt/kicker; python -m virtualenv venv; . venv/bin/activate; pip install docker-compose"

.PHONY: docker-compose-production.yml
docker-compose-production.yml:
	@echo "sync local and remote docker-compose-production.yml; --update ensures we end up with the newest version on both sides"
	rsync --update root@$(HOST):$(DIR)/docker-compose.yml docker-compose-production.yml || true
	rsync docker-compose-production.yml --update root@$(HOST):$(DIR)/docker-compose.yml || true

.PHONY: deploy
deploy: docker-compose-production.yml
	ssh root@$(HOST) tmux kill-session -t "kicker" || true
	ssh root@$(HOST) 'tmux new-session -d -s "kicker" "cd $(DIR); . venv/bin/activate; docker-compose down; docker-compose pull; docker-compose up kicker"'

.PHONY: build-image
build-image:
	./gradlew buildDocker

.PHONY: push-image
push-image: build-image
	docker tag kicker docker.m6r.eu/kicker:$(VERSION)
	docker tag kicker docker.m6r.eu/kicker:latest
	docker push docker.m6r.eu/kicker:$(VERSION)
	docker push docker.m6r.eu/kicker:latest
