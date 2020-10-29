package com.datacollection.core.transform;

import com.datacollection.core.collect.model.GraphModel;
import com.datacollection.core.extract.model.GenericModel;

/**
 * Transform và chuẩn hóa dữ liệu từ dạng generic về dạng phù hợp
 * để lưu trữ trong đồ thị. Với mỗi loại dữ liệu khác nhau sẽ cần
 * một Transformer khác nhau bằng cách implement lại interface này.
 *
 * @author <a href="https://github.com/tjeubaoit">tjeubaoit</a>
 */
public interface DataTransformer {

    /**
     * Tạo ra một đối tượng DataTransformer dựa vào type. Với mỗi
     * loại dữ liệu mới và một DataTransformer mới cần được update
     * lại vào hàm này.
     *
     * @param type type của kiểu dữ liệu cần transform
     * @return đối tượng DataTransformer tương ứng dùng để xử lý kiểu dữ liệu
     * được xác định bởi type.
     */
    static DataTransformer create(String type) {
        switch (type) {
            case GenericModel.TYPE_FB_FANPAGE_POST:
                return new FbPagePostTransformer();
            case GenericModel.TYPE_FB_FANPAGE_COMMENT:
                return new FbPageCmtTransformer();
            case GenericModel.TYPE_FB_GROUP_POST:
                return new FbGroupPostTransformer();
            case GenericModel.TYPE_FB_GROUP_COMMENT:
                return new FbGroupCmtTransformer();
            case GenericModel.TYPE_FB_PROFILE_NEW:
                return new FbProfile2Transformer();

            case GenericModel.TYPE_FORUM_ARTICLE:
                return new ForumArtTransformer();
            case GenericModel.TYPE_FORUM_COMMENT:
                return new ForumCmtTransformer();

            case GenericModel.TYPE_ZAMBA:
                return new ZambaTransformer();

            case GenericModel.TYPE_ORG:
                return new OrgTransformer();

            case GenericModel.TYPE_DMP:
                return new DmpTransformer();

            case GenericModel.TYPE_ECOMMERCE:
                return new EcommerceTransformer();
            case GenericModel.TYPE_API_VIETID:
                return new VietIdTransformer();

            case GenericModel.TYPE_API_RB:
                return new RbTransformer();

            case GenericModel.TYPE_ZAMBA_CHATBOT:
                return new ZambaChatbotTransformer();

            case GenericModel.TYPE_LINKEDIN:
                return new LinkedInTransformer();

            case GenericModel.TYPE_EXCEL:
                return new ExcelTransformer();

            case GenericModel.TYPE_ADCHATBOT:
                return new ChatBotTransformer();

            default:
                throw new IllegalArgumentException("Invalid data type");
        }
    }

    /**
     * Transform object GenericModel thành object GraphModel
     *
     * @param generic GenericModel object
     * @return GraphModel object
     */
    GraphModel transform(GenericModel generic);
}
