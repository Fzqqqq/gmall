package com.atguigu.gmall.pms.vo;

import com.atguigu.gmall.pms.vo.AttrValueVo;
import lombok.Data;

import java.util.List;

/**
 * @author fzqqq
 * @create 2020-10-12 18:32
 */
@Data
public class ItemGroupVo {
    private String groupName;
    private List<AttrValueVo> attrValues;


}
