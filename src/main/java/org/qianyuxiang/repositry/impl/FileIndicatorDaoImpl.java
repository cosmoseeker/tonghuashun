package org.qianyuxiang.repositry.impl;

import org.qianyuxiang.model.IdentityEntity;
import org.qianyuxiang.repositry.IndicatorDao;
import org.springframework.stereotype.Repository;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
public class FileIndicatorDaoImpl implements IndicatorDao {
    private final String rootDir = "E:\\idea\\tonghuashun\\src\\main\\resources\\";
    //数据缓冲区
    private final Map<String, Queue<IdentityEntity>> updateBuffers = new ConcurrentHashMap<>();
    private final Map<String, Thread> lock = new ConcurrentHashMap<String, Thread>();
    private volatile boolean writing = false;

    @Override
    public boolean exist(String volume, String indicator) {
        File indicatorFile = new File(rootDir + volume, indicator);
        //不存在时直接返回
        return indicatorFile.exists();
    }

    @Override
    public List<IdentityEntity> query(String volume, String indicator) {
        List<IdentityEntity> identityEntities = new ArrayList<>();
        File indicatorFile = new File(rootDir + volume, indicator);
        try (BufferedReader br = new BufferedReader(new FileReader(indicatorFile))) {
            String str;
            while ((str = br.readLine()) != null && str.length() > 0) {
                //一个具体的实体指标
                String[] entityIndicator = str.split(",");
                IdentityEntity entity = new IdentityEntity();
                entity.setIdentity(entityIndicator[0]);
                entity.setValue(new BigDecimal(entityIndicator[1]));
                identityEntities.add(entity);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return identityEntities;
    }

    @Override
    public boolean create(String volume, String indicator, String identity, BigDecimal value) {
        File indicatorFile = new File(rootDir + volume, indicator);
        try (BufferedWriter br = new BufferedWriter(new FileWriter(indicatorFile))) {
            br.write(identity + "," + value);
            br.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public boolean insert(String volume, String indicator, String identity, BigDecimal value) {
        //因为是基于文件实现的，没办法像数据库一样通过数据库本身实现事务和锁，需要手动模拟实现一下
        //修改文件中间的内容，既耗性能又麻烦，但是要求不能使用数据库、中间件
        String key = genKey(volume, indicator);
        Thread current = lock.computeIfAbsent(key, useless -> Thread.currentThread());

        //不确定更新的频率，如果更新频次非常高，那应该设置缓冲区达到一定大小后再刷新，否则太过频繁
        //目前是假设读的频次极高，写的频次一般

        //抢到更新锁，执行更新任务
        if (current == Thread.currentThread()) {
            try {
                List<IdentityEntity> refreshed = refresh(volume, indicator, identity, value, key);
                write(refreshed);
            } finally {
                lock.remove(key);
            }
            return true;
        }
        //没抢到锁，放入缓冲区，等待下次更新
        else {
            Queue<IdentityEntity> queue = updateBuffers.getOrDefault(key, new LinkedBlockingQueue<>());
            IdentityEntity identityEntity = new IdentityEntity();
            identityEntity.setIdentity(identity);
            identityEntity.setValue(value);
            queue.add(identityEntity);
            updateBuffers.put(key, queue);
            return true;
        }
    }

    private void write(List<IdentityEntity> refreshed) {
        File indicatorFile = new File(rootDir + 2022, "ROE");
        try (BufferedWriter br = new BufferedWriter(new FileWriter(indicatorFile))) {
            for (int i = 0, refreshedSize = refreshed.size(); i < refreshedSize; i++) {
                IdentityEntity identityEntity = refreshed.get(i);
                br.write(identityEntity.getIdentity() + "," + identityEntity.getValue());
                br.newLine();
                br.flush();
            }
            br.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 计算出最新版数据
     *
     * @param volume
     * @param indicator
     * @param identity
     * @param value
     * @param key
     * @return
     */
    private List<IdentityEntity> refresh(String volume, String indicator, String identity, BigDecimal value, String key) {
        List<IdentityEntity> allOld = query(volume, indicator);
        Queue<IdentityEntity> readToUpdate = updateBuffers.get(key);
        updateBuffers.remove(key);//重置缓冲区
        IdentityEntity identityEntity = new IdentityEntity();
        identityEntity.setIdentity(identity);
        identityEntity.setValue(value);
        readToUpdate.add(identityEntity);
        Map<String, IdentityEntity> updateMap = readToUpdate.stream().
                collect(Collectors.toMap(IdentityEntity::getIdentity, Function.identity()));
        for (Iterator<IdentityEntity> iterator = allOld.iterator(); iterator.hasNext(); ) {
            IdentityEntity entity = iterator.next();
            if (updateMap.containsKey(entity.getIdentity())) {
                iterator.remove();
            }
        }
        allOld.addAll(readToUpdate);
        return allOld;
    }

    private String genKey(String volume, String indicator) {
        return volume + indicator;
    }

    //可能存在静默期，一直没有新的写入请求触发缓冲区数据写入，为了保持数据时效性，还需要一个定时任务定时扫描缓冲区数据，保证更新频次
}
