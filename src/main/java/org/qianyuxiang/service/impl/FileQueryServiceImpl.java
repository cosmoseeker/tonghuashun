package org.qianyuxiang.service.impl;

import org.qianyuxiang.model.IdentityEntity;
import org.qianyuxiang.service.QueryService;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;

/**
 * 基于文件的实现版本
 */
public class FileQueryServiceImpl implements QueryService {
    private String rootDir = "E:\\idea\\tonghuashun\\src\\main\\resources\\";
    private final Map<String, List<IdentityEntity>> cache = new WeakHashMap<>();

    /**
     * 实现一个类似于列式存储的基于文件的指标查询排序服务
     *
     * @param volume    期数
     * @param indicator 指标
     * @param ascOrder  是否升序，否则降序
     * @param limit     需要的数据大小
     * @return 对应的指标实体列表
     */
    @Override
    public List<IdentityEntity> query(String volume, String indicator, boolean ascOrder, Integer limit) {
        //volume期数对应文件夹,indicator指标对应文件
        File indicatorFile = new File(rootDir + volume, indicator);
        //不存在时直接返回
        if (!indicatorFile.exists()) {
            return Collections.emptyList();
        }

        //文件读取
        String unionKey = volume + indicator;
        List<IdentityEntity> candidateResult;
        if (cache.containsKey(unionKey)) {
            candidateResult = readFromCache(cache.get(unionKey), ascOrder, limit);

        } else {
            candidateResult= readFromFile(indicatorFile, unionKey, ascOrder, limit);
        }
        if (ascOrder) {
            candidateResult.sort(Comparator.comparing(IdentityEntity::getValue));
        } else {
            candidateResult.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        }
        return candidateResult;
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
     * @param indicatorFile
     * @param unionKey
     * @param ascOrder
     * @param limit
     * @return
     */
    private List<IdentityEntity> readFromFile(File indicatorFile, String unionKey, boolean ascOrder, Integer limit) {
        List<IdentityEntity> indicatorCache = new ArrayList<>();
        Queue<IdentityEntity> result = getCandidateResult(ascOrder, limit);

        try (BufferedReader br = new BufferedReader(new FileReader(indicatorFile))) {
            String str;
            while ((str = br.readLine()) != null && str.length() > 0) {
                //一个具体的实体指标
                String[] entityIndicator = str.split(",");
                IdentityEntity entity = new IdentityEntity();
                entity.setIdentity(entityIndicator[0]);
                entity.setValue(new BigDecimal(entityIndicator[1]));
                indicatorCache.add(entity);
                addResult(result, entity, ascOrder, limit);
            }
            cache.put(unionKey, indicatorCache);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
}
