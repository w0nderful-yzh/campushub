ARG STACK_VERSION=8.13.4
FROM docker.elastic.co/elasticsearch/elasticsearch:${STACK_VERSION}

ARG STACK_VERSION=8.13.4

RUN bin/elasticsearch-plugin install --batch \
    "https://release.infinilabs.com/analysis-ik/stable/elasticsearch-analysis-ik-${STACK_VERSION}.zip"
