package org.qianyuxiang.repositry.impl;

import org.qianyuxiang.model.IdentityEntity;
import org.qianyuxiang.repositry.IndicatorDao;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class FileIndicatorDaoImpl implements IndicatorDao {
    private final String rootDir = "E:\\idea\\tonghuashun\\src\\main\\resources\\";
    private final Map<String, Queue<IdentityEntity>> buffer  = new ConcurrentHashMap<>();
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
        //开始写入
        writing = true;
        //如果文件正在写入，当前插入数据先放入缓冲区，等待本次写完成后，在下一次请求到来时触发新的写入操作
        if(writing) {
            String bufferKey = volume + indicator;
            Queue<IdentityEntity> queue = buffer.getOrDefault(bufferKey, new LinkedBlockingQueue<>());
            IdentityEntity identityEntity = new IdentityEntity();
            identityEntity.setIdentity(identity);
            identityEntity.setValue(value);
            queue.add(identityEntity);
            buffer.put(bufferKey, queue);
            return true;
        }


        List<IdentityEntity> entities = query(volume, indicator);


        writing = false;
        return true;
    }

    //可能存在静默期，没有新的写入请求触发缓冲区数据写入，还需要一个定时任务定时扫描缓冲区数据
}
