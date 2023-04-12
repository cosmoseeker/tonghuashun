package org.qianyuxiang.repositry;

import org.qianyuxiang.model.IdentityEntity;

import java.math.BigDecimal;
import java.util.List;

public interface IndicatorDao {
    boolean exist(String volume, String indicator);

    List<IdentityEntity> query(String volume, String indicator);

    boolean create(String volume, String indicator, String identity, BigDecimal value);

    boolean insert(String volume, String indicator, String identity, BigDecimal value);
}
