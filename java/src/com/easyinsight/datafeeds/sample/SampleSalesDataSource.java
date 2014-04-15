package com.easyinsight.datafeeds.sample;

import com.amazonaws.services.redshift.AmazonRedshiftClient;
import com.csvreader.CsvWriter;
import com.easyinsight.analysis.*;
import com.easyinsight.core.DateValue;
import com.easyinsight.core.Key;
import com.easyinsight.core.Value;
import com.easyinsight.database.EIConnection;
import com.easyinsight.datafeeds.FeedDefinition;
import com.easyinsight.datafeeds.FeedType;
import com.easyinsight.datafeeds.ServerDataSourceDefinition;
import com.easyinsight.dataset.DataSet;
import com.easyinsight.storage.IDataStorage;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * User: jamesboe
 * Date: 6/8/11
 * Time: 10:03 PM
 */
public class SampleSalesDataSource extends ServerDataSourceDefinition {
    public static final String CUSTOMER = "Sales Customer";
    public static final String PRODUCT = "Sales Product";
    public static final String QUANTITY = "Quantity";
    public static final String ORDER_DATE = "Order Date";

    private static final String[] customerNames = { "Bross Design", "Crestone Engineering", "Uncompahgre Systems",
            "Sneffels Technology", "Sunlight Architecture", "Pyramid Research", "Little Bear Consulting", "Torreys Inc" };

    private static final String[] productNames = { "Widget1", "Widget2", "Widget3", "Widget4", "Widget5"};

    public SampleSalesDataSource() {
        setFeedName("Sales");
    }

    @Override
    public FeedType getFeedType() {
        return FeedType.SAMPLE_SALES;
    }

    @NotNull
    @Override
    protected List<String> getKeys(FeedDefinition parentDefinition) {
        return Arrays.asList(CUSTOMER, PRODUCT, QUANTITY, ORDER_DATE);
    }

    public List<AnalysisItem> createAnalysisItems(Map<String, Key> keys, Connection conn, FeedDefinition parentDefinition) {
        List<AnalysisItem> items = new ArrayList<AnalysisItem>();
        AnalysisDimension customer = new AnalysisDimension(keys.get(CUSTOMER));
        customer.setHidden(true);
        items.add(customer);
        AnalysisDimension product = new AnalysisDimension(keys.get(PRODUCT));
        product.setHidden(true);
        items.add(product);
        items.add(new AnalysisMeasure(keys.get(QUANTITY), AggregationTypes.SUM));
        items.add(new AnalysisDateDimension(keys.get(ORDER_DATE), true, AnalysisDateDimension.DAY_LEVEL));
        return items;
    }

    @Override
    public DataSet getDataSet(Map<String, Key> keys, Date now, FeedDefinition parentDefinition, IDataStorage IDataStorage, EIConnection conn, String callDataID, Date lastRefreshDate) throws ReportException {
        DataSet dataSet = new DataSet();
        long startTime = System.currentTimeMillis();
        try {
            File file = new File(getDataFeedID() + ".csv");
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos, 512);
            CsvWriter csvWriter = new CsvWriter(bos, ',', Charset.forName("UTF-8"));
            String[] header = new String[keys.size()];
            int j = 0;
            for (Key key : keys.values()) {
                header[j++] = "k" + key.getKeyID();
            }
            csvWriter.writeRecord(header);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS");
            for (int i = 0; i < 1000000; i++) {
                IRow row = dataSet.createRow();

                row.addValue(keys.get(CUSTOMER), customerNames[i % customerNames.length]);
                row.addValue(keys.get(PRODUCT), productNames[(int) (Math.random() * 5)]);
                row.addValue(keys.get(QUANTITY), (int) (Math.random() * 3));
                Calendar cal = Calendar.getInstance();
                //cal.add(Calendar.YEAR, -1);
                cal.add(Calendar.DAY_OF_YEAR, - (int) (Math.random() * 365));
                row.addValue(keys.get(ORDER_DATE), cal.getTime());
                String[] rowValues = new String[keys.size()];
                j = 0;
                for (Key key : keys.values()) {
                    Value value = row.getValue(key);
                    if (value.type() == Value.DATE) {
                        rowValues[j++] = sdf.format(((DateValue) value).getDate());
                    } else {
                        rowValues[j++] = String.valueOf(value.toString());
                    }
                }
                csvWriter.writeRecord(rowValues);

                if (i % 1000 == 0) {
                    //IDataStorage.insertData(dataSet);
                    csvWriter.flush();
                    dataSet = new DataSet();
                }
            }
            csvWriter.flush();
            csvWriter.close();
            fos.close();



            //IDataStorage.insertData(dataSet);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Elapsed time = " + (endTime - startTime));
        return null;
    }
}
