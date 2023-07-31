/*
 * Copyright 2021 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package functions;

import static com.google.cloud.ServiceOptions.*;

import com.google.cloud.functions.CloudEventsFunction;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import functions.eventpojos.PubSubBody;
import io.cloudevents.CloudEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SubscribePublishToFromTopic implements CloudEventsFunction {
  private static final Logger logger = Logger.getLogger(SubscribePublishToFromTopic.class.getName());

  private static final String topicName = "downstream";

  @Override
  public void accept(CloudEvent event) {
    // The Pub/Sub message is passed as the CloudEvent's data payload.
    if (event.getData() != null) {
      // Extract Cloud Event data and convert to PubSubBody
      String cloudEventData = new String(event.getData().toBytes(), StandardCharsets.UTF_8);
      Gson gson = new Gson();
      PubSubBody body = gson.fromJson(cloudEventData, PubSubBody.class);

      // Retrieve and decode PubSub message data
      String encodedData = body.getMessage().getData();
      String decodedData =
          new String(Base64.getDecoder().decode(encodedData), StandardCharsets.UTF_8);
      logger.info("Decoded data:" + decodedData + "!");

      try {
        String projectID = getDefaultProjectId();
        ByteString byteStr = ByteString.copyFrom(cloudEventData, StandardCharsets.UTF_8);
        PubsubMessage pubsubApiMessage = PubsubMessage.newBuilder().setData(byteStr).build();

        // create the publisher
        logger.info("ProjectID = " + projectID);
        Publisher publisher = Publisher.newBuilder(TopicName.of(projectID, topicName)).build();

        // Publish message to "downstream" topic
        publisher.publish(pubsubApiMessage).get();
      } catch (InterruptedException | ExecutionException | IOException e) {
        logger.log(Level.SEVERE, "Error publishing Pub/Sub message: " + e.getMessage(), e);
      }
    }
  }
}

