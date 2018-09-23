# Caching kata

## The kata
### 1. Scheduled non-blocking fetch

The microservice shall periodically fetch data from an endpoint and overwrites the cache which is compressed.

#### Run tests

```
sbt test
```

#### Start

```
sbt run
```

#### Verify running application
The following should return ```OK```

```
curl http://localhost:9000/internal/status
```

#### Get cached item at index 0
The following should return an item

```
curl http://localhost:9000/0
```

### Remarks
#### Startup and Health Status
When starting the application it waits for the first fetch of the items to be cached.
When the cache is ready the status endpoint starts to return ```OK```.
This allows to check if the application is running on the one hand and ready to serve the cache on the other hand. If the status endpoint returns 
```OK``` you can add the application to a loadbalancer.

#### Number of actors
It is necessary to split the retrieval and delivery of the caches into two actors. If this would be done by one actor the actor would not be able 
to serve cached items while fetching their updates.

