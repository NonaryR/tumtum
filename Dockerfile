FROM clojure:onbuild
ARG build

COPY . /opt/tumtum
WORKDIR /opt/tumtum

EXPOSE 8080

RUN lein deps
RUN lein cljsbuild once "$build"
