package com.example.catalogservice.controller;

import com.example.catalogservice.dto.CatalogDto;
import com.example.catalogservice.jpa.CatalogEntity;
import com.example.catalogservice.service.CatalogService;
import com.example.catalogservice.vo.RequestCreateCatalog;
import com.example.catalogservice.vo.ResponseCatalog;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/")
@Slf4j
public class CatalogController {
    Environment env;
    CatalogService catalogService;

    @Autowired
    public CatalogController(Environment env, CatalogService catalogService) {
        this.env = env;
        this.catalogService = catalogService;
    }

    @GetMapping("/health_check")
    public String status() {
        return String.format("It's Working in Catalog Service on PORT %s",
                env.getProperty("local.server.port"));
    }

    @GetMapping("/catalogs")
    public ResponseEntity<List<ResponseCatalog>> getCatalogs() {
        Iterable<CatalogEntity> catalogList = catalogService.getAllCatalogs();

        List<ResponseCatalog> result = new ArrayList<>();
        catalogList.forEach(v -> {
            result.add(new ModelMapper().map(v, ResponseCatalog.class));
        });

        return ResponseEntity.status(HttpStatus.OK).body(result);
    }

    @GetMapping("/catalogs/{productId}")
    public ResponseEntity<ResponseCatalog> getCatalog(@PathVariable String productId) {
        log.info("Before retrieve catalogs data");
        CatalogEntity catalogEntity = catalogService.getCatalog(productId);

        log.info("After retrieve catalogs data");

        if (catalogEntity != null) {
            return ResponseEntity.status(HttpStatus.OK).body(
                    new ModelMapper().map(catalogEntity, ResponseCatalog.class));
        } else {
            return new ResponseEntity(HttpStatus.NOT_FOUND);
        }
    }
//    @PostMapping("/{userId}/orders")
//    public ResponseEntity<ResponseOrder> createOrder(@PathVariable("userId") String userId,
//                                                     @RequestBody RequestOrder orderDetails) {
//        log.info("Before add orders data");
//
//        // check how much stock is left
//        // order-service -> catalog-service
//        // resttemplate or openfeign(o)
//        boolean isAvailable = true;
//        ResponseCatalog responseCatalog = catalogServiceClient.getCatalog(orderDetails.getProductId());
//        if (responseCatalog != null &&
//                responseCatalog.getStock() - orderDetails.getQty() < 0)
//            isAvailable = false;
//
//        if (isAvailable) {
//            ModelMapper mapper = new ModelMapper();
//            mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
//
//            OrderDto orderDto = mapper.map(orderDetails, OrderDto.class);
//            orderDto.setUserId(userId);
//
//            OrderDto createdOrder = orderService.createOrder(orderDto);
////            ResponseOrder responseOrder = mapper.map(createdOrder, ResponseOrder.class);
//
//            /* send message to Kafka topic */
////            createdOrder.setOrderId(UUID.randomUUID().toString());
////            createdOrder.setTotalPrice(orderDto.getQty() * orderDto.getUnitPrice());
//            kafkaProducer.send("example-catalog-topic", createdOrder);
//            ResponseOrder responseOrder = mapper.map(createdOrder, ResponseOrder.class);
//
////            orderProducer.send("orders", orderDto);
//
//            log.info("After added orders data");
//            return ResponseEntity.status(HttpStatus.CREATED).body(responseOrder);
//        } else {
//            log.info("After added orders data");
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
//        }
//    }

    @PostMapping("/catalogs")
    public ResponseEntity<ResponseCatalog> createCatalog(@RequestBody RequestCreateCatalog catalog){
        log.info("Before add catalog data");

        ModelMapper mapper = new ModelMapper();
        mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        CatalogDto catalogDto = mapper.map(catalog, CatalogDto.class);
        catalogService.createCatalog(catalogDto);

        // convert CatalogDto to ResponseCatalog
        ResponseCatalog responseCatalog = mapper.map(catalogDto, ResponseCatalog.class);

        log.info("After added catalog data");
        return ResponseEntity.status(HttpStatus.CREATED).body(responseCatalog);


    }


    @DeleteMapping("/catalogs/{productId}")
    public ResponseEntity<String> deleteCatalog(@PathVariable("productId") String productId){

        String msg = "Done";
        catalogService.deleteCatalog(productId);
        return ResponseEntity.status(HttpStatus.OK).body(msg);
    }

}
