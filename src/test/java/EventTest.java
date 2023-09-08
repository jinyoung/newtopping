forEach: Policy
---
package moornmo.project;

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

import moornmo.project.config.kafka.KafkaProcessor;
import moornmo.project.domain.Inventory;
import moornmo.project.domain.InventoryRepository;
import moornmo.project.domain.InventoryUpdated;
import moornmo.project.domain.OrderCreated;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EventTest {

   private static final Logger LOGGER = LoggerFactory.getLogger(EventTest.class);
   
   @Autowired
   private KafkaProcessor processor;
   @Autowired
   private MessageCollector messageCollector;
   @Autowired
   public InventoryRepository repository;
   @Autowired
   private ApplicationContext applicationContext;

   @Test
   @SuppressWarnings("unchecked")
   public void testAccepted() {

      //given:  inventory에 상품 1번의 재고가 10개 있는 상태에서

      Inventory inventory = new Inventory();
      inventory.setId(1L);
      inventory.setStock(10L);
      repository.save(inventory);

      //when:   해당 상품에 대한 주문이벤트가 오면 
      
      OrderCreated o = new OrderCreated();
      o.setOrderId("1");
      o.setProductId(1L);

      InventoryApplication.applicationContext = applicationContext;

      ObjectMapper objectMapper = new ObjectMapper();
      try {
         String msg = objectMapper.writeValueAsString(o);

         processor.inboundTopic().send(
            MessageBuilder
            .withPayload(msg)
            .setHeader(
               MessageHeaders.CONTENT_TYPE,
               MimeTypeUtils.APPLICATION_JSON
            )
            .setHeader("type", o.getEventType())
            .build()
         );

         // will happen something here.
         
         //then:   재고량이 1 줄어든 이벤트가 퍼블리시 되어야 할 것이다.

         Message<String> received = (Message<String>) messageCollector.forChannel(processor.outboundTopic()).poll();
         InventoryUpdated inventoryUpdated = objectMapper.readValue(received.getPayload(), InventoryUpdated.class);

         LOGGER.info("Order response received: {}", received.getPayload());
         assertNotNull(received.getPayload());
         assertEquals(inventoryUpdated.getId(), o.getProductId());
         assertEquals(inventoryUpdated.getStock(), (Long)(inventory.getStock() - 1));

      } catch (JsonProcessingException e) {
         // TODO Auto-generated catch block
         assertTrue("exception", false);
      }
      
   }


}