Start the application:
1. Launch Redis server using `redis-server`.
2. Launch RabbitMQ server using `brew services start rabbitmq`. Shutdown with `brew services stop rabbitmq`.
3. Launch Elastic search and Kibana server in docker.
4. Launch nodejs demon server with `npm run server`.


Authorization:
1. Google OAuth2.0 as the IDP.
2. We regard Postman as the client side application in the project.
3. Backend and Redis are the resource server.


Conditional Read/Write:
Set up ETag for every object and store them in Redis only. Didn't store in Elastic Search server. Conditional Read is for Get API, and Conditional Write is for Patch API.


Update/Patch Request: 
1. Arrays in json (like 'linkedPlanServices') won't be actually changed. Instead, we will merge the new object into the array. Also remember to add new object key to the edge list.  
2. Only objects in json (like 'planCostShares') will be changed.


Delete API:
Current only support plan deletion. Not supportive for children nodes like planCostShares, linkedPlanServices, linkedService and planserviceCostShares. Because there are property names conflicts in the current data structure.


Flattened Data Structure:
`parent node` ---`edge`---> `children node`
We store these three parts separately. Storage type of parent node and children node: hash. Storage type of edge: for object, string; for array, list.

For data update, we have to restrict any change on objectId and objectType. Block this in backend seems not easy. In reality, we need to implement this restriction on the frontend part also.


Redis:
Use `redis-cli flushall` to deletes all keys from all databases on current host.
For Map object in redis, use `hgetall key` to get all values. eg. `hgetall plan:12xvxc345ssdsds-508`.
For list in redis, use `lrange key 0 -1` to get all values. eg. `lrange plan:12xvxc345ssdsds-508#linkedPlanServices 0 -1`.
Check all existed keys include special substring, use `keys keyname *`. eg. `keys plan:12xvxc345ssdsds-508 *`.


RabbitMQ:
- Username: guest
- Password: guest

Periodically receive messages from queue (5000ms).


Elastic Search:
for es01 on docker
- username: elastic
- password: -UGLeTtN+p=ScruYNBEU