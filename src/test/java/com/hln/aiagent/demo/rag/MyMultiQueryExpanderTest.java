package com.hln.aiagent.demo.rag;

import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.ai.rag.Query;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
class MyMultiQueryExpanderTest {
    @Resource
    private MyMultiQueryExpander myMultiQueryExpander;

    @Test
    void expand() {
        List<Query> expand = myMultiQueryExpander.expand("你好呀~婚后关系不好怎么办吼吼吼吼吼吼吼吼吼吼吼吼吼吼吼");
        Assertions.assertNotNull(expand);
    }
}