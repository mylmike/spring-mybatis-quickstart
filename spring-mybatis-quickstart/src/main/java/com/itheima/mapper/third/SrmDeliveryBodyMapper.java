package com.itheima.mapper.third;

import com.itheima.pojo.SrmDeliveryBody;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * MySQL srm_delivery_body 表 Mapper
 */
@Mapper
public interface SrmDeliveryBodyMapper {

    /**
     * 按条件查询送货单（联表 srm_delivery_head 取供应商/送货日期）
     * 参数说明（均为可选）：
     *   receiptQty        收货数量条件，未传时默认 receipt_qty = '0'（只查未收货）
     *   czf               收货数量操作符（已由 Controller 标准化）：gt/lt/ge/le/eq，非法值回退为 eq
     *   status            单头状态条件，未传时默认 status 非 'X'/'4'/'1'
     *   deliveryDateStart 送货日期起始（含），未传时不加下限
     *   deliveryDateEnd   送货日期截止（含），未传时不加上限
     */
    @Select("<script>" +
            "select b.*, h.supplier_no, h.delivery_date from srm_delivery_body b " +
            "join srm_delivery_head h on b.delivery_no = h.delivery_no and b.ent = h.ent and b.site = h.site " +
            "where 1=1 " +
            "<choose>" +
            "<when test='receiptQty == null or receiptQty == \"\"'>and b.receipt_qty = '0'</when>" +
            "<otherwise>" +
            "<choose>" +
            "<when test='czf == \"gt\"'>and b.receipt_qty &gt; #{receiptQty}</when>" +
            "<when test='czf == \"lt\"'>and b.receipt_qty &lt; #{receiptQty}</when>" +
            "<when test='czf == \"ge\"'>and b.receipt_qty &gt;= #{receiptQty}</when>" +
            "<when test='czf == \"le\"'>and b.receipt_qty &lt;= #{receiptQty}</when>" +
            "<otherwise>and b.receipt_qty = #{receiptQty}</otherwise>" +
            "</choose>" +
            "</otherwise>" +
            "</choose>" +
            "<choose>" +
            "<when test='status != null and status != \"\"'>and h.status = #{status}</when>" +
            "<otherwise>and h.status &lt;&gt; 'X' and h.status &lt;&gt; '4' and h.status &lt;&gt; '1'</otherwise>" +
            "</choose>" +
            "<if test='deliveryDateStart != null and deliveryDateStart != \"\"'>and h.delivery_date &gt;= #{deliveryDateStart}</if>" +
            "<if test='deliveryDateEnd != null and deliveryDateEnd != \"\"'>and h.delivery_date &lt;= #{deliveryDateEnd}</if>" +
            "</script>")
    List<SrmDeliveryBody> findUnreceived(@Param("receiptQty") String receiptQty,
                                         @Param("czf") String czf,
                                         @Param("status") String status,
                                         @Param("deliveryDateStart") String deliveryDateStart,
                                         @Param("deliveryDateEnd") String deliveryDateEnd);

    /**
     * 按 delivery_no + delivery_seq + ent + site 查询一条送货单身
     */
    @Select("select * from srm_delivery_body where delivery_no = #{deliveryNo} and delivery_seq = #{deliverySeq} and ent = #{ent} and site = #{site}")
    SrmDeliveryBody findByNoAndSeq(@Param("deliveryNo") String deliveryNo,
                                   @Param("deliverySeq") String deliverySeq,
                                   @Param("ent") String ent,
                                   @Param("site") String site);

    /**
     * 更新收货数量和标记
     */
    @Update("update srm_delivery_body set receipt_qty = #{receiptQty}, remark2 = now() " +
            "where delivery_no = #{deliveryNo} and delivery_seq = #{deliverySeq} and ent = #{ent} and site = #{site}")
    int updateReceiptQty(@Param("deliveryNo") String deliveryNo,
                         @Param("deliverySeq") String deliverySeq,
                         @Param("receiptQty") String receiptQty,
                         @Param("ent") String ent,
                         @Param("site") String site);

    /**
     * 结清未收部分：把送货数量改为等于已收货数量（delivery_qty = receipt_qty）
     */
    @Update("update srm_delivery_body set delivery_qty = receipt_qty, remark2 = now() " +
            "where delivery_no = #{deliveryNo} and delivery_seq = #{deliverySeq} and ent = #{ent} and site = #{site}")
    int updateDeliveryQtyToReceiptQty(@Param("deliveryNo") String deliveryNo,
                                      @Param("deliverySeq") String deliverySeq,
                                      @Param("ent") String ent,
                                      @Param("site") String site);

    /**
     * 统计该送货单号下还有多少未收货的明细（receipt_qty=0）
     */
    @Select("select count(*) from srm_delivery_body where delivery_no = #{deliveryNo} and ent = #{ent} and site = #{site} and receipt_qty = 0")
    int countUnreceivedByDeliveryNo(@Param("deliveryNo") String deliveryNo,
                                    @Param("ent") String ent,
                                    @Param("site") String site);

    /**
     * 删除一条送货单身记录
     */
    @Delete("delete from srm_delivery_body " +
            "where ent = #{ent} and site = #{site} " +
            "and delivery_no = #{deliveryNo} and delivery_seq = #{deliverySeq} " +
            "and purchase_no = #{purchaseNo} and purchase_seq = #{purchaseSeq}")
    int deleteByKeys(@Param("ent") String ent,
                     @Param("site") String site,
                     @Param("deliveryNo") String deliveryNo,
                     @Param("deliverySeq") String deliverySeq,
                     @Param("purchaseNo") String purchaseNo,
                     @Param("purchaseSeq") String purchaseSeq);

    /**
     * 统计该送货单号下的记录总数
     */
    @Select("select count(*) from srm_delivery_body where ent = #{ent} and site = #{site} and delivery_no = #{deliveryNo}")
    int countByDeliveryNo(@Param("ent") String ent,
                          @Param("site") String site,
                          @Param("deliveryNo") String deliveryNo);

    /**
     * 统计该送货单号下未完全收货的记录（delivery_qty != receipt_qty）
     */
    @Select("select count(*) from srm_delivery_body where ent = #{ent} and site = #{site} and delivery_no = #{deliveryNo} and delivery_qty != receipt_qty")
    int countNotFullyReceived(@Param("ent") String ent,
                              @Param("site") String site,
                              @Param("deliveryNo") String deliveryNo);
}
