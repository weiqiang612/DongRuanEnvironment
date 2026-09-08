package com.dongruan.environment;

import com.dongruan.environment.entity.AqiFeedback;
import com.dongruan.environment.service.IAqiFeedbackService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class DemoApplicationTests {

    @Test
    void contextLoads() {
    }

    @Resource
    private IAqiFeedbackService aqiFeedbackService;

    @Test
    void testselect(){
        System.out.println("测试");
        List<AqiFeedback> list = aqiFeedbackService.list();
        for (AqiFeedback aqiFeedback : list) {
            System.out.println(aqiFeedback);
        }
    }
    @Test
    void testadd(){
        System.out.println("测试增加");
        AqiFeedback aqiFeedback = createTestFeedback();
        assertTrue(aqiFeedbackService.save(aqiFeedback));
        assertNotNull(aqiFeedback.getAfId());
        assertTrue(aqiFeedbackService.removeById(aqiFeedback.getAfId()));
    }

    @Test
    void testfindById(){
        System.out.println("测试查询");
        AqiFeedback feedback = aqiFeedbackService.getById(1);
        System.out.println(feedback);
    }

    @Test
    void testdelete(){
        System.out.println("测试删除");
        AqiFeedback feedback = createTestFeedback();
        assertTrue(aqiFeedbackService.save(feedback));
        assertTrue(aqiFeedbackService.removeById(feedback.getAfId()));
    }

    @Test
    void testUpdate(){
        System.out.println("测试修改");
        AqiFeedback feedback = createTestFeedback();
        assertTrue(aqiFeedbackService.save(feedback));
        feedback.setAddress("河北理工真好");
        assertTrue(aqiFeedbackService.updateById(feedback));
        assertTrue(aqiFeedbackService.removeById(feedback.getAfId()));
    }

    private AqiFeedback createTestFeedback() {
        AqiFeedback aqiFeedback = new AqiFeedback();
        aqiFeedback.setTelId("13800000000");
        aqiFeedback.setProvinceId(1);
        aqiFeedback.setCityId(1);
        aqiFeedback.setAddress("河北理工");
        aqiFeedback.setInformation("今天天气白天转多云");
        aqiFeedback.setEstimatedGrade(1);
        aqiFeedback.setAfDate("2026-09-03");
        aqiFeedback.setAfTime("10:00:00");
        return aqiFeedback;
    }
}
