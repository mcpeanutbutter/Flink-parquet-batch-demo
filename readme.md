
### Building the docker images
After starting minikube, run 
```shell
eval $(minikube -p minikube docker-env)
```
so docker builds to the minikube docker environment. From the root of this project, run
```shell
docker build --no-cache -t flink-custom:latest .
```
then `cd` into `mini-custom` and run 
```shell
docker build --no-cache -t minio-custom:latest .
```

### Launch pod
Launch the pod via
```shell
kubectl apply -f /path/to/project/kubernetes/flink-pod.yml
```
and open a shell in the minio container via
```shell
kubectl exec --stdin --tty flink-pod -c minio-custom -- /bin/bash
```
Here, run the following commands to create a bucket and move some parquet and some csv files into it:
```shell
mc alias set minio-custom http://127.0.0.1:9000 minioadmin minioadmin
mc mb minio-custom/data
mc cp --recursive /opt/parquet-data minio-custom/data
mc cp --recursive /opt/csv-data minio-custom/data
```
If you want to access the MiniO UI (optional), `exit` the container and run
```shell
kubectl port-forward pods/flink-pod 9001 
```
to make it available under `localhost:9001`.

### Launch job

Open a shell in the flink container via
```shell
kubectl exec --stdin --tty flink-pod -c flink-custom -- /bin/bash
```
and run the job in application mode via
```shell
./bin/flink run-application \
--target kubernetes-application --class demo.Job \
-Dkubernetes.cluster-id=flink-application-cluster \
-Dkubernetes.container.image=flink-custom:latest \
-Dkubernetes.service-account=flink-service-account \
-Dparallelism.default=2 \
-Dexecution.runtime-mode=BATCH \
-Ds3.endpoint=http://flink-service:9000 \
-Ds3.path-style=true \
-Ds3.access-key=minioadmin \
-Ds3.secret-key=minioadmin \
local:///opt/flink/usrlib/flink-parquet-batch-demo.jar \
--input_path s3a://data/parquet-data/ \
--output_path s3a://data/output-data/
```