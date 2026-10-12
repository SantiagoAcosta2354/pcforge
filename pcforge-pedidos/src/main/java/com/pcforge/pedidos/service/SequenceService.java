package com.pcforge.pedidos.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import com.pcforge.pedidos.domain.Counter;

/**
 * Genera el numero legible de cada pedido.
 */
@Service
public class SequenceService {
    private static final String ORDERS_SEQUENCE = "orders";
    private final MongoTemplate mongoTemplate;
    @Value("${pcforge.orders.number-prefix:PF}")
    private String prefix;

    public String nextOrderNumber() {
        Counter counter = mongoTemplate.findAndModify(Query.query(Criteria.where("_id").is(ORDERS_SEQUENCE)), new Update().inc("seq", 1), FindAndModifyOptions.options().upsert(true).returnNew(true), Counter.class);
        long seq = counter == null ? 1L : counter.getSeq();
        return String.format("%s-%06d", prefix, seq);
    }

    public SequenceService(final MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }
}
