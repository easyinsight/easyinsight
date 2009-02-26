package com.easyinsight.solutions.teamcity;

import com.easyinsight.solutions.teamcity.webservice.*;

import java.io.FileReader;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.ArrayList;
import java.util.List;

/**
 * User: James Boe
 * Date: Feb 25, 2009
 * Time: 4:01:01 PM
 */
public class CoverageDataParse {
    public void parseData(String path, UncheckedPublishService service) {
        try {
            File file = new File(path);
            FileReader fileReader = new FileReader(file);
            char[] buf = new char[(int) file.length()];
            fileReader.read(buf);
            String contents = new String(buf);
            String[] lines = contents.split("\r\n");
            String topLine = lines[5];

            String packageLine = lines[9];
            String classLine = lines[10];
            String methodLine = lines[11];
            String executableFiles = lines[12];
            String executableLines = lines[13];
            List<Row> rows = new ArrayList<Row>();
            for (int i = 18; i < lines.length; i++) {
                String line = lines[i];
                System.out.println(line);
                if (line.charAt(0) != '-') {
                    int parenClassStart = line.indexOf("(", 0);
                    int classSlash = line.indexOf("/", parenClassStart);
                    double coveredClassCount = Double.parseDouble(line.substring(parenClassStart + 1, classSlash));
                    NumberPair coveredClassPair = new NumberPair();
                    coveredClassPair.setKey("Covered Class Count");
                    coveredClassPair.setValue(coveredClassCount);
                    int parentClassEnd = line.indexOf(")", classSlash);
                    double totalClassCount = Double.parseDouble(line.substring(classSlash + 1, parentClassEnd));
                    NumberPair totalClassPair = new NumberPair();
                    totalClassPair.setKey("Total Class Count");
                    totalClassPair.setValue(totalClassCount);

                    int parenMethodStart = line.indexOf("(", parentClassEnd);
                    int methodSlash = line.indexOf("/", parenMethodStart);
                    double coveredMethodCount = Double.parseDouble(line.substring(parenMethodStart + 1, methodSlash));
                    NumberPair coveredMethodPair = new NumberPair();
                    coveredMethodPair.setKey("Covered Method Count");
                    coveredMethodPair.setValue(coveredMethodCount);
                    int parentMethodEnd = line.indexOf(")", methodSlash);
                    double totalMethodCount = Double.parseDouble(line.substring(methodSlash + 1, parentMethodEnd));
                    NumberPair totalMethodPair = new NumberPair();
                    totalMethodPair.setKey("Total Method Count");
                    totalMethodPair.setValue(totalMethodCount);

                    int parentBlockStart = line.indexOf("(", parentMethodEnd);
                    int blockSlash = line.indexOf("/", parentBlockStart);
                    double coveredBlockCount = Double.parseDouble(line.substring(parentBlockStart + 1, blockSlash));
                    NumberPair coveredBlockPair = new NumberPair();
                    coveredBlockPair.setKey("Covered Block Count");
                    coveredBlockPair.setValue(coveredBlockCount);
                    int parenBlockEnd = line.indexOf(")", blockSlash);
                    double totalBlockCount = Double.parseDouble(line.substring(blockSlash + 1, parenBlockEnd));
                    NumberPair totalBlockPair = new NumberPair();
                    totalBlockPair.setKey("Total Block Count");
                    totalBlockPair.setValue(totalBlockCount);

                    int lineBlockStart = line.indexOf("(", parenBlockEnd);
                    int lineSlash = line.indexOf("/", lineBlockStart);
                    double coveredLineCount = Double.parseDouble(line.substring(lineBlockStart + 1, lineSlash));
                    NumberPair coveredLinePair = new NumberPair();
                    coveredLinePair.setKey("Covered Line Count");
                    coveredLinePair.setValue(coveredLineCount);
                    int parenLineEnd = line.indexOf(")", lineSlash);
                    double totalLineCount = Double.parseDouble(line.substring(lineSlash + 1, parenLineEnd));
                    NumberPair totalLinePair = new NumberPair();
                    totalLinePair.setKey("Total Line Count");
                    totalLinePair.setValue(totalLineCount);

                    String name = line.substring(parenLineEnd + 2).trim();
                    StringPair namePair = new StringPair();
                    namePair.setKey("Package Name");
                    namePair.setValue(name);
                    Calendar cal = Calendar.getInstance();
                    Row row = new Row();
                    DatePair datePair = new DatePair();
                    datePair.setKey("Date of Run");
                    datePair.setValue(cal);
                    row.setStringPairs(new StringPair[] { namePair } );
                    row.setNumberPairs(new NumberPair[] { coveredClassPair, totalClassPair, coveredMethodPair, totalMethodPair,
                        coveredBlockPair, totalBlockPair, coveredLinePair, totalLinePair} );
                    row.setDatePairs(new DatePair[] { datePair });
                    rows.add(row);
                }
                Row[] rowArray = new Row[rows.size()];
                rows.toArray(rowArray);
                service.addRows("teamcitycoverage", rowArray);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
