package com.hcl.utils;

import org.testng.annotations.DataProvider;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.*;
import java.lang.reflect.Method;
import java.util.*;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;

/**
 * DynamicDataProvider is a unified TestNG {@link DataProvider} implementation
 * designed to support multiple external test data formats.
 * <p>
 * Supported data sources:
 * <ul>
 *   <li>Excel (.xlsx)</li>
 *   <li>CSV (.csv)</li>
 *   <li>JSON (.json)</li>
 *   <li>XML (.xml)</li>
 * </ul>
 *
 * Each test method must be annotated with {@link TestDataFile}, which specifies
 * the data file and optional sheet name to be used.
 * <p>
 * The provider returns test data as {@code Object[][]}, where each row contains
 * a single {@code Map<String, String>} representing one test iteration.
 */
public class DynamicDataProvider {

    /**
     * Central TestNG DataProvider entry point.
     * <p>
     * This method reads the {@link TestDataFile} annotation applied on
     * the calling test method and dynamically loads test data from
     * the specified file and sheet.
     *
     * @param method current executing test method (reflection)
     * @return Object[][] containing test data maps
     */
    @DataProvider(name = "testData")
    public static Object[][] getData(Method method) {
        TestDataFile annotation = method.getAnnotation(TestDataFile.class);
        if (annotation == null) {
            throw new RuntimeException("⚠️ Missing @TestDataFile annotation on test: " + method.getName());
        }

        String fileName = annotation.file();
        String sheetName = annotation.sheet();

        return loadData(fileName, sheetName);
    }

    /**
     * Loads test data based on file extension and normalizes the content.
     * <p>
     * After reading raw data, all keys and values are trimmed to avoid
     * issues caused by trailing or leading spaces across different formats.
     *
     * @param fileName test data file path
     * @param sheetName optional sheet name (Excel only)
     * @return Object[][] ready for TestNG consumption
     */
    public static Object[][] loadData(String fileName, String sheetName) {
        try {
            List<Map<String, String>> rawData;

            if (fileName.endsWith(".xlsx")) rawData = readExcel(fileName, sheetName);
            else if (fileName.endsWith(".csv")) rawData = readCsv(fileName, sheetName);
            else if (fileName.endsWith(".json")) rawData = readJson(fileName);
            else if (fileName.endsWith(".xml")) rawData = readXml(fileName);
            else throw new RuntimeException("Unsupported file type: " + fileName);

            // 🔥 Unified TRIM – normalize keys & values for ALL formats
            List<Map<String, String>> cleanedData = new ArrayList<>();
            for (Map<String, String> original : rawData) {
                Map<String, String> trimmed = new LinkedHashMap<>();
                for (Map.Entry<String, String> entry : original.entrySet()) {
                    String key = entry.getKey() != null ? entry.getKey().trim() : null;
                    String val = entry.getValue() != null ? entry.getValue().trim() : null;
                    trimmed.put(key, val);
                }
                cleanedData.add(trimmed);
            }

            return convertToArray(cleanedData);

        } catch (Exception e) {
            throw new RuntimeException("Failed to load test data from: " + fileName, e);
        }
    }

    /**
     * Reads test data from an Excel (.xlsx) file using Apache POI.
     * <p>
     * The first row is treated as the header row and subsequent rows
     * represent individual test data records.
     *
     * @param filePath Excel file path
     * @param sheetName optional sheet name (defaults to first sheet)
     * @return List of key-value maps for each row
     */
    private static List<Map<String, String>> readExcel(String filePath, String sheetName) throws IOException {
        List<Map<String, String>> data = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(filePath); Workbook wb = new XSSFWorkbook(fis)) {
            Sheet sheet = (sheetName == null || sheetName.isEmpty()) ? wb.getSheetAt(0) : wb.getSheet(sheetName);
            Row headerRow = sheet.getRow(0);
            int colCount = headerRow.getPhysicalNumberOfCells();

            for (int r = 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;
                Map<String, String> map = new LinkedHashMap<>();
                for (int c = 0; c < colCount; c++) {
                    String key = headerRow.getCell(c).getStringCellValue();
                    String val = row.getCell(c) == null ? "" : row.getCell(c).toString();
                    map.put(key, val);
                }
                data.add(map);
            }
        }
        return data;
    }

    /**
     * Reads test data from a CSV file using OpenCSV.
     * <p>
     * The first row is treated as column headers.
     * Each subsequent row is mapped to header-value pairs.
     *
     * @param filePath CSV file path
     * @param sheetName unused (kept for interface consistency)
     * @return List of key-value maps
     */
    private static List<Map<String, String>> readCsv(String filePath, String sheetName) throws IOException, CsvException {
        List<Map<String, String>> data = new ArrayList<>();
        try (CSVReader reader = new CSVReader(new FileReader(filePath))) {
            List<String[]> rows = reader.readAll();
            if (rows.isEmpty()) return data;

            String[] headers = rows.get(0);

            // Trim all headers once
            for (int i = 0; i < headers.length; i++) {
                headers[i] = headers[i] != null ? headers[i].trim() : "";
            }

            for (int i = 1; i < rows.size(); i++) {
                String[] row = rows.get(i);
                Map<String, String> map = new LinkedHashMap<>();
                for (int j = 0; j < headers.length; j++) {
                    String value = j < row.length ? row[j] : "";
                    map.put(headers[j], value != null ? value.trim() : "");
                }
                data.add(map);
            }
        }
        return data;
    }

    /**
     * Reads test data from a JSON file.
     * <p>
     * The JSON file must contain an array of objects,
     * where each object represents one test iteration.
     *
     * @param filePath JSON file path
     * @return List of key-value maps
     */
    private static List<Map<String, String>> readJson(String filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new File(filePath), List.class);
    }

    /**
     * Reads test data from an XML file.
     * <p>
     * Each child element under the root node is treated as a test case,
     * and its sub-elements are mapped as key-value pairs.
     *
     * @param filePath XML file path
     * @return List of key-value maps
     */
    private static List<Map<String, String>> readXml(String filePath) throws Exception {
        List<Map<String, String>> data = new ArrayList<>();
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse(new File(filePath));
        NodeList testNodes = doc.getDocumentElement().getChildNodes();

        for (int i = 0; i < testNodes.getLength(); i++) {
            Node node = testNodes.item(i);
            if (node.getNodeType() != Node.ELEMENT_NODE) continue;

            Map<String, String> map = new LinkedHashMap<>();
            NodeList children = node.getChildNodes();
            for (int j = 0; j < children.getLength(); j++) {
                Node child = children.item(j);
                if (child.getNodeType() != Node.ELEMENT_NODE) continue;
                map.put(child.getNodeName(), child.getTextContent());
            }
            data.add(map);
        }
        return data;
    }

    /**
     * Converts a list of maps into a TestNG-compatible Object array.
     * <p>
     * Each test iteration receives exactly one Map argument.
     *
     * @param data normalized test data
     * @return Object[][] for DataProvider
     */
    private static Object[][] convertToArray(List<Map<String, String>> data) {
        Object[][] arr = new Object[data.size()][1];
        for (int i = 0; i < data.size(); i++) {
            arr[i][0] = data.get(i);
        }
        return arr;
    }
}
