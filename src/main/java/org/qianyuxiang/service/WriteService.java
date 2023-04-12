package org.qianyuxiang.service;

import org.qianyuxiang.model.IdentityEntity;

import java.math.BigDecimal;
import java.util.List;

public interface WriteService {
    boolean update(String volume, String indicator, String identity, BigDecimal value);
}
