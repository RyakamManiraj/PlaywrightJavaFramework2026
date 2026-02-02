package com.hcl.utils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation to specify test data file for a test method.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface TestDataFile {
    String file();        // json, xml, csv, xlsx
    String sheet() default ""; // optional for Excel
}
