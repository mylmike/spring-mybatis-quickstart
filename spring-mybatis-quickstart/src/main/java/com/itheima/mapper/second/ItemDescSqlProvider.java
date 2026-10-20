package com.itheima.mapper.second;

import org.apache.ibatis.jdbc.SQL;

import java.util.Map;

/**
 * 品号描述 imaal_t 查询 SQL 构造器（多语言品名/规格）
 */
public class ItemDescSqlProvider {

    /**
     * 按 账套 + 语言 + 品号 查询品名/规格
     * 参数：ent(必填)、lang(必填，语言，如 zh_CN)、item(必填，即 imaal001)
     */
    public String queryByItem(final Map<String, Object> params) {
        return new SQL() {{
            SELECT("imaal003, imaal004");
            FROM("imaal_t");
            WHERE("imaalent = #{ent}");
            WHERE("imaal002 = #{lang}");
            WHERE("imaal001 = #{item}");
        }}.toString();
    }

    /**
     * 按 品号(精确) / 品名(like) / 规格(like) 查询物料品名规格
     * 固定条件：imaalent(账套) + imaal002(语言, 默认 zh_CN)
     * 可选条件：imaal001(品号) 精确；imaal003(品名) like '%v%'；imaal004(规格) like '%v%'
     *          空值则忽略对应条件（由 Controller 保证至少提供一个）
     */
    public String search(final Map<String, Object> params) {
        return new SQL() {{
            SELECT("imaal001, imaal003, imaal004");
            FROM("imaal_t");
            WHERE("imaalent = #{imaalent}");
            WHERE("imaal002 = #{imaal002}");
            if (hasText(params.get("imaal001"))) {
                WHERE("imaal001 = #{imaal001}");
            }
            if (hasText(params.get("imaal003"))) {
                WHERE("imaal003 like '%' || #{imaal003} || '%'");
            }
            if (hasText(params.get("imaal004"))) {
                WHERE("imaal004 like '%' || #{imaal004} || '%'");
            }
            WHERE("rownum <= 20");
        }}.toString();
    }

    /** 判断参数是否为有效非空文本（null / 空串 / 纯空白 均返回 false） */
    private boolean hasText(Object val) {
        return val != null && !String.valueOf(val).trim().isEmpty();
    }
}
