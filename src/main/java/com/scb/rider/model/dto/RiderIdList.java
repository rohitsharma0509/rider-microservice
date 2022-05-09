package com.scb.rider.model.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@ToString
@Getter
@Setter
public class RiderIdList {
    private List<String> riders;

    public static RiderIdList of(List<String> riderProfileIds) {
        RiderIdList idList = new RiderIdList();
        idList.setRiders(riderProfileIds);
        return idList;
    }
}
