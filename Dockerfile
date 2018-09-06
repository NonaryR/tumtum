FROM clojure:onbuild

COPY . /opt/tumtum
WORKDIR /opt/tumtum

EXPOSE 8080

RUN lein deps
RUN lein migrate prod
RUN lein cljsbuild once min
CMD ["lein", "run", "prod"]
