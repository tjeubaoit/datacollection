package com.datacollection.core.collect;

import com.datacollection.common.config.Configuration;
import com.datacollection.common.utils.Pair;
import com.datacollection.common.utils.Strings;
import com.datacollection.core.platform.elastic.ElasticClientProvider;
import com.datacollection.core.platform.elastic.ElasticConfig;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Dùng để search dữ liệu công ty/tổ chức chuẩn (crawl từ các nguồn
 * tin cậy) từ tên công ty/tổ chức khai bảo bởi user trên Facebook/LinkedIn
 */
public class OrgSearcher {

    private Client client;
    private static final String index = "datacollection-org";
    private static final String type = "org";

    private static OrgSearcher ourInstance = new OrgSearcher();

    /**
     * @return Khởi tạo hoặc lấy về instance duy nhất
     */
    public static OrgSearcher getOurInstance() {
        return ourInstance;
    }

    private OrgSearcher() {
        ElasticConfig elasticConfig = new ElasticConfig(new Configuration());
        client = ElasticClientProvider.getOrCreate("profile2 transformer", elasticConfig);
    }

    /**
     * Tìm kiếm tên công ty/tổ chức chuẩn (crawl từ các nguồn
     * tin cậy) từ tên công ty/tổ chức không chính xác
     *
     * @param orgName Tên công ty/tổ chức cần tìm kiếm
     * @return List danh sách các công ty/tổ chức tìm được dưới dạng
     * Pair với giá trị thứ nhất là tên công ty/tổ chức, giá trị thứ
     * hai là điểm (nằm trong khoảng 0-1) cho biết độ chính xác dự đoán
     * của kết quả tìm kiếm
     */
    public Collection<Pair<String, String>> matchOrg(String orgName) {
        Set<Pair<String, String>> orgs = new HashSet<>();
        try {
            SearchResponse sr = client.prepareSearch(index)
                    .setTypes(type)
                    .setQuery(QueryBuilders.matchQuery("post.Title", normalizeOrgName(orgName)))
                    .setSize(3)
                    .execute().actionGet();

            float maxScore = 50;
            for (SearchHit hit : sr.getHits()) {
                String title = ((Map<String, String>) hit.getSource().get("post")).get("Title").toLowerCase();
                if (orgName.toLowerCase().equals(title)) return Collections.singleton(new Pair<>(title, "1"));
                orgs.add(new Pair<>(title, Strings.format("%3f", hit.score() / maxScore)));
            }
        } catch (ElasticsearchException ignored) {
        }
        return orgs;
    }

    private static String normalizeOrgName(String orgName) {
        String regex = "(tập đoàn|tap doan|công ty|cty|c.ty|cong ty" +
                "|dntn| dn |doanh nghiep|doanh nghiệp|tnhh|cổ phần|co phan" +
                "|university|group|department|company|school|corporation)";
        return orgName.toLowerCase().replaceAll(regex, " ");
    }

    public static void main(String[] args) {
        System.out.println(normalizeOrgName("công ty tnhh nhật anh"));
        System.out.println(normalizeOrgName("công ty nhật anh (tnhh)"));
        System.out.println(normalizeOrgName("dntn vận tải nhật anh"));
        System.out.println(normalizeOrgName("cty co phan vccorp"));
        System.out.println(normalizeOrgName("công ty cổ phần vccorp"));
        System.out.println(normalizeOrgName("dntn nhật anh"));

        OrgSearcher orgSearcher = new OrgSearcher();
        System.out.println(orgSearcher.matchOrg("công ty tnhh nhật anh"));
        System.out.println(orgSearcher.matchOrg("công ty nhật anh (tnhh)"));
        System.out.println(orgSearcher.matchOrg("dntn vận tải nhật anh"));
        System.out.println(orgSearcher.matchOrg("cty co phan vccorp"));
        System.out.println(orgSearcher.matchOrg("công ty cổ phần vccorp"));
        System.out.println(orgSearcher.matchOrg("dntn nhật anh"));
    }
}
