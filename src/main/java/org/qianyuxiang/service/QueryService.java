package org.qianyuxiang.service;


import org.qianyuxiang.model.IdentityEntity;

import java.util.List;

/**
 * 指标查询服务
 */
public interface QueryService {
    /**
     * @param volume 期数
     * @param indicator 指标
     * @param ascOrder 是否升序，否则降序
     * @param limit 需要的数据大小
     * @return 指标列表
     */
    List<IdentityEntity> query(String volume, String indicator, boolean ascOrder, Integer limit);
}
