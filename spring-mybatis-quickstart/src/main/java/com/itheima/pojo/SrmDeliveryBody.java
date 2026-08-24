package com.itheima.pojo;

/**
 * MySQL srm_delivery_body 表映射
 */
public class SrmDeliveryBody {

    private String deliveryNo;
    private String purchaseNo;
    private String purchaseSeq;
    private String receiptQty;
    private String id;
    private String createTime;
    private String updateTime;
    private String deliveryId;
    private String materialCode;
    private String materialName;
    private String deliveryQty;
    private String itemNo;
    private String unit;
    private String remark;
    private java.util.Date shippingDate;
    private String deliverySeq;
    private String ent;
    private String site;
    private String remark2;
    private String supplierNo;

    public String getDeliveryNo() { return deliveryNo; }
    public void setDeliveryNo(String deliveryNo) { this.deliveryNo = deliveryNo; }

    public String getPurchaseNo() { return purchaseNo; }
    public void setPurchaseNo(String purchaseNo) { this.purchaseNo = purchaseNo; }

    public String getPurchaseSeq() { return purchaseSeq; }
    public void setPurchaseSeq(String purchaseSeq) { this.purchaseSeq = purchaseSeq; }

    public String getReceiptQty() { return receiptQty; }
    public void setReceiptQty(String receiptQty) { this.receiptQty = receiptQty; }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }

    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }

    public String getDeliveryId() { return deliveryId; }
    public void setDeliveryId(String deliveryId) { this.deliveryId = deliveryId; }

    public String getMaterialCode() { return materialCode; }
    public void setMaterialCode(String materialCode) { this.materialCode = materialCode; }

    public String getMaterialName() { return materialName; }
    public void setMaterialName(String materialName) { this.materialName = materialName; }

    public String getDeliveryQty() { return deliveryQty; }
    public void setDeliveryQty(String deliveryQty) { this.deliveryQty = deliveryQty; }

    public String getItemNo() { return itemNo; }
    public void setItemNo(String itemNo) { this.itemNo = itemNo; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    public java.util.Date getShippingDate() { return shippingDate; }
    public void setShippingDate(java.util.Date shippingDate) { this.shippingDate = shippingDate; }

    private java.util.Date deliveryDate;
    public java.util.Date getDeliveryDate() { return deliveryDate; }
    public void setDeliveryDate(java.util.Date deliveryDate) { this.deliveryDate = deliveryDate; }

    public String getDeliverySeq() { return deliverySeq; }
    public void setDeliverySeq(String deliverySeq) { this.deliverySeq = deliverySeq; }

    public String getEnt() { return ent; }
    public void setEnt(String ent) { this.ent = ent; }

    public String getSite() { return site; }
    public void setSite(String site) { this.site = site; }

    public String getRemark2() { return remark2; }
    public void setRemark2(String remark2) { this.remark2 = remark2; }

    public String getSupplierNo() { return supplierNo; }
    public void setSupplierNo(String supplierNo) { this.supplierNo = supplierNo; }
}
