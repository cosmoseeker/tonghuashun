package org.qianyuxiang;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Random;

class MockData {
    private String rootDir = "E:\\idea\\tonghuashun\\src\\main\\resources\\";

    @Test
    void testMain() {
        File indicatorFile = new File(rootDir + 2022, "ROE");
        Random rand = new Random();
        try (BufferedWriter br = new BufferedWriter(new FileWriter(indicatorFile))) {
            for (int i = 0; i < 20000; i++) {
                br.write(i + "," + i);
                br.newLine();
                br.flush();
            }
            br.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme