package com.crio.qeats.repositories;

import com.crio.qeats.dto.Item;
import com.crio.qeats.models.ItemEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface ItemRepository extends MongoRepository<ItemEntity, String> {

}
