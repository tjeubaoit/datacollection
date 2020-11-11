package com.datacollection.collect.idgen;

import com.datacollection.common.config.Configurable;
import com.datacollection.common.config.Properties;
import com.datacollection.common.utils.Reflects;

import java.util.List;

/**
 * Vì các Collector có thể chạy phân tán và đồng thời trên nhiều máy. Sẽ xảy ra một
 * khả năng là một hoặc một số trusted entities chưa tồn tại trong đồ thị nhưng
 * được thêm vào đồ thị tại cùng một thời điểm bởi các Collector khác nhau, lúc đó
 * khả năng cao là chúng sẽ tạo ra 2 profile với ID khác nhau được generate bởi
 * Collector. Để tránh tình trạng này thì quá trình generate ID được kiểm soát bởi
 * một RemoteIdGenerator để bảo đảm rằng sẽ chỉ tạo ra một ID duy nhất.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface RemoteIdGenerator extends Configurable {

    /**
     * @param seeds  Danh sách các giá trị dùng làm seed. Thông thường Collector sẽ gửi
     *               list seed là các trusted entities dùng để generate một ID mới. Nếu
     *               có bất kì seed nào đã được sinh một ID (bởi một Collector khác) thì
     *               sẽ trả về ID này thay vì ID mới (defVal)
     * @param defVal Giá trị mặc định trả về nếu không tìm được ID nào có sẵn cho seeds
     * @return ID đang tồn tại và đã được gán với ít nhất một seed trong list hoặc
     * defVal nếu không có ID nào gán với các phần tử trong seeds.
     */
    long generate(List<String> seeds, long defVal);

    /**
     * Tạo ra một thể hiện của IdGenerator từ config
     *
     * @param p chứa thuộc tính dùng để khởi tạo RemoteIdGenerator
     * @return object implement RemoteIdGenerator
     */
    static RemoteIdGenerator create(Properties p) {
        RemoteIdGenerator ins = Reflects.newInstance(p.getProperty("remote.idgen.class"));
        ins.configure(p);
        return ins;
    }
}
