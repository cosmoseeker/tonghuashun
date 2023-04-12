package org.qianyuxiang.service.impl;

import org.qianyuxiang.repositry.IndicatorDao;
import org.qianyuxiang.service.WriteService;

import javax.annotation.Resource;
import java.math.BigDecimal;

public class WriteServiceImpl implements WriteService {
    @Resource
    private IndicatorDao indicatorDao;

    @Override
    public boolean update(String volume, String indicator, String identity, BigDecimal value) {
        //如果不存在，说明需要新建
        if(!indicatorDao.exist(volume, indicator)) {
            indicatorDao.create(volume, indicator, identity, value);
        }
        //在已有期的指标中更新
        else {
            indicatorDao.insert(volume, indicator, identity, value);
        }
        return true;
    }
}
