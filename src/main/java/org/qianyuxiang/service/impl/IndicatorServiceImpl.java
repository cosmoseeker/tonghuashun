package org.qianyuxiang.service.impl;

import org.qianyuxiang.model.IdentityEntity;
import org.qianyuxiang.repositry.IndicatorDao;
import org.qianyuxiang.service.IndicatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.ConcurrentReferenceHashMap;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

/**
 * 基于文件的实现版本
 */
@Service
public class IndicatorServiceImpl implements IndicatorService {
    private Logger logger = LoggerFactory.getLogger(IndicatorServiceImpl.class);
    @Resource
    private IndicatorDao indicatorDao;
    //确保缓存自动回收，缓存本身只是为了提效
    private final Map<String, List<IdentityEntity>> cache = new ConcurrentReferenceHashMap<>(16, ConcurrentReferenceHashMap.ReferenceType.SOFT);

    @Override
    public List<IdentityEntity> query(String volume, String indicator, boolean ascOrder, Integer limit) {
        //volume期数对应文件夹,indicator指标对应文件
        if (!indicatorDao.exist(volume, indicator)) {
            return Collections.emptyList();
        }

        //文件读取
        String unionKey = getCacheKey(volume, indicator);
        List<IdentityEntity> candidateResult;
        if (cache.containsKey(unionKey)) {
            logger.info("readFromCache");
            candidateResult = readFromCache(cache.get(unionKey), ascOrder, limit);
        } else {
            logger.info("readFromData");
            candidateResult = readFromData(indicatorDao.query(volume, indicator), unionKey, ascOrder, limit);
        }
        if (ascOrder) {
            candidateResult.sort(Comparator.comparing(IdentityEntity::getValue));
        } else {
            candidateResult.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        }
        return candidateResult;
    }

    @Override
    public boolean update(String volume, String indicator, String identity, BigDecimal value) {
        //如果不存在，说明需要新建
        if (!indicatorDao.exist(volume, indicator)) {
            indicatorDao.create(volume, indicator, identity, value);
        }
        //在已有期的指标中更新
        else {
            indicatorDao.insert(volume, indicator, identity, value);
            //删除缓存，下次访问时重新加载
            String cacheKey = getCacheKey(volume, indicator);
            cache.remove(cacheKey);
        }
        return true;
    }

    private static String getCacheKey(String volume, String indicator) {
        return volume + indicator;
    }

    /**
     * 从缓存中获取
     *
     * @param identityEntities
     * @param ascOrder
     * @param limit
     * @return
     */
    private List<IdentityEntity> readFromCache(List<IdentityEntity> identityEntities, boolean ascOrder, Integer limit) {
        Queue<IdentityEntity> result = getCandidateResult(ascOrder, limit);

        for (IdentityEntity identityEntity : identityEntities) {
            addResult(result, identityEntity, ascOrder, limit);
        }

        return new ArrayList<>(result);
    }

    /**
     * 从列文件中获取
     *
     * @param identityEntities
     * @param unionKey
     * @param ascOrder
     * @param limit
     * @return
     */
    private List<IdentityEntity> readFromData(List<IdentityEntity> identityEntities, String unionKey, boolean ascOrder, Integer limit) {
        Queue<IdentityEntity> result = getCandidateResult(ascOrder, limit);
        for (IdentityEntity identityEntity : identityEntities) {
            addResult(result, identityEntity, ascOrder, limit);
        }
        cache.put(unionKey, identityEntities);
        return new ArrayList<>(result);
    }

    private static Queue<IdentityEntity> getCandidateResult(boolean ascOrder, Integer limit) {
        Queue<IdentityEntity> result = ascOrder ?
                //正序，大顶堆，逐出最大的指标项
                new PriorityQueue<IdentityEntity>(limit, (a, b) -> b.getValue().compareTo(a.getValue())) :
                //逆序，小顶堆，逐出最小的指标项
                new PriorityQueue<IdentityEntity>(limit, Comparator.comparing(IdentityEntity::getValue));
        return result;
    }

    private void addResult(Queue<IdentityEntity> result, IdentityEntity entity, boolean ascOrder, Integer limit) {
        //队列满
        if (result.size() < limit) {
            result.add(entity);
        } else {
            IdentityEntity first = result.peek();
            //升序，当前值比最大的小，则替换
            if (ascOrder) {
                if (entity.getValue().compareTo(first.getValue()) < 0) {
                    result.remove();
                    result.add(entity);
                }
            }
            //降序，当前值比最小的大，则替换
            else {
                if (entity.getValue().compareTo(first.getValue()) > 0) {
                    result.remove();
                    result.add(entity);
                }
            }
        }
    }

    public void setIndicatorDao(IndicatorDao indicatorDao) {
        this.indicatorDao = indicatorDao;
    }
}
