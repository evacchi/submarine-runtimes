/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.integrationtests.springboot;

import java.time.Duration;
import java.util.Collections;
import java.util.Arrays;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.AdminClient;
import org.junit.jupiter.api.extension.ExtendWith;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kie.kogito.testcontainers.springboot.KafkaSpringBootTestResource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.kafka.core.KafkaAdmin;

import static io.restassured.RestAssured.given;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.CoreMatchers.equalTo;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = KogitoSpringbootApplication.class)
@ContextConfiguration(initializers = KafkaSpringBootTestResource.Conditional.class)
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class PingPongMessageTest {

    static {
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
    
    @Value("${spring.kafka.bootstrap-servers}")
    String kafkaServer;

    @Autowired
    private KafkaAdmin admin;

//    @Bean
//    NewTopic topic1(){
//        return new NewTopic("pong_start", -1, (short) -1);
//    }
//
//    @Bean
//    NewTopic topic2(){
//        return new NewTopic("pong_end", -1, (short) -1);
//    }

    @BeforeEach
    void init(){
        try {
//            System.out.println("admin = " + admin);
        System.out.println("kafkaServer = " + kafkaServer);
//        KafkaAdmin admin = new KafkaAdmin(Collections.singletonMap(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer));
//            AdminClient client = AdminClient.create(admin.getConfigurationProperties());
//            System.out.println("-- creating --");
//            client.createTopics(Arrays.asList(
//                    new NewTopic("pong_start", 1, (short) 1),
//                    new NewTopic("pong_end", 1, (short) 1)
//            ));
//            System.out.println("-- listing --");
//            client.listTopics().names().get().forEach(System.out::println);
//            client.close();
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }

    @Test
    void testPingPongBetweenProcessInstances() {
        String pId = given().body("{ \"message\": \"hello\" }")
                .contentType(ContentType.JSON)
                .when()
                .post("/ping_message")
                .then()
                .statusCode(201)
                .extract().body().path("id");

        System.out.println("pId = " + pId);

        await().atMost(Duration.ofSeconds(5))
                .untilAsserted(() -> given()
                        .contentType(ContentType.JSON)
                        .when()
                        .get("/ping_message/{pId}", pId)
                        .then()
                        .statusCode(200)
                        .body("message", equalTo("hello world")));

        given()
                .contentType(ContentType.JSON)
                .when()
                .post("/ping_message/{pId}/end", pId)
                .then()
                .statusCode(200);

        given()
                .contentType(ContentType.JSON)
                .when()
                .get("/ping_message/{pId}", pId)
                .then()
                .statusCode(404);
    }
}
