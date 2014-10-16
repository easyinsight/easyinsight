package com.easyinsight.storage;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;

import com.easyinsight.analysis.AnalysisDateDimension;
import com.easyinsight.analysis.AnalysisItem;
import com.easyinsight.analysis.IRow;
import com.easyinsight.analysis.InsightRequestMetadata;
import com.easyinsight.config.ConfigLoader;
import com.easyinsight.core.DateValue;
import com.easyinsight.core.Key;
import com.easyinsight.core.NumericValue;
import com.easyinsight.core.Value;
import com.easyinsight.database.Database;
import com.easyinsight.database.EIConnection;
import com.easyinsight.dataset.DataSet;
import com.easyinsight.logging.LogClass;
import org.apache.commons.lang3.text.translate.AggregateTranslator;
import org.apache.commons.lang3.text.translate.EntityArrays;
import org.apache.commons.lang3.text.translate.LookupTranslator;

import java.io.*;
import java.nio.charset.Charset;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

/**
 * User: jamesboe
 * Date: 4/10/14
 * Time: 1:56 PM
 */
public class AltPostgresStorageDialect implements IStorageDialect {

    private String tableName;
    private Map<Key, KeyMetadata> keys;
    private Key distKey;

    public AltPostgresStorageDialect(String tableName, Map<Key, KeyMetadata> keys) {
        this.tableName = tableName;
        this.keys = keys;
    }

    public AltPostgresStorageDialect(String tableName, Map<Key, KeyMetadata> keys, Key distKey) {
        this.tableName = tableName;
        this.keys = keys;
        this.distKey = distKey;
    }



    private String fileName;

    //private CsvWriter csvWriter;
    private List<File> files = new ArrayList<File>();
    private File file;
    private FileOutputStream fos;
    private int rows = 0;



    public void updateData(DataSet dataSet, List<IDataTransform> transforms, EIConnection coreDBConn, Database storageDatabase, DateDimCache dateDimCache) throws Exception {
        insertData(dataSet, transforms, coreDBConn, storageDatabase, dateDimCache);
    }

    private String escape(String string) {
        //String ret = string;
        String ret = new AggregateTranslator(new LookupTranslator(new String[][] {
                {"\"", "\\\""},
                {"\\", "\\\\"},
        }),new LookupTranslator(EntityArrays.JAVA_CTRL_CHARS_ESCAPE())).translate(string);
        if (ret.contains("|")) {
            ret = ret.replace("|", "\\|");
        }
        if (ret.contains("'")) {
            ret = ret.replace("'", "\\'");
        }
        return ret;
    }

    public void insertData(DataSet dataSet, List<IDataTransform> transforms, EIConnection coreDBConn, Database storageDatabase, DateDimCache dateDimCache) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        int i = 0;
        StringBuilder sb = new StringBuilder();
        for (IRow row : dataSet.getRows()) {
            rows++;
            i++;
            String[] rowValues = new String[keys.size()];
            int j = 0;
            for (Map.Entry<Key, KeyMetadata> entry : keys.entrySet()) {
                Key key = entry.getKey();
                KeyMetadata keyMetadata = entry.getValue();
                Value value = row.getValue(key);
                if (keyMetadata.getType() == Value.DATE) {
                    if (value.type() != Value.DATE) {
                        AnalysisItem analysisItem = keyMetadata.getAnalysisItem();
                        AnalysisDateDimension analysisDateDimension = (AnalysisDateDimension) analysisItem;
                        int prevLevel = analysisDateDimension.getDateLevel();
                        analysisDateDimension.setDateLevel(AnalysisDateDimension.DAY_LEVEL);
                        Calendar calendar = Calendar.getInstance();
                        Value transformedValue = analysisItem.transformValue(value, new InsightRequestMetadata(), false, calendar);
                        analysisDateDimension.setDateLevel(prevLevel);
                        if (transformedValue.type() == Value.EMPTY) {
                            rowValues[j++] = "";
                        } else {
                            DateValue dateValue = (DateValue) transformedValue;
                            String string = sdf.format(dateValue.getDate());
                            rowValues[j++] = escape(string);
                        }
                    } else {
                        DateValue dateValue = (DateValue) value;
                        if (dateValue.getDate() == null) {
                            rowValues[j++] = "";
                        } else {
                            String string = sdf.format(dateValue.getDate());
                            rowValues[j++] = escape(string);
                        }
                    }
                } else if (keyMetadata.getType() == Value.NUMBER) {
                    Double num = null;
                    if (value.type() == Value.STRING || value.type() == Value.TEXT) {
                        num = NumericValue.produceDoubleValue(value.toString());
                    } else if (value.type() == Value.NUMBER) {
                        NumericValue numericValue = (NumericValue) value;
                        num = numericValue.toDouble();
                    }
                    if (num == null) {
                        rowValues[j++] = "";
                    } else {
                        String string = String.valueOf(num);
                        rowValues[j++] = escape(string);
                    }
                } else if (value.type() == Value.DATE) {
                    DateValue dateValue = (DateValue) value;
                    if (dateValue.getDate() == null) {
                        rowValues[j++] = "";
                    } else {
                        String string = sdf.format(dateValue.getDate());
                        rowValues[j++] = escape(string);
                    }
                } else if (value.type() == Value.EMPTY) {
                    rowValues[j++] = "";
                } else if (value.type() == Value.NUMBER) {
                    Double num = null;
                    if (value.type() == Value.STRING || value.type() == Value.TEXT) {
                        num = NumericValue.produceDoubleValue(value.toString());
                    } else if (value.type() == Value.NUMBER) {
                        NumericValue numericValue = (NumericValue) value;
                        num = numericValue.toDouble();
                    }
                    if (num == null) {
                        rowValues[j++] = "";
                    } else {
                        String string = String.valueOf(num);
                        rowValues[j++] = escape(string);
                    }
                } else if (value.type() == Value.STRING) {
                    String string;
                    if (value.toString() == null) {
                        string = "";
                    } else {
                        string = value.toString();
                    }
                    if (string.length() > 100) {
                        string = string.substring(0, 100);
                    }
                    String escaped = escape(string);

                    rowValues[j++] = escaped;
                } else {
                    String string = value.toString();
                    if (string.length() > 253) {
                        System.out.println("*** " + string);
                    }
                    rowValues[j++] = escape(string);
                }
            }
            for (String string : rowValues) {
                sb.append(string).append("|");
            }
            sb.append("\n");
            //csvWriter.writeRecord(rowValues);
        }
        bos.write(sb.toString().getBytes(Charset.forName("UTF-8")));
        bos.flush();
        //csvWriter.flush();
        if (rows > 10000000) {
            rows = 0;
            bos.flush();
            bos.close();
            //csvWriter.close();
            fos.close();
            files.add(file);
            System.out.println("starting new file of " + tableName + files.size() + ".csv");
            file = new File(ConfigLoader.instance().getRedshiftCSVPath() + File.separator + tableName + files.size() + ".csv");
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos, 512);
            //csvWriter = new CsvWriter(bos, '|', Charset.forName("UTF-8"));
        }
    }

    public void commit() {
        try {
            bos.flush();
            bos.close();
            fos.close();

            files.add(this.file);

            AmazonS3 s3 = new AmazonS3Client(new BasicAWSCredentials("0AWCBQ78TJR8QCY8ABG2", "bTUPJqHHeC15+g59BQP8ackadCZj/TsSucNwPwuI"));
            String bucketName = "refresh" + tableName;
            s3.createBucket(bucketName);

            for (File file : files) {

                ByteArrayOutputStream archiveStream = new ByteArrayOutputStream();

                GZIPOutputStream zos = new GZIPOutputStream(archiveStream);
                BufferedOutputStream bufOS = new BufferedOutputStream(zos, 1024);
                byte[] buffer = new byte[1024];
                InputStream bfis = new FileInputStream(file);
                int nBytes;
                while ((nBytes = bfis.read(buffer)) != -1) {
                    bufOS.write(buffer, 0, nBytes);
                }
                bufOS.flush();
                zos.flush();

                bfis.close();

                archiveStream.flush();

                zos.close();

                archiveStream.close();

                byte[] bytes = archiveStream.toByteArray();
                ByteArrayInputStream stream = new ByteArrayInputStream(bytes);

                ObjectMetadata objectMetadata = new ObjectMetadata();
                objectMetadata.setContentLength(bytes.length);
                s3.putObject(new PutObjectRequest(bucketName, fileName, stream, objectMetadata));
                System.out.println("saved off " + fileName + " for debug");
                boolean success = file.delete();
                if (!success) {
                    LogClass.error("Could not delete " + fileName);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String defineTableSQL(boolean hugeTable) {
        return defineTableSQL(tableName, false);
    }

    private String defineTableSQL(String tableName, boolean hugeTable) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("CREATE TABLE ");
        sqlBuilder.append(tableName);
        sqlBuilder.append("( ");
        for (KeyMetadata keyMetadata : keys.values()) {
            String columnSQL = getColumnDefinitionSQL(keyMetadata.getKey(), keyMetadata.getType(), hugeTable);
            sqlBuilder.append(columnSQL);
            sqlBuilder.append(",");
        }
        String primaryKey = tableName + "_ID";
        sqlBuilder.append(primaryKey);
        sqlBuilder.append(" int identity,");
        if (sqlBuilder.charAt(sqlBuilder.length() - 1) == ',') sqlBuilder.deleteCharAt(sqlBuilder.length() - 1);
        if (distKey == null) {
            sqlBuilder.append(" )");
        } else {
            sqlBuilder.append(" ) DISTSTYLE KEY DISTKEY(");
            sqlBuilder.append("k").append(distKey.getKeyID() + ")");
        }
        //sqlBuilder.append(" ) CHARSET=utf8");
        return sqlBuilder.toString();
    }

    public String getColumnDefinitionSQL(Key key, int type, boolean hugeTable) {
        String column;
        if (type == Value.DATE) {
            column = "k" + key.getKeyID() + " TIMESTAMP, datedim_" + key.getKeyID() + "_id integer";
        } else if (type == Value.NUMBER) {
            column = "k" + key.getKeyID() + " DECIMAL(18, 4)";
        } else if (type == Value.TEXT) {
            column = "k" + key.getKeyID() + " TEXT";
        } else {
            if (hugeTable) {
                column = "k" + key.getKeyID() + " TEXT";
            } else {
                column = "k" + key.getKeyID() + " VARCHAR(255)";
            }
        }
        return column;
    }

    public void createTempTable(String sql, Database database, boolean insert) throws SQLException {
        try {
            if (!insert) {
                // create a load table
                EIConnection storageConn = database.getConnection();
                try {
                    String tableSQL = defineTableSQL("load" + tableName, true);
                    PreparedStatement createSQL = storageConn.prepareStatement(tableSQL);
                    createSQL.execute();
                    createSQL.close();
                } finally {
                    Database.closeConnection(storageConn);
                }
            }

            fileName = tableName + ".csv";

            file = new File(ConfigLoader.instance().getRedshiftCSVPath() + File.separator + fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos, 512);
            //csvWriter = new CsvWriter(bos, '|', Charset.forName("UTF-8"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private BufferedOutputStream bos;



    public String defineTempInsertTable() { return ""; }

    public String defineTempUpdateTable() {
        return "";
    }
}
