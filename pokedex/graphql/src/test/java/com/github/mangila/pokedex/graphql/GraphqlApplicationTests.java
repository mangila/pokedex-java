package com.github.mangila.pokedex.graphql;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.graphql.test.tester.GraphQlTester;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureGraphQlTester
class GraphqlApplicationTests {

    @Autowired
    private GraphQlTester graphQlTester;

    @Test
    void testQuery() {

    }
}