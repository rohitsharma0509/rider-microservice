package com.scb.rider.model.document;

import com.scb.rider.model.BaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@Document
@Getter
@Setter
@Cacheable
public class AppConfig extends BaseEntity {

    @Id
    private String id;

    private Integer version;

    private boolean forceUpdate;

}
