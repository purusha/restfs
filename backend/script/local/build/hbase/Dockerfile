# https://github.com/HariSekhon/Dockerfiles/tree/master/hbase

FROM alpine:latest

ARG HBASE_VERSION=1.2.3

ENV PATH $PATH:/hbase/bin

ENV JAVA_HOME=/usr

ENV Z_HOST=zookeeper

ENV Z_PORT=2181

LABEL Description="HBase Dev", \
      "HBase Version"="1.2.3"

WORKDIR /

RUN set -euxo pipefail && \
    apk add --no-cache bash openjdk8-jre-base

RUN set -euxo pipefail && \
    apk add --no-cache wget tar && \
    if [ "${HBASE_VERSION:0:1}" = "1" ]; then \
        url="http://www.apache.org/dyn/closer.lua?filename=hbase/1.2.3/hbase-1.2.3-bin.tar.gz&action=download"; \
        url_archive="http://archive.apache.org/dist/hbase/1.2.3/hbase-1.2.3-bin.tar.gz"; \
    else \
        echo "unrecognized HBase version"; exit 1; \
    fi && \
    # --max-redirect - some apache mirrors redirect a couple times and give you the latest version instead
    #                  but this breaks stuff later because the link will not point to the right dir
    #                  (and is also the wrong version for the tag)
    wget -t 10 --max-redirect 1 --retry-connrefused -O "hbase-1.2.3-bin.tar.gz" "$url" || \
    wget -t 10 --max-redirect 1 --retry-connrefused -O "hbase-1.2.3-bin.tar.gz" "$url_archive" && \
    mkdir "hbase-1.2.3" && \
    tar zxf "hbase-1.2.3-bin.tar.gz" -C "hbase-1.2.3" --strip 1 && \
    test -d "hbase-1.2.3" && \
    ln -sv "hbase-1.2.3" hbase && \
    rm -fv "hbase-1.2.3-bin.tar.gz" && \
    { rm -rf hbase/{docs,src}; : ; } && \
    mkdir /hbase-data && \
    apk del tar wget

VOLUME /hbase-data

COPY entrypoint.sh /
COPY hbase-env.sh /hbase/conf/hbase-env.sh
COPY hbase-site.xml /hbase/conf/hbase-site.xml

RUN sed -i -e 's/ZOOKEEPER_HOST/'"$Z_HOST"'/g' /hbase/conf/hbase-site.xml
RUN sed -i -e 's/ZOOKEEPER_PORT/'"$Z_PORT"'/g' /hbase/conf/hbase-site.xml
COPY profile.d/java.sh /etc/profile.d/

# Stargate  8080  / 8085
# Thrift    9093  / 9098
# HMaster   16000 / 16010
# RS        16201 / 16301
EXPOSE 8080 8085 9093 9098 16000 16010 16201 16301

CMD  "/entrypoint.sh"