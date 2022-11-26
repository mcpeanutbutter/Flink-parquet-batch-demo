FROM minio/minio

RUN curl https://dl.min.io/client/mc/release/linux-amd64/mc \
  --create-dirs \
  -o /opt/minio-binaries/mc

RUN chmod +x /opt/minio-binaries/mc
ENV PATH=$PATH:/opt/minio-binaries/

RUN mkdir -p /opt/parquet-data
COPY ./parquet-data/ /opt/parquet-data/

RUN mkdir -p /opt/csv-data
COPY ./csv-data/ /opt/csv-data/
