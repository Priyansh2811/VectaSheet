package com.vectasheet.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HtmlSanitizerTests {

    @Test
    void stripsScriptTags() {
        String input = "<p>Hello</p><script>alert('xss')</script>";
        String out = HtmlSanitizer.sanitize(input);
        assertFalse(out.contains("<script"));
        assertTrue(out.contains("<p>Hello</p>"));
    }

    @Test
    void stripsEventHandlerAttributes() {
        String input = "<img src=\"x.png\" onerror=\"alert(1)\">";
        String out = HtmlSanitizer.sanitize(input);
        assertFalse(out.toLowerCase().contains("onerror"));
    }

    @Test
    void neutralizesJavascriptUrls() {
        String input = "<a href=\"javascript:alert(1)\">click</a>";
        String out = HtmlSanitizer.sanitize(input);
        assertFalse(out.toLowerCase().contains("javascript:"));
    }

    @Test
    void keepsNormalFormatting() {
        String input = "<h1>Title</h1><p><b>Bold</b> and <i>italic</i></p><ul><li>Item</li></ul>";
        assertEquals(input, HtmlSanitizer.sanitize(input));
    }

    @Test
    void removesIframeAndFormTags() {
        String input = "<p>Text</p><iframe src=\"evil.com\"></iframe><form><input></form>";
        String out = HtmlSanitizer.sanitize(input);
        assertFalse(out.contains("<iframe"));
        assertFalse(out.contains("<form"));
        assertFalse(out.contains("<input"));
    }
}
