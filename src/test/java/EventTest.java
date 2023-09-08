forEach: Policy
fileName: {{namePascalCase}}StepDefinition.java
path: {{boundedContext.name}}/src/test/java/{{options.package}}/common
---

package {{options.package}};

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.context.ApplicationContext;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.MimeTypeUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import {{options.package}}.config.kafka.KafkaProcessor;
import {{options.package}}.domain.*;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class {{namePascalCase}}EventTest {

   private static final Logger LOGGER = LoggerFactory.getLogger(EventTest.class);
   
   @Autowired
   private KafkaProcessor processor;
   @Autowired
   private MessageCollector messageCollector;
   @Autowired
   private ApplicationContext applicationContext;
   @Autowired
   public InventoryRepository repository;

   @Test
   @SuppressWarnings("unchecked")
   public void test{{namePascalCase}}() {

      //given:  


      //when:  
      
   {{#incoming "Event" this}}
      {{pascalCase name}} {{camelCase name}} = new {{pascalCase name}}();
      {{#fieldDescriptors}}
      {{../nameCamelCase}}.set{{pascalCase name}}(...);
      {{/fieldDescriptors}}

       
      InventoryApplication.applicationContext = applicationContext;

      ObjectMapper objectMapper = new ObjectMapper();
      try {
         String msg = objectMapper.writeValueAsString({{camelCase name}});

         processor.inboundTopic().send(
            MessageBuilder
            .withPayload(msg)
            .setHeader(
               MessageHeaders.CONTENT_TYPE,
               MimeTypeUtils.APPLICATION_JSON
            )
            .setHeader("type", {{camelCase name}}.getEventType())
            .build()
         );

         // will happen something here.
         
         //then:   

         Message<String> received = (Message<String>) messageCollector.forChannel(processor.outboundTopic()).poll();
   {{/incoming}}

      {{#outgoing "Event" this}}
         {{pascalCase name}} {{camelCase name}} = objectMapper.readValue(received.getPayload(), {{pascalCase name}}.class);

         LOGGER.info("Response received: {}", received.getPayload());

         assertNotNull(received.getPayload());
         assertEquals({{camelCase name}}.getId(), ...);

      {{/outgoing}}

      } catch (JsonProcessingException e) {
         // TODO Auto-generated catch block
         assertTrue("exception", false);
      }

     
   }


}

