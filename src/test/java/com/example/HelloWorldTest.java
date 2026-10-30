package com.example;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HelloWorldTest {

    @Test
    void greetsWorldWhenNameIsNull() {
        assertEquals("Hello, World!", HelloWorld.greet(null));
    }

    @Test
    void greetsWorldWhenNameIsBlank() {
        assertEquals("Hello, World!", HelloWorld.greet("   "));
    }

    @Test
    void greetsByNameAndTrimsSpaces() {
        assertEquals("Hello, Gautam!", HelloWorld.greet("  Gautam "));
    }
}
