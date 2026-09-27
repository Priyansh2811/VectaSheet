package com.vectasheet.util;

import java.util.regex.Pattern;

/**
 * A minimal allowlist-style sanitizer for the small set of tags the Docs
 * editor's toolbar can produce (bold/italic/headings/lists/links/etc).
 *
 * This is NOT a substitute for a real HTML sanitization library (e.g. OWASP
 * Java HTML Sanitizer) — it's a pragmatic stopgap: it strips <script>/<style>
 * blocks, event-handler attributes (onclick, onerror, ...), and javascript:/
 * data: URLs, which covers the common XSS vectors for editor-generated HTML.
 * Swap this for a proper sanitizer library before handling untrusted HTML at
 * production scale or from sources other than this app's own editor.
 */
public final class HtmlSanitizer {

    private static final Pattern SCRIPT_TAG = Pattern.compile("(?is)<script.*?>.*?</script>");
    private static final Pattern STYLE_TAG = Pattern.compile("(?is)<style.*?>.*?</style>");
    private static final Pattern EVENT_ATTR = Pattern.compile("(?i)\\s+on\\w+\\s*=\\s*(\"[^\"]*\"|'[^']*'|[^\\s>]+)");
    private static final Pattern JS_URL = Pattern.compile("(?i)(href|src)\\s*=\\s*(\"|')\\s*javascript:[^\"']*(\"|')");
    private static final Pattern DATA_URL_NON_IMAGE = Pattern.compile("(?i)(href|src)\\s*=\\s*(\"|')\\s*data:(?!image/)[^\"']*(\"|')");
    private static final Pattern DISALLOWED_TAGS = Pattern.compile(
            "(?is)</?(iframe|object|embed|form|input|button|textarea|svg|math|link|meta|base)[^>]*>");

    private HtmlSanitizer() {}

    public static String sanitize(String html) {
        if (html == null) return null;
        String out = html;
        out = SCRIPT_TAG.matcher(out).replaceAll("");
        out = STYLE_TAG.matcher(out).replaceAll("");
        out = DISALLOWED_TAGS.matcher(out).replaceAll("");
        out = EVENT_ATTR.matcher(out).replaceAll("");
        out = JS_URL.matcher(out).replaceAll("$1=$2#$3");
        out = DATA_URL_NON_IMAGE.matcher(out).replaceAll("$1=$2#$3");
        return out;
    }
}
