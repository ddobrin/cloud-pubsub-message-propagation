# Publish messages from one Cloud Pub/Sub topic to another using EventArc and Cloud Functions v2

Create the upstream topic
```shell
gcloud pubsub topics create upstream
```

Create the downstream topic
```shell
gcloud pubsub topics create downstream
```

Build the code locally, for testing:
```shell
./mvnw package
```
Deploy the code, which creates the EventArc trigger automatically
```shell
gcloud functions deploy upstream-downstream --gen2 --source=. --entry-point=functions.SubscribePublishToFromTopic --runtime java17   --ingress-settings internal-and-gclb    --service-account="" --allow-unauthenticated --trigger-topic=upstream --region=us-central1
```

Deployment output
```shell
  ✓ [Build] Logs are available at [https://console.cloud.google.com/cloud-build/builds;region=us-central1/f214d9b7-d299-4a44-acc0-0b316aac8344?project=399726347076]                               
    [Service]                                                                                                                                                                                      
  . [Trigger]                                                                                                                                                                                      
  . [ArtifactRegistry]                                                                                                                                                                             
  . [Healthcheck]                                                                                                                                                                                  
  . [Triggercheck]                                                                                                                                                                                 
Completed with warnings:                                                                                                                                                                           
  [INFO] A new revision will be deployed serving with 100% traffic.
You can view your function in the Cloud Console here: https://console.cloud.google.com/functions/details/us-central1/upstream-downstream?project=test-project

buildConfig:
  build: projects/399726347076/locations/us-central1/builds/f214d9b7-d299-4a44-acc0-0b316aac8344
  entryPoint: functions.SubscribePublishToFromTopic
  runtime: java17
  source:
    storageSource:
      bucket: gcf-v2-sources-399726347076-us-central1
      generation: '1690826283904793'
      object: upstream-downstream/function-source.zip
  sourceProvenance:
    resolvedStorageSource:
      bucket: gcf-v2-sources-399726347076-us-central1
      generation: '1690826283904793'
      object: upstream-downstream/function-source.zip
environment: GEN_2
eventTrigger:
  eventType: google.cloud.pubsub.topic.v1.messagePublished
  pubsubTopic: projects/test-project/topics/upstream
  retryPolicy: RETRY_POLICY_DO_NOT_RETRY
  serviceAccountEmail: 399726347076-compute@developer.gserviceaccount.com
  trigger: projects/test-project/locations/us-central1/triggers/upstream-downstream-384315
  triggerRegion: us-central1
labels:
  deployment-tool: cli-gcloud
name: projects/test-project/locations/us-central1/functions/upstream-downstream
serviceConfig:
  allTrafficOnLatestRevision: true
  availableCpu: '0.1666'
  availableMemory: 256M
  ingressSettings: ALLOW_INTERNAL_AND_GCLB
  maxInstanceCount: 100
  maxInstanceRequestConcurrency: 1
  revision: upstream-downstream-00003-jal
  service: projects/test-project/locations/us-central1/services/upstream-downstream
  serviceAccountEmail: 399726347076-compute@developer.gserviceaccount.com
  timeoutSeconds: 60
  uri: https://upstream-downstream-ndn7ymldhq-uc.a.run.app
state: ACTIVE
updateTime: '2023-07-31T17:59:29.409772021Z'
url: https://us-central1-test-project.cloudfunctions.net/upstream-downstream

```

Send messages to the upstream topic
```shell
gcloud pubsub topics publish upstream --message "upstream-downstream"
```

Subscribe to the downstream topic
```shell
 gcloud pubsub subscriptions pull downstream-sub --auto-ack
```

Observe the message
```shell
┌────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┬──────────────────┬──────────────┬────────────┬──────────────────┬────────────┐
│                                                                                                                                                  DATA                                                                                                                                                  │    MESSAGE_ID    │ ORDERING_KEY │ ATTRIBUTES │ DELIVERY_ATTEMPT │ ACK_STATUS │
├────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┼──────────────────┼──────────────┼────────────┼──────────────────┼────────────┤
│ {"message":{"data":"dGVzdDI=","messageId":"8755078934865762","message_id":"8755078934865762","publishTime":"2023-07-31T16:52:15.298Z","publish_time":"2023-07-31T16:52:15.298Z"},"subscription":"projects/test-project/subscriptions/eventarc-us-central1-upstream-downstream-384315-sub-646"} │ 8752093729087847 │              │            │                  │ SUCCESS    │
└────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────────┴──────────────────┴──────────────┴────────────┴──────────────────┴────────────┘

```