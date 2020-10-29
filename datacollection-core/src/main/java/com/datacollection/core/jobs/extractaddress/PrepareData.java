package com.datacollection.core.jobs.extractaddress;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.datacollection.common.config.Configuration;
import com.datacollection.common.config.Properties;
import com.datacollection.core.platform.elastic.ElasticBulkInsert;
import com.datacollection.core.platform.elastic.ElasticClientProvider;
import com.datacollection.core.platform.elastic.ElasticConfig;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * TODO: Class description here.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public class PrepareData {

    private static final String ES_INDEX = "datacollection-hcvn";

    public static void main(String[] args) throws IOException {
//        Map<String, Province> provinces = convertData();
//        insertToEs(provinces.values());

        List<Province> provinces = loadProvinces();
        System.out.println(provinces);
    }

    public static List<Province> loadProvinces() throws IOException {
        Client client = ElasticClientProvider.getDefault(new ElasticConfig(new Configuration()));
        SearchResponse response = client.prepareSearch(ES_INDEX)
                .setQuery(QueryBuilders.matchAllQuery())
                .setSize(100)
                .execute().actionGet();
        ObjectMapper om = new ObjectMapper();
        List<Province> provinces = new LinkedList<>();

        for (SearchHit hit : response.getHits().getHits()) {
            Province p = om.readValue(hit.getSourceAsString(), Province.class);
            provinces.add(p);
        }

        return provinces;
    }

    public static void insertToEs(Collection<Province> provinces) throws IOException {
        ObjectMapper om = new ObjectMapper();
        System.out.println(om.writeValueAsString(provinces));

        Properties props = new Configuration().toSubProperties("address_extract");
        ElasticBulkInsert elasticBulkInsert = new ElasticBulkInsert(props);
        int i = 0;
        for (Province p : provinces) {
            elasticBulkInsert.addRequest("provinces", String.valueOf(++i), p.toMap());
        }
        elasticBulkInsert.submitBulk();
        elasticBulkInsert.client().admin().indices().prepareRefresh(ES_INDEX);
    }

    public static Map<String, Province> convertData() throws IOException {
        Map<String, Province> provinces = new LinkedHashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader("/home/anhtn/Desktop/hcvn.csv"))) {
            String line;
            String lastProvinceName = null;
            Province province = null;

            while ((line = reader.readLine()) != null) {
                String[] split = line.split("\t");
                String provinceName = split[0].toLowerCase()
                        .replaceAll("(thành phố|tỉnh) ", "")
                        .replace(' ', '_');
                if (!provinceName.equals(lastProvinceName)) {
                    lastProvinceName = provinceName;
                    province = new Province();
                    province.name = provinceName;
                }

                // find district
                String districtName = split[2].toLowerCase()
                        .replaceAll("(quận|huyện|thành phố|thị xã) ", "")
                        .replace(' ', '_');
                District district = findByName(districtName, province.districts);
                if (district == null) {
                    district = new District();
                    district.name = districtName;
                    province.districts.add(district);
                }

                // add commune to district
                String commune = split[4].toLowerCase()
                        .replaceAll("(phường|xã|thị trấn) ", "")
                        .replace(' ', '_');
                district.communes.add(commune);

                provinces.put(provinceName, province);
            }
        }

        return provinces;
    }

    public static District findByName(String target, List<District> districts) {
        for (District d : districts) {
            if (d.name.equals(target)) return d;
        }
        return null;
    }
}
